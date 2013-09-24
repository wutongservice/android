package com.borqs.qiupu.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.borqs.common.adapter.IconListAdapter;
import com.borqs.common.adapter.LeftMenuAdapter;
import com.borqs.common.listener.NotifyActionListener;
import com.borqs.common.listener.UpdateNotificationListener;
import com.borqs.common.model.MainItemInfo;
import com.borqs.common.util.FileUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.LeftMenuItemView;
import com.borqs.common.view.LeftMenuListView;
import com.borqs.common.view.LeftNavigationCallBack;
import com.borqs.common.view.MenuHorizontalScrollView;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.ui.bpc.BpcInformationActivity;
import com.borqs.qiupu.ui.bpc.BpcPostsNewActivity;
import com.borqs.qiupu.ui.bpc.EventListActivity;
import com.borqs.qiupu.ui.bpc.PollListActivity;
import com.borqs.qiupu.ui.company.CompanyListActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.LeftMenuMapping;
import twitter4j.UserCircle;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-12-20
 * Time: 上午11:35
 * To change this template use File | Settings | File Templates.
 */

public abstract class SlidingMenuActivity extends BasicActivity
        implements NotifyActionListener, Animation.AnimationListener, LeftMenuListView.LeftNavigationListener, UpdateNotificationListener {
    private static final String TAG = "Qiupu.SlidingMenuActivity";
    // change for to add list action menu
    private static final boolean FORCE_DISABLE_CUSTOMIZED_TITLE = false;

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
    private MenuHorizontalScrollView scrollView;
    private ImageView menuBtn;

    class LeftMenuClickListener implements View.OnClickListener {
        public LeftMenuClickListener() {
            super();
        }

        @Override
        public void onClick(View v) {
//            Log.d(TAG, "LeftMenuClickListener onClick() menuOut = " + menuOut);
            setScrollClickAnimation(false);
        }
    }

    private void setScrollClickAnimation(boolean withAnimation) {
        Log.d(TAG, "setScrollClickAnimation() menuOut = " + menuOut);

//        Animation anim = null;
        if (menuOut) {
//            anim = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
        } else {
            Log.d(TAG, "setScrollClickAnimation() openLeftUI()============== ");
            leftMenuListView.openLeftUI();
//            leftListLayout.setVisibility(View.VISIBLE);
//            setListAdapterForDynamic();
//            anim = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
        }
        menuOut = !menuOut;
        Log.d(TAG, "setScrollClickAnimation() end >>>>>> menuOut = " + menuOut);
//        anim.setAnimationListener(this);

        scrollView.clickMenuBtn(true, !menuOut);
        scrollView.setSmoothScrollingEnabled(true);
//        if (!menuOut) {
//            leftListLayout.setVisibility(View.GONE);
//        }
//        scrollView.startAnimation(anim);
    }

    protected View.OnClickListener leftClicker = new LeftMenuClickListener();

//    private void handleLeftMenu(boolean isShowAnimation) {
//        Animation anim = null;
//        menuOut = !menuOut;
//        Log.d(TAG, "handleLeftMenu() menuOut = " + menuOut);
//        if (leftMenu == null) {
//            return;
//        }
//
//        Log.d(TAG, "handleLeftMenu() 111 menuOut = " + menuOut);
//        if (!menuOut) {
//            leftListLayout.setClickable(true);
//            leftMenu.setVisibility(View.VISIBLE);
//            leftMenuListView.openLeftUI();
//            //EnfoldmentView.foldEnfoldmentView();
//            if (isShowAnimation) {
//                anim = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
//            }
//        } else {
//            if (isShowAnimation) {
//                anim = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
//            }
//            leftListLayout.setClickable(false);
//        }
//        Log.d(TAG, "handleLeftMenu() 222 menuOut = " + menuOut);
//        if (isShowAnimation) {
//            anim.setAnimationListener(this);
//            leftMenu.startAnimation(anim);
//        }
//    }

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
        return R.layout.menu_scroll_view;
    }

    private View appSpan;
    @Override
    protected boolean inflateNavigatingContentView(int layoutResID) {
        getPluginItemInfo();
        LayoutInflater inflater = LayoutInflater.from(this);
        View p_layout = getWindow().getDecorView();

//        ViewGroup layout_app = (ViewGroup) p_layout.findViewById(R.id.top);
        leftMenu = (ViewGroup) p_layout.findViewById(R.id.menu);
        LinearLayout layout_app = new LinearLayout(this);
        layout_app.setOrientation(LinearLayout.VERTICAL);
        appSpan = inflater.inflate(layoutResID, layout_app, true);

        if (null != appSpan && null != appSpan.findViewById(R.id.titlebar_container)) {
            leftMenuListView = setLeftMenuList(p_layout);

            setTitleResource(appSpan, R.layout.title_bar_nav_base);
            initHeadViews(appSpan);

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

            showSlideToggle(appSpan, leftClicker);

            leftListLayout = findViewById(R.id.list_container);

            menuBtn = (ImageView)appSpan.findViewById(R.id.img_slide);
            
            initScrollView(getWindowManager().getDefaultDisplay().getWidth());

            tryUpdateInitialDetect();
            return true;
        }

        return false;
    }

    private void initScrollView(int width) {
        if (leftListLayout == null) {
            return;
        }

        leftListLayout.setVisibility(View.INVISIBLE);
        leftListLayout.setBackgroundResource(android.R.color.transparent);

        scrollView = (MenuHorizontalScrollView)findViewById(R.id.scrollView);
        View leftView = new View(this);
        leftView.setBackgroundColor(Color.TRANSPARENT);
        final View[] children = new View[]{leftView, appSpan};

        scrollView.initViews(children, new SizeCallBackForMenu(menuBtn), leftListLayout,
                new MenuHorizontalScrollView.MenuCallBack() {
                    @Override
                    public void onMenuSlide(boolean out) {
                        menuOut = out;
                        Log.d(TAG, "======initScrollView ---->>>> onMenuSlide() ======== menuOut = " + menuOut);
//                        handleLeftMenu(out);
                    }
                }, width, menuOut);
        scrollView.setMenuBtn(menuBtn);
        Log.d(TAG, "$$$$$$$$  initScrollView() menuOut = " + menuOut + "  $$$$$$$$");
//        if (menuOut) {
//            scrollView.clickMenuBtn(false, !menuOut);
//        }
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
        leftMenuList.setUpdateNotificationListener(this);
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
                 
//                boolean clickSameItem = setAnimationInNav(LeftMenuMapping.TYPE_BpcInformationActivity);
//                if (clickSameItem) {
//                    return;
//                }
                if (isSameItem(LeftMenuMapping.TYPE_UserProfileFragmentActivity)) {
                    return;
                }
                
                IntentUtil.startProfileActivity(SlidingMenuActivity.this, getSaveUid(),
                        getUserNickname());
                setAnimaitonAndPosition();
                needCloseNavigationMenuOrFinish();
            }
        }

        ;
    };

//    private boolean setAnimationInNav(int index) {
//        if (index == getPosition()) {
//            handleLeftMenu(true);
//            return true;
//        } else {
//            handleLeftMenu(false);
//            return false;
//        }
//    }

    private boolean isSameItem(int index) {
        boolean isSameItem = false;
        if (index == getPosition()) {
            setScrollClickAnimation(true);
            isSameItem = true;
        } else {
            isSameItem = false;
        }
        return isSameItem;
    }

    private void setClickListener(IconListAdapter.IconListItem itemData) {
        int index = itemData.getIndex() + 1; // the first item was treat as listview header
//        boolean clickSameItem = setAnimationInNav(index);
//        if (clickSameItem) {
//            return;
//        }

        if (isSameItem(index)) {
            return;
        }

//        Log.d(TAG, "============ setClickListener() 111 closeMenu menuOut = " + menuOut);

        if (index == LeftMenuMapping.TYPE_BpcPostsNewActivity) {
            final UserCircle circle = QiupuApplication.mTopOrganizationId;
            if (circle == QiupuApplication.VIEW_MODE_PERSONAL) {
                IntentUtil.startStream(SlidingMenuActivity.this);
            } else {
                IntentUtil.gotoOrganisationHome(SlidingMenuActivity.this,
                        CircleUtils.getLocalCircleName(SlidingMenuActivity.this, circle.circleid, circle.name),
                        circle.circleid);
            }
//        } else if (index == LeftMenuMapping.TYPE_BpcInformationActivity) {
//            shootNotificationActivity();
        } else if (index == LeftMenuMapping.TYPE_FriendsFragmentActivity) {
        	IntentUtil.startFriendsFragmentActivity(SlidingMenuActivity.this, getSaveUid(),
        			getUserNickname());
        } else if (index == LeftMenuMapping.TYPE_BpcFriendsFragmentActivity) {
            IntentUtil.startFriendsCircleActivity(SlidingMenuActivity.this, getSaveUid(),
                    getUserNickname());
//        } else if (index == LeftMenuMapping.TYPE_BpcPageActivity) {
//        	IntentUtil.startPageListActivity(SlidingMenuActivity.this);
        } else if (index == LeftMenuMapping.TYPE_EventListActivity) {
            Intent intent = new Intent(SlidingMenuActivity.this, EventListActivity.class);
            startActivity(intent);
//        } else if (index == LeftMenuMapping.TYPE_CompanyListActivity) {
//            Intent intent = new Intent(this, CompanyListActivity.class);
//            startActivity(intent);
        } else if (index == LeftMenuMapping.TYPE_PollListActivity) {
            Intent intent = new Intent(this, PollListActivity.class);
            startActivity(intent);
        } else if (index == LeftMenuMapping.TYPE_BpcAddFriendsActivity) {
            IntentUtil.startBpcFriendsActivity(SlidingMenuActivity.this);
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
        Log.d(TAG, "needclose() isPersistActivity() = " + isPersistActivity() + ", menuOut = " + menuOut);
        if (isPersistActivity()) {
            closeLeftMenu();
        } else {
            menuOut = !menuOut;
            Log.d(TAG, "needclose() menuOut = " + menuOut);
            finish();
        }
    }

    @Override
    protected void onPrepareTitleDropDown() {
        if (menuOut) {
//            handleLeftMenu(true);
            setScrollClickAnimation(true);
        }
    }

    private void closeLeftMenu() {
        Log.d(TAG, "closeLeftMenu() 1111 >>>>> menuOut : " + menuOut);
        menuOut = !menuOut;
//        if (!menuOut) {
//            leftMenu.setVisibility(View.GONE);
//        }
        Log.d(TAG, "closeLeftMenu() 2222 >>>>> menuOut : " + menuOut);
        if (!menuOut) {
            scrollView.clickMenuBtn(true, !menuOut);
        }
    }

    private void setAnimaitonAndPosition() {
        overridePendingTransition(R.anim.push_activity_left_in, R.anim.push_activity_left_out);
    }

    private boolean longpress = false;

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode && !menuOut) {
//            handleLeftMenu(true);
            setScrollClickAnimation(true);
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
            Log.d(TAG, "onKeyUp() menuOut = " + menuOut);
            if (menuOut) {
                setScrollClickAnimation(true);
//                handleLeftMenu(true);
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

    @Override
    public void updateNotificationIcon(int count) {
        if (count > 0) {
            menuBtn.setImageResource(R.drawable.navbar_icon_launcher_new);
        } else {
            menuBtn.setImageResource(R.drawable.navbar_icon_launcher);
        }
    }

    @Override
    public void updateRequestCount(int count) {
//            mRequestCount = count;
    }


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
        QiupuHelper.registerNotificationListener(getClass().getName(), this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//            setLeftMenuPosition();
        Log.d(TAG, "=============== onResume() menuOut = " + menuOut);
        setListAdapterForDynamic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        QiupuHelper.unregisterNotificationListener(getClass().getName());
    }

    @Override
    public void updateNotificationCountUI(final int count) {
//        mBasicHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (count <= 0) {
//                    if (menuBtn != null) {
//                        menuBtn.setImageResource(R.drawable.navbar_icon_launcher);
//                    }
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
//                    if (menuBtn != null) {
//                        menuBtn.setImageResource(R.drawable.navbar_icon_launcher_new);
//                    }
//                }
//            }
//        });
    }

//    private View.OnClickListener onClickListener = new View.OnClickListener(){
//   		@Override
//   		public void onClick(View arg0) {
//   			scrollView.clickMenuBtn();
//   		}
//   	};

    static class SizeCallBackForMenu implements MenuHorizontalScrollView.SizeCallBack {
        private int menuWidth;
        private ImageView menu;

        public SizeCallBackForMenu(ImageView menu) {
            super();
            this.menu = menu;
        }

        @Override
        public void onGlobalLayout() {
//            this.menuWidth = enlargeWidth;
            this.menuWidth = this.menu.getMeasuredWidth() + MenuHorizontalScrollView.ENLARGE_WIDTH;
            Log.d(TAG, "========= SizeCallBackForMenu onGlobalLayout() menuWidth = " + menuWidth);
        }

        @Override
        public void getViewSize(int idx, int width, int height, int[] dims) {
            dims[0] = width;
            dims[1] = height;

            if (idx != 1) {
                dims[0] = width - this.menuWidth;
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Display display  = getWindowManager().getDefaultDisplay();
//        menuOut = false;
        Log.d(TAG, "----- onConfigurationChanged() width = " + display.getWidth() 
                + ", height = " + display.getHeight() + ", menuOut = " + menuOut);
        initScrollView(display.getWidth());
        
    }
}

