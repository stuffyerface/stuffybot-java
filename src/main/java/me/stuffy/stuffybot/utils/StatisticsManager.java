package me.stuffy.stuffybot.utils;

import java.util.HashMap;
import java.util.Map;

public class StatisticsManager {
    private static int totalCommandsRun = 0;
    private static final Map<String, Integer> commandUsage = new HashMap<>();

    public static void incrementCommandsRun() {
        totalCommandsRun++;
    }

    public static void incrementCommandUsage(String commandName) {
        int previousUsage = commandUsage.getOrDefault(commandName, 0);
        commandUsage.put(commandName, previousUsage + 1);
        incrementCommandsRun();
    }

    public static int getCommandUsage(String commandName) {
        return commandUsage.get(commandName);
    }

    public static int getTotalCommandsRun() {
        return totalCommandsRun;
    }


}
