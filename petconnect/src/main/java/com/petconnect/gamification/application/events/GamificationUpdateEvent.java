package com.petconnect.gamification.application.events;

import com.petconnect.gamification.application.dto.AchievementResponse;
import com.petconnect.gamification.application.dto.GoalResponse;

import java.util.List;
import java.util.UUID;

/**
 * Notifica al frontend (vía WebSocket) cuando el usuario sube de nivel o
 * desbloquea logros/metas. Incluye la info suficiente para mostrar toast + repintado.
 */
public record GamificationUpdateEvent(
        UUID userId,
        int newLevel,
        int totalXp,
        boolean leveledUp,
        int levelsGained,
        int xpGained,
        List<AchievementResponse> unlockedAchievements,
        List<GoalResponse> completedGoals) {
}