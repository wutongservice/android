package twitter4j;

import java.util.ArrayList;
import java.util.List;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

public class BackupRecordJSONImpl extends BackupRecord{
    
    public BackupRecordJSONImpl(JSONObject obj) throws TwitterException{
		
		try {			
			this.id = obj.getLong("id");
            this.apknum = obj.getLong("apknum");
            this.createtime = obj.getLong("createtime");
            this.uid = obj.getLong("uid");
           
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
	
	public static List<BackupRecord> createBackupRecordResponse(HttpResponse response) throws TwitterException {
		List<BackupRecord> list = new ArrayList<BackupRecord>();
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
        	list.add(new BackupRecordJSONImpl(obj));
        }
        
        return list;
	}

	public static BackupResponse createOneBackupRecordResponse(HttpResponse response) {
//		List<BackupRecord> list = new ArrayList<BackupRecord>();
//    	JSONArray array = null;
//    	try{
//    	    array = response.asJSONArray();
//    	}catch(TwitterException e){
//    		JSONObject obj = response.asJSONObject();
//    		int status_code = 0;
//        	String error_msg = "";
//        	try {
//				status_code = obj.getInt("error_code");
//				error_msg = obj.getString("error_msg");
//			} catch (JSONException ex) {
//				throw new TwitterException(e);
//			}
//            throw new TwitterException(status_code,error_msg);
//    	}
//    	
//        for(int i=0;i<array.length();i++){
//        	JSONObject obj;
//			try {
//				obj = array.getJSONObject(i);
//			} catch (JSONException e) {
//				throw new TwitterException(e);
//			}
//        	list.add(new BackupRecordJSONImpl(obj));
//        }
//        
//        return list;
		return null; //TODO
	}

}
