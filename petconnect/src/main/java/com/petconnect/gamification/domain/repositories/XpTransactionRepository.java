package com.petconnect.gamification.domain.repositories;

import com.petconnect.gamification.domain.XpTransaction;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface XpTransactionRepository {

    List<XpTransaction> findAllByUserIdAndXpOn(UUID userId, LocalDate xpOn);

    XpTransaction save(XpTransaction transaction);
}