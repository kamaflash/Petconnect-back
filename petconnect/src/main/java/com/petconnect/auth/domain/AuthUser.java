package com.petconnect.auth.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_users")
public class AuthUser extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean emailVerified;

    private String refreshToken;

    private LocalDateTime refreshTokenExpiry;

    protected AuthUser() {
        super();
    }

    public AuthUser(String email, String password, UserRole role) {
        super();
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = true;
        this.emailVerified = false;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void updateRefreshToken(String refreshToken, LocalDateTime expiry) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiry = expiry;
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiry = null;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void disable() {
        this.enabled = false;
    }

    public void enable() {
        this.enabled = true;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public LocalDateTime getRefreshTokenExpiry() {
        return refreshTokenExpiry;
    }
}