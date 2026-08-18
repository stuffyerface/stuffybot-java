package me.stuffy.stuffybot.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.profiles.games.warlords.Weapon;
import me.stuffy.stuffybot.utils.APIException;
import me.stuffy.stuffybot.utils.DiscordUtils;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.min;
import static me.stuffy.stuffybot.profiles.games.warlords.WarlordsUtils.*;
import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.discordTimeUnix;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;
import static net.dv8tion.jda.api.components.buttons.Button.secondary;

public class WarlordsCommand {

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public static MessageCreateData warlords(InteractionId interactionId) throws APIException {
        String ign = interactionId.getOptions().get("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();

        String embedContent =
                "Selected:\n" + getSpecName(getSelectedSpec(hypixelProfile)) + "\n" +
                getSelectedWeapon(hypixelProfile) + "\n\n" +
                "**Wins: " + hypixelProfile.getStatFormatted("Battleground.wins") + "**\n" +
                "CTF: " + hypixelProfile.getStatFormatted("Battleground.wins_capturetheflag") + "\n" +
                "TDM: " + hypixelProfile.getStatFormatted("Battleground.wins_teamdeathmatch") + "\n" +
                "Dom: " + hypixelProfile.getStatFormatted("Battleground.wins_domination") + "\n\n" +
                "Kills: " + hypixelProfile.getStatFormatted("Battleground.kills") + "\n" +
                "Assists: " + hypixelProfile.getStatFormatted("Battleground.assists");


        MessageEmbed warlordsStats = makeStatsEmbed(
                username + "'s Warlords Stats",
                embedContent
        );

        String classesInteractionId = InteractionId.newCommand("warlordsClasses", interactionId).getInteractionString();
        String weaponsInteractionId = InteractionId.newCommand("warlordsWeapons", interactionId).getInteractionString();

        return new MessageCreateBuilder()
                .addEmbeds(warlordsStats)
                .setComponents(ActionRow.of(
                        secondary(classesInteractionId, "Classes"),
                        secondary(weaponsInteractionId, "Weapons Inventory")
                ))
                .build();
    }

    public static MessageCreateData warlordsClasses(InteractionId interactionId) throws APIException{
        String ign = interactionId.getOptions().get("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();
        int selectedClass = interactionId.getOption("mwClass", 0);
        String classNameRaw = getClassRaw(selectedClass);
        String selectedClassName = getClassName(classNameRaw);
        StringBuilder stringBuilder = new StringBuilder();
        String classHeader = selectedClassName + " Lvl " + getClassLevel(hypixelProfile, getClassRaw(selectedClass));
        stringBuilder.append(classHeader).append("\n");
        for (int i = 0; i <= 2; i++) {
            String specName = getSpecName(classNameRaw, i);
            String isPrestiged = isPrestiged(hypixelProfile, specName);
            String specBoost = getSpecBoost(hypixelProfile, specName.toLowerCase());
            stringBuilder.append("__").append(specName).append("__").append(isPrestiged).append(specBoost).append("\n")
                    .append(hypixelProfile.getStat("Battleground.wins_" + specName.toLowerCase())).append(" wins").append("\n");

            String boundWeaponId = getBoundWeaponId(hypixelProfile, classNameRaw, specName.toLowerCase());
            if (boundWeaponId != null) {
                Weapon weapon = getWeaponById(hypixelProfile, boundWeaponId);
                if (weapon != null) {
                    String weaponName = weapon.getName();
                    if (!weaponName.isEmpty()) {
                        stringBuilder.append(weaponName);
                    } else {
                        stringBuilder.append("Unknown Weapon");
                    }
                }
            } else {
                stringBuilder.append("No Weapon Bound");
            }
            stringBuilder.append("\n\n");
        }


        MessageEmbed warlordsStats = makeStatsEmbed(
                username + "'s Warlords Classes (" + (selectedClass + 1) + "/4)",
                stringBuilder.toString()
        );

        String goBackInteractionId = InteractionId.newCommand("warlords", interactionId).getInteractionString();


        Button prev = Button.of(ButtonStyle.SECONDARY, interactionId.setOption("mwClass", selectedClass-1).getInteractionString(), "◀");
        Button next = Button.of(ButtonStyle.SECONDARY, interactionId.setOption("mwClass", selectedClass+1).getInteractionString(), "▶");
        if (selectedClass <= 0) {
            prev = prev.asDisabled();
        }
        if (selectedClass >= 3) {
            next = next.asDisabled();
        }
        return new MessageCreateBuilder()
                .addEmbeds(warlordsStats)
                .setComponents(ActionRow.of(
                        secondary(goBackInteractionId, "General Stats"),
                        prev,
                        next
                ))
                .build();
    }

    public static MessageCreateData warlordsWeapons(InteractionId interactionId) throws APIException{
        String ign = interactionId.getOptions().get("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();

        int page = interactionId.getOption("page", 0);
        Weapon[] weaponsInventory = getAllWeapons(hypixelProfile);
        int weaponsPerPage = 3;
        int startIndex = page * weaponsPerPage;
        int endIndex = min(startIndex + weaponsPerPage, weaponsInventory.length);
        int totalPages = (int) Math.ceil((double) weaponsInventory.length / weaponsPerPage);
        StringBuilder stringBuilder = new StringBuilder();

        Map<String, String> boundWeapons = new HashMap<>();
        for (JsonObject classObject : hypixelProfile.getStatObject("Battleground.bound_weapon")
                .entrySet().stream().map(Map.Entry::getValue).map(JsonElement::getAsJsonObject).toList()) {
            for (Map.Entry<String, JsonElement> specEntry : classObject.entrySet()) {
                String specName = specEntry.getKey();
                String weaponId = specEntry.getValue().getAsString();
                boundWeapons.put(weaponId, specName);
            }
        }

        for (int i = startIndex; i < endIndex; i++) {
            stringBuilder.append(formatWeapon(weaponsInventory[i], boundWeapons));
            if (i < endIndex - 1) {
                stringBuilder.append("\n");
            }
        }
        MessageEmbed warlordsStats = makeStatsEmbed(
                username + "'s Warlords Weapons (" + (page+1) + "/" + totalPages + ")",
                stringBuilder.toString()
        );

        String goBackInteractionId = InteractionId.newCommand("warlords", interactionId).getInteractionString();
        String prevPageId = interactionId.setOption("page", page - 1).getInteractionString();
        String nextPageId = interactionId.setOption("page", page + 1).getInteractionString();
        Button prev = Button.of(ButtonStyle.SECONDARY, prevPageId, "◀");
        Button next = Button.of(ButtonStyle.SECONDARY, nextPageId, "▶");
        if (page <= 0) {
            prev = prev.asDisabled();
        }
        if (endIndex >= weaponsInventory.length) {
            next = next.asDisabled();
        }
        return new MessageCreateBuilder()
                .addEmbeds(warlordsStats)
                .setComponents(ActionRow.of(
                        secondary(goBackInteractionId, "General Stats"),
                        prev,
                        next,
                        secondary(InteractionId.newCommand("warlordsWeaponSummary", interactionId).setOption("page", 0).getInteractionString(), "Summary"
                )))
                .build();
    }

    public static MessageCreateData warlordsWeaponSummary(InteractionId interactionId) throws APIException {
        String ign = interactionId.getOptions().get("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();

        int page = interactionId.getOption("page", 0);

        Map<String, String> boundWeapons = new HashMap<>();
        for (JsonObject classObject : hypixelProfile.getStatObject("Battleground.bound_weapon")
                .entrySet().stream().map(Map.Entry::getValue).map(JsonElement::getAsJsonObject).toList()) {
            for (Map.Entry<String, JsonElement> specEntry : classObject.entrySet()) {
                String specName = specEntry.getKey();
                String weaponId = specEntry.getValue().getAsString();
                boundWeapons.put(weaponId, specName);
            }
        }

        Weapon[] weapons = getAllWeapons(hypixelProfile);

        int weaponsPerPage = 10;
        int startIndex = page * weaponsPerPage;
        int endIndex = min(startIndex + weaponsPerPage, weapons.length);
        int count = endIndex - startIndex;
        String[] weaponsList = new String[count];
        for(int i = 0; i < count; i++) {
            Weapon weapon = weapons[i+startIndex];
            String weaponSummary = "";
            weaponSummary += i+startIndex+1 + ". ";
            weaponSummary += weapon.getRarity() + " ";
            weaponSummary += "`" + decimalFormat.format(100*weapon.getWeaponScore()) + "%` ";
            weaponSummary += weapon.getName();
            if(weapon.getUpgradeLevel() >= 1) {
                weaponSummary += " [" + weapon.getUpgradeLevel() + "/" + weapon.getMaxUpgradeLevel() + "]";
            }
            if(boundWeapons.containsKey(weapon.getWeaponId())) {
                weaponSummary += " **B**";
            }
            weaponsList[i] = weaponSummary;
        }
        String embedContent = String.join("\n", weaponsList);
        MessageEmbed warlordsStats = makeStatsEmbed(
                username + "'s Warlords Weapon Summary",
                embedContent
        );

        String goBackInteractionId = InteractionId.newCommand("warlordsWeapons", interactionId).setOption("page", 0).getInteractionString();
        String prevPageId = interactionId.setOption("page", page - 1).getInteractionString();
        String nextPageId = interactionId.setOption("page", page + 1).getInteractionString();
        Button prev = Button.of(ButtonStyle.SECONDARY, prevPageId, "◀");
        Button next = Button.of(ButtonStyle.SECONDARY, nextPageId, "▶");
        if (page <= 0) {
            prev = prev.asDisabled();
        }
        if (endIndex >= weapons.length) {
            next = next.asDisabled();
        }

        return new MessageCreateBuilder()
                .addEmbeds(warlordsStats)
                .setComponents(ActionRow.of(
                        secondary(goBackInteractionId, "Back to Weapons"),
                        prev,
                        next
                ))
                .build();
    }

    private static Weapon[] getAllWeapons(HypixelProfile hypixelProfile) {
        JsonArray weaponsObject = hypixelProfile.getStatArray("Battleground.weapon_inventory");
        Weapon[] weaponsInventory = new Weapon[weaponsObject.size()];
        for (int i = 0; i < weaponsObject.size(); i++) {
            JsonObject weaponObject = weaponsObject.get(i).getAsJsonObject();
            Weapon weapon = new Weapon(weaponObject);
            weaponsInventory[i] = weapon;
        }
        return Arrays.stream(weaponsInventory)
                .sorted(Comparator.comparingDouble(Weapon::getWeaponPoints).reversed())
                .toArray(Weapon[]::new);
    }

    private static String formatWeapon(Weapon weapon, Map<String, String> boundWeapons) {
        String weaponName = weapon.getName();
        String weaponId = weapon.getWeaponId();
        double weaponScore = weapon.getWeaponScore();
        int upgradeLevel = weapon.getUpgradeLevel();
        int maxUpgradeLevel = weapon.getMaxUpgradeLevel();
        StringBuilder weaponInfo = new StringBuilder();
        weaponInfo.append("__").append(weaponName).append("__");
        if(upgradeLevel >= 1) {
            weaponInfo.append(" [").append(upgradeLevel).append("/").append(maxUpgradeLevel).append("]");
        }

        weaponInfo.append("\n").append("Obtained ").append(discordTimeUnix(Long.parseLong(weaponId))).append("\n")
                .append("Weapon Score: `").append(decimalFormat.format(100*weaponScore)).append("%`\n");

        if(boundWeapons.containsKey(weaponId)) {
            weaponInfo.append("**BOUND**: ").append(getSpecName(boundWeapons.get(weaponId))).append("\n");
        }
        return weaponInfo.toString();
    }

    private static String getTimeStamp(JsonObject weaponObject) {
        long timestamp = weaponObject.get("id").getAsLong();
        return DiscordUtils.discordTimeUnix(timestamp);
    }

    private static Weapon getWeaponById(HypixelProfile hypixelProfile, String weaponId) {
        Weapon[] allWeapons = getAllWeapons(hypixelProfile);
        for (Weapon weapon : allWeapons) {
            String currentWeaponId = weapon.getWeaponId();
            if (currentWeaponId.equals(weaponId)) {
                return weapon;
            }
        }
        return null;
    }

    private static String getSelectedSpec(HypixelProfile hypixelProfile) {
        String chosenClass = hypixelProfile.getStatString("Battleground.chosen_class");
        String selectedSpec = hypixelProfile.getStatString("Battleground." + chosenClass + "_spec");
        if (selectedSpec.isEmpty()) {
            selectedSpec = "Unknown";
        }
        return selectedSpec;
    }

    private static int getClassLevel(HypixelProfile hypixelProfile, String classNameRaw) {
        return hypixelProfile.getStat("Battleground." + classNameRaw + "_skill1")
                + hypixelProfile.getStat("Battleground." + classNameRaw + "_skill2")
                + hypixelProfile.getStat("Battleground." + classNameRaw + "_skill3")
                + hypixelProfile.getStat("Battleground." + classNameRaw + "_skill4")
                + hypixelProfile.getStat("Battleground." + classNameRaw + "_skill5")
                + hypixelProfile.getStat("Battleground." + classNameRaw + "_health")
                + hypixelProfile.getStat("Battleground." + classNameRaw + "_energy")
                + hypixelProfile.getStat("Battleground." + classNameRaw + "_cooldown")
                + hypixelProfile.getStat("Battleground." + classNameRaw + "_critchance")
                + hypixelProfile.getStat("Battleground." + classNameRaw + "_critmultiplier");
    }

    private static String getSelectedWeapon(HypixelProfile hypixelProfile) {
        Weapon[] allWeapons = getAllWeapons(hypixelProfile);
        String selectedWeaponId = hypixelProfile.getStatString("Battleground.current_weapon");
        for (Weapon weapon : allWeapons) {
            String weaponId = weapon.getWeaponId();
            if (weaponId.equals(selectedWeaponId)) {
                return weapon.getName();
            }
        }
        return "Unknown Weapon";
    }

    private static String getBoundWeaponId(HypixelProfile hypixelProfile, String className, String specName) {
        String boundWeaponId = hypixelProfile.getStatString("Battleground.bound_weapon." + className + "." + specName);
        if (boundWeaponId.isEmpty()) {
            return null;
        }
        return boundWeaponId;
    }

    private static String isPrestiged(HypixelProfile hypixelProfile, String specName) {
        JsonArray prestigedClasses = hypixelProfile.getStatArray("Battleground.prestiged");
        if (prestigedClasses != null) {
            for (int i = 0; i < prestigedClasses.size(); i++) {
                if (prestigedClasses.get(i).getAsString().equalsIgnoreCase(specName)) {
                    return " ✪";
                }
            }
        }
        return "";
    }

    private static String getSpecBoost(HypixelProfile hypixelProfile, String specName) {
        JsonObject specBoosts = hypixelProfile.getStatObject("Battleground.active_boost");
        if (specBoosts != null && specBoosts.has(specName)) {
            String specBoost = specBoosts.get(specName).getAsString();
            String specBoostName = boostRawToName(specBoost);
            if(specBoostName != null) {
                String specBoostLevel = getSpecBoostLevel(hypixelProfile, specBoost);
                return " [" + specBoostName + " " + specBoostLevel + "]";
            }
        }
        return "";
    }

    private static String boostRawToName(String boostRaw) {
        return switch (boostRaw) {
            // Pyro
            case "meteor" -> "Meteor";
            case "arcane_shatter" -> "Arcane Shatter";
            case "dimensional_warp" -> "Dimensional Warp";
            case "burst_chain" -> "Burst Chain";
            case "flame_breath" -> "Flame Breath";
            // Cryo
            case "frost_missile" -> "Frost Missile";
            case "arcane_recluse" -> "Arcane Recluse";
            case "chilly_aura" -> "Chilly Aura";
            case "blizzard_breath" -> "Blizzard Breath";
            case "steadfast_warp" -> "Steadfast Warp";
            // Aqua
            case "typhoon_bolt" -> "Typhoon Bolt";
            case "arcane_reflection" -> "Arcane Reflection";
            case "acid_rain" -> "Acid Rain";
            case "clairvoyance" -> "Clairvoyance";
            case "divine_purification" -> "Divine Purification";
            //Bers
            case "wounding_strike_berserker" -> "Wounding Strike";
            case "blood_frenzy" -> "Blood Frenzy";
            case "mighty_fists" -> "Mighty Fists";
            case "berserkers_fury" -> "Berserker's Fury";
            case "seismic_shift" -> "Seismic Shift";
            // Def
            case "wounding_strike_defender" -> "Wounding Strike";
            case "heroic_intervention" -> "Heroic Intervention";
            case "solitary_resistance" -> "Solitary Resistance";
            case "fervent_force" -> "Fervent Force";
            case "vitality_boost" -> "Vitality Boost";
            // Rev
            case "orbs_of_life" -> "Orbs of Life";
            case "one_man_army" -> "One Man Army";
            case "reckless_ascent" -> "Reckless Ascent";
            case "undying_steed" -> "Undying Steed";
            case "healing_link" -> "Healing Link";
            // Ave
            case "divine_vindication" -> "Divine Vindication";
            case "warding_wrath" -> "Warding Wrath";
            case "greater_sacrality" -> "Greater Sacrality";
            case "arm_of_the_almighty" -> "Arm of the Almighty";
            case "zealous_mark" -> "Zealous Mark";
            // Crus
            case "blade_of_willpower" -> "Blade of Willpower";
            case "seraphim_shield" -> "Seraphim Shield";
            case "rallying_presence" -> "Rallying Presence";
            case "sovereign_solitude" -> "Sovereign Solitude";
            case "vigorous_infusion" -> "Vigorous Infusion";
            // Prot
            case "piercing_radiance" -> "Piercing Radiance";
            case "lustrous_crown" -> "Lustrous Crown";
            case "divine_effulgence" -> "Divine Effulgence";
            case "lightspeed_infusion" -> "Lightspeed Infusion";
            case "hammer_of_judgement" -> "Hammer of Judgement";
            // Tlord
            case "transistor" -> "Transistor";
            case "galvanized_spark" -> "Galvanized Spark";
            case "electromagnetic_chains" -> "Electromagnetic Chains";
            case "eye_of_the_storm" -> "Eye of the Storm";
            case "symphonic_windfury" -> "Symphonic Windfury";
            // Spirit
            case "wrath_of_the_fallen" -> "Wrath of the Fallen";
            case "devils_debt" -> "Devil's Debt";
            case "spiritual_deflection" -> "Spiritual Deflection";
            case "permeating_link" -> "Permeating Link";
            case "smotherin_soulbind" -> "Smothering Soulbind";
            // Earth
            case "megalithic_boulder" -> "Megalithic Boulder";
            case "augmented_chains" -> "Augmented Chains";
            case "earthbound_infusion" -> "Earthbound Infusion";
            case "totemic_boon" -> "Totemic Boon";
            case "accelerated_spike" -> "Accelerated Spike";
            default -> null;
        };
    }

    private static String getSpecBoostLevel(HypixelProfile hypixelProfile, String specBoostName) {
        int boostLevel = hypixelProfile.getStat("Battleground." + specBoostName);
        switch (boostLevel) {
            case 1 -> {
                return "II";
            }
            case 2 -> {
                return "III";
            }
            case 3 -> {
                return "IV";
            }
            case 4 -> {
                return "V";
            }
            default -> {
                return "I";
            }
        }
    }
}
