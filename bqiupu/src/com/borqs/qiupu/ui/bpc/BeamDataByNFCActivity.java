/*
 * Copyright 2011, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import twitter4j.QiupuAccountInfo.PhoneEmailInfo;
import twitter4j.QiupuUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.BorqsAccount;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.FileUtils;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.RequestContactInfoSimpleView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ContactUtils;
import com.borqs.qiupu.util.JSONUtil;

public class BeamDataByNFCActivity extends BasicActivity {
    private static final String TAG = "BeamDataByNFCActivity";

    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    private IntentFilter[] mNdefExchangeFilters;

    private static final int GET_USER_FROM_SERVER_END = 1;
    public static final int MESSAGE_BEGIN_IMPORT_VCARD = 2;
    public static final int MESSAGE_END_IMPORT_VCARD = 3;
    private Object mInfoLock = new Object();
    private boolean mUserIsReady = false;
    private boolean inInfoProcess;
    private QiupuUser mUser;
    private BorqsAccount mBorqsAccount;

    private long mBorqsId;
    private String mUserName;
    private ArrayList<String> mPhoneList = new ArrayList<String>();
    private ArrayList<String> mEmailList = new ArrayList<String>();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.exchange_vcard_nfc);
        setHeadTitle(R.string.exchange_nfc_header_title);

        showRightActionBtn(false);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            // this can't happen in normal case.
            Log.e(TAG, "onCreate() mNfcAdapter == null , error and impossible to happen.");
            showCustomToast(R.string.no_available_nfc);
        } else {
            mBorqsAccount = AccountServiceUtils.getBorqsAccount();
            if (mBorqsAccount != null) {
                mUser = orm.queryOneUserInfo(mBorqsAccount.uid);
                if (mUser == null) {
                    Log.d(TAG, "get user info from server, borqsAccount.uid = " + mBorqsAccount.uid);
                    mUserIsReady = true;
                    getUserInfo(mBorqsAccount.uid);
                } else {
                    setUI(mUser);
                }
            } else {
                // when not login we query contact profile.db
                // TODO: compatibility issue with 2.3.x
                mUser = createQiupuUser();
                mUser.uid = -1;
                setUI(mUser);
            }

            // Handle all of our received NFC intents in this activity.
            mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

            // Intent filters for reading message from exchanging over p2p.
            IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            try {
                ndefDetected.addDataType("text/plain");
            } catch (MalformedMimeTypeException e) { }
            mNdefExchangeFilters = new IntentFilter[] { ndefDetected };
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            setReceiveUI(getIntent());
            setIntent(new Intent());
        }
        if (mUser != null) {
            enableNdefExchangeMode();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableNdefExchangeMode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

//    private void setTitle() {
//        mTitle.setVisibility(View.VISIBLE);
//        int leftPading = (int) getResources().getDimension(R.dimen.exchange_vard_title_left_padding);
//        mTitle.setPadding(leftPading, 0, 0, 0);
//        setHeadTitle(R.string.friends_item_request_exchange);
//    }

    private QiupuUser createQiupuUser() {
        long profileId = ContactUtils.getProfileId(this);
        String name = ContactUtils.getProfileName(this, profileId);
        ArrayList<String> phones = ContactUtils.getProfilePhones(this, profileId);
        ArrayList<String> emails = ContactUtils.getProfileEmails(this, profileId);

        QiupuUser qiupuUser = new QiupuUser();
        qiupuUser.nick_name = name;

//        qiupuUser = setPhones(qiupuUser, phones);
//        qiupuUser = setEmails(qiupuUser, emails);
        qiupuUser.phoneList = getPhone(phones);
        qiupuUser.emailList = getEmail(emails);
        return qiupuUser;
    }

    private void enableNdefExchangeMode() {
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundNdefPush(BeamDataByNFCActivity.this, getMessageAsNdef());
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
        }
    }

    private void disableNdefExchangeMode() {
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundNdefPush(this);
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            setReceiveUI(intent);
        }
    }

    private NdefMessage getMessageAsNdef() {
        Log.d(TAG,"========== getMessageAsNdef() mUser = " + mUser);
        String contactInfo = JSONUtil.createPhoneAndEmailJSONArray(mUser.uid, mUser.nick_name, getPhoneList(), getEmailList());
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                new byte[] {}, contactInfo.getBytes());
        return new NdefMessage(new NdefRecord[] {
            textRecord
        });
    }

    private ArrayList<String> getPhoneList() {
        ArrayList<String> list = new ArrayList<String>();
        for(int i=0; i<mUser.phoneList.size(); i++) {
            insertIntoList(list, mUser.phoneList.get(i).info);    
        }
//        insertIntoList(list, mUser.contact_phone1);
//        insertIntoList(list, mUser.contact_phone2);
//        insertIntoList(list, mUser.contact_phone3);
        return list;
    }

    private ArrayList<String> getEmailList() {
        ArrayList<String> list = new ArrayList<String>();
        for(int i=0; i<mUser.emailList.size(); i++) {
        insertIntoList(list, mUser.emailList.get(i).info);
        }
//        insertIntoList(list, mUser.contact_email2);
//        insertIntoList(list, mUser.contact_email3);
        return list;
    }

    private ArrayList<String> insertIntoList(ArrayList<String> list, String contactWay) {
        if (!TextUtils.isEmpty(contactWay)) {
            list.add(contactWay);
        }
        return list;
    }

    private void setReceiveUI(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                NdefMessage msg = (NdefMessage) rawMsgs[0];
                msgs = new NdefMessage[] {msg};
            } else {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {record});
                msgs = new NdefMessage[] {msg};
            }
            setReceiveUI(new String(msgs[0].getRecords()[0].getPayload()));
        } else {
            Log.d(TAG, "Unknown intent.");
            finish();
        }
    }

    private View viewLayout;
    private View receiveTextView;
    private void setReceiveUI(String contactInfo) {
        if (TextUtils.isEmpty(contactInfo)) {
            Log.d(TAG, "receiveUI() : contactInfo is null, return and do not setUI");
            return;
        }
        viewLayout = findViewById(R.id.received_vcard_layout);
        receiveTextView = findViewById(R.id.received_card);
        setViewVisiable(true);

        View view = viewLayout.findViewById(R.id.id_vcard_rl);
        view.setVisibility(View.VISIBLE);
        ImageView import_vcard = (ImageView) view.findViewById(R.id.import_contact);
        TextView nameText = (TextView) view.findViewById(R.id.id_user_name);
        LinearLayout vcard = (LinearLayout) view.findViewById(R.id.id_vcard);

        setReceivedVcardUI(contactInfo, nameText, vcard, import_vcard);

        if (mUser.uid != -1) {
        	super.finishActionFriendsCallBack(buildQiupuUser());
//            AsyncApiUtils.updateDB(orm, buildQiupuUser());
            updateFriendsListUI(orm.queryOneUserInfo(mBorqsId));
            exchangeVcard(mBorqsId, false, CircleUtils.getDefaultCircleId(), CircleUtils.getDefaultCircleName(getResources()));
        }
    }

    private void setViewVisiable(boolean show) {
        if (show) {
            viewLayout.setVisibility(View.VISIBLE);
            receiveTextView.setVisibility(View.VISIBLE);
        } else {
            viewLayout.setVisibility(View.GONE);
            receiveTextView.setVisibility(View.GONE);
        }
    }

    private void setReceivedVcardUI(String contactInfo, TextView nameText,
            LinearLayout vcard, ImageView import_vcard) {
        if (vcard != null && vcard.getChildCount() > 0) {
            vcard.removeAllViews();
        }

        try {
            JSONTokener jsonTokener = new JSONTokener(contactInfo);
            JSONObject obj = new JSONObject(jsonTokener);
            String contactWay = null;
            JSONArray array = null;

            if (!obj.isNull("name")) {
                mUserName = obj.getString("name");
            }

            if (!obj.isNull("borqs_id")) {
                mBorqsId = obj.getLong("borqs_id");
            }

            if (!obj.isNull("phone_email")) {
                array = obj.getJSONArray("phone_email");
            }

            nameText.setText(mUserName);

            for (int i = 0; i < array.length(); i++) {
                if (!array.getJSONObject(i).isNull("phone")) {
                    String phone = array.getJSONObject(i).getString("phone");
                    contactWay = getResources().getString(R.string.phone_way) + phone;
                    RequestContactInfoSimpleView info = new RequestContactInfoSimpleView(this, contactWay);
                    vcard.addView(info);
                    mPhoneList.add(phone);
                }

                if (!array.getJSONObject(i).isNull("email")) {
                    String email = array.getJSONObject(i).getString("email");
                    contactWay = getResources().getString(R.string.email_way) + email;
                    RequestContactInfoSimpleView info = new RequestContactInfoSimpleView(this, contactWay);
                    vcard.addView(info);
                    mEmailList.add(email);
                }
            }

            import_vcard.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    DialogUtils.showConfirmDialog(BeamDataByNFCActivity.this, R.string.import_vcard,
                            R.string.import_vcard_message, R.string.label_ok, R.string.label_cancel, 
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Message msg = new Message();
                            msg.what = MESSAGE_BEGIN_IMPORT_VCARD;
                            mHandler.sendMessage(msg);
                            dialogInterface.dismiss();
                        }
                    });
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private QiupuUser buildQiupuUser() {
        QiupuUser qiupuUser = new QiupuUser();
        qiupuUser.nick_name = mUserName;
        qiupuUser.circleId = CircleUtils.getDefaultCircleId();
        qiupuUser.circleName =  CircleUtils.getDefaultCircleName(getResources());
        qiupuUser.name_pinyin = QiupuORM.getPinyin(mUserName);
//        qiupuUser = setPhones(qiupuUser, mPhoneList);
//        qiupuUser = setEmails(qiupuUser, mEmailList);
        qiupuUser.phoneList = getPhone(mPhoneList);
        qiupuUser.emailList = getEmail(mEmailList);
        return qiupuUser;
    }

    private ArrayList<PhoneEmailInfo> getPhone(ArrayList<String> phoneList) {
        ArrayList<PhoneEmailInfo> phones = new ArrayList<PhoneEmailInfo>();
        if (phoneList != null ) {
            for (int i = 0; i < phoneList.size(); i++) {
                PhoneEmailInfo info = new PhoneEmailInfo();
                info.uid = mBorqsId;
                info.info = phoneList.get(i);
                if (i == 0) {
                    info.type = QiupuConfig.TYPE_PHONE1;
                    phones.add(info);
                } else if (i == 1) {
                    info.type = QiupuConfig.TYPE_PHONE2;
                    phones.add(info);
                } else if (i == 2) {
                    info.type = QiupuConfig.TYPE_PHONE3;
                    phones.add(info);
                } else {
                    Log.d(TAG, "error , receiveUI() ---> phoneList: outOfArrayBoundary.");
                }
            }
        }
        return phones;
    }
    
    private ArrayList<PhoneEmailInfo> getEmail(ArrayList<String> emailList) {
        ArrayList<PhoneEmailInfo> emails = new ArrayList<PhoneEmailInfo>();
        if (emailList != null ) {
            for (int i = 0; i < emailList.size(); i++) {
                PhoneEmailInfo info = new PhoneEmailInfo();
                info.uid = mBorqsId;
                info.info = emailList.get(i);
                if (i == 0) {
                    info.type = QiupuConfig.TYPE_EMAIL1;
                    emails.add(info);
                } else if (i == 1) {
                    info.type = QiupuConfig.TYPE_EMAIL2;
                    emails.add(info);
                } else if (i == 2) {
                    info.type = QiupuConfig.TYPE_EMAIL3;
                    emails.add(info);
                } else {
                    Log.d(TAG, "error , receiveUI() ---> phoneList: outOfArrayBoundary.");
                }
            }
        }
        return emails;
    }
//    private QiupuUser setPhones(QiupuUser qiupuUser, ArrayList<String> phoneList) {
//        // TODO: we should consider more than 3 phones and emails when user didn't login.
//        if (phoneList != null) {
//            for (int i = 0; i < phoneList.size(); i++) {
//                if (i == 0) {
//                    qiupuUser.contact_phone1 = phoneList.get(0);
//                } else if (i == 1) {
//                    qiupuUser.contact_phone2 = phoneList.get(1);
//                } else if (i == 2) {
//                    qiupuUser.contact_phone3 = phoneList.get(2);
//                } else {
//                    Log.d(TAG, "error , receiveUI() ---> phoneList: outOfArrayBoundary.");
//                }
//            }
//        }
//        return qiupuUser;
//    }

//    private QiupuUser setEmails(QiupuUser qiupuUser, ArrayList<String> emailList) {
//        if (emailList != null) {
//            for (int i = 0; i < emailList.size(); i++) {
//                if (i == 0) {
//                    qiupuUser.contact_email1 = emailList.get(0);
//                } else if (i == 1) {
//                    qiupuUser.contact_email2 = emailList.get(1);
//                } else if (i == 2) {
//                    qiupuUser.contact_email3 = emailList.get(2);
//                } else {
//                    Log.d(TAG, "error , receiveUI() ---> emailList: outOfArrayBoundary.");
//                }
//            }
//        }
//        return qiupuUser;
//    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_USER_FROM_SERVER_END:{
                    boolean succeed = msg.getData().getBoolean(RESULT, false);
                    if (succeed) {
                        setUI(mUser);
                        if (mUserIsReady) {
                            enableNdefExchangeMode();
                        }
                    } else {
                        String errorMsg = msg.getData().getString(ERROR_MSG);
                        if(TextUtils.isEmpty(errorMsg)) {
                            showCustomToast(errorMsg);
                        }
                    }
                    break;
                }
                case MESSAGE_BEGIN_IMPORT_VCARD: {
                    BeamDataByNFCActivity.this.showProgressBtn(true);
                    FileUtils.insertContact(BeamDataByNFCActivity.this, mHandler, mUserName, mPhoneList, mEmailList);
                    break;
                }
                case MESSAGE_END_IMPORT_VCARD: {
                    BeamDataByNFCActivity.this.showProgressBtn(false);
                    boolean succeed = msg.getData().getBoolean("RESULT");
                    if (succeed) {
                        showCustomToast(R.string.import_vcard_success);
                        setViewVisiable(false);
                    } else {
                        showCustomToast(R.string.import_vcard_failed);
                    }
                }
                default:
                    break;
            }
        }
    }

    private boolean isUiLoading(){
        if(inInfoProcess){
            return true;
        }else{
            return false;
        }
    }

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

    private void setUI(QiupuUser user) {
        if (user == null) {
            Log.d(TAG, "setUI() : error, user is null");
            return;
        }

        findViewById(R.id.received_vcard_layout).setVisibility(View.GONE);
        View viewLayout = findViewById(R.id.my_vcard_layout);
        viewLayout.setVisibility(View.VISIBLE);
        View view = viewLayout.findViewById(R.id.id_vcard_rl);
        view.setVisibility(View.VISIBLE);
        view.findViewById(R.id.import_contact).setVisibility(View.GONE);
        TextView name = (TextView) view.findViewById(R.id.id_user_name);
        LinearLayout vcard = (LinearLayout) view.findViewById(R.id.id_vcard);

        name.setText(user.nick_name);

        String contactWay = getResources().getString(R.string.phone_way);
        if(user.phoneList != null && user.phoneList.size() > 0) {
            for(int i=0; i<user.phoneList.size(); i++ ){
                String content = user.phoneList.get(i).info;
                setContactWayUI(vcard, content, contactWay + content);
            }
        }
//        setContactWayUI(vcard, user.contact_phone1, contactWay + user.contact_phone1);
//        setContactWayUI(vcard, user.contact_phone2, contactWay + user.contact_phone2);
//        setContactWayUI(vcard, user.contact_phone3, contactWay + user.contact_phone3);

        contactWay = getResources().getString(R.string.email_way);
        if(user.emailList != null && user.emailList.size() > 0) {
            for(int i=0; i<user.emailList.size(); i++ ){
                String content = user.emailList.get(i).info;
                setContactWayUI(vcard, content, contactWay + content);
            }
        }
//        setContactWayUI(vcard, user.contact_email1, contactWay + user.contact_email1);
//        setContactWayUI(vcard, user.contact_email2, contactWay + user.contact_email2);
//        setContactWayUI(vcard, user.contact_email3, contactWay + user.contact_email3);
    }

    private void setContactWayUI(LinearLayout vcard, String userContact, String contactWay) {
        if (!TextUtils.isEmpty(userContact)) {
            RequestContactInfoSimpleView info = new RequestContactInfoSimpleView(this, contactWay);
            vcard.addView(info);
        }
    }

    private void updateFriendsListUI(final QiupuUser user) {
        mHandler.post(new Runnable() {
            public void run() {
                QiupuHelper.updateActivityUI(user);
            }
        });
    }

    @Override
    protected void showSetCircleProcessDialog() {
    	// no need show mDenyProgress .
    }
    @Override
    protected void finishActionFriendsCallBack(QiupuUser user) {
    	// do nothing.
    }
    @Override
    protected void doActionFriendEndCallBack(Message msg) {
    	//do nothing
    }

}