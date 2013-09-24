package twitter4j;

import android.text.TextUtils;
import android.util.Log;

import com.borqs.common.api.BpcApiUtils;

import twitter4j.Stream.Comments;
import twitter4j.Stream.Comments.Stream_Post;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StreamJSONImpl extends Stream{
	private static final long serialVersionUID = -4321498805147808526L;

	public StreamJSONImpl(JSONObject obj) throws TwitterException {
		try {
			post_id       = obj.getString("post_id");
			fromUser = QiupuNewUserJSONImpl.createSimpleUserResponse(obj.getJSONObject("from"));
			toUsers  = createToSimpleUserResponseList(obj, "to");
			message  = obj.getString("message");
			type     = obj.getInt("type");
            isPrivacy = obj.optBoolean("secretly", true);
			//get likes
			try{
                canLike = obj.getBoolean(STREAM_PROPERTY_CAN_LIKE);
                iLike = obj.getBoolean("iliked");
				likes = new Stream.Likes();
				JSONObject likeobj = obj.getJSONObject("likes");
				likes.count   = likeobj.getInt("count");
				likes.friends = createToSimpleUserResponseList(likeobj, "users"/*.getJSONObject("users")*/);
			}catch(Exception ne){}
			
			//get comments
			try{
                canComment = obj.getBoolean(STREAM_PROPERTY_CAN_COMMENT);
				comments = new Stream.Comments();
				JSONObject commentsobj = obj.getJSONObject("comments");
				comments.count = commentsobj.getInt("count");
                JSONArray commentArray = commentsobj.getJSONArray("latest_comments");
				createToStreamCommentsResponse(comments, commentArray);
			}catch(Exception ne){}

			//attachments
			parseAttachments(obj);
			
			created_time = obj.getLong("created_time");
			updated_time = obj.getLong("updated_time");
			
			source       = obj.getString("source");

            try {
                icon         = obj.getString("icon");
                extension    = obj.getString(DOWN_APP_DATA);
            } catch (Exception e) {
                // No 'icon' section in the json object.
                icon = "";
//                e.printStackTrace();
            }
			
//			application  = obj.getString("application");
			
			try{
				this.rootid = obj.getString("root_id");
                quote_id    = obj.optLong("quote");
			}catch(Exception ne){}
			
			try{
			    JSONObject  retw = obj.getJSONObject("retweeted_stream");
			    retweet = new StreamJSONImpl(retw);
			    
			    parent_id = retweet.post_id;
			}catch(Exception ne){}
			
			device = obj.getString("device");
			
			try{
			    location = obj.getString("location");
			}catch(Exception ne){}

            canReshare = obj.optBoolean(STREAM_PROPERTY_CAN_RESHARE, true);

            top_in_targets = obj.optString("top_in_targets");

		} catch (Exception e) {
			e.printStackTrace();
			throw new TwitterException(e.getMessage());
		}
	}

	public static Comments.Stream_Post createCommentResponse( HttpResponse response) throws TwitterException {
		Comments.Stream_Post postResponse = null;
		JSONObject obj = null;
		try {
			obj = response.asJSONObject();
			postResponse = createCommentItemResponse(obj);
		} catch (TwitterException e) {			
			throw e;			
		} catch (Exception e) {
			throw new TwitterException(e.getMessage());
		}

		return postResponse;
	}
	
	
	public static List<Stream_Post> getCommentsResponse( HttpResponse response) throws TwitterException {
		List<Stream_Post> postResponse = new ArrayList<Stream_Post>();
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
				postResponse.add(createCommentItemResponse(obj));
			}
			
		} catch (TwitterException e) {
			throw e;	
		} catch (Exception e) {
			throw new TwitterException(e.getMessage());
		}

		return postResponse;
	}
	
	public static List<Stream> createPostResponseList(HttpResponse response) throws TwitterException {
		List<Stream> list = new ArrayList<Stream>();
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
				list.add(new StreamJSONImpl(obj));
			}
		} catch (Exception e) {		
			throw new TwitterException(e);			
		}

		return list;
	}
	
	public static Stream createPostResponse( HttpResponse response) throws TwitterException {
		Stream postResponse = null;
		JSONObject obj = null;
		try {
			obj = response.asJSONObject();
			postResponse = new StreamJSONImpl(obj);
		} catch (TwitterException e) {
			throw e;		
		} catch (Exception e) {
			throw new TwitterException(e.getMessage());
		}

		return postResponse;
	}

    private void parseAttachments(JSONObject obj) {
        try{
				if(type == BpcApiUtils.APK_POST || type == BpcApiUtils.APK_COMMENT_POST
                        || type == BpcApiUtils.APK_LIKE_POST)
				{
                    attachment = new Stream.ApkAttachment();
					JSONArray array = obj.getJSONArray("attachments");
					for (int i = 0; i < array.length(); i++) {
						JSONObject apkj;
						try {
							apkj = array.getJSONObject(i);
							attachment.attachments.add(new ApkResponseJSONImpl(apkj));
						} catch (JSONException e) {}
					}
				}
				else if(type == BpcApiUtils.APK_LINK_POST)
				{
                    attachment = new Stream.ApkAttachment();
					attachment.link = new Stream.ApkAttachment.Link();
					try{
						JSONArray array = obj.getJSONArray("attachments");
						for (int i = 0; i < array.length(); i++) {
							JSONObject apkj;
							try {
								apkj = array.getJSONObject(0);
								attachment.link.href    = apkj.getString("href");
							} catch (JSONException e) {}
						}
					}catch(Exception ne){}
				}
				else if(type == BpcApiUtils.LINK_POST)
				{
                    attachment = new Stream.URLLinkAttachment();					
					try{
						JSONArray array = obj.getJSONArray("attachments");
						for (int i = 0; i < array.length(); i++) {
							JSONObject linkJson;
							try {
								linkJson = array.getJSONObject(0);
								
								Stream.URLLinkAttachment.URLLink urllink = new Stream.URLLinkAttachment.URLLink();
								
								urllink.title = linkJson.getString("title");
								urllink.url = linkJson.getString("url");
								urllink.host = linkJson.getString("host");
//								urllink.favorite_icon_url = linkJson.getString("img_url");
                                if (TextUtils.isEmpty(urllink.host)) {
                                    urllink.favorite_icon_url = "";
                                } else {
                                    if (urllink.host.toLowerCase().startsWith("http://")) {
                                        urllink.favorite_icon_url = urllink.host + "/favicon.ico";
                                    } else {
                                        urllink.favorite_icon_url = "http://" + urllink.host + "/favicon.ico";
                                    }
                                }

								urllink.description = linkJson.getString("description");
                                attachment.attachments.add(urllink) ;

                                String imgString = linkJson.getString("many_img_url");
                                final JSONArray imageArray = new JSONArray(imgString);
                                final int imageSize = null == imageArray ? 0 : imageArray.length();
                                if (imageSize > 0 ) {
                                    for (int j = 0; j < imageSize; ++j) {
                                        urllink.all_image_urls.add(imageArray.getString(j));
                                    }
                                }
							} catch (JSONException e) {
                            }
						}
					}catch(Exception ne){}
				}
                else if(type == BpcApiUtils.BOOK_POST || type == BpcApiUtils.BOOK_COMMENT_POST
                        || type == BpcApiUtils.BOOK_LIKE_POST)
                {
                    attachment = new BookAttachment();
                    JSONArray array = obj.getJSONArray("attachments");
					for (int i = 0; i < array.length(); i++) {
						JSONObject element;
						try {
							element = array.getJSONObject(i);
							attachment.attachments.add(new BookResponseJSONImpl(element));
						} catch (JSONException e) {
                            e.printStackTrace();
                        }
					}
                }
                else if(type == BpcApiUtils.IMAGE_POST)
                {
                	// TODO: enhancement for music rather than simply reuse brook
                    attachment = new PhotoAttachment();
                    JSONArray array = obj.getJSONArray("attachments");
					for (int i = 0; i < array.length(); i++) {
						JSONObject element;
						try {
							element = array.getJSONObject(i);
							attachment.attachments.add(createPhotoResponseJSONImpl(element));
						} catch (JSONException e) {
                            e.printStackTrace();
                        }
					}
                }
                else if(type == BpcApiUtils.MUSIC_POST || type == BpcApiUtils.MUSIC_COMMENT_POST
                        || type == BpcApiUtils.MUSIC_LIKE_POST)
                {
                    // TODO: enhancement for music rather than simply reuse brook
                    attachment = new BookAttachment();
                    JSONArray array = obj.getJSONArray("attachments");
					for (int i = 0; i < array.length(); i++) {
						JSONObject element;
						try {
							element = array.getJSONObject(i);
							attachment.attachments.add(new BookResponseJSONImpl(element));
						} catch (JSONException e) {
                            e.printStackTrace();
                        }
					}
                }
                else if(type == BpcApiUtils.MAKE_FRIENDS_POST)
                {
                	attachment = new makeFriendsAttachment();
                    JSONArray array = obj.getJSONArray("attachments");
					for (int i = 0; i < array.length(); i++) {
						JSONObject element;
						try {
							element = array.getJSONObject(i);
							attachment.attachments.add(QiupuNewUserJSONImpl.createSimpleUserResponse(element));
						} catch (JSONException e) {
                            e.printStackTrace();
                        }
					}
                } else if ((type & BpcApiUtils.AUDIO_POST) == BpcApiUtils.AUDIO_POST) {
                    attachment = new AudioAttachment();
                    JSONArray array = obj.getJSONArray("attachments");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject element;
                        try {
                            element = array.getJSONObject(i);
                            attachment.attachments.add(createFileBasicInfoResponse(element));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if ((type & BpcApiUtils.VIDEO_POST) == BpcApiUtils.VIDEO_POST) {
                    attachment = new VideoAttachment();
                    JSONArray array = obj.getJSONArray("attachments");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject element;
                        try {
                            element = array.getJSONObject(i);
                            attachment.attachments.add(createFileBasicInfoResponse(element));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if ((type & BpcApiUtils.STATIC_FILE_POST) == BpcApiUtils.STATIC_FILE_POST) {
                    attachment = new StaticFIleAttachment();
                    JSONArray array = obj.getJSONArray("attachments");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject element;
                        try {
                            element = array.getJSONObject(i);
                            attachment.attachments.add(createFileBasicInfoResponse(element));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
//                else if(type == BpcApiUtils.BOOK_LINK_POST)
//				{
//                    attachment = new Stream.ApkAttachment();
//					attachment.link = new Stream.BookAttachment.Link();
//					try{
//						JSONArray array = obj.getJSONArray("attachments");
//						for (int i = 0; i < array.length(); i++) {
//							JSONObject element;
//							try {
//								element = array.getJSONObject(0);
//								attachment.link.href    = element.getString("href");
//							} catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//						}
//					}catch(Exception ne){
//                        ne.printStackTrace();
//                    }
//				}
			} catch(Exception ne){
        }
    }

    private static void createToStreamCommentsResponse(Stream.Comments com, JSONArray array) throws TwitterException {
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj;
                try {
                    obj = array.getJSONObject(i);
                } catch (JSONException e) {
                    throw new TwitterException(e);
                }
                com.stream_posts.add(createCommentItemResponse(obj));
            }
        } catch (Exception e) {
            throw new TwitterException(e.getMessage());
        }
    }
       
    
    protected static QiupuPhoto createPhotoResponseJSONImpl(JSONObject obj) throws TwitterException {
        QiupuPhoto photo = new QiupuPhoto();
        if(!TextUtils.isEmpty(obj.optString("album_id"))) {
        	photo.album_id         = Long.valueOf(obj.optString("album_id"));
        }
    	photo.album_name           = obj.optString("album_name");
    	if(!TextUtils.isEmpty(obj.optString("photo_id"))) {
        	photo.photo_id         = Long.valueOf(obj.optString("photo_id"));
        }
    	photo.photo_url_original = obj.optString("photo_img_original");
    	photo.photo_url_middle = obj.optString("photo_img_middle");
    	photo.photo_url_big    = obj.optString("photo_img_big");
    	photo.photo_url_small  = obj.optString("photo_img_small");
    	photo.caption    = obj.optString("photo_caption");
    	photo.location        = obj.optString("photo_location");
    	if(!TextUtils.isEmpty(obj.optString("photo_created_time"))) {
        	photo.created_time         = Long.valueOf(obj.optString("photo_created_time"));
        }
    	
	    try {
            if(obj.has("comments")) {
                photo.comments_count = obj.getJSONObject("comments").optInt("count", 0);
            }
            if(obj.has("likes")) {
                photo.likes_count = (obj.getJSONObject("likes").optInt("count", 0));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return photo;
    }

    protected static FileBasicInfo createFileBasicInfoResponse(JSONObject obj) throws TwitterException {
        FileBasicInfo fileinfo = new FileBasicInfo();

        fileinfo.file_id = obj.optString("file_id");
        fileinfo.title = obj.optString("title");
        fileinfo.file_size = obj.optLong("file_size");
        fileinfo.user_id = obj.optString("user_id");
        fileinfo.exp_name = obj.optString("exp_name");
        fileinfo.html_url = obj.optString("html_url");
        fileinfo.content_type =  obj.optString("content_type");
        fileinfo.new_file_name = obj.optString("new_file_name");
        fileinfo.created_time = obj.optLong("created_time");
        fileinfo.updated_time = obj.optLong("updated_time");
        fileinfo.destroyed_time = obj.optLong("destroyed_time");
        fileinfo.file_url = obj.optString("file_url");

        fileinfo.description = obj.optString("description");
        fileinfo.summary = obj.optString("summary");
        fileinfo.thumbnail_url = obj.optString("thumbnail_url");

//        Log.d("StreamJSONImpl", "\n\nfileinfo.toString() = " + fileinfo.toString() + "\n\n");
        return fileinfo;
    }

    protected static ArrayList<String> createTopListIds(HttpResponse response) throws TwitterException {
        JSONObject obj = null;
        ArrayList<String> topIdList = new ArrayList<String>();
        try {
            obj = response.asJSONObject();
            String ids = obj.optString("result");
            if (TextUtils.isEmpty(ids) == true) {
                return null;
            } else {
                String[] idsArray = ids.split(",");
                for (String id: idsArray) {
                    topIdList.add(id);
                }
            }
        } catch (TwitterException e) {
            throw e;            
        } catch (Exception e) {
            throw new TwitterException(e.getMessage());
        }

        return topIdList;
    }
    
    public static List<Stream> createSearchPostResponseList(HttpResponse response) throws TwitterException {
    	JSONObject searchObj = response.asJSONObject();
		List<Stream> list = new ArrayList<Stream>();
		JSONArray array = null;
		try {
			array = searchObj.optJSONArray("posts");
			if(array != null && array.length() > 0) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject obj;
					try {
						obj = array.getJSONObject(i);
					} catch (JSONException e) {
						throw new TwitterException(e);
					}
					list.add(new StreamJSONImpl(obj));
				}
			}

		} catch (Exception e) {		
			throw new TwitterException(e);			
		}

		return list;
	}

}
