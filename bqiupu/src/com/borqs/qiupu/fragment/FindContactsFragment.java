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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.account.service.PeopleLookupHelper;
import com.borqs.account.service.PeopleLookupHelper.AddressBookSyncServiceListener;
import com.borqs.account.service.db.BorqsAccountORM;
import com.borqs.common.adapter.PickPeopleAdapter;
import com.borqs.common.listener.FriendsContactActionListner;
import com.borqs.common.listener.OnListItemClickListener;
import com.borqs.common.listener.ShakeActionListener;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.PickPeopleItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.bpc.InviteContactActivity;
import com.borqs.qiupu.util.ContactUtils;

public class FindContactsFragment extends PeopleSearchableFragment implements
        FriendsContactActionListner, AddressBookSyncServiceListener {
    private final static String TAG = "FindContactsFragment";

    private Handler mHandler;
    private PickPeopleAdapter mPickPeopleAdapter;
    private PeopleLookupHelper mBookSync;
    private Cursor mCurrentUsageCursor;
    private ShakeActionListener mShakeListener;
    private boolean mSkipFlag = false;

    @Override
    public void onAttach(Activity activity) {
        mActivity = activity;

        if (ShakeActionListener.class.isInstance(activity)) {
            mShakeListener = (ShakeActionListener) activity;
            mShakeListener.getContactFragmentCallBack(this);
        }

        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new MainHandler();

        mSkipFlag = QiupuORM.isEnableFindContacts(mActivity);

        BorqsAccountORM accountorm = BorqsAccountORM.getInstance(mActivity);
        AsyncBorqsAccount mAsyncBorqsAccount = new AsyncBorqsAccount(ConfigurationContext.getInstance(), null, null);
        mBookSync = PeopleLookupHelper.getInstance(mActivity, accountorm, mAsyncBorqsAccount);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View convertView = super.onCreateView(inflater, container, savedInstanceState);

        mGuideContainer = mConvertView.findViewById(R.id.guide_layout_container);
        mSkipView = (TextView) mConvertView.findViewById(R.id.find_friends_intro_skip);
        mFindView = (Button) mConvertView.findViewById(R.id.find_friends_intro_button);

        Log.d(TAG, "onCreateView() mGuideContainer = " + mGuideContainer + " mSkipView = " + mSkipView + " mFindView = " + mFindView);

        mListView = (ListView) convertView.findViewById(R.id.friends_list);
        mListView.setOnItemClickListener(contactitemClickListener);

        return convertView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showContactUI(mSkipFlag);

        mPickPeopleAdapter = new PickPeopleAdapter(mActivity, false);

        mHandler.postDelayed(new Runnable() {
            public void run() {
                mPickPeopleAdapter.registerFriendsContactActionListener(getClass().getName(), FindContactsFragment.this);
                mListView.setAdapter(mPickPeopleAdapter);
                int status = getCheckLocalPeopleSyncStatus();
                Log.d(TAG, "status : " + status);

                // 1, if doing will show center process
                if (status == PeopleLookupHelper.STATUS_DOING) {
                    begin();
                } else if (status == PeopleLookupHelper.STATUS_DEFAULT) {
                    mBookSync.triggerPeopleLookupSession(false);
                } else {
                    showAddContactUI();
                }

            }
        }, 500);
    }

    @Override
    public void onResume() {
        PeopleLookupHelper.registerFriendsServiceListener(getClass().getName(), this);
        super.onResume();
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
            if (view instanceof PickPeopleItemView) {
                PickPeopleItemView item = (PickPeopleItemView) view;
                if (item != null) {
                    IntentUtil.startContactDetailIntent(mActivity, item.getContactSimpleInfo().mContactId);
                }
            } else if (OnListItemClickListener.class.isInstance(mActivity)) {
                OnListItemClickListener listener = (OnListItemClickListener) mActivity;
                listener.onListItemClick(view, FindContactsFragment.this);
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

    private final static int ADDRESSBOOK_SYNC_CALLBACK = 103;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
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
                    }
                    break;
                }
                case QiupuMessage.MESSAGE_LOAD_DATA_END: {
                    
                    break;
                }
                default:
                    Log.d(TAG, "default case, no action.");
                    break;
            }
        }
    }

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
            showAddContactUI();
        }
        Message msg = mHandler.obtainMessage(ADDRESSBOOK_SYNC_CALLBACK);
        msg.getData().putInt("statuscode", msgcode);
        msg.sendToTarget();
    }

    private void refreshUI() {
        showAddContactUI();
    }

    private void showAddContactUI() {
        if (mCurrentUsageCursor != null && mCurrentUsageCursor.isClosed() == false) {
            mCurrentUsageCursor.close();
        }
        mCurrentUsageCursor = ContactUtils.getContacts(mActivity);

        Cursor cursorClone = ContactUtils.getContacts(mActivity);
        mPickPeopleAdapter.alterDataList(mCurrentUsageCursor, cursorClone, mAtoZ);
    }

    protected void doSearch(String key) {
        Log.d(TAG, "doSearch: " + key);

        if (mCurrentUsageCursor != null && mCurrentUsageCursor.isClosed() == false) {
            mCurrentUsageCursor.close();
        }

        mCurrentUsageCursor = ContactUtils.searchContactByKey(mActivity, key);
        Cursor cursorClone = ContactUtils.searchContactByKey(mActivity, key);
        mPickPeopleAdapter.alterDataList(mCurrentUsageCursor, cursorClone, mAtoZ);
    }

    private void refreshItemUI(final long borqsid, final long contactId) {
        mHandler.post(new Runnable() {
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

    public void updateListUI(QiupuUser user) {
        if (user != null) {
            showAddContactUI();
        }
    }

    public void showContactUI(boolean show) {
        if (show) {
            mGuideContainer.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mAtoZ.setVisibility(View.VISIBLE);
        } else {
            mGuideContainer.setVisibility(View.VISIBLE);
            setGuideUI();

            mListView.setVisibility(View.GONE);
            mAtoZ.setVisibility(View.GONE);
        }
    }

    private void setGuideUI() {
        if (null != mSkipView) {
            mSkipView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null != mWizardListener && mWizardListener.get() != null) {
                        mWizardListener.get().skip();
                    }
                }
            });
        }

        if (null != mFindView) {
            mFindView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null != mWizardListener && mWizardListener.get() != null) {
                        mWizardListener.get().invoke();
                    }
                }
            });
        }
    }

//    @Override
//    public void onListItemClick(View view, Fragment fg) {
//        if (PickPeopleItemView.class.isInstance(view)) {
//            Log.d(TAG, "onListItemClick ");
//            PickPeopleItemView item = (PickPeopleItemView) view;
//            if(item != null){
//                IntentUtil.startContactDetailIntent(mActivity, item.getContactSimpleInfo().mContactId);
//            }
//        }
//    }
}
