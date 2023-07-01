package me.stuffy.stuffybot.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.profiles.MojangProfile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class APIUtils {
    /**
     * Returns the player endpoint from the Hypixel API given their Minecraft UUID
     * @param uuid
     * @return
     */
    public static HypixelProfile getHypixelProfile(UUID uuid) {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.hypixel.net/player?uuid=" + uuid))
                .header("API-Key", System.getenv("HYPIXEL_API_KEY"))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.sendAsync(getRequest, HttpResponse.BodyHandlers.ofString()).join();

        if(response.statusCode() == 200) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(response.body());
            JsonObject object = element.getAsJsonObject();
            return new HypixelProfile(object.get("player").getAsJsonObject());
            // Check for "prefix" for custom ranks
            // Check for "buildTeam" for build team
            // Check for "rank" for admin, gm, etc.
        } else if (response.statusCode() == 409) {
            Long ratelimitReset = Long.getLong(response.headers().firstValue("Ratelimit-Reset").get()); // This is maybe trash
            try {
                Thread.sleep(ratelimitReset * 1000); // This is maybe trash
                return getHypixelProfile(uuid);
            } catch (InterruptedException e) {
                throw new IllegalArgumentException("Unknown error from HypixelAPI. Status Code: " + response.statusCode() + ".", e);
            }
        } else {
            throw new IllegalArgumentException("Unknown error from HypixelAPI. Status Code: " + response.statusCode() + ".");
        }
    }

    /**
     * Returns the uuid of a player from the Mojang API given their Minecraft username as a String, case insensitive
     * @param username
     * @return
     */
    public static MojangProfile getMojangProfile(String username) {
        MojangProfile profile;
        HttpRequest getRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + username))
            .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.sendAsync(getRequest, HttpResponse.BodyHandlers.ofString()).join();

        if (response.statusCode() == 200) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(response.body());
            JsonObject object = element.getAsJsonObject();
            UUID uuid = MiscUtils.formatUUID((object.get("id").getAsString()));
            String name = object.get("name").getAsString();
            profile = new MojangProfile(name, uuid);
        } else if (response.statusCode() == 204) {
            throw new IllegalArgumentException("Invalid username");
        } else {
            throw new IllegalArgumentException("Unknown error from Mojang API");
        }
        return profile;
    }
}
