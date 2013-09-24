package com.borqs.common.view;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

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

public class PublicCircleMemberPickItemView extends SNSItemView 
{
	private final String TAG="PublicCircleMemberPickItemView";
	
	private ImageView usericon;	
	private TextView  username;
	private TextView mRole;
	private CheckBox  chekbox;
	private View mContentView;
	private HashMap<String, CheckBoxClickActionListener> mCheckClickListenerMap;
	private HashMap<String, publicCirclePeopleActionListener> mActionLisenterMap;
	
	private PublicCircleRequestUser mUser;
	private int mStatus;
	private UserCircle mCircle;
	
	public PublicCircleMemberPickItemView(Context context, PublicCircleRequestUser di, int status, UserCircle circle) {
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
		mContentView  = factory.inflate(R.layout.public_circle_member_pick_item, null);		
		mContentView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
		addView(mContentView);
		
		chekbox    = (CheckBox)mContentView.findViewById(R.id.user_check);
		username   = (TextView)mContentView.findViewById(R.id.user_name);
		usericon   = (ImageView)mContentView.findViewById(R.id.user_icon);
		mRole = (TextView) mContentView.findViewById(R.id.role_text);
		
		chekbox.setOnClickListener(stOnClik);
		
		setUI();	
	}
	
	private void setUI()
	{
		username.setText(mUser.nick_name);
		
		if(AccountServiceUtils.getBorqsAccountID() == mUser.uid) {
		    chekbox.setVisibility(View.GONE);
		}else {
		    chekbox.setVisibility(View.VISIBLE);
		}
		if(mUser.role_in_group == PublicCircleRequestUser.ROLE_TYPE_CREATER) {
            mRole.setVisibility(View.VISIBLE);
            mRole.setText(R.string.public_circle_role_creator);
        }else if(PublicCircleRequestUser.ROLE_TYPE_MANAGER == mUser.role_in_group) {
            mRole.setVisibility(View.VISIBLE);
            mRole.setText(R.string.public_circle_role_manager);
        }else {
            mRole.setVisibility(View.GONE);
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
                    checkListener.changeItemSelect(mUser.uid, mUser.nick_name, mUser.selected, true);
                }
            }
		}
	};
	
	@Override
	public String getText() 
	{		
		return mUser !=null? mUser.name:"";
	}

    public void attachCheckListener(HashMap<String, CheckBoxClickActionListener> listenerMap) {
        mCheckClickListenerMap = listenerMap;
    }

    public void setStatus(int status) {
        mStatus = status;
    }
}
