package com.borqs.common.view;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.UserCircle;
import twitter4j.QiupuAccountInfo.PhoneEmailInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.borqs.common.util.DialogUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.AddProfileInfoActivity;
import com.borqs.qiupu.util.JSONUtil;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class AddContactInfoview extends SNSItemView {

    private static final String TAG = "AddContactInfoview";
    private Context mContext;
	private UserCircle mCircle;
//	private LinearLayout mContactView;
//	private Button mAddBtn;
	private EditText mGroup_phone;
	private EditText mGroup_email;
	private HashMap<String, String> tmpMap = new HashMap<String, String>();
	
    public AddContactInfoview(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }
    
    public AddContactInfoview(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

    public AddContactInfoview(Context context, UserCircle circle) {
        super(context);
        mContext = context;
        mCircle = circle;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        removeAllViews();
        LayoutInflater factory = LayoutInflater.from(mContext);
        final View contentView = factory.inflate(R.layout.add_contact_info_view, null);
        addView(contentView);
        
        mGroup_phone = (EditText) contentView.findViewById(R.id.group_phone);
        mGroup_email = (EditText) contentView.findViewById(R.id.group_email);
        
//        mContactView = (LinearLayout) contentView.findViewById(R.id.contact_info_ll);
//        mAddBtn = (Button) contentView.findViewById(R.id.btn_add);
//        mAddBtn.setOnClickListener(addContactInfoClickLisetner);

//        mGroup_phone = (EditText) mContactView.findViewById(R.id.group_phone);
//		mGroup_email = (EditText) mContactView.findViewById(R.id.group_email);
        setUI();
    }

    private void setUI() {
    	if(mCircle == null) {
//    		mContactView.setVisibility(View.GONE);
    	}else {
//    		mContactView.setVisibility(View.VISIBLE);
    		
    		if(mCircle.phoneList != null && mCircle.phoneList.size() > 0) {
    			String tmpPhone = mCircle.phoneList.get(0).info;
    			mGroup_phone.setText(tmpPhone);
    			tmpMap.put(QiupuConfig.TYPE_PHONE1, tmpPhone);
//    			for(int i=0; i<mCircle.phoneList.size(); i++) {
//    				mContactView.addView(getPhoneEmailEditText(tmpPhone, true));
//    				if(i == 0) {
//    					tmpMap.put(QiupuConfig.TYPE_PHONE1, tmpPhone);
//    				}else if(i == 1) {
//    					tmpMap.put(QiupuConfig.TYPE_PHONE2, tmpPhone);
//    				}else if(i == 3) {
//    					tmpMap.put(QiupuConfig.TYPE_PHONE3, tmpPhone);
//    				}
//    			}
		    }
		    if(mCircle.emailList != null && mCircle.emailList.size() > 0) {
		    	String tmpEmail = mCircle.emailList.get(0).info;
		    	mGroup_email.setText(tmpEmail);
		    	tmpMap.put(QiupuConfig.TYPE_EMAIL1, tmpEmail);
//		    	for(int i=0; i<mCircle.emailList.size(); i++) {
//    				mContactView.addView(getPhoneEmailEditText(tmpEmail, false));
//    				if(i == 0) {
//    					tmpMap.put(QiupuConfig.TYPE_EMAIL1, tmpEmail);
//    				}else if(i == 1) {
//    					tmpMap.put(QiupuConfig.TYPE_EMAIL2, tmpEmail);
//    				}else if(i == 3) {
//    					tmpMap.put(QiupuConfig.TYPE_EMAIL3, tmpEmail);
//    				}
//    			}
		    }
		    
//		    if(mCircle.phoneList != null && mCircle.phoneList.size() == 3
//		    		&& mCircle.emailList != null && mCircle.emailList.size() == 3) {
//		    	mAddBtn.setVisibility(View.GONE);
//		    }
    	}
    }
    
    public EditText getPhoneEmailEditText(String phoneMail, boolean isphone) {
    	EditText text = new EditText(mContext);
    	if(isphone) {
    		text.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.circle_phone_icon), null, null, null);
    	}else {
    		text.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.circle_email_icon), null, null, null);
    	}
    	text.setBackgroundDrawable(null);
    	text.setText(phoneMail);
    	return text;
    }
    
    public void setContent(UserCircle circle) {
    	if(circle != null) {
    		mCircle = circle;
    		setUI();
    	}
    }
    
    private AlertDialog mDialog ;
	
	
	View.OnClickListener addContactInfoClickLisetner = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String[] iteminfo = new String[]{getResources().getString(R.string.add_more_item_phone), getResources().getString(R.string.add_more_item_email)};
			DialogUtils.showItemsDialog(mContext, mContext.getString(R.string.add_info_title), -1, iteminfo, chooseItemClickLisener);
		}
	};
	
	
	private void dimissDialog() {
		if(mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}
	
	
	
	DialogInterface.OnClickListener chooseItemClickLisener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(which == 0) {
				
			}else if(which == 1) {
				
			}
			
		}
	};
	
	public HashMap<String, String> getBaseSetEditGroupMap(UserCircle mCopyCircle, HashMap<String, String> editMap) {
		final String phone = mGroup_phone.getText().toString().trim();
		final String email = mGroup_email.getText().toString().trim();
		
		if(checkPhoneEmailValid(phone, email) == false) {
		    return null;
		}
		
		HashMap<String, String> contactInfoMap = new HashMap<String, String>();
		
		boolean hasChange = false;
		if(StringUtil.isValidString(tmpMap.get(QiupuConfig.TYPE_PHONE1)) == false
		        && StringUtil.isValidString(phone) == false) {
		}else {
		    contactInfoMap.put(QiupuConfig.TYPE_PHONE1, phone);
		    if(!phone.equals(tmpMap.get(QiupuConfig.TYPE_PHONE1))) {
		        hasChange = true;
	            if(mCopyCircle.phoneList != null) {
	                mCopyCircle.phoneList.clear();
	            }else {
	                mCopyCircle.phoneList = new ArrayList<PhoneEmailInfo>();
	            }
	            if(StringUtil.isValidString(phone)) {
	            	PhoneEmailInfo phoneInfo = new PhoneEmailInfo();
	            	phoneInfo.uid = mCircle.circleid;
	            	phoneInfo.type = QiupuConfig.TYPE_PHONE1;
	            	phoneInfo.info = phone;
	            	mCopyCircle.phoneList.add(phoneInfo); 
	            }
	        }    
		}
		
		if(StringUtil.isValidString(tmpMap.get(QiupuConfig.TYPE_EMAIL1)) == false
                && StringUtil.isValidString(email) == false) {
		    
		}else {
		    contactInfoMap.put(QiupuConfig.TYPE_EMAIL1, email);
		    if(!email.equals(tmpMap.get(QiupuConfig.TYPE_EMAIL1))) {
		        hasChange = true;
		        if(mCopyCircle.emailList != null) {
		            mCopyCircle.emailList.clear();
		        }else {
		            mCopyCircle.emailList = new ArrayList<PhoneEmailInfo>();
		        }
		        if(StringUtil.isValidString(email)) {
		        	PhoneEmailInfo emailInfo = new PhoneEmailInfo();
		        	emailInfo.uid = mCircle.circleid;
		        	emailInfo.type = QiupuConfig.TYPE_EMAIL1;
		        	emailInfo.info = email;
		        	mCopyCircle.emailList.add(emailInfo);
		        }
		    }
		}

		if(hasChange) {
		    String contactInfoJson = JSONUtil.createContactInfoJSONObject(contactInfoMap);
		    Log.d(TAG, "create contact Info json : " + contactInfoJson);
		    editMap.put("contact_info", contactInfoJson);
		}
		return editMap;
	}
	
	private boolean checkPhoneEmailValid(final String phone, final String email) {
      if(StringUtil.isValidString(phone) 
      		&& phone.length() > 0 && !TextUtils.isDigitsOnly(phone) /* && !StringUtil.isValidMobileNumber(phone)*/) {
          mGroup_phone.requestFocus();
          ToastUtil.showShortToast(mContext, new Handler(), R.string.input_valid_phone);
          return false;
      }
      
      if(StringUtil.isValidString(email) && 
      		email.length() > 0 && !StringUtil.isValidEmail(email)) {
          mGroup_email.requestFocus();
          ToastUtil.showShortToast(mContext, new Handler(), R.string.input_valid_email);
          return false;
      }
      
      return true;
  }
}

