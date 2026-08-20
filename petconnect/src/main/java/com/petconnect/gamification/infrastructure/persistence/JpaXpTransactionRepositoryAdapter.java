package com.petconnect.gamification.infrastructure.persistence;

import com.petconnect.gamification.domain.XpTransaction;
import com.petconnect.gamification.domain.repositories.XpTransactionRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public class JpaXpTransactionRepositoryAdapter implements XpTransactionRepository {

    private final SpringDataXpTransactionRepository repository;

    public JpaXpTransactionRepositoryAdapter(SpringDataXpTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<XpTransaction> findAllByUserIdAndXpOn(UUID userId, LocalDate xpOn) {
        return repository.findAllByUserIdAndXpOn(userId, xpOn);
    }

    @Override
    public XpTransaction save(XpTransaction transaction) {
        return repository.save(transaction);
    }
}