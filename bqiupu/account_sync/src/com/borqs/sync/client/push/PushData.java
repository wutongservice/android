/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.sync.client.push;

import java.util.ArrayList;
import java.util.List;

import com.borqs.json.JSONArray;
import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.sync.client.common.Logger;

public class PushData {

    private static final String TAG = "PushMessage";

    private static final String PUSH_DATA_ACTION_KEY = "action";
    private static final String PUSH_DATA_VALUE_KEY = "value";
    private static final String PUSH_DATA_SYNC_DEVICES_KEY = "sync_devices";

    private String mDataAction;

    private List<String> mDevices = new ArrayList<String>();

    private PushData() {
    }

    public List<String> getNeedSyncDevices() {
        return mDevices;
    }

    public String getDataAction() {
        return mDataAction;
    }

    public static PushData parse(String data) {
        PushData pushData = new PushData();
        try {
            JSONObject dataJson = new JSONObject(data);
            pushData.mDataAction = dataJson.getString(PUSH_DATA_ACTION_KEY);
            Logger.logD(TAG, "data_action :" + pushData.mDataAction);
            JSONObject valueJson = dataJson.getJSONObject(PUSH_DATA_VALUE_KEY);
            parseValue(valueJson, pushData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pushData;
    }

    private static void parseValue(JSONObject valueJson, PushData pushData) throws JSONException {
        if (valueJson.has(PUSH_DATA_SYNC_DEVICES_KEY)) {
            JSONArray deviceArray = valueJson.getJSONArray(PUSH_DATA_SYNC_DEVICES_KEY);
            for (int i = 0; i < deviceArray.length(); i++) {
                String deviceId = deviceArray.getString(i);
                pushData.mDevices.add(deviceId);
            }
        }
    }

}
