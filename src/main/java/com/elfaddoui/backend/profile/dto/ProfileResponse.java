package com.elfaddoui.backend.profile.dto;

import java.util.Set;

public record ProfileResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String avatarUrl,
        String address,
        Set<String> roles
) {
}
