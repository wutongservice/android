package com.borqs.information.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.listener.NotificationListener;
import com.borqs.information.InformationBase;
import com.borqs.information.InformationDownloadService;
import com.borqs.information.InformationHttpPushReceiver;
import com.borqs.information.db.Notification.NotificationColumns;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.util.LeftMenuMapping;
import com.borqs.qiupu.util.StringUtil;

public class InformationUtils {
	private static final String TAG = "InformationUtils";
	
	private static final String sMethodShowUser = "user/show"; 
	
	public static boolean dismissNotifyById(Context context, long msg_id) {
		ContentValues values = new ContentValues();
		values.put(NotificationColumns.PROCESSED, true);
		return context.getContentResolver().update(ContentUris.withAppendedId(NotificationColumns.CONTENT_URI_WITHOUT_NOTIFY, msg_id), values, NotificationColumns.U_ID + "=?", new String[] {Long.toString(AccountServiceUtils.getBorqsAccountID())}) > 0;
	}
	
	public static boolean isIntentAvailable(Context context, String action) {
		if(TextUtils.isEmpty(action)) {
			return false;
		}
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(action);
	    List<ResolveInfo> list =
	            packageManager.queryIntentActivities(intent,
	                    PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}
	
	public static String call(String method,String[]paramNames,String []paramValues)
	{
		String result = null;
		AndroidHttpClient client = null;
		try {
			String urlBase = InformationConstant.getBorqsURL()+method;
			String paramStr = "";
			if(null!=paramNames)
			{
				paramStr = "?";
				for(int i=0;i<paramNames.length;i++)
				{
					paramStr += paramNames[i]+"="+paramValues[i];
					if(i!=paramNames.length-1)
					{
						paramStr +="&";
					}
				}
			}
			HttpGet httpGet = new HttpGet(urlBase+paramStr);
			httpGet.setHeader("Accept-Encoding", "gzip,deflate");

            client = AndroidHttpClient.newInstance(InformationConstant.USERAGENT);
            HttpResponse httpResponse = executeRequest(client, httpGet);
			if (isResponseStatusOk(httpResponse)) {
				HttpEntity entity = httpResponse.getEntity();
				Header ceheader = entity.getContentEncoding();
				if (ceheader != null) {
					HeaderElement[] codecs = ceheader.getElements();
					for (int i = 0; i < codecs.length; i++) {
						if (codecs[i].getName().equalsIgnoreCase("gzip")) {
							entity = new GzipDecompressingEntity(entity);
							break;
						}
					}
				}
				result = EntityUtils.toString(entity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return result;
	}
	
	static class GzipDecompressingEntity extends HttpEntityWrapper {
		public GzipDecompressingEntity(final HttpEntity entity) {
			super(entity);
		}

		@Override
		public InputStream getContent() throws IOException,
				IllegalStateException {
			InputStream wrappedin = wrappedEntity.getContent();

			return new GZIPInputStream(wrappedin);
		}

		@Override
		public long getContentLength() {
			// length of ungzipped content is not known
			return -1;
		}
	}
	
	public static ArrayList<InformationBase> downloadMessageTopN(Context context, String ticket, boolean isToMe, final String sceneId) throws JSONException {
		return parseJSON(context, downloadMessageJSONTopN(context, ticket, isToMe, sceneId));
	}
	
	public static ArrayList<InformationBase> downloadMessage(Context context, String ticket,String uid,long slastModified, boolean isToMe) throws JSONException {
		return parseJSON(context, downloadMessageJSON(context, ticket, slastModified, isToMe));
	}
	
	public static ArrayList<InformationBase> downloadMessageWithType(Context context, String ticket, long slastModified, boolean isToMe, int readstatus) throws JSONException {
		return parseJSON(context, downloadMessageJSONwithType(context, ticket, slastModified, isToMe, readstatus));
	}

	public static boolean setProcessed(Context context, String ticket, long id) {
		Uri.Builder uriBuilder = new Uri.Builder().scheme(InformationConstant.HTTP_SCHEME)
		.encodedAuthority(InformationConstant.getBorqsHost2(context))
		.encodedPath(InformationConstant.NOTIFICATION_HOST_DONE_PATH)
		.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_TICKET, ticket)
		.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_MID, String.valueOf(id));
		
		AndroidHttpClient client = null;
		try {
			HttpPut httpput = new HttpPut(uriBuilder.build().toString());
            client = AndroidHttpClient.newInstance(InformationConstant.USERAGENT);
            HttpResponse httpResponse = executeRequest(client, httpput);
			if (isResponseStatusOk(httpResponse)) {
				String res = EntityUtils.toString(httpResponse.getEntity());
				Log.d(TAG, "setProcessed response = " + res);
			}
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return true;
	}
	
	public static boolean setRead(Context context, String ticket, String ids) {
		Uri.Builder uriBuilder = new Uri.Builder().scheme(InformationConstant.HTTP_SCHEME)
		.encodedAuthority(InformationConstant.getBorqsHost2(context))
		.encodedPath(InformationConstant.NOTIFICATION_HOST_READ_PATH)
		.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_TICKET, ticket)
		.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_MID, ids);
		
		AndroidHttpClient client = null;
		try {
			HttpPut httpput = new HttpPut(uriBuilder.build().toString());
            client = AndroidHttpClient.newInstance(InformationConstant.USERAGENT);
			HttpResponse httpResponse = executeRequest(client, httpput);
			if (isResponseStatusOk(httpResponse)) {
				String res = EntityUtils.toString(httpResponse.getEntity());
				Log.d(TAG, "setRead response = " + res);
			}
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return true;
	}
	
	private static boolean TEST = false;
	private static JSONObject downloadMessageJSON(Context context, String ticket, long last, boolean isToMe) {
		long weekAgo = System.currentTimeMillis() - 7*24*60*60*1000L;
		Uri.Builder uriBuilder = new Uri.Builder().scheme(InformationConstant.HTTP_SCHEME)
		.encodedAuthority(InformationConstant.getBorqsHost2(context))
		.encodedPath(InformationConstant.NOTIFICATION_HOST_LIST_BY_TIME)
		.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_TICKET, ticket)
		.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_STATUS, "0")
		.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_COUNT, String.valueOf(InformationConstant.DEFAULT_EARLYNOTIFICATION_COUNT));
		if(last > 0) {
//			uriBuilder.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_FROM, String.valueOf(weekAgo > last ? weekAgo : (last + 1000)));
			uriBuilder.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_FROM, String.valueOf(last));
		}
		if(isToMe) {
			uriBuilder.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_TYPE, String.valueOf(InformationConstant.NOTIFICATION_INTENT_PARAM_TOME));
		}else {
			uriBuilder.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_TYPE, String.valueOf(InformationConstant.NOTIFICATION_INTENT_PARAM_NO_TOME));
		}
		
		final String sceneId = QiupuORM.getSettingValue(context, QiupuORM.HOME_ACTIVITY_ID);
		uriBuilder.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_SCENEID, String.valueOf(sceneId));
		
		AndroidHttpClient client = null;
		JSONObject obj = null;
		try {
			HttpGet httpGet = new HttpGet(uriBuilder.build().toString());
			Log.d(TAG, "fetch notification url = " + httpGet.getURI());
            client = AndroidHttpClient.newInstance(InformationConstant.USERAGENT);
			HttpResponse httpResponse = executeRequest(client, httpGet);
			if (isResponseStatusOk(httpResponse)) {
				
				if(TEST)
				{
					InputStream im = context.getResources().openRawResource(R.raw.testdata);
					char[] buffer = new char[4096];
					InputStreamReader isr = new InputStreamReader(im, "UTF-8");
					isr.read(buffer);
					
					//String res = EntityUtils.toString(httpResponse.getEntity());
					if (!TextUtils.isEmpty(new String(buffer))) {
						obj = new JSONObject(new String(buffer)).getJSONObject("result");
					}
				}
				else
				{
					String res = EntityUtils.toString(httpResponse.getEntity());
					if (!TextUtils.isEmpty(res)) {
						obj = new JSONObject(res).getJSONObject("result");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Information Download", "Informations List Download Failed.");
		} finally {
			if (client != null) {
				client.close();
			}
		}
		
		return obj;
	}
	private static JSONObject downloadMessageJSONwithType(Context context, String ticket, long last, boolean isToMe, int readstatus) {
		long weekAgo = System.currentTimeMillis() - 7*24*60*60*1000L;
		Uri.Builder uriBuilder = new Uri.Builder().scheme(InformationConstant.HTTP_SCHEME)
		.encodedAuthority(InformationConstant.getBorqsHost2(context))
		.encodedPath(InformationConstant.NOTIFICATION_HOST_LIST_BY_TIME)
		.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_TICKET, ticket)
		.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_STATUS, "0")
		.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_READ, String.valueOf(readstatus));
		if(isToMe) {
			uriBuilder.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_TYPE, String.valueOf(InformationConstant.NOTIFICATION_INTENT_PARAM_TOME));
		}else {
			uriBuilder.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_TYPE, String.valueOf(InformationConstant.NOTIFICATION_INTENT_PARAM_NO_TOME));
		}
		if(last > 0) {
//			uriBuilder.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_FROM, String.valueOf(weekAgo > last ? weekAgo : (last + 1000)));
			uriBuilder.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_FROM, String.valueOf(last));
			uriBuilder.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_COUNT, String.valueOf(InformationConstant.DEFAULT_EARLYNOTIFICATION_COUNT));
		}
		AndroidHttpClient client = null;
		JSONObject obj = null;
		try {
			HttpGet httpGet = new HttpGet(uriBuilder.build().toString());
			Log.d(TAG, "fetch notification url = " + httpGet.getURI());
            client = AndroidHttpClient.newInstance(InformationConstant.USERAGENT);
			HttpResponse httpResponse = executeRequest(client, httpGet);
			if (isResponseStatusOk(httpResponse)) {
				
				if(TEST)
				{
					InputStream im = context.getResources().openRawResource(R.raw.testdata);
					char[] buffer = new char[4096];
					InputStreamReader isr = new InputStreamReader(im, "UTF-8");
					isr.read(buffer);
					
					//String res = EntityUtils.toString(httpResponse.getEntity());
					if (!TextUtils.isEmpty(new String(buffer))) {
						obj = new JSONObject(new String(buffer)).getJSONObject("result");
					}
				}
				else
				{
					String res = EntityUtils.toString(httpResponse.getEntity());
					if (!TextUtils.isEmpty(res)) {
						obj = new JSONObject(res).getJSONObject("result");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Information Download", "Informations List Download Failed.");
		} finally {
			if (client != null) {
				client.close();
			}
		}
		
		return obj;
	}
	private static JSONObject downloadMessageJSONTopN(Context context, String ticket,  boolean isToMe, final String sceneId) {
		Uri.Builder uriBuilder = new Uri.Builder().scheme(InformationConstant.HTTP_SCHEME)
		.encodedAuthority(InformationConstant.getBorqsHost2(context))
		.encodedPath(InformationConstant.NOTIFICATION_HOST_TOP_PATH)
		.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_TICKET, ticket)
		.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_STATUS, "0")
		.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_TOPN, String.valueOf(InformationConstant.DEFAULT_NOTIFICATION_COUNT));
		
		if(isToMe) {
			uriBuilder.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_TYPE, String.valueOf(InformationConstant.NOTIFICATION_INTENT_PARAM_TOME));
		}else {
			uriBuilder.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_TYPE, String.valueOf(InformationConstant.NOTIFICATION_INTENT_PARAM_NO_TOME));
		}
		
		// add scene
		uriBuilder.appendQueryParameter(InformationConstant.NOTIFICATION_REQUEST_PARAM_SCENEID, String.valueOf(sceneId));
		
		AndroidHttpClient client = null;
		JSONObject obj = null;
		try {
			HttpGet httpGet = new HttpGet(uriBuilder.build().toString());
			Log.d(TAG, "fetch notification url = " + httpGet.getURI());
            client = AndroidHttpClient.newInstance(InformationConstant.USERAGENT);
			HttpResponse httpResponse = executeRequest(client, httpGet);
			if (isResponseStatusOk(httpResponse)) {
				
				if(TEST)
				{
					InputStream im = context.getResources().openRawResource(R.raw.testdata);
					char[] buffer = new char[4096];
					InputStreamReader isr = new InputStreamReader(im, "UTF-8");
					isr.read(buffer);
					
					//String res = EntityUtils.toString(httpResponse.getEntity());
					if (!TextUtils.isEmpty(new String(buffer))) {
						obj = new JSONObject(new String(buffer)).getJSONObject("result");
					}
				}
				else
				{
					String res = EntityUtils.toString(httpResponse.getEntity());
					if (!TextUtils.isEmpty(res)) {
						obj = new JSONObject(res).getJSONObject("result");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Information Download", "Informations List Download Failed.");
		} finally {
			if (client != null) {
				client.close();
			}
		}
		
		return obj;
	}
	private static ArrayList<InformationBase> parseJSON(Context ctx, JSONObject jsonObject) throws JSONException {
		if(jsonObject == null) {
			return null;
		}
		final int messagesCount = jsonObject.getInt("count");
		ArrayList<InformationBase> messages = new ArrayList<InformationBase>(messagesCount);
		
		if (messagesCount > 0) {
			JSONArray jsonArray = jsonObject.getJSONArray("informations");
			
			Set<String> senderSet = new HashSet<String>();
			for (int i = 0; i < messagesCount; i++) {
				InformationBase ms = parseJSONItem(ctx, jsonArray.getJSONObject(i));
				if (!TextUtils.isEmpty(ms.senderId)) {
					// We can not just get this information from local.
					// For example, "ABC just change his photo.", we need to fetch the latest photo from server rather
					// than getting it from local.
					
					// 0 will be treated as invalided user id.
					if (!"0".equals(ms.senderId)) {
						senderSet.add(ms.senderId);
					} else {
						ms.image_url = "";
					}

					messages.add(ms);
				}
			}
			
			//if no need to fetch from server
			if(senderSet.size() > 0)
			{
				StringBuffer idlist = new StringBuffer();
				Iterator<String> it = senderSet.iterator();
				while(it.hasNext()) {
					idlist.append(it.next()).append(',');
				}
				senderSet.clear();
				
				String jstr = InformationUtils.call(sMethodShowUser, 
						new String[]{"users","columns"}, new String[]{idlist.toString(),"user_id,image_url"});

				HashMap<String, JSONObject> map = new HashMap<String, JSONObject>();
				if(!TextUtils.isEmpty(jstr)) {
					JSONArray arrsy = new JSONArray(jstr);
					final int lenght = arrsy.length();
					for (int i = 0; i < lenght; i++) {
						JSONObject obj = (JSONObject) arrsy.get(i);
						String id = obj.getString("user_id");
						map.put(id, obj);
					}	
				}
				
				for (InformationBase ms : messages) {
					JSONObject obj = map.get(ms.senderId);
					if(null != obj)
					{
						String url = obj.getString("image_url");
						ms.image_url = url;
					}
				}
			}
		}
		return messages;
	}
	
	private static InformationBase parseJSONItem(Context ctx, JSONObject jo)
			throws JSONException {
		InformationBase ms = new InformationBase();
		ms.appId = jo.getString("appId");
		ms.date  = (jo.getLong("date"));
		ms.id    = (jo.getInt("id"));
		ms.receiverId = (jo.getString("receiverId"));
		ms.senderId   = jo.getString("senderId");
		
		ms.title      = (jo.getString("title"));
		try {
			if(!jo.isNull("body")) {
				ms.body = (jo.getString("body"));
			}
			if(!jo.isNull("bodyHtml"))  {
				ms.body_html = (jo.getString("bodyHtml"));
			}
			if(!jo.isNull("titleHtml"))  {
				ms.title_html = (jo.getString("titleHtml"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		ms.type       = (jo.getString("type"));
		//
		if(ms.type.equalsIgnoreCase("ntf.create_account"))
		{
			/*
			"user_id":10000,
			"login_phone1":13810101010,
			"login_email1":"",
			"display_name":"刘华东"
			*/
			try{
				JSONTokener jsonTokener = new JSONTokener(jo.getString("data"));
                JSONObject dataoj = new JSONObject(jsonTokener);
				String username = "";
				try{
				    String phone = dataoj.getString("login_phone1");
				    if(StringUtil.isEmpty(phone) == false)
				        username = queryContact(ctx, ms, phone, null, null);
				    
				}catch(Exception ne){}
				
				String userid = "";
				try{
				    userid = dataoj.getString("user_id");
				}catch(Exception ne){}
				
				if(StringUtil.isEmpty(username))
				{
					try{
					    String email = dataoj.getString("login_email1");
					    if(StringUtil.isEmpty(email) == false)
					        username = queryContact(ctx, ms, null, email, null);
					}catch(Exception ne){}
				}
				
				if(StringUtil.isEmpty(username))
				{
					try{
					    String display_name = dataoj.getString("display_name");
					    if(StringUtil.isEmpty(display_name) == false)
					        username = queryContact(ctx, ms, null, null, display_name);
					}catch(Exception ne){}
				}
				
				if(StringUtil.isEmpty(username) == false)
				{
					//Ignore
					ms.title_html = String.format(ctx.getString(R.string.contact_friends_activated), username, userid);;
					ms.title = "";
					ms.body  = "";
					ms.body_html = "";
				}
			}catch(Exception ne){
				Log.d(TAG, " error="+ne.getMessage());
			}
		}
		
		ms.uri        = (jo.getString("uri"));
		ms.data       = jo.getString("data");
		ms.lastModified = jo.getLong("lastModified");
		ms.read = jo.getBoolean("read");
		ms.scene = jo.optLong("scene");
		return ms;
	}
	static String[] dispalyNameproject = new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID};
	static String[] emailproject = new String[]{ContactsContract.CommonDataKinds.Email.DISPLAY_NAME, ContactsContract.CommonDataKinds.Email.CONTACT_ID};
	static String[] phoneproject = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
	private static String queryContact(Context ctx, InformationBase ms, String phone, String email, String display_name)
	{
		String name = "";
		try{
			if(StringUtil.isEmpty(phone) == false)
			{
				Cursor phonecursor = ctx.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, phoneproject,
		                ContactsContract.CommonDataKinds.Phone.NUMBER + " = \'" + phone +"\'", null, null);
				
				if(phonecursor.moveToFirst())
				{
				    name = phonecursor.getString(0);
				    ms.apppickurl = ContentUris.withAppendedId(Contacts.CONTENT_URI, phonecursor.getInt(1));
				}
				phonecursor.close();			
			}
			
			if(StringUtil.isEmpty(name) && StringUtil.isEmpty(email) == false)
			{
				Cursor emailcursor = ctx.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, emailproject,
		                 ContactsContract.CommonDataKinds.Email.ADDRESS + " = \'" + email+"\'" , null, null);
				if(emailcursor.moveToFirst())
				{
				    name = emailcursor.getString(0);
				    ms.apppickurl = ContentUris.withAppendedId(Contacts.CONTENT_URI, emailcursor.getInt(1));
				}
				emailcursor.close();		
			}
			
			if(StringUtil.isEmpty(name) && StringUtil.isEmpty(display_name))
			{
				Cursor namecursor = ctx.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, dispalyNameproject,
						ContactsContract.Contacts.DISPLAY_NAME + " = \'" + display_name+"\'" , null, null);
				
				if(namecursor.moveToFirst())
				{
				    name = namecursor.getString(0);
				    ms.apppickurl = ContentUris.withAppendedId(Contacts.CONTENT_URI, namecursor.getInt(1));
				}
				namecursor.close();	
			}
		}catch(Exception ne){
			ne.printStackTrace();
		}
		return name;
	}
	
	public static void sortMessageList(List<InformationBase> messages) {
		Collections.sort(messages, new Comparator<InformationBase>() {
			@Override
			public int compare(InformationBase ms1, InformationBase ms2) {
				return (int) (ms2.lastModified - ms1.lastModified);//(ms2.id - ms1.id);
			}
		});
	}

    public static boolean isActivityOnTop(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String className = mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return LeftMenuMapping.getIndex(className) > 0;
    }

	public static boolean isActivityOnTop(Context context, String activity) {
		if(TextUtils.isEmpty(activity)) {
			return false;
		}
		ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		String className = mActivityManager.getRunningTasks(1).get(0).topActivity
				.getClassName();
		return activity.equals(className);
	}
	
	public static void getInforByDelay(Context context, long delay) {
		InforGetRecord.scheduleGet(context, delay);
	}

    private static final long MIN_SCHEDULE_GET_DELAY = 10000L; // 10 seconds
    private static final long MAX_SCHEDULE_GET_DELAY = 60000L; // 1 minute
    private static long mCurrentDelay = MIN_SCHEDULE_GET_DELAY;
    public static void scheduleGetInfo(Context context, boolean hasNew) {
        if (hasNew) {
            mCurrentDelay = MIN_SCHEDULE_GET_DELAY;
        } else {
            if (mCurrentDelay < MAX_SCHEDULE_GET_DELAY) {
                mCurrentDelay *= 2;
                if (mCurrentDelay > MAX_SCHEDULE_GET_DELAY) {
                    mCurrentDelay = MAX_SCHEDULE_GET_DELAY;
                }
            }
        }
        InforGetRecord.scheduleGet(context, mCurrentDelay);
    }
	
	public static void cancelScheduledInforGet() {
		InforGetRecord.cancelScheduledGet();
	}
	
	static class InforGetRecord {
		
		private final static String NOTIFICATION_POST_TOKEN = "notification_delayed_post";
		private final static String NOTIFICATION_POST_TOKEN_TOME = "notification_delayed_post_tome";
		private static Handler mHandler;
		
		public static void scheduleGet(final Context context, long delay) {
			if (!hasMessages(NOTIFICATION_POST_TOKEN)) {
				mHandler = new Handler();
				mHandler.postAtTime(new Runnable() {
					@Override
					public void run() {
//						if (!InformationDownloadService.isDownloadServiceRunning()) {
							cancelScheduledGet();
							Intent intent = new Intent(context, InformationDownloadService.class);
							intent.putExtra(InformationConstant.NOTIFICATION_INTENT_SYNC_TYPE, InformationDownloadService.sync_topN_other);
							context.startService(intent);
//						}
//						else
//						{
//							//notify to update old UI, it is just to update the UI
//							context.getContentResolver().notifyChange(NotificationColumns.CONTENT_URI, null);
//						}
					}
				}, NOTIFICATION_POST_TOKEN, SystemClock.uptimeMillis() + delay);
			}
			
			if (!hasMessages(NOTIFICATION_POST_TOKEN_TOME)) {
				mHandler = new Handler();
				mHandler.postAtTime(new Runnable() {
					@Override
					public void run() {
//						if (!InformationDownloadService.isDownloadServiceRunning()) {
							cancelScheduleToMeGet();
							Intent intent = new Intent(context, InformationDownloadService.class);
							intent.putExtra(InformationConstant.NOTIFICATION_INTENT_SYNC_TYPE, InformationDownloadService.sync_topN_tome);
							intent.putExtra(InformationConstant.NOTIFICATION_INTENT_PARAM_ISTOME, true);
							context.startService(intent);
//						}
//						else
//						{
//							//notify to update old UI, it is just to update the UI
//							context.getContentResolver().notifyChange(NotificationColumns.CONTENT_URI, null);
//						}
					}
				}, NOTIFICATION_POST_TOKEN_TOME, SystemClock.uptimeMillis() + delay + 2 * QiupuConfig.A_SECOND);
			}
		}
		
		public static void cancelScheduledGet() {
			removeCallbacksAndMessages(InforGetRecord.NOTIFICATION_POST_TOKEN);
			mHandler = null;
		}
		
		public static void cancelScheduleToMeGet() {
			removeCallbacksAndMessages(InforGetRecord.NOTIFICATION_POST_TOKEN_TOME);
		}
		
		private static void removeCallbacksAndMessages(Object token) {
			if(mHandler != null) {
				mHandler.removeCallbacksAndMessages(token);
			}
		}
		
		private static boolean hasMessages(Object token) {
			if(mHandler == null) {
				return false;
			}
			if (mHandler.hasMessages(0, token)) {
				return true;
			}
			return false;
		}

	}
	
	public static boolean setReadStatus(final Context context, final String ids) {
	    boolean res = setRead(context, AccountServiceUtils.getSessionID(), ids);
	    if(!res) {
	        Log.d(TAG, "set read failed : " + ids);
	    }else {
	        Log.d(TAG, "set read suc: " + ids);
	    }
	    return res;
	}
	
	public static void showSysNotification(Context context, int count) {
	    String s = String.format(context.getText(R.string.new_notification_content).toString(), count);
        Notification notification = new Notification(R.drawable.ic_bpc_launcher, s, System.currentTimeMillis());
        notification.number = count;
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        Intent mainActivityIntent = new Intent("com.borqs.bpc.action.NOTIFICATION");
        mainActivityIntent.putExtra("isFromNotice", true);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, 0);
        notification.setLatestEventInfo(context, context.getText(R.string.app_name), s, contentIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(InformationHttpPushReceiver.HTTPPUSH, notification);
	}

    public static HttpResponse executeRequest(AndroidHttpClient client, HttpGet httpGet) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                                TrafficStats.setThreadStatsTag(0xB0AB);
        try {
//            client = AndroidHttpClient.newInstance(InformationConstant.USERAGENT);
			return client.execute(httpGet);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                                TrafficStats.clearThreadStatsTag();
        }
    }

    public static HttpResponse executeRequest(AndroidHttpClient client, HttpPut httpput) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            TrafficStats.setThreadStatsTag(0xB0AB);
        try {
            return client.execute(httpput);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                TrafficStats.clearThreadStatsTag();
        }
    }

    private static boolean isResponseStatusOk(HttpResponse response) {
        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return true;
            }
        }
        return false;
    }
    
    
    private static final HashMap<String,WeakReference<NotificationListener>> listeners = new HashMap<String,WeakReference<NotificationListener>>();
    
	public static void registerNotificationListener(String key,NotificationListener listener){
		synchronized(listeners)
		{
			WeakReference<NotificationListener> ref = listeners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			listeners.put(key, new WeakReference<NotificationListener>(listener));
		}
	}
	
	public static void unregisterNotificationListener(String key){
		synchronized(listeners)
		{
			WeakReference<NotificationListener> ref = listeners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			listeners.remove(key);
		}
	}
	
	public static void updateNtfUI(final boolean isToMe, final int count){

		synchronized(listeners)
        {
			Log.d(TAG, "listeners.size() : " + listeners.size());
            Set<String> set = listeners.keySet();
            Iterator<String> it = set.iterator();
            while(it.hasNext())
            {
                String key = it.next();
                NotificationListener listener = listeners.get(key).get();
                if(listener != null)
                {
                	listener.onNotificationDownloadCallBack(isToMe, count);
                }
            }      
        }      
    }
}
