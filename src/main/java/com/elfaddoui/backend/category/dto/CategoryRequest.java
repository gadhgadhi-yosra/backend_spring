package com.elfaddoui.backend.category.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CategoryRequest(
        @NotBlank(message = "must not be blank") String name,
        String key,
        String displayName,
        String imageUrl,
        Integer sortOrder,
        boolean isActive,
        boolean isPromo,
        boolean isBio,
        boolean isNew,
        boolean isPopular,
        List<String> customTags
) {
}
