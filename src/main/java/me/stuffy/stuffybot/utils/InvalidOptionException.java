package me.stuffy.stuffybot.utils;

public class InvalidOptionException extends Throwable {
    String option;
    String provided;
    public InvalidOptionException(String option, String provided) {
        this.option = option;
        this.provided = provided.replaceAll("`", "'");
    }

    public String getMessage() {
        return "Invalid option `" + provided + "` in `" + option + "` field" + "\n-# Violence is not an option.";
    }

    private String randomMessage() {
        String[] messages = {
                "Violence is also not an option.",
                "Anything but that!",
                "Out of all the options, you chose none of them.",
        };
        return messages[(int) (Math.random() * messages.length)];
    }
}
