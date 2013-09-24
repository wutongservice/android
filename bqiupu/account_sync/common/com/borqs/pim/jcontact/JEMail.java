/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.pim.jcontact;


import com.borqs.json.JSONArray;
import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.pim.JSONUtility;
import com.borqs.pim.jcontact.JContact.TypedEntity;

public class JEMail extends JContactProperty {
    //type string of email
    public static final String WORK = "WORK";
    public static final String HOME = "HOME";
    public static final String OTHER = "OTHER";
    public static final String MOBILE = "MOBILE";
    
    //package constant
    private static final String EXTRA = "EXTRA";
    private static final Object EXTRA_PRIMARY = "PRIMARY";
    
    private String mType;
    private boolean mIsPrimary = false;
    
    JEMail(){}
    
    JEMail(String type, String address, boolean isPrimary) throws JSONException {
        mType = type;
        mIsPrimary = isPrimary;
        put(type, address);
        if( isPrimary ){
            put(EXTRA, JSONUtility.putArray(EXTRA_PRIMARY));
        }    
    }

    @Override
    public String getType() {
        return mType;
    }
    

    public static boolean isPrimary(TypedEntity mailEntity) {
        if(mailEntity instanceof JEMail){
            JEMail mail = (JEMail)mailEntity;
            return mail.mIsPrimary;
        }
        return false;
    }


    @Override
    JSONObject parseJSON(JSONObject data) throws JSONException {
        //{WORK:sss@borqs.com, EXTRA:[PRIMARY]}    
        JSONObject object = new JSONObject(data.toString());
        
        if(object.length() > 2 || object.length()==0) {
            throw new JSONException("Invalid phone data: " + data.toString());
        }
        
        if(object.length() == 2){
            JSONArray extras = object.getJSONArray(EXTRA);
            if(extras == null){
                throw new JSONException("Invalid phone data: " + data.toString());  
            }
            //parse primary flag
            mIsPrimary = JSONUtility.has(extras, EXTRA_PRIMARY);
            object.remove(EXTRA);
        }
        
        //parse type
        JSONArray names = object.names();
        mType = names.getString(0);
        return object;
    }
}
