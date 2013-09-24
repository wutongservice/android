package com.borqs.appupdate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import android.net.TrafficStats;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class CheckService extends IntentService {



	public CheckService() {
		super("CheckVersionService");
	}

	static int connectionTimeout = 20*1000;
	static int readTimeout       = 120000;
	@Override
	protected void onHandleIntent(Intent intent) {
		if(intent.getAction().equals("android.intent.action.BORQSAPP_VERSION_UPDATE"))
		{
			String packageName = intent.getStringExtra("package");
			String versionCode = intent.getStringExtra("version_code");
			boolean force      = intent.getBooleanExtra("force_install", false);
			
			
			HashMap<String,String> map = new HashMap<String,String>();
			map.put("apps", packageName);
			map.put("history_version",     "false");
			
			String url = "http://api.borqs.com/qiupu/app/get";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                TrafficStats.setThreadStatsTag(0xB0AF);
			try {
				HttpURLConnection connection = (HttpURLConnection)(new URL(url).openConnection());
				
				connection.setConnectTimeout(connectionTimeout);			      
				connection.setReadTimeout(readTimeout);  
				
				
				connection.setDoInput(true);
				connection.addRequestProperty("apps", packageName);                
				connection.setRequestMethod("GET");
				

		        int statusCode = connection.getResponseCode();
		        InputStream is = null;
		        if(null == (is = connection.getErrorStream())){
		            is = connection.getInputStream();
		        }
		        if (null != is && "gzip".equals(connection.getContentEncoding())) {
		            // the response is gzipped
		            is = new GZIPInputStream(is);
		        }
		        
		        if (statusCode == 200)
		        {
		        	StringBuilder sb = new StringBuilder();
		        	asString(sb, is);	
		        	
		        	try {
		    			JSONArray arrlist = new JSONArray(sb.toString());
		    			if(arrlist.length() > 0)
		    			{
		    				JSONObject obj;
		    				try {
		    					obj = arrlist.getJSONObject(0);
		    					
		    					ApkInfo info = new ApkInfo();
		    					info.packagename = packageName;
		    					info.recent_change        = obj.getString("recent_change");
		    					info.apksize              = obj.getLong("file_size");
		    					info.apkurl               = obj.getString("file_url");
		    					info.targetSdkVersion     = obj.getInt("target_sdk_version");
		    					
		    					info.latest_versioncode   = obj.getInt("lasted_version_code");
		    					info.latest_versionname   = obj.getString("lasted_version_name");
		    					
		    					processReturnString(getApplicationContext(), info, versionCode, force);
		    				} catch (JSONException e) {}
		    			}
		    			else
		    			{
		    				return ;
		    			}
		    		} catch (Exception e) {		    			
		    		}		
		        }
		        
		        try{
		        	connection.disconnect();
		        }catch(Exception ne){}
				
			} catch (MalformedURLException e) {				
				e.printStackTrace();
			} catch (IOException e) {				
				e.printStackTrace();
			} finally {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                    TrafficStats.clearThreadStatsTag();
            }
		}else if(intent.getAction().equals("android.intent.action.BORQSAPP_SHOW_DIALOG")){
			final String packagename = intent.getStringExtra("packagename");
			final String url = intent.getStringExtra("url");
			final long  size = intent.getLongExtra("size", 0);
			final String version_name  = intent.getStringExtra("latest_versionname");
			String recent_change = intent.getStringExtra("recent_change");	
			
			String msg = String.format("Name: %1$s\nSize: %2$s\nRecent Changes:%3$s", version_name, size, recent_change);
			 new AlertDialog.Builder(this).
			 setTitle("New Version").
			 setMessage(msg).
			 setPositiveButton("Download",
             new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                	 downloadAndInstall(CheckService.this, packagename, version_name, url, size);
                 }
             }).setNegativeButton("Later",
             new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                 }
             }).create().show();
			
		}else if(intent.getAction().equals("android.intent.action.BORQSAPP_DOWNLOAD_APP")){
			String packagename = intent.getStringExtra("packagename");
			String url = intent.getStringExtra("url");
			long  size = intent.getLongExtra("size", 0);
			String version_name  = intent.getStringExtra("latest_versionname");
			String recent_change = intent.getStringExtra("recent_change");
			
			downloadAndInstall(CheckService.this, packagename, version_name, url, size);
		}
	}
	
	public static class ApkInfo{
		String packagename   ;
		String recent_change ;
		long   apksize       ;
		String apkurl        ;
		int targetSdkVersion ;
		
		int  latest_versioncode   ;
		String latest_versionname ;
	}
	
	private static void processReturnString(Context con, ApkInfo info, String localVersionCode, boolean force)
	{
		if(info.latest_versioncode > Long.parseLong(localVersionCode) )
		{
			if(force == true)
			{
				Intent intent = new Intent("android.intent.action.BORQSAPP_DOWNLOAD_APP");
				intent.putExtra("packagename",   info.packagename);
				intent.putExtra("url",           info.apkurl);
				intent.putExtra("size",          info.apksize);
				intent.putExtra("version_name",  info.latest_versionname);
				intent.putExtra("recent_change", info.recent_change);
				
				con.sendBroadcast(intent);
			}
			else
			{
				Intent intent = new Intent("android.intent.action.BORQSAPP_SHOW_DIALOG");
				intent.putExtra("packagename",   info.packagename);
				intent.putExtra("url",           info.apkurl);
				intent.putExtra("size",          info.apksize);
				intent.putExtra("version_name",  info.latest_versionname);
				intent.putExtra("recent_change", info.recent_change);
				
				con.sendBroadcast(intent);
			}
		}
	}
	
	private static void downloadAndInstall(Context con, String packagename, String version_name, String url, long size)
	{
		DownLoadFile.dowloadFile(con, packagename, version_name, url, size);
	}
	
	private static void asString(StringBuilder sb, InputStream stream )
	{		
        BufferedReader br;
        try {
            if (null == stream) {
                return ;
            }
            br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuffer buf = new StringBuffer();
            String line;
            while (null != (line = br.readLine())) {
                buf.append(line).append("\n");
            }
            
            sb.append(buf.toString());
           
            stream.close();                
        } catch (NullPointerException npe) {
        } catch (IOException ioe) {
        }finally{
            if(null != stream){
                try {
                    stream.close();
                } catch (IOException ignore) {
                }
            }
        }
	}
	
	static HashMap<Long, String> downloadMap = new HashMap<Long, String>();
	
	public static boolean isInDownloading(Context con, String packagename)
	{
		Set<Long> sets = downloadMap.keySet();
		Iterator<Long> it = sets.iterator();
		while(it.hasNext())
		{
			long key = it.next();			
			
			String apk = downloadMap.get(key);
			
			if(apk.equals(packagename))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static class DownLoadFile {
		 private static final String TAG = "Qiupu.DownLoadFile";
		 
		 public static boolean dowloadFile(Context con, String packagename, String VersionName, String url,long filesize){         
		    Log.d(TAG,"dowloadFile url:"+url);
		     
		    String filePath = con.getFilesDir().getAbsolutePath() + "/"+packagename + ".apk";		    
		    File finalfile = new File(filePath);				
            if(finalfile.exists() == true)
            {
            	finalfile.delete();
            }
            
            try {
				finalfile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                 TrafficStats.setThreadStatsTag(0xB0AF);
		     HttpURLConnection conn = null;
	            try {
	                OutputStream os = new FileOutputStream(finalfile);
	                conn = (HttpURLConnection)new URL(url).openConnection();    
	                //TODO
	                Toast.makeText(con, "Begin download load "+VersionName, Toast.LENGTH_SHORT);
	                
	                conn.setConnectTimeout(15*1000);
	                conn.setReadTimeout(60*1000);
	                InputStream in =conn.getInputStream();
	                
	                int len = -1;                
	                
	                long contentLen = conn.getContentLength();                
	                if(contentLen <=0)
	                {
	                	contentLen = filesize;
	                }
	                
	                byte []buf = new byte[1024*4];
	                long processedsize = 0;
	                while((len = in.read(buf, 0, 1024*4)) > 0)
	                {   
	                	os.write(buf, 0, len);
	                	processedsize += len;
	                	
	                	/*
	                	if(((int)((processedsize/(1.f*contentLen))*100))%10 == 0) {
	                		Toast.makeText(con, "Begin download load "+VersionName, Toast.LENGTH_SHORT);	                	
	                    }*/
	                }
	                
	                buf = null;
	                in.close();
	                os.close();
	                
	                Intent intent = new Intent(Intent.ACTION_VIEW);
	 	            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	 	            intent.setDataAndType(Uri.fromFile(finalfile), "application/vnd.android.package-archive");
	 	            con.startActivity(intent);
	                
	            } catch(Exception ne) {	            	
	            	Log.d(TAG,"===download file exception="+ne.getMessage()+"=="+ne.getLocalizedMessage());
	                ne.printStackTrace();
	            } finally {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                        TrafficStats.clearThreadStatsTag();
	                if(conn != null)
	                {
	                    conn.disconnect();
	                }
	            }  
			return true;
	    }	
   }
}
