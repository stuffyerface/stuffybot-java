package me.stuffy.stuffybot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Tournament extends BaseCommand{
    public Tournament(String name, String description){
        super(name, description,
                new OptionData(OptionType.STRING, "tournament", "The tournament, defaults to most recent", false).addChoices(

                ),
                new OptionData(OptionType.STRING, "username", "Player's username", false));
    }

    @Override
    protected void onCommand(SlashCommandInteractionEvent event) {
        String tournament = event.getOption("tournament").getAsString();
        String username = event.getOption("username").getAsString();
    }
}
