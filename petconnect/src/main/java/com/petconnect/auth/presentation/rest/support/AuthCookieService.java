package com.petconnect.auth.presentation.rest.support;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Construye las cookies de autenticación (access/refresh) con atributos
 * coherentes por entorno. Los valores por defecto sirven para desarrollo
 * (mismo origen vía proxy Angular): {@code SameSite=Lax}, sin
 * {@code Secure} ni {@code Domain}. En producción (front en
 * {@code petconnect.com}, API en {@code api.petconnect.com}) hay que
 * configurar {@code SameSite=None}, {@code Secure=true} y, si se quiere
 * compartir, {@code Domain=.petconnect.com} mediante variables de entorno.
 *
 * <p>
 * Nombres: {@code PC_AT} (access, corta duración) y {@code PC_RT}
 * (refresh, larga duración, restringido al path de auth).
 * </p>
 */
@Component
public class AuthCookieService {

    private final Duration accessMaxAge;
    private final Duration refreshMaxAge;
    private final String sameSite;
    private final boolean secure;
    private final String domain;
    private final String accessPath;
    private final String refreshPath;

    public AuthCookieService(
            @Value("${jwt.access-token.expiration:3600000}") long accessExpirationMs,
            @Value("${jwt.refresh-token.expiration:2592000000}") long refreshExpirationMs,
            @Value("${app.auth.cookie.same-site:Lax}") String sameSite,
            @Value("${app.auth.cookie.secure:false}") boolean secure,
            @Value("${app.auth.cookie.domain:}") String domain,
            @Value("${app.auth.cookie.access-path:/}") String accessPath,
            @Value("${app.auth.cookie.refresh-path:/api/v1/auth}") String refreshPath) {
        this.accessMaxAge = Duration.ofMillis(accessExpirationMs);
        this.refreshMaxAge = Duration.ofMillis(refreshExpirationMs);
        this.sameSite = sameSite;
        this.secure = secure;
        this.domain = domain == null || domain.isBlank() ? null : domain;
        this.accessPath = accessPath;
        this.refreshPath = refreshPath;
    }

    public static final String ACCESS_COOKIE = "PC_AT";
    public static final String REFRESH_COOKIE = "PC_RT";

    public ResponseCookie accessCookie(String token) {
        return base(ACCESS_COOKIE, token, accessPath, accessMaxAge).build();
    }

    public ResponseCookie refreshCookie(String token) {
        return base(REFRESH_COOKIE, token, refreshPath, refreshMaxAge).build();
    }

    public ResponseCookie clearAccessCookie() {
        return base(ACCESS_COOKIE, "", accessPath, Duration.ZERO).build();
    }

    public ResponseCookie clearRefreshCookie() {
        return base(REFRESH_COOKIE, "", refreshPath, Duration.ZERO).build();
    }

    /** Cabecera {@code Set-Cookie} lista para la respuesta (sin exponer el valor en logs). */
    public HttpHeaders setSessionHeaders(String accessToken, String refreshToken) {
        var headers = new HttpHeaders();
        var access = accessCookie(accessToken).toString();
        var refresh = refreshCookie(refreshToken).toString();
        headers.add(HttpHeaders.SET_COOKIE, access);
        headers.add(HttpHeaders.SET_COOKIE, refresh);
        return headers;
    }

    public HttpHeaders clearSessionHeaders() {
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, clearAccessCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString());
        return headers;
    }

    private ResponseCookie.ResponseCookieBuilder base(String name, String value, String path, Duration maxAge) {
        var builder = ResponseCookie.from(name, value == null ? "" : value)
                .httpOnly(true)
                .secure(secure)
                .path(path)
                .maxAge(maxAge)
                .sameSite(sameSite);
        if (domain != null) {
            builder.domain(domain);
        }
        return builder;
    }
}
