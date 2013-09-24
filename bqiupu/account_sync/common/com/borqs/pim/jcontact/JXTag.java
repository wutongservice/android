/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.pim.jcontact;

import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;

public class JXTag extends JContactProperty{
    public static final String X_ACCOUNT_TYPE = "X-ACCOUNT-TYPE";
	public static final String X_STARRED = "X-STARRED";
	public static final String X_BLOCK = "X-BLOCK";
	public static final String X_RINGTONG = "X-RINGTONG";
	public static final String X_GROUPS = "X-GROUPS";

	JXTag(){}   
	
    JXTag(String xTag_name, String xTag_value) throws JSONException {
        put(xTag_name, xTag_value);
    }


    @Override
    JSONObject parseJSON(JSONObject data) throws JSONException {
        if(data.length() != 1){
            throw new JSONException("Invalid IM: " + data.toString());
        }
        return data;
    }
}


