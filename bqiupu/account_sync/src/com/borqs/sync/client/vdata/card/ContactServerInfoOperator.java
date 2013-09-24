/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.sync.client.vdata.card;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.common.account.AccountAdapter;
import com.borqs.common.transport.AccountClient;
import com.borqs.common.account.AccountException;
import com.borqs.common.account.ProfileInfo;
import com.borqs.common.transport.SimpleHttpClient;
import com.borqs.common.util.BLog;
import com.borqs.json.JSONArray;
import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.sync.client.common.SyncDeviceContext;
import com.borqs.sync.client.transport.ContactServiceClient;
import com.borqs.sync.client.vdata.IContactServerInfoStatus;
import com.borqs.sync.ds.datastore.contacts.ContactsSrcOperator;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * create for Contact server info operation like contact serverId,contact
 * GBorqsID
 * 
 * @author b211
 */
public class ContactServerInfoOperator {

    private static final String TAG = "ContactServerInfoOperator";

    private Context mContext;

    private String mAccountId;

    private String mAccountName;

    private SyncDeviceContext mSyncDeviceContext;

    private HttpClient mHttpClient;

    private ContactServerInfoOperator(Context context, HttpClient httpClient) {
        mContext = context;
        mAccountId = AccountAdapter.getUserID(mContext);
        mAccountName = AccountAdapter.getLoginID(mContext);
        mSyncDeviceContext = new SyncDeviceContext(mContext);
        mHttpClient = httpClient;
    }

    public static void onSyncEnd(Context context, HttpClient httpClient,
            IContactServerInfoStatus contactServerInfoReady) {
        // update the sourceId of added contacts upload to server
        if (httpClient == null) {
            httpClient = SimpleHttpClient.get();
        }
        ContactServerInfoOperator cso = new ContactServerInfoOperator(context, httpClient);
        cso.processUpdateSourceIDs(context, contactServerInfoReady);
    }

    private void processUpdateSourceIDs(final Context context,
            final IContactServerInfoStatus contactServerInfoReady) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean sourceIDReady = updateSourceIDs(getLuidsWithoutSource());
                if (sourceIDReady) {
                    contactServerInfoReady.onSourceIDReady(context);
                    onSourceIDReady(context, contactServerInfoReady);
                } else {
                    contactServerInfoReady.onSourceIDReadyError(context);
                    contactServerInfoReady.onGBorqsIDReadyError(context);
                }
            }

        }).start();
    }

    private void onSourceIDReady(Context context, IContactServerInfoStatus contactServerInfoReady) {
        processUpdateGBorqsID(context, contactServerInfoReady);
    }

    // update global borqsID which registered in Borqs service
    private void processUpdateGBorqsID(final Context context,
            final IContactServerInfoStatus contactServerInfoReady) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean gBorqsIDReady = updateGBorqsID(context);
                if (gBorqsIDReady && processVerificationContactInfo(context)) {
                    contactServerInfoReady.onGBorqsIDReady(context);
                } else {
                    contactServerInfoReady.onGBorqsIDReadyError(context);
                }
            }

        }).start();
    }

    /**
     * update the sourceId whose sourceId is "12535"(means the contact is added
     * from client add upload to server successfully)
     */
    private boolean updateSourceIDs(List<String> luids) {
        // 1.batchQuery the sourceIDs from server by contactIDs
        ContactServiceClient csc = new ContactServiceClient(mContext, mHttpClient);
        try {
            if (luids == null || luids.size() <= 0) {
                BLog.d(TAG, "all added contacts have sourceId,do not need update");
                return true;
            }
            String response = csc.getSourceIDsByLuid(mAccountId, mSyncDeviceContext.getDeviceId(),
                    luids);
            // 2.batch update the sourceIDs
            updateSourceIdByContact(response, mContext);
            return true;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateSourceIdByContact(String response, Context context) {
        // 1.generate the sourceIDMapping from response
        Map<Long, Long> sourceIdMapping = parseSourceIDs(response);

        // 2.execute update by mapping
        ContactProviderOperation cpo = new ContactProviderOperation(context.getContentResolver());
        Set<Long> contactIdSet = sourceIdMapping.keySet();
        if (contactIdSet != null) {
            for (Long contactId : contactIdSet) {
                ContentValues values = new ContentValues();
                values.put(RawContacts.SOURCE_ID, String.valueOf(sourceIdMapping.get(contactId)));
                cpo.newUpdate(contactId, values);
                if (cpo.size() == ContactProviderOperation.MAX_OPERATIONS_PER_YIELD_POINT - 1) {
                    cpo.execute();
                    cpo.clear();
                }
            }
            cpo.execute();
        }
    }

    private Map<Long, Long> parseSourceIDs(String reponse) {
        // response:{"source_mapping":[{"luid":"0:100","guid":"65150"},{"luid":"0:101","guid":"65151"},
        // {"luid":"0:102","guid":"65152"}]}
        Map<Long, Long> sourceIdMapping = new HashMap<Long, Long>();
        try {
            if (TextUtils.isEmpty(reponse)) {
                throw new IllegalStateException("query source Id error!reponse is null");
            }
            // BLog.d(TAG, "source_mapping resonse:" + reponse);
            JSONArray sourceArray = new JSONArray(reponse);
            for (int i = 0; i < sourceArray.length(); i++) {
                JSONObject mappingObj = sourceArray.getJSONObject(i);
                String luid = mappingObj.getString("luid");
                String guid = mappingObj.getString("guid");
                if (!TextUtils.isEmpty(luid) && !TextUtils.isEmpty(guid)) {
                    String[] luidArray = luid.split(":");
                    sourceIdMapping.put(Long.parseLong(luidArray[1]), Long.parseLong(guid));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sourceIdMapping;
    }

    /**
     * @return the luid(0:raw_contactId) who is added from client then sync to
     *         server successfully. and never be updated by server.Means the
     *         srcId is "12535" defined by client.
     */
    private List<String> getLuidsWithoutSource() {
        // 1.check the added contactsId(sourceid=12535,means the contact is
        // added in client and sync to server successfully)
        List<String> contactIDs = new ArrayList<String>();
        if(TextUtils.isEmpty(mAccountName)){
            return contactIDs;
        }

        Cursor cursor = mContext.getContentResolver().query(
                RawContacts.CONTENT_URI,
                new String[] {
                    RawContacts._ID
                },
                RawContacts.ACCOUNT_TYPE + "=? AND " + RawContacts.ACCOUNT_NAME + "=? AND "
                        + RawContacts.SOURCE_ID + "=?",
                new String[] {
                        AccountAdapter.BORQS_ACCOUNT_TYPE, mAccountName,
                        ContactsSrcOperator.DEFAULT_SOURCE_ID
                }, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    long contactId = cursor.getLong(0);
                    contactIDs.add("0:" + contactId);
                }
            } finally {
                cursor.close();
            }
        }
        BLog.d(TAG, "the id of contact will be update sourceId is :" + contactIDs.toString());
        return contactIDs;
    }

    private boolean updateGBorqsID(Context context) {
        // 1.the GBorqsID from server by userid
        ContactServiceClient csc = new ContactServiceClient(mContext, mHttpClient);

        try {
            String gBorqsIDsJson = csc.getContactBorqsIDs(mAccountId);
            if(TextUtils.isEmpty(gBorqsIDsJson)){
            	return false;
            }
            /**
             * [{ "cid": "1", "bid": "10010" },{ "cid": "2", "bid": "10011"
             * },....,{ "cid": "3", "bid": "10013" }]
             */

            updateGBorqsIDByContact(mContext, gBorqsIDsJson);
            return true;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Map<Long, String> parseGBorqsIDResponse(String gBorqsIDsJson) {
        // BLog.d(TAG, "borqsIds mapping Json:" + gBorqsIDsJson);
        Map<Long, String> gBorqsIdMapping = new HashMap<Long, String>();
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(gBorqsIDsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mappingObj = jsonArray.getJSONObject(i);
                long sourceId = mappingObj.getLong("cid");
                String gBorqsID = mappingObj.getString("bid");
                gBorqsIdMapping.put(sourceId, gBorqsID);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return gBorqsIdMapping;
    }

    private void updateGBorqsIDByContact(Context context, String gBorqsIDsJson) {
        //TODO https://borqsbt2.borqs.com/view.php?id=59381
        //workaround,catch the exception.should find the root cause .
        try{
        ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();

        String where = RawContacts.ACCOUNT_TYPE + "=? AND " + RawContacts.ACCOUNT_NAME + "=? AND "
                + RawContacts.SOURCE_ID + "=?";

        Map<Long, String> gBorqsIdMapping = parseGBorqsIDResponse(gBorqsIDsJson);
        Set<Long> sourceIDSet = gBorqsIdMapping.keySet();
            if (sourceIDSet != null) {
                for (Long sourceID : sourceIDSet) {
                    // update sync3 by account_type and account_name and sourceid
                    ContentValues values = new ContentValues();
                    values.put(RawContacts.SYNC3, gBorqsIdMapping.get(sourceID));
                    cpo.add(ContentProviderOperation
                            .newUpdate(
                                    RawContacts.CONTENT_URI
                                            .buildUpon()
                                            .appendQueryParameter(
                                                    ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                                            .build())
                            .withSelection(
                                    where,
                                    new String[] {
                                            AccountAdapter.BORQS_ACCOUNT_TYPE, mAccountName,
                                            String.valueOf(sourceID)
                                    }).withValues(values).build());
                    if (cpo.size() == ContactProviderOperation.MAX_OPERATIONS_PER_YIELD_POINT - 1) {
                        try {
                            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpo);
                            cpo.clear();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (OperationApplicationException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpo);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //update the contact info which had verified
    private boolean processVerificationContactInfo(Context context){
        Log.d(TAG, "processVerificationContactInfo() begin");
        boolean  result = true;
        Map<String, Long> ids = getSocialContacts();

        //step1. group the borqs_id by number 10
        List<String> uids = buildBorqsIdArgsList(ids.keySet());
        AccountClient ac = new AccountClient(context, mHttpClient);
        String fields = "user_id,login_phone1,login_phone2,login_phone3,login_email1,login_email2,login_email3";

        //step2, fetch info for each group
        if(!uids.isEmpty()){
        	//1.before update verified status,we clear previous verified status
        	clearVerifiedStatus(context);
        }
        for(String idList : uids){
            Log.d(TAG, "processVerificationContactInfo() handle list:" + idList);
            try {
                List<ProfileInfo> profileInfos = ac.retrieveUserList(idList, mAccountId, fields);
                Log.d(TAG, "processVerificationContactInfo() got result list:" + profileInfos.size());
                ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();
                
            	//2.we update the verified status
                for(ProfileInfo info : profileInfos){
                    String borqsId = info.get_user_id();
                    long raw_contact_id = ids.get(borqsId);

                    //update for verified phones
                    String[] phones = new String[]{info.get_login_phone1(), info.get_login_phone2(),info.get_login_phone3()};
                    for(String phone : phones){
                        if(TextUtils.isEmpty(phone)) continue;
                        cpo.add(buildUpdatePhoneVerifiedStatus(raw_contact_id, phone));
                    }

                    //update for verified emails
                    String[] emails = new String[]{info.get_login_email1(), info.get_login_email2(),info.get_login_email3()};
                    for(String mail : emails){
                        if(TextUtils.isEmpty(mail)) continue;
                        cpo.add(buildUpdateEmailVerifiedStatus(raw_contact_id, mail));
                    }

                    if(cpo.size() > ContactProviderOperation.MAX_OPERATIONS_PER_YIELD_POINT-50){
                        context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpo);
                        cpo.clear();
                    }
                }
                if(cpo.size() > 0){
                    context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpo);
                }

                check();
            } catch (IOException e) {
                result = false; e.printStackTrace();
            } catch (AccountException e) {
                result = false; e.printStackTrace();
            } catch (org.json.JSONException e) {
                result = false; e.printStackTrace();
            } catch (RemoteException e) {
                result = false; e.printStackTrace();
            } catch (OperationApplicationException e) {
                result = false; e.printStackTrace();
            } catch (Exception e){
                result = false; e.printStackTrace();
            }
        }

        Log.d(TAG, "processVerificationContactInfo() end");
        return result;
    }

	private void clearVerifiedStatus(Context context) {
		List<Long> contactIds = getAllBorqsContactsId(context);
		String clearWhere = "(0=1 ";
		if (contactIds.isEmpty()) {
			return;
		}
		for (Long id : contactIds) {
			clearWhere += " OR " + ContactsContract.Data.RAW_CONTACT_ID + "="
					+ id;
		}
		clearWhere += ") AND (" + ContactsContract.Data.MIMETYPE + "=? OR "
				+ ContactsContract.Data.MIMETYPE + "=?)";
		ContentValues values = new ContentValues();
		values.put(ContactsContract.Data.SYNC1, false);
		context.getContentResolver()
				.update(ContactsContract.Data.CONTENT_URI
						.buildUpon()
						.appendQueryParameter(
								ContactsContract.CALLER_IS_SYNCADAPTER, "true")
						.build(),
						values,
						clearWhere,
						new String[] {
								ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
								ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE });

	}
	
	private List<Long> getAllBorqsContactsId(Context context){
		List<Long> ids = new ArrayList<Long>();
		String accountName = AccountAdapter.getLoginID(context);
        if(TextUtils.isEmpty(accountName)){
            BLog.e("accountName is null,getAllBorqsContactsId failed!");
            return ids;
        }
		String accountType = AccountAdapter.BORQS_ACCOUNT_TYPE;
		Cursor cursor = context.getContentResolver().query(
				ContactsContract.RawContacts.CONTENT_URI,
				new String[] { ContactsContract.RawContacts._ID },
				ContactsContract.RawContacts.ACCOUNT_TYPE + "=? AND "
						+ ContactsContract.RawContacts.ACCOUNT_NAME + "=?"
						+ " AND NOT (" +RawContacts.SYNC3 + "='' OR " + RawContacts.SYNC3 +" is null)",
				new String[]{accountType,accountName}, null);
		if(cursor != null){
			try{
				while(cursor.moveToNext()){
					ids.add(cursor.getLong(0));
				}
			}finally{
				cursor.close();
			}
		}
		return ids;
	}

	class TestD{
        TestD(long id, String type, String value){raw_id=id;t=type;v=value;}
        long raw_id;
        String t;
        String v;
    }
    //============================================
    List<TestD> data_ids = new ArrayList<TestD>();
    private void check(){

        Cursor c = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data.RAW_CONTACT_ID, ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Email.ADDRESS, ContactsContract.Data._ID},
                ContactsContract.Data.SYNC1+"=?", new String[]{String.valueOf(true)},null);
        while(c.moveToNext()){
            long raw_id = c.getLong(0);
            String type = c.getString(1);
            String phone = c.getString(2);
            String email = c.getString(3);
            long id = c.getLong(4);
            for(TestD d: data_ids){
                if(d.raw_id == raw_id && d.t.equals(type)){
                    if(type.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) && !d.v.equals(phone)){
                        Log.d(TAG, "FAILED phone on " + id);
                    } else if(type.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE) && !d.v.equals(email)){
                        Log.d(TAG, "FAILED email on " + id);
                    }
                }
            }
        }

    }
    //============================================


    private ContentProviderOperation buildUpdatePhoneVerifiedStatus(long raw_contact_id,
            String value) {
        String where = ContactsContract.Data.RAW_CONTACT_ID + "=? AND "
                + ContactsContract.Data.MIMETYPE + "=? AND ";
        String mime_type = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
        where += ContactsContract.CommonDataKinds.Phone.DATA4 + " like '%" + value + "'";
        data_ids.add(new TestD(raw_contact_id,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, value));

        ContentValues values = new ContentValues();
        values.put(ContactsContract.Data.SYNC1, true);
        return ContentProviderOperation
                .newUpdate(
                        ContactsContract.Data.CONTENT_URI
                                .buildUpon()
                                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,
                                        "true").build()).withSelection(where, new String[] {
                        String.valueOf(raw_contact_id), mime_type
                }).withValues(values).build();
    }
    
    private ContentProviderOperation buildUpdateEmailVerifiedStatus(long raw_contact_id,
            String value) {
        String where = ContactsContract.Data.RAW_CONTACT_ID + "=? AND "
                + ContactsContract.Data.MIMETYPE + "=? AND ";
        String mime_type = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
        where += ContactsContract.CommonDataKinds.Email.ADDRESS + "=?";
        mime_type = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;
        data_ids.add(new TestD(raw_contact_id,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, value));

        ContentValues values = new ContentValues();
        values.put(ContactsContract.Data.SYNC1, true);
        return ContentProviderOperation
                .newUpdate(
                        ContactsContract.Data.CONTENT_URI
                                .buildUpon()
                                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,
                                        "true").build()).withSelection(where, new String[] {
                        String.valueOf(raw_contact_id), mime_type, value
                }).withValues(values).build();
    }
    


    private Map<String, Long>  getSocialContacts() {
        // 1.check the added contactsId(sourceid=12535,means the contact is
        // added in client and sync to server successfully)
        Map<String, Long>  borqsIds = new HashMap<String, Long>();
        if(TextUtils.isEmpty(mAccountName)){
            return borqsIds;
        }

        Cursor cursor = mContext.getContentResolver().query(
                RawContacts.CONTENT_URI,
                new String[] { RawContacts._ID, RawContacts.SYNC3},
                        RawContacts.ACCOUNT_TYPE + "=? AND " + RawContacts.ACCOUNT_NAME + "=? AND " +
                        "NOT (" +RawContacts.SYNC3 + "='' OR " + RawContacts.SYNC3 +" is null)",
                new String[] { AccountAdapter.BORQS_ACCOUNT_TYPE, mAccountName}, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    borqsIds.put(cursor.getString(1), cursor.getLong(0));
                }
            } finally {
                cursor.close();
            }
        }

        return borqsIds;
    }

    private List<String> buildBorqsIdArgsList(Set<String> ids){
        List<String> args = new ArrayList<String>();

        StringBuilder sBuilder = new StringBuilder();
        int count = 0;
        for(String id : ids){
            if(sBuilder.length()>0) sBuilder.append(",");
            sBuilder.append(id);
            count ++;
            if(count % 10 == 0){
                args.add(sBuilder.toString());
                sBuilder = new StringBuilder();
            }
        }
        if(sBuilder.length() > 0){
            args.add(sBuilder.toString());
        }

        BLog.d(TAG, "split args in list: " + args);
        return args;
    }

//    private class SocialID{
//        public SocialID(String b_id, long c_id){borqs_id=b_id;raw_contact_id=c_id;}
//        public String borqs_id;
//        public long raw_contact_id;
//    }
}
