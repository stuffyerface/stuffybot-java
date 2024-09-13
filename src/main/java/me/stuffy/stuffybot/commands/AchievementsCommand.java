package me.stuffy.stuffybot.commands;

import com.google.gson.JsonObject;
import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.utils.APIException;
import me.stuffy.stuffybot.utils.InvalidOptionException;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.text.DecimalFormat;
import java.util.Objects;

import static me.stuffy.stuffybot.utils.APIUtils.getAchievementsResources;
import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;
import static me.stuffy.stuffybot.utils.MiscUtils.getMaxAchievementPoints;
import static me.stuffy.stuffybot.utils.MiscUtils.getMaxAchievements;

public class AchievementsCommand {

    public static MessageCreateData achievements(InteractionId interactionId) throws APIException, InvalidOptionException {
        String ign = interactionId.getOption("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();

        String gameId = interactionId.getOption("game", "none");
        String viewType = interactionId.getOption("type", "all");

        JsonObject gameAchievements = null;

        if (gameId.equals("none")) {
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
            content.append("Legacy Points: **").append(thousands.format(legacyPoints)).append("**\n\n");

            content.append("Easiest challenge: **").append(hypixelProfile.getEasiestChallenge()).append("**\n");
            content.append("Closest tiered: **").append(hypixelProfile.getEasiestTiered()).append("**\n");

            return new MessageCreateBuilder()
                    .addEmbeds(makeStatsEmbed("Achievement Data for " + hypixelProfile.getDisplayName(), "why is not showing", content.toString()))
                    .build();
        }

        JsonObject achievementData = getAchievementsResources().getAsJsonObject();

        for (String game : achievementData.keySet()) {
            JsonObject gameData = achievementData.getAsJsonObject(game);
            if (Objects.equals(game, gameId)) {
                gameAchievements = gameData;
            }
        }

        if (gameAchievements == null) {
            throw new InvalidOptionException("game", "Invalid game");
        }

        int total_points = 0;
        int total_legacy_points = 0;

        String subtitle = "  ";
        String description = "  ";


        return new MessageCreateBuilder()
                .addEmbeds(makeStatsEmbed(" ", subtitle, description))
                .build();
    }
}
