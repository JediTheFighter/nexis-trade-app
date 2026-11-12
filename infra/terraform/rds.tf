resource "aws_db_subnet_group" "this" {
  name       = "nexis"
  subnet_ids = aws_subnet.public[*].id
}

resource "aws_db_instance" "this" {
  identifier                  = "nexis"
  engine                      = "postgres"
  engine_version              = var.db_engine_version
  instance_class              = "db.t3.micro"
  allocated_storage           = 20
  db_name                     = var.db_name
  username                    = var.db_username
  manage_master_user_password = true
  db_subnet_group_name        = aws_db_subnet_group.this.name
  vpc_security_group_ids      = [aws_security_group.db.id]
  publicly_accessible         = false
  storage_encrypted           = true
  backup_retention_period     = 1
  skip_final_snapshot         = true
}
