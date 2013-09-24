package com.borqs.qiupu.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import twitter4j.AsyncQiupu;
import twitter4j.Requests;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.RequestsAdapter;
import com.borqs.common.listener.RequestActionListner;
import com.borqs.common.listener.RequestStatusListener;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.PhoneEmailColumns;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.JSONUtil;
import com.borqs.qiupu.util.StatusNotification;
import com.borqs.qiupu.util.ToastUtil;

public class RequestFragment extends BasicFragment implements RequestActionListner {
    private static final String TAG        = "Qiupu.RequestFragment";
    public static final String REQUEST_TYPES = "REQUEST_TYPES";
    private Handler             mHandler;
    private ListView            mListView;
    private View                mRefreshTutorial;
    private TextView            refresh_tv;

    private ArrayList<Requests> mRequests;
    private RequestsAdapter     mRequestAdapter;
    private static final String REQUEST_ID = "REQUEST_ID";
    private Map<String, String> map        = new LinkedHashMap<String, String>();

    private Activity     mActivity;
    private AsyncQiupu          asyncQiupu;
    private QiupuORM            orm;
    private RequestStatusListener mTitleListener;
    private String mRequestTypes;
    public static final int FRIEND_REQUEST = 91;
    public static final int EVENT_REQUEST  = 92;
    public static final int CIRCLE_REQUEST = 93;
    public static final int CHANGE_REQUEST = 94;

    public RequestFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
        mActivity = activity;
        if(RequestStatusListener.class.isInstance(activity)) {
            mTitleListener = (RequestStatusListener) activity;
        }
        orm = QiupuORM.getInstance(mActivity);
        asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null,
                null);
        mHandler = new MainHandler();
        mRequestTypes = getActivity().getIntent().getStringExtra("REQUEST_TYPES");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mRequests = orm.buildRequestList(mRequestTypes, -1);

        if (mRequests == null) {
        	mRequests = new ArrayList<Requests>();
        }
        
        mRequestAdapter = new RequestsAdapter(mActivity, mRequests);
        mRequestAdapter.setRequestActionListener(this);
//        mRequestAdapter.registerRequestActionListener(getClass().getName(), this);
        mRequestAdapter.notifyDataSetChanged();
        initContactInfomap();// init myinfo map to deal with update my info

//        if (mAllRequests == null ||  mAllRequests.size() <= 0) {
            mHandler.obtainMessage(REQUEST_GET).sendToTarget();
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.request_ui, null);

        mListView = (ListView) view.findViewById(R.id.content);
        mRefreshTutorial = view.findViewById(R.id.refresh_tutorial);
        refresh_tv = (TextView) view.findViewById(R.id.refresh_tv);

        mListView.setAdapter(mRequestAdapter);
        return view;
    }

    public void loadRefresh() {
        mHandler.obtainMessage(REQUEST_GET).sendToTarget();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRequestAdapter.setRequestActionListener(null);
//        mRequestAdapter.unRegisterRequestActionListener(getClass().getName());
    }

    private final static int REQUEST_GET      = 1;
    private final static int REQUEST_GET_END  = 2;
    private final static int REQUEST_DONE     = 3;
    private final static int REQUEST_DONE_END = 4;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_GET: {
                    getRequests();
                    break;
                }
                case REQUEST_GET_END: {
                    end();

                    boolean suc = msg.getData().getBoolean("RESULT");
                    if (suc) {
                        // if get request Successfully, will cancel notification
                        NotificationManager notificationManager = (NotificationManager) mActivity
                                .getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager
                                .cancel(StatusNotification.QIUPU_NITIFY_REQUESTS_ID);
                        mRequestAdapter.alterRequests(mRequests);
                        updateRequestCountUI();
//                        setMainActivityCount();
                        if (mRequests.size() <= 0) {
                            mRefreshTutorial.setVisibility(View.VISIBLE);
                            refresh_tv.setText(R.string.have_no_request_info);
                        } else {
                            mRefreshTutorial.setVisibility(View.GONE);
                        }
                    } else {
                        Log.d(TAG, "get request failed!");
                    }
                    break;
                }
                case REQUEST_DONE: {
                    String requestid = msg.getData().getString("REQUEST_ID");
                    Log.d(TAG, "=========== requestid = " + requestid);
                    doneRequests(requestid);
                    break;
                }
                case REQUEST_DONE_END: {
                    end();
                    boolean suc = msg.getData().getBoolean("RESULT");
                    if (suc) {
                        String request_id = msg.getData().getString("request_id");
                        boolean deletesucceed = deleteDoneRequest(request_id);
                        ToastUtil.showOperationOk(mActivity, mHandler, true);
                        Log.d(TAG, "request_id = " + request_id + " deletesucceed = " + deletesucceed);
                        updateUIAfterDone();
                    } else {
                        Log.d(TAG, "send done request failed! ");
                    }
                    break;
                }
            }
        }
    }

    private void getRequests() {
        if (!AccountServiceUtils.isAccountReady()) {
            Log.d(TAG, "getRequests, mAccount is null exit");
            return;
        }
        if (!DataConnectionUtils.testValidConnection(mActivity)) {
            if (mRequests.size() <= 0 ) {
                mRefreshTutorial.setVisibility(View.VISIBLE);
                refresh_tv.setText(R.string.network_exception);
            } else {
                mRefreshTutorial.setVisibility(View.GONE);
            }
            return;
        } else {
            mRefreshTutorial.setVisibility(View.GONE);
        }

        begin();
 
        String type = "";
        if (TextUtils.isEmpty(mRequestTypes)) {
            type = mRequestTypes;
        } else {
            if (mRequestTypes.equals(Requests.getFriendsRequestTypes())) {
                type = String.valueOf(FRIEND_REQUEST);
            } else if (mRequestTypes.equals(Requests.getEventRequestTypes())) {
                type = String.valueOf(EVENT_REQUEST);
            } else if (mRequestTypes.equals(Requests.getPublicCircleRequestTypes())) {
                type = String.valueOf(CIRCLE_REQUEST);
            } else if (mRequestTypes.equals(String.valueOf(Requests.REQUEST_TYPE_EXCHANGE_VCARD))) {
                type = String.valueOf(CHANGE_REQUEST);
            } else {
                Log.d(TAG, "unSupported request type, mRequestTypes = " + mRequestTypes);
            }
        }

        Log.d(TAG, "type = " + type);
    	
        asyncQiupu.getRequests(AccountServiceUtils.getSessionID(), /*mRequestTypes*/ type, new TwitterAdapter() {
                    public void getRequests(ArrayList<Requests> requests) {
                        Log.d(TAG, "finish getRequests=" + requests.size());

                        mRequests.clear();
                        mRequests.addAll(requests);

                        if (mRequests.size() > 0) {
                        	//TODO this class unused. param is -1
                            QiupuORM.getInstance(mActivity).cacheRequests(requests, mRequestTypes, -1);
                        }else {
                        	QiupuORM.getInstance(mActivity).clearRequestsWithType(mRequestTypes);
                        }

                        Message mds = mHandler.obtainMessage(REQUEST_GET_END);
                        mds.getData().putBoolean("RESULT", true);
                        mHandler.sendMessage(mds);

                    }

                    public void onException(TwitterException ex,
                            TwitterMethod method) {
                        TwitterExceptionUtils.printException(TAG,
                                "getRequests, server exception:", ex, method);

                        Message mds = mHandler.obtainMessage(REQUEST_GET_END);
                        mds.getData().putBoolean("RESULT", false);
                        mHandler.sendMessage(mds);
                    }
                });
    }
    
    private void doneRequests(final String requestid) {
    	doneRequests(requestid, -1, "", false);
    }

    private void doneRequests(final String requestid, final int type, final String data, final boolean isAccept) {
        if (!AccountServiceUtils.isAccountReady()) {
            Log.d(TAG, "getRequests, mAccount is null exit");
            return;
        }

        begin();
        asyncQiupu.doneRequests(AccountServiceUtils.getSessionID(), requestid, type, data, isAccept,
                new TwitterAdapter() {
                    public void doneRequests(boolean suc) {
                        Log.d(TAG, "finish doneRequests = " + suc);

                        Message mds = mHandler.obtainMessage(REQUEST_DONE_END);
                        mds.getData().putBoolean("RESULT", suc);
                        Log.d(TAG, "requestid = " + requestid);
                        mds.getData().putString("request_id", requestid);
                        mHandler.sendMessage(mds);

                    }

                    public void onException(TwitterException ex,
                            TwitterMethod method) {
                        TwitterExceptionUtils.printException(TAG,
                                "doneRequests, server exception:", ex, method);

                        Message mds = mHandler.obtainMessage(REQUEST_DONE_END);
                        mds.getData().putBoolean("RESULT", false);
                        mHandler.sendMessage(mds);
                    }
                });
    }

    private Requests mRequest = new Requests();
    private int      mType;

    @Override
    public void acceptRequest(Requests request, int type) {
        Log.d(TAG, "acceptRequest() type = " + type);
        mRequest = request;
        mType = type;
        if (type == Requests.REQUEST_TYPE_EXCHANGE_VCARD) {
            // set to my circles 'privacy circle/default circle'
            mTitleListener.setCircle(mRequest.user.uid,
                        CircleUtils.getDefaultCircleId(),
                        CircleUtils.getDefaultCircleName(getResources()));
        } else if (type == Requests.REQUEST_TYPE_CHANGE_PHONE_1) {
            gotoUpdateContactInfo(QiupuConfig.TYPE_PHONE1, mRequest.data);
        } else if (type == Requests.REQUEST_TYPE_CHANGE_PHONE_2) {
            gotoUpdateContactInfo(QiupuConfig.TYPE_PHONE2, mRequest.data);
        } else if (type == Requests.REQUEST_TYPE_CHANGE_PHONE_3) {
            gotoUpdateContactInfo(QiupuConfig.TYPE_PHONE3, mRequest.data);
        } else if (type == Requests.REQUEST_TYPE_CHANGE_EMAIL_1) {
            gotoUpdateContactInfo(QiupuConfig.TYPE_EMAIL1, mRequest.data);
        } else if (type == Requests.REQUEST_TYPE_CHANGE_EMAIL_2) {
            gotoUpdateContactInfo(QiupuConfig.TYPE_EMAIL2, mRequest.data);
        } else if (type == Requests.REQUEST_TYPE_CHANGE_EMAIL_3) {
            gotoUpdateContactInfo(QiupuConfig.TYPE_EMAIL3, mRequest.data);
        } else if(type == Requests.REQUEST_EVENT_INVITE || type == Requests.REQUEST_EVENT_JOIN
        		|| type == Requests.REQUEST_PUBLIC_CIRCLE_INVITE
        		|| type == Requests.REQUEST_PUBLIC_CIRCLE_JOIN) {
        	doneRequests(mRequest.rid, mRequest.type, mRequest.data, true);
        }
    }

    private void gotoUpdateContactInfo(String col, String data) {
        HashMap<String, String> contactInfoMap = organizationContactMap(col,
                data);

        HashMap<String, String> infoMap = new HashMap<String, String>();
        // String value = JSONUtil.createContactInfoJSONObject(contactInfoMap);
        infoMap.put("contact_info", JSONUtil.createContactInfoJSONObject(contactInfoMap));
        mTitleListener.updateUserInfo(infoMap);
    }

    public void doUpdateUserInfoEndCallBack(boolean suc) {
        if (suc) {
            long uid = AccountServiceUtils.getBorqsAccountID();
            String tmpType = "";
            if (orm.isExistingUser(uid)) {
                if (mType == Requests.REQUEST_TYPE_CHANGE_PHONE_1) {
                    tmpType = QiupuConfig.TYPE_PHONE1;
                } else if (mType == Requests.REQUEST_TYPE_CHANGE_PHONE_2) {
                    tmpType = QiupuConfig.TYPE_PHONE2;
                } else if (mType == Requests.REQUEST_TYPE_CHANGE_PHONE_3) {
                    tmpType = QiupuConfig.TYPE_PHONE3;
                } else if (mType == Requests.REQUEST_TYPE_CHANGE_EMAIL_1) {
                    tmpType = QiupuConfig.TYPE_EMAIL1;
                } else if (mType == Requests.REQUEST_TYPE_CHANGE_EMAIL_2) {
                    tmpType = QiupuConfig.TYPE_EMAIL2;
                } else if (mType == Requests.REQUEST_TYPE_CHANGE_EMAIL_3) {
                    tmpType = QiupuConfig.TYPE_EMAIL3;
                }
                orm.updatePhoneEmailInfo(uid, tmpType, mRequest.data);
            } else {
                Log.d(TAG, "doUpdateUserInfoEndCallBack, why no user id=" + uid);
            }

            updateUIAfterDone();
            Message donemsg = mHandler.obtainMessage(REQUEST_DONE);
            donemsg.getData().putString(REQUEST_ID, mRequest.rid);
            donemsg.sendToTarget();
        }
    }

    @Override
    public void refuseRequest(Requests request) {
        mRequest = request;
        if(request.type == Requests.REQUEST_EVENT_INVITE || request.type == Requests.REQUEST_EVENT_JOIN
        		|| request.type == Requests.REQUEST_PUBLIC_CIRCLE_INVITE
        		|| request.type == Requests.REQUEST_PUBLIC_CIRCLE_JOIN) {
        	doneRequests(request.rid, request.type, request.data, false);
        }else {
        	doneRequests(request.rid);
        }
    }

    public void doActionFriendEndCallBack(Message msg) {
        boolean suc = msg.getData().getBoolean("RESULT", false);
        if (suc) {
            boolean deleteResult = deleteDoneRequest(mRequest.rid);
            Log.d(TAG, "mRequest.rid = " + mRequest.rid + " deleteResult = " + deleteResult);
            updateUIAfterDone();
            ToastUtil.showOperationOk(mActivity, mHandler, true);
            // send done message
//            Message donemsg = mHandler.obtainMessage(REQUEST_DONE);
//            donemsg.getData().putString(REQUEST_ID, mRequest.rid);
//            donemsg.sendToTarget();
        } else {
            String ErrorMsg = msg.getData().getString("ERROR_MSG");
            if (TextUtils.isEmpty(ErrorMsg) == false) {
                ToastUtil.showOperationFailed(mActivity, mHandler, true, ErrorMsg);
            }
        }
    }

    private boolean deleteDoneRequest(String request_id) {
    	//TODO this class unused. param is -1
        return orm.deleteDoneRequest(request_id, -1);
    }

    private void updateUIAfterDone() {
        int excPos = mRequests.indexOf(mRequest);
        Log.d(TAG, "item postion :" + excPos);
        if (excPos != -1) {
        	mRequests.remove(excPos);
            mRequestAdapter.alterRequests(mRequests);
//            setMainActivityCount();
            NotificationManager notificationManager = (NotificationManager) mActivity
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager
                    .cancel(StatusNotification.QIUPU_NITIFY_REQUESTS_ID);
        }

        updateRequestCountUI();

        if (mRequests.size() <= 0) {
            mActivity.finish();
        }
    }

    private void updateRequestCountUI() {
        Log.d(TAG, "updateRequestCountUI() mRequests.size() = " + mRequests.size());
        synchronized (QiupuHelper.requestChangeCountListener) {
            Set<String> set = QiupuHelper.requestChangeCountListener.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                WeakReference<UpdateRequestCountListener> ref = QiupuHelper.requestChangeCountListener.get(key);
                if (ref != null && ref.get() != null) {
                    ref.get().updateRequestCount(mRequests.size(), mRequestTypes);
                }
            }
        }
    }

    
    //TODO
//    private void setMainActivityCount() {
//        ArrayList<Requests> allRequests = new ArrayList<Requests>();
//        allRequests.addAll(mExchangeRequests);
//        allRequests.addAll(mSuggestRequests);
//
//        if (QiupuService.mRequestsService != null) {
//            QiupuService.mRequestsService.updateRequestListener(allRequests);
//        }
//    }

    private HashMap<String, String> organizationContactMap(String col,
            String val) {
        HashMap<String, String> contactInfoMap = new HashMap<String, String>();
        contactInfoMap.put(col, val);
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (!key.equals(col)) {
                if (value.length() > 0) {
                    contactInfoMap.put(key, value);
                }
            }
        }
        return contactInfoMap;
    }

    private void initContactInfomap() {
        Cursor cursor = orm.queryOneUserPhoneEmail(AccountServiceUtils
                .getBorqsAccountID());
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String type = cursor.getString(cursor.getColumnIndex(PhoneEmailColumns.TYPE));
                String info = cursor.getString(cursor.getColumnIndex(PhoneEmailColumns.INFO));
                map.put(type, info);
            } while (cursor.moveToNext());
            cursor.close();
            cursor = null;
        }else {
            Log.d(TAG, "need load myself info from server");
        }
//        QiupuUser myInfo = orm.queryOneUserInfo(AccountServiceUtils
//                .getBorqsAccountID());
//        if (myInfo != null) {
//            if (StringUtil.isValidString(myInfo.contact_phone1)) {
//                map.put(QiupuConfig.TYPE_PHONE1, myInfo.contact_phone1);
//            }
//            if (StringUtil.isValidString(myInfo.contact_phone2)) {
//                map.put(QiupuConfig.TYPE_PHONE2, myInfo.contact_phone2);
//            }
//            if (StringUtil.isValidString(myInfo.contact_phone3)) {
//                map.put(QiupuConfig.TYPE_PHONE3, myInfo.contact_phone3);
//            }
//            if (StringUtil.isValidString(myInfo.contact_email1)) {
//                map.put(QiupuConfig.TYPE_EMAIL1, myInfo.contact_email1);
//            }
//            if (StringUtil.isValidString(myInfo.contact_email2)) {
//                map.put(QiupuConfig.TYPE_EMAIL2, myInfo.contact_email2);
//            }
//            if (StringUtil.isValidString(myInfo.contact_email3)) {
//                map.put(QiupuConfig.TYPE_EMAIL3, myInfo.contact_email3);
//            }
//        } else {
//            Log.d(TAG, "need load myself info from server");
//        }
    }

//    private void distributionRequests(ArrayList<Requests> requests) {
//        mExchangeRequests.clear();
//        mSuggestRequests.clear();
//        for (int i = 0; i < requests.size(); i++) {
//            Requests rq = requests.get(i);
//            if (Requests.REQUEST_TYPE_EXCHANGE_VCARD == rq.type) {
//                mExchangeRequests.add(rq);
//            } else {
////                mSuggestRequests.add(rq);
//            }
//        }
//    }

    public interface UpdateRequestCountListener {
        public void updateRequestCount(int exchangeRequestCount, String type);
    }

}
