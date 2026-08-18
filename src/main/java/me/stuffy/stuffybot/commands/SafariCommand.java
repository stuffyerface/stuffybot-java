package me.stuffy.stuffybot.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.MojangProfile;
import me.stuffy.stuffybot.profiles.SkyBlockProfile;
import me.stuffy.stuffybot.utils.APIException;
import me.stuffy.stuffybot.utils.MiscUtils;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static me.stuffy.stuffybot.utils.APIUtils.getCurrentSkyblockProfile;
import static me.stuffy.stuffybot.utils.APIUtils.getMojangProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;
import static me.stuffy.stuffybot.utils.MiscUtils.getNestedJson;
import static net.dv8tion.jda.api.components.buttons.Button.secondary;

public class SafariCommand {
    private static final Map<String, String> critterIds = new  HashMap<>();
    static {
        critterIds.put("AREITA", "Areita");
        critterIds.put("BILLYGOAT", "Billygoat");
        critterIds.put("BLOODBAT", "Bloodbat");
        critterIds.put("BLUEBIRD", "Bluebird");
        critterIds.put("CAVERNFISH", "Cavernfish");
        critterIds.put("CHUCKWALLA", "Chuckwalla");
        critterIds.put("DOOMSPIRAL", "Doomspiral");
        critterIds.put("DRIFTLING", "Driftling");
        critterIds.put("DUPLICO", "Duplico");
        critterIds.put("FLITTER", "Flitter");
        critterIds.put("FLUFFLING", "Fluffling");
        critterIds.put("FOXTROT", "Foxtrot");
        critterIds.put("GAZER", "Gazer");
        critterIds.put("GEMZIE", "Gemzie");
        critterIds.put("GIMMIEGOLD", "Gimmiegold");
        critterIds.put("HIDEONFLOOR", "Hideonfloor");
        critterIds.put("HIDEONWALL", "Hideonwall");
        critterIds.put("HIDEYHO", "Hideyho");
        critterIds.put("HONEYBUG", "Honeybug");
        critterIds.put("LITTERBUG", "Litterbug");
        critterIds.put("MACAW", "Macaw");
        critterIds.put("MANTIS_SHRIMP", "Mantis Shrimp");
        critterIds.put("NOZZLENOSE", "Nozzlenose");
        critterIds.put("PARAKEET", "Parakeet");
        critterIds.put("POLARIS", "Polaris");
        critterIds.put("ROCKMITE", "Rockmite");
        critterIds.put("SCRAPPY", "Scrappy");
        critterIds.put("SHUDDERSQUID", "Shuddersquid");
        critterIds.put("SHYWORM", "Shyworm");
        critterIds.put("SNOOZLE", "Snoozle");
        critterIds.put("SOLSNATCHER", "Solsnatcher");
        critterIds.put("STRONGARM", "Strongarm");
        critterIds.put("TEPID", "Tepid");
        critterIds.put("TREEFROG", "Treefrog");
        critterIds.put("TROODON", "Troodon");
        critterIds.put("WOODCHUCKER", "Woodchucker");
        critterIds.put("WUMPA", "Wumpa");
    }

    public static MessageCreateData getSparklingCritters(InteractionId interactionId) throws APIException {
        String ign = interactionId.getOption("ign");
        MojangProfile mojangProfile = getMojangProfile(ign);
        SkyBlockProfile profile = getCurrentSkyblockProfile(ign);
        JsonObject skyBlockData = profile.getData();
        JsonArray sparklingCritters = getNestedJson(skyBlockData, "members", mojangProfile.getUuidStripped(), "safari", "discovered_sparkling_critters").getAsJsonArray();
        int total = sparklingCritters.size();
        int dupes = getNestedJson(0, skyBlockData, "members", mojangProfile.getUuidStripped(), "safari", "total_captured_sparkling_critters").getAsInt() - total;

        ArrayList<String> foundSparklingCritters = new ArrayList<>();
        for (JsonElement critter : sparklingCritters) {
            String critterId = critter.getAsString();
            String critterName = critterIds.get(critterId);
            foundSparklingCritters.add(critterName);
        }
        foundSparklingCritters.sort(String::compareToIgnoreCase);
        String embedContent = String.join(", ",foundSparklingCritters) + "\n" +
                "\n-# `+" + dupes + "` duplicates.";

        DecimalFormat df = new DecimalFormat("#.##");
        String percentUnlocked = df.format((double) total / (double) 37 * 100);

        MessageEmbed safariTickets = makeStatsEmbed(
                "Sparkling Critters for " + mojangProfile.getUsername() + " (" + total + "/37) " + percentUnlocked + "%",
                embedContent
        );

        String newInteractionId = InteractionId.newCommand("safari_sparkling_missing", interactionId).getInteractionString();
        return new MessageCreateBuilder()
                .addEmbeds(safariTickets)
                .setComponents(ActionRow.of(
                        secondary(newInteractionId, "Show Missing Sparkling Critters")
                ))
                .build();
    }

    public static MessageCreateData getMissingSparklingCritters(InteractionId interactionId) throws APIException {
        String ign = interactionId.getOption("ign");
        MojangProfile mojangProfile = getMojangProfile(ign);
        SkyBlockProfile profile = getCurrentSkyblockProfile(ign);
        JsonObject skyBlockData = profile.getData();
        JsonArray sparklingCritters = getNestedJson(skyBlockData, "members", mojangProfile.getUuidStripped(), "safari", "discovered_sparkling_critters").getAsJsonArray();
        int total = sparklingCritters.size();

        ArrayList<String> missingSparklingCritters = new ArrayList<>(critterIds.keySet());
        for (JsonElement critter : sparklingCritters) {
            String critterId = critter.getAsString();
            missingSparklingCritters.remove(critterId);
        }
        missingSparklingCritters.forEach(critterId -> {
            String critterName = critterIds.get(critterId);
            missingSparklingCritters.set(missingSparklingCritters.indexOf(critterId), critterName);
        });
        missingSparklingCritters.sort(String::compareToIgnoreCase);
        String embedContent = String.join(", ",missingSparklingCritters);

        DecimalFormat df = new DecimalFormat("#.##");
        String percentMissing = df.format((double) (37-total) / (double) 37 * 100);

        MessageEmbed safariTickets = makeStatsEmbed(
                "Missing Sparkling Critters for " + mojangProfile.getUsername() + " (" + (37-total) + "/37) " + percentMissing + "%",
                embedContent
        );

        String newInteractionId = InteractionId.newCommand("safari_sparkling", interactionId).getInteractionString();
        return new MessageCreateBuilder()
                .addEmbeds(safariTickets)
                .setComponents(ActionRow.of(
                        secondary(newInteractionId, "Show Sparkling Critters")
                ))
                .build();
    }

    public static MessageCreateData getMutualSparklingCritters(InteractionId interactionId) throws APIException {
        ArrayList<String> inputPlayers = new ArrayList<>();
        String player1 = getMojangProfile(interactionId.getOption("player1")).getUsername();
        inputPlayers.add(player1);
        inputPlayers.add(getMojangProfile(interactionId.getOption("player2")).getUsername());
        if(interactionId.getOption("player3") != null){ inputPlayers.add(getMojangProfile(interactionId.getOption("player3")).getUsername()); }
        if(interactionId.getOption("player4") != null){ inputPlayers.add(getMojangProfile(interactionId.getOption("player4")).getUsername()); }

        MojangProfile mojangProfile = getMojangProfile(player1);
        SkyBlockProfile profile = getCurrentSkyblockProfile(player1);
        JsonObject skyBlockData = profile.getData();
        JsonArray sparklingCritters = getNestedJson(skyBlockData, "members", mojangProfile.getUuidStripped(), "safari", "discovered_sparkling_critters").getAsJsonArray();

        ArrayList<String> groupSparklingCritters = new ArrayList<>();
        for (JsonElement critter : sparklingCritters) {
            String critterId = critter.getAsString();
            groupSparklingCritters.add(critterId);
        }

        for (String player : inputPlayers.subList(1, inputPlayers.size())) {
            MojangProfile mojangProfileX = getMojangProfile(player);
            SkyBlockProfile skyBlockProfileX = getCurrentSkyblockProfile(player);
            JsonObject skyBlockDataX = skyBlockProfileX.getData();
            JsonArray sparklingCrittersX = getNestedJson(skyBlockDataX, "members", mojangProfileX.getUuidStripped(), "safari", "discovered_sparkling_critters").getAsJsonArray();
            ArrayList<String> soloSparklingCritters = new ArrayList<>();
            for (JsonElement critterX : sparklingCrittersX) {
                String critterId = critterX.getAsString();
                soloSparklingCritters.add(critterId);
            }
            groupSparklingCritters.removeIf(groupCritter -> !soloSparklingCritters.contains(groupCritter));
        }
        groupSparklingCritters.forEach(critterId -> {
            String critterName = critterIds.get(critterId);
            groupSparklingCritters.set(groupSparklingCritters.indexOf(critterId), critterName);
        });
        groupSparklingCritters.sort(String::compareToIgnoreCase);


        String embedContent =
                "Group: `" + String.join("`, `", inputPlayers) + "`\n" +
                        String.join(", ",  groupSparklingCritters);

        MessageEmbed sharedSparkling = makeStatsEmbed(
                "Shared sparkling critters (" + groupSparklingCritters.size() + ")",
                embedContent
        );

        return new MessageCreateBuilder()
                .addEmbeds(sharedSparkling)
                .build();
    }

    public static MessageCreateData getSafariTickets(InteractionId interactionId) throws APIException {
        String ign = interactionId.getOption("ign");
        MojangProfile mojangProfile = getMojangProfile(ign);
        SkyBlockProfile profile = getCurrentSkyblockProfile(ign);
        JsonObject skyBlockData = profile.getData();
        JsonObject ticketsData = getNestedJson(skyBlockData, "members", mojangProfile.getUuidStripped(), "safari", "tickets").getAsJsonObject();
        int basic = getNestedJson(0, ticketsData, "basic").getAsInt();
        int economy = getNestedJson(0, ticketsData, "economy").getAsInt();
        int premium = getNestedJson(0, ticketsData, "premium").getAsInt();
        int first_class = getNestedJson(0, ticketsData, "first_class").getAsInt();
        int total = basic + economy + premium + first_class;

        String embedContent =
                "Basic: `" + basic + "`\n" +
                        "Economy: `" + economy + "`\n" +
                        "Premium: `" + premium + "`\n" +
                        "First-Class: `" + first_class + "`\n";

        MessageEmbed safariTickets = makeStatsEmbed(
                "Safari Tickets for " + mojangProfile.getUsername() + " (" + total + ")",
                embedContent
        );

        return new MessageCreateBuilder()
                .addEmbeds(safariTickets)
                .build();
    }
}
