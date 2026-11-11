package com.petconnect.messages.application.service;

import com.petconnect.messages.application.dto.ConversationDTO;
import com.petconnect.messages.application.dto.MessageDTO;
import com.petconnect.messages.domain.model.Conversation;
import com.petconnect.messages.domain.model.Message;
import com.petconnect.messages.domain.model.MessageReaction;
import com.petconnect.messages.domain.repository.ConversationRepository;
import com.petconnect.messages.domain.repository.MessageRepository;
import com.petconnect.messages.domain.repository.MessageReactionRepository;
import com.petconnect.pets.domain.Pet;
import com.petconnect.pets.domain.repositories.PetRepository;
import com.petconnect.social.domain.repositories.FollowRepository;
import com.petconnect.users.domain.DirectMessagePolicy;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.application.BlockedUserService;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    @Autowired(required = false)
    private BlockedUserService blockedUserService;

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserProfileRepository userProfileRepository;
    private final FollowRepository followRepository;
    private final PetRepository petRepository;
    private final MessageReactionRepository messageReactionRepository;

    private static final Set<String> ALLOWED_REACTIONS = Set.of("❤️", "😂", "😮", "😢", "👏", "🐾");

    public MessageDTO sendMessage(UUID senderId, UUID receiverId, UUID conversationId,
            String content, String type) {
        return sendMessage(senderId, receiverId, conversationId, content, type, null, null);
    }

    public MessageDTO sendMessage(UUID senderId, UUID receiverId, UUID conversationId,
            String content, String type, String attachmentUrl, String metadata) {
        return sendMessage(senderId, receiverId, conversationId, content, type,
                attachmentUrl, metadata, null);
    }

    public MessageDTO sendMessage(UUID senderId, UUID receiverId, UUID conversationId,
            String content, String type, String attachmentUrl, String metadata, UUID replyToMessageId) {
        Conversation conversation = getConversationForSending(senderId, receiverId, conversationId);

        Message replyToMessage = null;
        if (replyToMessageId != null) {
            replyToMessage = messageRepository.findById(replyToMessageId)
                    .orElseThrow(() -> new IllegalArgumentException("Reply target not found"));
            if (!conversationId.equals(replyToMessage.getConversationId())) {
                throw new IllegalArgumentException("Reply target must belong to the same conversation");
            }
        }

        Message.MessageType messageType = Message.MessageType.valueOf(type);
        String messageContent = content;
        String messageAttachmentUrl = attachmentUrl;
        String messageMetadata = metadata;

        if (messageType == Message.MessageType.PET_CARD || messageType == Message.MessageType.ADOPTION_CARD) {
            UUID petId;
            try {
                petId = UUID.fromString(metadata);
            } catch (IllegalArgumentException | NullPointerException exception) {
                throw new IllegalArgumentException("A valid pet id is required for a shared pet card");
            }
            Pet pet = petRepository.findById(petId)
                    .orElseThrow(() -> new IllegalArgumentException("Pet not found"));
            UserProfile senderProfile = userProfileRepository.findByAuthUserId(senderId)
                    .orElseThrow(() -> new IllegalArgumentException("Sender profile not found"));
            boolean isOwner = senderId.equals(pet.getOwnerId())
                    || senderProfile.getId().equals(pet.getOwnerId());
            if (!isOwner && !pet.isAvailableForAdoption()) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Only the pet owner can share a private pet profile");
            }
            if (messageType == Message.MessageType.ADOPTION_CARD && !pet.isAvailableForAdoption()) {
                throw new IllegalArgumentException("Pet is not available for adoption");
            }
            messageContent = pet.getName();
            messageAttachmentUrl = pet.getAvatarUrl();
            messageMetadata = pet.getId().toString();
        } else if (messageType == Message.MessageType.IMAGE) {
            if (attachmentUrl == null || !attachmentUrl.startsWith("https://res.cloudinary.com/")) {
                throw new IllegalArgumentException("A valid uploaded image is required");
            }
            messageContent = "Image";
        } else if (attachmentUrl != null || metadata != null) {
            throw new IllegalArgumentException("Attachments and metadata are not valid for this message type");
        }

        Message message = Message.builder()
                .content(messageContent)
                .senderId(senderId)
                .receiverId(receiverId)
                .conversationId(conversationId)
                .status(Message.MessageStatus.SENT)
                .type(messageType)
                .attachmentUrl(messageAttachmentUrl)
                .metadata(messageMetadata)
                .replyToMessageId(replyToMessageId)
                .createdAt(LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(message);

        // Actualizar metadatos de la conversación
        conversation.setLastMessageId(saved.getId());
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return toMessageDTO(saved, replyToMessage, List.of());
    }

    public MessageDTO toggleReaction(UUID userId, UUID conversationId, UUID messageId, String emoji) {
        if (emoji == null || !ALLOWED_REACTIONS.contains(emoji)) {
            throw new IllegalArgumentException("Unsupported message reaction");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        if (!conversation.getParticipantIds().contains(userId)) {
            throw new IllegalArgumentException("User is not a participant of this conversation");
        }
        ensureConversationNotBlocked(conversation, userId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        if (!conversationId.equals(message.getConversationId())) {
            throw new IllegalArgumentException("Message does not belong to this conversation");
        }

        Optional<MessageReaction> existing = messageReactionRepository
                .findByMessageIdAndUserIdAndEmoji(messageId, userId, emoji);
        if (existing.isPresent()) {
            messageReactionRepository.delete(existing.get());
        } else {
            messageReactionRepository.save(new MessageReaction(messageId, userId, emoji));
        }

        Message replyToMessage = message.getReplyToMessageId() == null
                ? null
                : messageRepository.findById(message.getReplyToMessageId()).orElse(null);
        return toMessageDTO(message, replyToMessage, messageReactionRepository.findByMessageId(messageId));
    }

    public void validateCanSendMessage(UUID senderId, UUID receiverId, UUID conversationId) {
        getConversationForSending(senderId, receiverId, conversationId);
    }

    private Conversation getConversationForSending(UUID senderId, UUID receiverId, UUID conversationId) {
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
        if (receiverId != null
                && (!conversation.getParticipantIds().contains(receiverId) || receiverId.equals(senderId))) {
            throw new IllegalArgumentException("Recipient is not a participant of this conversation");
        }
        if (receiverId != null && !conversation.isGroup()) {
            ensureDirectMessageAllowed(senderId, receiverId);
        }
        return conversation;
    }

    /**
     * Obtiene (o crea si no existe) la conversación 1:1 entre dos usuarios.
     */
    public ConversationDTO getOrCreateDirectConversation(UUID userId, UUID otherUserId) {
        ensureDirectMessageAllowed(userId, otherUserId);
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

    @Transactional(readOnly = true)
    public boolean shouldNotifyMessages(UUID userId) {
        return userProfileRepository.findByAuthUserId(userId)
                .map(UserProfile::isNotifyMessages)
                .orElse(false);
    }

    private void ensureDirectMessageAllowed(UUID senderId, UUID receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Users cannot start a conversation with themselves");
        }
        if (blockedUserService != null && blockedUserService.isBlocked(senderId, receiverId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "This conversation is unavailable because one user blocked the other");
        }

        UserProfile recipient = userProfileRepository.findByAuthUserId(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient profile not found"));
        DirectMessagePolicy policy = recipient.getDirectMessagePolicy();
        if (policy == DirectMessagePolicy.NOBODY) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "This user is not accepting direct messages");
        }
        if (policy == DirectMessagePolicy.FOLLOWING) {
            UserProfile sender = userProfileRepository.findByAuthUserId(senderId)
                    .orElseThrow(() -> new IllegalArgumentException("Sender profile not found"));
            if (!followRepository.existsByFollowerIdAndFollowingId(recipient.getId(), sender.getId())) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "This user only accepts messages from accounts they follow");
            }
        }
    }

    /**
     * Lista las conversaciones del usuario con el resumen necesario para la UI.
     */
    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversationsForUser(UUID userId) {
        return conversationRepository.findByParticipant(userId).stream()
                .filter(conversation -> !isBlockedConversation(conversation, userId))
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
        ensureConversationNotBlocked(conversation, userId);

        Page<Message> messages = messageRepository.findByConversationId(conversationId, pageable);
        List<Message> content = messages.getContent();
        List<UUID> messageIds = content.stream().map(Message::getId).toList();
        List<UUID> replyIds = content.stream()
                .map(Message::getReplyToMessageId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<UUID, Message> repliesById = new HashMap<>();
        if (!replyIds.isEmpty()) {
            messageRepository.findAllById(replyIds).forEach(reply -> repliesById.put(reply.getId(), reply));
        }

        Map<UUID, List<MessageReaction>> reactionsByMessage = messageIds.isEmpty()
                ? Map.of()
                : messageReactionRepository.findByMessageIdIn(messageIds).stream()
                        .collect(Collectors.groupingBy(MessageReaction::getMessageId));

        return messages.map(message -> toMessageDTO(
                message,
                repliesById.get(message.getReplyToMessageId()),
                reactionsByMessage.getOrDefault(message.getId(), List.of())));
    }

    private MessageDTO toMessageDTO(Message message, Message replyToMessage, List<MessageReaction> reactions) {
        MessageDTO dto = MessageDTO.fromEntity(message);
        if (replyToMessage != null) {
            dto.setReplyToContent(replyToMessage.getContent());
            dto.setReplyToSenderId(replyToMessage.getSenderId());
        }
        dto.setReactions(reactions.stream()
                .map(reaction -> new MessageDTO.MessageReactionDTO(reaction.getEmoji(), reaction.getUserId()))
                .toList());
        return dto;
    }

    public void markAsRead(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (message.getReceiverId().equals(userId)) {
            conversationRepository.findById(message.getConversationId())
                    .ifPresent(conversation -> ensureConversationNotBlocked(conversation, userId));
        }
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
        conversationRepository.findById(conversationId)
                .ifPresent(conversation -> ensureConversationNotBlocked(conversation, userId));
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

    private boolean isBlockedConversation(Conversation conversation, UUID currentUserId) {
        if (blockedUserService == null || conversation.isGroup()) {
            return false;
        }
        return conversation.getParticipantIds().stream()
                .filter(participantId -> !participantId.equals(currentUserId))
                .anyMatch(participantId -> blockedUserService.isBlocked(currentUserId, participantId));
    }

    private void ensureConversationNotBlocked(Conversation conversation, UUID currentUserId) {
        if (isBlockedConversation(conversation, currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "This conversation is unavailable because one user blocked the other");
        }
    }
}