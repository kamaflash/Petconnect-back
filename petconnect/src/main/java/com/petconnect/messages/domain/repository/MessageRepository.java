package com.petconnect.messages.domain.repository;

import com.petconnect.messages.domain.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId ORDER BY m.createdAt DESC")
    Page<Message> findByConversationId(@Param("conversationId") UUID conversationId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId AND m.receiverId = :userId AND m.status != 'READ'")
    List<Message> findUnreadByConversationAndReceiver(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :userId AND m.status != 'READ'")
    long countUnreadMessages(@Param("userId") UUID userId);
}