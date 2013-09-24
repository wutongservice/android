/*
 * Copyright © 2012 Borqs Ltd.  All rights reserved.
 * 
 * This document is Borqs Confidential Proprietary 
 * and shall not be used, of published, or disclosed,
 * or disseminated outside of Borqs in whole or in part
 * without Borqs's permission.
 * 
 */

package com.borqs.sync.client.download;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.borqs.json.JSONArray;
import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AppInfo implements Parcelable {

    private String mPackageName;
    private String mUrl;
    private String mLabel;
    private int mSize;
    private String mVersionName;
    private int mVersionCode;
    private long mLastUpdate;

    public String getVersionName(){
        return mVersionName;
    }

    public int getVersionCode(){
        return mVersionCode;
    }

    public long getLastUpdate(){
        return mLastUpdate;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setSize(int size) {
        mSize = size;
    }

    /**
     * the app size---byte
     *
     * @return
     */
    public int getSize() {
        return mSize;
    }

    public int describeContents() {
        return 0;
    }

    public AppInfo(Parcel in) {
        readFromParcel(in);
    }

    public AppInfo() {
        mPackageName = "";
        mUrl = "";
        mLabel = "";
        mSize = 0;
    }

    private void readFromParcel(Parcel in) {
        mPackageName = in.readString();
        mUrl = in.readString();
        mLabel = in.readString();
        mSize = in.readInt();
        mVersionName = in.readString();
        mVersionCode = in.readInt();
        mLastUpdate = in.readLong();
    }

    public void writeToParcel(Parcel dest, int arg1) {
        dest.writeString(mPackageName == null ? "" : mPackageName);
        dest.writeString(mUrl == null ? "" : mUrl);
        dest.writeString(mLabel == null ? "" : mLabel);
        dest.writeInt(mSize);
        dest.writeValue(mVersionName==null?"":mVersionName);
        dest.writeValue(mVersionCode);
        dest.writeValue(mLastUpdate);
    }

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        public AppInfo createFromParcel(Parcel source) {
            return new AppInfo(source);
        }

        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

    @Override
    public String toString() {
        return "mPackageName:" + mPackageName + ",mUrl:" + mUrl + ",mLabel:" + mLabel + ",mSize:"
                + mSize +", versionName:"+mVersionName +", versionCode:"+mVersionCode;
    }

    /**
     * parse the appinfo json ,and then return the AppInfo struct collection
     *
     * @param appInfo the appInfo json
     * @return app collection
     */
    public static List<AppInfo> parse(String appInfo) {
        if (TextUtils.isEmpty(appInfo)) {
            return null;
        }

        List<AppInfo> apps = new ArrayList<AppInfo>();
        try {

            JSONArray appInfoJson = new JSONArray(appInfo);
            for (int i = 0; i < appInfoJson.length(); i++) {
                AppInfo app = new AppInfo();
                JSONObject object = appInfoJson.getJSONObject(i);
                if (object.has("package")) {
                    app.setPackageName(object.getString("package"));
                }
                if (object.has("app_name")) {
                    app.setLabel(object.getString("app_name"));
                } else {
                    app.setLabel(object.getString("package"));
                }
                if (object.has("file_size")) {
                    app.setSize(object.getInt("file_size"));
                }
                if (object.has("file_url")) {
                    app.setUrl(object.getString("file_url"));
                }

                if (object.has("version_name")) {
                    app.mVersionName =object.getString("version_name");
                }

                if (object.has("version_code")) {
                    app.mVersionCode = object.getInt("version_code");
                }

                if (object.has("created_time")) {
                    app.mLastUpdate= object.getLong("created_time");
                }
                apps.add(app);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return apps;
    }

}
