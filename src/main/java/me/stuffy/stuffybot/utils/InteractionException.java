package me.stuffy.stuffybot.utils;

public class InteractionException extends Throwable {
    String message;
    public InteractionException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}
