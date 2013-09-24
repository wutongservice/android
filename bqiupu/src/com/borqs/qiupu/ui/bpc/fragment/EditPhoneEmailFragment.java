package com.borqs.qiupu.ui.bpc.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.borqs.qiupu.fragment.BasicFragment;
import twitter4j.QiupuSimpleUser;
import twitter4j.QiupuUser;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.listener.ContactInfoActionListner;
import com.borqs.common.view.DetailContactInfoItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.AddProfileInfoActivity;

public class EditPhoneEmailFragment extends BasicFragment implements ContactInfoActionListner{
	private static final String TAG = "EditPhoneEmailFragment";
	boolean ischangeRequest;
	private long mUserid;
	private BasicActivity myActivity;
	private DetailContactInfoItemView phone1;
	private DetailContactInfoItemView phone2;
	private DetailContactInfoItemView phone3;
	private DetailContactInfoItemView email1;
	private DetailContactInfoItemView email2;
	private DetailContactInfoItemView email3;
	private LinearLayout id_contact_info;
//	private View phone_email_span;
	private Button btn_add;
//	private ImageView add_more_phone_email;
	private int size = 0;
	private int currSize = 0;
	LayoutInflater flater;

	public interface onEditPhoneEmailFragmentListener {

		public QiupuUser getCopyUser();
		public QiupuUser getUser();
	}
	onEditPhoneEmailFragmentListener listener;
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (onEditPhoneEmailFragmentListener) activity;
			if(listener != null && listener.getUser() != null){
			    mUserid = listener.getUser().uid;
			} 
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement onEditPhoneEmailFragmentListener");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myActivity = (BasicActivity) getActivity();
		QiupuHelper.registerContactUpdateListener(getClass().getName(),this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		flater = inflater;
		View v = inflater.inflate(R.layout.edit_phone_email, container, false);
		id_contact_info = (LinearLayout) v.findViewById(R.id.id_contact_info);
//		phone_email_span = v.findViewById(R.id.phone_email_span);

//		add_more_phone_email = (ImageView) v.findViewById(R.id.add_phone_email);
//		add_more_phone_email.setOnClickListener(userDisplayClickListener);
		btn_add = (Button) v.findViewById(R.id.btn_add);
		btn_add.setOnClickListener(userDisplayClickListener);
		return v;
	}

	View.OnClickListener userDisplayClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			int i = v.getId();
			if (i == R.id.add_phone_email || i == R.id.btn_add) {
				myActivity.showDialog(BasicActivity.PROFILE_ADD_INFO);
			}
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initContactInfoUi();
	}

	private ArrayList<DetailContactInfoItemView> itemArr = new ArrayList<DetailContactInfoItemView>();
	private LinkedHashMap<String, String> contactitemmap = new LinkedHashMap<String, String>();

	private void initContactInfoUi() {
		contactitemmap.clear();
		itemArr.clear();

		myActivity.initContactInfoMap(contactitemmap);

		Iterator iter = contactitemmap.entrySet().iterator();
		id_contact_info.removeAllViews();
		if (contactitemmap.size() <= 0) {
//			phone_email_span.setVisibility(View.GONE);
		} else {
//			phone_email_span.setVisibility(View.VISIBLE);
			size = contactitemmap.size();
			currSize = 0;
			while (iter.hasNext()) {
				currSize++;
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				String val = (String) entry.getValue();
				DetailContactInfoItemView view;
				if (QiupuConfig.TYPE_PHONE1.equals(key)) {
					phone1 = new DetailContactInfoItemView(myActivity, val,
							true, ischangeRequest,
							myActivity.DISPLAY_PHONE_NUMBER1, mUserid);
					setContactItembackground(phone1);
					phone1.setOnClickListener(phoneClickListener);
					id_contact_info.addView(phone1);
					itemArr.add(phone1);
					if (contactitemmap.size() == 1) {
						getContactItemBottomView();
					}
				} else if (QiupuConfig.TYPE_PHONE2.equals(key)) {
					phone2 = new DetailContactInfoItemView(myActivity, val,
							true, ischangeRequest,
							BasicActivity.DISPLAY_PHONE_NUMBER2, mUserid);
					setContactItembackground(phone2);
					phone2.setOnClickListener(phoneClickListener);
					id_contact_info.addView(phone2);
					itemArr.add(phone2);
					if (contactitemmap.size() == 1) {
						getContactItemBottomView();
					}
				} else if (QiupuConfig.TYPE_PHONE3.equals(key)) {
					phone3 = new DetailContactInfoItemView(myActivity, val,
							true, ischangeRequest,
							BasicActivity.DISPLAY_PHONE_NUMBER3, mUserid);
					setContactItembackground(phone3);
					phone3.setOnClickListener(phoneClickListener);
					id_contact_info.addView(phone3);
					itemArr.add(phone3);
					if (contactitemmap.size() == 1) {
						getContactItemBottomView();
					}
				} else if (QiupuConfig.TYPE_EMAIL1.equals(key)) {
					if (contactitemmap.size() == 1) {
						getContactItemTopView();
					}
					email1 = new DetailContactInfoItemView(myActivity, val,
							false, ischangeRequest,
							BasicActivity.DISPLAY_EMAIL1, mUserid);
					setContactItembackground(email1);
					email1.setOnClickListener(emailClickListener);
					id_contact_info.addView(email1);
					itemArr.add(email1);
				} else if (QiupuConfig.TYPE_EMAIL2.equals(key)) {
					if (contactitemmap.size() == 1) {
						getContactItemTopView();
					}
					email2 = new DetailContactInfoItemView(myActivity, val,
							false, ischangeRequest,
							BasicActivity.DISPLAY_EMAIL2, mUserid);
					setContactItembackground(email2);
					email2.setOnClickListener(emailClickListener);
					id_contact_info.addView(email2);
					itemArr.add(email2);
				} else if (QiupuConfig.TYPE_EMAIL3.equals(key)) {
					if (contactitemmap.size() == 1) {
						getContactItemTopView();
					}
					email3 = new DetailContactInfoItemView(myActivity, val,
							false, ischangeRequest,
							BasicActivity.DISPLAY_EMAIL3, mUserid);
					setContactItembackground(email3);
					email3.setOnClickListener(emailClickListener);
					id_contact_info.addView(email3);
					itemArr.add(email3);
				}
			}
		}
	}

	private void setContactItembackground(DetailContactInfoItemView view) {
//		TypedArray sa = myActivity.obtainStyledAttributes(null,
//				R.styleable.ContactListItemView);
//		view.setBackgroundDrawable(sa.getDrawable(0));
		if(currSize==size) {
			view.setBackgroundResource(R.drawable.below);
		}else if(currSize<size && currSize > 1) {
			view.setBackgroundResource(R.drawable.middle);
		}else {
			view.setBackgroundResource(R.drawable.above);
		}
	}

	View.OnClickListener phoneClickListener = new OnClickListener() {
		public void onClick(View view) {
			if (DetailContactInfoItemView.class.isInstance(view)) {
				DetailContactInfoItemView item = (DetailContactInfoItemView) view;
				String text = item.getText();
				if (mUserid == AccountServiceUtils.getBorqsAccountID()) {
					onClickMyPhoneNumber(text, item.gettype(), item.getbind());
				}
			}
		}
	};

	private int getBindCount() {
		int count = 0;
		for (int i = 0; i < itemArr.size(); i++) {
			DetailContactInfoItemView item = itemArr.get(i);
			if (item.getbind()) {
				count++;
			}
		}
		return count;
	}

	private void getContactItemBottomView() {
		email1 = new DetailContactInfoItemView(myActivity, "", false,
				ischangeRequest, BasicActivity.DISPLAY_EMAIL1, mUserid);
		email1.setOnClickListener(emtyEmailClickListener);
		email1.setBackgroundResource(R.drawable.below);
		id_contact_info.addView(email1);
	}

	private void getContactItemTopView() {
		phone1 = new DetailContactInfoItemView(myActivity, "", true,
				ischangeRequest, BasicActivity.DISPLAY_PHONE_NUMBER1, mUserid);
		phone1.setOnClickListener(emtyPhoneClickListener);
		phone1.setBackgroundResource(R.drawable.above);
		id_contact_info.addView(phone1);
	}

	private void onClickMyPhoneNumber(String text, int itemType, boolean wasBind) {
		Intent intent = new Intent(myActivity, AddProfileInfoActivity.class);
		intent.putExtra(AddProfileInfoActivity.EDIT_VALUE, text);
		intent.putExtra(AddProfileInfoActivity.INFO_ITEM, itemType);
		intent.putExtra("bindcount", getBindCount());
		intent.putExtra(AddProfileInfoActivity.ACTION_TYPE,
				wasBind ? AddProfileInfoActivity.TYPE_BIND_PHONE
						: AddProfileInfoActivity.TYPE_ACTION_INFO_PHONE);
		startActivity(intent);
	}

	View.OnClickListener emtyEmailClickListener = new View.OnClickListener() {
		public void onClick(View arg0) {
			if (AccountServiceUtils.getBorqsAccountID() == mUserid) {
				onClickAddEmail();
			}

		}
	};

	View.OnClickListener emtyPhoneClickListener = new View.OnClickListener() {
		public void onClick(View arg0) {
			if (AccountServiceUtils.getBorqsAccountID() == mUserid) {
				onClickAddPhoneNumber();
			}

		}
	};

	private void onClickAddPhoneNumber() {
		Intent intent = new Intent(myActivity, AddProfileInfoActivity.class);
		intent.putExtra(AddProfileInfoActivity.ACTION_TYPE,
				AddProfileInfoActivity.TYPE_ADD_PHONE);
		startActivity(intent);
	}

	private void onClickAddEmail() {
		Intent intent = new Intent(myActivity, AddProfileInfoActivity.class);
		intent.putExtra(AddProfileInfoActivity.ACTION_TYPE,
				AddProfileInfoActivity.TYPE_ADD_EMAIL);
		startActivity(intent);
	}

	View.OnClickListener emailClickListener = new OnClickListener() {
		public void onClick(View view) {
			if (DetailContactInfoItemView.class.isInstance(view)) {
				DetailContactInfoItemView item = (DetailContactInfoItemView) view;
				String text = item.getText();
				if (mUserid == AccountServiceUtils.getBorqsAccountID()) {
					onClickMyEmail(text, item.gettype(), item.getbind());
				}
			}
		}
	};

	private void onClickMyEmail(String text, int itemType, boolean wasBind) {
		Intent intent = new Intent(myActivity, AddProfileInfoActivity.class);
		intent.putExtra(AddProfileInfoActivity.EDIT_VALUE, text);
		intent.putExtra(AddProfileInfoActivity.INFO_ITEM, itemType);
		intent.putExtra("bindcount", getBindCount());
		intent.putExtra(AddProfileInfoActivity.ACTION_TYPE,
				wasBind ? AddProfileInfoActivity.TYPE_BIND_EMAIL
						: AddProfileInfoActivity.TYPE_ACTION_INFO_EMAIL);
		startActivity(intent);
	}

	@Override
	public void updateContactInfoUi() {
		initContactInfoUi();
	}

	@Override
	public void onDestroy() {
		QiupuHelper.unregisterContactUpdateListener(getClass().getName());
		super.onDestroy();
	}
	
//	@Override
//	protected void on() {
//		QiupuHelper.unregisterContactUpdateListener(getClass().getName());
//		super.onDestroy();
//	}
}
