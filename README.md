# TheHub — Production-Style Event-Driven Freelancing Marketplace

[![CI/CD Pipeline](https://github.com/adityaanand-dev/TheHub/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/adityaanand-dev/TheHub/actions/workflows/ci-cd.yml)
[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![React 18](https://img.shields.io/badge/React-18.3-blue.svg)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Kafka KRaft](https://img.shields.io/badge/Kafka-3.7%20KRaft-red.svg)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![Docker Compose](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)

TheHub is a production-style, containerized, event-driven freelancing marketplace where **Clients create and post projects/gigs requiring work**, and **Creators discover opportunities, submit proposals, chat with clients, and deliver work**. The platform coordinates asynchronous workflows via **Apache Kafka (KRaft mode)**, **Redis 7 caching**, **PostgreSQL 16**, **Spring Boot 3**, and **React 18**.

---

## 1. Core Marketplace Model & Roles

```text
CLIENT (Buyer)                                  CREATOR (Service Provider)
- Needs work completed                          - Provides services & skills
- Creates/posts projects & gigs                 - CANNOT create marketplace gigs
- Explores & evaluates Creators                 - Searches & filters client projects
- Receives proposals/applications               - Submits proposals with cover letter & pricing
- Hires / books Creator                         - Communicates with Client
- Direct project-linked chat                    - Works on project & updates progress
- Requests revisions OR approves work           - Submits work deliverables
- Releases payment & reviews Creator            - Receives payment & reviews
```

### Marketplace Workflow

```text
CLIENT Creates Project -> Project OPEN
    |
    v
CREATORS Browse & Apply -> Application PENDING (Duplicate Prevention enforced)
    |
    v
CLIENT Reviews Proposals & Hires Creator -> Project IN_PROGRESS
    |
    v
Direct Chat regarding requirements (PostgreSQL + Kafka Event)
    |
    v
CREATOR Submits Work Deliverables -> Project SUBMITTED
    |
    +-----> CLIENT Requests Revision -> Project REVISION_REQUESTED
    |          |
    |          v
    |       CREATOR Resubmits Work -> Project SUBMITTED
    |
    v
CLIENT Approves Work -> Project COMPLETED & Payment Released (Kafka Event)
    |
    v
CLIENT Leaves Review -> Creator Rating Updated
```

---

## 2. Target Architecture

```text
                    THEHUB
                       |
                 React 18 SPA
                       |
               Nginx Reverse Proxy
                       |
                       v
             Spring Boot 3 REST API
                       |
          +------------+------------+
          |            |            |
          v            v            v
      PostgreSQL     Redis        Kafka (KRaft)
      (Database)    (Cache)             |
                                        +-------------+-------------+
                                        |             |             |
                                        v             v             v
                                     Orders        Payments    Notifications
                                     Consumer      Consumer       Consumer
```

The architecture is **cloud-agnostic** and runs 100% locally via Docker without cloud lock-in.

---

## 3. Technology Stack

- **Frontend:** React 18, Vite, Tailwind CSS, Lucide Icons.
- **Backend API:** Java 21, Spring Boot 3.3.4 (Spring Web, Spring Security, Spring Data JPA, Spring Validation, Actuator).
- **Security:** Stateless JWT Authentication with BCrypt password hashing and Role-Based Access Control (`ROLE_CLIENT`, `ROLE_FREELANCER`, `ROLE_CREATOR`, `ROLE_ADMIN`).
- **Database:** PostgreSQL 16 with JPA relational mappings, foreign keys, and indexes.
- **Cache & Performance:** Redis 7 for catalog and platform statistics caching with automated cache invalidation.
- **Event Streaming Broker:** Apache Kafka 3.7 in modern KRaft mode (no ZooKeeper).
- **Event UI:** Provectus Kafka UI on port 8085 for visual inspection of topics, messages, and consumer groups.
- **Containerization:** Multi-stage Dockerfiles and unified `docker-compose.yml`.
- **Gateway & Proxy:** Nginx with rate limiting and security headers.
- **CI/CD:** GitHub Actions pipeline running unit tests, integration tests, React build, and Docker validation.

---

## 4. Quick Start with Docker (Recommended)

Start the entire system locally:

```bash
# 1. Clone repository
git clone https://github.com/adityaanand-dev/TheHub.git
cd TheHub

# 2. Copy environment template
cp .env.example .env

# 3. Launch all containers
docker compose up -d --build
```

### Accessing the Platform:
- **Web Application:** [http://localhost:3000](http://localhost:3000) (or port 80 via Nginx)
- **Spring Boot API:** [http://localhost:8080](http://localhost:8080)
- **Swagger UI API Documentation:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Kafka Visual UI:** [http://localhost:8085](http://localhost:8085)
- **Actuator Health Metrics:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 5. Pre-Configured Demo Accounts (1-Click Switcher)

The application includes instant 1-click demo switcher pills in the header to effortlessly test both perspectives:

| Role | Email | Password | Details |
| :--- | :--- | :--- | :--- |
| **Client** | `client@thehub.com` | `client123` | Ava Johnson (Posts projects, hires creators, reviews work) |
| **Creator** | `creator@thehub.com` | `creator123` | Alex Rivera (Finds projects, applies, submits deliverables) |
| **Admin** | `admin@thehub.com` | `admin123` | Platform Administrator (Moderation, user management) |

---

## 6. End-to-End Verification

The complete production stack and marketplace lifecycle can be verified with a single command:

```bash
python test_production_e2e.py
```

### Verification Checks Performed:
1. Actuator health & PostgreSQL readiness.
2. JWT authentication for Client and Creator.
3. Role Invariant: Creator blocked from posting projects (403/400).
4. Client creates marketplace project (`OPEN`).
5. Creator discovers project via search/filtering.
6. Role Invariant: Client blocked from submitting proposals (403/400).
7. Creator submits proposal (`PENDING`).
8. Unique proposal constraint: Duplicate application blocked (409 Conflict).
9. Client reviews applications & hires Creator (`IN_PROGRESS`).
10. Project-specific direct chat exchange between Client and Creator.
11. Creator submits deliverables (`SUBMITTED`).
12. Client reviews deliverables and requests revision (`REVISION_REQUESTED`).
13. Creator resubmits & Client approves completion (`COMPLETED` + Kafka payment event).
14. Creator discovery directory and Client/Creator overview dashboards.
15. React 18 production bundle, Nginx reverse proxy gateway, and Kafka UI.

---

## 7. Kafka Event-Driven Architecture

TheHub uses Apache Kafka in KRaft mode to decouple high-volume business actions:

```text
Client hires Creator / Creator submits work / Client approves
        |
        v
Spring Boot API (ProjectService / OrderService / ChatService)
        |
        v
Apache Kafka
        |
        +----> thehub-orders (OrderEvent: ORDER_CREATED, ORDER_ACCEPTED, ORDER_COMPLETED)
        |
        +----> thehub-notifications (NotificationEvent: in-app alerts dispatched to users)
        |
        +----> thehub-payments (PaymentEvent: escrow settlement & release)
```

Verify Kafka topics, messages, and consumer groups visually via [http://localhost:8085](http://localhost:8085).

---

## 8. Technologies Implemented

- **Backend:** Java 21, Spring Boot 3.3.4, Spring Data JPA, Spring Security, Spring Kafka, Spring Validation, Actuator.
- **Frontend:** React 18, Vite, Tailwind CSS, Lucide Icons.
- **Databases & Caching:** PostgreSQL 16, Redis 7 (Cacheable / CacheEvict).
- **Messaging:** Apache Kafka (KRaft mode), Kafka Producers & Consumers, Provectus Kafka UI.
- **DevOps & Containers:** Docker, Docker Compose, Multi-stage builds, Nginx reverse proxy, Rate limiting.
- **CI/CD & Cloud:** GitHub Actions, Terraform Infrastructure-as-Code, Linux VPS deployment.