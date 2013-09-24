package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import twitter4j.QiupuUser;
import android.os.Bundle;
import android.util.Log;

import com.borqs.common.listener.ShakeListActionListener;
import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.BpcShakingListFragment;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CircleUtils;

public class BpcShakingListActivity extends BasicActivity implements ShakeListActionListener {
    private static final String TAG = "Qiupu.BpcShakingListActivity";

    private BpcShakingListFragment mShakeListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.request_layout);
        setHeadTitle(R.string.shaking_friends);

        ArrayList<QiupuUser> lbsList = (ArrayList<QiupuUser>) getIntent().getSerializableExtra("lbs_user_list");

        mShakeListFragment = new BpcShakingListFragment(lbsList);
        getSupportFragmentManager().beginTransaction().add(R.id.request_container, mShakeListFragment).commit();

        enableLeftActionBtn(false);
        showMiddleActionBtn(false);
        showRightActionBtn(false);
    }

    @Override
    protected void createHandler() {
    }

    @Override
    protected void doUsersSetCallBack(String uid, boolean result) {
        Log.d(TAG, "doUsersSetCallBack() uid = " + uid);
        mShakeListFragment.updateShakeListItemUI(result, Long.valueOf(uid));
    }

    @Override
    public void sendRequestInFragment(QiupuUser user) {
        Log.d(TAG, "sendRequestInFragment() user.uid = " + user.uid);
        exchangeVcard(user.uid, true, CircleUtils.getDefaultCircleId(), CircleUtils.getDefaultCircleName(getResources()));
    }

}
