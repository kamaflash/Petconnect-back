package com.petconnect.gamification.domain;

/**
 * Tipo de objetivo variable. 'DAILY' se reinicia cada día y 'WEEKLY' cada semana,
 * permitiendo retos rotatorios además de los objetivos fijos (logros).
 */
public enum GoalType {
    DAILY,
    WEEKLY
}