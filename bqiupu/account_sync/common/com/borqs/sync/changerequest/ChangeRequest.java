
package com.borqs.sync.changerequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.pim.jcontact.JContact;

/**
 * Created by IntelliJ IDEA. User: b211 Date: 11/15/11 Time: 2:31 PM To change
 * this template use File | Settings | File Templates.
 */
public class ChangeRequest {

    private Map<Integer, List<JContact.TypedEntity>> mPhoneMap;
    private Map<Integer, List<JContact.TypedEntity>> mEmailMap;
    private Map<Integer, String> mNameMap;
    private String mContactFirstName;
    private String mContactMiddleName;
    private String mContactLastName;
    private String mOriginalName;

    private ChangeRequest() {
        mPhoneMap = new HashMap<Integer, List<JContact.TypedEntity>>();
        mEmailMap = new HashMap<Integer, List<JContact.TypedEntity>>();
        mNameMap = new HashMap<Integer, String>();
    }

    /**
     * @param changeType define in the ChangeRequestBuilder:
     *            CHANGE_TYPE_ADD,CHANGE_TYPE_UPDATE,CHANGE_TYPE_DELETE
     * @return added/updated/deleted phone,null:no specify type changed phone
     */
    public List<JContact.TypedEntity> getChangedPhone(int changeType) {
        List<JContact.TypedEntity> reallyChangedList = new ArrayList<JContact.TypedEntity>();
        if (mPhoneMap.containsKey(changeType)) {
            List<JContact.TypedEntity> changedList = mPhoneMap.get(changeType);
            for (JContact.TypedEntity phone : changedList) {
                if (!phone.getType()
                        .startsWith(ChangeRequestBuilder.CHANGE_ITEM_ORIGINAL_VALUE_PRE)) {
                    reallyChangedList.add(phone);
                }
            }
        }
        return reallyChangedList;
    }

    /**
     * @param changeType define in the ChangeRequestBuilder:
     *            CHANGE_TYPE_ADD,CHANGE_TYPE_UPDATE,CHANGE_TYPE_DELETE
     * @return added/updated/deleted email,null:no specify type changed email
     */
    public List<JContact.TypedEntity> getChangedEmail(int changeType) {
        List<JContact.TypedEntity> reallyChangedList = new ArrayList<JContact.TypedEntity>();
        if (mEmailMap.containsKey(changeType)) {
            List<JContact.TypedEntity> changedList = mEmailMap.get(changeType);
            for (JContact.TypedEntity email : changedList) {
                if (!email.getType()
                        .startsWith(ChangeRequestBuilder.CHANGE_ITEM_ORIGINAL_VALUE_PRE)) {
                    reallyChangedList.add(email);
                }
            }
        }
        return reallyChangedList;
    }

    /**
     * @param changeType
     * @return null,no specify type changed name
     */
    public String getChangedName(int changeType) {
        if (mNameMap.containsKey(changeType)) {
            return mNameMap.get(changeType);
        }
        return null;
    }

    /**
     * @param jType jphone of JPIM,like MOBILE,HOME,WORK
     * @return the original phone value
     */
    public String getUpdatePhoneOriginalValue(String jType) {
        List<JContact.TypedEntity> updatedPhones = mPhoneMap
                .get(ChangeRequestBuilder.CHANGE_TYPE_UPDATE);
        if (updatedPhones != null) {
            for (JContact.TypedEntity phone : updatedPhones) {
                if ((ChangeRequestBuilder.CHANGE_ITEM_ORIGINAL_VALUE_PRE + jType).equals(phone
                        .getType())) {
                    return phone.getValue().toString();
                }
            }
        }
        return null;
    }

    /**
     * @param jType jemail of JPIM,like HOME,WORK,OTHER
     * @return the original email value
     */
    public String getUpdatedEmailOriginalValue(String jType) {
        List<JContact.TypedEntity> updatedEmail = mEmailMap
                .get(ChangeRequestBuilder.CHANGE_TYPE_UPDATE);
        if (updatedEmail != null) {
            for (JContact.TypedEntity email : updatedEmail) {
                if ((ChangeRequestBuilder.CHANGE_ITEM_ORIGINAL_VALUE_PRE + jType).equals(email
                        .getType())) {
                    return email.getValue().toString();
                }
            }
        }
        return null;
    }

    /**
     * @return the original name if the name is updated
     */
    public String getUpdateNameOriginalValue() {
        return mOriginalName;
    }

    public String getContactFirstName() {
        return mContactFirstName;
    }

    public String getContactMiddleName() {
        return mContactMiddleName;
    }

    public String getContactLastName() {
        return mContactLastName;
    }

    public static ChangeRequest parse(String changeRequest) {
        ChangeRequest change = new ChangeRequest();
        try {
            change.parseData(new JSONObject(changeRequest));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return change;
    }

    private void parseData(JSONObject changeRequestJson) {
        try {
            // parse contact
            JSONObject changeContact = changeRequestJson
                    .getJSONObject(ChangeRequestBuilder.CHANGE_REQUEST_CONTACT_KEY);
            JSONObject addedItem = changeContact
                    .getJSONObject(ChangeRequestBuilder.CHANGE_REQUEST_CONTACT_ADDED_KEY);
            parseChangedItem(addedItem, ChangeRequestBuilder.CHANGE_TYPE_ADD);
            JSONObject updatedItem = changeContact
                    .getJSONObject(ChangeRequestBuilder.CHANGE_REQUEST_CONTACT_UPDATED_KEY);
            parseChangedItem(updatedItem, ChangeRequestBuilder.CHANGE_TYPE_UPDATE);

            // parse contact name detail
            JSONObject contactName = changeRequestJson
                    .getJSONObject(ChangeRequestBuilder.CHANGE_REQUEST_CONTACT_NAME_KEY);
            mContactFirstName = contactName
                    .getString(ChangeRequestBuilder.CHANGE_REQUEST_CONTACT_NAME_FIRST_NAME_KEY);
            mContactMiddleName = contactName
                    .getString(ChangeRequestBuilder.CHANGE_REQUEST_CONTACT_NAME_MIDDLE_NAME_KEY);
            mContactLastName = contactName
                    .getString(ChangeRequestBuilder.CHANGE_REQUEST_CONTACT_NAME_LAST_NAME_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseChangedItem(JSONObject changedItem, int changeType) {
        JContact jContact = JContact.fromJsonString(changedItem.toString());
        // phone
        mPhoneMap.put(changeType, jContact.getPhoneList());
        // email
        mEmailMap.put(changeType, jContact.getEmailList());
        // name
        mOriginalName = jContact.getFirstNamePinyin();
        mNameMap.put(changeType, jContact.getFirstName());
    }

}
