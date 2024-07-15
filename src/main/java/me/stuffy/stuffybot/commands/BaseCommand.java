package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.Bot;
import me.stuffy.stuffybot.utils.Logger;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

public abstract class BaseCommand extends ListenerAdapter {
    private String name;
    private String description;
    private final Map<String, Instant> latestValidInteraction = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


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
            latestValidInteraction.put(event.getHook().getId(), Instant.now());
            scheduler.scheduleAtFixedRate(this::endEvent, 0, 1, TimeUnit.SECONDS);
        }

        // StatisticsManager.incrementTotalCommandsRun();
    }

    private void endEvent() {
        latestValidInteraction.forEach((messageId, time) -> {
            if (Instant.now().getEpochSecond() - time.getEpochSecond() > 30) {
                latestValidInteraction.remove(messageId);
                cleanupEventResources(messageId);
            }
        });
    }

    protected abstract void onCommand(SlashCommandInteractionEvent event);

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        this.onButton(event);
    }

    protected abstract void onButton(ButtonInteractionEvent event);

    protected abstract void cleanupEventResources(String messageId);
}
