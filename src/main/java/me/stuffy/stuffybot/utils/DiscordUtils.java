package me.stuffy.stuffybot.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DiscordUtils {
    public static MessageEmbed makeEmbed(String embedTitle, String embedContent, int embedColor) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(embedTitle);
        embedBuilder.setDescription(embedContent);
        embedBuilder.setColor(embedColor);
        embedBuilder.setFooter("AP Machine by @stuffy");
        embedBuilder.setTimestamp(new Date().toInstant());
        return embedBuilder.build();
    }

    public static MessageEmbed makeErrorEmbed(String embedTitle, String embedContent) {
        return makeEmbed(":no_entry: " + embedTitle, embedContent, 0xff0000);
    }

    public static MessageEmbed makeUpdateEmbed(String embedTitle, String embedContent) {
        return makeEmbed(":mega: " + embedTitle, embedContent, 0xffef14);
    }

    public static String getDiscordUsername(String id){
        return id.replaceAll("#0000$", "");
    }

    /**
     * @see #discordTimeUnix(long, String)
     */
    public static String discordTimeNow(String formatType) {
        return discordTimeUnix(System.currentTimeMillis(), formatType);
    }

    /**
     * @see #discordTimeUnix(long, String)
     */
    public static String discordTimeNow() {
        return discordTimeNow("R");
    }

    /**
     * Returns a string that Discord will parse into a timestamp
     * types: t = short time, T = long time, d = short date, D = long date, f = short date/time, F = long date/time, R = relative time
     * @param timestamp
     * @param formatType
     * @return a string that Discord will parse into a timestamp
     */
    public static String discordTimeUnix(long timestamp, String formatType) {
        final List<String> VALID_TYPES = Arrays.asList("t", "T", "d", "D", "f", "F", "R");
        if (!VALID_TYPES.contains(formatType)) {
            throw new IllegalArgumentException("Invalid format type");
        }
        return "<t:" + timestamp / 1000 + ":" + formatType + ">";
    }

    /**
     * @see #discordTimeUnix(long, String)
     */
    public static String discordTimeUnix(long timestamp) {
        return discordTimeUnix(timestamp, "R");
    }
}
