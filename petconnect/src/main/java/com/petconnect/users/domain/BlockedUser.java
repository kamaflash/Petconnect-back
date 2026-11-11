package com.petconnect.users.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "blocked_users")
public class BlockedUser extends BaseEntity {

    @Column(name = "blocker_auth_user_id", nullable = false)
    private UUID blockerAuthUserId;

    @Column(name = "blocked_auth_user_id", nullable = false)
    private UUID blockedAuthUserId;

    protected BlockedUser() {
        super();
    }

    public BlockedUser(UUID blockerAuthUserId, UUID blockedAuthUserId) {
        super();
        this.blockerAuthUserId = blockerAuthUserId;
        this.blockedAuthUserId = blockedAuthUserId;
    }

    public UUID getBlockerAuthUserId() {
        return blockerAuthUserId;
    }

    public UUID getBlockedAuthUserId() {
        return blockedAuthUserId;
    }
}
