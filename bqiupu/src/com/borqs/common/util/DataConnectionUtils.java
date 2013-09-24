package com.borqs.common.util;

import android.app.Activity;
import com.borqs.qiupu.db.QiupuORM;
import twitter4j.conf.ConfigurationBase;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.borqs.qiupu.R;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: b608
 * Date: 11-10-17
 * Time: 下午12:23
 * To change this template use File | Settings | File Templates.
 */
public class DataConnectionUtils {
    private static final String TAG = "DataConnectionUtils";

    private static boolean mLastShowSavedMode;

    /**
     *  Global flag to assure not to show multiple alert dialog via repeatedly invoking showConnectivityAlertDialog.
     */
    private static boolean mIsConnectivityAlertDialog = false;
    private static boolean mIsPrivacyAlertDialog = false;

    public static boolean testValidConnection(Context context) {
        final NetworkInfo activeInfo = getActiveNetworkInfo(context);
        return (null != activeInfo);
    }

    public static boolean testSavedModeValidConnection(final Activity context) {
        if (testValidConnection(context)) {
            final boolean isAutoSavedMode = QiupuORM.isDataFlowAutoSaveMode(context);
            if (isWiFiConnection(context)) {
                if (isAutoSavedMode && mLastShowSavedMode != isAutoSavedMode) {
                    mLastShowSavedMode = isAutoSavedMode;
                    DialogUtils.showConfirmDialog(context, R.string.disable_data_flow_saved_mode_title,
                            R.string.disable_data_flow_saved_mode_message, R.string.label_ok, R.string.label_cancel, 
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            QiupuORM.setDataFlowAutoSaveMode(context, false);
                        }
                    });
                }
            } else {
                if (!isAutoSavedMode && mLastShowSavedMode == isAutoSavedMode) {
                    mLastShowSavedMode = !isAutoSavedMode;
                    DialogUtils.showConfirmDialog(context, R.string.enable_data_flow_saved_mode_title,
                            R.string.enable_data_flow_saved_mode_message, R.string.label_ok, R.string.label_cancel,
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            QiupuORM.setDataFlowAutoSaveMode(context, true);
                        }
                    });
                }
            }
            return true;
        }

        return false;
    }

    private static boolean isWiFiConnection(Context context) {
        final NetworkInfo activeInfo = getActiveNetworkInfo(context);
        return null != activeInfo && activeInfo.getTypeName().equalsIgnoreCase("WIFI");
    }

    public static boolean alarmTestValidConnection(Context context) {
        final NetworkInfo activeInfo = getActiveNetworkInfo(context);
        if (null == activeInfo) {
            showConnectivityAlertDialog(context);
            return false;
        }

        return true;
    }

    /**
     *  Detect if there is an active connectivity, or show alert dialog about invalid connection.
     * @param context, activity or view context, should not use Base Context of an activity or application context, which
     *  will fail to show alert dialog because of null window.
     */
    private static void showConnectivityAlertDialog(Context context) {
        if (mIsConnectivityAlertDialog) {
            Log.i(TAG, "showConnectivityAlertDialog, ignore the alert dialog is shown.");
            return;
        }

        mIsConnectivityAlertDialog = true;

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(context);
		dlgBuilder
				.setTitle(R.string.title_no_connectivity)
				.setMessage(R.string.dlg_msg_no_active_connectivity)
				.setNegativeButton(R.string.label_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
                                mIsConnectivityAlertDialog = false;
							}
						});
		dlgBuilder.create().show();
    }

    public static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = cm.getActiveNetworkInfo();
        return activeInfo;
    }

    private static HashMap<String, String> mCachedServerUrl = new HashMap<String, String>();
    public static void cleanCurrentServerUrl() {
        mCachedServerUrl.clear();
    }

    private static final String API_HOST = "api_host";
    public static String getCurrentApiHost(Context context) {
        return getCurrentServerUrl(context, API_HOST);
    }

    public static String getCurrentServerUrl(Context context, String appKey) {
        final boolean skipCache = appKey.equals(API_HOST);
        String serverUrl = mCachedServerUrl.get(appKey);
        if (TextUtils.isEmpty(serverUrl) || skipCache) {
            String urlHost = QiupuORM.queryHostConfiguration(context, appKey);

            // NullPointException urlHost = null;
            if(TextUtils.isEmpty(urlHost)) {
            	urlHost = ConfigurationBase.DEFAULT_BORQS_URL;
            }

            Log.d(TAG, "getCurrentServerUrl, return url name: " + urlHost);
            serverUrl = urlHost.endsWith("/") ? urlHost : urlHost + "/";

            if (!skipCache) {
                mCachedServerUrl.put(appKey, serverUrl);
            }
        }

        return serverUrl;
    }

    public static void showPrivacyPolicyInfo(Context context) {
        if (mIsPrivacyAlertDialog) {
            Log.i(TAG, "showPrivacyPolicyInfo, ignore the alert dialog is shown.");
            return;
        }

        mIsPrivacyAlertDialog = true;

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(context);
        dlgBuilder
                .setTitle(R.string.pref_string_privacy_info_title)
                .setMessage(R.string.pref_string_privacy_info_description)
                .setNegativeButton(R.string.label_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                mIsPrivacyAlertDialog = false;
                            }
                        });
        dlgBuilder.create().show();
    }
}
