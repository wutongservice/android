package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.quickaction.ActionItem;
import com.borqs.common.util.IntentUtil;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.StringUtil;

public class ProfileActionGatewayActivity extends BasicActivity {

	private static final String TAG = "ProfileActionGatewayActivity";
	
    private static final String ACTION_VIEW_PROFILE = "view";
    private static final String ACTION_ADD_TO_CIRCLES = "addtocircle";
    private static final String ACTION_REQUEST_CONVERSATION = "conversation";

    private static final String ACTION_INVITE_CONTACT = "inviteContact";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String uid = "";        
        String action = "";

        Uri dataUri = getIntent().getData();
        if(dataUri != null) {
        	Cursor cursor = getContentResolver().query(dataUri, new String[]{ContactsContract.Data.RAW_CONTACT_ID,
        			ContactsContract.Data.DATA4}, null, null, null);
        	if (cursor != null) {
        		try {
        			if (cursor.moveToNext()) {
        				long rawContactId = cursor.getLong(0);
        				uid = queryBorqsIDByContact(this.getApplicationContext(), rawContactId);
        				action = cursor.getString(1);
        			}
        		} finally {
        			cursor.close();
        		}
        	}
        }else {
        	action = getIntent().getAction();
        	uid = getIntent().getStringExtra("uid");
        }
        
        if(action.equalsIgnoreCase(ACTION_VIEW_PROFILE))
        {
        	IntentUtil.startUserDetailIntent(this, Long.parseLong(uid));        	
        }
        else  if(action.equalsIgnoreCase(ACTION_REQUEST_CONVERSATION))
        {
        	HashMap<String, String> map = new HashMap<String, String>();
            IntentUtil.startComposeIntent(this, String.valueOf(uid), true, map);
        }
        else  if(action.equalsIgnoreCase(ACTION_ADD_TO_CIRCLES))
        {
        	IntentUtil.startCircleSelectIntent(this, Long.parseLong(uid), null);
        }
        
        finish();
	}
	@Override
	protected void createHandler() {
		mHandler = new Handler();		
	}

    // For Contacts provider
    private static final String ACCOUNT_COLUMN_ID = ContactsContract.RawContacts._ID;
    private static final String ACCOUNT_COLUMN_CONTACT_ID = ContactsContract.RawContacts._ID;
    private static final String ACCOUNT_COLUMN_BORQS_ID = ContactsContract.RawContacts.SYNC4;
    private static final String[] ACCOUNT_BORQS_ID_PROJECTION = new String[]{
            ACCOUNT_COLUMN_ID, ACCOUNT_COLUMN_CONTACT_ID,
            ACCOUNT_COLUMN_BORQS_ID};

    private static String queryBorqsIDByContact(Context context,
                                                long id) {
        final String contactId = String.valueOf(id);
        Cursor cursor = context.getContentResolver().query(
                ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI,
                        Long.valueOf(contactId)), ACCOUNT_BORQS_ID_PROJECTION,
                null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    return cursor.getString(cursor
                            .getColumnIndexOrThrow(ACCOUNT_COLUMN_BORQS_ID));
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

//    // Set borqs plus entity in Contacts.
//    public static void setBorqsPlusEntities(Context context) {
//        Log.d(TAG, "setBorqsPlusEntities, invoking.");
//        ContactService cs = new ContactService(context);
//        List<ContactService.BorqsPlus> pluses = new ArrayList<ContactService.BorqsPlus>();
//        pluses.add(cs.new BorqsPlus(context.getString(R.string.view_bpc_profile), ACTION_VIEW_PROFILE));
//        pluses.add(cs.new BorqsPlus(context.getString(R.string.add_to_circles), ACTION_ADD_TO_CIRCLES));
//        pluses.add(cs.new BorqsPlus(context.getString(R.string.request_conversion), ACTION_REQUEST_CONVERSATION));
////        pluses.add(cs.new BorqsPlus(context.getString(R.string.invite_contact), ACTION_INVITE_CONTACT));
//        cs.setBorqsContactPlus(context, ContactService.MIME_TYPE_BORQS_PLUS_WUTONG, pluses);
//    }

    public static boolean isSensitiveActionList(final String action) {
        if (ACTION_ADD_TO_CIRCLES.equals(action)
                || ACTION_REQUEST_CONVERSATION.equals(action)
                || ACTION_VIEW_PROFILE.equals(action)) {
            return true;
        }

        return false;
    }

    public static boolean isInviteContactAction(final String action) {
        return ACTION_INVITE_CONTACT.equalsIgnoreCase(action);
    }

    public static ArrayList<ActionItem> generateOtherActionList(Context context, String contactId, String userId) {
        ArrayList<ActionItem> otherActionList = new ArrayList<ActionItem>();
        if(!TextUtils.isEmpty(userId) && TextUtils.isDigitsOnly(userId) && Long.parseLong(userId) == AccountServiceUtils.getBorqsAccountID()) {
        	otherActionList.add(newActionItem(context.getString(R.string.view_bpc_profile), ACTION_VIEW_PROFILE, userId, contactId));
        }else {
        	otherActionList.add(newActionItem(context.getString(R.string.request_conversion), ACTION_REQUEST_CONVERSATION, userId, contactId));
        	otherActionList.add(newActionItem(context.getString(R.string.add_to_circles), ACTION_ADD_TO_CIRCLES, userId, contactId));
        	otherActionList.add(newActionItem(context.getString(R.string.view_bpc_profile), ACTION_VIEW_PROFILE, userId, contactId));
        }
        return otherActionList;
    }

    public static ArrayList<ActionItem> generateInviteContactActionList(Context context, String contactId) {
        ArrayList<ActionItem> actionList = new ArrayList<ActionItem>();
        actionList.add(newActionItem(context.getString(R.string.add_to_circles), ACTION_INVITE_CONTACT, null, contactId));
        return actionList;
    }

    public static ActionItem newActionItem(String content, String type, String userId, String contactId) {
        ActionItem items = new ActionItem();
        items.setTitle(content);
        items.setType(type);
        if (StringUtil.isEmpty(userId) == false) {
            items.setUserId(userId);
        }
        if (StringUtil.isEmpty(contactId) == false) {
            items.setContactId(contactId);
        }
        return items;
    }
}
