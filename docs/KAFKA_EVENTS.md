# TheHub — Kafka Event-Driven Architecture Guide

## 1. Why Apache Kafka?

In a freelancing marketplace, high-traffic user interactions (such as submitting an order, changing a proposal status, or finalizing payment) trigger multiple downstream actions:
- In-app notification creation
- Email dispatch to clients/freelancers
- Live metric recalculation
- Audit logging & settlement workflows

Executing these synchronously within the HTTP request cycle introduces latency, risks cascading failures, and hurts user experience.

By introducing **Apache Kafka**:
1. **Decoupling:** The client receives an immediate `201 Created` or `200 OK` response while downstream tasks execute asynchronously.
2. **Resilience & Replayability:** If a notification worker or analytics consumer restarts or fails, messages remain persisted in Kafka topics and are processed upon recovery.
3. **Scalability:** Consumer groups allow consumers to scale independently without changing the core API.

---

## 2. Kafka Topics & Partitioning

| Topic Name | Partitions | Retention | Purpose |
| :--- | :--- | :--- | :--- |
| `thehub-orders` | 3 | 7 days | Emitted during booking lifecycle state changes |
| `thehub-payments` | 3 | 14 days | Emitted when payments are initiated, succeed, or fail |
| `thehub-notifications` | 3 | 3 days | Emitted for in-app alert and notification dispatch |

---

## 3. Event Schemas & Lifecycles

### A. OrderEvent (`thehub-orders`)
```json
{
  "eventId": "a7b3c2e1-4567-4890-a123-b456c789d012",
  "eventType": "ORDER_CREATED",
  "orderId": 14,
  "serviceId": 2,
  "serviceTitle": "Viral YouTube Thumbnails & Complete Branding Kit",
  "clientId": 3,
  "clientEmail": "ava@auramedia.io",
  "clientName": "Ava Johnson",
  "freelancerId": 4,
  "freelancerEmail": "maya@thehub.com",
  "creatorName": "Maya Chen",
  "amount": 45.00,
  "rejectionReason": null,
  "timestamp": "2026-09-30T17:00:00"
}
```

#### Event Types Supported:
- `ORDER_CREATED`: Published when client books a gig. Triggers alert to creator.
- `ORDER_ACCEPTED`: Published when creator accepts proposal. Triggers confirmation to client.
- `ORDER_DECLINED`: Published when creator declines proposal. Triggers notice to client with DP1 rejection reason.
- `ORDER_COMPLETED`: Published when order finishes. Triggers payment settlement and review prompt.

---

## 4. Consumer Groups

1. **`thehub-order-processors`** (`OrderEventConsumer`):
   - Listens to `thehub-orders`.
   - Inspects `eventType` and forwards structured notifications to `thehub-notifications`.
2. **`thehub-notification-processors`** (`NotificationEventConsumer`):
   - Listens to `thehub-notifications`.
   - Idempotently creates persistent notification records for recipients.
3. **`thehub-payment-processors`** (`PaymentEventConsumer`):
   - Listens to `thehub-payments`.
   - Settles transactions and marks delivered orders as `Completed`.

---

## 5. Kafka UI Inspection

When running via Docker Compose, access the visual Kafka UI at:
```text
http://localhost:8085
```
Here you can:
- Inspect active topics (`thehub-orders`, `thehub-payments`, `thehub-notifications`).
- View real-time messages and payload contents.
- Monitor consumer groups, partitions, and offset lag.
