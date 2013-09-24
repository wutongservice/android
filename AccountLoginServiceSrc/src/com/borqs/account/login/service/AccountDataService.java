package com.borqs.account.login.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.borqs.account.login.util.BLog;
import com.borqs.account.login.util.Configuration;

public class AccountDataService extends Service{

    private static String getUserData(Context context, String key) {
        String res = null;
        if (!TextUtils.isEmpty(key)) {
            AccountManager am = AccountManager.get(context);
            Account[] accounts = am.getAccountsByType(ConstData.BORQS_ACCOUNT_TYPE);
            if (accounts.length > 0) {
                try{
                    res = am.getUserData(accounts[0], key);
                }catch(Exception e){
                    BLog.d("ACD getuserdata error:" + key + ", " + e.getMessage());
                }
            }
        }        
        return res;
    }
    
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        BLog.d("acd on create");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        BLog.d("acd on bind:" + this.getPackageName() 
                + ", " + this.getApplicationInfo().className
                + ", " + this.getApplicationInfo().uid);
        return mBinder;
    }
    
    private final IAccountDataService.Stub mBinder = new IAccountDataService.Stub() {

        @Override
        public String getUserData(String key) throws RemoteException {
            BLog.d("acd get data:" + this.getCallingUid() +" , " + this.getCallingPid());
            BLog.d("acd get data2:" + getApplicationInfo().uid); 
            PackageManager pm = getPackageManager();
            BLog.d("test2 pm:" + pm.checkSignatures(getCallingUid(), getApplicationInfo().uid));
            BLog.d("test2 pm 2:" + pm.getClass().getPackage().getName());
            BLog.d("test2 pm 3:" + pm.getClass().getName() + ", " + pm.getClass().getCanonicalName());
            AccountManager am = AccountManager.get(AccountDataService.this);
            Account[] accounts = am.getAccountsByType(ConstData.BORQS_ACCOUNT_TYPE);
            if (accounts.length == 0) {
                return null;
            }
            return am.getUserData(accounts[0], key);
        }

        @Override
        public void setUserData(String key, String value)
                throws RemoteException {
            AccountManager am = AccountManager.get(AccountDataService.this);
            Account[] accounts = am.getAccountsByType(ConstData.BORQS_ACCOUNT_TYPE);
            if (accounts.length == 0) {
                return;
            }
            am.setUserData(accounts[0], key, value);            
        }

        @Override
        public String getVersion() throws RemoteException {
            return Configuration.getAccountServerVersion(AccountDataService.this);
        }
        
    };
}
