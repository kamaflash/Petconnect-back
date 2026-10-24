package com.petconnect.shared.infrastructure.websocket.config;

import com.petconnect.shared.infrastructure.websocket.security.JwtChannelInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@ConditionalOnProperty(name = "websocket.enabled", havingValue = "true", matchIfMissing = false)
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;
    private final List<String> allowedOrigins;

    public WebSocketConfig(
            JwtChannelInterceptor jwtChannelInterceptor,
            @Value("${app.cors.allowed-origins:http://localhost:4200}") List<String> allowedOrigins) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Orígenes explícitos: el handshake lleva las cookies de sesión y no
        // puede usar `*` junto con credenciales.
        String[] origins = allowedOrigins.toArray(new String[0]);
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(origins);

        // Fallback WebSocket (SockJS) para clientes que lo requieran
        registry.addEndpoint("/sockjs")
                .setAllowedOriginPatterns(origins)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Aplicación: mensajes del cliente al servidor
        registry.setApplicationDestinationPrefixes("/app");

        // Broker: mensajes del servidor al cliente
        registry.enableSimpleBroker("/topic", "/queue", "/user");

        // Destinos de usuario para mensajes privados
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }
}
