package com.thehub.service;

import com.thehub.dto.UserProfileResponse;
import com.thehub.entity.User;
import com.thehub.repository.ServiceRepository;
import com.thehub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;

    public AdminService(UserRepository userRepository, ServiceRepository serviceRepository) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            UserProfileResponse dto = new UserProfileResponse();
            dto.setId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setFullName(user.getFullName());
            dto.setRole(user.getRole().name());
            dto.setAvatarUrl(user.getAvatarUrl());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void toggleServiceStatus(Long serviceId) {
        serviceRepository.findById(serviceId).ifPresent(s -> {
            s.setActive(!s.getActive());
            serviceRepository.save(s);
        });
    }
}
