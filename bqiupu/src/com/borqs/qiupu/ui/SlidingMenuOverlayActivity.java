package com.borqs.qiupu.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import twitter4j.Requests;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.IconListAdapter;
import com.borqs.common.adapter.InformationAdapter;
import com.borqs.common.adapter.InformationAdapter.MoreItemCheckListener;
import com.borqs.common.adapter.LeftMenuAdapter;
import com.borqs.common.adapter.RequestsAdapter;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.NotificationListener;
import com.borqs.common.listener.RequestActionListner;
import com.borqs.common.listener.RequestRefreshListner;
import com.borqs.common.model.MainItemInfo;
import com.borqs.common.util.FileUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.util.UserTask;
import com.borqs.common.view.InformationItemView;
import com.borqs.common.view.LeftMenuItemView;
import com.borqs.common.view.LeftMenuListView;
import com.borqs.common.view.LeftNavigationCallBack;
import com.borqs.information.InformationBase;
import com.borqs.information.db.Notification;
import com.borqs.information.db.NotificationOperator;
import com.borqs.information.util.InformationConstant;
import com.borqs.information.util.InformationReadCache;
import com.borqs.information.util.InformationUtils;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.PhoneEmailColumns;
import com.borqs.qiupu.service.RequestsService;
import com.borqs.qiupu.service.RequestsService.RequestListener;
import com.borqs.qiupu.ui.bpc.BpcInformationActivity;
import com.borqs.qiupu.ui.bpc.BpcPostsNewActivity;
import com.borqs.qiupu.ui.bpc.EventListActivity;
import com.borqs.qiupu.ui.bpc.InformationListActivity;
import com.borqs.qiupu.ui.bpc.PollListActivity;
import com.borqs.qiupu.ui.circle.quickAction.NtfQuickAction;
import com.borqs.qiupu.ui.circle.quickAction.NtfQuickAction.OnDismissListener;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.JSONUtil;
import com.borqs.qiupu.util.LeftMenuMapping;
import com.borqs.qiupu.util.StringUtil;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-12-20
 * Time: 上午11:35
 * To change this template use File | Settings | File Templates.
 */

public abstract class SlidingMenuOverlayActivity extends BasicActivity
        implements /*NotifyActionListener,*/ Animation.AnimationListener, 
        LeftMenuListView.LeftNavigationListener/*, UpdateNotificationListener*/, NotificationListener, RequestActionListner, RequestListener, RequestRefreshListner, OnDismissListener, MoreItemCheckListener {
    private static final String TAG = "Qiupu.SlidingMenuOverlayActivity";
    // change for to add list action menu
    private static final boolean FORCE_DISABLE_CUSTOMIZED_TITLE = false;
    private View right;

    @Override
    protected boolean isUsingActionBar() {
        // force to use action bar in this activity
        if (FORCE_DISABLE_CUSTOMIZED_TITLE) {
            return !fromtab && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;
        }
        return super.isUsingActionBar();
    }

    @Override
    protected boolean isUsingTabNavigation(Context con, boolean fromtab) {
        if (isUsingActionBar()) {
            // force set false for this activity.
            return true;
        }

        return super.isUsingTabNavigation(con, fromtab);
    }

    @Override
    protected boolean performGoHomeAction() {
        return true;
    }

    @Override
    protected void prepareActionBar() {
        if (isUsingActionBar()) {
            boolean ret = requestWindowFeature(Window.FEATURE_ACTION_BAR | Window.FEATURE_PROGRESS);
            if (ret == true && getActionBar() != null) {
                // Override super method to disable background alternation.
//                    getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
                getActionBar().setDisplayShowCustomEnabled(true);

                invalidateOptionsMenu();
            }
        } else {
            super.prepareActionBar();
        }
    }

    boolean menuOut = false;
    ViewGroup leftMenu;
    View leftListLayout;
    LeftMenuListView leftMenuListView;

    class LeftMenuClickListener implements View.OnClickListener {
        private boolean isMenuRightPanel;

        public LeftMenuClickListener(boolean isMenuRightPanel) {
            super();
            this.isMenuRightPanel = isMenuRightPanel;
        }

        @Override
        public void onClick(View v) {
            handleLeftMenu(isMenuRightPanel, true);
        }
    }

    protected View.OnClickListener leftClicker = new LeftMenuClickListener(false);
    protected View.OnClickListener rightClicker = new LeftMenuClickListener(true);

    private void handleLeftMenu(boolean isMenuRightPanel, boolean isShowAnimation) {
        Animation anim = null;
        if ((isMenuRightPanel && !menuOut) || leftMenu == null) {
            return;
        }
        if (!menuOut) {
            right.setClickable(true);
            leftListLayout.setClickable(true);
            leftMenu.setVisibility(View.VISIBLE);
            leftMenuListView.openLeftUI();
            //EnfoldmentView.foldEnfoldmentView();
            if (isShowAnimation) {
                anim = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
            }
        } else {
            if (isShowAnimation) {
                anim = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
            }
            right.setClickable(false);
            leftListLayout.setClickable(false);
        }
        if (isShowAnimation) {
            anim.setAnimationListener(this);
            leftMenu.startAnimation(anim);
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        closeLeftMenu();
    }

    @Override
    protected int getOverlayContentId() {
        return R.layout.left_menu_list_layout;
    }

    @Override
    protected boolean inflateNavigatingContentView(int layoutResID) {
        getPluginItemInfo();
        LayoutInflater inflater = LayoutInflater.from(this);
        View p_layout = getWindow().getDecorView();

        LinearLayout layout_app = (LinearLayout) p_layout.findViewById(R.id.layout_app);
        leftMenu = (ViewGroup) p_layout.findViewById(R.id.menu);
        View appSpan = inflater.inflate(layoutResID, layout_app, true);
        if (null != appSpan.findViewById(R.id.titlebar_container)) {
            leftMenuListView = setLeftMenuList(p_layout);

            setTitleResource(R.layout.title_bar_base_ntf);
            initHeadViews();

            appSpan.findViewById(R.id.head_title);//.setVisibility(View.GONE);

            final TextView ntfTextView;
            if (isUsingActionBar() == false) {
                ntfTextView = (TextView) appSpan.findViewById(R.id.notification_btn_text_left);

                if (fromtab || isShowNotification() == false) {
                    if (ntfTextView != null) {
                        ntfTextView.setVisibility(View.GONE);
                    }
                }
            } else {
                View view = inflater.inflate(R.layout.notification_textview, null);
                ntfTextView = (TextView) view.findViewById(R.id.notification_btn_text_left_actionbar);

                getActionBar().setDisplayShowTitleEnabled(false);
                getActionBar().setCustomView(view);
            }

            if (BpcInformationActivity.class.isInstance(this) == false) {
                if (ntfTextView != null) {
                    ntfTextView.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            ntfTextView.setText("");
                            shootNotificationActivity();
                        }
                    });
                } else {
                    Log.d(TAG, "error, ntfTextView is null");
                }
            }

            showSlideToggle(leftClicker);

            right = findViewById(R.id.right);
            right.setOnClickListener(rightClicker);

            leftListLayout = findViewById(R.id.list_container);

            tryUpdateInitialDetect();
            
            initTitleNtfView();
            return true;
        }

        return false;
    }

    private ArrayList<MainItemInfo> mPluginInfo = new ArrayList<MainItemInfo>();

    private void getPluginItemInfo() {
        ArrayList<MainItemInfo> itemInfos = new ArrayList<MainItemInfo>();

        PackageManager pm = this.getPackageManager();
//            final Intent intent = new Intent(PLUGIN_ACTION);
//            intent.addCategory("android.intent.category.TEST_QIUPU");
//            List<ResolveInfo> plugin = pm.queryIntentActivities(intent, 0);
//            for (int i = 0, len = plugin.size(); i < len; i++) {
//                itemInfos.add(getMainItemForActivity(plugin.get(i).activityInfo));
//            }

        final MainItemInfo itemInfo = new MainItemInfo(
                getResources().getDrawable(R.drawable.ic_qiupu_launcher), getString(R.string.home_application));
        itemInfo.mComponent = IntentUtil.getAppBoxComponentName();
        itemInfos.add(itemInfo);

        final Intent launcherIntent = new Intent(PLUGIN_ACTION);
        launcherIntent.addCategory(Intent.CATEGORY_TEST);
        List<ResolveInfo> pluginActivity = pm.queryIntentActivities(launcherIntent, 0);
        final int size = pluginActivity.size();
        for (int i = 0; i < size; ++i) {
            if (IntentUtil.isAppBoxActivity(pluginActivity.get(i).activityInfo.name)) {
                continue;
            }
            itemInfos.add(getMainItemForActivity(pluginActivity.get(i).activityInfo));
        }
        mPluginInfo = itemInfos;
    }

    private final static String PLUGIN_ACTION = "com.borqs.bpc.action.APP_PLUGIN";

    private MainItemInfo getMainItemForActivity(ActivityInfo info) {
        PackageManager pm = this.getPackageManager();
        ComponentName componentName = new ComponentName(info.packageName, info.name);
        Drawable icon = null;
        if (IntentUtil.isAppBoxActivity(info.name)) {
            icon = getResources().getDrawable(R.drawable.main_allapp);
        } else {
            try {
                icon = pm.getActivityIcon(componentName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (null == icon) {
                try {
                    icon = pm.getApplicationIcon(info.packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        CharSequence label = info.loadLabel(pm);
        if (TextUtils.isEmpty(label)) {
            label = pm.getApplicationLabel(info.applicationInfo);
        }

        final MainItemInfo itemInfo = new MainItemInfo(icon, label.toString());
        itemInfo.mComponent = componentName;

        return itemInfo;
    }

    private LeftMenuAdapter leftMenuAdapter;

    private LeftMenuListView setLeftMenuList(View view) {
        LeftMenuListView leftMenuList = (LeftMenuListView) view.findViewById(R.id.left_memu_list);
        mLeftNavigationCallBack = new WeakReference<LeftNavigationCallBack>(leftMenuList);
//        leftMenuList.setUpdateNotificationListener(this);
        leftMenuList.setCloseLisner(this);
//            setLeftMenuPosition();
        leftMenuList.addHeaderView(initHeaderView());
        leftMenuAdapter = new LeftMenuAdapter(this, getPosition(), mPluginInfo);
        leftMenuList.setAdapter(leftMenuAdapter);
        leftMenuList.setOnItemClickListener(listener);
        return leftMenuList;
    }

    private void setListAdapterForDynamic() {
        if (leftMenuListView != null && isStreamIndex()) {
            getPluginItemInfo();
            leftMenuListView.setAdapter(new LeftMenuAdapter(this, getPosition(), mPluginInfo));
            setHeaderViewUI();
        }
    }

    ImageView icon;
    TextView text;

    private View initHeaderView() {
        LayoutInflater factory = LayoutInflater.from(this);
        View convertView = factory.inflate(R.layout.left_menu_header_view, null);
        if (getPosition() == 0) {
            convertView.setBackgroundResource(R.color.left_menu_press_background);
        } else {
            convertView.setBackgroundResource(0);
        }
        icon = (ImageView) convertView.findViewById(R.id.item_icon);
        text = (TextView) convertView.findViewById(R.id.item_text);
        setHeaderViewUI();
        return convertView;
    }

    private void setHeaderViewUI() {
        if (icon != null && text != null) {
            icon.setImageDrawable(FileUtils.createProfileIcon(this, orm.getUserImageUrl(getSaveUid())));
            text.setText(getUserNickname());
        }
    }

    private void shootPluginApp(final ComponentName component) {
        IntentUtil.startComponent(this, component);
    }

    //        public static int mPosition = -1;
//        public static String mTitle = "";
//        private int mRequestCount = 0;
    private int getPosition() {
        return LeftMenuMapping.getIndex(this.getClass().getName());
    }

    private boolean isStreamIndex() {
        return BpcPostsNewActivity.class.isInstance(this);
    }

    private AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (LeftMenuItemView.class.isInstance(view)) {
                LeftMenuItemView leftMenuItemView = (LeftMenuItemView) view;
                IconListAdapter.IconListItem itemData = leftMenuItemView.getItemData();

                if (itemData.component != null) {
                    shootPluginApp(itemData.component);
//                        closeLeftMenu();
                    needCloseNavigationMenuOrFinish();
                } else {
                    setClickListener(itemData);
                    setAnimaitonAndPosition();
                }
            } else {
                boolean clickSameItem = setAnimationInNav(LeftMenuMapping.TYPE_UserProfileFragmentActivity);
                if (clickSameItem) {
                    return;
                }
                IntentUtil.startProfileActivity(SlidingMenuOverlayActivity.this, getSaveUid(),
                        getUserNickname());
                setAnimaitonAndPosition();
                needCloseNavigationMenuOrFinish();
            }
        }

        ;
    };

    private boolean setAnimationInNav(int index) {
        if (index == getPosition()) {
            handleLeftMenu(false, true);
            return true;
        } else {
            handleLeftMenu(false, false);
            return false;
        }
    }

    private void setClickListener(IconListAdapter.IconListItem itemData) {
        int index = itemData.getIndex() + 1; // the first item was treat as listview header
        boolean clickSameItem = setAnimationInNav(index);
        if (clickSameItem) {
            return;
        }

        if (index == LeftMenuMapping.TYPE_BpcPostsNewActivity) {
            final UserCircle circle = QiupuApplication.mTopOrganizationId;
            if (circle == QiupuApplication.VIEW_MODE_PERSONAL) {
                IntentUtil.startStream(SlidingMenuOverlayActivity.this);
            }  else {
                IntentUtil.gotoOrganisationHome(SlidingMenuOverlayActivity.this,
                        CircleUtils.getLocalCircleName(SlidingMenuOverlayActivity.this, circle.circleid, circle.name),
                        circle.circleid);
            }
//        } else if (index == LeftMenuMapping.TYPE_BpcInformationActivity) {
//            shootNotificationActivity();
        } else if (index == LeftMenuMapping.TYPE_FriendsFragmentActivity) {
        	IntentUtil.startFriendsFragmentActivity(SlidingMenuOverlayActivity.this, getSaveUid(),
        			getUserNickname());
        } else if (index == LeftMenuMapping.TYPE_BpcFriendsFragmentActivity) {
            IntentUtil.startFriendsCircleActivity(SlidingMenuOverlayActivity.this, getSaveUid(),
                    getUserNickname());
//        } else if (index == LeftMenuMapping.TYPE_BpcPageActivity) {
//        	IntentUtil.startPageListActivity(SlidingMenuOverlayActivity.this);
        } else if (index == LeftMenuMapping.TYPE_EventListActivity) {
            Intent intent = new Intent(SlidingMenuOverlayActivity.this, EventListActivity.class);
            startActivity(intent);
//        } else if (index == LeftMenuMapping.TYPE_CompanyListActivity) {
//            Intent intent = new Intent(this, CompanyListActivity.class);
//            startActivity(intent);
        } else if (index == LeftMenuMapping.TYPE_PollListActivity) {
            Intent intent = new Intent(this, PollListActivity.class);
            startActivity(intent);
        } else if (index == LeftMenuMapping.TYPE_BpcAddFriendsActivity) {
            IntentUtil.startBpcFriendsActivity(SlidingMenuOverlayActivity.this);
//            } else if (index == LeftMenuMapping.TYPE_) {
//                IntentUtil.startExchangeVCardActivity(BasicNavigationActivity.this, LeftMenuListView.getExchangeRequestCount());
//            } else if (getString(R.string.application_recomment).equals(index)) {
//                IntentUtil.startApplicationBoxActivity(BasicNavigationActivity.this);
//            } else if (index == LeftMenuMapping.TYPE_BpcSettingsActivity) {
//            	gotoSettingActivity();
        } else if (index == LeftMenuMapping.TYPE_AlbumActivity) {
            IntentUtil.startAlbumIntent(this, getSaveUid(), getUserNickname(), true);
        } else {
            Log.d(TAG, "setClickListener, error, no such type in navigation.");
        }

        String title = itemData.getTitle();
        if (title == null) {
            title = "";
        }
        if (getString(R.string.application_recomment).equals(title)) {
            closeLeftMenu();
        } else if (getString(R.string.options).equals(title)) {
            gotoSettingActivity();
            closeLeftMenu();
        } else {
            needCloseNavigationMenuOrFinish();
        }
    }

    private void needCloseNavigationMenuOrFinish() {
        if (isPersistActivity()) {
            closeLeftMenu();
        } else {
            finish();
        }
    }

    @Override
    protected void onPrepareTitleDropDown() {
        if (menuOut) {
            handleLeftMenu(false, true);
        }
    }

    private void closeLeftMenu() {
        menuOut = !menuOut;
        if (!menuOut) {
            leftMenu.setVisibility(View.GONE);
        }
    }

    private void setAnimaitonAndPosition() {
        overridePendingTransition(R.anim.push_activity_left_in, R.anim.push_activity_left_out);
    }

    private boolean longpress = false;

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode && !menuOut) {
            handleLeftMenu(false, true);
            longpress = true;
            return true;
        }

        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            if (longpress) {
                longpress = false;
                return true;
            }

            // we don't care click top slide button case.
            if (menuOut) {
                handleLeftMenu(false, true);
                return true;
            }

//                if (supportLeftNavigation) {
//                    View leftMenuList = findViewById(R.id.left_memu_list);
//                    if (leftMenuList != null) {
//                        if (!menuOut) {
//                            lastUpKey = event;
//                            handleLeftMenu(false, true);
//                            return true;
//                        }
//                    }
//                }

            if ((fromtab && fromHome) || supportLeftNavigation) {
                if (lastUpKey != null
                        && lastUpKey.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    long span = event.getEventTime() - lastUpKey.getEventTime();
                    if (span < 5 * QiupuConfig.A_SECOND) {
                        if (preEscapeActivity()) {
                            return super.onKeyUp(keyCode, event);
                        } else {
                            return true;
                        }
                    } else {
                        lastUpKey = event;
//                            super.tryShowMoreBackKeyClick();
//                            return true;
                    }
                } else {
                    lastUpKey = event;
//                        super.tryShowMoreBackKeyClick();
//                        return true;
                }
            } else if (!preEscapeActivity()) {
                return true;
            }

//                // we don't care click top slide button case.
//                if (menuOut) {
//                    handleLeftMenu(false, true);
//                    return true;
//                }
        }

        lastUpKey = event;
        return super.onKeyUp(keyCode, event);
    }

//    @Override
//    public void updateNotificationIcon(int count) {
//        ImageView slideToggle = (ImageView) findViewById(R.id.img_slide);
//        if (count > 0) {
//            slideToggle.setImageResource(R.drawable.navbar_icon_launcher_new);
//        } else {
//            slideToggle.setImageResource(R.drawable.navbar_icon_launcher);
//        }
//    }

//    @Override
//    public void updateRequestCount(int count) {
////            mRequestCount = count;
//    }


    /**
     * Detect if the activity is a persist one while navigating to others.
     *
     * @return true false to finish itself while navigating to other activity from navigation panel.
     */
    protected boolean isPersistActivity() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOperator = new NotificationOperator(this);
//        QiupuHelper.registerNotificationListener(getClass().getName(), this);
        InformationUtils.registerNotificationListener(getClass().getName(), this);
        RequestsService.regiestRequestListener(getClass().getName(), this);
        QiupuHelper.registerRequestRefreshListner(getClass().getName(), this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//            setLeftMenuPosition();
        setListAdapterForDynamic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InformationUtils.unregisterNotificationListener(getClass().getName());
//        QiupuHelper.unregisterNotificationListener(getClass().getName());
        RequestsService.unRegiestRequestListener(getClass().getName());
        if(mRequestAdapter != null) {
        	mRequestAdapter.setRequestActionListener(null);
        }
        QiupuHelper.unregisterRequestRefreshListner(getClass().getName());
    }

//    @Override
//    public void updateNotificationCountUI(final int count) {
//        mBasicHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                ImageView slideToggle = (ImageView) findViewById(R.id.img_slide);
//                if (count <= 0) {
//                    slideToggle.setImageResource(R.drawable.navbar_icon_launcher);
//                    if (leftMenuAdapter != null) {
//                        ArrayList<IconListAdapter.IconListItem> items = leftMenuAdapter.getListData();
//                        if (items != null) {
//                            items.get(LeftMenuMapping.TYPE_BpcInformationActivity - 1).setCount(count);
//                            leftMenuAdapter.notifyDataSetChanged();
//                        }
//                    } else {
//                        // TODO: if error, no chance to fresh ui.
//                        Log.d(TAG, "leftMenuAdapter is null");
//                    }
//                } else {
//                    slideToggle.setImageResource(R.drawable.navbar_icon_launcher_new);
//                }
//            }
//        });
//    }
    
    private NtfQuickAction mRequestQuickDialog;
    private NtfQuickAction mToMeQuickDialog;
    private NtfQuickAction mOtherNtfQuickDialog;
    private InformationAdapter mInfomationAdapter;
    private RequestsAdapter     mRequestAdapter;
    private NotificationOperator mOperator;
    
    protected void initTitleNtfView() {
    	Log.d(TAG, "initHeadViews");
        
        refreshRequestNtf();
        refreshToMeNtf();
        refreshOtherNtf();
    }
    
    private long getSceneId () {
    	final String sId = QiupuORM.getSettingValue(this, QiupuORM.HOME_ACTIVITY_ID);
    	long sceneId = -1;
    	if(TextUtils.isEmpty(sId) == false) {
    		try {
    			sceneId = Long.parseLong(sId);
    		} catch (Exception e) {
    			Log.d(TAG, "homeid is null");
    		}
    	}
    	
    	return sceneId;
    }
    private void refreshRequestNtf() {
    	final long sceneId = getSceneId();
    	ImageView requestView = (ImageView) findViewById(R.id.head_request);
    	ArrayList<Requests> requeslist = orm.buildRequestList("", sceneId);
    	if(requeslist != null && requeslist.size() > 0) {
    		Drawable requestIcon = getResources().getDrawable(R.drawable.request_icon_light);
    		requestView.setImageBitmap(generatorTargetCountIcon(requestIcon, requeslist.size()));
    	}else {
    		requestView.setImageResource(R.drawable.request_icon);
    	}
    	requestView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mRequestQuickDialog == null ) {
					mRequestQuickDialog = new NtfQuickAction(SlidingMenuOverlayActivity.this,NtfQuickAction.VERTICAL, Notification.ntf_type_request);
					mRequestAdapter = new RequestsAdapter(SlidingMenuOverlayActivity.this);
					mRequestAdapter.setRequestActionListener(SlidingMenuOverlayActivity.this);
					mRequestQuickDialog.setListAdapter(mRequestAdapter);
					mRequestAdapter.alterRequests(orm.buildRequestList("", sceneId));
					mRequestQuickDialog.setOnDismissListener(SlidingMenuOverlayActivity.this);
					mRequestQuickDialog.show(v);
				}else {
					if(mRequestAdapter == null) {
						mRequestAdapter = new RequestsAdapter(SlidingMenuOverlayActivity.this);
					}
					mRequestAdapter.alterRequests(orm.buildRequestList("", sceneId));
					mRequestQuickDialog.setListAdapter(mRequestAdapter);
					mRequestQuickDialog.show(v);
				}
			}
		});
    }

    private void refreshToMeNtf() {
    	ImageView tomeView = (ImageView) findViewById(R.id.head_send_me);
    	int count = mOperator.loadUnReadToMeNtfCount();
    	if(count > 0) {
    		Drawable tomeicon = getResources().getDrawable(R.drawable.letter_icon_light);
    		tomeView.setImageBitmap(generatorTargetCountIcon(tomeicon, count));
    	}else {
    		tomeView.setImageResource(R.drawable.letter_icon);
    	}
    	tomeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mToMeQuickDialog == null ) {
					mToMeQuickDialog = new NtfQuickAction(SlidingMenuOverlayActivity.this,NtfQuickAction.VERTICAL, Notification.ntf_type_tome);
					mInfomationAdapter = new InformationAdapter(SlidingMenuOverlayActivity.this, SlidingMenuOverlayActivity.this);
					mToMeQuickDialog.setListAdapter(mInfomationAdapter);
					mToMeQuickDialog.setListItemClickListener(infomationItemClickListenter);
					mInfomationAdapter.alterDataList(mOperator.loadNtfToMe(""));
					mToMeQuickDialog.setOnDismissListener(SlidingMenuOverlayActivity.this);
					mToMeQuickDialog.show(v);
					//refresh title ToMe ntf icon
//					onNotificationDownloadCallBack(true, 0);
					
				}else {
					if(mInfomationAdapter == null) {
						mInfomationAdapter = new InformationAdapter(SlidingMenuOverlayActivity.this);
					}
					mInfomationAdapter.alterDataList(mOperator.loadNtfToMe(""));
					mToMeQuickDialog.setListAdapter(mInfomationAdapter);
					mToMeQuickDialog.show(v);
				}
			}
		});
    }
    private void refreshOtherNtf() {
    	ImageView otherNtfView = (ImageView) findViewById(R.id.head_ntf);
    	int count = mOperator.loadUnReadOtherNtfCount();
    	if(count > 0) {
    		Drawable otherntfIcon = getResources().getDrawable(R.drawable.notice_icon_light);
    		otherNtfView.setImageBitmap(generatorTargetCountIcon(otherntfIcon, count));
    	}else {
    		otherNtfView.setImageResource(R.drawable.notice_icon);
    	}
    	otherNtfView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// show quick dialog
				if(mOtherNtfQuickDialog == null ) {
					mOtherNtfQuickDialog = new NtfQuickAction(SlidingMenuOverlayActivity.this,NtfQuickAction.VERTICAL, Notification.ntf_type_other);
					mInfomationAdapter = new InformationAdapter(SlidingMenuOverlayActivity.this, SlidingMenuOverlayActivity.this);
					mOtherNtfQuickDialog.setListAdapter(mInfomationAdapter);
					mOtherNtfQuickDialog.setListItemClickListener(infomationItemClickListenter);
					mInfomationAdapter.alterDataList(mOperator.loadNtfWithOutToMe(""));
					mOtherNtfQuickDialog.setOnDismissListener(SlidingMenuOverlayActivity.this);
					mOtherNtfQuickDialog.show(v);
					
					//refresh title other ntf icon
//					onNotificationDownloadCallBack(false, 0);
					
				}else {
					if(mInfomationAdapter == null) {
						mInfomationAdapter = new InformationAdapter(SlidingMenuOverlayActivity.this);
					}
					mInfomationAdapter.alterDataList(mOperator.loadNtfWithOutToMe(""));
					mOtherNtfQuickDialog.setListAdapter(mInfomationAdapter);
					mOtherNtfQuickDialog.show(v);
				}
			}
		});
    }
    
    private Bitmap generatorTargetCountIcon(Drawable base, int count){ 
    	int iconWidth = base.getIntrinsicWidth();
		int iconHeight = base.getIntrinsicHeight();
    	
        Bitmap targetIcon=Bitmap.createBitmap(iconWidth, iconHeight, Config.ARGB_8888);  
        Canvas canvas=new Canvas(targetIcon);  
          
        // draw target icon
        Bitmap baseb = ((BitmapDrawable)base).getBitmap();
		canvas.drawBitmap(baseb, 0, 0, new Paint());
		
        // draw count bg
        Paint countbgPaint=new Paint();  
        countbgPaint.setDither(true);
        countbgPaint.setFilterBitmap(true);
        Bitmap countbgbt;
        if(count < 100) {
        	Drawable tmpDrawable = getResources().getDrawable(R.drawable.tips);
        	countbgbt = ((BitmapDrawable)tmpDrawable).getBitmap();
			int bgWidth = countbgbt.getWidth();
			int bgLeft = iconWidth - bgWidth;
			canvas.drawBitmap(countbgbt, bgLeft, 0, new Paint());
			
			Paint p = new Paint(Paint.ANTI_ALIAS_FLAG
					| Paint.DEV_KERN_TEXT_FLAG);
			p.setColor(Color.WHITE);
			p.setTextSize(getResources().getDimension(R.dimen.noti_count_text_size));
			p.setTypeface(Typeface.DEFAULT_BOLD);
			float countWidth = p.measureText(String.valueOf(count));
			float countHeight = (float)(Math.ceil(p.ascent())
					+  Math.ceil(p.descent()));

			float x = (bgWidth - countWidth) / 2 + bgLeft;
			float y = (countbgbt.getHeight() - countHeight) / 2;
			canvas.drawText(String.valueOf(count), x, y, p);
        	
        }else {
        	Drawable tmpDrawable = getResources().getDrawable(R.drawable.ntf_toast_icon);
        	countbgbt = ((BitmapDrawable)tmpDrawable).getBitmap();
			canvas.drawBitmap(countbgbt, iconWidth - countbgbt.getWidth(), 0, new Paint());
        }
        return targetIcon;  
    }  
    
    OnItemClickListener infomationItemClickListenter = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(InformationItemView.class.isInstance(view)) {
				InformationItemView informationview = (InformationItemView) view;
				InformationBase infor = informationview.getItem();
				if(infor != null) {
					if(!infor.read) {
						informationview.reverContentText(infor.read);
						mOperator.updateReadStatus(infor.id, true);
						InformationReadCache.ReadStreamCache.cacheUnReadNtfIdsWithoutDb(infor.id);
						refreshHeadNtfIcon();
					}
				}
//				closeNtfPopupWindow();
				forwardInformation(informationview.getItem());
			}
		}
	};
	
	private void refreshHeadTomeIcon(int count) {
		ImageView tomeView = (ImageView) findViewById(R.id.head_send_me);
		if(tomeView == null) {
			Log.d(TAG, "find head ntf icon is null");
			return;
		}
    	if(count > 0) {
    		Drawable tomeicon = getResources().getDrawable(R.drawable.letter_icon_light);
    		tomeView.setImageBitmap(generatorTargetCountIcon(tomeicon, count));
    	}else {
    		tomeView.setImageResource(R.drawable.letter_icon);
    	}
	}
	
	private void refreshHeadotherIcon(int count) {
		ImageView otherNtfView = (ImageView) findViewById(R.id.head_ntf);
		if(otherNtfView == null) {
			Log.d(TAG, "find head ntf icon is null");
			return;
		}
    	if(count > 0) {
    		Drawable otherntfIcon = getResources().getDrawable(R.drawable.notice_icon_light);
    		otherNtfView.setImageBitmap(generatorTargetCountIcon(otherntfIcon, count));
    	}else {
    		otherNtfView.setImageResource(R.drawable.notice_icon);
    	}
	}
	
	private void refreshHeadNtfIcon() {
		if(mToMeQuickDialog != null && mToMeQuickDialog.isShow()) {
			refreshHeadTomeIcon(mOperator.loadUnReadToMeNtfCount());
		}else if(mOtherNtfQuickDialog != null && mOtherNtfQuickDialog.isShow()) {
			refreshHeadotherIcon(mOperator.loadUnReadOtherNtfCount());
		}
	}
	
	private void forwardInformation(InformationBase msg) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		if (TextUtils.isEmpty(msg.uri)) {
			return;
		} else {
			intent.setData(Uri.parse(msg.uri));
		}

		if (BpcApiUtils.isActivityReadyForIntent(this, intent)) {
			intent.putExtra("MSG_ID", msg.id);
			intent.putExtra("DATA", msg.data);
			intent.putExtra("SENDER_ID", msg.senderId);
			intent.putExtra("WHEN", msg.lastModified);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
		}
	}
	

	@Override
	public void onNotificationDownloadCallBack(final boolean isToMe, final int count) {
		Log.d(TAG, "onNotificationDownLoadCallback: isTome: " + isToMe + " count: " + count );
		mBasicHandler.post(new Runnable() {
			@Override
			public void run() {
				if(isToMe) {
					refreshHeadTomeIcon(count);
					// refresh to me ListView
					if(mToMeQuickDialog != null) {
						mToMeQuickDialog.OnRefreshComplete();
						if(mToMeQuickDialog.isShow()) {
							if(mInfomationAdapter == null) {
								mInfomationAdapter = new InformationAdapter(SlidingMenuOverlayActivity.this);
							}
							mInfomationAdapter.alterDataList(mOperator.loadNtfToMe(""));
							mToMeQuickDialog.setListAdapter(mInfomationAdapter);
						}
					}
				}else {
					refreshHeadotherIcon(count);
					// refresh Other ListView
					if(mOtherNtfQuickDialog != null){
						mOtherNtfQuickDialog.OnRefreshComplete();
						if(mOtherNtfQuickDialog.isShow()) {
							if(mInfomationAdapter == null) {
								mInfomationAdapter = new InformationAdapter(SlidingMenuOverlayActivity.this);
							}
							mInfomationAdapter.alterDataList(mOperator.loadNtfWithOutToMe(""));
							mOtherNtfQuickDialog.setListAdapter(mInfomationAdapter);
						}
					} 
				}
			}
		});
	}
	
	private void setRead(String unReadIds) {
		new BatchReadTask(unReadIds).execute();
	}
	
	private class BatchReadTask extends UserTask<Long, Void, Void> {
		
		private String mUnReadIds;
		private String mCacheUnReadIds;
		
		public BatchReadTask(String unReadIds) {
			mUnReadIds = unReadIds;
			mCacheUnReadIds = InformationReadCache.ReadStreamCache.getCacheUnReadNtfIds();
		}
		
		@Override
		public Void doInBackground(Long... params) {
			if(QiupuConfig.DBLOGD)Log.d(TAG, "unread ids : " + mUnReadIds + " " + mCacheUnReadIds);
			StringBuilder unreads = new StringBuilder();
			if(StringUtil.isValidString(mUnReadIds)) {
				unreads.append(mUnReadIds);
			}
			if(StringUtil.isValidString(mCacheUnReadIds)) {
				if(unreads.length() > 0) {
					unreads.append(",");
				}
				unreads.append(mCacheUnReadIds);
			}
			if(unreads.length() > 0) {
				boolean res = InformationUtils.setReadStatus(SlidingMenuOverlayActivity.this, unreads.toString());
				if(res) {
					InformationReadCache.ReadStreamCache.removeNtfCacheWithIds(mCacheUnReadIds);
				}				
			}
			return null;
		}

		@Override
		public void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}
	
	private Requests mRequest = new Requests();
	private int      mType;
	
	@Override
    public void acceptRequest(Requests request, int type) {
        Log.d(TAG, "acceptRequest() type = " + type);
        mRequest = request;
        mType = type;
        if (type == Requests.REQUEST_TYPE_EXCHANGE_VCARD) {
            // set to my circles 'privacy circle/default circle'
            setCircle(mRequest.user.uid,
                        CircleUtils.getDefaultCircleId(),
                        CircleUtils.getDefaultCircleName(getResources()));
        } else if (type == Requests.REQUEST_TYPE_CHANGE_PHONE_1) {
            gotoUpdateContactInfo(QiupuConfig.TYPE_PHONE1, mRequest.data);
        } else if (type == Requests.REQUEST_TYPE_CHANGE_PHONE_2) {
            gotoUpdateContactInfo(QiupuConfig.TYPE_PHONE2, mRequest.data);
        } else if (type == Requests.REQUEST_TYPE_CHANGE_PHONE_3) {
            gotoUpdateContactInfo(QiupuConfig.TYPE_PHONE3, mRequest.data);
        } else if (type == Requests.REQUEST_TYPE_CHANGE_EMAIL_1) {
            gotoUpdateContactInfo(QiupuConfig.TYPE_EMAIL1, mRequest.data);
        } else if (type == Requests.REQUEST_TYPE_CHANGE_EMAIL_2) {
            gotoUpdateContactInfo(QiupuConfig.TYPE_EMAIL2, mRequest.data);
        } else if (type == Requests.REQUEST_TYPE_CHANGE_EMAIL_3) {
            gotoUpdateContactInfo(QiupuConfig.TYPE_EMAIL3, mRequest.data);
        } else if(type == Requests.REQUEST_EVENT_INVITE || type == Requests.REQUEST_EVENT_JOIN
        		|| type == Requests.REQUEST_PUBLIC_CIRCLE_INVITE
        		|| type == Requests.REQUEST_PUBLIC_CIRCLE_JOIN) {
        	doneRequests(mRequest.rid, mRequest.type, mRequest.data, true);
        }
    }
	
	@Override
    public void refuseRequest(Requests request) {
        mRequest = request;
        if(request.type == Requests.REQUEST_EVENT_INVITE || request.type == Requests.REQUEST_EVENT_JOIN
        		|| request.type == Requests.REQUEST_PUBLIC_CIRCLE_INVITE
        		|| request.type == Requests.REQUEST_PUBLIC_CIRCLE_JOIN) {
        	doneRequests(request.rid, request.type, request.data, false);
        }else {
        	doneRequests(request.rid);
        }
    }
	
	private void doneRequests(final String requestid) {
		doneRequests(requestid, -1, "", false);
	}
	
	private void doneRequests(final String requestid, final int type, final String data, final boolean isAccept) {
		if (!AccountServiceUtils.isAccountReady()) {
			Log.d(TAG, "getRequests, mAccount is null exit");
			return;
		}
		
//		begin();
		asyncQiupu.doneRequests(AccountServiceUtils.getSessionID(), requestid, type, data, isAccept,
				new TwitterAdapter() {
			public void doneRequests(boolean suc) {
				Log.d(TAG, "finish doneRequests = " + suc);
				orm.deleteDoneRequest(requestid, -1);
				updateRequestCountUI();
//				Message mds = mHandler.obtainMessage(REQUEST_DONE_END);
//				mds.getData().putBoolean("RESULT", suc);
//				Log.d(TAG, "requestid = " + requestid);
//				mds.getData().putString("request_id", requestid);
//				mHandler.sendMessage(mds);
				
			}
			
			public void onException(TwitterException ex,
					TwitterMethod method) {
				TwitterExceptionUtils.printException(TAG,
						"doneRequests, server exception:", ex, method);
				
//				Message mds = mHandler.obtainMessage(REQUEST_DONE_END);
//				mds.getData().putBoolean("RESULT", false);
//				mHandler.sendMessage(mds);
			}
		});
	}
	 
	private void updateRequestCountUI() {
        synchronized (QiupuHelper.requestrefreshListener) {
        	Log.d(TAG, "updateRequestCountUI: " + QiupuHelper.requestrefreshListener.size());
            Set<String> set = QiupuHelper.requestrefreshListener.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                WeakReference<RequestRefreshListner> ref = QiupuHelper.requestrefreshListener.get(key);
                if (ref != null && ref.get() != null) {
                    ref.get().refreshRequestUi();
                }
            }
        }
    }
	
	@Override
	public void refreshRequestUi() {
		Log.d(TAG, "refreshRequestUi: " );
		mBasicHandler.post(new Runnable() {
			@Override
			public void run() {
				if(mRequestQuickDialog != null) {
					ImageView requestView = (ImageView) findViewById(R.id.head_request);
					if(requestView == null) {
						return;
					}
					
			    	ArrayList<Requests> requeslist = orm.buildRequestList("", getSceneId());
			    	if(requeslist != null && requeslist.size() > 0) {
			    		Drawable requestIcon = getResources().getDrawable(R.drawable.request_icon_light);
			    		requestView.setImageBitmap(generatorTargetCountIcon(requestIcon, requeslist.size()));
			    	}else {
			    		requestView.setImageResource(R.drawable.request_icon);
			    	}
			    	
			    	if(mRequestAdapter == null) {
			    		mRequestAdapter = new RequestsAdapter(SlidingMenuOverlayActivity.this);
			    		mRequestQuickDialog.setListAdapter(mRequestAdapter);
			    	}
			    	mRequestAdapter.alterRequests(requeslist);
				}
			}
		});
	}

	private void gotoUpdateContactInfo(String col, String data) {
		
		Map<String, String> map = new LinkedHashMap<String, String>();
		initContactInfomap(map);
        HashMap<String, String> contactInfoMap = QiupuHelper.organizationContactMap(col,
                data, map);

        HashMap<String, String> infoMap = new HashMap<String, String>();
        // String value = JSONUtil.createContactInfoJSONObject(contactInfoMap);
        infoMap.put("contact_info", JSONUtil.createContactInfoJSONObject(contactInfoMap));
        updateUserInfo(infoMap);
    }
	
	private void initContactInfomap(Map<String, String> map) {
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
	
	@Override
	public void requestUpdated(final ArrayList<Requests> data) {
		if(QiupuConfig.LOGD)Log.d(TAG, "requestUpdated, requests count: " + (data != null ? data.size() : 0 ));
		mBasicHandler.post(new Runnable() {
			@Override
			public void run() {
				ImageView requestView = (ImageView) findViewById(R.id.head_request);
				if(requestView == null) {
					return ;
				}
		    	if(data != null && data.size() > 0) {
		    		Drawable requestIcon = getResources().getDrawable(R.drawable.request_icon_light);
		    		requestView.setImageBitmap(generatorTargetCountIcon(requestIcon, data.size()));
		    	}else {
		    		requestView.setImageResource(R.drawable.request_icon);
		    	}
		    	
		    	//refresh request list
		    	if(mRequestQuickDialog != null) {
		    		mRequestQuickDialog.OnRefreshComplete();
		    		if(mRequestAdapter == null) {
		    			mRequestAdapter = new RequestsAdapter(SlidingMenuOverlayActivity.this);
		    		}
		    		mRequestAdapter.alterRequests(data);
		    		mRequestQuickDialog.setListAdapter(mRequestAdapter);
		    	}
			}
		});
	}
	
	@Override
	protected void doActionFriendEndCallBack(Message msg) {
		if(mRequest != null) {
			orm.deleteDoneRequest(mRequest.rid, -1);
		}
		updateRequestCountUI();
	}
	
	@Override
	public void onDismiss(boolean onTop, int ntftype) {
		if(QiupuConfig.LOGD)Log.d(TAG, "onDismiss: " + onTop + " " + ntftype);
		if(ntftype == Notification.ntf_type_tome) {
			refreshHeadTomeIcon(0);
			if(mToMeQuickDialog != null) {
				mToMeQuickDialog.OnRefreshComplete();
			}
//			onNotificationDownloadCallBack(true, 0);
			//setRead to server
			setRead(mOperator.loadUnReadNtfToMeString());
			//refresh all local other ntf to read status
			mOperator.updateReadStatusWithType(true, true);
			
			
		}else if(ntftype == Notification.ntf_type_other) {
			refreshHeadotherIcon(0);
			if(mOtherNtfQuickDialog != null) {
				mOtherNtfQuickDialog.OnRefreshComplete();
			}
//			onNotificationDownloadCallBack(false, 0);
			//setRead to server
			setRead(mOperator.loadunReadNtfOtherString());			
			//refresh all local other ntf to read status
			mOperator.updateReadStatusWithType(false, true);
		}else if(ntftype == Notification.ntf_type_request) {
			if(mRequestQuickDialog != null) {
				mRequestQuickDialog.OnRefreshComplete();
			}
		}else {
			Log.d(TAG, "setPopupTitle: have not this ntf type. " + ntftype);
		}
	}
	
	private void closePopupWindow() {
		if(mToMeQuickDialog != null && mToMeQuickDialog.isShow()) {
			mToMeQuickDialog.dismiss();
		}
		if(mOtherNtfQuickDialog != null && mOtherNtfQuickDialog.isShow()) {
			mOtherNtfQuickDialog.dismiss();
		}
		if(mRequestQuickDialog != null && mRequestQuickDialog.isShow()) {
			mRequestQuickDialog.dismiss();
		}
	}
	
	@Override
	public boolean isMoreItemHidden() {
		return false;
	}
	
	@Override
	public OnClickListener getMoreItemClickListener() {
		return seeAllInformationListener;
	}
	
	@Override
	public int getMoreItemCaptionId() {
		return  R.string.ntf_see_all;
	}
	
	View.OnClickListener seeAllInformationListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mToMeQuickDialog != null && mToMeQuickDialog.isShow()) {
				Intent intent = new Intent(SlidingMenuOverlayActivity.this, InformationListActivity.class);
				intent.putExtra(InformationConstant.NOTIFICATION_INTENT_PARAM_ISTOME, true);
				startActivity(intent);
				mToMeQuickDialog.dismiss();
			}else if(mOtherNtfQuickDialog != null && mOtherNtfQuickDialog.isShow()) {
				Intent intent = new Intent(SlidingMenuOverlayActivity.this, InformationListActivity.class);
				intent.putExtra(InformationConstant.NOTIFICATION_INTENT_PARAM_ISTOME, false);
				startActivity(intent);
				mOtherNtfQuickDialog.dismiss();
			}
		}
	};
	
	public void onConfigurationChanged(android.content.res.Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		closePopupWindow();
	};
}

