/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.pim.jcontact;

import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;

//{QQ:12121}
public class JIM extends JContactProperty {
    //Type string of IM
    public static final String QQ = "QQ";
    public static final String FETION = "FETION";
    public static final String MSN = "MSN";
    public static final String SKYPE = "SKYPE";
    public static final String AIM = "AIM";
    public static final String YAHOO = "YAHOO";
    public static final String GOOGLE_TALK = "GTALK";
    public static final String ICQ = "ICQ";
    public static final String WIN_LIVE = "WLIVE";
    public static final String JABBER = "JABBER";
    public static final String NETMEETING = "NMEETING";
    
    JIM(){}
    
    JIM(String imType, String im) throws JSONException {
        put(imType, im);
    }

    @Override
    JSONObject parseJSON(JSONObject data) throws JSONException {
        if(data.length() != 1){
            throw new JSONException("Invalid IM: " + data.toString());
        }
        return data;
    }
}
