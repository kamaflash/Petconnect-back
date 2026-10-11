package com.petconnect.lostpets.domain;

/**
 * Ciclo de vida de un aviso de animal perdido.
 *
 * <ul>
 *   <li>{@code LOST}: el aviso está publicado y la búsqueda sigue abierta.</li>
 *   <li>{@code FOUND}: hay indicios de que el animal fue localizado.</li>
 *   <li>{@code REUNITED}: el animal ya está con su familia.</li>
 *   <li>{@code CLOSED}: el caso se cierra sin reencuentro (estado terminal).</li>
 * </ul>
 */
public enum LostPetReportStatus {

    LOST,
    FOUND,
    REUNITED,
    CLOSED;

    /** Indica si desde el estado actual se puede pasar al indicado. */
    public boolean canTransitionTo(LostPetReportStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case LOST -> target == FOUND || target == REUNITED || target == CLOSED;
            case FOUND -> target == REUNITED || target == CLOSED;
            case REUNITED, CLOSED -> false;
        };
    }

    public boolean isTerminal() {
        return this == CLOSED;
    }
}
