package com.petconnect.gamification.domain;

/**
 * Tipos de acción que otorgan experiencia (XP) dentro de la plataforma.
 * Cada acción tiene una recompensa base. El valor puede ajustarse en este
 * único punto de la tabla de recompensas XP.
 */
public enum ActionType {

    DAILY_LOGIN(10, "Iniciar sesión diario"),
    GENERATE_POST(25, "Crear publicación"),
    CREATE_COMMENT(10, "Crear comentario"),
    LIKE(2, "Dar me gusta"),
    FOLLOW(15, "Seguir usuario"),
    PET_REGISTERED(30, "Registrar una mascota"),
    PROFILE_COMPLETED(30, "Completar perfil"),
    APPOINTMENT_CREATED(20, "Crear cita médica"),
    PRODUCT_PURCHASED(40, "Comprar producto"),
    ADOPTION_REQUESTED(50, "Solicitar adopción");

    private final int xpReward;
    private final String description;

    ActionType(int xpReward, String description) {
        this.xpReward = xpReward;
        this.description = description;
    }

    public int getXpReward() {
        return xpReward;
    }

    public String getDescription() {
        return description;
    }
}