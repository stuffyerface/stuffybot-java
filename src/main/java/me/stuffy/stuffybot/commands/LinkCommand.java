package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.Bot;
import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.GlobalData;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.utils.APIException;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.APIUtils.updateLinkedDB;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeEmbed;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeErrorEmbed;

public class LinkCommand {
    static Map<String, Long> linkDelay = new HashMap<>();
    static int linkDelayTime = 60000;
    public static MessageCreateData link(InteractionId interactionId) throws APIException {
        String ign = interactionId.getOptions().get("ign");
        String discordId = interactionId.getUserId();

        HypixelProfile hypixelProfile = getHypixelProfile(ign);

        String mcUsername = hypixelProfile.getDisplayName();
        UUID uuid = hypixelProfile.getUuid();

        // #TODO: Check if the user is verified
        long current = System.currentTimeMillis();
        if (linkDelay.containsKey(discordId) && current - linkDelay.get(discordId) < linkDelayTime) {
            return new MessageCreateBuilder()
                    .addEmbeds(makeErrorEmbed("Account linking failed", "Please wait a bit before trying to link your account again."))
                    .build();
        }

        GlobalData globalData = Bot.getGlobalData();
        Map<String, UUID> linkedAccounts = globalData.getLinkedAccounts();

        if (linkedAccounts.containsKey(discordId) && linkedAccounts.get(discordId).equals(uuid)){
            return new MessageCreateBuilder()
                    .addEmbeds(makeErrorEmbed("Account linking failed", "You are already linked to this account."))
                    .build();
        }

        linkDelay.put(discordId, current);

        try {
            updateLinkedDB(discordId, uuid, mcUsername);
        } catch (Exception e) {
            e.printStackTrace();
            return new MessageCreateBuilder()
                    .addEmbeds(makeErrorEmbed("Account linking failed", "An error occurred while linking your account. Please try again later."))
                    .build();
        }

        MessageEmbed linkEmbed = makeEmbed(
                "Account Linked",
                "Successfully linked as **" + mcUsername + "**!",
                "You can now run commands without the ign parameter.",
                0x6AC672
        );

        return new MessageCreateBuilder()
                .addEmbeds(linkEmbed).build();
    }
}
