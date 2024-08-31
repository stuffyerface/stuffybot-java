package me.stuffy.stuffybot.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.utils.APIException;
import me.stuffy.stuffybot.utils.InvalidOptionException;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.Collection;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.APIUtils.getTournamentData;
import static me.stuffy.stuffybot.utils.DiscordUtils.discordTimeUnix;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;
import static me.stuffy.stuffybot.utils.MiscUtils.minutesFormatted;

public class TournamentCommand {

    public static MessageCreateData tournament(InteractionId interactionId) throws APIException, InvalidOptionException {
        String ign = interactionId.getOption("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();

        int tournamentId = interactionId.getOption("tournament", -1);

        JsonObject tournamentData = getTournamentData();
        if (tournamentData == null) {
            throw new APIException("Stuffy", "Failed to load tournament data.");
        }

        JsonObject selectedTournament = null;
        int highestId = -1;
        for (JsonElement entry : tournamentData.getAsJsonArray("tournaments")) {
            JsonObject tournament = entry.getAsJsonObject();
            int id = tournament.get("id").getAsInt();
            if (id > highestId) {
                highestId = id;
                if (tournamentId == -1) {
                    selectedTournament = tournament;
                }
            }
            if (id == tournamentId) {
                selectedTournament = tournament;
            }
        }
        if (tournamentId == -1) {
            tournamentId = highestId;
        }

        if (selectedTournament == null) {
            throw new InvalidOptionException("tournament", "Invalid tournament ID.");
        }

        String tournamentName = selectedTournament.get("name").getAsString();
        int tournamentIteration = selectedTournament.get("iteration").getAsInt();
        JsonObject tournamentDuration = selectedTournament.get("duration").getAsJsonObject();
        long tournamentStartTime = tournamentDuration.get("start").getAsLong();
        long tournamentEndTime = tournamentDuration.get("end").getAsLong();
        long currentTime = System.currentTimeMillis() / 1000;

        JsonObject selectedTournamentData = selectedTournament.get("data").getAsJsonObject();
        String tournamentField = selectedTournamentData.get("tourneyField").getAsString();

        int tributes = hypixelProfile.getTourneyTributesEarned(tournamentField);
        int playtime = hypixelProfile.getTourneyTimePlayed(tournamentField);

        StringBuilder title = new StringBuilder();
        title.append(tournamentName);
        if (tournamentIteration != 0) {
            title.append(" #").append(tournamentIteration + 1);
        }

        String subtitle = " From " + discordTimeUnix(tournamentStartTime * 1000) + " to " +
                discordTimeUnix(tournamentEndTime * 1000) + "\n-# ID:" + tournamentId;

        StringBuilder description = new StringBuilder();
        description.append("### **").append(username).append("**'s Tournament Stats\n");
        if(selectedTournamentData.has("gameLimit")){
            int gameLimit = selectedTournamentData.get("gameLimit").getAsInt();
            int gamesPlayed = hypixelProfile.getTourneyGamesPlayed(tournamentField);
            description.append(gamesPlayed).append("/").append(gameLimit).append(" Games Played\n");
        } else {
            int timeLimit = selectedTournamentData.get("timeLimit").getAsInt();
            description.append("Time Played: ").append(minutesFormatted(playtime)).append("/").append(minutesFormatted(timeLimit)).append("\n");
        }
        description.append(tributes).append("/100 Tributes Earned\n");
        description.append("Playtime: ").append(minutesFormatted(playtime)).append("\n");
        description.append("\n");

        if (selectedTournamentData.has("wins")) {
            String winsField = selectedTournamentData.get("wins").getAsString();
            description.append("Wins: ").append(hypixelProfile.getStat(winsField));
            if (selectedTournamentData.has("losses")) {
                String lossesField = selectedTournamentData.get("losses").getAsString();
                description.append(", Losses: ").append(hypixelProfile.getStat(lossesField)).append("\n");
            } else {
                description.append("\n");
            }
            if (selectedTournamentData.has("winstreak")) {
                description.append("Win Streak: ").append(hypixelProfile.getStat(selectedTournamentData.get("winstreak").getAsString())).append("\n");
            }
        }

        description.append("\n");

        if (selectedTournamentData.has("kills")) {
            description.append("Kills: ").append(hypixelProfile.getStat(selectedTournamentData.get("kills").getAsString()));
            if (selectedTournamentData.has("killstreak")) {
                description.append(", Kill Streak: ").append(hypixelProfile.getStat(selectedTournamentData.get("killstreak").getAsString())).append("\n");
            } else {
                description.append("\n");
            }
        }

        if (selectedTournamentData.has("assists")) {
            description.append("Assists: ").append(hypixelProfile.getStat(selectedTournamentData.get("assists").getAsString())).append("\n");
        }
        if (selectedTournamentData.has("deaths")) {
            description.append("Deaths: ").append(hypixelProfile.getStat(selectedTournamentData.get("deaths").getAsString())).append("\n");
        }


        String future = ":date:";
        String past = ":checkered_flag:";
        String active = ":video_game:";

        String emoji;

        if (currentTime < tournamentStartTime) {
            emoji = future;
        } else if (currentTime > tournamentEndTime) {
            emoji = past;
        } else {
            emoji = active;
        }

        Collection<Button> buttons = new ArrayList<>();
        Button previousButton = Button.of(ButtonStyle.PRIMARY, interactionId.setOption("tournament", tournamentId - 1).getInteractionString(), "Previous Tournament");
        if (tournamentId <= 0) {
            previousButton = previousButton.asDisabled();
        }
        buttons.add(previousButton);
        Button nextButton = Button.of(ButtonStyle.PRIMARY, interactionId.setOption("tournament", tournamentId + 1).getInteractionString(), "Next Tournament");
        if (tournamentId >= highestId) {
            nextButton = nextButton.asDisabled();
        }
        buttons.add(nextButton);

        return new MessageCreateBuilder()
                .addEmbeds(makeStatsEmbed(emoji + " " + title.toString(), subtitle, description.toString()))
                .addActionRow(buttons)
                .build();
    }
}
