package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.profiles.MojangProfile;
import me.stuffy.stuffybot.utils.Logger;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.UUID;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.APIUtils.getMojangProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.*;

public class Verify extends BaseCommand {

    public Verify(String name, String description) {
        super(name, description,
                new OptionData(OptionType.STRING, "ign", "Your Minecraft Username", true)
        );
    }

    /**
     * Verifies a user's Minecraft account is linked to the Discord account that sent the command via Hypixel.
     * @param event
     */
    @Override
    protected void onCommand(SlashCommandInteractionEvent event) {
        String ign = event.getOption("ign").getAsString();
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

        HypixelProfile hypixelProfile;
        try {
            hypixelProfile = getHypixelProfile(uuid);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            event.getHook().sendMessage("").addEmbeds(
                    makeErrorEmbed(
                            "Hypixel API Error",
                            "Error interacting with Hypixel API. Try again later."
                    )
            ).queue();

            return;
        }
        String linkedDiscord = hypixelProfile.getDiscord();
        String runnerUsername = getDiscordUsername(event.getUser().getName());
        if (linkedDiscord == null) {
            event.getHook().sendMessage("").addEmbeds(
                    makeErrorEmbed(
                            "Username Error",
                            "There is no discord account linked to the Minecraft account `" + profile.getUsername() + "`."
                    )
            ).queue();
            return;
        }

        if (!linkedDiscord.equals(runnerUsername)) {
            event.getHook().sendMessage("").addEmbeds(
                    makeErrorEmbed(
                            "Username Error",
                            "The ign you provided is linked to a different discord account than the one you're using.\n\nYour discord account: `" + runnerUsername + "`\n`" + profile.getUsername() + "`'s Linked discord account: `" + linkedDiscord + "`."
                    )
            ).queue();

            return;
        }

        // TODO: Check if there is a linked Discord and the same as the one that sent the command (check the database, update the entry, unverify the old one
        try{
            verifyUser(event.getUser(), ign);
        } catch (Exception e){
            System.out.println(e.getMessage());
            event.getHook().sendMessage("").addEmbeds(
                    makeErrorEmbed(
                            "Verification Error",
                            "An error occurred while verifying your account. Please try again later."
                    )
            ).queue();
            return;
        }
        String username = hypixelProfile.getDisplayName();
        Logger.log("<Verify> Linked " + username + " (" + uuid + ") for @" + runnerUsername + ".");

        event.getHook().sendMessage("Successfully verified your discord account! (jk this doesn't work yet.) ").queue();

    }
}
