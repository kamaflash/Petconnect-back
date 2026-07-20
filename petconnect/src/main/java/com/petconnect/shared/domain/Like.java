package com.petconnect.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Entidad genérica para likes de diferentes tipos de contenido.
 * Un usuario puede dar like a: mascotas, publicaciones, historias, comentarios,
 * eventos, grupos, etc.
 */
@Entity
@Table(name = "likes")
public class Like extends BaseEntity {

    /**
     * ID del usuario que da el like
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * ID del contenido al que se da el like (mascota, post, story, etc.)
     */
    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    /**
     * Tipo de contenido: PET, POST, STORY, COMMENT, EVENT, GROUP
     */
    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    protected Like() {
        super();
    }

    public Like(UUID userId, UUID targetId, String targetType) {
        super();
        this.userId = userId;
        this.targetId = targetId;
        this.targetType = targetType;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    /**
     * Tipos de contenido que pueden recibir likes
     */
    public enum TargetType {
        PET("PET"),
        POST("POST"),
        STORY("STORY"),
        COMMENT("COMMENT"),
        EVENT("EVENT"),
        GROUP("GROUP");

        private final String value;

        TargetType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}