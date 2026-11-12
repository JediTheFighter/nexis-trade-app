resource "aws_cloudwatch_log_group" "api" {
  name              = "/ecs/nexis-api"
  retention_in_days = 14
}

resource "aws_cloudwatch_log_group" "web" {
  name              = "/ecs/nexis-web"
  retention_in_days = 14
}
