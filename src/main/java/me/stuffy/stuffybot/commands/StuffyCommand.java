package me.stuffy.stuffybot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class StuffyCommand extends BaseCommand {

    public StuffyCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void onCommand(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage("Hello, person!").queue();
    }
}
