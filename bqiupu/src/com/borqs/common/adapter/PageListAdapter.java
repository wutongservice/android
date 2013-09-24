package com.borqs.common.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.PageInfo;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.view.PageListItemView;
import com.borqs.common.view.PageListItemView.followActionListener;
import com.borqs.qiupu.db.QiupuORM;

public class PageListAdapter extends BaseAdapter{
	private static final String TAG = "PageListAdapter";
    private Context mContext;
    private Cursor mPages;
    private ArrayList<PageInfo> mPageList = new ArrayList<PageInfo>();
    
    private HashMap<String, followActionListener> mPageActionMap = new HashMap<String, followActionListener>();

    public void registerPageActionListner(String key, followActionListener rl) {
    	mPageActionMap.put(key, rl);
    }

    public void unregisterPageActionListner(String key) {
    	mPageActionMap.remove(key);
    }
    
    
    private MoreItemCheckListener mCheckerListener;
    public interface MoreItemCheckListener {
        public boolean isMoreItemHidden();
        public View.OnClickListener getMoreItemClickListener();
        public int getMoreItemCaptionId();
    }
    
	public PageListAdapter(Context context){
		mContext = context;
	}
	
	public PageListAdapter(Context context, Cursor list){
        mContext = context;
        if(mPages != null) {
        	mPages.close();
        }
        mPages = list;
    }
	
	public PageListAdapter(Context context, MoreItemCheckListener listener){
		mContext = context;
		mCheckerListener = listener;
	}
	
	public void alterPages(Cursor list){
		if(mPages != null) {
			mPages.close();
	    }
		mPages = list;
		notifyDataSetChanged();
	}
	
	public void alertpages(ArrayList<PageInfo> pagelist) {
		if(mPageList != null) {
			mPageList.clear();
			mPageList.addAll(pagelist);
		}
		notifyDataSetChanged();
	}
	
	public int getCount() {
	    int count = 0;
	    if(mPages != null && mPages.getCount() > 0) {
            count = mPages.getCount();
        } else if(mPageList != null && mPageList.size() > 0) {
        	count = mPageList.size();
        	if (null != mCheckerListener && mCheckerListener.isMoreItemHidden()) {
			    ++count;
			}
        }
        return count;
	}
	
	public PageInfo getItem(int position) {
	    Log.d(TAG, "getItem:  " + position);
	    if(mPages != null && mPages.moveToPosition(position)){
			PageInfo info = QiupuORM.createPageListInformation(mContext, mPages);
			return info;
		}else if(mPageList != null && position < mPageList.size()) {
			return mPageList.get(position);
		}else{
			return null;
		}
	}
	
	public long getItemId(int position) {
		PageInfo info =  getItem(position);
		return info !=null ? info.page_id : -1;
	}

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        PageInfo info = getItem(position);
        if (info != null) {
            if (convertView == null
                    || false == (convertView instanceof PageListItemView)) {
            	PageListItemView rView = new PageListItemView(mContext, info);
                holder = new ViewHolder();

                rView.setTag(holder);
                holder.view = rView;
                convertView = rView;
                rView.attachActionListener(mPageActionMap);

            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.view.setPage(info);
                holder.view.attachActionListener(mPageActionMap);
            }
        }
        return convertView;

    }

	static class ViewHolder
	{
		public PageListItemView view;
	}

    public void clearCursor() {
    	QiupuORM.closeCursor(mPages);
    }
}
