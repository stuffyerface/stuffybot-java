package me.stuffy.stuffybot.profiles.games.warlords;

import com.google.gson.JsonObject;

import java.util.*;

import static me.stuffy.stuffybot.profiles.games.warlords.WarlordsUtils.getClassRaw;
import static me.stuffy.stuffybot.profiles.games.warlords.WarlordsUtils.getSpecName;

public class Weapon {
    private final String name;
    private final double weaponScore;
    private final String weaponId;
    private final int weaponPoints;
    private final WeaponRarity weaponRarity;
    private final int weaponUpgradeLevel;
    private final int weaponUpgradeMaxLevel;

    public Weapon(JsonObject weaponJson) {
        this.weaponId = getWeaponId(weaponJson);
        this.weaponRarity = getWeaponRarity(weaponJson);
        this.weaponUpgradeLevel = weaponJson.has("upgradeTimes") && !weaponJson.get("upgradeTimes").isJsonNull() ? weaponJson.get("upgradeTimes").getAsInt() : 0;
        this.weaponUpgradeMaxLevel = weaponJson.has("upgradeMax") && !weaponJson.get("upgradeMax").isJsonNull() ? weaponJson.get("upgradeMax").getAsInt() : 0;
        this.weaponScore = getWeaponScore(this, weaponJson);
        this.weaponPoints = getWeaponPoints(this, weaponJson);
        this.name = getWeaponName(this, weaponJson);
    }



    private enum WeaponStats {
        DAMAGE,
        CHANCE,
        MULTIPLIER,
        HEALTH,
        ENERGY,
        COOLDOWN,
        MOVEMENT;
    }


    public enum WeaponRarity {
        COMMON,
        RARE,
        EPIC,
        LEGENDARY;
    }
    public record Range(int min, int max) {}

    private static final Map<WeaponRarity, Map<WeaponStats, Range>> rarityStatRanges = new EnumMap<>(WeaponRarity.class);
    // Rarity stat ranges by weapon rarity
    static {
        rarityStatRanges.put(WeaponRarity.COMMON, Map.of(
                WeaponStats.DAMAGE, new Range(90, 100),
                WeaponStats.CHANCE, new Range(10, 18),
                WeaponStats.MULTIPLIER, new Range(150, 170),
                WeaponStats.HEALTH, new Range(180, 220)
        ));
        rarityStatRanges.put(WeaponRarity.RARE, Map.of(
                WeaponStats.DAMAGE, new Range(95, 105),
                WeaponStats.CHANCE, new Range(12, 20),
                WeaponStats.MULTIPLIER, new Range(160, 180),
                WeaponStats.HEALTH, new Range(200, 250),
                WeaponStats.ENERGY, new Range(10, 18)
        ));
        rarityStatRanges.put(WeaponRarity.EPIC, Map.of(
                WeaponStats.DAMAGE, new Range(100, 110),
                WeaponStats.CHANCE, new Range(15, 20),
                WeaponStats.MULTIPLIER, new Range(160, 190),
                WeaponStats.HEALTH, new Range(220, 275),
                WeaponStats.ENERGY, new Range(15, 20),
                WeaponStats.COOLDOWN, new Range(3, 5)
        ));
        rarityStatRanges.put(WeaponRarity.LEGENDARY, Map.of(
                WeaponStats.DAMAGE, new Range(110, 120),
                WeaponStats.CHANCE, new Range(15, 25),
                WeaponStats.MULTIPLIER, new Range(180, 200),
                WeaponStats.HEALTH, new Range(250, 400),
                WeaponStats.ENERGY, new Range(20, 25),
                WeaponStats.COOLDOWN, new Range(5, 10),
                WeaponStats.MOVEMENT, new Range(5, 10)
        ));
    }
    private static final Map<WeaponRarity, ArrayList<Integer>> rarityStatRangeTotals = new EnumMap<>(WeaponRarity.class);

    static {
        for (WeaponRarity rarity : WeaponRarity.values()) {
            int minTotal = 0;
            int maxTotal = 0;
            for (Range range : rarityStatRanges.get(rarity).values()) {
                minTotal += range.min();
                maxTotal += range.max();
            }
            rarityStatRangeTotals.put(rarity, new ArrayList<>(List.of(minTotal, maxTotal)));
        }
    }
    private static final Map<WeaponRarity, ArrayList<String>> rarityPrefixes = new EnumMap<>(WeaponRarity.class);

    static {
        rarityPrefixes.put(WeaponRarity.COMMON, new ArrayList<>(List.of("Crumbly", "Flimsy", "Rough", "Honed", "Refined", "Balanced")));
        rarityPrefixes.put(WeaponRarity.RARE, new ArrayList<>(List.of("Savage", "Vicious", "Deadly", "Perfect")));
        rarityPrefixes.put(WeaponRarity.EPIC, new ArrayList<>(List.of("Fierce", "Mighty", "Brutal", "Gladiator's")));
        rarityPrefixes.put(WeaponRarity.LEGENDARY, new ArrayList<>(List.of("Vanquisher's", "Champion's", "Warlord's")));
    }


    private static String getWeaponName(Weapon weapon, JsonObject weaponObject) {
        String weaponPrefix = getWeaponPrefix(weapon);

        String material = weaponObject.get("material").getAsString();
        String weaponBaseName = determineWeaponBaseName(material);

        JsonObject specObject = weaponObject.get("spec").getAsJsonObject();
        String specName = getSpecName(Objects.requireNonNull(getClassRaw(specObject.get("playerClass").getAsInt())), specObject.get("spec").getAsInt());

        return weaponPrefix + " " + weaponBaseName + " of the " + specName;

    }

    private static String getWeaponId(JsonObject weaponObject) {
        String weaponId = null;
        if (weaponObject.has("id") && !weaponObject.get("id").isJsonNull()) {
            weaponId = weaponObject.get("id").getAsString();
        }
        return weaponId;
    }

    private static String determineWeaponBaseName(String material) {
        return switch (material) {
            case "APPLE" -> "Enderfist";
            case "BAKED_POTATO" -> "Broccomace";
            case "BREAD" -> "Runic Axe";
            case "CLOWNFISH" -> "Magmasword";
            case "COD" -> "Frostbite";
            case "COOKED_BEEF" -> "Armblade";
            case "COOKED_COD" -> "Doubleaxe";
            case "COOKED_CHICKEN" -> "Tenderizer";
            case "COOKED_MUTTON" -> "Amaranth";
            case "COOKED_PORKCHOP" -> "Gemini";
            case "COOKED_RABBIT" -> "Cudgel";
            case "COOKED_SALMON" -> "Felflame Blade";
            case "DIAMOND_AXE" -> "Diamondspark";
            case "DIAMOND_HOE" -> "Gem Axe";
            case "DIAMOND_PICKAXE" -> "Void Twig";
            case "DIAMOND_SPADE" -> "Gemcrusher";
            case "GOLDEN_CARROT" -> "Void Edge";
            case "GOLD_AXE" -> "Venomstrike";
            case "GOLD_HOE" -> "Hatchet";
            case "GOLD_PICKAXE" -> "Flameweaver";
            case "GOLD_SPADE" -> "Stone Mallet";
            case "GRILLED_PORK" -> "Gemini";
            case "IRON_AXE" -> "Demonblade";
            case "IRON_HOE" -> "Elven Greatsword";
            case "IRON_PICKAXE" -> "World Tree Branch";
            case "IRON_SPADE" -> "Hammer";
            case "MELON" -> "Divine Reach";
            case "MUSHROOM_STEW" -> "Lunar Relic";
            case "MUTTON" -> "Claws";
            case "POISONOUS_POTATO" -> "Ruby Thorn";
            case "PORK" -> "Mandibles";
            case "POTATO" -> "Halberd";
            case "PUFFERFISH" -> "Golden Gladius";
            case "PUMPKIN_PIE" -> "Orc Axe";
            case "RABBIT_STEW" -> "Bludgeon";
            case "RAW_BEEF" -> "Katar";
            case "RAW_CHICKEN" -> "Nethersteel Katana";
            case "ROTTEN_FLESH" -> "Pike";
            case "SALMON" -> "Scimitar";
            case "STONE_AXE" -> "Training Sword";
            case "STONE_HOE" -> "Runeblade";
            case "STONE_PICKAXE" -> "Walking Stick";
            case "STONE_SPADE" -> "Drakefang";
            case "STRING" -> "Hammer of Light";
            case "WOOD_AXE" -> "Steel Sword";
            case "WOOD_HOE" -> "Zweireaper";
            case "WOOD_PICKAXE" -> "Abbadon";
            case "WOOD_SPADE" -> "Nomegusta";
            default -> "Unknown Weapon";
        };
    }

    private static WeaponRarity getWeaponRarity(JsonObject weaponObject) {
        String rarityString = weaponObject.get("category").getAsString();
        return switch (rarityString) {
            case "COMMON" -> WeaponRarity.COMMON;
            case "RARE" -> WeaponRarity.RARE;
            case "EPIC" -> WeaponRarity.EPIC;
            case "LEGENDARY" -> WeaponRarity.LEGENDARY;
            default -> null;
        };
    }

    private static String getWeaponPrefix(Weapon weapon) {
        ArrayList<String> prefixes = rarityPrefixes.get(weapon.weaponRarity);
        int weaponPoints = weapon.weaponPoints;
        int maxPoints = 0;
        int minPoints = 0;
        for (Range range : rarityStatRanges.get(weapon.weaponRarity).values()) {
            maxPoints += range.max();
            minPoints += range.min();
        }
        // account for (bugged) removed stat
        switch(weapon.weaponRarity) {
            case COMMON -> {
                minPoints += 4;
                maxPoints += 4;
            }
            case RARE -> {
                minPoints += 6;
                maxPoints += 8;
            }
            case EPIC -> {
                minPoints += 7;
                maxPoints += 9;
            }
            case LEGENDARY -> {
                minPoints += 10;
                maxPoints += 15;
            }
        }
        int reducedPoints = weaponPoints - minPoints;
        int pointsRange = maxPoints - minPoints;
        int prefixIndex = (int) ((double) reducedPoints / pointsRange * prefixes.size());
        if (prefixIndex >= prefixes.size()) {
            prefixIndex = prefixes.size() - 1;
        } else if (prefixIndex < 0) {
            prefixIndex = 0;
        }
        return prefixes.get(prefixIndex);
    }

    private static int getWeaponPoints(Weapon weapon, JsonObject weaponObject) {
        int totalPoints = 0;
        int weaponUpgradeLevel = weapon.weaponUpgradeLevel;
        for (WeaponStats stat : rarityStatRanges.get(weapon.weaponRarity).keySet()) {
            int statValue;
            if (!weaponObject.has(stat.name().toLowerCase()) || weaponObject.get(stat.name().toLowerCase()).isJsonNull()) {
                statValue = 0;
            } else {
                statValue = weaponObject.get(stat.name().toLowerCase()).getAsInt();
                if(weaponUpgradeLevel > 0) {
                    statValue = upgradeStat(stat, statValue, weaponUpgradeLevel);
                }
            }
            totalPoints += statValue;
        }
        return totalPoints;
    }

    private static int upgradeStat(WeaponStats stat, int baseValue, int upgradeLevel) {
        double upgradeAmount = switch (stat) {
            case HEALTH -> 0.25;
            case ENERGY -> 0.1;
            case DAMAGE, COOLDOWN, MOVEMENT -> 0.075;
            default -> 0;
        };
        return (int) Math.ceil(baseValue * (1 + upgradeAmount * upgradeLevel));
    }

    private static double getWeaponScore(Weapon weapon, JsonObject weaponObject) {
        WeaponRarity rarity = weapon.weaponRarity;
        double totalWeaponScore = 0;
        for (WeaponStats stat : rarityStatRanges.get(rarity).keySet()) {
            double statValue;
            if (!weaponObject.has(stat.name().toLowerCase()) || weaponObject.get(stat.name().toLowerCase()).isJsonNull()) {
                statValue = 0;
            } else {
                statValue = weaponObject.get(stat.name().toLowerCase()).getAsInt();
            }
            Range statRange = rarityStatRanges.get(rarity).get(stat);
            double statScore = (statValue - statRange.min()) / (statRange.max() - statRange.min());
            totalWeaponScore += statScore;
        }
        return totalWeaponScore / rarityStatRanges.get(rarity).size();
    }

    // Getters

    public String getWeaponId() {
        return this.weaponId;
    }
    public double getWeaponScore() {
        return this.weaponScore;
    }

    public String getName() {
        return this.name;
    }

    public double getWeaponPoints() {
        return this.weaponPoints;
    }

    public int getUpgradeLevel() {
        return this.weaponUpgradeLevel;
    }

    public int getMaxUpgradeLevel() {
        return this.weaponUpgradeMaxLevel;
    }

    public String getRarity() {
        switch(this.weaponRarity.name()) {
            case "COMMON" -> {
                return "[C]";
            }
            case "RARE" -> {
                return "[R]";
            }
            case "EPIC" -> {
                return "[E]";
            }
            case "LEGENDARY" -> {
                return "[L]";
            }
            default -> {
                return "[?]";
            }
        }
    }
}
