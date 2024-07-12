package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.profiles.Rank;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.*;

public class Stats extends BaseCommand{
    public Stats(String name, String description){
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


        String wins = hypixelProfile.getWins();
        Integer kills = 0; // //hypixelProfile.getKills();

        String rewardStreak = hypixelProfile.getRewardStreak();
        String questsCompleted = hypixelProfile.getQuestsCompleted();
        String networkLevel = hypixelProfile.getNetworkLevel();
        String karma = hypixelProfile.getKarma();
        String achievementPoints = hypixelProfile.getAchievementPoints();
        String firstLogin = hypixelProfile.getFirstLogin();
        //String guild = hypixelProfile.getGuild();
        String onlineStatus = hypixelProfile.getOnlineStatus();
        // String legacyAchievementPoints = 0; //hypixelProfile.getLegacyAchievementPoints();

        String embedContent =
                onlineStatus + "\n" +
                "Network Level: **" + networkLevel + "**\n" +
                "Karma: **" + karma + "**\n\n" +
                "Achievement Points: **" + achievementPoints + "**\n" +
                //"Legacy Achievement Points: " + legacyAchievementPoints + "\n\n" +
                "Quests Completed: **" + questsCompleted + "**\n" +
                        "Challenges Completed: **" + hypixelProfile.getChallengesCompleted() + "**\n\n" +
                "Reward Streak/Record: **" + rewardStreak + "**\n" +
                "Wins: " + wins + "\n" +
                "Kills: " + kills + "\n" +
                "First Login: " + firstLogin + "\n";

        event.getHook().sendMessage("").addEmbeds(
                makeStatsEmbed(
                        "Hypixel Stats for " + username,
                        embedContent
                )
        ).queue();
    }
}
