package com.elfaddoui.backend.profile.service;

import com.elfaddoui.backend.exception.NotFoundException;
import com.elfaddoui.backend.profile.dto.ChangePasswordRequest;
import com.elfaddoui.backend.profile.dto.ProfileResponse;
import com.elfaddoui.backend.profile.dto.UpdateProfileRequest;
import com.elfaddoui.backend.user.entity.User;
import com.elfaddoui.backend.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProfileService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getMe(String email) {
        return toResponse(findByEmail(email));
    }

    public ProfileResponse updateMe(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmailAndIdNot(normalizedEmail, user.getId())) {
            throw new IllegalStateException("Email already exists");
        }

        user.setFullName(request.fullName().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(normalizeNullable(request.phone()));
        user.setAvatarUrl(normalizeNullable(request.avatarUrl()));
        user.setAddress(normalizeNullable(request.address()));

        return toResponse(user);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findByEmail(email);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getAddress(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet())
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
