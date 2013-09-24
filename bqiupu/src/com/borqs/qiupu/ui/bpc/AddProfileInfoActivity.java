package com.borqs.qiupu.ui.bpc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import twitter4j.QiupuAccountInfo.PhoneEmailInfo;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.listener.ContactInfoActionListner;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.UserProvider;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.ErrorUtil;
import com.borqs.qiupu.util.JSONUtil;
import com.borqs.qiupu.util.StringUtil;

public class AddProfileInfoActivity extends BasicActivity
{
	private static final String TAG = "Qiupu.AddProfileInfoActivity";
	private Handler mhandler;
	private EditText editvalue;
	private Button addbtn;
	private Button delete_bind;
	private Button resend_verify;
	private Button delete_phone_email;
	private Button update_info;
		
	private TextView summary_tv;
	public static final String ACTION_TYPE = "action_type"; 
	public static final String EDIT_VALUE = "edit_value"; 
	public static final String INFO_ITEM = "info_item";
	public static final int TYPE_ADD_PHONE = 1;
	public static final int TYPE_ADD_EMAIL = 2;
	public static final int TYPE_BIND_PHONE = 3;
	public static final int TYPE_BIND_EMAIL = 4;
	public static final int TYPE_ACTION_INFO_PHONE = 5;
	public static final int TYPE_ACTION_INFO_EMAIL = 6;
	private int mtype;
	private int itemtype;
	private String intentValue;
//	private QiupuUser myinfo;
	private boolean isdelete;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_more_info_view);
		enableLeftActionBtn(false);
		enableMiddleActionBtn(false);
        enableRightActionBtn(false);
		addbtn     = (Button) findViewById(R.id.add_btn);
		editvalue  = (EditText) findViewById(R.id.edit_value);
		summary_tv = (TextView) findViewById(R.id.summary_tv);
		delete_bind = (Button) findViewById(R.id.delete_bind);
		resend_verify = (Button) findViewById(R.id.resend_verify);
		delete_phone_email = (Button) findViewById(R.id.delete_phone_email);
		update_info = (Button) findViewById(R.id.update_info);
		//get my info from DB
//		myinfo = orm.queryOneUserInfo(AccountServiceUtils.getBorqsAccountID());
//		if(myinfo == null )
//		{
//			Log.d(TAG, "why there have no my info in DB");
//		}
		processIntent();
		
		addbtn.setOnClickListener(bindBtnClickListener);
		delete_bind.setOnClickListener(this);
		resend_verify.setOnClickListener(resendVerifyClickListener);
		delete_phone_email.setOnClickListener(deleteInfoClickLisetener);
		update_info.setOnClickListener(this);
	}
	
	private void processIntent()
	{
		Intent intent = getIntent();
		mtype = intent.getIntExtra(ACTION_TYPE, -1);
		if(mtype == TYPE_ADD_EMAIL)
		{
			setHeadTitle(getString(R.string.add_email_title));
			summary_tv.setText(R.string.add_email_summary);
			editvalue.setHint(R.string.add_email_edit_hint);
			addbtn.setText(R.string.add_info_email);
		}
		else if(mtype == TYPE_ADD_PHONE)
		{
			setHeadTitle(getString(R.string.add_phone_title));
			summary_tv.setText(R.string.add_phone_summary);
			editvalue.setHint(R.string.add_phone_edit_hint);
			editvalue.setInputType(InputType.TYPE_CLASS_NUMBER);
			addbtn.setText(R.string.add_info_phone);
		}
		else if(mtype == TYPE_BIND_PHONE)
		{
			setHeadTitle(getString(R.string.add_phone_title));
			summary_tv.setText(R.string.add_info_bind_phone_summary);
			editvalue.setText(intent.getStringExtra(EDIT_VALUE));
			editvalue.setEnabled(false);
			itemtype = intent.getIntExtra(INFO_ITEM, -1);
			addbtn.setVisibility(View.GONE);
			delete_bind.setVisibility(View.VISIBLE);
		}
		else if(mtype == TYPE_BIND_EMAIL)
		{
			setHeadTitle(getString(R.string.add_email_title));
			summary_tv.setText(R.string.add_info_bind_email_summary);
			editvalue.setText(intent.getStringExtra(EDIT_VALUE));
			editvalue.setEnabled(false);
			itemtype = intent.getIntExtra(INFO_ITEM, -1);
			addbtn.setVisibility(View.GONE);
			delete_bind.setVisibility(View.VISIBLE);
		}
		else if(mtype == TYPE_ACTION_INFO_PHONE)
		{
			intentValue = intent.getStringExtra(EDIT_VALUE);
			setHeadTitle(getString(R.string.add_phone_title));
			summary_tv.setText(R.string.add_phone_summary);
			
			editvalue.setText(intentValue);
			editvalue.setHint(R.string.add_phone_edit_hint);
			editvalue.setInputType(InputType.TYPE_CLASS_NUMBER);
			editvalue.setSelection(intentValue.length());
			editvalue.setEnabled(false);
			
			itemtype = intent.getIntExtra(INFO_ITEM, -1);
			addbtn.setVisibility(View.GONE);
			resend_verify.setVisibility(View.VISIBLE);
			resend_verify.setText(R.string.resend_message_verify);
			delete_phone_email.setVisibility(View.VISIBLE);
			delete_phone_email.setText(R.string.delete_info_phone);
			update_info.setVisibility(View.VISIBLE);
			update_info.setText(R.string.update_info_phone);
		}
		else if(mtype == TYPE_ACTION_INFO_EMAIL)
		{
			intentValue = intent.getStringExtra(EDIT_VALUE);
			setHeadTitle(getString(R.string.add_email_title));
			summary_tv.setText(R.string.add_email_summary);
			
			editvalue.setText(intentValue);
			editvalue.setHint(R.string.add_email_edit_hint);
			editvalue.setSelection(intentValue.length());
			editvalue.setEnabled(false);
			
			itemtype = intent.getIntExtra(INFO_ITEM, -1);
			addbtn.setVisibility(View.GONE);
			resend_verify.setVisibility(View.VISIBLE);
			resend_verify.setText(R.string.resend_email_verify);
			delete_phone_email.setVisibility(View.VISIBLE);
			delete_phone_email.setText(R.string.delete_info_email);
			update_info.setVisibility(View.VISIBLE);
			update_info.setText(R.string.update_info_email);
		}
		else
		{
			Log.d(TAG, "AddProfileInfoActivity : have no type " + mtype);
		}
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	protected void createHandler()
	{
		mhandler = new MainHandler();
	}
	
	@Override
	public void onClick(View view)
	{
        int i = view.getId();
        if (i == R.id.delete_bind) {
            int bindcount = getIntent().getIntExtra("bindcount", -1);
            if (bindcount <= 1) {
                Log.d(TAG, "it is the last bind, do not delete");
                Toast.makeText(AddProfileInfoActivity.this, getString(R.string.not_delete_last_bind), Toast.LENGTH_SHORT).show();
            } else {
                showdeleteDialog();
            }
        } else if (i == R.id.update_info) {
            editvalue.setEnabled(true);
            update_info.setVisibility(View.GONE);
            delete_phone_email.setOnClickListener(updateInfoCancelClickLisetener);
            delete_phone_email.setText(R.string.label_cancel);
            if (mtype == TYPE_ACTION_INFO_PHONE) {
                update_info.setVisibility(View.VISIBLE);
                update_info.setOnClickListener(updateInfoOkClickLisetener);
                update_info.setText(getString(R.string.update_contact_info_btn));
            } else {
                resend_verify.setVisibility(View.VISIBLE);
                resend_verify.setOnClickListener(updateInfoOkClickLisetener);
                resend_verify.setText(getString(R.string.update_contact_info_btn));
            }
        } else {
        }
	}
	
	private final static int GOTO_BIND        = 1;	
	private final static int GOTO_BIND_END    = 2;	
	
	private class MainHandler extends Handler{
	    public void handleMessage(Message msg) {
	    	switch(msg.what) {
		    	case GOTO_BIND:
		    	{
		    		if(mtype == TYPE_ADD_EMAIL || mtype == TYPE_ACTION_INFO_EMAIL)
		    		{
		    			gotoBind("email", msg.getData().getString("value"));
		    		}
		    		else if(mtype == TYPE_ADD_PHONE || mtype == TYPE_ACTION_INFO_PHONE)
		    		{
		    			gotoBind("phone", msg.getData().getString("value"));
		    		}
		    		break;
		    	}
		    	case GOTO_BIND_END:
		    	{
		    		try {
		    			dismissDialog(ADD_INFO_PROCESS);
					} catch (Exception e) {
						Log.d(TAG, "dimiss mDenyProgress exception");
					}
		    		if(msg.getData().getBoolean(RESULT))
		    		{
		    			String textValue = msg.getData().getString("value");
		    			if(mtype == TYPE_ADD_PHONE)
		    			{
		    				parseUserContactPhone(textValue);
		    				showTutrialDialog(getString(R.string.add_phone_suc));
		    			}
		    			else if(mtype == TYPE_ADD_EMAIL)
		    			{
		    				parseUserContactEmail(textValue);
		    				showTutrialDialog(getString(R.string.add_email_suc));
		    			}
		    			else
		    			{
		    				Log.d(TAG, "no need do something");
		    			}
		    		}
		    		else
		    		{
		    			int errorCode = msg.getData().getInt(ErrorUtil.ERROR_CODE);
		    			String errorMsg = ErrorUtil.getBindErrorMessage(errorCode, AddProfileInfoActivity.this);
		    			if(errorMsg == null) {
		    				errorMsg = msg.getData().getString(ErrorUtil.ERROR_MSG);
		    			}
		    			Toast.makeText(AddProfileInfoActivity.this,errorMsg, Toast.LENGTH_SHORT).show();
		    		}
		    		break;
		    	}
	    	}
	    }
    }
	
	private void showTutrialDialog(String message)
	{
		AlertDialog dialog = new AlertDialog.Builder(AddProfileInfoActivity.this)
		.setTitle(message)
		.setPositiveButton(getString(R.string.label_ok),
		        new DialogInterface.OnClickListener() 
		        {
		            public void onClick(DialogInterface dialog, int whichButton) 
		            {
		            	updateActivityUI();
		                AddProfileInfoActivity.this.finish();
		            }
		        }).create();
		
		dialog.show();
	}
	
	private void gotoBind(final String type, final String value) 
	{
    	if(!AccountServiceUtils.isAccountReady()) {
    		Log.d(TAG, "gotoBind, mAccount is null exit");
    		return;
    	}
    	showDialog(ADD_INFO_PROCESS);
		asyncQiupu.gotoBind(AccountServiceUtils.getSessionID(), type, value, AccountServiceUtils.getBorqsAccountID(), new TwitterAdapter()
		{
			public void gotoBind(boolean flag) {
			    Log.d(TAG,"finish gotoBind= "+flag);
			    
			    Message mds = mhandler.obtainMessage(GOTO_BIND_END);
    			mds.getData().putBoolean("RESULT",      flag);
    			mds.getData().putString("value", value);
    			mhandler.sendMessage(mds);
			}
			
			public void onException(TwitterException ex,TwitterMethod method) 
			{
                TwitterExceptionUtils.printException(TAG, "gotoBind, server exception:", ex, method);

		    	Message mds = mhandler.obtainMessage(GOTO_BIND_END);
    			mds.getData().putBoolean(RESULT,      false);
    			mds.getData().putInt(ErrorUtil.ERROR_CODE, ex.getStatusCode());
    			mds.getData().putString(ErrorUtil.ERROR_MSG,ex.getMessage());
    			mhandler.sendMessage(mds);	
			}
		});   
	}
	
	View.OnClickListener bindBtnClickListener = new View.OnClickListener()
	{
		public void onClick(View arg0)
		{
			String editcontent = editvalue.getText().toString().trim();
			if(mtype == TYPE_ADD_EMAIL)
			{
				if(StringUtil.isValidEmail(editcontent))
				{
					Message msg = mhandler.obtainMessage(GOTO_BIND);
					msg.getData().putString("value", editcontent);
					msg.sendToTarget();
				}
				else
				{
					Toast.makeText(AddProfileInfoActivity.this, getString(R.string.input_valid_email), Toast.LENGTH_SHORT).show();
				}
			}
			else if(mtype == TYPE_ADD_PHONE)
			{
				if(StringUtil.isValidMobileNumber(editcontent))
				{
					Message msg = mhandler.obtainMessage(GOTO_BIND);
					msg.getData().putString("value", editcontent);
					msg.sendToTarget();
//					updateContactInfoAfterAddPhone(editcontent);
				}
				else
				{
					Toast.makeText(AddProfileInfoActivity.this, getString(R.string.input_valid_phone), Toast.LENGTH_SHORT).show();
				}
			}
		}
	};
	
//	private void parseUserContactEmail(QiupuUser info, String value)
//	{
//		if(!StringUtil.isValidString(info.contact_email1))
//		{
//			info.contact_email1 = value;
//		}
//		else if(!StringUtil.isValidString(info.contact_email2))
//		{
//			info.contact_email2 = value;
//		}
//		else if(!StringUtil.isValidString(info.contact_email3))
//		{
//			info.contact_email3 = value;
//		}
//	}
	
//	private void parseUserContactPhone(QiupuUser info, String value)
//	{
//		if(!StringUtil.isValidString(info.contact_phone1))
//		{
//			info.contact_phone1 = value;
////			info.login_phone1 = value;
//		}
//		else if(!StringUtil.isValidString(info.contact_phone2))
//		{
//			info.contact_phone2 = value;
////			info.login_phone2 = value;//TODO
//		}
//		else if(!StringUtil.isValidString(info.contact_phone3))
//		{
//			info.contact_phone3 = value;
////			info.login_phone3 = value; //TODO
//		}
//	}
	
	private void parseUserContactPhone(String value)
    {
	    long uid = AccountServiceUtils.getBorqsAccountID();
	    PhoneEmailInfo info = new PhoneEmailInfo();
	    info.uid = uid;
	    info.info = value;
	    
	    if(orm.getInfoWhitType(QiupuConfig.TYPE_PHONE1, uid) == null) {
	        info.type = QiupuConfig.TYPE_PHONE1;
	        orm.insertPhoneEmailInfo(info, UserProvider.TYPE_PHONE);
	    }else if (orm.getInfoWhitType(QiupuConfig.TYPE_PHONE2, uid) == null){
	        info.type = QiupuConfig.TYPE_PHONE2;
	        orm.insertPhoneEmailInfo(info, UserProvider.TYPE_PHONE);
	    }else if (orm.getInfoWhitType(QiupuConfig.TYPE_PHONE3, uid) == null){
	        info.type = QiupuConfig.TYPE_PHONE3;
	        orm.insertPhoneEmailInfo(info, UserProvider.TYPE_PHONE);
	    }
    }
	
	private void parseUserContactEmail(String value)
    {
        long uid = AccountServiceUtils.getBorqsAccountID();
        PhoneEmailInfo info = new PhoneEmailInfo();
        info.uid = uid;
        info.info = value;
        
        if (orm.getInfoWhitType(QiupuConfig.TYPE_EMAIL1, uid) == null){
            info.type = QiupuConfig.TYPE_EMAIL1;
            orm.insertPhoneEmailInfo(info, UserProvider.TYPE_EMAIL);
        }else if (orm.getInfoWhitType(QiupuConfig.TYPE_EMAIL2, uid) == null){
            info.type = QiupuConfig.TYPE_EMAIL2;
            orm.insertPhoneEmailInfo(info, UserProvider.TYPE_EMAIL);
        }else if (orm.getInfoWhitType(QiupuConfig.TYPE_EMAIL3, uid) == null){
            info.type = QiupuConfig.TYPE_EMAIL3;
            orm.insertPhoneEmailInfo(info, UserProvider.TYPE_EMAIL);
        }
    }
	
	 
	
	private HashMap<String, String> organizationContactMap(String col, String val)
	{
		HashMap<String, String> contactInfoMap = new HashMap<String, String>();
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		initContactInfoMap(map);
		contactInfoMap.put(col, val);
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			if(!key.equals(col))
			{
				if(value.length() > 0)
				{
					contactInfoMap.put(key, value);
				}
			}
		}
		return contactInfoMap;
	}
	
	private String getContactItem()
	{
		if(DISPLAY_PHONE_NUMBER1 == itemtype)
		{
			return QiupuConfig.TYPE_PHONE1;
		}
		else if(DISPLAY_PHONE_NUMBER2 == itemtype)
		{
			return QiupuConfig.TYPE_PHONE2;
		}
		else if(DISPLAY_PHONE_NUMBER3 == itemtype)
		{
			return QiupuConfig.TYPE_PHONE3;
		}
		else if(DISPLAY_EMAIL1 == itemtype)
		{
			return QiupuConfig.TYPE_EMAIL1;
		}
		else if(DISPLAY_EMAIL2 == itemtype)
		{
			return QiupuConfig.TYPE_EMAIL2;
		}
		else if(DISPLAY_EMAIL3 == itemtype)
		{
			return QiupuConfig.TYPE_EMAIL3;
		}
		else 
		{
			return "";
		}
	}
	
	View.OnClickListener resendVerifyClickListener = new View.OnClickListener()
	{
		public void onClick(View arg0)
		{
			String editcontent = editvalue.getText().toString().trim();
			if(StringUtil.isValidString(editcontent))
			{
				Message msg = mhandler.obtainMessage(GOTO_BIND);
				msg.getData().putString("value", editcontent);
				msg.sendToTarget();
			}
			else
			{
				Log.d(TAG, "the edit value is should not be empty");
			}
		}
	};
	
	View.OnClickListener updateInfoOkClickLisetener = new View.OnClickListener()
	{
		public void onClick(View arg0)
		{
			String textValue = editvalue.getText().toString().trim();
			if(textValue.equals(intentValue))
			{
				Log.d(TAG, "have no change info ");
				Toast.makeText(AddProfileInfoActivity.this, getString(R.string.have_no_change), Toast.LENGTH_SHORT).show();
			}
			else
			{
				if(mtype == TYPE_ACTION_INFO_PHONE)
				{
					if(StringUtil.isValidMobileNumber(textValue))
					{
						updateUserContactInfo(textValue);
					}
					else
					{
						Toast.makeText(AddProfileInfoActivity.this, getString(R.string.input_valid_phone), Toast.LENGTH_SHORT).show();
					}
				}
				else if(mtype == TYPE_ACTION_INFO_EMAIL)
				{
					if(StringUtil.isValidEmail(textValue))
					{
						updateUserContactInfo(textValue);
					}
					else
					{
						Toast.makeText(AddProfileInfoActivity.this, getString(R.string.input_valid_email), Toast.LENGTH_SHORT).show();
					}
				}
				
			}
		}
	};
	
	View.OnClickListener updateInfoCancelClickLisetener = new View.OnClickListener()
	{
		public void onClick(View arg0)
		{
			editvalue.setText(intentValue);
			editvalue.setEnabled(false);
			update_info.setVisibility(View.VISIBLE);
			resend_verify.setOnClickListener(resendVerifyClickListener);
			delete_phone_email.setOnClickListener(deleteInfoClickLisetener);
			if(mtype == TYPE_ACTION_INFO_PHONE)
			{
				update_info.setOnClickListener(AddProfileInfoActivity.this);
				update_info.setText(R.string.update_info_phone);
				delete_phone_email.setText(R.string.delete_info_phone);
			}
			else if(mtype == TYPE_ACTION_INFO_EMAIL)
			{
				delete_phone_email.setText(R.string.delete_info_email);
				resend_verify.setText(R.string.resend_email_verify);
			}
			else if(mtype == TYPE_ADD_PHONE)
			{
				resend_verify.setVisibility(View.GONE);
			}
		}
	};
	
	View.OnClickListener deleteInfoClickLisetener = new View.OnClickListener()
	{
		public void onClick(View arg0)
		{
			showdeleteDialog();
		}
	};
	
	private void showdeleteDialog()
	{
		String message = "";
		if(mtype == TYPE_BIND_PHONE || mtype == TYPE_ACTION_INFO_PHONE)
		{
			message = getString(R.string.delete_phone_message);
		}
		else if(mtype == TYPE_BIND_EMAIL || mtype == TYPE_ACTION_INFO_EMAIL)
		{
			message = getString(R.string.delete_email_message);
		}
			
		AlertDialog dialog = new AlertDialog.Builder(AddProfileInfoActivity.this)
		.setTitle(message)
		.setPositiveButton(getString(R.string.label_ok),
		        new DialogInterface.OnClickListener() 
		        {
		            public void onClick(DialogInterface dialog, int whichButton) 
		            {
		            	updateUserContactInfo("");
		            }
		        }).setNegativeButton(R.string.label_cancel,
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
					}
				}).create();
		
		dialog.show();
	}
	
	private void updateUserContactInfo(String value)
	{
		if(value.length() <= 0)
			isdelete = true;
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> contactInfoMap = organizationContactMap(getContactItem(), value);
		String contactInfoJson = JSONUtil.createContactInfoJSONObject(contactInfoMap);
		Log.d(TAG, "create contact Info json : " + contactInfoJson);
		map.put("contact_info", contactInfoJson);
		updateUserInfo(map);
	}
	
	@Override
	protected void doUpdateUserInfoEndCallBack(boolean suc)
	{
		String content = editvalue.getText().toString().trim();
		if(isdelete)
		{
//			content = "";
			content = null;
		}
		
		if(itemtype == DISPLAY_PHONE_NUMBER1)
		{
			if(isdelete) {
				orm.deletePhoneEmailInfo(AccountServiceUtils.getBorqsAccountID(), QiupuConfig.TYPE_PHONE1);
			}else {
				orm.updatePhoneEmailInfo(AccountServiceUtils.getBorqsAccountID(), QiupuConfig.TYPE_PHONE1, content);
			}
//			myinfo.contact_phone1 = content;
		}
		else if(itemtype == DISPLAY_PHONE_NUMBER2)
		{
			if(isdelete) {
				orm.deletePhoneEmailInfo(AccountServiceUtils.getBorqsAccountID(), QiupuConfig.TYPE_PHONE2);
			}else {
				orm.updatePhoneEmailInfo(AccountServiceUtils.getBorqsAccountID(), QiupuConfig.TYPE_PHONE2, content);
			}
//			myinfo.contact_phone2 = content;
		}
		else if(itemtype == DISPLAY_PHONE_NUMBER3)
		{
			if(isdelete) {
				orm.deletePhoneEmailInfo(AccountServiceUtils.getBorqsAccountID(), QiupuConfig.TYPE_PHONE3);
			}else {
				orm.updatePhoneEmailInfo(AccountServiceUtils.getBorqsAccountID(), QiupuConfig.TYPE_PHONE3, content);
			}
//			myinfo.contact_phone3 = content;
		}
		else if(itemtype == DISPLAY_EMAIL1)
		{
			if(isdelete) {
				orm.deletePhoneEmailInfo(AccountServiceUtils.getBorqsAccountID(), QiupuConfig.TYPE_EMAIL1);
			}else {
				orm.updatePhoneEmailInfo(AccountServiceUtils.getBorqsAccountID(), QiupuConfig.TYPE_EMAIL1, content);
			}
//			myinfo.contact_email1 = content;
		}
		else if(itemtype == DISPLAY_EMAIL2)
		{
			if(isdelete) {
				orm.deletePhoneEmailInfo(AccountServiceUtils.getBorqsAccountID(), QiupuConfig.TYPE_EMAIL2);
			}else {
				orm.updatePhoneEmailInfo(AccountServiceUtils.getBorqsAccountID(), QiupuConfig.TYPE_EMAIL2, content);
			}
//			myinfo.contact_email2 = content;
		}
		else if(itemtype == DISPLAY_EMAIL3)
		{
			if(isdelete) {
				orm.deletePhoneEmailInfo(AccountServiceUtils.getBorqsAccountID(), QiupuConfig.TYPE_EMAIL3);
			}else {
				orm.updatePhoneEmailInfo(AccountServiceUtils.getBorqsAccountID(), QiupuConfig.TYPE_EMAIL3, content);
			}
//			myinfo.contact_email3 = content;
		}
		updateActivityUI();
		if(TextUtils.isEmpty(content))
		{
			finish();
		}
		else
		{
			intentValue = content;
//			editvalue.setEnabled(false);
//			update_info.setVisibility(View.VISIBLE);
//			delete_phone_email.setOnClickListener(deleteInfoClickLisetener);
//			if(mtype == TYPE_ACTION_INFO_PHONE)
//			{
//				delete_phone_email.setText(R.string.delete_info_phone);
//			}
//			else if(mtype == TYPE_ACTION_INFO_EMAIL)
//			{
//				resend_verify.setText(R.string.resend_email_verify);
//				resend_verify.setOnClickListener(resendVerifyClickListener);
//				delete_phone_email.setText(R.string.delete_info_email);
//			}
			
			Toast.makeText(this, getString(R.string.edit_profile_update_suc), Toast.LENGTH_SHORT).show();
//			if(mtype == TYPE_ADD_PHONE)
//			{
				finish();
//			}
		}
	}
	
	private void updateActivityUI(){

		synchronized(QiupuHelper.contactinfolisteners)
        {
            Set<String> set = QiupuHelper.contactinfolisteners.keySet();
            Iterator<String> it = set.iterator();
            while(it.hasNext())
            {
                String key = it.next();
                ContactInfoActionListner listener = QiupuHelper.contactinfolisteners.get(key).get();
                if(listener != null)
                {
                    listener.updateContactInfoUi();
                }
            }      
        }      
    }
	
//	private void updateContactInfoAfterAddPhone(String  value)
//	{
//		if(QiupuConfig.LOGD) Log.d(TAG, "value : " + value);
//		if(!StringUtil.isValidString(myinfo.contact_phone1))
//		{
//			myinfo.contact_phone1 = value;
//		}
//		else if(!StringUtil.isValidString(myinfo.contact_phone2))
//		{
//			myinfo.contact_phone2 = value;
//		}
//		else if(!StringUtil.isValidString(myinfo.contact_phone3))
//		{
//			myinfo.contact_phone3 = value;
//		}
//		
//		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
//		HashMap<String, String> infomap = new HashMap<String, String>();
//		initContactInfoMap(map, myinfo);
//		String contactInfoJson = JSONUtil.createContactInfoJSONObject(map);
//		Log.d(TAG, "create contact Info json : " + contactInfoJson);
//		infomap.put("contact_info", contactInfoJson);
//		updateUserInfo(infomap);
//	}
}
