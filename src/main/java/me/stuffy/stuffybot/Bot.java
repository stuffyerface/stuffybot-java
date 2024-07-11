package me.stuffy.stuffybot;


import me.stuffy.stuffybot.commands.Achievements;
import me.stuffy.stuffybot.commands.MaxedGames;
import me.stuffy.stuffybot.commands.Tournament;
import me.stuffy.stuffybot.commands.Verify;
import me.stuffy.stuffybot.events.ActiveEvents;
import me.stuffy.stuffybot.events.UpdateBotStats;
import me.stuffy.stuffybot.utils.DiscordUtils;
import me.stuffy.stuffybot.utils.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Bot extends ListenerAdapter {
    private static Bot INSTANCE;
    private final JDA jda;
    private Guild testGuild;
    private Guild logGuild;
    private TextChannel logChannel;


    public Bot() throws InterruptedException {
        INSTANCE = this;
        // Get token from env variable
        String token = System.getenv("BOT_TOKEN");
        JDABuilder builder = JDABuilder.createDefault(token) ;
        builder.setActivity(Activity.customStatus("hating slash commands"));
        builder.addEventListeners(this);
        JDA jda = builder.build().awaitReady();
        this.jda = jda;

        // Initialize testing guild
        this.testGuild = jda.getGuildById("795108903733952562");
        assert this.testGuild != null : "Failed to find log guild";


        // Log startup
        String time = DiscordUtils.discordTimeNow();
        String self = jda.getSelfUser().getAsMention();
        Logger logger = new Logger();
        logger.log("<Startup> Bot " + self + " started successfully " + time + ".");

        // Register commands
        jda.addEventListener(
                new Verify("verify", "Links your discord account to your Minecraft account"),
                new MaxedGames("maxes", "Find the games with all achievements unlocked"),
                new Tournament("tournament", "Shows tournament stats"),
                new Achievements("achievements", "Shows achievements progress for a user")
        );

        // Start events
        new UpdateBotStats().startFixedRateEvent();
        new ActiveEvents().startFixedRateEvent();
    }
    public static Bot getInstance() {
        return INSTANCE;
    }

    public Guild getTestGuild() {
        return this.testGuild;
    }

    public Guild getLogGuild() {
        return this.logGuild;
    }

    public TextChannel getLogChannel() {
        return this.logChannel;
    }

    public Role getVerifiedRole() {
        return this.testGuild.getRoleById("795118862940635216");
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
