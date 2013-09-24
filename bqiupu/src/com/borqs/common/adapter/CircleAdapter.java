package com.borqs.common.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.listener.CheckBoxClickActionListener;
import com.borqs.common.view.CircleItemView;
import twitter4j.UserCircle;

import java.util.ArrayList;
import java.util.HashMap;

public class CircleAdapter extends BaseAdapter{
	private static final String TAG = "CircleAdapter";
    private ArrayList<UserCircle> items;
    private Context mContext;
    private boolean mIsShowCreate = true;
    
    private HashMap<String, CheckBoxClickActionListener> mCheckClickListenerMap = new HashMap<String, CheckBoxClickActionListener>();

    public void registerCheckClickActionListener(String key, CheckBoxClickActionListener rl) {
        mCheckClickListenerMap.put(key, rl);
    }

    public void unRegisterCheckClickActionListener(String key) {
        mCheckClickListenerMap.remove(key);
    }
    
	public void setItems(ArrayList<UserCircle> items) {
		this.items = items;
	}

	public CircleAdapter(Context context, ArrayList<UserCircle> list, boolean isShowCreate){
		mContext = context;
		items = list;
		mIsShowCreate = isShowCreate;
	}
	
	public CircleAdapter(Context context)
	{
		mContext = context;
		items = new ArrayList<UserCircle>();
	}
	public int getCount() {
		if(items == null){
			return 0;
        } else {
            return mIsShowCreate ? items.size() + 1 : items.size();
        }
	}
	
	public UserCircle getItem(int position) {
		if(mIsShowCreate) {
			if(position == 0)
				return null;
			else
			{
				if(items != null && (position - 1) < items.size())
				{
					return items.get(position - 1);
				}
				else
				{
					return null;
				}
			}
		}else {
			if(items != null && position < items.size()) {
				return items.get(position);
			}
			return null;
		}
	}
	
	public long getItemId(int position) {
		UserCircle circle =  getItem(position);
		return circle!=null?circle.uid:-1;
	}

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        UserCircle circle = getItem(position);

        if (convertView == null || !(convertView instanceof CircleItemView)) {
            CircleItemView FView = new CircleItemView(mContext, circle);
            FView.attachCheckListener(mCheckClickListenerMap);
            holder = new ViewHolder();

            FView.setTag(holder);
            holder.view = FView;

            convertView = FView;

        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.view.setCircle(circle);
            holder.view.attachCheckListener(mCheckClickListenerMap);
        }

        return convertView;
    }

	static class ViewHolder
	{
		public CircleItemView view;
	}
	
	public void alterDataList(ArrayList<UserCircle> newList) {
		items.clear();
		
		if (null != newList) {
			items.addAll(newList);
		}
		notifyDataSetChanged();
	}
}
