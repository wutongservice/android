package com.borqs.qiupu.ui.bpc.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.borqs.qiupu.fragment.BasicFragment;
import twitter4j.QiupuUser;
import twitter4j.QiupuAccountInfo.Address.AddressInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.borqs.common.util.DialogUtils;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.EditProfilesActivity;
import com.borqs.qiupu.util.JSONUtil;
import com.borqs.qiupu.util.StringUtil;

public class EditProfilesBaseInfoFragment extends BasicFragment {
	private static final String TAG = "EditProfilesBaseInfoFragment";
	private EditProfilesActivity mActivity;
	private CharSequence[] dialogitem;
	private int mSingleChoiceID = 0;
	public int displayType = -1;
	private QiupuUser mUser;
	private QiupuUser mCopyUser;
	private View layout_username;
	private View layout_address;
	private View layout_company;
	private View layout_department;
	private View layout_job_title;
	private View layout_office_address;
	private TextView detail_name;
	private View layout_gender;
	private View layout_birthday;
	private TextView tv_gender;
	private TextView detail_address;
	private TextView detail_birthday;
//	private RadioButton sex_m;
//	private RadioButton sex_f;
	private Button btn_save;
	private TextView et_office_address;
	private TextView detail_company;
	private TextView detail_department;
	private TextView detail_job_title;
	onCallBackListener listener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dialogitem = getResources().getTextArray(R.array.birthday_dialog_pick);
		mActivity = (EditProfilesActivity) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.edit_profile_base, container, false);
		layout_username = v.findViewById(R.id.layout_username);
		layout_address = v.findViewById(R.id.layout_address);
		layout_company = v.findViewById(R.id.layout_company);
		layout_department = v.findViewById(R.id.layout_department);
		layout_job_title = v.findViewById(R.id.layout_job_title);
		layout_office_address = v.findViewById(R.id.layout_office_address);
		detail_name = (TextView) v.findViewById(R.id.profile_username);
		layout_gender = v.findViewById(R.id.layout_gender);
		layout_birthday = v.findViewById(R.id.layout_birthday);
		tv_gender = (TextView) v.findViewById(R.id.tv_gender);
		layout_gender.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showGenderDialog();
				
			}
		});
		detail_address = (TextView) v.findViewById(R.id.edit_address);
		detail_birthday = (TextView) v.findViewById(R.id.edit_birthday);
//		sex_m = (RadioButton) v.findViewById(R.id.sex_man);
//		sex_f = (RadioButton) v.findViewById(R.id.sex_woman);
		et_office_address = (TextView) v.findViewById(R.id.et_office_address);
		detail_company = (TextView) v.findViewById(R.id.edit_company);
		detail_department = (TextView) v.findViewById(R.id.edit_department);
		detail_job_title = (TextView) v.findViewById(R.id.edit_job);
		btn_save = (Button) v.findViewById(R.id.btn_save);
		layout_username.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showUpdateStatusDialog("修改姓名",detail_name,true);
				
			}
		});
		layout_address.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showUpdateStatusDialog(getString(R.string.update_contact_info_btn)
						+getString(R.string.update_profile_address_hint),detail_address,false);
				
			}
		});
		layout_company.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showUpdateStatusDialog(getString(R.string.update_contact_info_btn)
						+getString(R.string.update_profile_company_hint),detail_company,false);
				
			}
		});
		layout_department.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showUpdateStatusDialog(getString(R.string.update_contact_info_btn)
						+getString(R.string.update_profile_department_hint),detail_department,false);
				
			}
		});
		layout_job_title.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showUpdateStatusDialog(getString(R.string.update_contact_info_btn)
						+getString(R.string.update_profile_job_hint),detail_job_title,true);
				
			}
		});
		layout_office_address.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showUpdateStatusDialog(getString(R.string.update_contact_info_btn)
						+getString(R.string.update_profile_address_hint),et_office_address,false);
				
			}
		});
		btn_save.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				HashMap<String, String> map = new HashMap<String, String>();
				if(QiupuORM.isOpenNewPlatformSettings(mActivity)) {
				    createEditBaseMap(map);
				}else {
				    if(!createEditMap(map)) {
				        map = null;
				        mActivity.showCustomToast(R.string.have_no_change);
				        return;
				    }
				}
				listener.updateBaseInfo(map);
			}
		});
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (onCallBackListener) activity;
			listener.callback(this);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement onCallBackListener");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mUser = mActivity.getmUser();
		mCopyUser = mActivity.getmCopyUser();
		refreshProfileUI();
		layout_birthday.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (detail_birthday.getText().toString().trim().length() > 0) {
					showBirthdayDialog();
				} else {
					showPickData();
				}
			}
		});

	}

	private void refreshProfileUI() {
		et_office_address.setText(mCopyUser.office_address);
		detail_company.setText(mCopyUser.company);
		detail_department.setText(mCopyUser.department);
		detail_job_title.setText(mCopyUser.jobtitle);
		detail_name.setText(mCopyUser.nick_name);
		detail_address.setText(mCopyUser.location);
		detail_birthday.setText(mCopyUser.date_of_birth);
//		if (("m").equals(mUser.gender)) {
//			sex_m.setChecked(true);
//		} else if (("f").equals(mUser.gender)) {
//			sex_f.setChecked(true);
//		} else {
//			Log.d(TAG, "don't know this user's gender !");
//		}
		if (("m").equals(mCopyUser.gender)) {
			tv_gender.setText(R.string.user_sex_man);
		} else if (("f").equals(mCopyUser.gender)) {
			tv_gender.setText(R.string.user_sex_woman);
		} else {
			Log.d(TAG, "don't know this user's gender !");
		}
	}

	private void showPickData() {
		Calendar calendar = Calendar.getInstance();
		new DatePickerDialog(
				getActivity(),
				new DatePickerDialog.OnDateSetListener() {
					public void onDateSet(DatePicker view, int year,
							int monthofYear, int dayofMonth) {
						displayType = BasicActivity.DISPLAY_BIRTHDAY;
						Date dt = new Date(year - 1900, monthofYear, dayofMonth);
						long birthday = dt.getTime();
						detail_birthday.setText(formatbirthday(birthday));
					}
				}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH)).show();
	}

	private String formatbirthday(long time) {
		Date date = new Date(Long.valueOf(time));
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(date);
	}

	private void showBirthdayDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(R.string.set_birthday_title);
		builder.setSingleChoiceItems(dialogitem, 0,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSingleChoiceID = whichButton;
						Log.d(TAG, "you select id" + whichButton + " , "
								+ dialogitem[whichButton]);
					}
				});
		builder.setPositiveButton(R.string.label_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (mSingleChoiceID == 0) {
							showPickData();
						} else if (mSingleChoiceID == 1) {
							detail_birthday.setText("");
						}
					}
				});
		builder.setNegativeButton(R.string.label_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
		builder.create().show();
	}

	public boolean createEditMap(HashMap<String, String> map) {
	    boolean haveUpdate = false;
		String office_address_text = et_office_address.getText().toString()
				.trim();
		if (!office_address_text.equals(mUser.office_address)) {
			map.put("office_address", office_address_text);
			mCopyUser.office_address = office_address_text;
			haveUpdate = true;
		}

		String company_text = detail_company.getText().toString().trim();
		if (!company_text.equals(mUser.company)) {
			map.put("company", company_text);
			mCopyUser.company = company_text;
			haveUpdate = true;
		}
		String department_text = detail_department.getText().toString().trim();
		if (!department_text.equals(mUser.department)) {
			map.put("department", department_text);
			mCopyUser.department = department_text;
			haveUpdate = true;
		}

		String job_text = detail_job_title.getText().toString().trim();
		if (!job_text.equals(mUser.jobtitle)) {
			map.put("job_title", job_text);
			mCopyUser.jobtitle = job_text;
			haveUpdate = true;
		}
		String name_text = detail_name.getText().toString().trim();
		if (!name_text.equals(mUser.nick_name)) {
			map.put("display_name", name_text);
			mCopyUser.nick_name = name_text;
			haveUpdate = true;
		}

		String address_text = detail_address.getText().toString().trim();
		if (!address_text.equals(mUser.location)) {
			map.put("address", StringUtil.createAddressJsonString(address_text));
			mCopyUser.location = address_text;
			haveUpdate = true;
		}

		String birthday_text = detail_birthday.getText().toString().trim();
		if (!birthday_text.equals(mUser.date_of_birth)) {
			map.put("birthday", birthday_text);
			mCopyUser.date_of_birth = birthday_text;
			haveUpdate = true;
		}

		// gender
//		String sex = "";
//		if (sex_m.isChecked()) {
//			sex = "m";
//		} else if (sex_f.isChecked()) {
//			sex = "f";
//		}
//		if (!sex.equals(mUser.gender)) {
//			map.put("gender", sex);
//			mCopyUser.gender = sex;
//		}
		String sex = "";
		if (getString(R.string.user_sex_man).equals(tv_gender.getText().toString())) {
			sex = "m";
		} else if (getString(R.string.user_sex_woman).equals(tv_gender.getText().toString())) {
			sex = "f";
		}
		if (!sex.equals(mUser.gender)) {
			map.put("gender", sex);
			mCopyUser.gender = sex;
			haveUpdate = true;
		}
		return haveUpdate;
	}

	public void createEditBaseMap(HashMap<String, String> map) {
        String office_address_text = et_office_address.getText().toString()
                .trim();
        if (!office_address_text.equals(mUser.office_address)) {
            map.put("office_address", office_address_text);
            mCopyUser.office_address = office_address_text;
        }

        String company_text = detail_company.getText().toString().trim();
        if (!company_text.equals(mUser.company)) {
            map.put("company", company_text);
            mCopyUser.company = company_text;
        }
        String department_text = detail_department.getText().toString().trim();
        if (!department_text.equals(mUser.department)) {
            map.put("department", department_text);
            mCopyUser.department = department_text;
        }

        String job_text = detail_job_title.getText().toString().trim();
        if (!job_text.equals(mUser.jobtitle)) {
            map.put("job_title", job_text);
            mCopyUser.jobtitle = job_text;
        }
        String name_text = detail_name.getText().toString().trim();
        if (!name_text.equals(mUser.nick_name)) {
            map.put("display_name", name_text);
            mCopyUser.nick_name = name_text;
        }

        String address_text = detail_address.getText().toString().trim();
        if (!address_text.equals(mUser.location)) {
            map.put("address", StringUtil.createAddressJsonString(address_text));
            mCopyUser.location = address_text;
        }

        String birthday_text = detail_birthday.getText().toString().trim();
        if (!birthday_text.equals(mUser.date_of_birth)) {
            map.put("birthday", birthday_text);
            mCopyUser.date_of_birth = birthday_text;
        }

//        // gender
//        String sex = "";
//        if (sex_m.isChecked()) {
//            sex = "m";
//        } else if (sex_f.isChecked()) {
//            sex = "f";
//        }
//        if (!sex.equals(mUser.gender)) {
//            map.put("gender", sex);
//            mCopyUser.gender = sex;
//        }
        String sex = "";
		if (getString(R.string.user_sex_man).equals(tv_gender.getText().toString())) {
			sex = "m";
		} else if (getString(R.string.user_sex_woman).equals(tv_gender.getText().toString())) {
			sex = "f";
		}
		if (!sex.equals(mUser.gender)) {
			map.put("gender", sex);
			mCopyUser.gender = sex;
		}
    }
	
	public interface onCallBackListener {
		public void callback(EditProfilesBaseInfoFragment base_fragment);

		public void updateBaseInfo(HashMap<String, String> map);
	}
	
	private EditText mStatusEditText;
	private TextView currentDialogTextView;
    private void showUpdateStatusDialog(String title,TextView currenttv,boolean isSingleLine){
    	this.currentDialogTextView = currenttv;
//    	View selectview = LayoutInflater.from(mActivity).inflate(R.layout.edit_profile_status, null);
//    	
//    	TextView profile_dialog_title = (TextView) selectview.findViewById(R.id.profile_dialog_title);
//    	mStatusEditText = (EditText) selectview.findViewById(R.id.edit_content);
//    	ImageView profile_status_commit = (ImageView) selectview.findViewById(R.id.profile_status_commit);
    	mStatusEditText = new EditText(mActivity);
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    	params.setMargins(10, 10, 10, 10);
    	mStatusEditText.setLayoutParams(params);
    	if(isSingleLine) {
    		mStatusEditText.setSingleLine();
    	}else {
    		mStatusEditText.setMinLines(3);
    		mStatusEditText.setMaxLines(5);
    	}
//    	mStatusEditText.setTextColor(Color.BLACK);
//    	mStatusEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
    	
//    	profile_status_commit.setVisibility(View.GONE);
//    	profile_dialog_title.setVisibility(View.GONE);
    	
    	mStatusEditText.setText(currentDialogTextView.getText().toString().trim());
//    	mStatusEditText.setHint(R.string.update_my_status_hint);
//    	mStatusEditText.setSelection(0, mStatusEditText.getText().length() - 1);
    	
    	DialogUtils.ShowDialogwithView(mActivity,title, 0,
    			mStatusEditText, UpdateStatusOkListener, dialogCancleListener);
        mStatusEditText.requestFocus();
    }
	
    DialogInterface.OnClickListener UpdateStatusOkListener = new DialogInterface.OnClickListener() {
    	
    	@Override
    	public void onClick(DialogInterface dialog, int which) {
    		currentDialogTextView.setText(mStatusEditText.getText().toString().trim());
    		
    	}
    };
    
    DialogInterface.OnClickListener dialogCancleListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			
		}
	};
	
	private void showGenderDialog() {
		final CharSequence[] items = {getString(R.string.user_sex_man),getString(R.string.user_sex_woman)}; 
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(getString(R.string.update_contact_info_btn)+getString(R.string.user_gender));
		int itemid = 0;
		if (getString(R.string.user_sex_man).equals(tv_gender.getText().toString())) {
			itemid = 0;
		} else  {
			itemid = 1;
		}
		builder.setSingleChoiceItems(items,itemid, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        if(item==0) {
		        	tv_gender.setText(R.string.user_sex_man);
		        }else {
		        	tv_gender.setText(R.string.user_sex_woman);
		        }
		        dialog.dismiss();
		    }
		});
    	if(dialogCancleListener != null) {
    		builder.setNegativeButton(R.string.label_cancel, dialogCancleListener);
    	}
		AlertDialog alert = builder.create();
		alert.show();
	}
    
}
