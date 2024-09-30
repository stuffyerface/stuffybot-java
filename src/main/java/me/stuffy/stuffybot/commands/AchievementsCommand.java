package me.stuffy.stuffybot.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.utils.APIException;
import me.stuffy.stuffybot.utils.InvalidOptionException;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.text.DecimalFormat;
import java.util.*;

import static me.stuffy.stuffybot.utils.APIUtils.getAchievementsResources;
import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;
import static me.stuffy.stuffybot.utils.MiscUtils.*;

public class AchievementsCommand {
    private static final Map<String, JsonObject> gameDataCache = new HashMap<>();

    public static MessageCreateData achievements(InteractionId interactionId) throws APIException, InvalidOptionException {
        String ign = interactionId.getOption("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();

        String readableName = interactionId.getOption("game", "none");
        String viewType = interactionId.getOption("type", "all");

        JsonObject gameAchievements = null;

        if (readableName.equals("none")) {
            StringBuilder content = new StringBuilder();
            int unlockedAchievements = hypixelProfile.getAchievementsUnlocked();
            int maxAchievements = getMaxAchievements();
            int achievementPoints = hypixelProfile.getAchievementPoints();
            int maxAchievementPoints = getMaxAchievementPoints();

            int legacyUnlocked = hypixelProfile.getLegacyAchievementsUnlocked();
            int legacyPoints = hypixelProfile.getLegacyAchievementPoints();

            DecimalFormat thousands = new DecimalFormat("#,###");
            DecimalFormat percentage = new DecimalFormat("#.##");

            content.append("Unlocked: **").append(thousands.format(unlockedAchievements)).append("**/").append(thousands.format(maxAchievements)).append(" (").append(percentage.format((double) unlockedAchievements / maxAchievements * 100)).append("%)\n");
            content.append("Points: **").append(thousands.format(achievementPoints)).append("**/").append(thousands.format(maxAchievementPoints)).append(" (").append(percentage.format((double) achievementPoints / maxAchievementPoints * 100)).append("%)\n\n");
            content.append("Legacy Unlocked: **").append(thousands.format(legacyUnlocked)).append("**\n");
            content.append("Legacy Points: **").append(thousands.format(legacyPoints)).append("**");

//            content.append("\n\nEasiest challenge: **").append(hypixelProfile.getEasiestChallenge()).append("**\n");
//            content.append("Closest tiered: **").append(hypixelProfile.getEasiestTiered()).append("**\n");

            return new MessageCreateBuilder()
                    .addEmbeds(makeStatsEmbed("Achievement Data for " + username, content.toString()))
                    .build();
        }

        JsonObject achievementData = getAchievementsResources().getAsJsonObject();
        String gameId = fromReadableName(readableName);

        String _id = interactionId.getId();
        if (gameDataCache.containsKey(_id)) {
            gameAchievements = gameDataCache.get(_id);
        } else {
            for (String game : achievementData.keySet()) {
                if (Objects.equals(game, gameId)) {
                    gameAchievements = achievementData.getAsJsonObject(game);
                    gameDataCache.put(_id, gameAchievements);
                }
            }
        }

        if (gameAchievements == null) {
            throw new InvalidOptionException("game", readableName);
        }

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();


        Button allButton = Button.of(ButtonStyle.PRIMARY, interactionId.setOption("type", "all").getInteractionString(), "All");
        Button challengeButton = Button.of(ButtonStyle.PRIMARY, interactionId.setOption("type", "challenge").getInteractionString(), "Challenge");
        Button tieredButton = Button.of(ButtonStyle.PRIMARY, interactionId.setOption("type", "tiered").getInteractionString(), "Tiered");

        String embedTitle = "";
        String embedContent = "";

        List<String> challengeUnlocked = new ArrayList<>();
        List<String> challengeLocked = new ArrayList<>();
        int challengeMaxUnlocked = 0;
        int challengeMaxPoints = 0;
        int challengeUnlockedPoints = 0;
        JsonArray challengeAchievements = hypixelProfile.getAchievements().get("achievementsOneTime").getAsJsonArray();
        List<String> playerOneTimeString = new ArrayList<>();
        for (JsonElement element : challengeAchievements) {
            try {
                playerOneTimeString.add(element.getAsString());
            } catch (Exception ignored) {
            }
        }
        for (String achievement : gameAchievements.get("one_time").getAsJsonObject().keySet()) {
            String inData = gameId + "_" + achievement.toLowerCase();
            JsonObject achievementObject = gameAchievements.get("one_time").getAsJsonObject().get(achievement).getAsJsonObject();
            String readableAchievement = achievementObject.get("name").getAsString() + achievementObject.get("points").getAsString();
            int points = achievementObject.get("points").getAsInt();
            if(achievementObject.has("legacy")) {
                if (achievementObject.get("legacy").getAsBoolean()) {
                    continue;
                }
            }
            challengeMaxUnlocked++;
            challengeMaxPoints += points;


            if (playerOneTimeString.contains(inData)) {
                challengeUnlockedPoints += points;
                challengeUnlocked.add(readableAchievement);
            } else {
                challengeLocked.add(readableAchievement);
            }
        }

        int tieredMaxUnlocked = 0;
        int tieredMaxPoints = 0;
        int tieredUnlockedPoints = 0;
        int tieredTotalUnlocked = 0;


        JsonObject tieredAchievements = hypixelProfile.getAchievements().get("achievementsTiered").getAsJsonObject();
        for (String achievement : gameAchievements.get("tiered").getAsJsonObject().keySet()) {
            JsonObject achievementObject = gameAchievements.get("tiered").getAsJsonObject().get(achievement).getAsJsonObject();
            if(achievementObject.has("legacy")) {
                if (achievementObject.get("legacy").getAsBoolean()) {
                    continue;
                }
            }
            String readableAchievement = achievementObject.get("name").getAsString();
            JsonArray tieredTiers = achievementObject.get("tiers").getAsJsonArray();
            String inData = gameId + "_" + achievement.toLowerCase();

            int tierCount = 0;
            int tieredUnlocked = 0;
            for (JsonElement tier : tieredTiers) {
                JsonObject tierObject = tier.getAsJsonObject();
                int points = tierObject.get("points").getAsInt();
                int amount = tierObject.get("amount").getAsInt();
                tieredMaxUnlocked++;
                tieredMaxPoints += points;
                tierCount++;

                if (tieredAchievements.has(inData)) {
                    int playerProgress = tieredAchievements.get(inData).getAsInt();
                    if (playerProgress >= amount) {
                        tieredUnlocked++;
                        tieredUnlockedPoints += points;
                    }
                }
            }
            tieredTotalUnlocked += tieredUnlocked;
        }


        if (Objects.equals(viewType, "challenge")) {
            challengeButton = challengeButton.asDisabled();
            embedTitle = readableName + " Challenge Achievements for " + username;

            embedContent += "Unlocked: " + challengeUnlocked.size() + "/" + challengeMaxUnlocked + "\n";
            embedContent += "Points: " + challengeUnlockedPoints + "/" + challengeMaxPoints + "\n\n";
        }
        if (Objects.equals(viewType, "tiered")) {
            tieredButton = tieredButton.asDisabled();
            embedTitle = readableName + " Tiered Achievements for " + username;

            embedContent += "Unlocked: " + tieredTotalUnlocked + "/" + tieredMaxUnlocked + "\n";
            embedContent += "Points: " + tieredUnlockedPoints + "/" + tieredMaxPoints + "\n\n";
        }
        if (Objects.equals(viewType, "all")) {
            allButton = allButton.asDisabled();
            embedTitle = readableName + " Achievement Summary for " + username;

            int totalUnlocked = challengeUnlocked.size() + tieredTotalUnlocked;
            int totalMaxUnlocked = challengeMaxUnlocked + tieredMaxUnlocked;
            int totalPoints = challengeUnlockedPoints + tieredUnlockedPoints;
            int totalMaxPoints = challengeMaxPoints + tieredMaxPoints;
            embedContent += "Total Unlocked: " + totalUnlocked + "/" + totalMaxUnlocked + "\n";
            embedContent += "Challenge Unlocked: " + challengeUnlocked.size() + "/" + challengeMaxUnlocked + "\n";
            embedContent += "Tiered Unlocked: " + tieredTotalUnlocked + "/" + tieredMaxUnlocked + "\n\n";

            embedContent += "Total Points: " + totalPoints + "/" + totalMaxPoints + "\n";
            embedContent += "Challenge Points: " + challengeUnlockedPoints + "/" + challengeMaxPoints + "\n";
            embedContent += "Tiered Points: " + tieredUnlockedPoints + "/" + tieredMaxPoints + "\n\n";
        }

        messageCreateBuilder.addActionRow(allButton, challengeButton, tieredButton);

        messageCreateBuilder.addEmbeds(makeStatsEmbed(embedTitle, embedContent));

        return messageCreateBuilder.build();
    }
}
