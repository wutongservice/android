package twitter4j;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;

import android.text.TextUtils;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

public class Stream extends CacheMap implements Comparable<Stream>, java.io.Serializable {
	private static final long serialVersionUID = -6829613008578261206L;
	public static final String UP_APP_DATA = "app_data";
    public static final String DOWN_APP_DATA = "app_data";

    protected static final String STREAM_PROPERTY_CAN_COMMENT = "can_comment";
    protected static final String STREAM_PROPERTY_CAN_LIKE = "can_like";
    protected static final String STREAM_PROPERTY_CAN_RESHARE = "can_reshare";

	public String                post_id;
	public String                parent_id;

    public long                quote_id;
	
	public QiupuSimpleUser       fromUser;
	public List<QiupuSimpleUser> toUsers;
	
	public String    message;	
	
	
	public Comments   comments;	
	public Likes      likes;

    protected boolean    isPrivacy;
	public boolean    iLike;
    public boolean    canComment;
    public boolean    canLike;
    public boolean    canReshare;

	public AttachmentBase attachment;
	
	public long       created_time;
	public long       updated_time;
	
	public String     source;
	public String     icon;        //source icon
	public String     application;
    public String     extension;

	public boolean isFromSerialize;
	
	public Stream     retweet;
	
	public int    type;
	public String    rootid;
	public String    device;
	public String    location;

    public boolean mWasAnimated;
    public String top_in_targets;

    public int mSelectImageIndex;
    public int default_image_random_id = 0;
	
    public boolean isOwnBy(long uid) {
        if (null != fromUser && fromUser.uid == uid) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Stream) {
            return post_id.equals(((Stream)obj).post_id);
        }
        return false;
    }

	public void despose()
	{
		post_id = null;
		parent_id = null;

		if(fromUser != null)
		{
			fromUser.despose();
			fromUser = null;
		}
		if(toUsers != null)
		{
			for(QiupuSimpleUser user: toUsers)
			{
				user.despose();
				user = null;
			}
			toUsers.clear();
			toUsers = null;
		}
		
		
		message = null;	
		
		
		if(comments != null)
		{
			comments.despose();
			comments = null;
		}
		
		if(likes != null)
		{
			likes.despose();
			likes = null;
		}
		
		if(attachment != null)
		{
			attachment.despose();
			attachment = null;
		}
		
		source = null;
		icon = null;
		application = null;

		if(retweet != null)
		{
			retweet.despose();
			retweet = null;
		}
		
		rootid = null;
		device = null;
		location = null;
		top_in_targets = null;
		
		super.despose();
	}
	
	public boolean    isRetweet()
	{
		return quote_id > 0 || retweet != null;
	}

    public boolean wasDeletedRetweet() {
        return quote_id > 0 && null == retweet;
    }
	
	public static class Likes implements java.io.Serializable 
	{
		private static final long serialVersionUID = 1L;
		
		public int count;		
		public List<QiupuSimpleUser> friends;//or likes		
		public Likes()
    	{
			friends = new ArrayList<QiupuSimpleUser>();			
    	}

		public void despose()
		{
			for(QiupuSimpleUser user: friends)
			{
				user.despose();
				user = null;
			}
				
			friends.clear();
			friends = null;
		}
	}


    public static class Comments implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        protected int count;
        protected List<Stream_Post> stream_posts;

        public int getCount() {
            return count;
        }

        public List<Stream_Post> getCommentList() {
            return stream_posts;
        }

        public void alterCommentList(List<Stream_Post> newList, int totalCount) {
            if (null == newList) {
                if (null != stream_posts) {
                    stream_posts.clear();
                }
                count = 0;
            } else {
                if (null == stream_posts) {
                    stream_posts = new ArrayList<Stream_Post>();
                } else {
                    stream_posts.clear();
                }

                stream_posts.addAll(newList);
                count = totalCount;
            }
        }

        public Comments() {
            count = 0;
            stream_posts = new ArrayList<Stream_Post>();
        }

        public void despose() {
            if (null != stream_posts) {
                for (Stream_Post item : stream_posts) {
                    item.despose();
                    item = null;
                }
                stream_posts.clear();
                stream_posts = null;
            }
            
        }

        /* A full comments result:
           "latest_comments" : [ {
           "comment_id" : 2792956758595423940,
                   "target" : "2:2792946091731244652",
                   "created_time" : 1331785563752,
                   "commenter" : 10001,
                   "commenter_name" : "姜长胜",
                   "message" : "好",
                   "device" : "os=android-15-x86;client=B+ 197;lang=CN;model=unknown-generic;deviceid=d1c88d143a372f6a4dd0fe6b8616d816",
                   "can_like" : true,
                   "destroyed_time" : 0,
                   "comment_id_s" : "2792956758595423940"
       } ]*/
    	public static class Stream_Post extends CacheMap implements Comparable, java.io.Serializable
    	{
    		private static final long serialVersionUID = 1L;
            private static final int MAX_REFERRED_TEXT_LENGTH = 100;
            private static final String TO_USERS_LINK = "<a href='borqs://profile/details?uid=%1$s'>%2$s</a>";

            public long   id;
            public long   referredId;
            public Stream_Post referredComment;
	    	public String target;
	    	public long   created_time;
	    	
	    	public String uid;
	    	public String username;
	    	
	    	public String message;

            public boolean can_like;
            public boolean    iLike;
//            public int        like_count;
            public Likes      likerList;
            public String image_url;

            public boolean isOwnBy(long userId) {
                return Long.valueOf(uid) == userId;
            }

	    	public Stream_Post clone()
	    	{
	    		Stream_Post item  = new Stream_Post();
                item.id           = id;
	    		item.target      = target;
	    		item.created_time = created_time;

	    		item.uid          = uid;
	    		item.username     = username;

	    		item.message      = message;

                item.can_like     = can_like;
                item.iLike        = iLike;
                item.likerList    = likerList;
                item.image_url = image_url;
                item.cacheMap     = (HashMap<String, String>)cacheMap.clone();

	    		return item;
	    	}
	    	
	    	public void despose()
	    	{
	    		if(likerList != null) {
	    			likerList.despose();
	    			likerList = null;
	    		}

                target  = null;
	    		username = null;
	    		uid      = null;
                message  = null;
                image_url = null;
                
                super.despose();
	    	}
	    	public String toString()
	    	{
	    		return " target       = " + target +
	    		       " id            = " +id+
	    		       " created_time  = "+created_time+
	    		       " uid           = "+uid+
	    		       " message       = "+message+
	    		       " username      = "+username+
	    		       " image_url = " + image_url;
	    	}
	    	
            public int compareTo(Object another) 
            {
                Stream_Post item = (Stream_Post)another;
                if(item.created_time < this.created_time)
                {
                    return -1;
                }
                else if(item.created_time > this.created_time)
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
               
            }
            
            @Override public boolean equals(Object obj)
        	{
        		if(!(obj instanceof Stream_Post))
        		{
        			return false;
        		}
        		Stream_Post ap = (Stream_Post)obj;
        		return (ap.id == id);		
        	}

            public String asRepliedText() {
                if (TextUtils.isEmpty(message)) {
                    return "";
                } else {
                    final String referredText;
                    if (MAX_REFERRED_TEXT_LENGTH > message.length()) {
                        referredText = message;
                    } else {
                        referredText = message.substring(0, MAX_REFERRED_TEXT_LENGTH) + "...";
                    }
                    return referredText;
                }
            }

            public String asMentionUser() {
                return String.format(TO_USERS_LINK, uid, "+" + username + " ");
            }
    	}    	
	}
	
	 public static class Action_Links implements java.io.Serializable
	    {
	    	private static final long serialVersionUID = 1L;
			public List<Links> links;
	    	public Action_Links()
	    	{
	    		links = new ArrayList<Links>();
	    	}
	    	
	    	
	    	public void despose()
	    	{
	    		for(Links item: links)
	    		{
	    			item.despose();
	    			item = null;
	    		}
	    		links.clear();
	    		links = null;
	    	}
	    	public static class Links implements java.io.Serializable
	    	{
	    		private static final long serialVersionUID = 1L;
		    	public String text;
		    	public String href;
		    	public void despose()
		    	{
		    		text = null;
		    		href = null;
		    	}
	    	}
	    }
	    
	 
	public static abstract class AttachmentBase implements java.io.Serializable
    {
    	private static final long serialVersionUID = 1L;		
    	
//    	public int                attachment_type;
    	public List attachments;
    	public Link               link;
    	
    	public AttachmentBase()
    	{
    		attachments = createAttachmentList();
        }

        protected  abstract List createAttachmentList();

    	public abstract void despose();
    	
    	public String getID(){return "";}

    	public static class Link implements java.io.Serializable
        {
        	private static final long serialVersionUID = 1L;	
        	
    		public String name;
    		public String icon;//thumbnail
    		public String href;    
    		public String caption;
    		public String desc;
    	}
    	
    }
	
	 public static class makeFriendsAttachment extends AttachmentBase {
	        public makeFriendsAttachment() {
	            super();
	        }

	        protected List createAttachmentList() {
	            return new ArrayList<QiupuSimpleUser>();
	    	}

	        public void despose()
	    	{
	    		for(QiupuSimpleUser item: (List<QiupuSimpleUser>)attachments)
	    		{
	    			item.despose();
	    			item = null;
	    		}
	    		attachments.clear();
	    		attachments = null;
	    	}
	    }

    public static class ApkAttachment extends AttachmentBase {
        public ApkAttachment() {
            super();
        }

        protected List createAttachmentList() {
            return new ArrayList<ApkBasicInfo>();
    	}
        
    	public String getID(){
    		if(attachments.size() > 0)
    		    return ((ApkBasicInfo)attachments.get(0)).apk_server_id;
    		else
    			return "";
    	}

        public void despose()
    	{
    		for(ApkBasicInfo item: (List<ApkBasicInfo>)attachments)
    		{
    			item.despose();
    			item = null;
    		}
    		attachments.clear();

    		attachments = null;
    	}
    }

    public static class URLLinkAttachment extends AttachmentBase {
        public URLLinkAttachment() {
            super();
        }

        protected List createAttachmentList() {
            return new ArrayList<URLLink>();
    	}

        public void despose()
    	{
    		for(URLLink item: (List<URLLink>)attachments)
    		{
    			item.despose();
    			item = null;
    		}
    		attachments.clear();

    		attachments = null;
    	}
        
        public static class URLLink extends CacheMap implements java.io.Serializable
        {
        	private static final long serialVersionUID = 1L;	
        	
    		public String title;
    		public String url;
    		public String host;    
    		public String favorite_icon_url;
    		public String description;
            public ArrayList<String> all_image_urls = new ArrayList<String>();
			public void despose() {
				title = null;
				url   = null;
				host  = null;
				favorite_icon_url = null;
				description       = null;
				
				all_image_urls.clear();
				all_image_urls = null;
			}
    	}
    }
    public static class BookAttachment extends AttachmentBase {
        public BookAttachment() {
            super();
        }

        protected List createAttachmentList() {
            return new ArrayList<BookBasicInfo>();
    	}

        public void despose()
    	{
    		for(BookBasicInfo item: (List<BookBasicInfo>)attachments)
    		{
    			item.despose();
    			item = null;
    		}
    		attachments.clear();

    		attachments = null;
    	}
    }
    
    public static class PhotoAttachment extends AttachmentBase {
        public PhotoAttachment() {
            super();
        }

        protected List createAttachmentList() {
            return new ArrayList<QiupuPhoto>();
    	}

        public void despose()
    	{
    		for(QiupuPhoto item: (List<QiupuPhoto>)attachments)
    		{
    			item.despose();
    			item = null;
    		}
    		attachments.clear();

    		attachments = null;
    	}
    }

    public static class AudioAttachment extends AttachmentBase {
        public AudioAttachment() {
            super();
        }

        protected List createAttachmentList() {
            return new ArrayList<FileBasicInfo>();
        }

        public void despose()
        {
            for(FileBasicInfo item: (List<FileBasicInfo>)attachments)
            {
                item.despose();
                item = null;
            }
            attachments.clear();

            attachments = null;
        }
    }

    public static class VideoAttachment extends AttachmentBase {
        public VideoAttachment() {
            super();
        }

        protected List createAttachmentList() {
            return new ArrayList<FileBasicInfo>();
        }

        public void despose()
        {
            for(FileBasicInfo item: (List<FileBasicInfo>)attachments)
            {
                item.despose();
                item = null;
            }
            attachments.clear();

            attachments = null;
        }
    }

    public static class StaticFIleAttachment extends AttachmentBase {
        public StaticFIleAttachment() {
            super();
        }

        protected List createAttachmentList() {
            return new ArrayList<FileBasicInfo>();
        }

        public void despose()
        {
            for(FileBasicInfo item: (List<FileBasicInfo>)attachments)
            {
                item.despose();
                item = null;
            }
            attachments.clear();

            attachments = null;
        }
    }

	public int compareTo(Stream another) {		
		if(Stream.class.isInstance(another))
		{
			long anDate = ((Stream)another).created_time;
			if(created_time > anDate)
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}		
		return 0;
	}
    
    public static String getFromUserPhotoUrl(Stream post) {
        if (null == post || null == post.fromUser || TextUtils.isEmpty(post.fromUser.profile_image_url)) {
            return "";
        }

        return post.fromUser.profile_image_url;
    }

    protected static Comments.Stream_Post createCommentItemResponse(JSONObject obj) throws TwitterException {
        return createCommentItemResponse(obj, true);
    }

    private static Comments.Stream_Post createCommentItemResponse(JSONObject obj, boolean topLevel) throws TwitterException {
        Comments.Stream_Post comments = new Comments.Stream_Post();
        try {
            comments.id       = obj.getLong("comment_id");
            comments.target   = obj.optString("target");
            comments.uid      = obj.getString("commenter");
            comments.username = obj.getString("commenter_name");

            comments.message      = obj.getString("message");
            comments.created_time = obj.getLong("created_time");

            comments.can_like     = obj.optBoolean(STREAM_PROPERTY_CAN_LIKE, true);
            comments.iLike        = obj.optBoolean("iliked");
//            comments.like_count   = obj.optInt("like_count");
            comments.likerList    = getLikerList(obj);
            comments.image_url = obj.getString("image_url");
//            comments.likerList    = getCommentLikerList(obj);
//            comments.likerList.count = comments.like_count;
            if (topLevel) {
                comments.referredId = obj.optLong("parent_id");
                if (comments.referredId > 0) {
                    JSONObject referredObj = obj.optJSONObject("parent_comment");
                    if (null == referredObj) {
                        comments.referredComment = null;
                    } else {
                        comments.referredComment = createCommentItemResponse(referredObj);
                    }
                }
            } else {
                comments.referredComment = null;
                comments.referredId = 0;
            }
        } catch (JSONException jsone) {}

        return comments;
    }

    private static Likes getCommentLikerList(JSONObject obj) {
        Likes likerList = new Stream.Likes();

        if (null != obj) {
            try {
                likerList.friends = createToSimpleUserResponseList(obj, "like_users");
            } catch (Exception ne) {
            }
        }

        return likerList;
    }

    protected static Likes getLikerList(JSONObject obj) {
        Likes likerList = new Stream.Likes();

        if (null != obj) {
            JSONObject likeObj = obj.optJSONObject("likes");
            if (null != likeObj) {
                likerList.count = likeObj.optInt("count");
                try {
                    likerList.friends = createToSimpleUserResponseList(likeObj, "users");
                }catch(Exception ne){}
            }
        }

        return likerList;
    }

    protected static List<QiupuSimpleUser> createToSimpleUserResponseList(JSONObject toUserObjects, String tag) throws TwitterException {
        List<QiupuSimpleUser> list = new ArrayList<QiupuSimpleUser>();
        JSONArray array = null;
        try {
            array = toUserObjects.getJSONArray(tag);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj;
                try {
                    obj = array.getJSONObject(i);
                } catch (JSONException e) {
                    throw new TwitterException(e);
                }
                list.add(QiupuNewUserJSONImpl.createSimpleUserResponse(obj));
            }
        } catch (Exception e) {
            throw new TwitterException(e.getMessage());
        }

        return list;
    }


    public static int getCommentCount(Stream stream) {
        int count = 0;
        if (null != stream && null != stream.comments) {
            count = getCommentCount(stream.comments);
        }
        return count;
    }

    public static int getCommentCount(Comments comments) {
        int count = 0;
        if (null != comments && null != comments.stream_posts) {
            count = comments.stream_posts.size();
        }
        return count;
    }

    public boolean isPrivacy() {
        return isPrivacy;
    }
}
