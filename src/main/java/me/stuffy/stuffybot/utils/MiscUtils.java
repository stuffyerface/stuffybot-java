package me.stuffy.stuffybot.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static me.stuffy.stuffybot.utils.APIUtils.getAchievementsResources;

public class MiscUtils {
    private static final Gson gson = new Gson();
    public static UUID formatUUID(String uuid) {
        return UUID.fromString(uuid.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"
        ));
    }

    public static JsonElement getNestedJson(JsonObject object, String... keys) {
        JsonElement currentElement = object;
        for (String key : keys) {
            // Split the key at character '.' and iterate through the keys
            String[] splitKey = key.split("\\.");
            for (String split : splitKey) {
                if (currentElement.isJsonObject() && currentElement.getAsJsonObject().has(split)) {
                    currentElement = currentElement.getAsJsonObject().get(split);
                } else {
                    throw new IllegalArgumentException("Key " + key + " not found or not a JsonObject");
                }
            }
        }
        return currentElement;
    }

    public static JsonElement getNestedJson(Integer defaultValue, Object object, String... keys) {
        try {
            return getNestedJson((JsonObject) object, keys);
        } catch (IllegalArgumentException e) {
            return stringToJson(defaultValue.toString());
        }
    }

    public static JsonElement getNestedJson(Double defaultValue, Object object, String... keys) {
        try {
            return getNestedJson((JsonObject) object, keys);
        } catch (IllegalArgumentException e) {
            return stringToJson(defaultValue.toString());
        }
    }

    public static JsonElement getNestedJson(Boolean defaultValue, Object object, String... keys) {
        try {
            return getNestedJson((JsonObject) object, keys);
        } catch (IllegalArgumentException e) {
            return stringToJson(defaultValue.toString());
        }
    }

    public static JsonElement getNestedJson(String defaultValue, Object object, String... keys) {
        try {
            return getNestedJson((JsonObject) object, keys);
        } catch (IllegalArgumentException e) {
            return stringToJson(defaultValue);
        }
    }

    public static JsonElement stringToJson(String jsonString) {
        return gson.fromJson(jsonString, JsonElement.class);
    }

    public static String convertToRomanNumeral(int number) {
        if (number <= 0) {
            return "0";
        }
        LinkedHashMap<Integer, String> lookup = new LinkedHashMap<>();
        lookup.put(1000, "M");
        lookup.put(900, "CM");
        lookup.put(500, "D");
        lookup.put(400, "CD");
        lookup.put(100, "C");
        lookup.put(90, "XC");
        lookup.put(50, "L");
        lookup.put(40, "XL");
        lookup.put(10, "X");
        lookup.put(9, "IX");
        lookup.put(5, "V");
        lookup.put(4, "IV");
        lookup.put(1, "I");

        StringBuilder roman = new StringBuilder();
        for (Map.Entry<Integer, String> entry : lookup.entrySet()) {
            while (number >= entry.getKey()) {
                roman.append(entry.getValue());
                number -= entry.getKey();
            }
        }
        return roman.toString();
    }

    public static Integer pitXpToLevel(long experience) {
        long[] prestige_xp = { 65950L, 138510L, 217680L, 303430L, 395760L,
                        494700L, 610140L, 742040L, 906930L, 1104780L, 1368580L,
                        1698330L, 2094030L, 2555680L, 3083280L, 3676830L, 4336330L,
                        5127730L, 6051030L, 7106230L, 8293330L, 9612330L, 11195130L,
                        13041730L, 15152130L, 17526330L, 20164330L, 23132080L,
                        26429580L, 31375830L, 37970830L, 44631780L, 51292730L,
                        57953680L, 64614630L, 71275580L, 84465580L, 104250580L,
                        130630580L, 163605580L, 213068080L, 279018080L, 361455580L,
                        460380580L,575793080L, 707693080L, 905543080L, 1235293080L,
                        1894793080L, 5192293080L, 11787293080L };
        int[] xp_map = {15, 30, 50, 75, 125, 300, 600, 800, 900, 1000, 1200, 1500, 0};
        int[] prestigeXPModifiers = { 100, 110, 120, 130, 140, 150, 175, 200, 250, 300, 400, 500, 600, 700, 800, 900, 1000, 1200, 1400, 1600, 1800, 2000, 2400, 2800, 3200, 3600, 4000, 4500, 5000, 7500, 10000, 10100, 10100, 10100, 10100, 10100, 20000, 30000, 40000, 50000, 75000, 100000, 125000, 150000, 175000, 200000, 300000, 500000, 1000000, 5000000, 10000000};

        int workingPrestige = 0;
        int workingLevel = 120;
        long maxXpCurrentPrestige = 0;

        for (; workingPrestige < 50; workingPrestige++) {
            if (experience <= prestige_xp[workingPrestige]) {
                break;
            }
        }

        maxXpCurrentPrestige = prestige_xp[workingPrestige];

        while (maxXpCurrentPrestige > experience) {
            workingLevel--;
            maxXpCurrentPrestige = (long) (maxXpCurrentPrestige - Math.ceil((double) (xp_map[(workingLevel / 10)] * prestigeXPModifiers[workingPrestige]) / 100));
        }

        return workingLevel;
    }

    public static String toSkillIssue(String toModify) {
        // An Error, an error, AN ERROR
        return toModify
                .replace("n error", " skill issue")
                .replace("n Error", " Skill Issue")
                .replace("N ERROR", " SKILL ISSUE")
                .replace("error", "skill issue")
                .replace("Error", "Skill Issue")
                .replace("ERROR", "SKILL ISSUE");
    }

    public static boolean requiresIgn(String commandName) {
        return true;
    }

    public static boolean validCommand(String commandName) {
        return true;
    }

    public static String genBase64(Integer length){
        String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        SecureRandom RANDOM = new SecureRandom();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(BASE62.length());
            sb.append(BASE62.charAt(randomIndex));
        }
        return sb.toString();
    }

    public static String toReadableName(String resourcesName) {
        Map<String,String> resourceNames = new HashMap<>();
        resourceNames.put("arcade", "Arcade");
        resourceNames.put("arena", "Arena Brawl");
        resourceNames.put("bedwars", "Bed Wars");
        resourceNames.put("blitz", "Blitz Survival Games");
        resourceNames.put("buildbattle", "Build Battle");
        resourceNames.put("christmas2017", "Christmas");
        resourceNames.put("copsandcrims", "Cops and Crims");
        resourceNames.put("duels", "Duels");
        resourceNames.put("easter", "Easter");
        resourceNames.put("general", "General");
        resourceNames.put("gingerbread", "Turbo Kart Racers");
        resourceNames.put("halloween2017", "Halloween");
        resourceNames.put("housing", "Housing");
        resourceNames.put("murdermystery", "Murder Mystery");
        resourceNames.put("paintball", "Paintball");
        resourceNames.put("pit", "Pit");
        resourceNames.put("quake", "Quakecraft");
        resourceNames.put("skyblock", "SkyBlock");
        resourceNames.put("skyclash", "SkyClash");
        resourceNames.put("skywars", "SkyWars");
        resourceNames.put("speeduhc", "Speed UHC");
        resourceNames.put("summer", "Summer");
        resourceNames.put("supersmash", "Smash Heroes");
        resourceNames.put("tntgames", "The TNT Games");
        resourceNames.put("truecombat", "Crazy Walls");
        resourceNames.put("uhc", "UHC Champions");
        resourceNames.put("vampirez", "VampireZ");
        resourceNames.put("walls", "Walls");
        resourceNames.put("walls3", "Mega Walls");
        resourceNames.put("warlords", "Warlords");
        resourceNames.put("woolgames", "Wool Games");

        return resourceNames.getOrDefault(resourcesName, resourcesName);
    }

    public static String minutesFormatted(int minutes) {
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        if (hours == 0)
            return remainingMinutes + "m";
        return hours + "h " + remainingMinutes + "m";
    }
    public static int getMaxAchievements() {
        JsonObject achievementData = getAchievementsResources().getAsJsonObject();
        int total = 0;
        for (String game : achievementData.keySet()) {
            JsonObject gameData = achievementData.getAsJsonObject(game);

            JsonObject oneTime = gameData.getAsJsonObject("one_time");
            JsonObject tiered = gameData.getAsJsonObject("tiered");

            for (String key : oneTime.keySet()) {
                boolean isLegacy = getNestedJson(false, oneTime, key, "legacy").getAsBoolean();
                if (!isLegacy) {
                    total++;
                }
            }

            for (String key : tiered.keySet()) {
                boolean isLegacy = getNestedJson(false, tiered, key, "legacy").getAsBoolean();
                if (!isLegacy) {
                    total+= getNestedJson(tiered, key, "tiers").getAsJsonArray().size();
                }
            }
        }
        return total;
    }

    public static int getMaxAchievementPoints() {
        JsonObject achievementData = getAchievementsResources().getAsJsonObject();
        int total = 0;
        for (String game : achievementData.keySet()) {
            JsonObject gameData = achievementData.getAsJsonObject(game);

            int totalPoints = getNestedJson(0, gameData, "total_points").getAsInt();
            total += totalPoints;
        }
        return total;
    }
}
