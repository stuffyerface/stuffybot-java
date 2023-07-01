package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.utils.MojangProfile;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.UUID;

import static me.stuffy.stuffybot.utils.DiscordUtils.getUsername;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeErrorEmbed;
import static me.stuffy.stuffybot.utils.HypixelApiUtils.getDiscord;
import static me.stuffy.stuffybot.utils.MojangApiUtils.getProfile;

public class VerifyCommand extends BaseCommand {

    public VerifyCommand(String name, String description) {
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
            profile = getProfile(ign);
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
        String linkedDiscord = getDiscord(uuid);
        String runnerUsername = getUsername(event.getUser().getAsTag());
        System.out.println(event.getUser().getName());
        if (linkedDiscord == null) {
            event.getHook().sendMessage("No discord linked to the account '" + ign + "'.").queue();
            return;
        } else if (!linkedDiscord.equals(runnerUsername)) {
            event.getHook().sendMessage("").addEmbeds(
                    makeErrorEmbed(
                            "Username Error",
                            "The ign you provided is linked to a different discord account than the one you're using.\n\nYour discord account: `" + runnerUsername + "`\n`" + profile.getUsername() + "`'s Linked discord account: `" + linkedDiscord + "`."
                    )
            ).queue();
            return;
        }



        // TODO: Check if there is a linked Discord and the same as the one that sent the command


        // TODO: Link the Discord account to the Minecraft account

        event.getHook().sendMessage("Your uuid is " + uuid).queue();

    }
}
