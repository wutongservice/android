package twitter4j;

import android.os.Parcel;
import java.util.ArrayList;
import java.util.List;

import com.borqs.common.api.BpcApiUtils;
import twitter4j.Stream.Comments.Stream_Post;

public class ApkBasicInfo extends CacheMap implements java.io.Serializable{	
	private static final long serialVersionUID = -8413959193945386198L;

    /**
     *  the scheme string to launch Apk detail activity defined by APK component in AndroidManifest.xml.
     */
    public static final String DETAIL_ACTIVITY_SCHEME = BpcApiUtils.APP_DETAIL_SCHEME_PATH;

    public final static String INSTALLUSERREASON = "installed";
    public final static String APK_COMMENT_COUNT = "APK_COMMENT_COUNT";

	public long   id;            //it is used for local database id, nothing, need remove
    public long   uid;           //who own this apk, in local database
    public String apk_server_id; //apk id in server db
    
    public String label;
    public String packagename;
    
    public int    versioncode;
    public String versionname;
    public int    latest_versioncode;
    public String latest_versionname;
    
    public String apkurl;       //server url.
    public String downloadpath; //download url.
    public long   apksize;      //local/server apk's file size
    
	public String intallpath;   // phone install path
    
    public long   categoryid;
    public long   subcategoryid;
    
    public String iconurl;
    public ArrayList<String> screenshotLink = new ArrayList<String>();
    
    public int    progress;
    public int    visibility;
    public float  ratio;
    public String description;
    public int    targetSdkVersion;
    public int    status = APKStatus.STATUS_NEED_DOWNLOAD;// STATUS_DEFAULT;
    
    public int    comments_count;
    public int    likes_count;
    public int    download_times;
    public int    install_times;
    public String recent_change;
    
    public boolean iLike;
    public boolean isFavorite;
    public boolean selected;
    
    public boolean app_used;
    
    public boolean iscancelApp;
    
    public Stream.Comments comments = new Stream.Comments();
	public Likes    likes    = new Likes();
	
	public OtherVersions otherVersions = new OtherVersions();
	
	public QiupuUser uploadUser;
	
	public long      upload_time;
	public float price;
	public long last_installed_time;

    public int category;

    public void despose() {
		if(uploadUser != null)
		{
			uploadUser.despose();		
		    uploadUser = null;
		}
		
		if(otherVersions != null)
		{
			otherVersions.despose();		
		    otherVersions = null;
		}
		
		if(likes != null)
		{
			likes.despose();
		    likes = null;
		}
		
		if(comments != null)
		{
			comments.despose();
			comments = null;
		}
		
		description = null;
		
		for(String item :screenshotLink)
		{
			item = null;
		}
		screenshotLink.clear();
		screenshotLink = null;
		
	    apk_server_id = null;
	    
	    label = null;
	    packagename = null;
	    versionname = null;	    
	    latest_versionname = null;
	    
	    apkurl = null;
	    intallpath = null;
	    
	    iconurl = null;
	    recent_change = null;
	    
	    super.despose();
	}
    
	public static interface APKStatus {
		public final static int APK_SERVER_RESPONSE_NEED_UPLOAD = -1;
		//public final static int APK_SERVER_RESPONSE_NEED_BACKUP = -2;
		
		public final static int STATUS_DEFAULT = 0; //
		public final static int STATUS_NEED_UPDATE = STATUS_DEFAULT + 1;
        public final static int STATUS_UPDATING = STATUS_NEED_UPDATE + 1;
		public final static int STATUS_NEED_DOWNLOAD = STATUS_UPDATING + 1;
		public final static int STATUS_DOWNLOADING = STATUS_NEED_DOWNLOAD + 1;
//		public final static int STATUS_NEED_BACKUP = STATUS_DOWNLOADING + 1;
		public final static int STATUS_NEED_UPLOAD = STATUS_DOWNLOADING + 1;
		public final static int STATUS_UPLOADING = STATUS_NEED_UPLOAD + 1;
		public final static int STATUS_SYNC_OK = STATUS_UPLOADING + 1; 
	}
	
	@Override
	public String toString() {
		return "ApkResponse id:" + id 
				+ " uid:" + uid 
				+ " apk_server_id:" + apk_server_id 
				+ " label:" + label
				+"  packagename:"+packagename
				+ " versioncode:" + versioncode
				+ " versionname:" + versionname
				+"  latest_versionname:"+latest_versionname
				+ " latest_versioncode:" + latest_versioncode
				+ " apkurl:" + apkurl 
				+ " apksize:" + apksize 
				+ " intallpath:" + intallpath 
				+"  categoryid:"+categoryid
				+ " subcategoryid:" + subcategoryid
				+ " iconurl:"+iconurl
				+"  progress:"+progress
				+ " visibility:"+visibility
				+ " ratio:"+ratio
//				+"  description:"+description
				+ " targetSdkVersion:" + targetSdkVersion
				+ " status:" + status
				+ " comments_count:" + comments_count
				+ " likes_count:" + likes_count
				+ " download_times:" + download_times
				+ " install_times:" + install_times
		        + " iLike:"+iLike
		        + " isFavorite:"+isFavorite
		        + " selected:"+selected
		        + " uploader:"+uploadUser
		        + " price:"+price
		        + " upload_time:"+upload_time
		        + " app_used:"+app_used
		        + " last_installed = " + last_installed_time
		        + " category = " + category;
		
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}	
	
	public boolean hasSunVersions()
	{
		return otherVersions.subversions.size()>0?true:false;
	}
	
	public ApkResponse clone() {
		ApkResponse apk = new ApkResponse();
		apk.id             = id;
		apk.uid            = uid;
		apk.apk_server_id  = apk_server_id;
		  
		apk.label             = label;
		apk.packagename       = packagename;
		  
		apk.versioncode       = versioncode;
		apk.versionname		  = versionname;
		apk.latest_versioncode = latest_versioncode;
		apk.latest_versionname = latest_versionname;
		  
		apk.apkurl	  = apkurl;
		apk.apksize   = apksize;

		apk.intallpath       = intallpath;

		apk.categoryid       = categoryid;
		apk.subcategoryid    = subcategoryid;

		apk.iconurl          = iconurl;
		apk.screenshotLink.clear();
		apk.screenshotLink.addAll(screenshotLink);

		apk.progress         = progress;
		apk.visibility       = visibility;
		apk.ratio            = ratio;
		apk.description      = description;
		apk.targetSdkVersion = targetSdkVersion;
		apk.status           = status;
		apk.comments_count   = comments_count;
		apk.likes_count      = likes_count;
		apk.download_times   = download_times;
		apk.install_times    = install_times;
		apk.price            = price;
		apk.iLike = iLike;
		apk.isFavorite = isFavorite;
		apk.selected = selected;
		apk.app_used = app_used;
		apk.last_installed_time = last_installed_time;
		apk.recent_change = recent_change;

        apk.category = category;

		//apk.comments = new Comments();
		apk.comments.stream_posts.clear();
		for(int i=0;i<comments.stream_posts.size();i++)
		{
			apk.comments.stream_posts.add(comments.stream_posts.get(i).clone());
		}
		apk.comments.count = apk.comments.stream_posts.size();
		
		//apk.likes = new Likes();
		apk.likes.friends.clear();
		for(int i=0;i<likes.friends.size();i++)
		{
			apk.likes.friends.add(likes.friends.get(i).clone());
		}
		apk.likes.count = apk.likes.friends.size();
		
		if(uploadUser != null && uploadUser.uid>0)
		{
		    apk.uploadUser = uploadUser.clone();
		}
		
		apk.upload_time = upload_time;
		
		
		apk.otherVersions.subversions.clear();
		for(int i=0;i<otherVersions.subversions.size();i++)
		{
			apk.otherVersions.subversions.add(otherVersions.subversions.get(i).clone());
		}
		
		return apk;
	}
	
	public static class Likes implements java.io.Serializable 
	{
		private static final long serialVersionUID = 1L;
		
		public int count;		
		public ArrayList<QiupuSimpleUser> friends;//or likes		
		public Likes()
    	{
			friends = new ArrayList<QiupuSimpleUser>();			
    	}
		
		public void despose()
		{
			for(QiupuSimpleUser user:friends)
			{
				user.despose();				
			}
			friends.clear();
			friends = null;
		}
	}
	
	public static class OtherVersions implements java.io.Serializable
	{
		private static final long serialVersionUID   = 1L;
		public List<OtherVersionsPairs>     subversions;
		
		public OtherVersions()
		{
			subversions = new ArrayList<OtherVersionsPairs>();
		}

		public void despose() {
			for(OtherVersionsPairs item:subversions)
			{
				item.despose();				
			}
			
			subversions.clear();
			subversions = null;
		}
		
	}
	
	public static class OtherVersionsPairs implements java.io.Serializable, Comparable<OtherVersionsPairs>
	{
		private static final long serialVersionUID   = 1L;
		public String apk_id;
		public String version_name;
		public String apkurl;
		
		public OtherVersionsPairs clone()
		{
			OtherVersionsPairs item = new OtherVersionsPairs();
			item.apk_id = apk_id;
			item.version_name = version_name;
			item.apkurl       = apkurl;
			return item;
		}

		public void despose() {
			apkurl = null;
			version_name = null;
			apk_id = null;
		}

		@Override
		public int compareTo(OtherVersionsPairs another) {
			 final String id = ((OtherVersionsPairs)another).apk_id;
	         return id.compareTo(apk_id);	         
		}
	}

//
//    public static class Comments implements java.io.Serializable {
//        private static final long serialVersionUID = 1L;
//        public int count;
//        public List<Stream_Post> stream_posts;
//
//        public Comments() {
//            stream_posts = new ArrayList<Stream_Post>();
//        }
//
//        public void despose() {
//            if (stream_posts != null) {
//                while (stream_posts.size() > 0) {
//                    Stream_Post item = stream_posts.get(0);
//                    item.despose();
//
//                    stream_posts.remove(0);
//                }
//
//                stream_posts = null;
//            }
//        }
//    }
	
	@Override public boolean equals(Object obj)
	{
		if(!(obj instanceof ApkBasicInfo))
		{
			return false;
		}
		ApkBasicInfo ap = (ApkBasicInfo)obj;
//		return (ap.apk_server_id.equals(apk_server_id) && ap.packagename.equals(packagename));
		return (ap.apk_server_id == apk_server_id && ap.packagename.equals(packagename));
	}

    public boolean isAbsentFromPhone() {
        return isAbsentFromPhone(status);
    }

    public static boolean isAbsentFromPhone(int apkStatus) {
        return apkStatus == APKStatus.STATUS_NEED_DOWNLOAD || apkStatus == APKStatus.STATUS_DOWNLOADING;
    }
}
