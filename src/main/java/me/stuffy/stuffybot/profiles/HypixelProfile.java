package me.stuffy.stuffybot.profiles;

import com.google.gson.JsonObject;
import me.stuffy.stuffybot.utils.MiscUtils;

import java.util.UUID;

import static me.stuffy.stuffybot.utils.MiscUtils.getNestedJson;

public class HypixelProfile {
    private UUID uuid;
    private String displayName;
    private String rank; // Enum this
    private JsonObject profile;

    public HypixelProfile(JsonObject profile) {
        this.profile = profile.deepCopy();
        this.uuid = MiscUtils.formatUUID(profile.get("uuid").getAsString());
        this.displayName = profile.get("displayname").getAsString();
        this.rank = profile.get("rank").getAsString();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRank() {
        return rank;
    }

    public String getDiscord() {
        return getNestedJson(profile, "socialMedia", "links", "DISCORD").getAsString();
    }

    public JsonObject getProfile() {
        return profile;
    }
}
