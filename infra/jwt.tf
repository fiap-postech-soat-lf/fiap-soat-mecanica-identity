# Par RS256 do Identity (F2-d): gerado a cada janela, entao os tokens de janelas anteriores
# deixam de valer. A chave privada fica tambem no state (bucket privado e criptografado,
# aceito na ADR-F4-07).
resource "tls_private_key" "jwt" {
  algorithm = "RSA"
  rsa_bits  = 2048
}

resource "aws_secretsmanager_secret" "jwt" {
  name                    = "mecanica-identity-jwt"
  description             = "Par RS256 do Identity (privateKeyPem em PKCS#8 e publicKeyPem)."
  recovery_window_in_days = 0 # destroy diario: o nome nao pode ficar reservado por 7 dias
}

resource "aws_secretsmanager_secret_version" "jwt" {
  secret_id = aws_secretsmanager_secret.jwt.id
  secret_string = jsonencode({
    privateKeyPem = tls_private_key.jwt.private_key_pem_pkcs8
    publicKeyPem  = tls_private_key.jwt.public_key_pem
  })
}

# Lido pelo scripts/smoke-borda.sh do -k8s, que assina o token de teste.
resource "aws_ssm_parameter" "jwt_secret_arn" {
  name           = "/mecanica/identity/jwt-secret-arn"
  type           = "String"
  insecure_value = aws_secretsmanager_secret.jwt.arn
}

# Lido pelo infra/ do -auth, que passa a chave como env da Lambda Authorizer (F2-c).
# So a chave publica: nenhum segredo vai para o SSM.
resource "aws_ssm_parameter" "jwt_public_key" {
  name           = "/mecanica/identity/jwt-public-key"
  type           = "String"
  insecure_value = tls_private_key.jwt.public_key_pem
}
