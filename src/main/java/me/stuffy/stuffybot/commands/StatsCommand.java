package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.utils.APIException;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.text.DecimalFormat;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;

public class StatsCommand {
    public static MessageCreateData stats(InteractionId interactionId) throws APIException {
        String ign = interactionId.getOptions().get("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();

        DecimalFormat df = new DecimalFormat("#,###");
        DecimalFormat df2 = new DecimalFormat("#,###.##");

        Integer questsCompleted = hypixelProfile.getQuestsCompleted();
        Integer wins = hypixelProfile.getWins();
        Integer kills = hypixelProfile.getKills();
        Integer rewardStreak = hypixelProfile.getRewardStreak();
        Integer rewardRecord = hypixelProfile.getRewardRecord();
        Integer karma = hypixelProfile.getKarma();
        Integer achievementPoints = hypixelProfile.getAchievementPoints();
        Integer legacyAchievementPoints = hypixelProfile.getLegacyAchievementPoints();
        Integer challenges = hypixelProfile.getChallengesCompleted();

        Double networkLevel = hypixelProfile.getNetworkLevel();

        String firstLogin = hypixelProfile.getFirstLogin();
        String onlineStatus = hypixelProfile.getOnlineStatus();

        String embedContent =
                onlineStatus + "\n" +
                        "Network Level: **" + df2.format(networkLevel) + "**\n" +
                        "Karma: **" + df.format(karma) + "**\n\n" +
                        "Achievement Points: **" + df.format(achievementPoints) + "** (+" + df.format(legacyAchievementPoints) + " legacy)" + "\n" +
                        "Quests Completed: **" + df.format(questsCompleted) + "**\n" +
                        "Challenges Completed: **" + df.format(challenges) + "**\n\n" +
                        "Reward Streak|Record: **" + df.format(rewardStreak) + "** | " + df.format(rewardRecord) + "\n" +
                        "Wins: **" + df.format(wins) + "**\n" +
                        "Kills: **" + df.format(kills) + "**\n" +
                        "First Login: " + firstLogin + "\n";

        MessageEmbed stats = makeStatsEmbed(
                "Stats for " + username,
                embedContent
        );

        return new MessageCreateBuilder()
                .addEmbeds(stats)
                .build();

    }
}
