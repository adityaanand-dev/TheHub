package com.thehub.service;

import com.thehub.dto.*;
import com.thehub.dto.event.NotificationEvent;
import com.thehub.dto.event.OrderEvent;
import com.thehub.dto.event.PaymentEvent;
import com.thehub.entity.*;
import com.thehub.exception.BadRequestException;
import com.thehub.exception.ConflictException;
import com.thehub.exception.ResourceNotFoundException;
import com.thehub.kafka.KafkaEventProducer;
import com.thehub.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final KafkaEventProducer kafkaEventProducer;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectApplicationRepository applicationRepository,
                          UserRepository userRepository,
                          ClientProfileRepository clientProfileRepository,
                          FreelancerProfileRepository freelancerProfileRepository,
                          ChatMessageRepository chatMessageRepository,
                          KafkaEventProducer kafkaEventProducer) {
        this.projectRepository = projectRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.freelancerProfileRepository = freelancerProfileRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.kafkaEventProducer = kafkaEventProducer;
    }

    // =========================================================================
    // CLIENT ACTIONS
    // =========================================================================

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request, String clientEmail) {
        User client = userRepository.findByEmail(clientEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + clientEmail));

        // Enforce role: Only Client or Admin can create projects
        if (client.getRole() == Role.ROLE_FREELANCER || client.getRole() == Role.ROLE_CREATOR) {
            throw new BadRequestException("Creators cannot create projects. Only Clients can post projects requiring work.");
        }

        Project project = new Project(
                request.getTitle().trim(),
                request.getDescription().trim(),
                request.getCategory().trim(),
                client,
                request.getBudget(),
                request.getDeadlineDays(),
                request.getExperienceLevel(),
                request.getRequiredSkills()
        );
        if (request.getAttachments() != null) {
            project.setAttachments(request.getAttachments());
        }

        Project saved = projectRepository.save(project);
        return mapToResponse(saved, null);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getClientProjects(String clientEmail) {
        User client = userRepository.findByEmail(clientEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientEmail));
        List<Project> projects = projectRepository.findByClientIdOrderByCreatedAtDesc(client.getId());
        return projects.stream().map(p -> mapToResponse(p, client.getId())).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientDashboardOverview getClientOverview(String clientEmail) {
        User client = userRepository.findByEmail(clientEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientEmail));

        List<Project> clientProjects = projectRepository.findByClientIdOrderByCreatedAtDesc(client.getId());
        long active = clientProjects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS || p.getStatus() == ProjectStatus.SUBMITTED || p.getStatus() == ProjectStatus.REVISION_REQUESTED)
                .count();
        long open = clientProjects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.OPEN || p.getStatus() == ProjectStatus.APPLICATION_RECEIVED)
                .count();
        long completed = clientProjects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.COMPLETED)
                .count();

        long pendingApps = 0;
        for (Project p : clientProjects) {
            pendingApps += applicationRepository.countByProjectIdAndStatus(p.getId(), ApplicationStatus.PENDING);
        }

        long unreadMessages = chatMessageRepository.countByRecipientIdAndIsReadFalse(client.getId());

        ClientDashboardOverview overview = new ClientDashboardOverview();
        overview.setActiveProjectsCount(active);
        overview.setOpenProjectsCount(open);
        overview.setPendingApplicationsCount(pendingApps);
        overview.setCompletedProjectsCount(completed);
        overview.setUnreadMessagesCount(unreadMessages);
        overview.setRecentProjects(clientProjects.stream().limit(5).map(p -> mapToResponse(p, client.getId())).collect(Collectors.toList()));
        return overview;
    }

    @Transactional
    public ProjectResponse hireCreator(Long applicationId, String clientEmail) {
        ProjectApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        Project project = app.getProject();
        User client = userRepository.findByEmail(clientEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + clientEmail));

        // Authorization: only project owner can hire
        if (!project.getClient().getId().equals(client.getId()) && client.getRole() != Role.ROLE_ADMIN) {
            throw new BadRequestException("You are not authorized to hire for this project.");
        }

        // Validate state
        if (project.getStatus() != ProjectStatus.OPEN && project.getStatus() != ProjectStatus.APPLICATION_RECEIVED) {
            throw new ConflictException("Project is already assigned or closed: status is " + project.getStatus());
        }

        // Assign creator and set project to IN_PROGRESS
        project.setSelectedCreator(app.getCreator());
        project.setStatus(ProjectStatus.IN_PROGRESS);
        app.setStatus(ApplicationStatus.ACCEPTED);

        // Reject other pending applications
        List<ProjectApplication> otherApps = applicationRepository.findByProjectIdOrderByCreatedAtDesc(project.getId());
        for (ProjectApplication other : otherApps) {
            if (!other.getId().equals(app.getId()) && other.getStatus() == ApplicationStatus.PENDING) {
                other.setStatus(ApplicationStatus.REJECTED);
            }
        }
        projectRepository.save(project);
        applicationRepository.save(app);

        // Kafka Event to Creator: Hired!
        NotificationEvent notif = new NotificationEvent(
                app.getCreator().getId(),
                app.getCreator().getEmail(),
                "Hired for Project!",
                "Congratulations! You were hired by " + client.getFullName() + " for project '" + project.getTitle() + "'.",
                "PROJECT_ASSIGNED"
        );
        kafkaEventProducer.publishNotificationEvent(notif);

        // Publish Order/Project Event to Kafka
        OrderEvent orderEvent = new OrderEvent(
                project.getId(),
                project.getId(),
                project.getTitle(),
                client.getId(),
                client.getFullName(),
                app.getCreator().getId(),
                app.getCreator().getFullName(),
                app.getProposedPrice(),
                "ORDER_ACCEPTED",
                "Project assigned to creator"
        );
        kafkaEventProducer.publishOrderEvent(orderEvent);

        return mapToResponse(project, client.getId());
    }

    @Transactional
    public ProposalResponse rejectApplication(Long applicationId, String clientEmail) {
        ProjectApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        User client = userRepository.findByEmail(clientEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + clientEmail));

        if (!app.getProject().getClient().getId().equals(client.getId()) && client.getRole() != Role.ROLE_ADMIN) {
            throw new BadRequestException("Not authorized to manage applications for this project.");
        }

        app.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(app);

        NotificationEvent notif = new NotificationEvent(
                app.getCreator().getId(),
                app.getCreator().getEmail(),
                "Application Update",
                "Your proposal for project '" + app.getProject().getTitle() + "' was not selected.",
                "APPLICATION_REJECTED"
        );
        kafkaEventProducer.publishNotificationEvent(notif);

        return mapToProposalResponse(app);
    }

    @Transactional
    public ProjectResponse requestRevision(Long projectId, RevisionRequest request, String clientEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        User client = userRepository.findByEmail(clientEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + clientEmail));

        if (!project.getClient().getId().equals(client.getId()) && client.getRole() != Role.ROLE_ADMIN) {
            throw new BadRequestException("Only the project client can request revisions.");
        }

        if (project.getStatus() != ProjectStatus.SUBMITTED) {
            throw new ConflictException("Revisions can only be requested when work has been submitted.");
        }

        project.setStatus(ProjectStatus.REVISION_REQUESTED);
        project.setRevisionNotes(request.getRevisionNotes());
        projectRepository.save(project);

        if (project.getSelectedCreator() != null) {
            NotificationEvent notif = new NotificationEvent(
                    project.getSelectedCreator().getId(),
                    project.getSelectedCreator().getEmail(),
                    "Revision Requested",
                    client.getFullName() + " requested revisions on project '" + project.getTitle() + "': " + request.getRevisionNotes(),
                    "REVISION_REQUESTED"
            );
            kafkaEventProducer.publishNotificationEvent(notif);
        }

        return mapToResponse(project, client.getId());
    }

    @Transactional
    public ProjectResponse approveWork(Long projectId, String clientEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        User client = userRepository.findByEmail(clientEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + clientEmail));

        if (!project.getClient().getId().equals(client.getId()) && client.getRole() != Role.ROLE_ADMIN) {
            throw new BadRequestException("Only the project client can approve work.");
        }

        if (project.getStatus() != ProjectStatus.SUBMITTED) {
            throw new ConflictException("Project can only be approved after work has been submitted.");
        }

        project.setStatus(ProjectStatus.COMPLETED);
        projectRepository.save(project);

        if (project.getSelectedCreator() != null) {
            // Payment Event released via Kafka
            PaymentEvent paymentEvent = new PaymentEvent(
                    project.getId(),
                    project.getBudget(),
                    "TXN-" + System.currentTimeMillis(),
                    client.getEmail()
            );
            kafkaEventProducer.publishPaymentEvent(paymentEvent);

            // Notification Event
            NotificationEvent notif = new NotificationEvent(
                    project.getSelectedCreator().getId(),
                    project.getSelectedCreator().getEmail(),
                    "Project Approved & Payment Released!",
                    client.getFullName() + " approved your work on '" + project.getTitle() + "'! Payment of ₹" + project.getBudget() + " has been released.",
                    "PROJECT_APPROVED"
            );
            kafkaEventProducer.publishNotificationEvent(notif);
        }

        return mapToResponse(project, client.getId());
    }

    // =========================================================================
    // CREATOR ACTIONS
    // =========================================================================

    @Transactional
    public ProposalResponse applyToProject(Long projectId, ProposalRequest request, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found: " + creatorEmail));

        // Enforce role: Only Creator can apply
        if (creator.getRole() == Role.ROLE_CLIENT) {
            throw new BadRequestException("Clients cannot apply to projects. Switch to Creator profile to send proposals.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        // State validation
        if (project.getStatus() != ProjectStatus.OPEN && project.getStatus() != ProjectStatus.APPLICATION_RECEIVED) {
            throw new ConflictException("This project is no longer accepting applications (Current status: " + project.getStatus() + ").");
        }

        // Prevent duplicate applications
        if (applicationRepository.existsByProjectIdAndCreatorId(projectId, creator.getId())) {
            throw new ConflictException("You have already submitted a proposal for this project.");
        }

        ProjectApplication application = new ProjectApplication(
                project,
                creator,
                request.getCoverLetter().trim(),
                request.getProposedPrice(),
                request.getEstimatedDays(),
                request.getRelevantExperience()
        );
        if (request.getAttachments() != null) {
            application.setAttachments(request.getAttachments());
        }

        ProjectApplication savedApp = applicationRepository.save(application);

        // Update project status to APPLICATION_RECEIVED if currently OPEN
        if (project.getStatus() == ProjectStatus.OPEN) {
            project.setStatus(ProjectStatus.APPLICATION_RECEIVED);
            projectRepository.save(project);
        }

        // Kafka Event to Client: New Creator Application
        NotificationEvent notif = new NotificationEvent(
                project.getClient().getId(),
                project.getClient().getEmail(),
                "New Creator Application",
                creator.getFullName() + " submitted a proposal (₹" + request.getProposedPrice() + ") for your project '" + project.getTitle() + "'.",
                "NEW_APPLICATION"
        );
        kafkaEventProducer.publishNotificationEvent(notif);

        return mapToProposalResponse(savedApp);
    }

    @Transactional(readOnly = true)
    public List<ProposalResponse> getCreatorApplications(String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found: " + creatorEmail));
        List<ProjectApplication> apps = applicationRepository.findByCreatorIdOrderByCreatedAtDesc(creator.getId());
        return apps.stream().map(this::mapToProposalResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAssignedProjectsForCreator(String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found: " + creatorEmail));
        List<Project> projects = projectRepository.findBySelectedCreatorIdOrderByCreatedAtDesc(creator.getId());
        return projects.stream().map(p -> mapToResponse(p, creator.getId())).collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse submitWork(Long projectId, WorkSubmissionRequest request, String creatorEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        User creator = userRepository.findByEmail(creatorEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + creatorEmail));

        // Verify caller is assigned creator
        if (project.getSelectedCreator() == null || !project.getSelectedCreator().getId().equals(creator.getId())) {
            throw new BadRequestException("You are not the assigned creator for this project.");
        }

        if (project.getStatus() != ProjectStatus.IN_PROGRESS && project.getStatus() != ProjectStatus.REVISION_REQUESTED) {
            throw new ConflictException("Work can only be submitted for in-progress or revision-requested projects.");
        }

        project.setStatus(ProjectStatus.SUBMITTED);
        project.setSubmissionNotes(request.getSubmissionNotes());
        project.setSubmissionUrl(request.getSubmissionUrl());
        projectRepository.save(project);

        // Kafka Event to Client
        NotificationEvent notif = new NotificationEvent(
                project.getClient().getId(),
                project.getClient().getEmail(),
                "Work Submitted for Review",
                creator.getFullName() + " has submitted completed work for project '" + project.getTitle() + "'. Please review and approve.",
                "WORK_SUBMITTED"
        );
        kafkaEventProducer.publishNotificationEvent(notif);

        return mapToResponse(project, creator.getId());
    }

    @Transactional(readOnly = true)
    public CreatorDashboardOverview getCreatorOverview(String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found: " + creatorEmail));

        long availableProjects = projectRepository.countByStatus(ProjectStatus.OPEN) +
                projectRepository.countByStatus(ProjectStatus.APPLICATION_RECEIVED);
        long myApplications = applicationRepository.countByCreatorId(creator.getId());

        List<Project> assignedProjects = projectRepository.findBySelectedCreatorIdOrderByCreatedAtDesc(creator.getId());
        long active = assignedProjects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS || p.getStatus() == ProjectStatus.SUBMITTED || p.getStatus() == ProjectStatus.REVISION_REQUESTED)
                .count();
        long completed = assignedProjects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.COMPLETED)
                .count();
        long unreadMessages = chatMessageRepository.countByRecipientIdAndIsReadFalse(creator.getId());

        CreatorDashboardOverview overview = new CreatorDashboardOverview();
        overview.setAvailableProjectsCount(availableProjects);
        overview.setMyApplicationsCount(myApplications);
        overview.setActiveProjectsCount(active);
        overview.setCompletedProjectsCount(completed);
        overview.setUnreadMessagesCount(unreadMessages);
        overview.setActiveProjects(assignedProjects.stream()
                .filter(p -> p.getStatus() != ProjectStatus.COMPLETED && p.getStatus() != ProjectStatus.CANCELLED)
                .map(p -> mapToResponse(p, creator.getId()))
                .collect(Collectors.toList()));
        return overview;
    }

    // =========================================================================
    // SHARED / DISCOVERY ACTIONS
    // =========================================================================

    @Transactional(readOnly = true)
    public List<ProjectResponse> searchProjects(String category, String search, String skills,
                                                BigDecimal minBudget, BigDecimal maxBudget,
                                                String experienceLevel, String sortBy, String currentEmail) {
        Long currentUserId = null;
        if (currentEmail != null && !currentEmail.isBlank()) {
            currentUserId = userRepository.findByEmail(currentEmail.toLowerCase())
                    .map(User::getId).orElse(null);
        }

        Specification<Project> spec = (root, query, cb) -> cb.conjunction();

        if (category != null && !category.equalsIgnoreCase("All") && !category.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
        }
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("requiredSkills")), pattern)
            ));
        }
        if (skills != null && !skills.isBlank()) {
            String pattern = "%" + skills.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("requiredSkills")), pattern));
        }
        if (experienceLevel != null && !experienceLevel.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("experienceLevel")), experienceLevel.trim().toLowerCase()));
        }
        if (minBudget != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("budget"), minBudget));
        }
        if (maxBudget != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("budget"), maxBudget));
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("budget_asc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "budget");
        } else if ("budget_desc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "budget");
        }

        List<Project> projects = projectRepository.findAll(spec, sort);
        final Long userId = currentUserId;
        return projects.stream().map(p -> mapToResponse(p, userId)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id, String currentEmail) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        Long currentUserId = null;
        if (currentEmail != null && !currentEmail.isBlank()) {
            currentUserId = userRepository.findByEmail(currentEmail.toLowerCase())
                    .map(User::getId).orElse(null);
        }
        return mapToResponse(project, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<ProposalResponse> getProjectApplications(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        User user = userRepository.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Client owner or Admin can view all proposals; Creator can only view their own
        if (project.getClient().getId().equals(user.getId()) || user.getRole() == Role.ROLE_ADMIN) {
            return applicationRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                    .stream().map(this::mapToProposalResponse).collect(Collectors.toList());
        } else {
            return applicationRepository.findByProjectIdAndCreatorId(projectId, user.getId())
                    .stream().map(this::mapToProposalResponse).collect(Collectors.toList());
        }
    }

    // =========================================================================
    // MAPPERS
    // =========================================================================

    private ProjectResponse mapToResponse(Project project, Long currentUserId) {
        ProjectResponse resp = new ProjectResponse();
        resp.setId(project.getId());
        resp.setTitle(project.getTitle());
        resp.setDescription(project.getDescription());
        resp.setCategory(project.getCategory());
        resp.setClientId(project.getClient().getId());
        resp.setClientName(project.getClient().getFullName());

        clientProfileRepository.findByUserId(project.getClient().getId())
                .ifPresent(p -> resp.setClientCompany(p.getCompanyName()));

        if (project.getSelectedCreator() != null) {
            resp.setSelectedCreatorId(project.getSelectedCreator().getId());
            resp.setSelectedCreatorName(project.getSelectedCreator().getFullName());
        }

        resp.setBudget(project.getBudget());
        resp.setDeadlineDays(project.getDeadlineDays());
        resp.setExperienceLevel(project.getExperienceLevel());
        resp.setRequiredSkills(project.getRequiredSkills());
        resp.setAttachments(project.getAttachments());
        resp.setStatus(project.getStatus());
        resp.setSubmissionNotes(project.getSubmissionNotes());
        resp.setSubmissionUrl(project.getSubmissionUrl());
        resp.setRevisionNotes(project.getRevisionNotes());
        resp.setCreatedAt(project.getCreatedAt());
        resp.setUpdatedAt(project.getUpdatedAt());

        resp.setApplicationCount(project.getApplications() != null ? project.getApplications().size() : 0);

        if (currentUserId != null) {
            resp.setHasApplied(applicationRepository.existsByProjectIdAndCreatorId(project.getId(), currentUserId));
        }

        return resp;
    }

    private ProposalResponse mapToProposalResponse(ProjectApplication app) {
        ProposalResponse resp = new ProposalResponse();
        resp.setId(app.getId());
        resp.setProjectId(app.getProject().getId());
        resp.setProjectTitle(app.getProject().getTitle());
        resp.setClientId(app.getProject().getClient().getId());
        resp.setClientName(app.getProject().getClient().getFullName());
        resp.setCreatorId(app.getCreator().getId());
        resp.setCreatorName(app.getCreator().getFullName());
        resp.setCoverLetter(app.getCoverLetter());
        resp.setProposedPrice(app.getProposedPrice());
        resp.setEstimatedDays(app.getEstimatedDays());
        resp.setRelevantExperience(app.getRelevantExperience());
        resp.setAttachments(app.getAttachments());
        resp.setStatus(app.getStatus());
        resp.setCreatedAt(app.getCreatedAt());

        freelancerProfileRepository.findByUserId(app.getCreator().getId()).ifPresent(fp -> {
            resp.setCreatorHeadline(fp.getHeadline());
            resp.setCreatorRating(fp.getRatingAvg());
            resp.setCreatorSkills(fp.getSkills());
        });
        resp.setCreatorAvatar(app.getCreator().getAvatarUrl());
        resp.setCreatorCompletedProjects(projectRepository.findBySelectedCreatorIdOrderByCreatedAtDesc(app.getCreator().getId())
                .stream().filter(p -> p.getStatus() == ProjectStatus.COMPLETED).count());

        return resp;
    }
}
