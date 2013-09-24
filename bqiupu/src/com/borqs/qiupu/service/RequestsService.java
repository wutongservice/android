package com.borqs.qiupu.service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.BorqsAccount;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.service.RequestsService.RequestListener;
import com.borqs.qiupu.util.StatusNotification;

import twitter4j.AsyncQiupu;
import twitter4j.Requests;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.conf.ConfigurationContext;

public class RequestsService {
	final String TAG="BPC.Alarm.RequestsService";
	
	ArrayList<Requests> mRequests = new ArrayList<Requests>();
	
	private static HashMap<String, WeakReference<RequestListener>> listeners = new HashMap<String, WeakReference<RequestListener>>();
	
	private BorqsAccount         mAccount; 
	private QiupuORM             orm;
	private QiupuService         mService;
	private AsyncQiupu           asyncTwitter;
	private int                  nErrorCount = 0;
	private Handler              handler;
	private Context              mContext;
	
	public void init(BorqsAccount account)
	{
		if(QiupuConfig.LOGD)Log.d(TAG, "start="+account);
		mAccount = account;
		
		if(mAccount != null) 
		{
			nErrorCount = 0;	
		}
	}
	
	public void start(BorqsAccount account)
	{
		if(QiupuConfig.LOGD)Log.d(TAG, "start="+account);
		mAccount = account;
		
		if(mAccount != null) 
		{
			nErrorCount = 0;
			rescheduleRequests(false);
		}
	}
	
	public RequestsService(QiupuService qs)
	{
		mContext = qs.getApplicationContext();
		orm = QiupuORM.getInstance(mContext);
		mService = qs;
		
		handler = new RequestHandler();
		
		mAccount = mService.getBorqsAccount();
		asyncTwitter = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
	}
	
	public void onLogin(BorqsAccount account) 
	{
		if(QiupuConfig.LOGD)Log.d(TAG, "onLogin="+account);
		//start(account);
		init(account);
	}
	
	public void onLogout() 
	{
		Log.d(TAG, "logout");
    	handler.post(new Runnable()
    	{
    		public void run()
    		{
				Intent i = new Intent(mService, mService.getClass());
				i.setAction(QiupuService.INTENT_QP_SYNC_FRIENDS_INFORMATION);
				PendingIntent userpi = PendingIntent.getService(mService.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
				
				AlarmManager alarmMgr = (AlarmManager)mService.getSystemService(Context.ALARM_SERVICE);		
				alarmMgr.cancel(userpi);
    		}});
	}
	
	public void destroy() {
		onLogout();
	}
	
	public void onCancelLogin() {
		if(mAccount != null) 
		{
			mAccount = null;
		}
	}
		
	public void rescheduleRequests(boolean force)
	{
		if(orm.isEnableNotification() == false && force == false)
			return ;
		
		AlarmManager alarmMgr = (AlarmManager)mService.getSystemService(Context.ALARM_SERVICE);
		long nexttime;
		
		long current_time = System.currentTimeMillis();
		long last_update_time = orm.getRequstsLastSyncTime();
		long donespan  = (current_time-last_update_time);
		long left_time = orm.getRequestsInterval()*60*1000L - donespan;
		if(donespan <0 || left_time <=0)
		{
			long waittime=1;
			for(int i=0;i<nErrorCount && i<10;i++)
			{
				waittime = waittime*2;
			}
            nexttime = System.currentTimeMillis()+ 60*1000*waittime;			
		}
		else
		{
			nexttime = System.currentTimeMillis()+ left_time;	
		}
		
		if(force == true)
		{
			nTryTimes = 0;
			nexttime = System.currentTimeMillis()+ 2*1000;
		}
		
		if(QiupuService.TEST_LOOP)
		{
			nexttime = System.currentTimeMillis()+ 2*60*1000;
		}
		
		
		Intent i = new Intent(mService, mService.getClass());
		i.setAction(QiupuService.INTENT_QP_SYNC_REQUESTS_INFORMATION);
		i.putExtra("no_notification", force);
		PendingIntent phonebookpi = PendingIntent.getService(mService.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);		
		alarmMgr.set(AlarmManager.RTC, nexttime, phonebookpi);
	}
	
	int nTryTimes = 0;
	public void alarmRequetsComming(Message callback, boolean isConnectionAvailable, boolean enableNotification) {
		 Log.d(TAG, "alarmRequetsComming" + "enable Notification : " + orm.isEnableNotification());		 
        
        long nexttime;

        if (isConnectionAvailable) {
        	nTryTimes = 0;
            Message msg = handler.obtainMessage(REQUEST_GET);
            msg.getData().putParcelable(CALLBACK, callback);
            msg.getData().putBoolean("no_notification", enableNotification);
            handler.sendMessageDelayed(msg, 10 * QiupuConfig.A_SECOND);
            nexttime = System.currentTimeMillis() +
                    orm.getRequestsInterval() * QiupuConfig.A_MINUTE;
        } else {
        	nTryTimes++;
        	
        	if(nTryTimes >= 10)
        		nTryTimes = 1;
        	
            nexttime = (long) (System.currentTimeMillis() + Math.pow(2, nTryTimes) *60 * QiupuConfig.A_SECOND);
        }


        if (QiupuService.TEST_LOOP) {
            nexttime = System.currentTimeMillis() + 90 * QiupuConfig.A_SECOND;
        }

        if(orm.isEnableNotification())
        {
            final long finaltime = nexttime;
            handler.post(new Runnable() {
                public void run() {
                    AlarmManager alarmMgr = (AlarmManager) mService.getSystemService(Context.ALARM_SERVICE);

                    Intent i = new Intent(mService, mService.getClass());
                    i.setAction(QiupuService.INTENT_QP_SYNC_REQUESTS_INFORMATION);
                    PendingIntent userpi = PendingIntent.getService(mService.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
                    alarmMgr.set(AlarmManager.RTC, finaltime, userpi);
                }
            });
        }        
	}

    final String CALLBACK = "callback";
    final static int REQUEST_GET = 1;
    final static int REQUEST_GET_END = 2;

    private class RequestHandler extends Handler {
        public RequestHandler()
        {
            super();            
            Log.d(TAG, "new RequestHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case REQUEST_GET:
	            {
	            	getRequests(msg.getData().getParcelable(CALLBACK), msg.getData().getBoolean("no_notification", false));
	            	break;
	            }
	            case REQUEST_GET_END:
	            {
	            	boolean suc = msg.getData().getBoolean("RESULT");
	        		if(suc)
	        		{            	
	        			nErrorCount = 0;	        			
        				//set record time
            			orm.setRequestLastSyncTime();
            			
            			Message callback = msg.getData().getParcelable(CALLBACK);
            			if(callback != null)
            			{
            			    Log.d(TAG, "Suc Get request, call back to sender");
            			    callback.getData().putBoolean("RESULT", true);
            			    callback.sendToTarget();                			    
            			}
	        		}
	        		else
	        		{
	        		    Message callback = msg.getData().getParcelable(CALLBACK);
	                    if(callback != null)
	                    {
	                        Log.d(TAG, "fail Get reqeusts, call back to sender");
	                        callback.getData().putBoolean("RESULT", false);
	                        callback.sendToTarget();                                
	                    }
	        		    else
	        		    {
	            			nErrorCount++;            			
	            			Log.d(TAG, "Fail to get request reschedule, current page=");
	            			rescheduleRequests(false);
	        		    }
	        		}       
	            }
	          	break;
	        }
        }
    }
	
	
	
	public static void regiestRequestListener(String key, RequestListener rl)
	{
		WeakReference<RequestListener> ref = listeners.get(key);
		if(ref != null && ref.get() != null)
		{
			ref.clear();
		}
		
		listeners.put(key, new WeakReference<RequestListener>(rl));		
	}
	
	public static void unRegiestRequestListener(String key)
	{
		WeakReference<RequestListener> ref = listeners.get(key);
		if(ref != null && ref.get() != null)
		{
			ref.clear();
		}
		
		listeners.remove(key);		
	}
	
	public void updateRequestListener(ArrayList<Requests> requests)
	{
		Set<String> sets =  listeners.keySet();
	    Iterator<String> its = sets.iterator();
	    while(its.hasNext())
	    {
	    	String key = its.next();
	    	WeakReference<RequestListener> ref = listeners.get(key);
	    	
	    	if(ref != null && ref.get() != null)
		    {
		        ref.get().requestUpdated(requests);
		    }
	    }
	}
	
	private void removeOlderRequests(ArrayList<Requests> requests)
	{
		for(Requests old: mRequests)
		{
			for(Requests item: requests)
			{
				if(item.rid.equals(old.rid))
				{
					requests.remove(item);
					break;
				}
			}
		}
		
	}
	
	Object mLock = new Object();
	boolean isInprocess = false;
	
	private void getRequests(final Parcelable callBack, final boolean no_notifications ) 
	{
    	if(mAccount == null) {
    		Log.d(TAG, "getRequests, mAccount is null exit");
    		return;
    	}
    	
    	synchronized(mLock)
    	{
    		if(isInprocess)
    		{
    			updateRequestListener(mRequests);
    			Log.d(TAG, "getRequests, I am loading, ignore, just show UI");
    			return;
    		}
    	}
    	
    	
    	synchronized(mLock)
    	{
    		isInprocess = true;
    	}
    	
    	final String sId = QiupuORM.getSettingValue(mContext, QiupuORM.HOME_ACTIVITY_ID);
    	long tmpsceneId = -1;
    	if(TextUtils.isEmpty(sId) == false) {
    		try {
    			tmpsceneId = Long.parseLong(sId);
    		} catch (Exception e) {
    			Log.d(TAG, "homeid is null");
    		}
    	}
    	
    	final long sceneId = tmpsceneId;
		asyncTwitter.getRequests(AccountServiceUtils.getSessionID(), Requests.getWutongRequesttypes(), new TwitterAdapter()
		{
			public void getRequests(ArrayList<Requests> requests) {
			    Log.d(TAG,"finish getRequests="+requests.size());
			    
			    ArrayList<Requests> tmpList = new ArrayList<Requests>();
			    tmpList.addAll(requests);
			    
			    removeOlderRequests(tmpList);
			    mRequests.clear();
				    
			    //remove duplicate records
			    mRequests.addAll(requests);

			    if (mRequests.size() > 0) {
			        orm.cacheRequests(mRequests, "", sceneId);
			    } else {
			        orm.deleteDoneRequest(null, sceneId);
			    }

			    updateRequestListener(mRequests);				 
			    
			    //post notification
			    //format request
			    //already in notification
			    if(no_notifications == false && false)//we don't need do notification at main
			    {
				    if(tmpList.size() > 0)
				    {
					    StringBuilder profilesb = new StringBuilder();
	                    final int MAX_DISP_NUM = 1;
					    for(int i = 0; i < MAX_DISP_NUM; i++)
					    {
					    	Requests rq = tmpList.get(i);
					    	if(rq.type == Requests.REQUEST_TYPE_EXCHANGE_VCARD)
					    	{
					    		if(profilesb.length() >0)
					    			profilesb.append(", ");
	
					    		profilesb.append(rq.user.nick_name);
					    	}
					    }
	//				    profilesb.append(" ");
	                    if (tmpList.size() > MAX_DISP_NUM) {
	                        profilesb.append(String.format(mService.getString(R.string.other_more_people).toLowerCase(),
	                        		tmpList.size() - MAX_DISP_NUM));
	                    }
	
	                    if(profilesb.length() > 0){
	                    	profilesb.append(mService.getString(R.string.request_see_profile).toLowerCase());				    
	                    	StatusNotification.notifyRequests(mService, profilesb.toString(), orm.isEnableVibrate());
	                    }
				    }
			    }		    
			    
			    tmpList.clear();
		    
				
			    synchronized(mLock)
		    	{
		    		isInprocess = false;
		    	}
			    
				Message mds = handler.obtainMessage(REQUEST_GET_END);
    			mds.getData().putBoolean("RESULT",      true);
    			mds.getData().putParcelable(CALLBACK,   callBack);
                handler.sendMessage(mds);
                
			}
			
			public void onException(TwitterException ex,TwitterMethod method) 
			{
				TwitterExceptionUtils.printException(TAG, "getRequests, server exception:", ex, method);
				
				synchronized(mLock)
		    	{
		    		isInprocess = false;
		    	}
				
		    	Message mds = handler.obtainMessage(REQUEST_GET_END);
    			mds.getData().putBoolean("RESULT",      false);
    			mds.getData().putParcelable(CALLBACK, callBack);
                handler.sendMessage(mds);	
			}
		});   
	}

	public interface RequestListener {
		public void requestUpdated(ArrayList<Requests> data);
	}
}
