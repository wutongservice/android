/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.common.account;

import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public final class ProfileContactInfo {
    private JSONObject mJSONData;
    
    public static ProfileContactInfo from(JSONObject jsonObject) {
        if(jsonObject == null){
            return null;
        }
        
        ProfileContactInfo info = new ProfileContactInfo();
        info.mJSONData = jsonObject;
        return info;
    }
    
    public boolean has(String key){
        return mJSONData.has(key);
    }

	public String getField(String key) {
		try {
			if (mJSONData.has(key)) {
				return mJSONData.getString(key);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
    
    public ArrayList<String> getPhones() {
    	ArrayList<String> phones = new ArrayList<String>();
    	String[] columns = new String[] {"mobile_telephone_number","mobile_2_telephone_number","mobile_3_telephone_number"};
    	for(String key : columns) {
    		String value = getField(key);
    		if(!TextUtils.isEmpty(value)) {
    			phones.add(value);
    		}
    	}
    	return phones;
    }
    
    public ArrayList<String> getEmails() {
    	ArrayList<String> emails = new ArrayList<String>();
    	String[] columns = new String[] {"email_address","email_2_address","email_3_address"};
    	for(String key : columns) {
    		String value = getField(key);
    		if(!TextUtils.isEmpty(value)) {
    			emails.add(value);
    		}
    	}
    	return emails;
    }
}
