package me.stuffy.stuffybot.interactions;

import me.stuffy.stuffybot.commands.*;
import me.stuffy.stuffybot.utils.APIException;
import me.stuffy.stuffybot.utils.InteractionException;
import me.stuffy.stuffybot.utils.InvalidOptionException;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.HashMap;
import java.util.regex.Pattern;

import static me.stuffy.stuffybot.utils.DiscordUtils.makeErrorEmbed;

public class InteractionManager {

    public static MessageCreateData getResponse(InteractionId interactionId) throws InteractionException {

        String command = interactionId.getCommand();
        HashMap<String, String> options = interactionId.getOptions();

        if(options.containsKey("ign")){
            String ign = options.get("ign");
            if (ign.length() > 16){
                throw new InteractionException("Invalid field `ign`, Usernames cannot be longer than 16 characters");
            }
            Pattern pattern = Pattern.compile("[^a-zA-Z0-9_]");
            if (pattern.matcher(ign).find()){
                throw new InteractionException("Invalid field `ign`, Usernames can only contain " +
                        "letters, numbers and underscores");
            }
        }

        try {
            return switch (command) {
                case "pit" -> PitCommand.pit(interactionId);
                case "pitDetailed" -> PitCommand.pitDetailed(interactionId);
                case "stats" -> StatsCommand.stats(interactionId);
                case "tkr" -> TkrCommand.tkr(interactionId);
                case "maxes" -> MaxesCommand.maxes(interactionId);
                case "blitz" -> BlitzCommand.blitz(interactionId);
                case "megawalls" -> MegaWallsCommand.megawalls(interactionId);
                case "tournament" -> TournamentCommand.tournament(interactionId);
                case "achievements" -> AchievementsCommand.achievements(interactionId);
                case "link" -> LinkCommand.link(interactionId);
                default -> throw new InteractionException("Invalid command");
            };
        } catch (APIException e) {
            return new MessageCreateBuilder()
                    .addEmbeds(makeErrorEmbed(e.getAPIType() + " API Error", e.getMessage()))
                    .build();
        } catch (InvalidOptionException e) {
            return new MessageCreateBuilder()
                    .addEmbeds(makeErrorEmbed("Invalid Option", e.getMessage()))
                    .build();
        }
    }
}
