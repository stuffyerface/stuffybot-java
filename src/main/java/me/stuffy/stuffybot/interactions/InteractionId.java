package me.stuffy.stuffybot.interactions;

import java.util.ArrayList;
import java.util.HashMap;

public class InteractionId {
    private final String id;
    private final String command;
    private final String userId;
    private final HashMap<String, String> options;
    public InteractionId (String componentId) {
        String[] parts = componentId.split(":");
        if (parts.length > 4 || parts.length < 3) {
            throw new IllegalArgumentException("Improperly formatted componentId");
        }

        this.id = parts[0];
        this.command = parts[1];
        this.userId = parts[2];

        this.options = new HashMap<>();
        try {
            String options = parts[3];
            String[] optionParts = options.split(",");
            for (String option : optionParts) {
                String[] optionParts2 = option.split("=");
                this.options.put(optionParts2[0], optionParts2[1]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // No options
        }
    }

    public InteractionId(String id, String command, String userId, ArrayList<String> options) {
        this.id = id;
        this.command = command;
        this.userId = userId;

        this.options = new HashMap<>();
        for (String option : options) {
            String[] optionParts = option.split("=");
            this.options.put(optionParts[0], optionParts[1]);
        }
    }

    public String getId() {
        return this.id;
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

    public String getOption(String key) {
        return this.options.get(key);
    }

    public String getOption(String key, String defaultValue) {
        return this.options.getOrDefault(key, defaultValue);
    }

    public String getOptionsString() {
        StringBuilder optionsString = new StringBuilder();
        for (String key : this.options.keySet()) {
            optionsString.append(key).append("=").append(this.options.get(key)).append(",");
        }
        return optionsString.toString();
    }

    public static InteractionId newCommand(String command, InteractionId interactionId) {
        return new InteractionId(interactionId.getId() + ":" + command + ":" + interactionId.getUserId() + ":" + interactionId.getOptionsString());
    }

    public String getInteractionString() {
        return this.id + ":" + this.command + ":" + this.userId + ":" + this.getOptionsString();
    }

    public InteractionId setOption(String key, String value) {
        this.options.put(key, value);
        return this;
    }
}
