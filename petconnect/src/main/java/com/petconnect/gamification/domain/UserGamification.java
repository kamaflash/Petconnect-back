package com.petconnect.gamification.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Estado de gamificación de un usuario (nivel, XP, racha, límite diario).
 * El usuario comienza con nivel 0 y XP 0.
 */
@Entity
@Table(name = "user_gamification")
public class UserGamification extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private int level;

    @Column(name = "total_xp", nullable = false)
    private int totalXp;

    @Column(name = "current_level_xp", nullable = false)
    private int currentLevelXp;

    @Column(name = "xp_to_level", nullable = false)
    private int xpToLevel;

    @Column(name = "rank_title", nullable = false, length = 40)
    private String rankTitle;

    @Column(name = "daily_xp_earned", nullable = false)
    private int dailyXpEarned;

    @Column(name = "daily_xp_date")
    private LocalDate dailyXpDate;

    @Column(name = "streak_days", nullable = false)
    private int streakDays;

    @Column(name = "last_login_date")
    private LocalDate lastLoginDate;

    protected UserGamification() {
        super();
    }

    public UserGamification(UUID userId) {
        super();
        this.userId = userId;
        this.level = 0;
        this.totalXp = 0;
        this.currentLevelXp = 0;
        this.xpToLevel = LevelCurve.xpForNextLevel(0);
        this.rankTitle = RankTitle.forLevel(0);
        this.dailyXpEarned = 0;
        this.streakDays = 0;
    }

    public UserGamification(UUID id, UUID userId, int level, int totalXp) {
        super(id);
        this.userId = userId;
        this.level = level;
        this.totalXp = totalXp;
        recomputeDerived();
    }

    /** Actualiza campos derivados (nivel, barra de progreso y rango) desde el XP total. */
    public void recomputeDerived() {
        this.level = LevelCurve.levelForTotalXp(this.totalXp);
        this.currentLevelXp = LevelCurve.currentLevelXp(this.totalXp, this.level);
        this.xpToLevel = LevelCurve.xpForNextLevel(this.level);
        this.rankTitle = RankTitle.forLevel(this.level);
    }

    public void addXp(int amount, LocalDate today) {
        if (today != null && !today.equals(this.dailyXpDate)) {
            this.dailyXpDate = today;
            this.dailyXpEarned = 0;
        }
        this.totalXp += amount;
        this.dailyXpEarned += amount;
        recomputeDerived();
    }

    public UUID getUserId() {
        return userId;
    }

    public int getLevel() {
        return level;
    }

    public int getTotalXp() {
        return totalXp;
    }

    public int getCurrentLevelXp() {
        return currentLevelXp;
    }

    public int getXpToLevel() {
        return xpToLevel;
    }

    public String getRankTitle() {
        return rankTitle;
    }

    public int getDailyXpEarned() {
        return dailyXpEarned;
    }

    public LocalDate getDailyXpDate() {
        return dailyXpDate;
    }

    public int getStreakDays() {
        return streakDays;
    }

    public LocalDate getLastLoginDate() {
        return lastLoginDate;
    }

    public void registerLogin(LocalDate today) {
        if (today.equals(this.lastLoginDate)) {
            return;
        }
        LocalDate yesterday = today.minusDays(1);
        this.streakDays = yesterday.equals(this.lastLoginDate) ? this.streakDays + 1 : 1;
        this.lastLoginDate = today;
    }
}