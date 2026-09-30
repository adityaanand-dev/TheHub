# TheHub — Production Deployment & Operations Guide

## 1. Local Development via Docker Compose

### Prerequisites
- Docker Engine 24+ & Docker Compose v2+
- Ports 80, 3000, 5432, 6379, 8080, 8085, 9092 free

### Start All Services
```bash
# 1. Create environment file from template
cp .env.example .env

# 2. Build and launch containers in background
docker compose up -d --build

# 3. Check container status
docker compose ps
```

### Access URLs
- **Web Application (React SPA):** `http://localhost:3000` or `http://localhost:80`
- **Backend API (Spring Boot):** `http://localhost:8080`
- **Swagger UI API Docs:** `http://localhost:8080/swagger-ui.html`
- **Kafka Visual UI:** `http://localhost:8085`
- **Actuator Health Check:** `http://localhost:8080/actuator/health`

---

## 2. Generic Linux VPS Deployment

### Architecture on VPS
```
Internet (Ports 80, 443)
       |
       v
Nginx Reverse Proxy
  ├── /api/       --> Spring Boot API (:8080)
  └── / (root)    --> React Frontend (:80)
```

### Step 1: Server Provisioning via Terraform
```bash
cd terraform
terraform init
terraform apply
```
This generates `terraform/cloud-init-vps.sh` containing automated commands to install Docker, Docker Compose, and configure UFW firewall rules on Ubuntu/Debian.

### Step 2: Bootstrap Server
```bash
scp terraform/cloud-init-vps.sh root@YOUR_SERVER_IP:/tmp/
ssh root@YOUR_SERVER_IP "bash /tmp/cloud-init-vps.sh"
```

### Step 3: Deploy Application Stack
```bash
# Copy compose files to server
scp docker-compose.yml .env root@YOUR_SERVER_IP:/opt/thehub/

# Launch containers on VPS
ssh root@YOUR_SERVER_IP "cd /opt/thehub && docker compose up -d"
```

---

## 3. Production Hardening & SSL/TLS

To enable HTTPS with Let's Encrypt Certbot:
```bash
# Install certbot on host
sudo apt-get install -y certbot python3-certbot-nginx

# Request certificate
sudo certbot --nginx -d hub.yourdomain.com
```
Certbot will automatically configure SSL certificates in `/etc/letsencrypt/` and update Nginx configurations.
