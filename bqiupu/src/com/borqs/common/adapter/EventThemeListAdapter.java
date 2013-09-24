package com.borqs.common.adapter;

import twitter4j.EventTheme;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.view.EventThemeItemView;
import com.borqs.qiupu.db.QiupuORM;

public class EventThemeListAdapter extends BaseAdapter {
	private static final String TAG = "EventThemeListAdapter";
	private Cursor mThemeCursor;
	private Context mContext;

	public EventThemeListAdapter(Context context) {
		mContext = context;
	}

	public int getCount() {
		if (mThemeCursor == null) {
			return 0;
		} else {
			return mThemeCursor.getCount();
		}
	}

	public EventTheme getItem(int position) {

		EventTheme theme = null;
		if(mThemeCursor != null && mThemeCursor.moveToPosition(position)){
			theme = QiupuORM.createEventThemeInfo(mThemeCursor);
		} 
		return theme;
	}

	public long getItemId(int position) {
		EventTheme theme = getItem(position);
		return theme != null ? theme.id : -1;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		EventTheme theme = getItem(position);
		if (theme != null) {
			if (convertView == null
					|| false == (convertView instanceof EventThemeItemView)) {
				EventThemeItemView FView = new EventThemeItemView(mContext,
						theme);
				holder = new ViewHolder();

				FView.setTag(holder);
				holder.view = FView;

				convertView = FView;

			} else {
				holder = (ViewHolder) convertView.getTag();
				holder.view.setItem(theme);
			}
		} 
		return convertView;

	}

	static class ViewHolder {
		public EventThemeItemView view;
	}

	public void alterDataList(Cursor themeCursor) {
		if(mThemeCursor != null) {
			mThemeCursor.close();
        }
		mThemeCursor = themeCursor;

		notifyDataSetChanged();
	}
}
