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


    public PitCommand(String name, String description) {
        super(name, description,
                new OptionData(OptionType.STRING, "ign", "Your Minecraft Username", false)
        );
    }

    @Override
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

        originalEmbeds.put(event.getHook().getId(), pitStats);

        event.getHook().sendMessage("")
                .addActionRow(
                        secondary("pitDetailed", "Challenge Achievement Progress")
                )
                .addEmbeds(
                        pitStats
        ).queue();

    }


    public void onButton(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("pitDetailed")) {
            event.editMessageEmbeds(
                    makeStatsEmbed("Pit Stats for Stuffy","test2"))
                    .setActionRow(secondary("go back", "Go Back"))
                    .queue();
        }
        if (event.getComponentId().equals("go back")) {
            MessageEmbed backButton = originalEmbeds.get(event.getHook().getId());
            if(backButton == null) {
                return;
            }
            event.editMessageEmbeds(backButton)
                    .setActionRow(secondary("pitDetailed", "Challenge Achievement Progress"))
                    .queue();
        }
    }
}
