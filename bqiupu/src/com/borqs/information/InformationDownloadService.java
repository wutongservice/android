package com.borqs.information;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.information.db.IOperator;
import com.borqs.information.db.NotificationOperator;
import com.borqs.information.util.InformationConstant;
import com.borqs.information.util.InformationUtils;
import com.borqs.qiupu.db.QiupuORM;

public class InformationDownloadService extends IntentService {  
	private static final String TAG = "InfomationDownloadService";
	private static volatile boolean isRunning = false;
	private Context mContext;
	private IOperator mOp;  
	
	private static final HashSet<Integer> runningSet = new HashSet<Integer>();
	public static int sync_topN_tome = 1;
	public static int sync_topN_other = 2;
	public static int sync_listbytime_tome = 3;
	public static int sync_listbytime_other = 4;
	
    public InformationDownloadService() {  
        super("InfomationDownloadService");  
    }  
    
  	@Override
	public void onCreate() {
		super.onCreate();
        mOp = new NotificationOperator(this); 
        mContext = this.getApplicationContext();
	}
  	
	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "onStart");
		//If downloading thread is already working, we refer to ignoring this request.
//		if (isRunning) {
//			Log.d(TAG, "the downloading thread is already working, we refer to ignoring this request.");
//			return;
//		} else {
			super.onStart(intent, startId);
//		}
	}

	@Override
	public void onDestroy() {
		runningSet.clear();
//		isRunning = false;
		super.onDestroy();
	}
	
	public static void clearListbytimeRunningSet() {
		runningSet.remove(sync_topN_tome);
		runningSet.remove(sync_listbytime_other);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent");
//		broadcastStatus(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_START, 0, 0);
////		int updateCount = downloadData();
		
		int sync_type = intent.getIntExtra(InformationConstant.NOTIFICATION_INTENT_SYNC_TYPE, -1);
		if(runningSet.contains(sync_type)) {
			Log.d(TAG, "in loading this sync type: " + sync_type);
			return;
		}
		
		long fromtime = intent.getLongExtra(InformationConstant.NOTIFICATION_REQUEST_PARAM_FROM, -1);
		boolean isToMe = intent.getBooleanExtra(InformationConstant.NOTIFICATION_INTENT_PARAM_ISTOME, false);
//		boolean isFromActivity = intent.getBooleanExtra(InformationConstant.NOTIFICATION_INTENT_PARAM_ISFromActivity, false);
		int updateCount = 0;
		if(sync_type == sync_listbytime_other || sync_type == sync_listbytime_tome) {
			updateCount = downloadDataWithTime(fromtime, isToMe);
			broadcastStatus(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FINISHED, 
					updateCount, updateCount == -1 ? 0 : intent.getIntExtra(InformationConstant.NOTIFICATION_DOWNLOAD_MODE, 
							InformationConstant.NOTIFICATION_DOWNLOAD_MODE_ATUO));
			
		}else if(sync_type == sync_topN_other || sync_type == sync_topN_tome) {
			updateCount = downloadDataTopN(isToMe);
//			if(updateCount > 0) {
				InformationUtils.updateNtfUI(isToMe, updateCount);
//			}
		}
//		if(fromtime > 0) {
//			updateCount = downloadDataWithTime(fromtime, isToMe);
//			broadcastStatus(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FINISHED, 
//					updateCount, updateCount == -1 ? 0 : intent.getIntExtra(InformationConstant.NOTIFICATION_DOWNLOAD_MODE, 
//							InformationConstant.NOTIFICATION_DOWNLOAD_MODE_ATUO));
//		}else {
//			if(isFromActivity) {
//				downloadDataWithTime(fromtime, isToMe);
//				broadcastStatus(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FINISHED, 
//						updateCount, updateCount == -1 ? 0 : intent.getIntExtra(InformationConstant.NOTIFICATION_DOWNLOAD_MODE, 
//								InformationConstant.NOTIFICATION_DOWNLOAD_MODE_ATUO));
//			}else {
//				updateCount = downloadDataTopN(isToMe);
//				if(updateCount > 0) {
//					InformationUtils.updateNtfUI(isToMe, updateCount);
//				}
//			}
//		}
//		final boolean hasNew = updateCount > 0;
////        InformationUtils.scheduleGetInfo(mContext, hasNew);
//		if (hasNew) {
//			fireDataChange(updateCount);
//		}
//		broadcastStatus(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FINISHED, 
//				updateCount, updateCount == -1 ? 0 : intent.getIntExtra(InformationConstant.NOTIFICATION_DOWNLOAD_MODE, 
//						InformationConstant.NOTIFICATION_DOWNLOAD_MODE_ATUO));
		
//		updateCount = downloadDataWithTimeAndType(fromtime, isToMe, InformationConstant.NOTIFICATION_INTENT_PARAM_UNREAD);
		
		
	}
	
	public static boolean isDownloadServiceRunning() {
		return isRunning;
	}
	
	private void broadcastStatus(int notifationDownloadStart, int updateCount, int mode) {
		Intent sender = new Intent();
        sender.setAction(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_STATUS_ACTION);
		sender.putExtra(InformationConstant.NOTIFICATION_DOWNLOAD_STATUS, notifationDownloadStart);
		sender.putExtra(InformationConstant.NOTIFICATION_DOWNLOAD_MODE, mode);
		sender.putExtra(InformationConstant.NOTIFICATION_DOWNLOADED_COUNT, updateCount);
		sendBroadcast(sender, null);
	}
	
	private void fireDataChange(int updateCount) {
		//no new, no show
		if(updateCount <= 0) {
			return;
		}
//		boolean isMainRunning = InformationUtils.isActivityOnTop(getApplicationContext(), "com.borqs.qiupu.ui.bpc.BpcInformationActivity");
        boolean isMainRunning = InformationUtils.isActivityOnTop(getApplicationContext());
		if (isMainRunning) {
			return;
		}
		int count = (new NotificationOperator(this)).getThisWeekUnReadCount();
		if(count <= 0) {
			return;
		}
		InformationUtils.showSysNotification(this, count);
//		String s = String.format(getText(R.string.new_notification_content).toString(), count);
//		Notification notification = new Notification(R.drawable.ic_bpc_launcher, s, System.currentTimeMillis());
//		notification.number = count;
//		notification.flags = Notification.FLAG_AUTO_CANCEL;
//		Intent mainActivityIntent = new Intent("com.borqs.bpc.action.NOTIFICATION");
//		mainActivityIntent.putExtra("isFromNotice", true);
//		mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, 0);
//		notification.setLatestEventInfo(this,getText(R.string.app_name), s, contentIntent);
//		NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//		mNotificationManager.notify(InformationHttpPushReceiver.HTTPPUSH, notification);
	}
    private int downloadData() {
        return downloadData(null);
    }
	private int downloadData(String appFilter) {
		try {
			isRunning = true;
			Log.d(TAG, "Start to download informations.");
			String ticket = AccountServiceUtils.getSessionID();
			String uid = String.valueOf(AccountServiceUtils.getBorqsAccountID());
			long lastDate = mOp.getLastModifyDate();
			Log.d(TAG, "date of the last information is " + lastDate);
			ArrayList<InformationBase> mDownload = InformationUtils.downloadMessage(mContext, ticket, uid, lastDate, false);
            // ugly code as notification server has encode the origin app ID and
            // pose its appID as "110".
            if (!TextUtils.isEmpty(appFilter)) {
                List<InformationBase> exclusion = new ArrayList<InformationBase>();
                for (InformationBase info : mDownload) {
                    if (!appFilter.equalsIgnoreCase(info.appId)) {
                        exclusion.add(info);
                    }
                }
                Log.d(TAG, "downloadData, excluding info of other app, count = " + exclusion.size());
                mDownload.removeAll(exclusion);
            }
			if (null != mDownload) {
				if(mDownload.size() > 0) {
					mOp.add(mDownload);
					return mDownload.size();
				}
			} else {
				broadcastStatus(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FAILED, 0, 0);
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Information download failed, logic exception.");
		} finally {
			isRunning = false;
			Log.d(TAG, "Download informations finished.");
		}
		return 0;
	}
	
	private int downloadDataWithTime(long fromTime, boolean isToMe) {
		try {
//			isRunning = true;
			Log.d(TAG, "Start to download informations.");
			String ticket = AccountServiceUtils.getSessionID();
			String uid = String.valueOf(AccountServiceUtils.getBorqsAccountID());
			
			if(ticket == null || uid == null || ticket.length() ==0 || uid.length() == 0)
			{
				Log.e(TAG, "No ticket in Phone, return directly.");
				broadcastStatus(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FAILED, 0, 0);
				return -1;
			}
			runningSet.add(isToMe ? sync_listbytime_tome : sync_listbytime_other);
//			long lastDate = mOp.getLastModifyDate();
//			Log.d(TAG, "date of the last information is " + lastDate);
			broadcastStatus(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_START, 0, 0);
			ArrayList<InformationBase> mDownload = InformationUtils.downloadMessage(mContext, ticket, uid, fromTime, isToMe);
            // ugly code as notification server has encode the origin app ID and
            // pose its appID as "110".
//            if (!TextUtils.isEmpty(appFilter)) {
//                List<InformationBase> exclusion = new ArrayList<InformationBase>();
//                for (InformationBase info : mDownload) {
//                    if (!appFilter.equalsIgnoreCase(info.appId)) {
//                        exclusion.add(info);
//                    }
//                }
//                Log.d(TAG, "downloadData, excluding info of other app, count = " + exclusion.size());
//                mDownload.removeAll(exclusion);
//            }
			if (null != mDownload) {
				if(mDownload.size() > 0) {
					mOp.add(mDownload, isToMe);
//				    return mOp.add(mDownload);
					return mDownload.size();
				}
			} else {
				broadcastStatus(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FAILED, 0, 0);
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Information download failed, logic exception.");
		} finally {
			runningSet.remove(isToMe ? sync_listbytime_tome : sync_listbytime_other);
//			isRunning = false;
			Log.d(TAG, "Download informations finished.");
		}
		return 0;
	}
	
	private int downloadDataTopN(boolean isToMe) {
		try {
//			isRunning = true;
			Log.d(TAG, "Start to download informations.");
			String ticket = AccountServiceUtils.getSessionID();
//			String uid = String.valueOf(AccountServiceUtils.getBorqsAccountID());
//			long lastDate = mOp.getLastModifyDate();
//			Log.d(TAG, "date of the last information is " + lastDate);
			runningSet.add(isToMe ? sync_topN_tome : sync_topN_other);
			
			final String sceneId = QiupuORM.getSettingValue(this, QiupuORM.HOME_ACTIVITY_ID);
			ArrayList<InformationBase> mDownload = InformationUtils.downloadMessageTopN(mContext, ticket, isToMe, sceneId);
            // ugly code as notification server has encode the origin app ID and
            // pose its appID as "110".
//            if (!TextUtils.isEmpty(appFilter)) {
//                List<InformationBase> exclusion = new ArrayList<InformationBase>();
//                for (InformationBase info : mDownload) {
//                    if (!appFilter.equalsIgnoreCase(info.appId)) {
//                        exclusion.add(info);
//                    }
//                }
//                Log.d(TAG, "downloadData, excluding info of other app, count = " + exclusion.size());
//                mDownload.removeAll(exclusion);
//            }
			if (null != mDownload) {
				if(mDownload.size() > 0) {
					return mOp.addTopNWithType(isToMe, mDownload, sceneId); 
//					return mDownload.size();
				}
			} else {
//				broadcastStatus(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FAILED, 0, 0);
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Information download failed, logic exception.");
		} finally {
			runningSet.remove(isToMe ? sync_topN_tome : sync_topN_other);
//			isRunning = false;
			Log.d(TAG, "Download informations finished.");
		}
		return 0;
	}
	
	private int downloadDataWithTimeAndType(long fromTime, boolean isToMe, int readstatus) {
		try {
			isRunning = true;
			Log.d(TAG, "Start to download informations.");
			String ticket = AccountServiceUtils.getSessionID();
			String uid = String.valueOf(AccountServiceUtils.getBorqsAccountID());
			
			if(ticket == null || uid == null || ticket.length() ==0 || uid.length() == 0)
			{
				Log.e(TAG, "No ticket in Phone, return directly.");
				broadcastStatus(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FAILED, 0, 0);
				return -1;
			}
//			long lastDate = mOp.getLastModifyDate();
//			Log.d(TAG, "date of the last information is " + lastDate);
			ArrayList<InformationBase> mDownload = InformationUtils.downloadMessageWithType(mContext, ticket, fromTime, isToMe, readstatus);
            // ugly code as notification server has encode the origin app ID and
            // pose its appID as "110".
//            if (!TextUtils.isEmpty(appFilter)) {
//                List<InformationBase> exclusion = new ArrayList<InformationBase>();
//                for (InformationBase info : mDownload) {
//                    if (!appFilter.equalsIgnoreCase(info.appId)) {
//                        exclusion.add(info);
//                    }
//                }
//                Log.d(TAG, "downloadData, excluding info of other app, count = " + exclusion.size());
//                mDownload.removeAll(exclusion);
//            }
			if (null != mDownload) {
				if(mDownload.size() > 0) {
				    return mOp.addUnReadWithType(isToMe, mDownload);
//					return mDownload.size();
				}
			} else {
//				broadcastStatus(InformationConstant.NOTIFICATION_DOWNLOAD_SERVICE_FAILED, 0, 0);
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Information download failed, logic exception.");
		} finally {
			isRunning = false;
			Log.d(TAG, "Download informations finished.");
		}
		return 0;
	}
}  