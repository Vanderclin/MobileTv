package com.mobiletv.app.update;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mobiletv.app.R;

public class UpdateDialog {
    static void show(final Context context, String content, final String downloadUrl) {
        if (isContextValid(context)) {
            MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(context, R.style.MaterialDialog);
            mBuilder.setTitle(R.string.new_update);
            mBuilder.setMessage(content);
            mBuilder.setPositiveButton(R.string.download, (dialog, id) -> {
                goToDownload(context, downloadUrl);
            });
            mBuilder.setCancelable(false);
            mBuilder.show();
        }
    }

    private static boolean isContextValid(Context context) {
        return context instanceof Activity && !((Activity) context).isFinishing();
    }

    private static void goToDownload(Context context, String downloadUrl) {
        Intent intent = new Intent(context.getApplicationContext(), DownloadService.class);
        intent.putExtra(Constants.UPDATE_APK, downloadUrl);
        context.startService(intent);
    }
}
