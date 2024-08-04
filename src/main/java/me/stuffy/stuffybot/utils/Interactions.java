package me.stuffy.stuffybot.utils;

import me.stuffy.stuffybot.commands.InteractionId;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.HashMap;
import java.util.regex.Pattern;

import static me.stuffy.stuffybot.utils.DiscordUtils.makeErrorEmbed;
import static net.dv8tion.jda.api.interactions.components.buttons.Button.secondary;

public class Interactions {

    public static MessageCreateData getResponse(String componentId) throws InteractionException {

        InteractionId interactionId;

        try {
            interactionId = new InteractionId(componentId);
        } catch (Exception e) {
            throw new InteractionException(new String[]{
                    "You can't fool me, this is gibberish",
                    "This is not the interaction you are looking for",
                    "*Someone* messed up the interactionId, and it wasn't me",
                    "I'm sorry, Dave. I'm afraid I can't do that",
                    "You can't fool me, that button doesn't exist",
                    "So, we're just making up our own buttons now?",
                    "How did you even get here?"});
        }

        String command = interactionId.getCommand();
        String userId = interactionId.getUserId();
        HashMap<String, String> options = interactionId.getOptions();

        // TODO: Error handling for invalid command
//        switch (command) {
//            case "pit":
//                return null;
//            default:
//                throw new InteractionException("Invalid command, how did you even get here?");
//        }
        // TODO: Error handling for invalid userId


        if(options == null){
            throw new InteractionException("No options provided, what am I supposed to do with this?");
        }

        if(options.containsKey("ign")){
            String ign = options.get("ign");
            if (ign.length() > 16){
                throw new InteractionException("Invalid field `ign`, Usernames cannot be longer than 16 characters");
            }
            Pattern pattern = Pattern.compile("[^a-zA-Z0-9_]");
            if (pattern.matcher(ign).find()){
                throw new InteractionException("Invalid field `ign`, Usernames can only contain letters, numbers and underscores");
            }
        }

        MessageEmbed embed = makeErrorEmbed("Invalid interactionId", "Invalid interactionId");
        MessageCreateBuilder data = new MessageCreateBuilder()
                .addEmbeds(embed)
                .addActionRow(secondary("This is a button", "button"));
        return data.build();
    }
}
