package com.borqs.qiupu.ui.bpc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.ContactUtils;

public class InviteContactActivity extends BasicActivity{
	private static final String TAG = "InviteContactActivity";
	private String[] mAllPhoneEmails;
	private String mContactName;
	private long mContactId = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Uri dataUri = getIntent().getData();
        if(dataUri != null) {
        	Cursor cursor = getContentResolver().query(dataUri, 
        			              new String[]{ContactsContract.Contacts._ID}, null, null, null);
        	if (cursor != null && cursor.getCount() > 0) {
        		try {
        			if (cursor.moveToNext()) {
        				mContactId = cursor.getLong(0);  
        			}
        		} finally {
                cursor.close();
                cursor = null;
        		}
        	}
        }else {
        	Bundle bundle = getIntent().getExtras();
        	mContactId = Long.parseLong(bundle.getString("contactId"));
        }
        Log.d(TAG, "rawContactId " + mContactId );
        HashMap<String, Integer> phoneAndEmail = ContactUtils.getPhoneAndEmails(this, mContactId);
        mContactName = ContactUtils.getContactName(this, mContactId);
        if(phoneAndEmail.size() > 0) {
        	if(phoneAndEmail.size() == 1) {
        		generatePhoneEmailItems(phoneAndEmail);
        		startUserCircleSelectedActivity(mAllPhoneEmails[0]);
        	}else {
        		generatePhoneEmailItems(phoneAndEmail);
        		DialogUtils.showSingleChoiceDialog(this, R.string.invite_contact_dialog_title, mAllPhoneEmails, 0,
        				itemClickListener, null, negativeListener);
        	}
        }else {
            IntentUtil.startPeopleSearchIntent(this, mContactName);
            finish();
        }
	}
	@Override
	protected void createHandler() {
		mHandler = new Handler();		
	}
	
	private void generatePhoneEmailItems(HashMap<String, Integer> map) {
		mAllPhoneEmails = new String[map.size()]; 
		int i = 0;
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			mAllPhoneEmails[i] = (String) entry.getKey();
			i++;
		}
	}
	
	DialogInterface.OnClickListener itemClickListener = new OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			startUserCircleSelectedActivity(mAllPhoneEmails[which]);
		}
	};
	
	DialogInterface.OnClickListener negativeListener = new OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			InviteContactActivity.this.finish();
		}
	};
	
	private void startUserCircleSelectedActivity(String content) {
		Intent intent = new Intent(this, UserCircleSelectedActivity.class);
		intent.putExtra("contactname", mContactName);
		if(TextUtils.isEmpty(content) == false) {
			intent.putExtra("content", content);
			intent.putExtra("contactId", mContactId);
		}
		startActivity(intent);
       finish();
	}
}
