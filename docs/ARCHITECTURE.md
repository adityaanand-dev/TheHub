# TheHub — System Architecture Documentation

## 1. High-Level Architecture Overview

TheHub is a production-grade, containerized, cloud-agnostic freelancing marketplace architected around an event-driven core.

```
                    Internet / Users
                           |
                           v
            Nginx Reverse Proxy (:80 / :443)
              |                          |
              v                          v
    React 18 SPA (:3000)      Spring Boot 3 API (:8080)
                                         |
               +-------------------------+-------------------------+
               |                         |                         |
               v                         v                         v
        PostgreSQL 16                 Redis 7            Apache Kafka (KRaft)
     (Relational Entities)      (Cache & Performance)     (Event Streaming)
                                                                   |
                                         +-------------------------+-------------------------+
                                         |                         |                         |
                                         v                         v                         v
                                   Orders Consumer         Payment Consumer       Notification Consumer
```

---

## 2. Core Two-Sided Marketplace Model

The platform strictly differentiates between two primary marketplace actors:

### A. Client (Buyer)
- **Role:** The party needing work completed.
- **Actions:**
  - Posts projects/gigs with detailed descriptions, budget, timeline, and required skills.
  - Explores and evaluates Creators via ratings, portfolio links, and hourly rates.
  - Receives, compares, and evaluates Creator proposals/applications.
  - Hires/books a Creator (locks the project to that Creator and moves status to `IN_PROGRESS`).
  - Directly communicates with the assigned Creator via project-linked chat.
  - Reviews submitted deliverables, requests revisions, or approves completion.
  - Releases payment and leaves platform reviews.

### B. Creator (Service Provider)
- **Role:** The party providing services, design, or technical talent.
- **Rule:** **Creators CANNOT create marketplace projects/gigs.**
- **Actions:**
  - Discovers open projects posted by Clients with keyword and category filters.
  - Submits proposals with cover letter, proposed pricing, and estimated days.
  - Exchanges direct messages with the Client regarding requirements.
  - Submits work deliverables with links and notes.
  - Addresses client revision requests.
  - Receives payment and reviews upon client approval.

---

## 3. Marketplace State Machine & Workflow

```
CLIENT                              THEHUB (CORE)                              CREATOR
  |                                       |                                       |
  |--- Create Project (POST /projects) -->|                                       |
  |                                       |===> Project Created [OPEN]            |
  |                                       |     (Dispatched to Kafka Topic)       |
  |                                       |                                       |
  |                                       |<-- Browse Projects (GET /projects) ---|
  |                                       |                                       |
  |                                       |<-- Apply (POST /projects/{id}/apply) -|
  |                                       |===> Application [PENDING]             |
  |                                       |                                       |
  |<-- List Applications -----------------|                                       |
  |                                       |                                       |
  |--- Hire Creator --------------------->|                                       |
  |    (POST /applications/{id}/accept)   |===> Project [IN_PROGRESS]             |
  |                                       |     Application [ACCEPTED]            |
  |                                       |     (Dispatched to Kafka Topic)       |
  |                                       |                                       |
  |<================ Direct Chat (POST /chat/projects/{id}/messages) ============>|
  |                                       |                                       |
  |                                       |<-- Submit Work (/projects/{id}/submit)|
  |                                       |===> Project [SUBMITTED]               |
  |                                       |                                       |
  |--- Request Revision ----------------->|                                       |
  |    (POST /projects/{id}/request-rev)  |===> Project [REVISION_REQUESTED]      |
  |                                       |                                       |
  |                                       |<-- Resubmit Work (/projects/{id}/sub) |
  |                                       |===> Project [SUBMITTED]               |
  |                                       |                                       |
  |--- Approve Work --------------------->|                                       |
  |    (POST /projects/{id}/approve)      |===> Project [COMPLETED]               |
  |                                       |     Payment Released                  |
  |                                       |     (Dispatched to Kafka Topic)       |
  |                                       |                                       |
  |--- Leave Review (POST /reviews) ----->|===> Rating Updated in DB & Redis      |
```

---

## 4. Database Schema & Relational Integrity

PostgreSQL 16 models relationships with strict foreign key constraints and indexes:

1. **`users`:** Core user credentials, hashed passwords (BCrypt), and primary role (`ROLE_CLIENT`, `ROLE_FREELANCER`, `ROLE_CREATOR`, `ROLE_ADMIN`).
2. **`freelancer_profiles`:** 1-to-1 relation with user. Stores headline, bio, skills, hourly rate, rating count, rating average, and portfolio URL.
3. **`client_profiles`:** 1-to-1 relation with user. Stores company name, website, and payment method details.
4. **`projects`:** Core marketplace project posted by a Client. Includes budget, deadline days, required skills, experience level, attachments, submission notes, and revision notes. References `client_id` (foreign key) and optional `selected_creator_id` (foreign key).
5. **`project_applications`:** Proposal submitted by a Creator. Unique composite constraint on `(project_id, creator_id)` preventing duplicate proposals.
6. **`chat_messages`:** Real-time messages linked to a `project_id`, `sender_id`, and `recipient_id`.
7. **`categories`:** Technology, Design, Video, Writing, etc.
8. **`reviews`:** Review ratings (1-5) and feedback submitted upon project completion.
9. **`orders` & `payments`:** Transaction audit log and financial escrow records.

---

## 5. Kafka Event-Driven Architecture

Apache Kafka 3.7 running in modern KRaft mode coordinates asynchronous processing:

| Topic | Event Types | Producers | Consumers |
| :--- | :--- | :--- | :--- |
| `thehub-orders` | `ORDER_CREATED`, `ORDER_ACCEPTED`, `ORDER_COMPLETED`, `ORDER_CANCELLED` | `ProjectService`, `OrderService` | Order Consumer, Analytics Consumer |
| `thehub-payments` | `PAYMENT_SUCCESS`, `PAYMENT_RELEASED`, `PAYMENT_FAILED` | `ProjectService`, `OrderService` | Payment Consumer, Escrow Worker |
| `thehub-notifications` | `PROJECT_POSTED`, `APPLICATION_RECEIVED`, `CREATOR_HIRED`, `WORK_SUBMITTED`, `MESSAGE_SENT` | `ProjectService`, `ChatService` | Notification Consumer |

Consumer groups process events idempotently and support resilient replayability.

---

## 6. Security & Invariant Enforcement

- **Role Invariant 1 (Creator Project Restriction):** Creators cannot create projects (`@PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")` + service-level checks throw 403/400).
- **Role Invariant 2 (Client Proposal Restriction):** Clients cannot apply to projects (`@PreAuthorize("hasAnyRole('FREELANCER', 'CREATOR', 'ADMIN')")` + service-level checks throw 403/400).
- **Role Invariant 3 (Unique Proposals):** DB unique constraint and repository duplicate check prevent multiple proposals from the same creator on a project.
- **Role Invariant 4 (Participant Authorization):** Chat messages and project submissions verify participant ownership before accepting state mutations.
