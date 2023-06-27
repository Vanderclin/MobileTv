package com.mobiletv.app.javabot;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mobiletv.app.R;

import java.io.File;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Javabot {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private final Map<String, String> chatMap;

    public Javabot(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        chatMap = new HashMap<>();
        String[] javabot_primary = context.getResources().getStringArray(R.array.javabot_primary);
        String[] javabot_secondary = context.getResources().getStringArray(R.array.javabot_secondary);

        for (int i = 0; i < javabot_primary.length; i++) {
            String question = normalize(javabot_primary[i]);
            String answer = javabot_secondary[i];
            chatMap.put(question, answer);
        }
    }

    public String getQuestion(Context context, String msg) {
        String formatted = normalize(msg);
        String response = chatMap.get(formatted);

        if (mAuth.getCurrentUser() != null) {
            String name = mUser.getDisplayName();
            String email = mUser.getEmail();
            if (response == null && !formatted.endsWith("?")) {
                formatted += "?";
                response = chatMap.get(formatted);
                String questionShare = normalize("Compartilhar");
                if (formatted.equals(questionShare)) {
                    shareHistory(context);
                }
            }
            if (response != null && response.contains("%1$s")) {
                String questionDate = normalize("Oi");
                String questionHour = normalize("Olá");
                if (formatted.equals(questionDate)) {
                    response = String.format(response, name);
                } else if (formatted.equals(questionHour)) {
                    response = String.format(response, name);
                }
            }
            return response != null ? response : context.getString(R.string.sorry_did_not_understand);
        }
        return response != null ? response : context.getString(R.string.user_not_logged_in);
    }

    private String getDate() {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(currentDate);
    }

    private String getHour() {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return dateFormat.format(currentDate);
    }

    private void deleteHistory(Context context) {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/Javabot/javabot.jb");
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    Toast.makeText(context, context.getString(R.string.history_deleted_successfully), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, context.getString(R.string.history_could_not_be_deleted), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, context.getString(R.string.history_file_does_not_exist), Toast.LENGTH_SHORT).show();
            }
        }, 1200);
    }

    private void shareHistory(Context context) {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/Javabot/javabot.jb");
            if (file.exists()) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                PackageManager packageManager = context.getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(shareIntent, 0);
                if (activities.size() > 0) {
                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_file)));
                } else {
                    Toast.makeText(context, context.getString(R.string.no_share_apps_available), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, context.getString(R.string.history_file_does_not_exist), Toast.LENGTH_SHORT).show();
            }
        }, 1200);
    }

    private String normalize(String input) {
        String normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalizedString = normalizedString.replaceAll("\\p{M}", "");
        return normalizedString.trim().toLowerCase();
    }
}
