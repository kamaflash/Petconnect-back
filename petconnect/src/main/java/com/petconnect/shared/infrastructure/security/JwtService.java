package com.petconnect.shared.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey accessTokenSecret;
    private final SecretKey refreshTokenSecret;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(
            @Value("${jwt.access-token.secret}") String accessTokenSecret,
            @Value("${jwt.refresh-token.secret}") String refreshTokenSecret,
            @Value("${jwt.access-token.expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token.expiration}") long refreshTokenExpiration) {
        this.accessTokenSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessTokenSecret));
        this.refreshTokenSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshTokenSecret));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(UUID userId, String email, String role) {
        return generateAccessToken(userId, email, role, false);
    }

    /**
     * Genera el access token incluyendo el claim {@code profileComplete}, que
     * indica si el perfil tenía toda la información obligatoria en el momento
     * de emitir el token.
     *
     * <p>
     * Nota: el claim puede quedar desactualizado si el usuario completa el
     * perfil después; por eso las decisiones de autorización no deben
     * solo de él.
     * </p>
     */
    public String generateAccessToken(UUID userId, String email, String role, boolean profileComplete) {
        var claims = new java.util.HashMap<String, Object>();
        claims.put("profileComplete", profileComplete);
        return generateToken(userId, email, role, claims, accessTokenSecret, accessTokenExpiration);
    }

    public String generateRefreshToken(UUID userId, String email) {
        // El refresh token no lleva el claim `profileComplete`: se recalcula en
        // cada refresh y solo agranda el token almacenado en BD.
        return generateToken(userId, email, null, Map.of(), refreshTokenSecret, refreshTokenExpiration);
    }

    /** Claim {@code profileComplete} del access token (false si no está presente). */
    public boolean isProfileCompleteClaim(String token) {
        try {
            var claims = extractAllClaims(token, accessTokenSecret);
            var value = claims.get("profileComplete");
            return Boolean.TRUE.equals(value);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmailFromAccessToken(String token) {
        return extractClaim(token, accessTokenSecret, Claims::getSubject);
    }

    public String extractEmailFromRefreshToken(String token) {
        return extractClaim(token, refreshTokenSecret, Claims::getSubject);
    }

    public boolean isAccessTokenValid(String token) {
        return isTokenValid(token, accessTokenSecret);
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token, refreshTokenSecret);
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    private String generateToken(UUID userId, String email, String role, Map<String, Object> extraClaims,
            SecretKey secret, long expiration) {
        var claims = new java.util.HashMap<String, Object>();
        claims.put("userId", userId.toString());
        if (role != null) {
            claims.put("role", role);
        }
        claims.putAll(extraClaims);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secret)
                .compact();
    }

    private <T> T extractClaim(String token, SecretKey secret, Function<Claims, T> claimsResolver) {
        var claims = extractAllClaims(token, secret);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, SecretKey secret) {
        return Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenValid(String token, SecretKey secret) {
        try {
            var claims = extractAllClaims(token, secret);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}