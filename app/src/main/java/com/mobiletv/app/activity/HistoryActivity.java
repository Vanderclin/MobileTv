package com.mobiletv.app.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.mobiletv.app.R;
import com.mobiletv.app.tools.JavabotFileAdapter;
import com.mobiletv.app.tools.JavabotFileMessage;
import com.mobiletv.app.tools.JavabotFileReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView mListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        initializeFindViews();
        initializeIntent();
    }

    private void initializeFindViews() {
        MaterialToolbar mToolbar = findViewById(R.id.activity_toolbar_reader);
        mListView = findViewById(R.id.activity_reader_list);
        setSupportActionBar(mToolbar);
    }

    private void initializeIntent() {
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            String path = getPathFromUri(data);
            Toast.makeText(this, "Path:" + path, Toast.LENGTH_SHORT).show();
            if (path != null) {
                JavabotFileReader fileReader = new JavabotFileReader();
                List<JavabotFileMessage> messages = fileReader.openFileReader(this, path);
                JavabotFileAdapter adapter = new JavabotFileAdapter(this, messages);
                mListView.setAdapter(adapter);
            }
        }
    }

    private String getPathFromUri(Uri uri) {
        String path = null;
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            path = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            String[] projection = {MediaStore.MediaColumns.DATA};
            try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    if (columnIndex != -1) {
                        path = cursor.getString(columnIndex);
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            // Handle WhatsApp content scheme
            if (path == null && "com.whatsapp.provider.media".equals(uri.getAuthority())) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        File tempFile = createTempFile();
                        FileOutputStream outputStream = new FileOutputStream(tempFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        path = tempFile.getAbsolutePath();
                        inputStream.close();
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return path;
    }

    private File createTempFile() throws IOException {
        File cacheDir = getCacheDir();
        String fileName = "temp_" + System.currentTimeMillis();
        return File.createTempFile(fileName, null, cacheDir);
    }

}
