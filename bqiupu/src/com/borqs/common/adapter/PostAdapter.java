package com.borqs.common.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.borqs.common.view.AbstractStreamRowView;
import com.borqs.common.view.SNSItemView;
import com.borqs.common.view.StreamApplicationRowView;
import com.borqs.common.view.StreamItemView;
import com.borqs.qiupu.R;
import twitter4j.Stream;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends BaseAdapter{
	private static final String TAG = "Qiupu.PostAdapter";
    public interface OlderPostsLoader {
        public View.OnClickListener getLoadOlderClickListener();
        public int getCaptionResourceId();
    };

    // change it to false if prefer to use traditional StreamItem view. 
    private static boolean STREAM_ROW_VIEW = true;
    protected boolean forappshareview = false;
    private boolean showLoadMoreButton = true;
    
    public void showLoadMoreButton(boolean  showLoadMoreButton) {
        this.showLoadMoreButton = showLoadMoreButton;
    }

    Context mContext;
    private List<Stream> mPosts = new ArrayList<Stream>();
//    View.OnClickListener mLoadOlderListener;
    WeakReference<OlderPostsLoader> mOlderPostsLoader;

    public static PostAdapter newInstance(Context context, OlderPostsLoader loader) {
    	return newInstance(context, loader, false);
    }
    public static PostAdapter newInstance(Context context, OlderPostsLoader loader, boolean forapp) {
        if (STREAM_ROW_VIEW) {
        	return new StreamRowAdapter(context, loader, forapp);
        } else {
            return new PostAdapter(context, loader);
        }
    }

    public static AbstractStreamRowView newStreamItemView(Context context, Stream stream, boolean forComment) {
        if (STREAM_ROW_VIEW) {
            return AbstractStreamRowView.newInstance(context, stream, forComment);
        }
        return new StreamItemView(context, stream, forComment);
    }
    
	PostAdapter(Context context, OlderPostsLoader loader) {
		mContext = context;
		if(showLoadMoreButton) {
		    mOlderPostsLoader = new WeakReference<OlderPostsLoader>(loader);
		}
	}

	public int getCount() {
		return (mPosts != null && mPosts.size()>0)? (showLoadMoreButton?mPosts.size()+1:mPosts.size()):0;
	}
	
	public Stream getItem(int pos) {
	    if(pos >= mPosts.size())
	    {
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
            mLoadMoreButton = (Button)(((Activity) mContext).getLayoutInflater().inflate(R.layout.more_button_stream, null));
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
	
	static class ViewHolder
	{
		public SNSItemView view;
	}	

    public void alterDataList(List<Stream> dataList) {
        // TODO: invalid check and necessary check.
        mPosts.clear();
		mPosts.addAll(dataList);

		notifyDataSetChanged();
    }

    protected ViewHolder touchStreamRowView(View convertView, Stream post) {
        ViewHolder holder;

        if (null != convertView && (convertView instanceof AbstractStreamRowView || convertView instanceof StreamItemView)) {
            holder = (ViewHolder)convertView.getTag();
            if (holder.view instanceof StreamItemView) {
                ((StreamItemView)holder.view).setContent(post);
            } 
            else if(holder.view instanceof StreamApplicationRowView) {
                ((StreamApplicationRowView)holder.view).setContent(post);	
            }else
            {
                Log.w(TAG, "touchStreamRowView, unexpected type of view in view holder.");
            }
        } else  {
        	
        	if(forappshareview == false)
        	{
	            StreamItemView view = new StreamItemView(mContext, post);
	            view.setContent(post);
	            holder = new ViewHolder();
	            holder.view =  view;
	            view.setTag(holder);
        	}
        	else
        	{
        		StreamApplicationRowView view = new StreamApplicationRowView(mContext, post);
	            view.setContent(post);
	            holder = new ViewHolder();
	            holder.view =  view;
	            view.setTag(holder);
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
}

class StreamRowAdapter extends PostAdapter {
    private static final String TAG = "Qiupu.StreamRowAdapter";

    StreamRowAdapter(Context context,  OlderPostsLoader loader, boolean forapp) {
        super(context, loader);

        forappshareview = forapp;
    }

    @Override
    protected ViewHolder touchStreamRowView(View convertView, Stream post) {

        ViewHolder holder;

        if (null != convertView && convertView instanceof AbstractStreamRowView) {
            holder = (ViewHolder)convertView.getTag();
            if (holder.view instanceof AbstractStreamRowView) {
                ((AbstractStreamRowView)holder.view).setContent(post);
            } else {
                Log.w(TAG, "touchStreamRowView, unexpected type of view in view holder.");
            }
        } else  {
            if(forappshareview == false)
        	{
                AbstractStreamRowView view = AbstractStreamRowView.newInstance(mContext, post, false);
	            view.setContent(post);
	            holder = new ViewHolder();
	            holder.view =  view;
	            view.setTag(holder);
        	}
        	else
        	{
        		StreamApplicationRowView view = new StreamApplicationRowView(mContext, post);
	            view.setContent(post);
	            holder = new ViewHolder();
	            holder.view =  view;
	            view.setTag(holder);
        	}
        }

        return holder;
    }
}
