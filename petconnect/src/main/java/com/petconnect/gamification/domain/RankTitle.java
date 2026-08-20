package com.petconnect.gamification.domain;

/**
 * Resuelve el rango/título mostrado según el nivel del usuario.
 */
public final class RankTitle {

    private RankTitle() {
    }

    public static String forLevel(int level) {
        if (level >= 25) {
            return "Leyenda de las Mascotas";
        }
        if (level >= 16) {
            return "Embajador/a PetCare";
        }
        if (level >= 10) {
            return "Experto/a en Mascotas";
        }
        if (level >= 6) {
            return "Petizen";
        }
        if (level >= 3) {
            return "Cuidalón";
        }
        if (level >= 1) {
            return "Amigo/a de Animales";
        }
        return "Novato/a";
    }
}