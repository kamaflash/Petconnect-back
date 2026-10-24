package com.petconnect.auth.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petconnect.auth.application.dto.LoginRequest;
import com.petconnect.auth.application.dto.RegisterRequest;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                                .andExpect(header().stringValues("Set-Cookie",
                                                hasItem(containsString("PC_AT="))))
                                .andExpect(header().stringValues("Set-Cookie",
                                                hasItem(containsString("PC_RT="))));
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
                                .andExpect(header().stringValues("Set-Cookie",
                                                hasItem(containsString("PC_AT="))))
                                .andExpect(header().stringValues("Set-Cookie",
                                                hasItem(containsString("PC_RT="))));
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

                var refreshCookie = registerResult.getResponse().getCookie("PC_RT");
                org.junit.jupiter.api.Assertions.assertNotNull(refreshCookie, "PC_RT cookie not set");

                // Now refresh: the refresh token travels in the PC_RT cookie
                mockMvc.perform(post("/api/v1/auth/refresh")
                                .cookie(refreshCookie))
                                .andExpect(status().isOk())
                                .andExpect(header().stringValues("Set-Cookie",
                                                hasItem(containsString("PC_AT="))))
                                .andExpect(header().stringValues("Set-Cookie",
                                                hasItem(containsString("PC_RT="))));
        }

        /**
         * El token CSRF debe devolverse tanto en la cookie como en el cuerpo y
         * en la cabecera `X-XSRF-TOKEN`. El cuerpo es imprescindible cuando
         * la API vive en un origen distinto al del front: la cookie se
         * guarda en el host de la API y `document.cookie` del navegador no
         * puede leerla.
         */
        @Test
        void shouldExposeCsrfTokenInBodyAndHeader() throws Exception {
                mockMvc.perform(get("/api/v1/csrf-token"))
                                .andExpect(status().isOk())
                                .andExpect(header().stringValues("Set-Cookie",
                                                hasItem(containsString("XSRF-TOKEN="))))
                                .andExpect(header().exists("X-XSRF-TOKEN"))
                                .andExpect(jsonPath("$.token").isNotEmpty());
        }

        /**
         * Una peticion anonima a una ruta protegida debe devolver 401, no 403:
         * el frontend distingue ambos codigos para decidir si reintenta con
         * refresh (401) o si es un problema de CSRF/permisos (403).
         */
        @Test
        void shouldReturn401WhenAnonymousOnProtectedRoute() throws Exception {
                mockMvc.perform(get("/api/v1/gamification/summary"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
        }

        /**
         * Una mutacion sin token CSRF debe devolver 403 con el codigo
         * `CSRF_INVALID`, para que el frontend pueda reintentar una sola vez
         * solicitando un token nuevo.
         */
        @Test
        void shouldReturn403CsrfInvalidOnMutationWithoutToken() throws Exception {
                mockMvc.perform(post("/api/v1/social/groups")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").value("CSRF_INVALID"));
        }

        /**
         * Las lecturas publicas deben responder sin sesion previa.
         */
        @Test
        void shouldAllowPublicReadsWithoutSession() throws Exception {
                mockMvc.perform(get("/api/v1/presence/online"))
                                .andExpect(status().isOk());
                mockMvc.perform(get("/api/v1/users/profiles"))
                                .andExpect(status().isOk());
        }
}

