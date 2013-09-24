package com.borqs.qiupu.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import twitter4j.QiupuUser;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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
import com.borqs.qiupu.ui.bpc.InvitePeopleFragmentActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;

public final class InviteSysUsersFragment extends PickContactBaseFragment implements
		AtoZ.MoveFilterListener, FriendsManager.FriendsServiceListener, CheckBoxClickActionListener{
	private static final String TAG = "InviteSysUsersFragment";

	private Activity mActivity;
//	private ListView mListView;
	private AtoZ mAtoZ;
	private PickCircleUserAdapter us;
	private Cursor mCircles;
	private Cursor searchUsers;
	private Cursor searchCircles;
	private Handler mHandler;
	public final static String RECEIVER_TYPE = "RECEIVER_TYPE";
	public final static String RECEIVE_ADDRESS = "RECEIVE_ADDRESS";
	public final static String FILTER_IDS = "FILTER_IDS";
	public static final int TYPE_USER = 1;
	public static final int TYPE_CIRCLE = 2;
	public static final int TYPE_CIRCLE_USER = 3;
	public static final int TYPE_EXCHANGE_USER = 4;
	private int mCurrentType = TYPE_CIRCLE_USER;
	
	private HashSet<Long> mSelectedUser = new HashSet<Long>();
	private HashSet<Long> mSelectedCircle = new HashSet<Long>();
	private HashMap<Long, String> mSelectedMap = new HashMap<Long, String>();
	private HashMap<Long, String> mSelectedCircleMap = new HashMap<Long, String>();
	private String[] receiveUserAdds;
	private String mFilterIds;
	private View mSecondHeadView;
	
	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		mActivity = activity;
		try{
		    CallBackInviteSysUsersFragmentListener listener = (CallBackInviteSysUsersFragmentListener)activity;
			listener.getInviteSysUsersFragment(this);
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
		receiveUserAdds = intent.getStringArrayExtra(RECEIVE_ADDRESS);
		mFilterIds = intent.getStringExtra(FILTER_IDS);
		mHandler = new MainHandler();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		mConvertView = inflater.inflate(R.layout.friends_list_a2z,
				container, false);
		mListView = (ListView) mConvertView.findViewById(R.id.friends_list);

		AtoZ atoz = (AtoZ) mConvertView.findViewById(R.id.atoz);
		if (atoz != null) {
			mAtoZ = atoz;
			atoz.setFocusable(true);
			atoz.setMoveFilterListener(this);
			atoz.setVisibility(View.VISIBLE);
			mAtoZ.setListView(mListView);
		}
		
		mListView.addHeaderView(initHeadView());
		mSecondHeadView = initHeadView(inflater);
		mListView.addHeaderView(mSecondHeadView);
		
		mListView.setOnItemClickListener(userItemClickListener);

		return mConvertView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		cursorAllPeopleCircleWithType();
		
		us = new PickCircleUserAdapter(mActivity, musers, mCircles, mAtoZ,  this);
		us.registerCheckClickActionListener(getClass().getName(), this);
		mListView.setAdapter(us);
		mHandler.obtainMessage(CHECK_SELECT_DATA).sendToTarget();
	}
	
	private View initHeadView(LayoutInflater inflater) {
	    View headview = inflater.inflate(R.layout.bpc_invite_people_head_ui, null, false);
	    View phoneInviteView = headview.findViewById(R.id.invite_by_phone_rl);
	    View emailInviteView = headview.findViewById(R.id.invite_by_email_rl);
	    phoneInviteView.setOnClickListener(phoneInviteClickListener);
	    emailInviteView.setOnClickListener(emailInviteClickListener);
	    return headview;
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
        QiupuORM.closeCursor(mCircles);
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
				break;
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
                } else if (id.contains("$")) {
					int index = id.indexOf("$");
					id = id.substring(index + 1, id.length());
					mSelectedCircle.add(Long.parseLong(id));
				}else {
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
//			}
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
				} else if (id.contains("$")) {
					int index = id.indexOf("$");
					id = id.substring(index + 1, id.length());
					selectedCircle.add(Long.parseLong(id));
				}else {
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
            if(mFilterIds != null) {
                musers = orm.querySimpleUsersWithoutIds(mFilterIds);
            }else {
                queryAllSimpleUser();
            }
        } else {
            if(mFilterIds != null) {
                musers = orm.querySimpleUsersWithoutIds(mFilterIds);
            }else {
                queryAllSimpleUser();
            }
//            mCircles = orm.queryAllCircleinfo(filtercirlceString, -1);
            QiupuORM.closeCursor(mCircles);
            mCircles = orm.queryLocalPublicCircleInfo(CircleUtils.getAllFilterCircleIds());
        }
    }

	private void loadUserFromServer() {
        IntentUtil.loadUsersFromServer(mActivity);
	}

    private void changeSelect(long bindid, boolean isSelected,
            boolean isUseritem) {
        Log.d(TAG, "changeSelect: " + bindid);
        if (isUseritem) {
            if (isSelected) {
                if (!mSelectedUser.contains(bindid)) {
                    mSelectedUser.add(bindid);
                    mSelectedMap.put(bindid, orm.getUserName(bindid));
                }
            } else {
                if (mSelectedUser.contains(bindid)) {
                    mSelectedUser.remove(bindid);
                    mSelectedMap.remove(bindid);
                }
            }
        } else {
            if (isSelected) {
                if (!mSelectedCircle.contains(bindid)) {
                    mSelectedCircle.add(bindid);
                    mSelectedCircleMap.put(bindid, orm.getCircleName(bindid));
                }
            } else {
                if (mSelectedCircle.contains(bindid)) {
                    mSelectedCircle.remove(bindid);
                    mSelectedCircleMap.remove(bindid);
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
	
    public void doSearch(String key) {
        if (!StringUtil.isEmpty(key)) {
            mListView.removeHeaderView(mSecondHeadView);
            if (null != searchUsers && !searchUsers.isClosed()) {
                searchUsers.close();
            }
            if (null != searchCircles && !searchCircles.isClosed()) {
                searchCircles.close();
            }

            searchUsers = orm.searchFriendsCursor(key);

            us.alterDataList(searchUsers, searchCircles, mSelectedUser,
                    mSelectedCircle, mAtoZ);
        } else {
            mListView.addHeaderView(mSecondHeadView);
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
	
	public interface CallBackInviteSysUsersFragmentListener {
		public void getInviteSysUsersFragment(InviteSysUsersFragment fragment);
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
        
        StringBuilder names = new StringBuilder();
        names.append(parseNames(mSelectedCircleMap));
        if (names.length() > 0) {
            names.append(",");
        }
        names.append(parseNames(mSelectedMap));
        return names.toString();
    }
    
    public String getselectValue() {
        StringBuilder ids = new StringBuilder();
        ids.append(parseLocalCircleUser(mSelectedCircle, true));
        if (ids.length() > 0) {
            ids.append(",");
        }
        ids.append(parseLocalCircleUser(mSelectedUser, false));
        return ids.toString();
    };
    
    private String parseNames(HashMap<Long, String> selectmap) {
        StringBuilder tmpString = new StringBuilder();
        if(selectmap.size() > 0) {
            Iterator iter = selectmap.entrySet().iterator();
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
    private String parseLocalCircleUser(HashSet<Long> set, boolean isgetCircles) {
        StringBuilder address = new StringBuilder();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            String id = String.valueOf(it.next());
            if (address.length() > 0) {
                address.append(',');
            }

            if (isgetCircles) {
            	if(QiupuConfig.isPublicCircleProfile(Long.parseLong(id))) {
					address.append("$" + id);
				}else {
					address.append("#" + id);
				}
            } else {
                address.append(id);
            }

        }
        return address.toString();
    }
    
    View.OnClickListener phoneInviteClickListener = new OnClickListener() {
        public void onClick(View v) {
            gotoStartInvitePeopleFragmentActivity(InvitePeopleFragmentActivity.TYPE_PHONE_INVITE);
        }
    };

    View.OnClickListener emailInviteClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            gotoStartInvitePeopleFragmentActivity(InvitePeopleFragmentActivity.TYPE_EMAIL_INVITE);
        }
    };
    
    private void gotoStartInvitePeopleFragmentActivity(int type) {
        Intent intent = new Intent(mActivity, InvitePeopleFragmentActivity.class);
        intent.putExtra(InvitePeopleFragmentActivity.TYPE_TAG, type);
        startActivity(intent);
    }
    
    @Override
    protected void invitePeopleCallback(ArrayList<Long> joinIds) {
        if(joinIds.size() > 0) {
            orm.updateLocalUserCircleInfo(joinIds, mCircleId, mCircleName);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //TODO update applyed user 
                }
            });
        }
        orm.updateInviteIds(parseLocalCircleUser(mSelectedUser, true), mCircleId);
    }

    @Override
    protected void doSearchEndCallBack(ArrayList<QiupuUser> userList) {
        if(isNeedRefreshUi) {
            us.alterDataArrayList(userList);
        }
    }
    
    @Override
    protected void begin() {
        showCenterProgress(true);
    }
    
    @Override
    protected void end() {
        showCenterProgress(false);
    }
}
