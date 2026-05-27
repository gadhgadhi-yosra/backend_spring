package com.elfaddoui.backend.auth.service;

import com.elfaddoui.backend.auth.dto.*;
import com.elfaddoui.backend.exception.UnauthorizedException;
import com.elfaddoui.backend.security.JwtService;
import com.elfaddoui.backend.user.entity.Role;
import com.elfaddoui.backend.user.entity.User;
import com.elfaddoui.backend.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final long OTP_TTL_SECONDS = 5 * 60;
    private static final long RESET_TTL_SECONDS = 15 * 60;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwt;
    private final ResetOtpDeliveryService resetOtpDeliveryService;
    private final SecureRandom random = new SecureRandom();

    public AuthService(
            UserRepository repo,
            BCryptPasswordEncoder encoder,
            JwtService jwt,
            ResetOtpDeliveryService resetOtpDeliveryService
    ) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwt = jwt;
        this.resetOtpDeliveryService = resetOtpDeliveryService;
    }

    public AuthResponse register(RegisterRequest req) {
        String email = normalizeEmail(req.email());
        if (repo.existsByEmail(email)) throw new IllegalStateException("Email already exists");

        User u = new User(req.fullName().trim(), email, encoder.encode(req.password()));
        u.setRoles(Set.of(Role.CLIENT));
        repo.save(u);

        return new AuthResponse(jwt.generate(u.getEmail()));
    }

    public AuthResponse login(LoginRequest req) {
        String email = normalizeEmail(req.email());
        User u = repo.findByEmail(email).orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!encoder.matches(req.password(), u.getPasswordHash())) throw new UnauthorizedException("Invalid credentials");
        if (!u.isEnabled()) throw new UnauthorizedException("Account disabled");

        return new AuthResponse(jwt.generate(u.getEmail()));
    }

    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest req) {
        String email = normalizeEmail(req.email());

        repo.findByEmail(email).ifPresent(user -> {
            String otp = generateOtp6Digits();
            log.info("[OTP DEBUG] {} -> {}", email, otp);

            user.setResetOtpHash(encoder.encode(otp));
            user.setResetOtpExpiresAt(Instant.now().plusSeconds(5 * 60));
            user.setResetOtpAttempts(0);
            user.setResetToken(null);
            user.setResetTokenExpiresAt(null);

            repo.save(user);
            try {
                resetOtpDeliveryService.sendResetOtp(email, otp);
            } catch (Exception ex) {
                log.error("OTP delivery failed for {}", email, ex);
            }
        });

        return new ForgotPasswordResponse("If this email exists, an OTP has been sent.");
    }

    public VerifyResetOtpResponse verifyResetOtp(VerifyResetOtpRequest req) {
        String email = normalizeEmail(req.email());
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));

        if (user.getResetOtpHash() == null || user.getResetOtpExpiresAt() == null) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        if (user.getResetOtpExpiresAt().isBefore(Instant.now())) {
            clearOtp(user);
            repo.save(user);
            throw new IllegalArgumentException("OTP expired");
        }

        int attempts = user.getResetOtpAttempts() == null ? 0 : user.getResetOtpAttempts();
        if (attempts >= MAX_OTP_ATTEMPTS) {
            throw new IllegalArgumentException("Too many OTP attempts");
        }

        if (!encoder.matches(req.otp(), user.getResetOtpHash())) {
            user.setResetOtpAttempts(attempts + 1);
            repo.save(user);
            throw new IllegalArgumentException("Invalid OTP");
        }

        String token = randomToken();
        user.setResetToken(token);
        user.setResetTokenExpiresAt(Instant.now().plusSeconds(RESET_TTL_SECONDS));

        clearOtp(user);
        repo.save(user);

        return new VerifyResetOtpResponse(token);
    }

    public void resetPassword(ResetPasswordRequest req) {
        User u = repo.findByResetToken(req.resetToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));

        if (u.getResetTokenExpiresAt() == null || u.getResetTokenExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Reset token expired");
        }

        u.setPasswordHash(encoder.encode(req.newPassword()));
        u.setResetToken(null);
        u.setResetTokenExpiresAt(null);
        clearOtp(u);
        repo.save(u);
    }

    private void clearOtp(User user) {
        user.setResetOtpHash(null);
        user.setResetOtpExpiresAt(null);
        user.setResetOtpAttempts(null);
    }

    private String generateOtp6Digits() {
        int n = 100000 + random.nextInt(900000);
        return String.valueOf(n);
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
