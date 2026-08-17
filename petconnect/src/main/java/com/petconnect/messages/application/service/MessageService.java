package com.petconnect.messages.application.service;

import com.petconnect.messages.application.dto.ConversationDTO;
import com.petconnect.messages.application.dto.MessageDTO;
import com.petconnect.messages.domain.model.Conversation;
import com.petconnect.messages.domain.model.Message;
import com.petconnect.messages.domain.repository.ConversationRepository;
import com.petconnect.messages.domain.repository.MessageRepository;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserProfileRepository userProfileRepository;

    public MessageDTO sendMessage(UUID senderId, UUID receiverId, UUID conversationId,
            String content, String type) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(senderId)) {
            throw new IllegalArgumentException("User is not a participant of this conversation");
        }

        if (receiverId == null) {
            receiverId = conversation.getParticipantIds().stream()
                    .filter(p -> !p.equals(senderId))
                    .findFirst()
                    .orElse(null);
        }

        Message message = Message.builder()
                .content(content)
                .senderId(senderId)
                .receiverId(receiverId)
                .conversationId(conversationId)
                .status(Message.MessageStatus.SENT)
                .type(Message.MessageType.valueOf(type))
                .createdAt(LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(message);

        // Actualizar metadatos de la conversación
        conversation.setLastMessageId(saved.getId());
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return MessageDTO.fromEntity(saved);
    }

    /**
     * Obtiene (o crea si no existe) la conversación 1:1 entre dos usuarios.
     */
    public ConversationDTO getOrCreateDirectConversation(UUID userId, UUID otherUserId) {
        Optional<Conversation> existing = conversationRepository.findDirectBetween(userId, otherUserId);
        Conversation conversation = existing.orElseGet(() -> {
            UserProfile partner = userProfileRepository.findByAuthUserId(otherUserId).orElse(null);
            Conversation created = Conversation.builder()
                    .participantIds(List.of(userId, otherUserId))
                    .title(partner != null ? partner.getFirstName() + " " + partner.getLastName() : "Chat")
                    .isGroup(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            return conversationRepository.save(created);
        });

        return toConversationDTO(conversation, userId);
    }

    /**
     * Lista las conversaciones del usuario con el resumen necesario para la UI.
     */
    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversationsForUser(UUID userId) {
        return conversationRepository.findByParticipant(userId).stream()
                .sorted(Comparator.comparing(Conversation::getLastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(c -> toConversationDTO(c, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> getMessagesByConversation(UUID conversationId, UUID userId, Pageable pageable) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(userId)) {
            throw new IllegalArgumentException("User is not a participant of this conversation");
        }

        return messageRepository.findByConversationId(conversationId, pageable)
                .map(MessageDTO::fromEntity);
    }

    public void markAsRead(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (message.getReceiverId().equals(userId)
                && message.getStatus() != Message.MessageStatus.READ) {
            message.setStatus(Message.MessageStatus.READ);
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);
        }
    }

    /**
     * Marca como leídos todos los mensajes no leídos de la conversación para el usuario.
     */
    public void markConversationAsRead(UUID conversationId, UUID userId) {
        List<Message> unread = messageRepository.findUnreadByConversationAndReceiver(conversationId, userId);
        if (unread.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        unread.forEach(m -> {
            m.setStatus(Message.MessageStatus.READ);
            m.setReadAt(now);
        });
        messageRepository.saveAll(unread);
    }

    @Transactional(readOnly = true)
    public long countUnreadMessages(UUID userId) {
        return messageRepository.countUnreadMessages(userId);
    }

    private ConversationDTO toConversationDTO(Conversation conversation, UUID currentUserId) {
        UUID partnerId = conversation.getParticipantIds().stream()
                .filter(p -> !p.equals(currentUserId))
                .findFirst()
                .orElse(null);

        UserProfile partner = partnerId != null
                ? userProfileRepository.findByAuthUserId(partnerId).orElse(null)
                : null;

        Message lastMessage = messageRepository
                .findTopByConversationIdOrderByCreatedAtDesc(conversation.getId())
                .orElse(null);

        long unreadCount = messageRepository
                .findUnreadByConversationAndReceiver(conversation.getId(), currentUserId)
                .size();

        return ConversationDTO.builder()
                .id(conversation.getId())
                .isGroup(conversation.isGroup())
                .title(conversation.getTitle())
                .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                .lastMessageAt(conversation.getLastMessageAt())
                .unreadCount(unreadCount)
                .partnerId(partnerId)
                .partnerFirstName(partner != null ? partner.getFirstName() : null)
                .partnerLastName(partner != null ? partner.getLastName() : null)
                .partnerAvatarUrl(partner != null ? partner.getAvatarUrl() : null)
                .build();
    }
}