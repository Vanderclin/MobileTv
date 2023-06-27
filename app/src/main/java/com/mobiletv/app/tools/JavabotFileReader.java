package com.mobiletv.app.tools;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JavabotFileReader {

    public List<JavabotFileMessage> readJavabotFile() {

        List<JavabotFileMessage> messages = new ArrayList<>();

        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/Javabot/javabot.jb");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split("=\\[\\{");
                if (parts.length == 2) {

                    //String timestampString = parts[0];
                    String dataString = parts[1].replace("}];", "");

                    // long timestamp = Long.parseLong(timestampString);
                    String[] dataValues = dataString.split("\",\"");
                    if (dataValues.length == 2) {
                        String message = dataValues[0].replace("\"", "");
                        String name = dataValues[1].replace("\"", "");

                        JavabotFileMessage javabotFileMessage = new JavabotFileMessage(message, name);
                        messages.add(javabotFileMessage);
                    }
                }
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public List<JavabotFileMessage> openFileReader(Context context, String filePath) {
        List<JavabotFileMessage> messages = new ArrayList<>();
        try {
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split("=\\[\\{");
                if (parts.length == 2) {
                    //String timestampString = parts[0];
                    String dataString = parts[1].replace("}];", "");
                    // long timestamp = Long.parseLong(timestampString);
                    String[] dataValues = dataString.split("\",\"");
                    if (dataValues.length == 2) {
                        String message = dataValues[0].replace("\"", "");
                        String name = dataValues[1].replace("\"", "");
                        JavabotFileMessage javabotFileMessage = new JavabotFileMessage(message, name);
                        messages.add(javabotFileMessage);
                    }
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return messages;
    }

}
