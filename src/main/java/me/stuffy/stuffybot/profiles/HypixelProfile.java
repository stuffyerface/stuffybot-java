package me.stuffy.stuffybot.profiles;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.stuffy.stuffybot.utils.MiscUtils;

import java.util.UUID;

import static me.stuffy.stuffybot.utils.MiscUtils.getNestedJson;

public class HypixelProfile {
    private UUID uuid;
    private String displayName;
    private Rank rank;
    private JsonObject profile;

    public HypixelProfile(JsonObject profile) {
        this.profile = profile.deepCopy();
        this.uuid = MiscUtils.formatUUID(profile.get("uuid").getAsString());
        this.displayName = profile.get("displayname").getAsString();
        this.rank = determineRank(profile);
    }

    private static Rank determineRank(JsonObject profile) {
        String rankString = null;

        if(profile.has("rank")){
            String rank = profile.get("rank").getAsString();
            if(rank.equalsIgnoreCase("ADMIN")){
                return Rank.ADMIN;
            } else if(rank.equalsIgnoreCase("GAME_MASTER")){
                return Rank.GAME_MASTER;
            } else if(rank.equalsIgnoreCase("YOUTUBER")){
                return Rank.YOUTUBER;
            }
        }
        if(profile.has("monthlyPackageRank")){
            if(profile.get("monthlyPackageRank").getAsString().equalsIgnoreCase("SUPERSTAR")){
                return Rank.MVP_PLUS_PLUS;
            }
        }

        rankString = profile.get("newPackageRank").getAsString();
        return Rank.fromString(rankString);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Rank getRank() {
        return rank;
    }

    public String getDiscord() {
        return getNestedJson(profile, "socialMedia", "links", "DISCORD").getAsString();
    }

    public JsonElement getAchievements() {
        // return an object with profile["achievements"],profile["achievementsOneTime"]
        JsonObject combined = new JsonObject();
        combined.add("achievements", getNestedJson(profile, "achievements"));
        combined.add("achievementsOneTime", getNestedJson(profile, "achievementsOneTime"));
        return combined;
    }

    public String[] getMaxedGames() {
        // return an array of strings with the maxed games
        JsonElement achievements = getAchievements();

        return new String[0];
    }

    public JsonObject getProfile() {
        return profile;
    }
}

