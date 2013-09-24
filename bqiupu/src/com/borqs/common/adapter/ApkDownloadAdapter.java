package com.borqs.common.adapter;

import java.util.ArrayList;

import twitter4j.ApkResponse;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.view.ApkDownloaditemView;

public class ApkDownloadAdapter extends BaseAdapter{
	private static final String TAG = "Qiupu.ApkDownloadAdapter";
    private Context mContext;
    private ArrayList<ApkResponse> mdownloadApks = new ArrayList<ApkResponse>();
    
    int type = 0;
    public void setType(int type)
    {
    }
    
	public ApkDownloadAdapter(Context context){
		mContext = context;
	}
	
	public int getCount() {
		return mdownloadApks != null ? mdownloadApks.size():0;
	}
	
	public ApkResponse getItem(int position) {
		return mdownloadApks != null ? mdownloadApks.get(position):null;
	}
	
	public long getItemId(int position) {
		return -1;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		final ApkResponse apk = getItem(position);
		if (convertView == null || false == (convertView instanceof ApkDownloaditemView))
		{
			ApkDownloaditemView view = new ApkDownloaditemView(mContext, apk);
			view.setApk(apk);
			holder = new ViewHolder();
			holder.view =  view;	   
			view.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();	        
			holder.view.setApk(apk);
		}
		return holder.view;
	}
	
	static class ViewHolder
	{
		public ApkDownloaditemView view;
	}
	
	public void setData(ArrayList<ApkResponse> data) {
		mdownloadApks.clear();
		mdownloadApks.addAll(data);
	}
}
