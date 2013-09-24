package com.borqs.qiupu.fragment;

import java.util.ArrayList;

import twitter4j.AsyncQiupu;
import twitter4j.QiupuUser;
import twitter4j.Requests;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.common.adapter.BPCFriendsNewAdapter;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.AsyncApiUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.AtoZ;
import com.borqs.common.view.BpcFriendsItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.service.FriendsManager;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.service.RequestsService;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.ToastUtil;

public class ExchangeCardFragment extends BasicFragment implements UsersActionListner,
        AsyncApiUtils.AsyncApiSendRequestCallBackListener,
        RequestsService.RequestListener,
        AtoZ.MoveFilterListener, FriendsManager.FriendsServiceListener {
    private static final String TAG = "Qiupu.SendCardFragment";

    private ListView mListView;
    private TextView mRequestView;
    private BPCFriendsNewAdapter mBPCFriendsNewAdapter;
    private Activity mActivity;
    private MainHandler mHandler;
    private AtoZ mAtoZ;
    private int mRequestCount;
    private String mTitleIndex;
    private ProgressDialog mprogressDialog;
    private QiupuORM orm;

    public ExchangeCardFragment() {
        
    }

    public ExchangeCardFragment(int count, String title) {
        mRequestCount = count;
        mTitleIndex = title;
    }

    @Override
    public void onAttach(Activity activity) {
        orm = QiupuORM.getInstance(activity);

        mActivity = activity;
        super.onAttach(activity);
        mHandler = new MainHandler();
        try{
            GetSendCardFragmentCallBack listener = (GetSendCardFragmentCallBack)activity;
            listener.getSendCardFragment(this);
        }catch (ClassCastException e) {
            Log.d(TAG, activity.toString() +  "must implement GetSendCardFragmentCallBack");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerQiupuRequestListener();
        FriendsManager.registerFriendsServiceListener(getClass().getName(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friends_list_a2z, null);
        mListView = (ListView) view.findViewById(R.id.friends_list);
        mListView.setOnItemClickListener(mItemClickListener);
//        mListView.addHeaderView(initPeopleListHeadView());
        mAtoZ = (AtoZ) view.findViewById(R.id.atoz);
        if (mAtoZ != null) {
            mAtoZ.setFocusable(true);
            mAtoZ.setMoveFilterListener(this);
            mAtoZ.setVisibility(View.VISIBLE);
            mAtoZ.setListView(mListView);
        }
        mBPCFriendsNewAdapter = new BPCFriendsNewAdapter(mActivity, null, true, false);
        QiupuHelper.registerUserListener(getClass().getName(), this);
        refreshRequestItemUI(mRequestCount);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Cursor cursor = getUserCursor();
        if (cursor != null) {
            if (cursor.getCount() < 1) {
                IntentUtil.loadUsersFromServer(mActivity);
                begin();
            } else {
                mListView.setAdapter(mBPCFriendsNewAdapter);
                mBPCFriendsNewAdapter.alterDataList(cursor, mAtoZ);
            }
        } else {
            Log.d(TAG, "onActivityCreated() cursor is null ");
        }
    }

    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
            if (position == 0) {
                Log.d(TAG, "onclick() position = " + position);
            }

            if (BpcFriendsItemView.class.isInstance(view)) {
                BpcFriendsItemView sendcardView = (BpcFriendsItemView) view;
                QiupuUser user = sendcardView.getUser();
                IntentUtil.startUserDetailAboutIntent(mActivity, user.uid, user.nick_name);
            } 
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private View initPeopleListHeadView(){
        LayoutInflater factory = LayoutInflater.from(mActivity);
        View convertView = factory.inflate(R.layout.exchange_card_header_view, null);
        setHeaderViewListener(convertView);
        return convertView;
    }

    private void setHeaderViewListener(View convertView) {
        mRequestView = (TextView) convertView.findViewById(R.id.request_header_text);
        TextView cardCase = (TextView) convertView.findViewById(R.id.card_holder);
        cardCase.setMovementMethod(LinkMovementMethod.getInstance());

        View nfcHeader = convertView.findViewById(R.id.nfc_card_header);
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD && NfcAdapter.getDefaultAdapter(mActivity) != null) {
            nfcHeader.setVisibility(View.VISIBLE);
        } else {
            nfcHeader.setVisibility(View.GONE);
        }

        nfcHeader.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtil.startBeamDataByNFCActivity(mActivity);
            }
        });

        convertView.findViewById(R.id.request_card_header).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtil.startRequestActivity(mActivity, 0);
            }
        });

        convertView.findViewById(R.id.send_card_header).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtil.showExchangedUserList(mActivity);
            }
        });

//        convertView.findViewById(R.id.shake_card_header).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                IntentUtil.showShakingActivity(mActivity);
//            }
//        });

//        ImageView left_hand = (ImageView) convertView.findViewById(R.id.lbs_shake_hand_left);
//        ImageView right_hand = (ImageView) convertView.findViewById(R.id.lbs_shake_hand_right);
//
//        left_hand.setAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.rotate_left));
//
//        Bitmap localBitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.bump_shake_hand);
//        Matrix localMatrix = new Matrix();
//        localMatrix.setScale(-1.0F, 1.0F);
//        Bitmap localBitmap2 = Bitmap.createBitmap(localBitmap1, 0, 0, localBitmap1.getWidth(), localBitmap1.getHeight(), localMatrix, true);
//        right_hand.setImageBitmap(localBitmap2);
//
//        right_hand.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.rotate_right));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        QiupuHelper.unregisterUserListener(getClass().getName());
        FriendsManager.unregisterFriendsServiceListener(getClass().getName());
        unRegisterQiupuRequestListener();
    }

    private Cursor getUserCursor() {
        if (mActivity.getResources().getString(R.string.send_card_title).equals(mTitleIndex)) {
            return orm.queryFriendsHaveSentCard(String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE));
        } else if (mActivity.getResources().getString(R.string.received_card_friends).equals(mTitleIndex)) {
            return orm.queryFriendsHaveReceivedCard(String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE));
        } else if (mActivity.getResources().getString(R.string.card_holder_title).equals(mTitleIndex)) {
            return orm.queryFriendsHaveReceivedCard(String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE));
        } else {
            Log.d(TAG, "getUserCursor() unsupported type.");
            return null;
        }
//        String selection = UsersColumns.PROFILE_PRIVACY + " = 0 ";
//        String orderBy = " name_pinyin ASC";
//        return mActivity.getContentResolver().query(UserProvider.USER_CONTENT_URI, null, selection, null, orderBy);
    }

    private void setCursor() {
        Cursor cursor = getUserCursor();
        if (cursor != null && cursor.getCount() != 0) {
            mBPCFriendsNewAdapter.alterDataList(cursor, mAtoZ);
//            mBPCFriendsNewAdapter.notifyDataSetChanged();
        }
    }

    public interface GetSendCardFragmentCallBack {
        public void getSendCardFragment(ExchangeCardFragment sendCardFragment);
    }

    private static final int REFRESH_VALUE = 1;
    private static final int LOAD_FRIENDS_CALLBACK = 2;
    private static final int SEND_REQUEST_END = 3;
    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_VALUE: {
                    setCursor();
                    break;
                }
                case LOAD_FRIENDS_CALLBACK:
                    int statuscode = msg.getData().getInt("statuscode");
                    if (statuscode == FriendsManager.STATUS_DO_FAIL) {
                        end();
                    } else if (statuscode == FriendsManager.STATUS_DOING) {
                        begin();
                    } else if (statuscode == FriendsManager.STATUS_DO_OK) {
                        end();
                        mListView.setAdapter(mBPCFriendsNewAdapter);
                        setCursor();
                    } else if (statuscode == FriendsManager.STATUS_ITERATING) {
                        mListView.setAdapter(mBPCFriendsNewAdapter);
                        setCursor();
                    } else {
                        Log.d(TAG, "error, no such status.");
                    }
                    break;
                case SEND_REQUEST_END: {
                    try {
                        mprogressDialog.dismiss();
                        mprogressDialog = null;
                    } catch (Exception ne) {
                    }
                    boolean ret = msg.getData().getBoolean(BasicActivity.RESULT, false);
                    if (ret) {
                        long uid = msg.getData().getLong("uid");
                        Toast.makeText(mActivity, getString(R.string.request_ok), Toast.LENGTH_SHORT).show();
                    } else {
                        ToastUtil.showOperationFailed(mActivity, mHandler, true);
                    }
                }
                break;
                default:
                    break;
            }
        }
    }

    public void loadRefresh() {
        mHandler.obtainMessage(REFRESH_VALUE).sendToTarget();
    }

    private void refreshRequestItemUI(int count) {
        if (count == 0) {
            return;
        }
        String content = mActivity.getResources().getString(
                R.string.exchange_request_header_title)
                + "(" + count + ")";
        if (mRequestView != null) {
            mRequestView.setText(content);
        }
    }

    @Override
    public void enterPosition(String alpha, int position) {
        mListView.setSelection(position);
    }

    @Override
    public void leavePosition(String alpha) {
        
    }

    @Override
    public void beginMove() {
        
    }

    @Override
    public void endMove() {
        
    }

    public void refreshItemUI() {
        setCursor();
    }


    @Override
    public void updateUI(int msgcode, Message message) {
        Message msg = mHandler.obtainMessage(LOAD_FRIENDS_CALLBACK);
        msg.getData().putInt("statuscode", msgcode);
        msg.sendToTarget(); 
    }

    @Override
    public void requestUpdated(final ArrayList<Requests> data) {
        Log.d(TAG, "====== requestUpdated() data = " + data);
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                if (data != null) {
                    Log.d(TAG, "====== requestUpdated() data.size() = " + data.size());
                    refreshRequestItemUI(data.size());
                } else {
                    refreshRequestItemUI(0);
                }
            }
        });
    }

    private void registerQiupuRequestListener() {
        if (QiupuService.mRequestsService != null) {
            QiupuService.mRequestsService.regiestRequestListener(ExchangeCardFragment.class.getName(),
                            ExchangeCardFragment.this);
            QiupuService.mRequestsService.rescheduleRequests(true);
        } else {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    QiupuService.mRequestsService.regiestRequestListener(
                            ExchangeCardFragment.class.getName(),
                            ExchangeCardFragment.this);
                    QiupuService.mRequestsService.rescheduleRequests(true);
                }
            }, 5 * QiupuConfig.A_SECOND);
        }
    }

    private void unRegisterQiupuRequestListener() {
        if(QiupuService.mRequestsService != null) {
            QiupuService.mRequestsService.unRegiestRequestListener(ExchangeCardFragment.class.getName());
        }
    }

    @Override
    public void updateItemUI(QiupuUser user) {
        refreshItemUI();
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
//        sendApproveRequest(String.valueOf(user.uid), "");
        AsyncApiUtils.sendApproveRequest(user.uid, "", new AsyncQiupu(ConfigurationContext.getInstance(), null, null), this);
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
}
