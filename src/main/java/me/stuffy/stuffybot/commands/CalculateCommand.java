package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.utils.MiscUtils;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.text.DecimalFormat;

import static me.stuffy.stuffybot.utils.DiscordUtils.makeErrorEmbed;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;

public class CalculateCommand {
    public static MessageCreateData calcPit(InteractionId interactionId) {
        int prestige = interactionId.getOption("prestige", 0);
        int startingLevel = interactionId.getOption("starting_level", 0);
        int endingLevel = interactionId.getOption("ending_level", 120);

        if (prestige < 0 || prestige > 50) {
            return new MessageCreateBuilder()
                    .addEmbeds(makeErrorEmbed("Invalid Prestige", "Prestige must be a number between 0 and 50"))
                    .build();
        }
        if (startingLevel < 0 || startingLevel > 120) {
            return new MessageCreateBuilder()
                    .addEmbeds(makeErrorEmbed("Invalid Starting Level", "Starting level must be a number between 0 and 120"))
                    .build();
        }
        if (endingLevel < 0 || endingLevel > 120) {
            return new MessageCreateBuilder()
                    .addEmbeds(makeErrorEmbed("Invalid Ending Level", "Ending level must be a number between 0 and 120"))
                    .build();
        }
        if (startingLevel >= endingLevel) {
            return new MessageCreateBuilder()
                    .addEmbeds(makeErrorEmbed("Invalid Levels", "Starting level must be less than ending level"))
                    .build();
        }

        String fromLevel = "[**" + MiscUtils.convertToRomanNumeral(prestige) + "**-**" + startingLevel + "**]";
        String toLevel = "[**" + MiscUtils.convertToRomanNumeral(prestige) + "**-**" + endingLevel + "**]";

        DecimalFormat df = new DecimalFormat("#,###");

        MessageEmbed returnValue = makeStatsEmbed(
                "Pit XP Calculation",
                fromLevel + " -> " + toLevel + "\nTotal : **" + df.format(MiscUtils.calcPitXp(prestige, startingLevel, endingLevel)) + "** XP"
        );

        return new MessageCreateBuilder()
                .addEmbeds(returnValue)
                .build();
    }
}
