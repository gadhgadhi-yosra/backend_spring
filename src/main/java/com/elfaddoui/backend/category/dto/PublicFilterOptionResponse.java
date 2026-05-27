package com.elfaddoui.backend.category.dto;

public record PublicFilterOptionResponse(
        String key,
        String label,
        String param,
        String value,
        Integer count,
        boolean selected
) {
}
