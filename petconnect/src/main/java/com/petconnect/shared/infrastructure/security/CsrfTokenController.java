package com.petconnect.shared.infrastructure.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint auxiliar para obtener el token CSRF (double-submit).
 *
 * <p>Los endpoints de auth estan en {@code ignoringRequestMatchers} para CSRF,
 * asi que tras un login con Google el navegador no recibe la cookie
 * {@code XSRF-TOKEN}. Este endpoint (GET) pasa por {@code CsrfFilter} y hace
 * que Spring establezca la cookie.</p>
 *
 * <p>Ademas devuelve el token en el cuerpo ({@code {"token": "..."}}) y en la
 * cabecera {@code X-XSRF-TOKEN}. Esto es imprescindible cuando la API vive en
 * un origen distinto al del front ({@code api.petconnect.com} vs
 * {@code petconnect.com}): la cookie se almacena en el host de la API y
 * {@code document.cookie} del navegador no puede leerla, por lo que el front
 * necesita recibirla explicitamente para poder devolverla en la cabecera
 * {@code X-XSRF-TOKEN} de las mutaciones.</p>
 */
@RestController
@RequestMapping("/api/v1/csrf-token")
public class CsrfTokenController {

    @GetMapping
    public ResponseEntity<CsrfTokenResponse> csrfToken(CsrfToken csrfToken) {
        // El mero hecho de que la peticion pase por CsrfFilter hace que
        // CookieCsrfTokenRepository genere y establezca la cookie XSRF-TOKEN.
        return ResponseEntity.ok()
                .header("X-XSRF-TOKEN", csrfToken.getToken())
                .body(new CsrfTokenResponse(csrfToken.getToken()));
    }

    /** Token CSRF para el mecanismo double-submit. */
    public record CsrfTokenResponse(String token) {
    }
}
