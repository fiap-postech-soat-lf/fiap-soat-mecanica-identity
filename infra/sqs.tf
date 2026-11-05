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
