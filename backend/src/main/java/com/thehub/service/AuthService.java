package com.thehub.service;

import com.thehub.dto.AuthRequest;
import com.thehub.dto.AuthResponse;
import com.thehub.dto.RegisterRequest;
import com.thehub.dto.UserProfileResponse;
import com.thehub.entity.ClientProfile;
import com.thehub.entity.FreelancerProfile;
import com.thehub.entity.Role;
import com.thehub.entity.User;
import com.thehub.exception.BadRequestException;
import com.thehub.exception.ConflictException;
import com.thehub.exception.ResourceNotFoundException;
import com.thehub.repository.ClientProfileRepository;
import com.thehub.repository.FreelancerProfileRepository;
import com.thehub.repository.UserRepository;
import com.thehub.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       FreelancerProfileRepository freelancerProfileRepository,
                       ClientProfileRepository clientProfileRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.freelancerProfileRepository = freelancerProfileRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User already exists with email: " + email);
        }

        Role role = request.getRole() != null ? request.getRole() : Role.ROLE_CLIENT;
        User user = new User(email, passwordEncoder.encode(request.getPassword()), request.getFullName(), role);
        user = userRepository.save(user);

        if (role == Role.ROLE_FREELANCER) {
            FreelancerProfile profile = new FreelancerProfile(
                    user,
                    request.getHeadline() != null ? request.getHeadline() : "Freelancer on TheHub",
                    request.getBio() != null ? request.getBio() : "",
                    BigDecimal.valueOf(50.0)
            );
            freelancerProfileRepository.save(profile);
        } else if (role == Role.ROLE_CLIENT) {
            ClientProfile profile = new ClientProfile(user, request.getFullName(), "");
            clientProfileRepository.save(profile);
        }

        String token = tokenProvider.generateToken(user.getEmail(), user.getId(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole().name());
    }

    public AuthResponse login(AuthRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String token = tokenProvider.generateToken(authentication);
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentProfile(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole().name());
        response.setAvatarUrl(user.getAvatarUrl());

        if (user.getRole() == Role.ROLE_FREELANCER) {
            freelancerProfileRepository.findByUserId(user.getId()).ifPresent(fp -> {
                response.setHeadline(fp.getHeadline());
                response.setBio(fp.getBio());
                response.setHourlyRate(fp.getHourlyRate());
                response.setRatingAvg(fp.getRatingAvg());
                response.setRatingCount(fp.getRatingCount());
            });
        } else if (user.getRole() == Role.ROLE_CLIENT) {
            clientProfileRepository.findByUserId(user.getId()).ifPresent(cp -> {
                response.setCompanyName(cp.getCompanyName());
                response.setWebsite(cp.getWebsite());
            });
        }

        return response;
    }
}
