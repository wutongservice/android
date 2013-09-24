package com.borqs.common.view;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import twitter4j.Employee;
import twitter4j.PublicCircleRequestUser;
import twitter4j.UserCircle;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.listener.publicCirclePeopleActionListener;
import com.borqs.common.quickaction.QuickEmployeeActivity;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.ui.bpc.BpcSearchActivity;

public class TopSubMemberItemView extends SNSItemView 
{
	private final String TAG="TopSubMemberItemView";
	
	private ImageView usericon;	
	private TextView  username;
	private TextView mSecondContent;
	private TextView mRole;
	private ImageView mGrant_member;
	private View mContentView;
	private HashMap<String, publicCirclePeopleActionListener> mActionLisenterMap;
	
	private Employee mUser;
	private UserCircle mCircle;
	
	public TopSubMemberItemView(Context context, Employee di, UserCircle circle) {
		super(context);
		mContext = context;
		mUser = di;
		mCircle = circle;
		init();
	} 
    
	@Override
	protected void onFinishInflate()  {	
		super.onFinishInflate();		
		init();
	}
	
	public String getName() {
		return mUser.name;
	}	
	
	public Employee getUser() {
	    return mUser;
	}
	
	private void init()  {
		Log.d(TAG,  "call init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();

		//child 1
		mContentView  = factory.inflate(R.layout.public_circle_people_item, null);		
		mContentView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		addView(mContentView);
		
		username   = (TextView)mContentView.findViewById(R.id.user_name);
		usericon   = (ImageView)mContentView.findViewById(R.id.user_icon);
		mSecondContent = (TextView) mContentView.findViewById(R.id.second_content);
		mRole = (TextView) mContentView.findViewById(R.id.role_text);
		mGrant_member = (ImageView) mContentView.findViewById(R.id.grant_member);
		
		if (usericon != null) {
		    usericon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    Intent intent = new Intent(mContext, QuickEmployeeActivity.class);
                    intent.putExtra("Employee", mUser);
                    mContext.startActivity(intent);
                }
            });
        }
		setUI();	
	}
	
	private void setUI()
	{
		username.setText(mUser.name);
		if(mUser.role_in_group == PublicCircleRequestUser.ROLE_TYPE_CREATER) {
			mRole.setVisibility(View.VISIBLE);
			mRole.setText(R.string.public_circle_role_creator);
			mContentView.findViewById(R.id.in_member_action).setVisibility(View.GONE);
			mContentView.findViewById(R.id.apply_member_action).setVisibility(View.GONE);
			mContentView.findViewById(R.id.invite_member_action).setVisibility(View.GONE);
		}else if(PublicCircleRequestUser.ROLE_TYPE_MANAGER == mUser.role_in_group) {
			mRole.setVisibility(View.VISIBLE);
			mRole.setText(R.string.public_circle_role_manager);
			if(mCircle.mGroup != null && (mCircle.mGroup.viewer_can_grant || mCircle.mGroup.viewer_can_remove)) {
				mContentView.findViewById(R.id.in_member_action).setVisibility(View.VISIBLE);
				mGrant_member.setImageResource(R.drawable.ic_btn_down);
				mGrant_member.setOnClickListener(grantMemberClickListener);
				mContentView.findViewById(R.id.delete_member).setOnClickListener(deleteMemberClickListener);
			}
		}else {
			mRole.setVisibility(View.GONE);
			if(mCircle.mGroup != null && (mCircle.mGroup.viewer_can_grant || mCircle.mGroup.viewer_can_remove)) {
				mContentView.findViewById(R.id.in_member_action).setVisibility(View.VISIBLE);
				mGrant_member.setImageResource(R.drawable.ic_btn_up);
				mGrant_member.setOnClickListener(grantMemberClickListener);
				mContentView.findViewById(R.id.delete_member).setOnClickListener(deleteMemberClickListener);
			}
		}
		
		if(TextUtils.isEmpty(mUser.user_id) || Long.parseLong(mUser.user_id) == AccountServiceUtils.getBorqsAccountID()) {
			mContentView.findViewById(R.id.in_member_action).setVisibility(View.GONE);
		}
		
		usericon.setImageResource(R.drawable.default_user_icon);
		
		ImageRun imagerun = new ImageRun(null, mUser.image_url_m, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
        imagerun.noimage = false;        
        imagerun.addHostAndPath = true;
        imagerun.setImageView(usericon);        
        imagerun.post(null);
        
        if(BpcSearchActivity.class.isInstance(mContext)) {
        	mRole.setVisibility(View.GONE);
        }
	}
	
	public void setUserItem(Employee  di) 
	{
	    mUser = di;
	    setUI();
	}
	
	View.OnClickListener grantMemberClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            grantMember();
        }
    };
    
    View.OnClickListener deleteMemberClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            deleteMember();
        }
    };
    
    View.OnClickListener approveMemberClickListener = new View.OnClickListener() {
        public void onClick(View v) {
         approveMember();   
        }
    };
    
    View.OnClickListener ignoreMemberClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            ignoreMember();
        }
    };
    
    private void grantMember() {
        if (null != mActionLisenterMap) {
            Collection<publicCirclePeopleActionListener> listeners = mActionLisenterMap.values();
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                publicCirclePeopleActionListener listener = (publicCirclePeopleActionListener)it.next();
                listener.grantMember(String.valueOf(mUser.user_id), mUser.role_in_group, mUser.name);
            }
        }
    }
    private void deleteMember() {
        if (null != mActionLisenterMap) {
            Collection<publicCirclePeopleActionListener> listeners = mActionLisenterMap.values();
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                publicCirclePeopleActionListener listener = (publicCirclePeopleActionListener)it.next();
                listener.deleteMember(String.valueOf(mUser.user_id));
            }
        }
    }
    private void approveMember() {
        if (null != mActionLisenterMap) {
            Collection<publicCirclePeopleActionListener> listeners = mActionLisenterMap.values();
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                publicCirclePeopleActionListener listener = (publicCirclePeopleActionListener)it.next();
                listener.approveMember(String.valueOf(mUser.user_id));
            }
        }
    }
    private void ignoreMember() {
        if (null != mActionLisenterMap) {
            Collection<publicCirclePeopleActionListener> listeners = mActionLisenterMap.values();
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                publicCirclePeopleActionListener listener = (publicCirclePeopleActionListener)it.next();
                listener.ignoreMembar(String.valueOf(mUser.user_id));
            }
        }
    }
    
	@Override
	public String getText() 
	{		
		return mUser !=null? mUser.name:"";
	}

    public void attachActionListener(HashMap<String, publicCirclePeopleActionListener> listenerMap) {
        mActionLisenterMap = listenerMap;
    }
}
