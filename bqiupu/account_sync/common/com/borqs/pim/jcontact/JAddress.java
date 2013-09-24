/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.pim.jcontact;

import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.pim.JSONUtility;


//{WORK:{STREET:Wanghualu, CITY:BEIJING, ZIPCODE:10000}}
public class JAddress extends JContactProperty {
    public static final String WORK = "WORK";
    public static final String HOME = "HOME";
    public static final String OTHER = "OTHER";
      
    //private key in address info    
    private static final String STREET = "ST";
    private static final String CITY = "CITY";
    private static final String PROVINCE = "PRO";
    private static final String ZIPCODE = "ZC";
    
    JAddress(){}
    
    JAddress(String type, String street, String city, String province, String zipcode)
        throws JSONException {
        JSONObject address = new JSONObject();
        address.put(STREET, street)
            .put(CITY, city)
            .put(PROVINCE, province)
            .put(ZIPCODE, zipcode);
        
        put(type, address);
    }
    
    public static String street(Object value) {               
        return JSONUtility.getAttribute(value, STREET);
    }
    
    public static String city(Object value) {
        return JSONUtility.getAttribute(value, CITY);
    }
    
    public static String zipcode(Object value) {
        return JSONUtility.getAttribute(value, ZIPCODE);
    }
    
    public static String province(Object value) {
        return JSONUtility.getAttribute(value, PROVINCE);
    }
    
    
    @Override
    JSONObject parseJSON(JSONObject data) throws JSONException {
        if(data.length() != 1){
            throw new JSONException("Invalid address: " + data.toString());
        }
        return data;
    }

}
