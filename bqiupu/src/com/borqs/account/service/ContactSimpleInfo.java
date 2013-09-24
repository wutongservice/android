package com.borqs.account.service;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactSimpleInfo implements Parcelable, java.io.Serializable{
 
	private static final long serialVersionUID = -1346659777578076177L;
	public String display_name_primary;
    public String phone_number;
    public String email;
    public String image_url;
    public String system_display_name;
    public long mPhotoId;
    public long mContactId;
    public long mBorqsId;
    public int type = 0;
    public boolean isfriend;
    public boolean selected;
    public int mBindId = -1;
//    public int mBindPosition = -1;
    public static final int CONTACT_INFO_TYPE_PHONE = 0;
    public static final int CONTACT_INFO_TYPE_EMAIL = 1;
  
	public int describeContents() {
		return 0;
	}


    public ContactSimpleInfo(Parcel in) {
        readFromParcel(in);
    }
	 
	public ContactSimpleInfo() {
	}

	private void readFromParcel(Parcel in) {
		display_name_primary = in.readString();
        phone_number =  in.readString();
        email =  in.readString();
        type =  in.readInt();
        mPhotoId = in.readLong();
        mContactId = in.readLong();
        mBorqsId = in.readLong();
        isfriend = 1 == in.readInt();
        image_url = in.readString();
        system_display_name = in.readString();
	}

	public void writeToParcel(Parcel dest, int arg1) {
		dest.writeString(display_name_primary);
		dest.writeString(phone_number);
		dest.writeString(email);
		dest.writeInt(type);
		dest.writeLong(mPhotoId);
		dest.writeLong(mContactId);
		dest.writeLong(mBorqsId);
		dest.writeInt(isfriend ? 1 : 0);
		dest.writeString(image_url);
		dest.writeString(system_display_name);
	}
	
	public static final Creator<ContactSimpleInfo> CREATOR = new Creator<ContactSimpleInfo>() {
        public ContactSimpleInfo createFromParcel(Parcel source) {
            return new ContactSimpleInfo(source);
        }

        public ContactSimpleInfo[] newArray(int size) {
            return new ContactSimpleInfo[size];
        }
    };
    
    
    public void copyData(ContactSimpleInfo item) {		
    	display_name_primary = item.display_name_primary;
    	phone_number = item.phone_number;
    	email = item.email;
    	type = item.type;
    	mPhotoId = item.mPhotoId;
    	mContactId = item.mContactId;
    	mBorqsId = item.mBorqsId;
    	isfriend = item.isfriend;
    	image_url = item.image_url;
    	system_display_name = item.system_display_name;
	}
    @Override
    public String toString() {
    	return "display_name_primary: " + display_name_primary
    			+ " phone_number: " + phone_number
    			+ " type: " + type
    			+ " email: " + email
    			+ " mPhotoId: " + mPhotoId
    			+ " mContactId: " + mContactId
    			+ " mBorqsId: " + mBorqsId
    			+ " isfriend: " + isfriend
    			+ " image_url: " + image_url
    			+ " display_name: " + system_display_name;
    }
    
}
