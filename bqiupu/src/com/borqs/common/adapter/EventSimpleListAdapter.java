package com.borqs.common.adapter;

import twitter4j.UserCircle;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

import com.borqs.common.view.SimpleEventItemView;

public class EventSimpleListAdapter extends EventListAdapter{
	private static final String TAG = "EventSimpleListAdapter";
    
    
	public EventSimpleListAdapter(Context context){
		super(context);
	}
	
	public EventSimpleListAdapter(Context context, Cursor events){
		super(context, events);
    }
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        UserCircle circle = getItem(position);
        if (circle != null) {
            if (convertView == null
                    || false == (convertView instanceof SimpleEventItemView)) {
            	SimpleEventItemView rView = new SimpleEventItemView(mContext, circle);
                holder = new ViewHolder();

                rView.setTag(holder);
                holder.view = rView;
                convertView = rView;

            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.view.setCircle(circle);
            }
        }
        return convertView;

    }

	static class ViewHolder
	{
		public SimpleEventItemView view;
	}
}
