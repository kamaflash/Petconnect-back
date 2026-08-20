package com.petconnect.gamification.application.dto;

import com.petconnect.gamification.domain.GoalType;

import java.time.LocalDateTime;

/**
 * Representación de un objetivo variable (meta diaria/semanal) para el frontend.
 */
public record GoalResponse(
        String id,
        String code,
        String name,
        String description,
        String icon,
        GoalType type,
        int progress,
        int targetValue,
        int xpReward,
        boolean claimed,
        boolean canClaim,
        LocalDateTime periodStart,
        LocalDateTime periodEnd) {

    public int getProgressPercent() {
        if (targetValue <= 0) {
            return 0;
        }
        return Math.min(100, (int) ((progress * 100.0) / targetValue));
    }
}