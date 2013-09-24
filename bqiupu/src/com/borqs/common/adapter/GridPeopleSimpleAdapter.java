package com.borqs.common.adapter;

import java.util.ArrayList;

import twitter4j.UserImage;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.view.GridSimplePeopleItemView;

public class GridPeopleSimpleAdapter extends BaseAdapter{
	
	private Context mContext;
	private ArrayList<UserImage> mUserList;
	public GridPeopleSimpleAdapter(Context context,ArrayList<UserImage> userlist)
	{
		mContext = context;
		mUserList = userlist;
	}

	public int getCount() {
		return mUserList != null ? mUserList.size():0;
	}

	public UserImage getItem(int position) {
		return mUserList != null ? mUserList.get(position):null;
	}

	public long getItemId(int position) {
		return position;
	}
	private ViewHolder holder = null;
	public View getView(int position, View convertView, ViewGroup parent) {
		final UserImage userinfo = getItem(position);
		if(userinfo != null)
		{
			if (convertView == null || false == (convertView instanceof GridSimplePeopleItemView) )
		    {
				
				GridSimplePeopleItemView av = new GridSimplePeopleItemView(mContext, userinfo);
				holder = new ViewHolder();
				holder.view = av;
		        
				convertView = av;
		        av.setTag(holder);
		    } else {
		        holder = (ViewHolder)convertView.getTag();
		        holder.view.setUserInfo(userinfo);		        
		    }
		}
		return convertView;
	}
	
	static class ViewHolder
	{
		public GridSimplePeopleItemView view;
	}

	public GridSimplePeopleItemView getItemView() {
	    return holder.view;
	}
}
