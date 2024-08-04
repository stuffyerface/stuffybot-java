package me.stuffy.stuffybot.commands;

import kotlin.Triple;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.text.DecimalFormat;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.*;
import static me.stuffy.stuffybot.utils.MiscUtils.convertToRomanNumeral;
import static net.dv8tion.jda.api.interactions.components.buttons.Button.*;

public class PitCommand extends InteractionHandler {

//    public PitCommand(String name, String description) {
//        super(name, description,
//                new OptionData(OptionType.STRING, "ign", "Your Minecraft Username", false)
//        );
//    }

    protected void onCommand(SlashCommandInteractionEvent event) {
        String ign = getUsername(event);
        HypixelProfile hypixelProfile;
        try {
            hypixelProfile = getHypixelProfile(ign);
        }catch (Exception e){
            event.getHook().sendMessage("").addEmbeds(
                    makeErrorEmbed(
                            "API Error",
                            "An error occurred while fetching your Hypixel profile. Please try again later."
                    )
            ).queue();
            return;
        }

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


        String runnerId = event.getUser().getId();
        event.getHook().sendMessage("")
                .addActionRow(
                        secondary("pitDetailed:" + runnerId, "Challenge Achievement Progress")
                )
                .addEmbeds(
                        pitStats
        ).queue();


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

        StringBuilder embedContent2 = new StringBuilder();
        for (Triple<String, Integer, Integer> challenge : challengeAchievements) {
            if (challenge.getSecond() >= challenge.getThird()) {
                embedContent2.append("~~").append(challenge.getFirst()).append(": **").append(df.format(challenge.getSecond())).append("** / ").append(df.format(challenge.getThird())).append("~~\n");
            } else {
                String percentage = df.format((double) challenge.getSecond() / challenge.getThird() * 100);
                embedContent2.append(challenge.getFirst()).append(": **").append(df.format(challenge.getSecond())).append("** / ").append(df.format(challenge.getThird())).append(" (").append(percentage).append("%)\n");
            }
        }

        MessageEmbed extraPitStats = makeStatsEmbed(
                "Pit Achievement stats for " + username,
                embedContent2.toString()
        );
    }


    public void onButton(ButtonInteractionEvent event) {
//        String[] parts = event.getComponentId().split(":");
//        String action = parts[0];
//        String userId = parts[1];
//
//        if (action.equals("pitDetailed")) {
//            MessageEmbed detailedButton = null;
//            if(detailedButton == null) {
//                return;
//            }
//            event.editMessageEmbeds(detailedButton)
//                    .setActionRow(secondary("go_back:" + userId, "Go Back"))
//                    .queue();
//        }
//        if (action.equals("go_back")) {
//            MessageEmbed backButton = originalEmbeds.get(event.getHook().getId());
//            if(backButton == null) {
//                return;
//            }
//            event.editMessageEmbeds(backButton)
//                    .setActionRow(secondary("pitDetailed:" + userId, "Challenge Achievement Progress"))
//                    .queue();
//        }
    }
}
