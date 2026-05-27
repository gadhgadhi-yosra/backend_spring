package com.elfaddoui.backend;

import com.elfaddoui.backend.user.entity.Role;
import com.elfaddoui.backend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void profileEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedUserCanReadAndUpdateOwnProfile() throws Exception {
        String email = "profile-client@example.com";
        User user = new User("Client Test", email, passwordEncoder.encode("Password123!"));
        user.setRoles(Set.of(Role.CLIENT));
        userRepository.save(user);
        String token = clientToken(email);

        mockMvc.perform(get("/api/profile/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Client Test")))
                .andExpect(jsonPath("$.email", is(email)));

        mockMvc.perform(put("/api/profile/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Client Updated",
                                "email", "client.updated@example.com",
                                "phone", "+216 20 123 456",
                                "avatarUrl", "https://cdn.example.com/avatar.jpg",
                                "address", "Tunis Centre"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Client Updated")))
                .andExpect(jsonPath("$.email", is("client.updated@example.com")))
                .andExpect(jsonPath("$.phone", is("+216 20 123 456")))
                .andExpect(jsonPath("$.avatarUrl", is("https://cdn.example.com/avatar.jpg")))
                .andExpect(jsonPath("$.address", is("Tunis Centre")));
    }

    @Test
    void changePasswordRequiresCurrentPassword() throws Exception {
        User user = new User("Client Test", "profile-password@example.com", passwordEncoder.encode("Password123!"));
        user.setRoles(Set.of(Role.CLIENT));
        userRepository.save(user);

        mockMvc.perform(put("/api/profile/password")
                        .header("Authorization", "Bearer " + clientToken("profile-password@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "currentPassword", "wrong-password",
                                "newPassword", "NewPassword123!"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Current password is incorrect")));
    }
}
