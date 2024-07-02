package me.stuffy.stuffybot.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheLoader;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.profiles.MojangProfile;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class APIUtils {
    private static final LoadingCache<UUID, HypixelProfile> hypixelProfileCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<UUID, HypixelProfile>() {
                        public HypixelProfile load(@NotNull UUID uuid) {
                            return fetchHypixelProfile(uuid);
                        }
                    }
            );

    public static HypixelProfile getHypixelProfile(UUID uuid) {
        return hypixelProfileCache.getUnchecked(uuid);
    }

    /**
     * Returns the player endpoint from the Hypixel API given their Minecraft UUID
     * @param uuid
     * @return
     */
    public static HypixelProfile fetchHypixelProfile(UUID uuid) {
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
        } else {
            throw new IllegalArgumentException("Unknown error from HypixelAPI. Status Code: " + response.statusCode() + ".");
        }
    }

    private static final LoadingCache<String, MojangProfile> mojangProfileCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(
                    new CacheLoader<String, MojangProfile>() {
                        public MojangProfile load(String username) {
                            return fetchMojangProfile(username);
                        }
                    }
            );

    public static MojangProfile getMojangProfile(String username) {
        return mojangProfileCache.getUnchecked(username.toLowerCase());
    }

    /**
     * Returns the uuid of a player from the Mojang API given their Minecraft username as a String, case-insensitive
     * @param username
     * @return
     */
    public static MojangProfile fetchMojangProfile(String username) {
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
            UUID uuid = MiscUtils.formatUUID(object.get("id").getAsString());
            String name = object.get("name").getAsString();
            profile = new MojangProfile(name, uuid);
        } else {
            throw new IllegalArgumentException("Mojang API Error: " + response.statusCode() + " " + response.body());
        }
        return profile;
    }
}
