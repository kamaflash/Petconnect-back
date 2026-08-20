package com.petconnect.gamification.application.events;

import com.petconnect.gamification.application.GamificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Consume los {@link XpEvent} publicados por los distintos módulos y otorga XP
 * mediante el motor de gamificación. Mantiene el módulo de gamificación
 * desacoplado de las acciones de negocio (DDD).
 */
@Component
public class GamificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(GamificationEventListener.class);

    private final GamificationService gamificationService;

    public GamificationEventListener(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @EventListener
    public void onXpEvent(XpEvent event) {
        try {
            GamificationService.AwardResult result = gamificationService.awardXp(event.userId(), event.actionType());
            if (result.leveledUp()) {
                log.info("Level up: user={} level {} -> {} ({} XP)", event.userId(), result.oldLevel(),
                        result.newLevel(), result.xpGained());
            }
            if (!result.unlockedAchievements().isEmpty() || !result.completedGoals().isEmpty()) {
                log.info("Gamification update for user {}: achievements={}, goals={}", event.userId(),
                        result.unlockedAchievements(), result.completedGoals());
            }
        } catch (Exception e) {
            // La gamificación nunca debe romper la acción principal del usuario.
            log.warn("No se pudo otorgar XP para el usuario {} por la acción {}: {}", event.userId(),
                    event.actionType(), e.getMessage());
        }
    }
}