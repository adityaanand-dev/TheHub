package com.thehub.controller;

import com.thehub.dto.ReviewRequest;
import com.thehub.dto.ReviewResponse;
import com.thehub.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews", description = "Ratings and testimonials for completed gigs")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Submit a rating & review for a completed order")
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.addReview(request, userDetails.getUsername()));
    }

    @GetMapping("/service/{serviceId}")
    @Operation(summary = "Get all reviews for a specific service")
    public ResponseEntity<List<ReviewResponse>> getServiceReviews(@PathVariable Long serviceId) {
        return ResponseEntity.ok(reviewService.getReviewsForService(serviceId));
    }
}
