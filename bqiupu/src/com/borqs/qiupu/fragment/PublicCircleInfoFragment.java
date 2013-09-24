package com.borqs.qiupu.fragment;

import java.util.ArrayList;

import twitter4j.AsyncQiupu;
import twitter4j.Circletemplate;
import twitter4j.PublicCircleRequestUser;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.common.ShareSourceItem;
import com.borqs.common.adapter.GridPeopleSimpleAdapter;
import com.borqs.common.adapter.ProfileShareSourceAdapter;
import com.borqs.common.listener.RefreshCircleListener;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.MyGridView;
import com.borqs.common.view.ProfileSourceItemView;
import com.borqs.common.view.SNSItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.bpc.ShareResourcesActivity;
import com.borqs.qiupu.ui.company.CompanyDepListActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.DateUtil;
import com.borqs.qiupu.util.StringUtil;

public class PublicCircleInfoFragment extends BasicFragment implements RefreshCircleListener {
	private final static String TAG = "PublicCircleInfoFragment";
	private Activity mActivity; 
	
	private Handler mHandler;
	private QiupuORM orm;
	private AsyncQiupu asyncQiupu;
	private CircleInfoFragmentListenerCallBack mCallBackListener;
    private TextView mCircle_Name;
//    private TextView tv_top_post;
//	private View layout_top_post;
	private TextView mMember_limit;
	private View mAddress_info;
	private View mPhone_info;
	private View mEmail_info;
	private TextView mPublic_circle_des;
	private TextView mProfile_user_status;
	private TextView mLast_status_time;
	private ImageView mProfile_img_ui;
	private MyGridView mGridview;
	private ImageView mSelect_cover;
	
    private UserCircle mCircle;
    private static final String RESULT = "result";
    private static final String ERRORMSG = "errormsg";
    private ProgressDialog mprogressDialog;
    private GridView mGrid_in_member;
    private GridView mGrid_applyed_member;
    private GridView mGrid_invited_member;
    private GridView mGrid_Child_Circles;
    
//    private TextView mInviteBtn;
//    private TextView mApplyBtn;
    
    private View mContentView;
    
    public static final int TYPE_TEXT  = 0;
    public static final int PHOTO_PICKED_WITH_DATA = 3021;
    public static final int CAMERA_WITH_DATA = 3023;
    
    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
    	super.onAttach(activity);
    	mActivity = activity;

        if (mActivity instanceof CircleInfoFragmentListenerCallBack) {
        	mCallBackListener = (CircleInfoFragmentListenerCallBack)activity;
        	mCallBackListener.getCircleInfoFragment(this);
        	mCircle = mCallBackListener.getCircleInfo();
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
    	super.onCreate(savedInstanceState);
    	parserSavedState(savedInstanceState);
    	QiupuHelper.registerRefreshCircleListener(getClass().getName(), this);
    	orm = QiupuORM.getInstance(mActivity);
		asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
		mHandler = new MainHandler();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView");
    	
    	mContentView = inflater.inflate(R.layout.public_circle_info, container, false);
    	mCircle_Name = (TextView) mContentView.findViewById(R.id.public_cricle_name);
//    	tv_top_post = (TextView) mContentView.findViewById(R.id.tv_top_post);
//    	layout_top_post = mContentView.findViewById(R.id.layout_top_post);
    	mMember_limit = (TextView) mContentView.findViewById(R.id.member_limit);
    	mAddress_info = mContentView.findViewById(R.id.address_info_ll);
    	mPhone_info = mContentView.findViewById(R.id.phone_info_ll);
    	mEmail_info = mContentView.findViewById(R.id.email_info_ll);
    	mPublic_circle_des = (TextView) mContentView.findViewById(R.id.public_circle_des);
    	mProfile_img_ui =  (ImageView) mContentView.findViewById(R.id.public_circle_img_ui);
    	mProfile_user_status = (TextView) mContentView.findViewById(R.id.public_cricle_last_status);
    	mLast_status_time = (TextView) mContentView.findViewById(R.id.last_status_time);
    	mGrid_in_member = (GridView) mContentView.findViewById(R.id.grid_in_member);
    	mGrid_applyed_member = (GridView) mContentView.findViewById(R.id.grid_apply_member);
    	mGrid_invited_member = (GridView) mContentView.findViewById(R.id.grid_invite_member);
    	mGrid_Child_Circles = (GridView) mContentView.findViewById(R.id.grid_child_circle);
    	mGridview = (MyGridView) mContentView.findViewById(R.id.source_view);
    	mSelect_cover = (ImageView) mContentView.findViewById(R.id.select_cover);
    	
//    	mInviteBtn = (TextView) mContentView.findViewById(R.id.invite_btn);
//    	mInviteBtn.setOnClickListener(invitePeopleClickListener);
//    	
//    	mApplyBtn = (TextView) mContentView.findViewById(R.id.apply_btn);
//    	mApplyBtn.setOnClickListener(applyPeopleClickListener);
    	
    	View inMemberView = mContentView.findViewById(R.id.in_member_rl);
    	inMemberView.setOnClickListener(membersClickListener);
    	
    	View applyMemberView = mContentView.findViewById(R.id.apply_member_rl);
    	applyMemberView.setOnClickListener(membersClickListener);
        
        View inviteMemberView = mContentView.findViewById(R.id.invite_member_rl);
        inviteMemberView.setOnClickListener(membersClickListener);
        
        View childCircleView =  mContentView.findViewById(R.id.child_circle_rl);
        childCircleView.setOnClickListener(childCirclesClickListener);
        
//         mEdit_public_circle_tv = (TextView) mContentView.findViewById(R.id.edit_public_circle_tv);
//        if(mCircle != null) {
//            refreshEditCircleUI();
//        }
//        mEdit_public_circle_tv.setOnClickListener(editPublicCircleListener);
        
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
//    	    refreshActionBtn();
    	    refreshMemberUi();
    		refreshOtherInfoUi();
    		refreshEditCircleUI();
    		refreshShareSourceResult();
        	if(mCallBackListener != null) {
//				mCallBackListener.changeHeadTitle(mCircle);
			}
		}
    }
    
    private void refreshEditCircleUI() {
        if(mCircle.mGroup != null) {
//            if(mCircle.mGroup.viewer_can_update) {
//                mEdit_public_circle_tv.setVisibility(View.VISIBLE);
//            }else {
//                mEdit_public_circle_tv.setVisibility(View.GONE);
//            }
        }
    }
    
//    private void refreshActionBtn() {
//        if(mCircle.mGroup != null) {
//            if(PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
//            	if(PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group) ||
//            			PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group)) {
//            		mInviteBtn.setVisibility(View.VISIBLE);
//                    mApplyBtn.setVisibility(View.GONE);
//            	}else {
//            		if(UserCircle.VALUE_ALLOWED == mCircle.mGroup.can_member_invite) {
//                        mInviteBtn.setVisibility(View.VISIBLE);
//                        mApplyBtn.setVisibility(View.GONE);
//                    }else {
//                    	mInviteBtn.setVisibility(View.GONE);
//                        mApplyBtn.setVisibility(View.GONE);
//                    }	
//            	}
//            }else {
//            	if(UserCircle.JOIN_PERMISSION_DERECT_ADD == mCircle.mGroup.can_join
//            			|| UserCircle.JOIN_PREMISSION_VERIFY == mCircle.mGroup.can_join) { 
//            		mApplyBtn.setVisibility(View.VISIBLE);
//                    mInviteBtn.setVisibility(View.GONE);
//            	}else {
//            		mApplyBtn.setVisibility(View.GONE);
//                    mInviteBtn.setVisibility(View.GONE);
//            	}
//            }
//        }
//    }
    
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
        
        if(StringUtil.isValidString(mCircle.description)) {
            findFragmentViewById(R.id.circle_des_info_ll).setVisibility(View.VISIBLE);
            SNSItemView.attachHtml(mPublic_circle_des, mCircle.description);
        }else {
            findFragmentViewById(R.id.circle_des_info_ll).setVisibility(View.GONE);
        }
        
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
 
        if(StringUtil.isValidString(mCircle.description)) {
        	SNSItemView.attachHtml(mPublic_circle_des, mCircle.description);
        }
        
        if(mCircle.mGroup != null) {
            mMember_limit.setText(String.format(getString(R.string.circle_profile_member_limit), String.valueOf(mCircle.mGroup.member_limit)));
            refreshProfileStatus(mCircle.mGroup.bulletin, DateUtil.converToRelativeTime(getActivity(), mCircle.mGroup.bulletin_updated_time));
        }
    }
    
    private void refreshProfileStatus(final String status, final String time) {
        if(StringUtil.isValidString(status)) {
//            if(mCircle.mGroup.role_in_group == PublicCircleRequestUser.ROLE_TYPE_CREATER
//                    || mCircle.mGroup.role_in_group == PublicCircleRequestUser.ROLE_TYPE_CREATER) {
//                mProfile_user_status.setText(Html.fromHtml("<font color='#5f78ab'> "+ status + "</font>"));
//            }else {
        	SNSItemView.attachHtml(mProfile_user_status, status);
//                mProfile_user_status.setText(status);
//            }
            mLast_status_time.setText(time);
//        mProfile_user_status.setOnClickListener(updateStatusClickListener);
//        mLast_status_time.setOnClickListener(updateStatusClickListener);
        }
    }
    
    private void refreshHeadUi() {
        mCircle_Name.setText(mCircle.name);
        setViewIcon(mCircle.profile_image_url, mProfile_img_ui, true);
        if(mCircle.mGroup != null) {
			if(StringUtil.isEmpty(mCircle.mGroup.coverUrl)) {
				mSelect_cover.setImageResource(R.drawable.event_default_cover);
			}else {
				setViewIcon(mCircle.mGroup.coverUrl, mSelect_cover, false);
			}
		}
    }
    
    private void refreshMemberUi() {
        if(mCircle.memberCount > 0) {
            findFragmentViewById(R.id.in_member_rl).setVisibility(View.VISIBLE);
            TextView in_tv = (TextView) findFragmentViewById(R.id.tv_in_member);
            in_tv.setText(getString(R.string.members_in_circle) + "(" + mCircle.memberCount + ")");
            if(mCircle.inMembersImageList != null && mCircle.inMembersImageList.size() > 0) {
                mGrid_in_member.setAdapter(new GridPeopleSimpleAdapter(mActivity, mCircle.inMembersImageList));
            }
        }else {
            findFragmentViewById(R.id.in_member_rl).setVisibility(View.GONE);
        }
        
        if( mCircle.applyedMembersCount > 0) {
            findFragmentViewById(R.id.apply_member_rl).setVisibility(View.VISIBLE);
            TextView apply_tv = (TextView) findFragmentViewById(R.id.tv_apply_member);
            apply_tv.setText(getString(R.string.members_applyed_circle) + "(" + mCircle.applyedMembersCount + ")");
            if(mCircle.applyedMembersList != null && mCircle.applyedMembersList.size() > 0) {
                mGrid_applyed_member.setAdapter(new GridPeopleSimpleAdapter(mActivity, mCircle.applyedMembersList));
            }
        }else {
            findFragmentViewById(R.id.apply_member_rl).setVisibility(View.GONE);
        }
        
        if( mCircle.invitedMembersCount > 0) {
            findFragmentViewById(R.id.invite_member_rl).setVisibility(View.VISIBLE);
            TextView invite_tv = (TextView) findFragmentViewById(R.id.tv_invite_member);
            invite_tv.setText(getString(R.string.members_invited_circle) + "(" + mCircle.invitedMembersCount + ")");
            if(mCircle.invitedMembersList != null && mCircle.invitedMembersList.size() > 0) {
                mGrid_invited_member.setAdapter(new GridPeopleSimpleAdapter(mActivity, mCircle.invitedMembersList));
            }
        }else {
            findFragmentViewById(R.id.invite_member_rl).setVisibility(View.GONE);
        }
        
        if( mCircle.formalCirclesCount > 0) {
            findFragmentViewById(R.id.child_circle_rl).setVisibility(View.VISIBLE);
            TextView invite_tv = (TextView) findFragmentViewById(R.id.tv_child_circle);
            if(Circletemplate.SUBTYPE_TEMPLATEFORMALCOMPANY.equals(mCircle.mGroup.subtype)) {
            	invite_tv.setText(R.string.child_circle_label_department);
            }else if(Circletemplate.SUBTYPE_TEMPLATEFORMALSCHOOL.equals(mCircle.mGroup.subtype)) {
            	invite_tv.setText(R.string.child_circle_label_class);
            }else {
            	invite_tv.setText(R.string.user_circles);
            }
//            if(mCircle.invitedMembersList != null && mCircle.invitedMembersList.size() > 0) {
            mGrid_Child_Circles.setAdapter(new GridPeopleSimpleAdapter(mActivity, mCircle.formalCirclesList));
//            }
        }else {
            findFragmentViewById(R.id.child_circle_rl).setVisibility(View.GONE);
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
    	QiupuHelper.unregisterRefreshCircleListener(getClass().getName());
    	PickContactBaseFragment.unregisterUserListener(getClass().getName());
    }
    
	private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 0: {
//                updateStatus();
                break;
            }
            }
        }
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
	
	View.OnClickListener childCirclesClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(StringUtil.isValidString(mCircle.mGroup.child_circleids)) {
				Intent intent = new Intent(mActivity,CompanyDepListActivity.class);
				intent.putExtra(CircleUtils.CIRCLE_ID, mCircle.mGroup.child_circleids);
				if(Circletemplate.SUBTYPE_TEMPLATEFORMALCOMPANY.equals(mCircle.mGroup.subtype)) {
					intent.putExtra("title", getString(R.string.department_list));
				}else if(Circletemplate.SUBTYPE_TEMPLATEFORMALSCHOOL.equals(mCircle.mGroup.subtype)) {
					intent.putExtra("title", getString(R.string.class_list));
				}
				startActivity(intent);
			}else {
				Log.d(TAG, "have no child circles ");
			}
		}
	};
	
	public interface CircleInfoFragmentListenerCallBack {
		public void getCircleInfoFragment(PublicCircleInfoFragment fragment);
		public UserCircle getCircleInfo();
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
	
	private void approvedPeopleCallback(final long uid) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if(mCircle.applyedMembersList != null && mCircle.applyedMembersList.size() > 0) {
					for(int i=0; i<mCircle.applyedMembersList.size(); i++) {
						if(uid == mCircle.applyedMembersList.get(i).user_id) {
							mCircle.applyedMembersList.remove(i);
							mCircle.applyedMembersCount --;
						}
					}
				}
			}
		});
	}
	
	public void refreshCircle(UserCircle circle) {
		mCircle = circle;
		refreshCircleInfoUi();
	}

	@Override
	public void refreshUi() {
		mCircle = orm.queryOneCircleWithGroup(mCircle.circleid);
		refreshCircleInfoUi();
	}
}
