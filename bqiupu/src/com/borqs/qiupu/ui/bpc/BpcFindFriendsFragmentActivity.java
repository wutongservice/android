package com.borqs.qiupu.ui.bpc;

import twitter4j.UserCircle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.LocationRequest;
import com.borqs.common.adapter.BpcFindFriendsAdapter;
import com.borqs.common.listener.ShakeActionListener;
import com.borqs.common.util.IntentUtil;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.BpcShakeExchangeCardFragment;
import com.borqs.qiupu.fragment.FindContactsFragment;
import com.borqs.qiupu.fragment.FixedTabsView;
import com.borqs.qiupu.fragment.NearByListFragment;
import com.borqs.qiupu.fragment.SuggestionListFragment;
import com.borqs.qiupu.ui.BasicNavigationActivity;
import com.borqs.qiupu.ui.bpc.UsersArrayListActivity.UsersArrayListFragment;
import com.borqs.qiupu.ui.circle.EditPublicCircleActivity;
import com.borqs.qiupu.util.LocationUtils;

public class BpcFindFriendsFragmentActivity extends BasicNavigationActivity implements
        ShakeActionListener,
        FindContactsFragment.SingleWizardListener {

    private final static String TAG = "BpcFindFriendsFragmentActivity";

    public static final boolean DISABLED = true;

    private int mConcernType = 0;

    private BpcFindFriendsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableLeftNav();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.viewpager_friends_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Log.d(TAG, "mConcernType: " + mConcernType);

        mAdapter = new BpcFindFriendsAdapter(getSupportFragmentManager(), this, getSaveUid(), asyncQiupu);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(mConcernType);

        FixedTabsView mIndicator = (FixedTabsView) findViewById(R.id.indicator);
        mIndicator.setViewPager(viewPager);
        mIndicator.setSelectTab(mConcernType);
        mIndicator.setAdapter(mAdapter);
        mIndicator.setOnPageChangeListener(pagerOnPageChangeListener);

        setHeadTitle(R.string.string_select_user);

        showRightActionBtn(true);
        showMiddleActionBtn(false);
        enableLeftActionBtn(false);

        setHeadTitle(R.string.find_friends);
        mAdapter.setTabBtnBg(mConcernType);
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    OnPageChangeListener pagerOnPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int page) {
            mConcernType = page;
            mAdapter.setTabBtnBg(mConcernType);
            if (page == BpcFindFriendsAdapter.TAB_SHAKE) {
                BpcFindFriendsFragmentActivity.this.showLeftActionBtn(false);
            } else {
                BpcFindFriendsFragmentActivity.this.showLeftActionBtn(true);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int page) {
        }
    };

    @Override
    protected void loadRefresh() {
        Log.d(TAG, "currentpage: " + mConcernType);
        switch (mConcernType) {
            case BpcFindFriendsAdapter.TAB_SHAKE:
                break;
            case BpcFindFriendsAdapter.TAB_NEARBY:
                mNearByFragment.loadRefresh();
                break;
            case BpcFindFriendsAdapter.TAB_CONTACT:
                mContactFragment.loadRefresh();
                break;
            case BpcFindFriendsAdapter.TAB_SUGGESTION:
                mSuggestFragment.loadRefresh();
                break;
            case BpcFindFriendsAdapter.TAB_FANS:
                mFansFragment.loadRefresh();
                break;
            default:
                break;
        }
    }

    private final int CREATE_LOCAL_CIRCLE = 1;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CREATE_LOCAL_CIRCLE: {
                    createCircle(msg.getData().getString("circleName"));
                    break;
                }
                default:
                    Log.d(TAG, "default case, no action");
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

//    @Override
//    public void setLeftMenuPosition() {
//        mPosition = LeftMenuMapping.getPositionForActivity(this);
//        mTitle = getString(R.string.find_friends);
//    }

    OnClickListener addCircleListener = new OnClickListener() {
        public void onClick(View v) {
            showAddCircleDialog();
        }
    };
    
    private AlertDialog mAlertDialog;
    public void showAddCircleDialog(){
        LayoutInflater factory = LayoutInflater.from(this);  
        final View textEntryView = factory.inflate(R.layout.create_circle_dialog, null);  
        final EditText textContext = (EditText) textEntryView.findViewById(R.id.new_circle_edt);

        final CheckBox select_public_circle = (CheckBox) textEntryView.findViewById(R.id.select_public_circle);
        if(orm.isOpenPublicCircle()) {
            select_public_circle.setVisibility(View.VISIBLE);
        }
        select_public_circle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setDialogButtonEnable(!isChecked);
                if(isChecked) {
                    IntentUtil.gotoEditPublicCircleActivity(BpcFindFriendsFragmentActivity.this, textContext.getText().toString().trim(), null, EditPublicCircleActivity.type_create);
                    mAlertDialog.dismiss();
                }
            }
        });
        
        textContext.addTextChangedListener(new ButtonWatcher()); 
        
        mAlertDialog = new AlertDialog.Builder(this)
        .setTitle(R.string.new_circle_dialog_title)
        .setView(textEntryView)
        .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String textString = textContext.getText().toString().trim();
                boolean hasCirecle = false;
                if(textString.length() > 0)
                {
                    Cursor cursor = orm.queryAllCircleinfo(AccountServiceUtils.getBorqsAccountID());
                    final int size = null == cursor ? 0 : cursor.getCount();
                    for(int i=0; i < size; i++)
                    {
                        cursor.moveToPosition(i);
                        UserCircle tmpCircle = QiupuORM.createCircleInformation(cursor);
                        if(tmpCircle != null && tmpCircle.name != null && tmpCircle.name.equals(textString))
                        {
                            hasCirecle = true;
                            Toast.makeText(BpcFindFriendsFragmentActivity.this, getString(R.string.circle_exists), Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }

                    if (null != cursor) {
                        cursor.close();
                    }

                    if(!hasCirecle) {
                        mAlertDialog.dismiss();
                        Message msg = mHandler.obtainMessage(CREATE_LOCAL_CIRCLE);
                        msg.getData().putString("circleName",textString);
                        msg.sendToTarget();
                    }
                }
                else {
                    Toast.makeText(BpcFindFriendsFragmentActivity.this, getString(R.string.input_content), Toast.LENGTH_SHORT).show();
                }
                
            }
        })
        .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        })
        .create();
        mAlertDialog.show();
        setDialogButtonEnable(false);
    }
    
    private class ButtonWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().length() > 0) {
                setDialogButtonEnable(true);
            } else {
                setDialogButtonEnable(false);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
        }
    }
    
    private void setDialogButtonEnable(boolean flag) {
        mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(flag);
    }

    @Override
    public void showCustomFragmentToast(String msg) {
        showCustomToast(msg);
    }

    @Override
    public void activateLocation() {
        Log.d(TAG, "activateLocation() = " + LocationRequest.instance().getLocationListener());
        if (LocationRequest.instance().getLocationListener() == null) {
            setLocationListener();
        }
        LocationRequest.instance().activate(this);
    }

    @Override
    public void deactivateLocation() {
        LocationUtils.deactivateLocationService(this);
    }

    private BpcShakeExchangeCardFragment mShakeFragment;
    private NearByListFragment mNearByFragment;
    private UsersArrayListFragment mFansFragment;
    private SuggestionListFragment mSuggestFragment;
    private FindContactsFragment mContactFragment;

    @Override
    public void getShakeFragmentCallBack(BpcShakeExchangeCardFragment shakeFragment) {
        mShakeFragment = shakeFragment;
    }

    @Override
    protected void getLocationSucceed(String locString) {
        Log.d(TAG, "========== getLocationSucceed() locString = " + locString);
        if (mShakeFragment != null) {
            mShakeFragment.getLocationSucceed(locString);
        }

        if (mNearByFragment != null) {
            mNearByFragment.getLocationSucceed(locString);
        }
    }

    @Override
    public void skip() {
        mAdapter.notifyDataSetChanged();
        mContactFragment.showContactUI(true);
        
    }

    @Override
    public void invoke() {
        skip();
        QiupuORM.enableFindContacts(this, true);
    }

    @Override
    protected void loadSearch() {
        IntentUtil.startPeopleSearchIntent(this);
    }

    @Override
    public void getNearByFragmentCallBack(NearByListFragment nearbyFragment) {
        mNearByFragment = nearbyFragment;
    }

    @Override
    public void getFansFragmentCallBack(UsersArrayListFragment fansFragment) {
        mFansFragment = fansFragment;
    }

    @Override
    public void getSuggestFragmentCallBack(SuggestionListFragment suggestFragment) {
        mSuggestFragment = suggestFragment;
    }

    @Override
    public void getContactFragmentCallBack(FindContactsFragment contactFragment) {
        mContactFragment = contactFragment;
        mContactFragment.setSingleWizardListener(this);
    }

    @Override
    public boolean checkLocationApi() {
        return QiupuConfig.IS_USE_BAIDU_LOCATION_API;
    }
}
