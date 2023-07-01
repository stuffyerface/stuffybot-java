package me.stuffy.stuffybot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PingCommand extends BaseCommand {

    public PingCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void onCommand(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage("Pong!").queue();
    }
}
