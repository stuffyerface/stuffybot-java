package me.stuffy.stuffybot.profiles;

import com.opencsv.CSVReader;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.*;

import static me.stuffy.stuffybot.utils.APIUtils.*;

public class GlobalData {
    private final Map<String, UUID> linkedAccounts;
    private final Map<String, Integer> sessionCommandsRun;
    private final Map<String, String> sessionUniqueUsers;
    private final Map<String, Integer> sessionUserCommandsRun;
    private final ArrayList<String> verifiedAccounts;

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
        ArrayList<String> verifiedAccounts = new ArrayList<String>();
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
            if(Objects.equals(row[4], "TRUE")){
                verifiedAccounts.add(row[0]);
            }
        }

        this.linkedAccounts = linkedAccounts;
        this.verifiedAccounts = verifiedAccounts;
        this.sessionCommandsRun = new HashMap<>();
        this.sessionUniqueUsers = new HashMap<>();
        this.sessionUserCommandsRun = new HashMap<>();
    }

    public Map<String, UUID> getLinkedAccounts() {
        return this.linkedAccounts;
    }

    public void addLinkedAccount(String discordId, UUID uuid) {
        this.linkedAccounts.put(discordId, uuid);
    }

    public void incrementCommandsRun(String runnerId, String commandName) {
        this.sessionCommandsRun.put(commandName, this.sessionCommandsRun.getOrDefault(commandName, 0) + 1);
        this.sessionUserCommandsRun.put(runnerId, this.sessionUserCommandsRun.getOrDefault(runnerId, 0) + 1);
    }

    public Map<String, Integer> getSessionCommandsRun() {
        return this.sessionCommandsRun;
    }

    public void clearCommandsRun() {
        this.sessionCommandsRun.clear();
        this.sessionUserCommandsRun.clear();
    }

    public void addUniqueUser(String discordId, String discordName) {
        this.sessionUniqueUsers.put(discordId, discordName);
    }

    public Map<String, String> getSessionUniqueUsers() {
        return this.sessionUniqueUsers;
    }

    public void clearUniqueUsers() {
        this.sessionUniqueUsers.clear();
    }

    public Map<String, Integer> getSessionUserCommandsRun() {
        return this.sessionUserCommandsRun;
    }

    public ArrayList<String> getVerifiedAccounts() {
        return verifiedAccounts;
    }

    public void setVerifiedAccount(String discordId, boolean verified) {
        if(verified) {
            this.verifiedAccounts.add(discordId);
        } else {
            this.verifiedAccounts.remove(discordId);
        }
    }
}
