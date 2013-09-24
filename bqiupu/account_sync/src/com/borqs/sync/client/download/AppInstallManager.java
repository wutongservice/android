/*
 * Copyright © 2012 Borqs Ltd.  All rights reserved.
 * 
 * This document is Borqs Confidential Proprietary 
 * and shall not be used, of published, or disclosed,
 * or disseminated outside of Borqs in whole or in part
 * without Borqs's permission.
 * 
 */

package com.borqs.sync.client.download;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class AppInstallManager extends AppManager {

    private static final String TAG = "AppInstallManager";

    public AppInstallManager(Context context) {
        super(context);
    }

    /**
     * install the apk by file path
     * @param filePath the file path in the sdcard
     */
    public void installApk(String filePath) {
        File file = new File(filePath);
        Log.d(TAG, "install apk=" + filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

}
