package com.thehub.service;

import com.thehub.dto.ServiceRequest;
import com.thehub.dto.ServiceResponse;
import com.thehub.entity.Category;
import com.thehub.entity.Role;
import com.thehub.entity.Service;
import com.thehub.entity.User;
import com.thehub.exception.BadRequestException;
import com.thehub.exception.ConflictException;
import com.thehub.exception.ResourceNotFoundException;
import com.thehub.repository.CategoryRepository;
import com.thehub.repository.FreelancerProfileRepository;
import com.thehub.repository.OrderRepository;
import com.thehub.repository.ServiceRepository;
import com.thehub.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceListingService {

    private final ServiceRepository serviceRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public ServiceListingService(ServiceRepository serviceRepository,
                                 CategoryRepository categoryRepository,
                                 UserRepository userRepository,
                                 OrderRepository orderRepository,
                                 FreelancerProfileRepository freelancerProfileRepository,
                                 PasswordEncoder passwordEncoder) {
        this.serviceRepository = serviceRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.freelancerProfileRepository = freelancerProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> searchServices(String category, String search, String creatorName,
                                               BigDecimal minRate, BigDecimal maxRate, String sortBy) {
        String catFilter = (category != null && !category.equalsIgnoreCase("All") && !category.isBlank())
                ? category.trim() : null;
        String searchFilter = (search != null && !search.isBlank()) ? search.trim() : null;
        String creatorFilter = (creatorName != null && !creatorName.isBlank()) ? creatorName.trim() : null;

        Sort sort;
        if ("cheapest".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "rate");
        } else if ("priciest".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "rate");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Specification<Service> spec = (root, query, cb) -> cb.isTrue(root.get("active"));

        if (catFilter != null) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("categoryName")), catFilter.toLowerCase()));
        }
        if (searchFilter != null) {
            String pattern = "%" + searchFilter.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("creatorName")), pattern)
            ));
        }
        if (creatorFilter != null) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("creatorName")), creatorFilter.toLowerCase()));
        }
        if (minRate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("rate"), minRate));
        }
        if (maxRate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("rate"), maxRate));
        }

        List<Service> services = serviceRepository.findAll(spec, sort);
        return services.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Cacheable(value = "services", key = "#id")
    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gig with ID " + id + " not found"));
        return mapToResponse(service);
    }

    @CacheEvict(value = {"services", "stats"}, allEntries = true)
    @Transactional
    public ServiceResponse createService(ServiceRequest request, String authenticatedEmail) {
        User creator;
        if (authenticatedEmail != null && !authenticatedEmail.isBlank()) {
            creator = userRepository.findByEmail(authenticatedEmail.trim().toLowerCase())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authenticatedEmail));
        } else {
            // Support unauthenticated or demo creator posting from form
            String creatorName = (request.getCreatorName() != null && !request.getCreatorName().isBlank())
                    ? request.getCreatorName().trim() : "TheHub Creator";
            String generatedEmail = creatorName.toLowerCase().replaceAll("[^a-z0-9]", "") + "@thehub.com";
            creator = userRepository.findByEmail(generatedEmail).orElseGet(() -> {
                User u = new User(generatedEmail, passwordEncoder.encode("creator123"), creatorName, Role.ROLE_FREELANCER);
                return userRepository.save(u);
            });
        }

        Category category = categoryRepository.findByNameIgnoreCase(request.getCategory().trim()).orElse(null);

        Service service = new Service(
                creator,
                request.getCreatorName() != null && !request.getCreatorName().isBlank() ? request.getCreatorName().trim() : creator.getFullName(),
                category,
                request.getCategory().trim(),
                request.getTitle().trim(),
                request.getRate(),
                request.getDescription().trim(),
                request.getDeliveryDays()
        );

        Service saved = serviceRepository.save(service);
        return mapToResponse(saved);
    }

    @CacheEvict(value = "services", allEntries = true)
    @Transactional
    public ServiceResponse updateService(Long id, ServiceRequest request) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gig with ID " + id + " not found"));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            service.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            service.setDescription(request.getDescription().trim());
        }
        if (request.getRate() != null && request.getRate().compareTo(BigDecimal.ZERO) > 0) {
            service.setRate(request.getRate());
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            service.setCategoryName(request.getCategory().trim());
            categoryRepository.findByNameIgnoreCase(request.getCategory().trim()).ifPresent(service::setCategory);
        }
        if (request.getDeliveryDays() != null) {
            service.setDeliveryDays(request.getDeliveryDays());
        }

        Service updated = serviceRepository.save(service);
        return mapToResponse(updated);
    }

    @CacheEvict(value = {"services", "stats"}, allEntries = true)
    @Transactional
    public void deleteService(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gig with ID " + id + " not found"));

        long bookingCount = orderRepository.countByServiceId(id);
        if (bookingCount > 0) {
            throw new ConflictException("Booked gigs cannot be deleted because their order history is retained.");
        }

        serviceRepository.delete(service);
    }

    private ServiceResponse mapToResponse(Service service) {
        ServiceResponse dto = new ServiceResponse();
        dto.setId(service.getId());
        dto.setCreatorName(service.getCreatorName());
        dto.setTitle(service.getTitle());
        dto.setCategory(service.getCategoryName());
        dto.setRate(service.getRate());
        dto.setDescription(service.getDescription());
        dto.setDeliveryDays(service.getDeliveryDays());
        dto.setActive(service.getActive());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setFreelancerId(service.getFreelancer() != null ? service.getFreelancer().getId() : null);

        if (service.getFreelancer() != null) {
            freelancerProfileRepository.findByUserId(service.getFreelancer().getId()).ifPresent(fp -> {
                dto.setRatingAvg(fp.getRatingAvg());
                dto.setRatingCount(fp.getRatingCount());
            });
        }
        return dto;
    }
}
