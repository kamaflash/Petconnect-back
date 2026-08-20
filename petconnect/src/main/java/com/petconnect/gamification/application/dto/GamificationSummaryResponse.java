package com.petconnect.gamification.application.dto;

import java.time.LocalDate;

/**
 * Resumen del estado de gamificación del usuario (nivel, XP, barra, rango).
 */
public record GamificationSummaryResponse(
        int level,
        int currentLevelXp,
        int xpToLevel,
        int totalXp,
        String rankTitle,
        int streakDays,
        int dailyXpEarned,
        int dailyXpLimit,
        LocalDate dailyXpDate) {

    public int getLevelProgressPercent() {
        if (xpToLevel <= 0) {
            return 0;
        }
        return Math.min(100, (int) ((currentLevelXp * 100.0) / xpToLevel));
    }
}