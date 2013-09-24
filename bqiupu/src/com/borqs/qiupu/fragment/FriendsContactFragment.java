package com.borqs.qiupu.fragment;

import twitter4j.AsyncBorqsAccount;
import twitter4j.QiupuUser;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.borqs.account.service.PeopleLookupHelper;
import com.borqs.account.service.PeopleLookupHelper.AddressBookSyncServiceListener;
import com.borqs.account.service.db.BorqsAccountORM;
import com.borqs.common.adapter.PickPeopleAdapter;
import com.borqs.common.listener.FriendsContactActionListner;
import com.borqs.common.listener.OnListItemClickListener;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.PickPeopleItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.bpc.InviteContactActivity;
import com.borqs.qiupu.util.ContactUtils;

public class FriendsContactFragment extends PeopleSearchableFragment implements
        FriendsContactActionListner, AddressBookSyncServiceListener {
    private final static String TAG = "FriendsContactFragment";
    private QiupuORM orm;
    //    private ProgressDialog mprogressDialog;
//    private AsyncQiupu asyncQiupu;
    private Handler mhandler;
    private PickPeopleAdapter mPickPeopleAdapter;
    private static final String RESULT = "result";
    public static final int MODE_FRIENDS = 1;
    public static final int MODE_ADD_FRIENDS = 2;
    public static final int MODE_PICK_VCARD = 3;
    private int mMode;
    private String contactidInMyCircle;
    //    private Resources mResources;
    private PeopleLookupHelper mBookSync;
//    private static final int MAX_LENGTH = 70;

    private boolean mNeedTriggerPrompt = true;
    private Cursor mCurrentUsageCursor;

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        mActivity = activity;
//    	mResources = activity.getResources();

        try {
            CallBackFriendsContactFragmentListener listener = (CallBackFriendsContactFragmentListener) activity;
            listener.getFriendsContactFragment(this);
            mMode = listener.getMode();
        } catch (ClassCastException e) {
            Log.d(TAG, activity.toString() + "must implement CallBackFriendsContactFragmentListener");
        }

        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        orm = QiupuORM.getInstance(mActivity);
//        asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
        mhandler = new MainHandler();
        BorqsAccountORM accountorm = BorqsAccountORM.getInstance(mActivity);
        AsyncBorqsAccount mAsyncBorqsAccount = new AsyncBorqsAccount(ConfigurationContext.getInstance(), null, null);
        mBookSync = PeopleLookupHelper.getInstance(mActivity, accountorm, mAsyncBorqsAccount);

        if (MODE_FRIENDS == mMode) {
            // need to re-query a new cursor, which might be managed by book sync background.
            if (mBookSync.isEmptyLookupResult()) {
                mBookSync.verifyContactsTypeWithSync4(ContactUtils.getContacts(mActivity), false);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View convertView = super.onCreateView(inflater, container, savedInstanceState);

        mListView = (ListView) convertView.findViewById(R.id.friends_list);
        mListView.setOnItemClickListener(contactitemClickListener);

        return convertView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated " + mActivity);
        super.onActivityCreated(savedInstanceState);

        if (MODE_PICK_VCARD == mMode) {
            mPickPeopleAdapter = new PickPeopleAdapter(mActivity, true);
        } else {
            mPickPeopleAdapter = new PickPeopleAdapter(mActivity, false);
        }

        mhandler.postDelayed(new Runnable() {
            public void run() {
                mPickPeopleAdapter.registerFriendsContactActionListener(getClass().getName(), FriendsContactFragment.this);
                mListView.setAdapter(mPickPeopleAdapter);
                Cursor contactCursor = null;
                int status = getCheckLocalPeopleSyncStatus();
                Log.d(TAG, "status : " + status + " mode: " + mMode);

                switch (mMode) {
                    case MODE_FRIENDS: {
                        if (mCurrentUsageCursor != null && mCurrentUsageCursor.isClosed() == false) {
                            mCurrentUsageCursor.close();
                        }
                        mCurrentUsageCursor = ContactUtils.getContacts(mActivity);
                        Cursor cursorClone = ContactUtils.getContacts(mActivity);
                        mPickPeopleAdapter.alterDataList(mCurrentUsageCursor, cursorClone, mAtoZ);
                        if (status == PeopleLookupHelper.STATUS_DOING) {
                            begin();
                        } else if (status == PeopleLookupHelper.STATUS_DEFAULT) {
                            boolean hasConfirm = QiupuORM.isConfirmLookup(getActivity());
                            if (!hasConfirm) {
                                mBookSync.triggerPeopleLookupSession(mNeedTriggerPrompt);
                                mNeedTriggerPrompt = false;
                            }
                        }
                        // need to re-query a new cursor, which might be managed by book sync background.
//        	              mBookSync.verifyContactsType(ContactUtils.getContacts(mActivity));
                        break;
                    }
                    case MODE_ADD_FRIENDS: {
                        //TODO goto check status
                        // 1, if doing will show center process
                        if (status == PeopleLookupHelper.STATUS_DOING) {
                            begin();
                        } else if (status == PeopleLookupHelper.STATUS_DEFAULT) {
                            mBookSync.triggerPeopleLookupSession(mNeedTriggerPrompt);
                            mNeedTriggerPrompt = false;
                        } else {
                            showAddContactUI();
                        }
                        break;
                    }
                    case MODE_PICK_VCARD: {
                        if (mCurrentUsageCursor != null && mCurrentUsageCursor.isClosed() == false) {
                            mCurrentUsageCursor.close();
                        }

                        mCurrentUsageCursor = ContactUtils.getContacts(mActivity);
                        Cursor cursorClone = ContactUtils.getContacts(mActivity);
                        mPickPeopleAdapter.alterDataList(mCurrentUsageCursor, cursorClone, mAtoZ);
                        break;
                    }
                    default:
                        Log.d(TAG, "error, No such mode");
                        break;
                }
            }
        }, 500);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        PeopleLookupHelper.registerFriendsServiceListener(getClass().getName(), this);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mPickPeopleAdapter.unregisterFriendsContactActionListener(getClass().getName());
        PeopleLookupHelper.unregisterFriendsServiceListener(getClass().getName());
        super.onDestroy();

        ContactUtils.desposeCachedBitMap();

        if (mCurrentUsageCursor != null && mCurrentUsageCursor.isClosed() == false) {
            mCurrentUsageCursor.close();
        }
    }


    private AdapterView.OnItemClickListener contactitemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {

            if (OnListItemClickListener.class.isInstance(mActivity)) {
                OnListItemClickListener listener = (OnListItemClickListener) mActivity;
                listener.onListItemClick(view, FriendsContactFragment.this);
            }
        }
    };

    @Override
    public void addFriends(long uid) {
        IntentUtil.startCircleSelectIntent(mActivity, uid, null);
    }

    @Override
    public void inviteFriends(long contactid, final String display_name) {
        Log.d(TAG, "gotoStartInviteActivity : " + contactid);
        Intent intent = new Intent(mActivity, InviteContactActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("contactId", String.valueOf(contactid));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void editFriendsCircle(long uid, String circleids) {
        IntentUtil.startCircleSelectIntent(mActivity, uid, circleids);
    }

    /**
     * This filter will constrain edits not to make the length of the text
     * greater than the specified length.
     */
    public class LengthFilter implements InputFilter {
        private int mMax;

        public LengthFilter(int max) {
            mMax = max;
        }

        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            int keep = mMax - (dest.length() - (dend - dstart));
            if (keep <= 0) {
                Toast.makeText(mActivity, R.string.reach_max_length, Toast.LENGTH_SHORT).show();
                return "";
            } else if (keep >= end - start) {
                return null; // keep original
            } else {
                keep += start;
                if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                    --keep;
                    if (keep == start) {
                        return "";
                    }
                }
                return source.subSequence(start, keep);
            }
        }
    }

//	private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable){
//    	mprogressDialog = DialogUtils.createProgressDialog(mActivity, 
//    			resId, CanceledOnTouchOutside, Indeterminate, cancelable);
//    	mprogressDialog.show();    	
//    }

    private final static int MESSAGE_SEND_MAIL_END = 102;
    private final static int ADDRESSBOOK_SYNC_CALLBACK = 103;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SEND_MAIL_END: {
//				try {
//					mprogressDialog.dismiss();
//					mprogressDialog = null;
//				} catch (Exception ne) {}
                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret == false) {
                        Toast.makeText(mActivity, getString(R.string.send_message_failed), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mActivity, R.string.send_message_successful, Toast.LENGTH_SHORT).show();
                    }

                    break;
                }
                case ADDRESSBOOK_SYNC_CALLBACK: {
                    int statuscode = msg.getData().getInt("statuscode");

                    if (statuscode == PeopleLookupHelper.STATUS_DO_FAIL) {
                        end();

                    } else if (statuscode == PeopleLookupHelper.STATUS_DO_ALL_OK) {
                        int syncStatus = getCheckLocalPeopleSyncStatus();
                        if (syncStatus == PeopleLookupHelper.STATUS_DO_ALL_OK) {
                            end();
                        }
                        refreshUI();

                    } else if (statuscode == PeopleLookupHelper.STATUS_DOING) {
                        begin();

                    } else if (statuscode == PeopleLookupHelper.STATUS_DO_ONE_LOOP_OK) {
                        if (MODE_FRIENDS == mMode) {
                            mPickPeopleAdapter.notifyDataSetChanged();
                        }
                    }

                    break;
                }
            }
        }
    }

//	private void sendMessage(Context context, String phoneNumber, String display_name_primary, String message)
//	{
//        if (TextUtils.isEmpty(message)) {
//            message = ": ";
//        } else {
//            message = getString(R.string.to_say) + message + ",";
//        }
//
//        //the SIM card must be ready ,then can send the message.
//        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        int simState = mTelephonyManager.getSimState();
//        if(simState == TelephonyManager.SIM_STATE_ABSENT)
//        {
//        	Toast.makeText(context, R.string.sim_state_absent, Toast.LENGTH_SHORT).show();
//        	return ;
//        }
//        
//		SmsManager smsMgr=SmsManager.getDefault();
//		PendingIntent dummyEvent = PendingIntent.getBroadcast(context, 0, new Intent(), 0);
//		try 
//		{
//				String userinfo = phoneNumber+"/"+display_name_primary+"/"+AccountServiceUtils.getBorqsAccountID();
//				if(QiupuConfig.LOGD)Log.d(TAG, "userinfo : "+ userinfo);
//                final BorqsAccount account = AccountServiceUtils.getBorqsAccount();
//				String str = String.format(getString(R.string.qiupu_invite_message_content),
//                        account.nickname, getString(R.string.app_name), SimpleCrypt.encode(userinfo),
//                        ConfigurationBase.getAPIURL(), message);
//				if(QiupuConfig.LOGD)Log.d(TAG, "strs : "+ str);
//				if (str.length() > 70) {
//					ArrayList<String> msgs = smsMgr.divideMessage(str);
//						if(QiupuConfig.LOGD)Log.d(TAG, "msgs :"+msgs);
//						smsMgr.sendMultipartTextMessage(phoneNumber, null, msgs, null, null);
//					} else {
//						if(QiupuConfig.LOGD)Log.d(TAG, "str.length :"+str.length());
//						smsMgr.sendTextMessage(phoneNumber, null, str, dummyEvent, null);
//					}
//			Toast.makeText(context, R.string.send_message_successful, Toast.LENGTH_SHORT).show();
//		} catch (Exception e) {
//			if(QiupuConfig.LOGD)Log.e("SmsSending", "SendException", e);
//		}
//	}
//	
//	private boolean inviteWithEmail(final String phoneNumber, final String email, final String name, final String message) {
//		
//		showProcessDialog(R.string.email_invite_title, false, true, true);
//		
//		asyncQiupu.inviteWithMail(AccountServiceUtils.getSessionID(), phoneNumber, email, name, message, new TwitterAdapter() {
//			public void inviteWithMail(boolean result) {
//				Log.d(TAG, "finish inviteWithEmail="+result);				
//				
//				Message msg = mhandler.obtainMessage(MESSAGE_SEND_MAIL_END);				
//				msg.getData().putBoolean(RESULT, true);				
//				msg.sendToTarget();
//			}
//			
//			public void onException(TwitterException ex, TwitterMethod method) {
//				Message msg = mhandler.obtainMessage(MESSAGE_SEND_MAIL_END);				
//				msg.getData().putBoolean(RESULT, false);				
//				msg.sendToTarget();
//			}
//		});		
//		return false;
//	}

    private int getCheckLocalPeopleSyncStatus() {
        return mBookSync.getSyncStatus();
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

    @Override
    public void updateUI(int msgcode, Message message) {
        if (QiupuConfig.LOGD) Log.d(TAG, "msgcode: " + msgcode + " message: " + message);
        if (msgcode == PeopleLookupHelper.CONTENTOBSERVER_CHANGED) {
            if (MODE_FRIENDS == mMode) {

                if (mCurrentUsageCursor != null && mCurrentUsageCursor.isClosed() == false) {
                    mCurrentUsageCursor.close();
                }

                mCurrentUsageCursor = ContactUtils.getContacts(mActivity);
                Cursor cursorClone = ContactUtils.getContacts(mActivity);
                mPickPeopleAdapter.alterDataList(mCurrentUsageCursor, cursorClone, mAtoZ);

            } else if (MODE_ADD_FRIENDS == mMode) {
                showAddContactUI();
            }
        }
        Message msg = mhandler.obtainMessage(ADDRESSBOOK_SYNC_CALLBACK);
        msg.getData().putInt("statuscode", msgcode);
        msg.sendToTarget();
    }

    private void refreshUI() {
        if (MODE_FRIENDS == mMode) {
            //TODO update item which show on current screen
            mPickPeopleAdapter.refresh();
            mPickPeopleAdapter.notifyDataSetChanged();

        } else if (MODE_ADD_FRIENDS == mMode) {
            showAddContactUI();
        }
    }

    private void showAddContactUI() {
        if (contactidInMyCircle != null && contactidInMyCircle.length() > 0) {
            if (QiupuConfig.DBLOGD) Log.d(TAG, "no need query contact id in my circle " + contactidInMyCircle);
        } else {
            contactidInMyCircle = orm.getContactIdInMyCircle();
        }

        if (mCurrentUsageCursor != null && mCurrentUsageCursor.isClosed() == false) {
            mCurrentUsageCursor.close();
        }
        mCurrentUsageCursor = ContactUtils.getContactsNotInMyCircle(mActivity, contactidInMyCircle);

        Cursor cursorClone = ContactUtils.getContactsNotInMyCircle(mActivity, contactidInMyCircle);
        mPickPeopleAdapter.alterDataList(mCurrentUsageCursor, cursorClone, mAtoZ);
    }

    protected void doSearch(String key) {
        Log.d(TAG, "doSearch: " + key + " " + mMode);
        switch (mMode) {
            case MODE_FRIENDS: {
                if (mCurrentUsageCursor != null && mCurrentUsageCursor.isClosed() == false) {
                    mCurrentUsageCursor.close();
                }

                mCurrentUsageCursor = ContactUtils.searchContactByKey(mActivity, key);
                Cursor cursorClone = ContactUtils.searchContactByKey(mActivity, key);
                mPickPeopleAdapter.alterDataList(mCurrentUsageCursor, cursorClone, mAtoZ);
                break;
            }
            case MODE_ADD_FRIENDS: {
                contactidInMyCircle = orm.getContactIdInMyCircle();

                if (mCurrentUsageCursor != null && mCurrentUsageCursor.isClosed() == false) {
                    mCurrentUsageCursor.close();
                }

                mCurrentUsageCursor = ContactUtils.searchContactsNotInMyCircleByKey(mActivity, contactidInMyCircle, key);
                Cursor cursorClone = ContactUtils.searchContactsNotInMyCircleByKey(mActivity, contactidInMyCircle, key);
                mPickPeopleAdapter.alterDataList(mCurrentUsageCursor, cursorClone, mAtoZ);
                break;
            }
            case MODE_PICK_VCARD: {
                if (mCurrentUsageCursor != null && mCurrentUsageCursor.isClosed() == false) {
                    mCurrentUsageCursor.close();
                }

                mCurrentUsageCursor = ContactUtils.searchContactByKey(mActivity, key);
                Cursor cursorClone = ContactUtils.searchContactByKey(mActivity, key);
                mPickPeopleAdapter.alterDataList(mCurrentUsageCursor, cursorClone, mAtoZ);
                break;
            }
            default:
                Log.d(TAG, "error, No such mode");
                break;
        }
    }

    private void refreshItemUI(final long borqsid, final long contactId) {
        mhandler.post(new Runnable() {
            public void run() {
                //process for UI
                for (int j = 0; j < mListView.getChildCount(); j++) {
                    View v = mListView.getChildAt(j);
                    if (PickPeopleItemView.class.isInstance(v)) {
                        PickPeopleItemView fv = (PickPeopleItemView) v;
                        if (fv.refreshItem(borqsid, contactId)) {
                            break;
                        }
                    }
                }
            }
        });
    }

    public void loadRefresh() {
        mPickPeopleAdapter.refresh();
        mBookSync.alarmAddessbookComming(true);
    }

    public interface CallBackFriendsContactFragmentListener {
        public void getFriendsContactFragment(FriendsContactFragment fragment);

        public int getMode();
    }

    public void updateListUI(QiupuUser user) {

        if (user != null) {
            if (MODE_FRIENDS == mMode) {
                refreshItemUI(user.uid, user.contactId);

            } else if (MODE_ADD_FRIENDS == mMode) {
                showAddContactUI();
            }
        }
    }
}
