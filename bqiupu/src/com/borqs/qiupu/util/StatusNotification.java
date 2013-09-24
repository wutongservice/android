package com.borqs.qiupu.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;
import com.borqs.common.util.IntentUtil;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.DownloadManagerActivity;
import com.borqs.qiupu.ui.bpc.RequestActivity;
import twitter4j.ApkResponse;

import java.util.ArrayList;

public class StatusNotification {

	public static final String TAG = "Qiupu.StatusNotification";
	private Context mContext;
	private NotificationManager mNM;
	
	public static final int NOTIFICATION_UPDATE_STATUS = 1;
	public static final int NOTIFICATION_notice        = 2;
	
	public static final int QIUPU_NITIFY_DOWNLOADING_ID  = 3;
	public static final int QIUPU_NITIFY_SUMMARRY_ID     = 4;
	public static final int QIUPU_NITIFY_INTALLING_ID    = 5;
	public static final int QIUPU_NITIFY_REQUESTS_ID     = 6;
	public static ArrayList<NamePackage> packages = new ArrayList<NamePackage>();
	
	private static class NamePackage
	{
		public String label;
		public String packagename;
		
		public NamePackage(String name, String packages)
		{
			label = name;
			packagename = packages;
		}
		
		@Override public boolean equals(Object obj)
		{
			if(!(obj instanceof NamePackage))
			{
				return false;
			}
			NamePackage ap = (NamePackage)obj;
			return (ap.packagename.equals(packagename));		
		}	
	}
	
	private String getCurrendDownloadingApps()
	{
		StringBuilder sb = new StringBuilder();
		synchronized(mLock)
		{
			for(NamePackage np: packages)
			{
				if(sb.length()>0)
					sb.append(", ");
				
				sb.append(np.label);
			}
		}
		
		return sb.toString();
	}

	public StatusNotification(Context ctx) {
		mContext = ctx;
		mNM = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}
		
	private Object mLock = new Object();
	private  void increase(ApkResponse apk)
	{
		synchronized(mLock)
		{
			NamePackage np = new NamePackage(apk.label, apk.packagename);
			if(packages.contains(np) == false)
			{
		        packages.add(np);
			}
		}
	}
	private synchronized void decrease(ApkResponse apk)
	{
		synchronized(mLock)
		{
		    packages.remove(new NamePackage(apk.label, apk.packagename));
		    
		    if(packages.size() == 0)
		    {
		    	mNM.cancel(QIUPU_NITIFY_DOWNLOADING_ID);
		    }
		}
	}
	
	public static void notifyInstalling(Context ctx, String text, String packagename) {
		try{
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_qiupu_launcher;
		
		notification.tickerText = String.format(ctx.getString(R.string.installing_package), text);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
//		notification.contentIntent = PendingIntent.getActivity(ctx, 0,null, PendingIntent.FLAG_ONE_SHOT);
		RemoteViews views = new RemoteViews(ctx.getPackageName() , R.layout.notification_view); 
		views.setImageViewResource(R.id.listicon1, notification.icon);		
		views.setTextViewText(R.id.text1, text);
		views.setTextViewText(R.id.text2, ctx.getString(R.string.string_installing));		
		notification.contentView = views;
		
		NotificationManager MMNM = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		int packageid = (packagename+"installing").hashCode();
		MMNM.notify(packageid, notification);
		}
		catch(Exception ne){ne.printStackTrace();}
	}
	
	public static void notifyRequests(Context ctx, String text, boolean isVibrate) {
		try{
//		Notification notification = new Notification();
		Notification notification = new Notification(R.drawable.ic_bpc_launcher, text, System.currentTimeMillis());
//		notification.icon = R.drawable.ic_bpc_launcher;
		
//		notification.tickerText = text;
		notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE | Notification.DEFAULT_SOUND;
		Log.d(TAG, "notification defaults : " + isVibrate);
		notification.defaults = isVibrate ? Notification.DEFAULT_VIBRATE : Notification.DEFAULT_SOUND;
		
		Intent requestIntent = new Intent();
		requestIntent.setClassName(ctx.getPackageName(), RequestActivity.class.getName());
		requestIntent.putExtra("USER_CONCERN_TYPE", 2);
		
		notification.contentIntent = PendingIntent.getActivity(ctx, 0,requestIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		notification.setLatestEventInfo(ctx, ctx.getString(R.string.home_requests), text, notification.contentIntent);
		
//		RemoteViews views = new RemoteViews(ctx.getPackageName() , R.layout.notification_view); 
//		views.setImageViewResource(R.id.listicon1, notification.icon);		
//		views.setTextViewText(R.id.text1, ctx.getString(R.string.home_requests));
//		views.setTextViewText(R.id.text2, text);		
//		notification.contentView = views;
		
		NotificationManager MMNM = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		MMNM.notify(QIUPU_NITIFY_REQUESTS_ID, notification);
		}
		catch(Exception ne){ne.printStackTrace();}
	}
	
	public static void notifyInstallingCancel(Context ctx, String packagename) {
		NotificationManager MMNM = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		int packageid = (packagename+"installing").hashCode();		
		MMNM.cancel(packageid);
	}
	
	public void notifyOnGoing(String text, Bitmap icon, String packagename, ApkResponse apkinfo) {
		try{
		Intent statusintent = new Intent();
		statusintent.setClassName(mContext.getPackageName(), DownloadManagerActivity.class.getName());
		statusintent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);		
		statusintent.putExtra(QiupuMessage.BUNDLE_APKINFO, apkinfo);
		statusintent.putExtra("from_notify", true);
		statusintent.setAction(Intent.ACTION_VIEW);
		
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_bpc_launcher;
		
		notification.tickerText = mContext.getString(R.string.string_downloading) + text;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.contentIntent = PendingIntent.getActivity(mContext, 0,statusintent, PendingIntent.FLAG_UPDATE_CURRENT);
		RemoteViews views = new RemoteViews(mContext.getPackageName() , R.layout.notification_view); 
		views.setImageViewResource(R.id.listicon1, notification.icon);		
		views.setTextViewText(R.id.text1, text);
		views.setTextViewText(R.id.text2, mContext.getString(R.string.string_downloading));
		//views.setViewVisibility(R.id.text2, View.GONE);
		notification.contentView = views;

		increase(apkinfo);
		
		notification.tickerText = getCurrendDownloadingApps();
		mNM.notify(QIUPU_NITIFY_DOWNLOADING_ID, notification);
		}
		catch(Exception ne){ne.printStackTrace();}
	}
	
	public void notifyChangeOnGoing()
	{
		synchronized(mLock)
		{
			if(packages.size() > 0)
			{
				try{
				String text = getCurrendDownloadingApps();
				Intent statusintent = new Intent();
				statusintent.setClassName(mContext.getPackageName(), DownloadManagerActivity.class.getName());
				statusintent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				statusintent.putExtra("from_notify", true);
				statusintent.setAction(Intent.ACTION_VIEW);
				
				Notification notification = new Notification();
				notification.icon = R.drawable.ic_qiupu_launcher;
				
				notification.tickerText = mContext.getString(R.string.string_downloading) + text;
				notification.flags |= Notification.FLAG_ONGOING_EVENT;
				notification.contentIntent = PendingIntent.getActivity(mContext, 0,statusintent, PendingIntent.FLAG_UPDATE_CURRENT);
				RemoteViews views = new RemoteViews(mContext.getPackageName() , R.layout.notification_view); 
				views.setImageViewResource(R.id.listicon1, notification.icon);		
				views.setTextViewText(R.id.text1, text);
				views.setTextViewText(R.id.text2, mContext.getString(R.string.string_downloading));		
				notification.contentView = views;
				
				mNM.notify(QIUPU_NITIFY_DOWNLOADING_ID, notification);
				}
				catch(Exception ne){ne.printStackTrace();}
			}
			else
			{
				mNM.cancel(QIUPU_NITIFY_DOWNLOADING_ID);
			}
		}
	}
	
	public void notifyFinish(String text, Bitmap icon, String packagename, ApkResponse apkinfo) {
		if(QiupuConfig.LOGD)Log.d(TAG, "notifyFinish  apkinfo:"+apkinfo);
		try{
		Intent statusintent = new Intent();
//		statusintent.setClassName(mContext.getPackageName(), "com.borqs.appbox.ui.ApkDetailInfoActivity");
		statusintent.setClassName(mContext.getPackageName(), DownloadManagerActivity.class.getName());
		statusintent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);		
		statusintent.putExtra(QiupuMessage.BUNDLE_APKINFO, apkinfo);
		statusintent.putExtra("from_notify", true);
		statusintent.setAction(Intent.ACTION_VIEW);
		
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_qiupu_launcher;
		
		notification.tickerText = mContext.getString(R.string.string_downloading_finished) + text;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.contentIntent = PendingIntent.getActivity(mContext, 0,statusintent, PendingIntent.FLAG_UPDATE_CURRENT);
		RemoteViews views = new RemoteViews(mContext.getPackageName() , R.layout.notification_view); 
		views.setImageViewResource(R.id.listicon1, notification.icon);		
		views.setTextViewText(R.id.text1, text);
		views.setTextViewText(R.id.text2, mContext.getString(R.string.string_downloading_finished));
		//views.setViewVisibility(R.id.text2, View.GONE);
		notification.contentView = views;

		final int id = getNotifyID(packagename);	
		decrease(apkinfo);
		
		notifyChangeOnGoing();
		mNM.notify(id, notification);
		}
		catch(Exception ne){ne.printStackTrace();}
	}
	
	public void canelNotify(ApkResponse apkinfo) {
		if(QiupuConfig.LOGD)Log.d(TAG, "notifyFinish  apkinfo:"+apkinfo);
		try{
			decrease(apkinfo);
			
			notifyChangeOnGoing();
		}
		catch(Exception ne){ne.printStackTrace();}
	}
	
	
	
	public void notifyFail(String text, Bitmap icon, String packagename, ApkResponse apkinfo) {
		try{
//		Intent statusintent = new Intent();
//		statusintent.setClassName(mContext.getPackageName(), "com.borqs.appbox.ui.ApkDetailInfoActivity");
//		statusintent.setClassName(mContext.getPackageName(), ApkDetailInfoActivity.class.getName());
//            statusintent.putExtra(QiupuMessage.BUNDLE_APKINFO, apkinfo);
            Intent statusintent = IntentUtil.getApkDetailIntent(mContext, apkinfo);

            statusintent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		statusintent.putExtra("from_notify", true);
		statusintent.setAction(Intent.ACTION_VIEW);
		
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_qiupu_launcher;
		
		notification.tickerText = mContext.getString(R.string.string_downloading_fail) + text;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.contentIntent = PendingIntent.getActivity(mContext, 0,statusintent, PendingIntent.FLAG_UPDATE_CURRENT);
		RemoteViews views = new RemoteViews(mContext.getPackageName() , R.layout.notification_view); 
		views.setImageViewResource(R.id.listicon1, notification.icon);		
		views.setTextViewText(R.id.text1, text);
		views.setTextViewText(R.id.text2, mContext.getString(R.string.string_downloading_fail));
		//views.setViewVisibility(R.id.text2, View.GONE);
		notification.contentView = views;

		final int id = getNotifyID(packagename);	
		decrease(apkinfo);
		
		notifyChangeOnGoing();
		mNM.notify(id, notification);		
		}
		catch(Exception ne){ne.printStackTrace();}
	}	
		 
	private int getNotifyID(String packageName)
	{
		return packageName.hashCode();
	}
	
	public void cancel(String packageName) {
		if (mNM != null)
		{
			int id = getNotifyID(packageName);
			Log.d(TAG, "cancel notify id="+packageName);
			mNM.cancel(id);			
			
			ApkResponse apk = new ApkResponse();
			apk.label="";
			apk.packagename=packageName;
			decrease(apk);
		}
	}
	
	public void cancelNotification()
	{
		if (mNM != null)
			mNM.cancel(NOTIFICATION_notice);
	}
}
