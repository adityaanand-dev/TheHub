package com.thehub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ClientDashboardOverview {

    @JsonProperty("active_projects_count")
    private long activeProjectsCount;

    @JsonProperty("open_projects_count")
    private long openProjectsCount;

    @JsonProperty("pending_applications_count")
    private long pendingApplicationsCount;

    @JsonProperty("completed_projects_count")
    private long completedProjectsCount;

    @JsonProperty("unread_messages_count")
    private long unreadMessagesCount;

    @JsonProperty("recent_projects")
    private List<ProjectResponse> recentProjects;

    public ClientDashboardOverview() {}

    public long getActiveProjectsCount() { return activeProjectsCount; }
    public void setActiveProjectsCount(long activeProjectsCount) { this.activeProjectsCount = activeProjectsCount; }

    public long getOpenProjectsCount() { return openProjectsCount; }
    public void setOpenProjectsCount(long openProjectsCount) { this.openProjectsCount = openProjectsCount; }

    public long getPendingApplicationsCount() { return pendingApplicationsCount; }
    public void setPendingApplicationsCount(long pendingApplicationsCount) { this.pendingApplicationsCount = pendingApplicationsCount; }

    public long getCompletedProjectsCount() { return completedProjectsCount; }
    public void setCompletedProjectsCount(long completedProjectsCount) { this.completedProjectsCount = completedProjectsCount; }

    public long getUnreadMessagesCount() { return unreadMessagesCount; }
    public void setUnreadMessagesCount(long unreadMessagesCount) { this.unreadMessagesCount = unreadMessagesCount; }

    public List<ProjectResponse> getRecentProjects() { return recentProjects; }
    public void setRecentProjects(List<ProjectResponse> recentProjects) { this.recentProjects = recentProjects; }
}
