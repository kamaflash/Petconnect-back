package com.petconnect.gamification.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Registro de auditoría de cada XP otorgado. Permite el control anti-grind
 * (límite diario por acción / tope total diario) y trazabilidad.
 */
@Entity
@Table(name = "xp_transactions")
public class XpTransaction extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "action_type", nullable = false, length = 40)
    private String actionType;

    @Column(nullable = false)
    private int amount;

    @Column(name = "xp_on", nullable = false)
    private LocalDate xpOn;

    protected XpTransaction() {
        super();
    }

    public XpTransaction(UUID userId, ActionType actionType, int amount, LocalDate xpOn) {
        super();
        this.userId = userId;
        this.actionType = actionType.name();
        this.amount = amount;
        this.xpOn = xpOn;
    }

    public UUID getUserId() {
        return userId;
    }

    public ActionType getActionType() {
        return ActionType.valueOf(actionType);
    }

    public int getAmount() {
        return amount;
    }

    public LocalDate getXpOn() {
        return xpOn;
    }
}