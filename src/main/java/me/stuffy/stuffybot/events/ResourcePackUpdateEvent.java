package me.stuffy.stuffybot.events;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.stuffy.stuffybot.utils.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static me.stuffy.stuffybot.utils.APIUtils.*;

public class ResourcePackUpdateEvent extends BaseEvent{
    public ResourcePackUpdateEvent() {super("SkyBlockResourcePackUpdate", 10, TimeUnit.MINUTES);}

    @Override
    protected void execute() {
        JsonElement hypixelResourcePackAPIResponse = getHypixelResourcePackAPIResponse();
        Map<String, Long> lastUpdated = Map.of();
        try {
            lastUpdated = getResourcePacksLastUpdated();
        } catch (Exception e) {
            try {
                initializeResourcePackResponse(hypixelResourcePackAPIResponse);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        ArrayList<String> updatedPacks = new ArrayList<>();
        if (hypixelResourcePackAPIResponse.getAsJsonObject().has("packs")) {
            JsonArray packs = hypixelResourcePackAPIResponse.getAsJsonObject().get("packs").getAsJsonArray();
            for (JsonElement pack : packs) {
                String id = pack.getAsJsonObject().get("id").getAsString();
                Long lu = pack.getAsJsonObject().get("lastUpdated").getAsLong();
                if (!lastUpdated.containsKey(id) || lastUpdated.get(id) < lu) {
                    Logger.log("Detected update in pack: " + id);
                    updatedPacks.add(id);
                }
            }
        }
        if (updatedPacks.isEmpty()) { return; }

        Map<String, byte[]> allFiles = new HashMap<>();
        for (String id : updatedPacks) {
            for (JsonElement pack : hypixelResourcePackAPIResponse.getAsJsonObject().get("packs").getAsJsonArray()) {
                if (pack.getAsJsonObject().get("id").getAsString().equals(id)) {
                    for (JsonElement version : pack.getAsJsonObject().get("versions").getAsJsonArray()) {
                        int packFormat = version.getAsJsonObject().get("packFormat").getAsInt();
                        String url = version.getAsJsonObject().get("url").getAsString();
                        try {
                            Map<String, byte[]> files = downloadAndExtract(url, "packs/" + id + "/" + packFormat + "/");
                            allFiles.putAll(files);
                        } catch (Exception e) {
                            Logger.logError("Something went wrong downloading/extracting: " + e);
                        }
                    }
                }
            }
        }
        allFiles.put("response.json", String.valueOf(hypixelResourcePackAPIResponse).getBytes(StandardCharsets.UTF_8));
        String commitMessage = "Updated " +
                String.join(", ", updatedPacks) +
                " resource pack(s)";
        try {
            bulkUploadGitHubFiles(getResourcePackRepo(), allFiles, commitMessage, "packs");
        } catch (IOException e) {
            Logger.logError("Something went wrong uploading resource packs to repo");
            throw new RuntimeException(e);
        }


        // TODO: Dont check the github every time, cache it as long as the bot is up, and update the cache only if the packs get updated
    }
}
