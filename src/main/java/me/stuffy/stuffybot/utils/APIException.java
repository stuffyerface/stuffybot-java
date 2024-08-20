package me.stuffy.stuffybot.utils;

public class APIException extends Throwable {
    String apiType;
    String errorMessage;
    public APIException(String apiType, String message) {
        this.apiType = apiType;
        this.errorMessage = message;
    }
    public String getMessage() {
        return this.errorMessage;
    }

    public String getAPIType() {
        return this.apiType;
    }

}
