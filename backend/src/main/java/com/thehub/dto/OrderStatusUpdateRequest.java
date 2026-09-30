package com.thehub.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class OrderStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status;

    @JsonProperty("rejection_reason")
    @JsonAlias({"rejectionReason", "rejection_reason"})
    private String rejectionReason;

    public OrderStatusUpdateRequest() {}

    public OrderStatusUpdateRequest(String status, String rejectionReason) {
        this.status = status;
        this.rejectionReason = rejectionReason;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
