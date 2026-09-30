package com.thehub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class RevisionRequest {

    @NotBlank(message = "Revision feedback / requested changes are required")
    @JsonProperty("revision_notes")
    private String revisionNotes;

    public RevisionRequest() {}

    public String getRevisionNotes() { return revisionNotes; }
    public void setRevisionNotes(String revisionNotes) { this.revisionNotes = revisionNotes; }
}
