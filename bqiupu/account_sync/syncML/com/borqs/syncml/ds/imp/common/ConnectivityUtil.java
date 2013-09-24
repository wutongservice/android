package com.borqs.syncml.ds.imp.common;

//import oms.net.DataConnectivityHelper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkConnectivityListener;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import com.borqs.contacts_plus.R;
import com.borqs.syncml.ds.protocol.IProfile;

public class ConnectivityUtil {

	// Connectivity status change notification event
	private static final int EVENT_CONNECTIVITY_STATE_CHANGED = 100;
	private static final int INTERNAL_EVENT_START_NETWORK = 101;
	private static final String TAG = "SyncMLClient.ConnectivityUtil";
	
	public static final int APN_CONNECTED = 0;
	public static final int APN_DISCONNECTED = 1;
	public static final int APN_CONNECT_ERROR = 2;
	
	public static final int REASON_ERROR_AIRPLANEMODE = 200;
	public static final int REASON_ERROR_OTHER = 201;
	
	private NetworkConnectivityListener mConnectivityListener;
	private static ConnectivityUtil mInstance;
	
	private Context mContext;
	private Handler mConnectivityChangeListener;
	
	
	// APN setting used for current sync session
	private String mApnType;
	private String mApn;
	// proxy info if have
	private String mProxyHost;
	private int mProxyPort;
	private boolean mRuninEmulator = false;//SystemProperties.get("ro.kernel.qemu").equals("1");
		
	static{
		mInstance = new ConnectivityUtil();
	}
	
	/**
	 * 
	 */
	private ConnectivityUtil(){
	}
	
	/**
	 * @return
	 */
	public static ConnectivityUtil instance()
	{
		return mInstance;
	}
	
	/**
	 * @return proxy host for the current APN if has
	 */
	public String getProxyHost()
	{
		return mProxyHost;
	}
	/**
	 * @return proxy port for the current APN if has 
	 */
	public int getProxyPort()
	{
		return mProxyPort;
	}
	/**
	 * start up the network with APN setting
	 * @param context
	 * @param profile sync profile
	 * @param callback the handler will receive the events - APN_CONNECTED, APN_DISCONNECTED
	 * @return
	 */
	public void connect(Context context, IProfile profile, Handler callback){
		//TODO network connection
		if(mRuninEmulator || true){
        	callback.obtainMessage(APN_CONNECTED).sendToTarget();
        	return;
		}
		
		mContext = context.getApplicationContext();
		//check airplane mode
        if (Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) > 0) {
        	callback.obtainMessage(APN_CONNECT_ERROR, getFailureString(REASON_ERROR_AIRPLANEMODE))
        			.sendToTarget();
        	return;
        }
		// connection setup
		mConnectivityChangeListener = callback;
		mApnType = profile.getApn();
		
		
		mConnectivityListener = new NetworkConnectivityListener();
		mConnectivityListener.registerHandler(mConnectivityChangeHandler,EVENT_CONNECTIVITY_STATE_CHANGED);
		mConnectivityListener.startListening(mContext);
		mConnectivityChangeHandler.sendMessage(mConnectivityChangeHandler.obtainMessage(INTERNAL_EVENT_START_NETWORK));
	}

	/**
	 * disconnect the APN
	 */
	public void disconnect()
	{		
		//TODO network connection
		if(mRuninEmulator || true){
			return;
		}
		
		mConnectivityChangeListener =null;
		if(mConnectivityListener!=null)
		{
			ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			connMgr.stopUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE,mApnType);
			mConnectivityListener.unregisterHandler(mConnectivityChangeHandler);
			mConnectivityChangeHandler.removeMessages(EVENT_CONNECTIVITY_STATE_CHANGED);
			mConnectivityListener.stopListening();
		}

	}
	
//	private int startUsingNetwork()
//	{
//		int result = -1;
//		try{
//			ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//			//Currently, the network framework do not care the first argument 'TYPE'.  
//		    result = connMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, mApnType);
//		    
//		    switch(result)
//		    {
//		    	case Phone.APN_ALREADY_ACTIVE:
//	                NetworkInfo[] netInfos = connMgr.getAllNetworkInfo();
//	                NetworkInfo info = searchConnectedApn(netInfos, mApnType);
//	                if(info!=null){
//	                	onConnectivityChanged(info);
//	                }else{ //unexpected error.
//	                	mConnectivityChangeListener.obtainMessage(
//	                			APN_CONNECT_ERROR, getFailureString(REASON_ERROR_OTHER)).sendToTarget();
//	                }
//		    		break;
//		    	case Phone.APN_REQUEST_STARTED:
//		    		if (Config.DEBUG)	    		{
//		    			Log.d(TAG, "startUsingNetworkFeature(), wait for network start up...");
//		    		}
//		    		break;
//		    	case Phone.APN_REQUEST_FAILED:
//		    	case Phone.APN_TYPE_NOT_AVAILABLE:
//		    	case -1:
//		    		mConnectivityChangeListener.obtainMessage(APN_CONNECT_ERROR, getFailureString(REASON_ERROR_OTHER)).sendToTarget();
//		    		Log.e(TAG, "Got a error on invoking startUsingNetworkFeature()!");
//		    }
//		}catch(Exception e){
//			/**
//			 * TODO:find out the root cause.
//			 * 
//			 * Now we only add try{}catch{} to protect for NullPointerException throwed by connMgr.startUsingNetworkFeature().
//			 * 
//			 * case 1.run SyncMLClient.apk to device,then run the ut by shell command 
//			 * "adb shell am instrument -w oms.android.syncml.test/android.test.InstrumentationTestRunner",the 
//			 * program does not throw the NullPointerException
//			 * 
//			 * case 2.adb push SyncMLClient.apk to /system/app/,then run the ut.
//			 * It will throw the NullPointerException.
//			 * 
//			 * java.lang.NullPointerException
//			 * at android.os.Parcel.readException(Parcel.java:1328)
//			 * at android.os.Parcel.readException(Parcel.java:1276)
//			 * at android.net.IConnectivityManager$Stub$Proxy.startUsingNetworkFeature(IConnectivityManager.java:447)
//			 * at android.net.ConnectivityManager.startUsingNetworkFeature(ConnectivityManager.java:309)
//			 * at oms.syncml.ds.imp.common.ConnectivityUtil.startUsingNetwork(ConnectivityUtil.java:150)
//			 * at oms.syncml.ds.imp.common.ConnectivityUtil.access$0(ConnectivityUtil.java:146)
//			 * at oms.syncml.ds.imp.common.ConnectivityUtil$1.handleMessage(ConnectivityUtil.java:193)
//			 * at android.os.Handler.dispatchMessage(Handler.java:99)
//			 * at android.os.Looper.loop(Looper.java:130)
//			 * at android.app.ActivityThread.main(ActivityThread.java:3796)
//			 * at java.lang.reflect.Method.invokeNative(Native Method)
//			 * at java.lang.reflect.Method.invoke(Method.java:507)
//			 * at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:839)
//			 * at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:597)
//			 * at dalvik.system.NativeStart.main(Native Method)
//			 * 
//			 */
//		}
//	    return result;
//	}

	/**
	 * 
	 */
	private Handler mConnectivityChangeHandler = new Handler(){
		
		//!!we have to many workaround code for DCM fault. 
		private boolean requestSendout = false;
		private boolean connected = false;
		public void handleMessage(Message msg) {
	        switch (msg.what) {
	        	case INTERNAL_EVENT_START_NETWORK:
	                if(mConnectivityChangeListener!=null){
	                    mConnectivityChangeListener.obtainMessage(APN_CONNECTED).sendToTarget();
	                }
//	        		if (Config.DEBUG){
//		        		Log.d(TAG, "Got the signal to start up the network...");
//	        		}
//	        		requestSendout = true;
//	        		connected = startUsingNetwork()==Phone.APN_ALREADY_ACTIVE;
	        		break;
	        	case EVENT_CONNECTIVITY_STATE_CHANGED: {
//	        		if (Config.DEBUG){
//	        	   		Log.d(TAG, "Incoming EVENT_CONNECTIVITY_STATE_CHANGED.");
//	        		}
//	        		
//	        		if(!requestSendout){
//	        			if (Config.DEBUG){
//		        	   		Log.d(TAG, "Ignore the cached event.");
//		        		}	
//	        			break;
//	        		}
//                                       
//	                NetworkInfo info = mConnectivityListener.getNetworkInfo();
//	                if (info == null) {
//	                	Log.e(TAG, "Network Info is null ! ");
//	                    return;
//	                }
//	                
//	                // check if it is from the right APN
//	        		if (!TextUtils.equals(info.getApType(), mApnType)) {
//                        Log.e(TAG, "The msg is for (" + info.getApType()
//                                + "),  Discard this msg.");
//                        return;
//                    }
//	        		
//	    			//Temporary solution for the fault in DCM.(Can not handle correctly the 
//	    			//continuously startxxx()/stopxxx() in a short time.)
//	        		if(info.getState()==NetworkInfo.State.CONNECTED && connected){
//	        			return;
//	        		}
//	        		if(info.getState()==NetworkInfo.State.DISCONNECTED&&!connected){
//	        			//if the data connection is disabled by user, we should wait for the connected info.
//	        			if("dataDisabled".equalsIgnoreCase(info.getReason())){
//	        				//waiting for connecting.
//	        				return;
//	        			}
//	        			if(Config.DEBUG){
//	        				Log.d(TAG, "Connecting failed!");
//	        			}
//	        		}else if(info.getState()==NetworkInfo.State.CONNECTED && !connected){
//	        			//if we receive the connected info,we should check if the network is really connected.
//	        			if(startUsingNetwork()!=Phone.APN_ALREADY_ACTIVE){
//	        				//waiting for connecting.
//	        				return ;
//	        			}
//	        		}
//	        		connected = info.getState()==NetworkInfo.State.CONNECTED;
//	       			onConnectivityChanged(info);
	                break;
	            }
	        	default:break;
	        }
		}		
	};
	
	/**
	 * action on the connectivity status change
	 */
	private void onConnectivityChanged(NetworkInfo netInfo)
	{	
//	    
//		if(netInfo.getState()==NetworkInfo.State.CONNECTED)
//		{	
// 			if(!Phone.FEATURE_ENABLE_INTERNET.equals(mApnType)){
// 				DataConnectivityHelper.bindNetwork(netInfo);
// 			}
// 			
// 			if(WapServerTestApn.isWapServerTest()){
// 				WapServerTestApn.getTestAPN();
// 				mProxyHost = WapServerTestApn.mHost;
// 				mProxyPort = WapServerTestApn.mPort;
// 			} else{
// 			// get the proxy info for the APN
// 		    	String apnType = netInfo.getApType();;
// 		    	String apn = netInfo.getExtraInfo();
// 		    	mApn = apn;
// 		    	//proxyAndPort string format:"test:80"
// 		    	String proxyAndPort = DataConnectivityHelper.getProxyAndPort(mContext, apnType, apn);
// 		    	if(proxyAndPort != null){
// 		    		String[] pap = proxyAndPort.split(":");
// 		    		if(pap.length>1){
// 		    			mProxyHost = pap[0];
// 		    			mProxyPort = Integer.valueOf(pap[1]);
// 		    			if(Config.DEBUG){
// 	 		    			Log.d(TAG, "Proxy="+mProxyHost +":"+mProxyPort);
// 	 		    		}
// 		    		}
// 		    	}else{
// 		    		Log.e(TAG, "the APN is not found in DB.");
// 		    	}
// 		    	
//// 		    	Cursor cursor = SqliteWrapper.query(mContext,mContext.getContentResolver(),
//// 		    			Telephony.Carriers.CONTENT_URI, 
//// 		    			null, 
//// 		    			Carriers.TYPE +" like %'"+apnType+"'% AND "+Carriers.APN +"='" +apn+"'", 
//// 		    			null, 
//// 		    			null);
//// 		    	if(cursor!=null)
//// 		    	{
//// 		            try {
//// 		                while (cursor.moveToNext()) {
//// 		                	mProxyHost = cursor.getString(cursor.getColumnIndexOrThrow(Carriers.PROXY));
//// 		                	mProxyPort = cursor.getInt(cursor.getColumnIndexOrThrow(Carriers.PORT));
//// 		                }
//// 		            }catch(Exception e){
//// 		            	Log.e(TAG, e.getMessage());
//// 		            }
//// 		    		if(Config.DEBUG){
//// 		    			Log.d(TAG, "Proxy="+mProxyHost +":"+mProxyPort);
//// 		    		}
//// 		            cursor.close();
//// 		    	}else
//// 		    	{
//// 		    		Log.e(TAG, "the APN is not found in DB.");
//// 		    	}
// 			}
//			
//	    	
//	    	// send notification
//	    	if(mConnectivityChangeListener!=null){
//	    		mConnectivityChangeListener.obtainMessage(APN_CONNECTED).sendToTarget();
//	    	}
//		}else if(netInfo.getState()==NetworkInfo.State.DISCONNECTED)
//		{
//			// send notification
//	    	if(mConnectivityChangeListener!=null){
//	    		mConnectivityChangeListener.obtainMessage(APN_DISCONNECTED, getDisconnectString(netInfo.getReason())).sendToTarget();
//	    	}
//		}
//		
	}
//	
//	private NetworkInfo searchConnectedApn(NetworkInfo[] netInfos, String apnType){
//        NetworkInfo info = null;
//        for (NetworkInfo netInfo : netInfos) {
//            if (TextUtils.equals(mApnType, netInfo.getApType()) &&
//            	netInfo.getState() == NetworkInfo.State.CONNECTED) {
//            	info = netInfo;
//            	
//            	//for internet apn group, wifi is prefered
//            	if("internet".equals(apnType) && netInfo.getType()!=ConnectivityManager.TYPE_WIFI){
//            		// continue, try to wifi connection
//            		continue;
//            	}
//                break;
//            }
//        }
//        return info;
//	}
//	
	private String getFailureString(int status)
	{
		switch(status)
		{
		case REASON_ERROR_AIRPLANEMODE:
			return mContext.getString(R.string.conn_err_airplanemode);
		case REASON_ERROR_OTHER:
			return mContext.getString(R.string.conn_err_other);
		}
		return mContext.getString(R.string.conn_err_other);
	}
	
	private String getDisconnectString(String reason)
	{
//		StringBuilder msg = new StringBuilder();
//		msg.append(mContext.getString(R.string.conn_err_prefix));
//		msg.append(reason);
		return mContext.getString(R.string.conn_err_prefix);
	}
}
