package me.stuffy.stuffybot.profiles;

import java.util.List;

public class Achievements {
    private int totalAchievements;
    private int totalPoints;
    private int legacyAchievements;
    private int legacyPoints;

    private List<String> achievements;

    public Achievements(HypixelProfile profile) {
    }

    public int getTotalAchievements() {
        return totalAchievements;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public int getLegacyAchievements() {
        return legacyAchievements;
    }

    public int getLegacyPoints() {
        return legacyPoints;
    }

}
