package com.borqs.common.view;

import java.util.ArrayList;

import twitter4j.CircleGridData;
import twitter4j.Circletemplate;
import twitter4j.PublicCircleRequestUser;
import twitter4j.UserCircle;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.common.ShareSourceItem;
import com.borqs.common.adapter.ProfileShareSourceAdapter;
import com.borqs.common.util.IntentUtil;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.ui.bpc.ShareResourcesActivity;
import com.borqs.qiupu.ui.company.CompanyDepListActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;

public class PublicCircleLayoutView extends SNSItemView {

    private static final String TAG = "PublicCircleLayoutView";

    private Context mContext;
    private TextView tv_top_post;
	private TextView mMember_limit;
	private View mAddress_info;
	private View mPhone_info;
	private View mEmail_info;
	private TextView mPublic_circle_des;
	private MyGridView mGridview;
	
    private UserCircle mCircle;
    
//    private CircleGridView in_member;
    private CircleGridView apply_member;
    private CircleGridView invite_member;
//    private CircleGridView formal_circle;
//    private CircleGridView free_circle;
    
//    private View circle_poll_layout;
//    private TextView tv_poll_title;
//    private View view_more_poll;
//    private LinearLayout poll_container;
    
//    private View circle_event_layout;
//    private TextView tv_event_title;
//    private View view_more_event;
//    private LinearLayout event_container;
    
//    private  View album_view;
    
//    private TextView mInviteBtn;
//    private TextView mApplyBtn;
    
    private View mContentView;
    
    public static final int TYPE_TEXT  = 0;
    public static final int PHOTO_PICKED_WITH_DATA = 3021;
    public static final int CAMERA_WITH_DATA = 3023;

    public PublicCircleLayoutView(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }
    

    public PublicCircleLayoutView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		mContext = ctx;
	}

	public PublicCircleLayoutView(Context context, UserCircle circle) {
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

        // child 1
        mContentView = LayoutInflater.from(mContext).inflate(
                R.layout.public_circle_layout_view, null);
        mContentView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        		LayoutParams.WRAP_CONTENT));
        addView(mContentView);

    	mMember_limit = (TextView) mContentView.findViewById(R.id.member_limit);
    	mAddress_info = mContentView.findViewById(R.id.address_info_ll);
    	mPhone_info = mContentView.findViewById(R.id.phone_info_ll);
    	mEmail_info = mContentView.findViewById(R.id.email_info_ll);
    	mPublic_circle_des = (TextView) mContentView.findViewById(R.id.public_circle_des);
    	
//    	in_member = (CircleGridView) mContentView.findViewById(R.id.in_member);
    	apply_member = (CircleGridView) mContentView.findViewById(R.id.apply_member);
    	invite_member = (CircleGridView) mContentView.findViewById(R.id.invite_member);
//    	formal_circle = (CircleGridView) mContentView.findViewById(R.id.formal_circle);
//    	free_circle = (CircleGridView) mContentView.findViewById(R.id.free_circle);
    	
//    	album_view = mContentView.findViewById(R.id.album_view);
    	mGridview = (MyGridView) mContentView.findViewById(R.id.source_view);
    	
//    	poll_container = (LinearLayout) mContentView.findViewById(R.id.poll_container);
//    	tv_poll_title = (TextView) mContentView.findViewById(R.id.tv_poll_title);
//    	view_more_poll = mContentView.findViewById(R.id.view_more_poll);
//    	circle_poll_layout = mContentView.findViewById(R.id.circle_poll_layout);
//    	view_more_poll.setOnClickListener(pollCheckClickListener);
//    	
//    	event_container = (LinearLayout) mContentView.findViewById(R.id.event_container);
//        tv_event_title = (TextView) mContentView.findViewById(R.id.tv_event_title);
//        view_more_event = mContentView.findViewById(R.id.view_more_event);
//        circle_event_layout = mContentView.findViewById(R.id.circle_event_layout);
//        view_more_event.setOnClickListener(eventCheckClickListener);
        
        tv_top_post = (TextView) mContentView.findViewById(R.id.tv_top_post);
        mPublic_circle_des = (TextView) mContentView.findViewById(R.id.public_circle_des);

        if(mCircle != null && mCircle.mGroup != null && mCircle.mGroup.viewer_can_update) {
//            findFragmentViewById(R.id.icon_camera).setVisibility(View.VISIBLE);
//            mProfile_img_ui.setOnClickListener(editProfileImageListener);
        }

        setUI();
    }
    
    
    private void refreshCircleInfoUi() {
    	if(mCircle != null) {
    		mCircle.uid = mCircle.circleid;
//    	    refreshActionBtn();
    	    refreshMemberUi();
    		refreshOtherInfoUi();
    		refreshShareSourceResult();
    		refreshTopPostInfo();
//    		refreshResourceView();
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
        
        if(StringUtil.isValidString(mCircle.description)) {
        	mPublic_circle_des.setVisibility(View.VISIBLE);
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
            mMember_limit.setText(String.format(mContext.getString(R.string.circle_profile_member_limit), String.valueOf(mCircle.mGroup.member_limit)));
        }
    }
    
    private void refreshMemberUi() {
//        in_member.updateDataInfo(
//        		new CircleGridData(mCircle.memberCount, 
//        				mContext.getString(R.string.members_in_circle), mCircle.inMembersImageList, membersClickListener));
        apply_member.updateDataInfo(
        		new CircleGridData(mCircle.applyedMembersCount, 
        				mContext.getString(R.string.members_applyed_circle), mCircle.applyedMembersList, applyClickListener));
        invite_member.updateDataInfo(
        		new CircleGridData(mCircle.invitedMembersCount, 
        				mContext.getString(R.string.members_invited_circle), mCircle.invitedMembersList, inviteClickListener));
//        free_circle.updateDataInfo(
//        		new CircleGridData(mCircle.freeCirclesCount, 
//        				mContext.getString(R.string.interested_circle_label), mCircle.freeCirclesList, freeCirclesClickListener));
//        String name;
//        if(mCircle.mGroup == null) {
//        	name = mContext.getString(R.string.user_circles);
//        }else if(Circletemplate.SUBTYPE_TEMPLATEFORMALCOMPANY.equals(mCircle.mGroup.subtype)) {
//        	name = mContext.getString(R.string.child_circle_label_department);
//        }else if(Circletemplate.SUBTYPE_TEMPLATEFORMALSCHOOL.equals(mCircle.mGroup.subtype)) {
//        	name = mContext.getString(R.string.child_circle_label_class);
//        }else {
//        	name = mContext.getString(R.string.user_circles);
//        }
//        formal_circle.updateDataInfo(new CircleGridData(mCircle.formalCirclesCount,name,mCircle.formalCirclesList, formalCirclesClickListener));
        
        
//        if(mCircle.mGroup != null && mCircle.mGroup.top_poll_count > 0) {
//        	circle_poll_layout.setVisibility(View.VISIBLE);
//        	tv_poll_title.setText(mContext.getString(R.string.poll_title) + "(" + mCircle.mGroup.top_poll_count + ")");
//        	
//        	if(mCircle.simplePoll != null && mCircle.simplePoll.size()>0) {
//        		poll_container.removeAllViews();
//        		for(PollInfo poll :mCircle.simplePoll) {
//        			poll_container.addView(new PollItemView(mContext, poll, true));	
//        		}
//        	}
//        }else {
//        	circle_poll_layout.setVisibility(View.GONE);
//        }
        
//        if(mCircle.mGroup != null && mCircle.mGroup.event_count > 0) {
//        	circle_event_layout.setVisibility(View.VISIBLE);
//        	tv_event_title.setText(mContext.getString(R.string.event) + "(" + mCircle.mGroup.event_count + ")");
//        	
//        	if(mCircle.simpleEventList != null && mCircle.simpleEventList.size()>0) {
//        		event_container.removeAllViews();
//        		for(UserCircle event :mCircle.simpleEventList) {
//        			event_container.addView(new SimpleEventItemView(mContext, event));
//        		}
//        	}
//        }else {
//        	circle_event_layout.setVisibility(View.GONE);
//        }
    }
    
//    private void refreshResourceView() {
//    	album_view.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				IntentUtil.startAlbumIntent(mContext, mCircle.circleid,mCircle.name);
//			}
//		});
//    	
//		int width = getResources().getDisplayMetrics().widthPixels;
//		ViewGroup.LayoutParams params = album_view.getLayoutParams();
//		params.width = (width/5);
//		params.height = params.width;
//		album_view.invalidate();
//    }
    
    View.OnClickListener membersClickListener = new View.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		int status = -1;
    		String title = "";
    		status = PublicCircleRequestUser.STATUS_IN_CIRCLE;
    		title = mContext.getString(R.string.members_in_circle);
    		
    		IntentUtil.startPublicCirclePeopleActivity(mContext, mCircle, status, title);
    	}
    };
    View.OnClickListener applyClickListener = new View.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		int status = -1;
    		String title = "";
    		status = PublicCircleRequestUser.STATUS_APPLY;
    		title = mContext.getString(R.string.members_applyed_circle);
    		IntentUtil.startPublicCirclePeopleActivity(mContext, mCircle, status, title);
    	}
    };
    View.OnClickListener inviteClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
		    int status = -1;
		    String title = "";
		    status = PublicCircleRequestUser.STATUS_INVITE;
		    title = mContext.getString(R.string.members_invited_circle);
			IntentUtil.startPublicCirclePeopleActivity(mContext, mCircle, status, title);
		}
	};
    
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
                mContext.startActivity(intent);
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
            mContext.startActivity(intent);
        }
    };
    
    View.OnClickListener emailClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String new_url = new StringBuilder("mailto:").append(mCircle.emailList.get(0).info).toString();
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(new_url));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    };
    
    
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
    
//	View.OnClickListener membersClickListener = new View.OnClickListener() {
//		@Override
//		public void onClick(View v) {
//		    int status = -1;
//		    String title = "";
//		    if(v.getId() == R.id.in_member_rl) {
//		        status = PublicCircleRequestUser.STATUS_IN_CIRCLE;
//		        title = mContext.getString(R.string.members_in_circle);
//		    }else if(v.getId() == R.id.apply_member_rl) {
//		        status = PublicCircleRequestUser.STATUS_APPLY;
//		        title = mContext.getString(R.string.members_applyed_circle);
//		    }else if(v.getId() == R.id.invite_member_rl) {
//		        status = PublicCircleRequestUser.STATUS_INVITE;
//		        title = mContext.getString(R.string.members_invited_circle);
//		    }
//		    
//			IntentUtil.startPublicCirclePeopleActivity(mContext, mCircle, status, title);
//		}
//	};
	
    private void gotoGetChildCircle(int formal) {
    	Intent intent = new Intent(mContext,CompanyDepListActivity.class);
    	intent.putExtra(CircleUtils.CIRCLE_ID, mCircle.circleid);
    	intent.putExtra(CompanyDepListActivity.FORMAL_TAG, formal);
    	if(formal == UserCircle.circle_sub_formal) {
    		if(Circletemplate.SUBTYPE_TEMPLATEFORMALCOMPANY.equals(mCircle.mGroup.subtype)) {
    			intent.putExtra(CompanyDepListActivity.TITLE_TAG, mContext.getString(R.string.department_list));
    		}else if(Circletemplate.SUBTYPE_TEMPLATEFORMALSCHOOL.equals(mCircle.mGroup.subtype)) {
    			intent.putExtra("title", mContext.getString(R.string.class_list));
    		}
    	}
    	mContext.startActivity(intent);
    }
    View.OnClickListener formalCirclesClickListener = new View.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		gotoGetChildCircle(UserCircle.circle_sub_formal);
    	}
    };
    
	View.OnClickListener freeCirclesClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			gotoGetChildCircle(UserCircle.circle_free);
		}
	};
	
    private void refreshShareSourceResult() {
		ArrayList<ShareSourceItem> dataList = new ArrayList<ShareSourceItem>();
		dataList.addAll(mCircle.sharedResource);
		if(mGridview != null) {
			ProfileShareSourceAdapter shareAdapter = new ProfileShareSourceAdapter(mContext, mCircle);
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
				Intent intent = new Intent(mContext, ShareResourcesActivity.class);
				intent.putExtra("userid", mCircle.circleid);
				intent.putExtra("sourcefilter", item.mType);
				intent.putExtra("title", ShareSourceItem.getSourceItemLabel(mContext, item.mLabel, item.mType));
				mContext.startActivity(intent);
			}
		}
	};
	
	public void refreshCircle(UserCircle circle) {
		mCircle = circle;
		refreshCircleInfoUi();
	}


    private void setUI() {
        if (mCircle != null) {
        	mGridview.setOnItemClickListener(shareSourceItemClickListener);
            refreshCircleInfoUi();
        }
    }
    
    private View findFragmentViewById(int id) {
        if(mContentView != null) {
            return mContentView.findViewById(id);
        }
        return null;
    }

	public UserCircle getCircleInfo() {
		return mCircle;
	}

	public void setCircleInfo(UserCircle circle) {
		mCircle = circle;
		setUI();
	}

	protected void setViewIcon(final String url, final ImageView view, boolean isIcon) {
		ImageRun imagerun = new ImageRun(null, url, 0);
		imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
		imagerun.width = getResources().getDisplayMetrics().widthPixels;
		imagerun.height = imagerun.width;
		imagerun.noimage = true;
		imagerun.addHostAndPath = true;
		if(isIcon)
			imagerun.setRoundAngle=true;
		imagerun.setImageView(view);
		imagerun.post(null);
	}

	private void refreshTopPostInfo() {
      if(mCircle != null && mCircle.mGroup != null && mCircle.mGroup.top_post_count > 0) {
      	tv_top_post.setVisibility(View.VISIBLE);
//          span_view.setVisibility(View.VISIBLE);
          if(TextUtils.isEmpty(mCircle.mGroup.top_post_name)) {
              tv_top_post.setText(R.string.view_top);
          }else {
              tv_top_post.setText(mCircle.mGroup.top_post_name);
          }
          tv_top_post.setOnClickListener(new View.OnClickListener() {
              
              @Override
              public void onClick(View v) {
                  IntentUtil.startTopPostIntent(mContext, mCircle.circleid,mCircle.mGroup.top_post_name,
                          mCircle.mGroup.viewer_can_update);
                  
              }
          });
      }else {
      	tv_top_post.setVisibility(View.GONE);
      }
  }
	
//	View.OnClickListener pollCheckClickListener = new View.OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			if(mCircle != null) {
//				IntentUtil.startPollIntent(mContext, mCircle.circleid, mCircle.name, 1, true);
//			}else {
//				Log.d(TAG, "circle info is null");
//			}
//		}
//	};
	
//	View.OnClickListener eventCheckClickListener = new View.OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			if(mCircle != null) {
//				IntentUtil.startCircleEventList(mContext, mCircle.circleid, mCircle.name);
//			}else {
//				Log.d(TAG, "circle info is null");
//			}
//		}
//	};
	
}
