package me.stuffy.stuffybot.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Date;

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

    public static String getUsername(String id){
        return id.replaceAll("#0000$", "");
    }
}
