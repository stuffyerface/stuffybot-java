package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.utils.InteractionException;
import me.stuffy.stuffybot.utils.Logger;
import me.stuffy.stuffybot.utils.StatisticsManager;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static me.stuffy.stuffybot.utils.DiscordUtils.getUsername;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeErrorEmbed;
import static me.stuffy.stuffybot.commands.Interactions.getResponse;
import static me.stuffy.stuffybot.utils.MiscUtils.*;

public class InteractionHandler extends ListenerAdapter {
    private final Map<String, Instant> latestValidInteraction = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public InteractionHandler() {
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        event.deferReply().queue();
        String interactionId = commandName + ":" + event.getUser().getId() + ":";
        ArrayList<String> optionsArray = new ArrayList<String>();

        // TODO: Error handling for invalid command
        if(!validCommand(commandName)){
            MessageEmbed errorEmbed = makeErrorEmbed("Slash Command Error", "An error occurred while processing your command.\n-# Invalid command `" + commandName + "`");
            event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
            return;
        }

        if(requiresIgn(commandName) && event.getOption("ign") == null){
            String ign = getUsername(event);
            optionsArray.add("ign=" + ign);
        }

        Pattern pattern = Pattern.compile("[,=:]");
        for (OptionMapping option : event.getOptions()) {
            String optionString = option.getAsString();
            if(pattern.matcher(optionString).find()){
                MessageEmbed errorEmbed = makeErrorEmbed("Slash Command Error", "An error occurred while processing your command.\n-# Invalid character in option  `" + option.getName() + "`");
                event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
                return;
            }
            optionsArray.add(option.getName() + "=" + optionString);
        }

        String options = String.join(",", optionsArray);
        interactionId += options;

        Logger.log("<Command> @" + event.getUser().getName() + ": /" + commandName + " " + options);

        MessageCreateData response = null;
        try {
            response = getResponse(interactionId);;
        } catch (InteractionException e) {
            MessageEmbed errorEmbed = makeErrorEmbed("Slash Command Error", "An error occurred while processing your command.\n-# " + e.getMessage());
            event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
            return;
        }

        event.getHook().sendMessage(response).queue();

        StatisticsManager.incrementCommandUsage(commandName);

        latestValidInteraction.put(event.getHook().getId(), Instant.now());
        scheduler.scheduleAtFixedRate(this::endEvent, 0, 1, TimeUnit.SECONDS);
    }

    private void endEvent() {
        latestValidInteraction.forEach((messageId, time) -> {
            if (Instant.now().getEpochSecond() - time.getEpochSecond() > 30) {
                latestValidInteraction.remove(messageId);
            }
        });
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        event.deferEdit().queue();

        Logger.log("<Button> @" + event.getUser().getName() + ": " + event.getComponentId());
        // Break down the interaction ID
        // Throw invalid error if the interaction ID is invalid
        // Check if the user is allowed to press the button
//        if(!validInteractionId(event.getComponentId())){
//            MessageEmbed errorEmbed = makeErrorEmbed("Button Interaction Error", "An error occurred while processing your button press.\n-# Invalid interactionId `" + event.getComponentId() + "`");
//            event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
//            return;
//        }

        InteractionId interactionId;
        try {
            interactionId = new InteractionId(event.getComponentId());
        } catch (Exception e) {
            MessageEmbed errorEmbed = makeErrorEmbed("Button Interaction Error", "An error occurred while processing your button press.\n-# " + e.getMessage());
            event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
            return;
        }



        MessageCreateData data;
        try {
            data = getResponse(event.getComponentId());
        } catch (InteractionException e) {
            MessageEmbed errorEmbed = makeErrorEmbed("Button Interaction Error", "An error occurred while processing your button press.\n-# " + e.getMessage());
            event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
            return;
        }
        event.getHook().sendMessage(response).queue();
    }
}


