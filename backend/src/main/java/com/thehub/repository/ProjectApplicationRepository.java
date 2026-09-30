package com.thehub.repository;

import com.thehub.entity.ApplicationStatus;
import com.thehub.entity.ProjectApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {

    List<ProjectApplication> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<ProjectApplication> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    Optional<ProjectApplication> findByProjectIdAndCreatorId(Long projectId, Long creatorId);

    boolean existsByProjectIdAndCreatorId(Long projectId, Long creatorId);

    long countByProjectIdAndStatus(Long projectId, ApplicationStatus status);

    long countByCreatorId(Long creatorId);
}
