package com.petconnect.shared.infrastructure.security;

import com.petconnect.users.domain.repositories.UserProfileRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Bloquea las escrituras de un usuario cuyo perfil todavía está incompleto.
 *
 * <p>
 * Tras el registro (o el primer acceso con Google) el usuario debe pasar por
 * el onboarding. Hasta que lo complete, solo puede:
 * </p>
 * <ul>
 * <li>leer ({@code GET}, {@code HEAD}, {@code OPTIONS});</li>
 * <li>actualizar su propio perfil —necesario para completar el onboarding—;</li>
 * <li>consultar el estado de su perfil.</li>
 * </ul>
 * <p>
 * Cualquier otra escritura responde {@code 403} con el código
 * {@code PROFILE_INCOMPLETE} para que el frontend pueda redirigir al
 * onboarding.
 * </p>
 */
@Component
public class ProfileCompletionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ProfileCompletionFilter.class);

    /** Rutas que el usuario incompleto sí puede usar (además de las lecturas). */
    private static final List<String> ALLOWED_WHEN_INCOMPLETE = List.of(
            "/api/v1/users/profile",
            "/api/v1/users/onboarding",
            "/api/v1/auth/");

    private final UserProfileRepository userProfileRepository;
    private final CurrentUserService currentUserService;

    public ProfileCompletionFilter(
            UserProfileRepository userProfileRepository,
            CurrentUserService currentUserService) {
        this.userProfileRepository = userProfileRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (isReadOnly(request) || isAllowedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        var userDetails = currentUserService.getCurrentUserDetails().orElse(null);
        if (userDetails == null) {
            // Sin sesión no hay nada que bloquear (la seguridad se encarga).
            filterChain.doFilter(request, response);
            return;
        }

        boolean complete = userProfileRepository.findByAuthUserId(userDetails.getUserId())
                .map(profile -> profile.isProfileComplete())
                .orElse(false);

        if (complete) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("Blocked write for user with incomplete profile: userId={}, path={}",
                userDetails.getUserId(), request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"error\":\"PROFILE_INCOMPLETE\",\"status\":403,"
                        + "\"message\":\"Debes completar tu perfil antes de continuar.\"}");
        response.getWriter().flush();
    }

    private boolean isReadOnly(HttpServletRequest request) {
        var method = request.getMethod();
        return "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
    }

    private boolean isAllowedPath(String requestUri) {
        for (var allowed : ALLOWED_WHEN_INCOMPLETE) {
            if (requestUri.startsWith(allowed)) {
                return true;
            }
        }
        return false;
    }
}