
package com.borqs.qiupu.ui.bpc;

import java.util.HashMap;
import java.util.HashSet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.InformationAdapter;
import com.borqs.common.adapter.InformationAdapter.MoreItemCheckListener;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.util.UserTask;
import com.borqs.common.view.InformationItemView;
import com.borqs.information.InformationBase;
import com.borqs.information.InformationDownloadService;
import com.borqs.information.db.Notification.NotificationColumns;
import com.borqs.information.db.NotificationOperator;
import com.borqs.information.util.InformationConstant;
import com.borqs.information.util.InformationReadCache;
import com.borqs.information.util.InformationUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.StringUtil;

public class InformationListActivity extends BasicActivity implements MoreItemCheckListener{
	private static final String TAG = "InformationListActivity";
	
	private ListView informationListView;
	private InformationAdapter mAdapter;
	
	private NotificationOperator mOperator;
	private HashMap<Long, Boolean> readStatus;
	private DownloadServiceStatusReceiver downloadServiceReceiver;
	private IntentFilter serviceReceiverFilter;
	private long lastModified = 0;
	private HashSet<Long> processedIds = null;
	
	private Cursor mNtfCursor;
	private boolean mIsToMe;
	private static final String FROM_TIME = "from_Time";
	
	private boolean mHasMore = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.notification_list_view);
		showRightActionBtn(false);
		informationListView = (ListView) findViewById(R.id.information_list);
		mOperator = new NotificationOperator(this);
		readStatus = new HashMap<Long, Boolean>();
		
		mIsToMe = getIntent().getBooleanExtra(InformationConstant.NOTIFICATION_INTENT_PARAM_ISTOME, false);
//			if(mOperator.getUnProcessedNotification() >= InformationConstant.DEFAULT_NOTIFICATION_COUNT) {
//				mHasMore = true;
//			}
		mAdapter = new InformationAdapter(this, this);
		informationListView.setAdapter(mAdapter);
		informationListView.setOnItemClickListener(infomationItemClickListenter);
		
		downloadServiceReceiver = new DownloadServiceStatusReceiver();
		serviceReceiverFilter = new IntentFilter(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_STATUS_ACTION);
		InformationUtils.cancelScheduledInforGet();
		
		if(mIsToMe) {
			setHeadTitle(R.string.ntf_label_tome);
			mNtfCursor = mOperator.loadNtfToMe("");
		}else {
			setHeadTitle(R.string.ntf_label_other);
			mNtfCursor = mOperator.loadNtfWithOutToMe("");
		}
		long from_time = 0;
		if(mNtfCursor != null && mNtfCursor.getCount() > 0) {
			mHasMore = true;
			int ntfcount = mNtfCursor.getCount();
			if(mNtfCursor.moveToPosition(ntfcount -1)) {
				from_time = mNtfCursor.getLong(mNtfCursor.getColumnIndexOrThrow(NotificationColumns.LAST_MODIFY));
			}
			mAdapter.alterDataList(mNtfCursor);
			informationListView.setSelection(ntfcount);
		}
		Message msg = mHandler.obtainMessage(GET_INFORMATION_AUTO);
		msg.getData().putLong(FROM_TIME, from_time);
		msg.sendToTarget();
	}
	
	public boolean isShowNotification()
	{
		return false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		registerReceiver(downloadServiceReceiver, serviceReceiverFilter);
		if(mIsLoading) {
			mHandler.obtainMessage(VISIBLE_PROGRESS_BUTTON).sendToTarget();
		}
//		if(InformationDownloadService.isDownloadServiceRunning()) {
//			mHandler.obtainMessage(VISIBLE_PROGRESS_BUTTON).sendToTarget();
//		}else {
//			mHandler.obtainMessage(INVISIBLE_PROGRESS_BUTTON).sendToTarget();
//		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				lastModified = mOperator.getLastModifyDate();
				processedIds = mOperator.getProcessedItems();
			}
		});
		unregisterReceiver(downloadServiceReceiver);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		setReadAllMessageStatus();
		mAdapter.closeCursor();
		readStatus.clear();
		
		InformationDownloadService.clearListbytimeRunningSet();
	}
	
	private void setReadAllMessageStatus() {
		new Handler().postDelayed(new Runnable(){
			public void run()
			{
				setRead(-1);
			}
		}, 1500);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return false;
	}
	
	private void setRead(long id) {
		new BatchReadTask(id == -1).execute(id);
	}
	
	private class BatchReadTask extends UserTask<Long, Void, Void> {
		
		private boolean isAll;
		private boolean isToMe;
		private String mCacheUnReadIds;
		
		public BatchReadTask(boolean b) {
			this.isAll = b;
			mCacheUnReadIds = InformationReadCache.ReadStreamCache.getCacheUnReadNtfIds();
		}
		
		@Override
		public Void doInBackground(Long... params) {
			String ticket = AccountServiceUtils.getSessionID();
			if(!TextUtils.isEmpty(ticket)) {
				if(isAll) {
					String unReadIds = "";
					if(isToMe) {
						unReadIds = mOperator.loadUnReadNtfToMeString();
					}else {
						unReadIds = mOperator.loadunReadNtfOtherString();
					}
					if(QiupuConfig.DBLOGD)Log.d(TAG, "unread ids : " + unReadIds);
					//add filter with scene
					final String sceneId = QiupuORM.getSettingValue(InformationListActivity.this, QiupuORM.HOME_ACTIVITY_ID);
					mOperator.removeExcessntfWithType(mIsToMe, sceneId);  // only save <= 50 ntf.
					
					if(QiupuConfig.DBLOGD)Log.d(TAG, "unread ids : " + unReadIds + " " + mCacheUnReadIds);
					StringBuilder unreads = new StringBuilder();
					if(StringUtil.isValidString(unReadIds)) {
						unreads.append(unReadIds);
					}
					if(StringUtil.isValidString(mCacheUnReadIds)) {
						if(unreads.length() > 0) {
							unreads.append(",");
						}
						unreads.append(mCacheUnReadIds);
					}
					if(unreads.length() > 0) {
						boolean res = InformationUtils.setReadStatus(InformationListActivity.this, unreads.toString());
						if(res) {
							InformationReadCache.ReadStreamCache.removeNtfCacheWithIds(mCacheUnReadIds);
						}				
					}
					
//					if(StringUtil.isValidString(unReadIds)) {
//						boolean succeed = mOperator.updateAllReadStatus(true);
//					        if (succeed) {
//					            updateNotificationUI(0);
//					        }
//						InformationUtils.setReadStatus(InformationListActivity.this, unReadIds);
//					}
				} else {
					mOperator.updateReadStatus(params[0], true);
					InformationUtils.setReadStatus(InformationListActivity.this, String.valueOf(params[0]));
				}
			}
			return null;
		}
		
		@Override
		public void onPostExecute(Void result) {
			super.onPostExecute(result);
//				if(isAll) {
//	                reloadList();
//				}
//				mHandler.obtainMessage(REFRESH_TITLE).sendToTarget();
		}
		
	}
	
	public void refresh_Title(int difference) {
		int count = mOperator.getThisWeekUnReadCount() - difference;
		if(count > 0) {
			setHeadTitle(getText(R.string.message_center) + String.format(getText(R.string.notification_unread_count).toString(), count));
		}else {
			setHeadTitle(R.string.message_center);
		}
	}
	
	public void getInformation(boolean isAuto, long fromTime) {
//		if(!InformationDownloadService.isDownloadServiceRunning()) {
			Intent service = new Intent(this, InformationDownloadService.class);
			service.putExtra(InformationConstant.NOTIFICATION_DOWNLOAD_MODE, 
					isAuto ? InformationConstant.NOTIFICATION_DOWNLOAD_MODE_ATUO : InformationConstant.NOTIFICATION_DOWNLOAD_MODE_MANUAL);
			service.putExtra(InformationConstant.NOTIFICATION_REQUEST_PARAM_FROM, fromTime);
			service.putExtra(InformationConstant.NOTIFICATION_INTENT_PARAM_ISTOME, mIsToMe);
			service.putExtra(InformationConstant.NOTIFICATION_INTENT_SYNC_TYPE, mIsToMe ? InformationDownloadService.sync_listbytime_tome : InformationDownloadService.sync_listbytime_other);
//			service.putExtra(InformationConstant.NOTIFICATION_INTENT_PARAM_ISFromActivity, true);
			startService(service);  
//		}
	}
	
//		private boolean isLoading;
	private class DownloadServiceStatusReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "DownLoadServiceStatusReceiver: " + intent.getAction());
			if(intent.getAction().equals(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_STATUS_ACTION)) {
				int status = intent.getIntExtra(InformationConstant.NOTIFICATION_DOWNLOAD_STATUS, InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FINISHED);
				switch(status) {
				case InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_START:
					begin();
					mIsLoading = true;
					setMoreItem(true);
					break;
				case InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FINISHED:
					end();
					mIsLoading = false;
					setMoreItem(false);
					if(intent.getIntExtra(InformationConstant.NOTIFICATION_DOWNLOAD_MODE,
							InformationConstant.NOTIFICATION_DOWNLOAD_MODE_ATUO) == InformationConstant.NOTIFICATION_DOWNLOAD_MODE_MANUAL
							&& (intent.getIntExtra(InformationConstant.NOTIFICATION_DOWNLOADED_COUNT, 0) <= 0)) {
						showCustomToast(R.string.notification_none);
						mHasMore = false;
					} else {
						Log.d(TAG, "aaaaaaaaaaa" + intent.getIntExtra(InformationConstant.NOTIFICATION_DOWNLOADED_COUNT, 0));
						if(intent.getIntExtra(InformationConstant.NOTIFICATION_DOWNLOADED_COUNT, 0) < InformationConstant.DEFAULT_NOTIFICATION_COUNT) {
							mHasMore = false;
						}else {
							mHasMore = true;
						}
					}
					refreshUI();
					break;
				case InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FAILED:
					end();
					mIsLoading = false;
					setMoreItem(false);
					showCustomToast(R.string.notification_download_failed);
					break;
				default:
					return;
				}
			}
		}
	}
	
	private void refreshUI() {
		if(mAdapter == null || mOperator == null ) {
			return;
		}
		if(mIsToMe) {
			mNtfCursor = mOperator.loadNtfToMe("");
		}else {
			mNtfCursor = mOperator.loadNtfWithOutToMe("");
		}
		
		if(mNtfCursor != null && mNtfCursor.getCount() > 0) {
			mAdapter.alterDataList(mNtfCursor);
		}
	}
	
	@Override
	protected void createHandler() {
		mHandler = new InforHandler();
	}
	
	private static final int GET_INFORMATION_AUTO          = 1;
	private static final int GET_INFORMATION_MANUAL        = 2;
	private static final int VISIBLE_PROGRESS_BUTTON       = 3;
	private static final int INVISIBLE_PROGRESS_BUTTON     = 4;
	
	class InforHandler extends Handler {
		public void handleMessage(Message msg) {
			int what = msg.what;
			this.removeMessages(what);
			switch(what) {
			case GET_INFORMATION_AUTO:
				getInformation(true, msg.getData().getLong(FROM_TIME));
				break;
			case GET_INFORMATION_MANUAL:
				getInformation(false, msg.getData().getLong(FROM_TIME));
				break;
			case VISIBLE_PROGRESS_BUTTON:
				showProgressBtn(true);
				break;
			case INVISIBLE_PROGRESS_BUTTON:
				showProgressBtn(false);
				break;
			}
		}
	}
	
	View.OnClickListener loadOldInformationListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			loadOlderINformation();
		}
	};
	
	protected void loadOlderINformation() {
		long time = -1;
		if(mNtfCursor != null && mNtfCursor.getCount() > 0) {
			if(mNtfCursor.moveToPosition(mNtfCursor.getCount() -1)) {
				time = mNtfCursor.getLong(mNtfCursor.getColumnIndexOrThrow(NotificationColumns.LAST_MODIFY));
			}
		}

		if(time > 0) {
			Log.d(TAG, "earliest modify time is: " + time);
			Message msg = mHandler.obtainMessage(GET_INFORMATION_MANUAL);
			msg.getData().putLong(FROM_TIME, time);
			msg.sendToTarget();
		}else {
			Log.e(TAG, "load older information failed: time <= 0 ");
		}
	}
	
	@Override
	protected void loadRefresh() {
		Message msg = mHandler.obtainMessage(GET_INFORMATION_AUTO);
		msg.getData().putLong(FROM_TIME, 0);
		msg.sendToTarget();
	}
	
	private boolean mIsLoading;
	@Override
	protected void uiLoadBegin() {
		super.uiLoadBegin();
		mIsLoading = true;
	}
	
	@Override
	protected void uiLoadEnd() {
		super.uiLoadEnd();
		mIsLoading = false;
	}
	
	
	@Override
	public boolean isMoreItemHidden() {
		return !mHasMore;
	}
	
	@Override
	public OnClickListener getMoreItemClickListener() {
		return loadOldInformationListener;
	}
	
	@Override
	public int getMoreItemCaptionId() {
		return mIsLoading ? R.string.loading : R.string.list_view_more;
	}
	
	OnItemClickListener infomationItemClickListenter = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(InformationItemView.class.isInstance(view)) {
				InformationItemView informationview = (InformationItemView) view;
				InformationBase infor = informationview.getItem();
				if(infor != null) {
					if(!infor.read) {
						informationview.reverContentText(infor.read);
						mOperator.updateReadStatus(infor.id, true);
						InformationReadCache.ReadStreamCache.cacheUnReadNtfIdsWithoutDb(infor.id);
					}
				}
				forwardInformation(informationview.getItem());
			}
		}
	};
	
	private void forwardInformation(InformationBase msg) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		if (TextUtils.isEmpty(msg.uri)) {
			return;
		} else {
			intent.setData(Uri.parse(msg.uri));
		}

		if (BpcApiUtils.isActivityReadyForIntent(this, intent)) {
			intent.putExtra("MSG_ID", msg.id);
			intent.putExtra("DATA", msg.data);
			intent.putExtra("SENDER_ID", msg.senderId);
			intent.putExtra("WHEN", msg.lastModified);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
		}
	}
	
	private void setMoreItem(boolean loading) {
        //set load older button text process for UI
        if(informationListView != null) {
            for(int i=informationListView.getChildCount()-1;i>0;i--) {
                View v = informationListView.getChildAt(i);
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
	
}