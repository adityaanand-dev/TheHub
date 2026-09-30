package com.thehub.service;

import com.thehub.dto.*;
import com.thehub.entity.*;
import com.thehub.exception.BadRequestException;
import com.thehub.exception.ConflictException;
import com.thehub.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User clientUser;
    private User creatorUser;
    private User secondCreatorUser;

    @BeforeEach
    void setUp() {
        clientUser = userRepository.save(new User("testclient@thehub.dev", passwordEncoder.encode("secret"), "Alice Client", Role.ROLE_CLIENT));
        creatorUser = userRepository.save(new User("testcreator@thehub.dev", passwordEncoder.encode("secret"), "Bob Creator", Role.ROLE_FREELANCER));
        secondCreatorUser = userRepository.save(new User("creator2@thehub.dev", passwordEncoder.encode("secret"), "Charlie Creator", Role.ROLE_FREELANCER));
    }

    @Test
    @DisplayName("1. Client successfully creates project with status OPEN")
    void testClientCreatesProject() {
        ProjectCreateRequest req = new ProjectCreateRequest();
        req.setTitle("Next.js SaaS Dashboard");
        req.setDescription("Full-stack analytics dashboard with auth and charts.");
        req.setCategory("Tech & AI");
        req.setBudget(BigDecimal.valueOf(25000.0));
        req.setDeadlineDays(14);
        req.setExperienceLevel("Intermediate");
        req.setRequiredSkills("React, Next.js, Tailwind");

        ProjectResponse response = projectService.createProject(req, clientUser.getEmail());

        assertNotNull(response.getId());
        assertEquals("Next.js SaaS Dashboard", response.getTitle());
        assertEquals(ProjectStatus.OPEN, response.getStatus());
        assertEquals(clientUser.getId(), response.getClientId());
    }

    @Test
    @DisplayName("2. Creator role cannot create projects (Server-side enforcement)")
    void testCreatorCannotCreateProject() {
        ProjectCreateRequest req = new ProjectCreateRequest();
        req.setTitle("Disallowed Project");
        req.setDescription("Creators should not post projects.");
        req.setCategory("Tech & AI");
        req.setBudget(BigDecimal.valueOf(10000.0));
        req.setDeadlineDays(5);

        assertThrows(BadRequestException.class, () ->
                projectService.createProject(req, creatorUser.getEmail())
        );
    }

    @Test
    @DisplayName("3. Creator applies to Client project and transitions status to APPLICATION_RECEIVED")
    void testCreatorAppliesToProject() {
        Project project = projectRepository.save(new Project(
                "Mobile App UI Design", "Figma mobile app mockups", "Design & Graphics",
                clientUser, BigDecimal.valueOf(15000.0), 10, "Intermediate", "Figma, UI/UX"
        ));

        ProposalRequest req = new ProposalRequest();
        req.setCoverLetter("I have designed 20+ mobile apps in Figma.");
        req.setProposedPrice(BigDecimal.valueOf(14000.0));
        req.setEstimatedDays(8);
        req.setRelevantExperience("Fintech app lead designer.");

        ProposalResponse app = projectService.applyToProject(project.getId(), req, creatorUser.getEmail());

        assertNotNull(app.getId());
        assertEquals(ApplicationStatus.PENDING, app.getStatus());
        assertEquals(creatorUser.getId(), app.getCreatorId());

        Project updatedProject = projectRepository.findById(project.getId()).orElseThrow();
        assertEquals(ProjectStatus.APPLICATION_RECEIVED, updatedProject.getStatus());
    }

    @Test
    @DisplayName("4. Creator cannot submit duplicate proposal for same project")
    void testPreventDuplicateProposal() {
        Project project = projectRepository.save(new Project(
                "Mobile App UI Design", "Figma mobile app mockups", "Design & Graphics",
                clientUser, BigDecimal.valueOf(15000.0), 10, "Intermediate", "Figma, UI/UX"
        ));

        ProposalRequest req = new ProposalRequest();
        req.setCoverLetter("First proposal");
        req.setProposedPrice(BigDecimal.valueOf(14000.0));
        req.setEstimatedDays(8);

        projectService.applyToProject(project.getId(), req, creatorUser.getEmail());

        // Second proposal attempt by same creator
        assertThrows(ConflictException.class, () ->
                projectService.applyToProject(project.getId(), req, creatorUser.getEmail())
        );
    }

    @Test
    @DisplayName("5. Client role cannot apply to projects")
    void testClientCannotApplyToProject() {
        Project project = projectRepository.save(new Project(
                "Copywriting Gig", "Articles", "Writing & Translation",
                clientUser, BigDecimal.valueOf(5000.0), 3, "Entry", "Writing"
        ));

        ProposalRequest req = new ProposalRequest();
        req.setCoverLetter("Client trying to apply");
        req.setProposedPrice(BigDecimal.valueOf(5000.0));
        req.setEstimatedDays(3);

        assertThrows(BadRequestException.class, () ->
                projectService.applyToProject(project.getId(), req, clientUser.getEmail())
        );
    }

    @Test
    @DisplayName("6. Client hires Creator: transitions project to IN_PROGRESS, sets creator, accepts application")
    void testClientHiresCreator() {
        Project project = projectRepository.save(new Project(
                "Backend Architecture", "Spring Boot microservices", "Tech & AI",
                clientUser, BigDecimal.valueOf(40000.0), 20, "Expert", "Java, Kafka, Docker"
        ));

        ProposalRequest req1 = new ProposalRequest();
        req1.setCoverLetter("Creator 1 proposal");
        req1.setProposedPrice(BigDecimal.valueOf(38000.0));
        req1.setEstimatedDays(18);
        ProposalResponse app1 = projectService.applyToProject(project.getId(), req1, creatorUser.getEmail());

        ProposalRequest req2 = new ProposalRequest();
        req2.setCoverLetter("Creator 2 proposal");
        req2.setProposedPrice(BigDecimal.valueOf(40000.0));
        req2.setEstimatedDays(20);
        ProposalResponse app2 = projectService.applyToProject(project.getId(), req2, secondCreatorUser.getEmail());

        // Client hires Creator 1
        ProjectResponse hiredProject = projectService.hireCreator(app1.getId(), clientUser.getEmail());

        assertEquals(ProjectStatus.IN_PROGRESS, hiredProject.getStatus());
        assertEquals(creatorUser.getId(), hiredProject.getSelectedCreatorId());

        ProjectApplication app1Entity = applicationRepository.findById(app1.getId()).orElseThrow();
        assertEquals(ApplicationStatus.ACCEPTED, app1Entity.getStatus());

        ProjectApplication app2Entity = applicationRepository.findById(app2.getId()).orElseThrow();
        assertEquals(ApplicationStatus.REJECTED, app2Entity.getStatus());
    }

    @Test
    @DisplayName("7. Creator submits work, Client requests revision, Creator resubmits, Client approves (Full Lifecycle)")
    void testFullProjectLifecycle() {
        Project project = projectRepository.save(new Project(
                "Logo Redesign", "Vector logo", "Design & Graphics",
                clientUser, BigDecimal.valueOf(8000.0), 5, "Intermediate", "Illustrator"
        ));
        project.setSelectedCreator(creatorUser);
        project.setStatus(ProjectStatus.IN_PROGRESS);
        projectRepository.save(project);

        // Creator submits work
        WorkSubmissionRequest submitReq = new WorkSubmissionRequest();
        submitReq.setSubmissionNotes("Delivered 3 logo concepts and SVG assets.");
        submitReq.setSubmissionUrl("https://github.com/thehub/logo-delivery");
        ProjectResponse submitted = projectService.submitWork(project.getId(), submitReq, creatorUser.getEmail());
        assertEquals(ProjectStatus.SUBMITTED, submitted.getStatus());

        // Client requests revision
        RevisionRequest revReq = new RevisionRequest();
        revReq.setRevisionNotes("Please increase contrast on dark theme icon.");
        ProjectResponse revisionReq = projectService.requestRevision(project.getId(), revReq, clientUser.getEmail());
        assertEquals(ProjectStatus.REVISION_REQUESTED, revisionReq.getStatus());

        // Creator resubmits work
        submitReq.setSubmissionNotes("Updated high-contrast dark theme icon delivered.");
        ProjectResponse resubmitted = projectService.submitWork(project.getId(), submitReq, creatorUser.getEmail());
        assertEquals(ProjectStatus.SUBMITTED, resubmitted.getStatus());

        // Client approves work
        ProjectResponse approved = projectService.approveWork(project.getId(), clientUser.getEmail());
        assertEquals(ProjectStatus.COMPLETED, approved.getStatus());
    }
}
