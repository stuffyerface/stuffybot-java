package me.stuffy.stuffybot.interactions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.*;
import java.util.stream.Stream;

import static me.stuffy.stuffybot.utils.APIUtils.*;
import static me.stuffy.stuffybot.utils.MiscUtils.autoCompleteAchGames;
import static me.stuffy.stuffybot.utils.MiscUtils.toReadableName;

public class AutoCompleteHandler extends ListenerAdapter {
    private final Map<String, Integer> tournamentMap = getTournamentMap();
    private final List<String> skinOptions = getSkinOptions();

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent e) {
        String commandName = e.getName();
        String subcommandName = e.getSubcommandName();

        String commandOption = e.getFocusedOption().getName();
        String currentInput = e.getFocusedOption().getValue();
        switch (commandName) {
            case "megawalls" -> {
                if (commandOption.equals("skins")) {
                    List<String> options = new ArrayList<>();
                    for (String skin : skinOptions) {
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
            case "search" -> {
                assert subcommandName != null;
                if (subcommandName.equals("achievement")) {
                    if (commandOption.equals("search")) {
                        int searchCount = 0;
                        JsonObject achievementsResources = getAchievementsResources().getAsJsonObject();
                        List<Command.Choice> choices = new ArrayList<>();

                        for (String game : achievementsResources.keySet()) {
                            if (searchCount == 25) {
                                break;
                            }
                            JsonObject gameAchievements = achievementsResources.get(game).getAsJsonObject();
                            JsonObject gameOneTime = gameAchievements.get("one_time").getAsJsonObject();
                            JsonObject gameTiered = gameAchievements.get("tiered").getAsJsonObject();
                            for (String oneTimeID : gameOneTime.keySet()) {
                                if (searchCount == 25) {
                                    break;
                                }
                                JsonObject oneTimeAchievement = gameOneTime.get(oneTimeID).getAsJsonObject();
                                String achievementName = oneTimeAchievement.get("name").getAsString();
                                String achievementDescription = oneTimeAchievement.get("description").getAsString();

                                if (achievementName.toLowerCase().contains(currentInput.toLowerCase()) || achievementDescription.toLowerCase().contains(currentInput.toLowerCase())) {
                                    choices.add(new Command.Choice(toReadableName(game) + ": " + achievementName, game.toUpperCase() + "_" + oneTimeID));
                                    searchCount++;
                                }
                            }

                            for (String tieredID : gameTiered.keySet()) {
                                if (searchCount == 25) {
                                    break;
                                }
                                JsonObject tieredAchievement = gameTiered.get(tieredID).getAsJsonObject();
                                String achievementName = tieredAchievement.get("name").getAsString();
                                String achievementDescription = tieredAchievement.get("description").getAsString();

                                if (achievementName.toLowerCase().contains(currentInput.toLowerCase()) || achievementDescription.toLowerCase().contains(currentInput.toLowerCase())) {
                                    choices.add(new Command.Choice(toReadableName(game) + ": " + achievementName, game.toUpperCase() + "_" + tieredID));
                                    searchCount++;
                                }
                            }
                        }
                        e.replyChoices(choices).queue();
                    }
                } else if (subcommandName.equals("random")) {
                    if (commandOption.equals("game")) {
                        Map<String, String> gameData = autoCompleteAchGames();
                        List<Command.Choice> choices = new ArrayList<>();
                        for (Map.Entry<String, String> entry : gameData.entrySet()) {
                            if (entry.getValue().toLowerCase().contains(currentInput.toLowerCase())) {
                                choices.add(new Command.Choice(entry.getValue(), entry.getKey()));
                            }
                        }
                        choices.sort(Comparator.comparing(Command.Choice::getName));
                        if (choices.size() > 25) {
                            choices = choices.subList(0, 24);
                        }

                        e.replyChoices(choices).queue();
                    }
                }
            }
            default -> {
                e.replyChoices(Collections.emptyList()).queue();
            }
        }
    }


    @SuppressWarnings("SpellCheckingInspection")
    private List<String> getSkinOptions() {
        return Arrays.asList("Legendary", "Angel", "Arcanist", "Assassin", "Automaton", "Blaze", "Cow", "Creeper", "Dragon",
                "Dreadlord", "Enderman", "Golem", "Herobrine", "Hunter", "Moleman", "Phoenix", "Pigman", "Pirate", "Renegade",
                "Shaman", "Shark", "Sheep", "Skeleton", "Snowman", "Spider", "Squid", "Werewolf", "Zombie");
    }

    private Map<String, Integer> getTournamentMap() {
        Map<String, Integer> tournaments = new HashMap<>();
        JsonObject tournamentData = getTournamentData();
        assert tournamentData != null;
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
}
