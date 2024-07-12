package me.stuffy.stuffybot.profiles;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.stuffy.stuffybot.utils.MiscUtils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static me.stuffy.stuffybot.utils.APIUtils.getAchievementsResources;
import static me.stuffy.stuffybot.utils.DiscordUtils.discordTimeUnix;
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
        JsonElement allAchievements = getAchievementsResources();
        JsonElement achievements = getNestedJson(profile, "achievements");

        return new String[0];
    }

    public JsonObject getProfile() {
        return profile;
    }

    public String getFirstLogin() {
        Long firstLogin = getNestedJson(profile, "firstLogin").getAsLong();
        return discordTimeUnix(firstLogin, "D") + " " + discordTimeUnix(firstLogin, "R");
    }

    public String getNetworkLevel() {
        if (!profile.has("networkExp")) {
            return "0";
        }
        Integer networkExp = getNestedJson(profile, "networkExp").getAsInt();
        DecimalFormat df = new DecimalFormat("#,###.##");
        return df.format((Math.floor(Math.sqrt(networkExp + 15312.5) - 125/Math.sqrt(2)))/(25*Math.sqrt(2)));
    }

    public String getKarma() {
        if (!profile.has("karma")) {
            return "0";
        }
        Integer karma = getNestedJson(profile, "karma").getAsInt();
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(karma);
    }

    public String getAchievementPoints() {
        if (!profile.has("achievementPoints")) {
            return "0";
        }
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(getNestedJson(profile, "achievementPoints").getAsInt());
    }

    public String getOnlineStatus() {
        if (!profile.has("lastLogin")) {
            return "Online Status Hidden";
        }
        String status = "Last online " + discordTimeUnix(getNestedJson(profile, "lastLogout").getAsLong(), "R");
        return status;
    }

    public String getQuestsCompleted() {
        if (!profile.has("quests")) {
            return "0";
        }
        Integer questsCompleted = 0;
        JsonElement quests = getNestedJson(profile, "quests");
        for (String key : quests.getAsJsonObject().keySet()) {
            if (quests.getAsJsonObject().get(key).getAsJsonObject().has("completions")) {
                questsCompleted += quests.getAsJsonObject().get(key).getAsJsonObject().get("completions").getAsJsonArray().size();
            }
        }

        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(questsCompleted);
    }

    public String getChallengesCompleted() {
        if (!profile.has("challenges")) {
            return "0";
        }
        Integer challengesCompleted = 0;
        JsonElement challenges = getNestedJson(profile, "challenges", "all_time");
        for (String key : challenges.getAsJsonObject().keySet()) {
            challengesCompleted += challenges.getAsJsonObject().get(key).getAsInt();
        }

        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(challengesCompleted);
    }

    public String getRewardStreak() {
        String rewardStreak = "";
        if (!profile.has("rewardScore")) {
            rewardStreak += "0";
        } else {
            rewardStreak += getNestedJson(profile, "rewardScore").getAsString();
        }

        if (!profile.has("rewardHighScore")) {
            rewardStreak += "/0";
        } else {
            rewardStreak += "/" + getNestedJson(profile, "rewardHighScore").getAsString();
        }
        return rewardStreak;
    }

    public String getWins() {
        if (!profile.has("stats")) {
            return "0";
        }
        Integer totalWins = 0;

        // Arcade
        JsonObject arcade = (JsonObject) getNestedJson(profile, "stats", "Arcade");
        List<String> gameKeysArcade = Arrays.asList(
                "wins_party", "wins_soccer", "wins_mini_walls", "wins_party_2", "wins_farm_hunt",
                "wins_ender", "wins_dayone", "wins_simon_says", "wins_oneinthequiver",
                "seeker_wins_hide_and_seek", "hider_wins_hide_and_seek", "wins_hole_in_the_wall",
                "sw_game_wins", "wins_zombies", "wins_hypixel_sports", "wins_draw_their_thing",
                "wins_throw_out", "wins_santa_simulator", "wins_dragonwars2", "wins_easter_simulator",
                "wins_scuba_simulator", "wins_halloween_simulator", "wins_grinch_simulator_v2",
                "pixel_party.wins", "woolhunt_participated_wins", "dropper.wins"
        );

        totalWins += gameKeysArcade.stream()
                .mapToInt(key -> {
                    try {
                        return getNestedJson(arcade, key.split("\\.")).getAsInt();
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();

        // Arena
        try {
            totalWins += getNestedJson(profile, "stats", "Arena", "wins").getAsInt();
        } catch (Exception e) {
            // do nothing
        }

        // Battleground (Warlords)
        try {
            totalWins += getNestedJson(profile, "stats", "Battleground", "wins").getAsInt();
        } catch (Exception e) {
            // do nothing
        }

        // HungerGames (Blitz)




        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(totalWins);
    }
}

