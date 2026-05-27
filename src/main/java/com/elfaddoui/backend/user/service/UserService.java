package com.elfaddoui.backend.user.service;

import com.elfaddoui.backend.user.dto.UserMeResponse;
import com.elfaddoui.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public UserMeResponse me(String email) {
        var u = repo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserMeResponse(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())
        );
    }
}