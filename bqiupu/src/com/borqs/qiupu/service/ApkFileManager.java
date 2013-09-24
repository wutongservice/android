package com.borqs.qiupu.service;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.BorqsAccount;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.StatusNotification;
import com.borqs.qiupu.util.StringUtil;
import twitter4j.ApkBasicInfo.APKStatus;
import twitter4j.*;
import twitter4j.conf.ConfigurationContext;

import java.io.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ApkFileManager {
    private static final String TAG = "Qiupu.Alarm.ApkFileManager";
    private QiupuService mService;

    private QiupuORM orm;
    private boolean shutdown;
    private static Application mApp;
    private BorqsAccount mLastStartedAccount;
    private static ApkFileServiceHandler mHandler;
    private StatusNotification notify;

    private AsyncQiupu asyncQiupu;
    private AsyncQiupuUploadApk mAsyncQiupuUploadApk;
    private AsyncQiupuDownloadApk mAsyncQiupuDownloadApk;

    public static HashMap<String, ApkResponse> mUploadingApksMap = new HashMap<String, ApkResponse>();

    public static HashMap<String, ApkResponse> mDownloadingApksMap = new HashMap<String, ApkResponse>();
    public static ArrayList<ApkResponse> mDownloadingApksList = new ArrayList<ApkResponse>();

    private static final HashMap<String, WeakReference<ApkFileServiceListener>> listeners = new HashMap<String, WeakReference<ApkFileServiceListener>>();

    private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
    static {
        sWorkerThread.start();
    }
    public static final Handler sWorker = new Handler(sWorkerThread.getLooper());


    public ApkFileManager(QiupuService service, QiupuORM qiupuORM) {
        mService = service;
        mApp = service.getApplication();
        orm = qiupuORM;
        mHandler = new ApkFileServiceHandler();


        if (mAsyncQiupuUploadApk == null) {
            mAsyncQiupuUploadApk = new AsyncQiupuUploadApk(ConfigurationContext.getInstance(), null, null);
        }

        if (mAsyncQiupuDownloadApk == null) {
            mAsyncQiupuDownloadApk = new AsyncQiupuDownloadApk(ConfigurationContext.getInstance(), null, null);
        }

        if (asyncQiupu == null) {
            asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null, null);
        }

        notify = new StatusNotification(mService);
    }

    public void start(BorqsAccount account) {
        if (QiupuConfig.LOGD) Log.d(TAG, "start");
        if (mLastStartedAccount != account ||
                (null != mLastStartedAccount && !mLastStartedAccount.equals(account))) {
            mLastStartedAccount = account;
        } else {
            if (QiupuConfig.LOGD) Log.d(TAG, "start, ignore unique account.");
        }
    }

    public static HashMap<String, ApkResponse> getUploadingApksMap() {
        return mUploadingApksMap;
    }

    public static ArrayList<ApkResponse> getDownloadingApksList() {
        return mDownloadingApksList;
    }

    public static HashMap<String, ApkResponse> getDownloadingApksMap() {
        return mDownloadingApksMap;
    }

    public Message createMsg(int msgid, ApkResponse apkinfo)
    {
    	if(apkinfo != null)
    	{
    	  Message msg = mHandler.obtainMessage(msgid);
          msg.getData().putString(QiupuMessage.BUNDLE_APK_PACKAGENAME, apkinfo.packagename);
          msg.getData().putSerializable(QiupuMessage.BUNDLE_APKINFO, apkinfo);

          return msg;
    	}

    	return null;
    }

    private class ApkFileServiceHandler extends Handler {
        public ApkFileServiceHandler() {
            super();
            Log.d(TAG, "create ApkFileManagerHandler");
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case QiupuMessage.MESSAGE_DOWNLOAD_APK_LOADING:
                    updateActivityUI(QiupuMessage.MESSAGE_DOWNLOAD_APK_LOADING, msg);
                    break;
                case QiupuMessage.MESSAGE_DOWNLOAD_MADE_CONNECTION:
                	updateActivityUI(QiupuMessage.MESSAGE_DOWNLOAD_MADE_CONNECTION, msg);
                	break;
                case QiupuMessage.MESSAGE_DOWNLOAD_APK_OK: {
                    ApkResponse apkinfo = (ApkResponse) msg.getData().getSerializable(QiupuMessage.BUNDLE_APKINFO);
                    notify.cancel(apkinfo.packagename);
                    notify.notifyFinish(apkinfo.label, null, apkinfo.packagename, apkinfo);

                    updateActivityUI(QiupuMessage.MESSAGE_DOWNLOAD_APK_OK, msg);
                    break;
                }
                case QiupuMessage.MESSAGE_DOWNLOAD_APK_FAILED: {
                    ApkResponse apkinfo = (ApkResponse) msg.getData().getSerializable(QiupuMessage.BUNDLE_APKINFO);
                    notify.cancel(apkinfo.packagename);
                    if(apkinfo.iscancelApp)
                    {
                    	notify.canelNotify(apkinfo);
                    	Log.d(TAG, "user click cancel btn, no need to show fail notification!");
                    }
                    else
                    {
                    	notify.notifyFail(apkinfo.label, null, apkinfo.packagename, apkinfo);
                    }

                    updateActivityUI(QiupuMessage.MESSAGE_DOWNLOAD_APK_FAILED, msg);
                    break;
                }
                case QiupuMessage.MESSAGE_UPLOAD_APK_OK:
                    updateActivityUI(QiupuMessage.MESSAGE_UPLOAD_APK_OK, msg);
                    break;
                case QiupuMessage.MESSAGE_UPLOAD_APK_LOADING:
                    updateActivityUI(QiupuMessage.MESSAGE_UPLOAD_APK_LOADING, msg);
                    break;
                case QiupuMessage.MESSAGE_UPLOAD_APK_FAILED:
                    updateActivityUI(QiupuMessage.MESSAGE_UPLOAD_APK_FAILED, msg);
                    break;
                case QiupuMessage.MESSAGE_START_DOWNLOAD:
                	updateActivityUI(QiupuMessage.MESSAGE_START_DOWNLOAD, msg);
                	break;
                default:
            }
        }
    }

    public void onLogin(BorqsAccount account) {
        if (QiupuConfig.LOGD) Log.d(TAG, "onLogin");
        start(account);
    }

    public void onLogout() {
        Log.d(TAG, "logout");

        if (mLastStartedAccount != null) {
            mLastStartedAccount = null;
        }
    }

    public void onCancelLogin() {
        if (mLastStartedAccount != null) {
            mLastStartedAccount = null;
        }
    }

    public void destroy() {
        onLogout();
    }

    public void shutdown() {
        Log.d(TAG, "shutdown:" + shutdown);
        shutdown = true;
    }


    public void uploadApk(final ApkResponse apk) {
        if (apk == null) {
            Log.d(TAG, "uploadApk apk is null return.");
            return;
        }

        if (QiupuConfig.LOGD)
            Log.d(TAG, "uploadApk :" + apk);

        File file = new File(apk.intallpath);
        String iconPath = QiupuHelper.saveAPKBMP(mService, apk.packagename, file.getName());

        final String pkgName = apk.packagename;
        final String appName = apk.label;
        final String versionCode = String.valueOf(apk.versioncode);
        final String versionName = apk.versionname;
        final String newVersionHint = getNewVersionHint(mService, pkgName, apk.versioncode);
        mAsyncQiupuUploadApk.backupApk(pkgName, appName, versionCode, versionName,
                file, new File(iconPath), getSavedTicket(), newVersionHint, new TwitterAdapter() {
            public void backupApk(BackupResponse backupApk) {
                Log.d(TAG, "backupApk backupApk:" + backupApk);

                mUploadingApksMap.remove(apk.packagename);
                apk.apkurl = backupApk.url;
                apk.status = APKStatus.STATUS_SYNC_OK;
                apk.progress = 100;

                Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_UPLOAD_APK_OK);
                msg.getData().putString(QiupuMessage.BUNDLE_APK_PACKAGENAME, apk.packagename);
                msg.getData().putSerializable(QiupuMessage.BUNDLE_APKINFO, apk);
                msg.sendToTarget();
            }

            public void startProcess() {
                Log.d(TAG, "backupApk startProcess");
                apk.status = APKStatus.STATUS_UPLOADING;
            }

            public void updateProcess(long processedsize, long totalsize) {
                Log.d(TAG, "performBackup backupApk updateProcess:" + processedsize + "/" + totalsize);
                if (processedsize == totalsize) {
					apk.status = APKStatus.STATUS_SYNC_OK;
					apk.progress = 100;

					Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_UPLOAD_APK_OK);
					msg.getData().putString(QiupuMessage.BUNDLE_APK_PACKAGENAME, apk.packagename);
					msg.sendToTarget();

                } else {
                    apk.status = APKStatus.STATUS_UPLOADING;
                    apk.progress = (int) (100.f * processedsize / totalsize);

                    Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_UPLOAD_APK_LOADING);
                    msg.getData().putString(QiupuMessage.BUNDLE_APK_PACKAGENAME, apk.packagename);
                    msg.sendToTarget();
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                TwitterExceptionUtils.printException(TAG, "uploadApk, server exception:", ex, method);

                mUploadingApksMap.remove(apk.packagename);
                apk.status = APKStatus.STATUS_NEED_UPLOAD;

                Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_UPLOAD_APK_FAILED);
                msg.getData().putString(QiupuMessage.BUNDLE_APK_PACKAGENAME, apk.packagename);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.sendToTarget();
            }
        });
    }

    private String copyFile(String source) {

        File finalfile = new File(source);
        if (finalfile.exists()) {
            String dest = source.substring(0, source.lastIndexOf("."));
            try {
                new File(dest).delete();
            } catch (Exception ne) {
                ne.printStackTrace();
            }

            Log.d(TAG, "rename file fail=" + source);
            FileOutputStream fo = null;
            FileInputStream fi = null;
            try {
                fi = new FileInputStream(finalfile);
                fo = new FileOutputStream(new File(dest));

                byte[] buffer = new byte[8 * 1024];
                int len = 0;
                while ((len = fi.read(buffer)) > 0) {
                    fo.write(buffer, 0, len);
                }
                fo.flush();
                buffer = null;

                finalfile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fi != null) {
                    try {
                        fi.close();
                    } catch (Exception ne) {
                        ne.printStackTrace();
                    }
                }
                if (fo != null) {
                    try {
                        fo.close();
                    } catch (Exception ne) {
                        ne.printStackTrace();
                    }
                }
            }

            return dest;
        }
        return "";
    }

    public void downloadApk(final boolean isForUpdate, final ApkResponse inApkInfo) {

        Log.d(TAG, "doDownloadApk apkurl:" + inApkInfo.apkurl);
        if (!StringUtil.isValidString(inApkInfo.apkurl)) {

            inApkInfo.apkurl = QiupuHelper.getAPKURL(inApkInfo.packagename);
            Log.d(TAG, "doDownloadApk apkurl:" + inApkInfo.apkurl + " invalid download url, use search api");
        }

        if (QiupuConfig.LOGD) Log.d(TAG, "apkinfo packagename :" + inApkInfo.packagename + " " + inApkInfo.apk_server_id);

        ApkResponse temp = mDownloadingApksMap.get(inApkInfo.packagename);
        if (temp == null) {
            temp = inApkInfo;
            mDownloadingApksMap.put(inApkInfo.packagename, temp);
            mDownloadingApksList.add(temp);
        } else {
            if (!mDownloadingApksList.contains(temp)) {
                Log.w(TAG, "downloadApk, unexpected situation, data differ between downloading list and map:" + temp);
                mDownloadingApksList.add(temp);
            }

            if (temp.status == APKStatus.STATUS_DOWNLOADING ||
                    temp.status == APKStatus.STATUS_UPDATING) {
                Log.d(TAG, "doDownloadApk packagename:" + inApkInfo.packagename + " downloading/updating, return");
                return;
            } else if (temp.status == APKStatus.STATUS_NEED_DOWNLOAD) {
                temp.status = APKStatus.STATUS_DOWNLOADING;
            } else if (temp.status == APKStatus.STATUS_NEED_UPDATE) {
                temp.status = APKStatus.STATUS_UPDATING;
            }
        }

        final ApkResponse apkInfo = temp;
        apkInfo.iscancelApp = false;

        final String storePath = QiupuHelper.getDownloadPath();

        if (mAsyncQiupuDownloadApk == null) {
            mAsyncQiupuDownloadApk = new AsyncQiupuDownloadApk(ConfigurationContext.getInstance(), null, null);
        }

        final String apk_full_storepath = storePath + apkInfo.packagename + ".apk.dl";
        notify.notifyOnGoing(apkInfo.label, null, apkInfo.packagename, apkInfo);

        //TODO  to update application item UI
        Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_START_DOWNLOAD);
        msg.getData().putString(QiupuMessage.BUNDLE_APK_PACKAGENAME, apkInfo.packagename);
        msg.sendToTarget();

        mAsyncQiupuDownloadApk.downloadFiles(apkInfo.packagename, apkInfo.apkurl, storePath, apkInfo.packagename + ".apk.dl", apkInfo.apksize, new TwitterAdapter() {
            public void downloadFiles(boolean result) {

                Log.d(TAG, "downloadFiles result:" + result + " apk_full_storepath:" + apk_full_storepath);
                if (result) {

                    final String downloadFilePath = copyFile(apk_full_storepath);

                    apkInfo.progress = 100;
                    if (isForUpdate) {
                        apkInfo.status = APKStatus.STATUS_NEED_UPDATE;
                    } else {
                        apkInfo.status = APKStatus.STATUS_NEED_DOWNLOAD;
                    }

                    mDownloadingApksMap.remove(apkInfo.packagename);
                    mDownloadingApksList.remove(apkInfo);

                    Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_DOWNLOAD_APK_OK);
                    msg.getData().putString(QiupuMessage.BUNDLE_APK_PACKAGENAME, apkInfo.packagename);
                    msg.getData().putBoolean(QiupuMessage.BUNDLE_DOWNLOAD_FOR_APK_UPDATE, isForUpdate);
                    msg.getData().putSerializable(QiupuMessage.BUNDLE_APKINFO, apkInfo);
                    msg.sendToTarget();

                    addApkDownloadStatistic(apkInfo);

                    if (orm.isEnableAutoIntallation()) {
                        installApk(mService, downloadFilePath, apkInfo.label, apkInfo.packagename);
                    }
                } else {
                    if (isForUpdate) {
                        apkInfo.status = APKStatus.STATUS_NEED_UPDATE;
                    } else {
                        apkInfo.status = APKStatus.STATUS_NEED_DOWNLOAD;
                    }

                    mDownloadingApksMap.remove(inApkInfo.packagename);
                    mDownloadingApksList.remove(apkInfo);

                    Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_DOWNLOAD_APK_FAILED);
                    msg.getData().putSerializable(QiupuMessage.BUNDLE_APKINFO, apkInfo);
                    msg.sendToTarget();
                }
            }

            public void beginDownload(String packagename, HttpURLConnection connection)
            {
            	synchronized(mDownloadingApksMap)
            	{
            		mDownloadingApksMap.get(packagename).connection = connection;
            	}

            	 Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_DOWNLOAD_MADE_CONNECTION);
                 msg.getData().putSerializable(QiupuMessage.BUNDLE_APKINFO, apkInfo);
                 msg.sendToTarget();
            }

            public void endDownload(String packagename)
            {
            	synchronized(mDownloadingApksMap)
            	{
            		mDownloadingApksMap.get(packagename).connection = null;
            	}
            }

            public void updateProcess(long processedsize, long filesize) {
                apkInfo.status = isForUpdate ? APKStatus.STATUS_UPDATING :
                        APKStatus.STATUS_DOWNLOADING;

                apkInfo.progress = processedsize == filesize ? 100 :
                        (int) (100.f * processedsize / filesize);

                Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_DOWNLOAD_APK_LOADING);
                msg.getData().putString(QiupuMessage.BUNDLE_APK_PACKAGENAME, apkInfo.packagename);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                TwitterExceptionUtils.printException(TAG, "downloadApk, server exception:", ex, method);

                mDownloadingApksMap.remove(inApkInfo.packagename);
                mDownloadingApksList.remove(apkInfo);

                if (isForUpdate)
                    apkInfo.status = APKStatus.STATUS_NEED_UPDATE;
                else
                    apkInfo.status = APKStatus.STATUS_NEED_DOWNLOAD;

                Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_DOWNLOAD_APK_FAILED);
                msg.getData().putString(QiupuMessage.BUNDLE_APK_PACKAGENAME, apkInfo.packagename);
                msg.getData().putSerializable(QiupuMessage.BUNDLE_APKINFO, apkInfo);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.sendToTarget();
            }
        });
    }

    //??
    //now we just care runtime, if next time reboot app, will show the install again
    //
    private HashMap<String, String> installedApp = new HashMap<String, String>();

    private static boolean isEmpty(String str)
    {
    	return str == null || str.length() == 0;
    }

    private ArrayList<ApkResponse> mAutoUploadAppList = new ArrayList<ApkResponse>();

    public void filterInvalidException(TwitterException ne) {
        Log.d(TAG, "filterInvalidException " + ne.getMessage());
    }

    public interface ApkFileServiceListener {
        public void updateUI(int msgcode, Message msg);
    }

    public static void registerApkFileServiceListener(String key, ApkFileServiceListener listener) {
        if (listeners.get(key) == null) {
            synchronized (listeners) {
                listeners.put(key, new WeakReference<ApkFileServiceListener>(listener));
            }
        }
    }

    public static void unregisterApkFileServiceListener(String key) {
        synchronized (listeners) {
            listeners.remove(key);
        }
    }

    public static void updateActivityUI(int msgcode, Message msg) {
    	if(msg == null)
    	{
    		Log.d(TAG, "updateActivityUI why i am null");
    		return;
    	}

//    	if(QiupuConfig.LowPerformance)
        Log.d(TAG, "updateActivityUI msgcode:" + msgcode + " listener count:" + listeners.size());
        synchronized (listeners) {
            Set<String> set = listeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                ApkFileServiceListener listener = listeners.get(key).get();
                if (listener != null) {
                    listener.updateUI(msgcode, msg);
                }
            }
        }
    }

    public static void saveApkIcon(ApkResponse apk) {
        if (apk == null || apk.icon == null) {
            return;
        }

        URL url;
        FileOutputStream fOut = null;
        boolean needRewite    = false;
        String filename       = null;
        try {
        	url      = new URL(apk.iconurl);
        	filename = QiupuHelper.getImageFilePath(url, true);
            if (QiupuConfig.DBLOGD) Log.d(TAG, "saveApkIcon filename:" + filename);
            File iconFile = new File(filename);
            if (iconFile.exists() && iconFile.canRead() && iconFile.length() > 0) {

                needRewite = (apk.status == APKStatus.STATUS_NEED_UPDATE ||
                        apk.status == APKStatus.STATUS_NEED_DOWNLOAD);

                //
                //have performance issue
                //can be decode to bitmap
                try{
	                Bitmap bmp = BitmapFactory.decodeFile(filename);
	                if(bmp == null)
	                {
	                	//not an bmp file
	                	needRewite = true;
	                }
	                bmp.recycle();
	                bmp = null;
                }catch(Exception ne)
                {
                	needRewite = true;
                }
            }
            else
            {
            	needRewite = true;
            }

            if(needRewite == true)
            {
            	try{
            	    iconFile.createNewFile();
                    fOut = new FileOutputStream(iconFile);
                    apk.icon.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
            	}catch(Exception ne){
            		Log.i(TAG, "saveApkIcon" + ne.getMessage());
            	}
            }
            else
            {
//                Log.i(TAG, "saveApkIcon, not overwrite file: " + filename);
            }
        }
        catch(Exception ne)
        {
        	Log.d(TAG, ne.getMessage());
        }
        finally {
            if (fOut != null) {
                try {
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static ArrayList<ApkResponse> loadSavedApks(Context context) {
        if (QiupuConfig.LOGD) Log.d(TAG, "loadSavedApks");
        ArrayList<ApkResponse> apks = new ArrayList<ApkResponse>();
        String apkPhonedownloadPath = null;
        String apkSdcardownloadPath = null;
        File phoneDir, sdcardDir;

        String esStatus = Environment.getExternalStorageState();
        if (esStatus.equals(Environment.MEDIA_MOUNTED)) {
            apkSdcardownloadPath = QiupuHelper.getDownloadPath();
        }

        apkPhonedownloadPath = QiupuConfig.APP_PHONE_PATH + QiupuConfig.APP_APK_PHONE_PATH;

        if (apkPhonedownloadPath != null) {
            phoneDir = new File(apkPhonedownloadPath);
            if (phoneDir.exists()) {
                File[] pFiles = phoneDir.listFiles();
                if (pFiles != null && pFiles.length > 0) {
                    for (int i = 0; i < pFiles.length; i++) {
                        parseDownloadedPackageArchive(context, apks, pFiles[i]);
                    }
                }
            }
        }

        if (apkSdcardownloadPath != null) {
            sdcardDir = new File(apkSdcardownloadPath);
            if (sdcardDir.exists()) {
                File[] sFiles = sdcardDir.listFiles();
                if (sFiles != null && sFiles.length > 0) {
                    for (int i = 0; i < sFiles.length; i++) {
                        parseDownloadedPackageArchive(context, apks, sFiles[i]);
                    }
                }
            }
        }

        return apks;
    }

    private static void parseDownloadedPackageArchive(Context context, ArrayList<ApkResponse> apks, File apkFile) {
        if(!apkFile.getName().endsWith(".dl")) {
            ApkResponse info = new ApkResponse();
            info.apksize = apkFile.length();
            info.status = APKStatus.STATUS_NEED_DOWNLOAD;
            String fname = apkFile.getName();
            info.packagename = fname.substring(0, fname.lastIndexOf("."));
            info.apkurl = QiupuHelper.getAPKURL(info.packagename);//apkFile.getAbsolutePath();
            info.downloadpath = apkFile.getAbsolutePath();

            if (parsePackageArchiveInfo(context, info)) {
                if (info.iconurl == null) {
                    info.iconurl = QiupuHelper.encodeLocalIconPath(info.packagename, info.versioncode);
                    saveApkIcon(info);
                }
            } else {
                Log.w(TAG, "parseDownloadedPackageArchive, fail  file: " + apkFile.toString());
                info.label = info.packagename;
                info.icon  = null;
                //apkFile.delete();
            }

            apks.add(info);
        }
    }

    private static boolean parsePackageArchiveInfo(Context context, ApkResponse apk) {
        if (apk == null || apk.downloadpath == null) {
            return false;
        }

        return parseUninstallAPKIcon(context, apk);
    }

    public static boolean isValidApk(Context context, String filepath)
    {
        String PATH_PackageParser = "android.content.pm.PackageParser";
        String PATH_AssetManager = "android.content.res.AssetManager";
        try {
            // apk包的文件路径
            // 这是一个Package 解释器, 是隐藏的
            // 构造函数的参数只有一个, apk文件的路径
            // PackageParser packageParser = new PackageParser(apkPath);
            Class pkgParserCls = Class.forName(PATH_PackageParser);
            Class[] typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
            Object[] valueArgs = new Object[1];
            valueArgs[0] = filepath;
            Object pkgParser = pkgParserCt.newInstance(valueArgs);
            Log.d("ANDROID_LAB", "pkgParser:" + pkgParser.toString());
            // 这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            // PackageParser.Package mPkgInfo = packageParser.parsePackage(new
            // File(apkPath), apkPath,
            // metrics, 0);
            typeArgs = new Class[4];
            typeArgs[0] = File.class;
            typeArgs[1] = String.class;
            typeArgs[2] = DisplayMetrics.class;
            typeArgs[3] = Integer.TYPE;
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
            valueArgs = new Object[4];
            valueArgs[0] = new File(filepath);
            valueArgs[1] = filepath;
            valueArgs[2] = metrics;
            valueArgs[3] = 0;
            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
            // 应用程序信息包, 这个公开的, 不过有些函数, 变量没公开
            // ApplicationInfo info = mPkgInfo.applicationInfo;
            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
            ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
            // uid 输出为"-1"，原因是未安装，系统未分配其Uid。
            // Resources pRes = getResources();
            // AssetManager assmgr = new AssetManager();
            // assmgr.addAssetPath(apkPath);
            // Resources res = new Resources(assmgr, pRes.getDisplayMetrics(),
            // pRes.getConfiguration());
            Class assetMagCls = Class.forName(PATH_AssetManager);
            Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
            Object assetMag = assetMagCt.newInstance((Object[]) null);
            typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath",
                    typeArgs);
            valueArgs = new Object[1];
            valueArgs[0] = filepath;
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
            Resources res = context.getResources();
            typeArgs = new Class[3];
            typeArgs[0] = assetMag.getClass();
            typeArgs[1] = res.getDisplayMetrics().getClass();
            typeArgs[2] = res.getConfiguration().getClass();

            Constructor resCt = Resources.class.getConstructor(typeArgs);
            valueArgs = new Object[3];
            valueArgs[0] = assetMag;
            valueArgs[1] = res.getDisplayMetrics();
            valueArgs[2] = res.getConfiguration();

            res = (Resources) resCt.newInstance(valueArgs);
            CharSequence label = null;
            if (info.labelRes != 0) {
                label = res.getText(info.labelRes);
            }

            final String lable = String.valueOf(label);
            Log.d(TAG, "lable="+lable);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean parseUninstallAPKIcon(Context context, ApkResponse apk) {
        String PATH_PackageParser = "android.content.pm.PackageParser";
        String PATH_AssetManager = "android.content.res.AssetManager";
        try {
            // apk包的文件路径
            // 这是一个Package 解释器, 是隐藏的
            // 构造函数的参数只有一个, apk文件的路径
            // PackageParser packageParser = new PackageParser(apkPath);
            Class pkgParserCls = Class.forName(PATH_PackageParser);
            Class[] typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
            Object[] valueArgs = new Object[1];
            valueArgs[0] = apk.downloadpath;
            Object pkgParser = pkgParserCt.newInstance(valueArgs);
            Log.d("ANDROID_LAB", "pkgParser:" + pkgParser.toString());
            // 这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            // PackageParser.Package mPkgInfo = packageParser.parsePackage(new
            // File(apkPath), apkPath,
            // metrics, 0);
            typeArgs = new Class[4];
            typeArgs[0] = File.class;
            typeArgs[1] = String.class;
            typeArgs[2] = DisplayMetrics.class;
            typeArgs[3] = Integer.TYPE;
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage",
                    typeArgs);
            valueArgs = new Object[4];
            valueArgs[0] = new File(apk.downloadpath);
            valueArgs[1] = apk.downloadpath;
            valueArgs[2] = metrics;
            valueArgs[3] = 0;
            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
            // 应用程序信息包, 这个公开的, 不过有些函数, 变量没公开
            // ApplicationInfo info = mPkgInfo.applicationInfo;
            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
            ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
            // uid 输出为"-1"，原因是未安装，系统未分配其Uid。
            // Resources pRes = getResources();
            // AssetManager assmgr = new AssetManager();
            // assmgr.addAssetPath(apkPath);
            // Resources res = new Resources(assmgr, pRes.getDisplayMetrics(),
            // pRes.getConfiguration());
            Class assetMagCls = Class.forName(PATH_AssetManager);
            Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
            Object assetMag = assetMagCt.newInstance((Object[]) null);
            typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath",
                    typeArgs);
            valueArgs = new Object[1];
            valueArgs[0] = apk.downloadpath;
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
            Resources res = context.getResources();
            typeArgs = new Class[3];
            typeArgs[0] = assetMag.getClass();
            typeArgs[1] = res.getDisplayMetrics().getClass();
            typeArgs[2] = res.getConfiguration().getClass();
            Constructor resCt = Resources.class.getConstructor(typeArgs);
            valueArgs = new Object[3];
            valueArgs[0] = assetMag;
            valueArgs[1] = res.getDisplayMetrics();
            valueArgs[2] = res.getConfiguration();

            res = (Resources) resCt.newInstance(valueArgs);
            CharSequence label = null;
            if (info.labelRes != 0) {
                label = res.getText(info.labelRes);
            }

            apk.label = String.valueOf(label);
            if (apk.label == null || apk.label.equals("null") || apk.label.equals("")) {
                apk.label = info.packageName.substring(info.packageName.lastIndexOf(".") + 1);
            }

            if (QiupuConfig.LOGD) Log.d(TAG, "parseUninstallAPKIcon pkg:"
                    + info.packageName + " uid="
                    + info.uid + "  apk.packageName:"
                    + apk.packagename
                    + " apk.label:" + apk.label);

            // if (label == null) {
            // label = (info.nonLocalizedLabel != null) ? info.nonLocalizedLabel
            // : info.packageName;
            // }
//         if(QiupuConfig.LOGD)Log.d(TAG, "showUninstallAPKIcon label:" + label);  
            // 这里就是读取一个apk程序的图标
            if (info.icon != 0) {
                Drawable icon = res.getDrawable(info.icon);
                apk.icon = ((BitmapDrawable) icon).getBitmap();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    static class PackageInstallObserver extends IPackageInstallObserver.Stub {
        public void packageInstalled(String packageName, int returnCode) {
            Log.d(TAG,"log install ="+packageName+"==returnCode="+returnCode);
            if(returnCode == 1){

            }
            else if(returnCode < 0)
            {
            	Log.d(TAG,"intall FAIL="+packageName );
            	if(returnCode == -104)
            	{
            		mHandler.post(new Runnable()
                	{
                		public void run()
                		{
                			Toast.makeText(mApp.getApplicationContext(), R.string.install_inconsistent_certificates, Toast.LENGTH_LONG).show();
                		}
                	});
            	}
            	else
            	{
            		mHandler.post(new Runnable()
                	{
                		public void run()
                		{
                			Toast.makeText(mApp.getApplicationContext(), "Install application fail.", Toast.LENGTH_LONG).show();
                		}
                	});
            	}
            }

            StatusNotification.notifyInstallingCancel(mApp.getApplicationContext(), packageName);
            //TODO Delete download apk file           
            //TODO if install successfully update DB record
            //TODO if install failed
        }
    }

    private static boolean isOMSPlatform(Context cox)
    {
    	return true;
    }

    public static boolean installApk(Context context, String filePath, String label, String packagename, boolean useNotifications) {
    	 File file = new File(filePath);
         if (file.exists()) {
        	 //boolean installed = true;
         	boolean installed = false;
         	if(false && isOMSPlatform(context) && !packagename.equalsIgnoreCase(context.getPackageName()))
         	{
         		try {
     				Method installPackage = null;
     				Method[] methods = PackageManager.class.getMethods();
     				for(Method method: methods)
     				{
     					if(method.getName().equals("installPackage"))
     					{
     						installPackage = method;
     						break;
     					}
     				}
     				if(installPackage != null)
     				{
     					PackageInstallObserver observer = new PackageInstallObserver();
     					PackageManager pm = context.getPackageManager();
 					    installPackage.invoke(pm, new Object[]{Uri.fromFile(new File(filePath)), observer, 2, "Qiupu"});

 					    if(useNotifications)
 					        StatusNotification.notifyInstalling(mApp.getApplicationContext(), label, packagename);
     				}
 				} catch (IllegalArgumentException e) {
 					// TODO Auto-generated catch block
 					installed = false;
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					installed = false;
 				} catch (InvocationTargetException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					installed = false;
             	} catch (SecurityException e) {
         			// TODO Auto-generated catch block
         			e.printStackTrace();
         			installed = false;
         		} catch(Exception e)
         		{
         			e.printStackTrace();
         			installed = false;
         		}

         	}
         	if(installed == false)
         	{
 	            Log.d(TAG, "install apk=" + filePath);
 	            Intent intent = new Intent(Intent.ACTION_VIEW);
 	            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
 	            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
 	            context.startActivity(intent);
         	}
             return true;
         } else {
             Log.d(TAG, "not exist=" + filePath);
             return false;
         }
    }
    public static boolean installApk(Context context, String filePath, String label, String packagename) {
    	return installApk(context, filePath, label, packagename, true);
    }

    public static void rmApkFile(ApkResponse apk, String packagename) {
        String apkpath = QiupuHelper.getDownloadSdcardPath(packagename);
        if (QiupuConfig.LOGD) Log.d(TAG, "remove apk file, packagename:" + packagename + " filepath:" + apkpath);
        File apkFile = new File(apkpath);
        if (apkFile.exists()) {
            apkFile.delete();

            if(apk != null)
            {
            	Message msg = new Message();
            	msg.getData().putString(QiupuMessage.BUNDLE_APK_PACKAGENAME, apk.packagename);
            	msg.getData().putSerializable(QiupuMessage.BUNDLE_APKINFO, apk);
            	updateActivityUI(QiupuMessage.MESSAGE_RM_APK_FILE, msg);
            }
        }
    }

    public static boolean existDownloadAPK(Context con, String pkgName) {
        String apkpath = QiupuHelper.getDownloadSdcardPath(pkgName);
        return new File(apkpath).exists() && DownloadAdapterService.isInDownloading(con,pkgName) == false;

    }

    private static boolean deleteExistingDownloadAPK(Context con, String pkgName) {
        if (DownloadAdapterService.isInDownloading(con,pkgName)) {
            return false;
        }

        final String path = QiupuHelper.getDownloadSdcardPath(pkgName);
        File apkFile = new File(path);

        if (apkFile.exists() && apkFile.canWrite()) {
            Runtime runtime = Runtime.getRuntime();
            final String deleteCmd = "rm -r " + path;
            try {
                runtime.exec(deleteCmd);
                return true;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return false;
    }

    /**
     * send to server for update apk install statistic
     *
     * @param apk
     */
    private void addApkInstallStatistic(final ApkResponse apk) {
//    	mAsyncQiupuDownloadApk.installIncrease(QiupuApplication.getSessionID(), String.valueOf(apk.packagename),apk.versioncode, true, new TwitterAdapter()
//    	{
//    		public void installIncrease(boolean installIncrease)
//    		{
//    			Log.d(TAG, "increase the install count");
//    		}
//    		public void onException(TwitterException ex,TwitterMethod method) 
//    		{
//    			Log.d(TAG, "Fail to increase apk count="+ex.getMessage());
//    			
//    			//UserApplicationsCache addstatisticCache = mApp.getAddStatisticCache();
//    			//addstatisticCache.cache(apk);
//    		}
//    	});
    }

    //TODO
    private void addstreamInstall(ApkResponse apk) {
        BorqsAccount account = mService.getBorqsAccount();
        final String nickname = null == account ? "" : account.nickname;
        String content = String.format(mService.getString(R.string.install_apk_to_post_stream), apk.label);

        asyncQiupu.postQiupuShare(getSavedTicket(), "", content, BpcApiUtils.APK_POST,
                apk.apk_server_id, apk.packagename, false, false, false, false,"", new TwitterAdapter() {
            public void postQiupuShare(Stream post) {
                Log.d(TAG, "finish addstreamInstall=" + post);
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                TwitterExceptionUtils.printException(TAG, "addstreamInstall, server exception:", ex, method);
            }
        });
    }

    /**
     * send to server for update apk download statistic
     *
     * @param apk
     */
    private void addApkDownloadStatistic(final ApkResponse apk) {
//    	mAsyncQiupuDownloadApk.downloadIncrease(QiupuApplication.getSessionID(), apk.apk_server_id, new TwitterAdapter()
//    	{
//    		public void downloadIncrease(boolean installIncrease)
//    		{
//    			Log.d(TAG, "increase the download count");
//    		}
//    		public void onException(TwitterException ex,TwitterMethod method) 
//    		{
//    			Log.d(TAG, "Fail to download apk count="+ex.getMessage());
//    		}
//    	});
    }

    public static boolean putDownloadApkMap(ApkResponse apk) {
        if (apk == null) {
            return false;
        }

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO) {
            return true;
        }

        ApkResponse tmpdownloadapk = mDownloadingApksMap.get(apk.packagename); //TODO: version code check

        if (tmpdownloadapk == null) {
            if (QiupuConfig.LOGD) Log.d(TAG, "put downloadApk in map!!!!" + apk.packagename);
            mDownloadingApksMap.put(apk.packagename, apk);
            mDownloadingApksList.add(apk);
            Log.d(TAG, "putUploadApkMap, uploading size,: " + mUploadingApksMap.size());
            return true;
        }
        return false;
    }

    public static boolean putUploadApkMap(ApkResponse apk) {
        if (apk == null) {
            return false;
        }
        ApkResponse tmpuploadapk = mUploadingApksMap.get(apk.packagename);
        if (tmpuploadapk == null) {
            if (QiupuConfig.LOGD) Log.d(TAG, "put UpdloadApk in map!!!!");
            mUploadingApksMap.put(apk.packagename, apk);
//            mUploadingApksList.add(apk);
            Log.d(TAG, "putUploadApkMap, uploading map size: " + mUploadingApksMap.size());
            return true;
        }
        return false;
    }

    private String getSavedTicket() {
        return AccountServiceUtils.getSessionID();
    }

    public static boolean shootDownloadAppService(final Context context, final String pkgName, final String pkgLabel) {
        boolean ret = false;
        if (existDownloadAPK(context, pkgName)) {
            installApk(context, QiupuHelper.getDownloadSdcardPath(pkgName), pkgLabel, pkgName);
        } else {
            ret = emitDownloadAppTask(context, pkgName, pkgLabel);
        }

        return ret;
    }

    public static boolean shootReplaceDownloadAppService(final Context context, final String pkgName, final String pkgLabel) {
        boolean ret = false;
        if (existDownloadAPK(context, pkgName)) {
            deleteExistingDownloadAPK(context, pkgName);
        }
        ret = emitDownloadAppTask(context, pkgName, pkgLabel);
        return ret;
    }

    private static boolean emitDownloadAppTask(final Context context, final String pkgName, final String pkgLabel) {
        boolean ret = false;

        if (DataConnectionUtils.testValidConnection(context)) {
            final PackageManager pm = context.getPackageManager();
            PackageInfo info;
            try {
                info = pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
                info = null;
            }

            if (null == info) {
                ApkResponse apkResponse = new ApkResponse();
                apkResponse.packagename = pkgName;
                apkResponse.label = pkgLabel;
                apkResponse.apkurl = QiupuHelper.getAPKURL(pkgName);

                performApkDownloadTask(context, apkResponse);

                ret = true;
            } else {
                final int versionCode = info.versionCode;
                final String versionName = info.versionName;
                AsyncQiupu asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null, null);
                ;
                asyncQiupu.getApkDetailInformation("", pkgName, false, new TwitterAdapter() {
                    public void getApkDetailInformation(ApkResponse apkResponse) {
                        if (QiupuConfig.LOGD) Log.d(TAG, "shootDownloadAppService, get apk response:" +
                                apkResponse + ", for :" + pkgName + ", version:" + versionCode);

                        if (null != apkResponse) {
                            if (apkResponse.latest_versioncode > versionCode) {
                                performApkDownloadTask(context, apkResponse);
                            } else {
                                Log.i(TAG, "shootDownloadAppService, the installed app:" + pkgLabel +
                                        " was already updated to (v" + versionCode + ")" + versionName);
                            }
                        }
                    }

                    public void onException(TwitterException ex, TwitterMethod method) {
                        Log.d(TAG, "shootDownloadAppService exception:" + ex.getMessage());
                    }
                });
            }
        } else {
            Log.d(TAG, "shootDownloadAppService, no network available.");
        }

        return ret;
    }

    private static void performApkDownloadTask(Context context, ApkResponse apkResponse) {
        boolean flag = putDownloadApkMap(apkResponse);
        if (flag) {
            Intent service = new Intent(context, QiupuService.class);
            service.setAction(QiupuService.INTENT_QP_DOWNLOAD_APK);
            Serializable serializable = apkResponse;
            service.putExtra(QiupuMessage.BUNDLE_APKINFO, serializable);
            context.startService(service);
        }
    }

    private static String getNewVersionHint(Context context, final String pkgName, final int versionCode) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo info = packageManager.getPackageInfo(context.getPackageName(), 0);
            if (null != info.packageName && info.packageName.equalsIgnoreCase(pkgName)) {
                if (versionCode == Integer.valueOf(info.versionCode)) {

                }
            }
        } catch (PackageManager.NameNotFoundException e) {
        }

        return "";
    }

    private static ApkFileManager mApkFileManager;
    static ApkFileManager createInstance(QiupuService service, QiupuORM orm) {
                if (null == mApkFileManager) {
                    mApkFileManager = new ApkFileManager(service, orm);
                }
                return mApkFileManager;
    }
}