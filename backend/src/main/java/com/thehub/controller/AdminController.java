package com.thehub.controller;

import com.thehub.dto.UserProfileResponse;
import com.thehub.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administration controls for users, platform governance, and services")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @Operation(summary = "List all registered platform users")
    public ResponseEntity<List<UserProfileResponse>> getUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/services/{id}/toggle")
    @Operation(summary = "Activate or deactivate a gig listing")
    public ResponseEntity<Map<String, String>> toggleService(@PathVariable Long id) {
        adminService.toggleServiceStatus(id);
        return ResponseEntity.ok(Map.of("message", "Service status updated"));
    }
}
