# Testes do infra/ com o provider aws mockado: nao precisa de credencial AWS nem do EKS.
# O tls e o random rodam de verdade (geram o par e a senha localmente).

mock_provider "aws" {
  override_data {
    target = data.aws_ssm_parameter.vpc_id
    values = {
      insecure_value = "vpc-0123456789abcdef0"
    }
  }

  override_data {
    target = data.aws_ssm_parameter.subnet_ids
    values = {
      insecure_value = "subnet-0aaaaaaaaaaaaaaaa,subnet-0bbbbbbbbbbbbbbbb"
    }
  }

  override_data {
    target = data.aws_ssm_parameter.node_security_group_id
    values = {
      insecure_value = "sg-0123456789abcdef0"
    }
  }
}

# O servico-cd.yml transforma as chaves em ConfigMap e Secret com --from-env-file.
run "contrato_infra_cd" {
  command = apply

  assert {
    condition     = toset(keys(output.app_config)) == toset(["SPRING_DATASOURCE_URL", "AWS_REGION", "JWT_PUBLIC_KEY"])
    error_message = "app_config deve ter SPRING_DATASOURCE_URL, AWS_REGION e JWT_PUBLIC_KEY."
  }

  assert {
    condition     = toset(keys(output.app_secrets)) == toset(["SPRING_DATASOURCE_USERNAME", "SPRING_DATASOURCE_PASSWORD"])
    error_message = "app_secrets deve ter SPRING_DATASOURCE_USERNAME e SPRING_DATASOURCE_PASSWORD."
  }

  assert {
    condition     = startswith(output.app_config.SPRING_DATASOURCE_URL, "jdbc:postgresql://") && endswith(output.app_config.SPRING_DATASOURCE_URL, "/identity?sslmode=require")
    error_message = "SPRING_DATASOURCE_URL deve vir do jdbc_url do modulo -db (banco identity, sslmode=require)."
  }

  assert {
    condition     = output.ecr_repository_url == aws_ecr_repository.this.repository_url
    error_message = "ecr_repository_url deve ser a URL do repositorio ECR do servico."
  }
}

# --from-env-file nao aceita valor multilinha.
run "chave_publica_escapada_numa_linha" {
  command = apply

  assert {
    condition     = !strcontains(output.app_config.JWT_PUBLIC_KEY, "\n")
    error_message = "JWT_PUBLIC_KEY nao pode ter quebra de linha real."
  }

  assert {
    condition     = startswith(output.app_config.JWT_PUBLIC_KEY, "-----BEGIN PUBLIC KEY-----\\n")
    error_message = "JWT_PUBLIC_KEY deve ser o PEM da chave publica com \\n escapado."
  }

  assert {
    condition     = replace(output.app_config.JWT_PUBLIC_KEY, "\\n", "\n") == aws_ssm_parameter.jwt_public_key.insecure_value
    error_message = "A chave do ConfigMap e a do SSM (lida pelo -auth) devem ser a mesma."
  }
}

run "par_jwt_no_secret_e_publica_no_ssm" {
  command = apply

  assert {
    condition     = tls_private_key.jwt.algorithm == "RSA" && tls_private_key.jwt.rsa_bits == 2048
    error_message = "O par JWT deve ser RSA 2048 (RS256)."
  }

  assert {
    condition     = aws_secretsmanager_secret.jwt.name == "mecanica-identity-jwt" && aws_secretsmanager_secret.jwt.recovery_window_in_days == 0
    error_message = "Secret mecanica-identity-jwt com recovery_window_in_days = 0."
  }

  assert {
    condition     = toset(keys(jsondecode(aws_secretsmanager_secret_version.jwt.secret_string))) == toset(["privateKeyPem", "publicKeyPem"])
    error_message = "O secret deve guardar privateKeyPem e publicKeyPem."
  }

  assert {
    condition     = startswith(jsondecode(aws_secretsmanager_secret_version.jwt.secret_string).privateKeyPem, "-----BEGIN PRIVATE KEY-----")
    error_message = "privateKeyPem deve estar em PKCS#8."
  }

  assert {
    condition     = aws_ssm_parameter.jwt_public_key.name == "/mecanica/identity/jwt-public-key" && aws_ssm_parameter.jwt_public_key.type == "String"
    error_message = "Chave publica em /mecanica/identity/jwt-public-key (String)."
  }

  assert {
    condition     = aws_ssm_parameter.jwt_secret_arn.name == "/mecanica/identity/jwt-secret-arn" && aws_ssm_parameter.jwt_secret_arn.insecure_value == aws_secretsmanager_secret.jwt.arn
    error_message = "ARN do secret em /mecanica/identity/jwt-secret-arn."
  }
}

run "ecr_destrutivel_e_mutavel" {
  command = apply

  assert {
    condition     = aws_ecr_repository.this.name == "mecanica-identity"
    error_message = "Repositorio ECR deve se chamar mecanica-identity."
  }

  assert {
    condition     = aws_ecr_repository.this.force_delete && aws_ecr_repository.this.image_tag_mutability == "MUTABLE"
    error_message = "ECR com force_delete e tags MUTABLE (regras 3.3 do plano da F2)."
  }

  assert {
    condition     = jsondecode(aws_ecr_lifecycle_policy.this.policy).rules[0].selection.countNumber == 10
    error_message = "A lifecycle policy deve manter as 10 imagens mais recentes."
  }
}
