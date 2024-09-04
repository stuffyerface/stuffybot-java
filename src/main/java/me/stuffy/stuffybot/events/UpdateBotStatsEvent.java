package me.stuffy.stuffybot.events;

import me.stuffy.stuffybot.Bot;
import me.stuffy.stuffybot.utils.Logger;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UpdateBotStatsEvent extends BaseEvent{
    public UpdateBotStatsEvent() {
        super("UpdateBotStats", 6, TimeUnit.HOURS);
    }

    @Override
    protected void execute() {
        // How many servers the bot is in
        // How many commands have been run
        Bot bot = Bot.getInstance();
        int totalServers = bot.getJDA().getGuilds().size();


        Map<String, Integer> recentInteractions = Bot.getGlobalData().getInteractions();
        // # TODO: Update the bot stats on the website
        Bot.getGlobalData().clearInteractions();

    }
}
