package com.borqs.common.adapter;

import java.util.ArrayList;

import twitter4j.QiupuAccountInfo;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.ShareSourceItem;
import com.borqs.common.view.ProfileSourceItemView;
import com.borqs.qiupu.QiupuConfig;

public class ProfileShareSourceAdapter extends BaseAdapter{
	private static final String TAG = "ProfileShareSourceAdapter";
    private Context mContext;
    private ArrayList<ShareSourceItem> items = new ArrayList<ShareSourceItem>();
    private long mId;

    public ProfileShareSourceAdapter(Context context, QiupuAccountInfo user){
    	mContext = context;
    	mId = user.uid;
    }
	public ProfileShareSourceAdapter(Context context,long id){
		mContext = context;
		mId = id;
	}
	
	public int getCount() {
		if(items == null){
			return 0;
        } else {
            return items.size();
        }
	}
	
	public ShareSourceItem getItem(int position) {
		if (position >= items.size()) {
            return null;
        }
        return items.get(position);
	}
	
	public long getItemId(int position) {
		return position;
	}

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        ShareSourceItem item = getItem(position);
        if (convertView == null || !(convertView instanceof ProfileSourceItemView)) {
        	ProfileSourceItemView tmpView = new ProfileSourceItemView(mContext, item, QiupuConfig.isPublicCircleProfile(mId) || QiupuConfig.isEventIds(mId));
            holder = new ViewHolder();
            tmpView.setTag(holder);
            holder.view = tmpView;

            convertView = tmpView;

        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.view.setItem(item);
        }

        return convertView;
    }

	static class ViewHolder
	{
		public ProfileSourceItemView view;
	}
	
	public void alterDataList(ArrayList<ShareSourceItem> newList) {
		items.clear();
		
		if (null != newList) {
			items.addAll(newList);
		}
		notifyDataSetChanged();
	}
}
