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

//schema {HOME:12345, EXTRA:[PRIMARY]}   
public class JPhone extends JContactProperty{
    //Type string of Phone number
    public static final String MOBILE = "MOBILE";
    public static final String HOME = "HOME";
    public static final String WORK = "WORK";
    public static final String WORK_FAX = "WFAX";
    public static final String HOME_FAX = "HFAX";
    public static final String PAGE = "PAGE";    
    public static final String CALLBACK = "CBACK";
    public static final String CAR = "CAR";
    public static final String COMPANY_MAIN = "CMAIN";
    public static final String ISDN = "ISDN";
    public static final String OTHER = "OTHER";
    public static final String OTHER_FAX = "OFAX";
    public static final String RADIO = "RADIO";
    public static final String TELEGRAPH = "TELEGRAPH";
    public static final String TTY_TDD = "TTY";
    public static final String WORK_MOBILE = "WMOBILE";
    public static final String WORK_PAGE = "WPAGE";
    public static final String ASSISTANT = "ASSISTANT";
    public static final String MMS = "MMS";
    public static final String HOME_MOBILE = "HMOBILE";
	public static final String MAIN = "MAIN";
    
    //package constant
    private static final String EXTRA = "EXTRA";
    private static final Object EXTRA_PRIMARY = "PRIMARY";
    
    private String mType;
    private boolean mIsPrimary = false;
    
    JPhone(){}
    
    JPhone(String type, String number, boolean isPrimary) throws JSONException{
        mType = type;
        mIsPrimary = isPrimary;
        put(type, number);
        if( isPrimary ){
            put(EXTRA, JSONUtility.putArray(EXTRA_PRIMARY));
        }        
    }

    @Override
    public String getType() {
        return mType;
    }
        
    @Override
    public Object getValue() {
        //make sure the number is a string not integer
        return super.getValue().toString();
    }

    /**
     * check if the phone number entity has primary flag set
     * @param entity
     * @return
     */
    public static boolean isPrimary(TypedEntity phoneEntity) {
        if(phoneEntity instanceof JPhone){
            JPhone phone = (JPhone)phoneEntity;
            return phone.mIsPrimary;
        }
        return false;
    }

    @Override
    JSONObject parseJSON(JSONObject data) throws JSONException {
        //{HOME:12345, EXTRA:[PRIMARY]}        
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
