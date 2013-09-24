/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.sync.changerequest;

import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.pim.jcontact.JContactBuilder;

public class ChangeRequestBuilder {

    public static final int CHANGE_TYPE_ADD = 1;
    public static final int CHANGE_TYPE_UPDATE = 2;

    public static final String CHANGE_ITEM_ORIGINAL_VALUE_PRE = "ORIGINAL-";
    
    public static final String CHANGE_REQUEST_CONTACT_KEY = "changed_contact";
    public static final String CHANGE_REQUEST_CONTACT_NAME_KEY = "contact_name";

    public static final String CHANGE_REQUEST_CONTACT_NAME_FIRST_NAME_KEY = "first_name";
    public static final String CHANGE_REQUEST_CONTACT_NAME_MIDDLE_NAME_KEY = "middle_name";
    public static final String CHANGE_REQUEST_CONTACT_NAME_LAST_NAME_KEY = "last_name";
    
    public static final String CHANGE_REQUEST_CONTACT_ADDED_KEY = "added";
    public static final String CHANGE_REQUEST_CONTACT_UPDATED_KEY = "updated";

    private JContactBuilder mAddedBuilder;
    private JContactBuilder mUpdatedBuilder;

    private String mReceiverID;
    private boolean mChanged;
    
    private String mFirstName;
    private String mMiddleName;
    private String mLastName;

    public ChangeRequestBuilder() {
        mAddedBuilder = new JContactBuilder();
        mUpdatedBuilder = new JContactBuilder();
    }

    public String compose() {
        JSONObject changedContact = new JSONObject();
        JSONObject contactValue = composeContact();
        JSONObject contactNameValue = composeContactName();
        try {
            changedContact.put(CHANGE_REQUEST_CONTACT_KEY, contactValue);
            changedContact.put(CHANGE_REQUEST_CONTACT_NAME_KEY, contactNameValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return changedContact.toString();
    }

    private JSONObject composeContactName(){
        JSONObject contactName = new JSONObject();
        try {
            contactName.put(CHANGE_REQUEST_CONTACT_NAME_FIRST_NAME_KEY,mFirstName);
            contactName.put(CHANGE_REQUEST_CONTACT_NAME_MIDDLE_NAME_KEY,mMiddleName);
            contactName.put(CHANGE_REQUEST_CONTACT_NAME_LAST_NAME_KEY,mLastName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return contactName;
    }

    private JSONObject composeContact() {
        JSONObject contact = new JSONObject();
        String addedItem = mAddedBuilder.createJson();
        String updatedItem = mUpdatedBuilder.createJson();
        try {
            contact.put(CHANGE_REQUEST_CONTACT_ADDED_KEY, new JSONObject(addedItem));
            contact.put(CHANGE_REQUEST_CONTACT_UPDATED_KEY, new JSONObject(updatedItem));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return contact;
    }

    public void addPhone(int changeType, String type, String number) {
        switch (changeType) {
            case CHANGE_TYPE_ADD:
                mChanged = true;
                mAddedBuilder.addPhone(type, number, false);
                break;
            // case CHANGE_TYPE_DELETE:
            // mDeletedBuilder.addPhone(type, number, false);
            // break;
            case CHANGE_TYPE_UPDATE:
                mChanged = true;
                mUpdatedBuilder.addPhone(type, number, false);
                break;
            default:
                System.out.println("unknown changetype,do nothing");
        }
    }

    public void addEmail(int changeType, String type, String email) {
        switch (changeType) {
            case CHANGE_TYPE_ADD:
                mChanged = true;
                mAddedBuilder.addEmail(type, email, false);
                break;
            // case CHANGE_TYPE_DELETE:
            // mDeletedBuilder.addEmail(type, email, false);
            // break;
            case CHANGE_TYPE_UPDATE:
                mChanged = true;
                mUpdatedBuilder.addEmail(type, email, false);
                break;
            default:
                System.out.println("unknown changetype,do nothing");
        }
    }

    public void setReceiverID(String selfUserId) {
        mReceiverID = selfUserId;
    }

    public String getReceiverID() {
        return mReceiverID;
    }

    /**
     * use first name of JPIM for saving displayname TODO//current,we only use
     * FirstName to represent the Displayname, FirstNamePinyin to represent the
     * ORIGINAL-DISPLAYNAME
     * 
     * @param changeType
     * @param pinyin
     */
    public void addName(int changeType, String name, String pinyin) {
        switch (changeType) {
            case CHANGE_TYPE_ADD:
                mChanged = true;
                mAddedBuilder.setFirstName(name, pinyin);
                break;
            // case CHANGE_TYPE_DELETE:
            // mDeletedBuilder.setFirstName(name,pinyin);
            // break;
            case CHANGE_TYPE_UPDATE:
                mChanged = true;
                mUpdatedBuilder.setFirstName(name, pinyin);
                break;
            default:
                System.out.println("unknown changetype,do nothing");
        }
    }

    public boolean hasChange() {
        return mChanged;
    }

    /**
     * set the contact name detail when compose the change request.
     * its level in the json to client is same to change request.
     * @param firstName
     * @param middleName
     * @param lastName
     */
    public void setName(String firstName,String middleName,String lastName){
        mFirstName = firstName;
        mMiddleName = middleName;
        mLastName = lastName;
    }
}
