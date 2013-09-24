package com.borqs.qiupu.ui;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.ApkResponse;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.borqs.common.adapter.ApkDownloadAdapter;
import com.borqs.common.view.ApkDownloaditemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.service.ApkFileManager;
import com.borqs.qiupu.service.QiupuService;

public class DownloadManagerActivity extends BasicActivity implements ApkFileManager.ApkFileServiceListener {
	private static final String TAG = "Qiupu.DownloadManagerActivity";
    private ListView mSavedApkListView;
    private ListView mDownloadingApkListView;
    private ImageView save_apk_in_out;
    private ImageView download_apk_in_out;
    private View save_apk_title;
    private View download_apk_title;
    private ApkDownloadAdapter mSavedApkAdapter;
    private ApkDownloadAdapter mDownloadingApkAdapter;
    private HashMap<String, ApkResponse> mDownloadApksMap;
    private TextView mWaitInstallTV, mDownloadingTV;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_manager);
		setHeadTitle(R.string.home_downloadmanager);
		
		mDownloadApksMap = ApkFileManager.mDownloadingApksMap;

		mWaitInstallTV = (TextView)findViewById(R.id.title_wait_install);
		mDownloadingTV = (TextView)findViewById(R.id.title_downloading);
		
		save_apk_in_out = (ImageView) findViewById(R.id.save_apk_in_out);
		download_apk_in_out = (ImageView) findViewById(R.id.download_apk_in_out);
		
		save_apk_title = findViewById(R.id.save_apk_title);
		download_apk_title = findViewById(R.id.download_apk_title);
		
		mSavedApkListView = (ListView)findViewById(R.id.content_wait_install);
		mSavedApkAdapter = new ApkDownloadAdapter(this);
		mSavedApkListView.setAdapter(mSavedApkAdapter);
		mSavedApkListView.setOnItemClickListener(finishitemClickListener);
		
		mDownloadingApkListView = (ListView)findViewById(R.id.content_downloading);
		mDownloadingApkAdapter = new ApkDownloadAdapter(this);
		mDownloadingApkListView.setAdapter(mDownloadingApkAdapter);		
		mDownloadingApkListView.setOnItemClickListener(onGoingitemClickListener);
		
        showRightActionBtn(false);
        showLeftActionBtn(false);
        
//        save_apk_title.setOnClickListener(saveApkInOutClickListener);
//        download_apk_title.setOnClickListener(downloadApkClickListener);
	}
	
	View.OnClickListener saveApkInOutClickListener = new OnClickListener() {
		
		public void onClick(View v) {
			boolean flag = mSavedApkListView.getVisibility() == View.VISIBLE;
			save_apk_in_out.setImageResource(flag ? R.drawable.btn_show_out : R.drawable.btn_show_in);
			mSavedApkListView.setVisibility(flag ? View.GONE : View.VISIBLE );
		}
	};
	
	View.OnClickListener downloadApkClickListener = new OnClickListener() {
		
		public void onClick(View v) {
			boolean flag = mDownloadingApkListView.getVisibility() == View.VISIBLE;
			download_apk_in_out.setImageResource(flag ? R.drawable.btn_show_out : R.drawable.btn_show_in);
			mDownloadingApkListView.setVisibility(flag ? View.GONE : View.VISIBLE );
		}
	};
	
	AdapterView.OnItemClickListener finishitemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
            if(QiupuConfig.LOGD) Log.d(TAG, "onItemClick detail Apk");
            ApkDownloaditemView.invokeApkDetailActivity(v, null);
		}
	};
	
	AdapterView.OnItemClickListener onGoingitemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
            if(QiupuConfig.LOGD)Log.d(TAG, "apk info status");
            ApkDownloaditemView.invokeApkDetailActivity(v);
		}
	};
	
	
	@Override
	public void onLogin() {
		super.onLogin();
		//TOOD check uid, refresh data.
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if(QiupuConfig.LOGD)Log.d(TAG, "onNewIntent intent");
		super.onNewIntent(intent);
	}
	
	@Override
	protected void createHandler() {
		mHandler = new MainHandler();
	}
	
	@Override
	protected void onResume() {
		ApkFileManager.registerApkFileServiceListener(getClass().getName(), this);
		mHandler.obtainMessage(QiupuMessage.MESSAGE_LOAD_USER_APK).sendToTarget();
		super.onResume();
	}
	
	@Override
	protected void onPause() {		
		ApkFileManager.unregisterApkFileServiceListener(getClass().getName());
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();		
	}
	
    private class MainHandler extends Handler {
	    public void handleMessage(Message msg) {
	    	switch(msg.what){
		    	case QiupuMessage.MESSAGE_LOAD_USER_APK:
		    		loadDownloadApk();
		    		break;
		    	case QiupuMessage.MESSAGE_LOAD_USER_APK_END:
		    		end();
		    		break;
			    case QiupuMessage.MESSAGE_DOWNLOAD_APK:
			    {
		    		Intent service = new Intent(DownloadManagerActivity.this, QiupuService.class);
			    	service.setAction(QiupuService.INTENT_QP_DOWNLOAD_APK);
			    	Bundle bundle = new Bundle();
			    	bundle.putSerializable(QiupuMessage.BUNDLE_APKINFO, msg.getData().getSerializable(QiupuMessage.BUNDLE_APKINFO));
			    	service.putExtras(bundle);
			    	DownloadManagerActivity.this.startService(service);
		    		break; 
			    }
                case QiupuMessage.MESSAGE_DOWNLOAD_APK_LOADING:
                    // TODO: only refresh the changing
                	
                	final String apkpn = msg.getData().getString(QiupuMessage.BUNDLE_APK_PACKAGENAME);
                	//change UI
                	
                	for(int i=0;i<mDownloadingApkListView.getChildCount();i++)
                	{
                		final ApkDownloaditemView av = (ApkDownloaditemView)mDownloadingApkListView.getChildAt(i);
                		if(av.getAttachApk().packagename.endsWith(apkpn))
                		{
                			av.refreshProgress();
                			break;
                		}                		
                	}
                    //mDownloadingApkAdapter.notifyDataSetChanged();
                    break;
                case QiupuMessage.MESSAGE_DOWNLOAD_APK_OK:
			    case QiupuMessage.MESSAGE_DOWNLOAD_APK_FAILED:
			    {
			    	resetCount();
			    	break;
			    }
	    	}
	    }  
    }
    
    private void loadDownloadApk() {
    	ArrayList<ApkResponse> savedApksList;
    	savedApksList = ApkFileManager.loadSavedApks(this);
		if(savedApksList == null || savedApksList.size() <= 0) {
			savedApksList = new ArrayList<ApkResponse>();
		}
    	
    	mWaitInstallTV.setText(getResources().getString(R.string.apk_wait_install)+"("+savedApksList.size()+")");
    	
		mSavedApkAdapter.setData(savedApksList);
		mSavedApkAdapter.notifyDataSetChanged();
		
		//update Click Listener  
		if(savedApksList.size() > 0)
		{
			save_apk_in_out.setVisibility(View.VISIBLE);
			save_apk_title.setOnClickListener(saveApkInOutClickListener);
		}
		else
		{
			save_apk_in_out.setVisibility(View.GONE);
			save_apk_title.setOnClickListener(null);
		}
		
		ArrayList<ApkResponse> downloadApksList = ApkFileManager.getDownloadingApksList();
	    HashMap<String, ApkResponse> downloadApksMap = ApkFileManager.getDownloadingApksMap();

	    if(downloadApksList == null){
	        downloadApksList = new ArrayList<ApkResponse>();
	        downloadApksMap = new HashMap<String, ApkResponse>();
	    }
	    
	    mDownloadingTV.setText(getResources().getString(R.string.apk_downloading)+"("+downloadApksList.size()+")");
	    mDownloadingApkAdapter.setData(downloadApksList);
	    mDownloadingApkAdapter.notifyDataSetChanged();
	    
	    if(downloadApksList.size() > 0)
	    {
	    	download_apk_in_out.setVisibility(View.VISIBLE);
	    	download_apk_title.setOnClickListener(downloadApkClickListener);
	    }
	    else
	    {
	    	download_apk_in_out.setVisibility(View.GONE);
	    	download_apk_title.setOnClickListener(null);
	    }
	    mDownloadApksMap = downloadApksMap;
    }
    
    private void resetCount()
    {
    	loadDownloadApk();
    }
    
	public void onClick(View view) {
		final int id = view.getId();
		if (id == R.id.head_action_left) {
			forceSyncData();
			return;
		}
		
		super.onClick(view);
	}
	
//	@Override
//	protected void backtoPreActivity()
//	{
//		Intent intent = new Intent(this, MainActivity.class);
//		startActivity(intent);
//	}
	
	private void forceSyncData() {
		begin();
	}

	/*private void installApk(String filePath){
		File file = new File(filePath);
		if(file.exists()){
		  Intent intent = new Intent(Intent.ACTION_VIEW);  
	      intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");  
	      startActivity(intent);  
	    }
	}*/

	public void downloadApk(ApkResponse apkinfo){
		Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_DOWNLOAD_APK);
		msg.getData().putSerializable(QiupuMessage.BUNDLE_APKINFO, apkinfo);
		msg.sendToTarget();
	}

	public void updateUI(int msgcode, Message passmsg) {
		switch(msgcode) {		
		    case QiupuMessage.MESSAGE_DOWNLOAD_MADE_CONNECTION:
		    {
		    	break;
		    }
		    case QiupuMessage.MESSAGE_RM_APK_FILE:
		    {
		    	mHandler.obtainMessage(QiupuMessage.MESSAGE_LOAD_USER_APK).sendToTarget();
		    	break;
		    }			
			case QiupuMessage.MESSAGE_DOWNLOAD_APK_OK:
			{
				final String pn = passmsg.getData().getString(QiupuMessage.BUNDLE_APK_PACKAGENAME);
				ApkResponse downloadapk = ApkFileManager.mDownloadingApksMap.get(pn);
				if(downloadapk != null) {
					ApkResponse apk = mDownloadApksMap.get(pn);
					apk.status = ApkResponse.APKStatus.STATUS_NEED_DOWNLOAD;
				}
				
				mHandler.obtainMessage(QiupuMessage.MESSAGE_DOWNLOAD_APK_OK).sendToTarget();
				break;
			}
			case QiupuMessage.MESSAGE_DOWNLOAD_APK_FAILED:
			{
				mHandler.obtainMessage(QiupuMessage.MESSAGE_DOWNLOAD_APK_FAILED).sendToTarget();
				break;
			}
			case QiupuMessage.MESSAGE_DOWNLOAD_APK_LOADING:
			{
				final String pn = passmsg.getData().getString(QiupuMessage.BUNDLE_APK_PACKAGENAME);
				ApkResponse downloadapk = ApkFileManager.mDownloadingApksMap.get(pn);
				if(downloadapk != null) {
					ApkResponse apk = mDownloadApksMap.get(pn);
					apk.progress = downloadapk.progress;
				}
				
				Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_DOWNLOAD_APK_LOADING);
				msg.getData().putString(QiupuMessage.BUNDLE_APK_PACKAGENAME, pn);
				msg.sendToTarget();
				break;
			}
		}
	}

}
