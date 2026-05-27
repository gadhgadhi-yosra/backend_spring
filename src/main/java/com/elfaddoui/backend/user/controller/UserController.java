package com.elfaddoui.backend.user.controller;

import com.elfaddoui.backend.user.dto.UserMeResponse;
import com.elfaddoui.backend.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserMeResponse me(Authentication auth) {
        return userService.me(auth.getName()); // email
    }
}