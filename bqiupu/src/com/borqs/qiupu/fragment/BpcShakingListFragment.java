package com.borqs.qiupu.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import twitter4j.QiupuUser;
import twitter4j.Requests;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.borqs.common.adapter.BpcShakingListAdapter;
import com.borqs.common.listener.ShakeListActionListener;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.AsyncApiUtils.AsyncApiSendRequestCallBackListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.BpcShakingItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;

public class BpcShakingListFragment extends PeopleListFragment implements
    UsersActionListner, AsyncApiSendRequestCallBackListener {
    private static final String TAG = "Qiupu.BpcShakingListActivity";

    private BpcShakingListAdapter mBpcShakingListAdapter;

    private static final int SEND_REQUEST_END = 100;
    private Handler mHandler;
    private ProgressDialog mprogressDialog;
    private ArrayList<QiupuUser> mLBSList;
    private ShakeListActionListener mShakeListListener;

    public BpcShakingListFragment() {
        
    }

    public BpcShakingListFragment(ArrayList<QiupuUser> lbsList) {
        mLBSList = lbsList;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;

        if(ShakeListActionListener.class.isInstance(activity)) {
            mShakeListListener = (ShakeListActionListener) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View convertView = super.onCreateView(inflater, container, savedInstanceState);
        return convertView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHandler = new MainHandler();

        mBpcShakingListAdapter = new BpcShakingListAdapter(mActivity);
        mListView.setAdapter(mBpcShakingListAdapter);

        if (mLBSList != null && mLBSList.size() > 0) {
            mBpcShakingListAdapter.alterDataList(mLBSList);
            mListView.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "error, find nobody, it should not happen");
        }

        mListView.setOnItemClickListener(null);

        mBpcShakingListAdapter.registerUsersActionListner(getClass().getName(), this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mBpcShakingListAdapter.unregisterUsersActionListner(getClass().getName());
    }

    public void updateActivityUI(final QiupuUser user) {

        synchronized (QiupuHelper.userlisteners) {
            Set<String> set = QiupuHelper.userlisteners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                UsersActionListner listener = QiupuHelper.userlisteners.get(key).get();
                if (listener != null && !BpcShakingListFragment.class.isInstance(listener)) {
                    listener.updateItemUI(user);
                }
            }
        }
    }

    @Override
    public void sendRequest(QiupuUser user) {
//        AsyncApiUtils.sendApproveRequest(user.uid, "", new AsyncQiupu(ConfigurationContext.getInstance(), null, null), this);
//        sendApproveRequest(String.valueOf(user.uid), "");
        mShakeListListener.sendRequestInFragment(user);
//        exchangeVcard(user.uid, CircleUtils.getDefaultCircleId(), CircleUtils.getDefaultCircleName(getResources()));
    }


    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_REQUEST_END: {
                    try {
                        mprogressDialog.dismiss();
                        mprogressDialog = null;
                    } catch (Exception ne) {}

                    boolean ret = msg.getData().getBoolean("result", false);
                    if (ret) {
                        long uid = msg.getData().getLong("uid");
                        afterSendProfileAccessRequest(uid, true);
                    } else {
                        Log.d(TAG, "exchange vcard failed");
                    }
                    break;
                }
                default:
                    Log.d(TAG, "handleMessage() default case.");
                    break;
            }
        }
    }

    private void afterSendProfileAccessRequest(long uid, boolean requested) {
        for (int j = 0; j < mListView.getChildCount(); j++) {
            View v = mListView.getChildAt(j);
            if (BpcShakingItemView.class.isInstance(v)) {
                BpcShakingItemView shakeView = (BpcShakingItemView) v;
                QiupuUser user = shakeView.getUser();
                if (user != null && user.uid == uid) {
                    String requestid = user.pedding_requests;
                    user.pedding_requests = Requests.getrequestTypeIds(requestid);
                    shakeView.setUser(user);
                    shakeView.refreshUI();
                    break;
                }
            }
        }
    }

    private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable) {
        mprogressDialog = DialogUtils.createProgressDialog(mActivity,
                resId, CanceledOnTouchOutside, Indeterminate, cancelable);
        mprogressDialog.show();
    }

    @Override
    public void sendRequestCallBackBegin() {
        showProcessDialog(R.string.request_process_title, false, true, true);
    }

    @Override
    public void sendRequestCallBackEnd(boolean result, long uid) {
        Log.d(TAG, "sendRequestCallBackEnd() uid = " + uid);
        Message message = mHandler.obtainMessage(SEND_REQUEST_END);
        message.getData().putBoolean("result", result);
        message.getData().putLong("uid", uid);
        message.sendToTarget();
    }

    public void updateShakeListItemUI(boolean result, long uid) {
        Log.d(TAG, "updateShakeListItemUI() uid = " + uid);
        Message message = mHandler.obtainMessage(SEND_REQUEST_END);
        message.getData().putBoolean("result", result);
        message.getData().putLong("uid", uid);
        message.sendToTarget();
    }

    @Override
    public void updateItemUI(QiupuUser user) {
        Log.d(TAG, "updateItemUI() user = " + user.toString());
    }

    @Override
    public void addFriends(QiupuUser user) {
        Log.d(TAG, "addFriends() user.uid = " + user.uid);
        IntentUtil.startCircleSelectIntent(mActivity, user.uid, null);
    }

    @Override
    public void refuseUser(long uid) {
    }

    @Override
    public void deleteUser(QiupuUser user) {
    }

    public void updateItemUI(long uid, boolean result) {
        if (result) {
            for (int j = 0; j < mListView.getChildCount(); j++) {
                View v = mListView.getChildAt(j);
                if (BpcShakingItemView.class.isInstance(v)) {
                    BpcShakingItemView shakeView = (BpcShakingItemView) v;
                    QiupuUser user = shakeView.getUser();
                    if (user != null && user.uid == uid) {
                        String requestid = user.pedding_requests;
                        user.pedding_requests = Requests.getrequestTypeIds(requestid);
                        user.circleId = "5";
                        shakeView.setUser(user);
                        shakeView.refreshUI();
                        break;
                    }
                }
            }
        } else {
            //TODO: show toast to report error info.
        }
    }
}
