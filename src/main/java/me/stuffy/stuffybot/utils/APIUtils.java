package me.stuffy.stuffybot.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import me.stuffy.stuffybot.Bot;
import me.stuffy.stuffybot.commands.TournamentCommand;
import me.stuffy.stuffybot.profiles.HypixelProfile;
import me.stuffy.stuffybot.profiles.MojangProfile;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static me.stuffy.stuffybot.utils.Logger.log;
import static me.stuffy.stuffybot.utils.Logger.logError;

public class APIUtils {
    static String hypixelApiUrl = "https://api.hypixel.net/v2/";
    static String mojangApiUrl = "https://api.mojang.com/";
    static String mojangSessionApiUrl = "https://sessionserver.mojang.com/";
    static String privateApiRepo = "stuffybot/PrivateAPI";
    static String publicApiRepo = "stuffybot/PublicAPI";
    static String stuffyApiUrl = "https://raw.githubusercontent.com/stuffybot/PublicAPI/main/";

    public static HypixelProfile getHypixelProfile(String username) throws APIException {
        MojangProfile profile = getMojangProfile(username);
        String ign = profile.getUsername();
        return getHypixelProfile(profile.getUuid()).setDisplayName(ign);
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
                logError(response.body());
                logError("Hypixel API Error [Status Code: " + response.statusCode() + "] [UUID: " + uuid + "]");
                throw new APIException("Hypixel", "A field is missing, this should never happen.");
            }
            case 403 -> {
                logError(response.body());
                logError("Hypixel API Error [Status Code: " + response.statusCode() + "] [UUID: " + uuid + "]");
                throw new APIException("Hypixel", "Invalid API Key, contact the Stuffy immediately.");
            }
            case 429 -> {
                logError(response.body());
                logError("Hypixel API Error [Status Code: " + response.statusCode() + "] [UUID: " + uuid + "]");
                throw new APIException("Hypixel", "Rate limited by Hypixel API, try again later.");
            }
            default -> {
                logError(response.body());
                logError("Unknown Hypixel API Error [Status Code: " + response.statusCode() + "] [UUID: " + uuid + "]");
                throw new APIException("Hypixel", "I've never seen this error before.");
            }
        }
    }

    private static final LoadingCache<String, MojangProfile> mojangProfileCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(
                    new CacheLoader<String, MojangProfile>() {
                        public MojangProfile load(@NotNull String username) {
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

    private static final LoadingCache<UUID, MojangProfile> mojangProfileUUIDCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(
                    new CacheLoader<UUID, MojangProfile>() {
                        public MojangProfile load(@NotNull UUID uuid) {
                            try{
                                return fetchMojangProfile(uuid);
                            } catch (APIException e){
                                throw new RuntimeException(e);
                            }
                        }
                    }
            );

    public static MojangProfile getMojangProfile(UUID uuid) throws APIException{
        try{
            return mojangProfileUUIDCache.getUnchecked(uuid);
        } catch (RuntimeException e){
            if (e.getCause().getCause() instanceof APIException) {
                throw (APIException) e.getCause().getCause();
            } else {
                throw e;
            }
        }
    }

    public static MojangProfile fetchMojangProfile(UUID uuid) throws APIException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(mojangSessionApiUrl + "session/minecraft/profile/" + uuid.toString()))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.sendAsync(getRequest, HttpResponse.BodyHandlers.ofString()).join();
        switch (response.statusCode()) {
            case 200 -> {
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(response.body());
                JsonObject object = element.getAsJsonObject();
                String name = object.get("name").getAsString();
                return new MojangProfile(name, uuid);
            }
            case 204,404 -> {
                logError("Mojang API Error [Status Code: " + response.statusCode() + "] [UUID: " + uuid + "]");
                throw new APIException("Mojang", "There is no Minecraft account with that UUID.");
            }
            case 429 -> {
                logError("Mojang API Error [Status Code: " + response.statusCode() + "] [UUID: " + uuid + "]");
                throw new APIException("Mojang", "Rate limited by Mojang API, try again later.");
            }
            default -> {
                logError("Unknown Mojang API Error [Status Code: " + response.statusCode() + "] [UUID: " + uuid + "]");
                throw new APIException("Mojang", "I've never seen this error before.");
            }
        }
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

    private static final LoadingCache<String, JsonElement> playCommandCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(
                    new CacheLoader<>() {
                        public JsonElement load(@NotNull String key) {
                            return fetchPlayCommands();
                        }
                    }
            );

    public static JsonElement getPlayCommands() {
        return playCommandCache.getUnchecked("achievements");
    }

    private static JsonElement fetchPlayCommands() {
        try {
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(stuffyApiUrl + "apis/playcommands.json"))
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {

                JsonParser parser = new JsonParser();

                return parser.parse(response.body());

            } else {
                throw new IllegalStateException("Unexpected response from Stuffy API: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch play commands from Stuffy API", e);
        }
    }


    public static JsonObject getTournamentData() {
        // # TODO: Switch to https://raw.githubusercontent.com/stuffybot/PublicAPI/main/apis/tournaments.json
        try (InputStream inputStream = TournamentCommand.class.getResourceAsStream("/data/tournaments.json")) {
            assert inputStream != null;
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static GitHub connectToGitHub() {
        try {
            GitHub github = new GitHubBuilder().withOAuthToken(System.getenv("GITHUB_OAUTH")).build();
            log("<GitHub> Connected to GitHub as " + github.getMyself().getLogin() + " successfully.");
            return github;
        } catch (IOException e) {
            logError("<GitHub> Failed to connect to GitHub: " + e.getMessage());
            return null;
        }
    }

    public static void updateGitHubFile(String repo, String path, String content, String message) {
        try {
            Bot.getGitHub().getRepository(repo).createContent()
                    .path(path)
                    .content(content)
                    .message(message)
                    .sha(Bot.getGitHub().getRepository(repo).getFileContent(path).getSha())
                    .commit();
            log("<GitHub> Updated file " + path + " in " + repo + " successfully.");
        } catch (IOException e) {
            logError("<GitHub> Failed to update file " + path + " in " + repo + ": " + e.getMessage());
        }
    }

    public static void uploadGitHubFile(String repo, String path, String content, String message) {
        try {
            Bot.getGitHub().getRepository(repo).createContent()
                    .path(path)
                    .content(content)
                    .message(message)
                    .commit();
            log("<GitHub> Uploaded file " + path + " in " + repo + " successfully.");
        } catch (IOException e) {
            logError("<GitHub> Failed to upload file " + path + " in " + repo + ": " + e.getMessage());
        }
    }

    public static GHContent getGitHubFile(String repo, String path) {
        try {
            return Bot.getGitHub().getRepository(repo).getFileContent(path);
        } catch (IOException e) {
            logError("<GitHub> Failed to get file " + path + " in " + repo + ": " + e.getMessage());
            return null;
        }
    }

    public static String getPrivateApiRepo() {
        return privateApiRepo;
    }


    public static void updateBotStats(int totalServers, Map<String, Integer> commandsRun) {
        GHContent botStats = getGitHubFile(privateApiRepo, "apis/bot.json");
        if (botStats == null) throw new IllegalStateException("Failed to get bot.json from GitHub");

        try {
            // Read the JSON content
            String botStatsContent = readFile(botStats);
            JsonObject fullJson = JsonParser.parseString(botStatsContent).getAsJsonObject();

            // Update the JSON content
            fullJson.addProperty("lastUpdated", System.currentTimeMillis());
            JsonArray allBots = fullJson.get("bots").getAsJsonArray();
            String botId = Bot.getInstance().getJDA().getSelfUser().getId();

            JsonObject bot = null;
            for (JsonElement element : allBots) {
                JsonObject currentBot = element.getAsJsonObject();
                if (currentBot.get("id").getAsString().equals(botId)) {
                    bot = currentBot;
                    break;
                }
            }

            if (bot == null) {
                throw new IllegalStateException("Failed to find bot in bot.json");
            }

            bot.addProperty("servers", totalServers);

            JsonObject commands = bot.get("commandsRun").getAsJsonObject();

            for (Map.Entry<String, Integer> entry : commandsRun.entrySet()) {
                String command = entry.getKey();
                int count = entry.getValue();
                int currentCount = 0;
                if(commands.has(command)) currentCount = commands.get(command).getAsInt();
                commands.addProperty(command, currentCount + count);
            }

            fullJson.add("bots", allBots);

            // Write the updated content back to the JSON file
            updateGitHubFile(privateApiRepo, "apis/bot.json", fullJson.toString(), "Updated bot stats.");
            log("<Stats> Updated bot stats for " + totalServers + " servers.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateUsersStats(Map<String, String> uniqueUsers, Map<String, Integer> userCommandsRun) {
        GHContent linkedDB = getGitHubFile(privateApiRepo, "apis/linkeddb.csv");
        if (linkedDB == null) throw new IllegalStateException("Failed to get linkeddb.csv from GitHub");

        try {
            // Read the CSV content
            String linkedDBContent = readFile(linkedDB);
            List<String[]> csvData;
            try (CSVReader reader = new CSVReader(new StringReader(linkedDBContent))) {
                csvData = reader.readAll();
            }

            int newUsers = 0;
            int updatedUsers = 0;
            int newCommandsRun = 0;
            // Update the CSV content
            for (String discordId : uniqueUsers.keySet()) {
                String discordName = uniqueUsers.get(discordId);
                int commandsRun = userCommandsRun.getOrDefault(discordId, 0);
                newCommandsRun += commandsRun;
                boolean updated = false;
                for (String[] row : csvData) {
                    if (row[0].equals(discordId)) {
                        row[1] = discordName;
                        if (row[5].isEmpty()) row[5] = "0";
                        row[5] = String.valueOf(commandsRun + Integer.parseInt(row[5]));
                        updated = true;
                        updatedUsers++;
                        break;
                    }
                }
                if (!updated) {
                    newUsers++;
                    csvData.add(new String[]{discordId, discordName, "", "", "", String.valueOf(commandsRun)});
                }
            }

            // Write the updated content back to the CSV file
            StringWriter stringWriter = new StringWriter();
            try (CSVWriter writer = new CSVWriter(stringWriter)) {
                writer.writeAll(csvData);
            }
            String updatedContent = stringWriter.toString();
            updateGitHubFile(privateApiRepo, "apis/linkeddb.csv", updatedContent,
                    "Added `" + newCommandsRun + "` new commands run by `" + newUsers + "` new users and `"
                            + updatedUsers + "` existing users.");
            Logger.log("<Stats> Updated stats for " + newUsers + " new users and " + updatedUsers + " existing users.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFile(GHContent content) {
        try {
            InputStream inputStream = content.read();
            InputStreamReader reader = new InputStreamReader(inputStream);
            StringBuilder file = new StringBuilder();
            int character;
            while ((character = reader.read()) != -1) {
                file.append((char) character);
            }
            return file.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateLinkedDB(String discordId, UUID uuid, String ign) {
        String discordName = Bot.getGlobalData().getUniqueUsers().getOrDefault(discordId, "NULL");

        GHContent linkedDB = getGitHubFile(privateApiRepo, "apis/linkeddb.csv");
        if (linkedDB == null) throw new IllegalStateException("Failed to get linkeddb.csv from GitHub");

        try {
            // Read the CSV content
            String linkedDBContent = readFile(linkedDB);
            List<String[]> csvData;
            try (CSVReader reader = new CSVReader(new StringReader(linkedDBContent))) {
                csvData = reader.readAll();
            }

            // Update the CSV content
            boolean updated = false;
            for (String[] row : csvData) {
                if (row[0].equals(discordId)) {
                    row[1] = discordName;
                    row[2] = uuid.toString();
                    row[3] = ign;
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                csvData.add(new String[]{discordId, discordName, uuid.toString(), ign, "", ""});
            }

            // Write the updated content back to the CSV file
            StringWriter stringWriter = new StringWriter();
            try (CSVWriter writer = new CSVWriter(stringWriter)) {
                writer.writeAll(csvData);
            }
            String updatedContent = stringWriter.toString();
            updateGitHubFile(privateApiRepo, "apis/linkeddb.csv", updatedContent, "`@" + discordName + "` linked as `" + ign + "`");
            Bot.getGlobalData().addLinkedAccount(discordId, uuid);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void uploadLogs() {
        try {
            // Read the logs content
            String logsContent = String.join("\n", Logger.getLogs());

            String logName = Logger.getLogName();

            // Write the updated content back to the logs file
            uploadGitHubFile(privateApiRepo, "apis/logs/" + Bot.getInstance().getJDA().getSelfUser().getAsTag() + "/" + logName + ".txt", logsContent, "Uploaded logs for " + logName + ".");
            Logger.log("<Logs> Uploaded logs to GitHub.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
