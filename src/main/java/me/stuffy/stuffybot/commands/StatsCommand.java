package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.profiles.HypixelProfile;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.text.DecimalFormat;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.*;

public class StatsCommand extends BaseCommand{
    public StatsCommand(String name, String description){
        super(name, description,
                new OptionData(OptionType.STRING, "ign", "The username of the player you want to look up", false));
    }

    @Override
    protected void onCommand(SlashCommandInteractionEvent event) {
        String ign = getUsername(event);
        HypixelProfile hypixelProfile;
        try{
            hypixelProfile = getHypixelProfile(ign);
        } catch (Exception e){
            event.getHook().sendMessage("").addEmbeds(
                    makeErrorEmbed(
                            "API Error",
                            "An error occurred while fetching your Hypixel profile. Please try again later."
                    )
            ).queue();
            return;
        }

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

        event.getHook().sendMessage("").addEmbeds(
                makeStatsEmbed(
                        "Hypixel Stats for " + username,
                        embedContent
                )
        ).queue();
    }

    public void onButton(ButtonInteractionEvent event) {
    }
}
