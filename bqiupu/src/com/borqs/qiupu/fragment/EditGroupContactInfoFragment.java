package com.borqs.qiupu.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.QiupuAccountInfo.PhoneEmailInfo;
import twitter4j.UserCircle;
import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.util.JSONUtil;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class EditGroupContactInfoFragment extends EditGroupBaseFragment {
	private final static String TAG = "EditGroupContactInfoFragment";
	
	private EditText mGroup_phone;
	private EditText mGroup_email;
	private HashMap<String, String> tmpMap = new HashMap<String, String>();
	
	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View contentView = inflater.inflate(R.layout.public_circle_contact_info, container, false);
		mGroup_phone = (EditText) contentView.findViewById(R.id.group_phone);
		mGroup_email = (EditText) contentView.findViewById(R.id.group_email);
		mGroup_phone.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					mGroup_phone.setInputType(InputType.TYPE_CLASS_NUMBER);
				}
			}
		});
		return contentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		initUI();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public interface EditPublicCircleFragmentCallBack {
		public void getEditPublicCircleFragment(EditGroupContactInfoFragment fragment);
		public UserCircle getCircleInfo() ;
	}

	private void initUI() {
		if(mCircle != null) {
		    if(mCircle.phoneList != null && mCircle.phoneList.size() > 0) {
		        String tmpPhone = mCircle.phoneList.get(0).info;
		        mGroup_phone.setText(tmpPhone);
		        mGroup_phone.selectAll();
		        tmpMap.put(QiupuConfig.TYPE_PHONE1, tmpPhone);
		    }
		    if(mCircle.emailList != null && mCircle.emailList.size() > 0) {
		        String tmpEmail = mCircle.emailList.get(0).info;
		        mGroup_email.setText(tmpEmail);
		        tmpMap.put(QiupuConfig.TYPE_EMAIL1, tmpEmail);
		    }
		}
	}
	
	@Override
	public HashMap<String, String> getEditGroupMap() {
	    return getBaseSetEditGroupMap();
	}
	
    private HashMap<String, String> getBaseSetEditGroupMap() {
		mCopyCircle = mCircle.clone();
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
	            PhoneEmailInfo phoneInfo = new PhoneEmailInfo();
	            phoneInfo.uid = mCircle.circleid;
	            phoneInfo.type = QiupuConfig.TYPE_PHONE1;
	            phoneInfo.info = phone;
	            mCopyCircle.phoneList.add(phoneInfo); 
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
		        PhoneEmailInfo emailInfo = new PhoneEmailInfo();
		        emailInfo.uid = mCircle.circleid;
		        emailInfo.type = QiupuConfig.TYPE_EMAIL1;
		        emailInfo.info = email;
		        mCopyCircle.emailList.add(emailInfo);
		    }
		}

		HashMap<String , String> editMap = new HashMap<String, String>();
		if(hasChange) {
		    String contactInfoJson = JSONUtil.createContactInfoJSONObject(contactInfoMap);
		    Log.d(TAG, "create contact Info json : " + contactInfoJson);
		    editMap.put("contact_info", contactInfoJson);
		}
		return editMap;
	}
    
    private boolean checkPhoneEmailValid(final String phone, final String email) {
//        if(!StringUtil.isValidString(phone) && !StringUtil.isValidString(email)) {
//            ToastUtil.showShortToast(mActivity, mhandler, "");
//            return false;
//        }
        
        if(StringUtil.isValidString(phone) 
        		&& phone.length() > 0 && !TextUtils.isDigitsOnly(phone) /* && !StringUtil.isValidMobileNumber(phone)*/) {
            mGroup_phone.requestFocus();
            ToastUtil.showShortToast(mActivity, mhandler, R.string.input_valid_phone);
            return false;
        }
        
        if(StringUtil.isValidString(email) && 
        		email.length() > 0 && !StringUtil.isValidEmail(email)) {
            mGroup_email.requestFocus();
            ToastUtil.showShortToast(mActivity, mhandler, R.string.input_valid_email);
            return false;
        }
        
        return true;
    }
}
