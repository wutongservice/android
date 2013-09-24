package com.borqs.notification;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;


import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;

import com.borqs.account.login.service.AccountService;
import com.borqs.common.contact.ContactService;


public class NotificationService extends Service {

    static NotificationService mInstance;

    static String TAG = "NotificationService";
    private FileTransfers  filetransfers;

    private LinxSettings mLinxSettings;
    private INotificationListener mNotificationListener;

    int mBoundClients = 0;
    Account mAccount;
    boolean mNewAccount = false; // Means this account is read from device and never login.
    public Handler mHandler;
    public static int retryCount = 0;
    static boolean mIsUseGPRS = true;

    public static NotificationService getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        // TODO: Remove this for good performance.
        if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO) {
            android.os.StrictMode.setThreadPolicy(new android.os.StrictMode.ThreadPolicy.Builder()     
                .detectDiskReads()     
                .detectDiskWrites()     
                .permitNetwork()   // or .detectAll() for all detectable problems     
                .penaltyLog()     
                .build());     
            android.os.StrictMode.setVmPolicy(new android.os.StrictMode.VmPolicy.Builder()     
                .detectLeakedSqlLiteObjects()     
                //.detectLeakedClosableObjects()     
                .penaltyLog()     
                .penaltyDeath()     
                .build());
        }

        super.onCreate();

        // Must create LinxSettings in the first phase.
        mLinxSettings = LinxSettings.create(this);
        
        mInstance = this;
        mHandler = new MyHandler();
        if(mAccount == null) {
            boolean done = getDefaultAccount();
            if (Constants.LOGV_ENABLED) {
                Log.d(TAG, "getDefaultAccount: " + done);
            }
        }
        AccountManager.get(this).addOnAccountsUpdatedListener(
                mAccountListener, null, true);   
        
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (Constants.LOGV_ENABLED) {
            Log.d(TAG, "onStart");
        }

        mInstance = this;
        loginIfNeeded();
    }

    @Override
    public void onDestroy() {
        AccountManager.get(this).removeOnAccountsUpdatedListener(
                mAccountListener);
        super.onDestroy();
        if (Constants.LOGV_ENABLED) {
            Log.d(TAG, "onDestroy");
        }
        mInstance = null;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        if (Constants.LOGV_ENABLED) {
            Log.d(TAG, "onBind");
        }

        mInstance = this;
        loginIfNeeded();
        
        mBoundClients ++;
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int cmd = -1;       
        if (null != intent && null != intent.getExtras()) {
            cmd = intent.getExtras().getInt("service_command");
        }
        if (Constants.LOGD_ENABLED) {
            Log.d(TAG, "onStartCommand cmd = " + cmd);
        }

        switch (cmd) {
            case Constants.SERVICE_CMD_BOOTCOMPLETE:
                break;

            case Constants.SERVICE_CMD_WIFICONNECTED:
                if (Constants.LOGD_ENABLED) {
                    Log.d(TAG, "wifi connected");
                }
                if (inDebugMode() || isModelSupportLongConnection()) {
                    loginIfNeeded();
                }
                break;

            case Constants.SERVICE_CMD_WIFIDISCONNECTED:
                if (Constants.LOGD_ENABLED) {
                    Log.d(TAG, "wifi disconnected");
                }
                break;
            default:
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }
    
    boolean debug = true;

    public Handler getHandler() {
        return mHandler;
    }

    // See if we have account already.
    private boolean getDefaultAccount() {
        SharedPreferences p =
            PreferenceManager.getDefaultSharedPreferences(this);

        String nameOld = p.getString(Constants.KEY_USER_NAME, null);
        String serverOld = p.getString(Constants.KEY_SERVER, null);
        //directly to get from Constants.DEFAULT_SERVER
        String server = Constants.DEFAULT_SERVER;
        String name = getIdFromAccountService();
        String password = Constants.DEFAULT_PWD;
        if(nameOld == null || serverOld == null || (!serverOld.equals(server)) || (!nameOld.equals(name))) {
            mNewAccount = true;
            if(name == null || name.length() < 1) { // 1 is a magic number...should be 5 or...
                Log.w(TAG, "Invalid name: " + name);
                Account.saveRegistered(false);
                mAccount = null;
                return false;
            } else {
                // In case the user has owned a Borqs ID, we create account 
                // and register for her/him.
            }
            
            server = Helper.getHostName(this);
            if (Constants.LOGD_ENABLED) {
                Log.d(TAG, "Create user " + name + " and use server " + 
                        server + ", registered: " + false);
            }
            mAccount = new Account(name, password, server);
            mAccount.setRegistered(false);
            mAccount.save(this);
        } else {
            mAccount = new Account(name, password, server);
            mAccount.setRegistered(true);
        }
        mAccount.setAccountLoginListener(mNotificationListener);
        return true;
    }

    private String getIdFromAccountService() {
        //String id = AccountUtils.getAccountId(NotificationService.this);
        //String id = AccountUtils.getUserData(this,
        //        AccountUtils.BORQS_ACCOUNT_OPTIONS_KEY_UID);
	AccountService service = new AccountService(this);
        String id =  service.getUserId();
        if (Constants.LOGD_ENABLED) {
            Log.d(TAG, "getIdFromAccountService " + id);
        }
        return id;
    }

    // Use phone number as user ID.
    private String getIdFromDevice() {
        TelephonyManager tm = (TelephonyManager) getSystemService(
                Context.TELEPHONY_SERVICE);
        String msisdn = tm.getLine1Number();
        if(msisdn != null) {
            if(msisdn.startsWith("+86")) {
                msisdn = msisdn.substring(3);
            }
        }
        return (msisdn == null) ? "" : msisdn;
    }

    private boolean loginIfNeeded() {
        mInstance = this;
        
        if(mAccount == null) {
            Log.w(TAG, "Account is empty!");
            return false;
        }
        if(true) {
        boolean ret = mAccount.login();
        if(ret) {
            if(mNewAccount) {
                mAccount.save(this);
                mNewAccount = false;
            }
        }
        return ret;
        }
        // Android ICS does not allow network task running in main thread.
        else {
            /* TODO: Will remove.
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized(NotificationService.this) {
                boolean ret = mAccount.login();
                if(ret) {
                    if(mNewAccount) {
                        mAccount.save(NotificationService.this);
                        mNewAccount = false;
                    }
                }
                NotificationService.this.notify();
                }
            }
        }) {}.start();

        synchronized(NotificationService.this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return mAccount.isLogin();
        */
        }
        return false;
    }

    public int reTry() {
        retryCount += 1;
        if (retryCount == 6) {
            retryCount = 0;
            return 6;
        }
        Log.i("queryUser", "the " + retryCount + "time retry");
        ConnectivityManager connectivity = (ConnectivityManager) NotificationService
                .getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()){
                Log.i("queryuser", "network info is not null");
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    Log.i("queryUser", "network is connected ,retry 5 times");
                    int time = (int) Math.pow(2, retryCount);
                    Log.i("queryUser", "after " + time + "seconds to retry");
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(MSG_RELOGIN),
                            time * 1000);
                } else {
                    Log.i("queryUser", "network is not connected");
                }
            } else {
                Log.i("queryuser",
                        "network info is null or info.isConnected() is false");
            }
        } else {
            Log.i("queryUser", "ConnectivityManager is null");
        }
        return retryCount;
}

    public boolean onTryPoke() {
        Log.i("queryUser", "onTryPoke");
        if (mAccount != null) {
            if (mAccount.isLogin()) {
                Log.i("queryuser", "user have login");
                return true;
            } else {
                Log.i("queryuser", "user not login need to retry");
                int count = reTry();
                if (count == 6) {
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            Log.i("queryuser", "mAccount is null");
            return false;
        }
    }

    public boolean getLoginStatus() {
		return mAccount.isLogin();
	}

    // Implementation of the AIDL interfaces.
    private final INotificationService.Stub mBinder = 
        new INotificationService.Stub() {
        
        public boolean Poke() throws RemoteException{
            Log.i("queryUser",".....onTryPoke");
            return onTryPoke();
        }
        public void registerNotificationListener(INotificationListener listener) 
                throws RemoteException{
            mNotificationListener = listener;
            if (mAccount != null) {
                mAccount.setAccountLoginListener(listener);
            } else {
                Log.w(TAG, "Account is null when registering a listener.");
            }
        }
        @Override
        public boolean login() throws RemoteException {
            //assert(mAccount != null);
            if(mAccount == null && !getDefaultAccount()) {
                Log.d(TAG, "login() Failed. No account.");
                return false;
            }
            return loginIfNeeded();
        }

        @Override
        public String getAccount() throws RemoteException {
            if (Constants.LOGD_ENABLED) {
                Log.d(TAG, "getAccount: " + mNewAccount + "/" + mAccount);
            }
            if(mNewAccount) {
                return null;
            }
            return mAccount == null ? null : mAccount.getUserName();
        }

        @Override
        public boolean isLogin() throws RemoteException {
            if(mAccount != null && mAccount.isLogin()) {
                return true;
            }
            if (Constants.LOGD_ENABLED) {
                Log.d(TAG, "User not login: " + mAccount);
            }
            return false;
        }

        @Override
        public Map queryUser(String name, int type) throws RemoteException {
            UserInfo user = mAccount.queryUser(name, type); 
            if(user != null) {
                return user.toMap();
            }
            Log.i("queryUser","user is null");
            return null;
        }

        @Override
        public boolean sendMessage(int toAppId, String toUserName, String data)
                throws RemoteException {
            if(mAccount != null) {
                mAccount.sendMessage(toAppId, toUserName, data);
                return true;
            }
            Log.w(TAG, "Account is empty!");
            return false;            
        }

        @Override
        public void sendFile(String path, final String toUserName, IFileTransferListener listener)
                throws RemoteException {
            // Run the task in a thread then return here right away.
            // TODO: add a listener on the status.

            final FileTransfers fileTransfers = new FileTransfers(listener);
            final File file = new File(path);
           
            new Runnable() {
                @Override
                public void run() {
                    // Catch and handle any exceptions in FileTransfers!
                    fileTransfers.sendFile(file, toUserName, mAccount);
                }}.run();
            return;
        }
    };

    public boolean sendNotification(Intent intent) {
        sendBroadcast(intent);
        return true;
    }

    /*package*/ void notifyFileReceived() {
        
    }

    final static private String[] models = {"TD920"};
    public boolean isModelSupportLongConnection() {
        // Check OPL first. If OPL is set to true, we return directly. 
        //boolean opl_config = SystemProperties.getBoolean("apps.linx.long_connection", false);
        boolean opl_config = true;
        if (opl_config) 
            return true;

        // Otherwise, we check whether this is in our long-connection model list. 
        /*
        String model = getCellphoneModel();
        for (String m : models) {
            if (m.equals(model)) {
                return true;
            }
        }*/
        return false;
    }

    public boolean inDebugMode() {
        //String path = getFilesDir().getPath() + "/debug";
        //File f = new File(path);
        //return f.exists() || SystemProperties.getBoolean("com.borqs.linx.debug", false);
        return true;
    }

    private OnAccountsUpdateListener mAccountListener = new OnAccountsUpdateListener(){
        public void onAccountsUpdated(android.accounts.Account[] accounts) { 

            //String id = AccountUtils.getAccountId(NotificationService.this);
            //String id = AccountUtils.getUserData(NotificationService.this, 
            //        AccountUtils.BORQS_ACCOUNT_OPTIONS_KEY_UID);
	    AccountService service = new AccountService(NotificationService.this);
            String id =  service.getUserId();
            
            if(mAccount == null) {
                if(id == null) {
                    return;
                }
                // New account
                if (Constants.LOGD_ENABLED) {
                    Log.d(TAG, "onAccountsUpdated (new account): " + id);
                }
                mAccount = new Account(id, Constants.DEFAULT_PWD, 
                        Helper.getHostName(NotificationService.this));
                if (Constants.LOGD_ENABLED) {
                    Log.d(TAG, "User registered:" + id);
                }
                // Do registration.
                if(!mAccount.register()) {
                    mAccount = null;
                    Log.w(TAG, "Failed to register new account: " + id);
                    return;
                }
                if(!mAccount.login()) {
                    Log.w(TAG, "Failed to login new account: " + id);
                    return;
                }
            } else {
                if(id == null) {
                    Log.i(TAG,"account is not null but id is null");
                    mAccount.logout();
                    // delete account
                    Account.saveRegistered(false);
                    if (Constants.LOGD_ENABLED) {
                        Log.d(TAG, "onAccountsUpdated (delete account): " + 
                                mAccount==null?"":mAccount.getUserName());
                    }
                    mAccount = null;
                    // TODO:
                }
            }
        }
    };

    static final int MSG_RELOGIN = 100;
    static final int MSG_DETECT_NETWORK = 101;

    private class MyHandler extends Handler {

        public void handleMessage(Message msg) {
            if (Constants.LOGD_ENABLED) {
                Log.i(TAG, "handleMessage: " + msg);
            }
            switch (msg.what) {
            case MSG_RELOGIN:
                if (NotificationService.getInstance() == null) {
                    Log.i("queryUser",
                            "NotificationService is null and start NotificationService");
                    break;
                }
                boolean loginSuccess = NotificationService.getInstance()
                        .loginIfNeeded();
                if (!loginSuccess) {
                    Log.i("queryUser", "retry fail,try again.....");
                    reTry();
                } else {
                    retryCount = 0;
                    Log.i("queryUser", "retry success");
                }
                break;
            case MSG_DETECT_NETWORK:
                this.removeMessages(MSG_DETECT_NETWORK);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        String baiduUrl = "http://www.baidu.com";
                        String apptest2Url = "http://42.121.15.199";
                        int baiduresponceCode;
                        int apptest2responseCode;
                        URL baiduurl = null;
                        URL apptest2url = null;
                        try {
                            baiduurl = new URL(baiduUrl);
                            HttpURLConnection baiducon = (HttpURLConnection) baiduurl
                                    .openConnection();
                            baiducon.setRequestMethod("HEAD");
                            // con.setDoOutput(true);
                            baiducon.setConnectTimeout(5000); // 毫秒
                            baiducon.setReadTimeout(5000);
                            baiducon.connect();
                            baiduresponceCode = baiducon.getResponseCode();
                            if (baiduresponceCode == java.net.HttpURLConnection.HTTP_OK) {
                                Log.i("queryuser", "ping baidu is OK!");
                            } else {
                                Log.i("queryuser", "ping baidu is fail!");
                            }
                            baiducon.disconnect();

                            apptest2url = new URL(apptest2Url);
                            HttpURLConnection apptest2con = (HttpURLConnection) baiduurl
                                    .openConnection();
                            apptest2con.setRequestMethod("HEAD");
                            // con.setDoOutput(true);
                            apptest2con.setConnectTimeout(20000); // 毫秒
                            apptest2con.setReadTimeout(20000);
                            apptest2con.connect();
                            apptest2responseCode = apptest2con
                                    .getResponseCode();
                            if (apptest2responseCode == java.net.HttpURLConnection.HTTP_OK) {
                                Log.i("queryuser", "ping apptest2 is OK!");
                            } else {
                                Log.i("queryuser", "ping apptest2 is fail!");
                            }
                            apptest2con.disconnect();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                }).start();
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }
}
