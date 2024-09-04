package me.stuffy.stuffybot.events;

import me.stuffy.stuffybot.Bot;
import me.stuffy.stuffybot.profiles.GlobalData;
import me.stuffy.stuffybot.utils.Logger;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static me.stuffy.stuffybot.utils.APIUtils.updateBotStats;
import static me.stuffy.stuffybot.utils.APIUtils.updateUsersStats;

public class UpdateBotStatsEvent extends BaseEvent{
    public UpdateBotStatsEvent() {
        super("UpdateBotStats", 2, TimeUnit.HOURS);
    }

    @Override
    protected void execute() {
        publicExecute();
    }

    public static void publicExecute() {
        Logger.log("<UpdateBotStats> Updating bot stats.");
        Bot bot = Bot.getInstance();
        GlobalData globalData = Bot.getGlobalData();

        int totalServers = bot.getJDA().getGuilds().size();

        Map<String, String> uniqueUsers = globalData.getUniqueUsers();
        Map<String, Integer> commandsRun = globalData.getCommandsRun();
        Map<String, Integer> userCommandsRun = globalData.getUserCommandsRun();

        if(commandsRun.isEmpty()) {
            Logger.log("<UpdateBotStats> No data to update.");
        } else {
            updateBotStats(totalServers, commandsRun);
            Logger.log("<UpdateBotStats> Updated bot stats.");
        }
        if(uniqueUsers.isEmpty()) {
            Logger.log("<UpdateBotStats> No unique users to update.");
        } else {
            updateUsersStats(uniqueUsers, userCommandsRun);
            Logger.log("<UpdateBotStats> Updated user stats.");
        }

        globalData.clearCommandsRun();
        globalData.clearUniqueUsers();
    }
}
