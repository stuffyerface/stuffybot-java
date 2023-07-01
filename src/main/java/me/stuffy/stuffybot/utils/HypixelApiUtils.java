package me.stuffy.stuffybot.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class HypixelApiUtils {
    /**
     * Returns the player endpoint from the Hypixel API given their Minecraft UUID
     * @param uuid
     * @return
     */
    public static String getPlayer(String uuid) {
        String player = "";
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
            player = object.get("player").getAsJsonObject().toString();
        } else if (response.statusCode() == 409) {
            Long ratelimitReset = Long.getLong(response.headers().firstValue("Ratelimit-Reset").get());
            try {
                Thread.sleep(ratelimitReset * 1000);
                player = getPlayer(uuid);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("Unknown error from HypixelAPI. Status Code: " + response.statusCode() + ".");
        }
        return player;
    }

    public static String getDiscord(UUID uuid) {
        String player = getPlayer(uuid.toString());
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(player);
        JsonObject object = element.getAsJsonObject();
        String discord = object.get("socialMedia").getAsJsonObject().get("links").getAsJsonObject().get("DISCORD").getAsString();
        return discord;
    }
}
