package com.thehub.repository;

import com.thehub.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long>, JpaSpecificationExecutor<Service> {

    List<Service> findByFreelancerId(Long freelancerId);

    List<Service> findByCreatorNameIgnoreCase(String creatorName);

    long countByActiveTrue();
}

