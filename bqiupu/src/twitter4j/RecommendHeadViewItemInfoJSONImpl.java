package twitter4j;

import java.util.ArrayList;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import android.util.Log;

public class RecommendHeadViewItemInfoJSONImpl extends RecommendHeadViewItemInfo {
	private static final long serialVersionUID = 6662628759814282916L;

	final static String TAG = "RecommendHeadViewItemInfoJSONImpl";

	public RecommendHeadViewItemInfoJSONImpl(JSONObject obj) throws TwitterException {
		try {
			this.sub_id = obj.getLong("sub_id");
			JSONObject imageUrlobject = obj.getJSONObject("img_url");
			this.categoryIcon = createImageUrlJsonImpl(imageUrlobject);
			this.sub_name = obj.getString("sub_name");
			this.isSuggest = obj.getBoolean("ifsuggest");
		} catch (JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}

	public RecommendHeadViewItemInfoJSONImpl() {
	}
	
	
	public static CategoryIcon createImageUrlJsonImpl(JSONObject object) throws TwitterException
	{
		CategoryIcon imageUrl = new CategoryIcon();
		try
		{
			imageUrl.hdpi = object.getString("hdpi");
			imageUrl.mdpi = object.getString("mdpi");
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
		return imageUrl;
	}
	
	public static ArrayList<RecommendHeadViewItemInfo> createRecommendCategoryList(HttpResponse response)
			throws TwitterException {
		ArrayList<RecommendHeadViewItemInfo> list = new ArrayList<RecommendHeadViewItemInfo>();
		JSONArray array = null;
		try {
			array = response.asJSONArray();
		} catch (TwitterException e) {
			throw e;
		}

		Log.d(TAG, "createRecommendCategoryList size:" + array.length());
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj;
			try {
				obj = array.getJSONObject(i);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
			list.add(new RecommendHeadViewItemInfoJSONImpl(obj));
		}

		return list;
	}
}
