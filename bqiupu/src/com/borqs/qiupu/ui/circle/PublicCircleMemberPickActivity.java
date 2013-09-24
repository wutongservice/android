package com.borqs.qiupu.ui.circle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import com.borqs.qiupu.ui.BasicNavigationActivity;
import twitter4j.PublicCircleRequestUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.publicCircleMemberPickAdapter;
import com.borqs.common.listener.CheckBoxClickActionListener;
import com.borqs.common.view.PublicCircleMemberPickItemView;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;
import com.borqs.qiupu.*;

public class PublicCircleMemberPickActivity extends BasicNavigationActivity implements CheckBoxClickActionListener {

	private final static String TAG = "PublicCircleMemberPickActivity";

	private ListView mListView;
	private publicCircleMemberPickAdapter mAdapter;
	
	private int mInMemberPage = 0;
    private int mCount = 200;
	private ArrayList<PublicCircleRequestUser> mInMemberPeople = new ArrayList<PublicCircleRequestUser>();
	private HashSet<Long> mSelectIds = new HashSet<Long>();

	private long mCircleId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.public_circle_member_pick_ui);
        setHeadTitle(R.string.string_select_user);
        showRightActionBtn(false);
        mCircleId = getIntent().getLongExtra(CircleUtils.CIRCLE_ID, 0);
        mAdapter = new publicCircleMemberPickAdapter(this);
        mAdapter.registerCheckClickActionListener(getClass().getName(), this);
        mListView = (ListView) findViewById(R.id.default_listview);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(userItemClickListener);
        
        Button select_ok = (Button) this.findViewById(R.id.select_ok);
        Button select_cancel = (Button) this.findViewById(R.id.select_cancel);

        select_ok.setOnClickListener(doSelectClick);
        select_cancel.setOnClickListener(doCancel);
        
        Message msg = mHandler.obtainMessage(LOAD_REQUEST_PEOPLE);
        msg.getData().putInt("page", mInMemberPage);
        msg.getData().putInt("count", mCount);
        msg.sendToTarget();
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
	protected void loadSearch()  {
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAdapter.unRegisterCheckClickActionListener(getClass().getName());
	}

//	@Override
//	public void setLeftMenuPosition() {
//		mPosition = LeftMenuMapping.getPositionForActivity(this);
//	}
	
	private static final int LOAD_REQUEST_PEOPLE = 101;
    private static final int LOAD_REQUEST_PEOPLE_END = 102;
    
	private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case LOAD_REQUEST_PEOPLE: {
                    syncRequestPeople(mCircleId, PublicCircleRequestUser.STATUS_IN_CIRCLE,
                            msg.getData().getInt("page"),
                            msg.getData().getInt("count"));
                break;
            }
            case LOAD_REQUEST_PEOPLE_END: {
                end();
                if(msg.getData().getBoolean(BasicActivity.RESULT)) {
                    Collections.sort(mInMemberPeople, PublicCircleRequestUser.USER_ROLE_COMPARATOR);
                    mAdapter.alterDataList(mInMemberPeople);
                }else {
                    ToastUtil.showOperationFailed(PublicCircleMemberPickActivity.this, mHandler, true);
                }
                break;
            }
            }
        }
    }

	boolean inLoadingPeople = false;
    Object mLockLoadPeople = new Object();
    private void syncRequestPeople(final long circleid, final int status,
            final int page, final int count) {
        Log.d(TAG, "status :" + status);
        synchronized (mLockLoadPeople) {
            if (inLoadingPeople == true) {
                ToastUtil.showShortToast(this, mHandler, R.string.string_in_processing);
                return;
            }
        }
        synchronized (mLockLoadPeople) {
            inLoadingPeople = true;
        }
        
        begin();
        asyncQiupu.getRequestPeople(AccountServiceUtils.getSessionID(),
                circleid, status, page, count, new TwitterAdapter() {
            public void getRequestPeople(ArrayList<PublicCircleRequestUser> arraylist) {
                Log.d(TAG, "finish syncRequestPeople=" + arraylist.size());
                
                if (page == 0) {
                    mInMemberPeople.clear();
                }
                mInMemberPeople.addAll(arraylist);
                
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
                
                Message msg = mHandler.obtainMessage(LOAD_REQUEST_PEOPLE_END);
                msg.getData().putString(BasicActivity.ERROR_MSG,
                        ex.getMessage());
                msg.getData().putBoolean(BasicActivity.RESULT, false);
                msg.sendToTarget();
            }
        });
    }
	
    AdapterView.OnItemClickListener userItemClickListener = new FriendsItemClickListener();

    private class FriendsItemClickListener implements
            AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            
            if(PublicCircleMemberPickItemView.class.isInstance(view))
            {
                PublicCircleMemberPickItemView uv = (PublicCircleMemberPickItemView)view;
                uv.switchCheck();
                changeItemSelect(uv.getUserID(),"", uv.isSelected(), true);
            }
        }
    }
    
    View.OnClickListener doSelectClick = new View.OnClickListener() {
        
        StringBuilder idsBuilder = new StringBuilder();
        public void onClick(View arg0) {
            if(mSelectIds.size() > 0) {
                Iterator<Long> it = mSelectIds.iterator();
                while (it.hasNext()) {
                    if (idsBuilder.length() > 0) {
                        idsBuilder.append(",");
                    }
                    idsBuilder.append(it.next());
                }
            }
            
            Log.d(TAG, "setSelectResult, toUsers: " + idsBuilder.toString());
            Intent data = new Intent();
            data.putExtra("toUsers", idsBuilder.toString());
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    };
    
    View.OnClickListener doCancel = new View.OnClickListener() {
        
        public void onClick(View arg0){
            PublicCircleMemberPickActivity.this.setResult(Activity.RESULT_CANCELED);
            PublicCircleMemberPickActivity.this.finish();
        }
    };
    
	@Override
	protected void loadRefresh() {
	    Message msg = mHandler.obtainMessage(LOAD_REQUEST_PEOPLE);
        msg.getData().putInt("page", 0);
        msg.getData().putInt("count", mCount);
        msg.sendToTarget();
	}

    @Override
    public void changeItemSelect(long itemId, String itemLabel, boolean isSelect, boolean isuser) {
        if (isSelect) {
            if(!mSelectIds.contains(itemId))
            {
                mSelectIds.add(itemId);
            }
        } else {
            if(mSelectIds.contains(itemId))
            {
                mSelectIds.remove(itemId);
            }
        }
    }
}
