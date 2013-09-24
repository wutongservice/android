package com.borqs.wutong;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.PeopleListFragment;
import com.borqs.qiupu.ui.bpc.CircleFragmentActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;
import twitter4j.*;
import twitter4j.conf.ConfigurationContext;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: yangfeng
 * Date: 13-1-16
 * Time: 上午11:34
 * To change this template use File | Settings | File Templates.
 */
public class HomePickerFragment extends PeopleListFragment {
    private static final String TAG = "AllCirclesListFragment";
    private Activity mActivity;
    private QiupuORM orm ;
    private Handler mHandler;
    protected AsyncQiupu asyncQiupu;
    private static final String RESULT = "result";
    private ProgressDialog mprogressDialog;
    private Cursor mOrgazitaionCircles;

    private HomePickerAdapter mCircleMainListAdapter;

    // ugly code patch for ugly api
    private static HashMap<Long, Long> circleToCompany;
    static {
        circleToCompany = new HashMap<Long, Long>();
        circleToCompany.put(new Long(1L), new Long(15000000016L));
    }

    private static class HomePickerAdapter extends BaseAdapter {
        private static final String TAG = "HomePickerAdapter";
        private Cursor mOrgizationCircles;
        private Context mContext;

        public HomePickerAdapter(Context context){
            mContext = context;
        }

        public void alterCircles(Cursor publicCircles){
            if(mOrgizationCircles != null) {
                mOrgizationCircles.close();
            }
            mOrgizationCircles = publicCircles;

            notifyDataSetChanged();
        }

        public int getCount() {
            final int count = null == mOrgizationCircles ? 0 : mOrgizationCircles.getCount();
            return count;
        }

        public UserCircle getItem(int position) {
            if(mOrgizationCircles != null && mOrgizationCircles.moveToPosition(position)){
                Log.d(TAG, "get Item position="+position + " map to="+ position);
                UserCircle circle = QiupuORM.createPublicCircleListInfo(mContext, mOrgizationCircles);
                return circle;
            }else{
                return null;
            }
        }

        public long getItemId(int position) {
            UserCircle circle =  getItem(position);
            return circle !=null ? circle.type : -1;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            UserCircle circle = getItem(position);
            if (circle != null) {
                if (convertView == null
                        || false == (convertView instanceof HomePickerItem)) {
                    HomePickerItem rView = new HomePickerItem(mContext, circle);
                    holder = new ViewHolder();

                    rView.setTag(holder);
                    holder.view = rView;

                    convertView = rView;

                } else {
                    holder = (ViewHolder) convertView.getTag();
                    holder.view.setCircle(circle);
                }

                return convertView;
            } else {
                return null;
            }

        }

        static class ViewHolder
        {
            public HomePickerItem view;
        }

        public void clearCursor() {
            QiupuORM.closeCursor(mOrgizationCircles);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach ");
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate " );
        super.onCreate(savedInstanceState);
        orm = QiupuORM.getInstance(mActivity);
        asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
        mHandler = new MainHandler();
        showEditText = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView " );
        View convertView = super.onCreateView(inflater, container, savedInstanceState);

//		mListView = (ListView) inflater.inflate(R.layout.default_listview, container, false);
        mListView.setDivider(null);
        if(mActivity != null && !(mActivity instanceof CircleFragmentActivity)) {
            mListView.setOnItemClickListener(CircleListClickListener);
        }

        final Context activity = getActivity();
        mCircleMainListAdapter = new HomePickerAdapter(activity);
        mListView.setAdapter(mCircleMainListAdapter);

        HomePickerItem foot = new HomePickerItem(activity);
        foot.setBackgroundResource(R.drawable.list_selector_background);
        foot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QiupuORM.addSetting(getActivity(), QiupuORM.HOME_ACTIVITY_ID, "0");
                QiupuApplication.mTopOrganizationId = QiupuApplication.VIEW_MODE_PERSONAL;
                IntentUtil.startStreamListIntent(activity, true);
                mActivity.finish();
                HomePickerActivity.onPickerSelected(null);
            }
        });
        mListView.addFooterView(foot);

        return convertView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                refreshCircleList();
                if ((mOrgazitaionCircles != null && mOrgazitaionCircles.getCount() <= 0)) {
                    mHandler.obtainMessage(GET_CIRCLE).sendToTarget();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCircleMainListAdapter.clearCursor();
    }

    private void refreshCircleList() {
        if(mOrgazitaionCircles != null) {
            mOrgazitaionCircles.close();
        }
        mOrgazitaionCircles = orm.queryOrganizationWithImage();
        mCircleMainListAdapter.alterCircles(mOrgazitaionCircles);
    }

    private final int GET_CIRCLE = 1;
    private final int GET_CIRCLE_END = 2;
    private final int CIRCLE_DELETE_END = 3;
    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_CIRCLE: {
                    syncCircleInfo();
                    break;
                }
                case GET_CIRCLE_END:{
                    end();
                    if(msg.getData().getBoolean(RESULT)){
                        refreshCircleList();
                    }else{
                        Log.d(TAG, "sync circle info error");
                    }
                    break;
                }case CIRCLE_DELETE_END: {
                    try {
                        mprogressDialog.dismiss();
                        mprogressDialog = null;
                    } catch (Exception ne) {}
                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret)
                    {
                        refreshCircleList();
                        QiupuHelper.updateActivityUI(null);
                    } else {
                        ToastUtil.showOperationFailed(mActivity, mHandler, true);
                    }
                    break;
                }

            }
        }
    }

    AdapterView.OnItemClickListener CircleListClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if(HomePickerItem.class.isInstance(view)) {
                HomePickerItem item = (HomePickerItem) view;
                UserCircle circle = item.getCircle();
                if(circle != null) {
                    if(UserCircle.CIRLCE_TYPE_PUBLIC == circle.type) {
                        Log.d(TAG, "click public circles " + circle.circleid);
                        QiupuORM.addSetting(getActivity(), QiupuORM.HOME_ACTIVITY_ID, String.valueOf(circle.circleid));
                        QiupuApplication.mTopOrganizationId = circle;

                        IntentUtil.gotoOrganisationHome(mActivity,
                                CircleUtils.getLocalCircleName(mActivity, circle.circleid, circle.name),
                                circle.circleid);
                        IntentUtil.loadCircleDirectoryFromServer(mActivity, circle.circleid);
                        mActivity.finish();
                        HomePickerActivity.onPickerSelected(circle);
                    }
                }else {
                    Log.d(TAG, "get circle is null.");
                }
            }
        }
    };

    public void loadRefresh() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                mHandler.obtainMessage(GET_CIRCLE).sendToTarget();
            }
        }, 500);
    }

    boolean inloadingCircle = false;
    Object circleLock = new Object();

    private void syncCircleInfo() {
        synchronized (circleLock) {
            if (inloadingCircle == true) {
                Log.d(TAG, "in doing syncCircleInfo data");
                return;
            }
        }

        synchronized (circleLock) {
            inloadingCircle = true;
        }

        begin();
        asyncQiupu.getUserCircle(AccountServiceUtils.getSessionID(),
                AccountServiceUtils.getBorqsAccountID(), "", false,
                new TwitterAdapter() {
                    public void getUserCircle(ArrayList<UserCircle> userCircles) {
                        Log.d(TAG, "finish getUserCircle= " + userCircles.size());

                        if (userCircles.size() > 0) {
                            orm.removeAllCirclesWithOutNativeCircles();
                            orm.insertCircleList(userCircles, AccountServiceUtils.getBorqsAccountID());
                        }
                        dogetUserCircleCallBack(true, userCircles);
                        synchronized (circleLock) {
                            inloadingCircle = false;
                        }
                    }

                    public void onException(TwitterException ex,
                                            TwitterMethod method) {
                        TwitterExceptionUtils.printException(TAG,
                                "getUserCircle, server exception:", ex, method);
                        synchronized (circleLock) {
                            inloadingCircle = false;
                        }
                        dogetUserCircleCallBack(false, null);
                    }
                });
    }

    private void dogetUserCircleCallBack(boolean suc, ArrayList<UserCircle> userCircles) {
        Message msg = mHandler.obtainMessage(GET_CIRCLE_END);
        msg.getData().putBoolean(RESULT, suc);
        msg.sendToTarget();
    }

    public boolean getLoadStatus(){
        return inloadingCircle;
    }

    @Override
    protected View initHeadView() {
        View headView = LayoutInflater.from(mActivity).inflate(R.layout.wutong_home_picker_header, null, false);
        return headView;
//        TextView header = new TextView(getActivity());
//        header.setText(R.string.wutong_home_picker_header);
//
//        return header;
    }
}
