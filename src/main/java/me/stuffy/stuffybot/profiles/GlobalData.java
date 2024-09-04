package me.stuffy.stuffybot.profiles;

import com.opencsv.CSVReader;

import java.io.StringReader;
import java.util.*;

import static me.stuffy.stuffybot.utils.APIUtils.*;

public class GlobalData {
    private final Map<String, UUID> linkedAccounts;
    private final Map<String, Integer> commandsRun;
    private final Map<String, String> uniqueUsers;
    private final Map<String, Integer> userCommandsRun;

    public GlobalData() {
        String linkedContent = readFile(Objects.requireNonNull(getGitHubFile(getPrivateApiRepo(), "apis/linkeddb.csv")));

        List<String[]> csvData = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new StringReader(linkedContent))) {
            csvData = reader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean firstRow = true;
        Map<String, UUID> linkedAccounts = new HashMap<>();
        for (String[] row : csvData) {
            if(firstRow) {
                firstRow = false;
                continue;
            }
            try {
                if(Objects.equals(row[2], "")){
                    continue;
                }
                linkedAccounts.put(row[0], UUID.fromString(row[2]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.linkedAccounts = linkedAccounts;
        this.commandsRun = new HashMap<>();
        this.uniqueUsers = new HashMap<>();
        this.userCommandsRun = new HashMap<>();
    }

    public Map<String, UUID> getLinkedAccounts() {
        return this.linkedAccounts;
    }

    public void addLinkedAccount(String discordId, UUID uuid) {
        this.linkedAccounts.put(discordId, uuid);
    }

    public void incrementCommandsRun(String runnerId, String commandName) {
        this.commandsRun.put(commandName, this.commandsRun.getOrDefault(commandName, 0) + 1);
        this.userCommandsRun.put(runnerId, this.userCommandsRun.getOrDefault(runnerId, 0) + 1);
    }

    public Map<String, Integer> getCommandsRun() {
        return this.commandsRun;
    }

    public void clearCommandsRun() {
        this.commandsRun.clear();
        this.userCommandsRun.clear();
    }

    public void addUniqueUser(String discordId, String discordName) {
        this.uniqueUsers.put(discordId, discordName);
    }

    public Map<String, String> getUniqueUsers() {
        return this.uniqueUsers;
    }

    public void clearUniqueUsers() {
        this.uniqueUsers.clear();
    }

    public Map<String, Integer> getUserCommandsRun() {
        return this.userCommandsRun;
    }
}
