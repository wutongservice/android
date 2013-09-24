package com.borqs.qiupu.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.BPCFriendsNewAdapter;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.ShakeActionListener;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.BpcFriendsItemView;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.ProgressInterface;
import com.borqs.qiupu.util.ToastUtil;
import twitter4j.*;
import twitter4j.conf.ConfigurationContext;

import java.util.ArrayList;

public class SuggestionListFragment extends PeopleListFragment implements UsersActionListner {
    private static final String TAG = "SuggestionListFragment";

    private static final int MAY_KNOW_INDEX = 0;
    private static final int MAX_KNOW_PEOPLE_COUNT = 40;

    private ArrayList<QiupuUser> mUserList;
    private BPCFriendsNewAdapter mUserAdapter;

    private ProgressDialog mDenyProgress;

    private AsyncQiupu asyncQiupu;
    private Handler mHandler;

    private boolean mHeaderAppended;
    private ShakeActionListener mShakeListener;

    public static SuggestionListFragment newInstance(boolean withHeader) {
        SuggestionListFragment instance = new SuggestionListFragment();
        instance.mHeaderAppended = withHeader;
        return instance;
    }

    public SuggestionListFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(ShakeActionListener.class.isInstance(activity)) {
            mShakeListener = (ShakeActionListener) activity;
            mShakeListener.getSuggestFragmentCallBack(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstancesState) {
        super.onCreate(savedInstancesState);
        asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null, null);
        mHandler = new MainHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View convertView = super.onCreateView(inflater, container, savedInstanceState);
        showAddPeopleButton(false, null);

        mUserList = new ArrayList<QiupuUser>();
        mUserAdapter = new BPCFriendsNewAdapter(mActivity, null, false, false);

        initPeopleListHeadView();

        mListView.setAdapter(mUserAdapter);
        mListView.setOnItemClickListener(mMayKnowListener);

        mDenyProgress = new ProgressDialog(mActivity);
        mDenyProgress.setMessage(getString(R.string.delete_circle_process));
        mDenyProgress.setCanceledOnTouchOutside(false);
        mDenyProgress.setIndeterminate(true);
        mDenyProgress.setCancelable(true);

        setData();

        QiupuHelper.registerUserListener(getClass().getName(), this);
        mUserAdapter.registerUsersActionListner(getClass().getName(), this);

        return convertView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        QiupuHelper.unregisterUserListener(getClass().getName());
        mUserAdapter.unregisterUsersActionListner(getClass().getName());
    }

    private void initPeopleListHeadView() {
        if (mHeaderAppended) {
            LayoutInflater factory = LayoutInflater.from(mActivity);
            View convertView = factory.inflate(R.layout.add_friends_header_view, null);
            if (null != convertView) {
                setHeaderViewClickListener(convertView);
                mListView.addHeaderView(convertView);
            }
        }
    }

    private void setHeaderViewClickListener(View convertView) {
        TextView recomment = (TextView) convertView.findViewById(R.id.recomment_view);
        recomment.setMovementMethod(LinkMovementMethod.getInstance());
        View fans = convertView.findViewById(R.id.fans_layout);
        fans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lunchMyFans();
            }
        });

        View contacts = convertView.findViewById(R.id.contacts_layout);
        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtil.lunchMyContact(mActivity);
            }
        });

        View invites = convertView.findViewById(R.id.invites_layout);
        invites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lunchMyInvitation();
            }
        });
    }

    private AdapterView.OnItemClickListener mMayKnowListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if (position == 0) {
            } else {
                lunchUserDetail(view);
            }
        }
    };

    private void lunchMyFans() {
        IntentUtil.showUserFansList(mActivity, AccountServiceUtils.getBorqsAccountID());
//        Intent intent = new Intent(mActivity, UsersListActivity.class);
//        Bundle bundle = BpcApiUtils.getUserBundle(AccountServiceUtils.getBorqsAccountID());
//        bundle.putInt(UsersListActivity.USER_TYPE, UsersListActivity.TYPE_USER_FANS);
//        intent.putExtras(bundle);
//        startActivity(intent);
    }

    private void lunchMyInvitation() {
        Intent intent = new Intent(mActivity, com.borqs.qiupu.ui.InviteContactActivity.class);
        startActivity(intent);
    }

    private void lunchUserDetail(View view) {
        if (BpcFriendsItemView.class.isInstance(view)) {
            BpcFriendsItemView fv = (BpcFriendsItemView) view;
            QiupuUser tmpUser = fv.getUser();
            IntentUtil.startUserDetailIntent(fv.getContext(), tmpUser);
        }
    }

    public static final int REFUSE_USER = 0x9;
    public static final int REFUSE_USER_END = 0x10;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MAY_KNOW_INDEX: {
                    lookupFriends(MAX_KNOW_PEOPLE_COUNT);
                    break;
                }
                case QiupuMessage.MESSAGE_LOAD_DATA_END: {
                    end();

                    if (msg.getData().getBoolean(BasicActivity.RESULT)) {
                        mUserAdapter.alterDataList(mUserList);
                    } else {
                        showCustomToast(msg.getData().getString(BasicActivity.ERROR_MSG));
                    }
                    break;
                }
                case REFUSE_USER: {
                    requestForConfirmation();
                    break;
                }
                case REFUSE_USER_END: {
                    if (mDenyProgress.isShowing()) {
                        mDenyProgress.hide();
                    }

                    boolean ret = msg.getData().getBoolean(BasicActivity.RESULT, false);
                    if (ret) {
                        long uid = msg.getData().getLong(BpcApiUtils.User.USER_ID);
                        onUserDismissed(uid);
                    } else {
                        ToastUtil.showOperationFailed(mActivity, mHandler, true,
                                msg.getData().getString(BasicActivity.ERROR_MSG));
                    }
                    break;
                }
                default:
            }
        }
    }

    private void requestForConfirmation() {
        DialogUtils.showConfirmDialog(mActivity, R.string.refuse_user_title,
                R.string.refuse_user_message, R.string.label_ok, R.string.label_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        refuseUserToServer(refuseUid);
                    }
                }, null);
    }

    private void setData() {
        if (mUserList.size() <= 0) {
            mHandler.obtainMessage(MAY_KNOW_INDEX).sendToTarget();
        }
    }

    private boolean inProcess = false;
    private final Object mLock = new Object();
    private void lookupFriends(int count) {
        synchronized (mLock) {
            if (inProcess == true) {
                Log.d(TAG, "in doing get friends data");
                return;
            }
        }

        begin();

        inProcess = true;
        asyncQiupu.getUserYouMayKnow(AccountServiceUtils.getSessionID(), count, false,
                new TwitterAdapter() {
                    public void getUserYouMayKnow(ArrayList<QiupuUser> users) {
                        Log.d(TAG, "finish get users you may know! user.size() = " + users.size());

                        if (users.size() > 0) {
                            mUserList.clear();
                            mUserList.addAll(users);
                        }

                        Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_LOAD_DATA_END);
                        msg.getData().putBoolean(BasicActivity.RESULT, true);
                        msg.sendToTarget();
                        synchronized (mLock) {
                            inProcess = false;
                        }
                    }

                    public void onException(TwitterException ex,
                                            TwitterMethod method) {
                        TwitterExceptionUtils.printException(TAG, "getUserYouMayKnow, server exception:",
                                ex, method);
                        synchronized (mLock) {
                            inProcess = false;
                        }
                        Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_LOAD_DATA_END);
                        msg.getData().putBoolean(BasicActivity.RESULT, false);
                        msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                        msg.sendToTarget();
                    }
                });
    }

    @Override
    public void updateItemUI(QiupuUser user) {
        long uid = user.uid;
        if (uid > 0) {
            onUserDismissed(uid);
        }
    }

    @Override
    public void addFriends(QiupuUser user) {
        IntentUtil.startCircleSelectIntent(mActivity, user.uid, null);
    }

    @Override
    public void refuseUser(long uid) {
        refuseuser(uid);
    }

    private long refuseUid;

    public void refuseuser(long uid) {
        refuseUid = uid;
        mHandler.obtainMessage(REFUSE_USER).sendToTarget();
    }

    @Override
    public void deleteUser(QiupuUser user) {

    }

    @Override
    public void sendRequest(QiupuUser user) {

    }

    private void onUserDismissed(long uid) {
        if (null != mUserList) {
            final int count = mUserList.size();
            boolean isUserRemoved = false;
            QiupuUser user;
            for (int i = 0; i < count; ++i) {
                user = mUserList.get(i);
                if (uid == user.uid) {
                    mUserList.remove(i);
                    isUserRemoved = true;
                    break;
                }
            }
            Log.d("test", "isUserRemoved = " + isUserRemoved);
            if (isUserRemoved) {
                mUserAdapter.alterDataList(mUserList);
            }
        }
    }

    boolean inRefuseUser;
    Object mLockRefuseUser = new Object();

    protected void refuseUserToServer(final long uid) {
        if (inRefuseUser == true) {
            showCustomToast(R.string.string_in_processing);
            return;
        }

        synchronized (mLockRefuseUser) {
            inRefuseUser = true;
        }

        mDenyProgress.show();

        asyncQiupu.refuseUser(AccountServiceUtils.getSessionID(), uid, new TwitterAdapter() {
            public void refuseUser(boolean suc) {
                Log.d(TAG, "finish Refuse user=" + suc);

                Message msg = mHandler.obtainMessage(REFUSE_USER_END);
                msg.getData().putLong(BpcApiUtils.User.USER_ID, uid);
                msg.getData().putBoolean(BasicActivity.RESULT, suc);
                msg.sendToTarget();
                synchronized (mLockRefuseUser) {
                    inRefuseUser = false;
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockRefuseUser) {
                    inRefuseUser = false;
                }
                Message msg = mHandler.obtainMessage(REFUSE_USER_END);
                msg.getData().putBoolean(BasicActivity.RESULT, false);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.sendToTarget();
            }
        });
    }

    public void loadRefresh() {
        mHandler.obtainMessage(MAY_KNOW_INDEX).sendToTarget();
    }
}
