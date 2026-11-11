package com.petconnect.messages.application.dto;

import com.petconnect.messages.domain.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private UUID id;
    private String content;
    private UUID senderId;
    private UUID receiverId;
    private UUID conversationId;
    private String status;
    private String type;
    private String attachmentUrl;
    private String metadata;
    private UUID replyToMessageId;
    private String replyToContent;
    private UUID replyToSenderId;
    @Builder.Default
    private List<MessageReactionDTO> reactions = List.of();
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public static MessageDTO fromEntity(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .conversationId(message.getConversationId())
                .status(message.getStatus().name())
                .type(message.getType().name())
                .attachmentUrl(message.getAttachmentUrl())
                .metadata(message.getMetadata())
                .replyToMessageId(message.getReplyToMessageId())
                .createdAt(message.getCreatedAt())
                .readAt(message.getReadAt())
                .build();
    }

    public record MessageReactionDTO(String emoji, UUID userId) {
    }
}