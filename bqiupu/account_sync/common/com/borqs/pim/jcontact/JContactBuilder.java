/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.pim.jcontact;


import com.borqs.json.JSONArray;
import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.pim.JSONUtility;

public class JContactBuilder {
    private static final boolean DEBUG = true;
    
    private boolean mFailed = false;
    private JSONObject mContact = new JSONObject();
    private JName mName = new JName();
    private JSONArray mTel = new JSONArray();
    private JSONArray mEMail = new JSONArray();
    private JSONArray mAddress = new JSONArray();
    private JSONArray mOrganization= new JSONArray();
    private JSONArray mIM= new JSONArray();
    private JSONArray mWebpage= new JSONArray();
    private JSONArray mXTags = new JSONArray();

    public JContactBuilder setFirstName(String firstName, String pinyin) {
        try{
            mName.put(JName.FIRSTNAME, firstName);
            mName.put(JName.FIRSTNAME_PINYIN, pinyin);
        } catch (JSONException e){
            mFailed = true;
            e.printStackTrace();
        }
        return this;
    }

    public JContactBuilder setMiddleName(String middleName, String pinyin) {
        try{
            mName.put(JName.MIDDLENAME, middleName);
            mName.put(JName.MIDDLENAME_PINYIN, pinyin);
        } catch (JSONException e){
            mFailed = true;
            e.printStackTrace();
        }

        return this;
    }

    public JContactBuilder setLastName(String lastName, String pinyin) {
        try{
            mName.put(JName.LASTNAME, lastName);
            mName.put(JName.LASTNAME_PINYIN, pinyin);
        } catch (JSONException e){
            mFailed = true;
            e.printStackTrace();
        }
        return this;
    }

    public JContactBuilder setNamePrefix(String prefix) {
        try{
            mName.put(JName.PREFIX, prefix);
        } catch (JSONException e){
            mFailed = true;
            e.printStackTrace();
        }
        return this;
    }
    
    public JContactBuilder setNamePostfix(String postfix) {
        try{
            mName.put(JName.POSTFIX, postfix);
        } catch (JSONException e){
            mFailed = true;
            e.printStackTrace();
        }
        return this;
    }
    
    public JContactBuilder setNickName(String nickName) {
        try{
            mName.put(JName.NICKNAME, nickName);
        } catch (JSONException e){
            mFailed = true;
            e.printStackTrace();
        }
        return this;
    }

    /**
     * add a phone number with type {@link JPhone}
     * @param type   -   type of phone number 
     * @param number -   number
     * @param isPrimary -   flag if the phone number is primary
     * @return  this object of {@link JContactBuilder}
     */
    public JContactBuilder addPhone(String type, String number, boolean isPrimary) {
        //{HOME:12345, EXTRA:[PRIMARY]}        
        try{
            mTel.put(new JPhone(type, number, isPrimary).toJson());
        } catch (JSONException e){
            mFailed = true;
            e.printStackTrace();
        }
        
        return this;
    }

    /**
     * add a email address {@link JEMail} with type
     * @param mailType  -   mail type
     * @param address     -   mail address string
     * @param isPrimary -   flag if the mail is primary
     * @return this object
     */
    public JContactBuilder addEmail(String mailType, String address, boolean isPrimary){
        //{WORK:sss@borqs.com}    
        try{
            mEMail.put(new JEMail(mailType, address, isPrimary).toJson());
        } catch (JSONException e){
            mFailed = true;
            e.printStackTrace();
        }        

        return this;
    }

    /**
     * add a address info into the contact
     * @param type
     * @param street
     * @param city
     * @param province
     * @param zipcode
     * @return
     */
    public JContactBuilder addAddress(String type, String street, String city, String province,
            String zipcode){
        //{WORK:{STREET:Wanghualu, CITY:BEIJING, ZIPCODE:10000}}
        try{
            mAddress.put(new JAddress(type, street, city, province, zipcode).toJson());
        } catch (JSONException e){
            mFailed = true;
            e.printStackTrace();
        }        
        return this;
    }

    public JContactBuilder addOrg(String type, String company, String title) {
        // {WORK:{COMPANY:borqs,TITLE:enginner}}
        try{
            mOrganization.put(new JORG(type, company, title).toJson());
        } catch (JSONException e){
            mFailed = true;
            e.printStackTrace();
        }           
        return this;
    }

    public JContactBuilder addIM(String imType, String im) {
        // {QQ:12121}
        try{
            mIM.put(new JIM(imType, im).toJson());
        } catch (JSONException e){
            mFailed = true;
            e.printStackTrace();
        }           
        return this;
    }

    public JContactBuilder addWebpage(String type, String webpage) {
        // {HOMEPAGE:http://}
        try{
            mWebpage.put(new JWebpage(type, webpage).toJson());
        } catch (JSONException e){
            mFailed = true;
            e.printStackTrace();
        }           
        return this;
    }

    public JContactBuilder setBirthday(String birthday) {
        try {
            mContact.put(Tags.BIRTHDAY, birthday);
        } catch (JSONException e) {
            mFailed = true;
            e.printStackTrace();
        }
        return this;
    }

    public JContactBuilder setNote(String note) {
        try {
            mContact.put(Tags.NOTE, note);
        } catch (JSONException e) {
            mFailed = true;
            e.printStackTrace();
        }
        return this;
    }
        
    public JContactBuilder setPhoto(byte[] photo) {
        try {
        	if(photo != null){
        		mContact.put(Tags.PHOTO, JSONUtility.encodeBytes(photo));
        	}
        } catch (JSONException e) {
            mFailed = true;
            e.printStackTrace();
        }
        return this;
    }

    public JContactBuilder addXTag(String xTag_name, String xTag_value) {
        //{X-ACCOUNT:com.borqs}
        try{
            mXTags.put(new JXTag(xTag_name, xTag_value).toJson());
        } catch (JSONException e){
            mFailed = true;
            e.printStackTrace();
        }           
        return this; 
    }
    
    public String createJson() {
        if(mFailed){
            return null;
        }
        try {
            mContact.put(Tags.NAME, mName.toJson());
            mContact.put(Tags.TEL, mTel);
            mContact.put(Tags.EMAIL, mEMail);
            mContact.put(Tags.ADDR, mAddress);
            mContact.put(Tags.ORG, mOrganization);
            mContact.put(Tags.IM, mIM);
            mContact.put(Tags.WEBPAGE, mWebpage);
            mContact.put(Tags.XTAG, mXTags);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return mContact.toString();
    }

    private boolean isEmpty(String text){
        return text==null || "".equals(text.trim());
    }
}
