package me.stuffy.stuffybot.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.UUID;

public class MiscUtils {
    public static UUID formatUUID(String uuid) {
        return UUID.fromString(uuid.replaceFirst(
                "(\\w{8})(\\w{4}){3}(\\w{12})",
                "$1-$2-$3-$4-$5"));
    }

    public static JsonElement getNestedJson(JsonObject object, String... keys) {
        JsonObject currentObject = object;
        for (String key : keys) {
            if (currentObject.has(key)) {
                currentObject = currentObject.getAsJsonObject(key);
            } else {
                throw new IllegalArgumentException("Key " + key + " not found in " + object);
            }
        }
        return currentObject;
    }
}
