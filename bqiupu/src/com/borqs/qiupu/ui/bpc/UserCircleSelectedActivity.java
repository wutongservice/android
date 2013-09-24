package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;
import java.util.HashSet;

import com.borqs.common.util.IntentUtil;
import twitter4j.QiupuUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.borqs.common.adapter.CircleAdapter;
import com.borqs.common.listener.CheckBoxClickActionListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.view.CircleItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.service.FriendsManager;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;


public class UserCircleSelectedActivity extends BasicActivity 
                                 implements FriendsManager.FriendsServiceListener, CheckBoxClickActionListener
{
    private final String TAG = "UserCircleSelectedActivity";
    private Handler mhandler;
    private CheckBox  id_privacy_circle_check;
	private boolean selectall = true;
	private boolean selectprivacy;
    private ArrayList<UserCircle> circles = new ArrayList<UserCircle>();
    private CircleAdapter mcircleadapter;
//    private String bundleCirclename;
    private long mBorqsId;
    private String circleIds;
    private QiupuUser mUser;
    private String mContent = "";
    private String mContactName = "";
    private long mContactId;
    private AlertDialog mAlertDialog;
    private int mContentType ;
    public static final int CONTENTTYPE_BACKRESULT = 1;
    public static final int CONTENTTYPE_SETCIRCLE  = 2;
    private HashSet<Long> mOldCircleMap = new HashSet<Long>();
    private HashSet<Long> mNewCircleMap = new HashSet<Long>();
    
    //TODO
    //we need use cursor, not array list to do, this is to save memeory
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(QiupuConfig.LOGD)Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.circle_list_ui);
//		
//		setHeadTitle(R.string.circle_select_title);
		View circleview = LayoutInflater.from(this).inflate(R.layout.circle_list_ui, null);

        ListView circleListView = (ListView) circleview.findViewById(R.id.circle_list);
        View id_privacy_circle_rl = circleview.findViewById(R.id.id_privacy_circle_rl);
		id_privacy_circle_rl.setOnClickListener(privacyCircleOnClickListener);
		
		id_privacy_circle_check = (CheckBox) circleview.findViewById(R.id.id_privacy_circle_check);
//		id_privacy_circle_check.setChecked(selectprivacy);
		id_privacy_circle_check.setOnClickListener(privacyCircleOnClickListener);
		
		String dialogTitle = "";
		Intent intent = getIntent();
		mContentType = intent.getIntExtra("contentType", -1);
		
		if(mContentType == CONTENTTYPE_BACKRESULT) {
			mcircleadapter = new CircleAdapter(this,circles, false);
			circleIds = intent.getStringExtra(IntentUtil.EXTRA_KEY_CIRCLE_ID);
			id_privacy_circle_check.setChecked(false);
			dialogTitle = getString(R.string.stream_choose_circle);
			circleview.findViewById(R.id.id_privacy_circle_rl).setVisibility(View.GONE);
			circleview.findViewById(R.id.id_span_view).setVisibility(View.GONE);
		}else {
			mcircleadapter = new CircleAdapter(this,circles, true);
			dialogTitle = getString(R.string.circle_select_title);
			mBorqsId = intent.getLongExtra("uid", -1);
			circleIds = intent.getStringExtra(IntentUtil.EXTRA_KEY_CIRCLE_ID);
			if(isEmpty(circleIds)) {
				circleIds = orm.queryOneUserCircleIds(mBorqsId);
			}
			
			mContent = intent.getStringExtra("content");
			if(StringUtil.isEmpty(mContent) == false) {
				//TODO query local db to getborqsId, circleIds
				mContactName = intent.getStringExtra("contactname"); 
				mContactId = intent.getLongExtra("contactId", -1);
			}
		}
		mcircleadapter.registerCheckClickActionListener(getClass().getName(), this);
		circleListView.setAdapter(mcircleadapter);
		circleListView.setOnItemClickListener(circleItemClickListener);
	
		generateOldCircleMap(circleIds);
		
		showCircleDialogList(dialogTitle, circleview);
		
		Message msg = mhandler.obtainMessage(LOAD_DATA);
		mhandler.sendMessageDelayed(msg, 0);
	}
	
	AdapterView.OnItemClickListener circleItemClickListener = new AdapterView.OnItemClickListener() 
	{
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
        {
        	if(CircleItemView.class.isInstance(view))
        	{
        		CircleItemView uv = (CircleItemView)view;
        		if(uv.getCircle() != null)
        		{
        			uv.switchCheck();
        			changeSelect();
        		}
        		else
        		{
        			showDialog(DIALOG_CREATE_CIRCLE);
        		}
        	}
        }
	};
	
	View.OnClickListener privacyCircleOnClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
			selectprivacy = !selectprivacy;
			id_privacy_circle_check.setChecked(selectprivacy);
		}
	};
	
	private String[] getAddress()
	{
		String[] circleInfo = new String[2];
		StringBuilder circleId = new StringBuilder();
		StringBuilder circleName = new StringBuilder();
		for(UserCircle circle:circles)
		{
			if(circle.selected)
			{
				mNewCircleMap.add(circle.circleid);
				if(circleId.length()>0)
				{
					circleId.append(',');				    
				}
				circleId.append(circle.circleid);
				
				if(circleName.length()>0)
				{
					circleName.append(",");
				}
				if(QiupuHelper.inLocalCircle(String.valueOf(circle.circleid)) == false) {
				    circleName.append(CircleUtils.getCircleName(this, circle.circleid, circle.name));
				}
			}
		}
		// add privacy circle
		if(circleId.toString().length() > 0 && id_privacy_circle_check.isChecked())
		{
			circleId.append(",");
			circleId.append(QiupuConfig.ADDRESS_BOOK_CIRCLE);
//			circleName.append(",");
//			circleName.append(getResources().getString(R.string.address_book_circle));
			
			mNewCircleMap.add(new Long(QiupuConfig.ADDRESS_BOOK_CIRCLE));
		}
		
		circleInfo[0] = circleId.toString();
		circleInfo[1] = circleName.toString();
		return circleInfo;
	}
	
	@Override
	protected void deleteUserFromCircleCallBack() {
		gotoSetCircle();
	}
	
	@Override
	protected void loadRefresh() {
		super.loadRefresh();
		
		setLeftActionImageRes(selectall ?R.drawable.ic_btn_choice:R.drawable.ic_btn_choice_press);
		//select all resource
		for(UserCircle circle:circles)
		{
			circle.selected = selectall;
		}						
		selectall = !selectall;		
		mcircleadapter.notifyDataSetChanged();
	}
	
	
	@Override
	protected void onPause() {
		FriendsManager.unregisterFriendsServiceListener(getClass().getName());
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume:");
		FriendsManager.registerFriendsServiceListener(getClass().getName(), this);
		super.onResume();
	}
	
	@Override
	protected void createHandler() {
		mhandler = new MainHandler();
	}
	
	private static final int LOAD_DATA       = 1;
	private static final int GET_CIRCLE_INFO = 2;
	private static final int GET_CIRCLE_END  = 3;
	private static final int ADD_CONTACT_FRIENDS_END = 4;
	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case LOAD_DATA:
				{
					restructureCircleData();
					mcircleadapter.notifyDataSetChanged();
					break;
				}	
				case GET_CIRCLE_INFO:
				{
					syncCircleInfo();
					break;
				}
				case GET_CIRCLE_END:
				{
					end();
					if(msg.getData().getBoolean(RESULT))
					{
						setCircleData(getSaveUid());
						setcheckForCircle();
						mcircleadapter.notifyDataSetChanged();
					}
					break;
				}
				case ADD_CONTACT_FRIENDS_END: {
					try {
                        dismissDialog(DIALOG_SET_CIRCLE_PROCESS);
                    } catch (Exception ne) {
                        Log.d(TAG, "CIRCLE_SET_END " + ne.getMessage());
                    }
                    doActionFriendEndCallBack(msg);
					break;
				}
			}
		}
	}
	

	public void updateUI(int msgcode, Message msg) 
	{
		post(new Runnable()
		{
			public void run()
			{
				refreshUI();
			}
		});
	}
	
	private void refreshUI()
	{
		setCircleData(getSaveUid());
		setcheckForCircle();
	}
	
	@Override
	protected void doCircleActionCallBack(boolean isdelete)
	{
	    changeSelect();
        mcircleadapter.notifyDataSetChanged();
//	    if(!isdelete) {
//	        refreshUI();
//	    }
	}
	
	private void restructureCircleData(){
        if (setCircleData(getSaveUid())) {
            setcheckForCircle();
        } else {
			// no circles, have a try to get from server
			mhandler.obtainMessage(GET_CIRCLE_INFO).sendToTarget();
		}
	}
	
	private void setcheckForCircle(){
		if(StringUtil.isValidString(circleIds))
		{
			String[] circleArr = circleIds.split(",");
			for(int i=0;i<circleArr.length;i++)
			{
				if(Long.parseLong(circleArr[i]) == QiupuConfig.ADDRESS_BOOK_CIRCLE)
				{
					if(mContentType != CONTENTTYPE_BACKRESULT) {
						selectprivacy = true;
						id_privacy_circle_check.setChecked(true);
					}
				}
				else
				{
					for(int j=0;j<circles.size();j++)
					{
						UserCircle tmpcircle = circles.get(j);
						if(Long.parseLong(circleArr[i]) == tmpcircle.circleid)
						{
							tmpcircle.selected = true;
							break;
						}
					}
				}
			}
		}
		else 
		{
			selectprivacy = true;
			id_privacy_circle_check.setChecked(true);
//			setDialogButtonEnable(false);
		}
	}
	
	private boolean setCircleData(long uid)
	{
		circles.clear();
		boolean result;
		Cursor cursor;
		if(CONTENTTYPE_BACKRESULT == mContentType) {
			 cursor = orm.queryLocalCircles();
			 if(cursor != null && cursor.getCount() <= 0) {
				 orm.insertExpandCirCleInfo();
				 result = false;
			 }
		}else {
			cursor = orm.queryLocalCircles();
		}

		if(cursor != null) {
			if(cursor.moveToFirst()) {
				do {
					final UserCircle tmpcircle = QiupuORM.createCircleInformation(cursor);
					if(!QiupuHelper.inFilterStreamCircle(String.valueOf(tmpcircle.circleid)))
					{
						Log.d(TAG, "circle Name: " + tmpcircle.name);
						circles.add(tmpcircle);
					}
				} while (cursor.moveToNext());
			}
			result = cursor.getCount() > 0;
			cursor.close();
			cursor = null;
		}else {
			result = false;
		}

        return result;
	}
	
	private ArrayList<UserCircle> getSelectedCircles()
	{
		ArrayList<UserCircle> info = new ArrayList<UserCircle>();
		for(UserCircle circle : circles)
		{
			if(circle.selected)
			{
				info.add(circle);				    
			}
		}
		return info;
	}
	
	private void changeSelect(){
		ArrayList<UserCircle> selectCircles = getSelectedCircles();
		if(selectCircles.size() <= 0) {
			selectprivacy = false;
			id_privacy_circle_check.setChecked(false);
			if(mContentType == CONTENTTYPE_BACKRESULT) {
				setDialogButtonEnable(false);
			}
		}else {
			setDialogButtonEnable(true);
		}
	}
	
    @Override
    protected void dogetUserCircleCallBack(boolean suc, ArrayList<UserCircle> userCircles)
    {
    	if(suc)
    	{
    		Message msg = mhandler.obtainMessage(GET_CIRCLE_END);
			msg.getData().putBoolean(RESULT, true);
			msg.getData().putInt("size", userCircles.size());
			msg.sendToTarget();
    	}
    	else
    	{
    		Message msg = mhandler.obtainMessage(GET_CIRCLE_END);
			msg.getData().putBoolean(RESULT, false);
			msg.sendToTarget();
    	}
    }
    
    @Override
    protected void finishActionFriendsCallBack(QiupuUser user) {
    	mUser = user;
    	mUser.contactId = mContactId;
    	Log.d(TAG, "mUser.contactId : " + mUser.contactId);
    	super.finishActionFriendsCallBack(mUser);
    }
    
    @Override
    protected void doActionFriendEndCallBack(Message msg){
    	
    	boolean suc = msg.getData().getBoolean(RESULT, false);
		if(suc) {
			QiupuHelper.updateActivityUI(mUser);
		}else {
            showOperationFailToast("", true);
		}
		finish();
    }
    
    private void gotoSetCircle() {
    	String[] backInfo = getAddress();
        Log.d(TAG, "back data info : " + backInfo[0] + " " + backInfo[1] + "cricleids " + circleIds);

		if (mOldCircleMap.size() == mNewCircleMap.size() && mOldCircleMap.containsAll(mNewCircleMap)) {
			Log.d(TAG, "circle is not changed , do noting");
			finish();
        } else {
        	Log.d(TAG, "setcircle ids : " + backInfo[0] + " names: " + backInfo[1]);
        	if(StringUtil.isEmpty(mContent) == false) {
        		addContactFriends(mContactName, backInfo[0], mContent);
        	}else {
        		setCircle(mBorqsId, backInfo[0], backInfo[1]);
        	}
        }
    }
    
    private boolean inAddContactFriends;
    private Object mLockAddContactFriends = new Object();

    private void addContactFriends(final String name, final String circleid, final String content) {
        if (inAddContactFriends == true) {
            Toast.makeText(this, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLockAddContactFriends) {
        	inAddContactFriends = true;
        }
        showDialog(DIALOG_SET_CIRCLE_PROCESS);

        asyncQiupu.addFriendsContact(getSavedTicket(), name, circleid, content, new TwitterAdapter() {
            public void addFriendsContact(QiupuUser resultUser) {
                Log.d(TAG, "finish addContactFriends=" + resultUser);

                if (resultUser != null) {
                    finishActionFriendsCallBack(resultUser);
                }

                Message msg = mhandler.obtainMessage(ADD_CONTACT_FRIENDS_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();


                synchronized (mLockAddContactFriends) {
                	inAddContactFriends = false;
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockAddContactFriends) {
                	inAddContactFriends = false;
                }
                Message msg = mhandler.obtainMessage(ADD_CONTACT_FRIENDS_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }
    
    private void showCircleDialogList(String title, View view) {
    	mAlertDialog = DialogUtils.ShowDialogwithView(this, title, R.drawable.ic_bpc_launcher, view, positiveListener, negativeListener);
    	mAlertDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				UserCircleSelectedActivity.this.finish();
			}
    	});
    }
    
    private DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(CONTENTTYPE_BACKRESULT == mContentType) {
				// back select circleIds
				Intent data = new Intent();
				String[] backInfo = getAddress();
				data.putExtra("circleids", backInfo[0]);
				data.putExtra("circlenames", backInfo[1]);
				UserCircleSelectedActivity.this.setResult(Activity.RESULT_OK, data);
				UserCircleSelectedActivity.this.finish();
			}else {
				gotoSetCircle();
			}
		}
	};
	
	private DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(CONTENTTYPE_BACKRESULT == mContentType) {
				UserCircleSelectedActivity.this.setResult(Activity.RESULT_CANCELED);
			}
			UserCircleSelectedActivity.this.finish();
		}
	};
	
    private void setDialogButtonEnable(boolean flag) {
        mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(flag);
    }

	@Override
	public void changeItemSelect(long itemId, String itemLabel, boolean isSelect, boolean isuser) {
		changeSelect();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mcircleadapter.unRegisterCheckClickActionListener(getClass().getName());
	}
	
	private void generateOldCircleMap(String circleIds) {
		if(StringUtil.isEmpty(circleIds) == false) {
			String[] ids = circleIds.split(",");
			for(int i=0; i<ids.length; i++) {
				mOldCircleMap.add(Long.parseLong(ids[i]));
			}
		}
	}
	
	@Override
	protected void createCircleCallBack(UserCircle circle) {
	    circle.selected = true;
	    circles.add(circle);
	}
}
