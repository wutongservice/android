package com.borqs.common.adapter;

import java.util.ArrayList;

import twitter4j.QiupuUser;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.borqs.common.view.MulitPhoneEmailItemView;
import com.borqs.common.view.MulitPhoneEmailSelectItemView.checkPhoneEmailItemListener;
import com.borqs.qiupu.R;

public class MulitPhoneEmailListAdapter extends BaseAdapter{
	private static final String TAG = "MulitPhoneEmailListAdapter";
	private ArrayList<QiupuUser> mItems = new ArrayList<QiupuUser>();
    private Context mContext;
    
	public MulitPhoneEmailListAdapter(Context context,ArrayList<QiupuUser> list){
		mContext = context;
		mItems = list;
	}
	
	public MulitPhoneEmailListAdapter(Context context){
        mContext = context;
    }
	
	public void alterRequests(ArrayList<QiupuUser> userlist){
	    mItems.clear();
	    mItems.addAll(userlist);
		notifyDataSetChanged();
	}
	
	public int getCount() {
	    return mItems.size();
	}
	
	public QiupuUser getItem(int position) {
	    return mItems.get(position);
	}
	
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;		
		QiupuUser userinfo = getItem(position);
		if(userinfo != null) {
			if (convertView == null || false == (convertView instanceof MulitPhoneEmailItemView) )
		    {
			    MulitPhoneEmailItemView  rView = new MulitPhoneEmailItemView(mContext, userinfo);
		        holder = new ViewHolder();
		        
		        rView.setTag(holder);
		        holder.view = rView;
		        
		        convertView = rView;
		        
		    } else {
		        holder = (ViewHolder)convertView.getTag();
		        holder.view.setUserInfo(userinfo);
		    }
			
			return convertView;
		}
		return convertView;
	}

	private TextView generateSpanItem() {
	    TextView but = (TextView)(((Activity) mContext).getLayoutInflater().inflate(R.layout.a_2_z_textview, null));
        but.setTextColor(mContext.getResources().getColor(R.color.atoz_font));
        but.setOnClickListener(null);
        return but;
    }

	static class ViewHolder
	{
		public MulitPhoneEmailItemView view;
	}
}
