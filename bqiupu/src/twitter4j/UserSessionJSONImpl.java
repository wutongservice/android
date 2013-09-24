package twitter4j;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

public class UserSessionJSONImpl extends UserSession{

	public static UserSession createUserSession(HttpResponse response) throws TwitterException {
		JSONObject obj = response.asJSONObject();
		UserSession userSession = new UserSessionJSONImpl();
		
		try {			
            userSession.nickname = obj.getString("nickname");
            userSession.uid = obj.getString("uid");
            userSession.session_id = obj.getString("sessionid");
            userSession.username = obj.getString("username");  
            userSession.urlname = obj.getString("screenname");      
            return userSession;
            
        } catch (JSONException jsone) {
        	int status_code = 0;
        	String error_msg = "";
        	try {
				status_code = obj.getInt("error_code");
				error_msg = obj.getString("error_msg");
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
            throw new TwitterException(status_code,error_msg);
        }
	}
	
	public static boolean getUserpassword(HttpResponse response) throws TwitterException {
		JSONObject obj = response.asJSONObject();
		boolean result;
		
		try {			
			result = obj.getBoolean("result");     
            
        } catch (JSONException jsone) {
        	int status_code = 0;
        	String error_msg = "";
        	try {
				status_code = obj.getInt("error_code");
				error_msg = obj.getString("error_msg");
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
            throw new TwitterException(status_code,error_msg);
        }
        return result;
	}

}
