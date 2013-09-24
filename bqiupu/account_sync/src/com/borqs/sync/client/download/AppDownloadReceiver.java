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

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.borqs.common.util.BLog;

public class AppDownloadReceiver extends BroadcastReceiver {

    private static final String TAG = "AppDownloadReceiver";

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action = intent.getAction();
        BLog.d(TAG, "action=" + intent);
        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            onDownloadCompleted(downloadId);
        } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
            // show download list
            try {
                Intent viewdownload = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                viewdownload.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(viewdownload);
            } catch (Exception ne) {
                ne.printStackTrace();
            }
            BLog.d(TAG, "notification clicked");
        }
    }

    // after download completed or click download,we should check download
    // status and install it if completed.
    private void onDownloadCompleted(long downloadId) {
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO) {
            DownloadManager dm = (DownloadManager) mContext
                    .getSystemService(Context.DOWNLOAD_SERVICE);
            {
                BLog.d(TAG, "new download complete=" + downloadId);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor c = dm.query(query);
                if (c.moveToFirst()) {
                    int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (android.app.DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

                        String uriString = c.getString(c
                                .getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                        if (uriString.startsWith("file://")) {
                            uriString = uriString.substring(7);
                        }
                        
                        if(uriString.endsWith(".apk"))
                        {
                            installApk(uriString);
                        }
                    } else if (android.app.DownloadManager.STATUS_FAILED == c.getInt(columnIndex)) {
                        BLog.d(TAG, "new download fail=" + downloadId);
                    }
                }
            }
        }
    }

    private void installApk(String filePath) {
        BLog.d(TAG,"install apk:" +filePath);
        AppInstallManager aim = new AppInstallManager(mContext);
        aim.installApk(filePath);
    }

}
