package com.petconnect.messages.application.dto;

import com.petconnect.messages.domain.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
                .createdAt(message.getCreatedAt())
                .readAt(message.getReadAt())
                .build();
    }
}