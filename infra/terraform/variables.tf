variable "aws_region" {
  type        = string
  description = "Region for ECR, ECS, RDS, and the load balancer."
  default     = "us-east-1"
}

variable "db_name" {
  type        = string
  description = "Postgres database name created on the RDS instance."
  default     = "nexis"
}

variable "db_username" {
  type        = string
  description = "RDS master username. The password is generated into Secrets Manager."
  default     = "nexis"
}

variable "db_engine_version" {
  type        = string
  description = "RDS Postgres engine version. Change this if the region no longer offers it."
  default     = "16"
}
