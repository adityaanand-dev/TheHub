package com.thehub.config;

import com.thehub.entity.*;
import com.thehub.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final ProjectRepository projectRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           ServiceRepository serviceRepository,
                           FreelancerProfileRepository freelancerProfileRepository,
                           ClientProfileRepository clientProfileRepository,
                           ProjectRepository projectRepository,
                           ProjectApplicationRepository projectApplicationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.serviceRepository = serviceRepository;
        this.freelancerProfileRepository = freelancerProfileRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.projectRepository = projectRepository;
        this.projectApplicationRepository = projectApplicationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedCategories();
        seedUsers();
        seedSampleServices();
        seedSampleProjects();
    }

    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                    new Category("Video & UGC", "video-ugc", "Engaging user-generated content, reels, and video ads.", "video"),
                    new Category("Design & Graphics", "design-graphics", "Viral thumbnails, 3D renders, and complete branding kits.", "palette"),
                    new Category("Tech & AI", "tech-ai", "Full-stack apps, AI automation workflows, and custom scripts.", "cpu"),
                    new Category("Writing & Translation", "writing-translation", "Newsletters, copywriting, and SEO tech articles.", "pen-tool")
            ));
            logger.info("Initialized 4 core marketplace categories.");
        }
    }

    private void seedUsers() {
        // Admin
        if (userRepository.findByEmail("admin@thehub.com").isEmpty()) {
            User admin = new User("admin@thehub.com", passwordEncoder.encode("admin123"), "Platform Admin", Role.ROLE_ADMIN);
            userRepository.save(admin);
            logger.info("Created default administrator account: admin@thehub.com");
        }

        // Demo Client
        if (userRepository.findByEmail("client@thehub.com").isEmpty()) {
            User client = new User("client@thehub.com", passwordEncoder.encode("client123"), "Ava Johnson", Role.ROLE_CLIENT);
            userRepository.save(client);
            clientProfileRepository.save(new ClientProfile(client, "Aura Media Corp", "https://auramedia.io"));
            logger.info("Created demo client account: client@thehub.com");
        }

        // Demo Creator Alex Rivera
        createFreelancerIfNotExists("creator@thehub.com", "creator123", "Alex Rivera",
                "High-impact TikTok UGC Video Specialist", "Filming 4K conversion-focused product videos.", BigDecimal.valueOf(95.0));

        // Maya Chen
        createFreelancerIfNotExists("maya@thehub.com", "maya123", "Maya Chen",
                "3D Artist & YouTube CTR Specialist", "Designing viral high-contrast thumbnails.", BigDecimal.valueOf(45.0));

        // Dev Patel
        createFreelancerIfNotExists("dev@thehub.com", "dev123", "Dev Patel",
                "AI Automation & LLM Workflow Engineer", "Streamlining business operations with Zapier, Make, and OpenAI.", BigDecimal.valueOf(120.0));

        // Sarah Jenkins
        createFreelancerIfNotExists("sarah@thehub.com", "sarah123", "Sarah Jenkins",
                "Tech Copywriter & Newsletter Strategist", "Ghostwriting high-engagement founder newsletters.", BigDecimal.valueOf(60.0));

        // Marcus Brody
        createFreelancerIfNotExists("marcus@thehub.com", "marcus123", "Marcus Brody",
                "Audio Mastering & Short-Form Video Editor", "Mastering podcasts and cutting vertical social clips.", BigDecimal.valueOf(80.0));

        // Elena Rostova
        createFreelancerIfNotExists("elena@thehub.com", "elena123", "Elena Rostova",
                "Full-Stack MVP & Prototype Developer", "Building rapid interactive prototypes and dashboards.", BigDecimal.valueOf(150.0));
    }

    private User createFreelancerIfNotExists(String email, String rawPassword, String name, String headline, String bio, BigDecimal rate) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User(email, passwordEncoder.encode(rawPassword), name, Role.ROLE_FREELANCER);
            userRepository.save(user);
            freelancerProfileRepository.save(new FreelancerProfile(user, headline, bio, rate));
            return user;
        });
    }

    private void seedSampleServices() {
        if (serviceRepository.count() == 0) {
            Category videoCat = categoryRepository.findByNameIgnoreCase("Video & UGC").orElse(null);
            Category designCat = categoryRepository.findByNameIgnoreCase("Design & Graphics").orElse(null);
            Category techCat = categoryRepository.findByNameIgnoreCase("Tech & AI").orElse(null);
            Category writingCat = categoryRepository.findByNameIgnoreCase("Writing & Translation").orElse(null);

            User alex = userRepository.findByEmail("creator@thehub.com").orElse(null);
            User maya = userRepository.findByEmail("maya@thehub.com").orElse(null);
            User dev = userRepository.findByEmail("dev@thehub.com").orElse(null);
            User sarah = userRepository.findByEmail("sarah@thehub.com").orElse(null);
            User marcus = userRepository.findByEmail("marcus@thehub.com").orElse(null);
            User elena = userRepository.findByEmail("elena@thehub.com").orElse(null);

            if (alex != null && maya != null && dev != null && sarah != null && marcus != null && elena != null) {
                serviceRepository.saveAll(List.of(
                        new Service(alex, "Alex Rivera", videoCat, "Video & UGC",
                                "High-Converting TikTok & Reels UGC Video Ads",
                                BigDecimal.valueOf(95.0),
                                "Authentic, engaging user-generated content filmed in 4K. Includes hook ideation, caption scripts, and licensed trending audio.", 2),

                        new Service(maya, "Maya Chen", designCat, "Design & Graphics",
                                "Viral YouTube Thumbnails & Complete Branding Kit",
                                BigDecimal.valueOf(45.0),
                                "Custom 3D-rendered facial expressions, high-contrast typography, and CTR-tested layout guaranteed to lift your impressions.", 1),

                        new Service(dev, "Dev Patel", techCat, "Tech & AI",
                                "Custom AI Automation Workflow (Zapier / Make / OpenAI)",
                                BigDecimal.valueOf(120.0),
                                "Automate customer lead capture, email nurturing, and AI summarization without touching complex code. Includes 3 revisions.", 3),

                        new Service(sarah, "Sarah Jenkins", writingCat, "Writing & Translation",
                                "SEO-Optimized Tech & Founder Newsletters",
                                BigDecimal.valueOf(60.0),
                                "Deeply researched, entertaining newsletters tailored for Substack and Beehiiv readers. Boosts open rates with killer subject lines.", 2),

                        new Service(marcus, "Marcus Brody", videoCat, "Video & UGC",
                                "Podcast Audio Cleanup & Multi-Platform Social Clips",
                                BigDecimal.valueOf(80.0),
                                "Turn 1 hour raw audio/video into crystal-clear masters plus 5 vertical shorts with animated subtitles and sound effects.", 2),

                        new Service(elena, "Elena Rostova", techCat, "Tech & AI",
                                "Full-Stack MVP Landing Page in Streamlit / FastAPI",
                                BigDecimal.valueOf(150.0),
                                "Rapid interactive prototype deployed to cloud in 48 hours. Clean modern UI, responsive controls, and documented backend API.", 3)
                ));
                logger.info("Successfully seeded 6 initial marketplace services.");
            }
        }
    }

    private void seedSampleProjects() {
        if (projectRepository.count() == 0) {
            User client = userRepository.findByEmail("client@thehub.com").orElse(null);
            User alex = userRepository.findByEmail("creator@thehub.com").orElse(null);
            User maya = userRepository.findByEmail("maya@thehub.com").orElse(null);
            User dev = userRepository.findByEmail("dev@thehub.com").orElse(null);
            User marcus = userRepository.findByEmail("marcus@thehub.com").orElse(null);

            if (client != null) {
                // Project 1: Open for proposals
                Project p1 = new Project(
                        "React & Tailwind E-Commerce Platform",
                        "Need a modern, lightning-fast e-commerce frontend with product filters, shopping cart drawer, and Stripe checkout integration.",
                        "Tech & AI", client, BigDecimal.valueOf(35000.0), 20, "Intermediate", "React, Node.js, PostgreSQL, Tailwind CSS"
                );
                p1.setStatus(ProjectStatus.APPLICATION_RECEIVED);
                projectRepository.save(p1);

                if (alex != null) {
                    projectApplicationRepository.save(new ProjectApplication(
                            p1, alex,
                            "I have 4+ years building production React & Tailwind frontends. I can deliver a fluid, high-converting checkout experience within 18 days.",
                            BigDecimal.valueOf(32000.0), 18, "Built multiple high-traffic shopfronts with custom cart drawers."
                    ));
                }
                if (dev != null) {
                    projectApplicationRepository.save(new ProjectApplication(
                            p1, dev,
                            "Experienced full-stack engineer. I will implement clean state management and robust Stripe integration with full unit test coverage.",
                            BigDecimal.valueOf(35000.0), 20, "Ex-fintech engineer with extensive payment gateway experience."
                    ));
                }

                // Project 2: Video & UGC Project
                Project p2 = new Project(
                        "3D Product Animation & Social Video Ads",
                        "Looking for creative 3D product renders and rotating showcase clips for social media ads. Need vertical 9:16 and horizontal 16:9 formats.",
                        "Video & UGC", client, BigDecimal.valueOf(18000.0), 10, "Expert", "Blender, After Effects, 4K Filming, Color Grading"
                );
                p2.setStatus(ProjectStatus.APPLICATION_RECEIVED);
                projectRepository.save(p2);

                if (marcus != null) {
                    projectApplicationRepository.save(new ProjectApplication(
                            p2, marcus,
                            "Audio and video specialist. I can produce cinema-grade sound design, 3D animated text overlays, and high-impact pacing.",
                            BigDecimal.valueOf(17500.0), 8, "Edited and delivered over 100+ commercial video ads."
                    ));
                }

                // Project 3: Active Project with Hired Creator (Maya Chen)
                Project p3 = new Project(
                        "Viral YouTube Thumbnails & Complete Branding Kit",
                        "Design 5 high-CTR YouTube thumbnails with 3D typography and facial expressions, plus custom banner and logo assets.",
                        "Design & Graphics", client, BigDecimal.valueOf(8000.0), 5, "Intermediate", "Photoshop, Figma, 3D Typography"
                );
                p3.setStatus(ProjectStatus.IN_PROGRESS);
                if (maya != null) {
                    p3.setSelectedCreator(maya);
                }
                projectRepository.save(p3);

                // Project 4: Newsletter
                Project p4 = new Project(
                        "Tech Founder Weekly Newsletter & Copywriting",
                        "Looking for a skilled copywriter to research and draft 4 weekly editions of our founder newsletter covering AI and startup growth.",
                        "Writing & Translation", client, BigDecimal.valueOf(12000.0), 14, "Intermediate", "Copywriting, Substack, SEO, Startup Research"
                );
                p4.setStatus(ProjectStatus.OPEN);
                projectRepository.save(p4);

                // Project 5: AI Customer Support Bot
                Project p5 = new Project(
                        "AI Customer Support Agent (FastAPI / OpenAI)",
                        "Build an intelligent conversational bot connected to our product knowledge base using RAG, FastAPI, and OpenAI API.",
                        "Tech & AI", client, BigDecimal.valueOf(28000.0), 15, "Expert", "Python, FastAPI, OpenAI, LangChain, Docker"
                );
                p5.setStatus(ProjectStatus.OPEN);
                projectRepository.save(p5);

                logger.info("Successfully seeded 5 initial client marketplace projects and sample creator proposals.");
            }
        }
    }
}
