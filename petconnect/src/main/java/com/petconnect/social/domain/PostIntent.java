package com.petconnect.social.domain;

/**
 * Intención declarada por el autor al crear una publicación.
 *
 * <p>Permite dar contexto al feed (preguntas, recomendaciones, quedadas...) sin
 * cambiar el modelo de publicación. {@link #UPDATE} actúa como valor por defecto
 * para las publicaciones anteriores a esta funcionalidad, que no tienen intención
 * guardada: se comportan exactamente igual que antes.</p>
 */
public enum PostIntent {

    /** Actualización o historia. Valor por defecto para publicaciones antiguas. */
    UPDATE("Actualización"),
    /** Pregunta abierta a la comunidad que admite una respuesta marcada como solución. */
    QUESTION("Pregunta"),
    /** Recomendación de productos, servicios o cuidados. */
    RECOMMENDATION("Recomendación"),
    /** Logro o hito del autor o de su mascota. */
    ACHIEVEMENT("Logro"),
    /** Búsqueda de compañero para un paseo o actividad. */
    MEETUP("Quedada"),
    /**
     * Aviso urgente por mascota perdida. No sustituye al módulo de mascotas
     * perdidas: la interfaz enlaza a ese flujo para registrar el aviso completo.
     */
    URGENT_LOST("Aviso urgente");

    private final String label;

    PostIntent(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Convierte el texto recibido del cliente en una intención válida.
     *
     * @param value nombre del enum, insensible a mayúsculas
     * @return la intención, o {@code null} si el valor es nulo o vacío
     * @throws IllegalArgumentException si el valor no corresponde a ninguna intención
     */
    public static PostIntent parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return PostIntent.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Intención de publicación no válida: " + value);
        }
    }
}
