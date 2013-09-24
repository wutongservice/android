package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import com.borqs.common.util.IntentUtil;
import twitter4j.UserCircle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.PickCircleUserAdapter;
import com.borqs.common.view.AtoZ;
import com.borqs.common.view.CircleItemView;
import com.borqs.common.view.UserSelectItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM.CircleColumns;
import com.borqs.qiupu.db.QiupuORM.UsersColumns;
import com.borqs.qiupu.service.FriendsManager;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CircleUtils;


public class PickCircleUserActivity extends BasicActivity implements FriendsManager.FriendsServiceListener, AtoZ.MoveFilterListener
{
    final String TAG = "Qiupu.PickCircleUserActivity";
    
    private View      search_span;
    private ListView  userListView;
    private Button    select_ok;
    private Button    select_cancel;
//    private ArrayList<QiupuUser> users = new ArrayList<QiupuUser>();
    private Cursor musers;
    private Cursor mCircles;
    private Cursor searchUsers;
    private Cursor searchCircles;
    private PickCircleUserAdapter us;
    private String[] receiveUserAdds;
//    private String receiveCircleAdds;
    private String mCircleid;
    private int mType;
    
    private EditText  keyEdit;
    private HashSet<Long> mSelectedUser = new HashSet<Long>();
	private HashSet<Long> mSelectedCircle = new HashSet<Long>();
    public final static String RECEIVE_ADDRESS = "RECEIVE_ADDRESS";
    public final static String RECEIVER_TYPE = "RECEIVER_TYPE";
    public final static int type_add_friends = 1;
    public final static int type_delete_friends = 2;
    public final static int type_all_circle_friends = 3;
    public final static int type_all_friends = 4;
    private AtoZ mAtoZ;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(QiupuConfig.LOGD)Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_list_ui);

		search_span = this.findViewById(R.id.search_span);
		select_ok = (Button)this.findViewById(R.id.select_ok);

        String from_exchange = getIntent().getStringExtra("from_exchange");
        if ("from_exchange".equals(from_exchange)) {
            setHeadTitle(R.string.invite_friends_exchange_vcard);
            select_ok.setText(R.string.public_circle_invite);
            select_ok.setOnClickListener(exchangeListener);
        } else {
            setHeadTitle(R.string.string_select_user);
            select_ok.setOnClickListener(doSelectClick);
        }

		select_cancel = (Button)this.findViewById(R.id.select_cancel);
		
		select_cancel.setOnClickListener(doCancel);

        if (null != search_span && search_span instanceof EditText) {
            keyEdit = (EditText) search_span;
            keyEdit.addTextChangedListener(new MyWatcher());
        }
        
		View convertView = findViewById(R.id.all_people_list);
		userListView = (ListView) convertView.findViewById(R.id.friends_list);
		
		AtoZ atoz = (AtoZ) convertView.findViewById(R.id.atoz);
        if (atoz != null) {
            mAtoZ = atoz;
            atoz.setFocusable(true);
            atoz.setMoveFilterListener(this);
            atoz.setVisibility(View.VISIBLE);               
            mAtoZ.setListView(userListView);              
        }
        
        Intent intent = getIntent();
        mType = intent.getIntExtra(RECEIVER_TYPE, -1);
        receiveUserAdds = intent.getStringArrayExtra(RECEIVE_ADDRESS);
        mCircleid = intent.getStringExtra(IntentUtil.EXTRA_KEY_CIRCLE_ID);
        cursorAllPeopleCircleWithType();
        
        us = new PickCircleUserAdapter(this, musers, mCircles, mAtoZ);
        userListView.setAdapter(us);
        userListView.setOnItemClickListener(userItemClickListener);
		
		setLeftActionImageRes(R.drawable.ic_btn_choice_press);
		showRightActionBtn(false);
		
		mHandler.obtainMessage(CHECK_SELECT_DATA).sendToTarget();
	}

    private View.OnClickListener exchangeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String selectUseraddress = getAddress(mSelectedUser, false);
            String selectCircleaddress = getAddress(mSelectedCircle, true);
            Log.d(TAG, "selected address : " + selectUseraddress + " " + selectCircleaddress );
            if(selectUseraddress.length() <= 0 && selectCircleaddress.length() <= 0) {
                Toast.makeText(PickCircleUserActivity.this, R.string.selected_one_user, Toast.LENGTH_SHORT).show();
            } else {
                circleUpdate(selectUseraddress, String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE), true);
            }
        }
    };

    @Override
    protected void doUsersSetCallBack(String uid, boolean isadd) {
        finish();
    }

    private void checkData() {
        if (type_all_circle_friends == mType) {
            if (musers.getCount() > 0 && mCircles.getCount() > 0) {
                generateSelectData();
                us.setSelectUser(mSelectedUser, mSelectedCircle);
            } else {
                if (musers.getCount() <= 0) {
                    loadUserFromServer();
                }
                if (mCircles.getCount() <= 0) {
                    mHandler.obtainMessage(GET_CIRCLE_INFO).sendToTarget();
                }
            }
        } else {
            if (musers.getCount() > 0) {
                generateSelectData();
                us.setSelectUser(mSelectedUser, mSelectedCircle);
            }else {
                if (musers.getCount() <= 0) {
                    loadUserFromServer();
                }
            }
            
//            if (isLocalHasUser() && musers.getCount() <= 0) {
//                loadUserFromServer();
//            }
        }
    }
	
	AdapterView.OnItemClickListener userItemClickListener = new AdapterView.OnItemClickListener() 
	{
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
        {
        	if(CircleItemView.class.isInstance(view))
        	{
        		CircleItemView cv = (CircleItemView) view;
        		cv.switchCheck();
        	}
        	else if(UserSelectItemView.class.isInstance(view))
        	{
        		UserSelectItemView uv = (UserSelectItemView)view;
        		uv.switchCheck();
        	}
        }
	};
	
	private String getAddress(HashSet<Long> set, boolean isgetCircles)
	{
		StringBuilder address = new StringBuilder();
		Iterator it = set.iterator();
		while(it.hasNext())
		{
			String id = String.valueOf(it.next());
			if(address.length()>0)
			{
				address.append(',');				    
			}
			
			if(isgetCircles)
			{
				if(QiupuConfig.isPublicCircleProfile(Long.parseLong(id))) {
					address.append("$" + id);
				}else {
					address.append("#" + id);
				}
			}
			else
			{
				address.append(id);
			}
			
		}
		return address.toString();
	}
	
	View.OnClickListener doSelectClick = new View.OnClickListener() {
		
		public void onClick(View arg0){
			String selectUseraddress = getAddress(mSelectedUser, false);
			String selectCircleaddress = getAddress(mSelectedCircle, true);
//			if(QiupuConfig.DBLOGD)
				Log.d(TAG, "selected address : " + selectUseraddress + " " + selectCircleaddress );
			if(selectUseraddress.length() <= 0 && selectCircleaddress.length() <= 0)
			{
				Toast.makeText(PickCircleUserActivity.this, R.string.selected_one_user, Toast.LENGTH_SHORT).show();
			}
			else
			{
				Intent data = new Intent();
				data.putExtra("address", selectUseraddress);
				data.putExtra("circles", selectCircleaddress);
				data.putExtra("type", mType);
				PickCircleUserActivity.this.setResult(Activity.RESULT_OK, data);
				PickCircleUserActivity.this.finish();
			}
		}
	};
	
    View.OnClickListener doCancel = new View.OnClickListener() {
		
		public void onClick(View arg0){
			PickCircleUserActivity.this.setResult(Activity.RESULT_CANCELED);
			PickCircleUserActivity.this.finish();
		}
	};
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (null != musers)
			musers.close();
		
		if(null != searchUsers)
			searchUsers.close();
		
		if(null != searchCircles)
			searchCircles.close();
		
		if(null != mCircles)
			mCircles.close();
	}
	
	
	private static final int LOAD_DATA       = 1;
	private static final int GET_CIRCLE_INFO = 2;
	private static final int GET_CIRCLE_END  = 3;
	private static final int LOAD_FRIENDS_CALLBACK = 4;
	private static final int CHECK_SELECT_DATA = 5;
	
	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case LOAD_DATA:
				{
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
						if(type_all_circle_friends == mType)
						{
							mCircles = orm.queryLocalPublicCircleInfo(CircleUtils.getAllFilterCircleIds());
						}
						generateSelectData();
						us.alterDataList(musers, mCircles, mSelectedUser, mSelectedCircle, mAtoZ);
					}
					break;
				}
				case LOAD_FRIENDS_CALLBACK: {
					int statuscode = msg.getData().getInt("statuscode");
					if (statuscode == FriendsManager.STATUS_DO_FAIL) 
					{
						end();
					}
					else if (statuscode == FriendsManager.STATUS_DOING) 
					{
						begin();
					}
					else if (statuscode == FriendsManager.STATUS_DO_OK) 
					{
//						if(musers != null)
//							musers.close();
                        end();						
						if(type_all_circle_friends == mType)
						{
                            queryAllSimpleUsers();
							generateSelectData();
						}
						else if(type_delete_friends == mType)
						{
							musers = orm.queryFriendsCursorByCircleId(mCircleid);
						}
						else if(type_add_friends == mType)
						{
							musers = orm.queryUserNotInCircle(mCircleid);
						}
						us.alterDataList(musers, mCircles, mSelectedUser, mSelectedCircle, mAtoZ);
					}
                    else if (statuscode == FriendsManager.STATUS_ITERATING) {
                        if (type_all_circle_friends == mType) {
                            queryAllSimpleUsers();
                            generateSelectData();
                        } else if (type_delete_friends == mType) {
                            musers = orm.queryFriendsCursorByCircleId(mCircleid);
                        } else if (type_add_friends == mType) {
                            musers = orm.queryUserNotInCircle(mCircleid);
                        }
                        us.alterDataList(musers, mCircles, mSelectedUser, mSelectedCircle, mAtoZ);
                    }
					break;
				}
				case CHECK_SELECT_DATA:{
				    checkData();
				}
			}
		}
	}
	
	@Override
	protected void loadRefresh() {
		super.loadRefresh();
		selectall = !selectall;		
		setLeftActionImageRes(selectall ? R.drawable.ic_btn_choice : R.drawable.ic_btn_choice_press);					
		if(selectall)
		{
//			resetuserCursorData();
		    cursorAllPeopleCircleWithType();
			if(musers !=null && musers.getCount() > 0)
			{
				musers.moveToPosition(-1);
				while(musers.moveToNext()){
					long uid = musers.getLong(musers.getColumnIndex(UsersColumns.USERID));
					if(!mSelectedUser.contains(uid))
					{
						mSelectedUser.add(uid);
					}
					else{
						Log.d(TAG, "have no this user : " + uid);	
					}
				}  
			}
			if(mCircles != null && mCircles.getCount() > 0)
			{
				while(mCircles.moveToNext()){  
					long uid = mCircles.getLong(mCircles.getColumnIndex(CircleColumns.CIRCLE_ID));
					if(!mSelectedCircle.contains(uid))
					{
						mSelectedCircle.add(uid);
					}
				}
			}
		}
		else
		{
			mSelectedUser.clear();
			mSelectedCircle.clear();
		}
		us.setSelectUser(mSelectedUser, mSelectedCircle);
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
	
	private void loadUserFromServer() 
	{
        IntentUtil.loadUsersFromServer(this);
	}

//	ImageView head_refresh; 
	boolean selectall = false;
	
	@Override
	protected void loadSearch() {		
		super.loadSearch();
		
		if(View.VISIBLE == search_span.getVisibility())
		{
		    search_span.setVisibility(View.GONE);
		}
		else
		{
			search_span.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void createHandler() {
		mHandler = new MainHandler();
	}
	
	private class MyWatcher implements TextWatcher 
    {   
       public void afterTextChanged(Editable s) 
       {
           //do search
           doSearch(s.toString());
       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) 
       {
       }
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
   }
	
	private void doSearch(String key)
    {
		if(isEmpty(key) == false)
		{
			if (null != searchUsers && !searchUsers.isClosed()) {
				searchUsers.close();
            }
			if(null != searchCircles && !searchCircles.isClosed())
			{
				searchCircles.close();
			}
			if(type_all_circle_friends == mType)
			{
				searchUsers = orm.searchFriendsCursor(key);
//				searchCircles = orm.searchCirclesCurosr(key);
			}
			else if(type_add_friends == mType)
			{
				searchUsers = orm.querySearchUserNotInCircle(mCircleid, key);
			}
			else if(type_delete_friends == mType)
			{
				searchUsers = orm.querySearchUserInCircle(mCircleid, key);
			}
			else if(type_all_friends == mType)
			{
			    searchUsers = orm.searchFriendsCursor(key);
			}
			
			us.alterDataList(searchUsers, searchCircles, mSelectedUser, mSelectedCircle, mAtoZ);
		}
		else
		{
		    cursorAllPeopleCircleWithType();
			us.alterDataList(musers, mCircles, mSelectedUser, mSelectedCircle, mAtoZ);
		}
    }

	private void generateSelectData() {
		mSelectedUser.clear();
		mSelectedCircle.clear();
		if (receiveUserAdds != null) {
			for (int i = 0; i < receiveUserAdds.length; i++) {
				String id = receiveUserAdds[i];
				Log.d(TAG, "receiver id : " + id);
				if (id.contains("#")) {
					int index = id.indexOf("#");
					id = id.substring(index + 1, id.length());
					mSelectedCircle.add(Long.parseLong(id));
				} else if (id.contains("$")) {
					int index = id.indexOf("$");
					id = id.substring(index + 1, id.length());
					mSelectedCircle.add(Long.parseLong(id));
				} else {
					try {
						mSelectedUser.add(Long.parseLong(id));

					} catch (Exception e) {
						Log.d(TAG, "initSelectUser : it is not id ");
					}
				}
			}
		}
	}

	public void updateUI(int msgcode, Message message) 
	{
		Message msg = mHandler.obtainMessage(LOAD_FRIENDS_CALLBACK);
		msg.getData().putInt("statuscode", msgcode);
		msg.sendToTarget();
	}
	
	public void changeSelect(long bindid, boolean isSelected, boolean isUseritem) {
		if(isUseritem)
		{
			if (isSelected) {
				if(!mSelectedUser.contains(bindid))
				{
					mSelectedUser.add(bindid);
				}
			} else {
				if(mSelectedUser.contains(bindid))
				{
					mSelectedUser.remove(bindid);
				}
			}
		}
		else
		{
			if (isSelected) {
				if(!mSelectedCircle.contains(bindid))
				{
					mSelectedCircle.add(bindid);
				}
			} else {
				if(mSelectedCircle.contains(bindid))
				{
					mSelectedCircle.remove(bindid);
				}
			}
		}

		us.setSelectmap(mSelectedUser, mSelectedCircle);
    }
	
	@Override
	protected void dogetUserCircleCallBack(boolean suc, ArrayList<UserCircle> userCircles)
	{
		Message msg = mHandler.obtainMessage(GET_CIRCLE_END);
		msg.getData().putBoolean(RESULT, suc);
		msg.sendToTarget();
	}

    @Override
    public void enterPosition(String alpha, int position) {
        userListView.setSelection(position);
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
    
    private void cursorAllPeopleCircleWithType(){
             
        if(type_all_circle_friends == mType)
        {
            queryAllSimpleUsers();
            mCircles = orm.queryLocalPublicCircleInfo(CircleUtils.getAllFilterCircleIds());
//            mCircles = orm.queryAllCircleinfo(filtercirlceString, AccountServiceUtils.getBorqsAccountID());
        }
        else if(type_delete_friends == mType)
        {
            musers = orm.queryFriendsCursorByCircleId(mCircleid);
        }
        else if(type_add_friends == mType)
        {
            musers = orm.queryUserNotInCircle(mCircleid);
        }
        else if(type_all_friends == mType)
        {
            queryAllSimpleUsers();
        }
    }
    
    private boolean isLocalHasUser(){
        queryAllSimpleUsers();
        return musers.getCount() > 0;
    }

    private void queryAllSimpleUsers() {
        musers = orm.queryAllSimpleUserInfo();
        Log.d(TAG, "queryAllSimpleUsers, get count: " + musers.getCount());
    }
}
