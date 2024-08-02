package me.stuffy.stuffybot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class TestCommand extends BaseCommand{

    public TestCommand(String name, String description, OptionData... options) {
        super(name, description,
                new OptionData(OptionType.STRING, "ign", "Your Minecraft Username", false),
                new OptionData(OptionType.STRING, "ign2", "Your Minecraft Username", false),
                new OptionData(OptionType.STRING, "ign3", "Your Minecraft Username", false),
                new OptionData(OptionType.STRING, "ign4", "Your Minecraft Username", false));
    }

    @Override
    protected void onCommand(SlashCommandInteractionEvent event) {

    }

    @Override
    protected void onButton(ButtonInteractionEvent event) {

    }

    @Override
    protected void cleanupEventResources(String messageId) {

    }
}
