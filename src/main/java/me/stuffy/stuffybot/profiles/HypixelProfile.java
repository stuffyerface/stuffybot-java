package me.stuffy.stuffybot.profiles;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.stuffy.stuffybot.utils.MiscUtils;

import java.text.DecimalFormat;
import java.util.*;

import static me.stuffy.stuffybot.utils.APIUtils.getAchievementsResources;
import static me.stuffy.stuffybot.utils.DiscordUtils.discordTimeUnix;
import static me.stuffy.stuffybot.utils.MiscUtils.*;

public class HypixelProfile {
    private final UUID uuid;
    private final Rank rank;
    private final JsonObject profile;
    private String displayName;
    private final int achievementPoints;
    private int achievementsUnlocked;
    private int legacyAchievementPoints;
    private int legacyAchievementsUnlocked;
    private String easiestChallenge;
    private double easiestChallengeGlobalPercent;

    public HypixelProfile(JsonObject profile) {
        this.profile = profile.deepCopy();
        this.uuid = MiscUtils.formatUUID(profile.get("uuid").getAsString());
        this.displayName = profile.get("displayname").getAsString();
        this.rank = determineRank(profile);
        this.achievementPoints = getNestedJson(0, profile, "achievementPoints").getAsInt();

        instantiateAchievements();
    }

    private void instantiateAchievements() {
        int unlockCount = 0;
        int unlockCountLegacy = 0;
        int pointCountLegacy = 0;

        String easiestChallenge = null;
        double easiestChallengeGlobalPercent = 0;

        JsonObject achievements = getAchievements();
        List<JsonElement> playerOneTime = achievements.get("achievementsOneTime").getAsJsonArray().asList();
        List<String> playerOneTimeString = new ArrayList<>();
        for (JsonElement element : playerOneTime) {
            try {
                playerOneTimeString.add(element.getAsString());
            } catch (Exception ignored) {
            }
        }
        JsonObject playerTiered = achievements.get("achievementsTiered").getAsJsonObject();
        JsonElement achievementsResources = getAchievementsResources();
        for (String game : achievementsResources.getAsJsonObject().keySet()) {
            for (String oneTime : getNestedJson(achievementsResources.getAsJsonObject(), game, "one_time").getAsJsonObject().keySet()) {
                boolean isLegacy = getNestedJson(false, achievementsResources.getAsJsonObject(), game, "one_time", oneTime, "legacy").getAsBoolean();
                if (playerOneTimeString.contains((game + "_" + oneTime.toLowerCase()))) {
                    if (!isLegacy) {
                        unlockCount++;
                    } else {
                        unlockCountLegacy++;
                        pointCountLegacy += getNestedJson(achievementsResources.getAsJsonObject(), game, "one_time", oneTime, "points").getAsInt();
                    }
                } else {
                    double globalPercentUnlocked = getNestedJson(0.0, achievementsResources.getAsJsonObject(), game, "one_time", oneTime, "globalPercentUnlocked").getAsDouble();
                    if (globalPercentUnlocked > easiestChallengeGlobalPercent) {
                        easiestChallengeGlobalPercent = globalPercentUnlocked;
                        easiestChallenge = getNestedJson("Unknown", achievementsResources.getAsJsonObject(), game, "one_time", oneTime, "name").getAsString();
                    }
                }
            }

            for (String tiered : getNestedJson(achievementsResources.getAsJsonObject(), game, "tiered").getAsJsonObject().keySet()) {
                boolean isLegacy = getNestedJson(false, achievementsResources.getAsJsonObject(), game, "tiered", tiered, "legacy").getAsBoolean();
                for (JsonElement tier : getNestedJson(achievementsResources.getAsJsonObject(), game, "tiered", tiered, "tiers").getAsJsonArray()) {
                    int tierAmount = tier.getAsJsonObject().get("amount").getAsInt();
                    if (getNestedJson(0, playerTiered, game + "_" + tiered.toLowerCase()).getAsInt() >= tierAmount) {
                        if (!isLegacy) {
                            unlockCount++;
                        } else {
                            unlockCountLegacy++;
                            pointCountLegacy += tier.getAsJsonObject().get("points").getAsInt();
                        }
                    }
                }
            }
        }
        this.achievementsUnlocked = unlockCount;
        this.legacyAchievementsUnlocked = unlockCountLegacy;
        this.legacyAchievementPoints = pointCountLegacy;
        this.easiestChallenge = easiestChallenge;
        this.easiestChallengeGlobalPercent = easiestChallengeGlobalPercent;
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

        try {
            rankString = profile.get("newPackageRank").getAsString();
        } catch (Exception e){
            try {
                rankString = profile.get("packageRank").getAsString();
            } catch (Exception e2) {
                return Rank.NONE;
            }
        }
        return Rank.fromString(rankString);
    }

    public UUID getUuid() {
        return uuid;
    }

    public HypixelProfile setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
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

    public int getAchievementPoints() {
        return this.achievementPoints;
    }

    public String getOnlineStatus() {
        if (!profile.has("lastLogin")) {
            return "Online Status Hidden";
        }
        if (profile.get("lastLogin").getAsLong() > profile.get("lastLogout").getAsLong()) {
            return "Online since " + discordTimeUnix(getNestedJson(profile, "lastLogin").getAsLong(), "R");
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
        if (!profile.has("rewardScore")) {
            return 0;
        }
        return getNestedJson(profile, "rewardScore").getAsInt();
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
                "Arcade.pixel_party.wins", "Arcade.dropper.wins", "Arcade.disasters.stats.wins",

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

                // Turbo Kart Racers
                "GingerBread.gold_trophy", "GingerBread.tourney_gingerbread_solo_1_gold_trophy",

                // SkyWars
                "SkyWars.wins",
                "SkyWars.wins_lab",

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

                // Wool Games
                "WoolGames.wool_wars.stats.wins",
                "WoolGames.sheep_wars.stats.wins",
                "WoolGames.capture_the_wool.participated_wins"

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
                "Arcade.kills_farm_hunt",

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
                "SkyWars.kills_lab",

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
                "WoolGames.wool_wars.stats.kills",
                "WoolGames.capture_the_wool.stats.kills",
                "WoolGames.sheep_wars.stats.kills"

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

    public JsonObject getAchievements() {
        // return an object with profile["achievements"],profile["achievementsOneTime"]
        JsonObject combined = new JsonObject();
        combined.add("achievementsTiered", getNestedJson(profile, "achievements"));
        combined.add("achievementsOneTime", getNestedJson(profile, "achievementsOneTime"));
        return combined;
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
                case "ingots_collector" -> {
                    return getNestedJson(pitStats, "pit_stats_ptl", "ingots_picked_up").getAsInt();
                }
                case "golden_treat" -> {
                    return getNestedJson(pitStats, "pit_stats_ptl", "ghead_eaten").getAsInt();
                }
                case "golden_age" -> {
                    return getNestedJson(pitStats, "pit_stats_ptl", "extra_from_trickle_down").getAsInt();
                }
                case "infinite_quiver" -> {
                    return getNestedJson(pitStats, "pit_stats_ptl", "endless_quiver_arrows").getAsInt();
                }
                case "lucky_diamond" -> {
                    return getNestedJson(pitStats, "pit_stats_ptl", "lucky_diamond_pieces").getAsInt();
                }
                case "im_mining_here" -> {
                    return getNestedJson(pitStats, "pit_stats_ptl", "obsidian_broken").getAsInt();
                }
                case "nosferatu" -> {
                    return getNestedJson(pitStats, "pit_stats_ptl", "vampire_healed_hp").getAsInt();
                }
                case "raging_hunter" -> {
                    return getNestedJson(pitStats, "pit_stats_ptl", "rage_potatoes_eaten").getAsInt();
                }
                case "bounty_hunter" -> {
                    return getNestedJson(pitStats, "pit_stats_ptl", "bounties_of_500g_with_bh").getAsInt();
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

    public Long getPitTotalXpRequirement(Integer prestige) {
        switch(prestige) {
            case 1 -> {
                return 65950L;
            }
            case 2 -> {
                return 138510L;
            }
            case 3 -> {
                return 217680L;
            }
            case 4 -> {
                return 303430L;
            }
            case 5 -> {
                return 395760L;
            }
            case 6 -> {
                return 494700L;
            }
            case 7 -> {
                return 610140L;
            }
            case 8 -> {
                return 742040L;
            }
            case 9 -> {
                return 906930L;
            }
            case 10 -> {
                return 1104780L;
            }
            case 11 -> {
                return 1368580L;
            }
            case 12 -> {
                return 1698330L;
            }
            case 13 -> {
                return 2094030L;
            }
            case 14 -> {
                return 2555680L;
            }
            case 15 -> {
                return 3083280L;
            }
            case 16 -> {
                return 3676830L;
            }
            case 17 -> {
                return 4336330L;
            }
            case 18 -> {
                return 5127730L;
            }
            case 19 -> {
                return 6051030L;
            }
            case 20 -> {
                return 7106230L;
            }
            case 21 -> {
                return 8293330L;
            }
            case 22 -> {
                return 9612330L;
            }
            case 23 -> {
                return 11195130L;
            }
            case 24 -> {
                return 13041730L;
            }
            case 25 -> {
                return 15152130L;
            }
            case 26 -> {
                return 17526330L;
            }
            case 27 -> {
                return 20164330L;
            }
            case 28 -> {
                return 23132080L;
            }
            case 29 -> {
                return 26429580L;
            }
            case 30 -> {
                return 31375830L;
            }
            case 31 -> {
                return 37970830L;
            }
            case 32 -> {
                return 44631780L;
            }
            case 33 -> {
                return 51292730L;
            }
            case 34 -> {
                return 57953680L;
            }
            case 35 -> {
                return 64614630L;
            }
            case 36 -> {
                return 71275580L;
            }
            case 37 -> {
                return 84465580L;
            }
            case 38 -> {
                return 104250580L;
            }
            case 39 -> {
                return 130630580L;
            }
            case 40 -> {
                return 163605580L;
            }
            case 41 -> {
                return 213068080L;
            }
            case 42 -> {
                return 279018080L;
            }
            case 43 -> {
                return 361455580L;
            }
            case 44 -> {
                return 460380580L;
            }
            case 45 -> {
                return 575793080L;
            }
            case 46 -> {
                return 707693080L;
            }
            case 47 -> {
                return 905543080L;
            }
            case 48 -> {
                return 1235293080L;
            }
            case 49 -> {
                return 1894793080L;
            }
            case 50 -> {
                return 5192293080L;
            }
            case 51 -> {
                return 11787293080L;
            }
            default -> {
                return 0L;
            }
        }
    }

    public Integer getPitPrestigeGoldRequirement(Integer prestige) {
        switch(prestige) {
            case 1 -> {
                return 10000;
            }
            case 2,3,4 -> {
                return 20000;
            }
            case 5 -> {
                return 30000;
            }
            case 6 -> {
                return 35000;
            }
            case 7 -> {
                return 40000;
            }
            case 8 -> {
                return 45000;
            }
            case 9 -> {
                return 50000;
            }
            case 10 -> {
                return 60000;
            }
            case 11 -> {
                return 70000;
            }
            case 12 -> {
                return 80000;
            }
            case 13 -> {
                return 90000;
            }
            case 14 -> {
                return 100000;
            }
            case 15 -> {
                return 125000;
            }
            case 16 -> {
                return 150000;
            }
            case 17 -> {
                return 175000;
            }
            case 18 -> {
                return 200000;
            }
            case 19 -> {
                return 250000;
            }
            case 20 -> {
                return 300000;
            }
            case 21 -> {
                return 350000;
            }
            case 22 -> {
                return 400000;
            }
            case 23 -> {
                return 500000;
            }
            case 24 -> {
                return 600000;
            }
            case 25 -> {
                return 700000;
            }
            case 26 -> {
                return 800000;
            }
            case 27 -> {
                return 900000;
            }
            case 28,29,30,31,32,33 -> {
                return 1000000;
            }
            case 34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50 -> {
                return 2000000;
            }
            default -> {
                return 0;
            }
        }
    }


    public Map<String, Boolean> getTkrMaps() {
        JsonObject tkrStats = getNestedJson(profile, "stats", "GingerBread").getAsJsonObject();
        Map<String, String> winKeys = Map.of(
                "gold_trophy_canyon", "Canyon",
                "gold_trophy_hypixelgp", "Hypixel GP",
                "gold_trophy_junglerush", "Jungle Rush",
                "gold_trophy_olympus", "Olympus",
                "gold_trophy_retro", "Retro"
        );
        Map<String, Boolean> uniqueGolds = new HashMap<>();

        for (String key : winKeys.keySet()) {
            try {
                if (getNestedJson(tkrStats, key).getAsInt() >= 1) {
                    uniqueGolds.put(winKeys.get(key), true);
                } else {
                    uniqueGolds.put(winKeys.get(key), false);
                }
            } catch (Exception e) {
                uniqueGolds.put(winKeys.get(key), false);
            }
        }
        return uniqueGolds;
    }


    public Map<String, Boolean> getMegaWallsLegendaries() {
        JsonObject achievements = getAchievements();
        JsonArray oneTime = achievements.get("achievementsOneTime").getAsJsonArray();
        Map<String, Boolean> legendarySkins = new HashMap<>();

        Map<String, String> allLegendaries = new HashMap<>();
        allLegendaries.put("walls3_legendary_cow", "Cow");
        allLegendaries.put("walls3_legendary_hunter", "Hunter");
        allLegendaries.put("walls3_legendary_shark", "Shark");
        allLegendaries.put("walls3_legendary_arcanist", "Arcanist");
        allLegendaries.put("walls3_legendary_dreadlord", "Dreadlord");
        allLegendaries.put("walls3_legendary_golem", "Golem");
        allLegendaries.put("walls3_legendary_herobrine", "Herobrine");
        allLegendaries.put("walls3_legendary_pigman", "Pigman");
        allLegendaries.put("walls3_legendary_zombie", "Zombie");
        allLegendaries.put("walls3_legendary_blaze", "Blaze");
        allLegendaries.put("walls3_legendary_enderman", "Enderman");
        allLegendaries.put("walls3_legendary_shaman", "Shaman");
        allLegendaries.put("walls3_legendary_squid", "Squid");
        allLegendaries.put("walls3_legendary_creeper", "Creeper");
        allLegendaries.put("walls3_legendary_pirate", "Pirate");
        allLegendaries.put("walls3_legendary_sheep", "Sheep");
        allLegendaries.put("walls3_legendary_skeleton", "Skeleton");
        allLegendaries.put("walls3_legendary_spider", "Spider");
        allLegendaries.put("walls3_legendary_werewolf", "Werewolf");
        allLegendaries.put("walls3_legendary_angel", "Angel");
        allLegendaries.put("walls3_legendary_assassin", "Assassin");
        allLegendaries.put("walls3_legendary_automaton", "Automaton");
        allLegendaries.put("walls3_legendary_moleman", "Moleman");
        allLegendaries.put("walls3_legendary_phoenix", "Phoenix");
        allLegendaries.put("walls3_legendary_dragon", "Dragon");
        allLegendaries.put("walls3_legendary_renegade", "Renegade");
        allLegendaries.put("walls3_legendary_snowman", "Snowman");


        for (String skin : allLegendaries.keySet()) {
            if (oneTime.contains(stringToJson(skin))) {
                legendarySkins.put(allLegendaries.get(skin), true);
            } else {
                legendarySkins.put(allLegendaries.get(skin), false);
            }
        }

        return legendarySkins;
    }

    public Integer getMegaWallsFinalKills() {
        return getNestedJson(0, profile, "stats", "Walls3", "final_kills").getAsInt();
    }

    public Integer getMegaWallsWins() {
        return getNestedJson(0, profile, "stats", "Walls3", "wins").getAsInt();
    }

    public Integer getMegaWallsClassPoints() {
        return getNestedJson(0, profile, "stats", "Walls3", "class_points").getAsInt();
    }

    public String getMegaWallsSelectedClass() {
        return getNestedJson("None", profile, "stats", "Walls3", "chosen_class").getAsString();
    }

    public ArrayList<String> getMaxGames() {
        ArrayList<String> maxGames = new ArrayList<>();
        JsonObject achievements = getAchievements();
        List<JsonElement> playerOneTime = achievements.get("achievementsOneTime").getAsJsonArray().asList();
        List<String> playerOneTimeString = new ArrayList<>();
        for (JsonElement element : playerOneTime) {
            // Account for Hypixel Bug listing achievements as Empty Arrays instead of Strings
            try {
                playerOneTimeString.add(element.getAsString());
            } catch (Exception ignored) {
            }
        }
        JsonObject playerTiered = achievements.get("achievementsTiered").getAsJsonObject();
        JsonElement achievementsResources = getAchievementsResources();
        for (String game : achievementsResources.getAsJsonObject().keySet()) {
            if (game.equals("skyclash") || game.equals("truecombat")) {
                continue;
            }
            boolean maxed = true;
            for ( String oneTime : getNestedJson(achievementsResources.getAsJsonObject(), game, "one_time").getAsJsonObject().keySet()) {
                boolean isLegacy = getNestedJson(false, achievementsResources.getAsJsonObject(), game, "one_time", oneTime, "legacy").getAsBoolean();
                if (isLegacy) {
                    continue;
                }
                if (!playerOneTimeString.contains((game + "_" + oneTime.toLowerCase()))) {
                    maxed = false;
                    break;
                }
            }

            if (!maxed) {
                continue;
            }

            for (String tiered : getNestedJson(achievementsResources.getAsJsonObject(), game, "tiered").getAsJsonObject().keySet()) {
                boolean isLegacy = getNestedJson(false, achievementsResources.getAsJsonObject(), game, "tiered", tiered, "legacy").getAsBoolean();
                if (isLegacy) {
                    continue;
                }

                int maxTier = 0;
                for (JsonElement tier : getNestedJson(0, achievementsResources.getAsJsonObject(), game, "tiered", tiered, "tiers").getAsJsonArray()) {
                    int tierAmount = tier.getAsJsonObject().get("amount").getAsInt();
                    if (tierAmount > maxTier) {
                        maxTier = tierAmount;
                    }
                }
                if (getNestedJson(0, playerTiered, game + "_" + tiered.toLowerCase()).getAsInt() < maxTier) {
                    maxed = false;
                    break;
                }
            }

            if (maxed) {
                maxGames.add(game);
            }
        }



        return maxGames;
    }

    public Map<String, Integer> getBlitzStats() {
        Map<String, Integer> blitzStats = new HashMap<>();
        blitzStats.put("Ranger", getNestedJson(0, profile, "stats", "HungerGames", "exp_ranger").getAsInt());
        blitzStats.put("Donkey Tamer", getNestedJson(0, profile, "stats", "HungerGames", "exp_donkeytamer").getAsInt());
        blitzStats.put("Phoenix", getNestedJson(0, profile, "stats", "HungerGames", "exp_phoenix").getAsInt());
        blitzStats.put("Warrior", getNestedJson(0, profile, "stats", "HungerGames", "exp_warrior").getAsInt());

        return blitzStats;
    }

    public Integer getMegaWallsStat(String asString) {
        try {
            return getNestedJson(0, profile, "stats", "Walls3", asString).getAsInt();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public Integer getTourneyGamesPlayed(String tournamentField) {
        try {
            return getNestedJson(0, profile, "tourney", tournamentField, "games_played").getAsInt();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public Integer getTourneyTimePlayed(String tournamentField) {
        try {
            return getNestedJson(0, profile, "tourney", tournamentField, "playtime").getAsInt();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public Integer getTourneyTributesEarned(String tournamentField) {
        try {
            return getNestedJson(0, profile, "tourney", tournamentField, "tributes_earned").getAsInt();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public Integer getStat(String field) {
        try {
            return getNestedJson(0, profile, "stats", field).getAsInt();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public String getStatFormatted(String field) {
        try {
            DecimalFormat df = new DecimalFormat("#,###");
            return df.format(getStat(field));
        } catch (IllegalArgumentException e) {
            return "0";
        }
    }

    public String getStatString(String field) {
        try {
            return getNestedJson("", profile, "stats", field).getAsString();
        } catch (IllegalArgumentException | NullPointerException e) {
            return "";
        }
    }

    public JsonArray getStatArray(String field) {
        try {
            return getNestedJson(profile, "stats", field).getAsJsonArray();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return new JsonArray();
        }
    }

    public JsonObject getStatObject(String field) {
        try {
            return getNestedJson(profile, "stats", field).getAsJsonObject();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return new JsonObject();
        }
    }

    public int getAchievementsUnlocked() {
        return this.achievementsUnlocked;
    }

    public int getLegacyAchievementsUnlocked() {
        return this.legacyAchievementsUnlocked;
    }

    public int getLegacyAchievementPoints() {
        return this.legacyAchievementPoints;
    }

    public String getEasiestChallenge() {
        return this.easiestChallenge + " (" + String.format("%.2f", this.easiestChallengeGlobalPercent) + "%)";
    }

    public String getEasiestTiered() {
        return "`Game: Close Tiered III` (97.78%)";
    }
}

