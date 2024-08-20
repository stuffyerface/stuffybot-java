package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.utils.APIException;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.Map;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;
import static net.dv8tion.jda.api.interactions.components.buttons.Button.secondary;

public class TkrCommand {
    public static MessageCreateData tkr(InteractionId interactionId) throws APIException {
        String ign = interactionId.getOptions().get("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();

        Map<String, Boolean> uniqueGolds = hypixelProfile.getTkrMaps();
        StringBuilder embedContent = new StringBuilder();
        int uniqueGoldCount = 0;

        for (Map.Entry<String, Boolean> entry : uniqueGolds.entrySet()) {
            String key = entry.getKey();
            Boolean value = entry.getValue();
            if (value) {
                uniqueGoldCount++;
            }
            embedContent.append(value ? "✅ " : "❌ ").append(key).append("\n");
        }


        embedContent.insert(0, "Unique Gold Medals: **" + uniqueGoldCount + "**/5\n\n");

        MessageEmbed tkrStats = makeStatsEmbed(
                "TKR Stats for " + username,
                embedContent.toString()
        );

        return new MessageCreateBuilder()
                .addEmbeds(tkrStats)
                .addActionRow(
                        secondary("achievements:" + interactionId.getUserId() + ":game=tkr," + interactionId.getOptionsString(), "Looking for TKR Achievements?")
                )
                .build();
    }
}
