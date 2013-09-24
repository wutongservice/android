/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.pim;

import com.borqs.json.JSONArray;
import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.util.Base64;


public class JSONUtility {
    
    /**
     * put objects into a array 
     * @param values
     * @return
     */
    public static JSONArray putArray(Object... values){
        JSONArray array = new JSONArray();
        for(Object o : values){
            array.put(o);
        }
        return array;
    }
    
    /**
     * check if a object in the JSONArray list
     * @param array
     * @param o
     * @return
     */
    public static boolean has(JSONArray array, Object o){
        try {
            for(int i=0; i<array.length(); i++){
                boolean hasit = false;
                Object item = array.get(i);
                if(o instanceof JSONObject){
                    hasit = o.toString().equals(item.toString());
                } else {
                    hasit = item.equals(o);
                }
                
                if(hasit){
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * check and get the value for the key
     * @param o
     * @param key
     * @return
     */
    public static String getAttribute(Object o, String key){
        if(!(o instanceof JSONObject)){
            return null;
        }
        
        try {                        
            return ((JSONObject)o).getString(key);                        
        } catch (JSONException e) {
        }
        return null;
    }

    /**
     * encode a byte array to string for json package
     * @param photo
     * @return
     */
    public static String encodeBytes(byte[] photo) {        
        return Base64.encodeToString(photo, Base64.DEFAULT);
    }

    public static byte[] decodeBytes(String photoString) {        
        return Base64.decode(photoString, Base64.DEFAULT);
    }
}
