package com.borqs.qiupu.ui.bpc;

import java.util.HashMap;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.borqs.common.listener.RequestStatusListener;
import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.RequestFragment;
import com.borqs.qiupu.ui.BasicActivity;

public class RequestActivity extends BasicActivity implements RequestStatusListener {
    private static final String TAG = "Qiupu.RequestActivity";

    private RequestFragment mRequestFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.request_layout);
        setHeadTitle(R.string.home_requests);

        mRequestFragment = new RequestFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.request_container, mRequestFragment).commit();

        showMiddleActionBtn(false);
        enableRightActionBtn(false);
        enableMiddleActionBtn(false);
    }

    @Override
    protected void loadRefresh() {
        mRequestFragment.loadRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void createHandler() {}

    @Override
    protected void doUpdateUserInfoEndCallBack(boolean suc) {
        mRequestFragment.doUpdateUserInfoEndCallBack(suc);
    }

    @Override
    protected void doActionFriendEndCallBack(Message msg) {
        mRequestFragment.doActionFriendEndCallBack(msg);
    }

    public void beginStatus() {
        begin();
    }

    public void endStatus() {
        end();
    }

    @Override
    public void updateUserInfo(HashMap<String, String> infoMap) {
        super.updateUserInfo(infoMap);
    }

    public void setCircle(final long uid, final String circleid,
            final String circleName) {
        super.setCircle(uid, circleid, circleName);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
