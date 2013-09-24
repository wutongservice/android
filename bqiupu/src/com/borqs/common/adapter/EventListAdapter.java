package com.borqs.common.adapter;

import java.util.ArrayList;

import twitter4j.UserCircle;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.view.EventItemView;
import com.borqs.qiupu.db.QiupuORM;

public class EventListAdapter extends BaseAdapter{
	private static final String TAG = "EventListAdapter";
	private Cursor mEvents;
    protected Context mContext;
    
    private ArrayList<UserCircle> mEventList = new ArrayList<UserCircle>();
    
	public EventListAdapter(Context context){
		mContext = context;
	}
	
	public EventListAdapter(Context context, Cursor events){
        mContext = context;
        if(mEvents != null) {
        	mEvents.close();
        }
        mEvents = events;
    }
	
	public void alterCircles(Cursor event){
	    if(mEvents != null) {
	    	mEvents.close();
	    }
	    mEvents = event;
		notifyDataSetChanged();
	}
	public void alterCircles(ArrayList<UserCircle> events) {
		mEventList.clear();
		mEventList.addAll(events);
		notifyDataSetChanged();
	}
	
	public int getCount() {
	    int count = 0;
        if(mEvents != null && mEvents.getCount() > 0) {
            count = mEvents.getCount();
        }else if(mEventList != null && mEventList.size() > 0) {
        	count = mEventList.size();
        }
        return count;
	}
	
	public UserCircle getItem(int position) {
	    Log.d(TAG, "getItem:  " + position);
		if(mEvents != null && mEvents.moveToPosition(position)){
			UserCircle circle = QiupuORM.createEventListInformation(mContext, mEvents);
			return circle;
		}else if(mEventList.size() > 0 && position < mEventList.size()) {
			return mEventList.get(position);
		}else{
			return null;
		}
	}
	
	public long getItemId(int position) {
	    UserCircle circle =  getItem(position);
		return circle !=null ? circle.type : -1;
	}

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        UserCircle circle = getItem(position);
        if (circle != null) {
            if (convertView == null
                    || false == (convertView instanceof EventItemView)) {
            	EventItemView rView = new EventItemView(mContext, circle);
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
		public EventItemView view;
	}
	
    
    public void clearCursor() {
    	QiupuORM.closeCursor(mEvents);
    }
}
