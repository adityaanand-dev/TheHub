package com.thehub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thehub.entity.ApplicationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProposalResponse {

    private Long id;

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("project_title")
    private String projectTitle;

    @JsonProperty("client_id")
    private Long clientId;

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("creator_id")
    private Long creatorId;

    @JsonProperty("creator_name")
    private String creatorName;

    @JsonProperty("creator_headline")
    private String creatorHeadline;

    @JsonProperty("creator_avatar")
    private String creatorAvatar;

    @JsonProperty("creator_rating")
    private Double creatorRating;

    @JsonProperty("creator_skills")
    private String creatorSkills;

    @JsonProperty("creator_completed_projects")
    private Long creatorCompletedProjects;

    @JsonProperty("cover_letter")
    private String coverLetter;

    @JsonProperty("proposed_price")
    private BigDecimal proposedPrice;

    @JsonProperty("estimated_days")
    private Integer estimatedDays;

    @JsonProperty("relevant_experience")
    private String relevantExperience;

    private String attachments;
    private ApplicationStatus status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public ProposalResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getCreatorHeadline() { return creatorHeadline; }
    public void setCreatorHeadline(String creatorHeadline) { this.creatorHeadline = creatorHeadline; }

    public String getCreatorAvatar() { return creatorAvatar; }
    public void setCreatorAvatar(String creatorAvatar) { this.creatorAvatar = creatorAvatar; }

    public Double getCreatorRating() { return creatorRating; }
    public void setCreatorRating(Double creatorRating) { this.creatorRating = creatorRating; }

    public String getCreatorSkills() { return creatorSkills; }
    public void setCreatorSkills(String creatorSkills) { this.creatorSkills = creatorSkills; }

    public Long getCreatorCompletedProjects() { return creatorCompletedProjects; }
    public void setCreatorCompletedProjects(Long creatorCompletedProjects) { this.creatorCompletedProjects = creatorCompletedProjects; }

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

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
