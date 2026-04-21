package me.stuffy.stuffybot.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.utils.Logger;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.text.DecimalFormat;

import static me.stuffy.stuffybot.utils.APIUtils.getAchievementsResources;
import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeErrorEmbed;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;
import static me.stuffy.stuffybot.utils.MiscUtils.toReadableName;

public class SearchCommand {
    public static MessageCreateData searchAchievement(InteractionId interactionId) {
        try{
            String searchTerm = interactionId.getOptions().get("search");

            String[] parts = searchTerm.split("_", 2);
            String gameID = parts[0].toLowerCase();
            String achievementID = parts[1];

            JsonObject achievementResources = getAchievementsResources().getAsJsonObject();
            JsonObject gameAchievements = achievementResources.get(gameID).getAsJsonObject();
            JsonObject gameOneTimes = gameAchievements.get("one_time").getAsJsonObject();
            JsonObject gameTiered = gameAchievements.get("tiered").getAsJsonObject();

            String gameType = toReadableName(gameID);

            StringBuilder achievementBody;

            if (gameOneTimes.has(achievementID)) {
                achievementBody = formatChallengeAchievement(achievementID, gameType, searchTerm, gameOneTimes);
            } else if (gameTiered.has(achievementID)) {
                achievementBody = formatTieredAchievement(achievementID, gameType, searchTerm, gameTiered);
            } else {
                throw new Exception();
            }

            return new MessageCreateBuilder().addEmbeds(
                    makeStatsEmbed("Achievement Search Result", achievementBody.toString())
            ).build();
        } catch (Exception e) {
            Logger.logError(e.getMessage());
            MessageEmbed errorEmbed = makeErrorEmbed(
                    "Invalid Achievement",
                    "You typed something that does not exist!\n-# It has autocomplete for a reason"
            );
            return new MessageCreateBuilder()
                    .addEmbeds(errorEmbed)
                    .build();
        }
    }

    public static MessageCreateData searchRandom(InteractionId interactionId) {
        String gameID = interactionId.getOptions().getOrDefault("game","all").toLowerCase();
        String achievementType = interactionId.getOptions().getOrDefault("type", "challenge").toLowerCase();
        String exclude = interactionId.getOptions().getOrDefault("exclude", "both").toLowerCase();

        if (achievementType.equals("both")) {
            achievementType = Math.random() < 0.5 ? "challenge" : "tiered";
        }

        JsonObject achievementResources = getAchievementsResources().getAsJsonObject();
        JsonObject achievementResourcesCopy = achievementResources.deepCopy();
        // TODO: Remove all excluded achievements from the pool before selecting a random one
        for (String game : achievementResources.keySet()) {
            if (!gameID.equals("all") && !game.equals(gameID)) {
                achievementResourcesCopy.remove(game);
                continue;
            }
            if ((exclude.equals("seasonal") || exclude.equals("both")) && isSeasonal(game)) {
                achievementResourcesCopy.remove(game);
            }
            if ((exclude.equals("legacy") || exclude.equals("both")) && isLegacy(game)) {
                achievementResourcesCopy.remove(game);
            }
            if (achievementType.equals("challenge")) {
                try {
                    achievementResourcesCopy.get(game).getAsJsonObject().remove("tiered");
                } catch (Exception e) {
                    // No tiered achievements for this game, ignore
                }
            } else if (achievementType.equals("tiered")) {
                try {
                    achievementResourcesCopy.get(game).getAsJsonObject().remove("one_time");
                } catch (Exception e) {
                    // No one-time achievements for this game, ignore
                }
            }
            try {
                if ((achievementType.equals("tiered") && achievementResourcesCopy.get(game).getAsJsonObject().get("tiered")
                        .getAsJsonObject().entrySet().isEmpty()) || (achievementType.equals("challenge") &&
                        achievementResourcesCopy.get(game).getAsJsonObject().get("one_time").getAsJsonObject().entrySet().isEmpty())) {
                    achievementResourcesCopy.remove(game);
                }
            } catch (Exception e) {
                // No achievements for this game, ignore
                achievementResourcesCopy.remove(game);
            }
        }

        if (achievementResourcesCopy.entrySet().isEmpty()) {
            MessageEmbed errorEmbed = makeErrorEmbed(
                    "No achievements found",
                    "Try expanding your search criteria!"
            );
            return new MessageCreateBuilder()
                    .addEmbeds(errorEmbed)
                    .build();
        } else {
            int randomGameIndex = (int) (Math.random() * achievementResources.entrySet().size());
            String randomGameID = achievementResources.entrySet().stream().skip(randomGameIndex).findFirst().get().getKey();
            JsonObject randomGameAchievements = achievementResources.get(randomGameID).getAsJsonObject();
            String gameType = toReadableName(randomGameID);

            StringBuilder achievementBody;
            if (achievementType.equals("challenge")) {
                JsonObject gameOneTimes = randomGameAchievements.get("one_time").getAsJsonObject();
                int randomAchievementIndex = (int) (Math.random() * gameOneTimes.entrySet().size());
                String randomAchievementID = gameOneTimes.entrySet().stream().skip(randomAchievementIndex).findFirst().get().getKey();
                String randomAchievementFullID = randomGameID.toUpperCase() + "_" + randomAchievementID;
                achievementBody = formatChallengeAchievement(randomAchievementID, gameType, randomAchievementFullID, gameOneTimes);
            } else {
                JsonObject gameTiered = randomGameAchievements.get("tiered").getAsJsonObject();
                int randomAchievementIndex = (int) (Math.random() * gameTiered.entrySet().size());
                String randomAchievementID = gameTiered.entrySet().stream().skip(randomAchievementIndex).findFirst().get().getKey();
                String randomAchievementFullID = randomGameID.toUpperCase() + "_" + randomAchievementID;
                achievementBody = formatTieredAchievement(randomAchievementID, gameType, randomAchievementFullID, gameTiered);
            }

            return new MessageCreateBuilder().addEmbeds(
                    makeStatsEmbed("Random Achievement", achievementBody.toString())
            ).build();
        }
    }

    private static StringBuilder formatChallengeAchievement(String achievementID, String gameType, String internalID, JsonObject gameOneTimes){
        StringBuilder achievementBody = new StringBuilder();
        // CHALLENGE ACHIEVEMENTS
        JsonObject searchedAchievement = gameOneTimes.get(achievementID).getAsJsonObject();
        String achievementName = searchedAchievement.get("name").getAsString();
        String achievementDescription = searchedAchievement.get("description").getAsString();
        Integer achievementPoints = searchedAchievement.get("points").getAsInt();

        achievementBody.append("-# ").append(gameType).append(" Challenge Achievement\n");
        achievementBody.append("**").append(achievementName).append("** (+**").append(achievementPoints).append("** points)\n");
        achievementBody.append(achievementDescription).append("\n");

        if (searchedAchievement.has("gamePercentUnlocked") && searchedAchievement.has("globalPercentUnlocked")){
            DecimalFormat df = new DecimalFormat("#0.00");
            Double gamePercentUnlocked = searchedAchievement.get("gamePercentUnlocked").getAsDouble();
            Double globalPercentUnlocked = searchedAchievement.get("globalPercentUnlocked").getAsDouble();
            achievementBody.append("\nUnlocked by `");
            achievementBody.append(df.format(gamePercentUnlocked));
            achievementBody.append("%` of ").append(gameType).append(", `");
            achievementBody.append(df.format(globalPercentUnlocked));
            achievementBody.append("%` of all players");
        }

        if(searchedAchievement.has("legacy") && searchedAchievement.get("legacy").getAsBoolean()){
            achievementBody.append("\n-# Legacy Achievement");
        }
        achievementBody.append("\n-# Internal ID: `").append(internalID).append("`.");
        return achievementBody;
    }

    private static StringBuilder formatTieredAchievement(String achievementID, String gameType, String internalID, JsonObject gameTiered){
        StringBuilder achievementBody = new StringBuilder();
        // TIERED ACHIEVEMENTS
        JsonObject searchedAchievement = gameTiered.get(achievementID).getAsJsonObject();
        String achievementName = searchedAchievement.get("name").getAsString();
        String achievementDescription = searchedAchievement.get("description").getAsString();

        achievementBody.append("-# ").append(gameType).append(" Tiered Achievement\n");
        achievementBody.append("**").append(achievementName).append("**\n");

        DecimalFormat df = new DecimalFormat("#,###");
        JsonArray tiers = searchedAchievement.get("tiers").getAsJsonArray();
        Integer maxReq = 0;
        StringBuilder tiersBuilder = new StringBuilder();
        tiersBuilder.append("```");
        for (JsonElement tier : tiers){
            int points = tier.getAsJsonObject().get("points").getAsInt();
            int amount = tier.getAsJsonObject().get("amount").getAsInt();
            tiersBuilder.append("\n ").append(df.format(amount)).append(" | ").append(points).append(" points");

            if(amount > maxReq) {
                maxReq = amount;
            }
        }
        tiersBuilder.append("```");

        achievementBody.append(achievementDescription.replace("%s", df.format(maxReq))).append("\n");
        achievementBody.append(tiersBuilder);

        if(searchedAchievement.has("legacy") && searchedAchievement.get("legacy").getAsBoolean()){
            achievementBody.append("\n-# Legacy Achievement");
        }
        achievementBody.append("\n-# Internal ID: `").append(internalID).append("`.");
        return achievementBody;
    }


    private static boolean isSeasonal(String gameID){
        return switch (gameID) {
            case "christmas2017", "easter", "halloween2017", "summer" -> true;
            default -> false;
        };
    }

    private static boolean isLegacy(String gameID) {
        return switch (gameID) {
            case "skyclash", "truecombat" -> true;
            default -> false;
        };
    }
}
