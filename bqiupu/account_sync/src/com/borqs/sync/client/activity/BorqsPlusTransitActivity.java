/*
 * Copyright © 2012 Borqs Ltd.  All rights reserved.
 * 
 * This document is Borqs Confidential Proprietary 
 * and shall not be used, of published, or disclosed,
 * or disseminated outside of Borqs in whole or in part
 * without Borqs's permission.
 * 
 */

package com.borqs.sync.client.activity;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.widget.Toast;

import com.borqs.common.util.BLog;
import com.borqs.contacts_plus.R;
import com.borqs.sync.client.common.BorqsPlusManagent;
import com.borqs.sync.client.common.BorqsPlusStruct;
import com.borqs.sync.client.download.AppDownloadManager;
import com.borqs.sync.client.download.AppInfo;
import com.borqs.sync.client.download.AppManager;
import com.borqs.sync.client.download.AppUtils;
import com.borqs.sync.client.download.DownloadException;

public class BorqsPlusTransitActivity extends Activity {

    private static final int DIALOG_DOWNLOAD_APK_PROMPT = 0;

    private static final int MSG_APP_DOWNLOAD_START = 1;

    private static final int MSG_APP_DOWNLOAD_ERROR = 2;

    private static final int MSG_APP_DOWNLOAD_IN_PROCESS = 3;

    private static final int MSG_APP_DOWNLOAD_FETCH_APP_ERROR = 4;

    private static final String DIALOG_BUNDLE_MESSAGE_KEY = "msg_key";

    private static final String DIALOG_BUNDLE_PACKAGE_KEY = "package_key";

    private Bundle mDialogBundle;

    private AlertDialog mDownloadPromptDialog;

    private LoadApplicationTask mLat;

    private AppDownloadManager mDownloadManager;

    private AppInfo mAppInfo;

    private MyHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDownloadManager = new AppDownloadManager(this);
        mHandler = new MyHandler();

        transit(getIntent());

    }

    private void transit(Intent intent) {
        String mimeType = intent.getType();
        String action = intent.getAction();
        Uri uri = intent.getData();
        // view group stream
        if (Intent.ACTION_VIEW.equals(action)
                && (!TextUtils.isEmpty(uri.toString()) && uri.toString().startsWith(
                        "content://" + ContactsContract.AUTHORITY + "/groups"))) {
            // content://com.android.contacts/groups
            BLog.d("view group stream");
            BorqsPlusStruct bps = getPlusStructByMimeType(BorqsPlusManagent.WUTONG_ACCOUNT_PLUS_MIME_TYPE);
            if (bps == null || TextUtils.isEmpty(bps.getPackageName())) {
                BLog.e("can get the packagename by mimeType:" + mimeType
                        + ",maybe the borqs_plus load error!");
                finish();
                return;
            }
            BLog.d("the packagename is :" + bps.getPackageName());
            if (AppUtils.isAppInstalled(this, bps.getPackageName())) {
                BLog.d("package:" + bps.getPackageName()
                        + " installed,we transit to UserCircleDetailActivity");
                // view group stream
                Intent groupStreamIntent = new Intent(Intent.ACTION_VIEW);
                ComponentName cn = new ComponentName("com.borqs.qiupu",
                        "com.borqs.qiupu.ui.bpc.UserCircleDetailActivity");
                groupStreamIntent.setData(uri);
                groupStreamIntent.setComponent(cn);
                startActivity(groupStreamIntent);
                finish();
            } else {
                BLog.d(bps.getPackageName() + " is not installed into device,we should download it");
                // user need to download the package
                mLat = new LoadApplicationTask(bps);
                mLat.execute();
            }

        } else if (Intent.ACTION_VIEW.equals(action)) {
            BLog.d("the mimetype is " + mimeType + ",we transit it ");
            // plus action
            BorqsPlusStruct bps = getPlusStructByMimeType(mimeType);
            if (bps == null || TextUtils.isEmpty(bps.getPackageName())) {
                BLog.e("can get the packagename by mimeType:" + mimeType
                        + ",maybe the borqs_plus load error!");
                finish();
                return;
            }
            BLog.d("the packagename is :" + bps.getPackageName());
            if (AppUtils.isAppInstalled(this, bps.getPackageName())) {
                goToPlus(getIntent(), bps.getActivityMimeType());
            } else {
                BLog.d(bps.getPackageName() + " is not installed into device,we should download it");
                // user need to download the package
                mLat = new LoadApplicationTask(bps);
                mLat.execute();
            }

        }
    }

    private BorqsPlusStruct getPlusStructByMimeType(String mimeType) {
        BorqsPlusManagent bpm = new BorqsPlusManagent(this);
        try {
            List<BorqsPlusStruct> bps = bpm.getBorqsPlus();
            if (bps != null) {
                for (BorqsPlusStruct borqsPlusStruct : bps) {
                    if (mimeType.equals(borqsPlusStruct.getMimeType())) {
                        return borqsPlusStruct;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void goToPlus(Intent oriIntent, String activityMimeType) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(oriIntent.getData(), activityMimeType);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            finish();
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
        mDialogBundle = bundle;
        String message = mDialogBundle.getString(DIALOG_BUNDLE_MESSAGE_KEY);
        mDownloadPromptDialog = (AlertDialog) dialog;
        switch (id) {
            case DIALOG_DOWNLOAD_APK_PROMPT:
                mDownloadPromptDialog.setMessage(message);
        }
        super.onPrepareDialog(id, dialog);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
        mDialogBundle = bundle;
        String message = mDialogBundle.getString(DIALOG_BUNDLE_MESSAGE_KEY);
        final String packageName = mDialogBundle.getString(DIALOG_BUNDLE_PACKAGE_KEY);
        switch (id) {
            case DIALOG_DOWNLOAD_APK_PROMPT:
                mDownloadPromptDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.text_download_dialog_title)
                        .setMessage(message)
                        .setIcon(R.drawable.contact_ic_sync_launcher_download)
                        .setCancelable(false)
                        .setPositiveButton(R.string.text_download_dialog_title,
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        onDownloadClick(packageName);
                                        finish();
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel, new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).create();
                mDownloadPromptDialog.setOnShowListener(new OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {
                        mDownloadPromptDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                                .setEnabled(false);
                    }
                });
                return mDownloadPromptDialog;
        }
        return super.onCreateDialog(id);
    }

    private void onDownloadClick(final String packageName) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Message msg = new Message();

                if (mAppInfo == null) {
                    mAppInfo = AppManager.getAppInfo(BorqsPlusTransitActivity.this, packageName);
                    if (mAppInfo == null) {
                        msg.what = MSG_APP_DOWNLOAD_FETCH_APP_ERROR;
                        mHandler.sendMessage(msg);
                        return;
                    }
                }

                if (mDownloadManager.isInDownloading(BorqsPlusTransitActivity.this,
                        mAppInfo.getUrl())) {
                    msg.what = MSG_APP_DOWNLOAD_IN_PROCESS;
                    mHandler.sendMessage(msg);
                } else {
                    try {
                        mDownloadManager.startDownload(mAppInfo);
                        msg.what = MSG_APP_DOWNLOAD_START;
                        mHandler.sendMessage(msg);
                    } catch (DownloadException e) {
                        msg.obj = e;
                        msg.what = MSG_APP_DOWNLOAD_ERROR;
                        mHandler.sendMessage(msg);
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    class LoadApplicationTask extends AsyncTask<Void, Void, Integer> {

        BorqsPlusStruct plusStruct;

        public LoadApplicationTask(BorqsPlusStruct plusStruct) {
            this.plusStruct = plusStruct;
        }

        @Override
        protected void onPreExecute() {
            String msg = null;
            if (BorqsPlusManagent.WUTONG_ACCOUNT_PLUS_MIME_TYPE.equals(plusStruct.getMimeType())) {
                msg = String.format(getString(R.string.text_download_wutong_dialog_message),
                        getString(R.string.text_download_dialog_message_loading));
            } else if (BorqsPlusManagent.OPENFACE_ACCOUNT_PLUS_MIME_TYPE.equals(plusStruct
                    .getMimeType())) {
                msg = String.format(getString(R.string.text_download_openface_dialog_message),
                        getString(R.string.text_download_dialog_message_loading));
            }
            Bundle bundle = new Bundle();
            bundle.putString(DIALOG_BUNDLE_MESSAGE_KEY, msg);
            bundle.putString(DIALOG_BUNDLE_PACKAGE_KEY, plusStruct.getPackageName());
            showDialog(DIALOG_DOWNLOAD_APK_PROMPT, bundle);
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (mDownloadPromptDialog != null && mDownloadPromptDialog.isShowing()) {
                String dialogMsg = null;
                Message msg = new Message();
                if (BorqsPlusManagent.WUTONG_ACCOUNT_PLUS_MIME_TYPE
                        .equals(plusStruct.getMimeType())) {
                    if (result > 0) {
                        dialogMsg = String.format(
                                getString(R.string.text_download_wutong_dialog_message),
                                String.valueOf(result / (1024 * 1024)) + "MB");
                        // TODO we should use a common fuction to compute the
                        // file
                        // size,like 1MB,1KB
                    } else {
                        msg.what = MSG_APP_DOWNLOAD_FETCH_APP_ERROR;
                        mHandler.sendMessage(msg);
                        dialogMsg = String.format(
                                getString(R.string.text_download_wutong_dialog_message),
                                getString(R.string.text_app_downloading_failed));
                    }

                } else if (BorqsPlusManagent.OPENFACE_ACCOUNT_PLUS_MIME_TYPE.equals(plusStruct
                        .getMimeType())) {
                    if (result > 0) {
                        dialogMsg = String.format(
                                getString(R.string.text_download_openface_dialog_message),
                                String.valueOf(result / (1024 * 1024)) + "MB");
                        // TODO we should use a common fuction to compute the
                        // file
                        // size,like 1MB,1KB
                    } else {
                        msg.what = MSG_APP_DOWNLOAD_FETCH_APP_ERROR;
                        mHandler.sendMessage(msg);
                        dialogMsg = String.format(
                                getString(R.string.text_download_openface_dialog_message),
                                getString(R.string.text_app_downloading_failed));
                    }

                }
                mDownloadPromptDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(
                        result > 0);
                mDownloadPromptDialog.setMessage(dialogMsg);
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (mAppInfo == null) {
                mAppInfo = AppManager.getAppInfo(BorqsPlusTransitActivity.this,
                        plusStruct.getPackageName());
            }
            return mAppInfo == null ? 0 : mAppInfo.getSize();
        }
    }

    @Override
    protected void onDestroy() {
        if (mLat != null && mLat.getStatus() != Status.FINISHED) {
            mLat.cancel(true);
        }
        super.onDestroy();
    }

    class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DownloadException e = (DownloadException) msg.obj;
            switch (msg.what) {
                case MSG_APP_DOWNLOAD_START:
                    Toast.makeText(
                            BorqsPlusTransitActivity.this,
                            String.format(getString(R.string.text_download_app_start),
                                    mAppInfo.getLabel()), Toast.LENGTH_SHORT).show();
                    break;
                case MSG_APP_DOWNLOAD_IN_PROCESS:
                    Toast.makeText(BorqsPlusTransitActivity.this, R.string.text_app_downloading,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MSG_APP_DOWNLOAD_FETCH_APP_ERROR:
                    Toast.makeText(BorqsPlusTransitActivity.this,
                            R.string.text_app_download_fetch_error, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_APP_DOWNLOAD_ERROR:
                    Toast.makeText(
                            BorqsPlusTransitActivity.this,
                            AppDownloadManager.getDownloadErrorMsg(BorqsPlusTransitActivity.this,
                                    e.getErrorCode()), Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    }
}
