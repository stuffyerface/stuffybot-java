package me.stuffy.stuffybot.interactions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.stuffy.stuffybot.Bot;
import me.stuffy.stuffybot.profiles.GlobalData;
import me.stuffy.stuffybot.utils.*;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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
import static me.stuffy.stuffybot.utils.APIUtils.getPlayCommands;
import static me.stuffy.stuffybot.utils.APIUtils.getTournamentData;
import static me.stuffy.stuffybot.utils.DiscordUtils.*;
import static me.stuffy.stuffybot.utils.MiscUtils.autoCompleteAchGames;
import static me.stuffy.stuffybot.utils.MiscUtils.genBase64;

public class InteractionHandler extends ListenerAdapter {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();
    private final Map<String, Integer> tournamentMap = getTournamentMap();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        String id = genBase64(3);
        event.deferReply().queue();
        ArrayList<String> optionsArray = new ArrayList<String>();


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

        InteractionId interactionId = new InteractionId(id, commandName, event.getUser().getId(), optionsArray);

        Logger.log("<Command> @" + event.getUser().getName() + ": /" + commandName + " " + optionsArray.toString());

        GlobalData globalData = Bot.getGlobalData();
        globalData.incrementCommandsRun(event.getUser().getId(), commandName);
        globalData.addUniqueUser(event.getUser().getId(), event.getUser().getName());

        MessageCreateData response = null;
        try {
            response = getResponse(interactionId);
            ;
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

        if (response != null) {
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
                        "You can't use buttons on commands run by others.\n-# " + e.getMessage());
                event.getHook().sendMessageEmbeds(errorEmbed).setEphemeral(true).queue();
                return;
            }
        }

        if (interactionId.getCommand().equals("verify")) {
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
        if (event.getModalId().equals("verify")) {
            String ign = Objects.requireNonNull(event.getValue("ign")).getAsString();
            String captcha = Objects.requireNonNull(event.getValue("captcha")).getAsString();
            if (!captcha.equals("stuffy")) {
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
                    List<String> skins = Arrays.asList("Legendary", "Angel", "Arcanist", "Assassin", "Automaton", "Blaze", "Cow", "Creeper", "Dragon",
                            "Dreadlord", "Enderman", "Golem", "Herobrine", "Hunter", "Moleman", "Phoenix", "Pigman", "Pirate", "Renegade",
                            "Shaman", "Shark", "Sheep", "Skeleton", "Snowman", "Spider", "Squid", "Werewolf", "Zombie");
                    for (String skin : skins) {
                        if (skin.toLowerCase().contains(currentInput.toLowerCase())) {
                            options.add(skin);
                        }
                    }

                    String[] optionsArray = options.toArray(new String[0]);
                    if (optionsArray.length > 25) {
                        optionsArray = Arrays.copyOfRange(optionsArray, 0, 24);
                    }

                    List<Command.Choice> choices = Stream.of(optionsArray)
                            .map(option -> new Command.Choice(option, option.toLowerCase()))
                            .toList();

                    e.replyChoices(choices).queue();
                }
            }
            case "tournament" -> {
                if (commandOption.equals("tournament")) {
                    List<Command.Choice> choices = new ArrayList<>();
                    tournamentMap.forEach((name, id) -> {
                        if (name.toLowerCase().contains(currentInput.toLowerCase()) && choices.size() <= 25) {
                            choices.add(new Command.Choice(name, id));
                        }
                    });
                    e.replyChoices(choices).queue();
                }
            }
            case "playcommand" -> {
                if (commandOption.equals("game")) {
                    JsonElement gameData = getPlayCommands().getAsJsonObject().get("gameData");
                    if (gameData == null) {
                        e.replyChoices(Collections.emptyList()).queue();
                        break;
                    }

                    List<Command.Choice> choices = new ArrayList<>();
                    for (JsonElement entry : gameData.getAsJsonArray()) {
                        String gameName = entry.getAsJsonObject().get("name").getAsString();
                        JsonElement modes = entry.getAsJsonObject().get("modes");
                        if (modes == null) {
                            continue;
                        }

                        for (JsonElement modeEntry : modes.getAsJsonArray()) {
                            if (!modeEntry.getAsJsonObject().has("name") || !modeEntry.getAsJsonObject().has("identifier")) {
                                continue;
                            }
                            String modeName = modeEntry.getAsJsonObject().get("name").getAsString();
                            String fullGameName;
                            if (gameName.equals(modeName)) {
                                fullGameName = gameName;
                            } else {
                                fullGameName = gameName + ": " + modeName;
                            }
                            String identifier = modeEntry.getAsJsonObject().get("identifier").getAsString();
                            if (fullGameName.toLowerCase().contains(currentInput.toLowerCase())) {
                                choices.add(new Command.Choice(fullGameName, identifier));
                            }
                        }
                    }

                    if (choices.size() > 25) {
                        choices = choices.subList(0, 24);
                    }

                    e.replyChoices(choices).queue();
                }
            }
            case "achievements" -> {
                if (commandOption.equals("game")) {
                    Map<String, String> gameData = autoCompleteAchGames();
                    List<Command.Choice> choices = new ArrayList<>();
                    for (Map.Entry<String, String> entry : gameData.entrySet()) {
                        if (entry.getValue().toLowerCase().contains(currentInput.toLowerCase())) {
                            choices.add(new Command.Choice(entry.getValue(), entry.getValue()));
                        }
                    }

                    if (choices.size() > 25) {
                        choices = choices.subList(0, 24);
                    }

                    e.replyChoices(choices).queue();
                }
            }
            default -> {
                e.replyChoices(Collections.emptyList()).queue();
            }
        }
    }

    private Map<String, Integer> getTournamentMap() {
        Map<String, Integer> tournaments = new HashMap<>();
        JsonObject tournamentData = getTournamentData();
        for (JsonElement entry : tournamentData.getAsJsonArray("tournaments")) {
            JsonObject tournament = entry.getAsJsonObject();
            int id = tournament.get("id").getAsInt();
            String name = tournament.get("name").getAsString();
            int iteration = tournament.get("iteration").getAsInt();
            iteration++;

            tournaments.put(name + " #" + iteration, id);
        }


        return tournaments;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
//        String authorId = event.getAuthor().getId();
//        String authorName = event.getAuthor().getName();
//        Bot.getGlobalData().addUniqueUser(authorId, authorName);

        String message = event.getMessage().getContentRaw();
        if (message.toLowerCase().startsWith("ap!")) {
            Logger.logError("<LegacyCommand> @" + event.getAuthor().getName() + ": " + message);
            MessageCreateData data = new MessageCreateBuilder()
                    .addEmbeds(makeErrorEmbed("Outdated Command", "We no longer support chat based commands,\nInstead try using slash commands.\n-# Join our [Discord](https://discord.gg/8jdmT5Db3Y) for more info."))
                    .build();
            event.getMessage().reply(
                    data
            ).queue();
        }
    }
}


