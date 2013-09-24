package com.borqs.qiupu.fragment;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import twitter4j.AsyncQiupu;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.CircleMainListAdapter;
import com.borqs.common.listener.ActivityFinishListner;
import com.borqs.common.listener.CircleActionListner;
import com.borqs.common.listener.RefreshCircleListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.CircleItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.bpc.BpcSearchActivity;
import com.borqs.qiupu.ui.bpc.CircleFragmentActivity;
import com.borqs.qiupu.util.ToastUtil;

public final class AllCirclesListFragment extends PeopleSearchableFragment implements CircleActionListner, RefreshCircleListener{
	private static final String TAG = "AllCirclesListFragment";
	private Activity mActivity;
	private QiupuORM orm ;
	private Handler mHandler;
	private AlertDialog mAlertDialog;
	protected AsyncQiupu asyncQiupu;
	private static final String RESULT = "result";
	private static final String ERRORMSG = "errormsg";
	private ProgressDialog mprogressDialog;
	private Cursor mOrgazitaionCircles;
	private Cursor mFreeCircles;

	private CircleMainListAdapter mCircleMainListAdapter;
	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach " );
		super.onAttach(activity);
		mActivity = activity;

		try{
			CallBackCircleListFragmentListener listener = (CallBackCircleListFragmentListener)activity;
			listener.getCircleListFragment(this);
		}catch (ClassCastException e) {
			Log.d(TAG, activity.toString() +  "must implement CallBackCircleListFragmentListener");
		}
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
    	
		mCircleMainListAdapter = new CircleMainListAdapter(mActivity);
		mListView.setAdapter(mCircleMainListAdapter);
		
		return convertView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mHandler.post(new Runnable() {
		    @Override
		    public void run() {
		        if(isLocalCircle) {
		    		Cursor localCircles = orm.queryLocalCircles();
		    		mCircleMainListAdapter.alterCircles(localCircles, null);
		    		if (localCircles != null && localCircles.getCount() <= 0) {
		    			mHandler.obtainMessage(GET_CIRCLE).sendToTarget();
		    		}
		    	}else {
		    		Cursor orgazitaionCircles = orm.queryOrganizationWithImage();
		    		Cursor freeCircles = orm.searchFreeCircleWithImage("", true);
		    		mCircleMainListAdapter.alterOrgizationCircles(orgazitaionCircles, freeCircles);
		    		
		    		if (orm.isOpenPublicCircle()) {
		    			if ((orgazitaionCircles != null && orgazitaionCircles.getCount() <= 0)) {
		    				mHandler.obtainMessage(GET_CIRCLE).sendToTarget();
		    			}
		    		}
		    	}
		    }
		});
	}
	
	@Override
	public void onResume() {
		QiupuHelper.registerRefreshCircleListener(getClass().getName(), this);
		QiupuHelper.registerCircleActionListener(getClass().getName(), this);
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		QiupuHelper.unregisterCircleActionListener(getClass().getName());
        QiupuHelper.unregisterUserListener(getClass().getName());
        mCircleMainListAdapter.clearCursor();
	}

    private void refreshCircleList() {
    	if(isLocalCircle) {
    		
    		Cursor localCircles = orm.queryLocalCircles();
    		mCircleMainListAdapter.alterCircles(localCircles, null);
//    		if (localCircles != null && localCircles.getCount() <= 0) {
//    			mHandler.obtainMessage(GET_CIRCLE).sendToTarget();
//    		}
    	}else {
    		if(mOrgazitaionCircles != null) {
    			mOrgazitaionCircles.close();
    		}
    		if(mFreeCircles != null) {
    			mFreeCircles.close();
    		}
    		mOrgazitaionCircles = orm.queryOrganizationWithImage();
    		mFreeCircles = orm.searchFreeCircleWithImage("", true);
    		mCircleMainListAdapter.alterOrgizationCircles(mOrgazitaionCircles, mFreeCircles);
    		
//    		if (orm.isOpenPublicCircle()) {
//    			if ((orgazitaionCircles != null && orgazitaionCircles.getCount() <= 0)) {
//    				mHandler.obtainMessage(GET_CIRCLE).sendToTarget();
//    			}
//    		}
    	}
    } 
    
    private boolean isLocalCircle = false;
    public void setLocalCirle(boolean value) {
    	isLocalCircle = value;
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
                if (ret) {
                    refreshCircleList();
//                    QiupuHelper.updateActivityUI(null);
                } else {
                    ToastUtil.showOperationFailed(mActivity, mHandler, true);
                }
                break;
            }
            
            }
        }
    }

    OnItemClickListener CircleListClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            if(CircleItemView.class.isInstance(view)) {
                CircleItemView item = (CircleItemView) view;
                UserCircle circle = item.getCircle();
                if(circle != null) {
                    if(UserCircle.CIRLCE_TYPE_PUBLIC == circle.type) {
                        Log.d(TAG, "click public circles " + circle.circleid);
                        finishOtherActivity();
                        QiupuORM.addSetting(mActivity, QiupuORM.HOME_ACTIVITY_ID, String.valueOf(circle.circleid));
                        IntentUtil.gotoOrganisationHome(mActivity, circle.name, circle.circleid);
                        getActivity().finish();
                        
//                        final String homeid = QiupuORM.getSettingValue(mActivity, QiupuORM.HOME_ACTIVITY_ID);
//                    	if(circle.circleid == Long.parseLong(homeid)) {
//                    		IntentUtil.gotoOrganisationHome(mActivity, circle.name, circle.circleid);
//                    	}else {
//                    		IntentUtil.startPublicCircleDetailIntent(mActivity, circle);
//                    	}
//                    }else if(UserCircle.CIRCLE_TYPE_LOCAL == circle.type) {
//                        IntentUtil.startCircleDetailIntent(mActivity, circle, false);
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
	
    public void setButtonEnable(boolean flag)
    {
    	mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(flag);
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

	boolean inDeleteCircle;
    Object mLockdeleteCircle = new Object();
    
    public void deleteCircleFromServer(final String circleid, final String circleName, final int type) {
    	if (inDeleteCircle == true) {
    		Toast.makeText(mActivity, R.string.string_in_processing,Toast.LENGTH_SHORT).show();
    		return ;
    	}
    	
    	synchronized (mLockdeleteCircle) {
    		inDeleteCircle = true;
    	}
    	showProcessDialog(R.string.delete_circle_process, false, true, true);
    	asyncQiupu.deleteCircle(AccountServiceUtils.getSessionID(), circleid, type, new TwitterAdapter() {
    		public void deleteCircle(boolean suc) {
    			Log.d(TAG, "finish deleteCircle=" + suc);
    			
    			if(suc)
    			{
    				//delete circle in DB
    				orm.deleteCircleByCricleId(AccountServiceUtils.getBorqsAccountID(), circleid);
    				//update user info
    				orm.updateUserInfoInCircle(AccountServiceUtils.getBorqsAccountID(), circleid, circleName);
    			}
    			Message msg = mHandler.obtainMessage(CIRCLE_DELETE_END);
    			msg.getData().putBoolean(RESULT, suc);
    			msg.sendToTarget();
    			
    			synchronized (mLockdeleteCircle) {
    				inDeleteCircle = false;
    			}
    		}
    		
    		public void onException(TwitterException ex,TwitterMethod method) {
    			synchronized (mLockdeleteCircle) {
    				inDeleteCircle = false;
    			}
    			
    			Message msg = mHandler.obtainMessage(CIRCLE_DELETE_END);
    			msg.getData().putBoolean(RESULT, true);
    			msg.sendToTarget();
    		}
    	});
    }
    
    private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable){
    	mprogressDialog = DialogUtils.createProgressDialog(mActivity, 
    			resId, CanceledOnTouchOutside, Indeterminate, cancelable);
    	mprogressDialog.show();    	
    }

    private AlertDialog mDeleteDialog;
	@Override
	public void deleteCircle(final UserCircle circle) {
		Log.d(TAG, "deleteCircle: " + circle.circleid + " circleName: " + circle.name);
		mDeleteDialog = new AlertDialog.Builder(mActivity)
        .setTitle(R.string.delete_circle_title)
        .setMessage(R.string.delete_circle_message)
        .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                
            	deleteCircleFromServer(String.valueOf(circle.circleid), circle.name, circle.type);
            	mDeleteDialog.dismiss();
            }
        })
        .setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        })
        .create();
		
		mDeleteDialog.show();
	}

	@Override
	public void changeSelect(long circleid, boolean selected, boolean isUseritem) {
		
	}
	
	public boolean getLoadStatus(){
		return inloadingCircle;
	}
	
	public interface CallBackCircleListFragmentListener {
		public void getCircleListFragment(AllCirclesListFragment fragment);
	}

	public void doSearch(String newText) {
		
		if(mOrgazitaionCircles != null) {
			mOrgazitaionCircles.close();
		}
		mOrgazitaionCircles = orm.searchOrganizationWithImage(newText);
		
		if(mFreeCircles != null) {
			mFreeCircles.close();
		}
		mFreeCircles = orm.searchFreeCircleWithImage(newText, true);
		mCircleMainListAdapter.alterOrgizationCircles(mOrgazitaionCircles, mFreeCircles);
		showSearchFromServerButton(newText.length() > 0 ? true : false, newText);
	}
	
	@Override
	protected void showSearchFromServerButton(boolean show, final String key,
			OnClickListener callback) {
		super.showSearchFromServerButton(show, key, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				IntentUtil.startSearchActivity(mActivity, key, BpcSearchActivity.SEARCH_TYPE_CIRCLE);
			}
		});
	}
	
    private void finishOtherActivity(){

		synchronized(QiupuHelper.finishListener)
        {
			Log.d(TAG, "finishListener.size() : " + QiupuHelper.finishListener.size());
            Set<String> set = QiupuHelper.finishListener.keySet();
            Iterator<String> it = set.iterator();
            while(it.hasNext()) {
                String key = it.next();
                ActivityFinishListner listener = QiupuHelper.finishListener.get(key).get();
                if(listener != null) {
                    listener.finishActivity();
                }
            }      
        }      
    }

	@Override
	public void refreshUi() {
		refreshCircleList();
	}
}
