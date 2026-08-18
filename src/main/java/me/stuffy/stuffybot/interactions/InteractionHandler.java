package me.stuffy.stuffybot.interactions;

import me.stuffy.stuffybot.Bot;
import me.stuffy.stuffybot.profiles.GlobalData;
import me.stuffy.stuffybot.utils.*;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static me.stuffy.stuffybot.commands.SetupCommand.setupLinkingButton;
import static me.stuffy.stuffybot.interactions.InteractionManager.getResponse;
import static me.stuffy.stuffybot.utils.DiscordUtils.*;
import static me.stuffy.stuffybot.utils.MiscUtils.*;
import static me.stuffy.stuffybot.utils.Verification.verifyModal;

public class InteractionHandler extends ListenerAdapter {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        String subcommandName = event.getSubcommandName();
        if (subcommandName == null) {
            subcommandName = "";
        }
        String id = genBase64(5);
        event.deferReply().queue();
        ArrayList<String> optionsArray = new ArrayList<String>();

        if (commandName.equals("setup")) {
            String toSetup = event.getOption("to_setup").getAsString();
            if (toSetup.equals("verify")) {
                setupLinkingButton(event);
                MessageEmbed successEmbed = makeEmbed("Verification Setup", "Successful setup", "The Verify Embed has been setup successfully.", 0x3d84a2);
                event.getHook().setEphemeral(true).sendMessageEmbeds(successEmbed).queue();
                return;
            }
        }


        if (event.getOption("ign") == null) {
            String ign = null;
            try {
                ign = getUsername(event);
            } catch (APIException e) {
                event.getHook().sendMessageEmbeds(makeErrorEmbed(e.getAPIType() + " API Error", e.getMessage())).setEphemeral(true).queue();
            }
            optionsArray.add("ign=" + ign);
        }

        Pattern pattern = Pattern.compile("[,=:]");
        for (OptionMapping option : event.getOptions()) {
            String optionString = option.getAsString();
            if (pattern.matcher(optionString).find()) {
                MessageEmbed errorEmbed = makeErrorEmbed("Slash Command Error", "An error occurred while processing your command.\n-# Invalid character in option  `" + option.getName() + "`");
                event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
                return;
            }
            optionsArray.add(option.getName() + "=" + optionString);
        }

        InteractionId interactionId = new InteractionId(id, commandName, subcommandName, event.getUser().getId(), optionsArray);

        StringBuilder commandLog = new StringBuilder();
        commandLog.append("<");
        if (event.getIntegrationOwners().isUserIntegration()) {
            commandLog.append("User");
        }
        commandLog.append("Command> @").append(event.getUser().getName());
        commandLog.append(": /").append(commandName);
        if(!subcommandName.isEmpty()) {
            commandLog.append(" ").append(subcommandName);
        }
        if (!optionsArray.isEmpty()) {
            commandLog.append(" ").append(optionsArray);
        }
        Logger.log(commandLog.toString());

        GlobalData globalData = Bot.getGlobalData();
        globalData.incrementCommandsRun(event.getUser().getId(), commandName);
        globalData.addUniqueUser(event.getUser().getId(), event.getUser().getName());

        MessageCreateData response;
        try {
            response = getResponse(interactionId);
        } catch (InteractionException e) {
            MessageEmbed errorEmbed = makeErrorEmbed("Slash Command Error", "An error occurred while processing your command.\n-# " + e.getMessage());
            event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
            return;
        } catch (Exception e) {
            MessageEmbed errorEmbed = makeErrorEmbed("Unknown Error", "Uh Oh! I have no idea what went wrong, report this.\n-# Everybody makes mistakes.");
            Logger.logError("Unknown error in command: " + commandName + " " + optionsArray + " " + e.getMessage());
            e.printStackTrace();
            event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
            return;
        }

        if (response != null) {
            event.getHook().sendMessage(response).queue();
        }

        StatisticsManager.incrementCommandUsage(commandName);
        String uid = interactionId.getId();
        InteractionHook hook = event.getHook();
        ScheduledFuture<?> removeComponents = scheduler.schedule(() -> {
            try {
                if(hook.retrieveOriginal().complete().getComponents().isEmpty()) {
                    return;
                }
                hook.editOriginalComponents().queue();
            } catch (Exception e) {
                Logger.logError("Unable to remove original components, the message may have been deleted.");
            }
        }, 30, TimeUnit.SECONDS);

        scheduledTasks.put(uid, removeComponents);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        InteractionId interactionId;
        try {
            interactionId = new InteractionId(event.getComponentId());
        } catch (Exception e) {
            event.deferEdit().queue();
            MessageEmbed errorEmbed = makeErrorEmbed("Button Interaction Error",
                    "An error occurred while processing your button press.\n-# You pressed an imaginary button");
//                    "You can't fool me, this is gibberish",
//                    "This is not the interaction you are looking for",
//                    "*Someone* messed up the interactionId, and it wasn't me",
//                    "I'm sorry, Dave. I'm afraid I can't do that",
//                    "You can't fool me, that button doesn't exist",
//                    "So, we're just making up our own buttons now?",
//                    "How did you even get here?"
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
                        "You can't use buttons on commands run by others.\n-# " + e.getMessage());
                event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
                return;
            }
        }

        // Verify Button
        switch (interactionId.getCommand()) {
            case "verify" -> {
                Verification.verifyButton(event);
                return;
            }


            // Update Button
            case "update" -> {
                MessageCreateData data = new MessageCreateBuilder()
                        .setEmbeds(makeErrorEmbed("OOPS!", "This feature is currently unavailable in preparation for an overhaul.")).build();
                event.reply(data).setEphemeral(true).queue();

                return;
            }


            // Unverify Button
            case "unverify" -> {
                Verification.unverifyButton(event);
                return;
            }
        }

        event.deferEdit().queue();
        MessageCreateData data;
        try {
            data = getResponse(interactionId);
        } catch (InteractionException e) {
            MessageEmbed errorEmbed = makeErrorEmbed("Button Interaction Error", "An error occurred while processing your button press.\n-# " + e.getMessage());
            event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
            return;
        }


        if (data == null) {
            // When the button press does not require a response
            return;
        }

        InteractionHook hook = event.getHook();
        ScheduledFuture<?> scheduledFuture = scheduledTasks.get(interactionId.getId());
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        } else {
            Logger.logError("ScheduledFuture not found for button press '" + interactionId.getId() + "', was the bot restarted?");
        }

        ScheduledFuture<?> newScheduledFuture = scheduler.schedule(() -> {
            hook.editOriginalComponents().queue();
        }, 30, TimeUnit.SECONDS);
        scheduledTasks.put(interactionId.getId(), newScheduledFuture);

        MessageEditData editData = MessageEditData.fromCreateData(data);
        hook.editOriginal(editData).queue();
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String toLog = "<Modal> @" + event.getUser().getName() + ": `" + event.getModalId() + "`";
        for (ModalMapping mapping : event.getValues()) {
            toLog += " `" + mapping.getCustomId() + "=" + mapping.getAsString() + "`";
        }
        Logger.log(toLog);

        if (event.getModalId().equals("verify")) {
            verifyModal(event);
        }
    }
}


