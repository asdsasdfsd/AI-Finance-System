######################
# Root main.tf (EC2 + RDS + VPC via AWS)
######################
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 4.0"
    }
  }
  required_version = ">= 1.0"
}

provider "aws" {
  region = var.aws_region
}

# Create VPC, Subnets, Security Groups
module "vpc" {
  source     = "./modules/vpc"
  cidr_block = "10.0.0.0/16"
}

# Backend EC2
module "backend_ec2" {
  source             = "./modules/ec2"
  service_name       = "backend"
  docker_image       = "${var.docker_username}/ai-finance-backend:${var.backend_image_tag}"
  container_port     = 8080
  host_port          = 8080
  instance_type      = var.instance_type
  key_name           = var.key_name
  subnet_id          = module.vpc.public_subnet_ids[0]
  security_group_id  = module.vpc.ec2_security_group_id
  ami_id             = var.ami_id
}

# Frontend EC2
module "frontend_ec2" {
  source             = "./modules/ec2"
  service_name       = "frontend"
  docker_image       = "${var.docker_username}/ai-finance-frontend:${var.frontend_image_tag}"
  container_port     = 3000
  host_port          = 80
  instance_type      = var.instance_type
  key_name           = var.key_name
  subnet_id          = module.vpc.public_subnet_ids[1]
  security_group_id  = module.vpc.ec2_security_group_id
  ami_id             = var.ami_id
}

# RDS MySQL
module "rds" {
  source                 = "./modules/rds"
  db_name                = var.db_name
  db_user                = var.db_user
  db_password            = var.db_password
  db_instance_class      = var.db_instance_class
  subnet_ids             = module.vpc.private_subnet_ids
  vpc_security_group_ids = [module.vpc.rds_security_group_id]
}