package me.stuffy.stuffybot.utils;

import java.util.ArrayList;
import java.util.List;

public class Logger {
    private static final List<String> logs = new ArrayList<>();
    private static String logName;

    public static void log(String message) {
        String date = java.time.LocalDate.now().toString();
        String time = java.time.LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString();
        message = "[" + date + " " + time + "] " + message;
        logs.add(message);
        System.out.println(message);
    }

    public static void logError(String message) {
        String date = java.time.LocalDate.now().toString();
        String time = java.time.LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString();
        message = "[ERROR] [" + date + " " + time + "] " + message;
        logs.add(message);
        System.out.println(message);
    }

    public static List<String> getLogs() {
        return logs;
    }

    public static String getLogName() {
        return logName;
    }

    public static void setLogName(String name) {
        logName = name;
    }
}
