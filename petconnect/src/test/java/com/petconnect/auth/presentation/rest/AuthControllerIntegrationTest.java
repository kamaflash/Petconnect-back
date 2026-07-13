package com.petconnect.auth.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petconnect.auth.application.dto.LoginRequest;
import com.petconnect.auth.application.dto.RegisterRequest;
import com.petconnect.auth.application.dto.RefreshTokenRequest;
import com.petconnect.auth.domain.AuthUser;
import com.petconnect.auth.domain.UserRole;
import com.petconnect.auth.domain.repositories.AuthUserRepository;
import com.petconnect.shared.infrastructure.security.JwtService;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private AuthUserRepository authUserRepository;

        @Autowired
        private UserProfileRepository userProfileRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private JwtService jwtService;

        @BeforeEach
        void setUp() {
                // Clean up test data
                authUserRepository.findByEmail("logintest@test.com")
                                .ifPresent(u -> {
                                        userProfileRepository.findByAuthUserId(u.getId())
                                                        .ifPresent(userProfileRepository::delete);
                                        authUserRepository.delete(u);
                                });
                authUserRepository.findByEmail("refreshtest@test.com")
                                .ifPresent(u -> {
                                        userProfileRepository.findByAuthUserId(u.getId())
                                                        .ifPresent(userProfileRepository::delete);
                                        authUserRepository.delete(u);
                                });
                authUserRepository.findByEmail("newuser@test.com")
                                .ifPresent(u -> {
                                        userProfileRepository.findByAuthUserId(u.getId())
                                                        .ifPresent(userProfileRepository::delete);
                                        authUserRepository.delete(u);
                                });
        }

        @Test
        void shouldRegisterUser() throws Exception {
                var request = new RegisterRequest(
                                "newuser@test.com",
                                "password123",
                                "John",
                                "Doe",
                                "USER");

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                                .andExpect(jsonPath("$.role").value("USER"))
                                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                                .andExpect(jsonPath("$.profile").isNotEmpty());
        }

        @Test
        void shouldReturn400WhenRegisterWithInvalidEmail() throws Exception {
                var request = new RegisterRequest(
                                "invalid-email",
                                "password123",
                                "John",
                                "Doe",
                                "USER");

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400WhenRegisterWithEmptyPassword() throws Exception {
                var request = new RegisterRequest(
                                "test@test.com",
                                "",
                                "John",
                                "Doe",
                                "USER");

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldLoginSuccessfully() throws Exception {
                // Create user via register endpoint first
                var registerRequest = new RegisterRequest(
                                "logintest@test.com",
                                "password123",
                                "John",
                                "Doe",
                                "USER");

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                // Now login
                var loginRequest = new LoginRequest("logintest@test.com", "password123");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("logintest@test.com"))
                                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        void shouldReturn401WhenLoginWithWrongPassword() throws Exception {
                var request = new LoginRequest("test@test.com", "wrongpassword");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldRefreshToken() throws Exception {
                // Create user via register endpoint first
                var registerRequest = new RegisterRequest(
                                "refreshtest@test.com",
                                "password123",
                                "John",
                                "Doe",
                                "USER");

                var registerResult = mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                var responseJson = objectMapper.readTree(registerResult.getResponse().getContentAsString());
                var refreshToken = responseJson.get("refreshToken").asText();

                // Now refresh
                var refreshRequest = new RefreshTokenRequest(refreshToken);

                mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(refreshRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }
}