package com.thehub.controller;

import com.thehub.dto.CreatorCardResponse;
import com.thehub.service.CreatorDiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/creators")
@Tag(name = "Creator Discovery", description = "Endpoints for Clients to discover, evaluate, and view Creator profiles")
public class CreatorDiscoveryController {

    private final CreatorDiscoveryService creatorDiscoveryService;

    public CreatorDiscoveryController(CreatorDiscoveryService creatorDiscoveryService) {
        this.creatorDiscoveryService = creatorDiscoveryService;
    }

    @GetMapping
    @Operation(summary = "Browse and search Creators with skill, rating, and hourly rate filters")
    public ResponseEntity<List<CreatorCardResponse>> searchCreators(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String skill,
            @RequestParam(name = "min_rating", required = false) Double minRating,
            @RequestParam(name = "min_rate", required = false) BigDecimal minRate,
            @RequestParam(name = "max_rate", required = false) BigDecimal maxRate
    ) {
        return ResponseEntity.ok(creatorDiscoveryService.searchCreators(search, skill, minRating, minRate, maxRate));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed Creator profile by ID")
    public ResponseEntity<CreatorCardResponse> getCreatorProfile(@PathVariable Long id) {
        return ResponseEntity.ok(creatorDiscoveryService.getCreatorProfile(id));
    }
}
