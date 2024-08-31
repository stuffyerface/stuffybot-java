package me.stuffy.stuffybot.utils;

public class InteractionException extends Throwable {
    String message;
    public InteractionException(String message) {
        this.message = message;
    }

    public InteractionException(String[] messages) {
        this.message = messages[(int) (Math.random() * messages.length)];
    }

    public String getMessage() {
        return this.message;
    }

}
