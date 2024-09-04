package me.stuffy.stuffybot.profiles;

import com.opencsv.CSVReader;

import java.io.StringReader;
import java.util.*;

import static me.stuffy.stuffybot.utils.APIUtils.*;

public class GlobalData {
    private final Map<String, UUID> linkedAccounts;
    private final Map<String, Integer> interactions;

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
                linkedAccounts.put(row[0], UUID.fromString(row[2]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.linkedAccounts = linkedAccounts;
        this.interactions = new HashMap<>();
    }

    public Map<String, UUID> getLinkedAccounts() {
        return this.linkedAccounts;
    }

    public void addLinkedAccount(String discordId, UUID uuid) {
        this.linkedAccounts.put(discordId, uuid);
    }

    public void incrementInteractions(String discordId) {
        this.interactions.put(discordId, this.interactions.getOrDefault(discordId, 0) + 1);
    }

    public Map<String, Integer> getInteractions() {
        return this.interactions;
    }

    public void clearInteractions() {
        this.interactions.clear();
    }

}
