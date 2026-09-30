package com.thehub.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OrderRequest {

    @NotNull(message = "Service/Gig ID is required")
    @JsonProperty("service_id")
    @JsonAlias({"serviceId", "service_id", "gig_id", "gigId"})
    private Long serviceId;

    @NotBlank(message = "Client name is required")
    @JsonProperty("client_name")
    @JsonAlias({"clientName", "client_name"})
    private String clientName;

    @NotBlank(message = "Client email is required")
    @Email(message = "Valid email is required")
    @JsonProperty("client_email")
    @JsonAlias({"clientEmail", "client_email"})
    private String clientEmail;

    @NotBlank(message = "Requirements are required")
    @JsonProperty("requirements")
    @JsonAlias({"notes", "requirements", "details"})
    private String requirements;

    public OrderRequest() {}

    public OrderRequest(Long serviceId, String clientName, String clientEmail, String requirements) {
        this.serviceId = serviceId;
        this.clientName = clientName;
        this.clientEmail = clientEmail;
        this.requirements = requirements;
    }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
}
