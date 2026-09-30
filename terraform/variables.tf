variable "project_name" {
  type        = string
  description = "Project name tag"
  default     = "TheHub"
}

variable "environment" {
  type        = string
  description = "Deployment target environment (staging/production)"
  default     = "production"
}

variable "server_hostname" {
  type        = string
  description = "Domain or hostname for the Linux VPS"
  default     = "hub.example.com"
}

variable "admin_email" {
  type        = string
  description = "Administrator email for SSL/Let's Encrypt notices"
  default     = "admin@thehub.com"
}

variable "ssh_public_key" {
  type        = string
  description = "SSH public key for server administrative access"
  default     = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIGhubAdminKeyPlaceholder"
}

variable "allowed_ingress_ports" {
  type        = list(number)
  description = "Firewall open ports for production operation"
  default     = [22, 80, 443, 8085]
}
