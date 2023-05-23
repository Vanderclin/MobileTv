package com.mobiletv.app.update;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends IntentService {
    private static final int BUFFER_SIZE = 10 * 1024; // 8k ~ 32K
    private static final String TAG = "DownloadService";
    private int oldProgress = 0;
    private int progress;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String urlStr = intent.getStringExtra(Constants.UPDATE_APK);

        try {
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

            urlConnection.connect();

            long bytetotal = urlConnection.getContentLength();
            long bytesum = 0;
            int byteread;
            byte[] buffer = new byte[BUFFER_SIZE];

            InputStream in = urlConnection.getInputStream();
            File dir = StorageUtils.getCacheDirectory(this);
            String apkName = urlStr.substring(urlStr.lastIndexOf("/") + 1);
            File apkFile = new File(dir, apkName);
            FileOutputStream out = new FileOutputStream(apkFile);
            while ((byteread = in.read(buffer)) != -1) {
                bytesum += byteread;
                out.write(buffer, 0, byteread);

                progress = (int) (bytesum * 100L / bytetotal);
                if (progress != oldProgress) {
                    // Atualize o progresso aqui
                }
                oldProgress = progress;
            }

            UtilsPackage.installAPk(this, apkFile);
        } catch (Exception e) {
            Log.e(TAG, "download apk file error: " + e.getMessage());
        }
    }
}
