output "alb_dns_name" {
  description = "Open this hostname in a browser after the web service is healthy."
  value       = aws_lb.this.dns_name
}

output "ecr_api_url" {
  value = aws_ecr_repository.api.repository_url
}

output "ecr_web_url" {
  value = aws_ecr_repository.web.repository_url
}

output "ecs_cluster" {
  value = aws_ecs_cluster.this.name
}

output "aws_region" {
  value = var.aws_region
}
