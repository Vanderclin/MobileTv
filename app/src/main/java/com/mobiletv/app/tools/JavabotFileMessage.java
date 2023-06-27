package com.mobiletv.app.tools;

public class JavabotFileMessage {
    private final String message;
    private final String name;

    public JavabotFileMessage(String message, String name) {
        this.message = message;
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }
}
