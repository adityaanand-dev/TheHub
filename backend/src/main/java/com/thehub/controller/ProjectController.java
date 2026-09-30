package com.thehub.controller;

import com.thehub.dto.*;
import com.thehub.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Marketplace Projects", description = "Two-sided freelancing marketplace project workflows")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // =========================================================================
    // CLIENT ACTIONS
    // =========================================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Client posts a new project requiring work")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ProjectResponse response = projectService.createProject(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get projects created by current Client")
    public ResponseEntity<List<ProjectResponse>> getClientProjects(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(projectService.getClientProjects(userDetails.getUsername()));
    }

    @GetMapping("/overview/client")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get Client dashboard summary and statistics")
    public ResponseEntity<ClientDashboardOverview> getClientOverview(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(projectService.getClientOverview(userDetails.getUsername()));
    }

    @PostMapping("/applications/{applicationId}/accept")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Client accepts Creator proposal and hires them for the project")
    public ResponseEntity<ProjectResponse> hireCreator(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(projectService.hireCreator(applicationId, userDetails.getUsername()));
    }

    @PostMapping("/applications/{applicationId}/reject")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Client rejects Creator proposal")
    public ResponseEntity<ProposalResponse> rejectApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(projectService.rejectApplication(applicationId, userDetails.getUsername()));
    }

    @PostMapping("/{id}/request-revision")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Client requests revision on submitted work")
    public ResponseEntity<ProjectResponse> requestRevision(
            @PathVariable Long id,
            @Valid @RequestBody RevisionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(projectService.requestRevision(id, request, userDetails.getUsername()));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Client approves completed work, releasing payment")
    public ResponseEntity<ProjectResponse> approveWork(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(projectService.approveWork(id, userDetails.getUsername()));
    }

    // =========================================================================
    // CREATOR ACTIONS
    // =========================================================================

    @PostMapping("/{id}/apply")
    @PreAuthorize("hasAnyRole('FREELANCER', 'CREATOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Creator applies / submits proposal for a Client project")
    public ResponseEntity<ProposalResponse> applyToProject(
            @PathVariable Long id,
            @Valid @RequestBody ProposalRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ProposalResponse response = projectService.applyToProject(id, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/applications/my")
    @PreAuthorize("hasAnyRole('FREELANCER', 'CREATOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Creator views their submitted proposals")
    public ResponseEntity<List<ProposalResponse>> getMyApplications(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(projectService.getCreatorApplications(userDetails.getUsername()));
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasAnyRole('FREELANCER', 'CREATOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Creator views active assigned projects they are working on")
    public ResponseEntity<List<ProjectResponse>> getAssignedProjects(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(projectService.getAssignedProjectsForCreator(userDetails.getUsername()));
    }

    @GetMapping("/overview/creator")
    @PreAuthorize("hasAnyRole('FREELANCER', 'CREATOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get Creator dashboard overview and active work items")
    public ResponseEntity<CreatorDashboardOverview> getCreatorOverview(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(projectService.getCreatorOverview(userDetails.getUsername()));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('FREELANCER', 'CREATOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Creator submits completed work for Client review")
    public ResponseEntity<ProjectResponse> submitWork(
            @PathVariable Long id,
            @Valid @RequestBody WorkSubmissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(projectService.submitWork(id, request, userDetails.getUsername()));
    }

    // =========================================================================
    // DISCOVERY & PUBLIC / SHARED
    // =========================================================================

    @GetMapping
    @Operation(summary = "Discover client-posted projects with filters & search")
    public ResponseEntity<List<ProjectResponse>> searchProjects(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String skills,
            @RequestParam(name = "min_budget", required = false) BigDecimal minBudget,
            @RequestParam(name = "max_budget", required = false) BigDecimal maxBudget,
            @RequestParam(name = "experience_level", required = false) String experienceLevel,
            @RequestParam(name = "sort_by", defaultValue = "newest") String sortBy,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String currentEmail = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(projectService.searchProjects(category, search, skills, minBudget, maxBudget, experienceLevel, sortBy, currentEmail));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project details by ID")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String currentEmail = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(projectService.getProjectById(id, currentEmail));
    }

    @GetMapping("/{id}/applications")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get applications for a project (Owner Client or applicant)")
    public ResponseEntity<List<ProposalResponse>> getProjectApplications(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(projectService.getProjectApplications(id, userDetails.getUsername()));
    }
}
