/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.pim.jcontact;

import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.pim.JSONUtility;


//{WORK:{COMPANY:borqs,TITLE:enginner}}
public class JORG extends JContactProperty {

    public static final String WORK = "WORK";
    public static final String OTHER = "OTHER";

    //private key in ORG
    private static final String COMPANY = "COMPANY";
    private static final String TITLE = "TITLE";
    
    JORG(){}
    
    JORG(String type, String company, String title) throws JSONException {
        JSONObject address = new JSONObject();
        address.put(COMPANY, company)
            .put(TITLE, title);
        
        put(type, address);
    }

    public static String company(Object o) {
        return JSONUtility.getAttribute(o, COMPANY);
    }

    public static String title(Object o) {
        return JSONUtility.getAttribute(o, TITLE);
    }
    
    @Override
    JSONObject parseJSON(JSONObject data) throws JSONException {
        if(data.length() != 1){
            throw new JSONException("Invalid ORG: " + data.toString());
        }
        return data;
    }
}
