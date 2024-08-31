package me.stuffy.stuffybot.commands;

import kotlin.Triple;
import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.utils.APIException;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.text.DecimalFormat;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;
import static me.stuffy.stuffybot.utils.MiscUtils.convertToRomanNumeral;
import static net.dv8tion.jda.api.interactions.components.buttons.Button.secondary;

public class PitCommand {

    public static MessageCreateData pit(InteractionId interactionId) throws APIException {
        String ign = interactionId.getOptions().get("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();

        String pitPrestige = convertToRomanNumeral(hypixelProfile.getPit("prestige"));

        Long totalPitXp = hypixelProfile.getPitXP();

        Integer pitLevel = hypixelProfile.getPit("level");
        Integer pitRenown = hypixelProfile.getPit("renown");
        Integer pitGold = hypixelProfile.getPit("gold");
        Integer pitTotalGold = hypixelProfile.getPit("total_gold");

        DecimalFormat df = new DecimalFormat("#,###");

        String embedContent =
                "Prestige: [**" + pitPrestige + "**-**" + pitLevel + "**]\n" +
                "XP: **" + df.format(totalPitXp) + "**\n\n" +
                "Renown: **" + df.format(pitRenown) + "**\n" +
                "Gold|Total Gold: **" + df.format(pitGold) + "** | " + df.format(pitTotalGold) + "\n";


        MessageEmbed pitStats = makeStatsEmbed(
                "Pit Stats for " + username,
                embedContent
        );

        String newInteractionId = InteractionId.newCommand("pitDetailed", interactionId).getInteractionString();
        return new MessageCreateBuilder()
                .addEmbeds(pitStats)
                .addActionRow(
                        secondary(newInteractionId, "Challenge Achievement Progress")
                )
                .build();
    }

    public static MessageCreateData pitDetailed(InteractionId interactionId) throws APIException{
        String ign = interactionId.getOptions().get("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);

        Triple<String, Integer, Integer>[] challengeAchievements = new Triple[]{
                new Triple<>("Ingots Collector", hypixelProfile.getPit("ingots_collector"), 2000),
                new Triple<>("Golden Treat", hypixelProfile.getPit("golden_treat"), 1000),
                new Triple<>("Golden Age", hypixelProfile.getPit("golden_age"), 1000),
                new Triple<>("Infinite Quiver", hypixelProfile.getPit("infinite_quiver"), 1500),
                new Triple<>("Lucky Diamond!", hypixelProfile.getPit("lucky_diamond"), 50),
                new Triple<>("I'm Mining Here", hypixelProfile.getPit("im_mining_here"), 100),
                new Triple<>("Nosferatu", hypixelProfile.getPit("nosferatu"), 15000),
                new Triple<>("Raging Hunter", hypixelProfile.getPit("raging_hunter"), 100),
                new Triple<>("Bounty Hunter", hypixelProfile.getPit("bounty_hunter"), 30)
        };
        DecimalFormat df = new DecimalFormat("#,###");
        StringBuilder embedContent = new StringBuilder();
        for (Triple<String, Integer, Integer> challenge : challengeAchievements) {
            if (challenge.getSecond() >= challenge.getThird()) {
                embedContent.append("~~").append(challenge.getFirst()).append(": **").append(df.format(challenge.getSecond())).append("** / ").append(df.format(challenge.getThird())).append("~~\n");
            } else {
                String percentage = df.format((double) challenge.getSecond() / challenge.getThird() * 100);
                embedContent.append(challenge.getFirst()).append(": **").append(df.format(challenge.getSecond())).append("** / ").append(df.format(challenge.getThird())).append(" (").append(percentage).append("%)\n");
            }
        }

        MessageEmbed extraPitStats = makeStatsEmbed(
                "Pit Achievement stats for " + hypixelProfile.getDisplayName(),
                embedContent.toString()
        );

        String newInteractionId = InteractionId.newCommand("pit", interactionId).getInteractionString();
        return new MessageCreateBuilder()
                .addEmbeds(extraPitStats)
                .addActionRow(
                        secondary(newInteractionId, "Back to Pit Stats")
                )
                .build();
    }
}
