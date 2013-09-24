package com.borqs.notification;

import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;



public final class UserInfo {

    String mBorqsId;
    int mPresence;
    String mIpAddress;
    String mPaddingS;
    int mPaddingI;

    public UserInfo(String name) {
        mBorqsId = name == null ? "" : name;
    }

    /*
    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mUserName);
        out.writeInt(mPresence);
        out.writeString(mIpAddress);
        out.writeString(mPaddingS);
        out.writeInt(mPaddingI);
    }
    */

    public Map toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", mBorqsId);
        map.put("presence", new Integer(mPresence).toString());
        map.put("ip", mIpAddress);
        return map;
        
    }

    public String getUserName() {
        return mBorqsId;
    }

    public int getPresence() {
        return mPresence;
    }
    
    public String getIpAdress() {
        return mIpAddress;
    }

    public String getPaddingS() {
        return mPaddingS;
    }

    public int getPaddingI() {
        return mPaddingI;
    }
}