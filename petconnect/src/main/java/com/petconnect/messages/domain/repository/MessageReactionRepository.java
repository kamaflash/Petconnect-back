package com.petconnect.messages.domain.repository;

import com.petconnect.messages.domain.model.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, UUID> {

    List<MessageReaction> findByMessageIdIn(Collection<UUID> messageIds);

    List<MessageReaction> findByMessageId(UUID messageId);

    Optional<MessageReaction> findByMessageIdAndUserIdAndEmoji(UUID messageId, UUID userId, String emoji);
}
