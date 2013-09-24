package com.borqs.sync.service;


import org.apache.http.HttpStatus;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.borqs.common.util.BLog;
import com.borqs.contacts_plus.R;
import com.borqs.sync.client.common.Logger;
import com.borqs.sync.client.common.SyncHelper;
import com.borqs.sync.ds.config.SyncProfile;
import com.borqs.syncml.ds.exception.DsException;
import com.borqs.syncml.ds.imp.common.ConnectivityUtil;
import com.borqs.syncml.ds.imp.engine.DsOperator;
import com.borqs.syncml.ds.imp.tag.AlertCode;
import com.borqs.syncml.ds.imp.tag.StatusValue;
import com.borqs.syncml.ds.protocol.IDatastore;
import com.borqs.syncml.ds.protocol.IProfile;
import com.borqs.syncml.ds.protocol.ISyncListener;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;

public class SyncMLService extends Service {
	private static final String TAG = "syncml";
	private static final int SYNC_MSG = 1;
	private static final int SYNC_START_SYNC = 2;
	private static final int SYNC_ALERT_MSG = 3;
	private static final int SYNC_BEGIN_MSG = 4;
	private static final int SYNC_END_MSG = 5;
	private static final int SYNC_MSG_UPDATE_ITEM_STATUS = 6;
	private static final int SYNC_MSG_PHASE = 7;
	private static final int SYNC_MSG_REGISTER = 8;
	
	private static final int SHOW_REGISTER_DIALOG = 1;

	private NotificationManager mNM;

	private RemoteCallbackList<ISyncMLCallBack> mCallbacks = new RemoteCallbackList<ISyncMLCallBack>();

	private IProfile mProfile;
	private SyncServiceStatus mServiceStatus;
//	private NetworkListener mNetworkListener;

	@Override
	public void onCreate() {
		mServiceStatus = new SyncServiceStatus();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

    private void showNotification(int id,CharSequence text) {
        PendingIntent intent = PendingIntent.getActivity(
                this, -1, new Intent("com.borqs.account.action.SETTINGS_PLUGIN").setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS),
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        Notification notification = new Notification(R.drawable.account_borqs_icon, text,
                System.currentTimeMillis());
        notification.setLatestEventInfo(this, text, null, intent);
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        mNM.notify(id, notification);
    }
    
    private void cancelNotification(int id){
        mNM.cancel(id);
    }

	@Override
	public void onStart(Intent intent, int startId) {
	    //cancel the sync fail notification
	    cancelNotification(R.string.sync_fail_notification);
	    
		Logger.logD(TAG, "syncMLservice.onStart");
		
		if(intent == null){
		    Logger.logE(TAG, "Wrong sync request.");
			return;
		}
	
		int[] itemsToSync = intent
				.getIntArrayExtra(Define.EXTRA_NAME_SYNC_ITEM);
		int modeToSync = intent.getIntExtra(Define.EXTRA_NAME_SYNC_MODE,
				AlertCode.ALERT_CODE_FAST);

		long profile = intent.getLongExtra(Define.EXTRA_NAME_PROFILE, -1);

		if (itemsToSync == null || itemsToSync.length == 0 || profile == -1) {
		    Logger.logE(TAG, "Wrong sync request.");
			return;
		}
		
//		//sync begin
//		for (int requestItem : itemsToSync) {
//            if(Define.SYNC_ITEMS_INT_CONTACTS == requestItem){
//                Intent syncBegin = 
//                    new Intent(SyncIntent.INTENT_ACTION_BORQS_CONTACT_SYNC_BEGIN);
//                sendBroadcast(syncBegin); 
//            }
//        }

		mServiceStatus.syncRequest(profile, itemsToSync, modeToSync);
	}

	@Override
	public void onDestroy() {
		mCallbacks.kill();
//		mNM.cancel(R.string.syncml_service_started);
		mServiceStatus.onDestroy();
//		mNM = null;
		mServiceStatus = null;
		mBinder = null;
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private ISyncMLService.Stub mBinder = new ISyncMLService.Stub() {
		public int[] registerCallBack(ISyncMLCallBack cb) {
			if (cb != null) {
				mCallbacks.register(cb);
			}
			return mServiceStatus.syncRequestItem;
		}

		public int getSyncItemStatus(int item) throws RemoteException {
			return mServiceStatus.getItemStatus(item).getStatus();
		}

		public void stop() throws RemoteException {
			Logger.logD(TAG, "stop sync.");

			mServiceStatus.cancelSync();
		}

		public long getSyncingProfile() throws RemoteException {
			/*if (mProfile != null) {
				return mProfile.getId();
			} else {
				return 0;
			}*/
		    return 0;
		}

		public void setInBackground(boolean status) throws RemoteException {
			if (mProfile != null) {
				mProfile.setInBackground(status);
			}
		}

		public int getSyncStatus() throws RemoteException {
			return mServiceStatus.mSyncStatus;
		}

		public String getLastMessage() throws RemoteException {
			return mServiceStatus.mLastMessage;
		}

		public int getCurrentPhase() throws RemoteException {
			return mServiceStatus.mCurrentSyncPahse;
		}

		public void unregisterCallBack(ISyncMLCallBack cb)
				throws RemoteException {
			if(cb != null){
				mCallbacks.unregister(cb);
			}
		}
	};

	
	private void connect() {
	    Logger.logD(TAG, "connect(), dataConnection="+mProfile.getApn());

		ConnectivityUtil.instance().connect(this, mProfile, new Handler(){
			public void handleMessage(Message msg) {
				switch(msg.what)
				{
					case ConnectivityUtil.APN_CONNECTED:
						mServiceStatus.dataConnectionResult(true, null);
						break;
					case ConnectivityUtil.APN_DISCONNECTED:
					case ConnectivityUtil.APN_CONNECT_ERROR:
						mServiceStatus.dataConnectionResult(false, (String)msg.obj);
						break;
				}
			}
		});
	}

	// Tear down the GPRS link.
	private void disconnect() {
		Logger.logD(TAG, "disconnect");
		
		ConnectivityUtil.instance().disconnect();
	}

	class SyncServiceStatus {
		private SyncingItemStatus[] itemStatus;
		private DsOperator mSyncOperator;
		private PowerManager.WakeLock mWakeLock;
		PowerManager mPM;
		int[] syncRequestItem;
		int mSyncStatus;
		private int mModeToSync = AlertCode.ALERT_CODE_FAST;
		private Thread mSyncThread;
		private String mLastMessage;
		private int mCurrentSyncPahse;
		private SyncListener mSyncListener;
		private boolean mCancelingSync;

		SyncServiceStatus() {
			itemStatus = new SyncingItemStatus[Define.SYNC_ITEMS_INT_TOTAL];
			for (int i = 0; i < Define.SYNC_ITEMS_INT_TOTAL; i++) {
				itemStatus[i] = new SyncingItemStatus(i);
			}
			mPM = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mSyncListener = new SyncListener(mHandler);
			enterIdleStatus(-1,new DsException(-1, -1));
		}

		public void cancelSync() {			
			Logger.logD(TAG, "cancelSync");
			
			mCancelingSync = true;
			if (mProfile != null) {
				mProfile.cancelSync();
			}
			mHandler.obtainMessage(SYNC_MSG,
					getString(R.string.sync_item_status_interrupted))
					.sendToTarget();
			if (syncRequestItem != null) {
				for (int item : syncRequestItem) {
					SyncingItemStatus activeItem = itemStatus[item];
					if (activeItem != null) {
						if (activeItem.mStatus == Define.SYNC_ITEM_STATUS_WAITING_SYNC
								|| activeItem.mStatus == Define.SYNC_ITEM_STATUS_SYNCING) {
							itemStatus[item]
									.setStatus(Define.SYNC_ITEM_STATUS_SYNC_INTERRUPTED);
						}
					}
				}
			}
			
			enterIdleStatus(SyncHelper.SYNC_USER_INTERRUPTED,new DsException(-1,DsException.VALUE_INTERRUPT));
			stopSelf();
		}

		SyncingItemStatus getItemStatus(int item) {
			return itemStatus[item];
		}

		void syncRequest(long profile, int[] item, int mode) {
			if (mSyncStatus != Define.SYNC_STATUS_IDLE) {
				mHandler.obtainMessage(SYNC_ALERT_MSG,
						getString(R.string.sync_is_running)).sendToTarget();
				return;
			}
			mProfile = new SyncProfile(profile, SyncMLService.this);

			mWakeLock = mPM.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					"My Tag");
			mWakeLock.acquire();

			syncRequestItem = item;
			if(syncRequestItem != null){
			    for (int type : syncRequestItem) {
			        getItemStatus(type).setStatus(
			                Define.SYNC_ITEM_STATUS_WAITING_SYNC);
			    }
			}

			mModeToSync = mode;
			enterDataConnectingStatus();
		}


		void stopSyncByNetWorkError() {

			if (mProfile != null) {
				mProfile.stopSync();
			}
			mHandler.obtainMessage(SYNC_MSG,
					getString(R.string.access_server_error))
					.sendToTarget();
			if (syncRequestItem != null) {
				for (int item : syncRequestItem) {
					SyncingItemStatus activeItem = itemStatus[item];
					if (activeItem != null) {
						if (activeItem.mStatus == Define.SYNC_ITEM_STATUS_WAITING_SYNC
								|| activeItem.mStatus == Define.SYNC_ITEM_STATUS_SYNCING) {
							itemStatus[item]
									.setStatus(Define.SYNC_ITEM_STATUS_SYNC_END_WITH_ERROR);
						}
					}
				}
			}
			
			enterIdleStatus(SyncHelper.SYNC_FAIL,new DsException(DsException.CATEGORY_CLIENT_SETTING,DsException.VALUE_ACCESS_SERVER));
			stopSelf();
		}
		
	void dataConnectionResult(boolean success, String arg0) {			
			Logger.logD(TAG, "Data connection result:"
						+ (success ? "Success!" : "Failed!"));
			
			if(mSyncStatus != Define.SYNC_STATUS_DATA_CONNECTING){
				if(success){
					Logger.logE(TAG,"Wrong state for dataConnectionResult");
				} else{			
					stopSyncByNetWorkError();
				}
				return;
			}

			if (mCancelingSync) {
				enterIdleStatus(SyncHelper.SYNC_USER_INTERRUPTED,new DsException(-1,DsException.VALUE_INTERRUPT));
				stopSelf();
				return;
			}

			if (success) {
				enterSyncingStatus();
			} else {
			    if(syncRequestItem != null){
			        for (int type : syncRequestItem) {
			            getItemStatus(type).setStatus(Define.SYNC_ITEM_STATUS_IDLE);
			        }
			    }
				mHandler
						.obtainMessage(SYNC_MSG, getString(R.string.sync_error))
						.sendToTarget();
				enterIdleStatus(SyncHelper.SYNC_FAIL,new DsException(DsException.CATEGORY_CLIENT_SETTING,DsException.VALUE_ACCESS_SERVER));
				mHandler.obtainMessage(SYNC_ALERT_MSG,arg0)
				.sendToTarget();
				stopSelf();
			}
		}

		private void enterDataConnectingStatus() {
			Logger.logD(TAG, "enterDataConnectingStatus");

			mHandler.obtainMessage(SYNC_BEGIN_MSG).sendToTarget();
			mSyncStatus = Define.SYNC_STATUS_DATA_CONNECTING;

			connect();
			mSyncListener.setPhase(ISyncListener.PHASE_DATA_CONNECTION);
			mSyncListener.pleaseWaiting();
		}

		private void enterIdleStatus(int resultType,DsException ds) {
		    Logger.logD(TAG, "enterIdleStatus");

			if (mSyncStatus == Define.SYNC_STATUS_IDLE) {
				return;
			}
			mCancelingSync = false;
			Message endMsg = mHandler.obtainMessage(SYNC_END_MSG,resultType);
			endMsg.arg1 = ds.getValue();
			endMsg.arg2 = ds.getCategory();
			endMsg.sendToTarget();
			mSyncStatus = Define.SYNC_STATUS_IDLE;
			mLastMessage = null;
			if(mSyncListener != null){
			    mSyncListener.setPhase(ISyncListener.PHASE_NONE);
			}

			if(itemStatus != null){
			    for (SyncingItemStatus item : itemStatus) {
			        item.setStatusSilently(Define.SYNC_ITEM_STATUS_IDLE);
			    }
			}
			if (mWakeLock != null && mPM != null) {
				mPM.userActivity(0, false);
				mWakeLock.release();
			}
			disconnect();
		}

		private void enterSyncingStatus() {
			Logger.logD(TAG, "enterSyncingStatus with new status="+mSyncStatus);
			
			mSyncStatus = Define.SYNC_STATUS_SYNCING;
			mHandler.obtainMessage(SYNC_START_SYNC).sendToTarget();
		}

		void onDestroy() {
			Logger.logD(TAG, "onDestroy");

			mHandler.removeMessages(SYNC_MSG);
			syncRequestItem = null;
			mSyncOperator = null;
			mSyncListener = null;
		}

		private void prepareSync(int item) throws Exception {
			Logger.logD(TAG, "prepareSync");

			mSyncOperator = new DsOperator(mProfile, item);
		}

		private void syncFunction() {
			Logger.logD(TAG, "syncFunction");

			boolean stopSync = false;
			boolean isShowInvalidCredentialDialog = true;
			boolean isNotRegisterUser = false;

			if(syncRequestItem != null){
			    for (int item : syncRequestItem) {
			        String endMsg = null;
			        DsException excpetion = null;
			        SyncingItemStatus activeItem = itemStatus[item];
			        if (stopSync) {
			            activeItem
			            .setStatus(Define.SYNC_ITEM_STATUS_SYNC_INTERRUPTED);
			            continue;
			        }
			        IDatastore src = null;
			        try {
			            prepareSync(item);
			            mSyncListener.reset();
			            src = mProfile.getDatastore(item, mSyncListener);
			            src.setSyncMode(mModeToSync);
			            activeItem.setStatus(Define.SYNC_ITEM_STATUS_SYNCING);
			            mSyncOperator.sync(src);			            
			            activeItem.setStatus(Define.SYNC_ITEM_STATUS_SYNC_END_SUCCESSFULLY);
			            src.syncEnd(IDatastore.END_SYNC_SUCCESSFULLY);
			            enterIdleStatus(SyncHelper.SYNC_SUCCESS,new DsException(-1, -1));
			        } catch (DsException e) {
			            excpetion = e;
			            e.printStackTrace();
			            if(e.getValue() == StatusValue.INVALID_CREDENTIALS){
			                isNotRegisterUser = true;
			            }
			            endMsg = SyncHelper.getDsExceptionString(SyncMLService.this,e);
			        } catch (SQLiteFullException e) {
			            e.printStackTrace();
			            excpetion  =new DsException(DsException.CATEGORY_OTHER, DsException.VALUE_DATABASE_FULL);
			            endMsg = SyncHelper.getDsExceptionString(SyncMLService.this,excpetion);
			        } catch (SQLiteException e) {
			            e.printStackTrace();
			            excpetion  =new DsException(DsException.CATEGORY_OTHER, DsException.VALUE_DATABASE_FULL);
                        endMsg = SyncHelper.getDsExceptionString(SyncMLService.this,excpetion);
			        } catch (XmlPullParserException e) {
			            e.printStackTrace();
			            excpetion  =new DsException(DsException.CATEGORY_OTHER, DsException.VALUE_WBXML_ERROR);
                        endMsg = SyncHelper.getDsExceptionString(SyncMLService.this,excpetion);
			        } catch (Throwable e) {
			            excpetion  =new DsException(DsException.CATEGORY_OTHER, -1);
                        endMsg = SyncHelper.getDsExceptionString(SyncMLService.this,excpetion);
			        } finally {
			            if (endMsg != null) {
			                if (mProfile.isCanceled()) {
			                    if (src != null) {
			                        src.syncEnd(IDatastore.END_SYNC_USER_INTERRUT);
			                    }
			                    activeItem
			                    .setStatus(Define.SYNC_ITEM_STATUS_SYNC_INTERRUPTED);
			                    stopSync = true;
			                    mHandler
			                    .obtainMessage(
			                            SYNC_MSG,
			                            getString(R.string.sync_item_status_interrupted))
			                            .sendToTarget();
			                    enterIdleStatus(SyncHelper.SYNC_USER_INTERRUPTED,new DsException(-1,DsException.VALUE_INTERRUPT));
			                } else {
			                    if (src != null) {
			                        src.syncEnd(IDatastore.END_SYNC_FAILED);
			                    }
			                    activeItem
			                    .setStatus(Define.SYNC_ITEM_STATUS_SYNC_END_WITH_ERROR);
			                    
			                    if(isShowInvalidCredentialDialog && isNotRegisterUser){
			                        isShowInvalidCredentialDialog = false;
			                        Message msg = new Message();
			                        msg.what = SYNC_MSG_REGISTER;
			                        msg.obj = endMsg;
			                        msg.arg1 = SHOW_REGISTER_DIALOG;
			                        mHandler.sendMessage(msg);
			                    }else {
			                        mHandler.obtainMessage(SYNC_MSG, endMsg)
			                        .sendToTarget();
			                    }
			                    //sync fail,if first sync ,we should notify user .
			                    if(!SyncHelper.isFirstSyncEnd(SyncMLService.this)) {
                                    showNotification(R.string.sync_fail_notification,
                                            getString(R.string.sync_fail_notification));
                                }
			                    enterIdleStatus(SyncHelper.SYNC_FAIL,excpetion);
			                }
			            }
			            //mark first sync
			            if(!SyncHelper.isFirstSyncEnd(SyncMLService.this)){
			                SyncHelper.endFirstSync(SyncMLService.this);
			            }
			        }
			    }
			}else{
			    Logger.logE(TAG, "no sync items,we treat sync as success now.");
			    enterIdleStatus(SyncHelper.SYNC_SUCCESS,new DsException(-1, -1));
			}
			if (mProfile != null) {
				mProfile.stopSync();
			}
			mHandler.obtainMessage(SYNC_ALERT_MSG,
					getString(R.string.syncml_service_sync_end)).sendToTarget();
			stopSelf();
		}
		
		private void startSync() {
			mSyncThread = new Thread() {
				public void run() {
					syncFunction();
				}
			};
            mSyncThread.setPriority(Thread.MIN_PRIORITY);
			mSyncThread.start();
		}

		private Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (SYNC_START_SYNC == msg.what) {
					startSync();
					return;
				}
				final int N = mCallbacks.beginBroadcast();
				for (int i = 0; i < N; i++) {
					try {
						switch (msg.what) {
						case SYNC_MSG:
							mLastMessage = (String) msg.obj;
							mCallbacks.getBroadcastItem(i).handleCallBack(
									mLastMessage);
							break;
						case SYNC_MSG_REGISTER:
							mLastMessage = (String) msg.obj;
							mCallbacks.getBroadcastItem(i)
									.handleRegisterCallBack(mLastMessage,
											msg.arg1 == SHOW_REGISTER_DIALOG);
							break;
						case SYNC_ALERT_MSG:
							mCallbacks.getBroadcastItem(i).handleAlertMsg(
									(String) msg.obj);
							break;
						case SYNC_BEGIN_MSG:
							mCallbacks.getBroadcastItem(i).syncBegin();
							break;
						case SYNC_END_MSG:
						    mCallbacks.getBroadcastItem(i).syncEnd((Integer)msg.obj/*result*/,msg.arg1/*exception code*/,msg.arg2/*exception category*/);
							break;
						case SYNC_MSG_UPDATE_ITEM_STATUS:
							SyncingItemStatus item = (SyncingItemStatus) msg.obj;
							mCallbacks.getBroadcastItem(i)
									.updateSyncItemStatus(item.getItem(),
											item.getStatus());
							break;
						case SYNC_MSG_PHASE:
							mCurrentSyncPahse = (Integer) msg.obj;
							mCallbacks.getBroadcastItem(i).handleSyncPhase(
									mCurrentSyncPahse);
							break;
						default:
							break;
						}
					} catch (DeadObjectException e) {
						e.printStackTrace();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				mCallbacks.finishBroadcast();
			}
		};

		private class SyncingItemStatus {
			SyncingItemStatus(int element) {
				syncElement = element;
				setStatus(Define.SYNC_ITEM_STATUS_IDLE);
			}

			public int getItem() {
				return syncElement;
			}

			void setStatusSilently(int status) {
				mStatus = status;
			}

			void setStatus(int status) {
				mStatus = status;
				Message msg = mHandler
						.obtainMessage(SYNC_MSG_UPDATE_ITEM_STATUS);
				msg.obj = this;
				msg.sendToTarget();
			}

			int getStatus() {
				return mStatus;
			}

			private int mStatus;
			private int syncElement;
		}
	}


	private class SyncListener implements ISyncListener {
		//private int mServerNumberOfChange;
		private int mServerCount;
		private int mLocalNumberOfChange;
		private int mLocalCount;
		private int mTotalDeleteAllCount;
		private int mDeleteAllCount;
		private int mTotalAnalyzeCount;
		private int mAnalyzeCount;

		private Handler mHandler;
		private static final int SYNC_MSG = 1;

		public SyncListener(Handler handler) {

			mHandler = handler;
		}

		public void reset() {
			//mServerNumberOfChange = 0;
			mServerCount = 0;
			mLocalNumberOfChange = 0;
			mLocalCount = 0;
		}

		private void sendSyncMsg(String text) {
			mHandler.obtainMessage(SYNC_MSG, text).sendToTarget();
		}

		public void endSync(int result) {
			if (result == 0) {
				sendSyncMsg(getString(R.string.sync_msg_sync_successfully_completed));
			} else {
				sendSyncMsg(getString(R.string.sync_msg_error_in_sync));
			}
		}

		public void pleaseWaiting() {
			sendSyncMsg(getString(R.string.sync_msg_please_waiting));
		}

		public void checkingLocalData() {
			sendSyncMsg(getString(R.string.sync_msg_checking_device_data));
		}

		public void handleLocal() {
			mLocalCount++;
			showTatalProgressMsg(R.string.sync_msg_packaging_device_sync_items,
					mLocalNumberOfChange, mLocalCount);
		}

		public void handleServer(int opt) {
			mServerCount++;

			StringBuilder msg = new StringBuilder();
			msg
					.append(getString(R.string.sync_msg_handling_server_sync_items))
					.append(mServerCount);
			// if (mServerNumberOfChange > 0) {
			// msg.append("/").append(mServerNumberOfChange);
			// }
			sendSyncMsg(msg.toString());
		}

		public void setLocalNoc(int n) {
			mLocalNumberOfChange = n;
		}

		public void setServerNoc(int n) {
			//mServerNumberOfChange = n;
		}

		public void setPhase(int phase) {
			mHandler.obtainMessage(SYNC_MSG_PHASE, new Integer(phase))
					.sendToTarget();
		}

		public void deletedOneItemOfAll() {
			mDeleteAllCount++;
			showTatalProgressMsg(R.string.sync_msg_delete_device_all_content,
					mTotalDeleteAllCount, mDeleteAllCount);
		}
		
		public void deletedItemsOfAll(int items) {
			mDeleteAllCount = mDeleteAllCount + items;
			showTatalProgressMsg(R.string.sync_msg_delete_device_all_content,
					mTotalDeleteAllCount, mDeleteAllCount);
		}

		public void setDeleteAllCount(int count) {
			mTotalDeleteAllCount = count;
		}

		public void analyzeDeviceItem() {
			mAnalyzeCount++;
			showTatalProgressMsg(R.string.sync_msg_checking_device_data,
					mTotalAnalyzeCount, mAnalyzeCount);
		}

		public void setCurrentDeviceSize(int size) {
			mTotalAnalyzeCount = size;
		}

		private void showTatalProgressMsg(int stringId, int total, int progress) {
			StringBuilder msg = new StringBuilder();
			msg.append(getString(stringId)).append(progress);
			if (total > 0) {
				msg.append("/").append(total);
			}
			sendSyncMsg(msg.toString());
		}
	}
}
