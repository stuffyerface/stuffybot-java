package me.stuffy.stuffybot.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.utils.APIException;
import me.stuffy.stuffybot.utils.Logger;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static me.stuffy.stuffybot.utils.APIUtils.getAchievementsResources;
import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeErrorEmbed;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;
import static me.stuffy.stuffybot.utils.MiscUtils.toReadableName;

public class SearchCommand {
    public static MessageCreateData search(InteractionId interactionId) {
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

            StringBuilder achievementBody = new StringBuilder();

            if (gameOneTimes.has(achievementID)) {
                // CHALLENGE ACHIEVEMENTS
                JsonObject searchedAchievement = gameOneTimes.get(achievementID).getAsJsonObject();
                String achievementName = searchedAchievement.get("name").getAsString();
                String achievementDescription = searchedAchievement.get("description").getAsString();
                Integer achievementPoints = searchedAchievement.get("points").getAsInt();

                achievementBody.append("-# ").append(gameType).append(" Challenge Achievement Found.\n");
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
            } else if (gameTiered.has(achievementID)) {
                // TIERED ACHIEVEMENTS
                JsonObject searchedAchievement = gameTiered.get(achievementID).getAsJsonObject();
                String achievementName = searchedAchievement.get("name").getAsString();
                String achievementDescription = searchedAchievement.get("description").getAsString();

                achievementBody.append("-# ").append(gameType).append(" Tiered Achievement Found.\n");
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
            } else {
                throw new Exception();
            }

            achievementBody.append("\n-# Internal ID: `").append(searchTerm).append("`.");

            return new MessageCreateBuilder().addEmbeds(
                    makeStatsEmbed("Achievement Search Result", achievementBody.toString())
            ).build();
        } catch (Exception e) {
            Logger.logError(e.getMessage());
            MessageEmbed errorEmbed = makeErrorEmbed(
                    "Something went wrong",
                    "You should report this!"
            );
            return new MessageCreateBuilder()
                    .addEmbeds(errorEmbed)
                    .build();
        }
    }
}
