package com.petconnect.shared.infrastructure.config;

import com.petconnect.shared.infrastructure.security.JwtAuthenticationFilter;
import com.petconnect.shared.infrastructure.security.ProfileCompletionFilter;
import com.petconnect.shared.infrastructure.security.RestAuthenticationErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ProfileCompletionFilter profileCompletionFilter;
    private final RestAuthenticationErrorHandler authenticationErrorHandler;
    private final UserDetailsService userDetailsService;
    private final List<String> allowedOrigins;
    private final String csrfSameSite;
    private final boolean csrfSecure;
    private final String csrfDomain;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthFilter,
            ProfileCompletionFilter profileCompletionFilter,
            RestAuthenticationErrorHandler authenticationErrorHandler,
            UserDetailsService userDetailsService,
            @Value("${app.cors.allowed-origins:http://localhost:4200}") List<String> allowedOrigins,
            @Value("${app.auth.cookie.same-site:Lax}") String csrfSameSite,
            @Value("${app.auth.cookie.secure:false}") boolean csrfSecure,
            @Value("${app.auth.cookie.domain:}") String csrfDomain) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.profileCompletionFilter = profileCompletionFilter;
        this.authenticationErrorHandler = authenticationErrorHandler;
        this.userDetailsService = userDetailsService;
        this.allowedOrigins = allowedOrigins;
        this.csrfSameSite = csrfSameSite;
        this.csrfSecure = csrfSecure;
        this.csrfDomain = csrfDomain;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF double-submit para la SPA: el backend emite la cookie legible
        // `XSRF-TOKEN` y el frontend devuelve su valor en `X-XSRF-TOKEN`.
        // Las cookies de sesión son HttpOnly, así que SameSite por sí solo no
        // basta como defensa en el despliegue cross-site de producción.
        var csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookieCustomizer(cookie -> {
            cookie.path("/");
            cookie.sameSite(csrfSameSite);
            cookie.secure(csrfSecure);
            if (csrfDomain != null && !csrfDomain.isBlank()) {
                cookie.domain(csrfDomain);
            }
        });
        var csrfHandler = new CsrfTokenRequestAttributeHandler();
        // Las rutas /api/v1/auth/* son de lectura de sesión o emiten las
        // propias cookies (login/register/google/refresh); el logout sí exige
        // CSRF porque revoca la sesión.
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepository)
                        .csrfTokenRequestHandler(csrfHandler)
                        .ignoringRequestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/google",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/session",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/actuator/health", "/actuator/info",
                                "/ws", "/ws/**", "/sockjs/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/pet-passports/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/home/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/social/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/social/posts").permitAll()
                        // Los avisos de animales perdidos deben poder difundirse sin
                        // sesión (solo exponen datos no personales); toda escritura
                        // exige autenticación y se valida en el servicio.
                        .requestMatchers(HttpMethod.GET, "/api/v1/lost-pets/**").permitAll()
                        // Endpoint auxiliar para establecer la cookie XSRF-TOKEN tras el login.
                        // No est\u00e1 en ignoringRequestMatchers para CSRF as\u00ed que el filtro
                        // genera y establece la cookie en la respuesta.
                        .requestMatchers(HttpMethod.GET, "/api/v1/csrf-token").permitAll()
                        // Lecturas públicas que no requieren sesión previa.
                        // Nota: solo GET; ninguna escritura se abre aquí.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/users/profiles",
                                "/api/v1/users/providers",
                                "/api/v1/users/profile/*",
                                "/api/v1/users/by-auth/*",
                                "/api/v1/presence/online",
                                "/api/v1/pets/*",
                                "/api/v1/pets/by-owner/*",
                                "/api/v1/pets/*/like/status",
                                "/api/v1/pets/*/like/count",
                                "/api/v1/pets/*/likes",
                                "/api/v1/likes/**",
                                "/api/v1/marketplace/products",
                                "/api/v1/marketplace/products/category/**",
                                "/api/v1/geocode/**",
                                "/api/v1/adoptions/*/history")
                        .permitAll()
                        // Las comunidades y sus contenidos públicos son navegables sin sesión.
                        .requestMatchers(HttpMethod.GET, "/api/v1/communities/**").permitAll()
                        // WebSocket: el handshake HTTP no puede llevar cabecera Authorization
                        // (el token viaja en el frame STOMP CONNECT). Permitimos el endpoint y
                        // la autenticación se valida en el JwtChannelInterceptor.
                        .requestMatchers("/ws", "/ws/**", "/sockjs/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Sin esto, Spring responde 403 a las peticiones anónimas sobre
                // rutas protegidas (el frontend lo confundía con un fallo de CSRF).
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationErrorHandler)
                        .accessDeniedHandler(authenticationErrorHandler))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Bloquea las escrituras de quien aún no ha completado el onboarding.
                // Va después del filtro JWT para poder conocer al usuario.
                .addFilterAfter(profileCompletionFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        // Orígenes explícitos (sin `*`): obligatorios al usar credenciales
        // (cookies) en el despliegue cross-origin de producción.
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "X-XSRF-TOKEN"));
        // `X-XSRF-TOKEN` debe ser legible por el front: en cross-origin la
        // cabecera de respuesta solo es accesible si está en `exposedHeaders`.
        configuration.setExposedHeaders(List.of("Authorization", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}