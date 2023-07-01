package me.stuffy.stuffybot.profiles;

import java.util.UUID;

public class MojangProfile {
    private String username;
    private UUID uuid;

    public MojangProfile(String username, UUID uuid) {
        this.username = username;
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUuid() {
        return uuid;
    }
}
