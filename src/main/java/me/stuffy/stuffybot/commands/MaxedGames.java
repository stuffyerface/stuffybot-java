package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.profiles.HypixelProfile;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.getUsername;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeErrorEmbed;

public class MaxedGames extends BaseCommand{

    public MaxedGames(String name, String description) {
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
        hypixelProfile.getMaxedGames();
    }
}
