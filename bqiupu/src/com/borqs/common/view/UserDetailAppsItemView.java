package com.borqs.common.view;


import com.borqs.common.util.IntentUtil;
import twitter4j.ApkResponse;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;

public class UserDetailAppsItemView extends SNSItemView 
{
	private final String TAG="UserDetailAppsItemView";
	private ImageView icon;
	private TextView title;
	
	ApkResponse apkinfo;
	
	public UserDetailAppsItemView(Context context, ApkResponse di) {
		super(context);
		mContext = context;
		apkinfo = di;
		
		Log.d(TAG, "call UserSelectItem");
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
		return apkinfo.label;
	}	
	
	public String getapkID()
	{
		return apkinfo.apk_server_id;
	}	
	
	private void init() 
	{
		
		LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        
        //child 1
        View convertView  = factory.inflate(R.layout.application_grid_item_view, null);      
        convertView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    LayoutParams.WRAP_CONTENT));
        addView(convertView);
        
        icon  = (ImageView)convertView.findViewById(R.id.apk_icon);
        title = (TextView)convertView.findViewById(R.id.apk_title);
        
		setUI();	
	}
	
	private void setUI()
	{
		title.setText(apkinfo.label);
		
		icon.setImageResource(R.drawable.default_app_icon);
		
		ImageRun imagerun = new ImageRun(null, apkinfo.iconurl, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_APK;
        imagerun.noimage = true;
        imagerun.addHostAndPath = true;
        imagerun.setImageView(icon);        
        imagerun.post(null);
	}
	
	public void setapkItem(ApkResponse  di) 
	{
	    apkinfo = di;
	    setUI();
	}
	
	public ApkResponse getAttachApk()
	{
		return apkinfo;
	}
	@Override
	public String getText() {
		return null;
	}

    public static boolean invokeApkDetailActivity(View view) {
        if (UserDetailAppsItemView.class.isInstance(view)) {
            UserDetailAppsItemView av = (UserDetailAppsItemView) view;
            final ApkResponse rInfo = av.getAttachApk();
            IntentUtil.startApkDetailActivity(view.getContext(), ApkResponse.APKStatus.STATUS_NEED_DOWNLOAD, rInfo);
            return true;
        }
        return false;
    }
}
