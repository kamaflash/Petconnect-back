package com.petconnect.messages.application.dto;

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
public class ConversationDTO {
    private UUID id;
    private boolean isGroup;
    private String title;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;

    // Información del interlocutor (para chats 1:1)
    private UUID partnerId;
    private String partnerFirstName;
    private String partnerLastName;
    private String partnerAvatarUrl;
}