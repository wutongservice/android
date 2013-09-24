package com.borqs.qiupu.fragment;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Window;
import android.widget.*;
import twitter4j.AsyncQiupu;
import twitter4j.QiupuUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.conf.ConfigurationContext;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.common.adapter.PickCircleUserAdapter.MoreItemCheckListener;
import com.borqs.common.adapter.PickContactAdapter;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.view.ContactUserSelectItemView;
import com.borqs.common.view.ContactUserSelectItemView.AllPeoplecheckItemListener;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.InviteContactActivity.UITAB;
import com.borqs.qiupu.ui.bpc.BpcSearchActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;

public class PickContactBaseFragment extends PeopleSearchableFragment implements AllPeoplecheckItemListener, MoreItemCheckListener {
    private static final String TAG = "PickContactBaseFragment";

    protected Activity mActivity;
    protected ListView mListView;
    protected PickContactAdapter mAdapter;
    protected Handler mHandler;
    private AsyncQiupu asyncQiupu;
    private ProgressDialog mprogressDialog;
    protected long mCircleId;
    protected String mCircleName;
    private ArrayList<QiupuUser> mUserList = new ArrayList<QiupuUser>();

    private static final String RESULT = "result";
    private static final String ERRORMSG = "errormsg";
    private int mPage = 0;
    private static final int mCount = 20;

    private boolean isUserShowMore;

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        orm = QiupuORM.getInstance(mActivity);
        mHandler = new MainHandler();
        asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null, null);
        Intent intent = mActivity.getIntent();
        mCircleId = intent.getLongExtra(CircleUtils.CIRCLE_ID, 0);
        mCircleName = intent.getStringExtra(CircleUtils.CIRCLE_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        mListView = (ListView) inflater.inflate(R.layout.default_listview,
                container, false);

        mListView.addHeaderView(initHeadView());
        mListView.setOnItemClickListener(contactitemClickListener);
        mAdapter = new PickContactAdapter(mActivity, false);
        mListView.setAdapter(mAdapter);
        return mListView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
    }

    ;

    public void onPause() {
        super.onPause();
    }

    ;

    public void onDestroy() {
        super.onDestroy();
    }

    ;

    private static final int INVIT_PEOPLE_END = 101;
    private static final int LOAD_SEARCH_END = 102;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INVIT_PEOPLE_END: {
                    try {
                        mprogressDialog.dismiss();
                        mprogressDialog = null;
                    } catch (Exception ne) {
                    }
                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret == true) {
                        Log.d(TAG, "invite people end ");
                        ToastUtil.showOperationOk(mActivity, mHandler, true);
                        updateActivityUI();
                        mActivity.finish();
                    } else {
                        ToastUtil.showOperationFailed(mActivity, mHandler, true);
                    }
                    break;
                }
                case LOAD_SEARCH_END: {
                    end();
                    setMoreItem(false);
                    if (msg.getData().getBoolean(RESULT)) {
                        showSearchFromServerButton(false, mSearchKey, null);
                        doSearchEndCallBack(mUserList);
                        if (mUserList.size() <= 0) {
                            ToastUtil.showShortToast(mActivity, mHandler, R.string.search_people_result_null);
                        }
                    } else {
                        ToastUtil.showOperationFailed(mActivity, mHandler, true);
                    }
                    break;
                }
            }
        }
    }

    private AdapterView.OnItemClickListener contactitemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
            if (ContactUserSelectItemView.class.isInstance(view)) {
                ContactUserSelectItemView uv = (ContactUserSelectItemView) view;
                uv.switchCheck();
            }
        }
    };

    @Override
    public void selectItem(ContactSimpleInfo user) {
        if (user != null) {
            if (user.mBindId == UITAB.DISPLAY_EMAIL) {
                selectEmail(user);
            } else if (UITAB.DISPLAY_PHONE == user.mBindId) {
                selectPhone(user);
            }
        }
    }

    protected void selectEmail(ContactSimpleInfo user) {
    }

    protected void selectPhone(ContactSimpleInfo user) {
    }

    public String getSelectValue(HashSet<String> selectValue) {
        Iterator<String> it = selectValue.iterator();
        StringBuilder ids = new StringBuilder();

        while (it.hasNext()) {
            if (ids.length() > 0) {
                ids.append(",");
            }
            ids.append(it.next());

        }
        return ids.toString();
    }

    public String getSelectName(HashMap<String, String> selectMap) {
        StringBuilder tmpString = new StringBuilder();
        Iterator iter = selectMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String val = (String) entry.getValue();
            if (tmpString.length() > 0) {
                tmpString.append(",");
            }
            tmpString.append(val);
        }
        return tmpString.toString();
    }

//    private class MyWatcher implements TextWatcher {
//        public void afterTextChanged(Editable s) {
//            // do search
//            doSearch(s.toString().trim());
//        }
//
//        public void beforeTextChanged(CharSequence s, int start, int count,
//                                      int after) {
//        }
//
//        public void onTextChanged(CharSequence s, int start, int before,
//                                  int count) {
//        }
//    }

    protected void doSearch(String key) {
    }

    public interface PickContactBaseFragmentCallBack {
        public void getPickContactBaseFragment(PickContactBaseFragment fragment);
    }

    public String getSelectName() {
        return "";
    }

    public String getselectValue() {
        return "";
    }

    protected void invitePeopleCallback(ArrayList<Long> joinIds) {
    }

    private boolean mSendEmailStatus = true;
    private boolean mSendSmsStatus = false;

    public void invitePeople() {
        final String ids = getselectValue();
        final String names = getSelectName();
        if (QiupuConfig.LOGD) Log.d(TAG, "selectedValue : " + ids + " selectName: " + names);
        if (ids.length() > 0) {
//            LayoutInflater inflater = getActivity().getLayoutInflater();
//            View view = inflater.inflate(R.layout.invitation_composer, null);
//            final TextView recipientView = (TextView)view.findViewById(R.id.invitation_recipient);
//            if (null != recipientView) {
//                recipientView.setText(names);
//            }
//            final TextView messageView = (TextView)view.findViewById(R.id.invitation_message);
//            if (null == messageView) {
//                invitePeopleInPublicCircle(String.valueOf(mCircleId), ids, names, "");
//            } else {
//                AlertDialog.Builder invitationBuilder = new AlertDialog.Builder(getActivity());
//                invitationBuilder.setTitle(R.string.invitation_message_title)
//                        .setView(view)
//                        .setPositiveButton(R.string.send_action_title, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                String message = messageView.getText().toString();
//                                if (TextUtils.isEmpty(message)) {
//                                    Toast.makeText(getActivity(), R.string.toast_invalid_input_content, Toast.LENGTH_SHORT).show();
//                                    messageView.requestFocus();
//                                } else {
//                                    invitePeopleInPublicCircle(String.valueOf(mCircleId), ids, names, message);
//                                }
//                            }
//                        })
//                        .setNegativeButton(R.string.label_skip, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                final String message = messageView.getText().toString();
//                                if (TextUtils.isEmpty(message)) {
//                                    invitePeopleInPublicCircle(String.valueOf(mCircleId), ids, names, "");
//                                } else {
//                                    DialogUtils.showConfirmDialog(getActivity(), R.string.invitation_discard_title,
//                                            R.string.invitation_discard_message,
//                                            R.string.label_ok, R.string.label_cancel,
//                                            new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                    invitePeopleInPublicCircle(String.valueOf(mCircleId), ids, names, "");
//                                                }
//                                            }
//                                    );
//                                }
//                            }
//                        });
//                AlertDialog dialog = invitationBuilder.create();
//                dialog.setCanceledOnTouchOutside(false);
//
//                dialog.show();
//            }
            final Dialog dialog = new Dialog(getActivity());
//            dialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
            View view = getActivity().getLayoutInflater().inflate(R.layout.invitation_composer, null);
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            dialog.setContentView(view);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setTitle(R.string.invitation_message_title);
            final TextView recipientView = (TextView) dialog.findViewById(R.id.invitation_recipient);
            if (null != recipientView) {
                recipientView.setText(names);
            }

            final CheckBox sendEmail = (CheckBox) dialog.findViewById(R.id.send_email_checkbox);
            sendEmail.setVisibility(View.VISIBLE);
            if (null != sendEmail) {
                sendEmail.setChecked(mSendEmailStatus);
                sendEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSendEmailStatus = !mSendEmailStatus;
                        sendEmail.setChecked(mSendEmailStatus);
                    }
                });
            }

            final CheckBox sendSms = (CheckBox) dialog.findViewById(R.id.send_sms_checkbox);
            sendSms.setVisibility(View.VISIBLE);
            if (null != sendSms) {
                sendSms.setChecked(mSendSmsStatus);
                sendSms.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSendSmsStatus = !mSendSmsStatus;
                        sendSms.setChecked(mSendSmsStatus);
                    }
                });
            }

            final TextView messageView = (TextView) dialog.findViewById(R.id.invitation_message);
            if (null != messageView) {
                final View sendView = dialog.findViewById(R.id.invitation_btn_send);
                if (null != sendView) {
                    sendView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String message = messageView.getText().toString();
                            if (TextUtils.isEmpty(message)) {
                                Toast.makeText(getActivity(), R.string.toast_invalid_input_content, Toast.LENGTH_SHORT).show();
                                messageView.requestFocus();
                            } else {
                                invitePeopleInPublicCircle(String.valueOf(mCircleId), ids, names, message, mSendEmailStatus, mSendSmsStatus);
                                dialog.dismiss();
                            }
                        }
                    });
                }
                final View skipView = dialog.findViewById(R.id.invitation_btn_skip);
                if (null != skipView) {
                    skipView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final String message = messageView.getText().toString();
                            if (TextUtils.isEmpty(message)) {
                                invitePeopleInPublicCircle(String.valueOf(mCircleId), ids, names, "", mSendEmailStatus, mSendSmsStatus);
                                dialog.dismiss();
                            } else {
                                DialogUtils.showConfirmDialog(getActivity(), R.string.invitation_discard_title,
                                        R.string.invitation_discard_message,
                                        R.string.label_ok, R.string.label_cancel,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                invitePeopleInPublicCircle(String.valueOf(mCircleId), ids, names, "", mSendEmailStatus, mSendSmsStatus);
                                                dialog.dismiss();
                                            }
                                        }
                                );
                            }
                        }
                    });
                }
            }
            dialog.show();
        } else {
            ToastUtil.showCustomToast(mActivity, R.string.invite_have_no_select_toast);
        }
    }

    boolean inInvitePeople;
    Object mLockInvitePeople = new Object();

    private void invitePeopleInPublicCircle(final String circleId, final String toids, final String toNames, final String message,
            final boolean sendEmail, final boolean sendSms) {
        if (inInvitePeople == true) {
            ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
            return;
        }

        synchronized (mLockInvitePeople) {
            inInvitePeople = true;
        }

        showProcessDialog(R.string.invite_progress_dialog_message, false, true, true);

        asyncQiupu.publicInvitePeople(AccountServiceUtils.getSessionID(), circleId, toids, toNames, message,
                sendEmail, sendSms, new TwitterAdapter() {
            public void publicInvitePeople(ArrayList<Long> joinIds) {
                Log.d(TAG, "finish invitePeopleInPublicCircle=" + joinIds.size());
                invitePeopleCallback(joinIds);

                Message msg = mHandler.obtainMessage(INVIT_PEOPLE_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();
                synchronized (mLockInvitePeople) {
                    inInvitePeople = false;
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockInvitePeople) {
                    inInvitePeople = false;
                }
                Message msg = mHandler.obtainMessage(INVIT_PEOPLE_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }

    private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable) {
        mprogressDialog = DialogUtils.createProgressDialog(mActivity,
                resId, CanceledOnTouchOutside, Indeterminate, cancelable);
        mprogressDialog.show();
    }


    private void updateActivityUI() {
        synchronized (userlisteners) {
            Log.d(TAG, "userlisteners.size() : " + userlisteners.size());
            Set<String> set = QiupuHelper.userlisteners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                WeakReference<invitePepoleListeners> ref = userlisteners.get(key);
                if (ref != null && ref.get() != null) {
                    invitePepoleListeners listener = ref.get();
                    if (listener != null) {
                        listener.updateUi();
                    }
                }
            }
        }
    }

    public static final HashMap<String, WeakReference<invitePepoleListeners>> userlisteners = new HashMap<String, WeakReference<invitePepoleListeners>>();

    public static void registerUserListener(String key,
                                            invitePepoleListeners listener) {
        synchronized (userlisteners) {
            WeakReference<invitePepoleListeners> ref = userlisteners.get(key);
            if (ref != null && ref.get() != null) {
                ref.clear();
            }
            userlisteners.put(key, new WeakReference<invitePepoleListeners>(
                    listener));
        }
    }

    public static void unregisterUserListener(String key) {
        synchronized (userlisteners) {
            WeakReference<invitePepoleListeners> ref = userlisteners.get(key);
            if (ref != null && ref.get() != null) {
                ref.clear();
            }
            userlisteners.remove(key);
        }
    }

    @Override
    protected void showSearchFromServerButton(boolean show, final String key,
                                              OnClickListener callback) {
        super.showSearchFromServerButton(show, key, new View.OnClickListener() {
            public void onClick(View v) {
                mPage = 0;
                isNeedRefreshUi = true;
                searchFriends(key, mPage);
            }
        });
    }

    private void searchFriends(String str, int page) {
        if (QiupuConfig.LOGD) Log.d(TAG, "searchFriends");

        if (str == null || str.equals("")) {
            callFailLoadUserMethod();
            if (QiupuConfig.LOGD) Log.d(TAG, "input string is null");
            return;
        }
        str = str.trim().toLowerCase();

        begin();
        setMoreItem(true);
        asyncQiupu.getUserListWithSearchName(AccountServiceUtils.getSessionID(), str, str, str, page, mCount, new TwitterAdapter() {
            public void getUserListWithSearchName(ArrayList<QiupuUser> users) {
                Log.d(TAG, "finish search user : " + users.size());

                if (users != null) {
                    doSearchUserCallBack(true, users);
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                doSearchUserCallBack(false, null);
                callFailLoadUserMethod();
            }
        });
    }

    protected void doSearchUserCallBack(boolean result, ArrayList<QiupuUser> userList) {
        if (result) {
            if (mPage == 0) {
                mUserList.clear();
            }
            mUserList.addAll(userList);
            if (userList.size() <= 0) {
                isUserShowMore = false;
            } else {
                isUserShowMore = true;
            }
        }
        Message msg = mHandler.obtainMessage(LOAD_SEARCH_END);
        msg.getData().putBoolean(RESULT, result);
        msg.sendToTarget();
    }

    protected void doSearchEndCallBack(ArrayList<QiupuUser> userList) {
    }

    public interface invitePepoleListeners {
        public void updateUi();
    }

    @Override
    public boolean isMoreItemHidden() {
        return isUserShowMore;
    }

    @Override
    public OnClickListener getMoreItemClickListener() {
        return loadOlderClick;
    }

    @Override
    public int getMoreItemCaptionId() {
        return R.string.list_view_more;
    }

    public View.OnClickListener loadOlderClick = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "load more user");
            getOldData();
        }
    };

    private void getOldData() {
        mPage += 1;
        Log.i(TAG, "Page:" + mPage);
        try {
            failCallSyncUserMethod = PickContactBaseFragment.class
                    .getDeclaredMethod("SubUserPage", (Class[]) null);
        } catch (Exception e) {
        }
        searchFriends(mSearchKey, mPage);
    }

    protected Method failCallSyncUserMethod;

    protected void SubUserPage() {
        Log.d(TAG, "resore the dpage--");
        mPage--;
        if (mPage < 0)
            mPage = 0;
    }

    protected void callFailLoadUserMethod() {
        try {
            if (failCallSyncUserMethod != null) {
                failCallSyncUserMethod.invoke(this, (Object[]) null);
            }
        } catch (Exception ne) {
        }
    }

    private void setMoreItem(boolean loading) {
        //set load older button text process for UI
        if (mListView != null) {
            for (int i = mListView.getChildCount() - 1; i > 0; i--) {
                View v = mListView.getChildAt(i);
                if (Button.class.isInstance(v)) {
                    Button bt = (Button) v;
                    if (loading) {
                        bt.setText(R.string.loading);
                    } else {
                        bt.setText(R.string.list_view_more);
                    }
                    break;
                }
            }
        }
    }
}
