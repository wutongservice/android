package com.borqs.common.view;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import twitter4j.Employee;
import twitter4j.PublicCircleRequestUser;
import twitter4j.UserCircle;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.listener.CheckBoxClickActionListener;
import com.borqs.common.listener.publicCirclePeopleActionListener;
import com.borqs.common.quickaction.QuickPeopleActivity;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;

public class PublicCircleRequestPeopleItemView extends SNSItemView 
{
	private final String TAG="PublicCircleRequestPeopleItemView";
	
	private ImageView usericon;	
	private TextView  username;
	private TextView mSecondContent;
	private TextView mRole;
	private ImageView mGrant_member;
	private CheckBox  chekbox;
	private View mContentView;
	private HashMap<String, CheckBoxClickActionListener> mCheckClickListenerMap;
	private HashMap<String, publicCirclePeopleActionListener> mActionLisenterMap;
	
	private PublicCircleRequestUser mUser;
	private int mStatus;
	private UserCircle mCircle;
	
	public PublicCircleRequestPeopleItemView(Context context, PublicCircleRequestUser di, int status, UserCircle circle) {
		super(context);
		mContext = context;
		mUser = di;
		mStatus = status;
		mCircle = circle;
		init();
	} 
    
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	
	public String getName()
	{
		return mUser.name;
	}	
	
	public long getUserID()
	{
		return mUser.uid;
	}	
	
	public boolean isSelected()
	{
		return mUser.selected;
	}
	
	public PublicCircleRequestUser getUser() {
	    return mUser;
	}
	private void init() 
	{
		Log.d(TAG,  "call init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();

		//child 1
		mContentView  = factory.inflate(R.layout.public_circle_people_item, null);		
		mContentView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		addView(mContentView);
		
		chekbox    = (CheckBox)mContentView.findViewById(R.id.user_check);
		username   = (TextView)mContentView.findViewById(R.id.user_name);
		usericon   = (ImageView)mContentView.findViewById(R.id.user_icon);
		mSecondContent = (TextView) mContentView.findViewById(R.id.second_content);
		mRole = (TextView) mContentView.findViewById(R.id.role_text);
		mGrant_member = (ImageView) mContentView.findViewById(R.id.grant_member);
		
		chekbox.setOnClickListener(stOnClik);
		
		if (usericon != null) {
		    usericon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    Intent intent = new Intent(mContext, QuickPeopleActivity.class);
                    intent.putExtra("user", mUser);
                    mContext.startActivity(intent);
                }
            });
        }
		setUI();	
	}
	
	private void setUI()
	{
		username.setText(mUser.nick_name);
		if(PublicCircleRequestUser.STATUS_IN_CIRCLE == mStatus) {
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
		    
		    if(mUser.uid == AccountServiceUtils.getBorqsAccountID()) {
		    	mContentView.findViewById(R.id.in_member_action).setVisibility(View.GONE);
		    }
		}else if(PublicCircleRequestUser.STATUS_INVITE == mStatus) {
		    mContentView.findViewById(R.id.invite_member_action).setVisibility(View.VISIBLE);
		    if(mUser.source != null) {
                StringBuilder inviteName = new StringBuilder();
                for(int i=0;i<mUser.source.size();i++) {
                    if(inviteName.length() > 0) {
                        inviteName.append(",");
                    }
                    inviteName.append(mUser.source.get(i).nick_name);
                }
		        mSecondContent.setVisibility(View.VISIBLE);
		        mSecondContent.setText(String.format(mContext.getString(R.string.public_circle_invite_by), inviteName.toString()));
		    }else {
		        mSecondContent.setVisibility(View.GONE);
		    }
		}else if(PublicCircleRequestUser.STATUS_APPLY == mStatus) {
		    if(mCircle.mGroup != null && 
		    		(PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group) 
		    				|| PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group)
		    				|| mCircle.mGroup.can_member_approve == UserCircle.VALUE_ALLOWED)) {
		        mContentView.findViewById(R.id.apply_member_action).setVisibility(View.VISIBLE);
	            mContentView.findViewById(R.id.approve_member).setOnClickListener(approveMemberClickListener);
	            mContentView.findViewById(R.id.ignore_member).setOnClickListener(ignoreMemberClickListener);
            }else {
            	mContentView.findViewById(R.id.apply_member_action).setVisibility(View.GONE);
            }
		    
		}
		
		usericon.setImageResource(R.drawable.default_user_icon);
		
		ImageRun imagerun = new ImageRun(null, mUser.profile_image_url, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
        imagerun.noimage = false;        
        imagerun.addHostAndPath = true;
        imagerun.setImageView(usericon);        
        imagerun.post(null);
        
		chekbox.setChecked(mUser.selected);
	}
	
	public void setUserItem(PublicCircleRequestUser  di) 
	{
	    mUser = di;
	    setUI();
	}
	
	public void switchCheck()
	{
		mUser.selected = !mUser.selected;
		chekbox.setChecked(mUser.selected);
		 
		Log.d(TAG, "onClick select ="+ mUser.selected);
	}
	
	View.OnClickListener stOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			switchCheck();
			if (null != mCheckClickListenerMap) {
                Collection<CheckBoxClickActionListener> listeners = mCheckClickListenerMap.values();
                Iterator it = listeners.iterator();
                while (it.hasNext()) {
                    CheckBoxClickActionListener checkListener = (CheckBoxClickActionListener)it.next();
                    checkListener.changeItemSelect(mUser.uid,mUser.nick_name, mUser.selected, true);
                }
            }
		}
	};
	
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
                listener.grantMember(String.valueOf(mUser.uid), mUser.role_in_group, mUser.nick_name);
            }
        }
    }
    private void deleteMember() {
        if (null != mActionLisenterMap) {
            Collection<publicCirclePeopleActionListener> listeners = mActionLisenterMap.values();
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                publicCirclePeopleActionListener listener = (publicCirclePeopleActionListener)it.next();
                listener.deleteMember(String.valueOf(mUser.uid));
            }
        }
    }
    private void approveMember() {
        if (null != mActionLisenterMap) {
            Collection<publicCirclePeopleActionListener> listeners = mActionLisenterMap.values();
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                publicCirclePeopleActionListener listener = (publicCirclePeopleActionListener)it.next();
                listener.approveMember(String.valueOf(mUser.uid));
            }
        }
    }
    private void ignoreMember() {
        if (null != mActionLisenterMap) {
            Collection<publicCirclePeopleActionListener> listeners = mActionLisenterMap.values();
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                publicCirclePeopleActionListener listener = (publicCirclePeopleActionListener)it.next();
                listener.ignoreMembar(String.valueOf(mUser.uid));
            }
        }
    }
    
	@Override
	public String getText() 
	{		
		return mUser !=null? mUser.name:"";
	}

    public void attachCheckListener(HashMap<String, CheckBoxClickActionListener> listenerMap) {
        mCheckClickListenerMap = listenerMap;
    }
    public void attachActionListener(HashMap<String, publicCirclePeopleActionListener> listenerMap) {
        mActionLisenterMap = listenerMap;
    }
    public void setStatus(int status) {
        mStatus = status;
    }
}
