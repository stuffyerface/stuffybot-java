package me.stuffy.stuffybot.utils;

import me.stuffy.stuffybot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DiscordUtils {
    public static MessageEmbed makeEmbed(String embedTitle, String embedContent, int embedColor) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(embedTitle);
        embedBuilder.setDescription(embedContent);
        embedBuilder.setColor(embedColor);
        embedBuilder.setFooter("Stuffy Bot by @stuffy");
        embedBuilder.setTimestamp(new Date().toInstant());
        return embedBuilder.build();
    }

    public static MessageEmbed makeErrorEmbed(String embedTitle, String embedContent) {
        return makeEmbed(":no_entry: " + embedTitle, embedContent, 0xff0000);
    }

    public static MessageEmbed makeUpdateEmbed(String embedTitle, String embedContent) {
        return makeEmbed(":mega: " + embedTitle, embedContent, 0xffef14);
    }

    public static MessageEmbed makeStatsEmbed(String embedTitle, String embedContent) {

        return makeEmbed(embedTitle, embedContent, 0xf7cb72);
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

    public static void verifyUser(User user, String ign) {
        Bot bot = Bot.getInstance();
        bot.getHomeGuild().addRoleToMember(user, bot.getVerifiedRole()).queue();
        // bot.getHomeGuild().getMember(user).modifyNickname(ign).queue();
        updateRoles(user, ign, false);
    }

    public static void updateRoles(User user, String ign, boolean announce) {
        Bot bot = Bot.getInstance();
        // bot.getHomeGuild().getMember(user).modifyNickname(ign).queue();
    }

    public static String getUsername(SlashCommandInteractionEvent event) {
        String username = event.getOption("ign") == null ? null : event.getOption("ign").getAsString();
        if (username == null) {
            // TODO: First, check the database
            username = getDiscordUsername(event.getUser().getName());
        }
        return username;
    }
}
