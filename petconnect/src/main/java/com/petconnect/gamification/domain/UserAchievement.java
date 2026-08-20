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
 * Progreso de un logro fijo por usuario (estado, progreso, XP otorgado).
 */
@Entity
@Table(name = "user_achievements")
public class UserAchievement extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "achievement_code", nullable = false, length = 50)
    private String achievementCode;

    @Column(nullable = false)
    private int progress;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AchievementState state;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    protected UserAchievement() {
        super();
    }

    public UserAchievement(UUID userId, String achievementCode) {
        super();
        this.userId = userId;
        this.achievementCode = achievementCode;
        this.progress = 0;
        this.state = AchievementState.LOCKED;
    }

    public UserAchievement(UUID id, UUID userId, String achievementCode, int progress,
            AchievementState state, LocalDateTime unlockedAt, LocalDateTime createdAt) {
        super(id, createdAt);
        this.userId = userId;
        this.achievementCode = achievementCode;
        this.progress = progress;
        this.state = state;
        this.unlockedAt = unlockedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getAchievementCode() {
        return achievementCode;
    }

    public int getProgress() {
        return progress;
    }

    public AchievementState getState() {
        return state;
    }

    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }

    public void incrementProgress(AchievementCatalog catalog) {
        if (this.state == AchievementState.UNLOCKED || this.state == AchievementState.CLAIMED) {
            return;
        }
        this.progress = Math.min(catalog.getTargetValue(), this.progress + 1);
        if (this.progress >= catalog.getTargetValue()) {
            this.state = AchievementState.UNLOCKED;
            this.unlockedAt = LocalDateTime.now();
        }
    }

    public boolean unlockFromExistingProgress(AchievementCatalog catalog) {
        if (this.state != AchievementState.LOCKED && this.state != AchievementState.UNLOCKED) {
            return false;
        }
        if (this.progress >= catalog.getTargetValue() && this.state == AchievementState.LOCKED) {
            this.state = AchievementState.UNLOCKED;
            this.unlockedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    public void claim() {
        if (this.state == AchievementState.UNLOCKED) {
            this.state = AchievementState.CLAIMED;
        }
    }

    public boolean canClaim() {
        return this.state == AchievementState.UNLOCKED;
    }
}