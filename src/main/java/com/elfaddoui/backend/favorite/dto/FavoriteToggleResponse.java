package com.elfaddoui.backend.favorite.dto;

public record FavoriteToggleResponse(
        boolean favorite,
        FavoriteResponse item
) {
}
