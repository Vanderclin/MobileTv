package com.mobiletv.app.update;

import android.content.Context;
import android.util.Log;

public class UpdateChecker {

    public static void checkForDialog(Context context) {
        if (context != null) {
            new UpdateTask(context, true).execute();
        } else {
            Log.e(Constants.UPDATE_TAG, "The arg context is null");
        }
    }

}
