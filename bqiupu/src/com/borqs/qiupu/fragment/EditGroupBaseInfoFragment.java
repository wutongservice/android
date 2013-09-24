package com.borqs.qiupu.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.UserCircle.Group;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.borqs.common.SelectionItem;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.circle.EventThemeActivity;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class EditGroupBaseInfoFragment extends EditGroupBaseFragment {
	private final static String TAG = "EditGroupBaseInfoFragment";
	
	private EditText mPublic_circle_name;
	private EditText mPublic_circle_description;
	private EditText mPublic_circle_location;
	private EditText mPublic_circle_company;
	private EditText mPublic_circle_department;
	private EditText mPublic_circle_job;
//	private EditText mPublic_circle_office_address;
	private EditText mPublic_circle_bulletin;
	private ImageView mCoverView;
	private View btn_add;
	private long mThemeId = -1;
	private String mThemeImage;
	private Handler mhandler;
	private static final int selectThemeRequestCode = 44444;
	
	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		mhandler = new Handler();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View contentView = inflater.inflate(R.layout.public_circle_baseinfo_ui, container, false);
    	
    	mPublic_circle_name = (EditText) contentView.findViewById(R.id.public_circle_name);
    	mPublic_circle_description = (EditText) contentView.findViewById(R.id.public_circle_description);
    	mPublic_circle_location = (EditText) contentView.findViewById(R.id.public_circle_location);
    	mPublic_circle_company = (EditText) contentView.findViewById(R.id.public_circle_company);
    	mPublic_circle_department = (EditText) contentView.findViewById(R.id.public_circle_department);
    	mPublic_circle_job = (EditText) contentView.findViewById(R.id.public_circle_job);
//    	mPublic_circle_office_address = (EditText) contentView.findViewById(R.id.public_circle_office_address);
    	mPublic_circle_bulletin = (EditText) contentView.findViewById(R.id.public_circle_bulletin);
    	mCoverView = (ImageView) contentView.findViewById(R.id.select_cover);
    	btn_add = contentView.findViewById(R.id.btn_add);
    	btn_add.setOnClickListener(editProfileClick);
    	View cover_rl = contentView.findViewById(R.id.cover_rl);
    	cover_rl.setOnClickListener(new OnClickListener() {
    		
    		@Override
    		public void onClick(View v) {
    			Intent intent = new Intent(mActivity, EventThemeActivity.class); 
    			startActivityForResult(intent, selectThemeRequestCode);
    		}
    	});
		return contentView;
	}
	
	protected void showCorpusSelectionDialog(ArrayList<SelectionItem> items) {
	    if(btn_add != null) {
	        int location[] = new int[2];
	        btn_add.getLocationInWindow(location);
	        WindowManager m = mActivity.getWindowManager();
	   	 	Display d = m.getDefaultDisplay(); 
	        int x = d.getWidth() - location[0];
	        int y = d.getHeight() - location[1];
	        
	        DialogUtils.showCorpusSelectionDialog(mActivity, x, y,items,Gravity.LEFT|Gravity.BOTTOM, actionListItemClickListener);
	    }
	}
	
	View.OnClickListener editProfileClick = new View.OnClickListener() {
        public void onClick(View v) {
        	ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
			if(mPublic_circle_company.getVisibility() != View.VISIBLE) {
				items.add(new SelectionItem("", getString(R.string.update_profile_company_hint)));
			}
			if(mPublic_circle_department.getVisibility() != View.VISIBLE) {
				items.add(new SelectionItem("", getString(R.string.update_profile_department_hint)));
			}
			if(mPublic_circle_job.getVisibility() != View.VISIBLE) {
				items.add(new SelectionItem("", getString(R.string.update_profile_job_hint)));
			}
//			if(mPublic_circle_office_address.getVisibility() != View.VISIBLE) {
//				items.add(new SelectionItem("", getString(R.string.update_profile_address_hint)));
//			}
			if(items.size() > 0) {
				showCorpusSelectionDialog(items);
			}else {
				btn_add.setVisibility(View.GONE);
			}
        }
    };
    
    OnItemClickListener actionListItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(CorpusSelectionItemView.class.isInstance(view)) {
                CorpusSelectionItemView item = (CorpusSelectionItemView) view;
                onCorpusSelected(item.getText());             
            }
        }
    };
    
    private void onCorpusSelected(String value) {
        if (getString(R.string.update_profile_company_hint).equals(value)) {
        	mPublic_circle_company.setVisibility(View.VISIBLE);
        	mPublic_circle_company.requestFocus();
        }else if (getString(R.string.update_profile_department_hint).equals(value)) {
        	mPublic_circle_department.setVisibility(View.VISIBLE);
        	mPublic_circle_department.requestFocus();
        }else if (getString(R.string.update_profile_job_hint).equals(value)) {
        	mPublic_circle_job.setVisibility(View.VISIBLE);
        	mPublic_circle_job.requestFocus();
//        }else if (getString(R.string.update_profile_address_hint).equals(value)) {
//        	mPublic_circle_office_address.setVisibility(View.VISIBLE);
//        	mPublic_circle_office_address.requestFocus();
        }
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

	private void initUI() {
		if(mCircle != null) {
			mPublic_circle_name.setText(mCircle.name);
			mPublic_circle_name.selectAll();
			mPublic_circle_description.setText(mCircle.description);
			mPublic_circle_location.setText(mCircle.location);
			if(mCircle.mGroup != null) {
					if(StringUtil.isEmpty(mCircle.mGroup.coverUrl)) {
						mCoverView.setImageResource(R.drawable.event_default_cover);
					}else {
						setViewIcon(mCircle.mGroup.coverUrl, mCoverView, false);
					}
			    mPublic_circle_bulletin.setText(mCircle.mGroup.bulletin);
			}
			
			if(TextUtils.isEmpty(mCircle.company)) {
				mPublic_circle_company.setVisibility(View.GONE);
			}else {
				mPublic_circle_company.setVisibility(View.VISIBLE);
				mPublic_circle_company.setText(mCircle.company);
			}
			if(TextUtils.isEmpty(mCircle.department)) {
				mPublic_circle_department.setVisibility(View.GONE);
			}else {
				mPublic_circle_department.setVisibility(View.VISIBLE);
				mPublic_circle_department.setText(mCircle.department);
			}
			if(TextUtils.isEmpty(mCircle.jobtitle)) {
				mPublic_circle_job.setVisibility(View.GONE);
			}else {
				mPublic_circle_job.setVisibility(View.VISIBLE);
				mPublic_circle_job.setText(mCircle.jobtitle);
			}
//			if(TextUtils.isEmpty(mCircle.office_address)) {
//				mPublic_circle_office_address.setVisibility(View.GONE);
//			}else {
//				mPublic_circle_office_address.setVisibility(View.VISIBLE);
//				mPublic_circle_office_address.setText(mCircle.office_address);
//			}
		}
	}
	
	@Override
	public HashMap<String, String> getEditGroupMap() {
	    // TODO Auto-generated method stub
	    return getEditBaseInfoMap();
	}
	
    private HashMap<String, String> getEditBaseInfoMap() {
		final String circleName = mPublic_circle_name.getText().toString().trim();
		if(TextUtils.isEmpty(circleName)) {
			ToastUtil.showShortToast(mActivity, mhandler, R.string.name_isnull_toast);
			mPublic_circle_name.requestFocus();
			return null;
		}
		final String des = mPublic_circle_description.getText().toString().trim();
		final String location =  mPublic_circle_location.getText().toString().trim();
		final String company = mPublic_circle_company.getText().toString().trim();
		final String department = mPublic_circle_department.getText().toString().trim();
		final String job = mPublic_circle_job.getText().toString().trim();
//		final String office_address = mPublic_circle_office_address.getText().toString().trim();
		final String bulletin = mPublic_circle_bulletin.getText().toString().trim();
		
		mCopyCircle = mCircle.clone();
		mCopyCircle.name = circleName;
		mCopyCircle.description = des;
		mCopyCircle.location = location;
		mCopyCircle.company = company;
		mCopyCircle.department = department;
		mCopyCircle.jobtitle = job;
//		mCopyCircle.office_address = office_address;
		mCopyCircle.mGroup.bulletin = bulletin;
		if(mThemeId > 0) {
			mCopyCircle.mGroup.coverUrl = mThemeImage;
		}
		
		HashMap<String , String> editMap = new HashMap<String, String>();
		if(!(circleName.equals(mCircle.name))) {
			editMap.put("name", circleName);
		}
		if(!(des.equals(mCircle.description))) {
			editMap.put("description", des);
		}
		if(!(location.equals(mCircle.location))) {
			editMap.put("address", StringUtil.createAddressJsonString(location));
		}
		if(!(company.equals(mCircle.company))) {
			editMap.put("company", company);
		}
		if(!(department.equals(mCircle.department))) {
			editMap.put("department", department);
		}
		if(!(job.equals(mCircle.jobtitle))) {
			editMap.put("jobtitle", job);
		}
//		if(!(office_address.equals(mCircle.office_address))) {
//			editMap.put("office_address", office_address);
//		}
		
		if(mCopyCircle.mGroup == null) {
		    mCopyCircle.mGroup = new Group();
		}
		if(mThemeImage != null && !mThemeImage.equals(mCircle.mGroup.coverUrl)) {
			editMap.put("theme_id", String.valueOf(mThemeId));
		}
		mCopyCircle.mGroup.bulletin = bulletin;
		mCopyCircle.mGroup.bulletin_updated_time = System.currentTimeMillis();
		if(!(bulletin.equals(mCircle.mGroup.bulletin))) {
		    editMap.put("bulletin", bulletin);
		}
		
		return editMap;
	}
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode);
    	if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case selectThemeRequestCode: {
            	mThemeImage = data.getStringExtra(EventThemeActivity.SELECT_THEME_URL);
            	Log.d(TAG, "onActivityResult ; " + mThemeImage);
            	mThemeId = data.getLongExtra(EventThemeActivity.SELECT_THEME_ID, -1);
            	setViewIcon(mThemeImage, mCoverView, false);
            	break;
            }
        }
    }
    
//    private void shootImageRunner(String photoUrl,ImageView img) {
//    	ImageRun imageRun = new ImageRun(mhandler, photoUrl, 0);
//    	imageRun.addHostAndPath = true;
//		final Resources resources = getResources();
//		imageRun.width = resources.getDisplayMetrics().widthPixels;
//		imageRun.height = imageRun.width;
//		imageRun.need_scale = true;
//		imageRun.setImageView(img);
//		imageRun.post(null);
//	}
}
