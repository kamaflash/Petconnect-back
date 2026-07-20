package com.petconnect.messages.application.service;

import com.petconnect.messages.application.dto.MessageDTO;
import com.petconnect.messages.domain.model.Message;
import com.petconnect.messages.domain.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageDTO sendMessage(UUID senderId, UUID receiverId, UUID conversationId,
            String content, String type) {
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
        return MessageDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> getMessagesByConversation(UUID conversationId, Pageable pageable) {
        return messageRepository.findByConversationId(conversationId, pageable)
                .map(MessageDTO::fromEntity);
    }

    public void markAsRead(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (message.getReceiverId().equals(userId)) {
            message.setStatus(Message.MessageStatus.READ);
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);
        }
    }

    @Transactional(readOnly = true)
    public long countUnreadMessages(UUID userId) {
        return messageRepository.countUnreadMessages(userId);
    }
}