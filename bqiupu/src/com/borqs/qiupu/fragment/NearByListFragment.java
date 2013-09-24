package com.borqs.qiupu.fragment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import twitter4j.AsyncQiupu;
import twitter4j.QiupuUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.conf.ConfigurationContext;
import twitter4j.internal.http.HttpClientImpl;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.BPCFriendsNewAdapter;
import com.borqs.common.listener.ShakeActionListener;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.AsyncApiUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.BpcFriendsItemView;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.ui.bpc.BpcFriendsActivity;
import com.borqs.qiupu.ui.bpc.UsersArrayListActivity;

public class NearByListFragment extends PeopleListFragment implements
        BPCFriendsNewAdapter.MoreItemCheckListener, UsersActionListner,
        OnScrollListener, AsyncApiUtils.AsyncApiSendRequestCallBackListener {

    private static final String  TAG        = "NearByListFragment";

    private BPCFriendsNewAdapter mUserAdapter;
    private ArrayList<QiupuUser> mUserList  = new ArrayList<QiupuUser>();

    private int                  page       = 0;
    private static final int     PAGE_COUNT = 20;

    private ProgressDialog       mRequestProgress;
    private AsyncQiupu           asyncQiupu;
    private Handler              mHandler;

    private ShakeActionListener  mShakeListener;

    public NearByListFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (ShakeActionListener.class.isInstance(activity)) {
            mShakeListener = (ShakeActionListener) activity;
            mShakeListener.getNearByFragmentCallBack(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Log.d(TAG, "onCreate() activate location");
        // mShakeListener.activateLocation();

        asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null,
                null);
        mHandler = new MainHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View convertView = super.onCreateView(inflater, container,
                savedInstanceState);

        mUserList = new ArrayList<QiupuUser>();
        mUserAdapter = new BPCFriendsNewAdapter(mActivity, this, false, true);

        initListView();
        initProgressDialog();

        QiupuHelper.registerUserListener(getClass().getName(), this);
        mUserAdapter.registerUsersActionListner(getClass().getName(), this);

        return convertView;
    }

    private void initListView() {
        mListView.setAdapter(mUserAdapter);
        mListView.setOnItemClickListener(new FriendsItemClickListener());
        mListView.setOnScrollListener(this);
        mListView.setOnCreateContextMenuListener(this);
    }

    private void initProgressDialog() {
        mRequestProgress = new ProgressDialog(mActivity);
        mRequestProgress.setMessage(getString(R.string.request_process_title));
        mRequestProgress.setCanceledOnTouchOutside(false);
        mRequestProgress.setIndeterminate(true);
        mRequestProgress.setCancelable(true);
    }

    protected void getUserInfoEndCallBack(ArrayList<QiupuUser> users,
            boolean isfollowing) {
        if (users.size() <= 0) {
            followershowmore = false;
        } else {
            followershowmore = true;
            mUserList.addAll(users);
        }
    }

    private boolean followershowmore;

    @Override
    public boolean isMoreItemHidden() {
        return followershowmore;
    }

    @Override
    public OnClickListener getMoreItemClickListener() {
        return loadOlderClick;
    }

    @Override
    public int getMoreItemCaptionId() {
        return isInProcess() ? R.string.loading : R.string.list_view_more;
    }

    protected void doGetFriendsEndCallBack(Message msg) {

    }

    private void refreshPeopleUI(ArrayList<QiupuUser> users, boolean flag) {
        mUserAdapter.alterDataList(users);
    }

    @Override
    public void sendRequestCallBackBegin() {
        if (!mRequestProgress.isShowing()) {
            mRequestProgress.show();
        }
    }

    @Override
    public void sendRequestCallBackEnd(boolean result, final long uid) {
        if (mRequestProgress.isShowing()) {
            mRequestProgress.hide();
        }

        Message message = mHandler.obtainMessage(SEND_REQUEST_END);
        message.getData().putBoolean("result", result);
        message.getData().putLong("uid", uid);
        message.sendToTarget();
    }

    private class FriendsItemClickListener implements
            AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            Log.d(TAG, "click user item " + position);
            if (BpcFriendsItemView.class.isInstance(view)) {
                BpcFriendsItemView fv = (BpcFriendsItemView) view;
                QiupuUser tmpUser = fv.getUser();
                IntentUtil.startUserDetailIntent(fv.getContext(), tmpUser.uid,
                        tmpUser.nick_name, tmpUser.circleName);
            }
        }
    }

    private static final int GET_NEARBY_USER  = 2;
    private static final int SEND_REQUEST_END = 0x13;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_NEARBY_USER: {
                    Log.d(TAG, "GET_NEARBY_USER : page = " + page);
                    syncNearByPeople(page, PAGE_COUNT);
                    break;
                }
                case SEND_REQUEST_END: {
                    final boolean result = msg.getData().getBoolean("result");
                    if (result) {
                        long uid = msg.getData().getLong("uid");
                        onVcardExchanged(uid);
                    }
                    break;
                }
                case QiupuMessage.MESSAGE_LOAD_DATA_END: {
                    endProgress();
//                    end();
                    boolean sucadd = msg.getData().getBoolean("RESULT", false);
                    if (sucadd == true) {
                        // int count = msg.getData().getInt("PAGE_COUNT");
                        // int page = msg.getData().getInt("page");
                        ArrayList<QiupuUser> userlist = (ArrayList<QiupuUser>) msg
                                .getData().getSerializable("user_list");
                        if (mUserList.size() <= 0 && userlist.size() <= 0) {
                            mEmptyText.setVisibility(View.VISIBLE);
                            mListView.setVisibility(View.GONE);
                            mEmptyText.setText(R.string.find_no_nearby_people);
                        } else {
                            mEmptyText.setVisibility(View.GONE);
                            mListView.setVisibility(View.VISIBLE);

                            getUserInfoEndCallBack(userlist, false);
                            refreshPeopleUI(mUserList, false);
                        }
                    } else {
                        String ErrorMsg = msg.getData().getString("ERROR_MSG");
                        if (TextUtils.isEmpty(ErrorMsg) == false) {
                            showCustomToast(ErrorMsg);

//                            mEmptyText.setVisibility(View.VISIBLE);
//                            mListView.setVisibility(View.GONE);
//                            mEmptyText.setText(R.string.find_no_nearby_people);
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() deactivateLocation()");
        super.onDestroy();
        if (null != mRequestProgress) {
            mRequestProgress.dismiss();
            mRequestProgress = null;
        }

        HttpClientImpl.setLocation("");

        if (mShakeListener != null) {
            mShakeListener.deactivateLocation();
        }

        QiupuHelper.unregisterUserListener(getClass().getName());
    }

    private void updateActivityUI(final QiupuUser user) {
        synchronized (QiupuHelper.userlisteners) {
            Set<String> set = QiupuHelper.userlisteners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                UsersActionListner listener = QiupuHelper.userlisteners.get(key).get();
                if (listener != null && !UsersArrayListActivity.class.isInstance(listener)) {
                    listener.updateItemUI(user);
                }
            }
        }
    }

    public void updateItemUI(QiupuUser user) {
        if (user != null) {
            refreshUserItem(user);
        } else {
            Log.d(TAG, "updateItemUI() user is null");
        }
    }

    private int followerItem = 0;

    public void onScroll(AbsListView v, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        followerItem = firstVisibleItem + visibleItemCount;
    }

    public void onScrollStateChanged(AbsListView v, int state) {
        boolean forloadmore = (state == OnScrollListener.SCROLL_STATE_IDLE);
        loadOlderUsers(forloadmore, false);
    }

    protected void loadOlderUsers(boolean formore, boolean forceget) {
        int followercount = mUserList.size();
        Log.i(TAG, "followercount:" + followercount);
        if ((followerItem == followercount + 1 && formore) || forceget) {
            getOldNearByData();
        }
    }

    protected Method failCallFollowerMethod;

    protected void SubApplicationPage() {
        Log.d(TAG, "resore the dpage--");
        page--;
        if (page < 0) {
            page = 0;
        }
    }

    private void callFailLoadFollowerMethod() {
        page += 1;
        Log.i(TAG, "callFailLoadFollowerMethod() Page:" + page);
        try {
            failCallFollowerMethod = UsersArrayListActivity.class
                    .getDeclaredMethod("SubApplicationPage", (Class[]) null);
        } catch (Exception e) {}
    }

    public View.OnClickListener loadOlderClick = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "load more fans");
            getOldNearByData();
        }
    };

    private void getOldNearByData() {
        page += 1;
        Log.i(TAG, "getOldNearByData()  Page:" + page);
        try {
            failCallFollowerMethod = BpcFriendsActivity.class
                    .getDeclaredMethod("SubApplicationPage", (Class[]) null);
        } catch (Exception e) {}

        syncNearByPeople(page, PAGE_COUNT);
    }

    public void loadRefresh() {
        page = 0;
        mUserList.clear();
        mHandler.obtainMessage(GET_NEARBY_USER).sendToTarget();
    }

    public boolean isInProcess() {
        return inLoading;
    }

    public void addFriends(QiupuUser user) {
        IntentUtil.startCircleSelectIntent(mActivity, user.uid, null);
    }

    public void refuseUser(long uid) {
    }

    protected void doUsersSetCallBack(String uid, boolean isadd) {
        updateActivityUI(null);
    }

    @Override
    public void deleteUser(QiupuUser user) {
    }

    @Override
    public void sendRequest(QiupuUser user) {
        // sendApproveRequest(String.valueOf(user.uid), "");
        AsyncApiUtils.sendApproveRequest(user.uid, "", asyncQiupu, this);
    }

    private void refreshUserItem(final QiupuUser user) {
        final int count = mUserList.size();
        mHandler.post(new Runnable() {
            public void run() {
                for (int j = 0; j < count; j++) {
                    View v = mListView.getChildAt(j);
                    if (BpcFriendsItemView.class.isInstance(v)) {
                        BpcFriendsItemView fv = (BpcFriendsItemView) v;

                        if (fv.getUserID() == user.uid) {
                            // parse user info to db
                            QiupuUser tmpuser = fv.getUser();
                            tmpuser.pedding_requests = user.pedding_requests;
                            tmpuser.circleId = user.circleId;
                            tmpuser.circleName = user.circleName;

                            // refresh item
                            fv.refreshUI();
                            break;
                        }
                    }
                }
            }
        });
    }

    boolean inLoading    = false;
    Object  mLoadingLock = new Object();

    public void syncNearByPeople(final int page, final int count) {
        {
            synchronized (mLoadingLock) {
                if (inLoading == true) {
                    Log.d(TAG, "in doing get nearby people");
                    callFailLoadFollowerMethod();
                    return;
                } else {
                    inLoading = true;
                }
            }
        }

//        begin();
        beginProgress();
        Log.d(TAG, "getNearByPeopleListPage(): page = " + page);
        asyncQiupu.getNearByPeopleListPage(AccountServiceUtils.getSessionID(),
                page, count, new TwitterAdapter() {
                    public void getNearByPeopleList(ArrayList<QiupuUser> users) {
                        Log.d(TAG, "finish getNearByPeopleListPage = " + users.size());

                        synchronized (mLoadingLock) {
                            inLoading = false;
                        }

                        Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_LOAD_DATA_END);
                        msg.getData().putBoolean("RESULT", true);
                        msg.getData().putSerializable("user_list", users);
                        msg.getData().putInt("PAGE_COUNT", users.size());
                        msg.getData().putInt("page", page);
                        msg.sendToTarget();
                    }

                    public void onException(TwitterException ex,
                            TwitterMethod method) {
                        Log.d(TAG, "finish onException =" + ex.getMessage());

                        synchronized (mLoadingLock) {
                            inLoading = false;
                        }

                        callFailLoadFollowerMethod();

                        Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_LOAD_DATA_END);
                        msg.getData().putString("ERROR_MSG", ex.getMessage());
                        msg.getData().putBoolean("RESULT", false);
                        msg.sendToTarget();
                    }
                });
    }

    private boolean get_location_succeed = false;
    public void getLocationSucceed(String locationStr) {
        Log.d(TAG, "========= getLocationSucceed() locationStr = " + locationStr);
        if (TextUtils.isEmpty(locationStr) == false) {
            get_location_succeed = true;
        } else {
            get_location_succeed = false;
        }
//        setActionForShake();
    }

    public void getNearByPeople() {
        if (get_location_succeed) {
            Message msg = mHandler.obtainMessage(GET_NEARBY_USER);
            msg.getData().putBoolean("RESULT", true);
            msg.sendToTarget();
        } else {
            Log.d(TAG, "getNearByPeople(), now we didn't get location information");
        }
    }

    private ProgressDialog mprogressDialog;

    private void showProcessDialog(int resId, boolean CanceledOnTouchOutside,
            boolean Indeterminate, boolean cancelable) {
        mprogressDialog = DialogUtils.createProgressDialog(mActivity, resId,
                CanceledOnTouchOutside, Indeterminate, cancelable);
        mprogressDialog.show();
    }

    private void beginProgress() {
        showProcessDialog(R.string.near_by_people_tips, false, true, true);
    }

    private void endProgress() {
        try {
            mprogressDialog.dismiss();
            mprogressDialog = null;
        } catch (Exception e) {
        }
    }

}
