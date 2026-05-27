package com.elfaddoui.backend.user.dto;

import java.util.Set;

public record UserMeResponse(
        Long id,
        String fullName,
        String email,
        Set<String> roles
) {}