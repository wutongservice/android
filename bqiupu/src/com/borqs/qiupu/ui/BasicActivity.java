package com.borqs.qiupu.ui;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import com.borqs.qiupu.util.*;

import twitter4j.ApkResponse;
import twitter4j.AsyncQiupu;
import twitter4j.ErrorResponse;
import twitter4j.QiupuUser;
import twitter4j.RecommendHeadViewItemInfo;
import twitter4j.Stream;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationBase;
import twitter4j.conf.ConfigurationContext;
import twitter4j.internal.http.HttpClientImpl;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceActivity;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mobstat.StatService;
import com.borqs.account.commons.AccountServiceAdapter;
import com.borqs.account.login.service.AccountService;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.BorqsAccount;
import com.borqs.account.service.BorqsAccountService;
import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.account.service.LocationRequest;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.FriendsActionListner;
import com.borqs.common.listener.LikeListener;
import com.borqs.common.util.AsyncApiUtils;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.FileUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.AbstractStreamRowView;
import com.borqs.common.view.BpcFriendsItemView;
import com.borqs.common.view.CommentItemView;
import com.borqs.common.view.LeftNavigationCallBack;
import com.borqs.common.view.LeftNavigationView;
import com.borqs.common.view.SNSItemView;
import com.borqs.common.view.SearchView;
import com.borqs.qiupu.AccountListener;
import com.borqs.qiupu.AccountServiceConnectListener;
import com.borqs.qiupu.AccountServiceConnectObserver;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.UserAccountObserver;
import com.borqs.qiupu.cache.AnimationImageRun;
import com.borqs.qiupu.cache.ImageCacheManager;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.PhoneEmailColumns;
import com.borqs.qiupu.service.ApkFileManager;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.ui.bpc.AddProfileInfoActivity;
import com.borqs.qiupu.ui.bpc.BpcFriendsActivity;
import com.borqs.qiupu.ui.bpc.BpcFriendsFragmentActivity;
import com.borqs.qiupu.ui.bpc.BpcInformationActivity;
import com.borqs.qiupu.ui.bpc.BpcPostsNewActivity;
import com.borqs.qiupu.ui.bpc.BpcSearchActivity;
import com.borqs.qiupu.ui.bpc.BpcSettingsActivity;
import com.borqs.qiupu.ui.bpc.ProgressInterface;
import com.borqs.qiupu.ui.bpc.RequestActivity;
import com.borqs.qiupu.ui.bpc.UserCircleDetailActivity;
import com.borqs.qiupu.ui.bpc.UserProfileFragmentActivity;
import com.borqs.qiupu.ui.bpc.UsersArrayListActivity;
import com.borqs.qiupu.ui.bpc.UsersCursorListActivity;
import com.borqs.wutong.OrganizationHomeActivity;

public abstract class BasicActivity extends FragmentActivity implements ProgressInterface,
        OnClickListener, LocationRequest.IFLocationListener,
        AccountListener, AccountServiceConnectListener,
        FriendsActionListner, AccountService.IOnAccountLogin,
        LeftNavigationView.LeftNavigationListener, LikeListener,
        BaiduLocationProxy.LocationListener,
        com.borqs.common.view.SearchView.OnQueryTextListener,
        AbstractStreamRowView.SetTopInterface {
    private static final String TAG = "Qiupu.BasicActivity";

    protected QiupuORM orm;
    protected Handler mBasicHandler;
    protected Handler mHandler;
    protected Application mApp;
    public AsyncQiupu asyncQiupu;

    private TextView mTitle;
    protected TextView mSubTitle;
    
    private TextView mTitleActionText;


    protected ImageView mEditTitleBtn;
    protected ImageView mLeftActionBtn;
    protected ImageView mRightActionBtn;
    protected ImageView mMiddleActionBtn;
    
    protected MenuItem mLeftActionBtnMenu;
    protected MenuItem mRightActionBtnMenu;
    protected MenuItem mMiddleActionBtnMenu;

    public static final String USER_NICKNAME = "USER_NICKNAME";
    public static final String USER_CONCERN_TYPE = "USER_CONCERN_TYPE";
    private int displayType = -1;
    public final static int DISPLAY_ADDRESS = 0;
    public final static int DISPLAY_PHONE_NUMBER1 = 1;
    public final static int DISPLAY_PHONE_NUMBER2 = 2;
    public final static int DISPLAY_PHONE_NUMBER3 = 3;
    public final static int DISPLAY_COMPANY = 4;
    public final static int DISPLAY_BIRTHDAY = 5;
    public final static int DISPLAY_USER_IMAGE = 6;
    public final static int DISPLAY_NICK_NAME = 7;
    public final static int DISPLAY_EMAIL1 = 8;
    public final static int DISPLAY_EMAIL2 = 9;
    public final static int DISPLAY_EMAIL3 = 10;
    public final static int DISPLAY_STATUS = 11;

//    public static final int SELECT_USER_CIRCLE = 4024;
    public static final int EDIT_PROFILE_REQUEST_CODE = 4025;

    public static final int userselectcode = 200;

    public static final int REQUEST_CODE_STREAM_FILTER = 5005;

    public static final int CAMERA_WITH_DATA = 3023;
    public static final int PHOTO_PICKED_WITH_DATA = 3021;
    public static final int SELECT_USER_DATE = 3022;
    public static final int PROCESS_PHOTO = 3024;

    private static final int PICK_CONTACT = 4009;

    public final static String IDS_NAME = "oms.android.intent.extra.IDS";
    public final static String EMAIL_PHONES_NAME = "oms.android.intent.extra.EPHONE";
    public final static String NAMES_NAME = "oms.android.intent.extra.NAMES";
    public final static String EMAILS_NAME = "oms.android.intent.extra.EMAILS";
    public final static String PHONES_NAME = "oms.android.intent.extra.PHONES";

    public EditText editTextCallBack;
    protected String editTextContent;
    private int minviteType;

    protected final Object mLock = new Object();
    protected boolean inProcess = false;

    protected ArrayList<ContactSimpleInfo> contactsPhone;
    protected ArrayList<ContactSimpleInfo> contactsEmail;


    private View mTutortialView;

    private static boolean mIsInitVersionChecked = false;

    public boolean isInProcess() {
        synchronized (mLock) {
            return inProcess == true;
        }
    }

    public static final String RESULT = "RESULT";
    public static final String ERROR_MSG = "ERROR_MSG";
    private static final String FORCE_UPDATE = "FORCE_UPDATE";
    private static final String INIT_CHECKED = "INIT_CHECKED";
    protected StatusNotification notify;
    protected static final String BUNDLE_REQUEST_LOGIN_ACTIVITY = "borqs.request.login";
    protected static final String BUNDLE_REQUEST_LOGIN_OK = "borqs.request.login.ok";

    protected boolean fromtab;
    protected boolean fromHome;

    public WeakReference<LeftNavigationCallBack> mLeftNavigationCallBack;

    protected LinearLayout title_container;
    protected boolean supportLeftNavigation = false;
    protected boolean mIsShowNtf = false;

    protected void enableLeftNav() {
        enableLeftNav(true);
    }

    protected void enableLeftNav(boolean enable)
    {
    	supportLeftNavigation = enable;
    }
    
    protected void enableTitleNtf(boolean enable) {
    	mIsShowNtf = enable;
    }

    private static HandlerThread sWorkerThread = null;
	public static Handler sWorker = null;
	
	private synchronized void initUIThread()
	{
		if(sWorkerThread == null)
		{
			sWorkerThread = new HandlerThread("UI-Refresh-Thread");
			sWorkerThread.start();
			sWorker = new Handler(sWorkerThread.getLooper());
		}
	}

	// this is the place to check whether we need use action bar, sub class
	// might override this method if it prefer to alter the default condition.
	//
	protected boolean isUsingActionBar()
	{
		return false && supportLeftNavigation == false && fromtab == false && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;			
	}   

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate, " + this);
        QiupuConfig.enableStrictMode();

        super.onCreate(savedInstanceState);
        
        //QiupuORM.setIsSupportLeftNavigativon(this, supportLeftNavigation);
        
        fromtab = getIntent().getBooleanExtra("fromtab", false);
        fromHome = getIntent().getBooleanExtra("from_home", false);

        prepareActionBar();

        initUIThread();
        mApp = getApplication();
        if (!QiupuService.verifyAccountLogin(mApp)) {
            AccountServiceConnectObserver.registerAccountServiceConnectListener(getClass().getName(), this);
        }
        
        orm = QiupuORM.getInstance(mApp);
        QiupuHelper.setORM(orm);//TODO only to test

        asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null, null);
        asyncQiupu.attachAccountListener(this);
        
       

        createHandler();

        mBasicHandler = new BasicHandler();

        try {
            AccountServiceUtils.getBorqsAccount();
        } catch (SecurityException se) {
            resolveAccountServiceConflict();
        }

        //start service at background, make sure the service is running
        Intent in = new Intent(this, QiupuService.class);
        in.setAction(QiupuService.INTENT_QP_PHONE_BOOT);
        startService(in);

        Intent intent = getIntent();
        boolean loginResult = intent.getBooleanExtra(BUNDLE_REQUEST_LOGIN_OK, false);
        if (loginResult) {
            UserAccountObserver.login();
        }

//        setupLocationListener();

        //we should register this, if logout then login. need to refresh again
        UserAccountObserver.registerAccountListener(getClass().getName(), this);

        notify = new StatusNotification(this);
        
        if (mLeftNavigationCallBack != null && mLeftNavigationCallBack.get() != null) {
            mLeftNavigationCallBack.get().onCreate();
        }
    }

    protected void setupLocationListener() {
        if (QiupuConfig.IS_USE_BAIDU_LOCATION_API) {
            setBDLocationListener();
        } else {
            setLocationListener();
        }
    }

    protected void prepareActionBar() {
        if (isUsingActionBar()) {
            boolean ret = requestWindowFeature(Window.FEATURE_ACTION_BAR | Window.FEATURE_PROGRESS);
            if (ret == true && getActionBar() != null) {
                getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
                getActionBar().setDisplayShowCustomEnabled(true);

                invalidateOptionsMenu();
            }
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
    }

    protected void setBDLocationListener() {
        LocationUtils.setBDLocationListener(this, this);
    }

    protected void setLocationListener() {
        LocationUtils.setLocationListener(this);
    }

    @Override
    public void onPoiFailed() {
        getPoiFailed();
    }

    protected void getPoiFailed() {
        
    }

    @Override
    public void updatePoiWithBaiduApi(String poiJson) {
        updatePoi(poiJson);
    }

    protected void updatePoi(final String poiJson) {
        
    }

    public void updateLocationWithBaiduApi(final BDLocation loc, final String address, final String locString) {
        if (mHandler != null)
            mHandler.post(new Runnable() {
                public void run() {
                    String mapUrl = String.format("<a href='%1$s'>%2$s</a>", BaiduLocationProxy.getInstance(BasicActivity.this).getPureMapsSearchString(
                            BasicActivity.this, loc), address);
                    locationUpdated(mapUrl, locString);
                }
        });
    }

    public void onLocationSucceed(String locString) {
        Log.d(TAG, "onLocationSucceed() locString = " + locString);
        getLocationSucceed(locString);
    }

    public void onLocationFailed(int errorType) {
        getLocationFailed(errorType);
    }

    public void updateLocation(final Location loc) {
        String locString = String.format("longitude=%1$s;latitude=%2$s;altitude=%3$s;speed=%4$s;time=%5$s",
                loc.getLongitude(),
                loc.getLatitude(),
                loc.getAltitude(),
                loc.getSpeed(),
                loc.getTime());

        HttpClientImpl.setLocation(locString);

        getLocationSucceed(locString);

        //try to get address information,
        QiupuORM.sWorker.post(new Runnable() {
            public void run() {
                Address address = LocationRequest.getLocationAddress(BasicActivity.this, loc);
                if (address != null) {
                    final String locationInfo = LocationRequest.getAddressInfo(BasicActivity.this, address);
                    Log.d(TAG, "###########  locationInfo = " + locationInfo);
                    String locString = gLocationUpdate(loc, locationInfo);
                    HttpClientImpl.setLocation(locString);
                } else {
                    // get address failed
                    Log.d(TAG, "================ geo failed");
                    final String locationInfo = getResources().getString(R.string.location_at);
                    gLocationUpdate(loc, locationInfo);
                }
            }
        });
        Log.d(TAG, "change location=" + locString);
    }

    private String gLocationUpdate(final Location loc, final String address) {
        final String locString = String.format("longitude=%1$s;latitude=%2$s;altitude=%3$s;speed=%4$s;time=%5$s;geo=%6$s",
                loc.getLongitude(),
                loc.getLatitude(),
                loc.getAltitude(),
                loc.getSpeed(),
                loc.getTime(),
                address);

        if (mHandler != null)
            mHandler.post(new Runnable() {
                public void run() {
                    String mapUrl = String.format("<a href='%1$s'>%2$s</a>", LocationRequest.getPureMapsSearchString(
                            BasicActivity.this, loc), address);
                    locationUpdated(mapUrl, locString);
                }
            });

        return locString;
    }

    public void onGLocationFailed() {
        getGLocationFailed();
    }

    protected void getGLocationFailed() {
        
    }

    protected void getGLocationSucceed() {
        
    }

    protected void locationUpdated(String mapUrl, String locString) {
        Log.d(TAG, "BasicActivity locationUpdated()");
    }

    protected void getLocationSucceed(String locString) {
        
    }

    protected void getLocationFailed(int errorType) {
        if (QiupuConfig.IS_USE_MIXED_LBS) {
            LocationUtils.deactivateLocationService(this);
            QiupuConfig.setDefaultLocationApi(false);
            LocationUtils.activateLocationService(this);
        }
    }

    protected void openGoogleLocationRequest() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_status = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_status = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (gps_status || network_status) {
            LocationRequest.instance().activate(this);
        } else {
            DialogUtils.showOpenGPSDialog(this);
        }
    }

    // override this to set the title bar action, sub class
    // could alter the default behavior.
    protected void setTitleResource(int resId) {
        setTitleResource(getWindow().getDecorView(), resId);
    }

    protected void setTitleResource(View parent, int resId) {
    	if (isUsingActionBar()) {
    		title_container = (LinearLayout) parent.findViewById(R.id.titlebar_container);
    		if (null != title_container) {
    			title_container.setVisibility(View.GONE);
    		}
    	} else {
    		title_container = (LinearLayout) parent.findViewById(R.id.titlebar_container);
    		if (title_container != null) {
    			if (isUsingActionBar()) {
    				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
    					title_container.setVisibility(View.GONE);
    				} else {
    					title_container.setVisibility(View.VISIBLE);
    				}
    			} else {
    				View content = LayoutInflater.from(this).inflate(resId, null);
    				content.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    				title_container.addView(content);
    			}
    		} else {
    			Log.e(TAG, "why I am null, *****************");
    		}
    	}
    }

    public QiupuORM getORM() {
        return orm;
    }
    
    @Override
    public boolean onSearchRequested() {
    	return super.onSearchRequested();
    }
    
    @Override
    public boolean onQueryTextChange(String newText) {
    	return true;
    }
    
    @Override
    public boolean onQueryTextSubmit(String query, int searchType) {
    	if(searchType == BpcSearchActivity.SEARCH_TYPE_CIRCLE) {
    		IntentUtil.startSearchActivity(this, query, BpcSearchActivity.SEARCH_TYPE_CIRCLE);
    	}else if(searchType == BpcSearchActivity.SEARCH_TYPE_PEOPLE) {
    		IntentUtil.startPeopleSearchIntent(this, query);
    	}else if(searchType == BpcSearchActivity.SEARCH_TYPE_STREAM) {
    		IntentUtil.startSearchActivity(this, query, BpcSearchActivity.SEARCH_TYPE_STREAM, -1);
    	}else {
    		Log.d(TAG, "search no type , IntentUtil onQueryTextSubmit: " + query + " searchtype: " + searchType);    		
    	}
    	
    	return false;
    }

    float mDownMotionX;
    float mDownMotionY;
    private VelocityTracker mVelocityTracker;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        switch (action) {
            case MotionEvent.ACTION_MOVE:
//            	float span = ev.getX() -  mDownMotionX;            	  
//            	if(Math.abs(span) > 100)
//            	{
//            		if(span < 0)
//            			goNextPage();
//            		else
//            			goPrePage();
//            	}
                break;
            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mDownMotionX = ev.getX();
                mDownMotionY = ev.getY();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (Math.abs(ev.getY() - mDownMotionY) > Math.abs(ev.getX() - mDownMotionX)) {
                    Log.d(TAG, "dispatchTouchEvent motion up, ignore possible fling gestures.");
                } else {
                    mVelocityTracker.computeCurrentVelocity(1000);
                    int velocityX = (int) mVelocityTracker.getXVelocity();
                    int velocityY = (int) mVelocityTracker.getYVelocity();
                    if (Math.abs(velocityY) < Math.abs(velocityX) && Math.abs(velocityX) > 200) {
                        if (velocityX < 0) {
                            goNextPage();
                        } else {
                            goPrePage();
                        }
                    }
                }
                mVelocityTracker.clear();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    protected boolean goNextPage() {
        return true;
    }

    protected boolean goPrePage() {
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo i = (AdapterView.AdapterContextMenuInfo) menuInfo;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bpc_context, menu);

        setContextMenuItemVisibility(menu, i.targetView);
    }

    protected void setContextMenuItemVisibility(ContextMenu menu, View targetView) {
        if (BpcFriendsItemView.class.isInstance(targetView)) {
            BpcFriendsItemView bv = (BpcFriendsItemView) (targetView);
            menu.setHeaderTitle(bv.getUser().nick_name);

            menu.findItem(R.id.bpc_post_message).setVisible(true);
            menu.findItem(R.id.bpc_post_message).setIcon(R.drawable.actionbar_icon_release_normal);

            if (bv.getUser().isShortCut == false) {
                menu.findItem(R.id.bpc_add_shortcut).setVisible(true);
                menu.findItem(R.id.bpc_add_shortcut).setIcon(R.drawable.add);
            }

            if (bv.getUser().profile_privacy && BpcFriendsItemView.isalreadyRequestProfile(bv.getUser().pedding_requests) == false) {
                menu.findItem(R.id.bpc_exchange_profile).setVisible(true);
            }

//        } else if (MainActivity.ProfileItemView.class.isInstance(targetView)) {
//            MainActivity.ProfileItemView bv = (MainActivity.ProfileItemView) (targetView);
//            menu.setHeaderTitle(bv.getUser().nick_name);
//            menu.findItem(R.id.bpc_remove_shortcut).setVisible(true);
//            menu.findItem(R.id.bpc_remove_all_shortcut).setVisible(true);
//            menu.findItem(R.id.bpc_post_message).setVisible(true);
//
//            menu.findItem(R.id.bpc_remove_shortcut).setIcon(R.drawable.subtract);
//            menu.findItem(R.id.bpc_remove_all_shortcut).setIcon(R.drawable.subtract);
//            menu.findItem(R.id.bpc_add_shortcut).setIcon(R.drawable.add);
        } else if (AbstractStreamRowView.class.isInstance(targetView)) {
            AbstractStreamRowView streamRowView = (AbstractStreamRowView) targetView;
            final Stream post = streamRowView.getStream();
            boolean needShow = false;

            if (post.canComment) {
                menu.findItem(R.id.bpc_stream_comment).setVisible(true).
                        setIcon(R.drawable.comment_icon_small);
                needShow = true;
            }

            if (post.iLike) {
                menu.findItem(R.id.bpc_stream_unlike).setVisible(true).
                        setIcon(R.drawable.actionbar_icon_delete_praise_normal);
                needShow = true;
            } else if (post.canLike) {
                menu.findItem(R.id.bpc_stream_like).setVisible(true).
                        setIcon(R.drawable.actionbar_icon_praise_normal);
                needShow = true;
            } else {
                menu.findItem(R.id.bpc_stream_like).setVisible(false);
            }

            final long circleId = getTopStreamTargetId();
            if (circleId > 0) {
                addSetTopListMenuItem(menu, post.top_in_targets, circleId);
            }

//            if (post.isOwnBy(getSaveUid())) {
//                menu.findItem(R.id.bpc_item_delete).setVisible(true);
//                needShow = true;
//            } else {
//                menu.findItem(R.id.bpc_item_delete).setVisible(false);
//            }

            if (post.canReshare) {
                menu.findItem(R.id.bpc_stream_reshare).setVisible(true).
                        setIcon(R.drawable.ic_reshare);
                needShow = true;
            }

            menu.setHeaderIcon(R.drawable.main_stream);
            final String textFormat = needShow ? getString(R.string.context_menu_stream_title) :
                    getString(R.string.context_menu_stream_title_readonly);
            final String titleText = String.format(textFormat, post.fromUser.nick_name);
            menu.setHeaderTitle(titleText);
        } else if (CommentItemView.class.isInstance(targetView)) {
            CommentItemView commentItemView = (CommentItemView) targetView;
            final Stream.Comments.Stream_Post comment = commentItemView.getComment();
            boolean needShow = false;

            if (comment.iLike) {
                menu.findItem(R.id.bpc_stream_unlike).setVisible(true).
                        setIcon(R.drawable.actionbar_icon_delete_praise_normal);
                needShow = true;
            } else if (comment.can_like) {
                menu.findItem(R.id.bpc_stream_like).setVisible(true).
                        setIcon(R.drawable.actionbar_icon_praise_normal);
                needShow = true;
            } else {
                menu.findItem(R.id.bpc_stream_like).setVisible(false);
            }

            if (comment.isOwnBy(getSaveUid())) {
                menu.findItem(R.id.bpc_item_delete).setVisible(true);
                needShow = true;
            } else {
                menu.findItem(R.id.bpc_item_delete).setVisible(false);
            }

            
            if (needShow) {
                menu.setHeaderIcon(R.drawable.main_stream);
                final String titleText = String.format(getString(R.string.context_menu_comment_title),
                        comment.username);
                menu.setHeaderTitle(titleText);
            }
        }

        if (SNSItemView.class.isInstance(targetView)) {
            if (TextUtils.isEmpty(((SNSItemView)targetView).getText())) {
                menu.findItem(R.id.bpc_item_copy).setVisible(false);
            } else {
                menu.findItem(R.id.bpc_item_copy).setVisible(true);
            }

            if (BpcApiUtils.isActivityReadyForAction(this, IntentUtil.WUTONG_ACTION_TAGS)) {
                menu.findItem(R.id.bpc_item_tags).setVisible(true);
            }
        }
    }

    @Override
    public long getTopStreamTargetId() {
        return -1;
    }

    @Override
    public void notifyTopListChanged() {
        loadRefresh();
    }

    protected static void addSetTopListMenuItem(ContextMenu menu, String targets, long circleId) {
        if (TextUtils.isEmpty(targets) == false) {
            String[] targetArray = targets.split(",");
            ArrayList<String> targetList = new ArrayList<String>();
            for (String target : targetArray) {
                if (TextUtils.isEmpty(target) == false) {
                    targetList.add(target);
                }
            }

            if (targetList.contains(String.valueOf(circleId))) {
                menu.findItem(R.id.bpc_item_unset_top).setVisible(true).
                        setIcon(R.drawable.btn_show_out);
            } else {
                menu.findItem(R.id.bpc_item_set_top).setVisible(true).
                        setIcon(R.drawable.btn_show_in);
            }
        } else {
            menu.findItem(R.id.bpc_item_set_top).setVisible(true).
                    setIcon(R.drawable.btn_show_in);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo i = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int i1 = item.getItemId();
        return handleSelectedContextItem(i1, i.targetView);
    }

    protected boolean handleSelectedContextItem(int i1, View targetView) {
        if (i1 == R.id.bpc_post_message) {
            if (BpcFriendsItemView.class.isInstance(targetView)) {
                BpcFriendsItemView bv = (BpcFriendsItemView) (targetView);
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(String.valueOf(bv.getUserID()), bv.getUser().nick_name);
                postWall(bv.getUserID(), true, map);
//            } else if (MainActivity.ProfileItemView.class.isInstance(targetView)) {
//                MainActivity.ProfileItemView pv = (MainActivity.ProfileItemView) targetView;
//                postWall(pv.getUser().uid, true);
            }

        } else if (i1 == R.id.bpc_add_shortcut) {
            if (BpcFriendsItemView.class.isInstance(targetView)) {
                BpcFriendsItemView bv = (BpcFriendsItemView) (targetView);
                orm.setShortCutForUser(bv.getUserID(), true);
            }

        } else if (i1 == R.id.bpc_remove_shortcut) {
//            if (MainActivity.ProfileItemView.class.isInstance(targetView)) {
//                MainActivity.ProfileItemView bv = (MainActivity.ProfileItemView) (targetView);
//                orm.setShortCutForUser(bv.getUser().uid, false);
//                reloadShortCut();
//            }

        } else if (i1 == R.id.bpc_remove_all_shortcut) {
//            if (MainActivity.ProfileItemView.class.isInstance(targetView)) {
//                orm.clearShortCutForUser();
//                reloadShortCut();
//            }

        } else if (i1 == R.id.bpc_exchange_profile) {
            if (BpcFriendsItemView.class.isInstance(targetView)) {
                BpcFriendsItemView bv = (BpcFriendsItemView) (targetView);

                //show dialog
                Bundle data = new Bundle();
                data.putLong("uid", bv.getUserID());

                showDialog(DIALOG_EXCHANGE_PROFILE, data);
            }

        } else if (i1 == R.id.bpc_stream_comment) {
            if (AbstractStreamRowView.class.isInstance(targetView)) {
                AbstractStreamRowView streamRowView = (AbstractStreamRowView) targetView;
                streamRowView.gotoStreamItemComment();
            }

        } else if (i1 == R.id.bpc_stream_like) {
            if (AbstractStreamRowView.class.isInstance(targetView)) {
                AbstractStreamRowView streamRowView = (AbstractStreamRowView) targetView;
//                streamRowView.likePost(this);
                likeStream(streamRowView.getStream());
            } else if (CommentItemView.class.isInstance(targetView)) {
                CommentItemView commentItemView = (CommentItemView) targetView;
                final Stream.Comments.Stream_Post comment = commentItemView.getComment();
                likeComment(comment);
            }

        } else if (i1 == R.id.bpc_stream_unlike) {
            if (AbstractStreamRowView.class.isInstance(targetView)) {
                AbstractStreamRowView streamRowView = (AbstractStreamRowView) targetView;
//                streamRowView.unlikePost(this);
                unLikeStream(streamRowView.getStream());
            } else if (CommentItemView.class.isInstance(targetView)) {
                CommentItemView commentItemView = (CommentItemView) targetView;
                final Stream.Comments.Stream_Post comment = commentItemView.getComment();
                unLikeComment(comment);
            }

        } else if (i1 == R.id.bpc_stream_reshare) {
            if (AbstractStreamRowView.class.isInstance(targetView)) {
                AbstractStreamRowView streamRowView = (AbstractStreamRowView) targetView;
                streamRowView.gotoStreamReshare();
            }
        } else if (i1 == R.id.bpc_item_delete) {
            if (AbstractStreamRowView.class.isInstance(targetView)) {
                AbstractStreamRowView streamRowView = (AbstractStreamRowView) targetView;
            }
        } else if (i1 == R.id.bpc_item_copy) {
            SNSItemView.copyItem(targetView);
            showShortToast(R.string.content_copy);
        } else if (i1 == R.id.bpc_item_tags) {
            SNSItemView.tagItem(targetView);
        } else if (i1 == R.id.bpc_item_set_top) {
            if (AbstractStreamRowView.class.isInstance(targetView)) {
                final long circleId = getTopStreamTargetId();
                AbstractStreamRowView streamRowView = (AbstractStreamRowView) targetView;
                String stream_id = streamRowView.getStream().post_id;
                setTopList(String.valueOf(circleId), stream_id, true);
            }
//            setTopList(targetView, true);
        } else if (i1 == R.id.bpc_item_unset_top) {
            if (AbstractStreamRowView.class.isInstance(targetView)) {
                final long circleId = getTopStreamTargetId();
                AbstractStreamRowView streamRowView = (AbstractStreamRowView) targetView;
                String stream_id = streamRowView.getStream().post_id;
                setTopList(String.valueOf(circleId), stream_id, false);
            }
//            setTopList(targetView, false);
//            setTopListMenuListener(i1, targetView);
        } else {
        }

        return true;
    }

    protected void reloadShortCut() {

    }

    protected void postWall(long uid, boolean isPrivate, HashMap<String, String> receiverMap) {
        IntentUtil.startComposeIntent(this, String.valueOf(uid), isPrivate, receiverMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.qiupu_option_menu, menu);

        mLeftActionBtnMenu   = menu.findItem(R.id.menu_left);
        mMiddleActionBtnMenu = menu.findItem(R.id.menu_middle);
        mRightActionBtnMenu  = menu.findItem(R.id.menu_right);
        
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        mLeftActionBtnMenu   = menu.findItem(R.id.menu_left);
        mMiddleActionBtnMenu = menu.findItem(R.id.menu_middle);
        mRightActionBtnMenu  = menu.findItem(R.id.menu_right);
        
        // process main UI activity
        if (BpcFriendsActivity.class.isInstance(this)
                || BpcFriendsFragmentActivity.class.isInstance(this)) {
            menu.findItem(R.id.menu_post_towall).setVisible(true);
            menu.findItem(R.id.menu_search).setVisible(false);
        } else if (UserCircleDetailActivity.class.isInstance(this)) {
            menu.findItem(R.id.menu_refresh).setVisible(true);
            menu.findItem(R.id.menu_search).setVisible(false);
            menu.findItem(R.id.menu_delete_people).setVisible(true);
        } else if (UserProfileFragmentActivity.class.isInstance(this)) {
            menu.findItem(R.id.menu_refresh).setVisible(true);
            menu.findItem(R.id.menu_search).setVisible(false);
        } else if (RequestActivity.class.isInstance(this)) {
            menu.findItem(R.id.menu_refresh).setVisible(false);
            menu.findItem(R.id.menu_search).setVisible(false);
//        } else if (MainActivity.class.isInstance(this)) {
//            menu.findItem(R.id.menu_search).setVisible(false);
//            menu.findItem(R.id.menu_feedback).setVisible(true);
//            menu.findItem(R.id.menu_share_qiupu).setVisible(true);
//            menu.findItem(R.id.menu_version_check).setVisible(true);
//            menu.findItem(R.id.menu_refresh).setVisible(false);
//            menu.findItem(R.id.menu_settings).setVisible(true);
        } else if (UserCircleDetailActivity.class.isInstance(this)
                || UsersCursorListActivity.class.isInstance(this)
                || UsersArrayListActivity.class.isInstance(this)) {
            menu.findItem(R.id.menu_refresh).setVisible(true);
            menu.findItem(R.id.menu_search).setVisible(false);
            menu.findItem(R.id.menu_share_qiupu).setVisible(false);
            menu.findItem(R.id.menu_feedback).setVisible(false);
            menu.findItem(R.id.menu_version_check).setVisible(false);
        } else if (BpcPostsNewActivity.class.isInstance(this) 
        		|| OrganizationHomeActivity.class.isInstance(this)) {

            if (mLeftNavigationCallBack != null && mLeftNavigationCallBack.get() != null) {
                menu.findItem(R.id.menu_feedback).setVisible(true);
                menu.findItem(R.id.menu_share_qiupu).setVisible(true);
                menu.findItem(R.id.menu_version_check).setVisible(true);
                menu.findItem(R.id.menu_settings).setVisible(true);
                menu.findItem(R.id.menu_exit_qiupu).setVisible(true);
            }
            menu.findItem(R.id.menu_refresh).setVisible(false);
            menu.findItem(R.id.menu_search).setVisible(false);
        } else if (RequestActivity.class.isInstance(this)) {
            if (isUsingActionBar()) {
                menu.findItem(R.id.menu_refresh).setVisible(true);
            }
        } else {
            menu.findItem(R.id.menu_search).setVisible(false);
            menu.findItem(R.id.menu_refresh).setVisible(false);
        }


//        if(fromtab == false)
//        {
//        	//show home menu
//        	menu.findItem(R.id.menu_home).setVisible(true);
//
//        	if(MainActivity.class.isInstance(this))
//        	{
//        		menu.findItem(R.id.menu_home).setVisible(false);
//        	}
//        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if(isUsingActionBar())
        {
        	if (i == android.R.id.home)
        	{
        		// The user clicked on the Messaging icon in the action bar. Take them back from
                // wherever they came from
                return performGoHomeAction();
        	}
        }
        
        if (i == R.id.menu_refresh) {
            loadRefresh();

        } else if (i == R.id.menu_search) {
            loadSearch();

        } else if (i == R.id.menu_logout) {
            showDialog(DIALOG_LOGOUT);

        } else if (i == R.id.menu_share_qiupu) {
            shareQiuPuApp();

        } else if (i == R.id.menu_feedback) {
            feedBack();

        } else if (i == R.id.menu_version_check) {
            checkQiupuVersion(false);
        } else if (i == R.id.menu_post_towall) {
            postToSomeoneWall();

        } else if (i == R.id.menu_exit_qiupu) {
            doExit();

        } else if (i == R.id.menu_delete_apks) {
            deleteApp();

        } else if (i == R.id.menu_update_all) {
            updateAll();

        } else if (i == R.id.menu_backup_all) {
            backupAll();

        } else if (i == R.id.menu_delete_people) {
            deletePeopleInCircle();

        } else if (i == R.id.menu_settings) {
            gotoSettingActivity();

        }else if(i == R.id.menu_left || i == R.id.menu_middle || i == R.id.menu_right )
        {
            return true;
        }
        else {
            Log.e(TAG, "onOptionsItemSelected, unkexpected item:" + item);

        }

        return super.onOptionsItemSelected(item);
    }

    protected void postToSomeoneWall() {
        IntentUtil.startComposeIntent(this);
    }

    protected void feedBack() {
        if (AccountServiceUtils.isAccountReady()) {
            Intent intent = new Intent(this, QiupuFeedbackActivity.class);
            startActivity(intent);
        } else {
            gotoLogin();
        }
    }

    private void checkQiupuVersion(final boolean initCheck) {
        if (QiupuConfig.LOGD) Log.d(TAG, "checkQiupVersion enter");

        if (!testValidConnectivity()) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }

        final PackageManager packageManager = this.getPackageManager();
        final String pkgName = getPackageName();

        final String ticket;
        if (!initCheck) {
            showDialog(DIALOG_CHECK_QIUPU_VERSION);
            ticket = getSavedTicket();
        } else {
            ticket = "";
        }

        try {
            final int versionCode = packageManager.getPackageInfo(pkgName, 0).versionCode;
            asyncQiupu.getApkDetailInformation(ticket, pkgName, false, new TwitterAdapter() {
                public void getApkDetailInformation(ApkResponse info) {
                    if (QiupuConfig.LOGD) Log.d(TAG, "finish getApkDetailInformation info:" + info +
                            ", for :" + pkgName + ", version:" + versionCode);
                    if(info == null) {
                    	Log.e(TAG, "getApkDetailInformation: back apk info is null");
                    	return;
                    }

                    boolean haveNewVersion = false;
                    if (null != info && info.latest_versioncode > versionCode) {
//                        ApkResponse qiupuApk = orm.getUserApplication(getSaveUid(), pkgName);
//                        if (null != qiupuApk) {
//                            qiupuApk.status = ApkResponse.APKStatus.STATUS_NEED_UPDATE;
//                            qiupuApk.versioncode = versionCode;
//                            orm.syncApkResponse(getSaveUid(), qiupuApk);
//                        }

                        haveNewVersion = true;
                    } else {
                        haveNewVersion = false;
                    }
                    
                    Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_CHECK_UPDATE_QIUPU_END);
                    msg.getData().putBoolean(RESULT, true);
                    msg.getData().putBoolean(INIT_CHECKED, initCheck);
                    msg.getData().putBoolean("haveNewVersion", haveNewVersion);
                    msg.getData().putString("versionFeatures", info.recent_change);
                    msg.getData().putString("version", info.versionname);
                    msg.getData().putLong("size", info.apksize);
                    msg.sendToTarget();
                }

                public void onException(TwitterException ex, TwitterMethod method) {
                    TwitterExceptionUtils.printException(TAG, "getApkDetailInformation exception:", ex);
                    Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_CHECK_UPDATE_QIUPU_END);
                    msg.getData().putBoolean(RESULT, false);
                    msg.getData().putBoolean(INIT_CHECKED, initCheck);
                    msg.getData().putBoolean(FORCE_UPDATE,
                            ErrorResponse.FORCE_VERSION_UPDATE == ex.getStatusCode());
                    msg.getData().putString("versionFeatures", ex.getRecentChange());
                    msg.getData().putString("version", ex.getVersion());
                    msg.getData().putLong("size", ex.getSize());
                    msg.sendToTarget();
                }
            });
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void logout() {
        Log.d(TAG, "do logout");
        //always remove the outside account
        setExpiredSession();

        orm.logout();
        UserAccountObserver.logout();
        AccountServiceUtils.borqsLogout(false);

        try {
            Intent service = new Intent(BasicActivity.this, QiupuService.class);
            stopService(service);

            Intent bservice = new Intent(BasicActivity.this, BorqsAccountService.class);
            stopService(bservice);

            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception ne) {
        }


        finish();
    }

    protected void inLoadingDataFromServer(boolean promptHint) {
        if (promptHint) {
            Toast.makeText(this, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
        }
    }

    protected boolean isFromTabView() {
        return fromtab;
    }   

    protected static boolean isEmpty(String msg) {
        return msg == null || msg.length() == 0;
    }

    protected void attachStreamProperty(int resId, Stream stream) {
        if (null != stream) {
            attachStreamProperty(resId, stream, /*true*/ stream.fromUser.uid != getSaveUid());
        }
    }

    protected void attachStreamProperty(int resId, Stream stream, boolean readOnly) {
        if (null != stream) {
            attachStreamProperty(resId, stream.canComment, stream.canLike, stream.canReshare, readOnly);
        }
    }

    protected void attachStreamProperty(int resId, boolean canComment, boolean canLike, boolean canReshare) {
        attachStreamProperty(resId, canComment, canLike, canReshare, false);
    }

    protected void attachStreamProperty(int resId, boolean canComment, boolean canLike, boolean canReshare,
                                        final boolean readOnly) {
        View propView = findViewById(resId);
        if (null != propView && propView instanceof TextView) {

            ArrayList<String> enableText = new ArrayList<String>();
            ArrayList<String> disableText = new ArrayList<String>();

            if (canComment) {
                enableText.add(getString(R.string.news_feed_comment));
            } else {
                disableText.add(getString(R.string.news_feed_comment));
            }

            if (canLike) {
                enableText.add(getString(R.string.news_feed_like));
            } else {
                disableText.add(getString(R.string.news_feed_like));
            }

            if (canReshare) {
                enableText.add(getString(R.string.news_feed_reshare));
            } else {
                disableText.add(getString(R.string.news_feed_reshare));
            }

            OnClickListener listener = new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (readOnly) {
                        showCustomToast(R.string.prompt_stream_property_readonly);
                    } else {
                        setCommentSettingListener();
                    }
                }
            };

            TextView textView = (TextView) propView;
            textView.setText(StringUtil.formatOnOffHtmlString("ON", enableText, "OFF", disableText));
            if (readOnly) {
                textView.setCompoundDrawables(null, null, null, null);
            } else {
                textView.setCompoundDrawables(null, getResources().getDrawable(R.drawable.ic_settings), null, null);
            }
            textView.setVisibility(View.VISIBLE);
            textView.setOnClickListener(listener);
        }

    }

    protected void setCommentSettingListener() {
        
    }

    protected void shareAction() {

    }

    protected void shootLoginActivity(String username, String pwd) {
        if (!BorqsAccountService.refetchBorqsAccount(getApplicationContext(), this, this)
                && !QiupuConfig.FORCE_BORQS_ACCOUNT_SERVICE_USED) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(BUNDLE_REQUEST_LOGIN_ACTIVITY, IntentUtil.ACTION_MAIN_ACTIVITY);
            intent.putExtra("username", username);
            intent.putExtra("pwd", pwd);
            startActivity(intent);
//    	    startActivityForResult(intent, RESULTCODE_LOGIN);
        }
    }

    protected void shareQiuPuApp() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.recommend_qiupu_desc),
                QiupuHelper.getQpApkUrl()));

        try {
            startActivity(Intent.createChooser(send, getString(R.string.recommend_qiupu)));//name
        } catch (android.content.ActivityNotFoundException ex) {
        }
    }

    protected boolean isUsingTabNavigation(Context con, boolean fromtab) {
    	if (fromtab) {
            return true;
        }

//        return QiupuORM.isUsingTabNavigation(con);
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        QiupuHelper.updateDropDownDialogUI(newConfig);
        boolean isUsingActionBar = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;
        if (isUsingActionBar) {
            invalidateOptionsMenu();
            return;
        }

        title_container = (LinearLayout) findViewById(R.id.titlebar_container);

        // why do this?
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
                if (title_container != null) {
//                        title_container.setVisibility(View.GONE);
                }
        }
        else
        {
                if (title_container != null) {
                        if(isUsingActionBar() == false)
                        {
                                title_container.setVisibility(View.VISIBLE);
                        }
                }
        }
        
    }

	@Override
    public final void setContentView(int layoutResID) {
        boolean skip = false;

        if ((isUsingTabNavigation(this, fromtab) == false && supportLeftNavigation) || mIsShowNtf) {
            int overlayId = getOverlayContentId();
            if (overlayId > 0) {
                super.setContentView(overlayId);
                skip = inflateNavigatingContentView(layoutResID);
            }
        }
        
        if(mIsShowNtf) {
        	overrideSlideIcon(R.drawable.ic_back_holo_dark, new View.OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
            });
        }

        if (!skip) {
            super.setContentView(layoutResID);

            final View parent = getWindow().getDecorView();
            setTitleResource(parent, R.layout.title_bar_base);

            initActionBarContent();

            if (fromtab == false) {
                showSlideToggle(parent, new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                });
            } else {
                View headTitle = findViewById(R.id.layout_title);
                if (null != headTitle) {
                    headTitle.setPadding((int)getResources().getDimension(R.dimen.title_bar_title_left_padding), 0, 0, 0);
                }
            }

            initHeadViews(parent);
        }
    }

    protected int getOverlayContentId() {
        return -1;
    }

    protected boolean inflateNavigatingContentView(int layoutResID) {
        return false;
    }

    protected void showSlideToggle(View.OnClickListener listener) {
        showSlideToggle(getWindow().getDecorView(), listener);
    }

    protected void showSlideToggle(View parent, OnClickListener listener) {
        View slideToggle = parent.findViewById(R.id.img_slide);
        if (null != slideToggle) {
            slideToggle.setVisibility(View.VISIBLE);
            slideToggle.setOnClickListener(listener);

            View headTitle = parent.findViewById(R.id.layout_title);
            if (null != headTitle) {
                headTitle.setPadding(0, 0, 0, 0);
            }
        }
    }
    
    protected void overrideSlideIcon(int resId, OnClickListener listener) {
    	ImageView slideToggle = (ImageView) findViewById(R.id.img_slide);
    	if(null != slideToggle) {
    		slideToggle.setImageResource(resId);
            slideToggle.setOnClickListener(listener);
    	}
	}

    protected void initActionBarContent() {
        if (isUsingActionBar() && getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    private class Action {
        public Drawable icon;
        public String displayname;
        public String intent;
        public int actionid;
    }

    public ArrayList<Action> useActions = new ArrayList<Action>();
    public ArrayList<Action> guestActions = new ArrayList<Action>();

    public class ActionsAdapter extends BaseAdapter {
        private ArrayList<Action> actions;

        public int getCount() {
            return actions.size();
        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup arg2) {
            final ViewHolder holder;
            final Action action = actions.get(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(BasicActivity.this).inflate(R.layout.main_tab_more_item, null);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon_left);
                holder.title = (TextView) convertView.findViewById(R.id.id_title_tv);
                holder.goIcon = (ImageView) convertView.findViewById(R.id.icon_right);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.icon.setBackgroundDrawable(action.icon);
            holder.title.setText(action.displayname);

            return convertView;
        }

        class ViewHolder {
            public ImageView icon;
            public TextView title;
            public ImageView goIcon;
        }
    }

    private void resetActionListUi() {
        ListView naviList = (ListView) findViewById(R.id.navigation_list);
        if (null != naviList) {
            ActionsAdapter adapter = (ActionsAdapter) naviList.getAdapter();
            if (null != adapter) {
                if (AccountServiceUtils.isAccountReady()) {
                    adapter.actions = useActions;
                } else {
                    adapter.actions = guestActions;
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    protected void gotoAboutActivity() {
        Intent abouttent = new Intent(this, AboutActivity.class);
        startActivity(abouttent);
    }
    
    public void closeSlider() {
//        View scrollView = findViewById(R.id.myScrollView);
//        if (scrollView != null) {
//            ((AllScrolllScreen) scrollView).CloseSlider();
//        }
    }
    
    public void openSlider() {
//        View scrollView = findViewById(R.id.myScrollView);
//        if (scrollView != null) {
//            ((AllScrolllScreen) scrollView).OpenSlider();
//        }
    }
    
    public void finishCurrentActivity()
    {
    	finish();
    }
    
    public boolean isShowNotification()
    {
    	return true;
    }

    protected abstract void createHandler();

    protected final static int RESULTCODE_LOGIN = BorqsAccountService.RESULTCODE_LOGIN;

    protected void gotoLogin() {
        BorqsAccountService.gotoLogin(this, this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        if (resultCode != RESULT_OK) {
            Log.d(TAG, "onActivityResult, return with not OK result.");
            return;
        }

        if (requestCode == RESULTCODE_LOGIN) {
            Log.d(TAG, "login in suc" + this);
        } else if (requestCode == PICK_CONTACT) {
            if (null == data) {
                Log.d(TAG, "onActivityResult, ignore empty result from CONTACT PICKER");
                return;
            }

            long[] ids_name = data.getLongArrayExtra(IDS_NAME);

            ArrayList<ContactSimpleInfo> phoneList = new ArrayList<ContactSimpleInfo>();
            ArrayList<ContactSimpleInfo> emailList = new ArrayList<ContactSimpleInfo>();

            String[] dataArray = data.getStringArrayExtra(EMAIL_PHONES_NAME);
            final int size = null == dataArray ? 0 : dataArray.length;
            for (int i = 0; i < size; i++) {
                ContactSimpleInfo info = new ContactSimpleInfo();
                if (TextUtils.isEmpty(dataArray[i])) {
                    Log.e(TAG, "onActivityResult, PICK_CONTACT with empty phone#, i:" + i);
                } else {

                    if (StringUtil.isValidEmail(dataArray[i])) {
                        info.display_name_primary = getContactNameByPrimaryColumn(dataArray[i], ContactsContract.CommonDataKinds.Email._ID);
                        info.email = dataArray[i];
                        emailList.add(info);
                    } else {
                        info.display_name_primary = getContactNameByPhoneNumber(dataArray[i]);
                        info.phone_number = dataArray[i];
                        phoneList.add(info);
                    }
                    Log.v(TAG, "data and name :" + dataArray[i] + "  " + info.display_name_primary);
                }
            }
            contactsPhone = phoneList;
            contactsEmail = emailList;
            Log.d(TAG, "onActivityResult, PICK_CONTACT, selected phone/email count:"
                    + phoneList.size() + "/" + emailList.size());

            showDialog(SEND_MESSAGE_DIALOG);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void initHeadViews() {
        initHeadViews(getWindow().getDecorView());
    }

    protected void initHeadViews(View parent) {
        Log.d(TAG, "initHeadViews");

        mTitle = (TextView) parent.findViewById(R.id.head_title);
        mSubTitle = (TextView) parent.findViewById(R.id.sub_head_title);
        mTitleActionText = (TextView) parent.findViewById(R.id.head_action_text);

        mEditTitleBtn = (ImageView) parent.findViewById(R.id.head_action_edit_title);
        if (mEditTitleBtn != null) mEditTitleBtn.setOnClickListener(this);

        mLeftActionBtn = (ImageView) parent.findViewById(R.id.head_action_left);
        if (mLeftActionBtn != null) mLeftActionBtn.setOnClickListener(this);

        mMiddleActionBtn = (ImageView) parent.findViewById(R.id.head_action_middle);
        if (mMiddleActionBtn != null) mMiddleActionBtn.setOnClickListener(this);

        mRightActionBtn = (ImageView) parent.findViewById(R.id.head_action_right);
        if (mRightActionBtn != null) mRightActionBtn.setOnClickListener(this);
    }

    protected void overrideRightActionBtn(int drawableid, final View.OnClickListener click) {
        if(isUsingActionBar() && getActionBar() != null)
        {
            mRightActionBtnMenu.setIcon(drawableid);
            mRightActionBtnMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {                
                public boolean onMenuItemClick(MenuItem item) {
                    click.onClick(null);
                    return false;
                }
            });
        }
        else
        {
            if (mRightActionBtn != null) {
                mRightActionBtn.setImageResource(drawableid);
                mRightActionBtn.setOnClickListener(click);
            }
        }
    }
    
    protected void overrideMiddleActionBtn(int drawableid, final View.OnClickListener click) {
        if(isUsingActionBar() && getActionBar() != null)
        {
            mMiddleActionBtnMenu.setIcon(drawableid);
            mMiddleActionBtnMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {                
                public boolean onMenuItemClick(MenuItem item) {
                    click.onClick(null);
                    return false;
                }
            });
        }else{
            if (mMiddleActionBtn != null) {
                mMiddleActionBtn.setImageResource(drawableid);
                mMiddleActionBtn.setOnClickListener(click);
            }
        }
    }

    protected void overrideToggleActionButton() {
        View slideToggle = findViewById(R.id.img_slide);
        if (slideToggle != null) {
            setToggleClickListener(slideToggle);
        }
    }

    protected void setToggleClickListener(View slideToggle) {
        
    }

    protected void overrideEditTitleActionBtn(int drawableid, final View.OnClickListener click) {
        if(isUsingActionBar() && getActionBar() != null)
        {
//            mLeftActionBtnMenu.setIcon(drawableid);
//            mLeftActionBtnMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {                
//                public boolean onMenuItemClick(MenuItem item) {
//                    click.onClick(null);
//                    return false;
//                }
//            });
        }else{
            if (mEditTitleBtn != null) {
                mEditTitleBtn.setImageResource(drawableid);
                mEditTitleBtn.setOnClickListener(click);
            }
        }
    }

    protected void overrideLeftActionBtn(int drawableid, final View.OnClickListener click) {
        if(isUsingActionBar() && getActionBar() != null)
        {
            mLeftActionBtnMenu.setIcon(drawableid);
            mLeftActionBtnMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {                
                public boolean onMenuItemClick(MenuItem item) {
                    click.onClick(null);
                    return false;
                }
            });
        }else{
            if (mLeftActionBtn != null) {
                mLeftActionBtn.setImageResource(drawableid);
                mLeftActionBtn.setOnClickListener(click);
            }
        }
    }

    protected void overrideRightTextActionBtn(int textid, final View.OnClickListener click) {
        if (mTitleActionText != null) {
            mTitleActionText.setText(textid);
            mTitleActionText.setOnClickListener(click);
        }
    }
    
    protected void setHeadTitle(final String title) {
        mBasicHandler.post(new Runnable() {
            public void run() {
            	if(supportLeftNavigation == false && isUsingActionBar() && getActionBar() != null)
            	{
            	    setTitle(title);
            	}
            	else
            	{
	                if (mTitle != null)
	                    mTitle.setText(title);
	                else
	                	setTitle(title);
            	}                
            }
        });
    }

    protected void setHeadTitle(final int resid) {
    	mBasicHandler.post(new Runnable() {
    		public void run() {
    			if(supportLeftNavigation == false && isUsingActionBar() && getActionBar() != null)
    			{
    				setTitle(resid);
    			}
    			else
    			{
    				if (mTitle != null)
    					mTitle.setText(resid);
    				else
    					setTitle(resid);
    			}
    		}
    	});
    }
    
    protected void setSubTitle(final String title) {
    	mBasicHandler.post(new Runnable() {
    		public void run() {
    			if (mSubTitle != null) {
    				mSubTitle.setVisibility(View.VISIBLE);
    				mSubTitle.setText(title);
    			}
    		}
    	});
    }
    
    protected void setSubTitle(final int resid) {
        mBasicHandler.post(new Runnable() {
            public void run() {
                if (mSubTitle != null) {
                    mSubTitle.setVisibility(View.VISIBLE);
                    mSubTitle.setText(resid);
                }
            }
        });
    }
    
    protected void showTitleSpinnerIcon(boolean flag) {
        if(flag) {
//            mTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.spinner_ab_default_holo_light), null);
        	if(mTitle != null) {
        		mTitle.setBackgroundResource(R.drawable.spinner_bg);
        		mTitle.setOnClickListener(this);
        	}
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AccountServiceConnectObserver.unregisterAccountServiceConnectListener(getClass().getName());
        UserAccountObserver.unregisterAccountListener(getClass().getName());
        
        if (QiupuConfig.IS_USE_BAIDU_LOCATION_API == false) {
            LocationRequest.instance().setLocationListener(null);
        }

        notify = null;
        ImageCacheManager.ContextCache.revokeAllImageView(this);

        Log.d(TAG, "onDestroy" + this);

        //clear for left
        if (mLeftNavigationCallBack != null && mLeftNavigationCallBack.get() != null) {
            mLeftNavigationCallBack.get().onDestroy();

            mLeftNavigationCallBack.clear();
        }

        mLeftNavigationCallBack = null;
        // comment out while it might be crash from sub class.
        // asyncQiupu = null;
        notify     = null;
        
        //
        //no remove will crash
        //orm = null;
        //mBasicHandler = null;
        //mHandler = null;
        
        mApp = null;
        mTitle = null;
        mSubTitle = null;
        
        mLeftActionBtn = null;
        mRightActionBtn = null;
        mMiddleActionBtn = null;
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "onPause=" + this);

        StatService.onPause(this);

        lastDownKeyCode = 0;
        showProgressBtn(false);

        if (mLeftNavigationCallBack != null && mLeftNavigationCallBack.get() != null) {
            mLeftNavigationCallBack.get().onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume=" + this);

        StatService.onResume(this);

        showProgressBtn(false);

        if (mLeftNavigationCallBack != null && mLeftNavigationCallBack.get() != null) {
            mLeftNavigationCallBack.get().onResume();
        }
    }

    public synchronized void onLogin() {
        try {
            AccountServiceUtils.onLogin();

            resetActionListUi();

            if (asyncQiupu == null) {
                asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null, null);
                asyncQiupu.attachAccountListener(this);
            }

            loadRefresh();

        } catch (SecurityException se) {
            resolveAccountServiceConflict();
        }
    }


    public synchronized void onLogout() {
        AccountServiceUtils.onLogout();
        resetActionListUi();
    }

    public void onCancelLogin() {
        finish();
    }

    class BasicHandler extends Handler {
        public static final int PROGRESS_LOAD = 0x1;
        public static final int PROGRESS_LOAD_OK = 0x2;
        public static final int PROGRESS_LOAD_FAILED = 0x3;
        public static final int RETWEET_LIKE_END = 0x4;
        public static final int CREATE_CIRCLE_END = 0x5;
        public static final int CIRCLE_SET_END = 0x8;
        public static final int EXCHANGE_VCARD = 0x9;
        public static final int DELETE_USER = 0x11;
        public static final int USER_SET_CIRCLE_END = 0x12;
        public static final int REQUEST_PROFIEL_END = 0x13;
        public static final int UPDATE_USER_INFO_END = 0x14;
        public static final int DOWNLOAD_APK_END = 0x15;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS_LOAD:
                    uiLoadBegin();
                    break;
                case PROGRESS_LOAD_OK:
                    uiLoadEnd();
                    break;
                case PROGRESS_LOAD_FAILED:
                    uiLoadEnd();
                    break;

                case QiupuMessage.MESSAGE_ADD_FAVORITES: {
                    if (ensureAccountLogin()) {
                        showDialog(DIALOG_ADD_FORITES);
                        doAddFavorite((ApkResponse) msg.getData().getSerializable(QiupuMessage.BUNDLE_APKINFO));
                    }
                    break;
                }
                case QiupuMessage.MESSAGE_ADD_FAVORITES_END: {
                    try {
                        dismissDialog(DIALOG_ADD_FORITES);
                    } catch(Exception e) {}
                    if (msg.getData().getBoolean(RESULT, false)) {
                        //no need show toast
//                    showOperationSucToast(true);
                    } else {
                        showOperationFailToast(msg.getData().getString(ERROR_MSG), true);
                    }
                    break;
                }
                case QiupuMessage.MESSAGE_LOAD_DATA_END: {
                    end();
                    doGetFriendsEndCallBack(msg);
//				doActionFriendEndCallBack(msg);
                    break;
                }
                case QiupuMessage.MESSAGE_REMOVE_FAVORITES: {
                    showDialog(DIALOG_REMOVE_FORITES);
                    doRemoveFavorite((ApkResponse) msg.getData().getSerializable(QiupuMessage.BUNDLE_APKINFO));
                    break;
                }
                case QiupuMessage.MESSAGE_REMOVE_FAVORITES_END: {
                    try {
                        dismissDialog(DIALOG_REMOVE_FORITES);
                    } catch (Exception ne) {
                    }
                    if (msg.getData().getBoolean(RESULT, false) == false) {
                        Toast.makeText(BasicActivity.this, msg.getData().getString(ERROR_MSG), Toast.LENGTH_SHORT).show();
                    } else {
                        showOperationSucToast(true);
                    }
                    break;
                }

                case QiupuMessage.MESSAGE_CHECK_UPDATE_QIUPU_END: {
                    try {
                        dismissDialog(DIALOG_CHECK_QIUPU_VERSION);
                    } catch (Exception ne) {
                    }

                    final String versionFeatures = msg.getData().getString("versionFeatures");
                    final String version = msg.getData().getString("version");
                    final long size = msg.getData().getLong("size");
                    if (msg.getData().getBoolean(RESULT)) {
                        if (msg.getData().getBoolean("haveNewVersion", false)) {
                            showUpdateVersionDialog(false, versionFeatures, version, size);
                        } else {
                            if (!msg.getData().getBoolean(INIT_CHECKED)) {
                                Toast.makeText(BasicActivity.this, getString(R.string.check_back), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        if (msg.getData().getBoolean(FORCE_UPDATE)) {
                            showUpdateVersionDialog(true, versionFeatures, version, size);
                        } else {
                            mIsInitVersionChecked = false;
                        }
                    }
                    break;
                }
                case QiupuMessage.MESSAGE_RETWWET_END: {
                    try {
                        dismissDialog(DIALOG_RETWEET);
                    } catch (Exception ne) {
                    }
                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret == false) {
                        showOperationFailToast(msg.getData().getString(ERROR_MSG), true);
                    }

                    retweetCallback(ret);
                    break;
                }
                case QiupuMessage.MESSAGE_SEND_MAIL_END: {
                    dismissDialog(DIALOG_SEND_EMAIL);
                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret == false) {
                        Toast.makeText(BasicActivity.this, getString(R.string.send_message_failed), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(BasicActivity.this, R.string.send_message_successful, Toast.LENGTH_SHORT).show();
                        onEmailInvited();
                    }

                    break;
                }
                case QiupuMessage.MESSAGE_LIKE_ADD_END: {
                    try {
                        dismissDialog(DIALOG_ADD_LIKE);
                    } catch (Exception ne) {
                    }

                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret == true) {
                        //refresh UI is OK
                        String post_id = msg.getData().getString("post_id");
                        String targetType = msg.getData().getString("post_type");
                        QiupuHelper.onTargetLikeCreated(post_id, targetType);
                    } else {
                        showOperationFailToast(msg.getData().getString(ERROR_MSG), true);
                    }
                    break;
                }
                case QiupuMessage.MESSAGE_LIKE_REMOVE_END: {
                    try {
                        dismissDialog(DIALOG_REMOVE_LIKE);
                    } catch (Exception ne) {
                        ne.printStackTrace();
                    }

                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret == true) {
                        //refresh UI is OK
                        String post_id = msg.getData().getString("post_id");
                        final String targetType = msg.getData().getString("post_type");

                        QiupuHelper.onTargetLikeRemoved(post_id, targetType);
                    } else {
                        showOperationFailToast(msg.getData().getString(ERROR_MSG), true);
                    }
                    break;
                }
                case QiupuMessage.MESSAGE_DELETE_APK_END: {
                    try {
                        dismissDialog(DIALOG_PROCESS_DELETE_APPS);
                    } catch (Exception ne) {
                    }

                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret == true) {
                        String apk_id = msg.getData().getString("post_id");
                        showOperationSucToast(true);
                        deleteServerBackupAppsCallBack(apk_id);
                    } else {
                        showOperationFailToast(msg.getData().getString(ERROR_MSG), true);
                    }
                    break;
                }
                case QiupuMessage.MESSAGE_SET_TOP_END: {
                    try {
                        dismissDialog(DIALOG_SET_CIRCLE_PROCESS);
                    } catch (Exception ne) {
                    }
                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret == true) {
                        String group_id = msg.getData().getString("group_id");
                        String stream_id = msg.getData().getString("stream_id");
                        boolean setTop = msg.getData().getBoolean("set_top");
                        if (setTop == true) {
                            QiupuHelper.onTargetTopCreate(group_id, stream_id);
                        } else {
                            QiupuHelper.onTargetTopCancel(group_id, stream_id);
                        }
                        loadRefresh();
                    } else {
                        showOperationFailToast(msg.getData().getString(ERROR_MSG), true);
                    }
                    break;
                }
                case CREATE_CIRCLE_END: {
                    try {
                        dismissDialog(DIALOG_CREATE_CIRCLE_PROCESS);
                    } catch (Exception ne) {
                    }
                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret == true) {
                        doCircleActionCallBack(false);
                    } else {
                        showOperationFailToast("", true);
                    }
                    break;
                }
                case CIRCLE_SET_END: {
                    try {
                        dismissDialog(DIALOG_SET_CIRCLE_PROCESS);
                    } catch (Exception ne) {
                        Log.d(TAG, "CIRCLE_SET_END " + ne.getMessage());
                    }
                    doActionFriendEndCallBack(msg);
                    break;
                }
                case EXCHANGE_VCARD: {
                    try {
                        dismissDialog(DIALOG_SET_CIRCLE_PROCESS);
                    } catch (Exception ne) {
                        Log.d(TAG, "CIRCLE_SET_END " + ne.getMessage());
                    }

                    boolean succeed = msg.getData().getBoolean(RESULT, false);
                    if (succeed) {
                        long uid = msg.getData().getLong("uid");
                        boolean result = msg.getData().getBoolean(RESULT);
                        doUsersSetCallBack(String.valueOf(uid), result);
                    } else {
                        showLeftActionBtn(false);
                        String errorMsg = msg.getData().getString(ERROR_MSG);
                        showOperationFailToast(errorMsg, true);
                    }
                    break;
                }
                case DELETE_USER: {
                    //show dialog
//            	showDialog(DIALOG_REMOVE_USER, data);
                    showDeleteUserDialog(msg.getData().getString("userid"), msg.getData().getString(IntentUtil.EXTRA_KEY_CIRCLE_ID));
                    break;
                }
                case USER_SET_CIRCLE_END: {
                    try {
                        dismissDialog(DIALOG_SET_USER_PROCESS);
                    } catch (Exception ne) {
                    }
                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret) {
                        boolean isadd = msg.getData().getBoolean("isadd");
                        String uid = msg.getData().getString("uid");
                        doUsersSetCallBack(uid, isadd);
                    } else {
                        String errorMsg = msg.getData().getString(ERROR_MSG);
                        showOperationFailToast(errorMsg, true);
                    }
                    break;
                }
                case REQUEST_PROFIEL_END: {
                    try {
                        dismissDialog(DIALOG_PROFILE_REQUEST_PROCESS);
                    } catch (Exception ne) {
                    }

                    try {
                        dismissDialog(DIALOG_EXCHANGE_PROFILE);
                    } catch (Exception ne) {
                    }

                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret) {
                        long uid = msg.getData().getLong("uid");
                        Toast.makeText(BasicActivity.this, getString(R.string.request_ok), Toast.LENGTH_SHORT).show();
                    } else {
                        showOperationFailToast("", true);
                    }
                    break;
                }
                case UPDATE_USER_INFO_END: {
                    try {
                        dismissDialog(DIALOG_PROFILE_UPDATE_SERVER);
                    } catch (Exception e) {
                    }
                    boolean suc = msg.getData().getBoolean(RESULT);
                    if (suc) {
                        doUpdateUserInfoEndCallBack(suc);
                    } else {
                        String ErrorMsg = msg.getData().getString(ERROR_MSG);
                        if (!isEmpty(ErrorMsg)) {
                            showOperationFailToast(ErrorMsg, true);
                        }
                    }
                    break;
                }
                case DOWNLOAD_APK_END: {
                    String label = msg.getData().getString("label");
                    String packageName = msg.getData().getString("packageName");
                    notify.cancel(packageName);

                    ApkResponse apkResponse = new ApkResponse();
                    apkResponse.packagename = packageName;
                    apkResponse.label = label;
                    if (msg.getData().getBoolean(RESULT)) {
                        notify.notifyFinish(label, null, packageName, apkResponse);
                        // TODO: install the apk then.
                    } else {
                        notify.notifyFail(label, null, packageName, apkResponse);
                    }
                    break;
                }
                default:
            }
        }

    }

    protected void retweetCallback(boolean suc) {
    }

//    protected void deleteFriendApkFromDB(long uid) {
//        orm.deleteApkInfomation(uid);
//    }

    public final void begin() {
        mBasicHandler.sendEmptyMessage(BasicHandler.PROGRESS_LOAD);
    }

    public final void end() {
        mBasicHandler.sendEmptyMessage(BasicHandler.PROGRESS_LOAD_OK);
    }

    public final void failed() {
        mBasicHandler.sendEmptyMessage(BasicHandler.PROGRESS_LOAD_FAILED);
    }

    protected void uiLoadBegin() {
        showProgressBtn(true);
        showLeftActionBtn(false);
        
        if(isUsingActionBar() && getActionBar() != null)
        {
        	setProgress(500);
        }
    }

    protected void uiLoadEnd() {
        showProgressBtn(false);
        showLeftActionBtn(true);
        
        if(isUsingActionBar() && getActionBar() != null)
        {
        	setProgress(10000);
        }
    }

    protected void startCollectPhoneInfo() {
        IntentUtil.shootCollectPhoneInfoIntent(getApplicationContext());
    }

    public void onClick(View view) {
        final int id = view.getId();
        if (id == R.id.head_action_left) {
            loadRefresh();

        } else if (id == R.id.head_action_middle) {
            shareAction();

        } else if (id == R.id.head_action_right) {
            loadSearch();
        } else if(id == R.id.head_title) {
            onPrepareTitleDropDown();
            showCorpusSelectionDialog(mTitle);
        } 
    }

    protected void headRightTextAction() {
        
    }
    
    protected void backtoPreActivity() {
        finish();
    }

    protected void onPrepareTitleDropDown() {
    }

    protected void showCorpusSelectionDialog(View view) {
    }
    
    protected void loadSearch() {
    }

    protected void loadRefresh() {
    }

    protected void deleteApp() {
    }

    protected void deletePeopleInCircle() {
    }

    public void filterInvalidException(TwitterException ne) {
        ne.printStackTrace();

        int error_code = ne.getStatusCode();
        if (error_code == ErrorResponse.SESSION_INVALID) {
            Log.d(TAG, "filterInvalidException, invalid session");
            if (AccountServiceAdapter.isAccountPreloaded(this)) {
                AccountServiceAdapter.reLogin(this,
                        DataConnectionUtils.getCurrentApiHost(this), this);
            } else if (!QiupuConfig.FORCE_BORQS_ACCOUNT_SERVICE_USED) {
                //clear previous invalid session data
                AccountServiceUtils.clearAccount();

                setExpiredSession();
                //session is expire, should clear account
                AccountServiceUtils.borqsLogout(false);

                gotoLogin();
            } else {
                Log.w(TAG, "filterInvalidException, failed without require service.");
            }
        } else {
            failed();
        }
    }

    private void setExpiredSession() {
        try {
            AccountServiceAdapter.setExpiredSession(getApplicationContext());
            AccountServiceAdapter.performLogin(this, DataConnectionUtils.getCurrentApiHost(this), this);
        } catch (Exception ss) {
            Log.w(TAG, "setExpiredSession, failed with exception:" + ss.getMessage());
        }
    }

    protected void post(Runnable runnable) {
        mBasicHandler.post(runnable);
    }

    protected static final int DIALOG_UPDATE_STATUS = 1;
    protected static final int DIALOG_ADD_LIKE = 2;
    protected static final int DIALOG_REMOVE_LIKE = 3;
    protected static final int DIALOG_LOGOUT = 4;
    protected static final int DIALOG_PRAVICY = DIALOG_LOGOUT + 1;
    protected static final int DIALOG_RM_APKFILE = DIALOG_PRAVICY + 1;
    protected static final int DIALOG_SYNC_CONTACTS = DIALOG_RM_APKFILE + 1;
    protected static final int DIALOG_SHARE = DIALOG_SYNC_CONTACTS + 1;
    protected static final int DIALOG_RETWEET = DIALOG_SHARE + 1;
    protected static final int DIALOG_ADD_COMMENTS = DIALOG_RETWEET + 1;
    protected static final int DIALOG_CHECK_QIUPU_VERSION = DIALOG_ADD_COMMENTS + 1;
    protected static final int DIALOG_FEEDBACK = DIALOG_CHECK_QIUPU_VERSION + 1;
    protected static final int DIALOG_ADD_FORITES = DIALOG_FEEDBACK + 1;
    protected static final int DIALOG_REMOVE_FORITES = DIALOG_ADD_FORITES + 1;
    protected static final int DIALOG_PROFILE_UPDATE_SERVER = DIALOG_REMOVE_FORITES + 1;
    protected static final int DIALOG_POST_WALL = DIALOG_PROFILE_UPDATE_SERVER + 1;
    protected static final int EDIT_USER_PROFILE = DIALOG_POST_WALL + 1;
//    protected static final int PICK_PHOTO_ACTION = EDIT_USER_PROFILE + 1;
    protected static final int DIALOG_KEY = EDIT_USER_PROFILE + 1;
    protected static final int DIALOG_REMOVE_POST = DIALOG_KEY + 1;
    protected static final int DIALOG_REMOVE_COMMENT = DIALOG_REMOVE_POST + 1;
    protected static final int SEND_MESSAGE_DIALOG = DIALOG_REMOVE_COMMENT + 1;
//    protected static final int DIALOG_SERCH_APP_CLUE = SEND_MESSAGE_DIALOG + 1;
//    protected static final int DIALOG_DELETE_APPS = SEND_MESSAGE_DIALOG + 1;
    protected static final int DIALOG_PROCESS_DELETE_APPS = SEND_MESSAGE_DIALOG + 1;
    protected static final int SEND_EMAIL_DIALOG = DIALOG_PROCESS_DELETE_APPS + 1;
    protected static final int DIALOG_SEND_EMAIL = SEND_EMAIL_DIALOG + 1;
    protected static final int DIALOG_INVITE_USERINFO = DIALOG_SEND_EMAIL + 1;
    protected static final int DIALOG_TUTORAL_LOGIN = DIALOG_INVITE_USERINFO + 1;
    protected static final int DIALOG_LOGOUT_WITH_NO_TICKET = DIALOG_TUTORAL_LOGIN + 1;
    protected static final int VOICE_DIALOG_LIST = DIALOG_LOGOUT_WITH_NO_TICKET + 1;
    protected static final int DIALOG_CREATE_CIRCLE = VOICE_DIALOG_LIST + 1;
    protected static final int DIALOG_CREATE_CIRCLE_PROCESS = DIALOG_CREATE_CIRCLE + 1;
    protected static final int DIALOG_DELETE_USER_FROM_CIRCLE = DIALOG_CREATE_CIRCLE_PROCESS + 1;
    protected static final int DIALOG_SET_CIRCLE_PROCESS = DIALOG_DELETE_USER_FROM_CIRCLE + 1;
//    protected static final int DIALOG_DELETE_POST = DIALOG_SET_CIRCLE_PROCESS + 1;
    protected static final int DIALOG_DELETE_CIRCLE_PROCESS = DIALOG_SET_CIRCLE_PROCESS + 1;
//    protected static final int DIALOG_REFUSE_USER_PROCESS = DIALOG_DELETE_CIRCLE_PROCESS + 1;
    protected static final int DIALOG_REMOVE_USER = DIALOG_DELETE_CIRCLE_PROCESS + 1;
    protected static final int DIALOG_PROFILE_REQUEST_PROCESS = DIALOG_REMOVE_USER + 1;
    protected static final int DIALOG_SET_USER_PROCESS = DIALOG_PROFILE_REQUEST_PROCESS + 1;
    protected static final int DIALOG_CHANGE_REQUEST = DIALOG_SET_USER_PROCESS + 1;
    private static final int DIALOG_EXCHANGE_PROFILE = DIALOG_CHANGE_REQUEST + 1;
    public static final int PROFILE_ADD_INFO = DIALOG_EXCHANGE_PROFILE + 1;
    protected static final int ADD_INFO_PROCESS = PROFILE_ADD_INFO + 1;


    private Dialog popupLogoutDialog() {
        return new AlertDialog.Builder(BasicActivity.this)
                .setTitle(R.string.confirm_logout)
                .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        logout();
                    }
                })
                .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case SEND_MESSAGE_DIALOG: {
                final String dlgMsg = queryInvitationMessage();
                if (!TextUtils.isEmpty(dlgMsg)) {
                    if (AlertDialog.class.isInstance(dialog)) {
                        ((AlertDialog) dialog).setMessage(dlgMsg);
                    } else {
                        Log.e(TAG, "onPrepareDialog, SEND_MESSAGE_DIALOG is not AlertDialog unexpectedly.");
                    }
                }
            }
            break;
            default:
                super.onPrepareDialog(id, dialog);
                break;
        }
    }
    
    
    protected boolean isWifiActive() {
    	ConnectivityManager connMag = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    	if (connMag != null) {      
            NetworkInfo[] infos = connMag.getAllNetworkInfo();      
            if (infos != null) {      
                for(NetworkInfo ni : infos){  
                    if(ni.getTypeName().equals("WIFI") && ni.isConnected()){  
                        return true;  
                    }  
                }  
            }      
        }      
        return false; 
    }

    protected Dialog onCreateDialog(final int id) {
        switch (id) {
            case DIALOG_LOGOUT: {
                return popupLogoutDialog();
            }
            case DIALOG_TUTORAL_LOGIN: {
                return new AlertDialog.Builder(BasicActivity.this)
                        .setTitle(R.string.tutorial_goto_login)
                        .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                gotoLogin();
                            }
                        })
                        .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .create();
            }
            case DIALOG_LOGOUT_WITH_NO_TICKET: {
                return new AlertDialog.Builder(this).setTitle(
                        R.string.qiupu_exit_title).setMessage(
                        getString(R.string.qiupu_exit_message)).setPositiveButton(
                        getString(R.string.label_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                forceExitProcess();
                            }
                        }).setNegativeButton(getString(R.string.label_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).create();
            }
            case DIALOG_UPDATE_STATUS: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.status_update);
                dialog.setMessage(getString(R.string.status_update_summary));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG_ADD_COMMENTS: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.news_feed_comment);
                dialog.setMessage(getString(R.string.status_update_summary));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG_ADD_LIKE: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.news_feed_like);
                dialog.setMessage(getString(R.string.status_update_summary));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG_REMOVE_LIKE: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.news_feed_unlike);
                dialog.setMessage(getString(R.string.status_update_summary));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG_REMOVE_POST: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.stream_remove);
                dialog.setMessage(getString(R.string.status_update_summary));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG_REMOVE_COMMENT: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.comments_remove);
                dialog.setMessage(getString(R.string.status_update_summary));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case ADD_INFO_PROCESS: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(getString(R.string.add_info_process));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }

            case DIALOG_RETWEET: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.news_feed_reshare);
                dialog.setMessage(getString(R.string.status_update_summary));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG_CHECK_QIUPU_VERSION: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.version_check_qiupu);
                dialog.setMessage(getString(R.string.version_checking));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                return dialog;
            }
            case DIALOG_SEND_EMAIL: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.email_invite_title);
                dialog.setMessage(getString(R.string.email_invite_summary));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG_FEEDBACK: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(getString(R.string.feedback_submission));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG_ADD_FORITES: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.add_favorites);
                dialog.setMessage(getString(R.string.status_update_summary));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG_REMOVE_FORITES: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.remove_favorites);
                dialog.setMessage(getString(R.string.status_update_summary));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG_PROFILE_REQUEST_PROCESS: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(getString(R.string.request_process_title));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG_SET_USER_PROCESS: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(getString(R.string.set_user_process_title));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG_POST_WALL: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.post_to_wall);
                dialog.setMessage(getString(R.string.post_to_wall_summary));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG_SET_CIRCLE_PROCESS: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(getString(R.string.set_friends_circle));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG_PROFILE_UPDATE_SERVER: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(getString(R.string.edit_profile_update_dialog));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            case DIALOG_PROCESS_DELETE_APPS: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(getString(R.string.qiupu_deleting_apks));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
//            case PICK_PHOTO_ACTION: {
//                String[] items = new String[]{getString(R.string.edit_profile_img_camera),
//                        getString(R.string.edit_profile_img_location)};
//                AlertDialog builder = new AlertDialog.Builder(this)
//                        .setItems(items, new DialogInterface.OnClickListener() {
//
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                if (which == 0) {
//                                    dismissDialog(PICK_PHOTO_ACTION);
//                                    doTakePhotoCallback();// from camera
//                                } else {
//                                    dismissDialog(PICK_PHOTO_ACTION);
//                                    doPickPhotoFromGalleryCallback();// from  gallery
//                                }
//                            }
//                        }).create();
//
//                return builder;
//            }

            case DIALOG_CHANGE_REQUEST: {
                View selectview = LayoutInflater.from(this).inflate(R.layout.edit_user_info_dialog, null);
                EditText editcontent = (EditText) selectview.findViewById(R.id.edit_content);
                if (displayType == DISPLAY_PHONE_NUMBER1
                        || displayType == DISPLAY_PHONE_NUMBER2
                        || displayType == DISPLAY_PHONE_NUMBER3) {
                    editcontent.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                editcontent.setText(editTextContent);
                editcontent.setHint(dialoghint);
                editTextCallBack = editcontent;
                AlertDialog selectDialog = new AlertDialog.Builder(this)
                        .setTitle(dialogtilte)
                        .setView(selectview).setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0,
                                                        int arg1) {
                                        removeDialog(DIALOG_CHANGE_REQUEST);
                                        sendChangeRequestCallBack(editTextCallBack);
                                    }
                                }).setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0,
                                                        int arg1) {
                                        removeDialog(DIALOG_CHANGE_REQUEST);
                                    }
                                }).create();
                return selectDialog;
            }

            case SEND_MESSAGE_DIALOG: {
                final String dlgMsg = queryInvitationMessage();
//                if (TextUtils.isEmpty(dlgMsg)) {
//                    Log.d(TAG, "SEND_MESSAGE_DIALOG, ignore without data to send.");
//                    return null;
//                }
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.send_invitation_dialog_title)
                        .setMessage(dlgMsg)
                        .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                sendMessage(contactsPhone, false);
                                sendEmail(contactsEmail, false);
                                dismissDialog(id);
                            }
                        })
                        .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismissDialog(id);
                            }
                        })
                        .create();
            }
//            case DIALOG_DELETE_POST: {
//                return new AlertDialog.Builder(this)
//                        .setTitle(R.string.delete_post_title)
//                        .setMessage(R.string.post_delete_question)
//                        .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                doDeletePostCallBack();
//                            }
//                        })
//                        .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                            }
//                        })
//                        .create();
//            }

            case DIALOG_INVITE_USERINFO: {
                LayoutInflater factory = LayoutInflater.from(this);
                final View textEntryView = factory.inflate(R.layout.invite_user_dialog_view, null);
                final EditText name = (EditText) textEntryView.findViewById(R.id.invite_user_name_edt);
                final EditText typecontent = (EditText) textEntryView.findViewById(R.id.invite_type_edt);
                final TextView typeTitle = (TextView) textEntryView.findViewById(R.id.invite_type_tv);
                if (minviteType == QiupuConfig.INVITE_TYPE_EMAIL) {
                    typeTitle.setText(R.string.invite_dialog_email);
                } else if (minviteType == QiupuConfig.INVITE_TYPE_MESSAGE) {
                    typecontent.setInputType(InputType.TYPE_CLASS_PHONE);
                    typeTitle.setText(R.string.invite_dialog_phone);
                }
                final Button okbtn = (Button) textEntryView.findViewById(R.id.dialog_ok);
                final Button cancelbtn = (Button) textEntryView.findViewById(R.id.dialog_cancel);

                final AlertDialog builder = new AlertDialog.Builder(this)
                        .setTitle(R.string.qiupu_invite)
                        .setView(textEntryView)
                        .create();

                okbtn.setOnClickListener(new OnClickListener() {
                    public void onClick(View arg0) {
                        String nameString = name.getText().toString().trim();
                        String typeString = typecontent.getText().toString().trim();
                        if (nameString.length() <= 0 || typeString.length() <= 0) {
                            Toast.makeText(BasicActivity.this, getString(R.string.invite_dialog_toast), Toast.LENGTH_SHORT).show();
                        } else {
                            removeDialog(DIALOG_INVITE_USERINFO);
//						builder.dismiss();		
                            inviteUserDialogCallBack(nameString, typeString);
                        }
                    }

                });

                cancelbtn.setOnClickListener(new OnClickListener() {
                    public void onClick(View arg0) {
//					builder.dismiss();			
                        removeDialog(DIALOG_INVITE_USERINFO);
                    }
                });

                return builder;
            }
            case SEND_EMAIL_DIALOG: {
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.send_email_dialog_title)
                        .setMessage(R.string.send_email_dialog_message)
                        .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
//	           	 sendEmail(contacts);
                            }
                        })
                        .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .create();
            }
//            case DIALOG_DELETE_APPS: {
//                Builder deleteAppDialog = new AlertDialog.Builder(this);
//                deleteAppDialog.setTitle(R.string.qiupu_delete_apks);
//                if (ApkDetailInfoActivity.class.isInstance(this)) {
//                    deleteAppDialog.setMessage(R.string.qiupu_delete_apks_message_detail);
//                } else {
//                    deleteAppDialog.setMessage(R.string.qiupu_delete_apks_message);
//                }
//                deleteAppDialog.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        if (QiupuConfig.DBLOGD) Log.d(TAG, "selectAppAddress:" + selectAppAddress);
//                        deleteApps(selectAppAddress);
//                    }
//                });
//                deleteAppDialog.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                    }
//                });
//                return deleteAppDialog.create();
//            }

            case VOICE_DIALOG_LIST: {
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.voice_dialog_title)
                        .setItems(speechList, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                doClickVoiceDialogCallBack(speechList[which]);
                                removeDialog(VOICE_DIALOG_LIST);
                            }
                        })
                        .create();
            }
            case PROFILE_ADD_INFO: {
                String[] iteminfo = new String[]{getString(R.string.add_more_item_phone), getString(R.string.add_more_item_email)};
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.add_info_title)
                        .setItems(iteminfo, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                Intent intent = new Intent(BasicActivity.this, AddProfileInfoActivity.class);
                                if (which == 0) {
                                    intent.putExtra(AddProfileInfoActivity.ACTION_TYPE, AddProfileInfoActivity.TYPE_ADD_PHONE);
                                } else if (which == 1) {
                                    intent.putExtra(AddProfileInfoActivity.ACTION_TYPE, AddProfileInfoActivity.TYPE_ADD_EMAIL);
                                }
                                startActivity(intent);
                                removeDialog(PROFILE_ADD_INFO);
                            }
                        })
                        .create();
            }

            case DIALOG_DELETE_USER_FROM_CIRCLE: {

                return new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_user_from_circle_title).setMessage(
                                R.string.delete_user_from_circle_message).setPositiveButton(
                                R.string.label_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                deleteUserFromCircleCallBack();
                            }
                        }).setNegativeButton(R.string.label_cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                }).create();
            }

            case DIALOG_CREATE_CIRCLE_PROCESS: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(getString(R.string.create_circle_process));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }

            case DIALOG_DELETE_CIRCLE_PROCESS: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(getString(R.string.delete_circle_process));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
//            case DIALOG_REFUSE_USER_PROCESS: {
//                ProgressDialog dialog = new ProgressDialog(this);
//                dialog.setMessage(getString(R.string.delete_circle_process));
//                dialog.setCanceledOnTouchOutside(false);
//                dialog.setIndeterminate(true);
//                dialog.setCancelable(true);
//                return dialog;
//            }

            case DIALOG_CREATE_CIRCLE: {
                LayoutInflater factory = LayoutInflater.from(this);
                final View textEntryView = factory.inflate(R.layout.create_circle_dialog, null);
                final EditText textContext = (EditText) textEntryView.findViewById(R.id.new_circle_edt);

                textContext.addTextChangedListener(new ButtonWatcher());

                mAlertDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.new_circle_dialog_title)
                        .setView(textEntryView)
                        .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String textString = textContext.getText().toString().trim();
                                boolean hasCirecle = false;
                                if (textString.length() > 0) {
                                    Cursor cursor = orm.queryAllCircleinfo(getSaveUid());
                                    final int size = null == cursor ? 0 : cursor.getCount();
                                    for (int i = 0; i < size; i++) {
                                        cursor.moveToPosition(i);
                                        UserCircle tmpCircle = QiupuORM.createCircleInformation(cursor);
                                        if (tmpCircle != null && tmpCircle.name != null && tmpCircle.name.equals(textString)) {
                                            hasCirecle = true;
                                            Toast.makeText(BasicActivity.this, getString(R.string.circle_exists), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    }

                                    if (null != cursor) {
                                        cursor.close();
                                    }

                                    if (!hasCirecle) {
                                        createCircle(textString);
                                        mAlertDialog.dismiss();
                                    }
                                } else {
                                    Toast.makeText(BasicActivity.this, getString(R.string.input_content), Toast.LENGTH_SHORT).show();
                                }

                            }
                        })
                        .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create();

                return mAlertDialog;
            }

        }
        return super.onCreateDialog(id);
    }

    protected Dialog onCreateDialog(int id, final Bundle data) {
        switch (id) {
            case DIALOG_EXCHANGE_PROFILE: {
                final EditText message = new EditText(this);
                message.setPadding(5, 5, 5, 5);
                message.setMinLines(3);
                message.setHint(R.string.feedback_hint);
                message.setBackgroundResource(R.drawable.editbox_background);

                return new AlertDialog.Builder(this)
                        .setTitle(R.string.menu_exchange_profile)
                        .setView(message)
                                //.setMessage(getString(R.string.menu_exchange_profile_desc))
                        .setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String text = message.getText().toString();
                                AsyncApiUtils.sendApproveRequest(data.getLong("uid"), text, asyncQiupu,
                                        new AsyncApiUtils.AsyncApiSendRequestCallBackListener() {
                                            @Override
                                            public void sendRequestCallBackBegin() {
                                                showDialog(DIALOG_PROFILE_REQUEST_PROCESS);
                                            }

                                            @Override
                                            public void sendRequestCallBackEnd(boolean result, final long uid) {
                                                Message msg = mBasicHandler.obtainMessage(BasicHandler.REQUEST_PROFIEL_END);
                                                msg.getData().putBoolean(RESULT, result);
                                                msg.getData().putLong("uid", uid);
                                                msg.sendToTarget();
                                            }
                                        });
//                                sendApproveRequest(String.valueOf(data.getLong("uid")), text);
                            }
                        })
                        .setNegativeButton(getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).create();
            }
            case DIALOG_REMOVE_USER: {
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_user_title)
                        .setMessage(getString(R.string.delete_user_from_privacy_circle))
                        .setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                circleUpdate(data.getString("uid"), data.getString(IntentUtil.EXTRA_KEY_CIRCLE_ID), false);
                                removeDialog(DIALOG_REMOVE_USER);
                            }
                        })
                        .setNegativeButton(getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                removeDialog(DIALOG_REMOVE_USER);
                            }
                        }).create();
            }
        }
        return super.onCreateDialog(id, data);
    }

    protected Animation inFromRightAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(500);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    protected Animation outToLeftAnimation() {
        Animation outtoLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoLeft.setDuration(500);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
        return outtoLeft;
    }

    protected Animation inFromLeftAnimation() {
        Animation inFromLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromLeft.setDuration(500);
        inFromLeft.setInterpolator(new AccelerateInterpolator());
        return inFromLeft;
    }

    protected Animation outToRightAnimation() {
        Animation outtoRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoRight.setDuration(500);
        outtoRight.setInterpolator(new AccelerateInterpolator());
        return outtoRight;
    }

    public void startApk(ComponentName componentname) {
        if (QiupuConfig.LOGD) Log.d(TAG, "startActivitySafely componentname:" + componentname);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(componentname);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        try {
            startActivity(intent);
        } catch (Exception e) {

        }
    }

    public static void uninstallApk(Context context, String packagename) {
        Uri packageURI = Uri.parse("package:" + packagename);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(uninstallIntent);
    }

    protected boolean checkApkIsInstalled(String packagename) {
        if (QiupuConfig.LOGD) Log.d(TAG, "packagename :" + packagename);
        final PackageManager packageManager = getPackageManager();
        String className = null;
        try {
            ActivityInfo[] ainfos = packageManager.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES).activities;
            if (QiupuConfig.LOGD) {
                Log.d(TAG, "APk  ainfos :" + ainfos);
            }
            if (ainfos != null && ainfos.length > 0) {
                File sdcardDir = Environment.getExternalStorageDirectory();
                String path = "";
                if (sdcardDir != null) {
                    path = sdcardDir.getPath();
                }

                final String srcDir = ainfos[0].applicationInfo.sourceDir;
                if (QiupuConfig.LOGD) {
                    Log.d(TAG, "APk  src Dir :" + srcDir);
                }
                //only check the three path.
                if (srcDir != null && (srcDir.contains(path + "/app") || srcDir.contains("/data/app") || srcDir.contains("/mnt/asec/"))) {
                    className = ainfos[0].name;
                    Log.d(TAG, "checkApkIsInstalled  classname:" + className);
                    return true;
                }
            }
        } catch (NameNotFoundException e) {
            if (QiupuConfig.LOGD) Log.d(TAG, "NameNotFoundException :" + packagename);
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    //	protected void editUserProfileCallBack(EditText tmpContent)
//	{
//	}
    protected void sendChangeRequestCallBack(EditText tmpContent) {
    }

    protected Dialog showRMConfirmDialog(final ApkResponse apk) {
        return new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_apkfile)
                .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ApkFileManager.rmApkFile(apk, apk.packagename);
//    				apk.status = ApkResponse.APKStatus.STATUS_NEED_DOWNLOAD;
                        deleteAppCallback();
                    }
                })
                .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
    }

    protected void deleteAppCallback() {
        //cancel notification
    }

    protected void loadScreenShotFromServer(ImageView imagev, String url) {
        if (StringUtil.isValidString(url)) {
            ImageRun imagerun = new ImageRun(mHandler, url, 0);
            imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_APK;
            imagerun.noimage = true;
            imagerun.addHostAndPath = true;
            imagerun.forappprofile = true;
            imagerun.need_scale = true;
            imagerun.width = 120;
            boolean ret = imagerun.setScreenImageView(imagev);
            if (ret == false) {
                imagerun.post(null);
            }
        }
    }

    protected ArrayList<String> getPhoneIstallApps() {
        ArrayList<String> applist = new ArrayList<String>();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> packageList = getPackageManager().queryIntentActivities(mainIntent, 0);
        HashSet<String> set = new HashSet<String>();

        if (packageList != null) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            String path = "";
            if (sdcardDir != null) {
                path = sdcardDir.getPath();
                Log.d(TAG, "sdcard path=" + path);
            }
            for (int i = 0; i < packageList.size(); i++) {
                ResolveInfo resolveinfo = packageList.get(i);
                String packageName = resolveinfo.activityInfo.packageName;
                Log.d(TAG, "sourceDir=" + resolveinfo.activityInfo.applicationInfo.sourceDir);
                if (resolveinfo.activityInfo.applicationInfo.sourceDir.contains(path + "/app") ||
                        resolveinfo.activityInfo.applicationInfo.sourceDir.contains("/data/app") ||
                        resolveinfo.activityInfo.applicationInfo.sourceDir.contains("/mnt/asec/")) {
                    if (!set.contains(packageName)) {
                        String packagename = resolveinfo.activityInfo.packageName;
                        applist.add(packagename);
                        set.add(packageName);
                    }
                }
            }
            set.clear();
            set = null;
        }

        return applist;
    }


    //must override the following two functions
    //to process the end message
    protected void doActionFriendEndCallBack(Message msg) {
    }

    protected void doGetFriendsEndCallBack(Message msg) {
    }

    //to update data source
    protected void finishActionFriendsCallBack(final QiupuUser user) {
    	if(user == null || user.circleId == null) {
    		return ;
    	}
        updateCircleCount(user);// go to update circle count first
        if (user.circleId.length() <= 0) {
            orm.deleteFriendsinfo(user.uid);
            orm.clearAllUserCircles(user.uid);
            orm.updateLookupPeopleType(user.uid, false, user.contactId);
        } else//mean added user into my circle
        {
//			user.circleName = parseUserCircleNameToDB(user.circleId, circleName);
//			updateCircleCount(user);

            orm.insertUserinfo(user); // add user into db
            //update usercircle
            orm.clearUserCirclesWithOutPublicCircles(user.uid);
            orm.updateUserCircles(getCirlceOrUserIds(user.circleId, true), user.uid);
            orm.updateLookupPeopleType(user.uid, true, user.contactId);
        }
    }

    private void updateCircleCount(QiupuUser user) {
        ArrayList<Long> tmpids = new ArrayList<Long>();
        ArrayList<Long> newids = getCirlceOrUserIds(user.circleId, true);
        if(orm.isExistingUser(user.uid)) {
            final String curCircleId = orm.queryOneUserCircleIds(user.uid);
            ArrayList<Long> oldids = getCirlceOrUserIds(curCircleId, true);
            tmpids.addAll(oldids);
            
            oldids.removeAll(newids);
            newids.removeAll(tmpids);
            UserCircle tmpcircle;

            // need reduce
            for (int i = 0; i < oldids.size(); i++) {
                tmpcircle = orm.queryOneCircle(AccountServiceUtils.getBorqsAccountID(), oldids.get(i));
                if (tmpcircle != null) {
                    int count = tmpcircle.memberCount - 1;
                    tmpcircle.memberCount = count > 0 ? count : 0;
                    orm.updateCircleInfo(tmpcircle);
                }
            }
            
         // need add 
            for (int i = 0; i < newids.size(); i++) {
                tmpcircle = orm.queryOneCircle(AccountServiceUtils.getBorqsAccountID(), newids.get(i));
                if (tmpcircle != null) {
                    tmpcircle.memberCount = tmpcircle.memberCount + 1;
                    orm.updateCircleInfo(tmpcircle);
                }
            }
        }else {
         // need add 
            for (int i = 0; i < newids.size(); i++) {
                UserCircle tmpcircle = orm.queryOneCircle(AccountServiceUtils.getBorqsAccountID(), newids.get(i));
                if (tmpcircle != null) {
                    tmpcircle.memberCount = tmpcircle.memberCount + 1;
                    orm.updateCircleInfo(tmpcircle);
                }
            }
        }
    }

    public void followUser(QiupuUser user) {
        if (user != null) {
            Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_ADD_FRIEND);
            msg.getData().putLong(BpcApiUtils.User.USER_ID, user.uid);
            msg.sendToTarget();
        }
    }

    public void unFollowerUser(QiupuUser user) {
        if (user != null) {
            Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_DELETE_FRIEND);
            msg.getData().putLong(BpcApiUtils.User.USER_ID, user.uid);
            msg.sendToTarget();
        }
    }

    public void addFavorites(ApkResponse apk) {
        Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_ADD_FAVORITES);
        msg.getData().putSerializable(QiupuMessage.BUNDLE_APKINFO, apk);
        msg.sendToTarget();
    }

    public void removeFavorites(ApkResponse apk) {
        Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_REMOVE_FAVORITES);
        msg.getData().putSerializable(QiupuMessage.BUNDLE_APKINFO, apk);
        msg.sendToTarget();
    }

    protected void doRemoveFavoriteCallBack(String apkid) {
    }

    protected void doAddFavoriteCallBack(String apkid) {
    }

    public void doRemoveFavorite(final ApkResponse apk) {
        asyncQiupu.postRemoveFavorite(getSavedTicket(), String.valueOf(apk.apk_server_id), new TwitterAdapter() {
            public void postRemoveFavorite(boolean suc) {
                Log.d(TAG, "finish postRemoveFavorite=" + suc);

                doRemoveFavoriteCallBack(apk.apk_server_id);
                Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_REMOVE_FAVORITES_END);
                msg.getData().putBoolean(RESULT, true);
                msg.getData().putString("apk_server_id", apk.apk_server_id);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                Log.d(TAG, "fail remove favor the apk=" + ex.getMessage());

                Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_REMOVE_FAVORITES_END);
                msg.getData().putBoolean(RESULT, false);
                msg.getData().putString("apk_server_id", apk.apk_server_id);
                msg.getData().putString(ERROR_MSG, ex.getMessage());
                msg.sendToTarget();
            }
        });
    }

    public void doAddFavorite(final ApkResponse apk) {
        asyncQiupu.postAddFavorite(getSavedTicket(), String.valueOf(apk.apk_server_id), new TwitterAdapter() {
            public void postAddFavorite(boolean suc) {
                Log.d(TAG, "finish postAddFavorite=" + suc);

                doAddFavoriteCallBack(apk.apk_server_id);
                Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_ADD_FAVORITES_END);
                msg.getData().putBoolean(RESULT, suc);
                msg.getData().putString("apk_server_id", apk.apk_server_id);
                if (suc == false) {
                    msg.getData().putString(ERROR_MSG, getString(R.string.already_in_favorite));
                }
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                Log.d(TAG, "fail add favor the apk=" + ex.getMessage());

                Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_ADD_FAVORITES_END);
                msg.getData().putBoolean(RESULT, false);
                msg.getData().putString("apk_server_id", apk.apk_server_id);
                msg.getData().putString(ERROR_MSG, ex.getMessage());
                msg.sendToTarget();
            }
        });
    }

    protected KeyEvent lastUpKey = null;
    protected int lastDownKeyCode;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        lastDownKeyCode = event.getKeyCode();
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if(hideSearhView()) {
        		return true;
        	}
            if ((fromtab && fromHome) || supportLeftNavigation) {
                if (lastUpKey != null && lastUpKey.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    long span = event.getEventTime() - lastUpKey.getEventTime();
                    if (span < 5 * QiupuConfig.A_SECOND) {
                        if (preEscapeActivity()) {
                            return super.onKeyUp(keyCode, event);
                        } else {
                            return true;
                        }
                    } else {
                        lastUpKey = event;
                        tryShowMoreBackKeyClick();
                        return true;
                    }
                } else {
                    lastUpKey = event;
                    tryShowMoreBackKeyClick();
                    return true;
                }
            } else if (lastDownKeyCode != KeyEvent.KEYCODE_BACK) {
                return super.onKeyUp(keyCode, event);
            } else if (!preEscapeActivity()) {
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
//        	Log.d(TAG, "onkeyup: click search ");
//        	showSearhView();
//            loadSearch();
        }

        lastUpKey = event;
        return super.onKeyUp(keyCode, event);
    }

    protected void showSearhView() {
    	showSearhViewWithHint("");
    }
    
    protected void showSearhViewWithHint(String hint) {
    	View searchStub = ((ViewStub) getWindow().getDecorView().findViewById(R.id.search_stub));
    	if(searchStub != null) {
    		searchStub.setVisibility(View.VISIBLE);
    		SearchView view = (SearchView) getWindow().getDecorView().findViewById(R.id.panel_import);
    		if(view != null) {
    			view.setVisibility(View.VISIBLE);
    			view.setOnQueryTextListener(this);
    			view.setHint(hint);
    		}
    	}else {
    		SearchView view = (SearchView) getWindow().getDecorView().findViewById(R.id.panel_import);
    		if(view != null) {
    			view.setVisibility(View.VISIBLE);
    			view.onSearchClicked();
    			view.setOnQueryTextListener(this);
    			view.setHint(hint);
    		}else {
    			Log.d(TAG, "search stub is null");
    		}
    	}
    }
    
    protected boolean hideSearhView() {
    	boolean flag = false;
//    	View searchStub = ((ViewStub) getWindow().getDecorView().findViewById(R.id.search_stub));
//    	if(searchStub != null) {
//    		searchStub.setVisibility(View.GONE);
//    		flag = true;
//    	}else {
//    		Log.d(TAG, "search stub is null");
//    	}
    	
    	SearchView view = (SearchView) getWindow().getDecorView().findViewById(R.id.panel_import);
    	if(view != null && view.getVisibility() == View.VISIBLE) {
    		flag = true;
    		view.setVisibility(View.GONE);
    		view.clearText();
    	}
    	return flag;
    }
    
    private void tryShowMoreBackKeyClick() {
        if (KeyEvent.KEYCODE_BACK == lastDownKeyCode) {
            Toast.makeText(this, R.string.string_click_onemore_exit, Toast.LENGTH_SHORT).show();
        }
    }

    protected void doSerialization() {

    }

    private void doExit() {
        showDialog(DIALOG_LOGOUT_WITH_NO_TICKET);
    }

//	public void reShowDialog(int displaydialog){
//		Toast.makeText(this, getString(R.string.edit_profile_input_null), Toast.LENGTH_SHORT).show();
//		swithDialog(displaydialog);
//	}

    private String dialoghint;
    private String dialogtilte;
//	public void swithDialog(int witch){
//		displayType = witch;
//		if(QiupuConfig.LOGD)Log.d(TAG, "swithDialog displayType" +displayType);
//		if(displayType == DISPLAY_ADDRESS)
//		{
//			dialogtilte = getString(R.string.edit_profile_address);
//			dialoghint = getString(R.string.edit_profile_address_hint);
//			showDialog(EDIT_USER_PROFILE);
//		}
//		else if(displayType == DISPLAY_COMPANY)
//		{
//			dialogtilte = getString(R.string.edit_profile_company);
//			dialoghint = getString(R.string.edit_profile_company_hint);
//			showDialog(EDIT_USER_PROFILE);
//		}
//		else if(displayType == DISPLAY_NICK_NAME)
//		{
//			dialogtilte = getString(R.string.edit_profile_nick_name);
//			dialoghint = getString(R.string.edit_profile_nick_name_hint);
//			showDialog(EDIT_USER_PROFILE);
//		}
//	}


    public void switchChangeRequest(int witch) {
        displayType = witch;
        if (displayType == DISPLAY_PHONE_NUMBER1
                || displayType == DISPLAY_PHONE_NUMBER2
                || displayType == DISPLAY_PHONE_NUMBER3) {
            dialogtilte = getString(R.string.change_request_phone);
            dialoghint = getString(R.string.edit_profile_phone_number_hint);
            showDialog(DIALOG_CHANGE_REQUEST);
        } else if (displayType == DISPLAY_EMAIL1
                || displayType == DISPLAY_EMAIL2
                || displayType == DISPLAY_EMAIL3) {
            dialogtilte = getString(R.string.change_request_email);
            dialoghint = getString(R.string.edit_profile_emmail_hint);
            showDialog(DIALOG_CHANGE_REQUEST);
        }
    }

    public static boolean canDownload(ApkResponse apk) {
        String esStatus = Environment.getExternalStorageState();
        if (esStatus.equals(Environment.MEDIA_MOUNTED) == false) {
            Log.d(TAG, "canDownload, not mounted SDcard.");
            return false;
        }

        if (QiupuHelper.hasEnoughspace(apk.apksize) == false) {
            Log.d(TAG, "canDownload, not enough SDcard space.");
            return false;
        }

        return true;
    }

    protected void sendMessage(ArrayList<ContactSimpleInfo> users, boolean exchange_vcard) {
        if (null == users || users.size() <= 0) {
            Log.d(TAG, "sendMessage, ignore empty phone user list.");
            return;
        }

        //the SIM card must be ready ,then can send the message.
        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        int simState = mTelephonyManager.getSimState();
        if (simState == TelephonyManager.SIM_STATE_ABSENT) {
            Toast.makeText(this, R.string.sim_state_absent, Toast.LENGTH_SHORT).show();
            return;
        }

        SmsManager smsMgr = SmsManager.getDefault();
        PendingIntent dummyEvent = PendingIntent.getBroadcast(this, 0, new Intent(), 0);
//        try {
            for (ContactSimpleInfo info : users) {
                String userinfo = info.phone_number + "/" + info.display_name_primary + "/" + getSaveUid();
                if (QiupuConfig.LOGD) Log.d(TAG, "userinfo : " + userinfo);
//                final BorqsAccount account = AccountServiceUtils.getBorqsAccount();

                String standard = "";
                if (exchange_vcard) {
                    standard = getString(R.string.qiupu_invite_message_exchange_vcard_content);
                } else {
                    standard = getString(R.string.qiupu_invite_message_content);
                }

                String str = String.format(standard, getUserNickname(), getString(R.string.app_name),
                            SimpleCrypt.encode(userinfo), ConfigurationBase.getAPIURL(), "");

                if (QiupuConfig.LOGD) Log.d(TAG, "str : " + str);
                try {
                    if (str.length() > 70) {
                        ArrayList<String> msgs = smsMgr.divideMessage(str);
                        if (QiupuConfig.LOGD) Log.d(TAG, "msgs :" + msgs);
                        smsMgr.sendMultipartTextMessage(info.phone_number, null, msgs, null, null);
                    } else {
                        if (QiupuConfig.LOGD) Log.d(TAG, "str.length :" + str.length());
                        smsMgr.sendTextMessage(info.phone_number, null, str, dummyEvent, null);
                    }
                } catch (Exception e) {
                    Log.e("SmsSending", "SendException msg="+e.getMessage(), e);
                }
            }
            Toast.makeText(BasicActivity.this, R.string.send_message_successful, Toast.LENGTH_SHORT).show();
            onMessageInvited();
//        } catch (Exception e) {
//            Log.e("SmsSending", "SendException msg="+e.getMessage(), e);
//        }
    }

    protected void sendEmail(ArrayList<ContactSimpleInfo> users, boolean exchange_vcard) {
        if (null == users || users.size() <= 0) {
            Log.d(TAG, "sendEmail, ignore empty email user list.");
            return;
        }

        StringBuilder email = new StringBuilder();
        StringBuilder name = new StringBuilder();
        int size = users.size();
        for (int i = 0; i < size; i++) {
            ContactSimpleInfo tmpSimpleInfo = users.get(i);
            email.append(tmpSimpleInfo.email);
            if (i != size - 1) email.append(",");
            name.append(tmpSimpleInfo.display_name_primary);
            if (i != size - 1) name.append(",");

        }
        inviteWithEmail("", email.toString(), name.toString(), exchange_vcard);
    }

    private String getContactNameByPrimaryColumn(String number, String primaryColumn) {
        if (number == null || number.length() <= 0) {
            return null;
        }

        // Context context = getContext();
        Cursor cursor = null;
        String cn = "";

        try {
            cursor = getContentResolver()
                    .query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                            Uri.encode(number)),
                            new String[]{primaryColumn,
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                            null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                cn = cursor
                        .getString(cursor
                                .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return cn;
    }

    public String getContactNameByPhoneNumber(String number) {
        return getContactNameByPrimaryColumn(number, ContactsContract.CommonDataKinds.Phone._ID);
        /*if (number == null || number.length() <= 0) {
            return null;
        }

        // Context context = getContext();
        Cursor  cursor  = null;
        String cn  = "";
        
		try {
			cursor = getContentResolver()
					.query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
							Uri.encode(number)),
							new String[] {
									ContactsContract.CommonDataKinds.Phone._ID,
									ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME },
							null, null, null);
			if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
				cn = cursor
						.getString(cursor
								.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			}
		} catch(Exception e) {
        } finally {
        	if (cursor != null) {
                cursor.close(); 
        	}
        }
        return cn;*/
    }

    public void pickRecipients() {
        Intent intent = new Intent("oms.android.intent.action.MULTIPLE_PICK",
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        String[] projection = new String[]{
                Email.CONTENT_ITEM_TYPE,
                Phone.CONTENT_ITEM_TYPE};
        intent.putExtra("projection", projection);
        startActivityForResult(intent, PICK_CONTACT);
    }

    boolean iaminliking;
    final Object mLockLike = new Object();

    private void onLikeCreated(final String post_id, final String type, boolean suc) {
        Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_LIKE_ADD_END);
        msg.getData().putBoolean(RESULT, suc);
        msg.getData().putString("post_id", post_id);
        msg.getData().putString("post_type", type);
        msg.sendToTarget();

        synchronized (mLockLike) {
            iaminliking = false;
        }
    }

    protected void onLikeRemoved(final String post_id, final String type, final boolean suc) {
        synchronized (mLockLike) {
            iaminliking = false;
        }

        Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_LIKE_REMOVE_END);
        msg.getData().putBoolean(RESULT, suc);
        msg.getData().putString("post_id", post_id);
        msg.getData().putString("post_type", type);
        msg.sendToTarget();
    }

    public boolean likePost(final String post_id, final String apk_id) {
        if (StringUtil.isValidString(apk_id)) {
            return createLike(apk_id, QiupuConfig.TYPE_APK);
        } else {
            return createLike(post_id, QiupuConfig.TYPE_STREAM);
        }
    }

    protected boolean createLike(final String targetId, final String type) {
        TwitterAdapter adapter = new TwitterAdapter() {
            public void postLike(boolean suc) {
                Log.d(TAG, "finish createLike targetId = " + targetId + ", suc = " + suc);
                onLikeCreated(targetId, type, suc);
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                onLikeCreated(targetId, type, false);
            }
        };

        return createLike(targetId, type, adapter);
    }

    private boolean createLike(final String targetId, final String type, TwitterAdapter adapter) {
        if (iaminliking == true) {
            showShortToast(R.string.string_in_processing);
            return false;
        }

        synchronized (mLockLike) {
            iaminliking = true;
        }

        showDialog(DIALOG_ADD_LIKE);

        asyncQiupu.postLike(getSavedTicket(), targetId, type, adapter);
        return false;
    }

    public boolean unLikePost(final String post_id, final String apk_id) {
        if (StringUtil.isValidString(apk_id)) {
            return removeLike(apk_id, QiupuConfig.TYPE_APK);
        } else {
            return removeLike(post_id, QiupuConfig.TYPE_STREAM);
        }

    }

    protected boolean removeLike(final String post_id, final String type) {
        TwitterAdapter adapter = new TwitterAdapter() {
            public void postUnLike(boolean suc) {
                Log.d(TAG, "finish postLike=" + suc);
                onLikeRemoved(post_id, type, suc);
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                onLikeRemoved(post_id, type, false);
            }
        };

        return removeLike(post_id, type, adapter);
    }

    private boolean removeLike(final String post_id, String type, TwitterAdapter adapter) {
        if (iaminliking == true) {
            showShortToast(R.string.string_in_processing);
            return false;
        }

        synchronized (mLockLike) {
            iaminliking = true;
        }
        showDialog(DIALOG_REMOVE_LIKE);

        asyncQiupu.postUnLike(getSavedTicket(), post_id, type, adapter);
        return false;
    }

    protected void likeComment(final Stream.Comments.Stream_Post comment) {
        final String targetId = String.valueOf(comment.id);
        TwitterAdapter adapter = new TwitterAdapter() {
            public void postLike(boolean suc) {
                Log.d(TAG, "createLike, targetId = " + targetId + ", suc = " + suc);

//                comment.iLike = true;
//                comment.like_count += 1;
//
//                if (null != comment.likerList.friends) {
//                    QiupuSimpleUser user = new QiupuSimpleUser();
//                    BorqsAccount account = AccountServiceUtils.getBorqsAccount();
//                    user.uid = account.uid;
//                    user.nick_name = account.nickname;
//                    user.profile_image_url = "";
//                    comment.likerList.friends.add(user);
//                }

                onLikeCreated(targetId, QiupuConfig.TYPE_COMMENT, suc);
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                onLikeCreated(targetId, QiupuConfig.TYPE_COMMENT, false);
            }
        };

        createLike(targetId, QiupuConfig.TYPE_COMMENT, adapter);
    }

    protected void unLikeComment(final Stream.Comments.Stream_Post comment) {
        final String targetId = String.valueOf(comment.id);
        TwitterAdapter adapter = new TwitterAdapter() {
            public void postUnLike(boolean suc) {
                Log.d(TAG, "unLikeComment, targetId = " + targetId + ",  suc =" + suc);

//                comment.iLike = false;
//                comment.like_count -= 1;
//                final int likerCount = null == comment.likerList.friends ? 0 : comment.likerList.friends.size();
//                for (int i = 0; i < likerCount; ++i) {
//                    QiupuSimpleUser user = comment.likerList.friends.get(i);
//                    if (getSaveUid() == user.uid) {
//                        comment.likerList.friends.remove(user);
//                        break;
//                    }
//                }

                onLikeRemoved(targetId, QiupuConfig.TYPE_COMMENT, suc);
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                onLikeRemoved(targetId, QiupuConfig.TYPE_COMMENT, false);
            }
        };

        removeLike(targetId, QiupuConfig.TYPE_COMMENT, adapter);
    }

    boolean mIsSetTopList = false;
    Object mLockTopList = new Object();

    private boolean setTopList(final String group_id, final String stream_id, final boolean setTop) {
        Log.d(TAG, "setTopList() setTop = " + setTop);
        if (mIsSetTopList == true) {
            Toast.makeText(this, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return false;
        }

        synchronized (mLockTopList) {
            mIsSetTopList = true;
        }

        showDialog(DIALOG_SET_CIRCLE_PROCESS);

        asyncQiupu.setTopList(getSavedTicket(), group_id, stream_id, setTop, new TwitterAdapter() {
            public void setTopList(ArrayList<String> topIdList) {
                Log.d(TAG, "finish setTopList() topIdList = " + topIdList);

//                boolean result = false;
//                if (topIdList != null && topIdList.contains(group_id)) {
//                    result = true;
//                }
                synchronized (mLockTopList) {
                    mIsSetTopList = false;
                }

                Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_SET_TOP_END);
                msg.getData().putBoolean(RESULT, true);

                msg.getData().putString("stream_id", stream_id);
                msg.getData().putString("group_id", group_id);
                msg.getData().putBoolean("set_top", setTop);

                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockTopList) {
                    mIsSetTopList = false;
                }
                Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_SET_TOP_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
        return false;
    }

    boolean iaminRetweeting = false;
    Object mLockRetweet = new Object();

    public boolean retweet(final String post_id, final String tos, final String addedContent, boolean canComment,
                           boolean canLike, final boolean canShare, boolean privacy) {
        if (iaminRetweeting == true) {
            Toast.makeText(this, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return false;
        }

        synchronized (mLockRetweet) {
            iaminRetweeting = true;
        }

        showDialog(DIALOG_RETWEET);

        asyncQiupu.postRetweet(getSavedTicket(), post_id, tos, addedContent, canComment, canLike,
                canShare, privacy, new TwitterAdapter() {
            public void postRetweet(Stream retweet) {
                Log.d(TAG, "finish post retweet.isPrivacy()=" + retweet.isPrivacy());

                synchronized (mLockRetweet) {
                    iaminRetweeting = false;
                }
                Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_RETWWET_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockRetweet) {
                    iaminRetweeting = false;
                }
                Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_RETWWET_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
        return false;
    }

    // invite contact for friends with email
    private boolean inviteWithEmail(final String phoneNumber, final String email, final String name, final boolean exchange_vcard) {

        showDialog(DIALOG_SEND_EMAIL);

        asyncQiupu.inviteWithMail(getSavedTicket(), phoneNumber, email, name, null, exchange_vcard, new TwitterAdapter() {
            public void inviteWithMail(boolean result) {
                Log.d(TAG, "finish inviteWithEmail=" + result);

                synchronized (mLockRetweet) {
                    iaminRetweeting = false;
                }
                Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_SEND_MAIL_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockRetweet) {
                    iaminRetweeting = false;
                }
                Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_SEND_MAIL_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
        return false;
    }

    protected void deleteServerBackupAppsCallBack(String apkid) {
    }

    protected String getIntentURL(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            String url = intent.getData().toString();
            url = fixUrl(url);
            return url;
        }

        return "";
    }

    /* package */
    static String fixUrl(String inUrl) {
        if (inUrl.startsWith("http://") || inUrl.startsWith("https://"))
            return inUrl;
        if (inUrl.startsWith("http:") ||
                inUrl.startsWith("https:")) {
            if (inUrl.startsWith("http:/") || inUrl.startsWith("https:/")) {
                inUrl = inUrl.replaceFirst("/", "//");
            } else inUrl = inUrl.replaceFirst(":", "://");
        }
        return inUrl;
    }

    protected boolean updateAll() {
        return false;
    }

    protected boolean backupAll() {
        return false;
    }

    protected void setInviteType(int type) {
        minviteType = type;
    }
    
    protected void showSetCircleProcessDialog() {
        showDialog(DIALOG_SET_CIRCLE_PROCESS);
    }
    
    boolean inSetCircle;
    Object mLocksetCircle = new Object();

    public void setCircle(final long uid, final String circleid, final String circleName) {
        if (inSetCircle) {
            Toast.makeText(this, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLocksetCircle) {
            inSetCircle = true;
        }
        showSetCircleProcessDialog();

        asyncQiupu.setCircle(getSavedTicket(), uid, circleid, new TwitterAdapter() {
            public void setCircle(QiupuUser resultUser) {
                Log.d(TAG, "finish setCircle=" + resultUser);

                if (resultUser != null) {
                    finishActionFriendsCallBack(resultUser);
                }

                Message msg = mBasicHandler.obtainMessage(BasicHandler.CIRCLE_SET_END);
                msg.getData().putBoolean(RESULT, true);
                msg.getData().putLong("uid", uid);
                msg.sendToTarget();


                synchronized (mLocksetCircle) {
                    inSetCircle = false;
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLocksetCircle) {
                    inSetCircle = false;
                }
                Message msg = mBasicHandler.obtainMessage(BasicHandler.CIRCLE_SET_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }

    boolean inExchangeVcard;
    Object mLockexchangeVcard = new Object();

    public void exchangeVcard(final long uid, final boolean send_request, final String circleid, final String circleName) {
        if (inExchangeVcard) {
            Toast.makeText(this, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLockexchangeVcard) {
            inExchangeVcard = true;
        }
        showSetCircleProcessDialog();

        asyncQiupu.exchangeVcard(getSavedTicket(), uid, send_request, circleid, new TwitterAdapter() {
            public void exchangeVcard(QiupuUser resultUser) {
                Log.d(TAG, "finish exchangeVcard=" + resultUser);

                if (resultUser != null) {
                    finishActionFriendsCallBack(resultUser);
                }

                Message msg = mBasicHandler.obtainMessage(BasicHandler.EXCHANGE_VCARD);
                msg.getData().putBoolean(RESULT, true);
                msg.getData().putLong("uid", uid);
                msg.sendToTarget();


                synchronized (mLockexchangeVcard) {
                    inExchangeVcard = false;
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                Log.d(TAG, "=============== ex.getMessage() = " + ex.getMessage());
                synchronized (mLockexchangeVcard) {
                    inExchangeVcard = false;
                }

                Message msg = mBasicHandler.obtainMessage(BasicHandler.EXCHANGE_VCARD);
                msg.getData().putBoolean(RESULT, false);
                msg.getData().putString(ERROR_MSG, ex.getMessage());
                msg.sendToTarget();
            }
        });
    }

    protected void inviteUserDialogCallBack(String nameString, String typeString) {
    }

    boolean inloadRecommendcategory = false;
    Object mLoadRecommendcategory = new Object();

    public void loadRecommendCategory() {
        synchronized (mLoadRecommendcategory) {
            if (inloadRecommendcategory == true) {
                Log.d(TAG, "in doing get inloadRecommendcategory data");
                return;
            }
        }

        synchronized (mLoadRecommendcategory) {
            inloadRecommendcategory = true;
        }

        asyncQiupu.getRecommendCategoryList(getSavedTicket(), false, new TwitterAdapter() {
            public void getRecommendCategoryList(ArrayList<RecommendHeadViewItemInfo> infolist) {
                Log.d(TAG, "finish loadRecommendCategory");

                if (infolist.size() > 0) {
                    doRecommendCategoryCallBack(true, infolist);
                }

                synchronized (mLoadRecommendcategory) {
                    inloadRecommendcategory = false;
                }

            }

            public void onException(TwitterException ex, TwitterMethod method) {
                Log.d(TAG, "Fail to get recommend category=" + ex.getMessage());

                synchronized (mLoadRecommendcategory) {
                    inloadRecommendcategory = false;
                }

                doRecommendCategoryCallBack(false, null);
            }
        });
    }

    protected void doRecommendCategoryCallBack(boolean suc, ArrayList<RecommendHeadViewItemInfo> info) {
    }

    // TODO: will be implemented to ensure there is a valid account login to Borqs Account.
    public boolean ensureAccountLogin() {
        if (!AccountServiceUtils.isAccountReady()) {
            gotoLogin();
            return false;
        }

        return true;
    }

    protected String getSavedTicket() {
        return AccountServiceUtils.getSessionID();
    }

    public static long getSaveUid() {
        return AccountServiceUtils.getBorqsAccountID();
    }

    protected final void initTutorial(LinearLayout tutorial) {
        //set LinerLayout params.
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        View content = createTutorialView();
//    	View content = LayoutInflater.from(this).inflate(R.layout.base_tutorial_bar, null);
//    	content.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
//    	TextView gotologin = (TextView)content.findViewById(R.id.login_ok_btn);
//    	gotologin.setOnClickListener(new OnClickListener() {
//			public void onClick(View arg0) {
//				gotoLogin();
//			}
//		});
        tutorial.removeAllViews();
        tutorial.addView(content, params);
        mTutortialView = tutorial;

        setTutorialVisibility();
    }

    protected View createTutorialView() {
        View content = LayoutInflater.from(this).inflate(R.layout.base_tutorial_bar, null);
        content.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        TextView gotologin = (TextView) content.findViewById(R.id.login_ok_btn);
        gotologin.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                gotoLogin();
            }
        });
        return content;
    }

    protected boolean needTutorialViewHidden() {
        return AccountServiceUtils.isAccountReady();
    }

    protected final void setTutorialVisibility() {
        if (null != mTutortialView) {
            mTutortialView.setVisibility(needTutorialViewHidden() ? View.GONE : View.VISIBLE);
        }
    }

    protected void showOperationFailToast(String reason, boolean isShort) {
        ToastUtil.showOperationFailed(this, mHandler, isShort, reason);
    }

    protected void showOperationSucToast(boolean isShort) {
        ToastUtil.showOperationOk(this, mHandler, isShort);
    }

    protected void updateAccountServiceValue(String newValue) {
        AccountServiceUtils.updateValue(newValue);
    }

    void resolveAccountServiceConflict() {
        if (AccountServiceUtils.isExternalAccountServiceInvalid()) {
            return;
        }
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder
                .setTitle(R.string.account_service_deny)
                .setMessage(R.string.account_service_deny_msg)
                .setPositiveButton(R.string.label_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
//                                installApp(pkgName);
                            }
                        })
//                .setNegativeButton(R.string.label_cancel,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog,
//                                                int whichButton) {
//                            }
//                        })
        ;
        dlgBuilder.create().show();

        if (AccountServiceAdapter.isAccountPreloaded(getApplicationContext())) {
//            AccountServiceAdapter.removeAccount(getApplicationContext());
        }

        AccountServiceUtils.setExternalAccountServiceInvalid(true);
    }

    private void showUpdateVersionDialog(final boolean forceUpdate, String versionFeatures, final String version, final long size) {
        Log.d(TAG, "showUpdateVersionDialog, versionFeatures: " + versionFeatures);
        View infoView = LayoutInflater.from(this).inflate(R.layout.new_versioninfo_view, null);
        TextView view = (TextView) infoView.findViewById(R.id.version_info);
        
        TextView outDateView = (TextView)infoView.findViewById(R.id.version_outdate_msg);
        outDateView.setVisibility(forceUpdate ? View.VISIBLE : View.GONE);
        
        TextView version_size = (TextView)infoView.findViewById(R.id.version_size_info);
        version_size.setText(String.format(getString(R.string.update_version_info_size), version, FileUtils.formatPackageFileSize(this, size)));
        if(versionFeatures != null) {
            versionFeatures = versionFeatures.replaceAll("\r\n", "<br>");
        }else {
            versionFeatures = "";
        }
        view.setText(Html.fromHtml(versionFeatures));
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        
        if(forceUpdate) dlgBuilder.setCancelable(false);
        
        dlgBuilder
                .setTitle(forceUpdate ? R.string.version_outdate : R.string.version_update)
                .setView(infoView)
//                .setMessage(forceUpdate ? R.string.version_outdate_msg : R.string.version_update_msg)
                .setPositiveButton(R.string.update_version_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                ApkResponse currentApk = null; //orm.getApkInfoByComponentName(getPackageName());
                                if (currentApk == null) {
                                    currentApk = new ApkResponse();
                                    //build for qiupu                   
                                    parseQiupuApkInfo(currentApk, BasicActivity.this.getApplicationInfo());
                                    currentApk.apksize = size;
                                }

                                if (canDownload(currentApk)) {
                                    Intent service = new Intent(BasicActivity.this, QiupuService.class);
                                    service.setAction(QiupuService.INTENT_QP_DOWNLOAD_APK);

                                    currentApk.apkurl = QiupuHelper.getQpApkUrl();
                                    service.putExtra(QiupuMessage.BUNDLE_APKINFO, currentApk);
                                    startService(service);
                                }
                            }
                        })
                .setNegativeButton(forceUpdate ? R.string.qiupu_exit_title : R.string.update_version_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                if (forceUpdate) {
                                    forceExitProcess();
                                }
                            }
                        }
                );
        if (this.isFinishing()) {
            Log.w(TAG, "showUpdateVersionDialog, skip show update dialog while activity was finishing.");
        } else {
            dlgBuilder.create().show();
        }
    }

    private void parseQiupuApkInfo(ApkResponse apk, ApplicationInfo appInfo) {
        parseQiupuApkInfo(getPackageManager(), apk, appInfo);
    }

    public static void parseQiupuApkInfo(PackageManager packageManager, ApkResponse apk, ApplicationInfo appInfo) {
        BorqsAccount account = AccountServiceUtils.getBorqsAccount();
        apk.uid = null == account ? -1 : account.uid;
        apk.packagename = appInfo.packageName;
        apk.label = String.valueOf(appInfo.loadLabel(packageManager));
        final String srcDir = appInfo.sourceDir;
        apk.intallpath = srcDir;
        apk.apksize = new File(srcDir).length();
        apk.targetSdkVersion = appInfo.targetSdkVersion;
        try {
            apk.versionname = packageManager.getPackageInfo(apk.packagename, 0).versionName;
            apk.versioncode = packageManager.getPackageInfo(apk.packagename, 0).versionCode;

            if (apk.targetSdkVersion == apk.versioncode
                    && apk.versionname == QiupuConfig.apkLevelMap.get(apk.versioncode)) {
                apk.versioncode = 0;
            }

            if (QiupuConfig.LowPerformance)
                Log.d(TAG, "==== pn:" + apk.packagename + " vc:" + apk.versioncode);

            apk.iconurl = null;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(apk.apk_server_id)) {
            apk.apk_server_id = apk.packagename + "-" + apk.versioncode + "-" + Build.CPU_ABI;
        }
    }

    private void forceExitProcess() {
        this.onDestroy();

        try {
            Intent service = new Intent(BasicActivity.this, QiupuService.class);
            stopService(service);

            Intent accountservice = new Intent(BasicActivity.this, BorqsAccountService.class);
            stopService(accountservice);

            finish();

            mHandler.postDelayed(new Runnable() {
                public void run() {
                    //System.exit(0);
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }, 1000);

        } catch (Exception ne) {
        }
    }

    private String[] speechList;

    protected void setVoiceString(String[] speechlist) {
        speechList = speechlist;
    }

    protected void doClickVoiceDialogCallBack(String backString) {
    }

    protected void updateActivityFavoriteUI(boolean isadd, String apkid) {
//        QiupuHelper.updateActivityFavoriteUI(isadd, apkid);
        // TODO : update favoriate.
    }


    //show
    protected void showRightActionBtn(boolean show) {
        
        if(isUsingActionBar() && getActionBar() != null)
        {
            mRightActionBtnMenu.setVisible(show ? true : false);          
        }
        else
        {   
            if (mRightActionBtn != null && mRightActionBtn.isEnabled()) {
                mRightActionBtn.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }
    
    protected void showRightTextActionBtn(boolean show) {
        
        if (mTitleActionText != null && mTitleActionText.isEnabled()) {
            mTitleActionText.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    protected void showEditTitleActionBtn(boolean show) {
        if(isUsingActionBar() && getActionBar() != null)
        {
//            mLeftActionBtnMenu.setVisible(show ? true : false);          
        }
        else
        {   
            if (mEditTitleBtn != null && mEditTitleBtn.isEnabled()) {
                mEditTitleBtn.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }

    protected void showLeftActionBtn(boolean show) {
        if(isUsingActionBar() && getActionBar() != null)
        {
            mLeftActionBtnMenu.setVisible(show ? true : false);          
        }
        else
        {   
            if (mLeftActionBtn != null && mLeftActionBtn.isEnabled()) {
                mLeftActionBtn.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }
    
    protected void showMiddleActionBtn(boolean show) {
        if(isUsingActionBar() && getActionBar() != null)
        {
            mMiddleActionBtnMenu.setVisible(show ? true : false);          
        }
        else
        {  
            if (mMiddleActionBtn != null)
                mMiddleActionBtn.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    
    protected void setLeftActionImageRes(int res) {
        showLeftActionBtn(true);
        
        if(isUsingActionBar() && getActionBar() != null)
        {
            mLeftActionBtnMenu.setIcon(res);          
        }
        else
        { 
            if (mLeftActionBtn != null) {
                mLeftActionBtn.setImageResource(res);
            }
        }
    }
    protected void alterRightActionBtnByFavorites(int favRes) {
    	if(isUsingActionBar() && getActionBar() != null)
    	{
    		mRightActionBtnMenu.setIcon(favRes);          
    	}
    	else
    	{ 
    		if (mRightActionBtn != null) {
    			mRightActionBtn.setImageResource(favRes);
    		}
    	}
    }
    protected void alterMiddleActionBtnByFavorites(int favRes) {
        if(isUsingActionBar() && getActionBar() != null)
        {
            mMiddleActionBtnMenu.setIcon(favRes);          
        }
        else
        { 
            if (mMiddleActionBtn != null) {
            	mMiddleActionBtn.setImageResource(favRes);
            }
        }
    }

    protected void alterMiddleActionBtnByComposer(int favRes, View.OnClickListener composeClick) {
        showMiddleActionBtn(true);
        overrideMiddleActionBtn(favRes, composeClick);
    }

    protected void enableLeftActionBtn(boolean enable) {
        if(isUsingActionBar() && getActionBar() != null)
        {
            mLeftActionBtnMenu.setVisible(enable);
        }
        else
        {
            if (null != mLeftActionBtn) {
                if (enable && !mLeftActionBtn.isEnabled()) {
                    mLeftActionBtn.setEnabled(true);
                    mLeftActionBtn.setOnClickListener(this);
                } else if (!enable && mLeftActionBtn.isEnabled()) {
                    mLeftActionBtn.setOnClickListener(null);
                    mLeftActionBtn.setEnabled(false);
                    mLeftActionBtn.setVisibility(View.GONE);
                }
            }
        }
    }

    protected void enableMiddleActionBtn(boolean enable) {
        if(isUsingActionBar() && getActionBar() != null)
        {
            mMiddleActionBtnMenu.setVisible(enable);
        }
        else
        {  
            if (null != mMiddleActionBtn) {
                if (enable && !mMiddleActionBtn.isEnabled()) {
                    mMiddleActionBtn.setEnabled(true);
                    mMiddleActionBtn.setOnClickListener(this);
                } else if (!enable && mMiddleActionBtn.isEnabled()) {
                    mMiddleActionBtn.setOnClickListener(null);
                    mMiddleActionBtn.setEnabled(false);
                    mMiddleActionBtn.setVisibility(View.GONE);
                }
            }
        }
    }

    protected void enableRightActionBtn(boolean enable) {
        if(isUsingActionBar() && getActionBar() != null)
        {
            mRightActionBtnMenu.setVisible(enable);
        }
        else
        {
            if (null != mRightActionBtn) {
                if (enable && !mRightActionBtn.isEnabled()) {
                    mRightActionBtn.setEnabled(true);
                    mRightActionBtn.setOnClickListener(this);
                } else if (!enable && mRightActionBtn.isEnabled()) {
                    mRightActionBtn.setOnClickListener(null);
                    mRightActionBtn.setEnabled(false);
                    mRightActionBtn.setVisibility(View.GONE);
                }
            }
        }
    }
   

    boolean inloadingFollower = false;
    Object mFollowerLock = new Object();
    boolean inloadingFollowing = false;
    Object mFollowingLock = new Object();

    public void syncfriendsInfo(final long mUserid, final int page, final int count, final String circles, final boolean isfollowing) {
        Log.d(TAG, "mUserid :" + mUserid);
        if (isfollowing) {
            synchronized (mFollowingLock) {
                if (inloadingFollowing == true) {
                    Log.d(TAG, "in doing get Following data");
                    callFailLoadFollowingMethod();
                    return;
                }
            }

            synchronized (mFollowingLock) {
                inloadingFollowing = true;
            }
        } else {
            synchronized (mFollowerLock) {
                if (inloadingFollower == true) {
                    Log.d(TAG, "in doing get Follower data");
                    callFailLoadFollowerMethod();
                    return;
                }
            }

            synchronized (mFollowerLock) {
                inloadingFollower = true;
            }

        }

        Log.d(TAG, "mUserid :" + mUserid);
        begin();
        asyncQiupu.getFriendsListPage(AccountServiceUtils.getSessionID(), mUserid, circles, page, count, isfollowing, new TwitterAdapter() {
            public void getFriendsList(List<QiupuUser> users) {
                Log.d(TAG, "finish getFriendsList=" + users.size());

                if (mUserid == AccountServiceUtils.getBorqsAccountID() && isfollowing) {
                    //remove circle users, firstly			 
//                    if (page == 0) {
//                        orm.clearCircleUsers(circles);
//                    }

                    //update circle users
//                    orm.updateCircleUsers(circles, users);
                    orm.insertFriendsList(users);//add friends to DB
                }

                getUserInfoEndCallBack(users, isfollowing);

                if (isfollowing) {
                    synchronized (mFollowingLock) {
                        inloadingFollowing = false;
                    }
                } else {
                    synchronized (mFollowerLock) {
                        inloadingFollower = false;
                    }
                }

                Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_LOAD_DATA_END);
                msg.getData().putBoolean(RESULT, true);
                msg.getData().putInt("count", users.size());
                msg.getData().putInt("page", page);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                if (isfollowing) {
                    synchronized (mFollowingLock) {
                        inloadingFollowing = false;
                    }
                } else {
                    synchronized (mFollowerLock) {
                        inloadingFollower = false;
                    }
                }


                if (isfollowing) {
                    callFailLoadFollowingMethod();
                } else {
                    callFailLoadFollowerMethod();
                }

                Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_LOAD_DATA_END);
                msg.getData().putString(ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }

    protected void getUserInfoEndCallBack(List<QiupuUser> users, boolean isfollowing) {
    }
    
    protected void createCircleCallBack(UserCircle circle) {
    }

    boolean inCreateCircle;
    Object mLockcreateCircle = new Object();

    public void createCircle(final String circleName) {
        if (inCreateCircle == true) {
            Toast.makeText(this, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLockcreateCircle) {
            inCreateCircle = true;
        }
        showDialog(DIALOG_CREATE_CIRCLE_PROCESS);

        asyncQiupu.createCircle(AccountServiceUtils.getSessionID(), circleName,
                new TwitterAdapter() {
                    public void createCircle(long circleID) {
                        Log.d(TAG, "finish createCircle=" + circleID);

                        UserCircle backCircle = new UserCircle();
                        backCircle.circleid = circleID;
                        backCircle.name = circleName;
                        backCircle.uid = AccountServiceUtils.getBorqsAccountID();
                        backCircle.type = UserCircle.CIRCLE_TYPE_LOCAL;
//						backCircle.memberCount = 0;
                        orm.insertCircleInfo(backCircle);
                        createCircleCallBack(backCircle);

                        Message msg = mBasicHandler.obtainMessage(BasicHandler.CREATE_CIRCLE_END);
                        msg.getData().putBoolean(RESULT, true);
                        msg.sendToTarget();
                        synchronized (mLockcreateCircle) {
                            inCreateCircle = false;
                        }
                    }

                    public void onException(TwitterException ex, TwitterMethod method) {
                        synchronized (mLockcreateCircle) {
                            inCreateCircle = false;
                        }
                        Message msg = mBasicHandler.obtainMessage(BasicHandler.CREATE_CIRCLE_END);
                        msg.getData().putBoolean(RESULT, false);
                        msg.sendToTarget();
                    }
                });
    }

    protected void callFailLoadFollowingMethod() {
    }

    protected void callFailLoadFollowerMethod() {
    }

    protected void deleteUserFromCircleCallBack() {
    }

    protected void doCircleActionCallBack(boolean isdelete) {
    }

//    protected void doDeletePostCallBack() {
//    }

    protected static boolean isValidAccountSession() {
        return !StringUtil.isEmpty(AccountServiceUtils.getSessionID());
    }

    public void onAccountServiceConnected() {
        AccountServiceUtils.refreshBorqsAccount();
    }

    public void onAccountServiceDisconnected() {
    }

//    private String getCircleName(long circleId, String circleName) {
//        Resources res = getResources();
//        if (circleId == QiupuConfig.BLOCKED_CIRCLE) {
//            return res.getString(R.string.bolcked_circle);
//        } else if (circleId == QiupuConfig.ADDRESS_BOOK_CIRCLE) {
//            return res.getString(R.string.address_book_circle);
//        } else if (circleId == QiupuConfig.DEFAULT_CIRCLE) {
//            return res.getString(R.string.default_circle);
//        } else if (circleId == QiupuConfig.FAMILY_CIRCLE) {
//            return res.getString(R.string.family_circle);
//        } else if (circleId == QiupuConfig.CLOSE_FRIENDS_CIRCLE) {
//            return res.getString(R.string.close_friends_circle);
//        } else if (circleId == QiupuConfig.ACQUAINTANCE_CIRCLE) {
//            return res.getString(R.string.acquaintance_circle);
//        } else {
//            return circleName;
//        }
//    }

    protected void circleUpdateCallBack(String multiuids, String circleid, boolean isadd) {
        String[] uids = multiuids.split(",");
        if (uids.length > 0) {
            for (int i = 0; i < uids.length; i++) {
                orm.updateUserInfoToDb(uids[i], circleid, isadd);
            }
        }

        //update count of circle
        UserCircle tmpcircle = orm.queryOneCircle(AccountServiceUtils.getBorqsAccountID(), Long.parseLong(circleid));
        if (tmpcircle != null) {
            if (isadd) {
                tmpcircle.memberCount = tmpcircle.memberCount + uids.length;
                orm.updateCircleInfo(tmpcircle);
            } else {
                int count = tmpcircle.memberCount - uids.length;
                tmpcircle.memberCount = count > 0 ? count : 0;
                orm.updateCircleInfo(tmpcircle);
            }
        }

        //remove user from circleUser
        ArrayList<Long> ids = getCirlceOrUserIds(multiuids, false);
        if (isadd == false) {
            orm.clearCircleUser(circleid, ids);
        } else {
            orm.updateCircleUsers(circleid, ids);
        }
    }

//    private void updateUserInfoToDb(String uid, String circleid, boolean isadd) {
//        QiupuUser dbUser = orm.queryOneUserInfo(Long.parseLong(uid));
//        if (dbUser != null) {
//            String circleids = "";
//            String circlenames = "";
//            if (isadd) {
//                circleids = parseUserCircleIdIsadd(dbUser.circleId, circleid);
//                circlenames = parseUserCircleNameIsadd(dbUser.circleName, circleid);
//            } else {
//                circleids = parseUserCircleIdIsDelete(dbUser.circleId, circleid);
//                circlenames = parseUserCircleNameIsDelete(dbUser.circleName, circleid);
//            }
//            dbUser.circleId = circleids;
//            dbUser.circleName = circlenames;
//            if (circleids.length() <= 0 || circleids.equals(String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE))) {
//                orm.deleteFriendsinfo(dbUser.uid);
//                // update privacy circle count;
//                UserCircle tmpcircle = orm.queryOneCircle(AccountServiceUtils.getBorqsAccountID(), QiupuConfig.ADDRESS_BOOK_CIRCLE);
//                tmpcircle.memberCount = tmpcircle.memberCount - 1;
//                orm.updateCircleInfo(tmpcircle);
//            } else {
//                orm.updateFriendsInfo(dbUser);
//            }
//        }
//    }

//    private String parseUserCircleNameIsadd(String dbcircleName, String circleid) {
//        String localName = CircleUtils.getLocalCircleName(this, Long.parseLong(circleid), "");
//        if (localName.length() <= 0) {
//            UserCircle circleinfo = orm.queryOneCricleInfo(circleid);
//            if (dbcircleName.length() > 0) {
//                if (circleinfo != null)
//                    return dbcircleName + "," + circleinfo.name;
//            } else {
//                if (circleinfo != null)
//                    return circleinfo.name;
//            }
//        }
//        return dbcircleName;
//    }
//
//    private String parseUserCircleNameIsDelete(String circleName, String circleid) {
//        StringBuilder namebuilder = new StringBuilder();
//        UserCircle cricleinfo = orm.queryOneCricleInfo(circleid);
//        if (cricleinfo != null) {
//            String[] names = circleName.split(",");
//            for (int i = 0; i < names.length; i++) {
//                if (!cricleinfo.name.equals(names[i])) {
//                    if (namebuilder.length() > 0) {
//                        namebuilder.append(",");
//                    }
//                    namebuilder.append(names[i]);
//                }
//            }
//        } else {
//            namebuilder.append(circleName);
//        }
//
//        return namebuilder.toString();
//    }
//
//    private String parseUserCircleIdIsadd(String circleId, String circleid) {
//        if (circleId != null && circleId.length() > 0) {
//            return circleId + "," + circleid;
//        } else {
//            return circleid;
//        }
//    }
//
//    private String parseUserCircleIdIsDelete(String circleId, String circleid) {
//        StringBuilder idbuilder = new StringBuilder();
//        String[] ids = circleId.split(",");
//        for (int i = 0; i < ids.length; i++) {
//            if (!ids[i].equals(circleid)) {
//                if (idbuilder.length() > 0) {
//                    idbuilder.append(",");
//                }
//                idbuilder.append(ids[i]);
//            }
//        }
//        return idbuilder.toString();
//    }

    protected boolean testValidConnectivity() {
        final boolean isConnectivityActive = DataConnectionUtils.testValidConnection(this);

        if (!isConnectivityActive) {
            showCustomToast(R.string.dlg_msg_no_active_connectivity);
        }

        return isConnectivityActive;
    }

    protected boolean isLoadingFollower() {
        return inloadingFollower == true;
    }

    protected boolean isLoadingFollowing() {
        return inloadingFollowing == true;
    }

    //    private QiupuUser mdeleteUser;
//    private String mdeleteCircleid;
    public void deleteUser(String uid, String circleid) {
        Message msg = mBasicHandler.obtainMessage(BasicHandler.DELETE_USER);
        msg.getData().putString("userid", uid);
        msg.getData().putString(IntentUtil.EXTRA_KEY_CIRCLE_ID, circleid);
        msg.sendToTarget();
    }

    private class ButtonWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().length() > 0) {
                setButtonEnable(true);
            } else {
                setButtonEnable(false);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    private AlertDialog mAlertDialog;

    protected void setButtonEnable(boolean flag) {
        mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(flag);
    }

    boolean inUsersSet;
    Object mLockUsersSet = new Object();

    /*
	 * isadd means add/remove uid into circleid
	 */
    public void circleUpdate(final String uid, final String circleid, final boolean isadd) {
        if (inUsersSet == true) {
            Toast.makeText(this, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLockUsersSet) {
            inUsersSet = true;
        }
        showDialog(DIALOG_SET_USER_PROCESS);

        asyncQiupu.usersSet(getSavedTicket(), uid, circleid, isadd, new TwitterAdapter() {
            public void usersSet(boolean result) {
                Log.d(TAG, "finish usersSet :" + result);

                if (result) {
                    circleUpdateCallBack(uid, circleid, isadd);
                }

                Message msg = mBasicHandler.obtainMessage(BasicHandler.USER_SET_CIRCLE_END);
                msg.getData().putBoolean(RESULT, result);
                msg.getData().putBoolean("isadd", isadd);
                msg.getData().putString("uid", uid);
                msg.sendToTarget();
                synchronized (mLockUsersSet) {
                    inUsersSet = false;
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockUsersSet) {
                    inUsersSet = false;
                }
                Message msg = mBasicHandler.obtainMessage(BasicHandler.USER_SET_CIRCLE_END);
                msg.getData().putBoolean(RESULT, false);
                msg.getData().putString(ERROR_MSG, ex.getMessage());
                msg.sendToTarget();
            }
        });
    }

    private static ArrayList<Long> getCirlceOrUserIds(String circle, boolean isCircleIds) {
        ArrayList<Long> ids = new ArrayList<Long>();

        if (StringUtil.isValidString(circle)) {
            String[] cids = circle.split(",");
            for (int i = 0; i < cids.length; i++) {
                long tmpId = Long.parseLong(cids[i]);
                if(isCircleIds) {
                    if(QiupuConfig.isPublicCircleProfile(tmpId) == false) {
                        ids.add(Long.parseLong(cids[i]));
                    }
                }else {
                    ids.add(Long.parseLong(cids[i]));
                }
            }
        }

        return ids;
    }

    protected void doUsersSetCallBack(String uid, boolean isadd) {
    }

//    //	boolean inSendRequest;
////	Object mLockSendRequest = new Object();
//    public void sendApproveRequest(final String uid, final String message) {
////		if (inSendRequest == true) {
////			Toast.makeText(this, R.string.string_in_processing,Toast.LENGTH_SHORT).show();
////			return;
////		}
//
////		synchronized (mLockSendRequest) {
////			inSendRequest = true;
////		}
//        showDialog(DIALOG_PROFILE_REQUEST_PROCESS);
//        asyncQiupu.sendApproveRequest(getSavedTicket(), uid, message, new TwitterAdapter() {
//            public void sendApproveRequest(boolean result) {
//                Log.d(TAG, "finish sendApproveRequest :" + result);
//
//                if (result) {
//                    // update user info in DB
////					updateUserInfoAfterParse(uid, circleid);//TODO need Optimization
//                }
//
////				orm.setRequestUser(Long.parseLong(uid), true);
//
//                Message msg = mBasicHandler.obtainMessage(BasicHandler.REQUEST_PROFIEL_END);
//                msg.getData().putBoolean(RESULT, result);
//                msg.getData().putLong("uid", Long.parseLong(uid));
//                msg.sendToTarget();
////				synchronized (mLockSendRequest) {
////					inSendRequest = false;
////				}
//            }
//
//            public void onException(TwitterException ex, TwitterMethod method) {
////				synchronized (mLockSendRequest) {
////					inSendRequest = false;
////				}
//                Message msg = mBasicHandler.obtainMessage(BasicHandler.REQUEST_PROFIEL_END);
//                msg.getData().putBoolean(RESULT, false);
//                msg.sendToTarget();
//            }
//        });
//    }

    protected void showProgressBtn(boolean show) {
        View head_progress = findViewById(R.id.head_progress);
        if (head_progress != null && head_progress.isEnabled()) {
            head_progress.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }


    Object mEditInfoLock = new Object();
    boolean inEditProcess;

    public void updateUserInfo(HashMap<String, String> coloumsMap) {
        if (!AccountServiceUtils.isAccountReady()) {
            return;
        }

        synchronized (mEditInfoLock) {
            if (inEditProcess == true) {
                Log.d(TAG, "in update info data");
                return;
            }
        }
        showDialog(DIALOG_PROFILE_UPDATE_SERVER);

        synchronized (mEditInfoLock) {
            inEditProcess = true;
        }
        asyncQiupu.updateUserInfo(AccountServiceUtils.getSessionID(), coloumsMap,
                new TwitterAdapter() {
                    public void updateUserInfo(boolean result) {
                        Log.d(TAG, "finish edit user profile");
                        synchronized (mEditInfoLock) {
                            inEditProcess = false;
                        }

                        Message msg = mBasicHandler.obtainMessage(BasicHandler.UPDATE_USER_INFO_END);
                        msg.getData().putBoolean(RESULT, result);
                        msg.sendToTarget();
                    }

                    public void onException(TwitterException ex, TwitterMethod method) {

                        synchronized (mEditInfoLock) {
                            inEditProcess = false;
                        }
                        TwitterExceptionUtils.printException(TAG, "updateUserInfo, server exception:", ex, method);

                        Message msg = mBasicHandler.obtainMessage(BasicHandler.UPDATE_USER_INFO_END);
                        msg.getData().putBoolean(RESULT, false);
                        msg.getData().putString(ERROR_MSG, ex.getMessage());
                        msg.sendToTarget();
                    }
                });
    }

    protected void doUpdateUserInfoEndCallBack(boolean suc) {
    }

    public void initContactInfoMap(LinkedHashMap<String, String> map) {
        Cursor cursor = orm.queryOneUserPhoneEmail(AccountServiceUtils
                .getBorqsAccountID());
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String type = cursor.getString(cursor.getColumnIndex(PhoneEmailColumns.TYPE));
                String info = cursor.getString(cursor.getColumnIndex(PhoneEmailColumns.INFO));
                map.put(type, info);
            } while (cursor.moveToNext());
            cursor.close();
            cursor = null;
        }else {
            Log.d(TAG, "need load myself info from server");
        }
    }

    private String queryInvitationMessage() {
        StringBuffer stringBuffer = new StringBuffer();
        appendUserWithPhoneNumber(stringBuffer, contactsPhone);

        if (stringBuffer.length() > 0) {
            stringBuffer.append("\n");
        }

        appendUserWithEmail(stringBuffer, contactsEmail);

        return stringBuffer.toString();
    }

    private static final int MORE_BUDDY_THRESHOLD = 3;

    private void appendUserWithPhoneNumber(StringBuffer sb, ArrayList<ContactSimpleInfo> phoneList) {
        if (null != phoneList && phoneList.size() > 0) {
            final int size = phoneList.size();
            int count = MORE_BUDDY_THRESHOLD;
            sb.append(getString(R.string.message_invite_dialog_message));
            for (int i = 0; i < size; ++i) {
                if (TextUtils.isEmpty(phoneList.get(i).display_name_primary)) {
                    continue;
                }

                sb.append(phoneList.get(i).display_name_primary);
                if (count-- > 0 && i < size - 1) {
                    sb.append(", ");
                } else {
                    break;
                }
            }
            if (size > MORE_BUDDY_THRESHOLD) {
                sb.append(getString(R.string.other_more_people,
                        (size - MORE_BUDDY_THRESHOLD)));
            }
        }
    }

    private void appendUserWithEmail(StringBuffer sb, ArrayList<ContactSimpleInfo> emailList) {
        if (null != emailList && emailList.size() > 0) {
            final int size = emailList.size();
            sb.append(getString(R.string.email_invite_dialog_message));
            for (int i = 0; i < size; ++i) {
                sb.append(emailList.get(i).display_name_primary);
                if (i < MORE_BUDDY_THRESHOLD && i < size - 1) {
                    sb.append(", ");
                } else {
                    break;
                }
            }

            if (size > MORE_BUDDY_THRESHOLD) {
                sb.append(getString(R.string.other_more_people,
                        (size - MORE_BUDDY_THRESHOLD)));
            }
        }
    }

    boolean inloadingCircle = false;
    Object circleLock = new Object();

    protected void syncCircleInfo() {
        synchronized (circleLock) {
            if (inloadingCircle == true) {
                Log.d(TAG, "in doing syncCircleInfo data");
                return;
            }
        }

        synchronized (circleLock) {
            inloadingCircle = true;
        }
        begin();
        asyncQiupu.getUserCircle(AccountServiceUtils.getSessionID(), AccountServiceUtils.getBorqsAccountID(), "", false, new TwitterAdapter() {
            public void getUserCircle(ArrayList<UserCircle> userCircles) {
                Log.d(TAG, "finish getUserCircle= " + userCircles.size());

                if (userCircles.size() > 0) {
                    orm.insertCircleList(userCircles, AccountServiceUtils.getBorqsAccountID());
                }

                dogetUserCircleCallBack(true, userCircles);
                synchronized (circleLock) {
                    inloadingCircle = false;
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                TwitterExceptionUtils.printException(TAG, "getUserCircle, server exception:", ex, method);
                synchronized (circleLock) {
                    inloadingCircle = false;
                }
                dogetUserCircleCallBack(false, null);
            }
        });
    }

    protected void dogetUserCircleCallBack(boolean suc, ArrayList<UserCircle> userCircles) {
    }

    public void run(android.accounts.AccountManagerFuture<Bundle> future) {
        Log.d(TAG, "run, callback from AccountManagerCallback.");
        if (future != null) {
            Bundle result = null;
            try {
                if (future.isCancelled()) {
                    onAccountLoginCancelled();
                } else if (future.isDone()) {
                    onAccountLoginDone();
                } else {
                    result = future.getResult();
                    if (result.getBoolean("from_register")) {
                        onAccountRegistered();
                    }
                }
            } catch (OperationCanceledException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (AuthenticatorException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    @Override
    public void onAccountLogin(boolean flag, AccountService.AccountSessionData accountSessionData) {
        Log.d(TAG, "onAccountLogin, flag:" + flag + ", data: " + accountSessionData);
        if (flag) {
            onAccountLoginDone();
        } else {
            onAccountLoginCancelled();
        }
    }

    protected void onMessageInvited() {
        // call back while invitation message sent.
    }

    protected void onEmailInvited() {
        // call back while invitation email sent successfully.
    }


    protected void enableProgressBtn(boolean enable) {
        View head_progress = findViewById(R.id.head_progress);
        if (head_progress != null) {
            head_progress.setEnabled(enable);
            if (!enable) {
                head_progress.setVisibility(View.GONE);
            }
        }
    }
    

    // Query valid nick name from local database first, then possible get
    // from Account field.
    protected final String getUserNickname() {
        String nickName = orm.getUserName(getSaveUid());
        if (TextUtils.isEmpty(nickName)) {
            BorqsAccount account = AccountServiceUtils.getBorqsAccount();
            if (null == account) {
                nickName = "";
            } else {
                nickName = account.nickname;
            }
        }
        return TextUtils.isEmpty(nickName) ? "" : nickName;
    }

    protected void gotoSettingActivity() {
        startActivity(new Intent(this, BpcSettingsActivity.class));
    }

    private void showShortToast(final int resId) {
    	mBasicHandler.post(new Runnable(){
    		public void run()
    		{
    			Toast.makeText(BasicActivity.this, resId, Toast.LENGTH_SHORT).show();
    		}
    	});
        
    }

    private void showShortToast(final CharSequence message) {
        mBasicHandler.post(new Runnable(){
    		public void run()
    		{
    			Toast.makeText(BasicActivity.this, message, Toast.LENGTH_SHORT).show();
    		}
    	});
        
    }

    private void showLongToast(final int resId) {
    	mBasicHandler.post(new Runnable(){
    		public void run()
    		{
    			Toast.makeText(BasicActivity.this, resId, Toast.LENGTH_LONG).show();
    		}
    	});
    }

    private void showLongToast(final CharSequence message) {
        mBasicHandler.post(new Runnable(){
    		public void run()
    		{
    			Toast.makeText(BasicActivity.this, message, Toast.LENGTH_LONG).show();
    		}
    	});
    }

    public void showCustomToast(int resId,int title_bar_id) {
    	showCustomToast(getString(resId),title_bar_id);
    }
    
    public void showCustomToast(int resId) {
        showCustomToast(getString(resId),0);
    }

    public void showCustomToast(int resId, Handler handler) {
        showCustomToast(getString(resId),0, handler);
    }
    
    public void showCustomToast(final String textMsg) {
    	showCustomToast(textMsg, 0);
    }

    public void showCustomToast(final String textMsg, Handler handler) {
    	showCustomToast(textMsg, 0, handler);
    }
    
    public void showCustomToast(final String textMsg,final int title_bar_id) {
    	showCustomToast(textMsg, title_bar_id, mHandler);
    }
    
    public void showCustomToast(final String textMsg,final int title_bar_id, Handler handler) {
        if (null == handler) {
            return;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
            	View title = null;
            	if(title_bar_id == 0) {
            		title = findViewById(R.id.titlebar_container);
            	}else {
            		title = findViewById(title_bar_id);
            	}
            	if(title != null) {
            		View layout = getLayoutInflater().inflate(R.layout.notification_toast, null);
            		TextView tv = ((TextView) layout.findViewById(R.id.text));
            		tv.getBackground().setAlpha(220);
            		tv.setText(textMsg);
            		tv.setLayoutParams(new LayoutParams(title.getWidth() - 8, title.getHeight() - 8));
            		
            		Toast toast = new Toast(getApplicationContext());
            		toast.setView(layout);
            		toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, title.getHeight() + 4);
            		toast.setDuration(Toast.LENGTH_SHORT);
            		toast.show();
            	}else {
            		Log.e(TAG, "find title is null");
            	}
            }
        });
    }

    protected void preHandleTwitterException(TwitterException te) {
        if (null != te) {
            switch (te.getStatusCode()) {
                case ErrorResponse.STREAM_REMOVED:
                    showCustomToast(R.string.exception_stream_removed);
                    break;
                default:
                    if (!TextUtils.isEmpty(te.getMessage())) {
                        showCustomToast(te.getMessage(),0);
                    }
                    break;
            }
        }
    }

    protected void onAccountLoginCancelled() {
    }

    // declare as final, sub classes should not override this method, and
    // implememt post login logic via onLogin() that will be trigger later.
    protected final void onAccountLoginDone() {
        //reget account and notify borqs account service
        AccountServiceUtils.resetBorqsAccount(getApplicationContext());
        //notify current ui and service to do task for on login
        UserAccountObserver.login();
    }

    private void showDeleteUserDialog(final String uid, final String circleid) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_user_title)
                .setMessage(getString(R.string.delete_user_from_privacy_circle))
                .setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        circleUpdate(uid, circleid, false);
                    }
                })
                .setNegativeButton(getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).create().show();
    }

    protected void tryUpdateInitialDetect() {
        if (!mIsInitVersionChecked) {
            IntentUtil.ensureAppShareVersion(this);
            checkQiupuVersion(true);
            mIsInitVersionChecked = true;
        }
    }

    // new account registered and login, provide some guide and policy info.
    protected void onAccountRegistered() {
        DataConnectionUtils.showPrivacyPolicyInfo(this);
        Log.d(TAG, "onAccountRegistered, let goto people may know for " + getSaveUid());
        BpcApiUtils.startFriendsActivityWithAppId(getApplicationContext(), BpcApiUtils.APPID.BPC_APPID,
                getSaveUid(), BpcApiUtils.TAB_FRIENDS_SUGGESTION);
    }

    /**
     * Helper for examples with a HSV that should be scrolled by a menu View's width.
     */
    
    interface AccountChecker {
        public boolean preCheckForScrollingCondition();
    };

    private void shootProfileActivity() {
        if (ensureAccountLogin()) {
            IntentUtil.startUserDetailIntent(this, getSaveUid(), getUserNickname());
        }
    }

    private void shootFriendActivity(int current_type) {
        if (ensureAccountLogin()) {
            Intent intent = new Intent(this, BpcFriendsFragmentActivity.class);
            Bundle bundle = BpcApiUtils.getUserBundle(Long.valueOf(getSaveUid()));
            bundle.putString(BasicActivity.USER_NICKNAME, getUserNickname());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    protected void shootNotificationActivity() {
        if (ensureAccountLogin()) {
            Intent intent = new Intent(BasicActivity.this, BpcInformationActivity.class);
            
//            Intent intent = new Intent(BasicActivity.this, BpcNtfCenterActivity.class);
            startActivity(intent);
        }
    }

    protected void gotoSearchActivity() {
//        IntentUtil.startPeopleSearchIntent(this);
        onSearchRequested();
    }

    protected View.OnClickListener composeStreamListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            IntentUtil.startComposeIntent(BasicActivity.this);
        }
    };

    protected void bindBorqsAccountService() {
        QiupuService.bindBorqsAccountService(mApp);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return super.onKeyLongPress(keyCode, event);
    }
    protected void gotoStreamActivity() {
        IntentUtil.startStreamListIntent(this, false);
    }

    /**
     * Sub class should overwrite this methods to trigger some extra actions before leaving the activity.
     * @return true to directly escape from current activity, otherwise false.
     */
    protected boolean preEscapeActivity() {
        return true;
    }

    protected void likeStream(final Stream post) {
        if (!post.iLike) {
            if (BpcApiUtils.isValidTypeOfAppAttachment(post.type)
                    && !TextUtils.isEmpty(post.rootid)) {
                createLike(post.rootid, QiupuConfig.TYPE_APK);
            } else {
                createLike(post.post_id, QiupuConfig.TYPE_STREAM);
            }
        }
    }

    private void unLikeStream(final Stream post) {
        if (post.iLike) {
            if (BpcApiUtils.isValidTypeOfAppAttachment(post.type)
                    && !TextUtils.isEmpty(post.rootid)) {
                removeLike(post.rootid, QiupuConfig.TYPE_APK);
            } else {
                removeLike(post.post_id, QiupuConfig.TYPE_STREAM);
            }
        }
    }

    protected boolean performGoHomeAction() {
        finish();
        return true;
    }

    // abstract class for those prefer to use action bar for ics or later system.
    // start override super methods in order to force using system action bar for
    // android system later than api level 11.
    public ActionBar getActionBar() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            return super.getActionBar();
        }

        return null;
    }

    /**
     * provide statical function base classes with Baidu SDK.
     */
    public static class StatActivity extends FragmentActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            QiupuConfig.enableStrictMode();
            super.onCreate(savedInstanceState);
        }

        @Override
        protected void onPause() {
            super.onPause();

            Log.d(TAG, "onPause=" + this);

            StatService.onPause(this);
        }

        @Override
        protected void onResume() {
            super.onResume();

            Log.d(TAG, "onResume=" + this);

            StatService.onResume(this);
        }
    }

    public static abstract class StatTabActivity extends TabActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            QiupuConfig.enableStrictMode();
            super.onCreate(savedInstanceState);
        }

        @Override
        protected void onPause() {
            super.onPause();

            Log.d(TAG, "onPause=" + this);

            StatService.onPause(this);
        }

        @Override
        protected void onResume() {
            super.onResume();

            Log.d(TAG, "onResume=" + this);

            StatService.onResume(this);
        }
    }

    public static abstract class StatPreferenceActivity extends PreferenceActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            QiupuConfig.enableStrictMode();
            super.onCreate(savedInstanceState);
        }

        @Override
        protected void onPause() {
            super.onPause();

            Log.d(TAG, "onPause=" + this);

            StatService.onPause(this);
        }

        @Override
        protected void onResume() {
            super.onResume();

            Log.d(TAG, "onResume=" + this);

            StatService.onResume(this);
        }
    }
    // End of statical base classes
}
