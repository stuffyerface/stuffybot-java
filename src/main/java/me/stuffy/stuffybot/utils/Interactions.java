package me.stuffy.stuffybot.utils;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Map;
import java.util.regex.Pattern;

import static me.stuffy.stuffybot.utils.DiscordUtils.makeErrorEmbed;

public class Interactions {

    public static MessageEmbed getResponse(String interactionId) throws InteractionException {
        String[] parts = interactionId.split(":");
        String command = parts[0];
        String userId = parts[1];
        String options = parts[2];

        // TODO: Error handling for invalid command
        // TODO: Error handling for invalid userId


        Map<String, String> optionsMap = DiscordUtils.parseOptions(options);
        if(optionsMap == null){
            throw new InteractionException("No options provided, what am I supposed to do with this?");
        }

        if(optionsMap.containsKey("ign")){
            String ign = optionsMap.get("ign");
            if (ign.length() > 16){
                throw new InteractionException("Invalid field `ign`, Usernames cannot be longer than 16 characters");
            }
            Pattern pattern = Pattern.compile("[^a-zA-Z0-9_]");
            if (pattern.matcher(ign).find()){
                throw new InteractionException("Invalid field `ign`, Usernames can only contain letters, numbers and underscores");
            }
        }

        MessageEmbed embed = makeErrorEmbed("Invalid interactionId", "Invalid interactionId");
        return embed;
    }
}
