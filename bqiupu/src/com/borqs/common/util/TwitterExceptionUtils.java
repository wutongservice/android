package com.borqs.common.util;

import android.util.Log;
import com.borqs.qiupu.QiupuConfig;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by IntelliJ IDEA.
 * User: b608
 * Date: 11-11-14
 * Time: 下午10:14
 * To change this template use File | Settings | File Templates.
 */
public class TwitterExceptionUtils {
    public static void printException(final String TAG, final String summary, TwitterException ex) {
        Log.e(TAG, summary + ex.getDebugPrompt(QiupuConfig.LOGD, QiupuConfig.DBLOGD));
    }

    public static void printException(final String TAG, final String summary, TwitterException ex, TwitterMethod method) {
        Log.e(TAG, summary + ex.getDebugPrompt(QiupuConfig.LOGD, QiupuConfig.DBLOGD) + "==method=" + method.name());
    }
}
