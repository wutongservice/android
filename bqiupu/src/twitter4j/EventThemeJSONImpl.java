package twitter4j;

import java.util.ArrayList;

import android.util.Log;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

public class EventThemeJSONImpl extends EventTheme {

	private static final long serialVersionUID = -769306593176494771L;
	private final static String TAG = "EventThemeJSONImpl";

	public EventThemeJSONImpl(JSONObject obj) throws TwitterException {
		id = obj.optLong("id");
		creator = obj.optLong("creator");
		updated_time = obj.optLong("updated_time");
		name = obj.optString("name");
		image_url = obj.optString("image_url");
	}
	
	public static ArrayList<EventTheme> createEventThemeList(HttpResponse response)
			throws TwitterException {
		ArrayList<EventTheme> list = new ArrayList<EventTheme>();
		JSONArray array = null;
		try {
			array = response.asJSONArray();
		} catch (TwitterException e) {
			throw e;
		}

		Log.d(TAG, "createEventThemeList size:" + array.length());
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj;
			try {
				obj = array.getJSONObject(i);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
			list.add(new EventThemeJSONImpl(obj));
		}

		return list;
	}
}
