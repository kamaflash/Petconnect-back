package com.petconnect.shared.presentation;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public record CurrentUser(UUID userId, String email, String role) {

    public static CurrentUser fromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        var principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            var authorities = userDetails.getAuthorities();
            var role = authorities.stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse("USER");

            return new CurrentUser(
                    UUID.fromString((String) authentication.getDetails()),
                    userDetails.getUsername(),
                    role);
        }

        throw new IllegalStateException("Cannot extract user from authentication");
    }
}