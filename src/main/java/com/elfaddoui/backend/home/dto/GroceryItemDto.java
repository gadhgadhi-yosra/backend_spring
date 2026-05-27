package com.elfaddoui.backend.home.dto;

public record GroceryItemDto(
        String id,
        String name,
        String category,
        Integer quantity,
        String image
) {
}
