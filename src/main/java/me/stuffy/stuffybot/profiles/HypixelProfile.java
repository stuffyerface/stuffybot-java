package me.stuffy.stuffybot.profiles;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.stuffy.stuffybot.commands.AchievementCommand;
import me.stuffy.stuffybot.utils.MiscUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static me.stuffy.stuffybot.utils.APIUtils.getAchievementsResources;
import static me.stuffy.stuffybot.utils.DiscordUtils.discordTimeUnix;
import static me.stuffy.stuffybot.utils.MiscUtils.getNestedJson;
import static me.stuffy.stuffybot.utils.MiscUtils.pitXpToLevel;

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

    public Double getNetworkLevel() {
        if (!profile.has("networkExp")) {
            return 0.0;
        }
        int networkExp = getNestedJson(profile, "networkExp").getAsInt();
        return (Math.floor(Math.sqrt(networkExp + 15312.5) - 125/Math.sqrt(2)))/(25*Math.sqrt(2));
    }

    public Integer getKarma() {
        if (!profile.has("karma")) {
            return 0;
        }
        return getNestedJson(profile, "karma").getAsInt();
    }

    public Integer getAchievementPoints() {
        if (!profile.has("achievementPoints")) {
            return 0;
        }

        return getNestedJson(profile, "achievementPoints").getAsInt();
    }

    public String getOnlineStatus() {
        if (!profile.has("lastLogin")) {
            return "Online Status Hidden";
        }
        String status = "Last online " + discordTimeUnix(getNestedJson(profile, "lastLogout").getAsLong(), "R");
        return status;
    }

    public Integer getQuestsCompleted() {
        if (!profile.has("quests")) {
            return 0;
        }
        int questsCompleted = 0;
        JsonElement quests = getNestedJson(profile, "quests");
        for (String key : quests.getAsJsonObject().keySet()) {
            if (quests.getAsJsonObject().get(key).getAsJsonObject().has("completions")) {
                questsCompleted += quests.getAsJsonObject().get(key).getAsJsonObject().get("completions").getAsJsonArray().size();
            }
        }

        return questsCompleted;
    }

    public Integer getChallengesCompleted() {
        if (!profile.has("challenges")) {
            return 0;
        }
        int challengesCompleted = 0;
        JsonElement challenges = getNestedJson(profile, "challenges", "all_time");
        for (String key : challenges.getAsJsonObject().keySet()) {
            challengesCompleted += challenges.getAsJsonObject().get(key).getAsInt();
        }

        return challengesCompleted;
    }

    public Integer getRewardStreak() {
        if (!profile.has("rewardStreak")) {
            return 0;
        }
        return getNestedJson(profile, "rewardStreak").getAsInt();
    }

    public Integer getRewardRecord() {
        if (!profile.has("rewardHighScore")) {
            return 0;
        }
        return getNestedJson(profile, "rewardHighScore").getAsInt();
    }

    public Integer getWins() {
        if (!profile.has("stats")) {
            return 0;
        }
        int totalWins = 0;

        // Arcade
        JsonObject stats = (JsonObject) getNestedJson(profile, "stats");
        List<String> winsKeys = Arrays.asList(
                // Arcade
                "Arcade.wins_party", "Arcade.wins_soccer", "Arcade.wins_mini_walls", "Arcade.wins_party_2", "Arcade.wins_farm_hunt",
                "Arcade.wins_ender", "Arcade.wins_dayone", "Arcade.wins_simon_says", "Arcade.wins_oneinthequiver",
                "Arcade.seeker_wins_hide_and_seek", "Arcade.hider_wins_hide_and_seek", "Arcade.wins_hole_in_the_wall",
                "Arcade.sw_game_wins", "Arcade.wins_zombies", "Arcade.wins_hypixel_sports", "Arcade.wins_draw_their_thing",
                "Arcade.wins_throw_out", "Arcade.wins_santa_simulator", "Arcade.wins_dragonwars2", "Arcade.wins_easter_simulator",
                "Arcade.wins_scuba_simulator", "Arcade.wins_halloween_simulator", "Arcade.wins_grinch_simulator_v2",
                "Arcade.pixel_party.wins", "Arcade.woolhunt_participated_wins", "Arcade.dropper.wins",

                // Arena Brawl
                "Arena.wins",

                // Warlords
                "Battleground.wins",

                // Blitz SG TODO: Double check this
                "HungerGames.wins", "HungerGames.wins_teams",

                // Cops and Crims TODO: Double check this
                "MCGO.game_wins", "MCGO.game_wins_deathmatch", "MCGO.game_wins_gungame",

                // Paintball
                "Paintball.wins",

                // Quakecraft TODO: Double check this
                "Quake.wins", "Quake.wins_teams", "Quake.wins_solo_tourney", "Quake.wins_tourney_quake_solo2_1",

                // TNT Games TODO: Double check this
                "TNTGames.wins", "TNTGames.wins_tourney_tnt_run_0", "TNTGames.wins_tourney_tnt_run_1",

                // UHC TODO: Double check this
                "UHC.wins", "UHC.wins_solo",

                // VampireZ
                "VampireZ.human_wins", "VampireZ.vampire_wins",

                // Walls
                "Walls.wins",

                // Mega Walls
                "Walls3.wins",

                // Turbo Kart Racers TODO: Reduce to golds?
                "GingerBread.gold_trophy", "GingerBread.tourney_gingerbread_solo_1_gold_trophy",

                // SkyWars
                "SkyWars.wins",

                // Crazy Walls
                "TrueCombat.wins",

                // Smash Heroes
                "SuperSmash.wins",

                // Speed UHC
                "SpeedUHC.wins",

                // SkyClash
                "SkyClash.wins",

                // Bed Wars TODO: Double check this
                "Bedwars.wins_bedwars",

                // Murder Mystery
                "MurderMystery.wins",

                // Duels
                "Duels.wins",

                // Build Battle
                "BuildBattle.wins",

                // Wool Wars
                "WoolGames.wool_wars.stats.wins"

        );

        totalWins += winsKeys.stream()
                .mapToInt(key -> {
                    try {
                        return getNestedJson(stats, key.split("\\.")).getAsInt();
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();

        return totalWins;
    }

    public Integer getKills() {
        if (!profile.has("stats")) {
            return 0;
        }
        int totalKills = 0;

        JsonObject stats = (JsonObject) getNestedJson(profile, "stats");
        List<String> killsKeys = Arrays.asList(
                // Arcade
                "Arcade.sw_kills", "Arcade.kills_dragonwars2", "Arcade.kills_throw_out", "Arcade.kills_oneinthequiver", // Is this right?
                "Arcade.kills_mini_walls", "Arcade.final_kills_mini_walls", "Arcade.rpg_16_kills_party", "Arcade.hunter_kills_farm_hunt",
                "Arcade.kills_farm_hunt", "Arcade.woolhunt_kills",

                // Arena Brawl
                "Arena.kills_1v1", "Arena.kills_2v2", "Arena.kills_4v4",

                // Warlords
                "Battleground.kills",

                // Blitz SG
                "HungerGames.kills",

                // Cops and Crims TODO: Check tournament kills
                "MCGO.kills", "MCGO.kills_deathmatch", "MCGO.kills_gungame",

                // Paintball
                "Paintball.kills",

                // Quakecraft TODO: Double check this
                "Quake.kills", "Quake.kills_teams",

                // TNT Games
                "TNTGames.kills_capture", "TNTGames.kills_tntag", "TNTGames.kills_pvprun",

                // UHC
                "UHC.kills", "UHC.kills_solo",

                // VampireZ
                "VampireZ.human_kills", "VampireZ.vampire_kills",

                // Walls
                "Walls.kills",

                // Mega Walls
                "Walls3.kills",

                // SkyWars
                "SkyWars.kills",

                // Crazy Walls
                "TrueCombat.kills",

                // Smash Heroes
                "SuperSmash.kills",

                // Speed UHC
                "SpeedUHC.kills_insane", "SpeedUHC.kills_normal",

                // SkyClash
                "SkyClash.kills",

                // Bed Wars
                "Bedwars.kills_bedwars", "Bedwars.final_kills_bedwars",

                // Murder Mystery
                "MurderMystery.kills",

                // Duels
                "Duels.kills", "Duels.bridge_kills",

                // Pit
                "Pit.pit_stats_ptl.kills",

                // Wool Wars
                "WoolGames.wool_wars.stats.kills"

                );
        totalKills += killsKeys.stream()
                .mapToInt(key -> {
                    try {
                        return getNestedJson(stats, key.split("\\.")).getAsInt();
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();

        return totalKills;

    }

    public JsonElement getAchievements() {
        // return an object with profile["achievements"],profile["achievementsOneTime"]
        JsonObject combined = new JsonObject();
        combined.add("achievements", getNestedJson(profile, "achievements"));
        combined.add("achievementsOneTime", getNestedJson(profile, "achievementsOneTime"));
        return combined;
    }

    public Integer getLegacyAchievementPoints() {
        Achievements achievements = new Achievements(this);
        return 0;

    }

    public Integer getPit(String stat) {
        JsonObject pitStats = getNestedJson(profile, "stats", "Pit").getAsJsonObject();
        try {
            switch (stat) {
                case "prestige" -> {
                    return getNestedJson(pitStats, "profile", "prestiges").getAsJsonArray().size();
                }
                case "level" -> {
                    return pitXpToLevel(getNestedJson(pitStats, "profile", "xp").getAsLong());
                }
                case "renown" -> {
                    return getNestedJson(pitStats, "profile", "renown").getAsInt();
                }
                case "gold" -> {
                    return (int) Math.round(getNestedJson(pitStats, "profile", "cash").getAsDouble());
                }
                case "total_gold" -> {
                    return getNestedJson(pitStats, "pit_stats_ptl", "cash_earned").getAsInt();
                }
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    public Long getPitXP() {
        JsonObject pitStats = getNestedJson(profile, "stats", "Pit").getAsJsonObject();
        return getNestedJson(pitStats, "profile", "xp").getAsLong();
    }
}

