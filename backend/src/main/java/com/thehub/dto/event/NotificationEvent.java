package com.thehub.dto.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class NotificationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private Long recipientUserId;
    private String recipientEmail;
    private String title;
    private String message;
    private String type;
    private LocalDateTime timestamp;

    public NotificationEvent() {}

    public NotificationEvent(Long recipientUserId, String recipientEmail, String title, String message, String type) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.recipientUserId = recipientUserId;
        this.recipientEmail = recipientEmail;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public NotificationEvent(String eventId, Long recipientUserId, String recipientEmail, String title, String message, String type) {
        this.eventId = eventId;
        this.recipientUserId = recipientUserId;
        this.recipientEmail = recipientEmail;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public Long getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(Long recipientUserId) { this.recipientUserId = recipientUserId; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
