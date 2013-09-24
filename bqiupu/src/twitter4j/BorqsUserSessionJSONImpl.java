package twitter4j;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

public class BorqsUserSessionJSONImpl extends UserSession{

	public static BorqsUserSession createUserSession(HttpResponse response) throws TwitterException {
		JSONObject obj = response.asJSONObject();
		BorqsUserSession userSession = new BorqsUserSession();
		
		try {			
		    if(obj.has("ticket")) {
		        userSession.sessionid = obj.getString("ticket");
		    }
		    if(obj.has("user_id")) {
		        userSession.uid = obj.getLong("user_id");
		    }
		    if(obj.has("login_name")) {
                userSession.username = obj.getString("login_name");  
            }
		    if(obj.has("display_name")) {
                userSession.nickname = obj.getString("display_name");
            }
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

	public static boolean updatepassword(HttpResponse response) throws TwitterException {
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
