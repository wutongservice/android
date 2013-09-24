package twitter4j;

import java.util.ArrayList;
import java.util.List;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.util.StringUtil;

import twitter4j.Stream.Comments.Stream_Post;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

public class ApkResponseJSONImpl extends ApkResponse {
	private static final long serialVersionUID = 912398395716519171L;

	public ApkResponseJSONImpl(JSONObject obj) throws TwitterException {		
		try {
			this.apk_server_id   = obj.getString("apk_id");			
			this.label = obj.getString("app_name");
			this.packagename = obj.getString("package");
			this.versioncode = obj.getInt("version_code");
			this.versionname = obj.getString("version_name");
			this.description = obj.getString("description");
			this.apksize = obj.getLong("file_size");
			this.apkurl = obj.getString("file_url");
			this.categoryid = obj.getLong("category");
			this.subcategoryid = obj.getLong("sub_category");
//			this.screenshotlink = obj.getString("screenshot");
			this.iconurl = obj.getString("icon_url");			
			this.targetSdkVersion = obj.getInt("target_sdk_version");
			this.price = Float.valueOf(obj.getString("price"));
			this.download_times = obj.getInt("download_count");
			this.install_times  = obj.getInt("install_count");
			
			if(obj.has("recent_change")) {
			    recent_change = obj.getString("recent_change");
			}

			// get Rating
			try {
				String tmpratio = obj.getString("rating");
				if(StringUtil.isValidRating(tmpratio))
				{
					this.ratio = Float.valueOf(obj.getString("rating"));
				}
				else
				{
					this.ratio = QiupuConfig.DEFAULT_RATING;
				}
			} catch (Exception e) {			}
			
			// get visibility and app_used to set apk permission
			try
			{
				this.visibility = obj.getInt("visibility");
				this.app_used = obj.getBoolean("app_used");
			}catch(Exception e){}
			
			//screenshots_urls
			try{
				JSONArray array = obj.getJSONArray("screenshots_urls");
				createScreenShotInfo(screenshotLink, array);
			}catch (Exception e) {}
			
			//added too later, will cause crash for early version
			try{
				this.comments_count = obj.getInt("app_comment_count");
				this.likes_count = obj.getInt("app_like_count");
			}catch(Exception ne){}
			
            if (comments_count > 0) {
                //get comments
                try{
                    comments = new Stream.Comments();
                    comments.count = comments_count;

                    JSONArray commentsobj = obj.getJSONArray("app_comments");
                    createToAppCommentsResponse(comments, commentsobj);
                }catch(Exception ne){}

            }
            if(likes_count > 0) {
            	try{
            		likes.count = likes_count;
            		JSONArray likesObj = obj.getJSONArray("app_liked_users");
            		createToSimpleUserResponseList(likesObj,likes.friends);
            	}catch(Exception e) {}
            }
			try{
				this.iLike      = obj.getBoolean("app_likes");
				this.isFavorite = obj.getBoolean("app_favorite");
			}catch(Exception ne){}			
			
			try{
			   uploadUser = new QiupuNewUserJSONImpl(obj.getJSONObject("upload_user"));
			}catch(Exception ne){}
			
			try{
				upload_time = obj.getLong("upload_time");
		    }catch(Exception ne){}
			
		    try{
			    latest_versioncode = obj.getInt("lasted_version_code");
			    latest_versionname = obj.getString("lasted_version_name");
		    }catch(Exception ne){}
		    
		    try{
		    	getOtherVersions(otherVersions, obj.getJSONArray("otherVersion"));
		    }catch(Exception ne){}
		    
		    obj = null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new TwitterException(e.getMessage());
		}
	}
	
	private void createScreenShotInfo(ArrayList<String> shots, JSONArray array) throws TwitterException
	{
		if(array != null)
		{
			for (int i = 0; i < array.length(); i++) {
				try {
					shots.add((String) array.get(i));
				} catch (Exception e) {
					throw new TwitterException(e);
				}
			}
		}
	}
	

	public static List<ApkResponse> createBackupedApkResponse(HttpResponse response) throws TwitterException 
	{
		List<ApkResponse> list = new ArrayList<ApkResponse>();
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
				list.add(new ApkResponseJSONImpl(obj));
				
				obj = null;
			}
			
			array = null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new TwitterException(e);
		}

		return list;
	}
	
	public static String getApkPackageNameResponse(HttpResponse response) throws TwitterException {
		String pacakgename = null;
		try {
			pacakgename = response.asString();

		} catch (Exception e) {
			e.printStackTrace();
			throw new TwitterException(e);
		}

		return pacakgename;
	}	
	
	public static void getOtherVersions(OtherVersions subversions, JSONArray others) throws TwitterException
	{
		for (int i = 0; i < others.length(); i++) {
			
			try {
				JSONObject obj = others.getJSONObject(i);
				OtherVersionsPairs op = new OtherVersionsPairs();
				op.apk_id       = obj.getString("apk_id");
				op.version_name = obj.getString("version_name");
				op.apkurl = obj.getString("file_url");
				
				obj = null;
				
				subversions.subversions.add(op);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
		}
		
	}
	public static Stream.Comments.Stream_Post createApkCommentResponse(HttpResponse response) throws TwitterException {
		Stream.Comments.Stream_Post comments = new Stream_Post();		
		try {
			JSONObject obj = response.asJSONObject();
			comments.id       = obj.getLong("comment_id");
			comments.uid      = obj.getString("commenter");
			comments.username = obj.getString("commenter_name");
			
			comments.message      = obj.getString("message");
			comments.created_time = obj.getLong("created_time");
			
			obj = null;
			
			//just for add/remove comments for user
			try{
//			    comments.post_id  = obj.getString("comments_obj_id");
			}catch(Exception ne){}
	    } 
		catch (Exception e) {
		    throw new TwitterException(e.getMessage());
		}
		return comments;
	}

	public static ApkResponse createApkResponse(HttpResponse response) throws TwitterException {
		JSONObject obj;
		try {
			obj = response.asJSONObject();		
		    return new ApkResponseJSONImpl(obj);
		} catch (TwitterException e) {
			throw e;
		}		
	}
	
	//TODO here is array back from service,but only get one apkResponse
	public static ApkResponse createApkResponseList(HttpResponse response) throws TwitterException {
		try {
			JSONArray arrlist = response.asJSONArray();
			if(arrlist.length() > 0)
			{
				JSONObject obj;
				try {
					obj = arrlist.getJSONObject(0);
				} catch (JSONException e) {
					throw new TwitterException(e);
				}
				 return new ApkResponseJSONImpl(obj);
			}
			else
			{
				return null;
			}
		} catch (TwitterException e) {
			throw e;
		}		
	}

    private static void createToAppCommentsResponse(Stream.Comments com, JSONArray array) throws TwitterException {
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj;
                try {
                    obj = array.getJSONObject(i);
                } catch (JSONException e) {
                    throw new TwitterException(e);
                }
                com.stream_posts.add(Stream.createCommentItemResponse(obj));
            }
        } catch (Exception e) {
            throw new TwitterException(e.getMessage());
        }
    }
    
    public static void createToSimpleUserResponseList(JSONArray array,ArrayList<QiupuSimpleUser> userList) 
    		throws TwitterException {
        try {

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj;
                try {
                    obj = array.getJSONObject(i);
                } catch (JSONException e) {
                    throw new TwitterException(e);
                }
                userList.add(QiupuNewUserJSONImpl.createSimpleUserResponse(obj));
            }
        } catch (Exception e) {
            throw new TwitterException(e.getMessage());
        }

    }
}