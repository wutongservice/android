package com.borqs.common.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.listener.CommentActionListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.SmileyParser;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageCacheManager;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.QiupuCommentsActivity;
import com.borqs.qiupu.util.DateUtil;

import twitter4j.CacheMap;
import twitter4j.QiupuSimpleUser;
import twitter4j.QiupuUser;
import twitter4j.Stream.Comments;

import java.lang.ref.WeakReference;
import java.util.Date;

public class CommentItemView extends BaseEsItemView {
    private static final String TAG="CommtentItemView";

    private TextView                username;   
    private ImageView               userLogo;
    private TextView                message;
    private TextView                publishTime;
    private TextView                referredComment;
    
    private Comments.Stream_Post    comment;    
    private Context mContext;

    private boolean mIsOwner;

    private static WeakReference<CommentActionListener> mCommentActionListener;

    public static void setCommentActionListener(CommentActionListener commentActionListener) {
        mCommentActionListener = new WeakReference<CommentActionListener>(commentActionListener);
    }

    public CommentItemView(Context ctx, AttributeSet attrs) 
    {
        super(ctx, attrs);      
        mContext = ctx;  
        setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);
    }

    public CommentItemView(Context context, Comments.Stream_Post di, boolean isOwner)
    {       
        super(context);
        mContext = context;
        comment = di;
        mIsOwner = isOwner;
        Log.d(TAG, "call CommtentItemView");
        init();
    }
    
    public Comments.Stream_Post getComment() {
        return comment;
    }

    private void init() 
    {
        Log.d(TAG,  "call CommtentItemView init");
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        
        //child 1
        View v  = factory.inflate(R.layout.comments_item, null);      
        v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(v);

        v.setOnClickListener(itemListener);

        userLogo     = (ImageView)v.findViewById(R.id.tweet_img_ui);            
        publishTime  = (TextView)v.findViewById(R.id.tweet_publish_time);
        message      = (TextView)v.findViewById(R.id.tweet_publish_text);
        referredComment = (TextView)v.findViewById(R.id.tweet_referred_text);
        username     = (TextView)v.findViewById(R.id.tweet_user_name);

        userLogo.setOnClickListener(viewUserDetailsClick);
        username.setOnClickListener(viewUserDetailsClick);

//        setBackgroundColor(Color.TRANSPARENT);

        setCommentsUI();
    
    }   

    private View.OnClickListener itemListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null != mCommentActionListener && mCommentActionListener.get() != null) {
                mCommentActionListener.get().commentItemListener(comment);
            }
        }
    };

    View.OnClickListener viewUserDetailsClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            Log.d(TAG, "viewUserDetailsClick you click first one=");
            IntentUtil.startUserDetailIntent(getContext(), Long.valueOf(comment.uid), comment.username);
        }
    };

    View.OnClickListener deleteCommentsClick = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "deleteCommentsClick you click first one=");
            DialogUtils.showConfirmDialog(mContext, R.string.delete_comment_title,
                    R.string.delete_comment_message, R.string.label_ok, R.string.label_cancel, 
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deleteCommentAfterConfirm();
                }
            });
        }
    };

    private void deleteCommentAfterConfirm() {
        if (QiupuCommentsActivity.class.isInstance(mContext)) {
            QiupuCommentsActivity qa = (QiupuCommentsActivity) mContext;
            qa.deleteComments(comment);
        } else {
            Log.e(TAG, "deleteCommentsClick.onClick, unexpected context, skip");
        }
    }

    View.OnClickListener showLikerClicker = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "showLikerClicker, onClick");
            if (null != comment && null != comment.likerList && comment.likerList.count > 0) {
                Log.d(TAG, "liker count: " + comment.likerList.count);
                
                IntentUtil.ShowUserList(getContext(), 
                        mContext.getString(R.string.dialog_like_user_title), comment.likerList, DialogUtils.CommentLike, String.valueOf(comment.id));
            }
        }
    };
    
    private long getFromUID()
    {
        return Long.parseLong(comment.uid);        
    }

    private void setCommentsUI() {
//        final String profileUrl;
        String usernameCached = comment.getCache(CacheMap.CommentsUserName);
        if (isEmpty(usernameCached)) {
//            QiupuSimpleUser user = orm.querySimpleUserInfo(getFromUID());
            String nameText = null;
//            if (user != null) {
//                nameText = user.nick_name;
//                profileUrl = user.profile_image_url;
//            } else {
//                profileUrl = "";
//            }

            if (comment != null && isEmpty(comment.username) == false) {
                nameText = comment.username;
            }

            String formatedUserName = String.format("<a href=\'borqs://profile/details?uid=%1$s\'>%2$s</a>", getFromUID(), nameText);
            comment.cacheMeta(CacheMap.CommentsUserName, formatedUserName);

            attachHtmlTextView(username, formatedUserName, CacheMap.CommentsUserName);
        } else {
//            profileUrl = orm.getUserImageUrl(Long.parseLong(comment.uid));
            attachHtmlTextView(username, usernameCached, CacheMap.CommentsUserName);
        }

        userLogo.setImageResource(R.drawable.default_user_icon);
//        if (!TextUtils.isEmpty(profileUrl)) {
        if (!TextUtils.isEmpty(comment.image_url)) {
            postDelayLoadImage();
        }

        publishTime.setText(DateUtil.converToRelativeTime(mContext, new Date(getCreateTime())));

        if (comment.referredId <= 0 || null == comment.referredComment) {
            referredComment.setVisibility(GONE);
        } else {
            referredComment.setVisibility(VISIBLE);
            final String referredText = comment.referredComment.asRepliedText();
            final String showText = getContext().getString(R.string.comment_referred_text,
                    comment.referredComment.asMentionUser(),
//                    comment.referredComment.username,
                    referredText);
            attachHtmlTextView(referredComment, showText, CacheMap.CommentsReferredCache);
        }

        attachHtmlTextView(message, comment.message, CacheMap.CommentsMessageCahed);

        updateLikeCountUi(comment);
    }

    private void postDelayLoadImage() {
        boolean cacheHit = ImageCacheManager.instance().isCacheHit(comment.image_url) == true;
        long delayMillis = 1000;
        if (cacheHit) {
            delayMillis = 0;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setImageUI();
            }
        }, delayMillis);
    }

    private void setImageUI() {
        ImageRun imagerun = new ImageRun(null, comment.image_url, 0);
        imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_APK;
        imagerun.noimage = true;
        imagerun.addHostAndPath = true;
        imagerun.setRoundAngle = true;
        imagerun.setImageView(userLogo);
        imagerun.post(null);
    }

    protected Object getAttachedObject()
    {
    	return comment;
    }

    private void updateRemovedButtonUi(boolean isLikeCountShown) {
        View removeIcon     = findViewById(R.id.remove_action);
        if (null != removeIcon) {
            if(!isLikeCountShown && (mIsOwner ||
                    Long.valueOf(comment.uid) == AccountServiceUtils.getBorqsAccountID()))
            {
                removeIcon.setVisibility(View.VISIBLE);
                removeIcon.setOnClickListener(deleteCommentsClick);
            }
            else
            {
                removeIcon.setVisibility(View.GONE);
            }
        }
    }

    public boolean updateLikeCountUi(final Comments.Stream_Post item) {
        comment = item;
        boolean isLikeCountShown = false;
        TextView textView = (TextView) findViewById(R.id.post_like_count);
        if (null != textView) {
            if (null != comment && null != comment.likerList && comment.likerList.count > 0) {
                textView.setText(String.valueOf(comment.likerList.count));
                textView.setVisibility(View.VISIBLE);
                textView.setOnClickListener(showLikerClicker);
                isLikeCountShown = true;
            } else {
                textView.setOnClickListener(null);
                textView.setVisibility(View.GONE);
            }
        }

//        updateRemovedButtonUi(isLikeCountShown);

        return isLikeCountShown;
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

    // update view with data
    public void setCommentItem(Comments.Stream_Post di) {
        final long newTimeStamp = null == di ? 0 : di.created_time;
        final long oldTimeStamp = null == comment ? -1 : comment.created_time;
        if (newTimeStamp != oldTimeStamp) {
            comment = di;
            setCommentsUI();
        }
    }   

	@Override
	public String getText() 
	{		
		return comment.message;
	}

    @Override
    protected Bundle getTagBundle() {
        return getTagBundle(comment.id);
    }

    public static Bundle getTagBundle(long objId) {
        Bundle bundle = new Bundle();
        bundle.putString("TAGS_URI", "tag/create");
        bundle.putString("type", String.valueOf(QiupuConfig.COMMENT_OBJECT));
        bundle.putString("target_id", String.valueOf(objId));
        return bundle;
    }
}
