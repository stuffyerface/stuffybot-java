package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.profiles.HypixelProfile;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.*;

public class MegaWallsCommand extends BaseCommand{
    private final Map<String, Event> activeEvents = new HashMap<>();
    public MegaWallsCommand(String name, String description) {
        super(name, description,
                new OptionData(OptionType.STRING, "ign", "The username of the player you want to look up", false));
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
        Map<String, Boolean> legendary_skins = hypixelProfile.getMegaWallsLegendaries();
        int legendary_skins_unlocked = 0;
        for (Boolean unlocked : legendary_skins.values()){
            if (unlocked){
                legendary_skins_unlocked++;
            }
        }

        Integer wins = hypixelProfile.getMegaWallsWins();
        Integer finalKills = hypixelProfile.getMegaWallsFinalKills();
        Integer classPoints = hypixelProfile.getMegaWallsClassPoints();
        String className = hypixelProfile.getMegaWallsSelectedClass();

        DecimalFormat df = new DecimalFormat("#,###");
        event.getHook().sendMessage("").addEmbeds(
                makeStatsEmbed(
                        "Mega Walls Stats for " + username,
                        "Wins: **" + df.format(wins) + "**\n" +
                                "Final Kills: **" + df.format(finalKills) + "**\n" +
                                "Total Class Points: **" + df.format(classPoints) + "**\n\n" +
                                "Selected Class: **" + className + "**\n" +
                                "Legendary Skins Unlocked: **" + legendary_skins_unlocked + "**/27"

                )
        ).addActionRow(
                StringSelectMenu.create("megawalls-select")
                        .addOption("Legendaries", "legendaries")
                        .addOptions(
                SelectOption.of("Overall", "overall").withDefault(true)
                        ).build()
        ).queue();

    }

    @Override
    protected void onButton(ButtonInteractionEvent event) {

    }

    @Override
    protected void cleanupEventResources(String messageId) {
        Event event = activeEvents.remove(messageId);
        if (event instanceof SlashCommandInteractionEvent slashEvent) {
            slashEvent.getHook().retrieveOriginal().queue(message -> {
                message.editMessageComponents().queue();
            });
        }
    }
}
