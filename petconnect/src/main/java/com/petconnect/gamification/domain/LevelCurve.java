package com.petconnect.gamification.domain;

/**
 * Curva de niveles. El usuario comienza en el nivel 0 y va subiendo conforme
 * acumula experiencia total (totalXp).
 *
 * Regla sencilla:
 * - Se necesita XP para subir del nivel L al L+1: 100 * (L + 1).
 * - totalXp acumulado requerido para llegar al nivel n: 100 * n * (n + 1) / 2.
 */
public final class LevelCurve {

    private LevelCurve() {
    }

    /** XP acumulado (totalXp) necesario para alcanzar el nivel dado. */
    public static int totalXpForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        return 100 * level * (level + 1) / 2;
    }

    /** XP necesario para subir del nivel 'level' al siguiente. */
    public static int xpForNextLevel(int level) {
        return 100 * (level + 1);
    }

    /** Deriva el nivel a partir del XP total acumulado. */
    public static int levelForTotalXp(int totalXp) {
        int level = 0;
        while (totalXp >= totalXpForLevel(level + 1)) {
            level++;
        }
        return level;
    }

    /** XP transcurrido dentro del nivel actual. */
    public static int currentLevelXp(int totalXp, int level) {
        return Math.max(0, totalXp - totalXpForLevel(level));
    }

    /** XP restante para el siguiente nivel. */
    public static int xpRemainingToNextLevel(int totalXp, int level) {
        return Math.max(0, xpForNextLevel(level) - currentLevelXp(totalXp, level));
    }
}