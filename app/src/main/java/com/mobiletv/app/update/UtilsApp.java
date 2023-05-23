package com.mobiletv.app.update;

import android.content.Context;
import android.content.pm.PackageManager;

public class UtilsApp {
    public static int getVersionCode(Context mContext) {
        if (mContext != null) {
            try {
                return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return 0;
    }
}
