output "cloud_init_file" {
  description = "Path to the generated cloud-init deployment script"
  value       = local_file.cloud_init.filename
}

output "deployment_instructions" {
  description = "Steps to initialize TheHub on target Linux VPS"
  value       = <<-EOT
    1. Copy 'cloud-init-vps.sh' to your remote Linux server:
       scp terraform/cloud-init-vps.sh user@${var.server_hostname}:/tmp/
    2. Execute with root privileges:
       ssh user@${var.server_hostname} "sudo bash /tmp/cloud-init-vps.sh"
    3. Transfer docker-compose.yml and .env:
       scp docker-compose.yml .env user@${var.server_hostname}:/opt/thehub/
    4. Start the stack:
       ssh user@${var.server_hostname} "cd /opt/thehub && docker compose up -d"
  EOT
}
