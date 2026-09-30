package com.thehub.service;

import com.thehub.dto.CreatorCardResponse;
import com.thehub.entity.FreelancerProfile;
import com.thehub.entity.ProjectStatus;
import com.thehub.entity.Role;
import com.thehub.entity.User;
import com.thehub.exception.ResourceNotFoundException;
import com.thehub.repository.FreelancerProfileRepository;
import com.thehub.repository.ProjectRepository;
import com.thehub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreatorDiscoveryService {

    private final UserRepository userRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final ProjectRepository projectRepository;

    public CreatorDiscoveryService(UserRepository userRepository,
                                   FreelancerProfileRepository freelancerProfileRepository,
                                   ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.freelancerProfileRepository = freelancerProfileRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<CreatorCardResponse> searchCreators(String search, String skill,
                                                    Double minRating, BigDecimal minRate, BigDecimal maxRate) {
        List<User> creators = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_FREELANCER || u.getRole() == Role.ROLE_CREATOR)
                .collect(Collectors.toList());

        List<CreatorCardResponse> responses = new ArrayList<>();

        for (User user : creators) {
            FreelancerProfile profile = freelancerProfileRepository.findByUserId(user.getId()).orElse(null);

            // Filtering
            if (search != null && !search.isBlank()) {
                String q = search.toLowerCase();
                boolean matchesName = user.getFullName().toLowerCase().contains(q);
                boolean matchesHeadline = profile != null && profile.getHeadline() != null && profile.getHeadline().toLowerCase().contains(q);
                boolean matchesBio = profile != null && profile.getBio() != null && profile.getBio().toLowerCase().contains(q);
                if (!matchesName && !matchesHeadline && !matchesBio) continue;
            }

            if (skill != null && !skill.isBlank() && !skill.equalsIgnoreCase("All")) {
                if (profile == null || profile.getSkills() == null || !profile.getSkills().toLowerCase().contains(skill.toLowerCase())) {
                    continue;
                }
            }

            if (minRating != null) {
                if (profile == null || profile.getRatingAvg() == null || profile.getRatingAvg() < minRating) {
                    continue;
                }
            }

            if (minRate != null && profile != null && profile.getHourlyRate() != null) {
                if (profile.getHourlyRate().compareTo(minRate) < 0) continue;
            }

            if (maxRate != null && profile != null && profile.getHourlyRate() != null) {
                if (profile.getHourlyRate().compareTo(maxRate) > 0) continue;
            }

            responses.add(mapToCreatorCard(user, profile));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public CreatorCardResponse getCreatorProfile(Long creatorId) {
        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found: " + creatorId));
        FreelancerProfile profile = freelancerProfileRepository.findByUserId(creatorId).orElse(null);
        return mapToCreatorCard(user, profile);
    }

    private CreatorCardResponse mapToCreatorCard(User user, FreelancerProfile profile) {
        CreatorCardResponse card = new CreatorCardResponse();
        card.setId(user.getId());
        card.setFullName(user.getFullName());
        card.setEmail(user.getEmail());
        card.setAvatarUrl(user.getAvatarUrl());

        if (profile != null) {
            card.setHeadline(profile.getHeadline());
            card.setBio(profile.getBio());
            card.setSkills(profile.getSkills());
            card.setHourlyRate(profile.getHourlyRate());
            card.setRatingAvg(profile.getRatingAvg());
            card.setRatingCount(profile.getRatingCount());
            card.setPortfolioUrl(profile.getPortfolioUrl());
            card.setAvailability(profile.getAvailability());
        }

        long completedCount = projectRepository.findBySelectedCreatorIdOrderByCreatedAtDesc(user.getId())
                .stream().filter(p -> p.getStatus() == ProjectStatus.COMPLETED).count();
        card.setCompletedProjects(completedCount);

        return card;
    }
}
