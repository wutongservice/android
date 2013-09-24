package com.borqs.account.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class BorqsAccount implements Parcelable {
	public long    _id;
    public String sessionid;
    public long   uid;
    public String username;
    public long   createtime;
    public long   modifytime;
    public String nickname;
    public String screenname;
    public int    verify;
  
	public int describeContents() {
		return 0;
	}

    public BorqsAccount(Parcel in) {
        readFromParcel(in);
    }
	 
	public BorqsAccount() {		
	    sessionid = "";	    
	    username  = "";	    
	    nickname  = "";
	    screenname = "";
	    
	}

	private void readFromParcel(Parcel in) {
		_id = in.readLong();
		sessionid = in.readString();
		uid = in.readLong();
		username = in.readString();
		createtime = in.readLong();
		modifytime = in.readLong();
		nickname = in.readString();
		screenname =  in.readString();
        verify = in.readInt();
	}

	public void writeToParcel(Parcel dest, int arg1) {
		dest.writeLong(_id);
		dest.writeString(sessionid==null?"":sessionid);
		dest.writeLong(uid);
		dest.writeString(username == null ? "" : username);
		dest.writeLong(createtime);
		dest.writeLong(modifytime);
		dest.writeString(nickname == null ? "" : nickname);
		dest.writeString(screenname == null ? "" : screenname);
		dest.writeInt(verify);
	}
	
	public static final Creator<BorqsAccount> CREATOR = new Creator<BorqsAccount>() {
        public BorqsAccount createFromParcel(Parcel source) {
            return new BorqsAccount(source);
        }

        public BorqsAccount[] newArray(int size) {
            return new BorqsAccount[size];
        }
    };
    
    @Override
    public String toString() {
    	return "_id:" + _id
    	        +"sessionid:" + sessionid
    			+" uid:" + uid
    			+" username:" + username
    			+" createtime:" + createtime
    			+" modifytime:" + modifytime
    			+" nickname:" + nickname
    			+" screenname:" + screenname
    			+" verify:" + verify;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(sessionid);
    }
}
