package com.borqs.qiupu.ui.bpc;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import twitter4j.QiupuUser;
import twitter4j.Requests;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.BPCFriendsNewAdapter;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.AsyncApiUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.AtoZ;
import com.borqs.common.view.BpcFriendsItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM.UsersColumns;
import com.borqs.qiupu.ui.BasicActivity;

public class UsersCursorListActivity extends BasicActivity implements
        AsyncApiUtils.AsyncApiSendRequestCallBackListener,
        UsersActionListner, AtoZ.MoveFilterListener {
    private static final String TAG = "Qiupu.UsersCursorListActivity";

    private ListView mListView;
    private int mCircleId;
    private BPCFriendsNewAdapter mbpcFriendsAdapter;
    private Cursor mUserList;
    private Cursor mInLineSearch;

    private int page = 0;
    private int limit = 100;

    private View id_add_people;
    private TextView id_head_intutorial;
    private View search_span;
    private EditText keyEdit;
    private AtoZ mAtoZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        processIntent(getIntent());

        setContentView(R.layout.user_list_ui);
        QiupuHelper.registerUserListener(getClass().getName(), this);

        mListView = (ListView) findViewById(R.id.friends_list);
        AtoZ atoz = (AtoZ) findViewById(R.id.atoz);
        if (atoz != null) {
            mAtoZ = atoz;
            atoz.setFocusable(true);
            atoz.setMoveFilterListener(this);
            atoz.setVisibility(View.VISIBLE);
            mAtoZ.setListView(mListView);
        }

        mbpcFriendsAdapter = new BPCFriendsNewAdapter(this, null, false, false);
        mListView.setAdapter(mbpcFriendsAdapter);
        mListView.setOnItemClickListener(new FriendsItemClickListener());
        mListView.setOnCreateContextMenuListener(this);

        id_add_people = findViewById(R.id.id_add_people);
        id_add_people.setOnClickListener(this);

        id_head_intutorial = (TextView) findViewById(R.id.id_head_intutorial);
        search_span = findViewById(R.id.search_span);

        keyEdit = (EditText) findViewById(R.id.search_span);
        if (null != keyEdit) {
            keyEdit.addTextChangedListener(new MyWatcher());
        }

        enableLeftActionBtn(false);

        alterMiddleActionBtnByComposer(R.drawable.cmcc_icon_choice_of, editClickListener);
        overrideRightActionBtn(R.drawable.actionbar_icon_release_normal, composeClickListener);
        setHeadTitle(R.string.exchange_send_header_title);
        loadUserFromDb();
        if (mUserList != null && mUserList.getCount() <= 0) {
            mHandler.obtainMessage(GET_CIRCLE_PEOPLE).sendToTarget();
        }
        id_head_intutorial.setVisibility(View.VISIBLE);
        search_span.setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_view).setVisibility(View.VISIBLE);
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
    protected void getUserInfoEndCallBack(List<QiupuUser> users, boolean isfollowing) {
    }

    @Override
    protected void doGetFriendsEndCallBack(Message msg) {
        boolean sucadd = msg.getData().getBoolean(RESULT, false);
        if (sucadd == true) {
            int count = msg.getData().getInt("count");
            int page = msg.getData().getInt("page");
            if (count > 0) {
                //load next page
                syncfriendsInfo(AccountServiceUtils.getBorqsAccountID(), page + 1, 100, String.valueOf(mCircleId), true);
            }

            if (mUserList != null) {
                mUserList.close();
            }

            mUserList = orm.queryFriendsHaveSentCard(String.valueOf(mCircleId));
            refreshPeopleUI(mUserList);
        } else {
            String ErrorMsg = msg.getData().getString(ERROR_MSG);
            if (isEmpty(ErrorMsg) == false) {
                showCustomToast(ErrorMsg);
            }
        }
    }

    private void refreshPeopleUI(Cursor users) {
        mbpcFriendsAdapter.alterDataList(users, mAtoZ);
    }

    @Override
    public void sendRequestCallBackBegin() {
        begin();
    }

    @Override
    public void sendRequestCallBackEnd(boolean result, final long uid) {
        end();

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

    private static final int GET_CIRCLE_PEOPLE = 1;
    private static final int SEND_REQUEST_END = 0x13;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_CIRCLE_PEOPLE: {
                    syncfriendsInfo(AccountServiceUtils.getBorqsAccountID(), page, limit, String.valueOf(mCircleId), true);
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
            }
        }
    }

    private void onVcardExchanged(long uid) {
        for (int j = 0; j < mListView.getChildCount(); j++) {
            View v = mListView.getChildAt(j);
            if (BpcFriendsItemView.class.isInstance(v)) {
                BpcFriendsItemView fv = (BpcFriendsItemView) v;
                QiupuUser user = fv.getUser();
                if (user != null && user.uid == uid) {
                    String requestid = user.pedding_requests;
                    user.pedding_requests = Requests.getrequestTypeIds(requestid);
                    orm.setRequestUser(user.uid, user.pedding_requests);
                    fv.refreshUI();
                    break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        QiupuHelper.unregisterUserListener(getClass().getName());
        if (mUserList != null)
            mUserList.close();

        if (mInLineSearch != null)
            mInLineSearch.close();
    }

    private void updateActivityUI(final QiupuUser user) {

        synchronized (QiupuHelper.userlisteners) {
            Set<String> set = QiupuHelper.userlisteners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                UsersActionListner listener = QiupuHelper.userlisteners.get(key).get();
                if (listener != null && !UsersCursorListActivity.class.isInstance(listener)) {
                    listener.updateItemUI(user);
                }
            }
        }
    }

    public void updateItemUI(QiupuUser user) {
        if (mUserList != null)
            mUserList.close();

        mUserList = orm.queryFriendsCursorByCircleId(String.valueOf(mCircleId));
        refreshPeopleUI(mUserList);
    }

    protected Method failCallFollowerMethod;

    protected void SubApplicationPage() {
        if (page < 0)
            page = 0;
    }

    protected void callFailLoadFollowerMethod() {
        try {
            if (failCallFollowerMethod != null) {
                failCallFollowerMethod.invoke(this, (Object[]) null);
            }
        } catch (Exception ne) {
        }
    }

    public OnClickListener loadOlderClick = new OnClickListener() {
        public void onClick(View v) {
        }
    };

    @Override
    protected void loadRefresh() {
        mHandler.obtainMessage(GET_CIRCLE_PEOPLE).sendToTarget();
    }

    @Override
    public boolean isInProcess() {
        return false;
    }

    boolean isdelete = true;

    OnClickListener editClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            alterMiddleActionBtnByComposer(isdelete ? R.drawable.cmcc_icon_view_switching : R.drawable.cmcc_icon_choice_of, editClickListener);
            mbpcFriendsAdapter.setIsdeleteMode(isdelete);
            mbpcFriendsAdapter.notifyDataSetChanged();
            isdelete = !isdelete;
        }
    };

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.id_add_people) {
            Intent intent = new Intent(this, PickCircleUserActivity.class);
            intent.putExtra(PickCircleUserActivity.RECEIVER_TYPE, PickCircleUserActivity.type_add_friends);
            intent.putExtra(IntentUtil.EXTRA_KEY_CIRCLE_ID, String.valueOf(mCircleId));
            this.startActivityForResult(intent, userselectcode);
        } else {
        }
    }

    public void addFriends(QiupuUser user) {
        IntentUtil.startCircleSelectIntent(this, user.uid, null);
    }

    public void refuseUser(long uid) {
    }

    private class MyWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            //do search
            doSearch(s.toString());
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    private void doSearch(String key) {
        if (mInLineSearch != null)
            mInLineSearch.close();

        if (key != null && key.length() > 0) {
            mInLineSearch = orm.searchFriendsCursorByCircleId(String.valueOf(mCircleId), key);

            //show UI
            refreshPeopleUI(mInLineSearch);
        } else {
            loadUserFromDb();
        }
    }

    @Override
    protected void doUsersSetCallBack(String uid, boolean isadd) {
        loadUserFromDb();
        updateActivityUI(null);
    }

    private void loadUserFromDb() {
        if (mUserList != null)
            mUserList.close();

        mUserList = orm.queryFriendsCursorByCircleId(String.valueOf(mCircleId));
        refreshPeopleUI(mUserList);
    }

    @Override
    public void deleteUser(QiupuUser user) {
        // TODO Auto-generated method stub
        deleteUser(String.valueOf(user.uid), String.valueOf(mCircleId));
    }

    @Override
    public void sendRequest(QiupuUser user) {
        AsyncApiUtils.sendApproveRequest(user.uid, "", asyncQiupu, this);
    }

    OnClickListener composeClickListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            IntentUtil.startComposeIntent(arg0.getContext(), getAddress(), true, null);
        }
    };

    private String getAddress() {
        StringBuilder address = new StringBuilder();
        Cursor users = orm.queryFriendsCursorByCircleId(String.valueOf(mCircleId));
        if (users != null && users.getCount() > 0) {
            while (users.moveToNext()) {
                if (address.length() > 0) {
                    address.append(',');
                }
                address.append(users.getLong(users.getColumnIndex(UsersColumns.USERID)));
            }
        }
        users.close();

        return address.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case userselectcode: {
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getStringExtra("address");
                    circleUpdate(address, String.valueOf(mCircleId), true);
                }
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void beginMove() {
        // TODO Auto-generated method stub

    }

    @Override
    public void endMove() {
        // TODO Auto-generated method stub

    }

    @Override
    public void enterPosition(String alpha, int position) {
        mListView.setSelection(position);
    }

    @Override
    public void leavePosition(String alpha) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (null != bundle) {
            mCircleId = bundle.getInt(IntentUtil.EXTRA_KEY_CIRCLE_ID, QiupuConfig.CIRCLE_ID_ALL);
        } else {
            mCircleId = QiupuConfig.CIRCLE_ID_ALL;
        }
    }

    public static void showUserCursorList(Context context) {
        Intent intent = new Intent(context, UsersCursorListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(IntentUtil.EXTRA_KEY_CIRCLE_ID, QiupuConfig.ADDRESS_BOOK_CIRCLE);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
}
