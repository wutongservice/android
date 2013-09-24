package com.borqs.common.quickaction;

import java.util.ArrayList;
import java.util.HashMap;

import com.borqs.common.util.Utilities;
import twitter4j.QiupuUser;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.BpcFriendsItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.fragment.QuickPeopleListFragment;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.ProfileActionGatewayActivity;
import com.borqs.qiupu.util.ContactUtils;
import com.borqs.qiupu.util.StringUtil;

public class QuickPeopleActivity extends BasicActivity {

	private static final String TAG = "QuickPeopleActivity";
	private ViewPager mListPager;
	private QiupuUser mUser;
	private long mContactId;
	private ViewGroup mTrack;
	private HorizontalScrollView mTrackScroller;
	private View mSelectedTabRectangle;
//	private TextView mExchangeStatus;
	private HashMap<Integer, ArrayList<ActionItem>> mSortedActions = new HashMap<Integer, ArrayList<ActionItem>>();
	public static final String QUICK_TYPE_PHONE = "quick_phone";
	public static final String QUICK_TYPE_EMAIL = "quick_email";
	public static final String QUICK_TYPE_EXCHANGEVCARD = "exchange_vcard";
	private static final int sdkLevel11 = 11;
	private boolean isHasPhones;
	private boolean isHasEmails;
	private boolean isHasOtherInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quick_people_ui);
        
		mUser = (QiupuUser) getIntent().getSerializableExtra("user");
		mContactId = getIntent().getLongExtra("contactId", -1);

		final View quick_people = findViewById(R.id.quick_people);
		
		quick_people.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				QuickPeopleActivity.this.finish();
			}
		});
		
		final View open_detail = findViewById(R.id.open_details_push_layer);
		open_detail.setOnClickListener(openDetailClickListener);
		
        final ImageView peopleIcon = (ImageView) findViewById(R.id.photo);
        final TextView name = (TextView) findViewById(R.id.name);
        mTrack = (ViewGroup) findViewById(R.id.track);
        mTrackScroller = (HorizontalScrollView) findViewById(R.id.track_scroller);
        mSelectedTabRectangle = findViewById(R.id.selected_tab_rectangle);
        
        if(mUser != null){
        	loadPeopleIcon(mUser.profile_limage_url, peopleIcon);
        	showName(name);
        	
        	bindPhoneData(mUser, QUICK_TYPE_PHONE);
        	bindEmailData(mUser, QUICK_TYPE_EMAIL);
        	bindOtherAction(null, String.valueOf(mUser.uid));
        	if(StringUtil.isEmpty(mUser.circleId) == false) {
        	    bindExchangeStatus(mUser);
        	}
        }
        
        if(mContactId != -1){
        	name.setText(getIntent().getStringExtra("name"));
        	loadContactIcon(mContactId, peopleIcon);
        	bindContactPhoneData(mContactId, QUICK_TYPE_PHONE);
        	bindContactEmailData(mContactId, QUICK_TYPE_EMAIL);
        	
        	if(Integer.parseInt(Utilities.getSdkVersion()) >= sdkLevel11) {
        		if(ContactUtils.getBorqsIdFromContact(this, mContactId) > 0) {
        			bindOtherAction(String.valueOf(mContactId), null);
        		}else {
        			long localBorqsId = orm.queryBorqsIdByContactId(mContactId);
        			if(localBorqsId > 0) {
        				bindOtherAction(String.valueOf(mContactId), String.valueOf(localBorqsId));
        			}else {
        				bindAddToCircleAction(String.valueOf(mContactId));
        			}
        		}
        	}
        }
        
        mListPager = (ViewPager) findViewById(R.id.pager);
		mListPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        mListPager.setOnPageChangeListener(new PageChangeListener());
        
	}
	
	private void showName(TextView name) {
	    if(StringUtil.isValidString(mUser.remark)) {
	        name.setText(mUser.remark);
	    }else {
	        if(mUser.perhapsNames != null && mUser.perhapsNames.size() > 0) {
	            name.setText(mUser.perhapsNames.get(0).name);
	        }else {
	            name.setText(mUser.nick_name);
	        }
	    }
	}

    @Override
	protected void createHandler() {
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
			final CheckableImageView actionView = getActionViewAt(position);
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
	
	private void loadPeopleIcon(String iconurl, ImageView view) {
		view.setImageResource(R.drawable.default_user_icon);
		ImageRun imagerun = new ImageRun(null,iconurl, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
        imagerun.noimage = true;
        imagerun.addHostAndPath = true;
        imagerun.setRoundAngle=true;
        imagerun.setImageView(view);        
        imagerun.post(null);
	}
	
	private void loadContactIcon(long contactId, ImageView view) {
		Bitmap contactPhoto = ContactUtils.getContactPhoto(this, contactId);
        if (contactPhoto != null) {
        	view.setImageBitmap(contactPhoto);
            contactPhoto = null;
        }else {
        	view.setImageResource(R.drawable.default_user_icon);
        }
	}
	
	private static ActionItem newActionItem(String content, String type, String userId, String contactId) {
		return ProfileActionGatewayActivity.newActionItem(content, type, userId, contactId);
	}

	private void bindPhoneData(QiupuUser user, String phoneType) {
		ArrayList<ActionItem> phoneActionList = new ArrayList<ActionItem>();
		if(user.phoneList != null && user.phoneList.size() > 0) {
		    for(int i=0; i<user.phoneList.size(); i++) {
		        phoneActionList.add(newActionItem(user.phoneList.get(i).info, phoneType, null, null));
		    }
		}
		addPhoneToPageView(phoneActionList);
	}
	
	private void bindEmailData(QiupuUser user, String emailType) {
	    ArrayList<ActionItem> emailList = new ArrayList<ActionItem>();	
	    if(user.emailList != null && user.emailList.size() > 0) {
            for(int i=0; i<user.emailList.size(); i++) {
                emailList.add(newActionItem(user.emailList.get(i).info, emailType, null, null));
            }
        }
		addEmailToPageView(emailList);
	}
	
	private void bindContactPhoneData(long contactId, String phoneType) {
		ArrayList<String> phoneList = ContactUtils.getPhonesList(this, contactId);
		ArrayList<ActionItem> phoneActionList = new ArrayList<ActionItem>();							

		if (phoneList != null) {
			for(int i=0; i<phoneList.size(); i++) {
				phoneActionList.add(newActionItem(phoneList.get(i), phoneType, null, null));
			}
		}
		addPhoneToPageView(phoneActionList);
	}
	
	private void bindContactEmailData(long contactId, String emailType) {
		ArrayList<String> emailList = ContactUtils.getEmailsList(this, contactId);
		ArrayList<ActionItem> emailActionList = new ArrayList<ActionItem>();							

		if(emailList != null){
			for(int i=0; i<emailList.size(); i++) {
				emailActionList.add(newActionItem(emailList.get(i), emailType, null, null));
			}
		}
		addEmailToPageView(emailActionList);
	}
	
	private void bindOtherAction(String contactId, String userId) {
		ArrayList<ActionItem> otherActionList = ProfileActionGatewayActivity.generateOtherActionList(this, contactId, userId);
		addOtherActionToPageView(otherActionList);
	}
	
	
	private void bindAddToCircleAction(String contactId) {
		ArrayList<ActionItem> otherActionList = ProfileActionGatewayActivity.generateInviteContactActionList(this, contactId);
		addOtherActionToPageView(otherActionList);
	}
	
	private void addOtherActionToPageView(ArrayList<ActionItem> otherActionList) {
	    isHasOtherInfo = true;
		int mineType_other = 0;
		if(isHasEmails && isHasPhones) {
			mineType_other = 2;
		}else if(isHasEmails || isHasPhones) {
			mineType_other = 1;
		}
		mSortedActions.put(mSortedActions.size(), otherActionList);
		final View actionView = inflateAction(mineType_other, R.drawable.ic_bpc_launcher, mTrack);
		mTrack.addView(actionView);
	}
	
	private void addPhoneToPageView(ArrayList<ActionItem> phoneActionList) {
		if(phoneActionList.size() > 0){
			isHasPhones = true;
			mSortedActions.put(mSortedActions.size(), phoneActionList);
			final View actionView = inflateAction(0, R.drawable.account_phone_icon, mTrack);
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
			final View actionView = inflateAction(mineType_email, R.drawable.account_email_icon, mTrack);
			mTrack.addView(actionView);
		}
	}
	
	private View inflateAction(int mimeType, int imgResid, ViewGroup root) {
		final CheckableImageView typeView = (CheckableImageView) getLayoutInflater().inflate(
				R.layout.quickcontact_track_button, root, false);
		
		typeView.setTag(mimeType);
		typeView.setChecked(false);
		typeView.setImageResource(imgResid);
		typeView.setOnClickListener(mTypeViewClickListener);
		
		return typeView;
	}
	
	private CheckableImageView getActionViewAt(int position) {
        return (CheckableImageView) mTrack.getChildAt(position);
    }
	
	private final OnClickListener mTypeViewClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            final CheckableImageView actionView = (CheckableImageView)view;
            final int mimeType = (Integer) actionView.getTag();
            mListPager.setCurrentItem(mimeType);
        }
    };
	
	OnClickListener openDetailClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mUser != null){
				IntentUtil.startUserDetailAboutIntent(QuickPeopleActivity.this, mUser.uid, mUser.nick_name);
			}else if(mContactId != -1) {
				IntentUtil.startContactDetailIntent(QuickPeopleActivity.this, mContactId);
			}else {
				Log.d(TAG, "the people is null");
			}
		}
	};
	
	private String getExchangeStatus() {
	    boolean inMyPrivacyCircle = false;
	    String status;
	    if (mUser.circleId != null && mUser.circleId.length() > 0) {
	        String[] circleIds = mUser.circleId.split(",");
	        for (int i = 0; i < circleIds.length; i++) {
	            long circleid = Long.parseLong(circleIds[i]);
	            if (circleid == QiupuConfig.ADDRESS_BOOK_CIRCLE) {
	                inMyPrivacyCircle = true;
	                break;
	            }
	        }
	    }
	    
	    if (inMyPrivacyCircle) {
	        status = getString(R.string.friends_exchenge_info_allow);
	        if (mUser.profile_privacy
	                && BpcFriendsItemView
	                .isalreadyRequestProfile(mUser.pedding_requests)) {
	            status = getString(R.string.friends_exchenge_already_send_request);
	        } else if (!mUser.profile_privacy) {
	            status = getString(R.string.friends_exchenge_already_exchange_info);
	        }
	    } else {
	        if (mUser.profile_privacy
	                && BpcFriendsItemView
	                .isalreadyRequestProfile(mUser.pedding_requests)) {
	            status = getString(R.string.friends_exchenge_already_send_request);
	        } else {
	            status = getString(R.string.friends_item_request_exchange); 
	        }
	    }
	    return status;
	}
    
    private void bindExchangeStatus(QiupuUser user) {
        ArrayList<ActionItem> exchangeActionList = new ArrayList<ActionItem>();
        exchangeActionList.add(newActionItem(getExchangeStatus(), QUICK_TYPE_EXCHANGEVCARD, String.valueOf(user.uid), null));
        addExchangeToPageView(exchangeActionList);
    }

    private void addExchangeToPageView(ArrayList<ActionItem> exchangeActionList) {
        int mineType_other = 0;
        if(isHasEmails && isHasPhones && isHasOtherInfo) {
            mineType_other = 3;
        }else if(isHaveOneAction()) {
            mineType_other = 1;
        }else if(isHaveTwoAction()){
            mineType_other = 2;
        }
        mSortedActions.put(mSortedActions.size(), exchangeActionList);
        final View actionView = inflateAction(mineType_other, R.drawable.card_ok_icon, mTrack);
        mTrack.addView(actionView);
    }
    
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
}
