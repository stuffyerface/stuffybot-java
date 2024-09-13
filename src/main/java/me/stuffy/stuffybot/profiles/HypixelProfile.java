package me.stuffy.stuffybot.profiles;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.stuffy.stuffybot.utils.MiscUtils;

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

                // Turbo Kart Racers
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
            return getNestedJson(0, profile, "stats", "Walls3", asString).getAsJsonObject().getAsInt();
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

    public int getAchievementsUnlocked() {
        return achievementsUnlocked;
    }

    public int getLegacyAchievementsUnlocked() {
        return legacyAchievementsUnlocked;
    }

    public int getLegacyAchievementPoints() {
        return legacyAchievementPoints;
    }

    public String getEasiestChallenge() {
        return "`Game: Easy Challenge` (0.03%)";
    }

    public String getEasiestTiered() {
        return "`Game: Close Tiered III` (97.78%)";
    }
}

