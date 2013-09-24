package com.borqs.common.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.borqs.common.util.IntentUtil;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.service.ApkFileManager;
import com.borqs.qiupu.ui.DownloadManagerActivity;
import twitter4j.ApkResponse;

public class ApkDownloaditemView extends SNSItemView {

	private static final String TAG = "Qiupu.ApkDownloaditemView";
	private ApkResponse apkinfo;
	private ImageView icon;
	private TextView title;
	private ProgressBar progress;
	private TextView status;
	private TextView downloadoktv;
	public ApkDownloaditemView(Context context) {
		super(context);
	}

	public ApkResponse getAttachApk()
	{
		return apkinfo;
	}
	
	boolean forcomments = false;
	public ApkDownloaditemView(Context context, ApkResponse apk) {
		super(context);
		
		apkinfo = apk;
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
        View convertView  = factory.inflate(R.layout.download_list_item_view, null);      
        convertView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    LayoutParams.WRAP_CONTENT));
        addView(convertView);
        
        title = (TextView)convertView.findViewById(R.id.apk_title);
        progress = (ProgressBar)convertView.findViewById(R.id.apk_load_progress);
        status = (TextView)convertView.findViewById(R.id.apk_opbtn);
        downloadoktv = (TextView)convertView.findViewById(R.id.apk_download_ok);
        icon = (ImageView)convertView.findViewById(R.id.apk_icon);
        
		setUI();
	}
	
	public void setApk(ApkResponse apk)
	{
		apkinfo = apk;		
		setUI();
	}
	
	public void refreshProgress()
	{
		// only need refresh progress 
		progress.setProgress(apkinfo.progress);
		setActionButton();
//		setUI();
	}
	
	private void setUI()
	{
		title.setText(apkinfo.label);  
		progress.setProgress(apkinfo.progress);
		
		if(apkinfo.icon != null) {
			icon.setImageBitmap(apkinfo.icon);
		} else {
			//icon.setImageResource(R.drawable.default_app_icon);
		    ImageRun imagerun = new ImageRun(null, apkinfo.iconurl, 0);
	        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_APK;
	        imagerun.noimage = true;
	        imagerun.addHostAndPath = true;
	        imagerun.setImageView(icon);        
	        imagerun.post(null);
		}
        
		setActionButton();
	}
	
	private void setActionButton()
	{
		switch(apkinfo.status){
        case ApkResponse.APKStatus.STATUS_NEED_DOWNLOAD:
        {
        	if(ApkFileManager.existDownloadAPK(mContext,apkinfo.packagename)){
        		status.setText(R.string.apk_download_ok);
//				status.setBackgroundResource(R.drawable.apk_download);
				status.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						ApkFileManager.installApk(mContext, QiupuHelper.getDownloadSdcardPath(apkinfo.packagename), apkinfo.label, apkinfo.packagename);
					}
				});
				downloadoktv.setVisibility(View.VISIBLE);
				downloadoktv.setText(R.string.apk_download_finish);
				progress.setVisibility(View.GONE);
        	}
        	else
        	{
	        	status.setText(R.string.apk_download);
//	        	status.setBackgroundResource(R.drawable.apk_download);
	        	status.setOnClickListener(new OnClickListener(){
	        		public void onClick(View view) {
	        			status.setText(R.string.apk_downloading);
	        			apkinfo.status = ApkResponse.APKStatus.STATUS_DOWNLOADING;
	        			downloadoktv.setVisibility(View.GONE);
	    				progress.setVisibility(View.VISIBLE);
	    				
	        			if(DownloadManagerActivity.class.isInstance(mContext))
	    				{
	        				DownloadManagerActivity dm = (DownloadManagerActivity) mContext;
	        				dm.downloadApk(apkinfo);
	    				}			
	        		}
	        	});
	        	downloadoktv.setVisibility(View.GONE);
				progress.setVisibility(View.GONE);
        	}
        	break;
        }
        case ApkResponse.APKStatus.STATUS_UPDATING:
        // TODO: implement rather than fall through.
        case ApkResponse.APKStatus.STATUS_DOWNLOADING:
//        	if(apkinfo.progress == 0){
//				status.setText(R.string.apk_downloading);
//			}else{
				//status.setText(apkinfo.progress+"%");
				status.setText(R.string.label_cancel);   
//			}
//			status.setBackgroundResource(R.drawable.apk_download);
			status.setOnClickListener(new OnClickListener(){
				public void onClick(View view) {
					//cancel current downloading
					try{
						apkinfo.iscancelApp = true;
					    apkinfo.connection.disconnect();
					}
					catch(Exception ne)
					{
						ne.printStackTrace();
					}
				}
			});
			
			downloadoktv.setVisibility(View.GONE);
			progress.setVisibility(View.VISIBLE);
        	break;
        default:
        	downloadoktv.setVisibility(View.GONE);
			progress.setVisibility(View.GONE);
    }	
	}

    public static boolean invokeApkDetailActivity(View view, Bitmap icon) {
        if (ApkDownloaditemView.class.isInstance(view)) {
            ApkDownloaditemView av = (ApkDownloaditemView) view;
            final ApkResponse rInfo = av.getAttachApk();
            if (rInfo != null) {
                rInfo.status = ApkResponse.APKStatus.STATUS_NEED_DOWNLOAD;
                //we don't need icon, it can't be passed
                rInfo.icon = icon;
            }

            IntentUtil.startApkDetailActivity(view.getContext(), rInfo);
            return true;
        }
        return false;
    }

    public static boolean invokeApkDetailActivity(View view) {
        if (ApkDownloaditemView.class.isInstance(view)) {
            ApkDownloaditemView av = (ApkDownloaditemView) view;
            final ApkResponse rInfo = av.getAttachApk();
            IntentUtil.startApkDetailActivity(view.getContext(), rInfo);
            return true;
        }
        return false;
    }
}
