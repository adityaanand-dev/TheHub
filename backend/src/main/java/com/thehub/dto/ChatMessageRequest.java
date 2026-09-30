package com.thehub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class ChatMessageRequest {

    @NotBlank(message = "Message content is required")
    private String content;

    @JsonProperty("recipient_id")
    private Long recipientId;

    public ChatMessageRequest() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
}
