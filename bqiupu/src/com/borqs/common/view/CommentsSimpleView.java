package com.borqs.common.view;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.borqs.common.api.BpcApiUtils;
import twitter4j.CacheMap;
import twitter4j.Stream.Comments;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.borqs.qiupu.R;

public class CommentsSimpleView extends SNSItemView {
    private final String TAG = "CommentsSimpleView";

    private TextView message;
    private Context mContext;
    private Comments.Stream_Post comment;

    public CommentsSimpleView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        mContext = ctx;
        setOrientation(LinearLayout.HORIZONTAL);
        this.setVisibility(View.VISIBLE);
    }

    public CommentsSimpleView(Context context, Comments.Stream_Post di) {
        super(context);
        mContext = context;
        init();
    }

    public Comments.Stream_Post getComment() {
        return comment;
    }

    public void setComment(Comments.Stream_Post comment) {
        this.comment = comment;
    }

    private void init() {
        //Log.d(TAG,  "call CommentsSimpleView init");
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();

        //child 1
        View v = factory.inflate(R.layout.comments_simple_item, this, true);
//        v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
//        addView(v);

        message = (TextView) v.findViewById(R.id.tweet_publish_text);

        setCommentsUI();

    }


    private long getFromUID() {
        return Long.parseLong(comment.uid);
    }

    private void setCommentsUI() {
        if (null != comment && null != message) {
            if (CacheMap.class.isInstance(comment)) {
                String cachedData = comment.getCache(CacheMap.SimpleCommentsCached);
                if (isEmpty(cachedData) == false) {
                    stripHtmlUnderlines(message, cachedData);
//                    attachTextViewMovementMethod(message);
                } else {
                    final String content = String.format("<a href='%1$s%2$s'>%3$s</a> %4$s",
                            BpcApiUtils.PROFILE_SEARCH_USERID_PREFIX, comment.uid, comment.username,
                            formatHtmlContent(comment.message));
                    stripHtmlUnderlines(message, content);
//                    attachTextViewMovementMethod(message);

                    comment.cacheMeta(CacheMap.SimpleCommentsCached, content);
                }
            } else {
                final String content = String.format("<a href='%1$s%2$s'>%3$s</a> %4$s",
                        BpcApiUtils.PROFILE_SEARCH_USERID_PREFIX, comment.uid, comment.username,
                        formatHtmlContent(comment.message));
                stripHtmlUnderlines(message, formatHtmlContent(content));
//                attachTextViewMovementMethod(message);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    public void setCommentItem(Comments.Stream_Post di) {
        comment = di;
        setCommentsUI();
    }

    @Override
    public String getText() {
        return comment.message;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);

        if (null != message) {
            message.setOnClickListener(l);
        }
    }
}
