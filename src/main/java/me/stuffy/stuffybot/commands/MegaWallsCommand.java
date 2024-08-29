package me.stuffy.stuffybot.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.utils.APIException;
import me.stuffy.stuffybot.utils.InvalidOptionException;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Map;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;

public class MegaWallsCommand {

    private static JsonObject getMwClasses() {
        try (InputStream inputStream = MegaWallsCommand.class.getResourceAsStream("/data/mwclasses.json")) {
            assert inputStream != null;
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MessageCreateData megawalls(InteractionId interactionId) throws APIException, InvalidOptionException {
        String ign = interactionId.getOption("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();

        String mwClass = interactionId.getOption("skins", "all");
        Map<String, Boolean> legendary_skins = hypixelProfile.getMegaWallsLegendaries();
        int legendary_skins_unlocked = 0;
        DecimalFormat dfPercent = new DecimalFormat("##.##");
        DecimalFormat dfComma = new DecimalFormat("#,###");

        MessageEmbed response = null;
        switch (mwClass.toLowerCase()) {
            case "legendary" -> {
                StringBuilder legendarySkins = new StringBuilder();
                for (Map.Entry<String, Boolean> entry : legendary_skins.entrySet()) {
                    if (entry.getValue()) {
                        legendarySkins.append("✅ ");
                        legendary_skins_unlocked++;
                    } else {
                        legendarySkins.append("❌ ");
                    }
                    legendarySkins.append(entry.getKey()).append("\n");
                }
                response = makeStatsEmbed(
                        "Legendary Skins for " + username,
                        "Unlocked: **" + legendary_skins_unlocked + "**/27 (" + dfPercent.format(legendary_skins_unlocked / 27.0 * 100) + "%)",
                        legendarySkins.toString()
                );
            }
            case "all" -> {
                Integer wins = hypixelProfile.getMegaWallsWins();
                Integer finalKills = hypixelProfile.getMegaWallsFinalKills();
                Integer classPoints = hypixelProfile.getMegaWallsClassPoints();
                String selectedClass = hypixelProfile.getMegaWallsSelectedClass();
                for (Boolean unlocked : legendary_skins.values()) {
                    if (unlocked) {
                        legendary_skins_unlocked++;
                    }
                }
                response = makeStatsEmbed(
                        "Mega Walls Stats for " + username,
                        "Wins: **" + dfComma.format(wins) + "**\n" +
                                "Final Kills: **" + dfComma.format(finalKills) + "**\n" +
                                "Total Class Points: **" + dfComma.format(classPoints) + "**\n\n" +
                                "Selected Class: **" + selectedClass + "**\n" +
                                "Legendary Skins Unlocked: **" + legendary_skins_unlocked + "**/27"
                );
            }
            default -> {
                JsonObject mwClasses = getMwClasses();
                if (mwClasses == null) {
                    throw new APIException("Stuffy", "Failed to load Mega Walls classes data");
                }
                for (JsonElement element : mwClasses.getAsJsonArray("classes")) {
                    JsonObject mwClassObject = element.getAsJsonObject();
                    String id = mwClassObject.get("id").getAsString();
                    if (id.equals(mwClass)) {
                        String className = mwClassObject.get("name").getAsString();
                        JsonArray skins = mwClassObject.getAsJsonArray("skins");
                        int skinCount = skins.size();

                        String[] breakdown = new String[skinCount];
                        DecimalFormat df = new DecimalFormat("#,###");
                        for (int i = 0; i < skinCount; i++) {
                            StringBuilder skinString = new StringBuilder();
                            JsonObject skin = skins.get(i).getAsJsonObject();
                            String skinName = skin.get("name").getAsString();
                            Integer requiredStats = skin.get("max").getAsInt();
                            Integer playerProgress = 0;
                            for (JsonElement value : skin.get("values").getAsJsonArray()) {
                                playerProgress += hypixelProfile.getMegaWallsStat(value.getAsString());
                            }

                            if (playerProgress >= requiredStats) {
                                skinString.append("~~").append(skinName).append(" **").append(df.format(playerProgress)).append("**/").append(df.format(requiredStats)).append("~~");
                            } else {
                                skinString.append(skinName).append(" **").append(df.format(playerProgress)).append("**/").append(df.format(requiredStats));
                            }
                            breakdown[i] = skinString.toString();
                        }

                        response = makeStatsEmbed(
                                className + " Skins for " + username,
                                skinCount + " tracked skins",
                                String.join("\n", breakdown)
                        );
                        break;
                    }
                }
                if (response == null) {
                    throw new InvalidOptionException("skins", mwClass);
                }
            }
        }
        return new MessageCreateBuilder()
                .addEmbeds(response)
                .build();
    }
}
