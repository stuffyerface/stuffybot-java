package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.Bot;
import me.stuffy.stuffybot.utils.InteractionException;
import me.stuffy.stuffybot.utils.Logger;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static me.stuffy.stuffybot.utils.DiscordUtils.getUsername;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeErrorEmbed;
import static me.stuffy.stuffybot.utils.Interactions.getResponse;

public abstract class BaseCommand extends ListenerAdapter {
    private String name;
    private String description;
    private OptionData[] options;
    private final Map<String, Instant> latestValidInteraction = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public BaseCommand(String name, String description, OptionData... options) {
        this.name = name;
        this.description = description;
        this.options = options;
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
            String interactionId = this.name + ":" + event.getUser().getId() + ":";
            ArrayList<String> optionsArray = new ArrayList<String>();

            for (OptionData option : this.options) {
                String optionName = option.getName();
                if(optionName.equals("ign") && event.getOption("ign") == null){
                    String ign = getUsername(event);
                    optionsArray.add("ign=" + ign);
                }
            }
            Pattern pattern = Pattern.compile("[,=:]");
            for (OptionMapping option : event.getOptions()) {
                String optionString = option.getAsString();
                if(pattern.matcher(optionString).find()){
                    MessageEmbed errorEmbed = makeErrorEmbed("Slash Command Error", "An error occurred while processing your command.\n-# Invalid character in option  `" + option.getName() + "`");
                    event.getHook().sendMessageEmbeds(errorEmbed).queue();
                    return;
                }
                optionsArray.add(option.getName() + "=" + optionString);
            }

            String options = String.join(",", optionsArray);
            interactionId += options;

            Logger.log("<Command> @" + event.getUser().getName() + ": /" + this.name + " " + options);

            MessageEmbed response = null;
            try {
                response = getResponse(interactionId);
            } catch (InteractionException e) {
                MessageEmbed errorEmbed = makeErrorEmbed("Slash Command Error", "An error occurred while processing your command.\n-# " + e.getMessage());
                event.getHook().sendMessageEmbeds(errorEmbed).queue();
                return;
            }

            event.getHook().sendMessageEmbeds(response).queue();

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

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        this.onButton(event);
    }

    protected abstract void onCommand(SlashCommandInteractionEvent event);

    protected abstract void onButton(ButtonInteractionEvent event);

    protected abstract void cleanupEventResources(String messageId);
}
