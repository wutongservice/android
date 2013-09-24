package com.borqs.common.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

import twitter4j.PollInfo;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.borqs.common.view.PollItemView;
import com.borqs.qiupu.R;

public class PollListAdapter extends BaseAdapter {

    private static final String TAG = "PollListAdapter";

    private Context             mContext;
    private boolean mShowMoreButton;
    private ArrayList<PollInfo> mPollList = new ArrayList<PollInfo>();
    
    WeakReference<LoaderMoreListener> loaderMore;
    public interface LoaderMoreListener {
        public int getCaptionResourceId();
        
        public View.OnClickListener loaderMoreClickListener();
    }

    public PollListAdapter(Context context,ArrayList<PollInfo> dataList,boolean showMoreButton) {
    	mShowMoreButton = showMoreButton;
        mContext = context;
        mPollList.addAll(dataList);
        Collections.sort(mPollList, PollInfo.COMPARATOR);
        loaderMore = new WeakReference<PollListAdapter.LoaderMoreListener>((LoaderMoreListener)context);
    }
    
    public PollListAdapter(Context context,ArrayList<PollInfo> dataList,boolean showMoreButton, LoaderMoreListener morelistener) {
    	mShowMoreButton = showMoreButton;
        mContext = context;
        mPollList.addAll(dataList);
        Collections.sort(mPollList, PollInfo.COMPARATOR);
        loaderMore = new WeakReference<PollListAdapter.LoaderMoreListener>(morelistener);
    }

    public void alterPollList(ArrayList<PollInfo> dataList,boolean showMoreButton) {
    	mShowMoreButton = showMoreButton;
        mPollList.clear();
        mPollList.addAll(dataList);
        Collections.sort(mPollList, PollInfo.COMPARATOR);
        notifyDataSetChanged();
    }

    public int getCount() {
        return (mPollList != null && mPollList.size()>0)? (mShowMoreButton ? mPollList.size()+1 : mPollList.size())
                :0;
    }

    public PollInfo getItem(int position) {
//        Log.d(TAG, "getItem:  " + position);
        if(position >= mPollList.size())
        {
            return null;
        }
        return mPollList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        PollInfo pollInfo = getItem(position);
        if (pollInfo != null) {
            if (convertView == null || false == (convertView instanceof PollItemView)) {
                PollItemView itemView = new PollItemView(mContext, pollInfo);
                holder = new ViewHolder();

                itemView.setTag(holder);
                holder.view = itemView;
                convertView = itemView;

            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.view.setPollInfo(pollInfo);
            }
        }else {
            generateMoreItem();
            refreshLoadingStatus();

            return mLoadMoreButton;
        }
        return convertView;

    }

    static class ViewHolder {
        public PollItemView view;
    }
    
    Button mLoadMoreButton;
    private void generateMoreItem() {
        if (null == mLoadMoreButton) {
            mLoadMoreButton = (Button)(((Activity) mContext).getLayoutInflater().inflate(R.layout.more_button_stream, null));
            mLoadMoreButton.setTextColor(mContext.getResources().getColor(R.color.more_text_color));
            mLoadMoreButton.setBackgroundResource(R.drawable.list_selector_background);
        }
    }
    
    public void refreshLoadingStatus() {
        if (loaderMore != null && loaderMore.get() != null) {
            int resId = loaderMore.get().getCaptionResourceId();
            if (null != mLoadMoreButton && resId > 0) {
                mLoadMoreButton.setHeight((int) mContext.getResources().getDimension(R.dimen.more_button_height));
                mLoadMoreButton.setOnClickListener(loaderMore.get().loaderMoreClickListener());
                mLoadMoreButton.setText(resId);
            }
        }

    }
    

}
