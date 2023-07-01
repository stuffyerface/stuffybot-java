package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

public abstract class BaseCommand extends ListenerAdapter {
    private String name;
    private String description;

    public BaseCommand(String name, String description, OptionData... options) {
        this.name = name;
        this.description = description;
        Bot bot = Bot.getInstance();
        bot.getTestGuild().upsertCommand(name, description)
                .addOptions(options)
                .queue();
        bot.getLogChannel().sendMessage("Successfully registered command " + name + " with description " + description).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals(this.name)) {
            event.deferReply().queue();
            this.onCommand(event);
        }
    }
    protected abstract void onCommand(SlashCommandInteractionEvent event);

}
