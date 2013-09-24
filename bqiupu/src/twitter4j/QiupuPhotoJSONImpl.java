package twitter4j;

import java.util.ArrayList;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import android.util.Log;

public class QiupuPhotoJSONImpl extends QiupuPhoto {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1582686085178974185L;
	final static String TAG = "QiupuPhotoJSONImpl";

	public QiupuPhotoJSONImpl(JSONObject obj) throws TwitterException {
		createPhotoResponse(obj, this);
	}

	public QiupuPhotoJSONImpl() {
	}
	
	public static QiupuPhoto createPhoto(HttpResponse response) throws TwitterException {
		JSONObject obj = null;
		try{
			obj = response.asJSONObject();
		}catch(TwitterException e){
			throw e;
		}
		QiupuPhoto stream = createPhotoResponse(obj);
		
		return stream;
	}
	public static ArrayList<QiupuPhoto> createPhotoList(HttpResponse response) throws TwitterException {
		ArrayList<QiupuPhoto> list = new ArrayList<QiupuPhoto>();
		JSONArray array = null;
		try{
			array = response.asJSONArray();
		}catch(TwitterException e){
			throw e;
		}
		
		Log.d(TAG, "createUserList size:"+array.length());
		for(int i=0;i<array.length();i++){
			JSONObject obj;
			try {
				obj = array.getJSONObject(i);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
			list.add(createPhotoResponse(obj));
		}
		
		return list;
	}
	
	private static void createPhotoResponse(JSONObject obj,final QiupuPhoto s) throws TwitterException {
		try {
			if(obj.has("photo_id")){
				s.photo_id = obj.getLong("photo_id");
			}
			if(obj.has("album_id")){
				s.album_id = obj.getLong("album_id");
			}
			if(obj.has("created_time")){
				s.created_time = obj.getLong("created_time");
			}
			if(obj.has("user_id")){
				s.uid = obj.getLong("user_id");
			}
			if(obj.has("caption")){
				s.caption = obj.getString("caption");
			}
			if(obj.has("photo_url_original")){
				s.photo_url_original = obj.getString("photo_url_original");
			}
			if(obj.has("photo_url_middle")){
				s.photo_url_middle = obj.getString("photo_url_middle");
			}
			if(obj.has("photo_url_small")){
				s.photo_url_small = obj.getString("photo_url_small");
			}
			if(obj.has("photo_url_big")){
				s.photo_url_big = obj.getString("photo_url_big");
			}
			if(obj.has("photo_url_thumbnail")){
				s.photo_url_thumbnail = obj.getString("photo_url_thumbnail");
			}
			if(obj.has("location")){
				s.location = obj.getString("location");
			}
			if(obj.has("tag_ids")){
				s.tag_ids = obj.getString("tag_ids");
			}
			if(obj.has("likes")) {
			    JSONObject likesObj = obj.getJSONObject("likes");
			    if(likesObj.has("iliked")) {
			        s.iliked = likesObj.getBoolean("iliked");
			    }
			    if(likesObj.has("count")) {
			        s.likes_count = likesObj.getInt("count");
			    }
			}
			if(obj.has("from_user")) {
				JSONObject fromObj = obj.getJSONObject("from_user");
				if(fromObj.has("user_id")) {
					s.from_user_id = fromObj.getLong("user_id");
				}
				if(fromObj.has("display_name")) {
				    s.from_nick_name = fromObj.getString("display_name");
				}
				if(fromObj.has("image_url")) {
					s.from_image_url = fromObj.getString("image_url");
				}
			}
			if(obj.has("comments")) {
				JSONObject commentsObj = obj.getJSONObject("comments");
				if(commentsObj.has("count")) {
					s.comments_count = commentsObj.getInt("count");
				}
			}
//			if(obj.has("tag")){
//				s.tags = new ArrayList<PhotoTag>();
//				JSONArray tagArr = obj.getJSONArray("tag");
//				for(int i=0;i<tagArr.length();i++) {
//					JSONObject tagObj = tagArr.getJSONObject(i);
//					s.tags.add(createPhotoTagResponse(tagObj,s.photo_id));
//				}
//				
//			}
		} catch (JSONException jsone) {
			throw new TwitterException(jsone);
		}
		
	}
	public static QiupuPhoto createPhotoResponse(JSONObject obj) throws TwitterException {
		QiupuPhoto s = new QiupuPhoto();
		createPhotoResponse(obj, s);
		return s;
	}
	
	private static PhotoTag createPhotoTagResponse(JSONObject obj,long photo_id) throws TwitterException {
		PhotoTag tag = new PhotoTag();
		try {	
			tag.photo_id = photo_id;
			tag.top = obj.getInt("top");
			tag.left = obj.getInt("left");
			tag.frame_width = obj.getInt("frame_width");
			tag.frame_height = obj.getInt("frame_height");
			tag.user_id = obj.getString("user_id");
			tag.tag_text = obj.getString("tag_text");
			tag.photo_id = obj.getLong("photo_id");
			
        } catch (JSONException jsone) {}
        
        return tag;
	}
}
