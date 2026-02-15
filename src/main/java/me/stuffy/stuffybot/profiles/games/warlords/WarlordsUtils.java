package me.stuffy.stuffybot.profiles.games.warlords;

public class WarlordsUtils {
    public static String getSpecName(String specNameRaw) {
        return switch (specNameRaw.toLowerCase()) {
            case "pyromancer" -> "Pyromancer";
            case "cryomancer" -> "Cryomancer";
            case "aquamancer" -> "Aquamancer";
            case "berserker" -> "Berserker";
            case "defender" -> "Defender";
            case "revenant" -> "Revenant";
            case "avenger" -> "Avenger";
            case "crusader" -> "Crusader";
            case "protector" -> "Protector";
            case "thunderlord" -> "Thunderlord";
            case "earthwarden" -> "Earthwarden";
            case "spiritguard" -> "Spiritguard";
            default -> "Unknown Spec";
        };
    }

    public static String getSpecName(String classNameRaw, int specId) {
        return switch (classNameRaw) {
            case "mage" -> switch (specId) {
                case 0 -> "Pyromancer";
                case 1 -> "Cryomancer";
                case 2 -> "Aquamancer";
                default -> null;
            };
            case "warrior" -> switch (specId) {
                case 0 -> "Berserker";
                case 1 -> "Defender";
                case 2 -> "Revenant";
                default -> null;
            };
            case "paladin" -> switch (specId) {
                case 0 -> "Avenger";
                case 1 -> "Crusader";
                case 2 -> "Protector";
                default -> null;
            };
            case "shaman" -> switch (specId) {
                case 0 -> "Thunderlord";
                case 1 -> "Earthwarden";
                case 2 -> "Spiritguard";
                default -> null;
            };
            default -> null;
        };
    }

    public static String getClassName(String classNameRaw) {
        return switch (classNameRaw) {
            case "mage" -> "Mage";
            case "warrior" -> "Warrior";
            case "paladin" -> "Paladin";
            case "shaman" -> "Shaman";
            default -> null;
        };
    }

    public static String getClassRaw(int classId) {
        return switch (classId) {
            case 0 -> "mage";
            case 1 -> "warrior";
            case 2 -> "paladin";
            case 3 -> "shaman";
            default -> null;
        };
    }
}
