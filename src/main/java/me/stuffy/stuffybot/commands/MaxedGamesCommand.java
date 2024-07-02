package me.stuffy.stuffybot.commands;

import com.google.gson.JsonObject;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.profiles.MojangProfile;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.UUID;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.APIUtils.getMojangProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.getUsername;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeErrorEmbed;

public class MaxedGamesCommand extends BaseCommand{

    public MaxedGamesCommand(String name, String description) {
        super(name, description,
                new OptionData(OptionType.STRING, "ign", "Your Minecraft Username", false)
        );
    }
    @Override
    protected void onCommand(SlashCommandInteractionEvent event) {
        String ign = getUsername(event);

        MojangProfile profile;
        try {
            profile = getMojangProfile(ign);
        } catch (Exception e) {
            event.getHook().sendMessage("").addEmbeds(
                    makeErrorEmbed(
                            "Mojang API Error",
                            "Error interacting with Mojang API. Make sure you spelled the username correctly."
                    )
            ).queue();

            return;
        }
        UUID uuid = profile.getUuid();
        HypixelProfile hypixelProfile = getHypixelProfile(uuid);
        hypixelProfile.getMaxedGames();
    }
}
