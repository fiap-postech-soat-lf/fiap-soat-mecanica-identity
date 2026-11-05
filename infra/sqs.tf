# Fila de notificacoes do Identity (tabela 3.4 da F1): consome os eventos do OS e tem DLQ
# com redrive apos 5 recebimentos. Destrutivel sem sobra (regras 3.3 do plano da F2).
resource "aws_sqs_queue" "notificacoes_dlq" {
  name                      = "mecanica-identity-notificacoes-dlq"
  message_retention_seconds = 1209600
}

resource "aws_sqs_queue" "notificacoes" {
  name                       = "mecanica-identity-notificacoes"
  visibility_timeout_seconds = 30
  message_retention_seconds  = 345600

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.notificacoes_dlq.arn
    maxReceiveCount     = 5
  })
}

# F4-a: a assinatura os-eventos -> identity-notificacoes e criada pelo infra/ do OS (ADR-F4-03),
# que le o ARN desta fila no SSM. A policy fica aqui, sempre presente, com o ARN do topico montado
# a partir do nome: o Identity aplica antes do OS e nao depende do topico existir.
resource "aws_sqs_queue_policy" "notificacoes" {
  queue_url = aws_sqs_queue.notificacoes.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid       = "PermiteOsEventos"
      Effect    = "Allow"
      Principal = { Service = "sns.amazonaws.com" }
      Action    = "sqs:SendMessage"
      Resource  = aws_sqs_queue.notificacoes.arn
      Condition = {
        ArnEquals = {
          "aws:SourceArn" = "arn:aws:sns:${var.aws_region}:${data.aws_caller_identity.atual.account_id}:mecanica-os-eventos"
        }
      }
    }]
  })
}

# Lido pelo infra/ do OS (tabela 3.4 da F4).
resource "aws_ssm_parameter" "notificacoes_queue_arn" {
  name           = "/mecanica/identity/notificacoes-queue-arn"
  type           = "String"
  insecure_value = aws_sqs_queue.notificacoes.arn
}
