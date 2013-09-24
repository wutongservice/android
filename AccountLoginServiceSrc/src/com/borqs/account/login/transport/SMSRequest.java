/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.account.login.transport;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * "AccountRequest {t:"nb";c:"<guid>"}"
 * @author b211
 *
 */
public final class SMSRequest {
    private static final String TAG_TYPE = "t";
    private static final String TAG_CONTENT = "c";
    
    private RequestType mType;
    private String mContent;

    public static SMSRequest build(NumberBind checkMobile){
        SMSRequest request = new SMSRequest();
        request.mType = RequestType.NumberBind;
        request.mContent = checkMobile.toString();
        return request;
    }

    public static SMSRequest build(PasswordReset resetPassword){
        SMSRequest request = new SMSRequest();
        request.mType = RequestType.PasswordReset;
        request.mContent = resetPassword.toString();
        return request;
    }
    
    public static SMSRequest from(String rawRequest){
        String body = URLDecoder.decode(rawRequest);
        try {
            JSONObject ro = new JSONObject(body);
            SMSRequest request = new SMSRequest();
            request.mType = RequestType.from(ro.getString(TAG_TYPE));
            request.mContent = ro.getString(TAG_CONTENT);
            return request;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    public String toString(){
        JSONObject ro = new JSONObject();
        try {
            ro.put(TAG_TYPE, mType.value);
            ro.put(TAG_CONTENT, mContent);
        } catch (JSONException e) {            
            e.printStackTrace();
            return null;
        }
        return URLEncoder.encode(ro.toString());
    }
    
    public RequestType getType(){
        return mType;
    }
    
    public NumberBind castToNumberBindRequest(){
        if(RequestType.NumberBind.is(mType)){
            return NumberBind.from(mContent);
        }
        return null;
    }
   
    public PasswordReset castToPasswordRestRequest(){
        if(RequestType.PasswordReset.is(mType)){
            return PasswordReset.from(mContent);           
        }
        return null;
    }
        
    private SMSRequest(){} 
    
    public enum RequestType{        
        NumberBind("nb"),
        PasswordReset("pr");
        
        public final String value;
        RequestType(String type){
            this.value = type;
        }
        public static RequestType from(String type){
            if(RequestType.NumberBind.value.equals(type)){
                return RequestType.NumberBind;
            }
            if(RequestType.PasswordReset.value.equals(type)){
                return RequestType.PasswordReset;
            }
            return null;
        }
        public boolean is(RequestType t){
            return value.equals(t.value);
        }
    }
    
    public static final class NumberBind{
        private String mGuid; 
        public NumberBind(String guid){mGuid = guid;}        
        public String getGuid(){return mGuid;}        
        public String toString(){return mGuid;}
        public static NumberBind from(String content){return new NumberBind(content);}
    }
    
    public static  final class PasswordReset{
        private String mPassword; 
        public PasswordReset(String password){mPassword = password;}
        public String getPassword(){return mPassword;}        
        public String toString(){return mPassword;}   
        public static PasswordReset from(String content){return new PasswordReset(content);}
    }    
}
