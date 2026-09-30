package com.thehub.dto.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum EventType {
        PAYMENT_SUCCESS,
        PAYMENT_FAILED,
        PAYMENT_REFUNDED
    }

    private String eventId;
    private EventType eventType;
    private Long orderId;
    private BigDecimal amount;
    private String transactionId;
    private String payerEmail;
    private LocalDateTime timestamp;

    public PaymentEvent() {}

    public PaymentEvent(Long orderId, BigDecimal amount, String transactionId, String payerEmail) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.eventType = EventType.PAYMENT_SUCCESS;
        this.orderId = orderId;
        this.amount = amount;
        this.transactionId = transactionId;
        this.payerEmail = payerEmail;
        this.timestamp = LocalDateTime.now();
    }

    public PaymentEvent(String eventId, EventType eventType, Long orderId, BigDecimal amount, String transactionId, String payerEmail) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.orderId = orderId;
        this.amount = amount;
        this.transactionId = transactionId;
        this.payerEmail = payerEmail;
        this.timestamp = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getPayerEmail() { return payerEmail; }
    public void setPayerEmail(String payerEmail) { this.payerEmail = payerEmail; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
