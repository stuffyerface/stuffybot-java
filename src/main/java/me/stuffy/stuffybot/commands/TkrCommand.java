package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.profiles.HypixelProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Map;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.*;

public class TkrCommand extends BaseCommand{
    public TkrCommand(String name, String description) {
        super(name, description,
                new OptionData(OptionType.STRING, "ign", "The username of the player you want to look up", false));
    }

    @Override
    protected void onCommand(SlashCommandInteractionEvent event) {
        String ign = getUsername(event);
        HypixelProfile hypixelProfile;
        try{
            hypixelProfile = getHypixelProfile(ign);
        } catch (Exception e){
            event.getHook().sendMessage("").addEmbeds(
                    makeErrorEmbed(
                            "API Error",
                            "An error occurred while fetching your Hypixel profile. Please try again later."
                    )
            ).queue();
            return;
        }

        String username = hypixelProfile.getDisplayName();

        Map<String, Boolean> uniqueGolds = hypixelProfile.getTkrMaps();
        String embedContent = "";
        int uniqueGoldCount = 0;

        for (Map.Entry<String, Boolean> entry : uniqueGolds.entrySet()) {
            String key = entry.getKey();
            Boolean value = entry.getValue();
            if (value) {
                uniqueGoldCount++;
            }
            embedContent += (value ? "✅ " : "❌ ") + key + "\n";
        }


        embedContent = "Unique Gold Medals: **" + uniqueGoldCount + "**/5\n\n" + embedContent;


        event.getHook().sendMessage("").addEmbeds(
                makeStatsEmbed(
                        "TKR Unique Gold Medals for " + username,
                        embedContent
                )
        ).queue();
    }

    @Override
    protected void onButton(ButtonInteractionEvent event) {

    }

    @Override
    protected void cleanupEventResources(String messageId) {

    }
}
