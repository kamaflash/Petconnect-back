package com.petconnect.gamification.application.dto;

import com.petconnect.gamification.domain.AchievementCategory;
import com.petconnect.gamification.domain.AchievementState;

/**
 * Representación de un logro fijo (objetivo permanente) para el frontend.
 */
public record AchievementResponse(
        String id,
        String code,
        String name,
        String description,
        String icon,
        AchievementCategory category,
        int progress,
        int targetValue,
        int xpReward,
        AchievementState state,
        boolean canClaim,
        String unlockedAt) {

    public int getProgressPercent() {
        if (targetValue <= 0) {
            return 0;
        }
        return Math.min(100, (int) ((progress * 100.0) / targetValue));
    }
}