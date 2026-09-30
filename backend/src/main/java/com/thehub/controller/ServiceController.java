package com.thehub.controller;

import com.thehub.dto.ServiceRequest;
import com.thehub.dto.ServiceResponse;
import com.thehub.service.ServiceListingService;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Services", description = "Browse, search, and manage freelancer services / gigs")
public class ServiceController {

    private final ServiceListingService serviceListingService;

    public ServiceController(ServiceListingService serviceListingService) {
        this.serviceListingService = serviceListingService;
    }

    @GetMapping
    @Operation(summary = "Browse & search services with category, keyword, and price/recency sorting")
    public ResponseEntity<List<ServiceResponse>> getServices(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(name = "creator_name", required = false) String creatorName,
            @RequestParam(name = "min_rate", required = false) BigDecimal minRate,
            @RequestParam(name = "max_rate", required = false) BigDecimal maxRate,
            @RequestParam(name = "sort_by", defaultValue = "newest") String sortBy
    ) {
        return ResponseEntity.ok(serviceListingService.searchServices(category, search, creatorName, minRate, maxRate, sortBy));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single service details by ID")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceListingService.getServiceById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('FREELANCER', 'ADMIN')")
    @Operation(summary = "Create a new service listing (requires FREELANCER role)")
    public ResponseEntity<ServiceResponse> createService(
            @Valid @RequestBody ServiceRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceListingService.createService(request, email));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('FREELANCER', 'ADMIN')")
    @Operation(summary = "Update an existing service listing")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable Long id,
            @RequestBody ServiceRequest request
    ) {
        return ResponseEntity.ok(serviceListingService.updateService(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('FREELANCER', 'ADMIN')")
    @Operation(summary = "Delete an unbooked service listing")
    public ResponseEntity<Map<String, String>> deleteService(@PathVariable Long id) {
        serviceListingService.deleteService(id);
        return ResponseEntity.ok(Map.of("message", "Gig deleted successfully"));
    }
}
