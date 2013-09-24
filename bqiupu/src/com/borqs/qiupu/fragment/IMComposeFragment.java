package com.borqs.qiupu.fragment;

import java.util.ArrayList;
import java.util.Map;

import twitter4j.ChatInfo;
import twitter4j.QiupuSimpleUser;
import twitter4j.QiupuUser;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.IMComposeAdapter;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.ConversationMultiAutoCompleteTextView;
import com.borqs.common.view.IMComposeItemView;
//import com.borqs.notification.INotificationService;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.service.IMReceiver;
import com.borqs.qiupu.service.IMReceiver.IMServiceListener;

public class IMComposeFragment extends BasicFragment implements IMServiceListener {
    private static final String TAG = "Qiupu.IMComposeFragment";

    public static final int FROM_TYPE = 0;
    public static final int TO_TYPE = 1;

    public static final int MAX_CACHE_CHATING_LENGTH = 5;
    private static final int MAX_CACHE_COUNT = 10;

    private ListView mListView;
    private ImageView postButton;
    private ConversationMultiAutoCompleteTextView editText;

    private Activity mActivity;
    private IMComposeAdapter mImComposeAdapter;

    private QiupuUser mUser;
    private QiupuUser mSelf;
    private String to_url;
    private ArrayList<ChatInfo> mChatList = new ArrayList<ChatInfo>();
    private ArrayList<ChatInfo> mBackupChatList = new ArrayList<ChatInfo>();
    private Cursor mCursor;
    private int mCount = 0;
    private QiupuORM orm;
    private int mRecentlyChatSize = 0;

    public IMComposeFragment() {
        
    }

    public IMComposeFragment(QiupuUser user, String to_image_url, String fromData) {
        mUser = user;
        to_url = to_image_url;
    }

    @Override
    public void onAttach(Activity activity) {
        mActivity = activity;
        super.onAttach(activity);

        orm = QiupuORM.getInstance(activity);
        mCursor = orm.queryChatRecord(String.valueOf(mUser.uid));

        if (mCursor != null && mCursor.getCount() > 0) {
            mCount = mCursor.getCount();
            int tmp = mCount%MAX_CACHE_CHATING_LENGTH;
            mRecentlyChatSize = tmp == 0 ? MAX_CACHE_CHATING_LENGTH : tmp;
        }

        long uid = AccountServiceUtils.getBorqsAccountID();
        QiupuSimpleUser user = orm.querySimpleUserInfo(uid);
        if (user != null) {
            mSelf = QiupuUser.instance(user);
        }

        IMReceiver.setUpdateAbsentUIListener(this);

        try{
            GetFragmentCallBack listener = (GetFragmentCallBack)activity;
            listener.getFragment(this);
        }catch (ClassCastException e) {
            Log.d(TAG, activity.toString() +  " must implement GetFragmentCallBack");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy() mBackupChatList.size() = " + mBackupChatList.size());

        if (mBackupChatList.size() > 0) {
            long numbers = orm.bulkInsertChatRecord(mBackupChatList);
            Log.d(TAG, "onDestroy() ======== numbers = " + numbers);
        }

        QiupuORM.closeCursor(mCursor);

        IMReceiver.setUpdateAbsentUIListener(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.im_compose_layout, null);
        view.findViewById(R.id.comments_mention_button).setVisibility(View.GONE);

        editText = (ConversationMultiAutoCompleteTextView) view.findViewById(R.id.compose_text);
        postButton = (ImageView) view.findViewById(R.id.comments_share_button);
        postButton.setOnClickListener(postListener);

        mListView = (ListView) view.findViewById(R.id.im_listview);
        mListView.setOnItemClickListener(mItemClickListener);

        Log.d(TAG, "onCreateView() mRecentlyChatSize = " + mRecentlyChatSize);
        mImComposeAdapter = new IMComposeAdapter(mActivity, orm.createChatInfoObject(mCursor, -1, mRecentlyChatSize));
        return view;
    }

    private View.OnClickListener postListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String myChat = getComposingText();

            try {
                sendInstanceMessage(myChat);
            } catch (IllegalStateException e) {
//                Toast.makeText(mActivity, R.string.connect_break, Toast.LENGTH_SHORT).show();
                Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            addNewChat(myChat, TO_TYPE, mSelf);

            editText.setText(null);
        }
    };

    private String getComposingText() {
        return ConversationMultiAutoCompleteTextView.getConversationText(editText);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addHeaderView();
        mListView.setAdapter(mImComposeAdapter);
    }

    private View headerView;
    private void addHeaderView() {
        if (mCursor != null && mCursor.getCount() >= MAX_CACHE_CHATING_LENGTH) {
            headerView = LayoutInflater.from(mActivity).inflate(R.layout.im_header_view, null);
            headerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) mActivity.getResources().getDimension(R.dimen.list_item_height)));
            TextView textView = (TextView) headerView.findViewById(R.id.batch_action_bar_tx);
            textView.setText(mActivity.getString(R.string.im_more));
            textView.setOnClickListener(checkMoreListener);
            mListView.addHeaderView(headerView);
        } else {
            Log.d(TAG, "chat count < " + MAX_CACHE_CHATING_LENGTH + ", no header view");
        }
    }

    private View.OnClickListener checkMoreListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showMoreHistoryRecord();
        }
    };

    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
            if (IMComposeItemView.class.isInstance(view)) {
                IMComposeItemView itemView = (IMComposeItemView) view;
            } else {
                showMoreHistoryRecord();
            }
        }
    };

    private void showMoreHistoryRecord() {
        ArrayList<ChatInfo> list = orm.createChatInfoObject(mCursor, mRecentlyChatSize, MAX_CACHE_CHATING_LENGTH);
        Log.d(TAG, "========== list.size() = " + list.size());
        for (ChatInfo info : list) {
            Log.d(TAG, "\n\n info = \n" + info.toString());
        }
        mListView.setSelection(mRecentlyChatSize);
        mImComposeAdapter.alterDataListMore(list);
        mRecentlyChatSize += MAX_CACHE_CHATING_LENGTH;
        if (mRecentlyChatSize == mCount) {
            mListView.removeHeaderView(headerView);
//            headerView.setVisibility(View.GONE);
        }
    }

    public interface GetFragmentCallBack {
        public void getFragment(IMComposeFragment fragment);
    }

    public void loadRefresh() {
    }

    public void refreshItemUI() {
    }

    @Override
    public void onResume() {
        super.onResume();

        bindService();
    }

    private void addNewChat(String message, int type, QiupuUser user) {
        if (mChatList != null && mChatList.size() != 0) {
            mChatList.clear();
        }

        ChatInfo item = new ChatInfo(mUser.uid, message, type, user.profile_image_url, false, System.currentTimeMillis(), user.nick_name);
        mChatList.add(item);
        mBackupChatList.add(item);
        mImComposeAdapter.alterDataList(mChatList);

        if (mBackupChatList.size() >= MAX_CACHE_COUNT) {
            ArrayList<ChatInfo> tmpList = (ArrayList<ChatInfo>) mBackupChatList.clone();
            mBackupChatList.clear();
            long numbers = orm.bulkInsertChatRecord(tmpList);
            Log.d(TAG, "addNewChat() ======== numbers = " + numbers);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

//        if (mNotificationService != null) {
//            mNotificationService = null;
//            mActivity.unbindService(mConnection);
//        }
    }

//    INotificationService mNotificationService;
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "Service has unexpectedly disconnected");
//            mNotificationService = null;
        }

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            // Following the example above for an AIDL interface,
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
//            mNotificationService = INotificationService.Stub.asInterface(arg1);
        }
    };

    void bindService() {
        try {
            Intent intent = new Intent();
            intent.setAction("com.borqs.notification.StartService");
            if (!mActivity.bindService(intent, mConnection, android.content.Context.BIND_AUTO_CREATE)) {
                Log.w(TAG, "bindService, failed.");
            }
        } catch (Exception ee) {
            Log.e(TAG, ee.getMessage());
        }
    }

    private void sendInstanceMessage(String message) {
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(mActivity, R.string.toast_invalid_input_content, Toast.LENGTH_SHORT).show();
            return;
        }

//        if (null != mUser && null != mNotificationService) {
//            try {
//                mNotificationService.sendMessage(IntentUtil.WUTONG_NOTIFICATION_ID,
//                        String.valueOf(mUser.uid), message);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void updateUI(Message msg) {
        addNewChat(msg.getData().getString("data"), 0, mUser);
    }

}
