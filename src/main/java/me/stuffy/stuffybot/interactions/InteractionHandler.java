package me.stuffy.stuffybot.interactions;

import me.stuffy.stuffybot.utils.InteractionException;
import me.stuffy.stuffybot.utils.Logger;
import me.stuffy.stuffybot.utils.StatisticsManager;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static me.stuffy.stuffybot.interactions.InteractionManager.getResponse;
import static me.stuffy.stuffybot.utils.DiscordUtils.*;
import static me.stuffy.stuffybot.utils.MiscUtils.*;

public class InteractionHandler extends ListenerAdapter {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        String id = genBase64(3);
        event.deferReply().queue();
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

        InteractionId interactionId = new InteractionId(id, commandName, event.getUser().getId(), optionsArray);

        Logger.log("<Command> @" + event.getUser().getName() + ": /" + commandName + " " + optionsArray.toString());

        MessageCreateData response = null;
        try {
            response = getResponse(interactionId);;
        } catch (InteractionException e) {
            MessageEmbed errorEmbed = makeErrorEmbed("Slash Command Error", "An error occurred while processing your command.\n-# " + e.getMessage());
            event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
            return;
        } catch (Exception e) {
            MessageEmbed errorEmbed = makeErrorEmbed("Unknown Error", "Uh Oh! I have no idea what went wrong, report this.\n-# Everybody makes mistakes.");
            Logger.logError("Unknown error in command: " + commandName + " " + optionsArray.toString() + " " + e.getMessage());
            e.printStackTrace();
            event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
            return;
        }

        if(response != null) {
            event.getHook().sendMessage(response).queue();
        }

        StatisticsManager.incrementCommandUsage(commandName);
        String uid = interactionId.getId();
        InteractionHook hook = event.getHook();
        ScheduledFuture<?> scheduledFuture = scheduler.schedule(() -> {
            hook.editOriginalComponents().queue();
        }, 30, TimeUnit.SECONDS);

        scheduledTasks.put(uid, scheduledFuture);
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

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent e) {
        String commandName = e.getName();
        String commandOption = e.getFocusedOption().getName();
        String currentInput = e.getFocusedOption().getValue();

        switch (commandName) {
            case "megawalls" -> {
                if (commandOption.equals("skins")) {
                    List<String> options = new ArrayList<>();
                    if(currentInput.equals("")){
                        options.add("All");
                        options.add("Legendary");
                        options.add("Other (Begin Typing)");
                    } else {
                        List<String> skins = Arrays.asList("Legendary", "Angel", "Arcanist", "Assassin", "Automaton", "Blaze", "Cow", "Creeper", "Dragon",
                                "Dreadlord", "Enderman", "Golem", "Herobrine", "Hunter", "Moleman", "Phoenix", "Pigman", "Pirate", "Renegade",
                                "Shaman", "Shark", "Sheep", "Skeleton", "Snowman", "Spider", "Squid", "Werewolf", "Zombie");
                        for (String skin : skins) {
                            if (skin.toLowerCase().startsWith(currentInput.toLowerCase())) {
                                options.add(skin);
                            }
                        }
                    }

                    String[] optionsArray = options.toArray(new String[0]);
                    if (optionsArray.length > 25) {
                        optionsArray = Arrays.copyOfRange(optionsArray, 0, 25);
                    }

                    List<Command.Choice> choices = Stream.of(optionsArray)
                            .map(option -> new Command.Choice(option, option.toLowerCase()))
                            .toList();

                    e.replyChoices(choices).queue();
                    return;
                }
            }
        }
    }
}


