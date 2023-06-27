package com.mobiletv.app.javabot;

import java.util.ArrayList;
import java.util.List;

public class Java {
    private List<JavabotMessage> javabotMessages;

    public Java() {
        javabotMessages = new ArrayList<>();
    }

    public void addMessage(JavabotMessage javabotMessage) {
        javabotMessages.add(javabotMessage);
    }

    public List<JavabotMessage> getMessages() {
        return javabotMessages;
    }

    public void clearMessages() {
        javabotMessages.clear();
    }
}