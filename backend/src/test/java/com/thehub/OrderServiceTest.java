package com.thehub.service;

import com.thehub.dto.OrderRequest;
import com.thehub.dto.OrderResponse;
import com.thehub.dto.OrderStatusUpdateRequest;
import com.thehub.entity.OrderStatus;
import com.thehub.exception.ConflictException;
import com.thehub.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ServiceListingService serviceListingService;

    @Test
    void testCreateOrderAndAcceptance() {
        var services = serviceListingService.searchServices(null, null, null, null, null, "newest");
        assertFalse(services.isEmpty(), "Sample services should exist");
        Long serviceId = services.get(0).getId();

        OrderRequest request = new OrderRequest(serviceId, "Acme Brand", "brand@acme.com", "Need product promo video");
        OrderResponse response = orderService.createOrder(request, "brand@acme.com");

        assertNotNull(response.getId());
        assertEquals("Pending", response.getStatus());

        // Accept order
        OrderStatusUpdateRequest acceptReq = new OrderStatusUpdateRequest("Accepted", null);
        OrderResponse accepted = orderService.updateOrderStatus(response.getId(), acceptReq);
        assertEquals("Accepted", accepted.getStatus());
        assertNull(accepted.getRejectionReason());

        // Attempting to decline an already accepted booking should throw ConflictException (409)
        OrderStatusUpdateRequest declineReq = new OrderStatusUpdateRequest("Declined", "Already booked");
        assertThrows(ConflictException.class, () -> orderService.updateOrderStatus(response.getId(), declineReq));
    }

    @Test
    void testDeclineOrderWithReason_DP1() {
        var services = serviceListingService.searchServices(null, null, null, null, null, "newest");
        Long serviceId = services.get(0).getId();

        OrderRequest request = new OrderRequest(serviceId, "Nova Tech", "nova@tech.com", "Need website redesign");
        OrderResponse response = orderService.createOrder(request, "nova@tech.com");

        OrderStatusUpdateRequest declineReq = new OrderStatusUpdateRequest("Declined", "Currently booked for 2 weeks. Try our Design partners!");
        OrderResponse declined = orderService.updateOrderStatus(response.getId(), declineReq);

        assertEquals("Declined", declined.getStatus());
        assertEquals("Currently booked for 2 weeks. Try our Design partners!", declined.getRejectionReason());
    }

    @Test
    void testInvalidGigIdThrowsNotFound() {
        OrderRequest request = new OrderRequest(999999L, "Test", "test@test.com", "Reqs");
        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(request, "test@test.com"));
    }
}
