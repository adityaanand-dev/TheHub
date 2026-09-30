package com.thehub.controller;

import com.thehub.dto.OrderRequest;
import com.thehub.dto.OrderResponse;
import com.thehub.dto.OrderStatusUpdateRequest;
import com.thehub.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Marketplace booking proposals and project order lifecycles")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Submit a new booking request / project order")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : request.getClientEmail();
        OrderResponse response = orderService.createOrder(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Accept or Decline a booking (with DP1 rejection reason)")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }

    @GetMapping("/creator")
    @Operation(summary = "Get booking inquiries for creator workspace")
    public ResponseEntity<List<OrderResponse>> getCreatorOrders(
            @RequestParam(name = "creator_name", required = false) String creatorName,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(orderService.getCreatorOrders(creatorName, email));
    }

    @GetMapping("/client")
    @Operation(summary = "Get bookings for client workspace")
    public ResponseEntity<List<OrderResponse>> getClientOrders(
            @RequestParam(name = "client_name", required = false) String clientName,
            @RequestParam(name = "client_email", required = false) String clientEmail,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = (userDetails != null) ? userDetails.getUsername() : clientEmail;
        return ResponseEntity.ok(orderService.getClientOrders(clientName, email));
    }
}
