package me.stuffy.stuffybot.utils;

import me.stuffy.stuffybot.Bot;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static me.stuffy.stuffybot.utils.APIUtils.*;
import static me.stuffy.stuffybot.utils.DiscordUtils.*;

public class Verification {
    private static final Map<String, Long> captchaTimeouts = new HashMap<>();

    public static void verifyButton(ButtonInteractionEvent event) {
        User user = event.getUser();
        String userId = event.getUser().getId();

        if(captchaTimeouts.containsKey(userId)){
            if(captchaTimeouts.get(userId) > Instant.now().toEpochMilli()) {
                MessageCreateData data = new MessageCreateBuilder()
                        .setEmbeds(makeErrorEmbed("Verification Error", "You have failed a CAPTCHA too recently. Please try again " + discordTimeUnix(captchaTimeouts.get(userId)) + ".")).build();
                event.reply(data).setEphemeral(true).queue();
                return;
            }
            else {
                captchaTimeouts.remove(userId);
            }
        }

        // #TODO if I am verified in linked db, update me according to info there
        if(isVerified(userId)){
            MessageCreateData data = new MessageCreateBuilder()
                    .setEmbeds(makeErrorEmbed("Verification Error", "You have already verified your identity, silly goose.")).build();
            event.reply(data).setEphemeral(true).queue();
            return;
        }

        Modal modal = Modal.create("verify", "Verify your identity in Stuffy Discord")
                .addComponents(ActionRow.of(TextInput.create("ign", "Minecraft Username", TextInputStyle.SHORT)
                                .setPlaceholder("Your Minecraft Username")
                                .setMaxLength(16)
                                .setMinLength(1)
                                .setRequired(true)
                                .build()),
                        ActionRow.of(
                                TextInput.create("captcha", "CAPTCHA", TextInputStyle.PARAGRAPH)
                                        .setPlaceholder("Enter the word 'stuffy'.\n" +
                                                "To prevent abuse, failing the CAPTCHA " +
                                                "will result in a short timeout.")
                                        .setRequired(false)
                                        .build()))
                .build();
        event.replyModal(modal).queue();
    }

    public static boolean isVerified(String userID) {
        ArrayList<String> verifiedAccounts = Bot.getGlobalData().getVerifiedAccounts();
        if (verifiedAccounts.contains(userID)) {
            Logger.log("user " + userID + " is already verified");
            return true;
        }
        Logger.log("user " + userID + " is not verified");
        return false;
    }

    public static void verifyModal(ModalInteractionEvent event) {
        String ign = Objects.requireNonNull(event.getValue("ign")).getAsString();
        String captcha = Objects.requireNonNull(event.getValue("captcha")).getAsString();

        if (!ign.matches("[a-zA-Z0-9_]+")) {
            MessageEmbed errorEmbed = makeErrorEmbed("Verification Error", "Your Minecraft username may only contain letters, numbers, and underscores.");
            MessageCreateData data = new MessageCreateBuilder()
                    .addEmbeds(errorEmbed)
                    .build();
            event.reply(data).setEphemeral(true).queue();
            return;
        }

        if (!captcha.equals("stuffy")) {
            long timeout = Instant.now().plusSeconds(120).toEpochMilli();
            captchaTimeouts.put(event.getUser().getId(), timeout);
            MessageEmbed errorEmbed = makeErrorEmbed("Verification Error", "You entered the CAPTCHA incorrectly.\n-# Try again " + discordTimeUnix(timeout) + ".");
            MessageCreateData data = new MessageCreateBuilder()
                    .addEmbeds(errorEmbed)
                    .build();
            event.reply(data).setEphemeral(true).queue();
            return;
        }

        HypixelProfile profile = null;
        try {
            profile = getHypixelProfile(ign);
        } catch (APIException e) {
            event.replyEmbeds(makeErrorEmbed("Hypixel API Error", e.errorMessage)).setEphemeral(true).queue();
            return;
        }
        if ( profile == null) {
            event.replyEmbeds(makeErrorEmbed("Hypixel API Error", "The user " + ign + " does not seem to exist. SpoOo00oky")).setEphemeral(true).queue();
            return;
        }

        ign = profile.getDisplayName();

        String linkedDiscord = null;
        try {
            linkedDiscord = profile.getDiscord();
        } catch (Exception e) {
            event.replyEmbeds(makeEmbedWithImage("Verification Error", "Check your linked discord in game","The user `" + ign + "` does not have a discord linked.\n\nFollow the steps below to correct this, and try again.", "https://i.imgur.com/HHs9nbZ.gif", 0xC95353)).setEphemeral(true).queue();
            return;
        }

        String discordUsername = event.getUser().getName();
        if(!linkedDiscord.equals(discordUsername)){
            event.replyEmbeds(makeEmbedWithImage("Verification Error", "Check your linked discord in game", "The user `" + ign + "` has a different discord linked.\n In Game Linked Discord: `" + linkedDiscord + "`\n Your Discord Username: `" + discordUsername + "`\n\nFollow the steps below to correct this, and try again.", "https://i.imgur.com/HHs9nbZ.gif", 0xC95353)).setEphemeral(true).queue();
            return;
        }


        // User is verified, whoop-whoop
        updateLinkedDB(event.getUser().getId(), profile.getUuid(), ign);
        setVerifiedStatus(event.getUser().getId(), true);

        Bot bot = Bot.getInstance();
        bot.getJDA().getGuildById(Config.getHomeGuildId()).addRoleToMember(event.getUser(), bot.getVerifiedRole()).queue();
        bot.getJDA().getGuildById(Config.getHomeGuildId()).removeRoleFromMember(event.getUser(), bot.getNotVerifiedRole()).queue();

        event.replyEmbeds(DiscordUtils.makeEmbed("Verification Successful", "You have been verified.", "You may now enjoy all of the perks that come with that. You may unverify at any time.", 0x3d84a2)).setEphemeral(true).queue();
    }

    public static void unverifyButton(ButtonInteractionEvent event) {
        User user = event.getUser();
        String userId = event.getUser().getId();
        if(!isVerified(userId)){
            MessageCreateData data = new MessageCreateBuilder()
                    .setEmbeds(makeErrorEmbed("Unverification Error", "You have not yet verified, there is nothing to undo!")).build();
            event.reply(data).setEphemeral(true).queue();
            return;
        }

        setVerifiedStatus(userId, false);

        Bot bot = Bot.getInstance();
        bot.getJDA().getGuildById(Config.getHomeGuildId()).removeRoleFromMember(event.getUser(), bot.getVerifiedRole()).queue();
        bot.getJDA().getGuildById(Config.getHomeGuildId()).addRoleToMember(event.getUser(), bot.getNotVerifiedRole()).queue();

        // TODO: Remove all roles that the player has already earned

        MessageEmbed embed = DiscordUtils.makeEmbed("Unverify", "You have been unverified.", "You may verify again at any time.", 0x3d84a2);
        event.replyEmbeds(embed).setEphemeral(true).queue();
    }

    public static void verifyUser(User user) {
        // #TODO check if anyone already has the account verified, unverify them first
        // #TODO add this username,uuid to verified in linked db
        // #TODO remove unverified role, add verified role
        // #TODO add all roles that the player has already earned
    }

}
