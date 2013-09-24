package com.borqs.account.service;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.borqs.account.commons.AccountServiceAdapter;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.cache.QiupuHelper;

import twitter4j.QiupuSimpleUser;

/**
 * Created by IntelliJ IDEA.
 * User: b608
 * Date: 11-10-17
 * Time: 下午5:56
 * To change this template use File | Settings | File Templates.
 */ 
public class AccountServiceUtils {
    private static String TAG = "AccountServiceUtils";

    /**
     *  It need to keep identical to that in AndroidManifest.xml
     */
    public final static String DEFAULT_LOGIN_ACTION = "com.borqs.qiupu.maintab";

	private static IBorqsAccountService    mBorqsAccountService;
	private static BorqsAccount msBorqsAccount;
    private static boolean mIsExternalAccountServiceInvalid;
    
    private static void reset() {
        mBorqsAccountService = null;
        msBorqsAccount = null;
        mIsExternalAccountServiceInvalid = false;
    }

    public static void onCreate() {
        reset();
    }

    public static void onServiceDisconnected() {
        reset();
    }

    public static void onServiceConnected(IBinder service) {
        mBorqsAccountService = IBorqsAccountService.Stub.asInterface(service);
        
        refreshBorqsAccount();
    }

    public static BorqsAccount getBorqsAccount() {
        if (null == mBorqsAccountService) {
            if(QiupuConfig.DBLOGD)Log.w(TAG, "getBorqsAccount, return null while account service is empty.");
            return null;
        } else {
            if (null == msBorqsAccount) {
                try {
                    msBorqsAccount = mBorqsAccountService.getAccount();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            return msBorqsAccount;
        }
    }

    public static void doubleCheckAccountExisting() {
        if (isAccountServiceReady()) {
            BorqsAccount account;
            try {
                account = mBorqsAccountService.getAccount();
                if (account == null) {
                    msBorqsAccount = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static long getBorqsAccountID(){
        BorqsAccount account = getBorqsAccount();
        return account != null ? account.uid : -1;
    }

    public static String getSessionID(){
        BorqsAccount account = getBorqsAccount();
        return account != null ? account.sessionid : "";
    }

    /**
     * First, set current saved account to null, then try to get it.
     * @return true if the got account is not null.
     */
	public static boolean refreshBorqsAccount()	{
        forceGetBorqsAccount();

        return null != msBorqsAccount;
	}

    public static boolean isExternalAccountServiceInvalid() {
        return mIsExternalAccountServiceInvalid;
    }

    public static void setExternalAccountServiceInvalid(boolean isInvalid) {
        mIsExternalAccountServiceInvalid = isInvalid;
    }

    // move logic from BasicActivity
    public synchronized  static void onLogin() {
        forceGetBorqsAccount();
    }

    public synchronized static void onLogout() {
        msBorqsAccount = null;
    }

    public static void borqsLogout(boolean isNeedStartLogin) {
        borqsLogout(isNeedStartLogin, DEFAULT_LOGIN_ACTION);
    }

    private static void borqsLogout(boolean isNeedStartLogin, String requestActivity) {
         try {
        	 if (null != mBorqsAccountService) {
        		 mBorqsAccountService.borqsLogout(isNeedStartLogin, requestActivity);
        	 }
         }  catch (RemoteException e) {
			e.printStackTrace();
		}
    }

    public static void clearAccount() {
        try {
            if (null != mBorqsAccountService) {
                mBorqsAccountService.clearAccount();
                msBorqsAccount = null;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void updateValue(String newValue) {
        try {
        	if (null != mBorqsAccountService) {
        		mBorqsAccountService.updateValue(newValue);
        	}
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void login(BorqsAccount ba) {
        try {
            if (null != mBorqsAccountService) {
                mBorqsAccountService.login(ba);
            }
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch(Exception ne)
        {
        	ne.printStackTrace();
        }
    }

    public static void logout(String requestActivity) {
        try {
        	if (null != mBorqsAccountService) {
        		mBorqsAccountService.logout(requestActivity);
        	}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }

    public static boolean isAccountServiceReady() {
        return null != mBorqsAccountService;
    }

    public static boolean isAccountReady() {
//        return null != getBorqsAccount();
        return !TextUtils.isEmpty(getSessionID());
    }

    public static boolean resetBorqsAccount(Context context) {
        BorqsAccount ba = null;
        try {
            if (AccountServiceAdapter.isAccountPreloaded(context)) {
                final String session = AccountServiceAdapter.getSessionId(context);
                if (!TextUtils.isEmpty(session)) {
                    ba = new BorqsAccount();
                    ba.sessionid = session;
                    ba.nickname = AccountServiceAdapter.getNickName(context);
                    ba.screenname = AccountServiceAdapter.getScreenName(context);
                    ba.uid = Long.parseLong(AccountServiceAdapter.getUserID(context));
                } else {
                    Log.d(TAG, "resetBorqsAccount, but there is no preload borqs account.");
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "resetBorqsAccount, exception while query session id: " + e.getMessage());
        }

        login(ba);
        refreshBorqsAccount();

        return true;
    }

    private static void forceGetBorqsAccount() {
        if (null == mBorqsAccountService) {
            if (QiupuConfig.DBLOGD) Log.w(TAG, "forceGetBorqsAccount, set null with empty account service.");
            msBorqsAccount = null;
        } else {
            try {
                msBorqsAccount = mBorqsAccountService.getAccount();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static QiupuSimpleUser touchMySimpleUserInfo() {
        BorqsAccount account = AccountServiceUtils.getBorqsAccount();
        if (null != account && account.uid > 0) {
            QiupuSimpleUser user = new QiupuSimpleUser();
            user.uid = account.uid;
            user.nick_name = account.nickname;
            user.name = account.username;
            if(null != QiupuHelper.getORM()){
                user.profile_image_url = QiupuHelper.getORM().getUserProfileImageUrl(account.uid);
            }
            return user;
        }
        return null;
    }
}
