package com.petconnect.auth.application.dto;

import com.petconnect.users.application.dto.UserProfileResponse;

import java.util.UUID;

/**
 * Datos de sesión devueltos en el cuerpo de las respuestas de autenticación.
 * Los tokens (access/refresh) viajan exclusivamente en cookies
 * {@code HttpOnly} gestionadas por el backend y nunca se exponen aquí ni en
 * logs.
 */
public record SessionResponse(
                UUID userId,
                String email,
                String role,
                long expiresIn,
                UserProfileResponse profile,
                /** True si la cuenta acaba de crearse (registro o primer acceso con Google). */
                boolean newUser,
                /** True si el perfil tiene toda la información obligatoria de su tipo. */
                boolean profileComplete) {

    public static SessionResponse from(AuthResponse response) {
        return new SessionResponse(
                response.userId(),
                response.email(),
                response.role(),
                response.expiresIn(),
                response.profile(),
                response.newUser(),
                response.profileComplete());
    }
}
