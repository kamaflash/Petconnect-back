package com.petconnect.messages.infrastructure.websocket;

import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Escucha eventos de conexión y desconexión de sesiones WebSocket para
 * mantener el estado de presencia (online/offline) de los usuarios.
 */
@Component
@ConditionalOnProperty(name = "websocket.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class WebSocketPresenceListener {

    private final SimpMessagingTemplate messagingTemplate;

    /** sessionId -> userId, para poder resolver el userId al desconectar. */
    private final ConcurrentHashMap<String, UUID> sessionUserMap = new ConcurrentHashMap<>();

    /** userId -> número de sesiones activas (un usuario puede tener varias). */
    private final ConcurrentHashMap<UUID, AtomicInteger> userSessionCount = new ConcurrentHashMap<>();

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal principal = accessor.getUser();

        UUID userId = extractUserId(principal);
        if (userId == null) {
            log.warn("WebSocket session connected but user could not be resolved (sessionId={})", sessionId);
            return;
        }

        sessionUserMap.put(sessionId, userId);
        int count = userSessionCount.computeIfAbsent(userId, k -> new AtomicInteger(0))
                .incrementAndGet();

        if (count == 1) {
            // La primera sesión del usuario: está online
            broadcastOnlineStatus(userId, true);
            log.info("User {} is now online (sessionId={})", userId, sessionId);
        }
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        UUID userId = sessionUserMap.remove(sessionId);
        if (userId == null) {
            return;
        }

        AtomicInteger counter = userSessionCount.get(userId);

        if (counter != null) {
            int remaining = counter.decrementAndGet();

            if (remaining <= 0) {
                userSessionCount.remove(userId);
                broadcastOnlineStatus(userId, false);

                log.info(
                        "User {} is now offline (sessionId={})",
                        userId,
                        sessionId
                );
            }
        }
    }

    private void broadcastOnlineStatus(UUID userId, boolean online) {
        messagingTemplate.convertAndSend(
                "/topic/users/online",
                new MessageWebSocketController.UserOnlineStatus(userId, online));
    }

    /** Snapshot de los usuarios actualmente en línea (para inicializar la presencia del cliente). */
    public List<UUID> getOnlineUserIds() {
        return new ArrayList<>(userSessionCount.keySet());
    }

    private static UUID extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUserId();
        }
        return null;
    }
}