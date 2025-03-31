package me.stuffy.stuffybot.utils;

import me.stuffy.stuffybot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.*;

import static me.stuffy.stuffybot.utils.APIUtils.getMojangProfile;
import static me.stuffy.stuffybot.utils.MiscUtils.toSkillIssue;

public class DiscordUtils {
    public static MessageEmbed makeEmbed(String embedTitle, String embedSubtitle, String embedContent, int embedColor, Integer maxLines) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(embedTitle);
        String[] lines = embedContent.split("\n");
        int lineCount = lines.length;
        if (lineCount <= maxLines) {
            if(embedSubtitle == null)
                embedBuilder.setDescription(embedContent);
            else
                embedBuilder.setDescription("-# " + embedSubtitle + "\n" + embedContent);
        } else {
            if(embedSubtitle != null) {
                embedBuilder.setDescription("-# " + embedSubtitle);
            }
            StringBuilder column1 = new StringBuilder();
            StringBuilder column2 = new StringBuilder();
            for (int i = 0; i < lineCount; i++) {
                if (i % 2 == 0) {
                    column1.append(lines[i]).append("\n");
                } else {
                    column2.append(lines[i]).append("\n");
                }
            }
            embedBuilder.addField("", column1.toString(), true);
            embedBuilder.addField("", column2.toString(), true);
        }
        embedBuilder.setColor(embedColor);
        embedBuilder.setFooter("Stuffy Bot by @stuffy");
        embedBuilder.setTimestamp(new Date().toInstant());
        return embedBuilder.build();
    }

    public static MessageEmbed makeEmbed(String embedTitle, String embedSubtitle, String embedContent, int embedColor) {
        return makeEmbed(embedTitle, embedSubtitle, embedContent, embedColor, 15);
    }
    public static MessageEmbed makeErrorEmbed(String embedTitle, String embedContent) {
        if (Calendar.getInstance().get(Calendar.MONTH) == Calendar.APRIL && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1){
            embedTitle = toSkillIssue(embedTitle);
            embedContent = toSkillIssue(embedContent);
        }
        return makeEmbed(":no_entry: " + embedTitle, null, embedContent, 0xC95353);
    }

    public static MessageEmbed makeUpdateEmbed(String embedTitle, String embedContent) {
        return makeEmbed(":mega: " + embedTitle, null, embedContent, 0xEBD773);
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
        return makeEmbed(":mega: Rank Change Detected", null, embedContent, color);
    }

    public static MessageEmbed makeStatsEmbed(String embedTitle, String embedContent) {
        return makeEmbed(embedTitle, null, embedContent, 0x6D8FCE);
    }

    public static MessageEmbed makeStatsEmbed(String embedTitle, String embedSubtitle, String embedContent) {
        return makeEmbed(embedTitle, embedSubtitle , embedContent, 0x6D8FCE);
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

    public static String getUsername(SlashCommandInteractionEvent event) throws APIException {
        String username = event.getOption("ign") == null ? null : event.getOption("ign").getAsString();
        if (username == null) {
            if (Bot.getGlobalData().getLinkedAccounts().containsKey(event.getUser().getId())) {
                UUID uuid = Bot.getGlobalData().getLinkedAccounts().get(event.getUser().getId());
                username = getMojangProfile(uuid).getUsername();
            } else {
                username = getDiscordUsername(event.getUser().getName());
            }
        }
        return username;
    }
}
