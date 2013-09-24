package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import twitter4j.PollInfo;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.RecipientsAdapter;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.view.KeyboardLayout;
import com.borqs.common.view.KeyboardLayout.onKybdsChangeListener;
import com.borqs.common.view.PollTimeview;
import com.borqs.qiupu.AddressPadMini;
import com.borqs.qiupu.AddressPadMini.PhoneNumberEmailDecorater;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;

public class PollCreateActivity extends BasicActivity {

    private static final String TAG = "PollCreateActivity";

    public static final String RECEIVER_MAP_KEY = "receiversMap";
    public static final String RECEIVER_STR_KEY = "receivers";
    public static final String PRIVATE_KEY = "PRIVATE_KEY";
    public static final String POLL_KEY = "POLL_KEY";
    public static final String POLL_OUT_KEY = "POLL_OUT_KEY";
    public static final int ONCE_POLL = 0;
    public static final int INCREMENT_POLL = 1;
    public static final int REVERT_POLL = 2;

    private AddressPadMini      mShareTo;
    private EditText            mPollTitle;
    private PollTimeview        mTimeView;
    private LinearLayout        mPollItemContainer;
    private View                mAddItemView;
    private Spinner             mCountSpinner;
    private Spinner             mModeSpinner;
    private CheckBox            mAllowAddItem;
    private CheckBox            mSendEmail;
    private CheckBox            mSendSms;
    private EditText            mDescripView;
    private ImageView           mChooseUserIV;

    private ArrayList<String> mCanVoteCountList = new ArrayList<String>();
    private ArrayList<String> mModeList = new ArrayList<String>();
    private HashMap<String, String> mReceiveMap;
    private HashSet<Long> mSelectContactIds;
    private String mReceivers;
    private int mCanVoteCount = 0;
    private int mTmpMode = 0;
    private int mPollMode = 0;
//    private boolean mPrivateShare = true;

    private boolean mCheckedEmailStatus = true;
    private boolean mCheckedSmsStatus = false;
    private boolean mCheckedAddStatus = false;
    
    private HashMap<Long, String> mSelectUserCircleMap = new HashMap<Long, String>();
    
    private long mParentId;
    private long mSceneId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poll_create_main);
        setHeadTitle(R.string.create_poll);
        mParentId = getIntent().getLongExtra(CircleUtils.INTENT_PARENT_ID, -1);
        mSceneId = getIntent().getLongExtra(CircleUtils.INTENT_SCENE, -1);
        initInputMethodSeting();

        initIntentData();
        initLayout();

        initAddressListener();
        initAddItemListener();
        initSpinnerListener();
        initCheckBoxListener();

        showRightTextActionBtn(true);
        overrideRightTextActionBtn(R.string.create, createPollListener);
    }

    private void initInputMethodSeting() {
        KeyboardLayout mainView = (KeyboardLayout) findViewById(R.id.poll_create_layout);
        mainView.setOnkbdStateListener(new onKybdsChangeListener() {
            @Override
            public void onKeyBoardStateChange(int state) {
                if(state == KeyboardLayout.KEYBOARD_STATE_HIDE) {
                    
                }else if(state == KeyboardLayout.KEYBOARD_STATE_SHOW) {
                    lastDownKeyCode = 0;
                }
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void initIntentData() {
        Intent intent = getIntent();
        mReceiveMap = (HashMap<String, String>) intent.getSerializableExtra(RECEIVER_MAP_KEY);
        mReceivers = null == intent ? "" : intent.getStringExtra(RECEIVER_STR_KEY);
//        mPrivateShare = null == intent ? false : intent.getBooleanExtra(PRIVATE_KEY, false);
    }

    private void initLayout() {
    	mChooseUserIV = (ImageView) findViewById(R.id.choose_share_user);
    	mChooseUserIV.setOnClickListener(pickAttendeeListener);

        mShareTo = (AddressPadMini) findViewById(R.id.receiver_editor);
        mPollTitle = (EditText) findViewById(R.id.poll_title);
        mTimeView = (PollTimeview) findViewById(R.id.time_container);
        mPollItemContainer = (LinearLayout) findViewById(R.id.poll_item_container);
        mAddItemView = findViewById(R.id.add_poll_item_container);
        mCountSpinner = (Spinner) findViewById(R.id.can_poll_count_spinner);
        mModeSpinner = (Spinner) findViewById(R.id.poll_mode_spinner);
        mAllowAddItem = (CheckBox) findViewById(R.id.allow_add_item_checkbox);
        mSendEmail = (CheckBox) findViewById(R.id.send_email_checkbox);
        mSendSms = (CheckBox) findViewById(R.id.send_sms_checkbox);
        mDescripView = (EditText) findViewById(R.id.poll_description);

        mAllowAddItem.setVisibility(View.VISIBLE);
        mSendEmail.setVisibility(View.VISIBLE);
//        mSendSms.setVisibility(View.VISIBLE);

        mSendEmail.setChecked(mCheckedEmailStatus);
        mSendSms.setChecked(mCheckedSmsStatus);
    }

    private void initAddItemListener() {
        mAddItemView.setOnClickListener(mAddItemListener);
    }

    private View.OnClickListener mAddItemListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addItem();
        }
    };

    private void addItem() {
        LayoutInflater inflater = getLayoutInflater();
//        View view = inflater.inflate(R.layout.poll_default_item_ui, null);
//        view.findViewById(R.id.poll_item_edit).requestFocus();
        View view = inflater.inflate(R.layout.poll_item_ui, null);
        ImageView remove = (ImageView) view.findViewById(R.id.delete_image);
        remove.setOnClickListener(mDeleteItemListener);
        remove.setTag(Math.random());
        mPollItemContainer.addView(view);

        buildPollCountList();
    }

    private View.OnClickListener mDeleteItemListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            deleteItem(v);
        }
    };

    private void deleteItem(View v) {
        int count = mPollItemContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mPollItemContainer.getChildAt(i);
            if (view != null) {
                ImageView remove = (ImageView) view.findViewById(R.id.delete_image);
                if (remove != null) {
                    if (v.getTag() == remove.getTag()) {
                        mPollItemContainer.removeView(view);
                    }
                }
            }
        }

        buildPollCountList();
    }

    private View.OnClickListener pickAttendeeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startPickActivity();
        }
    };

    private void startPickActivity() {
    	Intent intent = new Intent(this, PickAudienceActivity.class);
        intent.putExtra(PickAudienceActivity.RECEIVE_ADDRESS, mShareTo.getAddressesArray());
        intent.putExtra(PickAudienceActivity.RECEIVE_SELECTUSERCIRCLE_NAME, mSelectUserCircleMap);
        intent.putExtra(PickAudienceActivity.PICK_FROM, PickAudienceActivity.PICK_FROM_POLL);
        intent.putExtra(CircleUtils.INTENT_SCENE, mSceneId);
        startActivityForResult(intent, PICK_ATTENDEE_CODE);
    }

    private View.OnClickListener createPollListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mShareTo.hasPendingSpan()) {
                Log.d(TAG, "ignore poll create with un-commit recipient.");
            } else {
                mHandler.obtainMessage(CREATE_POLL).sendToTarget();
            }
        }
    };

    private static final int PICK_ATTENDEE_CODE = 100;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case PICK_ATTENDEE_CODE:
                setShareToUI(intent);
                break;

            default:
                break;
        }
    };

    private void setShareToUI(Intent intent) {
        if (intent == null) {
            Log.d(TAG, "setShareToUI() intent = " + intent);
            return;
        }

//        String usersAddress = intent.getStringExtra("address");
//        String circlesAddress = intent.getStringExtra("circles");
//        String phones = intent.getStringExtra("phones");
//        String emails = intent.getStringExtra("emails");
//        mSelectContactIds = (HashSet<Long>) intent.getSerializableExtra("contactids");
//        if(QiupuConfig.DBLOGD)Log.d(TAG, "selectContactIds.size: " 
//                + (mSelectContactIds != null ? mSelectContactIds.size() : "null"));
//
//        circlesAddress = setprivateShareValue(circlesAddress);
//
//        StringBuilder sbUsers = new StringBuilder();
//        appendAddress(sbUsers, circlesAddress);
//        appendAddress(sbUsers, usersAddress);
//        appendAddress(sbUsers, phones);
//        appendAddress(sbUsers, emails);

        String usersAddress = intent.getStringExtra(PickAudienceActivity.RECEIVE_ADDRESS);
        mSelectUserCircleMap.clear();
        HashMap<Long, String> tmpMap = (HashMap<Long, String>) intent.getSerializableExtra(PickAudienceActivity.RECEIVE_SELECTUSERCIRCLE_NAME);
        mSelectUserCircleMap.putAll(tmpMap);
        
        Log.d(TAG, "onActivityResult address: " + usersAddress);
        mShareTo.setAddresses(usersAddress);
    }

    private String setprivateShareValue(String circleIds) {
        String[] ids = circleIds.split(",");
        StringBuilder newCircleids = new StringBuilder();
//        mPrivateShare = true;
        newCircleids.append(circleIds);
//        for(int i=0; i<ids.length; i++) {
//            if(ids[i].equals(QiupuConfig.CIRCLE_NAME_PUBLIC)) {
//                mPrivateShare = false;
//                break;
//            }
//        }
        return newCircleids.toString();
    }

    private void appendAddress(StringBuilder builder, String appendString){
        if(appendString != null && appendString.length() > 0){
            if(builder.length() > 0){
                builder.append(",");
                builder.append(appendString);
            }else{
                builder.append(appendString);
            }
        }
    }

    private void initAddressListener() {
    	mShareTo.setEnabled(true);
    	mShareTo.requestFocus();
        mShareTo.setAdapter(new RecipientsAdapter(this));
        mShareTo.setOnDecorateAddressListener(new FBUDecorater());
        StringBuilder ads = new StringBuilder();

//        if(mPrivateShare == false) {
//            ads.append(QiupuConfig.CIRCLE_NAME_PUBLIC);
//        }

        if (mReceivers != null) {
            if(ads.length() > 0) {
                ads.append(",");
            }
            ads.append(mReceivers);
//            mShareTo.setEnabled(false);
//            mChooseUserIV.setVisibility(View.GONE);
        }

        mShareTo.setAddresses(ads.toString());
    }

    private void buildPollCountList() {
        String formatter = getResources().getString(R.string.can_poll_count_title);
        mCanVoteCountList.clear();
        int count = mPollItemContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            mCanVoteCountList.add(String.format(formatter, (i+1)));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.event_spinner_textview, mCanVoteCountList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCountSpinner.setAdapter(adapter);
        Log.d(TAG, "mCanVoteCount = " + mCanVoteCount + " size = " + mCanVoteCountList.size());
        if (mCanVoteCount >= mCanVoteCountList.size()) {
            mCanVoteCount = mCanVoteCountList.size() - 1;
        }
        mCountSpinner.setSelection(mCanVoteCount);
    }

    private void buildPollModeList() {
        String signle = getResources().getString(R.string.poll_mode_single);
        String increase = getResources().getString(R.string.poll_mode_pending);
        String revert = getResources().getString(R.string.poll_mode_revert);

        mModeList.clear();
        mModeList.add(0, revert);
        if (mCanVoteCount > 0) {
            mModeList.add(0, increase);
        }
        mModeList.add(0, signle);

        setModeSpinerAdapter();
        mModeSpinner.setSelection(mTmpMode);
    }

    private void setModeSpinerAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.event_spinner_textview, mModeList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mModeSpinner.setAdapter(adapter);
    }

    private void initSpinnerListener() {
        buildPollCountList();
        mCountSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                Log.d(TAG, "mCountSpinner --> position = " + position + ", mCanVoteCount = " + mCanVoteCount);
                if (position == 0) {
                    if (mCanVoteCount > 0) {
                        switch (mTmpMode) {
                            case ONCE_POLL:
                                mPollMode = ONCE_POLL;
                                mTmpMode = ONCE_POLL;
                                break;
                            case INCREMENT_POLL:
                                mPollMode = ONCE_POLL;
                                mTmpMode = ONCE_POLL;
                                break;
                            case REVERT_POLL:
                                mPollMode = REVERT_POLL;
                                mTmpMode = 1;//avoid outofbound exception, it represent REVERT_POLL MODE
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    if (mPollMode == REVERT_POLL) {
                        switch (mTmpMode) {
                            case 0:
                                break;
                            case 1:
                                mTmpMode = REVERT_POLL;
                                break;
                            case REVERT_POLL:
                                break;
                            default:
                                break;
                        }
                    }
                }

                mCanVoteCount = position;
                buildPollModeList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

//        buildPollModeList();
        mModeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                Log.d(TAG, "mModeSpinner --> position = " + position + ", mTmpMode = " + mTmpMode + ", mPollMode = " + mPollMode);
                int count = mModeSpinner.getAdapter().getCount();
                switch (count) {
                    case 2:
                        if (mTmpMode == 1 || position == 1) {
                            mPollMode = REVERT_POLL;
                        } else {
                            mPollMode = ONCE_POLL;
                        }
                        break;
                    case 3:
                        mPollMode = position;
                        break;
                    default:
                        break;
                }
                mTmpMode = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initCheckBoxListener() {
        mAllowAddItem.setOnClickListener(mAddItemCheckListener);
        mSendEmail.setOnClickListener(mSendEmailCheckListener);
        mSendSms.setOnClickListener(mSendSmsCheckListener);
    }

    private View.OnClickListener mAddItemCheckListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCheckedAddStatus = !mCheckedAddStatus;
            mAllowAddItem.setChecked(mCheckedAddStatus);
        }
    };

    private View.OnClickListener mSendEmailCheckListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCheckedEmailStatus = !mCheckedEmailStatus;
            mSendEmail.setChecked(mCheckedEmailStatus);
        }
    };

    private View.OnClickListener mSendSmsCheckListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCheckedSmsStatus = !mCheckedSmsStatus;
            mSendSms.setChecked(mCheckedSmsStatus);
        }
    };

    private class FBUDecorater implements AddressPadMini.OnDecorateAddressListener {
        @Override
        public String onDecorate(String address) {
            String suid = address.trim();
            try {
                if (suid.contains("#")) {
                    int index = suid.indexOf("#");
                    suid = suid.substring(index + 1, suid.length());

                    UserCircle uc = orm.queryOneCircle(QiupuConfig.USER_ID_ALL,
                            Long.valueOf(suid));
                    if (uc != null) {
                        return CircleUtils.getCircleName(PollCreateActivity.this,
                                        uc.circleid, uc.name);
                    } else {
                        if (mReceiveMap != null) {
                            return mReceiveMap.get(suid);
                        }
                    }
                } else if (suid.contains("*")) {
                    int index = suid.indexOf("*");
                    suid = suid.substring(index + 1, suid.length());
                    PhoneNumberEmailDecorater number = new AddressPadMini(
                            PollCreateActivity.this).new PhoneNumberEmailDecorater();
                    return number.onDecorate(suid);
                } else {
                	String username = orm.queryUserName(Long.valueOf(suid));
                	if (username == null) {
                		if(mReceiveMap != null && mReceiveMap.get(suid) != null) {
                    		username = mReceiveMap.get(suid);
                		}else {
                			username = mSelectUserCircleMap.get(Long.parseLong(suid)); 
                		}
                    }
                	return username;
                }
            } catch (Exception ne) {
            	return null;
            }
            return address;
        }
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    private static final int CREATE_POLL = 1001;
    private static final int CREATE_POLL_END = 1002;

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CREATE_POLL:
                    createPoll();
                    break;
                case CREATE_POLL_END:
                    showLeftActionBtn(false);
                    try {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    } catch (Exception ne) { }
                    if (msg.getData().getBoolean("RESULT")) {
                        ToastUtil.showShortToast(PollCreateActivity.this, mHandler, R.string.create_vote_successfully);
                        PollInfo pollInfo = (PollInfo) msg.getData().getSerializable(POLL_KEY);
                        Intent intent = new Intent();
                        intent.putExtra(POLL_OUT_KEY, pollInfo);
                        setResult(Activity.RESULT_OK,intent);
                        finish();
                    } else {
                        ToastUtil.showOperationFailed(PollCreateActivity.this, mHandler, false);
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private String buildRecipient() {
//        mPrivateShare = true;
        StringBuilder users = new StringBuilder();
        String[] userArray = mShareTo.getAddressesArray();
        Log.d(TAG, "buildRecipient() " + ", userArray = " + userArray.length);
        for (int i = 0; i < userArray.length; i++) {
            if (i > 0) {
                users.append(",");
            }
//            if(userArray[i].equals(QiupuConfig.CIRCLE_NAME_PUBLIC)) {
//                mPrivateShare = false;
//                users.append(QiupuConfig.CIRCLE_NAME_PUBLIC);
//            }else {
//                users.append(userArray[i]);
//            }
            users.append(userArray[i]);
        }
        return users.toString();
    }

    private String buildEditTextContent(EditText editText) {
        Editable editable = editText.getText();
        String title = "";
        if (editable != null && TextUtils.isEmpty(editable.toString()) == false) {
            title = editable.toString();
        }
        return title;
    }

    private ArrayList<String> buildPollItemList() {
        ArrayList<String> pollItemList = new ArrayList<String>();
        int count = mPollItemContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mPollItemContainer.getChildAt(i);
            if (view != null) {
                EditText editText = (EditText) view.findViewById(R.id.poll_item_edit);
                Editable editable = editText == null ? null : editText.getText();
                if (editable != null && TextUtils.isEmpty(editable.toString()) == false) {
                    pollItemList.add(editable.toString());
                }
            }
        }
        return pollItemList;
    }

    private boolean inCreatePoll;
    private Object  mLockCreatePollInfo = new Object();

    public void createPoll() {
        if (!ToastUtil.testValidConnectivity(this)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }

        if (inCreatePoll == true) {
            ToastUtil.showShortToast(this, mHandler, R.string.string_in_processing);
            return;
        }

        if (mShareTo.getAddressesArray().length == 0) {
            DialogUtils.showConfirmDialog(this, R.string.invalid_recipient,
                    R.string.dlg_message_add_recipient, R.string.label_ok, R.string.label_cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startPickActivity();
                        }
                    });
//            showCustomToast(R.string.invalid_recipient);
//            mShareTo.requestFocus();
            return;
        }

        String title = buildEditTextContent(mPollTitle);
        if (TextUtils.isEmpty(title)) {
            showCustomToast(R.string.poll_title_toast);
            return;
        }

        ArrayList<String> pollItemList = buildPollItemList();
        if (pollItemList != null) {
            Log.d(TAG, "pollItemList.size() = " + pollItemList.size());
        }
        if (pollItemList != null && pollItemList.size() < 2) {
            showCustomToast(R.string.poll_item_toast);
            return;
        }

        int poll_count = 0;
        if (mCanVoteCount + 1 > pollItemList.size()) {
            poll_count = pollItemList.size();
        } else {
            poll_count = mCanVoteCount + 1;
        }

        String description = buildEditTextContent(mDescripView);
        long startTime = mTimeView.getStartMillis();
        long endTime = mTimeView.getEndMillis();
        
        synchronized (mLockCreatePollInfo) {
            inCreatePoll = true;
        }

//        begin();
        showProcessDialog(R.string.poll_creating_dialog_msg, false, true, false);
        String recipient = buildRecipient();
        Log.d(TAG, "mPollMode = " + mPollMode);
        asyncQiupu.createPoll(AccountServiceUtils.getSessionID(), recipient, title, description, startTime,
                endTime, pollItemList, poll_count, mPollMode, mCheckedAddStatus, mCheckedEmailStatus,
                mCheckedSmsStatus, recipient.contains(QiupuConfig.CIRCLE_NAME_PUBLIC), mParentId, new TwitterAdapter() {
                    public void createPoll(PollInfo pollInfo) {
                        Log.d(TAG, "finish createPoll = " + pollInfo);

                        Message msg = mHandler.obtainMessage(CREATE_POLL_END);
                        msg.getData().putBoolean("RESULT", true);
                        msg.getData().putSerializable(POLL_KEY, pollInfo);
                        msg.sendToTarget();
                        synchronized (mLockCreatePollInfo) {
                            inCreatePoll = false;
                        }
                    }

                    public void onException(TwitterException ex,
                            TwitterMethod method) {
                        synchronized (mLockCreatePollInfo) {
                            inCreatePoll = false;
                        }
                        Message msg = mHandler.obtainMessage(CREATE_POLL_END);
                        msg.getData().putBoolean("RESULT", false);
                        msg.sendToTarget();
                    }
                });
    }

    private ProgressDialog mProgressDialog;

    private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable){
        mProgressDialog = DialogUtils.createProgressDialog(this, 
                resId, CanceledOnTouchOutside, Indeterminate, cancelable);
        mProgressDialog.setInverseBackgroundForced(true);
        mProgressDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
    }
    @Override
    protected boolean preEscapeActivity() {
        onDiscard();
        return mDiscardConfirm;
    }

    public void onDiscard() {
        mDiscardConfirm = false;
        if (needEscapeConfirm()) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                    mDiscardConfirm = true;
                }
            };

            final String title = getString(R.string.play_title);
            final String message = getString(R.string.poll_quit_dialog_msg);
            DialogUtils.showConfirmDialog(this, title, message, listener);
        } else {
            finish();
            mDiscardConfirm = true;
        }
    }

    private boolean mDiscardConfirm = false;
    private boolean needEscapeConfirm() {
        boolean ret = false;
        boolean hasComments = TextUtils.isEmpty(buildEditTextContent(mPollTitle)) == false;

        if (hasComments) {
            ret = true;
        }

        return ret;
    }

}
