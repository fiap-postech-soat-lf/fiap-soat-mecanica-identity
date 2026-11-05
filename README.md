# fiap-soat-mecanica-identity

Microsserviço **identity** da **Fase 4** da Oficina Mecânica do Braia (Tech Challenge FIAP SOAT).

> 🚧 Gerado a partir do esqueleto `fiap-soat-mecanica-k8s/templates/servico-base/`. As seções com *TODO* são preenchidas na fase do serviço e revisadas na F9.

## Responsabilidade

Dono dos dados de **clientes** (PF e PJ), **veículos** e **funcionários** (usuários e cargos), e único **emissor de JWT** da Fase 4. Também envia as notificações de mudança de situação da OS ao cliente, porque o e-mail é dado dele. Banco próprio: `identity_db` (PostgreSQL).

## Arquitetura

- Spring Boot 3.5, Java 21 (virtual threads), arquitetura hexagonal: `domain/`, `application/`, `adapters/in|out/`, `config/`.
- Autenticação: JWT RS256 emitido pelo Identity, validado aqui também (defesa em profundidade). Públicos só `POST /auth/login`, `POST /auth/cpf`, `/actuator/health/**` e o Swagger.
- Mensageria: AWS SDK v2 direto (SNS e SQS), com outbox transacional e consumidor idempotente.
- Observabilidade: logs JSON com `correlationId`, métricas OTLP e agente OpenTelemetry injetado no EKS (Datadog).

*TODO*: diagrama do serviço.

## API

Rotas e perfis (contrato em `docs/contratos/rotas.md` do `-k8s`):

| Rota | Perfil |
|---|---|
| `POST /auth/login`, `POST /auth/cpf` | público |
| `GET /clientes/me` | CLIENTE |
| `POST /clientes`, `PATCH /clientes/{id}`, `GET /clientes/{id}`, `GET /clientes/por-cpf/{cpf}`, `GET /clientes/por-cnpj/{cnpj}`, `GET /clientes/por-usuario/{usuarioId}` | ATENDENTE |
| `POST /veiculos`, `PATCH /veiculos/{id}`, `GET /veiculos/por-placa/{placa}`, `GET /veiculos/por-cliente/{clienteId}` | ATENDENTE |
| `GET /veiculos/{id}` | ATENDENTE ou MECANICO (o OS repassa o JWT de quem chamou) |
| `POST /usuarios` | qualquer funcionário |

Logins: ambos devolvem `{accessToken, tokenType: "Bearer", expiresIn: 3600}`. `POST /auth/login` recebe `{email, senha}` (funcionário); `POST /auth/cpf` recebe `{cpf}` (cliente PF ativo). Erros sem enumeração: 401 `CREDENCIAIS_INVALIDAS` / `ACESSO_NEGADO`, 400 `CPF_INVALIDO`, 503 `EMISSOR_INDISPONIVEL` (sem `JWT_PRIVATE_KEY`). O formato do token está em `docs/contratos/jwt.md`. Se a chave privada não formar par com a pública, a aplicação não sobe.

Swagger: `/identity/swagger-ui.html` (via Traefik) ou `/swagger-ui.html` (local).

## Mensagens

**Consome** `SituacaoOrdemServicoAlterada` e `OrdemServicoCancelada` (v1) da fila `mecanica-identity-notificacoes` (assinatura de `os-eventos`). Busca o cliente pelo `clienteId` e registra `notificacao_os_enviada` com o e-mail mascarado (adapter de log; SMTP é evolução). Cliente inexistente: log `notificacao_sem_destinatario`, sem DLQ. **Não publica** eventos.

Os exemplos do produtor foram copiados do `-os` (`exemplos/*.v1.json` e os schemas) para `src/test/resources/contratos-consumidos/`. Se o OS mudar o contrato, a cópia precisa ser atualizada.

## Seed e logins de teste

Migration `V5__seed.sql`, com os mesmos UUIDs usados pelos seeds dos outros serviços:

| Tipo | UUID | Login |
|---|---|---|
| ATENDENTE | `11111111-1111-1111-1111-111111111111` | `atendente@mecanica.test` |
| MECANICO | `22222222-2222-2222-2222-222222222222` | `mecanico@mecanica.test` |
| ALMOXARIFE | `aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa` | `almoxarife@mecanica.test` |
| Cliente PF | `33333333-3333-3333-3333-333333333333` | CPF `52998224725` |
| Cliente PJ | `33333333-3333-3333-3333-333333333334` | CNPJ `11222333000181` (não loga por CPF) |
| Veículo do PF | `44444444-4444-4444-4444-444444444444` | placa `ABC1D23` |
| Veículo do PJ | `44444444-4444-4444-4444-444444444445` | placa `XYZ1234` |

A senha dos funcionários é `Senha@123` (só o hash BCrypt está no repositório). Ela fica documentada porque o ambiente é acadêmico. O BCrypt custa ~0,5 s por login com 250m de CPU, o que é aceito.

## Infraestrutura

`infra/` (Terraform, aplicado pelo `servico-cd.yml@v1`): ECR, RDS `identity_db`, par RS256, fila `identity-notificacoes` com DLQ e assinatura condicional de `os-eventos`. A chave privada vai ao pod só pelo Secret (`JWT_PRIVATE_KEY`). Detalhes e contrato com o CD em [`infra/README.md`](infra/README.md).

Como o esqueleto funciona:

- **Publicar:** dentro da transação do caso de uso, `outbox.registrar("<destino>", mensagemFactory.criar("<Tipo>", 1, payload))`. O `OutboxPublisher` publica a cada 1 s (lotes de 50, `FOR UPDATE SKIP LOCKED`). O `MensagemFactory` preenche `messageId`, `correlationId`, `causationId` e `traceparent`.
- **Consumir:** um bean `MensagemHandler` por tipo. O `SqsConsumidor` faz long polling nas filas de `app.messaging.consumidor.filas`, e o `ProcessadorMensagemSql` garante a idempotência pelo `messageId` na mesma transação do handler. Exceção no handler devolve a mensagem para a fila (DLQ depois de 5 recebimentos).
- **Configurar:** `app.messaging.destinos.<nome>` (ARN do tópico ou URL da fila), `app.messaging.consumidor.filas` e `app.messaging.dlqs` (gauge `dlq.depth{fila}`), com os valores vindos do ConfigMap (ver `infra/README.md`).

## Como rodar localmente

```bash
docker compose -f ../fiap-soat-mecanica-k8s/local/docker-compose.yml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
curl localhost:8081/actuator/health
```

Token de dev: `../fiap-soat-mecanica-k8s/local/jwt/gerar-token-dev.sh CLIENTE`.

## Testes e qualidade

```bash
./mvnw verify   # unitários + integração (Testcontainers), JaCoCo ≥ 80% de linhas
```

O CI (`ci / build`) roda os testes e a análise no SonarCloud (`fiap-postech-soat-lf_fiap-soat-mecanica-identity`) com espera do Quality Gate.

## Deploy

O CD (`servico-cd.yml@v1`) roda após o CI verde na `main`: `terraform apply` do `infra/` → imagem no ECR → ConfigMap/Secret → `kubectl apply` dos manifestos `k8s/` → rollout e smoke do health. Com o EKS desligado, termina verde sem deploy.

## Decisões

ADRs do serviço em `docs/adr/`. Decisões transversais no `-k8s`: [ADR-F4-07](https://github.com/fiap-postech-soat-lf/fiap-soat-mecanica-k8s/blob/main/docs/arquitetura-fase4/adr/ADR-F4-07-jwt-rs256.md) (JWT RS256) e [ADR-F4-12](https://github.com/fiap-postech-soat-lf/fiap-soat-mecanica-k8s/blob/main/docs/arquitetura-fase4/adr/ADR-F4-12-notificacoes-no-identity.md) (notificações no Identity).
