package twitter4j;

import java.util.ArrayList;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import android.util.Log;

public class PageInfoJSONImpl extends PageInfo {

	private static final long serialVersionUID = 1L;
	final static String TAG = "PageInfoJSONImpl";

	public PageInfoJSONImpl(JSONObject obj) throws TwitterException {
		
	    page_id = obj.optLong("page_id", -1);
	    name = obj.optString("name", "");
	    name_en = obj.optString("name_en", "");
	    address = obj.optString("address");
	    address_en = obj.optString("address_en");
	    description = obj.optString("description", "");
	    description_en = obj.optString("description_en", "");
	    email = obj.optString("email", "");
	    website = obj.optString("website", "");
	    tel = obj.optString("tel", "");
	    fax = obj.optString("fax", "");
	    zip_code = obj.optString("zip_code", "");
	    small_logo_url = obj.optString("small_logo_url", "");
	    logo_url = obj.optString("logo_url", "");
	    large_logo_url = obj.optString("large_logo_url", "");
	    small_cover_url = obj.optString("small_cover_url", "");
	    cover_url = obj.optString("cover_url", "");
	    large_cover_url = obj.optString("large_cover_url", "");
	    associated_id = obj.optLong("associated_id", -1);
	    created_time = obj.optLong("created_time");
	    updated_time = obj.optLong("created_time");
	    viewer_can_update = obj.optBoolean("viewer_can_update", false);
	    followers_count = obj.optInt("followers_count", 0);
	    followed = obj.optBoolean("followed");
	    creatorId = obj.optLong("creator");
	    free_circle_ids = obj.optString("free_circle_ids");
	    JSONObject associatedobj = obj.optJSONObject("associated");
	    if(associatedobj != null) {
	    	associatedCircle = parseSimpleCircleInfo(associatedobj);
	    }
	    JSONArray freeCirclesobj = obj.optJSONArray("free_circles");
	    if(freeCirclesobj != null) {
	    	freeCircles = parseFreeCircles(freeCirclesobj);
	    }
	    in_associated_circle = obj.optBoolean("in_associated_circle");
	    parseFans(obj);
	}
	
	public static ArrayList<PageInfo> createPageList(HttpResponse response)
			throws TwitterException {
		ArrayList<PageInfo> list = new ArrayList<PageInfo>();
		JSONArray array = null;
		try {
			array = response.asJSONArray();
		} catch (TwitterException e) {
			throw e;
		}

		Log.d(TAG, "createPageList size:" + array.length());
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj;
			try {
				obj = array.getJSONObject(i);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
			list.add(new PageInfoJSONImpl(obj));
		}

		return list;
	}
	
	public static PageInfo createPageResponse(HttpResponse response)
			throws TwitterException {
		PageInfo info = new PageInfo();
		JSONObject obj = null;
		try {
			obj = response.asJSONObject();
			info = new PageInfoJSONImpl(obj);
		} catch (TwitterException e) {
			throw e;
		}
		return info;
	}
	
	private UserCircle parseSimpleCircleInfo(JSONObject obj) {
		UserCircle tmpCircle = new UserCircle();
		tmpCircle.circleid = obj.optLong("id");
		tmpCircle.name = obj.optString("name");
		tmpCircle.profile_image_url = obj.optString("image_url");
		return tmpCircle;
	}
	
	private ArrayList<UserCircle> parseFreeCircles(JSONArray freeCircles) {
		ArrayList<UserCircle> list = new ArrayList<UserCircle>();

		Log.d(TAG, "parseFreeCircles size:" + freeCircles.length());
		for (int i = 0; i < freeCircles.length(); i++) {
			JSONObject obj = null;
			try {
				obj = freeCircles.getJSONObject(i);
			} catch (JSONException e) {
			}
			if(obj != null) {
				list.add(parseSimpleCircleInfo(obj));
			}
		}
		return list;
	}
	
	private void parseFans(JSONObject obj) throws TwitterException {
	    JSONArray fans = obj.optJSONArray("followers");
	    if(fans != null && fans.length() > 0) {
	        fansList = new ArrayList<UserImage>();
	        for(int i=0;i<fans.length();i++) {
                JSONObject memberobj = fans.optJSONObject(i);
                if(memberobj != null) {
                	fansList.add(QiupuNewUserJSONImpl.createUserImageItemResponse(memberobj, page_id));
                }
            }
	    }
	}
}
