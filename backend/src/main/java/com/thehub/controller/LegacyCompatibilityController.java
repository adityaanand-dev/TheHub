package com.thehub.controller;

import com.thehub.config.DataInitializer;
import com.thehub.dto.*;
import com.thehub.service.OrderService;
import com.thehub.service.ServiceListingService;
import com.thehub.service.StatsService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
public class LegacyCompatibilityController {

    private final ServiceListingService serviceListingService;
    private final OrderService orderService;
    private final StatsService statsService;
    private final DataInitializer dataInitializer;

    public LegacyCompatibilityController(ServiceListingService serviceListingService,
                                         OrderService orderService,
                                         StatsService statsService,
                                         DataInitializer dataInitializer) {
        this.serviceListingService = serviceListingService;
        this.orderService = orderService;
        this.statsService = statsService;
        this.dataInitializer = dataInitializer;
    }

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
                "status", "Marketplace API operational",
                "service", "Creator Gig Marketplace",
                "version", "1.0.0",
                "hackathon", "Code2Career 2026"
        );
    }

    @GetMapping("/api/stats")
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(statsService.getPlatformStats());
    }

    @PostMapping("/api/gigs")
    public ResponseEntity<Map<String, Object>> postGig(@Valid @RequestBody ServiceRequest request) {
        ServiceResponse response = serviceListingService.createService(request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", response.getId(),
                "message", "Gig posted successfully"
        ));
    }

    @GetMapping("/api/gigs")
    public ResponseEntity<List<ServiceResponse>> getGigs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(name = "creator_name", required = false) String creatorName,
            @RequestParam(name = "min_rate", required = false) BigDecimal minRate,
            @RequestParam(name = "max_rate", required = false) BigDecimal maxRate,
            @RequestParam(name = "sort_by", defaultValue = "newest") String sortBy
    ) {
        return ResponseEntity.ok(serviceListingService.searchServices(category, search, creatorName, minRate, maxRate, sortBy));
    }

    @GetMapping("/api/gigs/{gigId}")
    public ResponseEntity<ServiceResponse> getGig(@PathVariable Long gigId) {
        return ResponseEntity.ok(serviceListingService.getServiceById(gigId));
    }

    @PatchMapping("/api/gigs/{gigId}")
    public ResponseEntity<Map<String, Object>> updateGig(@PathVariable Long gigId, @RequestBody ServiceRequest request) {
        serviceListingService.updateService(gigId, request);
        return ResponseEntity.ok(Map.of(
                "message", "Gig updated successfully",
                "id", gigId
        ));
    }

    @DeleteMapping("/api/gigs/{gigId}")
    public ResponseEntity<Map<String, String>> deleteGig(@PathVariable Long gigId) {
        serviceListingService.deleteService(gigId);
        return ResponseEntity.ok(Map.of("message", "Gig deleted successfully"));
    }

    @PostMapping("/api/bookings")
    public ResponseEntity<Map<String, Object>> bookGig(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request, request.getClientEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", response.getId(),
                "status", "Pending",
                "message", "Booking request submitted successfully"
        ));
    }

    @GetMapping("/api/creator/bookings")
    public ResponseEntity<List<OrderResponse>> getCreatorBookings(
            @RequestParam(name = "creator_name", required = false) String creatorName
    ) {
        return ResponseEntity.ok(orderService.getCreatorOrders(creatorName, null));
    }

    @PatchMapping({"/api/bookings/{bookingId}", "/api/bookings/{bookingId}/status"})
    public ResponseEntity<Map<String, Object>> updateBookingStatus(
            @PathVariable Long bookingId,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderResponse response = orderService.updateOrderStatus(bookingId, request);
        return ResponseEntity.ok(Map.of(
                "message", "Booking status updated to " + response.getStatus(),
                "status", response.getStatus()
        ));
    }

    @GetMapping("/api/client/bookings")
    public ResponseEntity<List<OrderResponse>> getClientBookings(
            @RequestParam(name = "client_name", required = false) String clientName,
            @RequestParam(name = "client_email", required = false) String clientEmail
    ) {
        return ResponseEntity.ok(orderService.getClientOrders(clientName, clientEmail));
    }

    @PostMapping("/api/seed")
    public Map<String, String> resetSeed() {
        dataInitializer.run();
        return Map.of("message", "Sample database successfully reset and seeded");
    }
}
