package me.stuffy.stuffybot.profiles;

import com.google.gson.JsonElement;

public class Achievement {
    private final String name;
    private final String description;
    private final int points;
    private final boolean legacy;
    private final boolean secret;
    private final Type type;

    private enum Type {
        CHALLENGE,
        TIERED
    };

    public Achievement(JsonElement achievementData) {
        this.name = achievementData.getAsJsonObject().get("name").getAsString();
        this.description = achievementData.getAsJsonObject().get("description").getAsString();
        this.points = achievementData.getAsJsonObject().get("points").getAsInt();
        this.legacy = achievementData.getAsJsonObject().get("legacy").getAsBoolean();
        this.secret = achievementData.getAsJsonObject().get("secret").getAsBoolean();
        this.type = Type.valueOf(achievementData.getAsJsonObject().get("type").getAsString());

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPoints() {
        return points;
    }

    public boolean isLegacy() {
        return legacy;
    }
}
