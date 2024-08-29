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

import static me.stuffy.stuffybot.utils.Logger.logError;

public class APIUtils {
    static String hypixelApiUrl = "https://api.hypixel.net/v2/";
    static String mojangApiUrl = "https://api.mojang.com/";
    public static HypixelProfile getHypixelProfile(String username) throws APIException {
        MojangProfile profile = getMojangProfile(username);
        return getHypixelProfile(profile.getUuid());
    }

    private static final LoadingCache<UUID, HypixelProfile> hypixelProfileCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<UUID, HypixelProfile>() {
                        public HypixelProfile load(@NotNull UUID uuid){
                            try {
                                return fetchHypixelProfile(uuid);
                            } catch (APIException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
            );

    public static HypixelProfile getHypixelProfile(UUID uuid) throws APIException {
        try {
            return hypixelProfileCache.getUnchecked(uuid);
        } catch (RuntimeException e) {
            if (e.getCause().getCause() instanceof APIException) {
                throw (APIException) e.getCause().getCause();
            } else {
                throw e;
            }
        }
    }

    /**
     * Returns the player endpoint from the Hypixel API given their Minecraft UUID
     * @param uuid
     * @return
     */
    public static HypixelProfile fetchHypixelProfile(UUID uuid) throws APIException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(hypixelApiUrl + "player?uuid=" + uuid))
                .header("API-Key", System.getenv("HYPIXEL_API_KEY"))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.sendAsync(getRequest, HttpResponse.BodyHandlers.ofString()).join();
        switch (response.statusCode()) {
            case 200 -> {
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(response.body());
                JsonObject object = element.getAsJsonObject();
                if (object.get("player").isJsonNull()) {
                    logError("Hypixel API Error [Status Code: " + response.statusCode() + "] [UUID: " + uuid + "] (Player is null)");
                    throw new APIException("Hypixel", "This player has never logged into Hypixel.");
                }

                return new HypixelProfile(object.get("player").getAsJsonObject());
            }
            case 400 -> {
                logError("Hypixel API Error [Status Code: " + response.statusCode() + "] [UUID: " + uuid + "]");
                throw new APIException("Hypixel", "A field is missing, this should never happen.");
            }
            case 403 -> {
                logError("Hypixel API Error [Status Code: " + response.statusCode() + "] [UUID: " + uuid + "]");
                throw new APIException("Hypixel", "Invalid API Key, contact the Stuffy immediately.");
            }
            case 429 -> {
                logError("Hypixel API Error [Status Code: " + response.statusCode() + "] [UUID: " + uuid + "]");
                throw new APIException("Hypixel", "Rate limited by Hypixel API, try again later.");
            }
            default -> {
                logError("Unknown Hypixel API Error [Status Code: " + response.statusCode() + "] [UUID: " + uuid + "]");
                throw new APIException("Hypixel", "I've never seen this error before.");
            }
        }
    }

    private static final LoadingCache<String, MojangProfile> mojangProfileCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(
                    new CacheLoader<String, MojangProfile>() {
                        public MojangProfile load(String username) {
                            try{
                                return fetchMojangProfile(username);
                            } catch (APIException e){
                                throw new RuntimeException(e);
                            }
                        }
                    }
            );

    public static MojangProfile getMojangProfile(String username) throws APIException{
        try{
            return mojangProfileCache.getUnchecked(username.toLowerCase());
        } catch (RuntimeException e){
            if (e.getCause().getCause() instanceof APIException) {
                throw (APIException) e.getCause().getCause();
            } else {
                throw e;
            }
        }
    }

    /**
     * Returns the uuid of a player from the Mojang API given their Minecraft username as a String, case-insensitive
     * @param username
     * @return profile
     */
    public static MojangProfile fetchMojangProfile(String username) throws APIException {
        MojangProfile profile;
        HttpRequest getRequest = HttpRequest.newBuilder()
            .uri(URI.create(mojangApiUrl + "users/profiles/minecraft/" + username))
            .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.sendAsync(getRequest, HttpResponse.BodyHandlers.ofString()).join();
        switch (response.statusCode()) {
            case 200 -> {
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(response.body());
                JsonObject object = element.getAsJsonObject();
                UUID uuid = MiscUtils.formatUUID(object.get("id").getAsString());
                String name = object.get("name").getAsString();
                profile = new MojangProfile(name, uuid);
            }
            case 204,404 -> {
                logError("Mojang API Error [Status Code: " + response.statusCode() + "] [Username: " + username + "]");
                throw new APIException("Mojang", "There is no Minecraft account with that username.");
            }
            case 429 -> {
                logError("Mojang API Error [Status Code: " + response.statusCode() + "] [Username: " + username + "]");
                throw new APIException("Mojang", "Rate limited by Mojang API, try again later.");
            }
            default -> {
                logError("Unknown Mojang API Error [Status Code: " + response.statusCode() + "] [Username: " + username + "]");
                throw new APIException("Mojang", "I've never seen this error before.");

            }
        }
        return profile;
    }

    private static final LoadingCache<String, JsonElement> achievementsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(
                    new CacheLoader<>() {
                        public JsonElement load(@NotNull String key) {
                            return fetchAchievementsResources();
                        }
                    }
            );

    public static JsonElement getAchievementsResources() {
        return achievementsCache.getUnchecked("achievements");
    }

    private static JsonElement fetchAchievementsResources() {
        try {
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(hypixelApiUrl + "resources/achievements"))
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {

                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(response.body());
                JsonObject object = element.getAsJsonObject();

                return object.get("achievements");

            } else {
                throw new IllegalStateException("Unexpected response from Hypixel API: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch achievements from Hypixel API", e);
        }
    }



}
