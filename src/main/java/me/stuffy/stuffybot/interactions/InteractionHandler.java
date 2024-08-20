package me.stuffy.stuffybot.interactions;

import me.stuffy.stuffybot.utils.InteractionException;
import me.stuffy.stuffybot.utils.Logger;
import me.stuffy.stuffybot.utils.StatisticsManager;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static me.stuffy.stuffybot.interactions.InteractionManager.getResponse;
import static me.stuffy.stuffybot.utils.DiscordUtils.*;
import static me.stuffy.stuffybot.utils.MiscUtils.*;

public class InteractionHandler extends ListenerAdapter {
    private final Map<String, Instant> latestValidInteraction = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();

        event.deferReply().queue();
        String interactionId = commandName + ":" + event.getUser().getId() + ":";
        ArrayList<String> optionsArray = new ArrayList<String>();

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

        if(response != null) {
            event.getHook().sendMessage(response).queue();
        }

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

        InteractionId interactionId;
        try {
            interactionId = new InteractionId(event.getComponentId());
        } catch (Exception e) {
            event.deferEdit().queue();
            MessageEmbed errorEmbed = makeErrorEmbed("Button Interaction Error", "An error occurred while processing your button press.\n-# You pressed an imaginary button");
            event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
            Logger.logError("<Button> @" + event.getUser().getName() + ": `" + event.getComponentId() + "`");
            return;
        }

        Logger.log("<Button> @" + event.getUser().getName() + ": `" + event.getComponentId() + "`");

        if (!interactionId.getUserId().equals(event.getUser().getId()) && !interactionId.getUserId().equals("null")) {
            try {
                throw new InteractionException(new String[]{
                        "Keep your hands off other people's buttons",
                        "You can't press other people's buttons",
                        "How would you like if if I pressed your buttons?",
                        "This button does not belong to you",
                });
            } catch (InteractionException e) {
                event.deferEdit().queue();
                MessageEmbed errorEmbed = makeErrorEmbed("Invalid Button Ownership",
                        "You can't use modify commands run by others.\n-# " + e.getMessage());
                event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
                return;
            }
        }

        if (interactionId.getCommand().equals("verify")){
            verifyButton(event);
            return;
        }

        event.deferEdit().queue();
        MessageCreateData data;
        try {
            data = getResponse(event.getComponentId());
        } catch (InteractionException e) {
            MessageEmbed errorEmbed = makeErrorEmbed("Button Interaction Error", "An error occurred while processing your button press.\n-# " + e.getMessage());
            event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
            return;
        }


        if (data == null) {
            // When the button press does not require a response
            return;
        }
        MessageEditData editData = MessageEditData.fromCreateData(data);
        event.getHook().editOriginal(editData).queue();
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String toLog = "<Modal> @" + event.getUser().getName() + ": `" + event.getModalId() + "`";
        for (ModalMapping mapping : event.getValues()) {
            toLog += " `" + mapping.getId() + "=" + mapping.getAsString() + "`";
        }
        Logger.log(toLog);
        if(event.getModalId().equals("verify")) {
            String ign = Objects.requireNonNull(event.getValue("ign")).getAsString();
            String captcha = Objects.requireNonNull(event.getValue("captcha")).getAsString();
            if(!captcha.equals("stuffy")) {
                // TODO: Make this actually time out for 5 minutes
                MessageEmbed errorEmbed = makeErrorEmbed("Verification Error", "You entered the CAPTCHA incorrectly.\n-# Try again in " + discordTimeUnix(Instant.now().plusSeconds(300).toEpochMilli()));
                MessageCreateData data = new MessageCreateBuilder()
                        .addEmbeds(errorEmbed)
                        .build();
                event.reply(data).setEphemeral(true).queue();
                return;
            }

            event.reply("You got the captcha right, " + ign).setEphemeral(true).queue();
        }

    }
}


