module "backend_ec2" {
  source             = "./modules/ec2"
  service_name       = "backend"
  docker_image       = "${var.docker_username}/backend:${var.backend_image_tag}"
  container_port     = 8080
  host_port          = 8080
  instance_type      = var.instance_type
  key_name           = var.key_name
  subnet_id          = var.subnet_id
  security_group_id  = var.ec2_sg_id
  ami_id             = var.ami_id
}

# Frontend EC2
module "frontend_ec2" {
  source             = "./modules/ec2"
  service_name       = "frontend"
  docker_image       = "${var.docker_username}/frontend:${var.frontend_image_tag}"
  container_port     = 3000
  host_port          = 80
  instance_type      = var.instance_type
  key_name           = var.key_name
  subnet_id          = var.subnet_id
  security_group_id  = var.ec2_sg_id
  ami_id             = var.ami_id
}