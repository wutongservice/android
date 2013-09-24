/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.pim.jcontact;

import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;


abstract class JContactProperty implements JContact.TypedEntity{    
    private JSONObject mJsonContent = null;
    
    @Override
    public String getType() {
        try {
            return mJsonContent.names().getString(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public Object getValue() {
        try{
            return get(getType());
        }catch(JSONException e){
            e.printStackTrace();
            return null;
        }
    }        
    
    @Override
    public String toString(){
        return mJsonContent==null? "" : mJsonContent.toString();
    }
    
    JSONObject toJson(){
        return mJsonContent;
    }
        
    void parse(JSONObject data) throws JSONException{
        mJsonContent = parseJSON(data);
    }
    
    abstract JSONObject parseJSON(JSONObject data) throws JSONException;
        
    protected void put(String key, Object value) throws JSONException{
        //skip null
        if(value == null){
            return;
        }

        //skip the empty string
        if(value instanceof String && "".equals(((String) value).trim())){
            return;
        }
        
        if(mJsonContent == null){
            mJsonContent = new JSONObject();
        }
        mJsonContent.put(key, value);
    }
    
    protected Object get(String key) throws JSONException{
        if(mJsonContent.has(key)){
            //ex.{HOME:12345, EXTRA:[PRIMARY]}
            return mJsonContent.get(key);
        }
        return null;
    }

    protected String getString(String key) throws JSONException{
        if(mJsonContent.has(key)){
            //ex.{HOME:12345, EXTRA:[PRIMARY]}
            return mJsonContent.get(key).toString();
        }
        return "";
    }
}
