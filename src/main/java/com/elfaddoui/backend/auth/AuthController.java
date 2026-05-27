package com.elfaddoui.backend.auth;

import com.elfaddoui.backend.auth.dto.*;
import com.elfaddoui.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return auth.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgot(@Valid @RequestBody ForgotPasswordRequest req) {
        return auth.forgotPassword(req);
    }

    @PostMapping("/verify-reset-otp")
    public VerifyResetOtpResponse verifyResetOtp(@Valid @RequestBody VerifyResetOtpRequest req) {
        return auth.verifyResetOtp(req);
    }

    @PostMapping("/reset-password")
    public void reset(@Valid @RequestBody ResetPasswordRequest req) {
        auth.resetPassword(req);
    }
}
