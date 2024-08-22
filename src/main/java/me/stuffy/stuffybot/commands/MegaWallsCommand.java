package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.utils.APIException;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.text.DecimalFormat;
import java.util.Map;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;
import static net.dv8tion.jda.api.interactions.components.buttons.Button.secondary;

public class MegaWallsCommand {

    public static MessageCreateData megawalls(InteractionId interactionId) throws APIException {
        String ign = interactionId.getOption("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();

        String mwClass = interactionId.getOption("skins", "all");
        Map<String, Boolean> legendary_skins = hypixelProfile.getMegaWallsLegendaries();
        int legendary_skins_unlocked = 0;
        DecimalFormat dfPercent = new DecimalFormat("##.##");
        DecimalFormat dfComma = new DecimalFormat("#,###");

        switch (mwClass) {
            case "legendary":
                StringBuilder legendarySkins = new StringBuilder();
                for (Map.Entry<String, Boolean> entry : legendary_skins.entrySet()) {
                    if (entry.getValue()) {
                        legendarySkins.append("✅ ");
                        legendary_skins_unlocked++;
                    } else {
                        legendarySkins.append("❌ ");
                    }
                    legendarySkins.append(entry.getKey()).append("\n");
                }

                MessageEmbed legendarySkinsEmbed = makeStatsEmbed(
                        "Legendary Skins for " + username,
                        "Unlocked: **" + legendary_skins_unlocked + "**/27 (" + dfPercent.format(legendary_skins_unlocked/27.0*100) + "%)",
                        legendarySkins.toString()
                );

                return new MessageCreateBuilder()
                        .addEmbeds(legendarySkinsEmbed)
                        .build();
            default:
                Integer wins = hypixelProfile.getMegaWallsWins();
                Integer finalKills = hypixelProfile.getMegaWallsFinalKills();
                Integer classPoints = hypixelProfile.getMegaWallsClassPoints();
                String className = hypixelProfile.getMegaWallsSelectedClass();

                for (Boolean unlocked : legendary_skins.values()) {
                    if (unlocked) {
                        legendary_skins_unlocked++;
                    }
                }

                MessageEmbed megawallsStats = makeStatsEmbed(
                        "Mega Walls Stats for " + username,
                        "Wins: **" + dfComma.format(wins) + "**\n" +
                                "Final Kills: **" + dfComma.format(finalKills) + "**\n" +
                                "Total Class Points: **" + dfComma.format(classPoints) + "**\n\n" +
                                "Selected Class: **" + className + "**\n" +
                                "Legendary Skins Unlocked: **" + legendary_skins_unlocked + "**/27"
                );

                return new MessageCreateBuilder()
                        .addEmbeds(megawallsStats)
                        .addActionRow(
                                secondary(interactionId.setOption("skins", "legendary").getInteractionString(), "See Legendary Skins")
                        )
                        .build();
        }
    }
}
