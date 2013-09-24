package com.borqs.common.adapter;

import java.util.ArrayList;

import twitter4j.UserCircle;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.view.RecipientsCirclesItemView;

public class RecipientsCirclesAdapter extends BaseAdapter {

    private static final String TAG = RecipientsCirclesAdapter.class.getSimpleName();

    private Context             mContext;
    private ArrayList<UserCircle> mDataList = new ArrayList<UserCircle>();
    

    public RecipientsCirclesAdapter(Context context,ArrayList<UserCircle> dataList) {
        mContext = context;
        mDataList.addAll(dataList);
    }
    
    public void alterDataList(ArrayList<UserCircle> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public int getCount() {
        return (mDataList != null && mDataList.size()>0)? mDataList.size()
                :0;
    }

    public UserCircle getItem(int position) {
        if(position >= mDataList.size())
        {
            return null;
        }
        return mDataList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        UserCircle depInfo = getItem(position);
        if (depInfo != null) {
            if (convertView == null || false == (convertView instanceof RecipientsCirclesItemView)) {
            	convertView = new RecipientsCirclesItemView(mContext, depInfo);
                holder = new ViewHolder();

                holder.view = (RecipientsCirclesItemView)convertView;
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.view.setDataInfo(depInfo);
            }
        }
        return convertView;

    }

    static class ViewHolder {
        public RecipientsCirclesItemView view;
    }
    
    

}
