package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import com.borqs.qiupu.ui.BasicNavigationActivity;
import twitter4j.QiupuUser;
import twitter4j.Requests;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.database.Cursor;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.BorqsAccount;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.RequestContactInfoSimpleView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.UsersColumns;
import com.borqs.qiupu.service.FriendsManager;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.service.RequestsService;

public class BpcExchangeCardActivity extends BasicNavigationActivity implements
            RequestsService.RequestListener, FriendsManager.FriendsServiceListener {


    private final static String  TAG = "Qiupu.BpcExchangeCardActivity";

    private QiupuUser mUser;
    private TextView request;
    private TextView sendTextView;
//    private TextView receiveTextView;
    private TextView cardTextView;
    private View requestItem;
//    private ArrayList<Requests> requestList;
    private int request_count = 0;
    private boolean request_registered = false;
    private int sendCardCount = 0;
//    private int receivedCardCount = 0;
    private int vCardCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        enableLeftNav();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.exchange_card_layout);
        setHeadTitle(R.string.profile_control_label);

        request_count = getIntent().getIntExtra("request_count", 0);
//        requestList = (ArrayList<Requests>) getIntent().getSerializableExtra("request_list");

//        if (requestList != null && requestList.size() > 0) {
//            request_count = requestList.size();
//            Log.d(TAG, "requestList.size() = " + request_count);
//        }

        if (request_count <= 0) {
            request_registered = true;
            registerQiupuRequestListener();
        }

        obtainUser();
        setActionListener();

        showLeftActionBtn(true);
        showMiddleActionBtn(false);
        showRightActionBtn(false);

//        RequestFragment.setUpdateExchangeVcardUIListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FriendsManager.registerFriendsServiceListener(getClass().getName(), this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FriendsManager.unregisterFriendsServiceListener(getClass().getName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (request_registered) {
            unRegisterQiupuRequestListener();
        }
    }

    private void setActionListener() {
        TextView invite = (TextView) findViewById(R.id.invite_btn);
        TextView nfc = (TextView) findViewById(R.id.nfc_btn);
        TextView shake = (TextView) findViewById(R.id.shake_btn);

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD 
                && NfcAdapter.getDefaultAdapter(this) != null) {
            nfc.setVisibility(View.VISIBLE);
        } else {
            nfc.setVisibility(View.GONE);
        }

//        shake.setVisibility(View.GONE);

        invite.setOnClickListener(inviteListener);
        nfc.setOnClickListener(nfcListener);
        shake.setOnClickListener(shakeListener);

        requestItem = findViewById(R.id.invite_container);
        requestItem.setOnClickListener(requestListener);

        View sendView = findViewById(R.id.send_card_container);
        sendView.setOnClickListener(sendCardListener);

        View receiveView = findViewById(R.id.receive_card_container);
        receiveView.setOnClickListener(receiveCardListener);

        View vCardView = findViewById(R.id.card_holder_container);
        vCardView.setOnClickListener(vCardListener);
    }

    private View.OnClickListener requestListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            IntentUtil.startRequestActivity(BpcExchangeCardActivity.this, request_count);
        }
    };

    private View.OnClickListener inviteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            IntentUtil.showExchangeVcardListActivity(BpcExchangeCardActivity.this);
            IntentUtil.startPickUserActivity(BpcExchangeCardActivity.this, uids.toString());
        }
    };

    private View.OnClickListener nfcListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            IntentUtil.startBeamDataByNFCActivity(BpcExchangeCardActivity.this);
        }
    };

    private View.OnClickListener shakeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            IntentUtil.showShakingActivity(BpcExchangeCardActivity.this);
        }
    };

    private View.OnClickListener sendCardListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            IntentUtil.showExchangedUserList(BpcExchangeCardActivity.this);
            IntentUtil.showExchangeCardFriendsActivity(BpcExchangeCardActivity.this, getString(R.string.send_card_title));
        }
    };

    private View.OnClickListener receiveCardListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            IntentUtil.showExchangedUserList(BpcExchangeCardActivity.this);
            IntentUtil.showExchangeCardFriendsActivity(BpcExchangeCardActivity.this, getString(R.string.received_card_friends));
        }
    };

    private View.OnClickListener vCardListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            IntentUtil.showExchangedUserList(BpcExchangeCardActivity.this);
            IntentUtil.showExchangeCardFriendsActivity(BpcExchangeCardActivity.this, getString(R.string.card_holder_title));
        }
    };

    private void getCount() {
        fetchSendCardCount();
//        fetchReceivedCardCount();
        fetchCardCount();
    }

    private void fetchSendCardCount() {
        Cursor cursor = null;
        try {
            cursor = QiupuORM.getInstance(this).queryFriendsHaveSentCard(String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE));
            if (cursor != null && cursor.getCount() > 0) {
                sendCardCount = cursor.getCount();
                cursor.moveToFirst();
                do {
                    uids.append(cursor.getLong(cursor.getColumnIndex(UsersColumns.USERID))).append(",");
                } while(cursor.moveToNext());
            }

        } finally {
            QiupuORM.closeCursor(cursor);
        }
    }

//    private void fetchReceivedCardCount() {
//        Cursor cursor = null;
//        try {
//            cursor = QiupuORM.getInstance(this).queryFriendsHaveReceivedCard(String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE));
//            if (cursor != null && cursor.getCount() > 0) {
//                receivedCardCount = cursor.getCount();
//            }
//        } finally {
//            QiupuORM.closeCursor(cursor);
//        }
//    }

    private StringBuffer uids = new StringBuffer();
    private void fetchCardCount() {
        Cursor cursor = null;
        try {
            cursor = QiupuORM.getInstance(this).queryFriendsHaveReceivedCard(String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE));
            if (cursor != null && cursor.getCount() > 0) {
                vCardCount = cursor.getCount();
                cursor.moveToFirst();
                do {
                    uids.append(cursor.getLong(cursor.getColumnIndex(UsersColumns.USERID))).append(",");
                } while(cursor.moveToNext());
            }

        } finally {
            QiupuORM.closeCursor(cursor);
        }
    }

    private void obtainUser() {
        getCount();

        BorqsAccount mBorqsAccount = AccountServiceUtils.getBorqsAccount();
        if (mBorqsAccount != null) {
            mUser = orm.queryOneUserInfo(mBorqsAccount.uid);
            Log.d(TAG, "========= mUser = " + mUser);
            if (mUser == null) {
                Log.d(TAG, "get user info from server, borqsAccount.uid = " + mBorqsAccount.uid);
                getUserInfo(mBorqsAccount.uid);
            } else {
                setUI(mUser);
            }
        }
    }

    @Override
    protected void loadRefresh() {
        begin();
        IntentUtil.loadUsersFromServer(this);
    }

    private Object mInfoLock = new Object();
    private boolean inInfoProcess = false;
    private void getUserInfo(final long userid) {
        if (!AccountServiceUtils.isAccountReady()) {
            return;
        }

        synchronized (mInfoLock) {
            if (inInfoProcess == true) {
                Log.d(TAG, "in loading info data");
                return;
            }
        }

        if(isUiLoading() == false){
            begin();
        }

        synchronized (mInfoLock) {
            inInfoProcess = true;
        }
        asyncQiupu.getUserInfo(userid, getSavedTicket(),new TwitterAdapter() {
            public void getUserInfo(QiupuUser user) {
                Log.d(TAG, "finish getUserInfo=" + user);
                mUser = user;

                synchronized (mInfoLock) {
                    inInfoProcess = false;
                }

                // update database
                if((user.circleId != null && user.circleId.length() > 0)
                         || userid == AccountServiceUtils.getBorqsAccountID()) {
                    orm.insertUserinfo(user);
                    // create share source data, need it is my friends.
                    if(mUser.circleId != null && mUser.circleId.length() > 0) {
                        orm.updateShareSourceDB(userid, user.sharedResource);
                    }
                }

                Message msg = mHandler.obtainMessage(GET_USER_FROM_SERVER_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {

                synchronized (mInfoLock) {
                    inInfoProcess = false;
                }
                TwitterExceptionUtils.printException(TAG, "getUserInfo, server exception:", ex, method);
                Log.d(TAG, "fail to load user info=" + ex.getMessage());

                Message msg = mHandler.obtainMessage(GET_USER_FROM_SERVER_END);
                msg.getData().putString(ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }

    private boolean isUiLoading(){
        if(inInfoProcess){
            return true;
        }else{
            return false;
        }
    }

    private void setUI(QiupuUser user) {
        if (user == null) {
            Log.d(TAG, "setUI() : error, user is null");
            return;
        }

        View viewLayout = findViewById(R.id.my_vcard_layout);
        viewLayout.setVisibility(View.VISIBLE);
        View view = viewLayout.findViewById(R.id.id_vcard_rl);
        view.setVisibility(View.VISIBLE);

        request = (TextView) findViewById(R.id.invite_count_text);
        sendTextView = (TextView) findViewById(R.id.send_count_text);
//        receiveTextView = (TextView) findViewById(R.id.receive_count_text);
        cardTextView = (TextView) findViewById(R.id.holder_count_text);

        Log.d(TAG, "setUI() request_count = " + request_count);
        if (request_count > 0) {
            request.setText(String.valueOf(request_count));
//            request.setTextColor(R.color.red);
//            request.setClickable(true);
        } else {
//            request.setClickable(false);
        }

//        if (sendCardCount > 0) {
            sendTextView.setText(String.valueOf(sendCardCount));
//        }

//        if (receivedCardCount > 0) {
//            receiveTextView.setText(String.valueOf(receivedCardCount));
//        }

//        if (vCardCount > 0)  {
            cardTextView.setText(String.valueOf(vCardCount));
//        }

        view.findViewById(R.id.import_contact).setVisibility(View.GONE);
        ImageView photo = (ImageView) view.findViewById(R.id.id_user_icon);
        TextView name = (TextView) view.findViewById(R.id.id_user_name);
        LinearLayout vcard = (LinearLayout) view.findViewById(R.id.id_vcard);

        setImageUI(photo, user.profile_image_url);
        name.setText(user.nick_name);

        String contactWay = getResources().getString(R.string.phone_way);
        if(user.phoneList != null && user.phoneList.size() > 0) {
            for(int i=0; i<user.phoneList.size(); i++ ){
                String content = user.phoneList.get(i).info;
                setContactWayUI(vcard, content, contactWay + content);
            }
        }

        contactWay = getResources().getString(R.string.email_way);
        if(user.emailList != null && user.emailList.size() > 0) {
            for(int i=0; i<user.emailList.size(); i++ ){
                String content = user.emailList.get(i).info;
                setContactWayUI(vcard, content, contactWay + content);
            }
        }
    }

    private void setImageUI(ImageView profile_img_ui, String image_url) {
        profile_img_ui.setVisibility(View.VISIBLE);

        ImageRun imagerun = new ImageRun(mHandler, image_url, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
        imagerun.noimage = true;
        imagerun.addHostAndPath = true;
        imagerun.setRoundAngle=true;
        imagerun.setImageView(profile_img_ui);
        imagerun.post(null);
    }

    private void setContactWayUI(LinearLayout vcard, String userContact, String contactWay) {
        if (!TextUtils.isEmpty(userContact)) {
            RequestContactInfoSimpleView info = new RequestContactInfoSimpleView(this, contactWay);
            vcard.addView(info);
        }
    }

    private static final int GET_USER_FROM_SERVER_END = 1;
    private static final int LOAD_FRIENDS_CALLBACK = 2;
    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_USER_FROM_SERVER_END:{
                    boolean succeed = msg.getData().getBoolean(RESULT, false);
                    if (succeed) {
                        end();
                        setUI(mUser);
                    } else {
                        String errorMsg = msg.getData().getString(ERROR_MSG);
                        if(TextUtils.isEmpty(errorMsg) == false) {
                            showCustomToast(errorMsg);
                        }
                    }
                    break;
                }
                case LOAD_FRIENDS_CALLBACK: {
                    int statuscode = msg.getData().getInt("statuscode");
                    if (statuscode == FriendsManager.STATUS_DO_FAIL) {
                        end();
                    } else if (statuscode == FriendsManager.STATUS_DOING) {
                        begin();
                    } else if (statuscode == FriendsManager.STATUS_DO_OK) {
                        end();
                        getCount();
                        if (sendTextView != null) {
                            sendTextView.setText(String.valueOf(sendCardCount));
                        }
//                        receiveTextView.setText(String.valueOf(receivedCardCount));
                        if (cardTextView != null) {
                            cardTextView.setText(String.valueOf(vCardCount));
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

//    @Override
//    public void setLeftMenuPosition() {
//        mPosition = LeftMenuMapping.getPositionForActivity(this);
//        mTitle = getString(R.string.exchange_card);
//    }

    private void registerQiupuRequestListener() {
        if (QiupuService.mRequestsService != null) {
            QiupuService.mRequestsService.regiestRequestListener(BpcExchangeCardActivity.class.getName(),
                    this);
            QiupuService.mRequestsService.rescheduleRequests(true);
        } else {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    QiupuService.mRequestsService.regiestRequestListener(
                            BpcExchangeCardActivity.class.getName(),
                            BpcExchangeCardActivity.this);
                    QiupuService.mRequestsService.rescheduleRequests(true);
                }
            }, 5 * QiupuConfig.A_SECOND);
        }
    }

    private void unRegisterQiupuRequestListener() {
        if(QiupuService.mRequestsService != null) {
            QiupuService.mRequestsService.unRegiestRequestListener(BpcExchangeCardActivity.class.getName());
        }
    }

    @Override
    public void requestUpdated(final ArrayList<Requests> data) {
        if (data != null && data.size() > 0) {
            Log.d(TAG, "all request count = " + data.size());
            int exchange_count = 0;
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).type == Requests.REQUEST_TYPE_EXCHANGE_VCARD) {
                    exchange_count++;
                }
            }
            request_count = exchange_count;
        }

        mHandler.post(new Runnable(){
            @Override
            public void run() {
                if (data != null) {
                    Log.d(TAG, "====== requestUpdated() data.size() = " + data.size());
                    if (request_count > 0) {
                        if (request != null) {
                            request.setText(String.valueOf(request_count));
//                            request.setTextColor(R.color.red);
                        }

                        if (requestItem != null) {
                            requestItem.setClickable(true);
                        }
                    } else {
                        if (request != null) {
                            request.setTextColor(R.color.black);
                            request.setText("0");
                        }

                        if (requestItem != null) {
                            requestItem.setClickable(false);
                        }
                    }
                } else {
                    Log.d(TAG, "====== requestUpdated() data == null");
                }
            }
        });
    }

    @Override
    public void updateUI(int msgcode, Message message) {
        Message msg = mHandler.obtainMessage(LOAD_FRIENDS_CALLBACK);
        msg.getData().putInt("statuscode", msgcode);
        msg.sendToTarget();
    }
}
