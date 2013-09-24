package twitter4j;

import java.util.HashSet;
import java.util.Set;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

import com.borqs.account.service.ContactSimpleInfo;

public class JsonResult {
	public static boolean getBooleanResult(HttpResponse response) throws TwitterException {
		JSONObject obj = null;
		boolean result;
    	try{
    		obj = response.asJSONObject();
    		result=obj.getBoolean("result");
    	}catch(Exception e){
    		int status_code = 0;
        	String error_msg = "";
        	try {
				status_code = obj.getInt("error_code");
				error_msg = obj.getString("error_msg");
			} catch (JSONException ex) {
				throw new TwitterException(ex);
			}
        	throw new TwitterException(status_code, error_msg);	
    	}

        return result;
	}
	
	public static int getIntResult(HttpResponse response) throws TwitterException {
		JSONObject obj = null;
		int result;
    	try{
    		obj = response.asJSONObject();
    		result=obj.getInt("result");
    	}catch(Exception e){    		
			throw new TwitterException(e);			
    	}

        return result;
	}

    public static Set<ContactSimpleInfo> getSystemUsers(HttpResponse response) throws TwitterException {
        Set<ContactSimpleInfo> userInSystem = new HashSet<ContactSimpleInfo>();
        JSONArray array = null;
        try {
            array = response.asJSONArray();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj;
                try {
                    obj = array.getJSONObject(i);
                } catch (JSONException e) {
                    throw new TwitterException(e);
                }
                userInSystem.add(new ContactSimpleInfoJSONImpl(obj));
                obj = null;
            }
            array = null;
        }catch (Exception e) {
            e.printStackTrace();
            throw new TwitterException(e);
        }
        
        return userInSystem;
    }

}
