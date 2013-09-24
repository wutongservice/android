package com.borqs.common.adapter;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-11-20
 * Time: 下午3:39
 * To change this template use File | Settings | File Templates.
 */

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import com.borqs.qiupu.R;
import twitter4j.Stream;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


import com.borqs.common.view.AbstractStreamRowView;
import com.borqs.common.view.SNSItemView;

public class StreamListAdapter extends BaseAdapter {
    private static final String TAG = "StreamListAdapter";

    public interface OlderPostsLoader {
        public View.OnClickListener getLoadOlderClickListener();

        public int getCaptionResourceId();
    }

    private boolean showLoadMoreButton = true;

    public void showLoadMoreButton(boolean showLoadMoreButton) {
        this.showLoadMoreButton = showLoadMoreButton;
    }

    Context mContext;
    private List<Stream> mPosts = new ArrayList<Stream>();
    WeakReference<OlderPostsLoader> mOlderPostsLoader;

    public static StreamListAdapter newInstance(Context context, OlderPostsLoader loader) {
        return newInstance(context, loader, false);
    }

    public static StreamListAdapter newInstance(Context context, OlderPostsLoader loader, boolean forapp) {
        return new StreamListAdapter(context, loader);
    }

    public static AbstractStreamRowView newStreamItemView(Context context, Stream stream, boolean forComment) {
        return AbstractStreamRowView.newInstance(context, stream, forComment);
    }

    StreamListAdapter(Context context, OlderPostsLoader loader) {
        mContext = context;
        if (showLoadMoreButton) {
            mOlderPostsLoader = new WeakReference<OlderPostsLoader>(loader);
        }
    }

    public int getCount() {
        return (mPosts != null && mPosts.size() > 0) ? (showLoadMoreButton ? mPosts.size() + 1 : mPosts.size()) : 0;
    }

    public Stream getItem(int pos) {
        if (pos >= mPosts.size()) {
            return null;
        }
        return mPosts.get(pos);
    }

    public long getItemId(int position) {
        return position;
    }

    Button mLoadMoreButton;

    private void generateMoreItem() {
        if (null == mLoadMoreButton) {
            mLoadMoreButton = (Button) (((Activity) mContext).getLayoutInflater().inflate(R.layout.more_button_stream, null));
            mLoadMoreButton.setTextColor(mContext.getResources().getColor(R.color.more_text_color));
            mLoadMoreButton.setBackgroundResource(R.drawable.list_selector_background);
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final Stream post = getItem(position);
        if (post != null) {
            holder = touchStreamRowView(convertView, post);
        } else {
            generateMoreItem();
            refreshLoadingStatus();

            return mLoadMoreButton;
        }
        return holder.view;
    }

    static class ViewHolder {
        public SNSItemView view;
    }

    public void alterDataList(List<Stream> dataList) {
        mPosts.clear();
        mPosts.addAll(dataList);

        notifyDataSetChanged();
    }

    protected ViewHolder touchStreamRowView(View convertView, Stream post) {
        final int targetLayoutId = AbstractStreamRowView.getTargetLayoutTag(post);
        ViewHolder holder = null;
        if (null != convertView && convertView instanceof AbstractStreamRowView) {
            ViewHolder tag = (ViewHolder) convertView.getTag();
            if (tag.view instanceof AbstractStreamRowView) {
                AbstractStreamRowView rowView = (AbstractStreamRowView)tag.view;
                if (targetLayoutId == rowView.getTargetLayoutId()) {
                    rowView.setContent(post);
                    holder = tag;
                }
            } else {
                Log.w(TAG, "touchStreamRowView, unexpected type of view in view holder.");
            }
        }

        if (holder == null) {
            AbstractStreamRowView view = AbstractStreamRowView.newInstance(mContext, targetLayoutId, post);
            holder = new ViewHolder();
            holder.view = view;
            view.setTag(holder);
            if (!post.mWasAnimated) {
                post.mWasAnimated = true;
                view.startCreationAnimation();
            }
        }

        return holder;
    }

    public void refreshLoadingStatus() {
        if (mOlderPostsLoader != null && mOlderPostsLoader.get() != null) {
            int resId = mOlderPostsLoader.get().getCaptionResourceId();
            if (null != mLoadMoreButton && resId > 0) {
                mLoadMoreButton.setHeight((int) mContext.getResources().getDimension(R.dimen.more_button_height));
                mLoadMoreButton.setOnClickListener(mOlderPostsLoader.get().getLoadOlderClickListener());
                mLoadMoreButton.setText(resId);
            }
        }
    }
    
    public boolean isNeedLoadMore() {
    	if(mLoadMoreButton != null) {
    		return mContext.getResources().getString(R.string.list_view_more).equals(mLoadMoreButton.getText().toString());
    	}
    	return false;
    }
}

