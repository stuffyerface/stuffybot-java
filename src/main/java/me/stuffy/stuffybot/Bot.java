package me.stuffy.stuffybot;


import me.stuffy.stuffybot.interactions.*;
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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.lang.reflect.Array;
import java.util.ArrayList;

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
        assert this.homeGuild != null : "Failed to find home guild";


        // Log startup
        String time = DiscordUtils.discordTimeNow();
        String self = jda.getSelfUser().getAsMention();
        Logger.log("<Startup> Bot " + self + " started successfully " + time + ".");

        // Listen for interactions
        jda.addEventListener(
                new InteractionHandler()
        );

        // Register commands "global"ly or "local"ly
        registerCommands("local");

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

    public void registerCommands(String scope) {
        OptionData ignOption = new OptionData(OptionType.STRING, "ign", "The player's IGN").setRequired(false);
        // Create a list of commands first
        ArrayList<CommandData> commandList = new ArrayList<>();
//        commandList.add(Commands.slash("achievements", "Doesn't do anything yet"));
//        commandList.add(Commands.slash("help", "*Should* show a help message"));
        commandList.add(Commands.slash("pit", "Get Pit stats for a player")
                .addOptions(ignOption));
        commandList.add(Commands.slash("stats", "Get Hypixel stats for a player")
                .addOptions(ignOption));
        commandList.add(Commands.slash("tkr", "Get TKR stats for a player")
                .addOptions(ignOption));
        commandList.add(Commands.slash("maxes", "Get maxed games for a player")
                .addOptions(ignOption));
        commandList.add(Commands.slash("blitz", "Get Blitz Ultimate Kit xp for a player")
                .addOptions(ignOption));

        if (scope.equals("local")) {
            //clearLocalCommands();
            this.homeGuild.updateCommands().addCommands(
                    commandList
            ).queue();
            Logger.log("<Commands> Successfully Registered commands in guild.");
        } else if (scope.equals("global")){
            jda.updateCommands().addCommands(
                    commandList
            ).queue();
            Logger.log("<Commands> Successfully Registered commands globally.");
        } else {
            throw new IllegalArgumentException("Invalid scope: " + scope);
        }
    }

    public void clearCommands() {
        jda.updateCommands().queue();
        Logger.log("<Commands> Successfully cleared commands.");
    }

    public void clearLocalCommands() {
        this.homeGuild.updateCommands().queue();
        Logger.log("<Commands> Successfully cleared local commands.");
    }
}
