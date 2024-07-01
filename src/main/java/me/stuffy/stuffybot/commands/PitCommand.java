package me.stuffy.stuffybot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class PitCommand extends BaseCommand{

    public PitCommand(String name, String description) {
        super(name, description,
                new OptionData(OptionType.STRING, "username", "The username of the player you want to look up", true));
    }

    @Override
    protected void onCommand(SlashCommandInteractionEvent event) {
        String username = event.getOption("username").getAsString();

    }
}
