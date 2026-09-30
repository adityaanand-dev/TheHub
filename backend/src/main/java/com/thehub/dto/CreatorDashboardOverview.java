package com.thehub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CreatorDashboardOverview {

    @JsonProperty("available_projects_count")
    private long availableProjectsCount;

    @JsonProperty("my_applications_count")
    private long myApplicationsCount;

    @JsonProperty("active_projects_count")
    private long activeProjectsCount;

    @JsonProperty("completed_projects_count")
    private long completedProjectsCount;

    @JsonProperty("unread_messages_count")
    private long unreadMessagesCount;

    @JsonProperty("active_projects")
    private List<ProjectResponse> activeProjects;

    public CreatorDashboardOverview() {}

    public long getAvailableProjectsCount() { return availableProjectsCount; }
    public void setAvailableProjectsCount(long availableProjectsCount) { this.availableProjectsCount = availableProjectsCount; }

    public long getMyApplicationsCount() { return myApplicationsCount; }
    public void setMyApplicationsCount(long myApplicationsCount) { this.myApplicationsCount = myApplicationsCount; }

    public long getActiveProjectsCount() { return activeProjectsCount; }
    public void setActiveProjectsCount(long activeProjectsCount) { this.activeProjectsCount = activeProjectsCount; }

    public long getCompletedProjectsCount() { return completedProjectsCount; }
    public void setCompletedProjectsCount(long completedProjectsCount) { this.completedProjectsCount = completedProjectsCount; }

    public long getUnreadMessagesCount() { return unreadMessagesCount; }
    public void setUnreadMessagesCount(long unreadMessagesCount) { this.unreadMessagesCount = unreadMessagesCount; }

    public List<ProjectResponse> getActiveProjects() { return activeProjects; }
    public void setActiveProjects(List<ProjectResponse> activeProjects) { this.activeProjects = activeProjects; }
}
