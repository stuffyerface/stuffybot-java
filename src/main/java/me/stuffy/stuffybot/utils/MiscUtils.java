package me.stuffy.stuffybot.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.UUID;

public class MiscUtils {
    public static UUID formatUUID(String uuid) {
        return UUID.fromString(uuid.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"
        ));
    }

    public static JsonElement getNestedJson(JsonObject object, String... keys) {
        JsonElement currentElement = object;
        for (String key : keys) {
            if (currentElement.isJsonObject() && currentElement.getAsJsonObject().has(key)) {
                currentElement = currentElement.getAsJsonObject().get(key);
            } else {
                throw new IllegalArgumentException("Key " + key + " not found or not a JsonObject");
            }
        }
        return currentElement;
    }
}
