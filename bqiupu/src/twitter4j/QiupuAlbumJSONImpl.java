package twitter4j;

import java.util.ArrayList;

import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import android.util.Log;

public class QiupuAlbumJSONImpl extends QiupuAlbum {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1582686085178974185L;
	final static String TAG = "QiupuAlbumJSONImpl";

	public QiupuAlbumJSONImpl(JSONObject obj) throws TwitterException {
		createAlbumResponse(obj, this);
	}

	public QiupuAlbumJSONImpl() {
	}
	
	public static QiupuAlbum createAlbum(HttpResponse response) throws TwitterException {
		JSONObject obj = null;
		try{
			obj = response.asJSONObject();
		}catch(TwitterException e){
			throw e;
		}
		QiupuAlbum stream = createAlbumResponse(obj);
		
		return stream;
	}
	public static ArrayList<QiupuAlbum> createAlbumList(HttpResponse response) throws TwitterException {
		ArrayList<QiupuAlbum> list = new ArrayList<QiupuAlbum>();
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
			list.add(createAlbumResponse(obj));
		}
		
		return list;
	}
	
	private static void createAlbumResponse(JSONObject obj,QiupuAlbum s) throws TwitterException {
		try {
			if(obj.has("album_id")){
				s.album_id = obj.getLong("album_id");
			}
			if(obj.has("created_time")){
			    s.created_time = obj.getLong("created_time");
			}
			if(obj.has("updated_time")){
				s.updated_time = obj.getLong("updated_time");
			}
			if(obj.has("user_id")){
				s.user_id = obj.getLong("user_id");
			}
			if(obj.has("album_type")){
				s.album_type = obj.getInt("album_type");
			}
			if(obj.has("privacy")){
				s.privacy = obj.getInt("privacy") == 0?false:true;
			}
			if(obj.has("photo_count")){
				s.photo_count = obj.getInt("photo_count");
			}
			if(obj.has("title")){
				s.title = obj.getString("title");
			}
			if(obj.has("summary")){
				s.summary = obj.getString("summary");
			}
			if(obj.has("album_cover_photo_middle")){
				if(s.album_type == 0) {
					s.album_cover_photo_middle = obj.getString("album_cover_photo_big");
				}else {
					s.album_cover_photo_middle = obj.getString("album_cover_photo_small");
				}
			}
		} catch (JSONException jsone) {
			throw new TwitterException(jsone);
		}
		
	}
	public static QiupuAlbum createAlbumResponse(JSONObject obj) throws TwitterException {
		QiupuAlbum s = new QiupuAlbum();
		createAlbumResponse(obj, s);
		return s;
	}
}
