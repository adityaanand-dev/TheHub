package com.thehub.dto.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum EventType {
        ORDER_CREATED,
        ORDER_ACCEPTED,
        ORDER_DECLINED,
        ORDER_IN_PROGRESS,
        ORDER_DELIVERED,
        ORDER_COMPLETED,
        ORDER_CANCELLED
    }

    private String eventId;
    private EventType eventType;
    private Long orderId;
    private Long serviceId;
    private String serviceTitle;
    private Long clientId;
    private String clientEmail;
    private String clientName;
    private Long freelancerId;
    private String freelancerEmail;
    private String creatorName;
    private BigDecimal amount;
    private String rejectionReason;
    private LocalDateTime timestamp;

    public OrderEvent() {}

    public OrderEvent(Long orderId, Long serviceId, String serviceTitle, Long clientId, String clientName,
                      Long freelancerId, String creatorName, BigDecimal amount, String eventTypeStr, String reason) {
        this.eventId = java.util.UUID.randomUUID().toString();
        try {
            this.eventType = EventType.valueOf(eventTypeStr);
        } catch (Exception e) {
            this.eventType = EventType.ORDER_CREATED;
        }
        this.orderId = orderId;
        this.serviceId = serviceId;
        this.serviceTitle = serviceTitle;
        this.clientId = clientId;
        this.clientName = clientName;
        this.freelancerId = freelancerId;
        this.creatorName = creatorName;
        this.amount = amount;
        this.rejectionReason = reason;
        this.timestamp = LocalDateTime.now();
    }

    public OrderEvent(String eventId, EventType eventType, Long orderId, Long serviceId,
                      String serviceTitle, Long clientId, String clientEmail, String clientName,
                      Long freelancerId, String freelancerEmail, String creatorName,
                      BigDecimal amount, String rejectionReason) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.orderId = orderId;
        this.serviceId = serviceId;
        this.serviceTitle = serviceTitle;
        this.clientId = clientId;
        this.clientEmail = clientEmail;
        this.clientName = clientName;
        this.freelancerId = freelancerId;
        this.freelancerEmail = freelancerEmail;
        this.creatorName = creatorName;
        this.amount = amount;
        this.rejectionReason = rejectionReason;
        this.timestamp = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public String getServiceTitle() { return serviceTitle; }
    public void setServiceTitle(String serviceTitle) { this.serviceTitle = serviceTitle; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }

    public String getFreelancerEmail() { return freelancerEmail; }
    public void setFreelancerEmail(String freelancerEmail) { this.freelancerEmail = freelancerEmail; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
