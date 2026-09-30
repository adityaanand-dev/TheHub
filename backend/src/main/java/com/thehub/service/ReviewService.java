package com.thehub.service;

import com.thehub.dto.ReviewRequest;
import com.thehub.dto.ReviewResponse;
import com.thehub.entity.FreelancerProfile;
import com.thehub.entity.Order;
import com.thehub.entity.Review;
import com.thehub.entity.User;
import com.thehub.exception.BadRequestException;
import com.thehub.exception.ResourceNotFoundException;
import com.thehub.repository.FreelancerProfileRepository;
import com.thehub.repository.OrderRepository;
import com.thehub.repository.ReviewRepository;
import com.thehub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         OrderRepository orderRepository,
                         UserRepository userRepository,
                         FreelancerProfileRepository freelancerProfileRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.freelancerProfileRepository = freelancerProfileRepository;
    }

    @Transactional
    public ReviewResponse addReview(ReviewRequest request, String clientEmail) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

        User client = userRepository.findByEmail(clientEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientEmail));

        Review review = new Review(
                order,
                order.getService(),
                client,
                order.getFreelancer(),
                request.getRating(),
                request.getComment()
        );

        Review saved = reviewRepository.save(review);

        // Update freelancer rating
        if (order.getFreelancer() != null) {
            Double avgRating = reviewRepository.calculateAverageRatingForFreelancer(order.getFreelancer().getId());
            long count = reviewRepository.countByFreelancerId(order.getFreelancer().getId());

            freelancerProfileRepository.findByUserId(order.getFreelancer().getId()).ifPresent(fp -> {
                fp.setRatingAvg(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
                fp.setRatingCount((int) count);
                freelancerProfileRepository.save(fp);
            });
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForService(Long serviceId) {
        return reviewRepository.findByServiceIdOrderByCreatedAtDesc(serviceId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse dto = new ReviewResponse();
        dto.setId(review.getId());
        dto.setOrderId(review.getOrder().getId());
        dto.setServiceId(review.getService().getId());
        dto.setClientName(review.getClient().getFullName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
