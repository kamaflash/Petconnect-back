package com.petconnect.gamification.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Progreso de un objetivo variable (meta diaria/semanal) por usuario y período.
 */
@Entity
@Table(name = "user_goal_progress")
public class UserGoalProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "goal_code", nullable = false, length = 50)
    private String goalCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GoalType type;

    @Column(nullable = false)
    private int progress;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end")
    private LocalDateTime periodEnd;

    @Column(nullable = false)
    private boolean claimed;

    protected UserGoalProgress() {
        super();
    }

    public UserGoalProgress(UUID userId, String goalCode, GoalType type, LocalDateTime periodStart,
            LocalDateTime periodEnd) {
        super();
        this.userId = userId;
        this.goalCode = goalCode;
        this.type = type;
        this.progress = 0;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.claimed = false;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getGoalCode() {
        return goalCode;
    }

    public GoalType getType() {
        return type;
    }

    public int getProgress() {
        return progress;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public void increment(GoalCatalog catalog) {
        if (claimed) {
            return;
        }
        this.progress = Math.min(catalog.getTargetValue(), this.progress + 1);
    }

    public boolean canClaim(GoalCatalog catalog) {
        return !claimed && progress >= catalog.getTargetValue();
    }

    public void claim() {
        if (!claimed) {
            claimed = true;
        }
    }

    /** Reinicia el progreso para un período nuevo (nuevo día/semana). */
    public void reset(GoalType newType, LocalDateTime newStart, LocalDateTime newEnd) {
        this.type = newType;
        this.progress = 0;
        this.periodStart = newStart;
        this.periodEnd = newEnd;
        this.claimed = false;
    }
}