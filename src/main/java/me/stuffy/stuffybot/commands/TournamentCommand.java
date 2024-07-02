package me.stuffy.stuffybot.commands;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class TournamentCommand extends BaseCommand{
    public TournamentCommand(String name, String description){
        super(name, description,
                new OptionData(OptionType.STRING, "tournament", "The name of the tournament you want to look up", false).addChoices(
                ),
                new OptionData(OptionType.STRING, "username", "The username of the player you want to look up", false));
    }

}
