package com.petconnect.shared.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        var accessSecret = "dGhpcyBpcyBhIHRlc3QgYWNjZXNzIHRva2VuIHNlY3JldCBrZXkgZm9yIHVuaXQgdGVzdGluZw==";
        var refreshSecret = "dGhpcyBpcyBhIHRlc3QgcmVmcmVzaCB0b2tlbiBzZWNyZXQga2V5IGZvciB1bml0IHRlc3Rpbmc=";
        jwtService = new JwtService(accessSecret, refreshSecret, 3600000L, 2592000000L);
    }

    @Test
    void shouldGenerateAndValidateAccessToken() {
        var userId = UUID.randomUUID();
        var token = jwtService.generateAccessToken(userId, "test@example.com", "USER");

        assertNotNull(token);
        assertTrue(jwtService.isAccessTokenValid(token));
        assertEquals("test@example.com", jwtService.extractEmailFromAccessToken(token));
    }

    @Test
    void shouldGenerateAndValidateRefreshToken() {
        var userId = UUID.randomUUID();
        var token = jwtService.generateRefreshToken(userId, "test@example.com");

        assertNotNull(token);
        assertTrue(jwtService.isRefreshTokenValid(token));
        assertEquals("test@example.com", jwtService.extractEmailFromRefreshToken(token));
    }

    @Test
    void shouldRejectInvalidAccessToken() {
        assertFalse(jwtService.isAccessTokenValid("invalid-token"));
    }

    @Test
    void shouldRejectInvalidRefreshToken() {
        assertFalse(jwtService.isRefreshTokenValid("invalid-token"));
    }

    @Test
    void shouldRejectExpiredToken() throws InterruptedException {
        var shortLivedJwt = new JwtService(
                "dGhpcyBpcyBhIHRlc3QgYWNjZXNzIHRva2VuIHNlY3JldCBrZXkgZm9yIHVuaXQgdGVzdGluZw==",
                "dGhpcyBpcyBhIHRlc3QgcmVmcmVzaCB0b2tlbiBzZWNyZXQga2V5IGZvciB1bml0IHRlc3Rpbmc=",
                1L, 1L);

        var token = shortLivedJwt.generateAccessToken(UUID.randomUUID(), "test@example.com", "USER");
        Thread.sleep(10);
        assertFalse(shortLivedJwt.isAccessTokenValid(token));
    }

    @Test
    void shouldNotValidateRefreshTokenWithAccessSecret() {
        var userId = UUID.randomUUID();
        var refreshToken = jwtService.generateRefreshToken(userId, "test@example.com");

        assertFalse(jwtService.isAccessTokenValid(refreshToken));
    }

    @Test
    void shouldNotValidateAccessTokenWithRefreshSecret() {
        var userId = UUID.randomUUID();
        var accessToken = jwtService.generateAccessToken(userId, "test@example.com", "USER");

        assertFalse(jwtService.isRefreshTokenValid(accessToken));
    }
}