package com.borqs.common.view;

import java.lang.reflect.Method;
import java.util.ArrayList;

import twitter4j.AsyncQiupu;
import twitter4j.Employee;
import twitter4j.PublicCircleRequestUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.TopSubCircleMemberAdapter;
import com.borqs.common.adapter.publicCircleRequestpeopleAdapter;
import com.borqs.common.adapter.publicCircleRequestpeopleAdapter.MoreItemCheckListener;
import com.borqs.common.listener.publicCirclePeopleActionListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.pullRefreshGridView.PullToRefreshBase.OnRefreshListener;
import com.borqs.common.view.pullRefreshGridView.PullToRefreshListView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.PublicRequestPeopleFragment;
import com.borqs.qiupu.service.FriendsManager;
import com.borqs.qiupu.service.FriendsManager.FriendsServiceListener;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class StreamRightMemberListViewUi implements MoreItemCheckListener,
		publicCirclePeopleActionListener, OnScrollListener, FriendsServiceListener {
	private final static String TAG = StreamRightMemberListViewUi.class.getName();
	private publicCircleRequestpeopleAdapter mFreeCircleMemberAdapter;
	private TopSubCircleMemberAdapter mTopSubCircleMemberAdapter;
//	private RefreshableListView mListView;
	private ListView mListView;
	private PullToRefreshListView mPullView;
	private Handler mHandler;
	protected String RESULT = "result";
	protected Context mContext;
	protected AsyncQiupu mAsyncQiupu;
	private UserCircle mCircle;
	private ArrayList<PublicCircleRequestUser> mPeopleList = new ArrayList<PublicCircleRequestUser>();
	private ArrayList<PublicCircleRequestUser> mSearchPeopleList = new ArrayList<PublicCircleRequestUser>();
	
	private Cursor mEmployeeCursor;
	private boolean isUserShowMore; 
	private boolean isShowSearchMore;
	private int mPage = 0;
    private int mCount = 100;
    
    private int mSearchpage = 0;
    private boolean mIsSearchMode;
    private String mSearchKey;
    
    private boolean mAlreadyLoad;
    private ProgressDialog mprogressDialog;

	public StreamRightMemberListViewUi() {
	}

	public void init(Context con, PullToRefreshListView listView, UserCircle circle) {
		mContext = con;
		mHandler = new MainHandler();
		mAsyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null, null);
		mPullView = listView;
		mListView = mPullView.getRefreshableView();
		
		mCircle = circle;
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(friendItemClickListener);
		
		mPullView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadRefresh();
			}
		});
		
		FriendsManager.registerFriendsServiceListener(getClass().getName()  + mCircle.circleid, this);
		generateAdapterWithCircle();
	}
	
	private void generateAdapterWithCircle() {
		if(mCircle == null) {
			Log.d(TAG, "circle is null");
			return ;
		}else {
			if((mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_top_formal)
					|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_sub_formal)
					|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free
					&& PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group))) {
				mTopSubCircleMemberAdapter = new TopSubCircleMemberAdapter(mContext, mCircle);
				mTopSubCircleMemberAdapter.registerActionListener(getClass().getName(), this);
				mListView.setAdapter(mTopSubCircleMemberAdapter);
				
				mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						mEmployeeCursor = QiupuORM.queryCircleEmployee(mContext, mCircle.circleid);
						if(mEmployeeCursor != null && mEmployeeCursor.getCount() > 0) {
							mTopSubCircleMemberAdapter.alterDataCursor(mEmployeeCursor);
						}else {
							QiupuService.loadCircleDirectoryFromServer(mContext, mCircle.circleid);
						}
					}
				}, 1000);
				
			}else if(mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free) {
				if(PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
					Log.d(TAG, "in the circle load people from service.");
				}else {
					mFreeCircleMemberAdapter = new publicCircleRequestpeopleAdapter(mContext,
							PublicCircleRequestUser.STATUS_IN_CIRCLE, mCircle, this);
					mFreeCircleMemberAdapter.registerActionListener(getClass().getName(), this);
					mListView.setAdapter(mFreeCircleMemberAdapter);
				}
			}else {
				Log.d(TAG, "initUI: have no circle type. don't know how to create view");
			}
		}
	}
	
	public void loadDataOnMove() {
		if((mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_top_formal)
				|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_sub_formal)
				|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free
				&& PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group))) {
			if(mCircle != null && mEmployeeCursor != null && mEmployeeCursor.getCount() <= 0 && !mAlreadyLoad) {
				QiupuService.loadCircleDirectoryFromServer(mContext, mCircle.circleid);
			}
			
		}else if(mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free) {
			if(!mAlreadyLoad) {
				if(PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
					Log.d(TAG, "in the circle load people from service.");
				}else {
					mPage = 0;
					Message msg = mHandler.obtainMessage(LOAD_REQUEST_PEOPLE);
					msg.getData().putInt("page", mPage);
					msg.getData().putInt("count", mCount);
					msg.sendToTarget();
				}
			}
			
		}else {
			Log.d(TAG, "initUI: have no circle type. don't know how to create view");
		}
	}

	private void loadRefresh() {
		if((mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_top_formal)
				|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_sub_formal)
				|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free
				&& PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group))) {
			if(mCircle != null) {
				QiupuService.loadCircleDirectoryFromServer(mContext, mCircle.circleid, true);
			}
			
		}else if(mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free) {
			if(PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
				Log.d(TAG, "in the circle load people from service.");
			}else {
				mPage = 0;
				Message msg = mHandler.obtainMessage(LOAD_REQUEST_PEOPLE);
				msg.getData().putInt("page", mPage);
				msg.getData().putInt("count", mCount);
				msg.sendToTarget();
			}
			
		}else {
			Log.d(TAG, "initUI: have no circle type. don't know how to create view");
		}
	}
	
	private static final int LOAD_REQUEST_PEOPLE = 101;
	private static final int LOAD_REQUEST_PEOPLE_END = 102;
	private static final int APPROVE_PEOPLE_END = 103;
	private static final int REMOVE_PEOPLE_END = 104;
	private static final int GRANT_PEOPLE_END = 105;
	private static final int REFHRE_INLINE_SEARCH_LIST = 106;
	private static final int SYNC_EMPLOYEE_CALLBACK = 109;

	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_REQUEST_PEOPLE: {
				syncRequestPeople(mCircle.circleid,
						PublicCircleRequestUser.STATUS_IN_CIRCLE, msg.getData()
								.getInt("page"), msg.getData().getInt("count"));
				break;
			}
			case LOAD_REQUEST_PEOPLE_END: {
				
				if(mPullView != null) {
					mPullView.onRefreshComplete();
				}
				setMoreItem(false);
				if (msg.getData().getBoolean(BasicActivity.RESULT)) {
					mAlreadyLoad = true;
					mFreeCircleMemberAdapter.alterDataList(mPeopleList);
				} else {
					ToastUtil.showOperationFailed(mContext, mHandler, true);
				}
				break;
			}
			case APPROVE_PEOPLE_END: {
				break;
			}
			case REMOVE_PEOPLE_END: {
				dismissProcessDialog();
				if (msg.getData().getBoolean(BasicActivity.RESULT)) {
					if(mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free 
							&& PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group) == false) {
                		mFreeCircleMemberAdapter.alterDataList(mPeopleList);
        			}else if((mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_top_formal)
        					|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_sub_formal)
        					|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free
        					&& PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group))) {
        				refreshEmployeeCallback();
        			}
				} else {
					Log.d(TAG, "remove member failed ");
				}
				break;
			}
			case GRANT_PEOPLE_END: {
				dismissProcessDialog();
				if (msg.getData().getBoolean(BasicActivity.RESULT)) {
					if(mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free
							&& PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group) == false) {
                		mFreeCircleMemberAdapter.alterDataList(mPeopleList);
        			}else if((mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_top_formal)
        					|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_sub_formal)
        					|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free
        					&& PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group))) {
        				refreshEmployeeCallback();
        			}
				} else {
					Log.d(TAG, "grant member failed ");
				}
				break;
			}
			case REFHRE_INLINE_SEARCH_LIST: {
				if(mFreeCircleMemberAdapter != null) {
					Log.d(TAG, "search people --- list: " + mSearchPeopleList.size());
					mFreeCircleMemberAdapter.alterDataList(mSearchPeopleList);
				}
				break;
			}
			 case SYNC_EMPLOYEE_CALLBACK: {
	            	if(msg.getData().getInt(FriendsManager.SYNC_TYPE) != FriendsManager.SYNC_TYPE_DIRECTORY) {
	            		return ;
	            	}
	                int statuscode = msg.getData().getInt("statuscode");
	                if (statuscode == FriendsManager.STATUS_DO_FAIL) {
	                    if(mPullView != null){
	                    	mPullView.onRefreshComplete();
	                    }
	                    ToastUtil.showOperationFailed(mContext, mHandler, true);
	                } else if (statuscode == FriendsManager.STATUS_DOING) {
	                	if(mPullView != null) mPullView.setRefreshing();
	                } else if (statuscode == FriendsManager.STATUS_DO_OK) {
	                	mAlreadyLoad = true;
	                	if(mPullView != null) {
	                		mPullView.onRefreshComplete();
	                	}
	                	refreshEmployeeCallback();
	                } else if (statuscode == FriendsManager.STATUS_ITERATING) {
	                	refreshEmployeeCallback();
	                }
	                break;
	            }
			 
			}
		}
	}

	private void refreshEmployeeCallback() {
		if(mCircle != null && mTopSubCircleMemberAdapter != null) {
			mTopSubCircleMemberAdapter.alterDataCursor(QiupuORM.queryCircleEmployee(mContext, mCircle.circleid));
		}
	}
	
	boolean inLoadingPeople = false;
	Object mLockLoadPeople = new Object();

	private void syncRequestPeople(final long circleid, final int status,
			final int page, final int count) {
		Log.d(TAG, "status :" + status);
		synchronized (mLockLoadPeople) {
			if (inLoadingPeople == true) {
				ToastUtil.showShortToast(mContext, mHandler,
						R.string.string_in_processing);
				callFailLoadUserMethod();
				return;
			}
		}
		synchronized (mLockLoadPeople) {
			inLoadingPeople = true;
		}

		if(mPullView != null) {
			mPullView.setRefreshing();
		}
		setMoreItem(inLoadingPeople);
		mAsyncQiupu.getRequestPeople(AccountServiceUtils.getSessionID(),
				circleid, status, page, count, new TwitterAdapter() {
					public void getRequestPeople(
							ArrayList<PublicCircleRequestUser> arraylist) {
						Log.d(TAG,
								"finish syncRequestPeople=" + arraylist.size());

						if (page == 0) {
							mergeFirstPageMember(arraylist);
						}else {
							mPeopleList.addAll(arraylist);
						}

						getUserInfoEndCallBack(arraylist);

						synchronized (mLockLoadPeople) {
							inLoadingPeople = false;
						}

						Message msg = mHandler
								.obtainMessage(LOAD_REQUEST_PEOPLE_END);
						msg.getData().putBoolean(BasicActivity.RESULT, true);
						msg.sendToTarget();
					}

					public void onException(TwitterException ex,
							TwitterMethod method) {
						synchronized (mLockLoadPeople) {
							inLoadingPeople = false;
						}
						callFailLoadUserMethod();
						Message msg = mHandler
								.obtainMessage(LOAD_REQUEST_PEOPLE_END);
						msg.getData().putString(BasicActivity.ERROR_MSG,
								ex.getMessage());
						msg.getData().putBoolean(BasicActivity.RESULT, false);
						msg.sendToTarget();
					}
				});
	}

	private Method failCallSyncUserMethod;

	private void SubUserPage() {
		Log.d(TAG, "resore the dpage--");
		if (mIsSearchMode) {
			mSearchpage--;
			if (mSearchpage < 0) {
				mSearchpage = 0;
			}

		} else {
			mPage--;
			if (mPage < 0)
				mPage = 0;
		}
	}

	private void callFailLoadUserMethod() {
		try {
			if (failCallSyncUserMethod != null) {
				failCallSyncUserMethod.invoke(this, (Object[]) null);
			}
		} catch (Exception ne) {
		}
	}

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

	@Override
	public void deleteMember(final String userIds) {
		DialogUtils.showConfirmDialog(mContext, R.string.delete_user_from_circle_title, R.string.public_circle_remove_member_msg,
                R.string.label_ok, R.string.label_cancel,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    deletePublicCirclePeople(mCircle.circleid, userIds);
                }
        }, null);
	}

	@Override
	public void approveMember(String userIds) { }

	@Override
	public void grantMember(String userIds, int role, String display_name) {
		Resources res = mContext.getResources();
		String dialogMessage = "";
        String tmpadminIds = "";
        String tmpmemberIds = "";
        if(PublicCircleRequestUser.ROLE_TYPE_MANAGER == role) {
            dialogMessage = String.format(res.getString(R.string.public_circle_grant_member_message), display_name);
            tmpmemberIds = userIds;
        }else if(PublicCircleRequestUser.ROLE_TYPE_MEMEBER == role) {
            dialogMessage = String.format(res.getString(R.string.public_circle_grant_manager_message), display_name);
            tmpadminIds = userIds;
        }
        final String adminIds = tmpadminIds;
        final String memberIds = tmpmemberIds;
        DialogUtils.showConfirmDialog(mContext, res.getString(R.string.public_circle_grant_title), dialogMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                grantpublicCirclePeople(mCircle.circleid, adminIds, memberIds);
            }
        });
	}

	@Override
	public void ignoreMembar(String userIds) { }
	
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
	
	private void getUserInfoEndCallBack(ArrayList<PublicCircleRequestUser> arraylist) {
        if (arraylist != null && arraylist.size() < mCount) {
            isUserShowMore = false;
        } else {
            isUserShowMore = true;
        }
    }

	private int followerItem = 0;
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		followerItem = firstVisibleItem + visibleItemCount;		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int state) {
		boolean forloadmore = (state == OnScrollListener.SCROLL_STATE_IDLE);
        loadOlderUsers(forloadmore, false);		
	}
	
	private void loadOlderUsers(boolean formore, boolean forceget) {
        int followercount = mPeopleList.size();
        Log.i(TAG, "followercount:" + followercount);
        if ((followerItem == followercount + 1 && formore) || forceget) {
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
//        	gotoSearchCirclePeople(mSearchKey);
        }else {
        	syncRequestPeople(mCircle.circleid, PublicCircleRequestUser.STATUS_IN_CIRCLE, mPage, mCount);
        }
    }
	
	private View.OnClickListener loadOlderClick = new View.OnClickListener() {
		public void onClick(View v) {
			Log.d(TAG, "load more user");
			getOldPeopleData();
		}
	};
	
	private void mergeFirstPageMember(ArrayList<PublicCircleRequestUser> arraylist) {
		for(int i=0; i<arraylist.size(); i++) {
			PublicCircleRequestUser tmpUser = arraylist.get(i);
			if(!mPeopleList.contains(tmpUser)) {
				mPeopleList.add(tmpUser);
			}
		}
	}

	@Override
	public void updateUI(int msgcode, Message message) {
		if (QiupuConfig.LOGD) Log.d(TAG, "msgcode: " + msgcode + " message: " + message);
        Message msg = mHandler.obtainMessage(SYNC_EMPLOYEE_CALLBACK);
        msg.getData().putInt("statuscode", msgcode);
        if(message != null) {
        	msg.getData().putInt(FriendsManager.SYNC_TYPE, message.getData().getInt(FriendsManager.SYNC_TYPE));
        }
        msg.sendToTarget();
	}
	
	private AdapterView.OnItemClickListener friendItemClickListener = new FriendsItemClickListener();
	
	private class FriendsItemClickListener implements
	AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free 
					&& PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group) == false) {
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
			}else if((mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_top_formal)
					|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_sub_formal)
					|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free
					&& PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group))) {
				if(TopSubMemberItemView.class.isInstance(view)) {
					TopSubMemberItemView tb = (TopSubMemberItemView) view;
					Employee tmpUser = tb.getUser();
					if (!TextUtils.isEmpty(tmpUser.user_id) && TextUtils.isDigitsOnly(tmpUser.user_id) 
							&& Long.parseLong(tmpUser.user_id) > 0) {
						IntentUtil.startUserDetailIntent(tb.getContext(),
								Long.parseLong(tmpUser.user_id), tmpUser.name, "");
					}else {
						Log.d(TAG, "the user id < 0, is not system user");
					}
				}
				
			}else {
				Log.d(TAG, "initUI: have no circle type. don't know how to create view");
			}
		}
	}

	public void doSearch(String newText) {
		if(mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free
				&& PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group) == false) {
			if(StringUtil.isEmpty(newText)) {
				mIsSearchMode = false;
				mFreeCircleMemberAdapter.alterDataList(mPeopleList);			
			}else {
				mSearchKey = newText;
				if(mIsTaskExecuting == false) {
					mIsSearchMode = true;
					new InlineSearchPeopleTask(newText).execute((Void[]) null);
					mIsTaskExecuting = true;
				}
			}
		}else if((mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_top_formal)
				|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_sub_formal)
				|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free
						&& PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group))) {
			if (mEmployeeCursor != null)
	    		mEmployeeCursor.close();
	    	
	    	mEmployeeCursor = QiupuORM.queryCircleEmployeeWithFilter(mContext, mCircle.circleid, newText);
	    	mTopSubCircleMemberAdapter.alterDataCursor(mEmployeeCursor);
	    	
//		}else if(mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_sub_formal) {
//			if (mEmployeeCursor != null)
//	    		mEmployeeCursor.close();
//	    	
//	    	mEmployeeCursor = QiupuORM.querySubEmployeeWithFilter(mContext, mCircle.circleid, mCircle.name, newText);
//	    	mTopSubCircleMemberAdapter.alterDataCursor(mEmployeeCursor);
		}else {
			Log.d(TAG, "initUI: have no circle type. don't know how to create view");
		}
	}
	
	private boolean mIsTaskExecuting;
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
//		mHandler.obtainMessage(REFHRE_INLINE_SEARCH_LIST).sendToTarget();
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
	
	boolean inDeletePeople;
    Object mLockDeletePeople = new Object();
    private void deletePublicCirclePeople(final long circleid, final String userids) {
        if (inDeletePeople == true) {
            Toast.makeText(mContext, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLockDeletePeople) {
            inDeletePeople = true;
        }
        showProcessDialog(R.string.set_user_process_title, false, true, true);
        
        mAsyncQiupu.deletePublicCirclePeople(AccountServiceUtils.getSessionID(), circleid, userids, null, new TwitterAdapter() {
            public void deletePublicCirclePeople(boolean result) {
                Log.d(TAG, "finish deletePublicCirclePeople=" + result);
                if(result) {
                	if(mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free) {
                		reconstructedList(CircleUtils.getCirlceOrUserIds(userids), mPeopleList);
        			}else if((mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_top_formal)
        					|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_sub_formal)) {
        				QiupuORM.removeEmployeeWithCircle(circleid, userids, mContext);
        			}
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
    
    private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable) {
        mprogressDialog = DialogUtils.createProgressDialog(mContext,
                resId, CanceledOnTouchOutside, Indeterminate, cancelable);
        mprogressDialog.show();
    }
    
    private void dismissProcessDialog() {
    	try {
            mprogressDialog.dismiss();
            mprogressDialog = null;
        } catch (Exception e) { }
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
    
    boolean inGrantPeople;
    Object mLockGrantPeople = new Object();
    private void grantpublicCirclePeople(final long circleid, final String adminIds, final String memberIds) {
        if (inGrantPeople == true) {
            Toast.makeText(mContext, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLockGrantPeople) {
            inGrantPeople = true;
        }
        showProcessDialog(R.string.set_user_process_title, false, true, true);
        
        mAsyncQiupu.grantPublicCirclePeople(AccountServiceUtils.getSessionID(), circleid, adminIds, memberIds, new TwitterAdapter() {
            public void grantPublicCirclePeople(boolean result) {
                Log.d(TAG, "finish grantPublicCirclePeople=" + result);
                if(result) {
                	if(mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_free) {
                		grantPublicCirclePeopleCallBack(adminIds, memberIds);
        			}else if((mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_top_formal)
        					|| (mCircle.mGroup != null && mCircle.mGroup.formal == UserCircle.circle_sub_formal)) {
        				QiupuORM.updateEmployeeRole(circleid, adminIds, memberIds, mContext);
        			}
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

	public void onDestory() {
		FriendsManager.unregisterFriendsServiceListener(getClass().getName()  + mCircle.circleid);		
	}
}


