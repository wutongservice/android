package com.borqs.common.adapter;

import java.util.ArrayList;
import java.util.List;

import twitter4j.QiupuSimpleUser;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.borqs.common.view.UserSelectItemView;
import com.borqs.qiupu.R;

public class DialogUsersAdapter extends BaseAdapter {
    private static final String TAG = "DialogUsersAdapter";
    private ArrayList<QiupuSimpleUser> mAllUsers = new ArrayList<QiupuSimpleUser>();
    private ArrayList<QiupuSimpleUser> mPageUsers = new ArrayList<QiupuSimpleUser>();
    protected Context mContext;
    private int pagecout = 20;
    private int page = 1;

    public DialogUsersAdapter(Context context, List<QiupuSimpleUser> userslist) {
        mContext = context;
        mAllUsers.clear();
        
        if(userslist != null)
            mAllUsers.addAll(userslist);
        
        if(userslist != null && userslist.size() <= pagecout)
            mPageUsers.addAll(userslist);
        else
        {
            mPageUsers.addAll(mAllUsers.subList(0, pagecout));
        }
    }
    
    public DialogUsersAdapter(Context context) {
    	mContext = context;
    }

    public int getCount() {
        if(mPageUsers.size() < mAllUsers.size()){
            return mPageUsers.size() + 1;
        }
        else{
            return mPageUsers.size();
        }
    }

    public QiupuSimpleUser getItem(int position) {
    	if(position < mPageUsers.size())
		{
		    return mPageUsers.get(position);
		}
		else
		{
			return null;
		}
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final QiupuSimpleUser info = getItem(position);
        final ViewHolder holder;

        if (null != info) {
            if (convertView == null || false == (convertView instanceof UserSelectItemView) )
            {
                UserSelectItemView FView = new UserSelectItemView(mContext, info, true);
                holder = new ViewHolder();
                
                FView.setTag(holder);
                holder.view = FView;
                
                convertView = FView;
                
            } else {
                holder = (ViewHolder)convertView.getTag();
                holder.view.setUserItem(info);
            }
        }
        else{
            Button but = generateMoreItem();
            but.setHeight((int) mContext.getResources().getDimension(R.dimen.more_button_height));
            but.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    gotoloadMoreData();
                }
            });
            return but;
        }

        return convertView;
    }
    
    private Button generateMoreItem() {
        Button but = (Button)(((Activity) mContext).getLayoutInflater().inflate(R.layout.more_button_stream, null));
        but.setTextColor(mContext.getResources().getColor(R.color.more_text_color));
	    but.setBackgroundResource(R.drawable.list_selector_background);
        return but;
    }
    
    private void gotoloadMoreData(){
        page += 1;
        mPageUsers.clear();
        if(pagecout*page < mAllUsers.size())
        {
            mPageUsers.addAll(mAllUsers.subList(0, pagecout * page));
        }
        else{
            mPageUsers.addAll(mAllUsers.subList(0, mAllUsers.size()));
        }
        notifyDataSetChanged();
    }

    static class ViewHolder {
        public UserSelectItemView view;
    }

    //TODO
    //FIX ???
	public void alterDate(List<QiupuSimpleUser> friends) {
		 mPageUsers.clear();
		 mPageUsers.addAll(friends);
		 
		 notifyDataSetChanged();
	}
  
}