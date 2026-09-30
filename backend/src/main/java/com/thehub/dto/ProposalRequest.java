package com.thehub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProposalRequest {

    @NotBlank(message = "Cover letter is required")
    @JsonProperty("cover_letter")
    private String coverLetter;

    @NotNull(message = "Proposed price is required")
    @DecimalMin(value = "1.00", message = "Proposed price must be at least ₹1.00")
    @JsonProperty("proposed_price")
    private BigDecimal proposedPrice;

    @NotNull(message = "Estimated completion days is required")
    @Min(value = 1, message = "Estimated days must be at least 1")
    @JsonProperty("estimated_days")
    private Integer estimatedDays;

    @JsonProperty("relevant_experience")
    private String relevantExperience;

    private String attachments;

    public ProposalRequest() {}

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public BigDecimal getProposedPrice() { return proposedPrice; }
    public void setProposedPrice(BigDecimal proposedPrice) { this.proposedPrice = proposedPrice; }

    public Integer getEstimatedDays() { return estimatedDays; }
    public void setEstimatedDays(Integer estimatedDays) { this.estimatedDays = estimatedDays; }

    public String getRelevantExperience() { return relevantExperience; }
    public void setRelevantExperience(String relevantExperience) { this.relevantExperience = relevantExperience; }

    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
}
