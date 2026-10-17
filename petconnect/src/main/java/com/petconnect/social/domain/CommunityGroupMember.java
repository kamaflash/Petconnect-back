package com.petconnect.social.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Membresía de un usuario en un círculo de comunidad.
 *
 * <p>Existe como entidad propia, y no como un contador en el círculo, para poder
 * listar los círculos de un usuario y garantizar que no se una dos veces: la base
 * de datos impone el par (círculo, usuario) único. En esta primera versión todos
 * los círculos son públicos, así que unirse es inmediato y no hay solicitudes de
 * acceso pendientes.</p>
 */
@Entity
@Table(name = "community_group_members")
public class CommunityGroupMember extends BaseEntity {

    /** Rol dentro del círculo. De momento solo distingue miembro de moderador. */
    public enum Role {
        MEMBER,
        MODERATOR
    }

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    /** authUserId del miembro. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    protected CommunityGroupMember() {
        super();
    }

    public CommunityGroupMember(UUID groupId, UUID userId, Role role) {
        super();
        this.groupId = groupId;
        this.userId = userId;
        this.role = role == null ? Role.MEMBER : role;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }

    public boolean isModerator() {
        return role == Role.MODERATOR;
    }
}
