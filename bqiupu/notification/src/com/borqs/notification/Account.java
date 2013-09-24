package com.borqs.notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.packet.Presence;

//import com.borqs.account.login.util.AccountUtils;
import com.borqs.profile.AccountProfileInfo;

//import com.borqs.account.commons.AccountUtils;

import android.content.Context;
//import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;

public class Account extends Contacter {
    static String TAG = "AccountService";
    //Add this as default server
    static String DEFAULT_SERVER = "push.borqs.com";
    static int DEFAULT_PORT = 5222;
    private String mPassword;
    //Update this to support default server;
    private String mServerHost;
    //private String mServerHost = LinxClient.DEFAULT_SERVER;
    private int mServerPort = LinxClient.DEFAULT_PORT;
    private boolean mLogin;
    private boolean mRegistered;
    private INotificationListener mNotificationListener;
    
    //private NotificationService mServiceContext;

    LinxClient mXmppClient;

    public Account(String id, String pwd, String host) {
        //Contacter(id, name);
        super(id, "");
        if(id == null) {
            Log.d("----------", "null id", new Exception());
        }
        mPassword = pwd;
        mServerHost = host;

        //mServiceContext = (NotificationService)context;
        mXmppClient = new LinxClient(this);
        initAccountPhoneNumber();
        initAccountEmail();
    }

    public Account(String id, String pwd, String host, int port) {
        //Contacter(id, name);
        super(id, "");
        mPassword = pwd;
        mServerHost = host;
        mServerPort = port;

        mXmppClient = new LinxClient(this);
        initAccountPhoneNumber();
        initAccountEmail();
    }

    Map toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", mBorqsId);
        map.put("presence", new Integer(mPresence).toString());
        return map;
    }

    public void setAccountLoginListener(INotificationListener notificationListener) {
        mNotificationListener = notificationListener;
    }

    public void setLoginStatus(boolean value) {
        mLogin = value;
        if (mNotificationListener != null) {
            Log.i("queryUesr", "setLogin,change status is:" + value);
            try {
                mNotificationListener.onStatusChanged(value);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public boolean login() {
        // Register if needed.
        if(Constants.BORQS_ACCOUNT_ENABLED) {
            if(!mRegistered && !mXmppClient.register()) {
                return false;
            }
        }
        if (mXmppClient.login()) {
            setLoginStatus(true);
        } else {
            setLoginStatus(false);
        }
        if (Constants.LOGV_ENABLED) {
            Log.d(TAG, "login: " + mLogin);
        }
        return mLogin;
    }

    public void logout() {
        mXmppClient.logout();
        setLoginStatus(false);
        if (Constants.LOGV_ENABLED) {
            Log.i(TAG,"logout");
        }
    }

    public boolean isLogin() {
        return mXmppClient.isLogin();
    }

    // Do not user this method. We use accounts from other component.
    boolean register() {
        return mXmppClient.register();
    }

    public void sendMessage(int toAppId, String toUserName, String data) {
        mXmppClient.sendMessage(toAppId, toUserName, data, getPhoneNumber());
    }

    public void setRegistered(boolean b) {
        mRegistered = b;
        if(mRegistered) {
            saveRegistered(true);
        }

    }

    public boolean isRegistered() {
        return mRegistered;
    }

    //public Context getService() {
    //    return mServiceContext;
    //}

    public void getRoster() {
        
    }

    Presence queryUserPresence(String name) {
        if(!mLogin) {
        if (Constants.LOGV_ENABLED) {
            Log.i(TAG,"login failed");
        }
            return null;// not online
        }
        return mXmppClient.queryUserPresence(name);
    }

    public String getPhoneNumber() {
        Log.i(TAG,"mPhoneNumber is:"+mPhoneNumber);
        if (mPhoneNumber == null || mPhoneNumber.equals("")) {
            Log.i(TAG," mPhoneNumber is null");
            mPhoneNumber = initAccountPhoneNumber();
        }
        return mPhoneNumber;
    }

    public String getEmail() {
        if (mEmail == null) {
            mEmail = initAccountEmail();
        }
        return mEmail;
    }

    private String initAccountPhoneNumber() {
    	// This is deprecated, use com.borqs.profile.AccountProfileInfo instea
        //AccountUtils.Profile profile = AccountUtils.getProfile(NotificationService.getInstance().getBaseContext());
    	AccountProfileInfo profile = 
    			AccountProfileInfo.create(NotificationService.getInstance().getBaseContext());
    	List<Pair<String,Integer>> phones = profile.getPhones();
        StringBuilder from_phone= new StringBuilder();
        /*for(Pair<String, Integer> p : phones){
           if(from_phone.length()>0){
               from_phone.append(",");
           }
           from_phone.append(p.first);
        }*/
        Log.i(TAG,"phones is:"+phones);
        if(phones != null && phones.size() > 0 && phones.get(0) != null) {
            from_phone.append(phones.get(0).first);
            Log.d(TAG, "from_phone: " + from_phone);
            mPhoneNumber = from_phone.toString();
        } else {
            mPhoneNumber = "";
            Log.d(TAG, "failed to get phone # !");
        }
        return mPhoneNumber;
    }

    private String initAccountEmail() {
    	// This is deprecated, use com.borqs.profile.AccountProfileInfo instead
        //AccountUtils.Profile profile = AccountUtils.getProfile(NotificationService.getInstance().getBaseContext());
    	AccountProfileInfo profile = 
    			AccountProfileInfo.create(NotificationService.getInstance().getBaseContext());
    	List<Pair<String,Integer>> emails = profile.getEMails();
        StringBuilder from_email= new StringBuilder();
        /*for(Pair<String, Integer> p : phones){
           if(from_phone.length()>0){
               from_phone.append(",");
           }
           from_phone.append(p.first);
        }*/
        if(emails != null && emails.size() > 0 && emails.get(0) != null) {
            from_email.append(emails.get(0).first);
            Log.d(TAG, "from_phone: " + from_email);
            mEmail = from_email.toString();
        } else {
            mEmail = "";
            Log.d(TAG, "failed to get email # !");
        }
        return mEmail;
    }

    public boolean ifMyself(String id, int type) {
        if ((type == 0 && id.equals(mBorqsId))
                || (type == 1 && id.equals(mPhoneNumber))
                || (type == 2 && id.equals(mEmail))) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * Query user status by Borqs ID.
     * @param id May be borqs id/phone number/email.
     */
    UserInfo queryUser(String id, int type) {
        Log.d("queryUser", "name = " + id + "/ type=" + type);
        // No need to query over network if it's myself...
        if (ifMyself(id,type)) {
            if (Constants.LOGV_ENABLED) {
                Log.i("queryUser","query myself");
            }
            UserInfo userInfo = new UserInfo(mBorqsId);
            if (mLogin) {
                userInfo.mPresence = 1;
            } else {
                userInfo.mPresence = 0;
            }
            userInfo.mIpAddress = "127.0.0.1";
            return userInfo;
        }

        if (!mLogin) {
            if (Constants.LOGV_ENABLED) {
                Log.i("queryUser", "user is not online");
            }
            if (!loginAgain()) {
                return null;
            }
        } else {
            // Lookup in our local cache first, to avoid network load.
        }
        // For now we get user ID by phone#/email... over HTTP method.
        // TODO: Call AccountService when it's ready.
        if(type != 0) {
            id = Helper.getUserIdByName(id, type);
            Log.d("queryUser", "id = " + id);
        } 
        return mXmppClient.queryUser(id);
    }

    UserInfo queryUser(String name) {
        return queryUser(name, 0);
    }

    /*
     * Check network state and try to reconnect/login to server.
     * No need to login if network is not available.
     */
    private boolean loginAgain() {
        ConnectivityManager connectivity = (ConnectivityManager) NotificationService
                .getInstance().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }
        NetworkInfo info = connectivity.getActiveNetworkInfo();
        // TODO: Check for WiFi policy.
        if (info == null || !info.isConnected()) {
            Log.i(TAG, "network info is not null");
            return false;
        }
        return login();
    }

    public void startChatSession(String peerName) {
        
    }

    public void stopChatSession() {
        
    }

    public void sendNotification(String to, String msg) {
        mXmppClient.sendNotification(to, msg);
    }

    public String getServerHost() {
        return mServerHost;
    }

    public int getServerPort() {
        return mServerPort;
    }

    public String getPassword() {
        return mPassword;
    }
    /*
    // See if we have account already.
    public static boolean getDefaultAccount(Context context) {
        SharedPreferences p =
            PreferenceManager.getDefaultSharedPreferences(context);

        String name = p.getString(Constants.KEY_USER_NAME, null);
        String password = p.getString(Constants.KEY_PASSWORD, null);
        String server = p.getString(Constants.KEY_SERVER, null);
        
        if(name == null || password == null || server == null) {
            mNewAccount = true;
            // turn to login page
            if(false) {
                Intent intent = new Intent(this, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            name = getMSISDN(context);
            password = Constants.DEFAULT_PWD;
            server = Constants.DEFAULT_SERVER;
            if (Constants.LOGD_ENABLED) {
                Log.d(TAG, "Create user " + name + " and use server " + server);
            }
            mAccount = new Account(this, name, password, server);
            //mAccount.save(this);
            return true;
        } else {
            mAccount = new Account(this, name, password, server);
            return true;
        }
    }*/

    private static String getMSISDN(Context context) { // MSISDN
        TelephonyManager tm = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        String msisdn = tm.getLine1Number();
        if(msisdn != null) {
            if(msisdn.startsWith("+86")) {
                msisdn = msisdn.substring(3);
            }
        }
        return (msisdn == null) ? "" : msisdn;
    }

    public void save(Context context) {
        // Don't save if we're using Borqs ID.
        if(Constants.BORQS_ACCOUNT_ENABLED) {
            return;
        }

        SharedPreferences p =
            PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = p.edit(); 
        editor.putString(Constants.KEY_USER_NAME, getUserName());
        editor.putString(Constants.KEY_PASSWORD, mPassword);
        editor.putString(Constants.KEY_SERVER, mServerHost);
        editor.commit();
    }

    /*package*/static void saveRegistered(boolean b) {
        SharedPreferences p =
            PreferenceManager.getDefaultSharedPreferences(
                    NotificationService.getInstance());
        Editor editor = p.edit(); 
        editor.putBoolean(Constants.KEY_REGISTERED, true);
        editor.commit();
        if (Constants.LOGV_ENABLED) {
            Log.d(TAG, "setRegistered and saved.");
        }

    }
}
