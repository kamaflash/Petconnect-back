package com.petconnect.gamification.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Catálogo de logros fijos (objetivos permanentes). El progreso por usuario
 * se guarda en 'user_achievements'.
 */
@Entity
@Table(name = "achievement_catalog")
public class AchievementCatalog extends BaseEntity {

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
    private AchievementCategory category;

    @Column(name = "target_value", nullable = false)
    private int targetValue;

    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    protected AchievementCatalog() {
        super();
    }

    public AchievementCatalog(String code, String name, String description, String icon,
            AchievementCategory category, int targetValue, int xpReward) {
        super();
        this.code = code;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.category = category;
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

    public AchievementCategory getCategory() {
        return category;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public int getXpReward() {
        return xpReward;
    }
}