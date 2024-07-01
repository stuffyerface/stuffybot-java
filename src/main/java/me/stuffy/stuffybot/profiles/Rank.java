package me.stuffy.stuffybot.profiles;

public enum Rank {
    ADMIN,
    GAME_MASTER,
    YOUTUBER,
    MVP_PLUS_PLUS,
    MVP_PLUS,
    MVP,
    VIP_PLUS,
    VIP,
    NONE;

    public static Rank fromString(String rankString) {
        if (rankString != null) {
            for (Rank rank : Rank.values()) {
                if (rankString.equalsIgnoreCase(rank.name())) {
                    return rank;
                }
            }
        }
        return NONE;
    }
}
