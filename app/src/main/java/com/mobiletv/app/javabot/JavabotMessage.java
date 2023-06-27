package com.mobiletv.app.javabot;

public class JavabotMessage {

    private final boolean bot;
    private final String message;
    private final String name;

    public JavabotMessage(boolean bot, String message, String name) {
        this.bot = bot;
        this.message = message;
        this.name = name;
    }

    public boolean getBot() {
        return bot;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }
}