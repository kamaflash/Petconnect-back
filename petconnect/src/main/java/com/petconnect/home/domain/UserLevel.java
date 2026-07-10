package com.petconnect.home.domain;

public class UserLevel {
    private int level;
    private int currentXp;
    private int totalXp;

    public UserLevel() {
    }

    public UserLevel(int level, int currentXp, int totalXp) {
        this.level = level;
        this.currentXp = currentXp;
        this.totalXp = totalXp;
    }

    public int getLevel() {
        return level;
    }

    public int getCurrentXp() {
        return currentXp;
    }

    public int getTotalXp() {
        return totalXp;
    }
}