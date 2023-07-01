package me.stuffy.stuffybot.utils;

import java.util.Arrays;
import java.util.List;

public class TimeUtils {
    private static List<String> VALID_TYPES = Arrays.asList("t", "T", "d", "D", "f", "F", "R");

    /**
     * @see #discordTimeUnix(long, String)
     */
    public static String discordTimeNow(String formatType) {
        return discordTimeUnix(System.currentTimeMillis(), formatType);
    }

    /**
     * @see #discordTimeUnix(long, String)
     */
    public static String discordTimeNow() {
        return discordTimeNow("R");
    }

    /**
     * Returns a string that Discord will parse into a timestamp
     * types: t = short time, T = long time, d = short date, D = long date, f = short date/time, F = long date/time, R = relative time
     * @param timestamp
     * @param formatType
     * @return a string that Discord will parse into a timestamp
     */
    public static String discordTimeUnix(long timestamp, String formatType) {
        if (!VALID_TYPES.contains(formatType)) {
            throw new IllegalArgumentException("Invalid format type");
        }
        return "<t:" + timestamp / 1000 + ":" + formatType + ">";
    }

    /**
     * @see #discordTimeUnix(long, String)
     */
    public static String discordTimeUnix(long timestamp) {
        return discordTimeUnix(timestamp, "R");
    }
}
