package me.stuffy.stuffybot.events;

import me.stuffy.stuffybot.Bot;
import me.stuffy.stuffybot.profiles.GlobalData;
import me.stuffy.stuffybot.utils.Logger;
import net.dv8tion.jda.api.entities.Guild;

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

        Guild[] guilds = bot.getJDA().getGuilds().toArray(new Guild[0]);
        int totalUsers = 0;
        int totalServers = 0;
        for (Guild guild : guilds) {
            totalServers++;
            totalUsers += guild.getMemberCount();
        }

        Map<String, String> uniqueUsers = globalData.getSessionUniqueUsers();
        Map<String, Integer> commandsRun = globalData.getSessionCommandsRun();
        Map<String, Integer> userCommandsRun = globalData.getSessionUserCommandsRun();

        if(commandsRun.isEmpty()) {
            Logger.log("<UpdateBotStats> No data to update.");
        } else {
            updateBotStats(totalServers, totalUsers, commandsRun);
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
