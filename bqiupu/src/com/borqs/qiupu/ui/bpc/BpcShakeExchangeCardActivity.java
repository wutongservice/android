package com.borqs.qiupu.ui.bpc;

import com.borqs.qiupu.util.LocationUtils;
import twitter4j.QiupuUser;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.baidu.location.LocationClient;
import com.borqs.account.service.LocationRequest;
import com.borqs.common.listener.ShakeActionListener;
import com.borqs.common.listener.ShakeListActionListener;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.BpcShakeExchangeCardFragment;
import com.borqs.qiupu.fragment.BpcShakingListFragment;
import com.borqs.qiupu.fragment.FindContactsFragment;
import com.borqs.qiupu.fragment.NearByListFragment;
import com.borqs.qiupu.fragment.SuggestionListFragment;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.UsersArrayListActivity.UsersArrayListFragment;
import com.borqs.qiupu.util.BaiduLocationProxy;
import com.borqs.qiupu.util.CircleUtils;

public class BpcShakeExchangeCardActivity extends BasicActivity implements
        ShakeActionListener, ShakeListActionListener {

    private static final String TAG = "Qiupu.BpcShakeExchangeCardActivity";

    private BpcShakeExchangeCardFragment mShakeFragment;
    private BpcShakingListFragment mShakeListFragment;
    private FragmentManager fm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_layout);
        setHeadTitle(R.string.home_exchange);

        fm = getSupportFragmentManager();
        mShakeFragment = new BpcShakeExchangeCardFragment(asyncQiupu, true);
        fm.beginTransaction().add(R.id.request_container, mShakeFragment).commit();

        showLeftActionBtn(false);
        showMiddleActionBtn(false);
        showRightActionBtn(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLeftActionBtn(false);
    }

    @Override
    protected void createHandler() {
    }

    @Override
    public void showCustomFragmentToast(String msg) {
        showCustomToast(msg);
    }

    @Override
    public void activateLocation() {
        LocationUtils.activateLocationService(this);
    }

    @Override
    public void deactivateLocation() {
        LocationUtils.deactivateLocationService(this);
    }

    @Override
    protected void doUsersSetCallBack(String uid, boolean result) {
        mShakeFragment.updateShakeListItemUI(result, Long.valueOf(uid));
    }

    @Override
    public void sendRequestInFragment(QiupuUser user) {
        exchangeVcard(user.uid, true, CircleUtils.getDefaultCircleId(), CircleUtils.getDefaultCircleName(getResources()));
    }

    @Override
    protected void getLocationSucceed(String locString) {
        Log.d(TAG, "######### getLocationSucceed() locString = " + locString);
        mShakeFragment.getLocationSucceed(locString);
    }

    @Override
    protected void doActionFriendEndCallBack(Message msg) {
        Log.d(TAG, "uid = " + msg.getData().getLong("uid"));
        if (mShakeListFragment != null && mShakeListFragment.isVisible()) {
            mShakeListFragment.updateItemUI(msg.getData().getLong("uid"), msg.getData().getBoolean(RESULT));
        }
    }

    @Override
    public void getShakeFragmentCallBack(BpcShakeExchangeCardFragment shakeFragment) {}

    @Override
    public void getContactFragmentCallBack(FindContactsFragment contactFragment) {
    }

    @Override
    public void getSuggestFragmentCallBack(SuggestionListFragment suggestFragment) {
    }

    @Override
    public void getNearByFragmentCallBack(NearByListFragment nearbyFragment) {
    }

    @Override
    public void getFansFragmentCallBack(UsersArrayListFragment fansFragment) {
    }

    @Override
    public boolean checkLocationApi() {
        return QiupuConfig.IS_USE_BAIDU_LOCATION_API;
    }

}
