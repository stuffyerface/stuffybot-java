package me.stuffy.stuffybot.utils;

import me.stuffy.stuffybot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

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

    public static void verifyButton(ButtonInteractionEvent event) {
        Modal modal = Modal.create("verify", "Verify your identity in Stuffy Discord")
                .addComponents(ActionRow.of(TextInput.create("ign", "Minecraft Username", TextInputStyle.SHORT)
                                .setPlaceholder("Your Minecraft Username")
                                .setMaxLength(16)
                                .setMinLength(1)
                                .setRequired(true)
                                .build()),
                        ActionRow.of(
                                TextInput.create("captcha", "CAPTCHA", TextInputStyle.PARAGRAPH)
                                        .setPlaceholder("Enter the word 'stuffy'.\n" +
                                                "To prevent abuse, failing the CAPTCHA " +
                                                "will result in a short timeout.")
                                        .setRequired(false)
                                        .build()))
                .build();
        event.replyModal(modal).queue();
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
