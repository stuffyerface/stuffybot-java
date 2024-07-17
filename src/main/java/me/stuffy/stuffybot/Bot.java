package me.stuffy.stuffybot;


import me.stuffy.stuffybot.commands.*;
import me.stuffy.stuffybot.events.ActiveEvents;
import me.stuffy.stuffybot.events.UpdateBotStatsEvent;
import me.stuffy.stuffybot.utils.DiscordUtils;
import me.stuffy.stuffybot.utils.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Bot extends ListenerAdapter {
    private static Bot INSTANCE;
    private final JDA jda;
    private Guild homeGuild;


    public Bot() throws InterruptedException {
        INSTANCE = this;
        // Get token from env variable
        String token = System.getenv("BOT_TOKEN");
        JDABuilder builder = JDABuilder.createDefault(token) ;
        builder.setActivity(Activity.customStatus("hating slash commands"));
        builder.addEventListeners(this);
        JDA jda = builder.build().awaitReady();
        this.jda = jda;

        // Initialize home guild
        this.homeGuild = jda.getGuildById("795108903733952562");
//        this.homeGuild = jda.getGuildById("818238263110008863");
        assert this.homeGuild != null : "Failed to find home guild";


        // Log startup
        String time = DiscordUtils.discordTimeNow();
        String self = jda.getSelfUser().getAsMention();
        Logger logger = new Logger();
        logger.log("<Startup> Bot " + self + " started successfully " + time + ".");

        // Register commands
        jda.addEventListener(
                new VerifyCommand("verify", "Links your discord account to your Minecraft account"),
                new MaxedGamesCommand("maxes", "Find the games with all achievements unlocked"),
                new TournamentCommand("tournament", "Shows tournament stats"),
                new AchievementCommand("achievements", "Shows achievements progress for a user"),
                new StatsCommand("stats", "Shows overall hypixel stats for a user"),
                new PitCommand("pit", "Shows pit stats for a user"),
                new TkrCommand("tkr", "Shows completed maps in TKR")
        );



        // Start events
        new UpdateBotStatsEvent().startFixedRateEvent();
        new ActiveEvents().startFixedRateEvent();
    }
    public static Bot getInstance() {
        return INSTANCE;
    }

    public Guild getHomeGuild() {
        return this.homeGuild;
    }


    public Role getVerifiedRole() {
        return this.homeGuild.getRoleById("795118862940635216");
    }

    public static void main(String[] args) throws InterruptedException {
        new Bot();
    }

    public JDA getJDA() {
        return this.jda;
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        Guild joinedGuild = event.getGuild();
        Logger.log("<Guilds> Bot joined guild " + joinedGuild.getName() + " (" + joinedGuild.getId() + ")");
    }


    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        Guild leftGuild = event.getGuild();
        Logger.log("<Guilds> Bot left guild: " + leftGuild.getName() + " (" + leftGuild.getId() + ")");
    }
}
