package com.borqs.qiupu.ui.bpc;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import twitter4j.Circletemplate;
import twitter4j.PageInfo;
import twitter4j.UserCircle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.borqs.account.login.service.AccountService;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.BorqsAccountService;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.UserTask;
import com.borqs.qiupu.AccountServiceConnectListener;
import com.borqs.qiupu.AccountServiceConnectObserver;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.PhoneEmailColumns;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.page.CreateCircleMainActivity;
import com.borqs.qiupu.ui.page.CreateCircleMainForRegisterActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.wutong.HomePickerActivity;

public class MainSplashActivity extends BasicActivity.StatActivity implements AccountService.IOnAccountLogin,
        AccountServiceConnectListener,
        HomePickerActivity.PickerInterface {
	private static final String TAG = "QiuPu.MainSplashActivity";

    private static final long SPLASH_INTERVAL= 1500L; // 1.5 second

	private QiupuApplication mApp;
    private boolean fromHome;

    private PlaySplashTask mSplashTask;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate, " + this);
		super.onCreate(savedInstanceState);

        QiupuApplication.mTopOrganizationId = QiupuApplication.VIEW_MODE_PERSONAL;
        int versionCode = QiupuHelper.mVersionCode;
        if (versionCode <= 0) {
            try {
                versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        QiupuHelper.ensureAppIconPath(versionCode);

        fromHome = getIntent().getBooleanExtra("from_home", true);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.bpc_launch_ui);

        if (AccountServiceUtils.isAccountServiceReady()) {
            AccountServiceUtils.doubleCheckAccountExisting();
            onSplashTimeOut(2000);
        } else {
            mActivityCreateTime = System.currentTimeMillis();
            mApp = (QiupuApplication) getApplication();
            AccountServiceConnectObserver.registerAccountServiceConnectListener(getClass().getName(), this);
            (mSplashTask = new PlaySplashTask()).execute();
        }
    }

    private class PlaySplashTask extends UserTask<String, Void, Boolean> {
        public Boolean doInBackground(String... urls) {
            final boolean wasReady = wasEntryActivityReady();
            if (!wasReady) {
                Log.d(TAG, "PlaySplashTask, start to bind service.");
                mApp.bindBorqsAccountService();
            }

            return wasReady;
        }

        public void onPreExecute() {
            showProgressBtn(true);
        }

        public void onPostExecute(Boolean wasReady) {
            Log.d(TAG, "PlaySplashTask, onPostExecute");
            if (wasReady.booleanValue()) {
                onSplashPlayed();
            }
        }

        protected void onCancelled(Boolean wasReady) {
            Log.d(TAG, "PlaySplashTask, onCancelled");
        }
    }

    private synchronized void onSplashPlayed() {
        showProgressBtn(false);
        onSplashTimeOut();
    }

    // This will be trigger by two case:
    // 1. After splash time out
    // 2. Service was bind.
    private long mActivityCreateTime;
    private boolean isStartActivity = false;
    private synchronized void onSplashTimeOut() {
        Log.d(TAG, "onSplashTimeOut, entry activity and service complete.");
        
        if(isStartActivity) {
            return ;
        }
        isStartActivity = true;
        
        final long timeLeft = mActivityCreateTime + SPLASH_INTERVAL - System.currentTimeMillis();
        onSplashTimeOut(timeLeft);
    }

    private void onSplashTimeOut(long delay) {
        if (delay < 0) {
            delay = 0;
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                gotoFinalAccountVerify();
            }
        }, delay);
    }

    private int mTryCount = 0;
    private void gotoFinalAccountVerify() {
        if (AccountServiceUtils.isAccountReady() || mTryCount > 3) {
            if (mTryCount > 0) Log.w(TAG, "gotoFinalAccountVerify, so many fail to login." + mTryCount);
            startEntryActivity();
        } else {
            mTryCount++;
            BorqsAccountService.gotoLogin(this, this);
        }
    }

	private void startEntryActivity() {
        if (isFinishing()) {
            Log.d(TAG, "startEntryActivity, skip as we was order to exit.");
            return;
        }

        if (!gotoCustomizedHome()) {
        	if(isBorqsUser()) {
        		startMainCircle("", CircleUtils.BORQS_CIRCLE_ID);
        	}else {
        		UserCircle firstOrg = QiupuORM.getInstance(this).queryFirstTopCircle();
        		if(firstOrg != null) {
        			startMainCircle(firstOrg.name, firstOrg.circleid);
        		}else {
        			gotoCheckTopCircleTranActivity();
        		}
        	}
//            IntentUtil.startStreamListIntent(this, fromHome);
        }

        escapeFromActivity();
    }
	
	private void startMainCircle(String circleName, long circleId) {
		QiupuORM.addSetting(this, QiupuORM.HOME_ACTIVITY_ID, String.valueOf(circleId));
		UserCircle tmpcircle = new UserCircle();
		tmpcircle.circleid = circleId;
		tmpcircle.name = circleName;
		QiupuApplication.mTopOrganizationId = tmpcircle;
		IntentUtil.gotoOrganisationHome(this, circleName, circleId);
        IntentUtil.loadCircleDirectoryFromServer(this, circleId);
        finish();
	}
	
	private void gotoCheckTopCircleTranActivity() {
		Intent intent = new Intent(this, CheckTopCircleTranActivity.class);
		startActivity(intent);
	}
    
    private void escapeFromActivity() {
        if (null != mSplashTask) {
            mSplashTask.cancel(true);
        }

        final String key = getClass().getName();
        AccountServiceConnectObserver.unregisterAccountServiceConnectListener(key);

        finish();
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();

        escapeFromActivity();
	}

    private static class UserAccount
	{
		public String username;
		public String password;
	}
	
	private UserAccount checkDownloadAccount(String dir)
	{
		UserAccount ua = null;
//		String str = QiupuConfig.getSdcardPath(dir);
		String name = QiupuHelper.refreshFileList(QiupuConfig.SDCARD_DOWNLOAD_PATH);
		Log.d(TAG, "name :"+name);
		if(name !=null)
		{
			int firstPos = name.indexOf("beijj2012");
			if(firstPos > 0)
			{
				String apkname = name.substring(0, firstPos);
				Log.d(TAG, "apkname :"+apkname);						
				try {
					apkname = URLDecoder.decode(apkname, "utf-8");
					apkname = apkname.replaceAll("_40", "@");
					Log.d(TAG, "apkname :"+apkname);	
					String para[] = apkname.split("abz_seperate_998");
					if(para != null && para.length == 2)
					{
						ua = new UserAccount();
						ua.username = para[0];
						ua.password = para[1];
						Log.d(TAG, "username :"+ua.username + "pwd :"+ua.password);
					}						
				} catch (UnsupportedEncodingException e) {					
					e.printStackTrace();
				}
			}
		}
		
		return ua;
	}
	
	private boolean setBorqAccount()
	{
        return AccountServiceUtils.refreshBorqsAccount();
	}
//	private void checkLogin() {
//    	Log.d(TAG, "checkLogin");
//		try {
//			mBorqsAccountService = mApp.getBorqsAccountService();
//			BorqsAccount account = mBorqsAccountService.getAccount();
//			Log.d(TAG, "checkLogin account: "+account);
//			if(account == null || StringUtil.isEmpty(account.sessionid))
//			{
//				String username = "";
//				String pwd      = "";
//
//				UserAccount ua = checkDownloadAccount("/download");
//				if(ua == null)
//				{
//					ua = checkDownloadAccount("/UCDownloads");
//				}
//
//				if(ua != null)
//				{
//					username = ua.username;
//					pwd      = ua.password;
//				}
//
//				gotoLogin(username, pwd);
//
//			}else {
//				mApp.setBorqsAccount(account);
//				Intent qsintent = new Intent(this, QiupuService.class);
//				startService(qsintent);
//
//				Intent intent = new Intent(this,.class);
//		    	startActivity(intent);
//
//				this.finish();
//			}
//
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}
	
	public void onAccountServiceConnected() {
        Log.d(TAG, "onAccountServiceConnected, enter.");
		setBorqAccount();
        onSplashPlayed();
	}

	public void onAccountServiceDisconnected() {
		Toast.makeText(this, "connect failed", Toast.LENGTH_SHORT).show();
        escapeFromActivity();
		
	}

    private boolean wasEntryActivityReady() {
        boolean isReady = false;

        if (setBorqAccount()) {
            isReady = true;
        }

        if (AccountServiceUtils.isAccountServiceReady()) {
            isReady = true;
        }

        return isReady;
    }

    protected void showProgressBtn(boolean show) {
        if (mImportProgress != null && mImportProgress.isEnabled()) {
            mImportProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

//    public void run(AccountManagerFuture<Bundle> future) {
//        Log.d(TAG, "run, callback from AccountManagerCallback.");
//        if (future != null) {
//            Bundle result = null;
//            try {
//                if (future.isCancelled()) {
//                    finish();
//                } else if (future.isDone()) {
//                    postAccountLogin(false);
//                } else {
//                    result = future.getResult();
//                    if (result.getBoolean("from_register")) {
//                        postAccountLogin(true);
//                    }
//                }
//            } catch (OperationCanceledException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (AuthenticatorException e) {
//                e.printStackTrace();
//            }
//        }
//    }


    @Override
    public void onAccountLogin(boolean flag, AccountService.AccountSessionData accountSessionData) {
        Log.d(TAG, "onAccountLogin, flag:" + flag + ", data: " + accountSessionData);
        if (flag) {
        	AccountServiceUtils.resetBorqsAccount(getApplicationContext());
            postAccountLogin(accountSessionData.isNewRegister());
        } else {
            finish();
        }
    }

    private void postAccountLogin(boolean isRegister) {
        Log.d(TAG, "postAccountLogin, be back from login, isRegister = " + isRegister);
        if(isRegister) {
        	// if is new register, go to create first circle
        	Intent intent = new Intent(this, CreateCircleMainForRegisterActivity.class);
        	intent.putExtra(CreateCircleMainActivity.SUBTYPE, Circletemplate.SUBTYPE_TEMPLATE);
        	intent.putExtra(PageInfo.PAGE_ID, UserCircle.CREATE_CIRCLE_DEFAULT_PAGE_ID);
        	startActivity(intent);
        	finish();
        }else {
        	new LoadCirclesAndUsersTask().execute((Void[]) null);
//        IntentUtil.loadCircleAndUserFromServer(this);
//        showPanel();
        	onLoginWizardSkipped();
        }
    }

    private void showPanel() {
        if (mImportPanel == null) {
            mImportPanel = ((ViewStub) findViewById(R.id.stub_import)).inflate();
            mImportProgress = (ProgressBar) mImportPanel.findViewById(R.id.load_progress);

            final View cancelButton = mImportPanel.findViewById(R.id.button_cancel);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onLoginWizardSkipped();
                }
            });
        }
        showPanel(mImportPanel, true);
    }
    private void showPanel(View panel, boolean slideUp) {
        panel.startAnimation(AnimationUtils.loadAnimation(this,
                slideUp ? R.anim.slide_in : R.anim.slide_out_top));
        panel.setVisibility(View.VISIBLE);
    }

    private void hidePanel(View panel, boolean slideDown) {
        panel.startAnimation(AnimationUtils.loadAnimation(this,
                slideDown ? R.anim.slide_out : R.anim.slide_in_top));
        panel.setVisibility(View.GONE);
    }

    private void onLoginWizardSkipped() {
        gotoFinalAccountVerify();
    }
    private View mImportPanel;
    private ProgressBar mImportProgress;
    
    private class LoadCirclesAndUsersTask extends UserTask<Void, Void, Void> {
        @Override
        public Void doInBackground(Void... params) {
        	 IntentUtil.loadCircleAndUserFromServer(MainSplashActivity.this);
            return null;
        }

        @Override
        public void onPostExecute(Void param) {
        }
    }

    /**
     * gotoCustomizedHome, return false, if prefer to classic home that show friends' feed, or
     * jump to customized home that shows a formal circle as the top and return true.
     * todo: 1. alternative for detecting home empty home option and goto classical home is
     * set empty list view to wizard user add his first formal circles to be used as home.
     * 2. save chosen home option.
     * 3. finish picker activity after home show
     */
    private boolean gotoCustomizedHome() {
        // first read last stored home
        // then, return false or show customized home picker
        final String home = QiupuORM.getSettingValue(this, QiupuORM.HOME_ACTIVITY_ID);
        if (TextUtils.isEmpty(home)) {
//            if (QiupuORM.hasHomeOption(this)) {
//                HomePickerActivity.registerPickerListener(getClass().getName(), this);
//                IntentUtil.gotoHomePickerActivity(this);
//                return true;
//            }
        } else  {
            try {
                long id = Long.parseLong(home);
                if (id > 0) {
                    // if the id is a formal circles, then goto the circle
                    QiupuORM orm = QiupuORM.getInstance(this);
                    UserCircle circle = orm.queryOneCricleInfo(home);
                    if (null != circle) {
                        QiupuApplication.mTopOrganizationId = circle;

                        IntentUtil.gotoOrganisationHome(this,
                                CircleUtils.getLocalCircleName(this, circle.circleid, circle.name),
                                circle.circleid);
                        IntentUtil.loadCircleDirectoryFromServer(this, circle.circleid);
                        finish();
                    }else {
                    	return false;
                    }
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }
    
    private boolean isBorqsUser() {
    	boolean isBorqsUser = false;
    	Cursor tmpCursor = QiupuORM.queryOneuserPhoneEmail(AccountServiceUtils.getBorqsAccountID(), this);
    	if(tmpCursor != null) {
    		if(tmpCursor.moveToFirst()) {
    			do {
    				String tmpInfo = tmpCursor.getString(tmpCursor.getColumnIndex(PhoneEmailColumns.INFO));
    				if(tmpInfo != null && tmpInfo.contains("@borqs.com")) {
    					isBorqsUser = true;
    					break;
    				}
				} while (tmpCursor.moveToNext());
    		}
    		tmpCursor.close();
    		tmpCursor = null;
    	}
    	return isBorqsUser;
    }
    
    @Override
    public boolean onCancelled() {
        finish();
        HomePickerActivity.unregisterPickerListener(getClass().getName());
        return true;
    }

    @Override
    public boolean onPicked(UserCircle circle) {
        finish();
        HomePickerActivity.unregisterPickerListener(getClass().getName());
        return true;
    }
}
