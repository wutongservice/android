package com.borqs.qiupu.fragment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.borqs.common.adapter.PickCircleUserAdapter;
import com.borqs.common.listener.CheckBoxClickActionListener;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.AtoZ;
import com.borqs.common.view.CircleItemView;
import com.borqs.common.view.UserSelectItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.CircleColumns;
import com.borqs.qiupu.db.QiupuORM.UsersColumns;
import com.borqs.qiupu.service.FriendsManager;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;

public final class LocalUserListFragment extends BasicFragment.UserFragment implements
		AtoZ.MoveFilterListener, FriendsManager.FriendsServiceListener, CheckBoxClickActionListener{
	private static final String TAG = "LocalUserListFragment";

	private Activity mActivity;
	private ListView mListView;
	private TextView mEmptyView;
	private AtoZ mAtoZ;
	private PickCircleUserAdapter us;
	private Cursor mCircles;
	private Cursor searchUsers;
	private Cursor searchCircles;
	private Handler mHandler;
//	private int mType;
//	private String mCircleid;
	public final static String RECEIVER_TYPE = "RECEIVER_TYPE";
	public final static String RECEIVE_ADDRESS = "RECEIVE_ADDRESS";
//	public final static int type_add_friends = 1;
//	public final static int type_delete_friends = 2;
//	public final static int type_all_circle_friends = 3;
//	public final static int type_all_friends = 4;
	public static final int TYPE_USER = 1;
	public static final int TYPE_CIRCLE = 2;
	public static final int TYPE_CIRCLE_USER = 3;
	public static final int TYPE_EXCHANGE_USER = 4;
	private int mCurrentType = TYPE_CIRCLE_USER;
	
	private HashSet<Long> mSelectedUser = new HashSet<Long>();
	private HashSet<Long> mSelectedCircle = new HashSet<Long>();
	private HashMap<Long, String> mSelectedMap = new HashMap<Long, String>();
	private String[] receiveUserAdds;
	private final String filtercirlceString = QiupuConfig.BLOCKED_CIRCLE + "," + QiupuConfig.ADDRESS_BOOK_CIRCLE
	                                                 + "," + QiupuConfig.CIRCLE_ID_ALL + "," + QiupuConfig.CIRCLE_ID_HOT 
	                                                 + "," + QiupuConfig.CIRCLE_ID_NEAR_BY;

    public LocalUserListFragment() {
        
    }

//    private boolean mFromExchange = false;

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		mActivity = activity;
		try{
			CallBackLocalUserListFragmentListener listener = (CallBackLocalUserListFragmentListener)activity;
			listener.getLocalUserListFragment(this);
		}catch (ClassCastException e) {
			Log.d(TAG, activity.toString() +  "must implement CallBackLocalUserListFragmentListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		orm = QiupuORM.getInstance(mActivity);
		Intent intent = mActivity.getIntent();
//		mType = intent.getIntExtra(RECEIVER_TYPE, -1);
		receiveUserAdds = intent.getStringArrayExtra(
				RECEIVE_ADDRESS);
//		mCircleid = intent.getStringExtra(IntentUtil.EXTRA_KEY_CIRCLE_ID);
		mHandler = new MainHandler();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View convertView = inflater.inflate(R.layout.friends_list_a2z,
				container, false);
		mListView = (ListView) convertView.findViewById(R.id.friends_list);
		mEmptyView = (TextView) convertView.findViewById(R.id.empty_text);

		AtoZ atoz = (AtoZ) convertView.findViewById(R.id.atoz);
		if (atoz != null) {
			mAtoZ = atoz;
			atoz.setFocusable(true);
			atoz.setMoveFilterListener(this);
			atoz.setVisibility(View.VISIBLE);
			mAtoZ.setListView(mListView);
		}

		mListView.setOnItemClickListener(userItemClickListener);

		return convertView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		cursorAllPeopleCircleWithType();
		
		us = new PickCircleUserAdapter(mActivity, musers, mCircles, mAtoZ);
		us.registerCheckClickActionListener(getClass().getName(), this);
		if (mCurrentType == TYPE_EXCHANGE_USER) {
    		if (musers != null && musers.getCount() > 0) {
    		    mAtoZ.setVisibility(View.VISIBLE);
    		    mListView.setVisibility(View.VISIBLE);
    		    mEmptyView.setVisibility(View.GONE);
    		} else {
    		    mAtoZ.setVisibility(View.GONE);
    		    mListView.setVisibility(View.GONE);
    		    mEmptyView.setVisibility(View.VISIBLE);
    		    mEmptyView.setText(R.string.empty_tip_title);
    		}
		}
		mListView.setAdapter(us);
		mHandler.obtainMessage(CHECK_SELECT_DATA).sendToTarget();
	}
	
	public void onResume() {
		FriendsManager.registerFriendsServiceListener(getClass().getName(), this);
		super.onResume();
	};
	
	public void onPause() {
		FriendsManager.unregisterFriendsServiceListener(getClass().getName());
		super.onPause();
	};
	
	public void onDestroy() {
		super.onDestroy();
		us.unRegisterCheckClickActionListener(getClass().getName());
	};

	AdapterView.OnItemClickListener userItemClickListener = new FriendsItemClickListener();

	private class FriendsItemClickListener implements
			AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			if(CircleItemView.class.isInstance(view))
        	{
				CircleItemView cv = (CircleItemView) view;
				cv.switchCheck();
				changeSelect(cv.getDataItemId(), cv.isCircleSelected(), false);
        	}
        	else if(UserSelectItemView.class.isInstance(view))
        	{
        		UserSelectItemView uv = (UserSelectItemView)view;
        		uv.switchCheck();
        		changeSelect(uv.getUserID(), uv.isSelected(), true);
        	}
		}
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

	private static final int GET_CIRCLE_INFO = 2;
	private static final int GET_CIRCLE_END = 3;
	private static final int LOAD_FRIENDS_CALLBACK = 4;
	private static final int CHECK_SELECT_DATA = 5;

	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_CIRCLE_INFO: {
//				 mActivity.syncCircleInfo("", false);
				break;
			}
			case GET_CIRCLE_END: {
				// end();
				// if(msg.getData().getBoolean(RESULT))
				// {
				// if(type_all_circle_friends == mType)
				// {
				// mCircles = orm.queryAllCircleinfo(filtercirlceString,
				// AccountServiceUtils.getBorqsAccountID());
				// }
				// generateSelectData();
				// us.alterDataList(musers, mCircles, mSelectedUser,
				// mSelectedCircle, mAtoZ);
				// }
				break;
			}
			case LOAD_FRIENDS_CALLBACK: {
				int statuscode = msg.getData().getInt("statuscode");
				if (statuscode == FriendsManager.STATUS_DO_FAIL)
				{
//					end();
				}
				else if (statuscode == FriendsManager.STATUS_DOING)
				{
//					begin();
				}
				else if (statuscode == FriendsManager.STATUS_DO_OK)
				{
					// if(musers != null)
					// musers.close();
//					end();
//					if(type_all_circle_friends == mType)
//					{
                    queryAllSimpleUser();
						generateSelectData(receiveUserAdds, mSelectedUser, mSelectedCircle);
//					}
//					else if(type_delete_friends == mType)
//					{
//						musers = orm.queryFriendsCursorByCircleId(mCircleid);
//					}
//					else if(type_add_friends == mType)
//					{
//						musers = orm.queryUserNotInCircle(mCircleid);
//					}
					us.alterDataList(musers, mCircles, mSelectedUser,
							mSelectedCircle, mAtoZ);
				}
                else if (statuscode == FriendsManager.STATUS_ITERATING) {
                    queryAllSimpleUser();
                    generateSelectData(receiveUserAdds, mSelectedUser, mSelectedCircle);
                    us.alterDataList(musers, mCircles, mSelectedUser,
                            mSelectedCircle, mAtoZ);
                }
				break;
			}
			case CHECK_SELECT_DATA: {
				checkData();
			}
			}
		}
	}

	private void generateSelectData () {
        mSelectedUser.clear();
        mSelectedCircle.clear();
        if(receiveUserAdds!= null) {
            for(int i=0; i<receiveUserAdds.length; i++) {
                String id = receiveUserAdds[i];
                Log.d(TAG, "receiver id : " + id );
                if(id.contains("#")) {
                    int index = id.indexOf("#");
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

	private void checkData() {
//		if (type_all_circle_friends == mType) {
	    if (mCurrentType == TYPE_EXCHANGE_USER || mCurrentType == TYPE_USER) {
	        if (musers.getCount() > 0) {
                generateSelectData();
                us.setSelectUser(mSelectedUser, mSelectedCircle);
            }else {
                if (musers.getCount() <= 0) {
                    loadUserFromServer();
                }
            }
	    } else {
			if (musers.getCount() > 0 && mCircles.getCount() > 0) {
				generateSelectData(receiveUserAdds, mSelectedUser, mSelectedCircle);
				us.setSelectUser(mSelectedUser, mSelectedCircle);
			} else {
				if (musers.getCount() <= 0) {
					loadUserFromServer();
				}
				if (mCircles.getCount() <= 0) {
					mHandler.obtainMessage(GET_CIRCLE_INFO).sendToTarget();
				}
			}
	    }
//		} else {
//			if (isLocalHasUser() && musers.getCount() <= 0) {
//				loadUserFromServer();
//			}musers
//		}
	}

	public void generateSelectData(final String[] receiveAddr, HashSet<Long> selectedUser,
	        HashSet<Long> selectedCircle) {
	    selectedUser.clear();
	    selectedCircle.clear();
		if (receiveAddr != null) {
			for (int i = 0; i < receiveAddr.length; i++) {
				String id = receiveAddr[i];
				if (id.contains("#")) {
					int index = id.indexOf("#");
					id = id.substring(index + 1, id.length());

					selectedCircle.add(Long.parseLong(id));
				} else {
					try {
					    selectedUser.add(Long.parseLong(id));

					} catch (Exception e) {
						Log.d(TAG, "initSelectUser : it is not id ");
					}
				}
			}
		}
	}
	
	private void cursorAllPeopleCircleWithType(){
        
        if (mCurrentType == TYPE_EXCHANGE_USER) {
            musers = orm.queryUserNotInCircle(String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE));
        } else if(mCurrentType == TYPE_USER) {
            queryAllSimpleUser();
        } else {
            queryAllSimpleUser();
//            mCircles = orm.queryLocalPublicCircleInfo(CircleUtils.getAllFilterCircleIds());
            mCircles = orm.queryCirclesEventOutofMonthinfo(filtercirlceString, -1);
        }
    }

	private void loadUserFromServer() {
        IntentUtil.loadUsersFromServer(mActivity);
	}

	private void changeSelect(long bindid, boolean isSelected, boolean isUseritem) {
		Log.d(TAG, "changeSelect: " + bindid);
		if(isUseritem)
		{
			if (isSelected) {
				if(!mSelectedUser.contains(bindid))
				{
					mSelectedUser.add(bindid);
					mSelectedMap.put(bindid, orm.getUserName(bindid));
				}
			} else {
				if(mSelectedUser.contains(bindid))
				{
					mSelectedUser.remove(bindid);
					mSelectedMap.remove(bindid);
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
	
	public HashSet<Long> getLocalSelectuser(){
		return mSelectedUser;
	}
	public HashSet<Long> getLocalSelectCircle(){
		return mSelectedCircle;
	}
	
	public void doSearch(String key)
    {
		if(!StringUtil.isEmpty(key))
		{
			if (null != searchUsers && !searchUsers.isClosed()) {
				searchUsers.close();
            }
			if(null != searchCircles && !searchCircles.isClosed())
			{
				searchCircles.close();
			}
			
			searchUsers = orm.searchFriendsCursor(key);
			
			us.alterDataList(searchUsers, searchCircles, mSelectedUser, mSelectedCircle, mAtoZ);
		}
		else
		{
		    cursorAllPeopleCircleWithType();
			us.alterDataList(musers, mCircles, mSelectedUser, mSelectedCircle, mAtoZ);
		}
    }

    public void doSearch(String key, String uids) {
        if (!StringUtil.isEmpty(key)) {
            if (null != searchUsers && !searchUsers.isClosed()) {
                searchUsers.close();
            }
            if (null != searchCircles && !searchCircles.isClosed()) {
                searchCircles.close();
            }

            searchUsers = orm.searchExchangeCursor(key, uids);

            us.alterDataList(searchUsers, searchCircles, mSelectedUser,
                    mSelectedCircle, mAtoZ);
        } else {
            cursorAllPeopleCircleWithType();
            us.alterDataList(musers, mCircles, mSelectedUser, mSelectedCircle,
                    mAtoZ);
        }
    }

	public void selectAllUser(boolean selectall){
	    if(selectall)
        {
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
	public void updateUI(int msgcode, Message message) {
		if(QiupuConfig.LOGD)Log.d(TAG, "msgcode: " + msgcode + " message: " + message);
		Message msg = mHandler.obtainMessage(LOAD_FRIENDS_CALLBACK);
		msg.getData().putInt("statuscode", msgcode);
		msg.sendToTarget();		
	}

	@Override
	public void changeItemSelect(long itemId, String itemLabel, boolean isSelect, boolean isuser) {
		changeSelect(itemId, isSelect, isuser);
	}
	
	public interface CallBackLocalUserListFragmentListener {
		public void getLocalUserListFragment(LocalUserListFragment fragment);
	}

    public void setFromType(boolean from_exchange) {
        if(from_exchange) {
            mCurrentType = TYPE_EXCHANGE_USER;
        }
    }
    
    public void setType(int type) {
        mCurrentType = type;
    }
    
    public String getSelectName() {
        StringBuilder tmpString = new StringBuilder();
        if(mSelectedMap.size() > 0) {
            Iterator iter = mSelectedMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String val = (String) entry.getValue();
                if(tmpString.length() > 0) {
                    tmpString.append(",");
                }
                tmpString.append(val);
            }
        }
        return tmpString.toString();
    }
}
