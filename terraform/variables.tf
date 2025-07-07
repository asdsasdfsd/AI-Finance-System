variable "aws_region" {
  description = "ap-southeast-1"
  type        = string
}

variable "docker_username" {
  description = "tigerwk"
  type        = string
}

variable "backend_image_tag" {
  description = "latest"
  type        = string
}

variable "frontend_image_tag" {
  description = "latest"
  type        = string
}

variable "instance_type" {
  description = "t3.micro"
  type        = string
}

variable "key_name" {
  description = "terrform-key"
  type        = string
}

variable "ami_id" {
  description = "Amazon Linux 2 的 AMI ID，推荐：ap-southeast-1 区域 ami-0e8a34246278c21e4"
  type        = string
}

variable "db_name" {
  type        = string
  description = "testdb"
}

variable "db_user" {
  type        = string
  description = "admin"
}

variable "db_password" {
  type        = string
  description = "761127wk"
}

variable "db_instance_class" {
  type        = string
  description = "db.t3.micro"
}
