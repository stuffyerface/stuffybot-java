package me.stuffy.stuffybot.profiles;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.stuffy.stuffybot.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import static me.stuffy.stuffybot.utils.MiscUtils.getNestedJson;

public class SkyBlockProfile {

    private final String profileId;
    private final String cuteName;
    private final Boolean selected;
    private final ProfileType profileType;
    private final JsonObject data;

    public SkyBlockProfile(String profileId, String cuteName, Boolean selected, ProfileType profileType, JsonObject skyBlockProfiles) {
        this.data = skyBlockProfiles.deepCopy();
        this.profileId = profileId;
        this.cuteName = cuteName;
        this.selected = selected;
        this.profileType = profileType;
    }

    public static List<SkyBlockProfile> getSkyBlockProfiles(JsonArray profileData) {
        ArrayList<SkyBlockProfile> profiles = new ArrayList<>();
        for (JsonElement profile : profileData) {
            try{
                String profileId = getNestedJson((String) null, profile, "profile_id").getAsString();
                String cuteName = getNestedJson((String) null, profile, "cute_name").getAsString();
                Boolean selected = getNestedJson(false, profile, "selected").getAsBoolean();
                ProfileType profileType = ProfileType.fromString(getNestedJson("normal", profile, "game_mode").getAsString());
                profiles.add(new SkyBlockProfile(profileId, cuteName, selected, profileType, profile.getAsJsonObject()));
            } catch (Exception e) {
                Logger.logError("Error parsing SkyBlock profile: " + e.getMessage());
            }
        }
        return profiles;
    }

    public Boolean getSelected() {
        return selected;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getCuteName() {
        return cuteName;
    }

    public ProfileType getProfileType() {
        return profileType;
    }

    public JsonObject getData() {
        return data;
    }
}
