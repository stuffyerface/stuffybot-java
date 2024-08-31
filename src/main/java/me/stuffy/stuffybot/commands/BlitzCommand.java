package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.utils.APIException;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.text.DecimalFormat;
import java.util.Map;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;

public class BlitzCommand {
    public static MessageCreateData blitz(InteractionId interactionId) throws APIException {
        String ign = interactionId.getOptions().get("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();

        Map<String, Integer> blitzStats = hypixelProfile.getBlitzStats();

        DecimalFormat df = new DecimalFormat("#,###");
        DecimalFormat df2 = new DecimalFormat("#,###.##");
        StringBuilder embedContent = new StringBuilder();

        for (Map.Entry<String, Integer> entry : blitzStats.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            if (value < 10000) {
                embedContent.append(key).append(": **").append(df.format(value)).append("**/10,000 (").append(df2.format(value / 100)).append("%)\n");
            } else {
                embedContent.append("~~").append(key).append(": **").append(df.format(value)).append("**/10,000~~\n");
            }
        }

        MessageEmbed blitzEmbed = makeStatsEmbed(
                "Blitz Ultimate Kit Xp for " + username,
                embedContent.toString()
        );

        return new MessageCreateBuilder()
                .addEmbeds(blitzEmbed)
                .build();
    }
}
