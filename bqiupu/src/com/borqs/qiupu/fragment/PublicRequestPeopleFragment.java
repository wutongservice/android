package com.borqs.qiupu.fragment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

import twitter4j.AsyncQiupu;
import twitter4j.PublicCircleRequestUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.UserImage;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.publicCircleRequestpeopleAdapter;
import com.borqs.common.adapter.publicCircleRequestpeopleAdapter.MoreItemCheckListener;
import com.borqs.common.listener.CheckBoxClickActionListener;
import com.borqs.common.listener.publicCirclePeopleActionListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.PublicCircleRequestPeopleItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public final class PublicRequestPeopleFragment extends PeopleSearchableFragment implements CheckBoxClickActionListener
                                              , publicCirclePeopleActionListener, MoreItemCheckListener, OnScrollListener{
    private static final String TAG = "PublicRequestPeopleFragment";

    private Activity mActivity;
    private QiupuORM orm;
    private Handler mHandler;
    private boolean isLoading;
    private AsyncQiupu asyncQiupu;
    private ListView mListView;
    private View mBottomView;
    private ProgressDialog mprogressDialog;
    
    private UserCircle mCircle;
    private int mPage = 0;
    private int mCount = 100;
    private int mSearchpage = 0;
    private int mStatus;
    private publicCircleRequestpeopleAdapter mAdapter;
    private RequestPeopleCallBackListener mCallBackListener;
    private ArrayList<PublicCircleRequestUser> mPeopleList = new ArrayList<PublicCircleRequestUser>();
    private ArrayList<PublicCircleRequestUser> mSearchPeopleList = new ArrayList<PublicCircleRequestUser>();
    
    private HashSet<Long> mSelectedUser = new HashSet<Long>();
    
    private boolean isUserShowMore; 

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        super.onAttach(activity);
        mActivity = activity;

        try {
            mCallBackListener = (RequestPeopleCallBackListener) activity;
            mCallBackListener.getPublicRequestPeopleFragment(this);
        } catch (ClassCastException e) {
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        orm = QiupuORM.getInstance(mActivity);
        mHandler = new MainHandler();
        asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null, null);
        mCircle = mCallBackListener.getCircle();
        if(mCircle == null) {
            Log.d(TAG, "onCreate: mCircle is null ");
            mCircle = new UserCircle();
        }
        mStatus = mCallBackListener.getStatus();
        mAdapter = new publicCircleRequestpeopleAdapter(mActivity, mStatus, mCircle, this);
        mAdapter.registerCheckClickActionListener(getClass().getName(), this);
        mAdapter.registerActionListener(getClass().getName(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        mConvertView = inflater.inflate(R.layout.public_circle_people_view,
                container, false);
        mBottomView = mConvertView.findViewById(R.id.bottom_action);
        mListView = (ListView) mConvertView.findViewById(R.id.default_listview);
        mListView.addHeaderView(initHeadView());
        mListView.setOnItemClickListener(friendItemClickListener);
        mListView.setOnScrollListener(this);
        mListView.setAdapter(mAdapter);
        return mConvertView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        generateIntentData();
        Message msg = mHandler.obtainMessage(LOAD_REQUEST_PEOPLE);
        msg.getData().putInt("page", mPage);
        msg.getData().putInt("count", mCount);
        msg.sendToTarget();
    }

    private void generateIntentData() {
		if(mStatus == PublicCircleRequestUser.STATUS_IN_CIRCLE) {
			generateComingArray(mCircle.inMembersImageList);
		}else if(mStatus == PublicCircleRequestUser.STATUS_APPLY) {
			generateComingArray(mCircle.applyedMembersList);
			
		}else if(mStatus == PublicCircleRequestUser.STATUS_INVITE) {
			generateComingArray(mCircle.invitedMembersList);
		}
	}
    
    private void generateComingArray(ArrayList<UserImage> list) {
    	mPeopleList.clear();
    	if(list == null) {
    		Log.d(TAG, "coming list is null");
    		return;
    	}
    	for(int i=0; i<list.size(); i++) {
    		UserImage userimage = list.get(i);
    		PublicCircleRequestUser tmpUser = new PublicCircleRequestUser();
    		tmpUser.uid = userimage.user_id;
    		tmpUser.nick_name = userimage.userName;
    		tmpUser.profile_image_url = userimage.image_url;
    		mPeopleList.add(tmpUser);
    	}
    	mAdapter.alterDataList(mPeopleList);
    }

	public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPeopleList.clear();
        mSearchPeopleList.clear();
        mAdapter.unRegisterCheckClickActionListener(getClass().getName());
        mAdapter.unRegisterActionListener(getClass().getName());
    }

    private static final int LOAD_REQUEST_PEOPLE = 101;
    private static final int LOAD_REQUEST_PEOPLE_END = 102;
    private static final int APPROVE_PEOPLE_END = 103;
    private static final int REMOVE_PEOPLE_END = 104;
    private static final int GRANT_PEOPLE_END = 105;
    private static final int REFHRE_INLINE_SEARCH_LIST = 106;
    private static final int LOAD_SEARCH_END = 107;
    private static final int SEARCH_CIRCLE_PEOPLE = 108;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case LOAD_REQUEST_PEOPLE: {
                    syncRequestPeople(mCircle.circleid, mStatus,
                            msg.getData().getInt("page"),
                            msg.getData().getInt("count"));
                break;
            }
            case LOAD_REQUEST_PEOPLE_END: {
                end();
                setMoreItem(false);
                if(msg.getData().getBoolean(BasicActivity.RESULT)) {
                    mAdapter.alterDataList(mPeopleList);
                }else {
                    ToastUtil.showOperationFailed(mActivity, mHandler, true);
                }
                break;
            }
            case APPROVE_PEOPLE_END: {
            	dismissProcessDialog();
                if(msg.getData().getBoolean(BasicActivity.RESULT)) {
                    mAdapter.alterDataList(mPeopleList);
                } else {
                    Log.d(TAG, "approve member failed ");
                }
                break;
            }
            case REMOVE_PEOPLE_END: {
            	dismissProcessDialog();
                if(msg.getData().getBoolean(BasicActivity.RESULT)) {
                    mAdapter.alterDataList(mPeopleList);
                } else {
                    Log.d(TAG, "remove member failed ");
                }
                break;
            }
            case GRANT_PEOPLE_END: {
            	dismissProcessDialog();
                if(msg.getData().getBoolean(BasicActivity.RESULT)) {
                    mAdapter.alterDataList(mPeopleList);
                } else {
                    Log.d(TAG, "grant member failed ");
                }
                break;
            }
            case REFHRE_INLINE_SEARCH_LIST :{
            	mAdapter.alterDataList(mSearchPeopleList);
            	break;
            }
            case LOAD_SEARCH_END :{
            	end();
            	if(msg.getData().getBoolean(BasicActivity.RESULT)) {
                    showSearchFromServerButton(false, mSearchKey, null);
                    if(isNeedRefreshUi) {
                        mAdapter.alterDataList(mSearchPeopleList);
                    }
                    if(mSearchpage == 0 && mSearchPeopleList.size() <= 0) {
                        ToastUtil.showShortToast(mActivity, mHandler, R.string.search_people_result_null);
                    }
                }else {
                    ToastUtil.showOperationFailed(mActivity, mHandler, true);
                }
            	break;
            }
            case SEARCH_CIRCLE_PEOPLE: {
            	mSearchpage = 0;
            	gotoSearchCirclePeople(mSearchKey);
            	break;
            }
            }
        }
    }

    AdapterView.OnItemClickListener friendItemClickListener = new FriendsItemClickListener();

    private class FriendsItemClickListener implements
            AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            if (PublicCircleRequestPeopleItemView.class.isInstance(view)) {
                PublicCircleRequestPeopleItemView fv = (PublicCircleRequestPeopleItemView) view;
                PublicCircleRequestUser tmpUser = fv.getUser();
                if (tmpUser.uid > 0) {
                    IntentUtil.startUserDetailIntent(fv.getContext(),
                            tmpUser.uid, tmpUser.nick_name, tmpUser.circleName);
                }else {
                    Log.d(TAG, "the user id < 0, is not system user");
                }
            }
        }
    }

    public boolean getLoadStatus() {
        return isLoading;
    }

    boolean inLoadingPeople = false;
    Object mLockLoadPeople = new Object();
    private void syncRequestPeople(final long circleid, final int status,
            final int page, final int count) {
        Log.d(TAG, "status :" + status);
        synchronized (mLockLoadPeople) {
            if (inLoadingPeople == true) {
                ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
                callFailLoadUserMethod();
                return;
            }
        }
        synchronized (mLockLoadPeople) {
            inLoadingPeople = true;
        }
        
        begin();
        setMoreItem(true);
        asyncQiupu.getRequestPeople(AccountServiceUtils.getSessionID(),
                circleid, status, page, count, new TwitterAdapter() {
            public void getRequestPeople(ArrayList<PublicCircleRequestUser> arraylist) {
                Log.d(TAG, "finish syncRequestPeople=" + arraylist.size());
                
                if (page == 0) {
                    mPeopleList.clear();
                }
                mPeopleList.addAll(arraylist);
                
                getUserInfoEndCallBack(arraylist);
                
                synchronized (mLockLoadPeople) {
                    inLoadingPeople = false;
                }
                
                Message msg = mHandler.obtainMessage(LOAD_REQUEST_PEOPLE_END);
                msg.getData().putBoolean(BasicActivity.RESULT, true);
                msg.sendToTarget();
            }
            
            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockLoadPeople) {
                    inLoadingPeople = false;
                }
                callFailLoadUserMethod();
                Message msg = mHandler.obtainMessage(LOAD_REQUEST_PEOPLE_END);
                msg.getData().putString(BasicActivity.ERROR_MSG,
                        ex.getMessage());
                msg.getData().putBoolean(BasicActivity.RESULT, false);
                msg.sendToTarget();
            }
        });
    }
    
    boolean inApprovePeople = false;
    Object mLockApprovePeople = new Object();
    private void approvepublicCirclePeople(final long circleid, final String userids) {
    	if (inApprovePeople == true) {
            Toast.makeText(mActivity, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLockApprovePeople) {
        	inApprovePeople = true;
        }
        showProcessDialog(R.string.set_user_process_title, false, true, true);
        
        asyncQiupu.approvepublicCirclePeople(AccountServiceUtils.getSessionID(), circleid, userids, new TwitterAdapter() {
            public void approvepublicCirclePeople(ArrayList<Long> ids) {
                Log.d(TAG, "finish approvePeople=" + ids.size());
                orm.updateLocalUserCircleInfo(ids, mCircle.circleid, mCircle.name);
                reconstructedList(ids, mPeopleList);
                Message msg = mHandler.obtainMessage(APPROVE_PEOPLE_END);
                msg.getData().putBoolean(BasicActivity.RESULT, true);
                msg.sendToTarget();
                synchronized (mLockApprovePeople) {
                	inApprovePeople = false;
                }
                
            }
            
            public void onException(TwitterException ex, TwitterMethod method) {
                Message msg = mHandler.obtainMessage(APPROVE_PEOPLE_END);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(BasicActivity.RESULT, false);
                msg.sendToTarget();
                synchronized (mLockApprovePeople) {
                	inApprovePeople = false;
                }
            }
        });
    }
    
    boolean inIgnorePeople = false;
    Object mLockIgnorePeople = new Object();
    
    private void ignorepublicCirclePeople(final long circleid, final String userids) {
    	if (inIgnorePeople == true) {
            Toast.makeText(mActivity, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLockIgnorePeople) {
        	inIgnorePeople = true;
        }
        showProcessDialog(R.string.set_user_process_title, false, true, true);
        
        asyncQiupu.ignorepublicCirclePeople(AccountServiceUtils.getSessionID(), circleid, userids, new TwitterAdapter() {
            public void ignorepublicCirclePeople(ArrayList<Long> ids) {
                Log.d(TAG, "finish approvePeople=" + ids.size());
//                orm.updateLocalUserCircleInfo(ids, mCircle.circleid, mCircle.name);
                reconstructedList(ids, mPeopleList);
                Message msg = mHandler.obtainMessage(APPROVE_PEOPLE_END);
                msg.getData().putBoolean(BasicActivity.RESULT, true);
                msg.sendToTarget();
                synchronized (mLockIgnorePeople) {
                	inIgnorePeople = false;
                }
            }
            
            public void onException(TwitterException ex, TwitterMethod method) {
                Message msg = mHandler.obtainMessage(APPROVE_PEOPLE_END);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(BasicActivity.RESULT, false);
                msg.sendToTarget();
                synchronized (mLockIgnorePeople) {
                	inIgnorePeople = false;
                }
            }
        });
    }
    
    boolean inDeletePeople;
    Object mLockDeletePeople = new Object();
    private void deletePublicCirclePeople(final long circleid, final String userids) {
        if (inDeletePeople == true) {
            Toast.makeText(mActivity, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLockDeletePeople) {
            inDeletePeople = true;
        }
        showProcessDialog(R.string.set_user_process_title, false, true, true);
        
        asyncQiupu.deletePublicCirclePeople(AccountServiceUtils.getSessionID(), circleid, userids, null, new TwitterAdapter() {
            public void deletePublicCirclePeople(boolean result) {
                Log.d(TAG, "finish deletePublicCirclePeople=" + result);
                if(result) {
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
                            CircleUtils.circleUpdateCallBack(userids, String.valueOf(circleid), false, orm, mActivity);
                            reconstructedList(CircleUtils.getCirlceOrUserIds(userids), mPeopleList);
//                        }
//                    });
                }
                
                Message msg = mHandler.obtainMessage(REMOVE_PEOPLE_END);
                msg.getData().putBoolean(BasicActivity.RESULT, true);
                msg.sendToTarget();
                synchronized (mLockDeletePeople) {
                    inDeletePeople = false;
                }
            }
            
            public void onException(TwitterException ex, TwitterMethod method) {
                Message msg = mHandler.obtainMessage(REMOVE_PEOPLE_END);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(BasicActivity.RESULT, false);
                msg.sendToTarget();
                synchronized (mLockDeletePeople) {
                    inDeletePeople = false;
                }
            }
        });
    }
    
    boolean inGrantPeople;
    Object mLockGrantPeople = new Object();
    private void grantpublicCirclePeople(final long circleid, final String adminIds, final String memberIds) {
        if (inGrantPeople == true) {
            Toast.makeText(mActivity, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLockGrantPeople) {
            inGrantPeople = true;
        }
        showProcessDialog(R.string.set_user_process_title, false, true, true);
        
        asyncQiupu.grantPublicCirclePeople(AccountServiceUtils.getSessionID(), circleid, adminIds, memberIds, new TwitterAdapter() {
            public void grantPublicCirclePeople(boolean result) {
                Log.d(TAG, "finish grantPublicCirclePeople=" + result);
                if(result) {
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
                            grantPublicCirclePeopleCallBack(adminIds, memberIds);
//                        }
//                    });
                }
                
                Message msg = mHandler.obtainMessage(GRANT_PEOPLE_END);
                msg.getData().putBoolean(BasicActivity.RESULT, true);
                msg.sendToTarget();
                synchronized (mLockGrantPeople) {
                    inGrantPeople = false;
                }
            }
            
            public void onException(TwitterException ex, TwitterMethod method) {
                Message msg = mHandler.obtainMessage(GRANT_PEOPLE_END);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(BasicActivity.RESULT, false);
                msg.sendToTarget();
                synchronized (mLockGrantPeople) {
                    inGrantPeople = false;
                }
            }
        });
    }
    
    private void reconstructedList(ArrayList<Long> ids, ArrayList<PublicCircleRequestUser> userList) {
        for(int i=0; i<ids.size(); i++) {
            PublicCircleRequestUser tmpUser = new PublicCircleRequestUser();
            tmpUser.uid = ids.get(i);
            int pos = userList.indexOf(tmpUser);
            if (pos != -1) {
                userList.remove(pos);
                break;
            }
        }
    }
    
    public interface RequestPeopleCallBackListener {
        public void getPublicRequestPeopleFragment(
                PublicRequestPeopleFragment fragment);

        public UserCircle getCircle();
        public int getStatus();
        
        public void changeTitleSelectUI(int size);
    }

    public void loadRefresh() {
        Message msg = mHandler.obtainMessage(LOAD_REQUEST_PEOPLE);
        msg.getData().putInt("page", 0);
        msg.getData().putInt("count", mCount);
        msg.sendToTarget();
    }

    @Override
    public void changeItemSelect(long itemId, String itemLabel, boolean isSelect, boolean isuser) {
        changeSelect(itemId, isSelect);
    }
    
    private void changeSelect(long itemId, boolean isSelect) {
        if (isSelect) {
            if(!mSelectedUser.contains(itemId)) {
                mSelectedUser.add(itemId);
            }
        } else {
            if(mSelectedUser.contains(itemId)) {
                mSelectedUser.remove(itemId);
            }
        }
        mBottomView.setVisibility(mSelectedUser.size() > 0 ? View.VISIBLE : View.GONE);            
        
        mCallBackListener.changeTitleSelectUI(mSelectedUser.size());
    }
    
    public void doneSelect() {
        mBottomView.setVisibility(View.GONE);
        mSelectedUser.clear();
        mAdapter.clearSelect();
    }
    
    private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable) {
        mprogressDialog = DialogUtils.createProgressDialog(mActivity,
                resId, CanceledOnTouchOutside, Indeterminate, cancelable);
        mprogressDialog.show();
    }
    
    private void dismissProcessDialog() {
    	try {
            mprogressDialog.dismiss();
            mprogressDialog = null;
        } catch (Exception e) { }
    }
    
    private void grantPublicCirclePeopleCallBack(String adminIds, String memberIds) {
        if(adminIds != null && adminIds.length() > 0) {
            String[] aids = adminIds.split(",");
            for(int i=0; i<aids.length; i++) {
                PublicCircleRequestUser tmpUser = new PublicCircleRequestUser();
                tmpUser.uid = Long.parseLong(aids[i]);
                int pos = mPeopleList.indexOf(tmpUser);
                if (pos != -1) {
                    PublicCircleRequestUser user = mPeopleList.get(pos);
                    user.role_in_group = PublicCircleRequestUser.ROLE_TYPE_MANAGER;
                }
                tmpUser = null;
            }
        }
        
        if(memberIds != null && memberIds.length() > 0) {
            String[] mids = memberIds.split(",");
            for(int i=0; i<mids.length; i++) {
                PublicCircleRequestUser tmpUser = new PublicCircleRequestUser();
                tmpUser.uid = Long.parseLong(mids[i]);
                int pos = mPeopleList.indexOf(tmpUser);
                if (pos != -1) {
                    PublicCircleRequestUser user = mPeopleList.get(pos);
                    user.role_in_group = PublicCircleRequestUser.ROLE_TYPE_MEMEBER;
                }
                tmpUser = null;
            }
        }
    }

    @Override
    public void deleteMember(final String userIds) {
        DialogUtils.showConfirmDialog(mActivity, R.string.delete_user_from_circle_title, R.string.public_circle_remove_member_msg,
                R.string.label_ok, R.string.label_cancel,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    deletePublicCirclePeople(mCircle.circleid, userIds);
                }
        }, null);
    }

    @Override
    public void approveMember(String userIds) {
        approvepublicCirclePeople(mCircle.circleid, userIds);
    }

    @Override
    public void grantMember(String userIds, int role, String display_name) {
        String dialogMessage = "";
        String tmpadminIds = "";
        String tmpmemberIds = "";
        if(PublicCircleRequestUser.ROLE_TYPE_MANAGER == role) {
            dialogMessage = String.format(getString(R.string.public_circle_grant_member_message), display_name);
            tmpmemberIds = userIds;
        }else if(PublicCircleRequestUser.ROLE_TYPE_MEMEBER == role) {
            dialogMessage = String.format(getString(R.string.public_circle_grant_manager_message), display_name);
            tmpadminIds = userIds;
        }
        final String adminIds = tmpadminIds;
        final String memberIds = tmpmemberIds;
        DialogUtils.showConfirmDialog(mActivity, getString(R.string.public_circle_grant_title), dialogMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                grantpublicCirclePeople(mCircle.circleid, adminIds, memberIds);
            }
        });
    }

    @Override
    public void ignoreMembar(String userIds) {
        ignorepublicCirclePeople(mCircle.circleid, userIds);
    }
    
    private int followerItem = 0;

    public void onScroll(AbsListView v, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        followerItem = firstVisibleItem + visibleItemCount;
    }

    public void onScrollStateChanged(AbsListView v, int state) {
        boolean forloadmore = (state == OnScrollListener.SCROLL_STATE_IDLE);
        loadOlderUsers(forloadmore, false);
    }

    protected void loadOlderUsers(boolean formore, boolean forceget) {
        int followercount = mPeopleList.size();
        Log.i(TAG, "followercount:" + followercount);
        if ((followerItem == followercount + 2 && formore) || forceget) {
        	getOldPeopleData();
        }
    }

    private void getOldPeopleData() {
    	if(mIsSearchMode) {
    		mSearchpage += 1;
    	}else {
    		mPage += 1;
    		Log.i(TAG, "Page:" + mPage);
    	}
        try {
            failCallSyncUserMethod = PublicRequestPeopleFragment.class
                    .getDeclaredMethod("SubUserPage", (Class[]) null);
        } catch (Exception e) {
        }

        if(mIsSearchMode) {
        	gotoSearchCirclePeople(mSearchKey);
        }else {
        	syncRequestPeople(mCircle.circleid, mStatus, mPage, mCount);
        }
    }
    
    protected Method failCallSyncUserMethod;

    protected void SubUserPage() {
        Log.d(TAG, "resore the dpage--");
        if(mIsSearchMode) {
        	mSearchpage--;
        	if(mSearchpage < 0) {
        		mSearchpage = 0;
        	}
        	
        }else {
        	mPage--;
        	if (mPage < 0)
        		mPage = 0;
        }
    }

    protected void callFailLoadUserMethod() {
        try {
            if (failCallSyncUserMethod != null) {
                failCallSyncUserMethod.invoke(this, (Object[]) null);
            }
        } catch (Exception ne) {
        }
    }
    
    public View.OnClickListener loadOlderClick = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "load more user");
            getOldPeopleData();
        }
    };
    
    @Override
    public boolean isMoreItemHidden() {
    	if(mIsSearchMode) {
    		return isShowSearchMore;
    	}else {
    		return isUserShowMore;
    	}
    }

    @Override
    public OnClickListener getMoreItemClickListener() {
        return loadOlderClick;
    }

    @Override
    public int getMoreItemCaptionId() {
        return inLoadingPeople ? R.string.loading : R.string.list_view_more;
    }
    
    protected void getUserInfoEndCallBack(ArrayList<PublicCircleRequestUser> arraylist) {
        if (arraylist.size() <= 0) {
            isUserShowMore = false;
        } else {
            isUserShowMore = true;
        }
    }
    
    private void setMoreItem(boolean loading) {
        //set load older button text process for UI
        if(mListView != null) {
            for(int i=mListView.getChildCount()-1;i>0;i--) {
                View v = mListView.getChildAt(i);
                if(Button.class.isInstance(v)) {
                    Button bt = (Button)v;
                    if(loading) {
                        bt.setText(R.string.loading);
                    } else {
                        bt.setText(R.string.list_view_more);
                    }
                    break;
                }
            }
        }
    }

    private String mSearchKey;
    private boolean mIsTaskExecuting;
    private boolean mIsSearchMode;
    private boolean isShowSearchMore;
	@Override
	protected void doSearch(String key) {
		if(StringUtil.isEmpty(key)) {
			mIsSearchMode = false;
			 mAdapter.alterDataList(mPeopleList);			
		}else {
			mSearchKey = key;
			if(mIsTaskExecuting == false) {
				mIsSearchMode = true;
				new InlineSearchPeopleTask(key).execute((Void[]) null);
				mIsTaskExecuting = true;
			}
		}
	}
	
	private class InlineSearchPeopleTask extends android.os.AsyncTask<Void, Void, Void> {
		private String mkey;
		 public InlineSearchPeopleTask(String key) {
	            super();
	            mkey = key;
	        }
        @Override
        protected Void doInBackground(Void... params) {
        	Log.d(TAG, "InlineSearchPeopleTask: " + mkey);
        	generateSearchList(mkey);
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
        }
    }
	
	private void generateSearchList(String key) {
		Log.d(TAG, "generateSerachList: " + key);
		mSearchPeopleList.clear();
		mHandler.obtainMessage(REFHRE_INLINE_SEARCH_LIST).sendToTarget();
		for(PublicCircleRequestUser user : mPeopleList) {
			if((user.nick_name != null && user.nick_name.toLowerCase().contains(key.toLowerCase())) || 
					(user.name_pinyin != null && user.name_pinyin.contains(key.toLowerCase()))) {
				mSearchPeopleList.add(user);
			}
		}
		
		if(key.equals(mSearchKey) == false) {
			generateSearchList(mSearchKey);
		}else {
			mHandler.obtainMessage(REFHRE_INLINE_SEARCH_LIST).sendToTarget();
			mIsTaskExecuting = false;
		}
	}

	@Override
	protected void showSearchFromServerButton(boolean show, final String key,
			OnClickListener callback) {
		
		super.showSearchFromServerButton(show, key, R.string.search_circle_people, new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSearchpage = 0;
				gotoSearchCirclePeople(key);
			}
		});
	}
	
	private void gotoSearchCirclePeople(String key) {
		isNeedRefreshUi = true;
		searchCirclePeople(mCircle.circleid, mStatus, key, mSearchpage, mCount);
	}
	
	boolean inSearchPeople = false;
	Object mSearchPeopleLock = new Object();
	
	private void searchCirclePeople(final long circleid, final int status, String key,
            final int page, final int count) {
        if (QiupuConfig.LOGD) Log.d(TAG, "searchCirclePeople" + key);

        final String tmpkey = key.trim().toLowerCase();

        if (StringUtil.isEmpty(tmpkey)) {
            callFailLoadUserMethod();
            if (QiupuConfig.LOGD) Log.d(TAG, "input string is null");
            return;
        }
        
		synchronized (mSearchPeopleLock) {
			if (inSearchPeople == true) {
				Log.d(TAG, "in loading data");
				return;
			}
		}
        
        begin();
        setMoreItem(true);
		
        synchronized (mSearchPeopleLock) {
        	inSearchPeople = true;
		}
        asyncQiupu.SearchPublicCirclePeople(AccountServiceUtils.getSessionID(),
                circleid, status, tmpkey, page, count, new TwitterAdapter() {
            public void SearchPublicCirclePeople(ArrayList<PublicCircleRequestUser> users) {
                Log.d(TAG, "finish search user : " + users.size());

                if (users != null) {
                	if(mSearchpage == 0) {
                		mSearchPeopleList.clear();
                	}
                	mSearchPeopleList.addAll(users);
                	if(users.size() <= 0) {
                		isShowSearchMore = false;
                	}else {
                		isShowSearchMore = true;
                	}
                }
                doSearchUserCallBack(true, tmpkey);
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                doSearchUserCallBack(false, null);
                callFailLoadUserMethod();
            }
        });
    }
	
	private void doSearchUserCallBack(boolean result, String searchKey) {
		
		synchronized(mSearchPeopleLock) {
    		inSearchPeople = false;			
    	}
		
		if(mSearchKey != null) {
			if(mSearchKey.equals(searchKey)) {
				Message msg = mHandler.obtainMessage(LOAD_SEARCH_END);
				msg.getData().putBoolean(BasicActivity.RESULT, result);
				msg.sendToTarget();
			}else {
				mHandler.obtainMessage(SEARCH_CIRCLE_PEOPLE).sendToTarget();
			}
		}
        
    }
}
