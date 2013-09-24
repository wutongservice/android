package com.borqs.common.adapter;

import java.util.ArrayList;
import java.util.List;

import twitter4j.QiupuSimpleUser;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.view.RecommendItemView;
import com.borqs.common.view.UserSelectItemView;

public class RecommendUserAdapter extends BaseAdapter {
    private static final String TAG = "RecommendUserAdapter";
    private ArrayList<QiupuSimpleUser> mRecommendUserList = new ArrayList<QiupuSimpleUser>();
    protected Context mContext;

    public RecommendUserAdapter(Context context, List<QiupuSimpleUser> userslist) {
        mContext = context;
        mRecommendUserList.clear();
        mRecommendUserList.addAll(userslist);
    }
    
    public RecommendUserAdapter(Context context) {
    	mContext = context;
    }

    public int getCount() {
        return mRecommendUserList.size();
    }

    public QiupuSimpleUser getItem(int position) {
        return mRecommendUserList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final QiupuSimpleUser info = getItem(position);
        final ViewHolder holder;

        if (convertView == null || false == (convertView instanceof UserSelectItemView) ) {
            RecommendItemView FView = new RecommendItemView(mContext, info);
            holder = new ViewHolder();
            FView.setTag(holder);
            holder.view = FView;
            convertView = FView;
        } else {
            holder = (ViewHolder)convertView.getTag();
            holder.view.setUserItem(info);
        }
        return convertView;
    }

    static class ViewHolder {
        public RecommendItemView view;
    }

	public void alterDate(List<QiupuSimpleUser> friends) {
		 notifyDataSetChanged();
	}
  
}