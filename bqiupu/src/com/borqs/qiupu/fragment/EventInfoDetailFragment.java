package com.borqs.qiupu.fragment;

import java.util.ArrayList;

import twitter4j.AsyncQiupu;
import twitter4j.PublicCircleRequestUser;
import twitter4j.QiupuUser;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
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
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.MyGridView;
import com.borqs.common.view.ProfileSourceItemView;
import com.borqs.common.view.SNSItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.bpc.ShareResourcesActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.DateUtil;
import com.borqs.qiupu.util.StringUtil;

public class EventInfoDetailFragment extends BasicFragment implements UsersActionListner {
	private final static String TAG = "EventInfoDetailFragment";
	private Activity mActivity; 
	
	private QiupuORM orm;
	private AsyncQiupu asyncQiupu;
	private EventDetailFragmentListenerCallBack mCallBackListener;
	private TextView mEvent_title;
	private View mAddress_info;
	private View mPhone_info;
	private View mEmail_info;
	private TextView mEvent_des;
	private ImageView mProfile_img_ui;
	
    private UserCircle mCircle;
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

        if (mActivity instanceof EventDetailFragmentListenerCallBack) {
        	mCallBackListener = (EventDetailFragmentListenerCallBack)activity;
        	mCallBackListener.getEventDetailInfoFragment(this);
        	mCircle = mCallBackListener.getCircleInfo();
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
    	
    	mContentView = inflater.inflate(R.layout.event_info_view, container, false);
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
        
        View head_rl = mContentView.findViewById(R.id.head_rl);
        head_rl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				final View bodyInfo = findFragmentViewById(R.id.body_ll);
//				if(bodyInfo.getVisibility() == View.VISIBLE) {
//					bodyInfo.setVisibility(View.GONE);
//				}else {
//					bodyInfo.setVisibility(View.VISIBLE);
//				}
			}
		});
        
        if(mCircle != null && mCircle.mGroup != null && mCircle.mGroup.viewer_can_update) {
//            findFragmentViewById(R.id.icon_camera).setVisibility(View.VISIBLE);
//            mProfile_img_ui.setOnClickListener(editProfileImageListener);
        }
    	return mContentView;
    }
    
    private void parserSavedState(Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            if(mCircle == null) {
                mCircle = new UserCircle();
            }
            
            mCircle.circleid = savedInstanceState.getLong(CircleUtils.CIRCLE_ID);
            mCircle.uid = savedInstanceState.getLong(CircleUtils.CIRCLE_ID);
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
    	if(mCircle != null && isDetached() == false && getActivity() != null) {
    		mCircle.uid = mCircle.circleid;
    	    refreshHeadUi();
    	    refreshMemberUi();
    		refreshOtherInfoUi();
    		refreshShareSourceResult();
		}
    }
    
    private void refreshShareSourceResult() {
		ArrayList<ShareSourceItem> dataList = new ArrayList<ShareSourceItem>();
		dataList.addAll(mCircle.sharedResource);
		if(mGridview != null) {
			ProfileShareSourceAdapter shareAdapter = new ProfileShareSourceAdapter(mActivity, mCircle);
			mGridview.setAdapter(shareAdapter);
			shareAdapter.alterDataList(dataList);
		}
	}
    
    private void refreshOtherInfoUi() {
        if(StringUtil.isValidString(mCircle.location)) {
        	findFragmentViewById(R.id.address_info_title).setVisibility(View.VISIBLE);
        	findFragmentViewById(R.id.span_view2).setVisibility(View.VISIBLE);
            mAddress_info.setVisibility(View.VISIBLE);
            mAddress_info.setOnClickListener(addressClickListener);
            TextView address = (TextView) findFragmentViewById(R.id.address_info);
            address.setText(mCircle.location);
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
        if(mCircle.phoneList != null && mCircle.phoneList.size() > 0) {
        	isshowContactTitle = true;
            mPhone_info.setVisibility(View.VISIBLE);
            mPhone_info.setOnClickListener(PhoneClickListener);
            
            TextView phone = (TextView) findFragmentViewById(R.id.phone_info);
            phone.setText(mCircle.phoneList.get(0).info);
        }else {
            mPhone_info.setVisibility(View.GONE);
        }
        
        if(mCircle.emailList != null && mCircle.emailList.size() > 0) {
        	isshowContactTitle = true; 
            mEmail_info.setVisibility(View.VISIBLE);
            mEmail_info.setOnClickListener(emailClickListener);
            TextView email = (TextView) findFragmentViewById(R.id.email_info);
            email.setText(mCircle.emailList.get(0).info);
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
        mEvent_title.setText(mCircle.name);
        if(mCircle.mGroup != null) {
			if(mCircle.circleid == 14000000000L) {
				mCover.setImageResource(R.drawable.borqs_cover);
			}else if(StringUtil.isEmpty(mCircle.mGroup.coverUrl)) {
				mCover.setImageResource(R.drawable.event_default_cover);
			}else {
				setViewIcon(mCircle.mGroup.coverUrl, mCover, false);
			}
			StringBuilder timeStr = new StringBuilder();
			timeStr.append(DateUtil.converToRelativeTime(mActivity,
					mCircle.mGroup.startTime));
			if(mCircle.mGroup.endTime > 0) {
				timeStr.append(" ~ ");
				timeStr.append(DateUtil.converToRelativeTime(mActivity,
	    					mCircle.mGroup.endTime));
			}
			mTime.setText(timeStr.toString());
			
			mEvent_des.setSelected(true);
			if(StringUtil.isValidString(mCircle.mGroup.bulletin)) {
				SNSItemView.attachHtml(mEvent_des, mCircle.mGroup.bulletin);
            }else if(StringUtil.isValidString(mCircle.description)){
            	SNSItemView.attachHtml(mEvent_des, mCircle.description);
            }else {
            	mEvent_des.setVisibility(View.GONE);
            }
			
			if(UserCircle.isPrivacyOpen(mCircle.mGroup)) {
				mPrivacyTextView.setText(R.string.privacy_set_open);
		    }else if(UserCircle.isPrivacyClosed(mCircle.mGroup)) { 
		    	mPrivacyTextView.setText(R.string.privacy_set_closed);
		    }else if(UserCircle.isPrivacySecret(mCircle.mGroup)) {
		    	mPrivacyTextView.setText(R.string.privacy_set_secret);
		    }
			if(mCircle.mGroup.creator != null) {
				setViewIcon(mCircle.mGroup.creator.profile_image_url, mProfile_img_ui, true);
			}
		}
    }
    
    private void refreshMemberUi() {
        if(mCircle.memberCount > 0) {
            findFragmentViewById(R.id.in_member_rl).setVisibility(View.VISIBLE);
            TextView in_tv = (TextView) findFragmentViewById(R.id.tv_in_member);
            in_tv.setText(getString(R.string.event_in_member_title) + "(" + mCircle.memberCount + ")");
            if(mCircle.inMembersImageList != null && mCircle.inMembersImageList.size() > 0) {
                mGrid_in_member.setAdapter(new GridPeopleSimpleAdapter(mActivity, mCircle.inMembersImageList));
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
        
        if( mCircle.invitedMembersCount > 0) {
            findFragmentViewById(R.id.invite_member_rl).setVisibility(View.VISIBLE);
            TextView invite_tv = (TextView) findFragmentViewById(R.id.tv_invite_member);
            invite_tv.setText(getString(R.string.event_waiting_title) + "(" + mCircle.invitedMembersCount + ")");
            if(mCircle.invitedMembersList != null && mCircle.invitedMembersList.size() > 0) {
                mGrid_invited_member.setAdapter(new GridPeopleSimpleAdapter(mActivity, mCircle.invitedMembersList));
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
                        Uri.parse("http://ditu.google.cn/maps?hl=zh&mrt=loc&q=" + mCircle.location));
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
            String new_url = new StringBuilder("tel:").append(mCircle.phoneList.get(0).info).toString();
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(new_url));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };
    
    View.OnClickListener emailClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String new_url = new StringBuilder("mailto:").append(mCircle.emailList.get(0).info).toString();
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(new_url));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mCircle != null) {
            outState.putLong(CircleUtils.CIRCLE_ID, mCircle.circleid);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	QiupuHelper.unregisterUserListener(getClass().getName());
    	PickContactBaseFragment.unregisterUserListener(getClass().getName());
    }
    
	View.OnClickListener membersClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
		    int status = -1;
		    String title = "";
		    if(v.getId() == R.id.in_member_rl) {
		        status = PublicCircleRequestUser.STATUS_IN_CIRCLE;
		        title = getString(R.string.members_in_circle);
		    }else if(v.getId() == R.id.apply_member_rl) {
		        status = PublicCircleRequestUser.STATUS_APPLY;
		        title = getString(R.string.members_applyed_circle);
		    }else if(v.getId() == R.id.invite_member_rl) {
		        status = PublicCircleRequestUser.STATUS_INVITE;
		        title = getString(R.string.members_invited_circle);
		    }
		    
			IntentUtil.startPublicCirclePeopleActivity(mActivity, mCircle, status, title);
		}
	};

	public interface EventDetailFragmentListenerCallBack {
		public void getEventDetailInfoFragment(EventInfoDetailFragment fragment);
		public UserCircle getCircleInfo();
	}

	@Override
	public void updateItemUI(QiupuUser user) {
		mCircle = orm.queryOneCircleWithGroup(mCircle.circleid);
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
				intent.putExtra("userid", mCircle.circleid);
				intent.putExtra("sourcefilter", item.mType);
				intent.putExtra("title", ShareSourceItem.getSourceItemLabel(mActivity, item.mLabel, item.mType));
				mActivity.startActivity(intent);
			}
		}
	};
	
	public void refreshEvent(UserCircle event) {
		mCircle = event;
		refreshCircleInfoUi();
	}
}
