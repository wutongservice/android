package com.borqs.common.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import com.borqs.common.view.CommentItemView;
import com.borqs.qiupu.R;
import twitter4j.Stream.Comments;
import twitter4j.Stream.Comments.Stream_Post;

import java.util.List;

public class CommentsAdapter extends BaseAdapter {
    private final String TAG = "Qiupu.CommentsAdapter";
    private Context mContext;
    private boolean hasmore = false;
    private boolean mIsOwner;

    private List<Comments.Stream_Post> mCommentItems;

    public CommentsAdapter(Context con, List<Stream_Post> com, boolean hasmore, boolean isOwner) {
        mContext = con;
        mCommentItems = com;
        this.hasmore = hasmore;
        mIsOwner = isOwner;

        Log.d(TAG, "CommentsAdapter, mCommentItems size:" + +mCommentItems.size());
    }


    public int getCount() {
        if (hasmore && mCommentItems.size() > 0)
            return mCommentItems.size() + 1;
        else
            return mCommentItems.size();
    }

    public Object getItem(int pos) {
        final int commentSize = mCommentItems.size();
        if (pos == commentSize)
            return null;

//        final int reversePos = commentSize - 1 - pos;
//        return mCommentItems.get(reversePos);
        return mCommentItems.get(pos);
    }

    public long getItemId(int pos) {
        if (pos == mCommentItems.size())
            return -1;
        return Long.parseLong(mCommentItems.get(pos).uid);
    }

    private Button generateMoreItem() {
        Button but = (Button) (((Activity) mContext).getLayoutInflater().inflate(R.layout.more_button_stream, null));
        but.setBackgroundResource(R.drawable.list_selector_background);
        but.setTextColor(mContext.getResources().getColor(R.color.more_text_color));
        return but;
    }

    public View getView(int position, View convertView, ViewGroup arg2) {
        if (position < 0 || position >= getCount()) {
            return null;
        }

        final ViewHolder holder;
        Comments.Stream_Post di = (Comments.Stream_Post) getItem(position);
        if (di != null) {
            if (convertView == null || false == (convertView instanceof CommentItemView)) {
                holder = new ViewHolder();
                CommentItemView v = new CommentItemView(mContext, di, mIsOwner);
                holder.view = v;
                v.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.view.setCommentItem(di);
            }
            return holder.view;
        } else {
            Button but = generateMoreItem();
//		      if(FacebookCommentsActivity.class.isInstance(mContext))
//		      {
//		    	  FacebookCommentsActivity fs = (FacebookCommentsActivity)mContext;
//		          but.setOnClickListener(fs.loadOlderClick);
//		          if(fs.isInProcess())
//                  {
//                      but.setText(mContext.getString(R.string.loading_string));
//                  }
//		      }		         
            return but;
        }
    }

    static class ViewHolder {
        public CommentItemView view;
    }
}
