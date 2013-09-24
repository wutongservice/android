package com.borqs.common.adapter;

import twitter4j.UserCircle;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.view.DepartmentItemView;
import com.borqs.qiupu.db.QiupuORM;

public class DepartmentGridviewAdapter extends BaseAdapter {

    private Context             mContext;
    private Cursor mCursor;
    
    public DepartmentGridviewAdapter(Context context, Cursor refrerCursor) {
        super();

        mContext = context;
        mCursor = refrerCursor;
    }
    
    public DepartmentGridviewAdapter(Context context) {
        super();
        mContext = context;
    }

    public int getCount() {
    	int count = 0;
    	if(mCursor != null && mCursor.getCount() > 0) {
    		count = mCursor.getCount(); 
        }
    	return count;		
    }

    public UserCircle getItem(int position) {
    	if(mCursor != null && mCursor.moveToPosition(position)) {
    		UserCircle circle = QiupuORM.createCircleCircles(mCursor);
            return circle;
        }else {
        	return null;
        }
    }

    public long getItemId(int position) {
        return position;
    }
    
    public void alertData(Cursor cursor) {
//    	if(mCursor != null) {
//    		mCursor.close();
//    	}
    	mCursor = cursor;
    	notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        UserCircle depInfo = getItem(position);
        if (depInfo != null) {
            if (convertView == null || false == (convertView instanceof DepartmentItemView)) {
            	convertView = new DepartmentItemView(mContext, depInfo, true);
                holder = new ViewHolder();

                holder.view = (DepartmentItemView)convertView;
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.view.setDataInfo(depInfo);
            }
        }
        return convertView;
    }
    
    static class ViewHolder {
        public DepartmentItemView view;
    }
}
