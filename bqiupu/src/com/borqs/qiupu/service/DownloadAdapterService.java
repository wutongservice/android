package com.borqs.qiupu.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.ui.BasicActivity;

import twitter4j.ApkResponse;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class DownloadAdapterService {
	final static String TAG="DownloadAdapterService";
	
	static HashMap<Long, ApkResponse> downloadMap = new HashMap<Long, ApkResponse>();
	ApkFileManager mFileManager;
	
	public static boolean isInDownloading(Context con, String packagename)
	{
		Set<Long> sets = downloadMap.keySet();
		Iterator<Long> it = sets.iterator();
		while(it.hasNext())
		{
			long key = it.next();			
			
			ApkResponse apk = downloadMap.get(key);
			
			if(null != apk && null != apk.packagename && apk.packagename.equals(packagename))
			{
				android.app.DownloadManager dm = (android.app.DownloadManager) con.getSystemService(Context.DOWNLOAD_SERVICE);
				 android.app.DownloadManager.Query query = new android.app.DownloadManager.Query();	    
	                query.setFilterById(key);
	                Cursor c = dm.query(query);
	                if (c.moveToFirst()) {
	                    int columnIndex = c.getColumnIndex(android.app.DownloadManager.COLUMN_STATUS);
	                    if(android.app.DownloadManager.STATUS_FAILED == c.getInt(columnIndex))
	                    {
	                    	downloadMap.remove(key);
	    					return false;
	                    }
	                }else {
	                	downloadMap.remove(key);
    					return false;
	                }
//				//check file is exist
//				final String filePath = QiupuHelper.getDownloadSdcardPath(packagename);
//				if(new File(filePath).exists() == false)
//				{
//					//already deleted
//					downloadMap.remove(key);
//					return false;
//				}
				return true;
			}
		}
		
		return false;
	}
	
	
	BroadcastReceiver receiver = new BroadcastReceiver() {		
        @Override
        public void onReceive(Context con, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG,"action="+intent);
            
            if (android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) 
            {            
	            if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO)
	            {
	            	android.app.DownloadManager dm = (android.app.DownloadManager) con.getSystemService(Context.DOWNLOAD_SERVICE);
		            //if (android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) 
		            {
		                long downloadId = intent.getLongExtra(android.app.DownloadManager.EXTRA_DOWNLOAD_ID, 0);
		                ApkResponse apk = downloadMap.get(downloadId);
		                downloadMap.remove(downloadId);
		                
		                Log.d(TAG, "new download complete="+downloadId);
		                android.app.DownloadManager.Query query = new android.app.DownloadManager.Query();	    
		                query.setFilterById(downloadId);
		                Cursor c = dm.query(query);
		                if (c.moveToFirst()) {
		                    int columnIndex = c.getColumnIndex(android.app.DownloadManager.COLUMN_STATUS);
		                    if (android.app.DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
		
		                        String uriString = c.getString(c.getColumnIndex(android.app.DownloadManager.COLUMN_LOCAL_FILENAME));

		                        if(uriString.startsWith("file://"))
 		                        {
 		                            uriString = uriString.substring(7);
 		                        }
		                        
		                        if(uriString.endsWith(".apk"))
		                        {
		                            ApkFileManager.installApk(con, uriString, "", "", false);
		                            //
			                        //no need remove??
			                        //dm.remove(downloadId);
//	                                dm.remove(downloadId);

		                            if(mFileManager != null)
			                        mFileManager.updateActivityUI(QiupuMessage.MESSAGE_DOWNLOAD_APK_OK, mFileManager.createMsg(QiupuMessage.MESSAGE_DOWNLOAD_APK_OK, apk));
		                        }
		                        
		                        
		                    }
		                    else if(android.app.DownloadManager.STATUS_FAILED == c.getInt(columnIndex))
		                    {
		                    	Log.d(TAG, "new download fail="+downloadId);
		                    	
		                    	if(mFileManager != null)
		                    	mFileManager.updateActivityUI(QiupuMessage.MESSAGE_DOWNLOAD_APK_FAILED, mFileManager.createMsg(QiupuMessage.MESSAGE_DOWNLOAD_APK_FAILED, apk));
		                    }
		                }
		            }
	            }
            }
            else if (android.app.DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) 
            {
            	if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO)
 	            {
            		android.app.DownloadManager dm = (android.app.DownloadManager) con.getSystemService(Context.DOWNLOAD_SERVICE);
 		            //if (android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) 
 		            {
 		                long downloadId = intent.getLongExtra(android.app.DownloadManager.EXTRA_DOWNLOAD_ID, 0);
 		                Log.d(TAG, "new download complete="+downloadId);
 		                android.app.DownloadManager.Query query = new android.app.DownloadManager.Query();	    
 		                query.setFilterById(downloadId);
 		                Cursor c = dm.query(query);
 		                if (c.moveToFirst()) {
 		                    int columnIndex = c.getColumnIndex(android.app.DownloadManager.COLUMN_STATUS);
 		                    if (android.app.DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
 		
 		                        String uriString = c.getString(c.getColumnIndex(android.app.DownloadManager.COLUMN_LOCAL_FILENAME)); 		                        
 		                        if(uriString.startsWith("file://"))
 		                        {
 		                            uriString = uriString.substring(7);
 		                        }
 		                        
 		                        Log.d(TAG, "new downloaded"+uriString);
 		                        if(uriString.endsWith(".apk"))
		                        {
 		                            ApkFileManager.installApk(con, uriString, "", "", false);
		                        }
 		                        else
 		                        {
 		                        	openDownloadsPage(con);
 		                        }
 		                    }
 		                    else if(android.app.DownloadManager.STATUS_FAILED == c.getInt(columnIndex))
 		                    {
 		                    	Log.d(TAG, "new download fail="+downloadId);
 		                    }
 		                }
 		            }
 	            }
            	else
            	{
            		openDownloadsPage(con);
            	}
            }
        }
    };
    
    
    private void openDownloadsPage(Context context) {
        Intent pageView = new Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS);
        pageView.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(pageView);
    }

    
	public void start(Context con)
	{
		if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO)
		{	
		    con.registerReceiver(receiver, new IntentFilter(android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		    con.registerReceiver(receiver, new IntentFilter(android.app.DownloadManager.ACTION_NOTIFICATION_CLICKED));
		} 
	}
	
	public void stop(Context con)
	{
		if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO)
		{	
		    con.unregisterReceiver(receiver);		    
		} 
	}
	
	public void download(Context con, ApkResponse apk)
	{
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == false)
		{
			Toast.makeText(con, R.string.no_sdcard_no_download, Toast.LENGTH_SHORT).show();
			return ;			 
		}
		
		if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO)
		{
			/*
			ContentValues values = new ContentValues();  
			values.put(Downloads.URI, apk.apkurl);  
			//values.put(Downloads.MIMETYPE, "image/jpeg");  
			//values.put(Downloads.FILENAME_HINT, getFullFilename("android_downloadprovider_market.jpg"));  
			values.put(Downloads.TITLE, apk.label);  
			//values.put(Downloads.DESCRIPTION, "screenshot file for DownloadProvider Demo");  
			values.put(Downloads.VISIBILITY, Downloads.VISIBILITY_VISIBLE);  
			values.put(Downloads.NOTIFICATION_CLASS, "com.borqs.qiupu.service.DownloadAdapterService.DownloadReceiver");  
			values.put(Downloads.NOTIFICATION_PACKAGE, getPackageName());
			getContentResolver().insert(Downloads.CONTENT_URI, values);  
			*/
			if (isInDownloading(con,apk.packagename)) {
                Log.i(TAG, "download, ongoing item: " + apk.packagename);
                return;
            }

			android.app.DownloadManager dm = (android.app.DownloadManager) con.getSystemService(Context.DOWNLOAD_SERVICE);
			android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(Uri.parse(apk.apkurl));
			request.setMimeType("application/vnd.android.package-archive");			
			request.setTitle(apk.label);
			request.setVisibleInDownloadsUi(true);
			request.setShowRunningNotification(true);			
						
			//request.setDestinationInExternalPublicDir(QiupuConfig.APP_APK_SDCARD_PATH, apk.packagename+".apk");
			
			final String filestr = QiupuHelper.getDownloadSdcardPath(apk.packagename);
			request.setDestinationUri(Uri.fromFile(new File(filestr)));
			
			if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB)
			{
				try{
				    request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE|android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
				}catch(Exception ne){}
			}
			//request.setNotificationVisibility(true);			
			long enqueue = dm.enqueue(request);
			
			downloadMap.put(new Long(enqueue), apk);
			//downloading, should reflect UI
			
			if(mFileManager != null)
			mFileManager.updateActivityUI(QiupuMessage.MESSAGE_DOWNLOAD_APK_LOADING, mFileManager.createMsg(QiupuMessage.MESSAGE_DOWNLOAD_APK_LOADING, apk));
			//maintain the status for download
			Log.d(TAG, "new download="+enqueue);
		}
		else
		{
			//download directly
			Intent downIntent = new Intent(Intent.ACTION_VIEW);
			downIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			downIntent.setData(Uri.parse(apk.apkurl));
        	try {
			    con.startActivity(downIntent);
			}catch(Exception ne){
				ne.printStackTrace();
			}			
		}
	}

	public void setApkFileManager(ApkFileManager mApkFileManager) {
		mFileManager = 	mApkFileManager;
	}
}
