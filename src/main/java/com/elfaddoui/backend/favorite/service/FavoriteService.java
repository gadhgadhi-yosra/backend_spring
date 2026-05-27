package com.elfaddoui.backend.favorite.service;

import com.elfaddoui.backend.favorite.dto.FavoriteResponse;
import com.elfaddoui.backend.favorite.dto.FavoriteToggleResponse;

import java.util.List;

public interface FavoriteService {
    List<FavoriteResponse> getFavorites(String userEmail);
    FavoriteResponse add(String userEmail, Long productId);
    void remove(String userEmail, Long productId);
    FavoriteToggleResponse toggle(String userEmail, Long productId);
}
