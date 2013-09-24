package com.borqs.common.view;


import twitter4j.QiupuUser;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;

public class UserGalleryItemView extends SNSItemView 
{
	private final String TAG="UserGalleryItemView";
	
	private ImageView usericon;	
	private TextView  label;
	private QiupuUser mUser;
	
	public UserGalleryItemView(Context context, QiupuUser di) {
		super(context);
		mContext = context;
		mUser = di;
		init();
	} 
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	
	public QiupuUser getUser()
	{
		return mUser;
	}
	
	public void setUser(QiupuUser apkinfo)
	{
		mUser = apkinfo;		
		setUI();
	}
	
	private void init() 
	{
		Log.d(TAG,  "call init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();

		//child 1
		View v  = factory.inflate(R.layout.gallery_item_view, null);		
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		addView(v);
		
		label   = (TextView)v.findViewById(R.id.id_label);
		usericon   = (ImageView)v.findViewById(R.id.id_icon);
		setUI();	
	}
	
	private void setUI()
	{
		label.setText(mUser.nick_name);	
		
		usericon.setImageResource(R.drawable.default_user_icon);
		
		ImageRun imagerun = new ImageRun(null,mUser.profile_image_url, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
        imagerun.noimage = true;
        imagerun.addHostAndPath = true;
        imagerun.setRoundAngle=true;
        imagerun.setImageView(usericon);        
        imagerun.post(null);
	}
	
	public void setUserItem(QiupuUser  di) 
	{
		mUser = di;
	    setUI();
	}
	
	@Override
	public String getText() 
	{		
		return mUser !=null?mUser.nick_name:"";
	}
}
