package com.mobiletv.app.update;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mobiletv.app.R;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateTask extends AsyncTask<Void, Void, String> {
    @SuppressLint("StaticFieldLeak")
    private final Context mContext;
    private ProgressDialog dialog;
    private final boolean mShowProgressDialog;
    private static final String url = Constants.UPDATE_JSON;

    public UpdateTask(Context context, boolean showProgressDialog) {
        this.mContext = context;
        this.mShowProgressDialog = showProgressDialog;
    }

    @Override
    protected void onPreExecute() {
        if (mShowProgressDialog) {
            dialog = new ProgressDialog(mContext, R.style.MaterialProgressDialog);
            dialog.setMessage(mContext.getString(R.string.checking_the_version));
            dialog.show();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if (!TextUtils.isEmpty(result)) {
            parseJson(result);
        }
    }

    private void parseJson(String result) {
        try {
            JSONObject obj = new JSONObject(result);
            String updateMessage = obj.getString(Constants.UPDATE_DESCRIPTION);
            String apkUrl = obj.getString(Constants.UPDATE_APK);
            int apkCode = obj.getInt(Constants.UPDATE_CODE);
            int versionCode = UtilsApp.getVersionCode(mContext);

            if (apkCode > versionCode) {
                showDialog(mContext, updateMessage, apkUrl);
            } else if (mShowProgressDialog) {
                Toast.makeText(mContext, mContext.getString(R.string.last_installed_version), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(Constants.UPDATE_TAG, "parse json error");
        }
    }

    private void showDialog(Context context, String content, String apkUrl) {
        UpdateDialog.show(context, content, apkUrl);
    }

    @Override
    protected String doInBackground(Void... args) {
        return HttpUtils.get(url);
    }
}
