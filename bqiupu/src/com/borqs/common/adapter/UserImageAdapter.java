package com.borqs.common.adapter;

import java.util.ArrayList;

import twitter4j.QiupuUser;
import android.R.color;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.view.UserGalleryItemView;
import com.borqs.qiupu.R;

public class UserImageAdapter extends BaseAdapter{
	
	private Context mContext;
	private ArrayList<QiupuUser> mUsers;
	private int mGalleryItemBackground;
	public UserImageAdapter(Context context,ArrayList<QiupuUser> apks)
	{
		mContext = context;
		mUsers = apks;
		 TypedArray a = mContext.obtainStyledAttributes(R.styleable.Gallery1);
         mGalleryItemBackground = a.getResourceId(
                 R.styleable.Gallery1_android_galleryItemBackground, 0);
         a.recycle();
	}

	public int getCount() {
		return mUsers != null ? mUsers.size():0;
	}

	public QiupuUser getItem(int position) {
		return mUsers != null ? mUsers.get(position):null;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final QiupuUser userinfo = getItem(position);
		if(userinfo != null)
		{
			final ViewHolder holder;
			if (convertView == null || false == (convertView instanceof UserGalleryItemView) )
		    {
				holder = new ViewHolder();
				UserGalleryItemView av = new UserGalleryItemView(mContext, userinfo);
				holder.apkView = av;
		        
				convertView = av;
		        av.setTag(holder);
//		        av.setBackgroundResource(mGalleryItemBackground);
		    } else {
		        holder = (ViewHolder)convertView.getTag();
		        holder.apkView.setUser(userinfo);	
//		        holder.apkView.setBackgroundResource(mGalleryItemBackground);
		    }
		}
		convertView.setBackgroundColor(color.background_dark);
		return convertView;
	}
	
	static class ViewHolder
	{
		public UserGalleryItemView apkView;
	}

}
