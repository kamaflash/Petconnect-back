package com.petconnect.messages.presentation.rest;

import com.petconnect.messages.infrastructure.websocket.WebSocketPresenceListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Expone el estado de presencia (usuarios online) para que el cliente pueda
 * inicializar la interfaz con un snapshot en lugar de depender solo de eventos
 * en tiempo real (que pueden perderse si el usuario ya estaba conectado).
 */
@RestController
@RequestMapping("/api/v1/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final ObjectProvider<WebSocketPresenceListener> presenceListenerProvider;

    @GetMapping("/online")
    public ResponseEntity<List<UUID>> getOnlineUsers() {
        WebSocketPresenceListener listener = presenceListenerProvider.getIfAvailable();
        if (listener == null) {
            // WebSocket desactivado (websocket.enabled=false): nadie "online" por WS.
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(listener.getOnlineUserIds());
    }
}