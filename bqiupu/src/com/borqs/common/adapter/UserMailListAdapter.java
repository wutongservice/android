package com.borqs.common.adapter;


import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.borqs.qiupu.R;

public class UserMailListAdapter extends BaseAdapter{
	private static final String TAG = "Qiupu.UsrMailListAdapter";
	private List<String> items;

	private Context mContext;
	public UserMailListAdapter(Context context){
		mContext = context;
	}
	public List<String> getItems() {
		return items;
	}
    
	public void setItems(List<String> items) {
		this.items = items;
	}
	public int getCount() {
		return items != null ? items.size():0;
	}
	
	public String getItem(int position) {
		return items != null ? items.get(position):null;
	}
	
	public long getItemId(int position) {
		return -1;
	}
	public View getView(int position, View convertView, ViewGroup parent) {
//		ApkResponse apkinfo=getItem(position);
		final String mailName = items.get(position);
		final ViewHolder holder;
		if (convertView == null) 
	    {
			convertView = (RelativeLayout)LayoutInflater.from(mContext).inflate(R.layout.login_mail_item_view, null);
	        holder = new ViewHolder();
	        holder.title = (TextView)convertView.findViewById(R.id.id_user_mail);
	        
	        holder.delete = (ImageView)convertView.findViewById(R.id.id_user_mail_del);
	        convertView.setTag(holder);
	    } else {
	        holder = (ViewHolder)convertView.getTag();
	    }
		holder.title.setText(mailName);
		holder.delete.setImageResource(R.drawable.btn_browser_stop_1);
	    return convertView;
	}

	
	
	static class ViewHolder
	{
		public TextView title;
        public ImageView delete;
	}
}
