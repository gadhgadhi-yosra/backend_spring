package com.elfaddoui.backend;

import com.elfaddoui.backend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Instant;
import java.util.Map;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends ApiIntegrationTestSupport {

    @MockBean
    private com.elfaddoui.backend.auth.service.ResetOtpDeliveryService resetOtpDeliveryService;

    @Test
    void registerReturnsJwtAndStoresNormalizedEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Yosra Test",
                                  "email": "YOSRA@Example.COM",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())));

        User created = userRepository.findByEmail("yosra@example.com").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(created.getFullName()).isEqualTo("Yosra Test");
        org.assertj.core.api.Assertions.assertThat(created.getEmail()).isEqualTo("yosra@example.com");
    }

    @Test
    void loginIsCaseInsensitiveOnEmailAndReturnsUnauthorizedForBadPassword() throws Exception {
        User user = new User("Client Test", "client@example.com", passwordEncoder.encode("Password123!"));
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "CLIENT@EXAMPLE.COM",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "client@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void loginTokenCanAccessProtectedCartAndFavoritesEndpoints() throws Exception {
        User user = new User("Client Test", "client@example.com", passwordEncoder.encode("Password123!"));
        userRepository.save(user);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "client@example.com",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/favorites")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointsAcceptBearerTokenWithCommonClientFormatting() throws Exception {
        User user = new User("Client Test", "client@example.com", passwordEncoder.encode("Password123!"));
        userRepository.save(user);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "client@example.com",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "bearer \"" + token + "\""))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/favorites")
                        .header("Authorization", "Bearer '" + token + "'"))
                .andExpect(status().isOk());
    }

    @Test
    void corsPreflightIsAcceptedForProtectedEndpoints() throws Exception {
        mockMvc.perform(options("/api/cart")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void forgotPasswordDoesNotEnumerateEmailAndStoresOtpMetadata() throws Exception {
        User user = new User("Reset Test", "reset@example.com", passwordEncoder.encode("OldPassword123!"));
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "reset@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If this email exists, an OTP has been sent."));

        User updated = userRepository.findByEmail("reset@example.com").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updated.getResetOtpHash()).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(updated.getResetOtpExpiresAt()).isAfter(Instant.now());
        org.assertj.core.api.Assertions.assertThat(updated.getResetOtpAttempts()).isZero();
        org.assertj.core.api.Assertions.assertThat(updated.getResetToken()).isNull();
        org.assertj.core.api.Assertions.assertThat(updated.getResetTokenExpiresAt()).isNull();

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "missing@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If this email exists, an OTP has been sent."));
    }

    @Test
    void forgotPasswordStillReturnsNeutralResponseWhenOtpDeliveryFails() throws Exception {
        User user = new User("Reset Mail Failure", "mailfail@example.com", passwordEncoder.encode("OldPassword123!"));
        userRepository.save(user);
        doThrow(new RuntimeException("SMTP timeout")).when(resetOtpDeliveryService).sendResetOtp(org.mockito.ArgumentMatchers.eq("mailfail@example.com"), org.mockito.ArgumentMatchers.anyString());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "mailfail@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If this email exists, an OTP has been sent."));

        User updated = userRepository.findByEmail("mailfail@example.com").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updated.getResetOtpHash()).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(updated.getResetOtpExpiresAt()).isAfter(Instant.now());
        org.assertj.core.api.Assertions.assertThat(updated.getResetOtpAttempts()).isZero();
    }

    @Test
    void verifyResetOtpRejectsExpiredOrInvalidOtp() throws Exception {
        User user = new User("Otp Test", "otp@example.com", passwordEncoder.encode("OldPassword123!"));
        user.setResetOtpHash(passwordEncoder.encode("123456"));
        user.setResetOtpExpiresAt(Instant.now().minusSeconds(5));
        user.setResetOtpAttempts(0);
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/verify-reset-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "otp@example.com",
                                  "otp": "123456"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("OTP expired"));

        User expired = userRepository.findByEmail("otp@example.com").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(expired.getResetOtpHash()).isNull();
        org.assertj.core.api.Assertions.assertThat(expired.getResetOtpExpiresAt()).isNull();
        org.assertj.core.api.Assertions.assertThat(expired.getResetOtpAttempts()).isNull();

        expired.setResetOtpHash(passwordEncoder.encode("654321"));
        expired.setResetOtpExpiresAt(Instant.now().plusSeconds(300));
        expired.setResetOtpAttempts(0);
        userRepository.save(expired);

        mockMvc.perform(post("/api/auth/verify-reset-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "otp@example.com",
                                  "otp": "111111"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid OTP"));

        User invalid = userRepository.findByEmail("otp@example.com").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(invalid.getResetOtpAttempts()).isEqualTo(1);
    }

    @Test
    void resetPasswordFlowVerifiesOtpThenUpdatesPasswordAndClearsResetState() throws Exception {
        User user = new User("Reset Test", "reset@example.com", passwordEncoder.encode("OldPassword123!"));
        user.setResetOtpHash(passwordEncoder.encode("123456"));
        user.setResetOtpExpiresAt(Instant.now().plusSeconds(300));
        user.setResetOtpAttempts(0);
        userRepository.save(user);

        String verifyResponse = mockMvc.perform(post("/api/auth/verify-reset-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "reset@example.com",
                                  "otp": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resetToken", not(blankOrNullString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String resetToken = objectMapper.readTree(verifyResponse).get("resetToken").asText();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "resetToken", resetToken,
                                "newPassword", "NewPassword123!"
                        ))))
                .andExpect(status().isOk());

        User updated = userRepository.findByEmail("reset@example.com").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches("NewPassword123!", updated.getPasswordHash())).isTrue();
        org.assertj.core.api.Assertions.assertThat(updated.getResetToken()).isNull();
        org.assertj.core.api.Assertions.assertThat(updated.getResetTokenExpiresAt()).isNull();
        org.assertj.core.api.Assertions.assertThat(updated.getResetOtpHash()).isNull();
        org.assertj.core.api.Assertions.assertThat(updated.getResetOtpExpiresAt()).isNull();
        org.assertj.core.api.Assertions.assertThat(updated.getResetOtpAttempts()).isNull();
    }
}
