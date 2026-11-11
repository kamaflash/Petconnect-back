package com.petconnect.shared.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        var requestURI = request.getRequestURI();

        // Login, registro, Google y refresh no necesitan sesión previa.
        // `/session` requiere una cookie válida; `/logout` permite continuar
        // sin autenticación si la cookie ya expiró, para poder borrar ambas.
        if (isSessionFreeAuthEndpoint(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        var authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (request.getCookies() != null) {
            // Autenticación por cookie HttpOnly (el frontend ya no envía
            // `Authorization`). Se acepta la cabecera como fallback durante
            // la transición.
            for (var cookie : request.getCookies()) {
                if ("PC_AT".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null || token.trim().isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var email = jwtService.extractEmailFromAccessToken(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isAccessTokenValid(token)) {
                    var userDetails = userDetailsService.loadUserByUsername(email);
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    if (continueUnauthenticatedLogout(request, response, filterChain)) {
                        return;
                    }
                    log.warn("Invalid or expired JWT token for email: {}", email);
                    sendUnauthorizedResponse(response, "Token expired or invalid");
                    return;
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            if (continueUnauthenticatedLogout(request, response, filterChain)) {
                return;
            }
            sendUnauthorizedResponse(response, "Token has expired");
            return;
        } catch (io.jsonwebtoken.JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            if (continueUnauthenticatedLogout(request, response, filterChain)) {
                return;
            }
            sendUnauthorizedResponse(response, "Invalid token");
            return;
        } catch (Exception e) {
            log.error("Unexpected error during JWT authentication: {}", e.getMessage());
            sendUnauthorizedResponse(response, "Authentication failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Endpoints de autenticacion que no dependen de una sesion previa: emitir
     * tokens (login, register, google) o rotarlos (refresh).
     */
    private static boolean isSessionFreeAuthEndpoint(String requestURI) {
        return requestURI.startsWith("/api/v1/auth/login")
                || requestURI.startsWith("/api/v1/auth/register")
                || requestURI.startsWith("/api/v1/auth/google")
                || requestURI.startsWith("/api/v1/auth/refresh");
    }

    private static boolean continueUnauthenticatedLogout(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {
        if (!"POST".equals(request.getMethod())
                || !"/api/v1/auth/logout".equals(request.getRequestURI())) {
            return false;
        }

        SecurityContextHolder.clearContext();
        filterChain.doFilter(request, response);
        return true;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + message + "\",\"status\":401}");
        response.getWriter().flush();
    }
}
