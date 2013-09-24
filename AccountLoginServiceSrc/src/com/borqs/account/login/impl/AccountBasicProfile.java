package com.borqs.account.login.impl;

import java.io.ByteArrayOutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.text.TextUtils;

import com.borqs.account.login.util.Base64;
import com.borqs.account.login.util.MD5;

public class AccountBasicProfile {
    private String mUserId;
    private String mPassword;
    private String mUserDispName;
    private String mUserPhone = "";
    private String mUserBirthday; //yyyy-mm-dd
    private String mUserAddress;
    private Bitmap mUserPhoto;
    private String mUserEmail = "";

    public AccountBasicProfile(){
        this(null, null);
    }
    public AccountBasicProfile(String userId, String password){
        mUserId = userId;
        mPassword = password;
    }
    
    public String getUserId() {
        return mUserId;
    }
    public void setUserId(String Id) {
        mUserId = Id;
    }
    public String getPassword() {
        return mPassword;
    }
    public void setPassword(String password) {
        mPassword = password;
    }
    public String getUserDispName() {
        return mUserDispName;
    }
    public void setUserDispName(String name) {
        mUserDispName = name;
    }
    public String getUserPhone() {
        return mUserPhone;
    }
    public void setUserPhone(String phone) {
        mUserPhone = phone;
    }
    public String getUserEmail() {
        return mUserEmail;
    }
    public void setUserEmail(String email) {
        mUserEmail = email;
    }
    public String getUserBirthday() {
        return mUserBirthday;
    }
    public void setUserBirthday(String birthday) {
        mUserBirthday = birthday;
    }
    public String getUserAddress() {
        return mUserAddress;
    }
    public void setUserAddress(String address) {
        mUserAddress = address;
    }
    
    public String getUserPhoto() {
        String result = null;
        if (mUserPhoto != null){
            ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
            mUserPhoto.compress(CompressFormat.JPEG, 100, baos);
            byte[] bytes  = baos.toByteArray();
            result = Base64.encodeToString(bytes, Base64.DEFAULT); 
        }
        return result;
    }
    public void setUserPhoto(Bitmap photo) {
        mUserPhoto = photo;
    }  
    
    public JSONObject toJson() throws JSONException{
        JSONObject fv = new JSONObject();
        
        if (!TextUtils.isEmpty(mUserDispName)){
            fv.put("display_name", mUserDispName);
        }
        
        if (!TextUtils.isEmpty(mPassword)){
            fv.put("password", MD5.toMd5(mPassword.getBytes()).toUpperCase());
        }
        
        if (!TextUtils.isEmpty(mUserPhone) || !TextUtils.isEmpty(mUserEmail)){
            JSONObject jContact = new JSONObject();
            
            jContact.put("email_address", mUserEmail);
            jContact.put("mobile_telephone_number", mUserPhone);
            
            fv.put("contact_info", jContact);
        }
        
        if (!TextUtils.isEmpty(mUserBirthday)){
            fv.put("birthday", mUserBirthday);
        }
        
        if (!TextUtils.isEmpty(mUserAddress)){
            JSONArray jAddrList = new JSONArray();
            JSONObject jAddr = new JSONObject();
            
            jAddr.put("type", "");                    
            jAddr.put("po_box", "");
            jAddr.put("extended_address", "");
            jAddr.put("street", mUserAddress);
            jAddr.put("city", "");
            jAddr.put("state", "");
            jAddr.put("postal_code", "");
            jAddr.put("country", "");
            jAddrList.put(jAddr);
            
            fv.put("address", jAddrList);
        }
       
       return fv;
    }
}
