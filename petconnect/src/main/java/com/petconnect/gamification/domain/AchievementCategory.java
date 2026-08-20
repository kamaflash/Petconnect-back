package com.petconnect.gamification.domain;

/**
 * Categorías de logros fijos, usadas también para filtrar en la UI (frontend).
 */
public enum AchievementCategory {
    PET("Mascotas"),
    SOCIAL("Comunidad"),
    SHOP("Mercado"),
    PROFILE("Perfil"),
    CARE("Cuidado"),
    ADOPTION("Adopción"),
    STREAK("Constancia");

    private final String label;

    AchievementCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}