package me.stuffy.stuffybot.commands;

import com.google.gson.JsonObject;
import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.utils.APIException;
import me.stuffy.stuffybot.utils.InvalidOptionException;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Objects;

import static me.stuffy.stuffybot.utils.APIUtils.getAchievementsResources;
import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;

public class AchievementsCommand {

    public static MessageCreateData achievements(InteractionId interactionId) throws APIException, InvalidOptionException {
        String ign = interactionId.getOption("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();

        String gameId = interactionId.getOption("game", "none");
        String viewType = interactionId.getOption("type", "all");

        JsonObject achievementData = getAchievementsResources().getAsJsonObject();

        if (achievementData == null) {
            throw new APIException("Stuffy", "Failed to load achievement data.");
        }

        JsonObject gameAchievements = null;

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
