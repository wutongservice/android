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

import static android.os.Environment.getExternalStorageDirectory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.common.account.AccountAdapter;
import com.borqs.common.transport.AccountClient;
import com.borqs.common.transport.SimpleHttpClient;

public class AppManager {

    private static final String TAG = "AppManager";

    public static final String SD_ROOT = getExternalStorageDirectory().getPath();

    public static final String CONTACT_PLUS_ROOT_PATH = SD_ROOT + "/contact_plus/";

    public static final String CONTACT_PLUS_DOWNLOAD_PATH = CONTACT_PLUS_ROOT_PATH + "download/";

    protected Context mContext;

    public AppManager(Context context) {
        mContext = context;
    }

    /**
     * get the appinfo by packageName from Borqs App store
     * 
     * @param context
     * @param packageName
     * @return null if the AppInfo get error!
     */
    public static AppInfo getAppInfo(Context context, String packageName) {
        AccountClient ac = new AccountClient(context, SimpleHttpClient.get());
        try {
            String result = ac.getAppInfo(packageName, AccountAdapter.getSessionId(context));
            List<AppInfo> apps = AppInfo.parse(result);
            if (apps != null) {
                for (AppInfo appInfo : apps) {
                    if (packageName.equals(appInfo.getPackageName())) {
                        return appInfo;
                    }
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * return the download sdcard path for apk file
     * 
     * @param packagename
     * @return
     * @throws DownloadException
     */
    public static String getDownloadSDCardPath(String packagename) throws DownloadException {
        String apkdownloadPath = "";
        String esStatus = Environment.getExternalStorageState();
        if (esStatus.equals(Environment.MEDIA_MOUNTED)) {
            apkdownloadPath = CONTACT_PLUS_DOWNLOAD_PATH;
        } else {
            throw new DownloadException(DownloadException.DOWNLOAD_EXCEPTION_ERROR_NO_SDCARD,
                    "no sdcard");
        }

        File destDir = new File(apkdownloadPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
            createAllWritableFolder(apkdownloadPath);
        }

        return apkdownloadPath + packagename + ".apk";
    }

    private static void createAllWritableFolder(String pathName) {
        if (TextUtils.isEmpty(pathName)) {
            Log.d(TAG, "createAllWritableFolder, invalid path name: " + pathName);
            return;
        }

        File pathFile = new File(pathName);
        if (!pathFile.exists()) {
            pathFile.mkdirs();

            Process p;
            int status;
            try {
                p = Runtime.getRuntime().exec("chmod 777 " + pathName);
                status = p.waitFor();
                if (status == 0) {
                    Log.i(TAG, "createAllWritableFolder, chmod succeed, " + pathName);
                } else {
                    Log.i(TAG, "createAllWritableFolder, chmod failed, " + pathName);
                }
            } catch (Exception ex) {
                Log.i(TAG, "createAllWritableFolder, chmod exception, " + pathName);
                ex.printStackTrace();
            }
        }
    }

}
