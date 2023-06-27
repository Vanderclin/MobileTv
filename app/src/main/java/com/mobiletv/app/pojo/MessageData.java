package com.mobiletv.app.pojo;

public class MessageData {

    private String timestamp;
    private String message;
    private String author;
    private boolean isBot;

    public MessageData(String timestamp, String message, String author, boolean isBot) {
        this.timestamp = timestamp;
        this.message = message;
        this.author = author;
        this.isBot = isBot;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isBot() {
        return isBot;
    }
}