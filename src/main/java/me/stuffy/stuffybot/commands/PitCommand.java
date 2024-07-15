package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.profiles.HypixelProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.*;
import static me.stuffy.stuffybot.utils.MiscUtils.convertToRomanNumeral;
import static net.dv8tion.jda.api.interactions.components.buttons.Button.*;

public class PitCommand extends BaseCommand{
    private final Map<String, MessageEmbed> originalEmbeds = new HashMap<>();
    private final Map<String, MessageEmbed> originalDetailedEmbeds = new HashMap<>();
    private final Map<String, Event> activeEvents = new HashMap<>();

    public PitCommand(String name, String description) {
        super(name, description,
                new OptionData(OptionType.STRING, "ign", "Your Minecraft Username", false)
        );
    }

    @Override
    protected void onCommand(SlashCommandInteractionEvent event) {
        activeEvents.put(event.getHook().getId(), event);
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

        originalEmbeds.put(event.getHook().getId(), pitStats);

        event.getHook().sendMessage("")
                .addActionRow(
                        secondary("pitDetailed", "Challenge Achievement Progress")
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
        originalDetailedEmbeds.put(event.getHook().getId(), extraPitStats);
        System.out.println(originalEmbeds);
        System.out.println(originalDetailedEmbeds);
    }


    public void onButton(ButtonInteractionEvent event) {
        String[] parts = event.getComponentId().split(":");
        String command = parts[0];
        String action = parts[1];
        String userId = parts[2];

        if (action.equals("pitDetailed")) {
            MessageEmbed detailedButton = originalDetailedEmbeds.get(event.getHook().getId());
            if(detailedButton == null) {
                return;
            }
            event.editMessageEmbeds(detailedButton)
                    .setActionRow(secondary(command + ":go_back:" + userId, "Go Back"))
                    .queue();
        }
        if (action.equals("go_back")) {
            MessageEmbed backButton = originalEmbeds.get(event.getHook().getId());
            if(backButton == null) {
                return;
            }
            event.editMessageEmbeds(backButton)
                    .setActionRow(secondary(command + ":pitDetailed:" + userId, "Challenge Achievement Progress"))
                    .queue();
        }
    }

    @Override
    protected void cleanupEventResources(String messageId) {
        originalEmbeds.remove(messageId);
        originalDetailedEmbeds.remove(messageId);
        Event event = activeEvents.remove(messageId);
        if (event instanceof SlashCommandInteractionEvent) {
            SlashCommandInteractionEvent slashEvent = (SlashCommandInteractionEvent) event;
            slashEvent.getHook().retrieveOriginal().queue(message -> {
                message.editMessageComponents().queue();
            });
        }
    }
}
