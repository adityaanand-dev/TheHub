package com.thehub.repository;

import com.thehub.entity.Project;
import com.thehub.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    List<Project> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<Project> findBySelectedCreatorIdOrderByCreatedAtDesc(Long creatorId);

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    long countByClientId(Long clientId);

    long countBySelectedCreatorId(Long creatorId);

    long countByStatus(ProjectStatus status);
}
