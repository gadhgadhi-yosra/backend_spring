package com.elfaddoui.backend.profile.controller;

import com.elfaddoui.backend.profile.dto.ChangePasswordRequest;
import com.elfaddoui.backend.profile.dto.ProfileResponse;
import com.elfaddoui.backend.profile.dto.UpdateProfileRequest;
import com.elfaddoui.backend.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/profile", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ProfileResponse me(Authentication authentication) {
        return profileService.getMe(authentication.getName());
    }

    @PutMapping("/me")
    public ProfileResponse update(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        return profileService.updateMe(authentication.getName(), request);
    }

    @PutMapping("/password")
    public void changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(authentication.getName(), request);
    }
}
