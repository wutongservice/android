package twitter4j;

import java.util.HashMap;

import android.util.Log;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.util.StringUtil;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

public class SyncResponseJSONImpl extends SyncResponse{
	final static String TAG = "Qiupu.SyncResponseJSONImpl"; 
    public SyncResponseJSONImpl(JSONObject obj) throws TwitterException{
		try {			
			versioncode  = obj.getInt("version_code");
			try{
				versionName  = obj.getString("version_name");
			}
			catch(Exception ex){}
            packagename  = obj.getString("package");
            apkname      = obj.getString("app_name");
            iconurl      = obj.getString("icon_url");
            lastedapkurl = obj.getString("file_url");
            apksize      = obj.getLong("file_size");
            apk_server_id = obj.getString("apk_id");

            if (obj.has("last_installed_time")) {
                JSONObject jobj = obj.getJSONObject("last_installed_time");
                last_installed_time = jobj.getLong("last_installed_time");
            }
         // get Rating
			try {
				String tmpratio = obj.getString("rating");
				if(StringUtil.isValidRating(tmpratio))
				{
					this.rating = Float.valueOf(obj.getString("rating"));
				}
				else
				{
					this.rating = QiupuConfig.DEFAULT_RATING;
				}
			} catch (Exception e) {}
        } catch (JSONException jsone) {
			throw new TwitterException(jsone);
        }
	}
	
	public static HashMap<String, SyncResponse> createSyncResponseList(HttpResponse response) throws TwitterException {
		HashMap<String, SyncResponse> list = new HashMap<String, SyncResponse>();
		JSONArray array = null;
    	try
    	{
    	    array = response.asJSONArray();
    	}
    	catch(TwitterException e)
    	{
            throw e;
    	}
    	
        for(int i=0;i<array.length();i++){
        	JSONObject obj = null;
        	String packageName;
			try {
				obj = array.getJSONObject(i);
				packageName = obj.getString("package");
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
			
        	list.put(packageName, new SyncResponseJSONImpl(obj));
        }
        
        return list;
	}

}
