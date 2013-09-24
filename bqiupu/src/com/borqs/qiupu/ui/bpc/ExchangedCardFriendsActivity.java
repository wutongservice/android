package com.borqs.qiupu.ui.bpc;

import java.util.Iterator;
import java.util.Set;

import twitter4j.QiupuUser;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.borqs.common.listener.OnListItemClickListener;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.fragment.ExchangeCardFragment;
import com.borqs.qiupu.fragment.ExchangeCardFragment.GetSendCardFragmentCallBack;
import com.borqs.qiupu.ui.BasicActivity;

public class ExchangedCardFriendsActivity extends BasicActivity implements
        OnListItemClickListener, GetSendCardFragmentCallBack {

    private final static String  TAG = "Qiupu.ExchangedCardFriendsActivity";
    private ExchangeCardFragment mExchangeCardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_layout);

        String title_index = getIntent().getStringExtra("exchange_vcard_title");
        setTitle(title_index);

        mExchangeCardFragment = new ExchangeCardFragment(getIntent().getIntExtra("request_count", 0), title_index);
        getSupportFragmentManager().beginTransaction().add(R.id.request_container, mExchangeCardFragment).commit();

        showMiddleActionBtn(false);
        showLeftActionBtn(true);

    }

    private void setTitle(String title_index) {
        if (getString(R.string.send_card_title).equals(title_index)) {
            setHeadTitle(R.string.exchange_send_header_title);
        } else if (getString(R.string.received_card_friends).equals(title_index)) {
            setHeadTitle(R.string.received_card_friends);
        } else if (getString(R.string.card_holder_title).equals(title_index)) {
            setHeadTitle(R.string.card_holder_title);
        } else {
            Log.d(TAG, "unsupported title");
        }
    }

    @Override
    protected void createHandler() {
    }

    @Override
    protected void loadSearch() {
        gotoSearchActivity();
    }

    @Override
    public void onListItemClick(View view, Fragment fg) {
    }

    @Override
    protected void loadRefresh() {
        if(null != mExchangeCardFragment){
            mExchangeCardFragment.loadRefresh();
        }
    }

    @Override
    public void getSendCardFragment(ExchangeCardFragment sendCardFragment) {
        mExchangeCardFragment = sendCardFragment;
    }

    public void beginStatus() {
        begin();
    }

    public void endStatus() {
        end();
    }

    @Override
    protected void doUsersSetCallBack(String uid, boolean isadd) {
        updateActivityUI(null);
    }

    private void updateActivityUI(final QiupuUser user) {
        synchronized (QiupuHelper.userlisteners) {
            Set<String> set = QiupuHelper.userlisteners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                UsersActionListner listener = QiupuHelper.userlisteners.get(key).get();
                if (listener != null && !ExchangedCardFriendsActivity.class.isInstance(listener)) {
                    listener.updateItemUI(user);
                }
            }
        }
    }
}
