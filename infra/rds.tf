module "db" {
  source = "git::https://github.com/fiap-postech-soat-lf/fiap-soat-mecanica-db.git//modules/rds-postgres?ref=v1"

  name_prefix = "mecanica-identity"
  # "identity" e palavra reservada para DBName no RDS Postgres (InvalidParameterValue).
  db_name                    = "identity_db"
  vpc_id                     = data.aws_ssm_parameter.vpc_id.insecure_value
  subnet_ids                 = split(",", data.aws_ssm_parameter.subnet_ids.insecure_value)
  allowed_security_group_ids = [data.aws_ssm_parameter.node_security_group_id.insecure_value]
}
