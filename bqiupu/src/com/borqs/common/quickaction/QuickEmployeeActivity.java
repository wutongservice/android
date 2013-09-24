package com.borqs.common.quickaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import twitter4j.Employee;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.fragment.QuickPeopleListFragment;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.ProfileActionGatewayActivity;

public class QuickEmployeeActivity extends BasicActivity {

	private static final String TAG = "QuickPeopleActivity";
	private ViewPager mListPager;
	private Employee mEmp;
	private ViewGroup mTrack;
	private HorizontalScrollView mTrackScroller;
	private View mSelectedTabRectangle;
//	private TextView mExchangeStatus;
	private HashMap<Integer, ArrayList<ActionItem>> mSortedActions = new HashMap<Integer, ArrayList<ActionItem>>();
	public static final String QUICK_TYPE_EXTENTION = "quick_extention";
	public static final String QUICK_TYPE_PHONE = "quick_phone";
	public static final String QUICK_TYPE_EMAIL = "quick_email";
	public static final String VIEW_WUTONG = "VIEW_WUTONG";
	public static final String DOWNLOAD_WUTONG = "DOWNLOAD_WUTONG";
	public static final String QUICK_TYPE_EXCHANGEVCARD = "exchange_vcard";
	private static final int sdkLevel11 = 11;
	private boolean isHasPhones;
	private boolean isHasEmails;
	private boolean isHasOtherInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quick_employee_ui);
        
		mEmp = (Employee) getIntent().getSerializableExtra("Employee");

		final View dismiss_layout = findViewById(R.id.dismiss_layout);
		
		dismiss_layout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				QuickEmployeeActivity.this.finish();
			}
		});
		
		final View open_detail = findViewById(R.id.open_details_push_layer);
		open_detail.setOnClickListener(openDetailClickListener);
		
        final ImageView peopleIcon = (ImageView) findViewById(R.id.photo);
        final TextView name = (TextView) findViewById(R.id.name);
        mTrack = (ViewGroup) findViewById(R.id.track);
        mTrackScroller = (HorizontalScrollView) findViewById(R.id.track_scroller);
        mSelectedTabRectangle = findViewById(R.id.selected_tab_rectangle);
        
        if(mEmp != null){
        	if(TextUtils.isEmpty(mEmp.image_url_l)) {
        		loadPeopleIcon(mEmp.image_url_m, peopleIcon);
        	}else {
        		loadPeopleIcon(mEmp.image_url_l, peopleIcon);
        	}
        	if(!TextUtils.isEmpty(mEmp.name)) {
    	        name.setText(mEmp.name);
    	    }
        	
        	bindPhoneData(mEmp, QUICK_TYPE_PHONE);
        	bindEmailData(mEmp, QUICK_TYPE_EMAIL);
        	bindOtherAction(null, mEmp.user_id);
        }
        
        
        mListPager = (ViewPager) findViewById(R.id.pager);
		mListPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        mListPager.setOnPageChangeListener(new PageChangeListener());
        
	}
	
	private class ViewPagerAdapter extends FragmentPagerAdapter {
		public ViewPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}
		
		@Override
		public Fragment getItem(int position) {
			QuickPeopleListFragment fragment = new QuickPeopleListFragment();
			fragment.setActions(mSortedActions.get(position));
			return fragment;
		}
		
		@Override
		public int getCount() {
			return mSortedActions.size();
		}
	}
	 
	private class PageChangeListener extends SimpleOnPageChangeListener {
		@Override
		public void onPageSelected(int position) {
//			final CheckableImageView actionView = getActionViewAt(position);
			final TextView actionView = getActionViewAt(position);
			mTrackScroller.requestChildRectangleOnScreen(actionView,
					new Rect(0, 0, actionView.getWidth(), actionView.getHeight()), false);
		}
		
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			final RelativeLayout.LayoutParams layoutParams =
					(RelativeLayout.LayoutParams) mSelectedTabRectangle.getLayoutParams();
			final int width = mSelectedTabRectangle.getWidth();
			layoutParams.leftMargin = (int) ((position + positionOffset) * width);
			mSelectedTabRectangle.setLayoutParams(layoutParams);
		}
	}
	
	private void loadPeopleIcon(String photoUrl, ImageView img) {
//		// Get singletone instance of ImageLoader
//		ImageLoader imageLoader = DirectoryApplication.getApplication(img.getContext()).getImageLoader();
//		// Creates display image options for custom display task (all options are optional)
//		DisplayImageOptions options = new DisplayImageOptions.Builder()
//		           .resetViewBeforeLoading()
//		           .cacheInMemory()
//		           .cacheOnDisc()
//		           .showImageForEmptyUri(R.drawable.img_contacts_default)
//		           .showStubImage(R.drawable.img_contacts_default)
//		           .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
//		           .bitmapConfig(Bitmap.Config.RGB_565)
//		           .build();
//		// Load and display image asynchronously
//		imageLoader.displayImage(photoUrl, img,options,null );
		ImageRun photo_1 = new ImageRun(null, photoUrl, 0);
		photo_1.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
		photo_1.addHostAndPath = true;
		final Resources resources = img.getResources();
		photo_1.width = resources.getDisplayMetrics().widthPixels;
		photo_1.height = resources.getDisplayMetrics().heightPixels;
		photo_1.noimage = true;
		photo_1.setImageView(img);
		photo_1.post(null);
	}
	
//	private void loadContactIcon(long contactId, ImageView view) {
//		Bitmap contactPhoto = ContactUtils.getContactPhoto(this, contactId);
//        if (contactPhoto != null) {
//        	view.setImageBitmap(contactPhoto);
//            contactPhoto = null;
//        }else {
//        	view.setImageResource(R.drawable.default_user_icon);
//        }
//	}
	
	
	public static ActionItem newActionItem(String content, String type, String userId, String contactId) {
        ActionItem items = new ActionItem();
        items.setTitle(content);
        items.setType(type);
        if (!TextUtils.isEmpty(userId)) {
            items.setUserId(userId);
        }
        if (!TextUtils.isEmpty(contactId)) {
            items.setContactId(contactId);
        }
        return items;
    }

	private void bindPhoneData(Employee emp, String phoneType) {
		ArrayList<ActionItem> phoneActionList = new ArrayList<ActionItem>();
		if(!TextUtils.isEmpty(emp.mobile_tel)) {
			phoneActionList.add(newActionItem(emp.mobile_tel, phoneType, null, null));
		}
		if(!TextUtils.isEmpty(emp.tel)) {
			phoneActionList.add(newActionItem(emp.tel, QUICK_TYPE_EXTENTION, null, null));
		}
		addPhoneToPageView(phoneActionList);
	}
	
	private void bindEmailData(Employee emp, String emailType) {
	    ArrayList<ActionItem> emailList = new ArrayList<ActionItem>();	
	    if(!TextUtils.isEmpty(emp.email)) {
	    	emailList.add(newActionItem(emp.email, emailType, null, null));
	    }
		addEmailToPageView(emailList);
	}
	
//	private void bindContactPhoneData(String phoneNumber, String phoneType) {
//		ArrayList<String> phoneList = new ArrayList<String>();
//		phoneList.add(phoneNumber);
//		ArrayList<ActionItem> phoneActionList = new ArrayList<ActionItem>();							
//
//		if (phoneList != null) {
//			for(int i=0; i<phoneList.size(); i++) {
//				phoneActionList.add(newActionItem(phoneList.get(i), phoneType, null, null));
//			}
//		}
//		addPhoneToPageView(phoneActionList);
//	}
//	
//	private void bindContactEmailData(long contactId, String emailType) {
//		ArrayList<String> emailList = ContactUtils.getEmailsList(this, contactId);
//		ArrayList<ActionItem> emailActionList = new ArrayList<ActionItem>();							
//
//		if(emailList != null){
//			for(int i=0; i<emailList.size(); i++) {
//				emailActionList.add(newActionItem(emailList.get(i), emailType, null, null));
//			}
//		}
//		addEmailToPageView(emailActionList);
//	}
	
	private void bindOtherAction(String contactId, String userId) {
//		ArrayList<ActionItem> otherActionList = generateOtherActionList(this, contactId, userId);
		ArrayList<ActionItem> otherActionList = ProfileActionGatewayActivity.generateOtherActionList(this, contactId, userId);
		addOtherActionToPageView(otherActionList);
	}
	
	public  ArrayList<ActionItem> generateOtherActionList(Context context, String contactId, String userId) {
        ArrayList<ActionItem> otherActionList = new ArrayList<ActionItem>();
//        if(isExitWutong()) {
//        	BLog.v("isExitWutong --- true");
        	otherActionList.add(newActionItem(context.getString(R.string.view_bpc_profile), VIEW_WUTONG, userId, contactId));
//        }else {
//        	BLog.v("isExitWutong --- false");
//        	otherActionList.add(newActionItem(context.getString(R.string.download_wutong), DOWNLOAD_WUTONG, userId, contactId));
//        }
        return otherActionList;
    }
//	
//	
//	private void bindAddToCircleAction(String contactId) {
//		ArrayList<ActionItem> otherActionList = ProfileActionGatewayActivity.generateInviteContactActionList(this, contactId);
//		addOtherActionToPageView(otherActionList);
//	}
	
	private void addOtherActionToPageView(ArrayList<ActionItem> otherActionList) {
	    isHasOtherInfo = true;
		int mineType_other = 0;
		if(isHasEmails && isHasPhones) {
			mineType_other = 2;
		}else if(isHasEmails || isHasPhones) {
			mineType_other = 1;
		}
		mSortedActions.put(mSortedActions.size(), otherActionList);
//		final View actionView = inflateAction(mineType_other, R.drawable.ic_bpc_launcher, mTrack);
		final View actionView = inflateAction(mineType_other, R.string.app_name, mTrack);
		mTrack.addView(actionView);
	}
	
	private void addPhoneToPageView(ArrayList<ActionItem> phoneActionList) {
		if(phoneActionList.size() > 0){
			isHasPhones = true;
			mSortedActions.put(mSortedActions.size(), phoneActionList);
//			final View actionView = inflateAction(0, R.drawable.account_phone_icon_big, mTrack);
			final View actionView = inflateAction(0, R.string.phone_label, mTrack);
			mTrack.addView(actionView);
		}
	}
	
	private void addEmailToPageView(ArrayList<ActionItem> emailActionList) {
		if(emailActionList.size() > 0){
			int mineType_email = 0;
			isHasEmails = true;
			if(isHasPhones) {
				mineType_email = 1;
			}
			mSortedActions.put(mSortedActions.size(), emailActionList);
//			final View actionView = inflateAction(mineType_email, R.drawable.account_email_icon_big, mTrack);
			final View actionView = inflateAction(mineType_email, R.string.email_label, mTrack);
			mTrack.addView(actionView);
		}
	}
	
//	private View inflateAction(int mimeType, int imgResid, ViewGroup root) {
//		final CheckableImageView typeView = (CheckableImageView) getLayoutInflater().inflate(
//				R.layout.quickcontact_track_button, root, false);
//		
//		typeView.setTag(mimeType);
//		typeView.setChecked(false);
//		typeView.setImageResource(imgResid);
//		typeView.setOnClickListener(mTypeViewClickListener);
//		
//		return typeView;
//	}
	private View inflateAction(int mimeType, int imgResid, ViewGroup root) {
		final TextView typeView = (TextView) getLayoutInflater().inflate(
				R.layout.quickcontact_track_textview, root, false);
		
		typeView.setTag(mimeType);
//		typeView.setChecked(false);
//		typeView.setImageResource(imgResid);
		typeView.setText(imgResid);
		typeView.setOnClickListener(mTypeViewClickListener);
		
		return typeView;
	}
	
	private TextView getActionViewAt(int position) {
        return (TextView) mTrack.getChildAt(position);
    }
	
	private final OnClickListener mTypeViewClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            final TextView actionView = (TextView)view;
            final int mimeType = (Integer) actionView.getTag();
            mListPager.setCurrentItem(mimeType);
        }
    };
	
	OnClickListener openDetailClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
//			if(mEmp != null){
//				IntentUtil.startUserDetailAboutIntent(QuickPeopleActivity.this, mUser.uid, mUser.nick_name);
//			}else {
//				Log.d(TAG, "the people is null");
//			}
		}
	};
	
    

    
    private boolean isHaveOneAction() {
        return (isHasEmails && !isHasPhones && !isHasOtherInfo)
                || (!isHasEmails && isHasPhones && !isHasOtherInfo)
                || (!isHasEmails && !isHasPhones && isHasOtherInfo);
    }
    
    private boolean isHaveTwoAction() {
        return (isHasPhones && isHasEmails)
                || (isHasPhones && isHasOtherInfo)
                || (isHasEmails && isHasOtherInfo);
    }
    
    /**
     * 判断手机中是否安装了梧桐客户端
     * @return
     */
    private boolean isExitWutong() {
    	Intent intent = new Intent();
    	intent.setPackage("com.borqs.qiupu");
    	List<ResolveInfo> activities = getPackageManager().queryIntentActivities(intent, 0);
//    	List< ApplicationInfo> appList = getPackageManager().getInstalledApplications(PackageManager.get_);
    	if(activities != null && activities.size() > 0) {
    		return true;
    	}
    	return false;
    }

	@Override
	protected void createHandler() {
		// TODO Auto-generated method stub
		
	}
    
}
