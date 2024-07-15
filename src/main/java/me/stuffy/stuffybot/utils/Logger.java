package me.stuffy.stuffybot.utils;

public class Logger {
//    public Guild logGuild;
//    public Channel logChannel;

    public static void log(String message) {
        String date = java.time.LocalDate.now().toString();
        String time = java.time.LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString();
        message = "[" + date + " " + time + "] " + message;
        System.out.println(message);
    }

    public static void logError(String message) {
        String date = java.time.LocalDate.now().toString();
        String time = java.time.LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString();
        message = "[ERROR] [" + date + " " + time + "] " + message;
        System.out.println(message);
    }
}
