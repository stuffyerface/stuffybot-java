package me.stuffy.stuffybot.commands;

import me.stuffy.stuffybot.interactions.InteractionId;
import me.stuffy.stuffybot.profiles.MojangProfile;
import me.stuffy.stuffybot.utils.APIException;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.UUID;

import static me.stuffy.stuffybot.utils.APIUtils.getMojangProfile;
import static me.stuffy.stuffybot.utils.DiscordUtils.makeStatsEmbed;

public class UuidCommand {
    public static MessageCreateData uuid(InteractionId interactionId) throws APIException {
        String ign = interactionId.getOptions().get("ign");
        MojangProfile mojangProfile = getMojangProfile(ign);
        UUID uuid = mojangProfile.getUuid();
        UUIDStats uuidStats = new UUIDStats(mojangProfile.getUsername(), uuid);


        MessageEmbed uuidEmbed = makeStatsEmbed("UUID Data",
                "`" + uuidStats.username + "`'s UUID is `" + uuidStats.uuid + "`\n" +
                        "That's better than `" + uuidStats.getFormattedBetterThanPercentage() + "`% of all UUIDs!" +
                        "\n-# Position `#" + String.format("%,d", uuidStats.estimatedRank(65340094)) + "`."
        );

        return new MessageCreateBuilder()
                .addEmbeds(uuidEmbed)
                .build();

    }

    private static class UUIDStats {
        private final String username;
        private final UUID uuid;
        private final double betterThanPercentage;

        public UUIDStats(String username, UUID uuid) {
            this.username = username;
            this.uuid = uuid;
            this.betterThanPercentage = getBetterThanPercentage();
        }

        private double getBetterThanPercentage() {
            String characterRanking = "0123456789abcdef";
            String uuid = this.uuid.toString().replace("-", "");
            double totalScore = 0;
            double remainingScore = 100.0;
            for (int i = 0; i < uuid.length(); i++) {
                int totalChars = characterRanking.length();
                int pos =  characterRanking.indexOf(uuid.charAt(i));
                totalScore += remainingScore * (pos / (double) totalChars);
                remainingScore = remainingScore / 16;
            }
            return totalScore;
        }

        public int estimatedRank(int totalPlayers) {
            return (int) (Math.ceil((1 - (this.betterThanPercentage / 100)) * totalPlayers));
        }

        public String getFormattedBetterThanPercentage() {
            return String.format("%.2f", this.betterThanPercentage);
        }
    }
}
