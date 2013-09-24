package twitter4j;

import java.util.ArrayList;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

public class InfoCategoryResponseJSONImpl extends InfoCategory {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6808296618161774732L;

	public InfoCategoryResponseJSONImpl(JSONObject obj) throws TwitterException {		
		try {
			categoryId = obj.optLong("category_id");
			categoryName = obj.optString("category");
			creatorId = obj.optLong("user_id");
			scopeId = obj.optLong("scope");

		} catch (Exception e) {
			e.printStackTrace();
			throw new TwitterException(e.getMessage());
		}
	}

	public static ArrayList<InfoCategory> createInfoCategoryListResponse(HttpResponse response) throws TwitterException 
	{
		ArrayList<InfoCategory> list = new ArrayList<InfoCategory>();
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
				list.add(new InfoCategoryResponseJSONImpl(obj));
				
				obj = null;
			}
			
			array = null;
		} catch (Exception e) {
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

		return list;
	}
	
	public static InfoCategory createInfoCategoryResponse(HttpResponse response) throws TwitterException {
		JSONArray array;
		try {
			array = response.asJSONArray();		
			if(array != null && array.length() > 0) {
				JSONObject obj;
				try {
					obj = array.getJSONObject(0);
				} catch (JSONException e) {
					throw new TwitterException(e);
				}
				 return new InfoCategoryResponseJSONImpl(obj);
			}
			else {
				return null;
			}
		} catch (TwitterException e) {
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
	}
}