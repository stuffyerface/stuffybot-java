package me.stuffy.stuffybot;


import me.stuffy.stuffybot.commands.MaxedGamesCommand;
import me.stuffy.stuffybot.commands.VerifyCommand;
import me.stuffy.stuffybot.utils.DiscordUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class Bot {
    private static Bot INSTANCE;
    private Guild testGuild;
    private Guild logGuild;
    private TextChannel logChannel;



    public Bot() throws InterruptedException {
        INSTANCE = this;
        // Get token from env variable
        String token = System.getenv("BOT_TOKEN");
        JDABuilder builder = JDABuilder.createDefault(token) ;
        builder.setActivity(Activity.competing("a hot dog eating contest"));
        JDA jda = builder.build().awaitReady();

        // Initialize testing guild
        this.testGuild = jda.getGuildById("795108903733952562");
        assert this.testGuild != null : "Failed to find log guild";
        // Initialize log guild and channel
        this.logGuild = jda.getGuildById("818238263110008863");
        this.logChannel = this.logGuild.getTextChannelById("818611323755036702");
        assert logChannel != null : "Failed to find log channel";

        // Log startup
        String time = DiscordUtils.discordTimeNow();
        String self = jda.getSelfUser().getAsMention();
        logChannel.sendMessage("Bot " + self + " started successfully " + time + ".").queue();


        // Register commands
        jda.addEventListener(
                new VerifyCommand("verify", "Verifies you!"),
                new MaxedGamesCommand("maxes", "Shows maxed games!")
        );

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
}
