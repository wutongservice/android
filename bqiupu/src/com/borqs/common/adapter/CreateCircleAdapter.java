package com.borqs.common.adapter;

import java.util.ArrayList;

import twitter4j.Circletemplate.TemplateInfo;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.view.CreateCircleItemView;

public class CreateCircleAdapter extends BaseAdapter{
	private static final String TAG = "CreateCircleAdapter";
    private Context mContext;
    private ArrayList<TemplateInfo> mItemList = new ArrayList<TemplateInfo>();
    
	public CreateCircleAdapter(Context context){
		mContext = context;
	}
	
	public void alertData(ArrayList<TemplateInfo> itemlist) {
		if(mItemList != null) {
			mItemList.clear();
			mItemList.addAll(itemlist);
		}
		notifyDataSetChanged();
	}
	
	public int getCount() {
	    int count = 0;
	    if(mItemList != null && mItemList.size() > 0) {
        	count = mItemList.size();
        }
        return count;
	}
	
	public TemplateInfo getItem(int position) {
	    Log.d(TAG, "getItem:  " + position);
	    if(mItemList != null && position < mItemList.size()) {
			return mItemList.get(position);
		}else{
			return null;
		}
	}
	
	public long getItemId(int position) {
		return  -1;
	}

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        TemplateInfo info = getItem(position);
        if (info != null) {
            if (convertView == null
                    || false == (convertView instanceof CreateCircleItemView)) {
            	CreateCircleItemView rView = new CreateCircleItemView(mContext, info);
                holder = new ViewHolder();

                rView.setTag(holder);
                holder.view = rView;
                convertView = rView;

            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.view.setTemplate(info);
            }
        }
        return convertView;

    }

	static class ViewHolder
	{
		public CreateCircleItemView view;
	}
}
