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

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.borqs.contacts_plus.R;

/**
 * download application
 * 
 * @author b211
 */
public class AppDownloadManager extends AppManager {

    private static final String TAG = "AppDownloadManager";

    public static final String DOWNLOAD_EXTRA_APP_INFO = "app_info";


    public AppDownloadManager(Context context) {
        super(context);
    }

    private boolean isNetworkReady() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo networkInfo : info) {
                    if (networkInfo.getState() == State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * download a file by packageName,we add packagename to
     * DOWNLOAD_APP_ROOT_URI ,then download it.
     * 
     * @param packageName
     * @throws DownloadException
     */
    public void startDownload(final AppInfo app) throws DownloadException {
        if (!isNetworkReady()) {
            throw new DownloadException(DownloadException.DOWNLOAD_EXCEPTION_ERROR_NO_NETWORK,
                    "no network");
        }
        download(app);
    }

    public void stopDownload(AppInfo app) {
    }

    private void download(AppInfo apk) throws DownloadException {
        if (apk == null || TextUtils.isEmpty(apk.getUrl())) {
            throw new DownloadException(DownloadException.DOWNLOAD_EXCEPTION_ERROR_INVALID_URL,
                    "invalid URL");
        }

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == false) {
            throw new DownloadException(DownloadException.DOWNLOAD_EXCEPTION_ERROR_NO_SDCARD,
                    "no sdcard");
        }

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO) {
            if (isInDownloading(mContext, apk.getPackageName())) {
                Log.i(TAG, "download, ongoing item: " + apk.getPackageName());
                return;
            }

            DownloadManager dm = (DownloadManager) mContext
                    .getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apk.getUrl()));
            request.setMimeType("application/vnd.android.package-archive");
            request.setTitle(apk.getLabel());
            request.setVisibleInDownloadsUi(true);
            request.setShowRunningNotification(true);

            String filestr = null;
            try {
                filestr = AppManager.getDownloadSDCardPath(apk.getPackageName());
            } catch (DownloadException e) {
                e.printStackTrace();
                Toast.makeText(mContext, "no sdcard", Toast.LENGTH_SHORT).show();
                return;
            }
            request.setDestinationUri(Uri.fromFile(new File(filestr)));

            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB) {
                try {
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE
                            | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                } catch (Exception ne) {
                }
            }
            long enqueue = dm.enqueue(request);

            Log.d(TAG, "new download=" + enqueue);
        } else {
            // download directly
            Intent downIntent = new Intent(Intent.ACTION_VIEW);
            downIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            downIntent.setData(Uri.parse(apk.getUrl()));
            try {
                mContext.startActivity(downIntent);
            } catch (Exception ne) {
                ne.printStackTrace();
            }
        }
    }

    /**
     * check if the app is downloading
     * 
     * @param con
     * @param uri the app uri
     * @return true if the app is downloading
     */
    public boolean isInDownloading(Context con, String uri) {
        DownloadManager dm = (DownloadManager) con.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        Cursor c = dm.query(query);
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    int uriColumnIndex = c.getColumnIndex(DownloadManager.COLUMN_URI);
                    int statusColumnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    String downloadingUri = c.getString(uriColumnIndex);
                    int status = c.getInt(statusColumnIndex);
                    if (uri.equals(downloadingUri)
                            && (status != DownloadManager.STATUS_FAILED && status != DownloadManager.STATUS_SUCCESSFUL)) {
                        return true;
                    }
                }
            } finally {
                c.close();
            }
        }
        return false;
    }

    /**
     * get download detail error message
     * 
     * @param context
     * @param errorCode
     * @return download detail error message
     */
    public static String getDownloadErrorMsg(Context context, int errorCode) {
        switch (errorCode) {
            case DownloadException.DOWNLOAD_EXCEPTION_ERROR_NO_NETWORK:
                return context.getString(R.string.text_download_error_no_network);
            case DownloadException.DOWNLOAD_EXCEPTION_ERROR_NO_SDCARD:
                return context.getString(R.string.text_download_error_no_sdcard);
            case DownloadException.DOWNLOAD_EXCEPTION_ERROR_INVALID_URL:
                return context.getString(R.string.text_download_invalid_url);
            case DownloadException.DOWNLOAD_EXCEPTION_ERROR_UNKNOWN:
            default:
                return context.getString(R.string.text_download_error_unknown);
        }
    }

}
