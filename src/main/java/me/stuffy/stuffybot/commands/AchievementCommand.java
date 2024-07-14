package me.stuffy.stuffybot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import static me.stuffy.stuffybot.utils.DiscordUtils.getUsername;

public class AchievementCommand extends BaseCommand{
    public AchievementCommand(String name, String description){
        super(name, description,
                new OptionData(OptionType.STRING, "ign", "Minecraft Username", false),
                new OptionData(OptionType.STRING, "game", "The game", false)
                        .addChoice("Skyblock", "skyblock"),
                new OptionData(OptionType.STRING, "type", "Challenge/Tiered", false)
                        .addChoice("Challenge", "challenge")
                        .addChoice("Tiered", "tiered"
                )
        );
    }


    protected void onCommand(SlashCommandInteractionEvent event) {
        String ign = getUsername(event);
        if (event.getOption("game") == null) {
            event.getHook().sendMessage("no game provided").queue();
            return;
        }
        String game = event.getOption("game").getAsString();
        event.getHook().sendMessage("game: " + game).queue();
    }

    public void onButton(ButtonInteractionEvent event) {
    }
}
