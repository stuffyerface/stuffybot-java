package me.stuffy.stuffybot.profiles;

public enum ProfileType {
    NORMAL,
    IRON_MAN,
    STRANDED,
    BINGO,
    UNKNOWN;

    public static ProfileType fromString(String profileString) {
        if (profileString != null) {
            for (ProfileType profileType : ProfileType.values()) {
                if (profileString.equalsIgnoreCase(profileType.name())) {
                    return profileType;
                }
            }
        }
        return UNKNOWN;
    }
}
