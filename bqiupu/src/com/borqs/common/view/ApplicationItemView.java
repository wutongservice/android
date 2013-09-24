package com.borqs.common.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.RatingView;
import com.borqs.common.view.SNSItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import twitter4j.ApkResponse;

public class ApplicationItemView extends SNSItemView {

	private ApkResponse apkinfo;
	private ImageView   icon;
	private TextView    title, times, apk_versioncode;
	private RatingView rating;
	public ApplicationItemView(Context context) {
		super(context);
	}

	public ApkResponse getAttachApk()
	{
		return apkinfo;
	}
	
	boolean forcomments = false;
	public ApplicationItemView(Context context, ApkResponse apk,boolean forcomments) {
		super(context);
		
		apkinfo = apk;
		this.forcomments = forcomments;
		init();
	}
	

	@Override
	public String getText() {
		return null;
	}

	@Override
    protected void onFinishInflate() 
    {   
        super.onFinishInflate();        
        init();
    }
	
	private void init() {
		
		LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        
        //child 1
        View convertView  = factory.inflate(R.layout.apk_sort_item_view, null);
        convertView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, (int) mContext.getResources().getDimension(R.dimen.list_item_height)));
        addView(convertView);
        
        icon    = (ImageView)convertView.findViewById(R.id.apk_icon);
        title   = (TextView)convertView.findViewById(R.id.id_apk_name);        
        rating  = (RatingView)convertView.findViewById(R.id.apk_rating);
        
        times   = (TextView) convertView.findViewById(R.id.qiupu_apk_download_install_times);
        apk_versioncode = (TextView) convertView.findViewById(R.id.qiupu_apk_versioncode);
        
//        icon.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View arg0) {
//				QuickLauncher ql = new QuickLauncher();
//	        	ql.popupApplicationLauncher((BasicActivity)getContext(), ApplicationItemView.this, apkinfo);				
////				MmsContract.QuickMms.showQuickMms(getContext(), ApplicationItemView.this, null, 1, new String[]{});
//			}
//		});
        
		setUI();
	}
	
	public void setApk(ApkResponse apk)
	{
		apkinfo = apk;		
		setUI();
	}
	
	private void setUI()
	{
		title.setText(apkinfo.label);
		icon.setImageResource(R.drawable.default_app_icon);
		apk_versioncode.setText(String.format(mContext.getString(R.string.apk_current_version), apkinfo.versionname));
		
		ImageRun imagerun = new ImageRun(null, apkinfo.iconurl, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_APK;
        imagerun.noimage = true;
        imagerun.addHostAndPath = true;
        imagerun.setImageView(icon);        
        imagerun.post(null);
        
		rating.drawRating(apkinfo.ratio);
		
		times.setText(String.format(mContext.getString(R.string.install_download_times), apkinfo.download_times,apkinfo.install_times));
		
		if(apkinfo.uploadUser != null && apkinfo.uploadUser.uid>0 &&  apkinfo.uploadUser.uid != QiupuConfig.QIUPU_USER_ID)
		{
			StringBuilder hint = new StringBuilder();
			hint.append(String.format(mContext.getString(R.string.upload_from), apkinfo.uploadUser.nick_name));
			if(apkinfo.upload_time>0)
			{
				hint.append(", ");
				hint.append(com.borqs.qiupu.util.DateUtil.converToRelativeTime(mContext, apkinfo.upload_time));
			}
		}
		else
		{
//			poster.setVisibility(View.GONE);
		}		
	}

    public static boolean invokeApkDetailActivity(View view) {
        if (ApplicationItemView.class.isInstance(view)) {
            ApplicationItemView av = (ApplicationItemView) view;
            final ApkResponse rInfo = av.getAttachApk();
            IntentUtil.startApkDetailActivity(view.getContext(), ApkResponse.APKStatus.STATUS_NEED_DOWNLOAD, rInfo);
            return true;
        }
        return false;
    }
}
