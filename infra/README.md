# infra/ do serviço identity

Terraform do Identity, aplicado pelo `servico-cd.yml@v1` antes do build da imagem. A F2 deixou o mínimo para o esqueleto subir no EKS; a F3 acrescentou a fila `identity-notificacoes` (+ DLQ), a `JWT_PRIVATE_KEY` no `app_secrets` e as chaves de mensageria no `app_config`. Na F4 a assinatura `os-eventos → identity-notificacoes` passou para o `infra/` do OS (F4-a).

## Recursos

| Arquivo | Recursos |
|---|---|
| `data.tf` | Lê do SSM a VPC, as subnets e o SG dos nodes publicados pelo `-k8s` (`/mecanica/k8s/*`) |
| `jwt.tf` | Par RSA 2048 (`tls_private_key`); secret `mecanica-identity-jwt` com `privateKeyPem` (PKCS#8) e `publicKeyPem`; SSM `/mecanica/identity/jwt-secret-arn` e `/mecanica/identity/jwt-public-key` |
| `ecr.tf` | Repositório `mecanica-identity` (`MUTABLE`, `force_delete`, scan on push) e lifecycle que mantém as 10 imagens mais recentes |
| `rds.tf` | Módulo `fiap-soat-mecanica-db//modules/rds-postgres?ref=v1`: RDS, secret e SG `mecanica-identity-db`, banco `identity_db` |
| `sqs.tf` | Fila `mecanica-identity-notificacoes` (`visibility_timeout` 30 s, retenção 4 dias) e DLQ `-dlq`, com redrive após 5 recebimentos |
| `sqs.tf` (policy e SSM) | Policy da fila, **sempre presente**, permitindo `sqs:SendMessage` do tópico `arn:aws:sns:<região>:<conta>:mecanica-os-eventos` (ARN montado a partir do nome, sem ler nada do OS); SSM `/mecanica/identity/notificacoes-queue-arn`, lido pelo `infra/` do OS |

- O par JWT é gerado a cada janela (o ambiente é destruído e recriado), então os tokens de janelas anteriores deixam de valer. A chave privada fica também no state (bucket privado e criptografado, ADR-F4-07).
- O `-auth` lê `/mecanica/identity/jwt-public-key` e passa a chave à Lambda Authorizer como variável de ambiente. Por isso o `-auth` é aplicado depois do Identity (`subir-ambiente.sh`).
- Tudo é destrutível sem sobra: secret com `recovery_window_in_days = 0`, ECR com `force_delete` e RDS sem snapshot final (regras 3.3 do plano da F2).

## Comandos

```bash
terraform -chdir=infra init -backend=false
terraform -chdir=infra validate
terraform -chdir=infra test   # provider aws mockado, sem credencial
```

## Contrato com o CD (`servico-cd.yml@v1`)

O `infra/outputs.tf` precisa expor:

| Output | Tipo | Uso no CD |
|---|---|---|
| `ecr_repository_url` | `string` | Repositório da imagem (`mecanica-identity`) |
| `app_config` | `map(string)` | Vira o ConfigMap `mecanica-identity-config` |
| `app_secrets` | `map(string)`, `sensitive = true` | Vira o Secret `mecanica-identity-secret` |

Valores atuais:

| Output | Chave | Valor |
|---|---|---|
| `app_config` | `SPRING_DATASOURCE_URL` | `jdbc_url` do módulo (`sslmode=require`) |
| `app_config` | `AWS_REGION` | `var.aws_region` |
| `app_config` | `JWT_PUBLIC_KEY` | PEM da chave pública com `\n` escapado (`--from-env-file` não aceita valor multilinha) |
| `app_config` | `APP_MESSAGING_CONSUMIDOR_FILAS`, `APP_MESSAGING_DLQS` | URL da fila `identity-notificacoes` e da sua DLQ |
| `app_secrets` | `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Usuário e senha master do RDS |
| `app_secrets` | `JWT_PRIVATE_KEY` | PEM PKCS#8 com `\n` escapado. Só no Secret, nunca no ConfigMap |

- Backend S3 configurado pelo CD: bucket `mecanica-tfstate-<accountId>`, key `fiap-soat-mecanica-identity/terraform.tfstate`. O `backend.tf` fica só com `backend "s3" {}`.
- Provider AWS `~> 6.0`, `required_version = ">= 1.10.0, < 2.0.0"`.
- O CD passa `TF_VAR_mp_access_token` e `TF_VAR_mp_webhook_secret`. Variáveis não declaradas são ignoradas.
- Nomes de filas, tópicos, RDS e SSM seguem a tabela 3.4 do plano da F1.

> **A assinatura é do OS (F4-a).** O `infra/` do OS cria `os-eventos → identity-notificacoes` (raw) lendo o ARN da fila em `/mecanica/identity/notificacoes-queue-arn`. É uma exceção à regra "o consumidor é dono da assinatura": na ordem antiga, o OS precisava da chave pública do JWT, que só existe depois do apply do Identity, e criava um ciclo. Veja a [ADR-F4-03](https://github.com/fiap-postech-soat-lf/fiap-soat-mecanica-k8s/blob/main/docs/arquitetura-fase4/adr/ADR-F4-03-sns-sqs-sdk-v2.md). A F3-h (assinatura condicional) foi substituída.

### Chaves de mensageria no `app_config`

As chaves do `app_config` viram variáveis de ambiente do pod (`envFrom`).

| Chave | Valor | Propriedade |
|---|---|---|
| `APP_MESSAGING_CONSUMIDOR_FILAS` | URLs das filas consumidas, separadas por vírgula | `app.messaging.consumidor.filas` |
| `APP_MESSAGING_DLQS` | URLs das DLQs dessas filas | `app.messaging.dlqs` (gauge `dlq.depth`) |
| `MESSAGING_DESTINO_<NOME>` | ARN do tópico ou URL da fila | declarada no `application.properties` do serviço como `app.messaging.destinos.<nome>=${MESSAGING_DESTINO_<NOME>:}` |

Os destinos usam variável explícita porque a ligação relaxada do Spring não preserva o `-` das chaves de mapa vindas de variáveis de ambiente (`execucao-comandos` viraria `execucao.comandos`). A role do pod precisa de `sns:Publish` nos tópicos e de `sqs:SendMessage`, `ReceiveMessage`, `DeleteMessage` e `GetQueueAttributes` nas filas.
