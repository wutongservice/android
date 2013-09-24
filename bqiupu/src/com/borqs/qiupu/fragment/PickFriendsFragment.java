package com.borqs.qiupu.fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import twitter4j.QiupuUser;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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
import com.borqs.qiupu.AddressPadMini;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.EmployeeColums;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.CircleColumns;
import com.borqs.qiupu.db.QiupuORM.UsersColumns;
import com.borqs.qiupu.service.FriendsManager;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.ui.bpc.PickAudienceActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;

public final class PickFriendsFragment extends PickAudienceBaseFragment implements
		AtoZ.MoveFilterListener, FriendsManager.FriendsServiceListener, CheckBoxClickActionListener{
	private static final String TAG = "PickFriendsFragment";

	private Activity mActivity;
//	private ListView mListView;
	private AtoZ mAtoZ;
	private PickCircleUserAdapter us;
	private Cursor mFrequentCircles;
	private Cursor mFrequentUsers;
	private Cursor mCircles;
	private Cursor mEvents;
	private Cursor mGroup;
	private Cursor searchUsers;
	private Cursor searchCircles;
	private Cursor searchEvents;
	private Cursor searchGroup;
	private Cursor searchFrequentCircles;
	private Cursor searchFrequentUsers;
	
	private Cursor mCompany;
	private Cursor mSearchCompany;
	
	private Handler mHandler;
	public final static String RECEIVER_TYPE = "RECEIVER_TYPE";
	public final static String FILTER_IDS = "FILTER_IDS";
	public static final int TYPE_USER = 1;
	public static final int TYPE_CIRCLE = 2;
	public static final int TYPE_CIRCLE_USER = 3;
	public static final int TYPE_EXCHANGE_USER = 4;
	private int mCurrentType = TYPE_CIRCLE_USER;
	private HashSet<Long> mSelectedUser = new HashSet<Long>();
    private HashSet<Long> mSelectedCircle = new HashSet<Long>();
    
	private String mFilterIds;
	private View mSearchHeadView;
	private View mAddressHeadView;
	private View mSecondHeadView;
	private CallBackPickFriendsFragmentListener mCallBackListener;
	private int mPickFrom;
	private long mId;
	private long mFromId;
	private long mScene;
	
	private final static int DEFAULT_FREQUENT_CIRCLE_COUNT = 4;
	private final static int DEFAULT_FREQUENT_USER_COUNT = 8; 
	
	private String sortby = EmployeeColums.NAME_PINYIN + " ASC ";
	
	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		mActivity = activity;
		try{
			mCallBackListener = (CallBackPickFriendsFragmentListener)activity;
			mCallBackListener.getPickFriendsFragment(this);
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
		mFilterIds = intent.getStringExtra(FILTER_IDS);
		mId = intent.getLongExtra(CircleUtils.CIRCLE_ID, -1);
		mFromId = intent.getLongExtra(CircleUtils.INTENT_FROM_ID, -1);
		mScene = intent.getLongExtra(CircleUtils.INTENT_SCENE, -1);
		mPickFrom = intent.getIntExtra(PickAudienceActivity.PICK_FROM, -1);
		mHandler = new MainHandler();
		AddressPadMini.registerNoteActionListener(getClass().getName(), this);
		mSelectedCircle = mCallBackListener.getSelectCircle();
		mSelectedUser = mCallBackListener.getSelectUser();
		FriendsManager.registerFriendsServiceListener(getClass().getName(), this);
		
		/*if(QiupuConfig.DBLOGD)*/Log.d(TAG, "mFromId : " + mFromId + " mSence: " + mScene);
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
		mSearchHeadView = initHeadView();
		mAddressHeadView = initHeadAddressView(inflater);
		mListView.addHeaderView(mSearchHeadView);
		mListView.addHeaderView(mAddressHeadView);
		
		// only create/invite from top circle, need show pick email/phone
		if(mPickFrom == PickAudienceActivity.PICK_FROM_CREATE_CIRCLE && mScene <=0) {
			mSecondHeadView = initHeadView(inflater);
			mListView.addHeaderView(mSecondHeadView);
		}else {
			Log.d(TAG, "no need invite contact" + mPickFrom);
		}
		
		mListView.setOnItemClickListener(userItemClickListener);

		return mConvertView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		cursorAllPeopleCircleWithType();
		mainSelectAdds = getselectCircleUserValue();
		us = new PickCircleUserAdapter(mActivity, musers, mCircles, mEvents, mGroup, mFrequentCircles, mFrequentUsers, mAtoZ,  this, mCompany);
		us.registerCheckClickActionListener(getClass().getName(), this);
		us.setSelectUser(mSelectedUser, mSelectedCircle);
		mListView.setAdapter(us);
		mHandler.obtainMessage(CHECK_SELECT_DATA).sendToTarget();
		setAddress();
	}
	
	private View initHeadView(LayoutInflater inflater) {
	    View headview = inflater.inflate(R.layout.bpc_invite_people_head_ui, null, false);
	    View phoneInviteView = headview.findViewById(R.id.invite_by_phone_rl);
	    View emailInviteView = headview.findViewById(R.id.invite_by_email_rl);
	    phoneInviteView.setOnClickListener(phoneInviteClickListener);
	    emailInviteView.setOnClickListener(emailInviteClickListener);
	    return headview;
	}
	
	public void onDestroy() {
		super.onDestroy();
		us.closeAllCursor();
		us.unRegisterCheckClickActionListener(getClass().getName());
		FriendsManager.unregisterFriendsServiceListener(getClass().getName());
		AddressPadMini.unregisterNoteActionListener(getClass().getName());
	};

	AdapterView.OnItemClickListener userItemClickListener = new FriendsItemClickListener();

	private class FriendsItemClickListener implements
			AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			if(CircleItemView.class.isInstance(view))
        	{
				CircleItemView cv = (CircleItemView) view;
				cv.switchCheck();
				changeSelect(cv.getDataItemId(), cv.getItemView().name, cv.isCircleSelected(), false);
        	}
        	else if(UserSelectItemView.class.isInstance(view))
        	{
        		UserSelectItemView uv = (UserSelectItemView)view;
        		uv.switchCheck();
        		changeSelect(uv.getUserID(), uv.getName(), uv.isSelected(), true);
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
//                    queryAllSimpleUser();
//						generateSelectData(receiveUserAdds, mSelectedUser, mSelectedCircle);
//					}
//					else if(type_delete_friends == mType)
//					{
//						musers = orm.queryFriendsCursorByCircleId(mCircleid);
//					}
//					else if(type_add_friends == mType)
//					{
//						musers = orm.queryUserNotInCircle(mCircleid);
//					}
                    
                    cursorAllPeopleCircleWithType();
                    us.alterDataList(musers, mCircles, mEvents, mGroup, mFrequentCircles, mFrequentUsers, mSelectedUser, mSelectedCircle,
                            mAtoZ, mCompany);
				}
                else if (statuscode == FriendsManager.STATUS_ITERATING) {
                	cursorAllPeopleCircleWithType();
                	us.alterDataList(musers, mCircles, mEvents, mGroup, mFrequentCircles, mFrequentUsers, mSelectedUser, mSelectedCircle,
                            mAtoZ, mCompany);
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

	private void checkData() {
//		if (type_all_circle_friends == mType) {
	    if (mCurrentType == TYPE_EXCHANGE_USER || mCurrentType == TYPE_USER) {
	        if (musers != null && musers.getCount() > 0) {
//                generateSelectData();
                us.setSelectUser(mSelectedUser, mSelectedCircle);
            }else {
                if (musers != null && musers.getCount() <= 0) {
                    loadUserFromServer();
                }
            }
	    } else {
			if (musers != null && musers.getCount() > 0 && mCircles != null && mCircles.getCount() > 0) {
//				generateSelectData(receiveUserAdds, mSelectedUser, mSelectedCircle);
				us.setSelectUser(mSelectedUser, mSelectedCircle);
			} else {
				if(mScene > 0) {
					if (musers != null && musers.getCount() <= 0) {
						QiupuService.loadCircleDirectoryFromServer(mActivity, mScene);
					}
				}else {
					if (musers != null && musers.getCount() <= 0) {
						loadUserFromServer();
					}
				}
				if (mCircles != null && mCircles.getCount() <= 0) {
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
            QiupuORM.closeCursor(musers);
            QiupuORM.closeCursor(mCircles);
            QiupuORM.closeCursor(mFrequentCircles);
            QiupuORM.closeCursor(mFrequentUsers);
            QiupuORM.closeCursor(mEvents);
            QiupuORM.closeCursor(mGroup);
            
            if(mPickFrom == PickAudienceActivity.PICK_FROM_COMPOSE) {
            	if(mScene > 0) {
//            		mCompany = orm.queryCirclesWithIds(String.valueOf(QiupuConfig.CIRCLE_ID_PUBLIC + "," + mSence));
            		musers = QiupuORM.queryCircleEmployeeWithPinyin(mActivity, mScene);
            		mFrequentUsers = QiupuORM.queryRefrerredEmployee(getActivity(), mScene, DEFAULT_FREQUENT_USER_COUNT);
//            		if(mFromId > 0 && mFromId != mSence) {
//            			mCircles = orm.queryCirclesWithIds(String.valueOf(mFromId));
//            		}else {
//            			mCircles = orm.queryChildCircles(mSence);
//            			mEvents = orm.queryCircleEventsCursor(mSence);
//            		}
            		if(mFromId <= 0) { // for share source from extra
            			mEvents = orm.queryEventForPick("", "");
            		}
            		
            	}else {
//            		if(mFilterIds != null) {
//                        musers = orm.querySimpleUsersWithoutIds(mFilterIds);
//                    }else {
//                        queryAllSimpleUser();
//                    }
                        if(mFromId > 0) {
                        	musers = QiupuORM.queryCircleEmployeeWithPinyin(mActivity, mFromId);
                        	mFrequentUsers = QiupuORM.queryRefrerredEmployee(getActivity(), mFromId, DEFAULT_FREQUENT_USER_COUNT);
//                        	mEvents = orm.queryCircleEventsCursor(mFromId);
//                        	mCircles = orm.queryCirclesWithIds(String.valueOf(QiupuConfig.CIRCLE_ID_PUBLIC + "," + mFromId));
                        }else {
                        	Log.d(TAG, "have no from id, don't know show what");
                        }
                         
//                        mFrequentCircles = orm.queryFrequentlyCircle(DEFAULT_FREQUENT_CIRCLE_COUNT, "");
//                		mFrequentUsers = QiupuORM.querySuggestionUser(null, null, DEFAULT_FREQUENT_USER_COUNT);
//                		mEvents = orm.queryEventOutofMonthinfo("", "");
//                		mGroup = orm.queryGroupInfo(CircleUtils.getFilterCircleIdsWithOutPublic(), "");
//                		mCircles = orm.queryCirclesEventOutofMonthinfo(CircleUtils.getFilterCircleIdsWithOutPublic(), -1);
//                		mCircles = orm.queryCirclesInfo(CircleUtils.getFilterCircleIdsWithOutPublic(), "");
            	}
//            	if(mFromId > 0) {
////            		mFrequentUsers = QiupuORM.querySuggestionUser(null, null, 8);
//            		mCircles = orm.queryCirclesWithIds(/*QiupuConfig.CIRCLE_ID_PUBLIC + "," + */String.valueOf(mFromId));
//            	}else {
//            		
//            	}
            }else if(mPickFrom == PickAudienceActivity.PICK_FROM_POLL 
            		|| mPickFrom == PickAudienceActivity.PICK_FROM_EVNET) {
            	if(mScene > 0) {
            		mCompany = orm.queryCirclesWithIds(String.valueOf(mScene));
            		if(TextUtils.isEmpty(mFilterIds)) {
            			musers = QiupuORM.queryCircleEmployeeWithPinyin(mActivity, mScene);
            			mCircles = orm.queryChildCircles(mScene);
            		}else {
            			musers = QiupuORM.queryCircleEmployeeWithPinyinFilter(mActivity, mScene, mFilterIds, "", sortby);
            			mCircles = orm.queryChildCirclesFilter(mScene, mFilterIds, "");
            		}
            	}else {
            		Log.d(TAG, "pick from create event/poll have no sence, do not know who you can pick.");
            	}
            }else if(mPickFrom == PickAudienceActivity.PICK_FROM_CREATE_CIRCLE) {
            	if(mScene > 0) {
            		if(mFilterIds != null) {
            			musers = QiupuORM.queryCircleEmployeeWithPinyinFilter(mActivity, mScene, mFilterIds, "", sortby);
            		}else {
            			musers = QiupuORM.queryCircleEmployeeWithPinyin(mActivity, mScene);
            		}
            	}else {
            		// scene is null. is create top circle
            		musers = orm.querySimpleUsersWithoutIds(mFilterIds);
//            		Log.d(TAG, "Pick from create circle have no sence, do not know who you can pick.");
            	}
            }else {
            	if(mScene > 0) {
            		if(mFilterIds != null) {
            			musers = QiupuORM.queryCircleEmployeeWithPinyinFilter(mActivity, mScene, mFilterIds, "", sortby);
            		}else {
            			musers = QiupuORM.queryCircleEmployeeWithPinyin(mActivity, mScene);
            		}
            		if(mFromId > 0 && mFromId != mScene) {
            			mCircles = orm.queryChildCirclesFilter(mScene, String.valueOf(mFromId), "");
            		}else {
            			mCircles = orm.queryChildCircles(mScene);
            		}
            		mEvents = orm.queryCircleEventsCursor(mScene);
            	}else {
            		musers = orm.querySimpleUsersWithoutIds(mFilterIds);
            		mFrequentCircles = orm.queryFrequentlyCircle(DEFAULT_FREQUENT_CIRCLE_COUNT, "");
            		mFrequentUsers = QiupuORM.querySuggestionUser(null, null, DEFAULT_FREQUENT_USER_COUNT);
            		mEvents = orm.queryEventOutofMonthinfo("", "");
            		mGroup = orm.queryGroupInfo(CircleUtils.getFilterCircleIdsWithOutPublic(), "");
            		if(mId > 0) {
            			mCircles = orm.queryCirclesInfo(CircleUtils.getAllFilterCircleIds() + "," + mId, "");
            		}else {
            			mCircles = orm.queryCirclesInfo(CircleUtils.getAllFilterCircleIds(), "");
            		}
            	}
            }
        }
    }

	private void loadUserFromServer() {
        IntentUtil.loadUsersFromServer(mActivity);
	}

    private void changeSelect(long bindid, String selectName, boolean isSelected,
            boolean isUseritem) {
        Log.d(TAG, "changeSelect: " + bindid + " " + selectName);
        if (isUseritem) {
            if (isSelected) {
                if (!mSelectedUser.contains(bindid)) {
                    mSelectedUser.add(bindid);
                    mSelectedUserCircleNameMap.put(bindid, selectName);
                }
            } else {
                if (mSelectedUser.contains(bindid)) {
                    mSelectedUser.remove(bindid);
                    mSelectedUserCircleNameMap.remove(bindid);
                }
            }
        } else {
            if (isSelected) {
                if (!mSelectedCircle.contains(bindid)) {
                    mSelectedCircle.add(bindid);
                    mSelectedUserCircleNameMap.put(bindid, selectName);
                }
            } else {
                if (mSelectedCircle.contains(bindid)) {
                    mSelectedCircle.remove(bindid);
                    mSelectedUserCircleNameMap.remove(bindid);
                }
            }
        }

        us.setSelectmap(mSelectedUser, mSelectedCircle);
        mainSelectAdds = getselectCircleUserValue();
		setAddress();
    }
	
	public HashSet<Long> getLocalSelectuser(){
		return mSelectedUser;
	}
	public HashSet<Long> getLocalSelectCircle(){
		return mSelectedCircle;
	}
	
    public void doSearch(String key) {
        if (!StringUtil.isEmpty(key)) {
        	if(mSecondHeadView != null) {
        		mListView.removeHeaderView(mSecondHeadView);
        	}
            if (null != searchUsers && !searchUsers.isClosed()) {
                searchUsers.close();
            }
            if (null != searchCircles && !searchCircles.isClosed()) {
                searchCircles.close();
            }

            if(mPickFrom == PickAudienceActivity.PICK_FROM_COMPOSE) {
            	if(mScene > 0) {
            		searchUsers = QiupuORM.searchCircleEmployeeWithPinyin(mActivity, mScene, key);
//            		if(mFromId > 0 && mFromId != mSence) {
//            			searchCircles = orm.searchCirclesWithIds(String.valueOf(mFromId), key);
//            		}else {
//            			searchCircles = orm.searchChildCircles(mSence, key);
//            			searchEvents = orm.searchCircleEventsCursor(mSence, key);
//            		}
            	}else {
            		if(mFromId > 0) {
            			searchUsers = QiupuORM.searchCircleEmployeeWithPinyin(mActivity, mFromId, key);
//            			searchEvents = orm.searchCircleEventsCursor(mFromId, key);
//            			searchCircles = orm.searchCirclesWithIds(String.valueOf(mFromId), key);
            		}else {
            			Log.d(TAG, "have no from id, don't know show what");
            		}
            	}
            }else if(mPickFrom == PickAudienceActivity.PICK_FROM_POLL 
            		|| mPickFrom == PickAudienceActivity.PICK_FROM_EVNET) {
            	if(mScene > 0) {
            		if(TextUtils.isEmpty(mFilterIds)) {
            			searchUsers = QiupuORM.searchCircleEmployeeWithPinyin(mActivity, mScene, key);
                		searchCircles = orm.searchChildCircles(mScene, key);
            		}else {
            			searchUsers = QiupuORM.queryCircleEmployeeWithPinyinFilter(mActivity, mScene, mFilterIds, key, sortby);
            			searchCircles = orm.queryChildCirclesFilter(mScene, mFilterIds, key);
            		}
            	}else {
            		Log.d(TAG, "pick from create event/poll have no sence, do not know who you can pick.");
            	}
            	
//            	searchUsers = orm.searchFriendsWithFilterIds(key, mFilterIds);
//            	if(mFromId > 0) {
//                	Log.d(TAG, "the from id > 0, no need search circle/event etc's info");
//                }else {
//                	searchFrequentCircles = orm.queryFrequentlyCircle(DEFAULT_FREQUENT_CIRCLE_COUNT, key);
//                	searchFrequentUsers = QiupuORM.querySuggestionUser(key, null, DEFAULT_FREQUENT_USER_COUNT);
//                	searchGroup = orm.queryGroupInfo(CircleUtils.getFilterCircleIdsWithOutPublic(), key);
//                	searchCircles = orm.queryCirclesInfo(CircleUtils.getFilterCircleIdsWithOutPublic(), key);
//                	searchEvents = orm.queryEventOutofMonthinfo("", key);
//                }
            }else if(mPickFrom == PickAudienceActivity.PICK_FROM_CREATE_CIRCLE) {
            	if(mScene > 0) {
            		if(TextUtils.isEmpty(mFilterIds)) {
            			searchUsers = QiupuORM.searchCircleEmployeeWithPinyin(mActivity, mScene, key);
            		}else {
            			searchUsers = QiupuORM.queryCircleEmployeeWithPinyinFilter(mActivity, mScene, mFilterIds, key, sortby);
            		}
            	}else {
            		searchUsers = orm.searchFriendsWithFilterIds(key, mFilterIds);
            		Log.d(TAG, "Pick from create circle have no sence, do not know who you can pick.");
            	}
            } else {
            	if(mScene > 0) {
            		if(mFilterIds != null) {
            			searchUsers = QiupuORM.queryCircleEmployeeWithPinyinFilter(mActivity, mScene, mFilterIds, key, sortby);
            		}else {
            			searchUsers = QiupuORM.searchCircleEmployeeWithPinyin(mActivity, mScene, key);
            		}
            		if(mFromId > 0 && mFromId != mScene) {
            			searchCircles = orm.queryChildCirclesFilter(mScene, String.valueOf(mFromId), key);
            		}else {
            			searchCircles = orm.searchChildCircles(mScene, key);
            		}
            		searchEvents = orm.searchCircleEventsCursor(mFromId, key);
            	}else {
            		searchUsers = orm.searchFriendsWithFilterIds(key, mFilterIds);
            		searchFrequentCircles = orm.queryFrequentlyCircle(DEFAULT_FREQUENT_CIRCLE_COUNT, key);
                	searchFrequentUsers = QiupuORM.querySuggestionUser(key, null, DEFAULT_FREQUENT_USER_COUNT);
                	searchGroup = orm.queryGroupInfo(CircleUtils.getFilterCircleIdsWithOutPublic(), key);
                	searchCircles = orm.queryCirclesInfo(CircleUtils.getFilterCircleIdsWithOutPublic(), key);
                	searchEvents = orm.queryEventOutofMonthinfo("", key);
            	}
            }
            
            us.alterDataList(searchUsers, searchCircles, searchEvents, searchGroup, searchFrequentCircles, searchFrequentUsers, mSelectedUser,
                    mSelectedCircle, mAtoZ, mCompany);
        } else {
        	if(mSecondHeadView != null) {
        		if(mListView.getHeaderViewsCount() > 0) {
        			mListView.removeHeaderView(mSearchHeadView);
        			mListView.removeHeaderView(mSecondHeadView);
        			mListView.removeHeaderView(mAddressHeadView);
        		}
        		
        		mListView.setAdapter(null);
        		
        		mListView.addHeaderView(mSearchHeadView);
        		mListView.addHeaderView(mAddressHeadView);
        		mListView.addHeaderView(mSecondHeadView);
        		mListView.setAdapter(us);
        	}
            cursorAllPeopleCircleWithType();
            us.alterDataList(musers, mCircles, mEvents, mGroup, mFrequentCircles, mFrequentUsers, mSelectedUser, mSelectedCircle,
                    mAtoZ, mCompany);
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
		changeSelect(itemId, itemLabel, isSelect, isuser);
	}
	
	public interface CallBackPickFriendsFragmentListener {
		public void getPickFriendsFragment(PickFriendsFragment fragment);
		public void gotoPickContactFragment(int type);
		public HashSet<Long> getSelectUser();
		public HashSet<Long> getSelectCircle();
	}

    public void setFromType(boolean from_exchange) {
        if(from_exchange) {
            mCurrentType = TYPE_EXCHANGE_USER;
        }
    }
    
    public void setType(int type) {
        mCurrentType = type;
    }
    
    View.OnClickListener phoneInviteClickListener = new OnClickListener() {
        public void onClick(View v) {
        	if(mCallBackListener != null) {
        		mCallBackListener.gotoPickContactFragment(PICK_TYPE_PHONE);
        	}
        }
    };

    View.OnClickListener emailInviteClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        	if(mCallBackListener != null) {
        		mCallBackListener.gotoPickContactFragment(PICK_TYPE_EMAIL);
        	}
        }
    };
    
    @Override
    protected void doSearchEndCallBack(ArrayList<QiupuUser> userList) {
        if(isNeedRefreshUi) {
        	isSearchMode = true;
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
    
    @Override
	public void noteRemove(String notStr) {
		if(QiupuConfig.DBLOGD)Log.d(TAG, "noteRemove, notStr: " + notStr);
		if(notStr != null) {
			if(notStr.contains("*")) {
				return;
			}
			
			if (notStr.contains("#")) {
				int index = notStr.indexOf("#");
				notStr = notStr.substring(index + 1, notStr.length());
				mSelectedCircle.remove(Long.parseLong(notStr));
				mSelectedUserCircleNameMap.remove(Long.parseLong(notStr));
				us.setSelectmap(mSelectedUser, mSelectedCircle);
				
				final String tmpStr = notStr;
				mHandler.post( new Runnable() {
					public void run() {
						for(int i= 0; i < mListView.getChildCount(); i++) {
							View v = mListView.getChildAt(i);
							if(CircleItemView.class.isInstance(v)) {
								CircleItemView fv = (CircleItemView)v;
								if(fv.refreshCheckBox(Long.parseLong(tmpStr))){
									break;
								}
							}
						}
					}
				});
			} else {
				mSelectedUser.remove(Long.parseLong(notStr));
				mSelectedUserCircleNameMap.remove(Long.parseLong(notStr));
				us.setSelectmap(mSelectedUser, mSelectedCircle);
				
				final String tmpStr = notStr;
				mHandler.post( new Runnable() {
					public void run() {
						for(int i= 0; i < mListView.getChildCount(); i++) {
							View v = mListView.getChildAt(i);
							if(UserSelectItemView.class.isInstance(v)) {
								UserSelectItemView fv = (UserSelectItemView)v;
								if(fv.refreshCheckBox(Long.parseLong(tmpStr))){
									break;
								}
							}
						}
					}
				});
			}
			mainSelectAdds = getselectCircleUserValue();
			setAddress();
		}
	}
    
    public boolean isBackSearch() {
//    	if(isSearchMode) {
    		if(mInputEditText != null && mInputEditText.getText().length() > 0) {
    			mInputEditText.setText("");
    			return true;
    		}
//    	}
    	return false;
    }
    
    public String getselectCircleUserValue() {
        StringBuilder ids = new StringBuilder();
        ids.append(parseLocalCircleUser(mSelectedCircle, true));
        if (ids.length() > 0) {
            ids.append(",");
        }
        ids.append(parseLocalCircleUser(mSelectedUser, false));
        return ids.toString();
    };
    
    private String parseLocalCircleUser(HashSet<Long> set, boolean isgetCircles) {
        StringBuilder address = new StringBuilder();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            String id = String.valueOf(it.next());
            if (address.length() > 0) {
                address.append(',');
            }

            if (isgetCircles) {
//            	if(QiupuConfig.isPublicCircleProfile(Long.parseLong(id))) {
//					address.append("$" + id);
//				}else {
					address.append("#" + id);
//				}
            } else {
                address.append(id);
            }
        }
        return address.toString();
    }
}
