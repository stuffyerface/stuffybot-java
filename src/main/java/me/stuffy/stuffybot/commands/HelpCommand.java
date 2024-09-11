package me.stuffy.stuffybot.commands;

import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.HashMap;
import java.util.Map;

import static me.stuffy.stuffybot.utils.DiscordUtils.makeEmbed;

public class HelpCommand {
    public static MessageCreateData help() {
        Map<String, String> commands = new HashMap<>();
        commands.put("`/link`", "Set a Default account to check when running commands");
        commands.put("`/stats`", "Get your Hypixel stats");
        commands.put("`/maxes`", "Get your maxed games");
        commands.put("`/playcommand`", "Lookup the command to quickly hop into a game");
        commands.put("`/tournament`", "Get your tournament stats");

        StringBuilder commandList = new StringBuilder();
        for (Map.Entry<String, String> entry : commands.entrySet()) {
            commandList.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        String helpText = "-# Bot Commands\n" + commandList;
        helpText += """
                ...and more!

                -# Discord Server
                Join the [discord server](https://discord.gg/X6WJT7WNVz) for help, feedback, and more!
                We also announce Staff Rank changes, Hypixel leaks, and more!
                
                -# Source Code
                The source code for this bot, its ToS, and Privacy Policy are
                available on [GitHub](https://github.com/stuffyerface/stuffybot-java).
                Feel free to contribute, report bugs, or suggest new features!
                Don't forget to follow, star, and check out our API!
                """;

        return new MessageCreateBuilder().addEmbeds(
                makeEmbed("Stuffy Bot Help Menu", null, helpText, 0x570FF4, 30)
        ).build();
    }
}
