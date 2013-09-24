package twitter4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.util.Log;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

public class NotificationJSONImpl extends NotificationInfo {

	public NotificationJSONImpl(JSONObject obj) throws TwitterException {		
		try {
			this.ntftype   = obj.getString("apk_id");			
			this.ntfvalue = obj.getString("app_name");
		} catch (Exception e) {
			e.printStackTrace();
			throw new TwitterException(e.getMessage());
		}
	}
	
	//TODO here is array back from service,but only get one apkResponse
	public static ArrayList<NotificationInfo> createnotificationList(HttpResponse response) throws TwitterException {
		
		ArrayList<NotificationInfo> list = new ArrayList<NotificationInfo>();
		
		JSONObject ntfInfo = response.asJSONObject();
		
		try {
			Iterator iter = ntfInfo.keys();
	        while (iter.hasNext()) {
	            String key = (String) iter.next();
	            NotificationInfo info = new NotificationInfo();
	            info.ntftype = key;
	            info.ntfvalue = ntfInfo.getString(key);
	            list.add(info);
	        }
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new TwitterException(e);
		}

		return list;
	}
}
