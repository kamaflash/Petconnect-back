package com.petconnect.messages.infrastructure.websocket;

import com.petconnect.messages.application.dto.MessageDTO;
import com.petconnect.messages.application.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
            Principal principal = headerAccessor.getUser();
            if (principal == null) {
                log.warn("Unauthorized message attempt");
                return;
            }

            MessageDTO message = messageService.sendMessage(
                    UUID.fromString(principal.getName()),
                    request.receiverId(),
                    request.conversationId(),
                    request.content(),
                    request.type());

            // Enviar a la conversación específica
            messagingTemplate.convertAndSend(
                    "/topic/conversations/" + request.conversationId(),
                    message);

            // Enviar notificación al receptor
            messagingTemplate.convertAndSendToUser(
                    request.receiverId().toString(),
                    "/queue/notifications",
                    new NotificationDTO("NEW_MESSAGE", message));

            log.info("Message sent: {} to conversation: {}", message.getId(), request.conversationId());
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }

    @MessageMapping("/chat.read")
    public void markAsRead(@Payload MarkAsReadRequest request, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Principal principal = headerAccessor.getUser();
            if (principal == null) {
                return;
            }

            messageService.markAsRead(request.messageId(), UUID.fromString(principal.getName()));

            // Notificar a la conversación
            messagingTemplate.convertAndSend(
                    "/topic/conversations/" + request.conversationId() + "/read",
                    request.messageId());
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
}