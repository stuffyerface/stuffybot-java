package me.stuffy.stuffybot.utils;

import me.stuffy.stuffybot.Bot;
import me.stuffy.stuffybot.profiles.GlobalData;
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
import java.util.Objects;

import static me.stuffy.stuffybot.utils.DiscordUtils.discordTimeUnix;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeErrorEmbed;

public class Verification {
    public static void verifyButton(ButtonInteractionEvent event) {
        User user = event.getUser();
        String userId = event.getUser().getId();

        // #TODO if I am verified in linked db, update me according to info there
        if(isVerified(userId)){
            MessageCreateData data = new MessageCreateBuilder()
                    .setEmbeds(DiscordUtils.makeErrorEmbed("Verification Error", "You have already verified your identity, silly goose.")).build();
            event.reply(data).setEphemeral(true).queue();
            return;
        }

        // #TODO if I am linked, check if my mc account has this discord account listed in api, if yes, verifyUser()
        // #TODO prompt the user with the modal, even if they are linked
        // #TODO if the account does not have the discord linked, time out for 3 minutes, teach them how
        // #TODO if all is valid, verify them

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
        if (!captcha.equals("stuffy")) {
            // TODO: Make this actually time out for 5 minutes
            MessageEmbed errorEmbed = makeErrorEmbed("Verification Error", "You entered the CAPTCHA incorrectly.\n-# Try again in " + discordTimeUnix(Instant.now().plusSeconds(300).toEpochMilli()));
            MessageCreateData data = new MessageCreateBuilder()
                    .addEmbeds(errorEmbed)
                    .build();
            event.reply(data).setEphemeral(true).queue();
            return;
        }

        event.reply("You got the captcha right, " + ign).setEphemeral(true).queue();

    }

    public static void verifyUser(User user) {
        // #TODO check if anyone already has the account verified, unverify them first
        // #TODO add this username,uuid to verified in linked db
        // #TODO remove unverified role, add verified role
        // #TODO add all roles that the player has already earned
    }

}
