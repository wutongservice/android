package com.borqs.qiupu.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import twitter4j.AsyncQiupu;
import twitter4j.QiupuUser;
import twitter4j.Requests;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.internal.http.HttpClientImpl;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.BorqsAccount;
import com.borqs.common.adapter.BpcShakingListAdapter;
import com.borqs.common.listener.ShakeActionListener;
import com.borqs.common.listener.ShakeListActionListener;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.AsyncApiUtils.AsyncApiSendRequestCallBackListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.BpcShakingItemView;
import com.borqs.common.view.RequestContactInfoSimpleView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;

public class BpcShakeExchangeCardFragment extends BasicFragment implements SensorEventListener,
            UsersActionListner, AsyncApiSendRequestCallBackListener {

    private static final String TAG = "Qiupu.BpcShakeExchangeCardFragment";

    private QiupuUser mUser;
    private Vibrator mVibrator;
    private SensorManager mSensorManager;
    private long ticket;
    private boolean initialLocation = true;
    private static final boolean debug = false;
    private Context mActivity;
    private Handler mHandler;
    private QiupuORM orm;
    private AsyncQiupu asyncQiupu;
    private ShakeActionListener mShakeListener;
    private ShakeListActionListener mShakeListListener;
    private TextView nobodyText;
    private boolean from_exchange = false;
    private ListView mListView;
    private View mLbsContainer;

    public BpcShakeExchangeCardFragment() {
        
    }

    public BpcShakeExchangeCardFragment(AsyncQiupu aq, boolean fromExchange) {
        asyncQiupu = aq;
        from_exchange = fromExchange;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;

        QiupuHelper.registerUserListener(getClass().getName(), this);
        getListeners(activity);
    }

    private void getListeners(Activity activity) {

        if(ShakeActionListener.class.isInstance(activity)) {
            mShakeListener = (ShakeActionListener) activity;
            mShakeListener.getShakeFragmentCallBack(this);
        }

        if (ShakeListActionListener.class.isInstance(activity)) {
            mShakeListListener = (ShakeListActionListener) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.exchange_vcard_by_shake, null);
        mListView = (ListView) view.findViewById(R.id.friends_list);
        mLbsContainer = view.findViewById(R.id.lbs_container);

//        if (from_exchange) {
            setShakeAnimation(view);
//        } else {
//            setShakeViewAndAnimation(view);
//        }
        return view;
    }

    private BpcShakingListAdapter mBpcShakingListAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new MainHandler();
        orm = QiupuORM.getInstance(mActivity);

        Log.d(TAG, "onActivityCreated() from_exchange = " + from_exchange);
        mBpcShakingListAdapter = new BpcShakingListAdapter(mActivity, from_exchange);
        mBpcShakingListAdapter.registerUsersActionListner(getClass().getName(), this);

        if (from_exchange) {
            BorqsAccount borqsAccount = AccountServiceUtils.getBorqsAccount();
            if (borqsAccount != null) {
                ticket = borqsAccount.uid;
                mUser = orm.queryOneUserInfo(ticket);
                if (mUser == null) {
                    Log.d(TAG, "===== get user info from server, ticket = " + ticket);
                    getUserInfo(ticket);
                } else {
                    setUI(mUser);
                }
            }
        } else {
            
        }

        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        mVibrator = (Vibrator) mActivity.getSystemService(Service.VIBRATOR_SERVICE);

        mShakeListener.activateLocation();

        if (mShakeListener.checkLocationApi() == false){
            openGPSSettings();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setAlphaAnimation(String title) {
        nobodyText.setVisibility(View.VISIBLE);
        nobodyText.setText(title);
        AlphaAnimation alp = new AlphaAnimation(0.0f,1.0f);
        alp.setDuration(2000);
        nobodyText.setAnimation(alp);
        alp.setAnimationListener(showTextListener);
    }

    private AnimationListener showTextListener = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            AlphaAnimation alp = new AlphaAnimation(1.0f, 0.0f);
            alp.setDuration(2000);
            nobodyText.setAnimation(alp);
            alp.setAnimationListener(goneTextListener);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };

    
    private AnimationListener goneTextListener = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            nobodyText.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };

    private void setShakeAnimation(View view) {
        viewLayout = view.findViewById(R.id.my_vcard_layout);
        nobodyText = (TextView) view.findViewById(R.id.find_nobody);

        setShakeViewAndAnimation(view);
    }

    private void setShakeViewAndAnimation(View view) {
        ImageView left_hand = (ImageView) view.findViewById(R.id.lbs_shake_hand_left);
        ImageView right_hand = (ImageView) view.findViewById(R.id.lbs_shake_hand_right);

        if (from_exchange) {
            // do nothing
        } else {
            LinearLayout shakeView = (LinearLayout) view.findViewById(R.id.lbs_container);
            shakeView.setGravity(Gravity.CENTER);
            view.findViewById(R.id.my_vcard_layout).setVisibility(View.GONE);
            TextView title = (TextView) view.findViewById(R.id.lbs_shake_title);
            TextView subtitle = (TextView) view.findViewById(R.id.lbs_shake_subtitle);
            title.setText(R.string.shaking_to_find_friends);
            subtitle.setVisibility(View.GONE);
        }

        left_hand.setAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.rotate_left));

        Bitmap localBitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.bump_shake_hand);
        Matrix localMatrix = new Matrix();
        localMatrix.setScale(-1.0F, 1.0F);
        Bitmap localBitmap2 = Bitmap.createBitmap(localBitmap1, 0, 0, localBitmap1.getWidth(), localBitmap1.getHeight(), localMatrix, true);
        right_hand.setImageBitmap(localBitmap2);

        right_hand.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.rotate_right));
    }

    private View viewLayout;

    private void setUI(QiupuUser user) {
        if (user == null) {
            Log.d(TAG, "setUI() : error, user is null");
            return;
        }

        View view = viewLayout.findViewById(R.id.id_vcard_rl);
        view.setVisibility(View.VISIBLE);
        view.findViewById(R.id.import_contact).setVisibility(View.GONE);
        TextView name = (TextView) view.findViewById(R.id.id_user_name);
        LinearLayout vcard = (LinearLayout) view.findViewById(R.id.id_vcard);

        ImageView photo = (ImageView) view.findViewById(R.id.id_user_icon);
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

        ImageRun imagerun = new ImageRun(null, image_url, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
        imagerun.noimage = true;
        imagerun.addHostAndPath = true;
        imagerun.setRoundAngle=true;
        imagerun.setImageView(profile_img_ui);
        imagerun.post(null);
    }

    private void setContactWayUI(LinearLayout vcard, String userContact, String contactWay) {
        if (!TextUtils.isEmpty(userContact)) {
            RequestContactInfoSimpleView info = new RequestContactInfoSimpleView(mActivity, contactWay);
            vcard.addView(info);
        }
    }

    private static final int GET_USER_FROM_SERVER_END = 1;
    private static final int GET_LBS_USER = 2;
    private static final int GET_LBS_USER_END = 3;
    private static final int SEND_REQUEST_END = 100;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_USER_FROM_SERVER_END:{
                    boolean succeed = msg.getData().getBoolean("RESULT", false);
                    if (succeed) {
                        setUI(mUser);
                    } else {
                        String errorMsg = msg.getData().getString("ERROR_MSG");
                        if(TextUtils.isEmpty(errorMsg) == false) {
                            mShakeListener.showCustomFragmentToast(errorMsg);
                        }
                    }
                    break;
                }
                case GET_LBS_USER: {
                    boolean succeed = msg.getData().getBoolean("RESULT", false);
                    if (succeed) {
                        getLBSUsers();
                    } else {
                        end();
                        endProgress();
                        mShakeListener.showCustomFragmentToast("failed to fetch location");
                    }
                    break;
                }
                case GET_LBS_USER_END: {
                    endProgress();

                    boolean succeed = msg.getData().getBoolean("RESULT", false);
                    if (succeed) {
                        final ArrayList<QiupuUser> lbsUserList = (ArrayList<QiupuUser>) msg.getData().getSerializable("lbs_user_list");

                        if (lbsUserList != null && lbsUserList.size() > 0) {
                            if (debug) {
                                // follow QQ phone book design
//                                View view ;
//                                DialogUtils.ShowDialogwithView(BpcShakeExchangeCardActivity.this, R.string.shaking_exchange_apply, 
//                                       view, R.string.qiupu_invite, R.string.qiupu_abandon, null, null);
                            } else {
//                                startShakingListActivity(lbsUserList);
//                                mShakeListener.changeFragment(true, lbsUserList);
                                setListUI(lbsUserList);
                                
//                                setAnimationWhenStartActivity();
                            }
                        } else {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "============ lbsUserList = " + lbsUserList);
                                    setAlphaAnimation(mActivity.getResources().getString(R.string.shaking_nothing_toast));
                                }
                            });
//                            Toast.makeText(mActivity, R.string.shaking_nothing_toast, Toast.LENGTH_SHORT);
                        }
                        
                    } else {
                        final String errorMsg = msg.getData().getString("ERROR_MSG");
                        Log.d(TAG, "====== errorMsg = " + errorMsg);
                        if(TextUtils.isEmpty(errorMsg) == false) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    setAlphaAnimation(errorMsg);
                                }
                            });
//                            mShakeListener.showCustomFragmentToast(errorMsg);
//                            Toast.makeText(mActivity, errorMsg, Toast.LENGTH_SHORT);
                        }
                    }
//                    mShakeListener.deactivateLocation();

                    synchronized (mShakingObjLock) {
                        mShakingProcess = false;
                    }

                    break;
                }
                case SEND_REQUEST_END: {
                    try {
                        mprogressDialog.dismiss();
                        mprogressDialog = null;
                    } catch (Exception ne) {}

                    boolean ret = msg.getData().getBoolean("result", false);
                    if (ret) {
                        long uid = msg.getData().getLong("uid");
                        afterSendProfileAccessRequest(uid, true);
                    } else {
                        Log.d(TAG, "exchange vcard failed");
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    private void afterSendProfileAccessRequest(long uid, boolean requested) {
        Log.d(TAG, "afterSendProfileAccessRequest() uid = " + uid);
        for (int j = 0; j < mListView.getChildCount(); j++) {
            View v = mListView.getChildAt(j);
            if (BpcShakingItemView.class.isInstance(v)) {
                BpcShakingItemView shakeView = (BpcShakingItemView) v;
                shakeView.setFromStatus(from_exchange);
                QiupuUser user = shakeView.getUser();
                if (requested) {
                    user.circleId = "5";
                    mUser.profile_privacy = false;
                }
                if (user != null && user.uid == uid) {
                    String requestid = user.pedding_requests;
                    user.pedding_requests = Requests.getrequestTypeIds(requestid);
                    Log.d(TAG, "user.pedding_requests = " + user.pedding_requests);
                    shakeView.setUser(user);
//                    shakeView.refreshUI();
                    break;
                }
            }
        }
    }

    private void showListUI(boolean show) {
        if (show) {
            viewLayout.setVisibility(View.GONE);
            mLbsContainer.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            if (from_exchange) {
                viewLayout.setVisibility(View.VISIBLE);
                mLbsContainer.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
            } else {
                mLbsContainer.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
            }
        }
    }

    private void setListUI(ArrayList<QiupuUser> lbsUserList) {
        showListUI(true);

        mListView.setAdapter(mBpcShakingListAdapter);

        if (lbsUserList != null && lbsUserList.size() > 0) {
            mBpcShakingListAdapter.alterDataList(lbsUserList);
            mListView.setVisibility(View.VISIBLE);
        } else {
            // should not happen
            Log.d(TAG, "error, find nobody, it should not happen");
        }

        mListView.setOnItemClickListener(null);
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
        asyncQiupu.getUserInfo(userid, getSavedTicket(), new TwitterAdapter() {
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
                msg.getData().putBoolean("RESULT", true);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {

                synchronized (mInfoLock) {
                    inInfoProcess = false;
                }
                TwitterExceptionUtils.printException(TAG, "getUserInfo, server exception:", ex, method);
                Log.d(TAG, "fail to load user info=" + ex.getMessage());

                Message msg = mHandler.obtainMessage(GET_USER_FROM_SERVER_END);
                msg.getData().putString("ERROR_MSG", ex.getMessage());
                msg.getData().putBoolean("RESULT", false);
                msg.sendToTarget();
            }
        });
    }

    private boolean isUiLoading(){
        if (inInfoProcess){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() ====== mShakeListener = " + mShakeListener);
        super.onDestroy();
        if (mShakeListener != null) {
            mShakeListener.deactivateLocation();
        }

        HttpClientImpl.setLocation("");
        QiupuHelper.unregisterUserListener(getClass().getName());
        mBpcShakingListAdapter.unregisterUsersActionListner(getClass().getName());
    }

    private Object mShakingObjLock = new Object();
    private boolean mShakingProcess = false;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (disable_sensor) {
            return;
        }
        int sensorType = event.sensor.getType();
        float[] values = event.values;
        if(sensorType == Sensor.TYPE_ACCELEROMETER){
            if((Math.abs(values[0]) > 14 || Math.abs(values[1]) > 14 || Math.abs(values[2]) > 14)){

                synchronized (mShakingObjLock) {
                    if (mShakingProcess == true) {
                        Log.d(TAG, "onSensorChanged() in shaking now");
                        return;
                    }
                }

                synchronized (mShakingObjLock) {
                    mShakingProcess = true;
                }

//                mShakeListener.changeFragment(false, null);
                showListUI(false);

                if(isUiLoading() == false){
//                  begin();
                  beginProgress();
                }

                Log.d(TAG, "onSensorChanged() initialLocation = " + initialLocation);

                if (initialLocation == false) {
                    getLBSUsers();
                } else {
                    mShakeListener.deactivateLocation();
                    mShakeListener.activateLocation();
                    initialLocation = false;
                }
//                getLBSUsers();
//                Log.d(TAG, "onSensorChanged getLBSUsers() ");
                mVibrator.vibrate(500);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        
    }

    public void getLocationSucceed(String locationStr) {
        setActionForShake(locationStr);
    }

//    public void locationUpdated(final Location location, final String address, String locString) {
//        Log.d(TAG, "locationUpdated() =========== initialLocation = " + initialLocation);
//        setActionForShake(address);
//    }

    private void setActionForShake(String locationStr) {
        Log.d(TAG, "setActionForShake() =========== initialLocation = " + initialLocation);
        if (initialLocation) {
            initialLocation = false;
            return;
        } else {
            if (locationStr != null && locationStr.length() > 0) {
                Message msg = mHandler.obtainMessage(GET_LBS_USER);
                msg.getData().putBoolean("RESULT", true);
                msg.sendToTarget();
            } else {
                Message msg = mHandler.obtainMessage(GET_LBS_USER);
                msg.getData().putBoolean("RESULT", false);
                msg.sendToTarget();
            }
        }
    }

    private void openGPSSettings() {
        LocationManager lm = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_status = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_status = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d(TAG, "######## openGPSSettings() ----> gps_status = " + gps_status + " network_status = " + network_status);
        if (gps_status || network_status) {
//            LocationRequest.instance().activate(this);
        } else {
            DialogUtils.showConfirmDialog(mActivity, R.string.location_title,
                    R.string.location_message, R.string.label_ok, R.string.label_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        Intent intent = new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
                        String className = null; 
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                            className = "com.android.settings.LocationSettings";
                        } else {
                            className = "com.android.settings.Settings$LocationSettingsActivity";
                        }
                        intent.setClassName("com.android.settings", className);
                        startActivity(intent);
                    } catch(ActivityNotFoundException e) {
                        Log.d(TAG, "it's not standard android setting intent or package name");
                    }
                }
            });
        }
    }

    private String getSavedTicket() {
        return AccountServiceUtils.getSessionID();
    }

    private void getLBSUsers() {
        if (!AccountServiceUtils.isAccountReady()) {
            return;
        }

        synchronized (mInfoLock) {
            if (inInfoProcess == true) {
                Log.d(TAG, "============getLBSUsers() in loading info data");
                return;
            }
        }

//        if(isUiLoading() == false){
//            begin();
//            beginProgress();
//        }

        synchronized (mInfoLock) {
            inInfoProcess = true;
        }

        asyncQiupu.getLBSUsersInfo(ticket, getSavedTicket(),new TwitterAdapter() {
            public void getLBSUsersInfo(ArrayList<QiupuUser> lbsUsers) {
                Log.d(TAG, "====== finish getUserInfo=" + lbsUsers);

                if (lbsUsers != null) {
                    Log.d(TAG, "===== lbsUsers.size() = " + lbsUsers.size());
                }

//                mLBSUserList.clear();
//                mLBSUserList.addAll(lbsUsers);

                synchronized (mInfoLock) {
                    inInfoProcess = false;
                }

                Message msg = mHandler.obtainMessage(GET_LBS_USER_END);
                msg.getData().putSerializable("lbs_user_list", lbsUsers);
                msg.getData().putBoolean("RESULT", true);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {

                synchronized (mInfoLock) {
                    inInfoProcess = false;
                }

                TwitterExceptionUtils.printException(TAG, "getLBSUsersInfo, server exception:", ex, method);
                Log.d(TAG, "fail to load user info=" + ex.getMessage());

                Message msg = mHandler.obtainMessage(GET_LBS_USER_END);
                msg.getData().putString("ERROR_MSG", ex.getMessage());
                msg.getData().putBoolean("RESULT", false);
                msg.sendToTarget();
            }
        });
    }

    private ProgressDialog mprogressDialog;
    private void showProcessDialog(int resId, int resTitle, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable) {
        mprogressDialog = DialogUtils.createProgressDialogWithTitle(mActivity,
                resId, resTitle, CanceledOnTouchOutside, Indeterminate, cancelable);
        mprogressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                synchronized (mShakingObjLock) {
                    mShakingProcess = false;
                }
            }
        });
        mprogressDialog.show();
    }

    private void beginProgress() {
        int res_title = 0;
        if (from_exchange) {
            res_title = R.string.home_exchange;
        } else {
            res_title = R.string.find_friends_title;
        }
        showProcessDialog(R.string.shaking_progress_msg, res_title, false, true, true);
    }

    private void endProgress() {
        try {
            mprogressDialog.dismiss();
            mprogressDialog = null;
        } catch(Exception e){}
    }

    @Override
    public void updateItemUI(QiupuUser user) {
        Log.d(TAG, "updateItemUI() ###### user = " + user.toString());
        for (int j = 0; j < mListView.getChildCount(); j++) {
            View v = mListView.getChildAt(j);
            if (BpcShakingItemView.class.isInstance(v)) {
                BpcShakingItemView shakeView = (BpcShakingItemView) v;
                QiupuUser quser = shakeView.getUser();
                if (quser != null && quser.uid == user.uid) {
                    String requestid = quser.pedding_requests;
                    quser.pedding_requests = Requests.getrequestTypeIds(requestid);
                    quser.circleId = "5";
                    shakeView.setUser(quser);
                    break;
                }
            }
        }
    }

    @Override
    public void addFriends(QiupuUser user) {
        IntentUtil.startCircleSelectIntent(mActivity, user.uid, null);
    }

    @Override
    public void refuseUser(long uid) {
        
    }

    @Override
    public void deleteUser(QiupuUser user) {
        
    }

    @Override
    public void sendRequest(QiupuUser user) {
        mShakeListListener.sendRequestInFragment(user);
    }

    @Override
    public void sendRequestCallBackBegin() {
        showProcessDialog(R.string.request_process_title, false, true, true);
    }

    @Override
    public void sendRequestCallBackEnd(boolean result, long uid) {
        Log.d(TAG, "sendRequestCallBackEnd() uid = " + uid);
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

    public void updateShakeListItemUI(boolean result, long uid) {
        Log.d(TAG, "updateShakeListItemUI() uid = " + uid);
        Message message = mHandler.obtainMessage(SEND_REQUEST_END);
        message.getData().putBoolean("result", result);
        message.getData().putLong("uid", uid);
        message.sendToTarget();
    }

    public void updateItemUI(long uid, boolean result) {
        Log.d(TAG, "updateItemUI() uid = " + uid + " result = " + result);
        if (result) {
            for (int j = 0; j < mListView.getChildCount(); j++) {
                View v = mListView.getChildAt(j);
                if (BpcShakingItemView.class.isInstance(v)) {
                    BpcShakingItemView shakeView = (BpcShakingItemView) v;
                    QiupuUser user = shakeView.getUser();
                    if (user != null && user.uid == uid) {
                        String requestid = user.pedding_requests;
                        user.pedding_requests = Requests.getrequestTypeIds(requestid);
                        user.circleId = "5";
                        shakeView.setUser(user);
//                        shakeView.refreshUI();
                        break;
                    }
                }
            }
        } else {
            //TODO: show toast to report error info.
        }
    }

    public void updateActivityUI(final QiupuUser user) {
        Log.d(TAG, "updateActivityUI() user = " + user.toString());
        synchronized (QiupuHelper.userlisteners) {
            Set<String> set = QiupuHelper.userlisteners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                UsersActionListner listener = QiupuHelper.userlisteners.get(key).get();
                if (listener != null && !BpcShakingListFragment.class.isInstance(listener)) {
                    listener.updateItemUI(user);
                }
            }
        }
    }

    private boolean disable_sensor = false;
    public void disableSensor(boolean disable) {
        disable_sensor = disable;
    }
}
