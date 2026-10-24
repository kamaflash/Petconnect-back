package com.petconnect.shared.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Traduce los errores de Spring Security a respuestas JSON consistentes.
 *
 * <p>Sin esto, Spring devuelve un 403 vacío cuando una petición no
 * autenticada choca contra una ruta protegida, lo que el frontend confundía
 * con un rechazo de CSRF o con un problema de permisos. Con estas clases:</p>
 * <ul>
 * <li>sin sesión válida → <b>401</b> (el frontend reintenta con refresh);</li>
 * <li>con sesión pero sin permiso → <b>403</b>;</li>
 * <li>token CSRF ausente o inválido → <b>403</b> con código
 * {@code CSRF_INVALID} (el frontend puede reintentar una vez);</li>
 * <li>perfil incompleto → <b>403</b> con código {@code PROFILE_INCOMPLETE}.</li>
 * </ul>
 */
@Component
public class RestAuthenticationErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAuthenticationErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Petición anónima a un recurso protegido: 401 (no 403). */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        write(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "Debes iniciar sesión para acceder a este recurso.");
    }

    /** Petición autenticada sin permisos o con CSRF inválido: 403. */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        var cause = accessDeniedException.getCause();
        var message = accessDeniedException.getMessage();
        var code = "FORBIDDEN";

        if (cause instanceof org.springframework.security.web.csrf.CsrfException
                || (message != null && message.toLowerCase().contains("csrf"))) {
            code = "CSRF_INVALID";
            message = "Token CSRF ausente o inválido. Recarga la página e inténtalo de nuevo.";
        }

        write(response, HttpStatus.FORBIDDEN, code,
                message != null ? message : "No tienes permisos para realizar esta acción.");
    }

    private void write(HttpServletResponse response, HttpStatus status, String code, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of(
                "error", code,
                "status", status.value(),
                "message", message));
        response.getWriter().flush();
    }
}