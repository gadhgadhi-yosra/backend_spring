package com.elfaddoui.backend.favorite.controller;

import com.elfaddoui.backend.favorite.dto.FavoriteResponse;
import com.elfaddoui.backend.favorite.dto.FavoriteToggleResponse;
import com.elfaddoui.backend.favorite.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public List<FavoriteResponse> getFavorites(Authentication authentication) {
        return favoriteService.getFavorites(authentication.getName());
    }

    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriteResponse add(Authentication authentication, @PathVariable Long productId) {
        return favoriteService.add(authentication.getName(), productId);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(Authentication authentication, @PathVariable Long productId) {
        favoriteService.remove(authentication.getName(), productId);
    }

    @PostMapping("/{productId}/toggle")
    public FavoriteToggleResponse toggle(Authentication authentication, @PathVariable Long productId) {
        return favoriteService.toggle(authentication.getName(), productId);
    }
}
