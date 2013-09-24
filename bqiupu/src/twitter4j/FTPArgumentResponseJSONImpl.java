package twitter4j;

import java.util.ArrayList;
import java.util.List;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

public class FTPArgumentResponseJSONImpl extends FTPArgumentResponse{
	
	public FTPArgumentResponseJSONImpl(JSONObject obj) throws TwitterException{
		
		try {			
			this.host = obj.getString("host");
            this.user = obj.getString("user");
            this.pass = obj.getString("pass");            
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
	
    public static FTPArgumentResponse createFTPArgumentResponse(HttpResponse response) throws TwitterException{
    	JSONObject obj = response.asJSONObject();
		return new FTPArgumentResponseJSONImpl(obj);
    }
    
    public static List<FTPArgumentResponse> createFTPArgumentResponseList(HttpResponse response) throws TwitterException{
    	List<FTPArgumentResponse> list = new ArrayList<FTPArgumentResponse>();
    	JSONArray array = null;
    	try{
    	    array = response.asJSONArray();
    	}catch(TwitterException e){
    		JSONObject obj = response.asJSONObject();
    		int status_code = 0;
        	String error_msg = "";
        	try {
				status_code = obj.getInt("error_code");
				error_msg = obj.getString("error_msg");
			} catch (JSONException ex) {
				throw new TwitterException(e);
			}
            throw new TwitterException(status_code,error_msg);
    	}
    	
        for(int i=0;i<array.length();i++){
        	JSONObject obj;
			try {
				obj = array.getJSONObject(i);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
        	list.add(new FTPArgumentResponseJSONImpl(obj));
        }
        
        return list;
    }
}
