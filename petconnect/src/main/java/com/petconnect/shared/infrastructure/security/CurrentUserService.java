package com.petconnect.shared.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Resuelve el id del usuario autenticado (authUserId) desde el contexto de
 * seguridad establecido por {@link JwtAuthenticationFilter}.
 */
@Component
public class CurrentUserService {

    public Optional<UUID> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return Optional.of(userDetails.getUserId());
        }
        return Optional.empty();
    }

    public UUID requireUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en el contexto"));
    }
}