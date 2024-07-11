package me.stuffy.stuffybot.profiles;

import me.stuffy.stuffybot.utils.Logger;

import java.util.UUID;

public class MojangProfile {
    private String username;
    private UUID uuid;

    public MojangProfile(String username, UUID uuid) {
        Logger.log("<MojangProfile> Username " + username + " with UUID " + uuid.toString());
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
