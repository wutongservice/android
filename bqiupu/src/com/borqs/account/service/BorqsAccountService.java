package com.borqs.account.service;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import com.borqs.account.commons.AccountServiceAdapter;
import com.borqs.account.login.service.AccountService;
import com.borqs.account.service.db.BorqsAccountORM;
import com.borqs.account.service.ui.LoginActivity;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.UserAccountObserver;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.util.StringUtil;
import twitter4j.AsyncBorqsAccount;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.conf.ConfigurationContext;
import twitter4j.internal.http.HttpClientImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BorqsAccountService extends Service implements com.borqs.account.service.AccountListener {
	public final static boolean LOGD = true;
	private final static String TAG = "BorqsAccountService";
	
	private BorqsAccountORM orm; 
	private BorqsAccount mAccount;
	private AsyncBorqsAccount mAsyncBorqsAccount;
	private AddressBookSync   mAddressbookSync;
	
	public static boolean TEST_LOOP = false;
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");     
		super.onCreate();
		
		orm = BorqsAccountORM.getInstance(getApplicationContext());
		mAsyncBorqsAccount = new AsyncBorqsAccount(ConfigurationContext.getInstance(),null,null);
		mAddressbookSync = AddressBookSync.getInstance(this,orm, mAsyncBorqsAccount);
		mAddressbookSync.start();
		
		formatUserAgent(getApplicationContext());
		
		AccountObserver.registerAccountListener(getClass().getName(), this);
	}
	
	 public static void formatUserAgent(Context con)
		{
			 PackageManager manager = con.getPackageManager();
	         try {
	            PackageInfo info = manager.getPackageInfo(con.getPackageName(), 0);
//	            String versionName = info.versionName;
	            String versionCode = con.getString(R.string.app_name) + " " + info.versionCode;
	            String language = Locale.getDefault().getCountry();//TODO or getLanguage() to back 'zh'
	     
	            String deviceid = QiupuORM.getSettingValue(con, "deviceid");
	            if(deviceid == null || deviceid.length() == 0)
	            {
	            	deviceid = QiupuService.getDeviceID(con);	            	
	            }
	            
	            Log.d("BorqsAccountService.UserAgent", "phone UA Info : "+ Build.BOARD + Build.BRAND + " app " + versionCode +  "/" + Build.VERSION.SDK +"/"+Build.CPU_ABI +"/"+language);
	            String UA = String.format("os=android-%1$s-%2$s;client=%3$s;lang=%4$s;model=%5$s;deviceid=%6$s", Build.VERSION.SDK, Build.CPU_ABI, versionCode, language, Build.BOARD + "-" + Build.BRAND, deviceid);
	            
	            HttpClientImpl.setUserAgent(UA);
	         } catch (PackageManager.NameNotFoundException e){	     
	         }
		}
	 
	@Override
	public void onStart(Intent intent, int startId)	{
		Log.d(TAG, "onStart startId:"+startId+" intent:"+intent);
		super.onStart(intent, startId);

		if(intent != null && intent.getAction() != null) {
			final String action = intent.getAction();
			Log.d(TAG, "onStart startId:"+startId+" action:"+action);
	        if("QiupuService.INTENT_QP_SYNC_ADDRESSBOOK_INFORMATION".equals(action))
			{
	        	mAddressbookSync.alarmAddressBookComing();
			}
	        else if(IntentUtil.ACTION_COLLECT_PHONE_INFO.equals(action))
	        {
	        	mAddressbookSync.start();
	        }
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		if (LOGD)Log.d(TAG, "onBind intent:" + intent);
		return mBinder;
	}

	private IBorqsAccountService.Stub mBinder = new IBorqsAccountService.Stub() {
		public List<ContactSimpleInfo> getContactsSimepleInfos() throws RemoteException {
			Log.d(TAG, "------------ getContactsSimepleInfos -----------");
			return uploadContactSimpleInfos();
		}

		public BorqsAccount getAccount() throws RemoteException {
			return getBorqsAccount();
		}

        public void logout(String requestActivity) throws RemoteException {
			Log.d(TAG, "------------ logout -----------");
			
	 		if(mAccount == null) {
	 			mAccount = orm.getAccount();
	 		}
	 		
	 		AccountObserver.logout();
            orm.logout();

            tryServerLogout();
        }

		public void login(BorqsAccount loginAccount)
		{
			BorqsAccount preAccount = orm.getAccount();
			if(preAccount != null && loginAccount != null &&
                    !preAccount.isEmpty() && !loginAccount.isEmpty() &&
                    preAccount.sessionid.equals(loginAccount.sessionid))
			{
				Log.d(TAG, "I am already logined, no need do this");
				return ;
			}
			
            if (loginAccount == null) {
                Log.d(TAG, "ignore the empty account except clear the DB item");
                BorqsAccount emptyAccount = new BorqsAccount();
                orm.updateAccount(emptyAccount);
            } else {
                orm.updateAccount(loginAccount);
                AccountObserver.login();
            }
		}
		
		public void clearAccount()
		{
			orm.logout();
		}

		public void borqsLogout(boolean isNeedStartLogin, String requestActivity)
				throws RemoteException {
			if(isNeedStartLogin) {
				restartLogin(requestActivity);
			}

	 		if(mAccount == null) {
	 			mAccount = orm.getAccount();
	 		}
	 		
	 		orm.logout();
	 		
	 		AccountObserver.logout();
            QiupuHelper.ClearCache();

            tryServerLogout();
        }

		public void updateValue(String value) throws RemoteException {
			if(mAccount == null) {
	 			mAccount = orm.getAccount();
	 			mAccount.nickname = value;
	 			orm.updateAccount(mAccount);
	 		}
			else
			{
				mAccount.nickname = value;
	 			orm.updateAccount(mAccount);
			}
		}
	};
	
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		
		AccountObserver.unregisterAccountListener(getClass().getName());
		
		mAddressbookSync.stop();
	};

	public List<ContactSimpleInfo> uploadContactSimpleInfos() {
		if(LOGD)Log.d(TAG, "uploadContactSimpleInfos");
		List<ContactSimpleInfo> contacts = new ArrayList<ContactSimpleInfo>(); 
		
		final ContentResolver cr = getContentResolver();

		Cursor contactCursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		while (contactCursor.moveToNext()) {
			int contactId = contactCursor.getInt(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
			String name = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

			Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
									 ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
			
			while (phones.moveToNext()) {
				String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));// "data1"
				if(!StringUtil.isValidString(phone)) {
					continue;
				}
				
				ContactSimpleInfo cinfo = new ContactSimpleInfo();
				cinfo.display_name_primary = name;
				cinfo.phone_number = phone;
				cinfo.type = ContactSimpleInfo.CONTACT_INFO_TYPE_PHONE;
				contacts.add(cinfo);
			}
			phones.close();

			Cursor emails = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
									 ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null);
			
			Log.d(TAG, "name :"+name+" phones size:"+phones.getCount()+"  emails size:"+emails.getCount());
			while (emails.moveToNext()) {
				String email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));// "data1"
				if(!StringUtil.isValidString(email)) {
					continue;
				}
				
				ContactSimpleInfo cinfo = new ContactSimpleInfo();
				cinfo.display_name_primary = name;
				cinfo.email =  email;
				cinfo.type = ContactSimpleInfo.CONTACT_INFO_TYPE_EMAIL;
				contacts.add(cinfo);
			}
			emails.close();
		}
		
		contactCursor.close();
		
		if(AccountServiceConfig.LOGD) {
			for(int i=0; i<contacts.size(); i++) {
				ContactSimpleInfo cinfo = contacts.get(i);
				if(QiupuConfig.LowPerformance)
				Log.i(TAG, "------- uploadContactSimpleInfos cinfo:'" + cinfo+"'");
			}
		}
		
		return contacts;
	}

	protected BorqsAccount getBorqsAccount() {
//		if (LOGD)Log.d(TAG, "getBorqsAccount: " + orm.getAccount() +
//                ", flag = " + AccountServiceUtils.isExternalAccountServiceInvalid());
        final Context context =getApplicationContext();

        if (AccountServiceAdapter.isAccountPreloaded(context) &&
                !AccountServiceUtils.isExternalAccountServiceInvalid())
        {
            String userId = null;
            String sessionId = null;
            try {
                sessionId = AccountServiceAdapter.getSessionId(context);
                userId = AccountServiceAdapter.getUserID(context);
            } catch (Exception e) {
                Log.w(TAG, "getBorqsAccount, exception while query session id: " + e.getMessage());
            }

            if (TextUtils.isEmpty(sessionId) || TextUtils.isEmpty(userId)) {
                orm.logout(); // clear account db if sessionId is null
                if(QiupuConfig.DBLOGD)Log.i(TAG, "getBorqsAccount, empty session or id.");
            } else {
                BorqsAccount ba = orm.getAccount();
                if(QiupuConfig.DBLOGD)Log.v(TAG, "getBorqsAccount, from db:" + ba);
                if (ba == null) {
                    ba = new BorqsAccount();
                }

                try {
                    ba.sessionid = sessionId;
                    ba.uid = Long.parseLong(userId);
                    ba.nickname = AccountServiceAdapter.getNickName(context);
                    ba.screenname = AccountServiceAdapter.getScreenName(context);
                } catch (Exception e) {
                    Log.w(TAG, "getBorqsAccount, exception while query session id: " + e.getMessage());
                }
                if(QiupuConfig.DBLOGD)Log.v(TAG, "getBorqsAccount, query from utils:" + ba);
                orm.updateAccount(ba);

                return orm.getAccount();
            }
        } else {
            Log.d(TAG,"getBorqsAccount, without borqs account service.");
            return orm.getAccount();
        }
        
        return null;
		
	}

	public void onLogin() {
		Log.d(TAG, "onLogin");
		mAccount = orm.getAccount();		
	}

	public void onLogout() {
		Log.d(TAG, "onLogout");
	}

	public void onCancelLogin() {

    }

    private void restartLogin(String requestActivity) {
    	if(!AccountServiceAdapter.isAccountPreloaded(this))
		{
	        if (!TextUtils.isEmpty(requestActivity) && !QiupuConfig.FORCE_BORQS_ACCOUNT_SERVICE_USED) {
	            Intent intent = new Intent(BorqsAccountService.this, com.borqs.qiupu.ui.LoginActivity.class);
	            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
	            intent.putExtra(LoginActivity.BUNDLE_REQUEST_LOGIN_ACTIVITY, requestActivity);
	            startActivity(intent);
	        }
    	}
    }

    private void tryServerLogout() {
        if (DataConnectionUtils.testValidConnection(getApplicationContext())) {
            if (mAccount != null && StringUtil.isValidString(mAccount.sessionid)) {
                mAsyncBorqsAccount.logoutAccount(mAccount.sessionid, new TwitterAdapter() {
                    public void logoutAccount(boolean res) {
                    }

                    public void onException(TwitterException ex, TwitterMethod method) {
                        TwitterExceptionUtils.printException(TAG, "logout, fail with server exception:", ex);
                    }
                });
            }
        } else {
            Log.d(TAG, "tryServerLogout, ignore without valid connection.");
        }
    }

    public static void gotoLogin(Activity activity, AccountService.IOnAccountLogin callback) {
        Log.d(TAG, "gotoLogin activity:" + activity);
        if (!refetchBorqsAccount(activity.getApplicationContext(), activity, callback)) {
            if (AccountServiceAdapter.isAccountPreloaded(activity)) {
                AccountServiceAdapter.performLogin(activity,
                        DataConnectionUtils.getCurrentApiHost(activity), callback);
            } else if (!QiupuConfig.FORCE_BORQS_ACCOUNT_SERVICE_USED) {
                Intent intent = new Intent(activity, com.borqs.qiupu.ui.LoginActivity.class);
                activity.startActivityForResult(intent, RESULTCODE_LOGIN);
            } else {
                Log.w(TAG, "gotoLogin, fail to login without require service.");
            }
        }
    }

    public static boolean refetchBorqsAccount(Context context, Activity activity, AccountService.IOnAccountLogin callback) {
        boolean ret = false;
        if (AccountServiceAdapter.isAccountPreloaded(context)) {
            if (!AccountServiceUtils.isExternalAccountServiceInvalid()) {
                if (!AccountServiceAdapter.performLogin(activity,
                        DataConnectionUtils.getCurrentApiHost(context), callback)) {
                    AccountServiceUtils.resetBorqsAccount(context);
                    UserAccountObserver.login();
                }
                ret = true;
            }
        }

        Log.d(TAG, "refetchBorqsAccount, return " + ret);
        return ret;
    }

    public final static int RESULTCODE_LOGIN = 1001;
    private static final String APP_FEATURE_ID = "1";
    public static Bundle getAccountLoginBundle(Context context) {
        Bundle data = new Bundle();
        final String accountServerUrl = DataConnectionUtils.getCurrentApiHost(context);
        data.putString(AccountServiceAdapter.CONFIG_ACCOUNT_SERVER_HOST, accountServerUrl);
        return data;
    }
}



