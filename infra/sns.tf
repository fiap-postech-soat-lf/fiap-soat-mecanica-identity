# F3-h: o topico os-eventos e do OS, que so ganha infra/ na F4. A assinatura existe apenas
# quando o OS publica /mecanica/os/os-eventos-topic-arn. Usar o data "by_path" evita o
# ParameterNotFound de um data "aws_ssm_parameter" direto; o count e conhecido no plan.
data "aws_ssm_parameters_by_path" "os" {
  path = "/mecanica/os"
}

locals {
  os_parametros = nonsensitive(zipmap(
    data.aws_ssm_parameters_by_path.os.names,
    data.aws_ssm_parameters_by_path.os.values,
  ))
  os_eventos_topic_arn = lookup(local.os_parametros, "/mecanica/os/os-eventos-topic-arn", null)
}

resource "aws_sns_topic_subscription" "os_eventos" {
  count = local.os_eventos_topic_arn == null ? 0 : 1

  topic_arn            = local.os_eventos_topic_arn
  protocol             = "sqs"
  endpoint             = aws_sqs_queue.notificacoes.arn
  raw_message_delivery = true
}

resource "aws_sqs_queue_policy" "notificacoes" {
  count = local.os_eventos_topic_arn == null ? 0 : 1

  queue_url = aws_sqs_queue.notificacoes.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid       = "PermiteOsEventos"
      Effect    = "Allow"
      Principal = { Service = "sns.amazonaws.com" }
      Action    = "sqs:SendMessage"
      Resource  = aws_sqs_queue.notificacoes.arn
      Condition = { ArnEquals = { "aws:SourceArn" = local.os_eventos_topic_arn } }
    }]
  })
}
