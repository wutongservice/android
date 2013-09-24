/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.sync.client.test.contactdata;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.borqs.contacts.app.ApplicationGlobals;
import com.borqs.contacts.app.ContactsApp;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.test.AndroidTestCase;
import android.util.Log;

import com.borqs.json.JSONArray;
import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.sync.client.vdata.IContactServerInfoStatus;
import com.borqs.sync.client.vdata.card.ContactOperator;
import com.borqs.sync.client.vdata.card.ContactServerInfoOperator;
import com.borqs.sync.client.vdata.card.ContactStruct;
import com.borqs.sync.ds.datastore.contacts.ContactsSrcOperator;

public class ContactServerInfoOperatorTest extends AndroidTestCase {

    private Object mSourceLock = new Object();

    private Object mGBorqsIDLock = new Object();

    private ContatInfoOperatorStatu mCos;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mCos = new ContatInfoOperatorStatu();
    }

    public void test_ContactServerInfoUpdate() {
        long[] ids = null;
        try {
            // 1.generate data
            ContactStruct[] csArray = new ContactStruct[2];
            ContactStruct cs = new ContactStruct();
            cs.setFirstName("firstname1");
            cs.setMiddleName("middleName1");
            cs.setLastName("lastName1");
            cs.addPhone(Phone.TYPE_MOBILE, "13800138001", null, true);
            cs.setSourceID(ContactsSrcOperator.DEFAULT_SOURCE_ID);
            csArray[0] = cs;

            cs = new ContactStruct();
            cs.setFirstName("firstname2");
            cs.setMiddleName("middleName2");
            cs.setLastName("lastName2");
            cs.addPhone(Phone.TYPE_MOBILE, "13800138002", null, true);
            cs.setSourceID(ContactsSrcOperator.DEFAULT_SOURCE_ID);
            csArray[1] = cs;

            // 2.insert data
            ids = ContactOperator.batchAdd(csArray, getContext().getContentResolver());
            assertTrue(ids != null);
            assertTrue(ids.length == 2);
            assertTrue(ids[0] > 0);
            assertTrue(ids[1] > 0);
            // 3.generate mock response

            String sourceID1 = "0:" + ids[0];
            String sourceID2 = "0:" + ids[1];

            JSONObject sourceMappingRoot = new JSONObject();
            JSONArray sourceMappingArray = new JSONArray();
            try {
                JSONObject sourceMappingObj1 = new JSONObject();
                sourceMappingObj1.put("luid", sourceID1);
                sourceMappingObj1.put("guid", "65151");
                sourceMappingArray.put(sourceMappingObj1);

                JSONObject sourceMappingObj2 = new JSONObject();
                sourceMappingObj2.put("luid", sourceID2);
                sourceMappingObj2.put("guid", "65152");
                sourceMappingArray.put(sourceMappingObj2);

                sourceMappingRoot.put("source_mapping", sourceMappingArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String mockResponse = sourceMappingRoot.toString();

            MockHttpClient mockHttpClient = new MockHttpClient();
            mockHttpClient.expectResponse(mockResponse);

            // 4.invoke function to update sourceID
            ContactServerInfoOperator.onSyncEnd(getContext(), mockHttpClient, mCos);

            synchronized (mSourceLock) {
                try {
                    mSourceLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // 5.assert
            cs = ContactOperator.load(ids[0], getContext().getContentResolver());
            assertEquals("65151", cs.getSourceID());

            cs = ContactOperator.load(ids[1], getContext().getContentResolver());
            assertEquals("65152", cs.getSourceID());

            // 6.update gBorqsID
            mockHttpClient = new MockHttpClient();
            /**
             * [{ "contactid": "1", "borqsid": "10010" },{ "contactid": "2",
             * "borqsid": "10011" },....,{ "contactid": "3", "borqsid": "10013"
             * }]
             */
            JSONArray mappingArray = new JSONArray();
            JSONObject obj = new JSONObject();
            try {
                obj.put("cid", 65151);
                obj.put("bid", "229");
                mappingArray.put(obj);

                obj = new JSONObject();
                obj.put("cid", 65152);
                obj.put("bid", "");
                mappingArray.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mockHttpClient.expectResponse(mappingArray.toString());
            // 7.invoke function to update gBorqsID
            ContactServerInfoOperator.onSyncEnd(getContext(), mockHttpClient, mCos);

            synchronized (mGBorqsIDLock) {
                try {
                    mGBorqsIDLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // 8.assert
            Cursor cursor = getContext().getContentResolver().query(
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, ids[0]), new String[] {
                        RawContacts.SYNC3
                    }, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToNext()) {
                        assertEquals("229", cursor.getString(0));
                    }
                } finally {
                    cursor.close();
                }
            }

            cursor = getContext().getContentResolver().query(
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, ids[1]), new String[] {
                        RawContacts.SYNC3
                    }, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToNext()) {
                        assertEquals("", cursor.getString(0));
                    }
                } finally {
                    cursor.close();
                }
            }
        } finally {
            ContactOperator.delete(ids[0], getContext().getContentResolver());
            ContactOperator.delete(ids[1], getContext().getContentResolver());
        }
    }

    class ContatInfoOperatorStatu implements IContactServerInfoStatus {

        @Override
        public void onSourceIDReady(Context context) {
            synchronized (mSourceLock) {
                mSourceLock.notifyAll();
            }
        }

        @Override
        public void onSourceIDReadyError(Context context) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onGBorqsIDReady(Context context) {
            synchronized (mGBorqsIDLock) {
                mGBorqsIDLock.notifyAll();
            }

        }

        @Override
        public void onGBorqsIDReadyError(Context context) {
            // TODO Auto-generated method stub

        }

    }

}
