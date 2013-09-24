/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.pim.jcontact;

import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;


public class JWebpage extends JContactProperty{
    //type string of webpage
    public static final String HOMEPAGE = "HOMEPAGE";
    public static final String BLOG = "BLOG";
    public static final String PROFILE = "PROFILE";
    public static final String HOME = "HOME";
    public static final String WORK = "WORK";
    public static final String FTP = "FTP";
    public static final String OTHER = "OTHER";

    JWebpage(){}
    
    JWebpage(String type, String webpage) throws JSONException {
        put(type, webpage);
    }

    @Override
    JSONObject parseJSON(JSONObject data) throws JSONException {
        if(data.length() != 1){
            throw new JSONException("Invalid address: " + data.toString());
        }
        return data;
    }

}
