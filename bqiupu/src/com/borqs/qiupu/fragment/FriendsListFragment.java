package com.borqs.qiupu.fragment;


import java.util.List;

import twitter4j.AsyncQiupu;
import twitter4j.QiupuUser;
import twitter4j.Requests;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.AsyncApiUtils;
import com.borqs.common.util.AsyncApiUtils.AsyncApiSendRequestCallBackListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.BpcFriendsItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.service.FriendsManager;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.BpcFriendsListViewUi;
import com.borqs.qiupu.ui.bpc.PickCircleUserActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;

public final class FriendsListFragment extends PeopleSearchableFragment implements
        FriendsManager.FriendsServiceListener, UsersActionListner, AsyncApiSendRequestCallBackListener {
    private static final String TAG = "FriendsListFragment";

    private BpcFriendsListViewUi mAllPeople;
    private Cursor mFriends;
    private QiupuORM orm;
    private Handler mHandler;
    private ProgressDialog mprogressDialog;
    private String mCircleId;
    private AsyncQiupu asyncQiupu;
    private static final int SYNC_FRIENDS_DEFAULT_COUNT = 200;

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        super.onAttach(activity);
        mActivity = activity;

        try {
            FriendsListFragmentCallBackListener listener = (FriendsListFragmentCallBackListener) activity;
            listener.getFriendsListFragment(this);
            long id = listener.getCircleId();
            mCircleId = String.valueOf(id);
        } catch (ClassCastException e) {
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        orm = QiupuORM.getInstance(mActivity);
        mHandler = new MainHandler();
        asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        showEditText = false;
        View convertView = super.onCreateView(inflater, container, savedInstanceState);

        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(friendItemClickListener);

        setAddPeopleButton();

        mAllPeople = new BpcFriendsListViewUi();
        mAllPeople.initUsers(mActivity, mListView);

        return convertView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(QiupuService.mFriendsManager != null && QiupuService.mFriendsManager.getLoadStatus(FriendsManager.SYNC_TYPE_FRIENDS)) {
        	begin();
        }
        
        mHandler.post(new Runnable() {
			@Override
			public void run() {
				loadFriendsData();
			}
		});
    }

    public void onResume() {
        FriendsManager.registerFriendsServiceListener(getClass().getName(), this);
        QiupuHelper.registerUserListener(getClass().getName(), this);
        mAllPeople.registerUsersActionListner(getClass().getName(), this);
        super.onResume();
    }

    public void onPause() {
        FriendsManager.unregisterFriendsServiceListener(getClass().getName());
        mAllPeople.unregisterUsersActionListner(getClass().getName());
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        QiupuHelper.unregisterUserListener(getClass().getName());
        if (mFriends != null)
            mFriends.close();
    }

    private static final int LOAD_FRIENDS_CALLBACK = 101;
    private static final int SEND_REQUEST_END = 102;
    private static final int GET_CIRCLE_PEOPLE = 103;
    private static final int GET_CIRCLE_PEOPLE_END = 104;
    private static final int USER_SET_CIRCLE_END = 105;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case QiupuMessage.MESSAGE_LOAD_DATA_END: {
                    Log.d(TAG, "load data end");
                    end();
                    afterGetDataEnd(msg);
                    break;
                }
                case LOAD_FRIENDS_CALLBACK: {
                	if(msg.getData().getInt(FriendsManager.SYNC_TYPE) != FriendsManager.SYNC_TYPE_FRIENDS) {
                		return ;
                	}
                    int statuscode = msg.getData().getInt("statuscode");
                    if (statuscode == FriendsManager.STATUS_DO_FAIL) {
                        end();
                    } else if (statuscode == FriendsManager.STATUS_DOING) {
                        begin();
                    } else if (statuscode == FriendsManager.STATUS_DO_OK) {
                        end();

//                        if (mFriends != null)
//                            mFriends.close();
//
//                        mFriends = orm.queryFriendsCursor();
                        queryFriends(null);
                        mAllPeople.loadUserRefresh(mFriends, mAtoZ);
                    } else if (statuscode == FriendsManager.STATUS_ITERATING) {
//                        if (mFriends != null)
//                            mFriends.close();
//
//                        mFriends = orm.queryFriendsCursor();
                        queryFriends(null);
                        mAllPeople.loadUserRefresh(mFriends, mAtoZ);
                    }
                    break;
                }
                case SEND_REQUEST_END: {
                    try {
                        mprogressDialog.dismiss();
                        mprogressDialog = null;
                    } catch (Exception ne) {
                    }

                    boolean ret = msg.getData().getBoolean("result", false);
                    if (ret) {
                        long uid = msg.getData().getLong("uid");

                        for (int j = 0; j < mListView.getChildCount(); j++) {
                            View v = mListView.getChildAt(j);
                            if (BpcFriendsItemView.class.isInstance(v)) {
                                BpcFriendsItemView fv = (BpcFriendsItemView) v;
                                QiupuUser user = fv.getUser();
                                if (user != null && user.uid == uid) {
                                    user.pedding_requests = Requests.getrequestTypeIds(user.pedding_requests);
                                    orm.setRequestUser(user.uid, user.pedding_requests);
                                    fv.refreshUI();
                                    break;
                                }
                            }
                        }

//                        if (mFriends != null)
//                            mFriends.close();
//
//                        mFriends = orm.queryFriendsCursor();
                        queryFriends(null);
                        mAllPeople.resetCursor(mFriends);

                        Toast.makeText(mActivity, getString(R.string.request_ok), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "exchange vcard failed");
                    }
                    break;
                }

                case GET_CIRCLE_PEOPLE: {
                    syncfriendsInfo(AccountServiceUtils.getBorqsAccountID(), 0, SYNC_FRIENDS_DEFAULT_COUNT, mCircleId);
                    break;
                }
                case GET_CIRCLE_PEOPLE_END: {
                    Log.d(TAG, "load circle friends end");
                    end();
                    loadCircleFriendsEnd(msg);
                    break;
                }
                case USER_SET_CIRCLE_END: {
                    try {
                        mprogressDialog.dismiss();
                        mprogressDialog = null;
                    } catch (Exception ne) {
                    }
                    boolean ret = msg.getData().getBoolean(BasicActivity.RESULT, false);
                    if (ret) {
//                     boolean isadd = msg.getData().getBoolean("isadd");
//                     String uid = msg.getData().getString("uid");
                        onCircleMemberUpdated("");
//                        if (mFriends != null)
//                            mFriends.close();
//
//                        mFriends = orm.queryFriendsCursorByCircleId(mCircleId);
//                        mAllPeople.loadUserRefresh(mFriends, mAtoZ);

                        QiupuHelper.updateActivityUI(null);
                    } else {
                        Log.d(TAG, "user circle set failed");
                    }
                    break;
                }

            }
        }
    }

    @Override
    public void enterPosition(String alpha, int position) {
        mListView.setSelection(position + mListView.getHeaderViewsCount());
    }

    @Override
    public void leavePosition(String alpha) {
    }

    ;

    @Override
    public void beginMove() {
    }

    ;

    @Override
    public void endMove() {
    }

    ;

    AdapterView.OnItemClickListener friendItemClickListener = new FriendsItemClickListener();

    private class FriendsItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (BpcFriendsItemView.class.isInstance(view)) {
                BpcFriendsItemView fv = (BpcFriendsItemView) view;
                QiupuUser tmpUser = fv.getUser();
                IntentUtil.startUserDetailIntent(fv.getContext(), tmpUser.uid,
                        tmpUser.nick_name, tmpUser.circleName);
            }
        }
    }

    private void loadFriendsData() {
        if (mFriends != null)
            mFriends.close();

        if (isAllFriends()) {
            mFriends = orm.queryFriendsCursor();
            if (mFriends != null && mFriends.getCount() <= 1) {
                IntentUtil.loadUsersFromServer(mActivity);
            } else {
                mAllPeople.loadUserRefresh(mFriends, mAtoZ);
            }
        } else {
            mFriends = orm.queryFriendsCursorByCircleId(mCircleId);
            if (mFriends != null && mFriends.getCount() <= 0) {
                mHandler.obtainMessage(GET_CIRCLE_PEOPLE).sendToTarget();
            } else {
                mAllPeople.loadUserRefresh(mFriends, mAtoZ);
            }
        }
    }

    private void afterGetDataEnd(Message msg) {
        boolean sucadd = msg.getData().getBoolean(BasicActivity.RESULT, false);
        if (sucadd) {
            mAllPeople.loadUserRefresh(mFriends, mAtoZ);
        } else {
            String ErrorMsg = msg.getData().getString(BasicActivity.ERROR_MSG);
            if (StringUtil.isEmpty(ErrorMsg) == false) {
                Toast.makeText(mActivity, ErrorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void updateUI(int msgcode, Message message) {
        if (QiupuConfig.LOGD) Log.d(TAG, "msgcode: " + msgcode + " message: " + message);
        Message msg = mHandler.obtainMessage(LOAD_FRIENDS_CALLBACK);
        msg.getData().putInt("statuscode", msgcode);
        if(message != null) {
        	msg.getData().putInt(FriendsManager.SYNC_TYPE, message.getData().getInt(FriendsManager.SYNC_TYPE));
        }
        msg.sendToTarget();
    }

    @Override
    public void updateItemUI(QiupuUser user) {
    }

    @Override
    public void addFriends(QiupuUser user) {
    }

    @Override
    public void refuseUser(long uid) {
    }

    @Override
    public void deleteUser(QiupuUser user) {
    }

    @Override
    public void sendRequest(QiupuUser user) {
        AsyncApiUtils.sendApproveRequest(user.uid, "", new AsyncQiupu(ConfigurationContext.getInstance(), null, null), this);
    }

    public interface FriendsListFragmentCallBackListener {
        public void getFriendsListFragment(FriendsListFragment fragment);
        public long getCircleId();
    }

    @Override
    public void sendRequestCallBackBegin() {
        showProcessDialog(R.string.request_process_title, false, true, true);
    }

    @Override
    public void sendRequestCallBackEnd(boolean result, long uid) {
        Message message = mHandler.obtainMessage(SEND_REQUEST_END);
        message.getData().putBoolean("result", result);
        message.getData().putLong("uid", uid);
        message.sendToTarget();
    }

    private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable) {
        mprogressDialog = DialogUtils.createProgressDialog(mActivity,
                resId, CanceledOnTouchOutside, Indeterminate, cancelable);
        mprogressDialog.show();
    }

    public boolean getLoadStatus() {
        return FriendsManager.mSyncStatus == FriendsManager.STATUS_DOING || inloadingFollower;
    }

    boolean inloadingFollower = false;
    Object mFollowerLock = new Object();

    //used to sync circle people
    public void syncfriendsInfo(final long mUserid, final int page, final int count, final String circles) {
        Log.d(TAG, "mUserid :" + mUserid);
        {
            synchronized (mFollowerLock) {
                if (inloadingFollower == true) {
                    Log.d(TAG, "in doing get Follower data");
                    return;
                }
            }

            synchronized (mFollowerLock) {
                inloadingFollower = true;
            }
        }

        Log.d(TAG, "mUserid :" + mUserid);
        begin();
        asyncQiupu.getFriendsListPage(AccountServiceUtils.getSessionID(), mUserid, circles, page, count, true, new TwitterAdapter() {
            public void getFriendsList(List<QiupuUser> users) {
                Log.d(TAG, "finish getFriendsList=" + users.size());

                if (mUserid == AccountServiceUtils.getBorqsAccountID()) {
                    //remove circle users, firstly
//                    if (page == 0) {
//                        orm.clearCircleUsers(circles);
//                    }

                    //update circle users
//                    orm.updateCircleUsers(circles, users);
                    orm.insertFriendsList(users);//update friends to DB
                }

                synchronized (mFollowerLock) {
                    inloadingFollower = false;
                }

                Message msg = mHandler.obtainMessage(GET_CIRCLE_PEOPLE_END);
                msg.getData().putBoolean(BasicActivity.RESULT, true);
                msg.getData().putInt("count", users.size());
                msg.getData().putInt("page", page);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mFollowerLock) {
                    inloadingFollower = false;
                }

                Message msg = mHandler.obtainMessage(GET_CIRCLE_PEOPLE_END);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(BasicActivity.RESULT, false);
                msg.sendToTarget();
            }
        });
    }

    private void loadCircleFriendsEnd(Message msg) {
        boolean sucadd = msg.getData().getBoolean(BasicActivity.RESULT, false);
        if (sucadd == true) {
            int count = msg.getData().getInt("count");
            int page = msg.getData().getInt("page");
            if (count > 0) {
                syncfriendsInfo(AccountServiceUtils.getBorqsAccountID(), page + 1, SYNC_FRIENDS_DEFAULT_COUNT, mCircleId);
            } else {
                onCircleMemberUpdated("");
            }
        } else {
            String ErrorMsg = msg.getData().getString(BasicActivity.ERROR_MSG);
            if (!TextUtils.isEmpty(ErrorMsg)) {
                Toast.makeText(mActivity, ErrorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    View.OnClickListener addPeopleClickListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            gotoPickCircleUserActivity(PickCircleUserActivity.type_add_friends);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        switch (requestCode) {
            case BasicActivity.userselectcode: {
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getStringExtra("address");
                    int type = data.getIntExtra("type", -1);
                    if (type == PickCircleUserActivity.type_delete_friends) {
                        circleUpdate(address, mCircleId, false);
                    } else if (type == PickCircleUserActivity.type_add_friends) {
                        circleUpdate(address, mCircleId, true);
                    }
                }
                break;
            }
        }
    }

    boolean inUsersSet;
    Object mLockUsersSet = new Object();

    /*
      * isadd means add/remove uid into circleid
      */
    public void circleUpdate(final String uid, final String circleid, final boolean isadd) {
        if (inUsersSet == true) {
            Toast.makeText(mActivity, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLockUsersSet) {
            inUsersSet = true;
        }
        showProcessDialog(R.string.set_user_process_title, false, true, true);

        asyncQiupu.usersSet(AccountServiceUtils.getSessionID(), uid, circleid, isadd, new TwitterAdapter() {
            public void usersSet(boolean result) {
                Log.d(TAG, "finish usersSet :" + result);

                if (result) {
                    CircleUtils.circleUpdateCallBack(uid, circleid, isadd, orm, mActivity);
                }

                Message msg = mHandler.obtainMessage(USER_SET_CIRCLE_END);
                msg.getData().putBoolean(BasicActivity.RESULT, result);
//                msg.getData().putBoolean("isadd", isadd);
//                msg.getData().putString("uid", uid);
                msg.sendToTarget();
                synchronized (mLockUsersSet) {
                    inUsersSet = false;
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockUsersSet) {
                    inUsersSet = false;
                }
                Message msg = mHandler.obtainMessage(USER_SET_CIRCLE_END);
                msg.getData().putBoolean(BasicActivity.RESULT, false);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.sendToTarget();
            }
        });
    }

    public void gotoPickCircleUserActivity(int type) {
        Intent intent = new Intent(mActivity, PickCircleUserActivity.class);
        intent.putExtra(PickCircleUserActivity.RECEIVER_TYPE, type);
        intent.putExtra(IntentUtil.EXTRA_KEY_CIRCLE_ID, mCircleId);
        startActivityForResult(intent, BasicActivity.userselectcode);
    }

    public void updateListUI(final boolean isfriendsList) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
//                if (mFriends != null)
//                    mFriends.close();
//
//                if (isfriendsList) {
//                    mFriends = orm.queryFriendsCursor();
//                } else {
//                    mFriends = orm.queryFriendsCursorByCircleId(mCircleId);
//                }
                queryFriends(null);
                mAllPeople.loadUserRefresh(mFriends, mAtoZ);
            }
        });
    }

    public void loadRefresh(final boolean isfriendsList) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (isfriendsList)
                    IntentUtil.loadUsersFromServer(mActivity);
                else
                    mHandler.obtainMessage(GET_CIRCLE_PEOPLE).sendToTarget();
            }
        }, 500);
    }

    public void switchCircle(long circleId) {
        mCircleId = String.valueOf(circleId);
        setAddPeopleButton();
//        if (circleId > 0) {
//            loadRefresh(false);
//        } else {
//            loadRefresh(true);
//        }
        onCircleMemberUpdated(null);
    }

    private void onCircleMemberUpdated(String filterText) {
    	queryFriends(filterText);
        mAllPeople.loadUserRefresh(mFriends, mAtoZ);
    }
    
    private void queryFriends(String filterText) {
    	if (mFriends != null)
    		mFriends.close();
    	
    	mFriends = isAllFriends() ? orm.queryFriendsCursor(filterText) :
    		orm.queryFriendsCursorByCircleId(mCircleId, filterText);
    }

    private boolean isAllFriends () {
        return TextUtils.isEmpty(mCircleId) || mCircleId.equals(String.valueOf(QiupuConfig.CIRCLE_ID_ALL));
    }

    private void setAddPeopleButton() {
        showAddPeopleButton(!isAllFriends(), addPeopleClickListener);
    }

    public void doSearch(String key) {
        Log.d(TAG, "doSearch: " + key);
        onCircleMemberUpdated(key);
        showSearchFromServerButton(key.length() > 0 ? true : false, key);
    }
}
