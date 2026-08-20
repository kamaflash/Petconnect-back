package com.petconnect.gamification.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Catálogo de objetivos variables (metas diarias/semanales rotatorias).
 */
@Entity
@Table(name = "goal_catalog")
public class GoalCatalog extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "icon_class", nullable = false, length = 100)
    private String icon;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GoalType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType sourceAction;

    @Column(name = "target_value", nullable = false)
    private int targetValue;

    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    @Column(nullable = false)
    private boolean active = true;

    protected GoalCatalog() {
        super();
    }

    public GoalCatalog(String code, String name, String description, String icon, GoalType type,
            ActionType sourceAction, int targetValue, int xpReward) {
        super();
        this.code = code;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.type = type;
        this.sourceAction = sourceAction;
        this.targetValue = targetValue;
        this.xpReward = xpReward;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public GoalType getType() {
        return type;
    }

    public ActionType getSourceAction() {
        return sourceAction;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public int getXpReward() {
        return xpReward;
    }

    public boolean isActive() {
        return active;
    }
}