package com.thehub.repository;

import com.thehub.entity.Order;
import com.thehub.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByFreelancerIdOrderByCreatedAtDesc(Long freelancerId);

    Page<Order> findByFreelancerIdOrderByCreatedAtDesc(Long freelancerId, Pageable pageable);

    @Query("SELECT o FROM Order o JOIN o.service s WHERE LOWER(s.creatorName) = LOWER(:creatorName) ORDER BY o.createdAt DESC")
    List<Order> findByServiceCreatorNameIgnoreCase(@Param("creatorName") String creatorName);

    List<Order> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<Order> findByClientEmailIgnoreCaseOrderByCreatedAtDesc(String clientEmail);

    List<Order> findByClientNameContainingIgnoreCaseOrderByCreatedAtDesc(String clientName);

    long countByServiceId(Long serviceId);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.price), 0) FROM Order o WHERE o.status IN (com.thehub.entity.OrderStatus.Accepted, com.thehub.entity.OrderStatus.InProgress, com.thehub.entity.OrderStatus.Delivered, com.thehub.entity.OrderStatus.Completed)")
    BigDecimal sumTotalAcceptedVolume();
}
