package com.petconnect.gamification.application.events;

import com.petconnect.gamification.domain.ActionType;

import java.util.UUID;

/**
 * Acción del usuario que debe otorgar XP a gamificación. Se publica desde los
 * distintos módulos (social, marketplace, pets, etc.) de forma desacoplada.
 *
 * El 'userId' corresponde al authUserId que ya usan los controladores existentes.
 */
public record XpEvent(UUID userId, ActionType actionType) {
}