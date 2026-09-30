package com.thehub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thehub.entity.ProjectStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProjectResponse {

    private Long id;
    private String title;
    private String description;
    private String category;

    @JsonProperty("client_id")
    private Long clientId;

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("client_company")
    private String clientCompany;

    @JsonProperty("selected_creator_id")
    private Long selectedCreatorId;

    @JsonProperty("selected_creator_name")
    private String selectedCreatorName;

    private BigDecimal budget;

    @JsonProperty("deadline_days")
    private Integer deadlineDays;

    @JsonProperty("experience_level")
    private String experienceLevel;

    @JsonProperty("required_skills")
    private String requiredSkills;

    private String attachments;
    private ProjectStatus status;

    @JsonProperty("submission_notes")
    private String submissionNotes;

    @JsonProperty("submission_url")
    private String submissionUrl;

    @JsonProperty("revision_notes")
    private String revisionNotes;

    @JsonProperty("application_count")
    private int applicationCount;

    @JsonProperty("has_applied")
    private boolean hasApplied;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public ProjectResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientCompany() { return clientCompany; }
    public void setClientCompany(String clientCompany) { this.clientCompany = clientCompany; }

    public Long getSelectedCreatorId() { return selectedCreatorId; }
    public void setSelectedCreatorId(Long selectedCreatorId) { this.selectedCreatorId = selectedCreatorId; }

    public String getSelectedCreatorName() { return selectedCreatorName; }
    public void setSelectedCreatorName(String selectedCreatorName) { this.selectedCreatorName = selectedCreatorName; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public Integer getDeadlineDays() { return deadlineDays; }
    public void setDeadlineDays(Integer deadlineDays) { this.deadlineDays = deadlineDays; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }

    public String getSubmissionNotes() { return submissionNotes; }
    public void setSubmissionNotes(String submissionNotes) { this.submissionNotes = submissionNotes; }

    public String getSubmissionUrl() { return submissionUrl; }
    public void setSubmissionUrl(String submissionUrl) { this.submissionUrl = submissionUrl; }

    public String getRevisionNotes() { return revisionNotes; }
    public void setRevisionNotes(String revisionNotes) { this.revisionNotes = revisionNotes; }

    public int getApplicationCount() { return applicationCount; }
    public void setApplicationCount(int applicationCount) { this.applicationCount = applicationCount; }

    public boolean isHasApplied() { return hasApplied; }
    public void setHasApplied(boolean hasApplied) { this.hasApplied = hasApplied; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
