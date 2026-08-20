package com.petconnect.auth.infrastructure.security;

import com.petconnect.auth.domain.exceptions.AuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

/**
 * Valida un ID token de Google (firma, emisor, audiencia y email verificado)
 * y extrae los datos del usuario.
 */
@Service
public class GoogleIdTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(GoogleIdTokenVerifier.class);

    private final String clientId;
    private final JwtDecoder jwtDecoder;

    public GoogleIdTokenVerifier(@Value("${google.client-id}") String clientId) {
        this.clientId = clientId;
        // URL pÃƒÂºblica y estable de las claves de Google. Con withJwkSetUri el JWK Set
        // se descarga de forma perezosa (solo en el primer decode) y se cachea, por lo
        // que no se produce ninguna llamada de red al arranque ni en entornos offline.
        this.jwtDecoder = NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build();
    }

    public GoogleUserDetails verify(String idToken) {
        var jwt = jwtDecoder.decode(idToken);

        // Validar el emisor: Google firma los ID token con este issuer (o su alias).
        String issuer = jwt.getClaimAsString("iss");
        if (issuer == null ||
                (!issuer.equals("https://accounts.google.com")
                        && !issuer.equals("https://openid.google.com"))) {
            log.warn("Google ID token has invalid issuer: {}", issuer);
            throw new AuthException("Invalid Google token issuer");
        }

        // Validar que el token fue emitido para NUESTRO client id
        var audiences = jwt.getClaimAsStringList("aud");
        if (audiences == null || !audiences.contains(clientId)) {
            log.warn("Google ID token has invalid audience");
            throw new AuthException("Invalid Google token");
        }

        // Google solo emite ID tokens con email verificado para la mayorÃƒÂ­a de cuentas.
        Boolean emailVerified = jwt.getClaim("email_verified");
        if (!Boolean.TRUE.equals(emailVerified)) {
            log.warn("Google ID token email not verified");
            throw new AuthException("Google email is not verified");
        }

        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new AuthException("Google account has no email");
        }

        return new GoogleUserDetails(
                jwt.getSubject(),
                email,
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name"));
    }

    public record GoogleUserDetails(
            String googleSub,
            String email,
            String firstName,
            String lastName) {
    }
}
