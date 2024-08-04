package me.stuffy.stuffybot.commands;

import java.util.HashMap;

public class InteractionId {
    private final String command;
    private final String userId;
    private final HashMap<String, String> options;
    public InteractionId (String componentId) {
        String[] parts = componentId.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Improperly formatted componentId");
        }

        this.command = parts[0];
        this.userId = parts[1];

        String options = parts[2];
        this.options = new HashMap<>();
        String[] optionParts = options.split(",");
        for (String option : optionParts) {
            String[] optionParts2 = option.split("=");
            this.options.put(optionParts2[0], optionParts2[1]);
        }
    }

    public String getCommand() {
        return this.command;
    }

    public String getUserId() {
        return this.userId;
    }

    public HashMap<String, String> getOptions() {
        return this.options;
    }
}
