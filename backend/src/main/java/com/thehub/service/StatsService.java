package com.thehub.service;

import com.thehub.dto.StatsResponse;
import com.thehub.entity.OrderStatus;
import com.thehub.repository.OrderRepository;
import com.thehub.repository.ServiceRepository;
import com.thehub.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class StatsService {

    private final ServiceRepository serviceRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public StatsService(ServiceRepository serviceRepository, OrderRepository orderRepository, UserRepository userRepository) {
        this.serviceRepository = serviceRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Cacheable(value = "stats", key = "'global'")
    @Transactional(readOnly = true)
    public StatsResponse getPlatformStats() {
        long totalGigs = serviceRepository.countByActiveTrue();
        long totalBookings = orderRepository.count();
        long pendingBookings = orderRepository.countByStatus(OrderStatus.Pending);
        long acceptedBookings = orderRepository.countByStatus(OrderStatus.Accepted)
                + orderRepository.countByStatus(OrderStatus.InProgress)
                + orderRepository.countByStatus(OrderStatus.Delivered)
                + orderRepository.countByStatus(OrderStatus.Completed);

        BigDecimal totalVolume = orderRepository.sumTotalAcceptedVolume();
        if (totalVolume == null) {
            totalVolume = BigDecimal.ZERO;
        }

        long totalUsers = userRepository.count();

        return new StatsResponse(totalGigs, totalBookings, pendingBookings, acceptedBookings, totalVolume, totalUsers);
    }
}
