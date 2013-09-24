package com.borqs.qiupu.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.borqs.qiupu.util.LocationUtils;
import twitter4j.ApkResponse;
import twitter4j.AsyncBorqsAccount;
import twitter4j.TwitterException;
import twitter4j.conf.ConfigurationContext;
import twitter4j.internal.http.HttpClientImpl;
import twitter4j.util.StringUtil;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.Application;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.borqs.account.commons.AccountServiceAdapter;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.BorqsAccount;
import com.borqs.account.service.LocationRequest;
import com.borqs.account.service.PeopleLookupHelper;
import com.borqs.account.service.db.BorqsAccountORM;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.information.InformationHttpPushReceiver;
import com.borqs.qiupu.AccountListener;
import com.borqs.qiupu.AccountServiceConnectListener;
import com.borqs.qiupu.AccountServiceConnectObserver;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.UserAccountObserver;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.util.MD5;
import com.borqs.qiupu.util.StatusNotification;

public class QiupuService extends Service implements AccountListener, AccountServiceConnectListener, LocationRequest.IFLocationListener{
	private static final String TAG="Qiupu.QiupuService";

    public interface BorqsAccountAppInterface {
        public boolean ensureBorqsAccountService();
        public boolean isAppScanDependent();
        public void bindBorqsAccountService();
    };

	private QiupuORM orm;
//	private BorqsAccount mAccount;
	private static QiupuService mService;
	private static ApkFileManager mApkFileManager;
	public static FriendsManager mFriendsManager;
	public static RequestsService mRequestsService;
    public static DownloadAdapterService ds;
    public static ShareSourceResultReceiver resultReceiver;
    
    public static String NetworkErrorString="";
	
//	private IBorqsAccountService mAccountService;
	
	/** intent for phone boot firstly*/
	public static final String INTENT_QP_PHONE_BOOT = "INTENT_QP_PHONE_BOOT";
	
	public static final String BUNDLE_QP_APK_UI_POSTION = "BUNDLE_QP_APK_UI_POSTION";
	
	/** intent&bundle for down load application */
	public static final String INTENT_QP_DOWNLOAD_APK = "INTENT_QP_DOWNLOAD_APK";
	public static final String BUNDLE_QP_DOWNLOAD_APK_PACKAGENAME = "BUNDLE_QP_DOWNLOAD_APK_PACKAGENAME";
	public static final String BUNDLE_QP_DOWNLOAD_APK_URL = "BUNDLE_QP_DOWNLOAD_APK_URL";
	public static final String BUNDLE_QP_DOWNLOAD_APK_FILESIZE = "BUNDLE_QP_DOWNLOAD_APK_FILESIZE";
	public static final String INTENT_QP_SERIALIZE_IM = "INTENT_QP_SERIALIZE_IM";
	public static final String INTENT_QP_DESERIALIZE_IM = "INTENT_QP_DESERIALIZE_IM";
	
	public static final String INTENT_QP_UPDATE_APK = "INTENT_QP_UPDATE_APK";
	
	/** intent&bundle for upload application */
	public static final String INTENT_QP_UPLOAD_APK = "INTENT_QP_UPLOAD_APK";
	public static final String BUNDLE_QP_UPLOAD_APK = "BUNDLE_QP_UPLOAD_APK";
	public static final String BUNDLE_QP_UPLOAD_APK_PACKAGENAME = "BUNDLE_QP_UPLOAD_APK_PACKAGENAME";

	public static final String INTENT_QP_BACKUP_APK_RECORD = "INTENT_QP_BACKUP_APK_RECORD";
	public static final String BUNDLE_QP_BACKUP_APK_RECORD_PACKAGENAME = "BUNDLE_QP_BACKUP_APK_RECORD_PACKAGENAME";
	
	/** intent for sync phone owner's own applications' information */
//	private static final String INTENT_QP_SYNC_USER_APK_INFORMATION = "INTENT_QP_SYNC_USER_APK_INFORMATION";
	
	/** intent for load owner's current installed applications */
	public static final String INTENT_INSTALL_APK = "INTENT_INSTALL_APK";
	
	protected static final String INTENT_QP_SYNC_FRIENDS_INFORMATION="INTENT_QP_SYNC_FRIENDS_INFORMATION";
    protected static final String INTENT_QP_SYNC_DIRECTORY_INFORMATION="INTENT_QP_SYNC_DIRECTORY_INFORMATION";
    protected static final String INTENT_QP_SYNC_EVENT_INFORMATION="INTENT_QP_SYNC_EVENT_INFORMATION";

	public static final String INTENT_QP_SYNC_REQUESTS_INFORMATION="INTENT_QP_SYNC_REQUESTS_INFORMATION";
	public static final String INTENT_QP_SYNC_FRIENDS_APK_INFO_FROM_SERVER="INTENT_QP_SYNC_FRIENDS_APK_INFO_FROM_SERVER";
	public static final String INTENT_QP_LOAD_FRIEND_APK_FROM_SERVER="INTENT_QP_LOAD_FRIEND_APK_FROM_SERVER";
	public static final String INTENT_QP_LOAD_FRNED_APK_FROM_DB="INTENT_QP_LOAD_FRNED_APK_FROM_DB";

	public static final String UID="UID";

    private static final String INTENT_KEY_IMMEDIATELY = "INTENT_KEY_IMMEDIATELY";
    private static final String INTENT_KEY_INCLUDING_CIRCLES = "INTENT_KEY_INCLUDING_CIRCLES";
    private static final String INTENT_KEY_ONLY_SYCN_CIRCLE = "INTENT_KEY_ONLY_SYCN_CIRCLE";

    protected static final String INTENT_KEY_CIRCLE_ID = "INTENT_KEY_CIRCLE_ID";
    protected static final String INTENT_KEY_SYNC_FLAGS = "INTENT_KEY_SYNC_FLAGS";

	public static final boolean TEST_LOOP = false;
	ImageBackService     imageBackService;
	
	Location             latestLation;
	
	public static PeopleLookupHelper mBookSync;

    private IMReceiver mIMReceiver;

	@Override 
	public void onCreate() {
		Log.d(TAG, "onCreate"+this);     
        super.onCreate();           
        
        mService = this;
        
        /*
        MessageManager.getInstance().initialize(getApplicationContext());
        MessageManager.getInstance().setSilentTime(getApplicationContext(), 23, 8);
        MessageManager.getInstance().setHeartbeatInterval(getApplicationContext(), 7);
        */
        
        
        orm = QiupuORM.getInstance(getApplicationContext());
        QiupuHelper.setORM(orm);
        
        //may not receiver, because the applicatiion bind return very fast, so need check the account
        AccountServiceConnectObserver.registerAccountServiceConnectListener(getClass().getName(), this);
        UserAccountObserver.registerAccountListener(getClass().getName(), this);

//        orm.resetDownloadingStatus();
//        orm.resetUploadingStatus();

//        orm.verifyProfileActionGateway();
        
        //try to get account
        tryGetAccount();
        
        //remove qiupu apk;
        QiupuORM.sWorker.post(new Runnable()
        {
        	public void run()
        	{
                QiupuHelper.removeQiupuApplication();
        	}
        });
        
        
        //remove the early status
        NotificationManager mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNM.cancel(StatusNotification.QIUPU_NITIFY_DOWNLOADING_ID);
      
        //start account service in background
        IntentUtil.shootBorqsAccountServiceStartIntent(getApplicationContext());
        
        if(isLogin()) {
        	ensureFriendManagerExist();
        	mFriendsManager.start(getBorqsAccount());
//        	mRequestsService.start(mAccount);
        }

        Log.d(TAG, "============= QiupuService onCreate() register receiver ===========");
        mIMReceiver = IMReceiver.createInstance(this);

        ensureApkManagerExist(this, orm);
        
        ds = new DownloadAdapterService();
        ds.start(this);
        ds.setApkFileManager(mApkFileManager);

        imageBackService = new ImageBackService(this, orm);

        AccountManager.get(this).addOnAccountsUpdatedListener(mAccountListener, null, true);
        
        formatUserAgent(this.getApplicationContext(), orm);
        
        httpDebugSetting = orm.isDebugMode();
        //TODo add location here??

		if(mRequestsService == null) {
			mRequestsService = new RequestsService(this);
			
			 if(isLogin()) {
			    mRequestsService.start(getBorqsAccount());
			 }
		}
		
        final BorqsAccount account = AccountServiceUtils.getBorqsAccount();
        final long uid = null == account ? -1 : account.uid;
        sendShareSourceBroadcast(this, uid);
    }

    public static void sendShareSourceBroadcast(Context context, long uid) {
        if (AccountServiceUtils.isAccountServiceReady()) {
            Intent intent = new Intent(BpcApiUtils.Action.SHARE_SOURCE);
            List<ResolveInfo> infoList = context.getPackageManager().queryBroadcastReceivers(intent, 0);
            if (infoList != null && infoList.size() > 0) {
                if (null == resultReceiver) {
                    resultReceiver = new ShareSourceResultReceiver(infoList);
                    IntentFilter filter = new IntentFilter(BpcApiUtils.Action.SHARE_SOURCE_RESULT);
                    context.getApplicationContext().registerReceiver(resultReceiver, filter);
                }

                intent.putExtra(BpcApiUtils.User.USER_ID, uid);

                context.sendBroadcast(intent);
            }
        }
    }
	
    public static void formatUserAgent(Context con, QiupuORM orm)
	{
		 PackageManager manager = con.getPackageManager();
         try {
            PackageInfo info = manager.getPackageInfo(con.getPackageName(), 0);
//            String versionName = info.versionName;
            String versionCode = con.getString(R.string.app_name) + " " + info.versionCode;
            String language = Locale.getDefault().getCountry();//TODO or getLanguage() to back 'zh'
     
            String deviceid = orm.getSettingValue("deviceid");
            if(deviceid == null || deviceid.length() == 0)
            {
            	deviceid = getDeviceID(con);
            	orm.addSetting("deviceid", deviceid);
            }

//            final String UA = String.format("os=android-%1$s-%2$s;client=%3$s;lang=%4$s;model=%5$s-%6$s;deviceid=%7$s",
//                    Build.VERSION.SDK, Build.CPU_ABI, versionCode, language, Build.BOARD, Build.BRAND, deviceid);

             final String UA = String.format("os=android-%1$s-%2$s;client=%3$s;lang=%4$s;model=%5$s-%6$s;deviceid=%7$s",
                     Build.VERSION.SDK, Build.CPU_ABI, versionCode, language, Build.MODEL, Build.BRAND, deviceid);
             Log.d(TAG, "formatUserAgent, phone UA Info : "+ UA);

            HttpClientImpl.setUserAgent(UA);
            NetworkErrorString = con.getString(R.string.network_exception);
            HttpClientImpl.setNetworkExceptionMessage(NetworkErrorString);
            
         } catch (PackageManager.NameNotFoundException e){	     
         }
	}
	
	public static String getDeviceID(Context con)
	{	
		String deviceid = "";
		WifiManager wm = (WifiManager ) con.getSystemService(Context.WIFI_SERVICE);
		try{
			WifiInfo info = wm.getConnectionInfo();
			deviceid = info.getMacAddress().replace(":", "");
			Log.d("DEVICE", "deviceid 1="+deviceid);
		}catch(Exception ne){
			ne.printStackTrace();
			Log.d("DEVICE", "deviceid 1 exception="+ne.getMessage());
		}
		
		//2. imei/imsi
		TelephonyManager tm = (TelephonyManager ) con.getSystemService(Context.TELEPHONY_SERVICE);
		if(deviceid == null || deviceid.length() == 0)
		{	
			String imei = tm.getDeviceId();
			String imsi = tm.getSubscriberId();
			
			deviceid = (imei==null?"":imei + imsi==null?"":imsi);
			Log.d("DEVICE", "deviceid 2="+deviceid);
		}
		 
		//3. phone number
		if(deviceid == null || deviceid.length() == 0)
		{
			deviceid = tm.getLine1Number();
			Log.d("DEVICE", "deviceid 3="+deviceid);
		} 
		
		if(deviceid == null)
		{
			deviceid = UUID.randomUUID().toString();
			Log.d("DEVICE", "deviceid 4="+deviceid);
		}
		
		Log.d("DEVICE", "deviceid="+deviceid);
		
		return MD5.toMd5(deviceid.getBytes());
	}
	
	public void onAccountServiceConnected()	{
		tryGetAccount();
		
		//after borqs account service is connected, ready to run the background service now
		onLogin();
	}
	
	private void tryGetAccount()
	{
//		IBorqsAccountService mAccountService = ((QiupuApplication)getApplication()).getBorqsAccountService();
//    	if(mAccountService != null) {
    		try {
				AccountServiceUtils.getBorqsAccount();
                mAccountId = AccountServiceAdapter.getUserID(getApplicationContext());
			} catch (java.lang.SecurityException se) {
                doUninstallAndDownloadNewAPK();
//            } catch(RemoteException e) {
//				e.printStackTrace();
			}			
//    	}
//    	else
//    	{
//    		Log.d(TAG, "null account Manager");
//    	}
	}

	public void onAccountServiceDisconnected() {
		Log.d(TAG, "onAccountServiceDisconnected");   
	}
	
	
	public static QiupuService getQiupuService() {	
		return mService;
	}

	@Override
    public void onDestroy() {   
		Log.d(TAG, "onDestroy");     
        super.onDestroy();
        
        try{
		    if (null != mApkFileManager) {
                mApkFileManager.destroy();
                mApkFileManager = null;
		    }

            if (null != resultReceiver) {
                getApplicationContext().unregisterReceiver(resultReceiver);
                resultReceiver = null;
            }

            if (null != mIMReceiver) {
                Log.d(TAG, "=========== QiupuService onDestroy() unregister receiver");
                IMReceiver.unregisterService(this);
                getApplicationContext().unregisterReceiver(mIMReceiver);
                mIMReceiver = null;
            }

		}catch(Exception ne){}
		
		UserAccountObserver.unregisterAccountListener(getClass().getName());
		
		ds.stop(this);

        AccountManager.get(this).removeOnAccountsUpdatedListener(mAccountListener);
        AccountServiceConnectObserver.unregisterAccountServiceConnectListener(getClass().getName());

        mFriendsManager.destroy();
        mFriendsManager = null;

        imageBackService.Stop();

        LocationUtils.deactivateLocationService(this);
        
        mService = null;
        mBookSync = null;

        android.os.Process.killProcess(android.os.Process.myPid());        
	}
	
	@Override
	public void onStart(Intent intent, int startId)	{
		Log.d(TAG, "onStart startId:"+startId+" intent:"+intent);     
		super.onStart(intent, startId);
		//need do this after logined
		ensureFriendManagerExist();
		
		if(isLogin()) {			
	    	mFriendsManager.start(getBorqsAccount());
	    	
	    	if(mBookSync == null) {
	    		BorqsAccountORM accountOrm = BorqsAccountORM.getInstance(this);
	    		AsyncBorqsAccount mAsyncBorqsAccount = new AsyncBorqsAccount(ConfigurationContext.getInstance(),null,null);
	    		mBookSync = PeopleLookupHelper.getInstance(this, accountOrm, mAsyncBorqsAccount);
	    	}

	    	mBookSync.tryToUploadALlContacts(this);
		}
		
		final String action = null == intent ? null : intent.getAction();
		if(null != action) {
			if(INTENT_QP_DOWNLOAD_APK.equals(action))
			{
				ApkResponse apk = (ApkResponse)intent.getSerializableExtra(QiupuMessage.BUNDLE_APKINFO);
				
				if(orm.isUsingDownloadService())
				{
				    ds.download(this, apk);					
				}
				else
				{
				    ensureApkManagerExist(this, orm).downloadApk(false,apk);
				}
			}
			else if(INTENT_QP_UPDATE_APK.equals(action)) 
			{
				ApkResponse apk = (ApkResponse)intent.getSerializableExtra(QiupuMessage.BUNDLE_APKINFO);
				if(orm.isUsingDownloadService())
				{
				    ds.download(this, apk);					
				}
				else
				{
				    ensureApkManagerExist(this, orm).downloadApk(true,apk);
				}
			}
			
			else if(INTENT_QP_UPLOAD_APK.equals(action))
			{
				ensureApkManagerExist(this, orm).uploadApk((ApkResponse)intent.getSerializableExtra(BUNDLE_QP_UPLOAD_APK));
			} 
			else if(INTENT_QP_BACKUP_APK_RECORD.equals(action))
			{
//				ensureApkManagerExist(this, orm).backupApkRecord((ApkResponse)intent.getSerializableExtra(BUNDLE_QP_UPLOAD_APK));
			}
			else if(INTENT_QP_SYNC_FRIENDS_INFORMATION.equals(action))
			{
				boolean immediately = intent.getBooleanExtra(INTENT_KEY_IMMEDIATELY, false);
                boolean includingCircles = intent.getBooleanExtra(INTENT_KEY_INCLUDING_CIRCLES, false);
                boolean isOnlySyncCircle = intent.getBooleanExtra(INTENT_KEY_ONLY_SYCN_CIRCLE, false);
				mFriendsManager.alarmFriendsComing(immediately, includingCircles, isOnlySyncCircle);
			}
            else if (INTENT_QP_SYNC_DIRECTORY_INFORMATION.equals(action)) {
                long circleId = intent.getLongExtra(INTENT_KEY_CIRCLE_ID, 0);
                boolean force = intent.getBooleanExtra(INTENT_KEY_SYNC_FLAGS, false);
                mFriendsManager.alarmCircleDirectoryComing(circleId, force);
            }
            else if (INTENT_QP_SYNC_EVENT_INFORMATION.equals(action)) {
                boolean force = intent.getBooleanExtra(INTENT_KEY_SYNC_FLAGS, false);
                mFriendsManager.alarmSyncEventComing(force);
            }
			else if(INTENT_QP_SYNC_REQUESTS_INFORMATION.equals(action))
			{
                mRequestsService.alarmRequetsComming(null, DataConnectionUtils.testValidConnection(getApplicationContext()), intent.getBooleanExtra("no_notification", false));                
			}
			else if(INTENT_INSTALL_APK.equals(action))
			{
				
			}
			else if(INTENT_QP_PHONE_BOOT.equals(action))
			{
				
			}				
			else if("ACTION_BORQS_LOGIN_SUCCESS".equals(action))
			{
				postLoginSuccess(intent);
			}
			
		}
	}

    private void postLoginSuccess(Intent intent) {
        BorqsAccount ba = (BorqsAccount)intent.getParcelableExtra("Account");
				Log.d(TAG, "successfully login="+ba);

				AccountServiceUtils.refreshBorqsAccount();
				UserAccountObserver.login();
    }

    private boolean isLogin() {
        try {
            BorqsAccount account = getBorqsAccount();
            if (account != null && !StringUtil.isEmpty(account.sessionid)) {
                return true;
            }
        } catch (java.lang.SecurityException se) {
            doUninstallAndDownloadNewAPK();
        }
        Log.d(TAG, "isLogin, account or session is empty.");

        return false;
    }
    
	public void onCancelLogin() {
		Log.d(TAG, "onCancelLogin");
		if(mFriendsManager != null) {
			
		}
		
		if(mApkFileManager != null) {
		
		}
	}

	public void onLogin() 
	{
		Log.d(TAG, "onLogin");
//		try {
//			IBorqsAccountService mAccountService = QiupuApplication.getBorqsAccountService();
//			if(mAccountService != null)
//			{
				BorqsAccount account = getBorqsAccount();
				if(account != null && com.borqs.qiupu.util.StringUtil.isValidString(account.sessionid)) {
					ensureFriendManagerExist();
                    ensureApkManagerExist(this, orm).onLogin(account);

					mFriendsManager.onLogin(account);
					mRequestsService.onLogin(account);
				}
//			}
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}

        final long uid = null == account ? -1 : account.uid;
        sendShareSourceBroadcast(this, uid);
	}

	public void onLogout() {
		Log.d(TAG, "onLogout");

		if(mFriendsManager != null)
		{
			mFriendsManager.onLogout();
		}
		
		if(mApkFileManager != null)
		{
			mApkFileManager.onLogout();
		}
		
		if(mRequestsService != null)
		{
			mRequestsService.onLogout();
		}

        // Clear cache directory
        Log.v(TAG, "onLogout, clear cache folder start time: "  + System.currentTimeMillis());
        QiupuHelper.ClearCache();
        Log.v(TAG, "onLogout, clear cache folder end time: " + System.currentTimeMillis());
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	public void filterInvalidException(TwitterException ne) {
	}

	public BorqsAccount getBorqsAccount() {
        BorqsAccount account = AccountServiceUtils.getBorqsAccount();
        if (null == account) {
            AccountServiceUtils.refreshBorqsAccount();
            account = AccountServiceUtils.getBorqsAccount();
        }

        return account;
	}

	private static ApkFileManager ensureApkManagerExist(QiupuService service, QiupuORM orm) {
		if(orm != null) {
			mApkFileManager = ApkFileManager.createInstance(service, orm);
        }

        return mApkFileManager;
	}

	private void ensureFriendManagerExist() {
		if(mFriendsManager == null && orm != null) {
			mFriendsManager = new FriendsManager(this, orm);			
		}
		
	}

	//
	
	private boolean IHaveUsedBorqsAccount=false;
    private String mAccountId;
    private OnAccountsUpdateListener mAccountListener = new OnAccountsUpdateListener(){
        private void performLogout() {
            AccountServiceUtils.borqsLogout(false);
            UserAccountObserver.logout();
            orm.logout();
            QiupuHelper.ClearCache();

            try {
                Intent service = new Intent(getApplicationContext(), QiupuService.class);
//            System.exit(0);
                stopService(service);
                android.os.Process.killProcess(android.os.Process.myPid());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

		public void onAccountsUpdated(Account[] accounts) {
            if (null == accounts) {
                Log.d(TAG, "onAccountsUpdated, invalid Account.");
                
                //should we think it as logout?
                //yes, we should
                
                if (false == AccountServiceAdapter.isAccountPreloaded(getApplicationContext())) {
                    Log.d(TAG, "onAccountsUpdated, No Borqs Account service");
                    
                    //
                    //why become no borqs account, 
                    //that is because, user remove it
                    //
                    if(IHaveUsedBorqsAccount == true)
                    {
                        performLogout();
                    }
                }            
                
                return;
            }

            if (false == AccountServiceAdapter.isAccountPreloaded(getApplicationContext())) {
                Log.d(TAG, "onAccountsUpdated, No Borqs Account service");
                
                //
                //why become no borqs account, 
                //that is because, user remove it
                //
                if(IHaveUsedBorqsAccount == true)
                {
                    performLogout();
                }
                
                return;
            }            

            if (AccountServiceUtils.isExternalAccountServiceInvalid()) {
                Log.d(TAG, "onAccountsUpdated, Invalid Borqs Account service.");
                return;
            }

            boolean noBorqsAccount = true;
            for (int i = 0; i < accounts.length; ++i) {
                if (AccountServiceAdapter.BORQS_ACCOUNT_TYPE.equals(accounts[i].type)) {
                    noBorqsAccount = false;
                    break;
                }
            }

            //user removed the borqs account
            if (noBorqsAccount && IHaveUsedBorqsAccount == true) {
                Log.d(TAG, "onAccountsUpdated, skip without Borqs Account.");
                
                //TODO
                //this means user deleted the borqs account from system.
                //so do logout?
                performLogout();
                return;
            }

//            QiupuApplication app = (QiupuApplication)getApplication();
            Application app = getApplication();
            if (!QiupuService.verifyAccountLogin(app)) {
                Log.d(TAG, "onAccountsUpdated, skip if account service is not ready.");
                return;
            }

            try {
            	String oldAccount = mAccountId;
                mAccountId = AccountServiceAdapter.getUserID(getApplicationContext());

                Log.d(TAG, "onAccountsUpdated=" + mAccountId);
                if (mAccountId != null && oldAccount == null) {
                	
                	IHaveUsedBorqsAccount = true;

                    AccountServiceUtils.resetBorqsAccount(getApplicationContext());
                    UserAccountObserver.login();
                } else if (mAccountId == null && oldAccount != null) {
                    Log.d(TAG, "remove unhandled notification ");
                    NotificationManager mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNM.cancel(InformationHttpPushReceiver.HTTPPUSH);
                    performLogout();
                }
            } catch (java.lang.SecurityException se) {
                doUninstallAndDownloadNewAPK();
            }
		}
	};

    private void doUninstallAndDownloadNewAPK() {
//        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
//        dlgBuilder
//                .setIcon(R.drawable.dialog_question)
//                .setTitle(R.string.title_request_account_login)
//                .setMessage(R.string.dlg_msg_request_account_login)
//                .setPositiveButton(R.string.label_ok,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog,
//                                                int whichButton) {
//                                if (isAccountServiceActive()) {
//                                    checkLogin();
//                                } else {
//                                    AccountServiceConnectObserver.registerAccountServiceConnectListener(getClass().getName(),
//                                            MainActivity.this);
//                                    UserAccountObserver.registerAccountListener(getClass().getName(),
//                                            MainActivity.this);
//                                    mApp.bindBorqsAccountService();
//                                }
//                            }
//                        })
//                .setNegativeButton(R.string.label_cancel,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog,
//                                                int whichButton) {
//                            }
//                        });
//        dlgBuilder.create().show();
        if (AccountServiceAdapter.isAccountPreloaded(getApplicationContext())) {
            AccountServiceUtils.setExternalAccountServiceInvalid(true);
//            AccountServiceAdapter.removeAccount(getApplicationContext());
        }
        Toast.makeText(this, "The signature of current version is conflict with some account service. suggest to uninstall it and download new version from server", 1000).show();
    }

	public void updateLocation(Location loc) {
		latestLation = loc;
		
		String locString = String.format("longitude=%1$s;latitude=%2$s;altitude=%3$s;speed=%4$s;time=%5$s", 
				latestLation.getLongitude(),
				latestLation.getLatitude(),
				latestLation.getAltitude(),
				latestLation.getSpeed(),
				latestLation.getTime());
		
		HttpClientImpl.setLocation(locString);
	}

	static boolean httpDebugSetting = false; 
	public static boolean getHttpDebugSetting() {
		
		return httpDebugSetting;
	}

    public static boolean verifyAccountLogin(Application app) {
        if (null == app || app instanceof BorqsAccountAppInterface) {
            BorqsAccountAppInterface baaInterface = (BorqsAccountAppInterface) app;
            if (!baaInterface.ensureBorqsAccountService()) {
                return false;
            }
        }
        return true;
    }

    public static void bindBorqsAccountService(Application app) {
        if (null == app || app instanceof BorqsAccountAppInterface) {
            BorqsAccountAppInterface baaInterface = (BorqsAccountAppInterface) app;
            baaInterface.bindBorqsAccountService();
        }
    }

    /**
     * Synchronize my concerning people info from server, including my circles. It was
     * invoked immediately regardless including circles sync or not (INTENT_KEY_IMMEDIATELY is true)
     * @param context
     * @param includingCircles, true if the syncing session should include circles.
     */
    public static void loadUsersFromServer(Context context, boolean includingCircles) {
        loadUsersFromServer(context, includingCircles, false);
    }
    
    public static void loadUsersFromServer(Context context, boolean includingCircles, boolean isOnlySyncCircle) {
        Intent intent = new Intent(context, QiupuService.class);
        intent.setAction(QiupuService.INTENT_QP_SYNC_FRIENDS_INFORMATION);
        intent.putExtra(INTENT_KEY_IMMEDIATELY, true);
        intent.putExtra(INTENT_KEY_INCLUDING_CIRCLES, includingCircles);
        intent.putExtra(INTENT_KEY_ONLY_SYCN_CIRCLE, isOnlySyncCircle);
        context.startService(intent);
    }

    public static void loadCircleDirectoryFromServer(Context context, long circleId) {
        loadCircleDirectoryFromServer(context, circleId, false);
    }
    
    public static void loadCircleDirectoryFromServer(Context context, long circleId, boolean force) {
        Intent intent = new Intent(context, QiupuService.class);
        intent.setAction(QiupuService.INTENT_QP_SYNC_DIRECTORY_INFORMATION);
        intent.putExtra(INTENT_KEY_CIRCLE_ID, circleId);
        intent.putExtra(INTENT_KEY_SYNC_FLAGS, force);
        context.startService(intent);
    }
    
    public static void loadEventFromServer(Context context) {
        Intent intent = new Intent(context, QiupuService.class);
        intent.setAction(QiupuService.INTENT_QP_SYNC_EVENT_INFORMATION);
        context.startService(intent);
    }

    @Override
    public void onGLocationFailed() {
    }

}
