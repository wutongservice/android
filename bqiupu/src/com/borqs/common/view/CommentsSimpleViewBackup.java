package com.borqs.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.util.DateUtil;
import twitter4j.CacheMap;
import twitter4j.Stream.Comments;

import java.util.Date;

public class CommentsSimpleViewBackup extends SNSItemView {
    private final String TAG="CommentsSimpleView";

    private TextView                username;
    private TextView                message;
    private TextView                publishTime;
    private Context                 mContext;
    private Comments.Stream_Post    comment;

    public CommentsSimpleViewBackup(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
        mContext = ctx;
        setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);
    }

    public CommentsSimpleViewBackup(Context context, Comments.Stream_Post di)
    {       
        super(context);
        mContext = context;
        //Log.d(TAG, "call CommentsSimpleView");
        init();
    }
    
    public Comments.Stream_Post getComment() {
        return comment;
    }

    public void setComment(Comments.Stream_Post comment) {
        this.comment = comment;
    }

    private void init() 
    {
        //Log.d(TAG,  "call CommentsSimpleView init");
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        
        //child 1
        View v  = factory.inflate(R.layout.comments_simple_item_backup, null);
        v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    LayoutParams.WRAP_CONTENT));
        addView(v);            
        
        publishTime  = (TextView)v.findViewById(R.id.tweet_publish_time);
        message      = (TextView)v.findViewById(R.id.tweet_publish_text);       
        username     = (TextView)v.findViewById(R.id.tweet_user_name);
        
        setCommentsUI();        
    
    }   
    
   
    
    private long getFromUID()
    {
        return Long.parseLong(comment.uid);        
    }
    
    private void setCommentsUI()
    { 
    	if(comment != null)
    	{
	        if(comment!=null && isEmpty(comment.username) == false)
	        {
//	            username.setText(comment.username);
                attachHtmlTextView(username, comment.username, CacheMap.CommentsUserName);
	        }
	        
	        publishTime.setText(DateUtil.converToRelativeTime(mContext, new Date(getCreateTime())));
//            stripHtmlUnderlines(message, comment.message);
            attachHtmlTextView(message, comment.message, CacheMap.CommentsMessageCahed);
    	}
    }
    
    private long getCreateTime()
    {
        return comment.created_time;        
    }
   
    @Override
    protected void onFinishInflate() 
    {   
        super.onFinishInflate();        
        init();
    }
    
    public void setCommentItem(Comments.Stream_Post di) 
    {
        comment = di;
        setCommentsUI();
    }   

	@Override
	public String getText() 
	{		
		return comment.message;
	}

    @Override
    protected Object getAttachedObject() {
        return comment;
    }
}
