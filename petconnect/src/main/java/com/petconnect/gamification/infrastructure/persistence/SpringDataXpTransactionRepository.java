package com.petconnect.gamification.infrastructure.persistence;

import com.petconnect.gamification.domain.XpTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataXpTransactionRepository extends JpaRepository<XpTransaction, UUID> {
    List<XpTransaction> findAllByUserIdAndXpOn(UUID userId, LocalDate xpOn);
}