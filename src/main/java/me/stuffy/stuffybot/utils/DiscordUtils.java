package me.stuffy.stuffybot.utils;

import me.stuffy.stuffybot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.*;

import static me.stuffy.stuffybot.utils.MiscUtils.toSkillIssue;

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
        if (Calendar.getInstance().get(Calendar.MONTH) == Calendar.APRIL && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1){
            embedTitle = toSkillIssue(embedTitle);
            embedContent = toSkillIssue(embedContent);
        }
        return makeEmbed(":no_entry: " + embedTitle, embedContent, 0xff0000);
    }

    public static MessageEmbed makeUpdateEmbed(String embedTitle, String embedContent) {
        return makeEmbed(":mega: " + embedTitle, embedContent, 0xffef14);
    }

    public static MessageEmbed makeStaffRankChangeEmbed(String ign, String oldRank, String newRank, String position) {
        String embedContent = "### **" + ign + "**:  `" + oldRank + "` ⇒ `" + newRank + "`\nPlayer stats: [Plancke](https://plancke.io/hypixel/player/stats/" + ign + ")\n";

        if (position != null){
            embedContent += "Suspected Position: " + position + "\n";
        }
        int color = 0xaaaaaa;
        if(newRank.equals("GM")){
            color = 0x00aa00;
        }
        if (newRank.equals("ADMIN")){
            color = 0xff5555;
        }
        return makeEmbed(":mega: Rank Change Detected", embedContent, color);
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

    public static Map<String, String> parseOptions(String options) {
        String[] parts = options.split(",");
        Map<String, String> optionsMap = new HashMap<>();
        for (String part : parts) {
            String[] keyValue = part.split("=");
            if (keyValue.length != 2) {
                return null;
            }
            optionsMap.put(keyValue[0], keyValue[1]);
        }
        return optionsMap;
    }
}
