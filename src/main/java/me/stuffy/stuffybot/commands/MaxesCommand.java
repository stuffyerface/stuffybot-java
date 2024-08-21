package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.utils.APIException;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;
import static me.stuffy.stuffybot.utils.MiscUtils.toReadableName;

public class MaxesCommand {
    public static MessageCreateData maxes(InteractionId interactionId) throws APIException {
        String ign = interactionId.getOptions().get("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();

        ArrayList<String> maxesArray = hypixelProfile.getMaxGames();

        StringBuilder embedContent = new StringBuilder();
        embedContent.append("-# ").append(username).append(" has ").append(maxesArray.size()).append(" Maxed Games\n");
        for (String game : maxesArray) {
            embedContent.append(toReadableName(game)).append("\n");
        }

        if (maxesArray.isEmpty()) {
            embedContent.append("No maxed games found");
        }

        MessageEmbed maxesEmbed = makeStatsEmbed(
                "Maxed Games for " + username,
                embedContent.toString()
        );

        return new MessageCreateBuilder()
                .addEmbeds(maxesEmbed)
                .build();

    }
}
