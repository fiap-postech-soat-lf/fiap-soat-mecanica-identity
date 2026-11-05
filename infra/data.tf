# Publicados pelo -k8s (cluster.yml, ADR-F4-14). Sem eles o servico-cd nem chega aqui:
# o porteiro pula o deploy com o EKS desligado.
data "aws_ssm_parameter" "vpc_id" {
  name = "/mecanica/k8s/vpc-id"
}

data "aws_ssm_parameter" "subnet_ids" {
  name = "/mecanica/k8s/subnet-ids"
}

data "aws_ssm_parameter" "node_security_group_id" {
  name = "/mecanica/k8s/node-security-group-id"
}

data "aws_caller_identity" "atual" {}
