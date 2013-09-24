package com.borqs.qiupu.fragment;

import java.util.ArrayList;

import twitter4j.AsyncQiupu;
import twitter4j.Company;
import twitter4j.PublicCircleRequestUser;
import twitter4j.QiupuUser;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.common.ShareSourceItem;
import com.borqs.common.adapter.GridPeopleSimpleAdapter;
import com.borqs.common.adapter.ProfileShareSourceAdapter;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.MyGridView;
import com.borqs.common.view.ProfileSourceItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.bpc.ShareResourcesActivity;
import com.borqs.qiupu.ui.company.CompanyDepListActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.DateUtil;
import com.borqs.qiupu.util.StringUtil;

public class CompanyDetailFragment extends BasicFragment implements UsersActionListner {
	private final static String TAG = "CompanyDetailFragment";
	private Activity mActivity; 
	
	private QiupuORM orm;
	private AsyncQiupu asyncQiupu;
	private CompanyDetailFragmentListenerCallBack mCallBackListener;
	private TextView mEvent_title;
	private View mAddress_info;
	private View mPhone_info;
	private View mEmail_info;
	private TextView mEvent_des;
	private ImageView mProfile_img_ui;
	
    private Company mCompany;
    private GridView mGrid_in_member;
    private GridView mGrid_invited_member;
    
    private View mContentView;
    private TextView mPrivacyTextView;
    
    private ImageView mCover;
    private TextView mTime;
    private MyGridView mGridview;
    public static final int TYPE_TEXT  = 0;
    public static final int PHOTO_PICKED_WITH_DATA = 3021;
    public static final int CAMERA_WITH_DATA = 3023;
    
    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
    	super.onAttach(activity);
    	mActivity = activity;

        if (mActivity instanceof CompanyDetailFragmentListenerCallBack) {
        	mCallBackListener = (CompanyDetailFragmentListenerCallBack)activity;
        	mCallBackListener.getCompanyDetailInfoFragment(this);
        	mCompany = mCallBackListener.getCompanyDetailInfo();
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
    	super.onCreate(savedInstanceState);
    	parserSavedState(savedInstanceState);
    	QiupuHelper.registerUserListener(getClass().getName(), this);
    	orm = QiupuORM.getInstance(mActivity);
		asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView");
    	
    	mContentView = inflater.inflate(R.layout.company_detail_fragment, container, false);
    	mEvent_title = (TextView) mContentView.findViewById(R.id.id_event_title);
//    	mMember_limit = (TextView) mContentView.findViewById(R.id.member_limit);
    	mAddress_info = mContentView.findViewById(R.id.address_info_ll);
    	mPhone_info = mContentView.findViewById(R.id.phone_info_ll);
    	mEmail_info = mContentView.findViewById(R.id.email_info_ll);
    	mEvent_des = (TextView) mContentView.findViewById(R.id.id_description);
    	mProfile_img_ui =  (ImageView) mContentView.findViewById(R.id.creator_icon);
    	mGridview = (MyGridView) mContentView.findViewById(R.id.source_view);
    	mGrid_in_member = (GridView) mContentView.findViewById(R.id.grid_in_member);
//    	mGrid_applyed_member = (GridView) mContentView.findViewById(R.id.grid_apply_member);
    	mGrid_invited_member = (GridView) mContentView.findViewById(R.id.grid_invite_member);
    	
    	mCover = (ImageView) mContentView.findViewById(R.id.select_cover);
    	mTime = (TextView) mContentView.findViewById(R.id.id_time);
    	mPrivacyTextView = (TextView) mContentView.findViewById(R.id.id_privacy);
    	
    	View inMemberView = mContentView.findViewById(R.id.in_member_rl);
    	inMemberView.setOnClickListener(membersClickListener);
    	
//    	View applyMemberView = mContentView.findViewById(R.id.apply_member_rl);
//    	applyMemberView.setOnClickListener(membersClickListener);
        
        View inviteMemberView = mContentView.findViewById(R.id.invite_member_rl);
        inviteMemberView.setOnClickListener(membersClickListener);
        
    	return mContentView;
    }
    
    private void parserSavedState(Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            if(mCompany == null) {
            	mCompany = new Company();
            }
            
            mCompany.id = savedInstanceState.getLong(Company.COMPANY_ID);
        }
    }
    
    private View findFragmentViewById(int id) {
        if(mContentView != null) {
            return mContentView.findViewById(id);
        }
        return null;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mGridview.setOnItemClickListener(shareSourceItemClickListener);
        refreshCircleInfoUi();
    }

    private void refreshCircleInfoUi() {
    	if(mCompany != null && isDetached() == false && getActivity() != null) {
    	    refreshHeadUi();
    	    refreshMemberUi();
    		refreshOtherInfoUi();
    		refreshShareSourceResult();
		}
    }
    
    private void refreshShareSourceResult() {
		ArrayList<ShareSourceItem> dataList = new ArrayList<ShareSourceItem>();
		
		initShareSourceList(dataList);
		if(mGridview != null) {
			ProfileShareSourceAdapter shareAdapter = new ProfileShareSourceAdapter(mActivity, QiupuConfig.PUBLIC_CIRCLE_MIN_ID);
			mGridview.setAdapter(shareAdapter);
			shareAdapter.alterDataList(dataList);
		}
	}
    
    private ShareSourceItem createShareSoureWithType(int type) {
    	ShareSourceItem shared = new ShareSourceItem("");
		shared.mType = type;
		return shared;
    }
    private void initShareSourceList(ArrayList<ShareSourceItem> dataList) {
    	dataList.add(createShareSoureWithType(BpcApiUtils.TEXT_POST));
    	dataList.add(createShareSoureWithType(BpcApiUtils.IMAGE_POST));
    	dataList.add(createShareSoureWithType(BpcApiUtils.BOOK_POST));
    	dataList.add(createShareSoureWithType(BpcApiUtils.APK_POST));
    	dataList.add(createShareSoureWithType(BpcApiUtils.LINK_POST));
    	dataList.add(createShareSoureWithType(BpcApiUtils.AUDIO_POST));
    	dataList.add(createShareSoureWithType(BpcApiUtils.VIDEO_POST));
		dataList.add(createShareSoureWithType(BpcApiUtils.STATIC_FILE_POST));
		
    }
    
    private void refreshOtherInfoUi() {
        if(StringUtil.isValidString(mCompany.address)) {
        	findFragmentViewById(R.id.address_info_title).setVisibility(View.VISIBLE);
        	findFragmentViewById(R.id.span_view2).setVisibility(View.VISIBLE);
            mAddress_info.setVisibility(View.VISIBLE);
            mAddress_info.setOnClickListener(addressClickListener);
            TextView address = (TextView) findFragmentViewById(R.id.address_info);
            address.setText(mCompany.address);
        } else {
            mAddress_info.setVisibility(View.GONE);
            findFragmentViewById(R.id.address_info_title).setVisibility(View.GONE);
        	findFragmentViewById(R.id.span_view2).setVisibility(View.GONE);
        }
        
//        if(StringUtil.isValidString(mCircle.description)) {
//            findFragmentViewById(R.id.circle_des_info_ll).setVisibility(View.VISIBLE);
//            mEvent_des.setText(mCircle.description);
//        }else {
//            findFragmentViewById(R.id.circle_des_info_ll).setVisibility(View.GONE);
//        }
        
        boolean isshowContactTitle = false;
        if(!TextUtils.isEmpty(mCompany.tel)) {
        	isshowContactTitle = true;
            mPhone_info.setVisibility(View.VISIBLE);
            mPhone_info.setOnClickListener(PhoneClickListener);
            
            TextView phone = (TextView) findFragmentViewById(R.id.phone_info);
            phone.setText(mCompany.tel);
        }else {
            mPhone_info.setVisibility(View.GONE);
        }
        
        if(!TextUtils.isEmpty(mCompany.email)) {
        	isshowContactTitle = true; 
            mEmail_info.setVisibility(View.VISIBLE);
            mEmail_info.setOnClickListener(emailClickListener);
            TextView email = (TextView) findFragmentViewById(R.id.email_info);
            email.setText(mCompany.email);
        }else {
            mEmail_info.setVisibility(View.GONE);
        }
        
        if(isshowContactTitle) {
        	findFragmentViewById(R.id.contact_info_title).setVisibility(View.VISIBLE);
        	findFragmentViewById(R.id.span_view3).setVisibility(View.VISIBLE);
        }else {
        	findFragmentViewById(R.id.contact_info_title).setVisibility(View.GONE);
        	findFragmentViewById(R.id.span_view3).setVisibility(View.GONE);
        }
    }
    
    private void refreshHeadUi() {
        mEvent_title.setText(mCompany.name);
        mTime.setText(mCompany.website);
        if(mCompany.id == 15000000016L) {
        	mCover.setImageResource(R.drawable.borqs_company_cover);
        }else if(StringUtil.isEmpty(mCompany.cover_url)) {
        	mCover.setImageResource(R.drawable.event_default_cover);
        }else {
        	setViewIcon(mCompany.cover_url, mCover);
        }
         if(!TextUtils.isEmpty(mCompany.large_logo_url)) {
        	setViewIcon(mCompany.large_logo_url, mProfile_img_ui);
        }
        
        mEvent_des.setSelected(true);
		if(StringUtil.isValidString(mCompany.description)) {
        	mEvent_des.setText(mCompany.description);
        }else {
        	mEvent_des.setVisibility(View.GONE);
        }
    }
    
    private void refreshMemberUi() {
        if(mCompany.person_count > 0) {
            findFragmentViewById(R.id.in_member_rl).setVisibility(View.VISIBLE);
            TextView in_tv = (TextView) findFragmentViewById(R.id.tv_in_member);
            in_tv.setText(getString(R.string.members_in_circle) + "(" + mCompany.person_count + ")");
            if(mCompany.memberList != null && mCompany.memberList.size() > 0) {
                mGrid_in_member.setAdapter(new GridPeopleSimpleAdapter(mActivity, mCompany.memberList));
            }
        }else {
            findFragmentViewById(R.id.in_member_rl).setVisibility(View.GONE);
        }
        
//        if( mCircle.applyedMembersCount > 0) {
//            findFragmentViewById(R.id.apply_member_rl).setVisibility(View.VISIBLE);
//            TextView apply_tv = (TextView) findFragmentViewById(R.id.tv_apply_member);
//            apply_tv.setText(getString(R.string.members_applyed_circle) + "(" + mCircle.applyedMembersCount + ")");
//            if(mCircle.applyedMembersList != null && mCircle.applyedMembersList.size() > 0) {
//                mGrid_applyed_member.setAdapter(new GridPeopleSimpleAdapter(mActivity, mCircle.applyedMembersList));
//            }
//        }else {
//            findFragmentViewById(R.id.apply_member_rl).setVisibility(View.GONE);
//        }
        
        if(mCompany.department_count > 0) {
            findFragmentViewById(R.id.invite_member_rl).setVisibility(View.VISIBLE);
            TextView invite_tv = (TextView) findFragmentViewById(R.id.tv_invite_member);
            invite_tv.setText((getString(R.string.department_in_company)) + "(" + mCompany.department_count + ")");
            if(mCompany.depList != null && mCompany.depList.size() > 0) {
                mGrid_invited_member.setAdapter(new GridPeopleSimpleAdapter(mActivity, mCompany.depList));
            }
        }else {
            findFragmentViewById(R.id.invite_member_rl).setVisibility(View.GONE);
        }
        
    }
    
    View.OnClickListener addressClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
                        Uri.parse("http://ditu.google.cn/maps?hl=zh&mrt=loc&q=" + mCompany.address));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                        & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); 
                
                intent.setClassName("com.google.android.apps.maps", 
                        "com.google.android.maps.MapsActivity"); 
                startActivity(intent);
            } catch(ActivityNotFoundException e) {
                Log.d(TAG, "error, no MapsActivity in system.");
            }
        }
    };
    
    View.OnClickListener PhoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String new_url = new StringBuilder("tel:").append(mCompany.tel).toString();
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(new_url));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };
    
    View.OnClickListener emailClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String new_url = new StringBuilder("mailto:").append(mCompany.email).toString();
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(new_url));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	if(mCompany != null) {
            outState.putLong(Company.COMPANY_ID, mCompany.id);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	QiupuHelper.unregisterUserListener(getClass().getName());
    	PickContactBaseFragment.unregisterUserListener(getClass().getName());
    }
    
	 private void setViewIcon(final String url, final ImageView view) {
			ImageRun imagerun = new ImageRun(null, url, 0);
//			imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
			imagerun.noimage = true;
			imagerun.addHostAndPath = true;
			imagerun.setRoundAngle=true;
			imagerun.setImageView(view);
			imagerun.post(null);
}
    
	View.OnClickListener membersClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
		    int status = -1;
		    String title = "";
		    if(v.getId() == R.id.in_member_rl) {
		        status = PublicCircleRequestUser.STATUS_IN_CIRCLE;
		        title = getString(R.string.members_in_circle);
		        UserCircle circle = new UserCircle();
		        circle.circleid = mCompany.department_id;
		        IntentUtil.startPublicCirclePeopleActivity(mActivity, circle, status, title);
//		    }else if(v.getId() == R.id.apply_member_rl) {
//		        status = PublicCircleRequestUser.STATUS_APPLY;
//		        title = getString(R.string.members_applyed_circle);
		    }else if(v.getId() == R.id.invite_member_rl) {
//		        status = PublicCircleRequestUser.STATUS_INVITE;
//		        title = getString(R.string.members_invited_circle);
		    	Intent intent = new Intent(mActivity,CompanyDepListActivity.class);
	              intent.putExtra(Company.COMPANY_ID, String.valueOf(mCompany.id));
	              startActivity(intent);
		    }
		}
	};

	public interface CompanyDetailFragmentListenerCallBack {
		public void getCompanyDetailInfoFragment(CompanyDetailFragment fragment);
		public Company getCompanyDetailInfo();
	}

	@Override
	public void updateItemUI(QiupuUser user) {
		mCompany = orm.queryCompany(mActivity, mCompany.id);
		refreshCircleInfoUi();
	}

	@Override
	public void addFriends(QiupuUser user) {
	}

	@Override
	public void refuseUser(long uid) {
	}

	@Override
	public void deleteUser(QiupuUser user) {
	}

	@Override
	public void sendRequest(QiupuUser user) {
	}
	
	OnItemClickListener shareSourceItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(ProfileSourceItemView.class.isInstance(view)) {
				ProfileSourceItemView itemview = (ProfileSourceItemView) view;
				ShareSourceItem item = itemview.getItem();
				Intent intent = new Intent(mActivity, ShareResourcesActivity.class);
				intent.putExtra("userid", mCompany.department_id);
				intent.putExtra("sourcefilter", item.mType);
				intent.putExtra("title", ShareSourceItem.getSourceItemLabel(mActivity, item.mLabel, item.mType));
				mActivity.startActivity(intent);
			}
		}
	};
	
	public void refreshCompany(Company company) {
		mCompany = company;
		refreshCircleInfoUi();
	}
}
