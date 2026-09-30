package com.thehub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class WorkSubmissionRequest {

    @NotBlank(message = "Submission notes / summary of completed work is required")
    @JsonProperty("submission_notes")
    private String submissionNotes;

    @JsonProperty("submission_url")
    private String submissionUrl;

    public WorkSubmissionRequest() {}

    public String getSubmissionNotes() { return submissionNotes; }
    public void setSubmissionNotes(String submissionNotes) { this.submissionNotes = submissionNotes; }

    public String getSubmissionUrl() { return submissionUrl; }
    public void setSubmissionUrl(String submissionUrl) { this.submissionUrl = submissionUrl; }
}
