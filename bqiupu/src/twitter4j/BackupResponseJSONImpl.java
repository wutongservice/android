package twitter4j;

import java.util.ArrayList;
import java.util.List;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

public class BackupResponseJSONImpl extends BackupResponse{
	
	public BackupResponseJSONImpl(JSONObject obj) throws TwitterException{
		
		try {			
			this.id = obj.getString("apk_id");
			this.url = obj.getString("file_url");
//            this.filename = obj.getString("filename");
//            this.lastmodifytime = obj.getLong("lastmodifytime");
            this.filesize = obj.getLong("file_size");
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
	
    public static BackupResponse createBackupResponse(HttpResponse response) throws TwitterException{
    	JSONObject obj = response.asJSONObject();
		return new BackupResponseJSONImpl(obj);
    }
    
    public static List<BackupResponse> createBackupResponseList(HttpResponse response) throws TwitterException{
    	List<BackupResponse> list = new ArrayList<BackupResponse>();
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
        	list.add(new BackupResponseJSONImpl(obj));
        }
        
        return list;
    }
}
