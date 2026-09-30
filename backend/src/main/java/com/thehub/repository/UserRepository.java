package com.thehub.repository;

import com.thehub.entity.Role;
import com.thehub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByFullNameIgnoreCase(String fullName);
    Boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    long countByRole(Role role);
}
