package me.stuffy.stuffybot.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                Logger.logError("Unable to find config.properties");
                throw new RuntimeException("Unable to find config.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getEnvironment() {
        return properties.getProperty("environment", "development");
    }

    public static String getHomeGuildId() {
        return properties.getProperty("homeGuildId");
    }

    public static String getVerifiedRoleId() {
        return properties.getProperty("verifiedRoleId");
    }

    public static String getNotVerifiedRoleId() {
        return properties.getProperty("notVerifiedRoleId");
    }

    public static String getLinkCommandId() {
        return properties.getProperty("linkCommandId");
    }
}
