package me.stuffy.stuffybot.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.utils.APIException;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;

import static me.stuffy.stuffybot.utils.APIUtils.getHypixelProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;

public class BuildBattleCommand {
    public static MessageCreateData buildBattleBackdrops(InteractionId interactionId) throws APIException {
        String ign = interactionId.getOptions().get("ign");
        HypixelProfile hypixelProfile = getHypixelProfile(ign);
        String username = hypixelProfile.getDisplayName();

        JsonArray bbPackages = hypixelProfile.getStatArray("BuildBattle.packages");
        JsonObject bbBackdropWins = hypixelProfile.getStatObject("BuildBattle.backdrop_wins");

        ArrayList<String> missingBackdrops = new ArrayList<>();
        for (JsonElement ownedItem : bbPackages) {
            String packageName = ownedItem.getAsString();
            if (!packageName.isEmpty()) {
                if(packageName.startsWith("backdrops_")) {
                    packageName = packageName.replaceFirst("backdrops_", "");
                    if (!bbBackdropWins.has(packageName ) || bbBackdropWins.get(packageName).getAsInt() <= 0) {
                        missingBackdrops.add(packageName);
                    }
                }
            }
        }

        StringBuilder returnContent = new StringBuilder();
        if(missingBackdrops.isEmpty()) {
            returnContent = new StringBuilder("No backdrops found!");
        } else {
            int count = missingBackdrops.size();
            returnContent = new StringBuilder("Missing " + count + " backdrops.");
            int index = -1;
            for (String mbd : missingBackdrops) {
                index += 1 ;
                if (index > 9) {
                    continue;
                }
                if (index == 9 && count > 10) {
                    returnContent.append("\n*and ").append(count - index).append(" more...*");
                    continue;
                }
                returnContent.append("\n`").append(mbd).append("`");
            }
        }

        MessageEmbed bbBackups = makeStatsEmbed(
                "Yet to win backdrops for " + username, "Names listed are internal Hypixel IDs", returnContent.toString()
        );

        return new MessageCreateBuilder()
                .addEmbeds(bbBackups)
                .build();
    }
}
