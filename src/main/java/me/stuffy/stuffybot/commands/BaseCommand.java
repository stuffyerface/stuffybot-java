package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.Bot;
import me.stuffy.stuffybot.utils.Logger;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public abstract class BaseCommand extends ListenerAdapter {
    private String name;
    private String description;

    public BaseCommand(String name, String description, OptionData... options) {
        this.name = name;
        this.description = description;
        Bot bot = Bot.getInstance();
        bot.getHomeGuild().upsertCommand(name, description)
                .addOptions(options)
                .queue();
        Logger.log("<Startup> Registered command " + name + ": " + description);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals(this.name)) {
            event.deferReply().queue();
            String options = "";
            for (OptionMapping option : event.getOptions()) {
                options += option.getName() + ": " + option.getAsString() + ", ";
            }
            Logger.log("<Command> @" + event.getUser().getName() + ": /" + this.name + " " + options);
            this.onCommand(event);
        }

        // StatisticsManager.incrementTotalCommandsRun();
    }
    protected abstract void onCommand(SlashCommandInteractionEvent event);

}
