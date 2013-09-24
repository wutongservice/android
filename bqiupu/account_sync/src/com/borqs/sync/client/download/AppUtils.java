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

import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AppUtils {

    /**
     * check if the target package is installed
     * 
     * @param context
     * @param packageName the package you want to check
     * @return true(installed)
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        if (packages != null) {
            for (PackageInfo packageInfo : packages) {
                if (packageName.equals(packageInfo.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
