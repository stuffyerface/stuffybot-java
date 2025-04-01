package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class SetupCommand {
    public static void setupLinkingButton(SlashCommandInteractionEvent event) {
        String linkCommandId = Config.getLinkCommandId();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Verify or Update");
        embedBuilder.setDescription(
                "Verification gives you permission to chat in this discord and have linked roles based on your Hypixel Stats. You must have a discord linked in game to do that. You can always use the button below to unverify your account, but you will lose all perks of being a verified user.\n" +
                        "-# :globe_with_meridians: You do __not__ need to verify your account to use commands inside or outside of this discord, receive announcements from Stuffy Bot, or any other feature we offer.\n" +
                        "\n" +
                        "If you just wish to link your account so slash commands will automatically assume your username for the `ign` field, you may use use " +
                        "</link:" + linkCommandId + ">" +
                        ", which will not require verifying in game.\n" +
                        "\n" +
                        "If you've earned new accomplishments and want to update them, click the update button below.\n" +
                        "-# :warning: Stuffy Bot and Staff of this Discord will __never__ ask for your passwords or other personal information, please protect yourself online."
        );
        embedBuilder.setFooter("Stuffy Bot by @stuffy");
        embedBuilder.setColor(0x3d84a2);
        embedBuilder.build();


        MessageCreateData toBeSent = new MessageCreateBuilder().addEmbeds(
                embedBuilder.build()
        ).addActionRow(
                Button.of(ButtonStyle.SECONDARY, "000:verify:null", "Verify"),
                Button.of(ButtonStyle.SECONDARY, "000:update:null", "Update"),
                Button.of(ButtonStyle.DANGER, "000:unverify:null", "Unverify")
        ).build();

        event.getChannel().sendMessage(toBeSent).queue();
    }
}
