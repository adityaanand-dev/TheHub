package com.thehub.service;

import com.thehub.dto.OrderRequest;
import com.thehub.dto.OrderResponse;
import com.thehub.dto.OrderStatusUpdateRequest;
import com.thehub.dto.event.OrderEvent;
import com.thehub.entity.*;
import com.thehub.exception.BadRequestException;
import com.thehub.exception.ConflictException;
import com.thehub.exception.ResourceNotFoundException;
import com.thehub.kafka.KafkaEventProducer;
import com.thehub.repository.OrderRepository;
import com.thehub.repository.ServiceRepository;
import com.thehub.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final KafkaEventProducer kafkaEventProducer;
    private final PasswordEncoder passwordEncoder;

    public OrderService(OrderRepository orderRepository,
                        ServiceRepository serviceRepository,
                        UserRepository userRepository,
                        KafkaEventProducer kafkaEventProducer,
                        PasswordEncoder passwordEncoder) {
        this.orderRepository = orderRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.kafkaEventProducer = kafkaEventProducer;
        this.passwordEncoder = passwordEncoder;
    }

    @CacheEvict(value = "stats", allEntries = true)
    @Transactional
    public OrderResponse createOrder(OrderRequest request, String authenticatedEmail) {
        com.thehub.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Gig ID " + request.getServiceId() + " does not exist."));

        String email = (authenticatedEmail != null && !authenticatedEmail.isBlank())
                ? authenticatedEmail.trim().toLowerCase() : request.getClientEmail().trim().toLowerCase();

        User client = userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User(email, passwordEncoder.encode("client123"), request.getClientName(), Role.ROLE_CLIENT);
            return userRepository.save(u);
        });

        Order order = new Order(
                service,
                client,
                request.getClientName().trim(),
                email,
                service.getFreelancer(),
                request.getRequirements().trim(),
                service.getRate()
        );

        Order saved = orderRepository.save(order);

        // Publish event to Kafka
        OrderEvent event = new OrderEvent(
                UUID.randomUUID().toString(),
                OrderEvent.EventType.ORDER_CREATED,
                saved.getId(),
                service.getId(),
                service.getTitle(),
                client.getId(),
                client.getEmail(),
                client.getFullName(),
                service.getFreelancer() != null ? service.getFreelancer().getId() : null,
                service.getFreelancer() != null ? service.getFreelancer().getEmail() : null,
                service.getCreatorName(),
                saved.getPrice(),
                null
        );
        kafkaEventProducer.publishOrderEvent(event);

        return mapToResponse(saved);
    }

    @CacheEvict(value = "stats", allEntries = true)
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking ID " + orderId + " not found."));

        if (order.getStatus() != OrderStatus.Pending) {
            throw new ConflictException("Only pending bookings can be updated.");
        }

        String rawStatus = request.getStatus().trim();
        OrderStatus newStatus;
        if ("Accepted".equalsIgnoreCase(rawStatus)) {
            newStatus = OrderStatus.Accepted;
            order.setRejectionReason(null);
        } else if ("Declined".equalsIgnoreCase(rawStatus)) {
            newStatus = OrderStatus.Declined;
            order.setRejectionReason(request.getRejectionReason() != null ? request.getRejectionReason().trim() : null);
        } else {
            throw new BadRequestException("Invalid status transition: " + rawStatus);
        }

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        // Publish status transition event to Kafka
        OrderEvent.EventType eventType = (newStatus == OrderStatus.Accepted)
                ? OrderEvent.EventType.ORDER_ACCEPTED
                : OrderEvent.EventType.ORDER_DECLINED;

        OrderEvent event = new OrderEvent(
                UUID.randomUUID().toString(),
                eventType,
                updated.getId(),
                updated.getService().getId(),
                updated.getService().getTitle(),
                updated.getClient() != null ? updated.getClient().getId() : null,
                updated.getClientEmail(),
                updated.getClientName(),
                updated.getFreelancer() != null ? updated.getFreelancer().getId() : null,
                updated.getFreelancer() != null ? updated.getFreelancer().getEmail() : null,
                updated.getService().getCreatorName(),
                updated.getPrice(),
                updated.getRejectionReason()
        );
        kafkaEventProducer.publishOrderEvent(event);

        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getCreatorOrders(String creatorName, String creatorEmail) {
        List<Order> orders;
        if (creatorEmail != null && !creatorEmail.isBlank()) {
            User user = userRepository.findByEmail(creatorEmail.trim().toLowerCase()).orElse(null);
            if (user != null) {
                orders = orderRepository.findByFreelancerIdOrderByCreatedAtDesc(user.getId());
            } else {
                orders = orderRepository.findByServiceCreatorNameIgnoreCase(creatorName != null ? creatorName.trim() : "");
            }
        } else if (creatorName != null && !creatorName.isBlank()) {
            orders = orderRepository.findByServiceCreatorNameIgnoreCase(creatorName.trim());
        } else {
            orders = orderRepository.findAll();
        }

        return orders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getClientOrders(String clientName, String clientEmail) {
        List<Order> orders;
        if (clientEmail != null && !clientEmail.isBlank()) {
            orders = orderRepository.findByClientEmailIgnoreCaseOrderByCreatedAtDesc(clientEmail.trim().toLowerCase());
        } else if (clientName != null && !clientName.isBlank()) {
            orders = orderRepository.findByClientNameContainingIgnoreCaseOrderByCreatedAtDesc(clientName.trim());
        } else {
            orders = orderRepository.findAll();
        }
        return orders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.setId(order.getId());
        dto.setGigId(order.getService().getId());
        dto.setServiceId(order.getService().getId());
        dto.setClientName(order.getClientName());
        dto.setClientEmail(order.getClientEmail());
        dto.setRequirements(order.getRequirements());
        dto.setStatus(order.getStatus().name());
        dto.setRejectionReason(order.getRejectionReason());
        dto.setPrice(order.getPrice());
        dto.setGigTitle(order.getService().getTitle());
        dto.setCreatorName(order.getService().getCreatorName());
        dto.setCategory(order.getService().getCategoryName());
        dto.setRate(order.getPrice());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }
}
