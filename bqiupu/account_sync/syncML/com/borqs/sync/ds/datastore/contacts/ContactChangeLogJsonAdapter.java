/*
 * Copyright © 2012 Borqs Ltd.  All rights reserved.
 * 
 * This document is Borqs Confidential Proprietary 
 * and shall not be used, of published, or disclosed,
 * or disseminated outside of Borqs in whole or in part
 * without Borqs's permission.
 * 
 */

package com.borqs.sync.ds.datastore.contacts;

import java.util.ArrayList;
import java.util.List;

import com.borqs.json.JSONArray;
import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.syncml.ds.protocol.IDataChangeListener;
import com.borqs.syncml.ds.protocol.IDataChangeListener.ContactsChangeData;

public class ContactChangeLogJsonAdapter {

    private static final String CONTACT_NAME = "name";
    private static final String CONTACT_CONTACT_ID = "cid";
    private static final String CONTACT_RAWCONTACT_ID = "rid";

    /**
     * [{"name":"123"},{"name":"456"}]
     * 
     * @throws JSONException
     */
    public static String toJson(List<ContactsChangeData> cds) {
        JSONArray changeArray = new JSONArray();
        try {
            for (ContactsChangeData cd : cds) {
                if (cds != null) {
                    changeArray.put(toJson(cd));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return changeArray.toString();
    }

    private static JSONObject toJson(ContactsChangeData cd) throws JSONException {
        JSONObject contactObj = new JSONObject();
        contactObj.put(CONTACT_NAME, cd.name);
        contactObj.put(CONTACT_CONTACT_ID, cd.contactId);
        contactObj.put(CONTACT_RAWCONTACT_ID, cd.rawContactId);
        return contactObj;
    }

    /**
     * convert to ContactSChangeData [{"name":"123"},{"name":"456"}]
     * 
     * @param changeJson
     * @return
     * @throws JSONException
     */
    public static List<ContactsChangeData> toContactsChangeData(String changeJson) {
        List<ContactsChangeData> ccds = new ArrayList<IDataChangeListener.ContactsChangeData>();
        try {
            JSONArray changeArray = new JSONArray(changeJson);
            for (int i = 0; i < changeArray.length(); i++) {
                JSONObject contactObj = changeArray.getJSONObject(i);
                ccds.add(toContactChangeData(contactObj.toString()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ccds;
    }

    private static ContactsChangeData toContactChangeData(String changeJson) throws JSONException {
        ContactsChangeData contactData = new ContactsChangeData();
        JSONObject contactObj = new JSONObject(changeJson);
        contactData.name = contactObj.getString(CONTACT_NAME);
        contactData.contactId = contactObj.getLong(CONTACT_CONTACT_ID);
        contactData.rawContactId = contactObj.getLong(CONTACT_RAWCONTACT_ID);
        return contactData;
    }
}
