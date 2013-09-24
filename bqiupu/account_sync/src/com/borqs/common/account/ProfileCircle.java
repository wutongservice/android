/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.common.account;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Date: 5/18/12
 * Time: 6:17 PM
 * Borqs project
 */
public class ProfileCircle {
    private int mId;
    private String mName;
    private int mMemberCount;
    private long mLastUpdate;

    private ProfileCircle(){}

    public String name(){
        return mName;
    }

    public int id(){
        return mId;
    }

    public static ProfileCircle fromJson(JSONObject json) throws JSONException {
        ProfileCircle circle = new ProfileCircle();
        circle.mId = json.getInt("circle_id");
        circle.mName = json.getString("circle_name");
        circle.mMemberCount = json.getInt("member_count");
        circle.mLastUpdate = json.getLong("updated_time");

        return circle;
    }
}
