package com.petconnect.messages.infrastructure.websocket;

import com.petconnect.messages.application.dto.MessageDTO;
import com.petconnect.messages.application.service.MessageService;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@ConditionalOnProperty(name = "websocket.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class MessageWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
        try {
            UUID senderId = extractUserId(headerAccessor);
            if (senderId == null) {
                log.warn("Unauthorized message attempt");
                return;
            }

            // Si no llega la conversación, la creamos (o reutilizamos) entre sender y receiver
            UUID conversationId = request.conversationId();
            if (conversationId == null) {
                if (request.receiverId() == null) {
                    log.warn("Cannot create conversation without receiver");
                    return;
                }
                conversationId = messageService
                        .getOrCreateDirectConversation(senderId, request.receiverId())
                        .getId();
            }

            MessageDTO message = messageService.sendMessage(
                    senderId,
                    request.receiverId(),
                    conversationId,
                    request.content(),
                    request.type());

            // Enviar a la conversación específica
            messagingTemplate.convertAndSend(
                    "/topic/conversations/" + conversationId,
                    message);

            // Enviar notificación al receptor
            if (message.getReceiverId() != null) {
                messagingTemplate.convertAndSendToUser(
                        message.getReceiverId().toString(),
                        "/queue/notifications",
                        new NotificationDTO("NEW_MESSAGE", message));
            }

            log.info("Message sent: {} to conversation: {}", message.getId(), conversationId);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }

    @MessageMapping("/chat.read")
    public void markAsRead(@Payload MarkAsReadRequest request, SimpMessageHeaderAccessor headerAccessor) {
        try {
            UUID userId = extractUserId(headerAccessor);
            if (userId == null) {
                return;
            }

            if (request.messageId() != null) {
                messageService.markAsRead(request.messageId(), userId);
            } else {
                messageService.markConversationAsRead(request.conversationId(), userId);
            }

            // Notificar a la conversación
            messagingTemplate.convertAndSend(
                    "/topic/conversations/" + request.conversationId() + "/read",
                    true);
        } catch (Exception e) {
            log.error("Error marking message as read", e);
        }
    }

    @MessageMapping("/notification.subscribe")
    public void subscribeToNotifications(SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal != null) {
            log.info("User {} subscribed to notifications", principal.getName());
        }
    }

    @MessageMapping("/chat.typing")
    public void sendTyping(@Payload TypingRequest request,
                           SimpMessageHeaderAccessor headerAccessor) {
        try {
            UUID userId = extractUserId(headerAccessor);
            if (userId == null) {
                log.warn("Unauthorized typing notification attempt");
                return;
            }

            // Broadcast typing status to every subscriber of the conversation topic
            messagingTemplate.convertAndSend(
                    "/topic/conversations/" + request.conversationId() + "/typing",
                    new TypingDTO(userId, request.conversationId(), request.isTyping()));

        } catch (Exception e) {
            log.error("Error broadcasting typing notification", e);
        }
    }

    /**
     * Extrae el UUID del usuario autenticado del principal del WebSocket.
     * El principal es un {@link UsernamePasswordAuthenticationToken} cuyo principal
     * es un {@link CustomUserDetails}, de modo que getName() (email) NO es el userId.
     */
    private UUID extractUserId(SimpMessageHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUserId();
        }
        return null;
    }

    // DTOs internos
    public record SendMessageRequest(
            UUID conversationId,
            UUID receiverId,
            String content,
            String type) {
    }

    public record MarkAsReadRequest(
            UUID messageId,
            UUID conversationId) {
    }

    public record NotificationDTO(
            String type,
            MessageDTO data) {
    }

    public record TypingRequest(
            UUID conversationId,
            boolean isTyping) {
    }

    public record TypingDTO(
            UUID userId,
            UUID conversationId,
            boolean isTyping) {
    }

    public record UserOnlineStatus(
            UUID userId,
            boolean online) {
    }
}