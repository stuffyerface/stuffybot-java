package me.stuffy.stuffybot.commands;

import com.google.gson.JsonObject;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.profiles.MojangProfile;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.UUID;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.APIUtils.getMojangProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeErrorEmbed;

public class MaxedGamesCommand extends BaseCommand{

    public MaxedGamesCommand(String name, String description) {
        super(name, description);
    }
    @Override
    protected void onCommand(SlashCommandInteractionEvent event) {
        String ign = event.getOption("ign").getAsString(); // maybe a utility method to get the ign from the event
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
        hypixelProfile.getDiscord();
    }
}
