package com.borqs.common.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.borqs.common.view.UserDetailAppsItemView;
import com.borqs.qiupu.db.QiupuORM;
import twitter4j.ApkResponse;

public class FriendsApplicationAdapter extends BaseAdapter{
	private static final String TAG = "Qiupu.FriendsApplicationAdapter";
    private Context mContext;
    private Cursor mCursor;
//    private ArrayList<ApkResponse> mUserApks = new ArrayList<ApkResponse>();
//    private Handler mHandler;
    
//    public void setHandler(Handler handler){
//    	mHandler = handler;
//    }
    
    public void setData(Cursor data) {
		mCursor = data;
	}

	public FriendsApplicationAdapter(Context context){
		mContext = context;
	}
	public int getCount() {
		return mCursor != null ? mCursor.getCount():0;
	}
	
	public ApkResponse getItem(int position) {
        ApkResponse apk = null;
		if (mCursor != null) {
            final int curPosistion = mCursor.getPosition();
            mCursor.moveToPosition(position);
            apk = QiupuORM.createApkResponse(mCursor);
            mCursor.moveToPosition(curPosistion);
        }
        return apk;
	}
	
	public long getItemId(int position) {
		return -1;
	}

	
	public View getView(int position, View convertView, ViewGroup parent) {
		final ApkResponse apkinfo=getItem(position);
		final ViewHolder holder;
		if (convertView == null) 
	    {
			UserDetailAppsItemView view = new UserDetailAppsItemView(mContext, apkinfo);
			view.setapkItem(apkinfo);
			holder = new ViewHolder();
			holder.view =  view;	   
			view.setTag(holder);
	    } else {
	    	holder = (ViewHolder)convertView.getTag();	        
			holder.view.setapkItem(apkinfo);
	    }
		return holder.view;
	}
	
	public void alterDataList(Cursor newCursor) {
        if (newCursor == mCursor) {
            return;
        }

        mCursor = newCursor;
        notifyDataSetChanged();
    }
	
	static class ViewHolder
	{
		public UserDetailAppsItemView view;
	}
}
