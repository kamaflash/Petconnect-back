package com.petconnect.messages.presentation.rest;

import com.petconnect.messages.application.dto.ConversationDTO;
import com.petconnect.messages.application.dto.MessageDTO;
import com.petconnect.messages.application.service.MessageService;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(messageService.getConversationsForUser(userDetails.getUserId()));
    }

    @PostMapping("/conversations")
    public ResponseEntity<ConversationDTO> createConversation(
            @RequestBody CreateConversationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ConversationDTO conversation = messageService.getOrCreateDirectConversation(
                userDetails.getUserId(), request.otherUserId());
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Page<MessageDTO>> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(
                messageService.getMessagesByConversation(conversationId, userDetails.getUserId(), pageable));
    }

    @GetMapping("/messages/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(messageService.countUnreadMessages(userDetails.getUserId()));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<MessageDTO> sendMessage(
            @PathVariable UUID conversationId,
            @RequestBody PostMessageRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MessageDTO saved = messageService.sendMessage(
                userDetails.getUserId(), request.receiverId(), conversationId,
                request.content(), request.type());

        // Emitir en tiempo real a la conversación (solo si el broker WebSocket está activo)
        SimpMessagingTemplate messagingTemplate = messagingTemplateProvider.getIfAvailable();
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, saved);
        }

        return ResponseEntity.ok(saved);
    }

    public record CreateConversationRequest(UUID otherUserId) {
    }

    public record PostMessageRequest(UUID receiverId, String content, String type) {
    }
}