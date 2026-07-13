package com.petconnect.home.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_levels")
public class UserLevel extends BaseEntity {

    @Column(nullable = false)
    private int level;

    @Column(name = "current_xp", nullable = false)
    private int currentXp;

    @Column(name = "next_level_xp", nullable = false)
    private int nextLevelXp;

    protected UserLevel() {
        super();
    }

    public UserLevel(int level, int currentXp, int nextLevelXp) {
        super();
        this.level = level;
        this.currentXp = currentXp;
        this.nextLevelXp = nextLevelXp;
    }

    public UserLevel(UUID id, int level, int currentXp, int nextLevelXp) {
        super(id);
        this.level = level;
        this.currentXp = currentXp;
        this.nextLevelXp = nextLevelXp;
    }

    public int getLevel() {
        return level;
    }

    public int getCurrentXp() {
        return currentXp;
    }

    public int getNextLevelXp() {
        return nextLevelXp;
    }
}