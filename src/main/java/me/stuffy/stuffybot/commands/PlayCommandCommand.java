package me.stuffy.stuffybot.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.stuffy.stuffybot.interactions.InteractionId;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import static me.stuffy.stuffybot.utils.APIUtils.getPlayCommands;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeErrorEmbed;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;

public class PlayCommandCommand {
    public static MessageCreateData playCommand(InteractionId interactionId) {
        String input = interactionId.getOption("game");

        JsonElement gameData = getPlayCommands().getAsJsonObject().get("gameData");
        if (gameData == null) {
            return new MessageCreateBuilder().setContent("Failed to load play command data.").build();
        }

        JsonArray games = gameData.getAsJsonArray();
        for (JsonElement game : games) {
            String gameName = game.getAsJsonObject().get("name").getAsString();
            JsonArray modes = game.getAsJsonObject().get("modes").getAsJsonArray();
            for (JsonElement mode : modes) {
                if(!mode.getAsJsonObject().has("identifier") || !mode.getAsJsonObject().has("name")) {
                    continue;
                }
                String modeName = mode.getAsJsonObject().get("name").getAsString();
                String identifier = mode.getAsJsonObject().get("identifier").getAsString();
                String fullName;
                if (gameName.equals(modeName)) {
                    fullName = gameName;
                } else {
                    fullName = gameName + ": " + modeName;
                }
                if (input.equals(identifier)) {
                    return new MessageCreateBuilder().addEmbeds(
                            makeStatsEmbed("Play Command Search", "-# Use this to quickly join a game from anywhere." +
                                    "\n**" + fullName + "**\n `/play " + identifier + "`")
                    ).build();
                }
            }
        }

        return new MessageCreateBuilder().addEmbeds(
                makeErrorEmbed("Invalid Game or Mode", "I don't have a play command for that game or mode." +
                        "\n-# Try using the search feature.")
        ).build();
    }
}
