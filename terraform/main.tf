# ==============================================================================
# TheHub — Cloud-Agnostic VPS Infrastructure as Code
# Generates automated cloud-init provisioning scripts to bootstrap any
# standard Linux VPS (Ubuntu/Debian) with Docker, Docker Compose, and UFW firewall.
# ==============================================================================

locals {
  cloud_init_script = <<-EOT
    #!/bin/bash
    set -euo pipefail

    echo "==> [TheHub Bootstrap] Updating base packages..."
    apt-get update -y && apt-get upgrade -y

    echo "==> [TheHub Bootstrap] Installing essential tools..."
    apt-get install -y curl git ufw fail2ban ca-certificates gnupg

    echo "==> [TheHub Bootstrap] Installing Docker Engine & Compose plugin..."
    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    chmod a+r /etc/apt/keyrings/docker.gpg

    echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
      $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
      tee /etc/apt/sources.list.d/docker.list > /dev/null

    apt-get update -y
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

    systemctl enable docker
    systemctl start docker

    echo "==> [TheHub Bootstrap] Configuring UFW firewall rules..."
    ufw default deny incoming
    ufw default allow outgoing
    ufw allow 22/tcp comment 'SSH'
    ufw allow 80/tcp comment 'HTTP Nginx'
    ufw allow 443/tcp comment 'HTTPS Nginx'
    ufw allow 8085/tcp comment 'Kafka UI'
    echo "y" | ufw enable

    echo "==> [TheHub Bootstrap] Provisioning TheHub application workspace..."
    mkdir -p /opt/thehub
    cd /opt/thehub

    echo "==> [TheHub Bootstrap] Initialization finished successfully."
  EOT
}

resource "local_file" "cloud_init" {
  filename = "${path.module}/cloud-init-vps.sh"
  content  = local.cloud_init_script
}
