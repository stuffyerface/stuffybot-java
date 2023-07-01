package me.stuffy.stuffybot.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class MojangApiUtils {
    /**
     * Returns the uuid of a player from the Mojang API given their Minecraft username as a String, case insensitive
     * @param username
     * @return
     */
    public static MojangProfile getProfile(String username) {
        MojangProfile profile;
        HttpRequest getRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + username))
            .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.sendAsync(getRequest, HttpResponse.BodyHandlers.ofString()).join();

        if(response.statusCode() == 200) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(response.body());
            JsonObject object = element.getAsJsonObject();
            UUID uuid = UUID.fromString(object.get("id").getAsString().replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
            ));
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
