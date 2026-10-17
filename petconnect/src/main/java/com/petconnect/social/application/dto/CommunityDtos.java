package com.petconnect.social.application.dto;

/**
 * Contratos de la API de círculos de comunidad.
 */
public final class CommunityDtos {

    private CommunityDtos() {
    }

    /**
     * Círculo tal y como lo ve un visitante concreto. Los indicadores de
     * pertenencia se calculan en el servidor a partir del usuario autenticado,
     * nunca a partir de datos que lleguen del cliente.
     *
     * @param createdBy authUserId del creador; {@code null} en círculos antiguos
     *                  anteriores a la migración de propiedad, por lo que no puede
     *                  interpretarse como cadena vacía sin perder información
     * @param owner     {@code true} solo si el usuario autenticado es el creador.
     *                  Es lo que habilita los botones de editar/eliminar en la UI
     * @param moderator reservado para cuando exista moderación; hoy solo es
     *                  {@code true} para el creador, que se une como MODERATOR
     */
    public record CommunityResponse(
            String id,
            String name,
            String description,
            String avatarUrl,
            String category,
            String topic,
            int membersCount,
            String createdBy,
            boolean member,
            boolean moderator,
            boolean owner) {
    }

    /**
     * Alta de un círculo. La validación vive en el servicio y no en anotaciones
     * de bean validation: es el patrón que sigue el resto de la API de escritura
     * y permite devolver mensajes concretos en español.
     */
    public record CreateCommunityRequest(
            String name,
            String description,
            String avatarUrl,
            String category,
            String topic) {
    }

    /**
     * Edición de un círculo. Todos los campos vuelven a enviarse completos: la
     * UI rellena el formulario con los datos actuales, así que no hace falta un
     * semántica de "campo no tocado" y un null significativo solo complicaría.
     */
    public record UpdateCommunityRequest(
            String name,
            String description,
            String avatarUrl,
            String category,
            String topic) {
    }
}
