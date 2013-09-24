package com.borqs.qiupu.ui.bpc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
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
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.ShakeActionListener;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.AsyncApiUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.BpcFriendsItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.PeopleListFragment;
import com.borqs.qiupu.ui.BasicActivity;
import twitter4j.*;
import twitter4j.conf.ConfigurationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UsersArrayListActivity extends BasicActivity {
    private static final String TAG = "Qiupu.UsersArrayListActivity";

    private UsersArrayListFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.request_layout);

        processIntent(getIntent());

        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.request_container, mFragment);
            fragmentTransaction.commit();
        }

        enableLeftActionBtn(false);
        showMiddleActionBtn(false);
        showRightActionBtn(false);

        setHeadTitle(R.string.title_my_fans);
    }

    @Override
    protected void loadRefresh() {
        if (null != mFragment && mFragment.isVisible()) {
            mFragment.loadRefresh();
        }
    }

    @Override
    protected void createHandler() {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        processIntent(intent);
    }

    @Override
    protected void getUserInfoEndCallBack(List<QiupuUser> users, boolean isfollowing) {
        if (null != mFragment) {
            mFragment.getUserInfoEndCallBack(users, isfollowing);
        }
    }

    @Override
    protected void doUsersSetCallBack(String uid, boolean isadd) {
        if (null != mFragment) {
            mFragment.doUsersSetCallBack(uid, isadd);
        }
    }

    private void processIntent(Intent intent) {
        long userId = QiupuConfig.USER_ID_ALL;

        Bundle bundle = intent.getExtras();
        if (null != bundle) {
            userId = bundle.getLong(BpcApiUtils.User.USER_ID, QiupuConfig.USER_ID_ALL);
        }

        final String url = getIntentURL(intent);
        if (!isEmpty(url)) {
            final String uid = BpcApiUtils.parseSchemeValue(intent, BpcApiUtils.SEARCH_KEY_UID);
            if (!TextUtils.isEmpty(uid)) {
                if (TextUtils.isDigitsOnly(uid)) {
                    userId = Long.parseLong(uid);
                } else {
                    Log.i(TAG, String.format("processIntent, set uid to (%s) as unexpected scheme url: %s",
                            getSaveUid(), url));
                    userId = getSaveUid();
                }
            }
        }

        mFragment = UsersArrayListFragment.newInstance(userId);
    }

    public static void showUserArrayList(Context context, long uid) {
        Intent intent = new Intent(context, UsersArrayListActivity.class);
        Bundle bundle = BpcApiUtils.getUserBundle(uid);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static class UsersArrayListFragment extends PeopleListFragment implements
            BPCFriendsNewAdapter.MoreItemCheckListener, UsersActionListner,
            OnScrollListener, AsyncApiUtils.AsyncApiSendRequestCallBackListener {

        private long mUserId;

        private BPCFriendsNewAdapter mUserAdapter;
        private ArrayList<QiupuUser> mUserList = new ArrayList<QiupuUser>();

        private int page = 0;
        private static final int PAGE_COUNT = 20;

        private ProgressDialog mRequestProgress;

        private AsyncQiupu asyncQiupu;
        private Handler mHandler;
        private ShakeActionListener  mShakeListener;

        public static UsersArrayListFragment newInstance(long uid) {
            UsersArrayListFragment instance = new UsersArrayListFragment();
            instance.mUserId = uid;
            return instance;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            if (ShakeActionListener.class.isInstance(activity)) {
                mShakeListener = (ShakeActionListener) activity;
                mShakeListener.getFansFragmentCallBack(this);
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null, null);
            mHandler = new MainHandler();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "onCreateView");
            View convertView = super.onCreateView(inflater, container, savedInstanceState);

            mUserList = new ArrayList<QiupuUser>();
            mUserAdapter = new BPCFriendsNewAdapter(mActivity, this, false, false);

            mListView.setAdapter(mUserAdapter);
            mListView.setOnItemClickListener(new FriendsItemClickListener());

            mRequestProgress = new ProgressDialog(mActivity);
            mRequestProgress.setMessage(getString(R.string.request_process_title));
            mRequestProgress.setCanceledOnTouchOutside(false);
            mRequestProgress.setIndeterminate(true);
            mRequestProgress.setCancelable(true);

            mListView.setOnScrollListener(this);
            mListView.setOnCreateContextMenuListener(this);
            mHandler.obtainMessage(GET_USER_FANS).sendToTarget();

            QiupuHelper.registerUserListener(getClass().getName(), this);
            mUserAdapter.registerUsersActionListner(getClass().getName(), this);

            return convertView;

        }

        protected void getUserInfoEndCallBack(List<QiupuUser> users, boolean isfollowing) {
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

        private class FriendsItemClickListener implements AdapterView.OnItemClickListener {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "click user item " + position);
                if (BpcFriendsItemView.class.isInstance(view)) {
                    BpcFriendsItemView fv = (BpcFriendsItemView) view;
                    QiupuUser tmpUser = fv.getUser();
                    IntentUtil.startUserDetailIntent(fv.getContext(),
                            tmpUser.uid, tmpUser.nick_name, tmpUser.circleName);
                }
            }
        }

        private static final int GET_USER_FANS = 2;
        private static final int SEND_REQUEST_END = 0x13;

        private class MainHandler extends Handler {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case GET_USER_FANS: {
                        syncFansInfo(mUserId, page, PAGE_COUNT, "");
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
                        end();
                        boolean sucadd = msg.getData().getBoolean(RESULT, false);
                        if (sucadd == true) {
                            int count = msg.getData().getInt("PAGE_COUNT");
                            int page = msg.getData().getInt("page");
                            refreshPeopleUI(mUserList, false);
                        } else {
                            String ErrorMsg = msg.getData().getString(ERROR_MSG);
                            if (isEmpty(ErrorMsg) == false) {
                                showCustomToast(ErrorMsg);
                            }
                        }
                        break;
                    }
                }
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (null != mRequestProgress) {
                mRequestProgress.dismiss();
                mRequestProgress = null;
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
            {
                if (user != null) {
                    refreshUserItem(user);
                }
            }
        }

        private int followerItem = 0;

        public void onScroll(AbsListView v, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
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
                getOldFansData();
            }
        }

        protected Method failCallFollowerMethod;

        protected void SubApplicationPage() {
            Log.d(TAG, "resore the dpage--");
            page--;
            if (page < 0)
                page = 0;
        }

        private void callFailLoadFollowerMethod() {
            page += 1;
            Log.i(TAG, "Page:" + page);
            try {
                failCallFollowerMethod = UsersArrayListActivity.class.getDeclaredMethod("SubApplicationPage", (Class[]) null);
            } catch (Exception e) {
            }
        }

        public View.OnClickListener loadOlderClick = new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "load more fans");
                getOldFansData();
            }
        };

        private void getOldFansData() {
            page += 1;
            Log.i(TAG, "Page:" + page);
            try {
                failCallFollowerMethod = BpcFriendsActivity.class.getDeclaredMethod("SubApplicationPage", (Class[]) null);
            } catch (Exception e) {
            }

            syncFansInfo(mUserId, page, PAGE_COUNT, "");
        }

        public void loadRefresh() {
            page = 0;
            mHandler.obtainMessage(GET_USER_FANS).sendToTarget();
        }

        public boolean isInProcess() {
            return inLoading;
        }

//        boolean isdelete = true;
//        View.OnClickListener editClickListener = new OnClickListener() {
//            public void onClick(View arg0) {
//                alterMiddleActionBtnByComposer(isdelete ? R.drawable.cmcc_icon_view_switching : R.drawable.cmcc_icon_choice_of, editClickListener);
//                mUserAdapter.setIsdeleteMode(isdelete);
//                mUserAdapter.notifyDataSetChanged();
//                isdelete = !isdelete;
//            }
//        };

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
//            sendApproveRequest(String.valueOf(user.uid), "");
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


        boolean inLoading = false;
        Object mLoadingLock = new Object();

        public void syncFansInfo(final long mUserid, final int page, final int count, final String circles) {
            Log.d(TAG, "mUserid :" + mUserid);
            {
                synchronized (mLoadingLock) {
                    if (inLoading == true) {
                        Log.d(TAG, "in doing get Follower data");
                        callFailLoadFollowerMethod();
                        return;
                    } else {
                        inLoading = true;
                    }
                }
            }

            Log.d(TAG, "mUserid :" + mUserid);
            begin();
            asyncQiupu.getFriendsListPage(AccountServiceUtils.getSessionID(), mUserid, circles, page, count, false, new TwitterAdapter() {
                public void getFriendsList(List<QiupuUser> users) {
                    Log.d(TAG, "finish getFriendsList=" + users.size());

                    getUserInfoEndCallBack(users, false);

                    synchronized (mLoadingLock) {
                        inLoading = false;
                    }

                    Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_LOAD_DATA_END);
                    msg.getData().putBoolean(RESULT, true);
                    msg.getData().putInt("PAGE_COUNT", users.size());
                    msg.getData().putInt("page", page);
                    msg.sendToTarget();
                }

                public void onException(TwitterException ex, TwitterMethod method) {

                    synchronized (mLoadingLock) {
                        inLoading = false;
                    }

                    callFailLoadFollowerMethod();

                    Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_LOAD_DATA_END);
                    msg.getData().putString(ERROR_MSG, ex.getMessage());
                    msg.getData().putBoolean(RESULT, false);
                    msg.sendToTarget();
                }
            });
        }
    }
}
