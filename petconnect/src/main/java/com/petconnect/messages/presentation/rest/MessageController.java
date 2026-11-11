package com.petconnect.messages.presentation.rest;

import com.petconnect.messages.application.dto.ConversationDTO;
import com.petconnect.messages.application.dto.MessageDTO;
import com.petconnect.messages.application.service.MessageService;
import com.petconnect.shared.infrastructure.services.CloudinaryService;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Locale;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider;
    private final ObjectProvider<CloudinaryService> cloudinaryServiceProvider;

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
                request.content(), request.type(), request.attachmentUrl(), request.metadata(),
                request.replyToMessageId());

        broadcastMessage(conversationId, saved);

        return ResponseEntity.ok(saved);
    }

    @PostMapping("/conversations/{conversationId}/messages/{messageId}/reactions")
    public ResponseEntity<MessageDTO> toggleReaction(
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId,
            @RequestBody ReactionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MessageDTO updated = messageService.toggleReaction(
                userDetails.getUserId(), conversationId, messageId, request.emoji());
        SimpMessagingTemplate messagingTemplate = messagingTemplateProvider.getIfAvailable();
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, updated);
        }
        return ResponseEntity.ok(updated);
    }

    @PostMapping(value = "/conversations/{conversationId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageDTO> sendImage(
            @PathVariable UUID conversationId,
            @RequestParam UUID receiverId,
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        messageService.validateCanSendMessage(userDetails.getUserId(), receiverId, conversationId);
        if (image.isEmpty() || image.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image must be between 1 byte and 5 MB");
        }
        String contentType = image.getContentType();
        if (contentType == null || !List.of("image/jpeg", "image/png", "image/webp", "image/gif")
                .contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported image format");
        }

        CloudinaryService cloudinary = cloudinaryServiceProvider.getIfAvailable();
        if (cloudinary == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Image uploads are unavailable");
        }

        String imageUrl;
        try {
            imageUrl = cloudinary.uploadImage(image);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Image upload failed", exception);
        }
        MessageDTO saved = messageService.sendMessage(
                userDetails.getUserId(), receiverId, conversationId,
                image.getOriginalFilename(), "IMAGE", imageUrl, null);
        broadcastMessage(conversationId, saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    private void broadcastMessage(UUID conversationId, MessageDTO saved) {
        SimpMessagingTemplate messagingTemplate = messagingTemplateProvider.getIfAvailable();
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, saved);
            if (saved.getReceiverId() != null
                    && messageService.shouldNotifyMessages(saved.getReceiverId())) {
                messagingTemplate.convertAndSendToUser(
                        saved.getReceiverId().toString(),
                        "/queue/notifications",
                        new com.petconnect.messages.infrastructure.websocket.MessageWebSocketController.NotificationDTO(
                                "NEW_MESSAGE", saved));
            }
        }
    }

    public record CreateConversationRequest(UUID otherUserId) {
    }

    public record PostMessageRequest(
            UUID receiverId,
            String content,
            String type,
            String attachmentUrl,
            String metadata,
            UUID replyToMessageId) {
    }

    public record ReactionRequest(String emoji) {
    }
}