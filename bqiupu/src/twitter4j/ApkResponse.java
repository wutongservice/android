package twitter4j;

import android.graphics.Bitmap;
import android.util.Log;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Comparator;

public class ApkResponse extends ApkBasicInfo {	
	private static final long serialVersionUID = 8874223720940313422L;
	
    public transient Bitmap icon;
    
    //just for serialize
    public boolean isFromSerialize;
    public transient HttpURLConnection connection;
	 
    public static final Comparator<ApkResponse> APKS_LABEL_COMPARATOR = new Comparator<ApkResponse>() {
		public final int compare(ApkResponse a, ApkResponse b) {
			return a.label.compareToIgnoreCase(b.label);
		}
	};
    
    public static final Comparator<ApkResponse> APKS_STATUS_COMPARATOR = new Comparator<ApkResponse>() {
		public final int compare(ApkResponse a, ApkResponse b) {
			if (a.status > b.status) {
				return 1;
			} else if (a.status < b.status) {
				return -1;
			} else {
				if (a.download_times > b.download_times) {
					return -1;
				} else if (a.download_times < b.download_times) {
					return 1;
				} else {
					if (a.install_times > b.install_times) {
						return -1;
					} else if (a.install_times < b.install_times) {
						return 1;
					}	
					else
					{
						if (a.ratio > b.ratio) {
							return -1;
						} else if (a.ratio < b.ratio) {
							return 1;
						} 
						return 0;
					}
				}
			}
		}
	};
	
	public static final Comparator<ApkResponse> APKS_DOWNLOAD_COMPARATOR = new Comparator<ApkResponse>() {
		public final int compare(ApkResponse a, ApkResponse b) {
			if (a.download_times > b.download_times) {
				return -1;
			} else if (a.download_times < b.download_times) {
				return 1;
			} else {
				if (a.install_times > b.install_times) {
					return -1;
				} else if (a.install_times < b.install_times) {
					return 1;
				}	
				else
				{
					if (a.ratio > b.ratio) {
						return -1;
					} else if (a.ratio < b.ratio) {
						return 1;
					} 
					return 0;
				}
			}
		}
	};
	
	public static final Comparator<ApkResponse> APKS_Ratio_COMPARATOR = new Comparator<ApkResponse>() {
		public final int compare(ApkResponse a, ApkResponse b) {
			if (a.ratio > b.ratio) {
				return -1;
			} else if (a.ratio < b.ratio) {
				return 1;
			} 
			else 
			{
				if (a.download_times > b.download_times) 
				{
					return -1;
				}
				else if (a.download_times < b.download_times) 
				{
					return 1;
				} 
				else 
				{
					if (a.install_times > b.install_times) 
					{
						return -1;
					} 
					else if (a.install_times < b.install_times) 
					{
						return 1;
					}	
					return 0;
				}
			}
		}
	};
	
	
	public static final Comparator<ApkResponse> APKS_UPLOAD_DATE_COMPARATOR = new Comparator<ApkResponse>() {
		public final int compare(ApkResponse a, ApkResponse b) {
			if (a.upload_time > b.upload_time) {
				return -1;
			} else if (a.upload_time < b.upload_time) {
				return 1;
			} 
			else 
			{
				return 0;				
			}
		}
	};
	
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
				+"  price:"+price
				+ " targetSdkVersion:" + targetSdkVersion 
				+ " status:" + status
				+ " comments_count:" + comments_count
				+ " likes_count:" + likes_count
				+ " download_times:" + download_times
				+ " app_used:"+app_used
		        + " last_installed = " + last_installed_time;
	}

	public void copyData(ApkResponse item) {		
		id             = item.id;
		uid            = item.uid;
		apk_server_id  = item.apk_server_id;
		  
		label             = item.label;
		packagename       = item.packagename;
		  
		versioncode       = item.versioncode;
		versionname		  = item.versionname;
		latest_versioncode = item.latest_versioncode;
		latest_versionname = item.latest_versionname;
		  
		apkurl	  = item.apkurl;
		apksize   = item.apksize;
		category  = item.category;

		intallpath       = item.intallpath;

		categoryid       = item.categoryid;
		subcategoryid    = item.subcategoryid;

		iconurl          = item.iconurl;
		if(screenshotLink == null)
		{
			screenshotLink = new ArrayList<String>();
		}
		if(item.screenshotLink != null)
		{
			screenshotLink.clear();
			screenshotLink.addAll(item.screenshotLink);		
		}

		progress         = item.progress;
		visibility       = item.visibility;
		ratio            = item.ratio;
		description      = item.description;
		targetSdkVersion = item.targetSdkVersion;
		status           = item.status;
		comments_count   = item.comments_count;
		likes_count      = item.likes_count;
		download_times   = item.download_times;
		install_times    = item.install_times;
		price            = item.price;
		
		iLike       = item.iLike;
		isFavorite  = item.isFavorite;
		selected    = item.selected;
		app_used    = item.app_used;
		last_installed_time = item.last_installed_time;
		
		//apk.comments = new Comments();
		comments.stream_posts.clear();
		for(int i=0;i<item.comments.stream_posts.size();i++)
		{
			comments.stream_posts.add(item.comments.stream_posts.get(i).clone());
		}
		comments.count = comments.stream_posts.size();
		
		//apk.likes = new Likes();
		if (likes != null && likes.friends != null) {
    		likes.friends.clear();
    		for(int i=0;i<item.likes.friends.size();i++)
    		{
    			likes.friends.add(item.likes.friends.get(i).clone());
    		}
    		likes.count = likes.friends.size();
		}
		
		if(item.uploadUser != null && item.uploadUser.uid>0)
		{
		    uploadUser = item.uploadUser.clone();
		}
		
		upload_time = item.upload_time;		
		
		icon = item.icon;
		
		otherVersions.subversions.clear();
		for(int i=0;i<item.otherVersions.subversions.size();i++)
		{
			otherVersions.subversions.add(item.otherVersions.subversions.get(i).clone());
		}
	}

	public static ApkResponse createInstanceFrom(SyncResponse response, long userId, ApkResponse apkResponse, boolean existLocal) {		
		apkResponse.uid = userId;
		apkResponse.packagename = response.packagename;
		
		if(existLocal == false)
		{
			apkResponse.label = response.apkname;
			apkResponse.versioncode = response.versioncode;
			apkResponse.versionname = response.versionName;
		}
		apkResponse.apksize = response.apksize;
		apkResponse.iconurl = response.iconurl;
		apkResponse.apkurl = response.lastedapkurl;
		apkResponse.status = ApkResponse.APKStatus.STATUS_NEED_DOWNLOAD;
		apkResponse.apk_server_id = response.apk_server_id;
		apkResponse.ratio = response.rating;
		apkResponse.last_installed_time = response.last_installed_time;
        return apkResponse; 
	}

    public static final Comparator<ApkResponse> APKS_STATUS_LABEL_COMPARATOR = new Comparator<ApkResponse>() {
		public final int compare(ApkResponse a, ApkResponse b) {
			if (a.status > b.status) {
				return 1;
			} else if (a.status < b.status) {
				return -1;
			} else {
                int result = a.label.compareToIgnoreCase(b.label);
                if (0 == result) {
                    if (a.download_times > b.download_times) {
                        return -1;
                    } else if (a.download_times < b.download_times) {
                        return 1;
                    } else {
                        if (a.install_times > b.install_times) {
                            return -1;
                        } else if (a.install_times < b.install_times) {
                            return 1;
                        } else {
                            if (a.ratio > b.ratio) {
                                return -1;
                            } else if (a.ratio < b.ratio) {
                                return 1;
                            }
                            return 0;
                        }
                    }
                } else {
                    return result;
                }
			}
		}
	};
}
