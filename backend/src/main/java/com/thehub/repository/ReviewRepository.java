package com.thehub.repository;

import com.thehub.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByServiceIdOrderByCreatedAtDesc(Long serviceId);
    List<Review> findByFreelancerIdOrderByCreatedAtDesc(Long freelancerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.freelancer.id = :freelancerId")
    Double calculateAverageRatingForFreelancer(@Param("freelancerId") Long freelancerId);

    long countByFreelancerId(Long freelancerId);
}
