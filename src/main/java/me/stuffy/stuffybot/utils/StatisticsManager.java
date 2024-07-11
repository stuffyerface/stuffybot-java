package me.stuffy.stuffybot.utils;

import java.util.Map;

public class StatisticsManager {
    private static int totalCommandsRun = 0;
    private static Map<String, Integer> commandUsage;

    public static void incrementCommandsRun() {
        totalCommandsRun++;
    }

    public static int getTotalCommandsRun() {
        return totalCommandsRun;
    }

    public static void incrementCommandUsage(String commandName) {
        if (commandUsage.containsKey(commandName)) {
            commandUsage.put(commandName, commandUsage.get(commandName) + 1);
        } else {
            commandUsage.put(commandName, 1);
        }
    }

    public static int getCommandUsage(String commandName) {
        return commandUsage.get(commandName);
    }


}
