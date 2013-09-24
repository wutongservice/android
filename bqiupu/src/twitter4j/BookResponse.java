package twitter4j;

public class BookResponse extends BookBasicInfo {
	private static final long serialVersionUID = 5374469805449058514L;
//	private static final long serialVersionUID = 8874223720940313421L;
	
//    public Bitmap icon;
    
    //just for serialize
//    public boolean isFromSerialize;
	
    /*
	public ApkResponse(Parcel in)
    {
       readFromParcel(in);
    }
	
	public void readFromParcel(Parcel in)
    {
		  id     = in.readLong();
		  uid    = in.readLong();
		  apk_server_id  = in.readLong();
		  
		  label         = in.readString();
		  packagename   = in.readString();
		  
		  versioncode         = in.readInt();
		  versionname		  = in.readString();
		  latest_versioncode  = in.readInt();
		  latest_versionname  = in.readString();
		  
		  apkurl	  = in.readString();
		  apksize     = in.readLong();

		  intallpath  = in.readString();

		  categoryid   = in.readLong();
		  subcategoryid = in.readLong();

		  iconurl            = in.readString();
		  screenshotlink     = in.readString();

		  progress    = in.readInt();
		  visibility  = in.readInt();
		  ratio       = in.readFloat();
		  description     = in.readString();
		  targetSdkVersion = in.readInt();
		  status = in.readInt();
		  
		  comments_count = in.readInt();
		  likes_count = in.readInt();
		  download_times = in.readInt();
		  
		  //TODO, please use do for comments and likes
		  
    }

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int arg1) {
        out.writeLong(id);
        out.writeLong(uid);            
        out.writeLong(apk_server_id);  
        
        out.writeString(label==null?"":label);
        out.writeString(packagename==null?"":packagename);
        
        out.writeInt(versioncode);	
        out.writeString(versionname==null?"":versionname);
        out.writeInt(latest_versioncode);
        out.writeString(latest_versionname==null?"":latest_versionname);
        
        out.writeString(apkurl==null?"":apkurl);
        out.writeLong(apksize);

        out.writeString(intallpath==null?"":intallpath);
        
        out.writeLong(categoryid);
        out.writeLong(subcategoryid);
        
        out.writeString(iconurl==null?"":iconurl);
        out.writeString(screenshotlink);
        
        out.writeInt(progress);
        out.writeInt(visibility);
        out.writeFloat(ratio);
        out.writeString(description==null?"":description);
        out.writeInt(targetSdkVersion);	
        out.writeInt(status);
        
        out.writeInt(comments_count);
        out.writeInt(likes_count);
        out.writeInt(download_times);
	}
	
    public static final Parcelable.Creator<ApkResponse> CREATOR  = new Parcelable.Creator<ApkResponse>() {
	  public ApkResponse createFromParcel(Parcel in) {
	      return new ApkResponse(in);
	  }

	  public ApkResponse[] newArray(int size) {
	      return new ApkResponse[size];
	  }
	};
	*/
    
//	@Override
//	public String toString() {
//		return "ApkResponse id:" + id
//				+ " uid:" + uid
//				+ " apk_server_id:" + apk_server_id
//				+ " label:" + label
//				+"  packagename:"+packagename
//				+ " versioncode:" + versioncode
//				+ " versionname:" + versionname
//				+"  latest_versionname:"+latest_versionname
//				+ " latest_versioncode:" + latest_versioncode
//				+ " apkurl:" + apkurl
//				+ " apksize:" + apksize
//				+ " intallpath:" + intallpath
//				+"  categoryid:"+categoryid
//				+ " subcategoryid:" + subcategoryid
//				+ " iconurl:"+iconurl
//				+"  progress:"+progress
//				+ " visibility:"+visibility
//				+ " ratio:"+ratio
//				+"  price:"+price
//				+ " targetSdkVersion:" + targetSdkVersion
//				+ " status:" + status
//				+ " comments_count:" + comments_count
//				+ " likes_count:" + likes_count
//				+ " download_times:" + download_times;
//	}
}
