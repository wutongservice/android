/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.pim.jcontact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.borqs.json.JSONArray;
import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.pim.JSONUtility;


/**
 * 
 */
public class JContact {
    private JSONObject mData;
    private JName mName;
    private JSONArray mTel;
    private JSONArray mEMail;
    private JSONArray mAddress ;
    private JSONArray mOrganization;
    private JSONArray mIM;
    private JSONArray mWebpage;
    private JSONArray mXTags;
    
    public interface TypedEntity{
        public String getType();
        public Object getValue();        
    };

    public static JContact fromJsonString(String jsonContact){
        JContact contact = new JContact();
        
        try {
            contact.parseData(new JSONObject(jsonContact));
        } catch (JSONException e) {        
            e.printStackTrace();
            contact = null;
        }        
        return contact;
    }
    
    /**
     * convert the contact struct to a json string
     * @return  -   Json contact string, null if the construct is invalid.
     */
    public String toJsonString() {
        if(mData != null){
            return mData.toString();
        }
        return null;
    }

    /**
     * First name of the contact
     * @return  name string, or null if the name is not set
     */
    public String getFirstName() {
        return parseName(JName.FIRSTNAME);
    }

    /**
     * Middle name of the contact
     * @return  name string, or null if the name is not set
     */
    public String getMiddleName() {
        return parseName(JName.MIDDLENAME);
    }

    /**
     * Last name of the contact
     * @return  name string, or null if the name is not set
     */
    public String getLastName() {
        return parseName(JName.LASTNAME);
    }
    
    /**
     * Nick name of the contact
     * @return  name string, or null if the name is not set
     */
    public String getNickName() {
        return parseName(JName.NICKNAME);
    }  
    
    /**
     * First name in pinyin of the contact
     * @return  name string, or null if the name is not set
     */
    public String getFirstNamePinyin() {
        return parseName(JName.FIRSTNAME_PINYIN);
    }
    
    /**
     * Middle name in pinyin of the contact
     * @return  name string, or null if the name is not set
     */
    public String getMiddleNamePinyin() {
        return parseName(JName.MIDDLENAME_PINYIN);
    }

    /**
     * Last name in pinyin of the contact
     * @return  name string, or null if the name is not set
     */
    public String getLastNamePinyin() {
        return parseName(JName.LASTNAME_PINYIN);
    }

    /**
     * Prefix name of the contact
     * @return  name string, or null if the name is not set
     */
    public String getNamePrefix() {
        return parseName(JName.PREFIX);
    }

    /**
     * Postfix name of the contact
     * @return  name string, or null if the name is not set
     */
    public String getNamePostfix() {
        return parseName(JName.POSTFIX);
    }    

    /**
     * Webpages of the contact
     * @return  webpages or empty list.
     */
    public List<TypedEntity> getWebpageList() {        
        return parseArrayProperty(mWebpage, JWebpage.class);
    }

    /**
     * Birthday of the contact
     * @return  Birthday in string, or null if it is not set
     */
    public String getBirthday() {
        return parseProperty(Tags.BIRTHDAY);
    }

    /**
     * Note of the contact
     * @return  Note in string, or null if it is not set
     */
    public String getNote() {        
        return parseProperty(Tags.NOTE);
    }

    /**
     * Phone number of the contact
     * @return  Numbers or empty list.
     */
    public List<TypedEntity> getPhoneList() {
        return parseArrayProperty(mTel, JPhone.class);
    }

    /**
     * Email of the contact
     * @return  Emails or empty list.
     */
    public List<TypedEntity> getEmailList() {
        return parseArrayProperty(mEMail, JEMail.class);
    }
    
    /**
     * Address of the contact
     * @return  addresses or empty list.
     */
    public List<TypedEntity> getAddressList() {
        return parseArrayProperty(mAddress, JAddress.class);
    }
    
    /**
     * IM of the contact
     * @return  IMs or empty list.
     */
    public List<TypedEntity> getIMList() {
        return parseArrayProperty(mIM, JIM.class);
    }

    /**
     * Organizations of the contact
     * @return  organizations or empty list.
     */
    public List<TypedEntity> getOrgList() {
        return parseArrayProperty(mOrganization, JORG.class);
    }
    
    /**
     * extended properties of the contact
     * @return  properties or empty list.
     */
    public List<TypedEntity> getXTags() {
        return parseArrayProperty(mXTags, JXTag.class);
    }

    /**
     * Photo of the contact
     * @return  Photo or null if it is not set
     */
    public byte[] getPhoto() {
        String photoString = null;
        try {
            if(mData.has(Tags.PHOTO)){
                photoString = mData.getString(Tags.PHOTO);
            }
        } catch (JSONException e) {
            photoString = null;
        }  
        
        if(photoString==null){
            return null;
        }
        
        return JSONUtility.decodeBytes(photoString);
    }
    
    private void parseData(JSONObject jdata) throws JSONException{
        mData = jdata;
        if(mData.has(Tags.NAME)){
            mName = new JName();
            mName.parse(mData.getJSONObject(Tags.NAME));    
        }
        
        if(mData.has(Tags.TEL)){
            mTel = mData.getJSONArray(Tags.TEL);    
        }
        if(mData.has(Tags.EMAIL)){
            mEMail = mData.getJSONArray(Tags.EMAIL);    
        }
        if(mData.has(Tags.ADDR)){
            mAddress = mData.getJSONArray(Tags.ADDR);    
        }
        if(mData.has(Tags.ORG)){
            mOrganization = mData.getJSONArray(Tags.ORG);    
        }
        if(mData.has(Tags.IM)){
            mIM = mData.getJSONArray(Tags.IM);    
        }
        if(mData.has(Tags.WEBPAGE)){
            mWebpage = mData.getJSONArray(Tags.WEBPAGE);    
        }
        if(mData.has(Tags.XTAG)){
            mXTags = mData.getJSONArray(Tags.XTAG);    
        }
    }
    
    private String parseName(String key){
        try {
        	if(mName != null){
        		return (String)mName.get(key);
        	}
        } catch (JSONException e) {
        }
        return null;
    }
    
    private String parseProperty(String name){
        try {
            if(mData != null && mData.has(name)){
                return mData.getString(name);
            }
        } catch (JSONException e) {
        }
        return null;
    }
    
    private List<TypedEntity> parseArrayProperty(JSONArray rawData, Class<? extends JContactProperty> type) {
        if(rawData == null){
            return Collections.emptyList();
        }
        
        int length = rawData.length();
        ArrayList<TypedEntity> array = new ArrayList<TypedEntity>();
        for (int i = 0; i < length; i++) {
            try {
                JSONObject item = rawData.getJSONObject(i);
                JContactProperty entity = type.newInstance();
                entity.parse(item);
                array.add(entity);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array;
    }
}
