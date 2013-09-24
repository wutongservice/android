package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.borqs.qiupu.R;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;

public class CheckTopCircleTranActivity extends BasicActivity {

	private static final String TAG = CheckTopCircleTranActivity.class.getSimpleName();
	
	private ProgressDialog mprogressDialog;
	private TextView mToast_view;
	@Override
	protected void createHandler() {
		mHandler = new MainHandler();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bpc_check_top_circle_main);
		mToast_view = (TextView) findViewById(R.id.toast_view);
		mToast_view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadRefresh();
			}
		});
		mHandler.obtainMessage(GET_USER_TOP_CIRCLE).sendToTarget();
	}
	
	
	@Override
	protected void loadRefresh() {
		mHandler.obtainMessage(GET_USER_TOP_CIRCLE).sendToTarget();
	}
	
	private final static int GET_USER_TOP_CIRCLE = 101;
	private final static int GET_USER_TOP_CIRCLE_END = 102;
	private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case GET_USER_TOP_CIRCLE: {
            	syncTopCircle();
            	break;
            }
            case GET_USER_TOP_CIRCLE_END: {
            	try {
                    mprogressDialog.dismiss();
                    mprogressDialog = null;
                } catch (Exception e) { }
            	if(msg.getData().getBoolean(RESULT)) {
            		String name = msg.getData().getString(CircleUtils.CIRCLE_NAME);
            		long circleid = msg.getData().getLong(CircleUtils.CIRCLE_ID);
            		QiupuORM.addSetting(CheckTopCircleTranActivity.this, QiupuORM.HOME_ACTIVITY_ID, String.valueOf(circleid));
        			UserCircle uc = orm.queryOneCircle(QiupuConfig.USER_ID_ALL, circleid);
        			QiupuApplication.mTopOrganizationId = uc;
            		IntentUtil.gotoOrganisationHome(CheckTopCircleTranActivity.this, name, circleid);
//                    IntentUtil.loadCircleDirectoryFromServer(CheckTopCircleTranActivity.this, circleid);
                    finish();
            	}else {
            		//show error ui
            		ToastUtil.showOperationFailed(CheckTopCircleTranActivity.this, mHandler, true);
            		mToast_view.setVisibility(View.VISIBLE);
            		mToast_view.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.base_empty_view), null, null);
            		mToast_view.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							loadRefresh();
						}
					});
            	}
            	break;
            }
            }
        }
    }
	
	boolean isLoading = false;
    Object mLockLoad = new Object();
    private void syncTopCircle() {
    	
    	if(!DataConnectionUtils.testValidConnection(this)) {
    		ToastUtil.showCustomToast(this, R.string.dlg_msg_no_active_connectivity, mHandler);
    		mToast_view.setVisibility(View.VISIBLE);
    		mToast_view.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.base_empty_view), null, null);
    		return ;	
    	}
    	
        synchronized (mLockLoad) {
            if (isLoading == true) {
                ToastUtil.showShortToast(this, mHandler, R.string.string_in_processing);
                return;
            }
        }
        synchronized (mLockLoad) {
        	isLoading = true;
        }
        
        showProcessDialog(R.string.sync_default_top_circle, false, true, true);
        mToast_view.setVisibility(View.GONE);
        asyncQiupu.syncTopCircle(AccountServiceUtils.getSessionID(), new TwitterAdapter() {
            public void syncTopCircle(ArrayList<UserCircle> circles) {
                Log.d(TAG, "finish syncTopCircle=" + circles.size());
                
                if (circles.size() > 0) {
                    orm.removeAllCirclesWithOutNativeCircles();
                    orm.insertCircleList(circles, AccountServiceUtils.getBorqsAccountID());
                }
                
                long tmpid = 0 ;
                String tmpName = "";
                
                if(circles != null) {
                	for(int i=0; i<circles.size(); i++) {
                		UserCircle tmpCircle = circles.get(i);
                		if(i == 0) {
                			tmpid = tmpCircle.circleid;
                			tmpName = tmpCircle.name;
                		}
                		if(tmpCircle.circleid == CircleUtils.BORQS_CIRCLE_ID) {
                			tmpid = tmpCircle.circleid;
                			tmpName = tmpCircle.name;
                			break;
                		}
                	}
                }
                
                synchronized (mLockLoad) {
                	isLoading = false;
                }
                
                Message msg = mHandler.obtainMessage(GET_USER_TOP_CIRCLE_END);
                msg.getData().putBoolean(RESULT, true);
                msg.getData().putLong(CircleUtils.CIRCLE_ID, tmpid);
                msg.getData().putString(CircleUtils.CIRCLE_NAME, tmpName);
                msg.sendToTarget();
            }
            
            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockLoad) {
                	isLoading = false;
                }
                Message msg = mHandler.obtainMessage(GET_USER_TOP_CIRCLE_END);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }
    private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable) {
        mprogressDialog = DialogUtils.createProgressDialog(this,
                resId, CanceledOnTouchOutside, Indeterminate, cancelable);
        mprogressDialog.show();
    }	
	
}
