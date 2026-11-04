# Contrato infra -> CD (secao 3.6 do plano da F1): o servico-cd.yml le estes tres outputs.

output "ecr_repository_url" {
  description = "Repositorio da imagem do servico."
  value       = aws_ecr_repository.this.repository_url
}

output "app_config" {
  description = "Vira o ConfigMap mecanica-identity-config."
  value = {
    SPRING_DATASOURCE_URL = module.db.jdbc_url
    AWS_REGION            = var.aws_region
    # --from-env-file nao aceita valor multilinha; o JwtTokenValidator aceita PEM com \n escapado.
    JWT_PUBLIC_KEY = replace(tls_private_key.jwt.public_key_pem, "\n", "\\n")
  }
}

output "app_secrets" {
  description = "Vira o Secret mecanica-identity-secret."
  sensitive   = true
  value = {
    SPRING_DATASOURCE_USERNAME = module.db.username
    SPRING_DATASOURCE_PASSWORD = module.db.password
    # F3: JWT_PRIVATE_KEY (escapada do mesmo jeito)
  }
}
