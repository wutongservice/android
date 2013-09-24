package com.borqs.common.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.LocationRequest;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.LikeListener;
import com.borqs.common.util.HtmlUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.MyHtml;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.bpc.FriendsListActivity;
import com.borqs.qiupu.util.DateUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import twitter4j.ApkBasicInfo;
import twitter4j.CacheMap;
import twitter4j.QiupuSimpleUser;
import twitter4j.Stream;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStreamRowView extends BaseEsItemView {
    private static final String TAG = "AbstractStreamRowView";

    private static final int MAX_MESSAGE_LINE_COUNT = 10;
    private static final int NO_MESSAGE_LINE_COUNT = Integer.MAX_VALUE;

    private static final int MORE_TO_PEOPLE = -9999;

    final static String profileURL = BpcApiUtils.PROFILE_SEARCH_USERID_PREFIX;
    protected static final int LEN_MAX_LINK_TITLE = 50;
    protected static final int LEN_MAX_EXTRA_TEXT = 100;
    
    protected static final String TO_SIGN_STRING = /*" > "*/"";
    protected static final String SEPARATOR_COMMA = ", ";
    
    protected static final String POSTER_NAME_FORMAT_SINGLE = "<a href='%3$s%2$s'>%1$s</a>";
    protected static final String POSTER_NAME_FORMAT_SUFFIX = "%4$s<a href='%3$s%2$s'>%1$s</a>";

    private static final int DEFAULT_SHARE_LAYOUT= R.layout.stream_row_layout_allinone;
    private static final int DEFAULT_FORWARD_LAYOUT= R.layout.stream_reshare_layout_allinone;

    Stream post;

    boolean isForReshare;
    boolean forcomments = false;

    static String ilike;
    static String likeStr;
    static String unlikeStr;
    static String commentStr;
    static String from_device, from_device_single;
    static String recipient_limit, recipient_public, anonymous_public;
    
    protected QiupuORM orm;

    public AbstractStreamRowView(Context ctx) {
        super(ctx);

        initRes();
    }

    abstract protected void setUI();

    public boolean refreshPostLike(String id, final Stream stream) {
        if (post != null && post.post_id != null && post.post_id.equals(id)) {
            post = stream;
//            startAnimation(getLikeAnimation());
            setupStreamLikeUi();
            return true;
        }

        return false;
    }

    public boolean refreshPostUnlike(String id, final Stream stream) {
        if (post != null && post.post_id != null && post.post_id.equals(id)) {
            post = stream;
            setupStreamLikeUi();
            return true;
        }

        return false;
    }

    public boolean refreshItem(String id, final Stream stream) {
        if (post != null && post.post_id != null && post.post_id.equals(id)) {
            post = stream;
            setUI();
            return true;
        }

        return false;
    }

    protected static String replaceToUserNickName(String userName) {
        return userName.replace(" ", "&nbsp;");
    }
    

    protected static boolean isValidAppAttachment(Stream post) {
        boolean ret = false;
        if (null != post) {
            if (BpcApiUtils.isValidTypeOfAppAttachment(post.type)) {
                if (null != post.attachment && post.attachment instanceof Stream.ApkAttachment) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    protected static boolean isValidAppLinkAttachment(Stream post) {
        boolean ret = false;
        if (null != post) {
            if (post.type == BpcApiUtils.APK_LINK_POST) {
                if (null != post.attachment && post.attachment instanceof Stream.ApkAttachment) {
                    ret = true;
                }
            }
        }
        return ret;
    }
    
    protected static boolean isValidPhotoAttachment(Stream post) {
        boolean ret = false;
        if (null != post) {
            if (post.type == BpcApiUtils.IMAGE_POST) {
                if (null != post.attachment && post.attachment instanceof Stream.PhotoAttachment) {
                    ret = true;
                }
            }
        }
        return ret;
    }
    
    protected static boolean isValidUrlLinkAttachment(Stream post) {
        boolean ret = false;
        if (null != post) {
            if (post.type == BpcApiUtils.LINK_POST && TextUtils.isEmpty(post.extension)) {
                if (post.attachment != null &&
                        post.attachment.attachments != null &&
                        post.attachment.attachments.size() > 0) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    protected static boolean isValidBookAttachment(Stream post) {
        boolean ret = false;
        if (post.type == BpcApiUtils.BOOK_POST || post.type == BpcApiUtils.BOOK_COMMENT_POST
                || post.type == BpcApiUtils.BOOK_LIKE_POST) {
            if (post.attachment != null &&
                    post.attachment instanceof Stream.BookAttachment) {
                ret = true;
            }
        }
        return ret;
    }

    protected static boolean isValidMusicAttachment(Stream post) {
        boolean ret = false;
        if (post.type == BpcApiUtils.MUSIC_POST || post.type == BpcApiUtils.MUSIC_COMMENT_POST
                || post.type == BpcApiUtils.MUSIC_LIKE_POST) {
            if (post.attachment != null &&
                    post.attachment instanceof Stream.BookAttachment) {
                ret = true;
            }
        }
        return ret;
    }

    protected static boolean isValidAudioAttachment(Stream post) {
        boolean ret = false;
        if ((post.type & BpcApiUtils.AUDIO_POST) == BpcApiUtils.AUDIO_POST) {
            if (post.attachment != null &&
                    post.attachment instanceof Stream.AudioAttachment) {
                ret = true;
            }
        }
        return ret;
    }

    protected static boolean isValidVideoAttachment(Stream post) {
        boolean ret = false;
        if ((post.type & BpcApiUtils.VIDEO_POST) == BpcApiUtils.VIDEO_POST) {
            if (post.attachment != null &&
                    post.attachment instanceof Stream.VideoAttachment) {
                ret = true;
            }
        }
        return ret;
    }

    protected static boolean isValidStaticFileAttachment(Stream post) {
        boolean ret = false;
        if ((post.type & BpcApiUtils.STATIC_FILE_POST) == BpcApiUtils.STATIC_FILE_POST) {
            if (post.attachment != null &&
                    post.attachment instanceof Stream.StaticFIleAttachment) {
                ret = true;
            }
        }
        return ret;
    }

    protected static boolean isValidCardAttachment(Stream post) {
        boolean ret = false;
        if (post != null && !TextUtils.isEmpty(post.extension) && !"null".equals(post.extension)) {
            try {
                JSONTokener jsonTokener = new JSONTokener(post.extension);
                JSONObject obj = new JSONObject(jsonTokener);

                String vcard = null;
                String version = null;
                if (!obj.isNull("mime_type")) {
                    vcard = obj.getString("mime_type");
                }

                if (!obj.isNull("version")) {
                    version = obj.getString("version");
                }

                if ("vcard".equals(vcard) && "1.0".equals(version)) {
                    return true;
                } else {
                    return false;
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return ret;
    }

    protected static boolean isValidLocationAttachment(Stream post) {
        boolean ret = false;
        if (post != null) {
            if (!TextUtils.isEmpty(post.location)/* && TextUtils.isEmpty(post.message)*/) {
                ret = true;
            }
        }
        return ret;
    }

    public void gotoStreamItemComment() {
        if (!forcomments && null != post) {
            if (BpcApiUtils.isValidTypeOfAppAttachment(post.type)) {
                IntentUtil.startAppCommentIntent(getContext(), post);
//            } else if (post.type == BpcApiUtils.BOOK_COMMENT_POST || post.type == BpcApiUtils.BOOK_LIKE_POST) {
//                gotoBookItemComment();
//            } else if (post.type == BpcApiUtils.MUSIC_COMMENT_POST || post.type == BpcApiUtils.MUSIC_LIKE_POST) {
//                gotoMusicItemComment();
            } else {
                IntentUtil.startStreamCommentIntent(getContext(), post);
            }
        }
    }

    private void initRes() {
        if (isEmpty(ilike)) {
            ilike = String.format(getContext().getString(R.string.who_like), getContext().getString(R.string.i_like));
            likeStr = getContext().getString(R.string.news_feed_like);
            unlikeStr = getContext().getString(R.string.news_feed_unlike);
//            reshareString = getContext().getString(R.string.news_feed_reshare_hint);
            commentStr = getContext().getString(R.string.view_all_comment);
        }

        if (isEmpty(from_device)) {
            from_device        = mContext.getString(R.string.from_device);
            from_device_single = mContext.getString(R.string.from_device_single);
        }

        if (isEmpty(recipient_limit)) {
            recipient_limit = mContext.getString(R.string.recipient_limit);
            recipient_public = mContext.getString(R.string.recipient_public);
            anonymous_public = mContext.getString(R.string.anonymous_public);
        }
    }


    OnClickListener reshareClick = new StreamRowItemClicker() {
        public void onRowItemClick(View arg0) {
            if (ensureAccountLogin()) {
                gotoStreamReshare();
            }

        }
    };

    void gotoBookItemComment() {
        // TODO: need to estimate and define such case.
    }

    void gotoMusicItemComment() {
        // TODO: enhancement for music rather than simply reuse brook
    }

    public void gotoStreamReshare() {
        IntentUtil.startComposeIntent(getContext(), post, new long[]{post.fromUser.uid});
    }

    protected static View.OnClickListener itemClick = new StreamRowItemClicker() {
		public void onRowItemClick(View v) {
			if(QiupuConfig.DBLOGD) Log.d(TAG, "onClick, item clicked");
	        if (AbstractStreamRowView.class.isInstance(v)) {
	            AbstractStreamRowView siv = (AbstractStreamRowView)v;
	            siv.gotoStreamItemComment();
	        }
		}
	};

    public static void attachListViewItemClickerContext(Activity context, ListView listView) {
    	//listView.setOnItemClickListener(postClickListener);
        listView.setOnCreateContextMenuListener(context);        
    }

    protected void attachMovementMethod(TextView textView) {
        if (true || forcomments) {
        	
        	//do set span?
        	
            attachTextViewMovementMethod(textView);
        }
    }
    
    protected void setSpan(TextView view)
    {
		final CharSequence charSequence = view.getText();
		if (charSequence instanceof SpannableString) {
		    SpannableString sb = (SpannableString) charSequence;
		    URLSpan[] spans = view.getUrls();
		    for (URLSpan span1 : spans) {
		        int start = sb.getSpanStart(span1);
		        int end = sb.getSpanEnd(span1);
		
		        BorqsURLSPan my = new BorqsURLSPan(span1.getURL(), mContext);
		        ((SpannableString)(charSequence)).setSpan(my, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		        my = null;
		    }
		}
    }

	abstract public void setContent(Stream post2);
	abstract public Stream getStream();


    protected boolean isForMyself() {
        boolean ret = false;
        if (post.toUsers != null && post.toUsers.size() == 1) {
            long toid = post.toUsers.get(0).uid;
            if (toid == post.fromUser.uid) {
                ret = true;
            }
        }

        return ret;
    }

//    private String getPostedContent() {
//        final String message;
//        if (isForMyself()) {
//            if (isEmpty(post.message)) {
//                message = isEmpty(post.parent_id) ? "" : reshareString;
//
//            } else {
//                message = isEmpty(post.parent_id) ? post.message :
//                        reshareString + "<br>" + post.message;
//            }
//        } else {
//            if (!isEmpty(post.message)) {
//                message = post.message;
//            } else if (!isEmpty(post.parent_id)) {
//                message = reshareString;
//            } else {
//                message = "";
//            }
//        }
//        return message;
//    }

//    private String getPostedContent() {
//        final String message;
//        if (isForMyself()) {
//            if (isEmpty(post.message)) {
//                message = "";
//            } else {
//                message = post.message;
//            }
//        } else {
//            if (!isEmpty(post.message)) {
//                message = post.message;
//            } else {
//                message = "";
//            }
//        }
//        return message;
//    }

    protected void initExpandablePostMessageView() {
//        ExpendableTextPanel expandableWidget = (ExpendableTextPanel) findViewById(R.id.expandable_span);
//        if (null != expandableWidget) {
//            expandableWidget.initExpandableTextView(MAX_MESSAGE_LINE_COUNT);
//            final TextView textView = (TextView) expandableWidget.findViewById(R.id.summary_introduction);
//            attachMovementMethod(textView);
//        }
    }

    protected void refreshPostedMessageTextContent(View parent, String message) {
        View viewStub = parent.findViewById(R.id.expandable_span);
//        final String message = stream.message;
        if (isEmpty(message)) {
            if (null != viewStub && viewStub instanceof ExpendableTextPanel) {
                viewStub.setVisibility(View.GONE);
            }
        } else {
            View expendedView = null;
            if (null != viewStub && viewStub instanceof ViewStub) {
                expendedView = ((ViewStub)viewStub).inflate();
            }

            if (null != expendedView && expendedView instanceof ExpendableTextPanel) {
                ExpendableTextPanel expandableWidget = (ExpendableTextPanel)expendedView;
                expandableWidget.initExpandableTextView(forcomments ? NO_MESSAGE_LINE_COUNT : MAX_MESSAGE_LINE_COUNT);
                final TextView textView = (TextView) expandableWidget.findViewById(R.id.summary_introduction);
                
                if(hasLinks(message))
                {
                	stripHtmlUnderlines(textView, formatHtmlContent(message));
                	attachMovementMethod(textView);
                }
                else
                {
                	stripHtmlUnderlines(textView, formatHtmlContent(message));
                	//attachMovementMethod(textView);
                }
                
                expandableWidget.setVisibility(View.VISIBLE);
            }
        }
//        ExpendableTextPanel expandableWidget = (ExpendableTextPanel) findViewById(R.id.expandable_span);
//        if (null != expandableWidget) {
//            final String message = getPostedContent();
//            if (!TextUtils.isEmpty(message.trim())) {
//                final TextView textView = (TextView) expandableWidget.findViewById(R.id.summary_introduction);
//                stripHtmlUnderlines(textView, message);
//
//                expandableWidget.setVisibility(View.VISIBLE);
//            } else {
//                expandableWidget.setVisibility(View.GONE);
//            }
//        }
    }

    protected boolean setupStreamLikeUi() {
        View viewStub = findViewById(R.id.stream_like_string);
        if (null == viewStub) {
            return false;
        }

        if (viewStub instanceof ViewStub) {
            final String showLikeTxt = formatLikerText();
            if (!TextUtils.isEmpty(showLikeTxt)) {
                TextView view = (TextView) ((ViewStub) viewStub).inflate();
                setLikeTextView(view, showLikeTxt);
                return true;
            } else {
                viewStub.setVisibility(View.GONE);
            }
        } else if (viewStub instanceof TextView) {
            final int likerCount = null == post || null == post.likes ? 0 : post.likes.count;
            TextView view = (TextView) viewStub;
            final Resources res = mContext.getResources();
            if (null != post && post.iLike) {
                view.setTextColor(res.getColor(android.R.color.white));
                view.setBackgroundDrawable(res.getDrawable(R.drawable.btn_plusone_red_bg));
            } else {
                view.setTextColor(res.getColor(android.R.color.black));
                view.setBackgroundDrawable(res.getDrawable(R.drawable.btn_plusone_normal_bg));
            }
            view.setText(String.valueOf(likerCount));

            view.setOnClickListener(likeClick);

            return likerCount > 0;
        } else if (viewStub instanceof ImageView) {
            ImageView view = (ImageView) viewStub;
            if (null != post && post.iLike) {
                view.setImageResource(R.drawable.btn_stream_like_selected_bg);
            } else {
                view.setImageResource(R.drawable.btn_stream_like_normal_bg);
            }

            view.setOnClickListener(likeClick);
            return true;
        } else {
            Log.e(TAG, "setupStreamLikeUi, unexpected view: " + viewStub);
        }

        return false;
    }

    private void setLikeTextView(TextView view, String text) {
        if (null != view) {
            view.setText(text);
            view.setOnClickListener(new StreamRowItemClicker() {
                @Override
                public void onRowItemClick(View view) {
                    int type = getLikeType(post.type);
                    String target = getLikeTarget(post);
                    IntentUtil.ShowUserList(getContext(),
                            mContext.getString(R.string.dialog_like_user_title),
                            post.likes, type, target);
                }
            });

            view.setVisibility(View.VISIBLE);
        }
    }

    private String formatLikerText() {
        final int likerCount = null == post || null == post.likes ? 0 : post.likes.count;

        StringBuilder likerBuilder = new StringBuilder();
        boolean isMoreLiker = false;
        if (likerCount > 0) {
            final long myUid = AccountServiceUtils.getBorqsAccountID();
            final int size = null == post.likes.friends ? 0 : post.likes.friends.size();
            for (QiupuSimpleUser user : post.likes.friends) {
                long tmpid = user.uid;
                if (myUid == tmpid) {
                    post.iLike = true;
                } else {
                    String tmpname = user.nick_name; //orm.getUserName(tmpid);
                    if (!isEmpty(tmpname)) {
                        if (isMoreLiker) {
                            likerBuilder.append(SEPARATOR_COMMA);
                        } else {
                            isMoreLiker = true;
                        }
                        likerBuilder.append(tmpname);
                    }
                }
            }

            if (likerCount > size) {
                likerBuilder.append(getContext().getString(R.string.other_more_people,
                        (likerCount - size)));
            }
        }

        if (post.iLike) {
            return isMoreLiker ? getContext().getString(R.string.who_like, getContext().getString(R.string.i_like) + ", " + likerBuilder) : ilike;
        } else {
            return isMoreLiker ? likerBuilder + likeStr : null;
        }
    }
    
    private static String getLikeTarget(Stream post) {
		if(post.type == BpcApiUtils.APK_POST ||  
				post.type == BpcApiUtils.APK_COMMENT_POST ||
			    post.type == BpcApiUtils.APK_LIKE_POST)
			return post.attachment.getID();
					
		return post.post_id;
	}
    
    private static int getLikeType(int type)
    {
    	int ret = 2;
    	switch(type)
    	{
    	    case BpcApiUtils.TEXT_POST:
    	    case BpcApiUtils.LINK_POST:
    	    case BpcApiUtils.APK_LINK_POST:
    	    	ret = 2;
    	    	break;
    	    case BpcApiUtils.APK_POST:
    	    case BpcApiUtils.APK_COMMENT_POST:
    	    case BpcApiUtils.APK_LIKE_POST:
    	    	ret = 4;
    	    	break;
    	    case BpcApiUtils.IMAGE_POST:
    	    	ret = 2;
    	    	break;
    	    case BpcApiUtils.VIDEO_POST:
    	    	ret = 2;    	    	
    	    	break;
    	    case BpcApiUtils.BOOK_LIKE_POST:
    	    case BpcApiUtils.BOOK_POST:
    	    case BpcApiUtils.BOOK_COMMENT_POST:
    	    	ret = 2;
    	    	break;
    	    case BpcApiUtils.AUDIO_POST:
    	    	ret = 2;    	    	
    	    	break;    	    	
    	    case BpcApiUtils.MAKE_FRIENDS_POST:
    	    	ret = 2;    	    	
    	    	break;    	    	
    	    case BpcApiUtils.MUSIC_POST:
    	    case BpcApiUtils.MUSIC_COMMENT_POST:
    	    case BpcApiUtils.MUSIC_LIKE_POST:
    	    	ret = 2;
    	    	break;    	    
    	}
    	
    	return ret;
    }
    protected void setupPrivacyUi() {
        ImageView img_pic_ui = (ImageView)findViewById(R.id.img_pic_ui);
        if (null != img_pic_ui) {
            if (null != post && post.isPrivacy()) {
//                img_pic_ui.setImageResource(R.drawable.stream_row_privacy);
                img_pic_ui.setVisibility(View.VISIBLE);
            } else {
//                img_pic_ui.setImageResource(R.drawable.stream_row_public);
                img_pic_ui.setVisibility(View.GONE);
            }

        }
    }

    private void likePost() {
        if (!post.iLike) {
            if (LikeListener.class.isInstance(getContext()) && ensureAccountLogin()) {
                LikeListener ll = (LikeListener) getContext();
                ll.likePost(post.post_id, post.rootid);
            }
            View view = findViewById(R.id.stream_like_string);
            if (null != view) {
//                view.startAnimation(getShowAnimation());
            }
        }
    }

    private void unlikePost() {
        if (post.iLike) {
            if (LikeListener.class.isInstance(getContext()) && ensureAccountLogin()) {
                LikeListener ll = (LikeListener) getContext();
                ll.unLikePost(post.post_id, post.rootid);
            }

            View view = findViewById(R.id.stream_like_string);
            if (null != view) {
//                view.startAnimation(getHideAnimation());
            }
        }
    }

//    protected Animation getLikeAnimation() {
//        Animation likeAction = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f,
//                Animation.RELATIVE_TO_SELF, 0.0f,
//                Animation.RELATIVE_TO_SELF, 0.0f);
//        likeAction.setDuration(500);
//        return likeAction;
//    }
//
//    protected Animation getHideAnimation() {
//        Animation hideAction = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f,
//                Animation.RELATIVE_TO_SELF, 0.0f,
//                Animation.RELATIVE_TO_SELF, 0.0f);
//        hideAction.setDuration(500);
//        return hideAction;
//    }
//
//    protected Animation getShowAnimation() {
//        Animation showAction = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f,
//                Animation.RELATIVE_TO_SELF, 0.0f,
//                Animation.RELATIVE_TO_SELF, 0.0f);
//        showAction.setDuration(500);
//        return showAction;
//    }

    public void startCreationAnimation() {
//        startAnimation(getShowAnimation());
    }

    private OnClickListener likeClick = new StreamRowItemClicker() {
        public void onRowItemClick(View arg0) {
            if (post.iLike) {
                unlikePost();
            } else {
                likePost();
            }
        }
    };


    /**
     * To depress duplicate responding action for double click to stream row item, simply
     * add protected to origin View.OnClickListener.
     */
    static abstract class StreamRowItemClicker implements OnClickListener {
        private boolean mWasReady = true;
        protected abstract void onRowItemClick(View view);
        public void onClick(View view) {
                if (mWasReady) {
                    mWasReady = false;
                    onRowItemClick(view);
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mWasReady = true;
                        }
                    }, 500);
                }
        }
    }

    protected OnClickListener userClick = new StreamRowItemClicker() {
        public void onRowItemClick(View arg0) {
            IntentUtil.startUserDetailIntent(getContext(), post.fromUser.uid, post.fromUser.nick_name);
        }
    };

    void gotoApkDetail(ApkBasicInfo response) {
        IntentUtil.startApkDetailActivity(getContext(), response);
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(response.DETAIL_ACTIVITY_SCHEME));
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//
//        intent.putExtra(QiupuMessage.BUNDLE_APKINFO, response.clone());
//
//        if (BpcApiUtils.isActivityReadyForIntent(getContext(), intent)) {
//            getContext().startActivity(intent);
//        } else {
//            Log.e(TAG, "gotoApkDetail, Impossible case, no activity for intent: " + intent
//                    + ", apk info:" + response);
//        }
    }

    void setupCommonUi() {
//        View posterIconIV = findViewById(R.id.user_icon_cover);
//        if (null != posterIconIV) {
//            posterIconIV.setOnClickListener(userClick);
//        }

        initExpandablePostMessageView();

//        TextView author_name = (TextView) findViewById(R.id.poster_name);
//        attachMovementMethod(author_name);

    }


    private StringBuilder getFromUsersInHTML(StringBuilder formatBuilder,
            QiupuSimpleUser qsUser, String formatter) {
        return formatBuilder.append(String.format(formatter, qsUser.nick_name,
                qsUser.uid, profileURL));
    }

    private static StringBuilder getToUsersInHTML(StringBuilder formatBuilder,
            QiupuSimpleUser qsUser, String formatter, String toSign) {
        return formatBuilder.append(String.format(formatter,
                replaceToUserNickName(qsUser.nick_name), qsUser.uid,
                profileURL, toSign));
    }

    private String encodeStreamPosterHtmlText(Stream stream){
        StringBuilder formatBuilder = new StringBuilder();

        if (!isForMyself()) {
            final int toUserSize = stream.toUsers.size();
            if (toUserSize > 0) {
                formatBuilder = getFromUsersInHTML(formatBuilder, stream.fromUser, POSTER_NAME_FORMAT_SINGLE);

                formatBuilder = getToUsersInHTML(formatBuilder, stream.toUsers.get(0), POSTER_NAME_FORMAT_SUFFIX, TO_SIGN_STRING);
                if (forcomments) {
                    for (int i = 1; i < toUserSize; i++) {
                        formatBuilder = getToUsersInHTML(formatBuilder, stream.toUsers.get(i), POSTER_NAME_FORMAT_SUFFIX, SEPARATOR_COMMA);
                    }
                } else {
                    final int moreCount = toUserSize - 1;
                    if (moreCount > 0) {
                        if (moreCount == 1) {
                            formatBuilder = getToUsersInHTML(formatBuilder, stream.toUsers.get(1), POSTER_NAME_FORMAT_SUFFIX, SEPARATOR_COMMA);
                        } else {
                            final String moreUser = String.format(getContext().getString(R.string.other_more_people), moreCount);
                            formatBuilder.append(String.format(POSTER_NAME_FORMAT_SUFFIX,
                                    replaceToUserNickName(moreUser),
                                    MORE_TO_PEOPLE, profileURL, SEPARATOR_COMMA));
                        }
                    }
                }
            }
            //for make friends
            else if (stream.type == BpcApiUtils.MAKE_FRIENDS_POST) {
                if (stream.attachment != null && stream.attachment.attachments.size() > 0) {
                    boolean isFirst = true;
                    StringBuilder raw = new StringBuilder();
                    StringBuilder htmlraw = new StringBuilder();
                    for (Object obj : stream.attachment.attachments) {
                        QiupuSimpleUser suser = (QiupuSimpleUser) obj;

                        raw.append((!isFirst ? SEPARATOR_COMMA : ""));
                        raw.append(suser.nick_name);

                        htmlraw.append(String.format(POSTER_NAME_FORMAT_SUFFIX,
                                suser.nick_name, suser.uid, profileURL,
                                (!isFirst ? SEPARATOR_COMMA : "")));

                        if (isFirst) {
                            isFirst = false;
                        }
                    }

                    formatBuilder.append(String.format(getContext().getString(R.string.stream_make_friends), htmlraw));
                }
            }
        }

        String htmlText = HtmlUtils.text2html(formatBuilder.toString());
        return htmlText;
    }

    protected void refreshPostedMessageTextContent(View parent, Stream stream) {
        if (null == stream) {
            Log.d(TAG, "refreshPostedMessageTextContent, skip null stream");
            return;
        }

        if (stream.type == BpcApiUtils.MAKE_FRIENDS_POST) {
            String htmlText = stream.getCache(CacheMap.postFrom);
            if (isEmpty(htmlText)) {
                htmlText = encodeStreamPosterHtmlText(stream);
                stream.cacheMeta(CacheMap.postFrom, htmlText);
            }
            refreshPostedMessageTextContent(parent, htmlText);
        } else {
            refreshPostedMessageTextContent(parent, stream.message);
        }
    }

    void setupPosterSummary(View parent, Stream stream) {
        if (null == parent || null == stream || null == stream.fromUser) {
            Log.w(TAG, "setupPosterSummary, ignore invalid view or stream item.");
            return;
        }

        TextView author_name = (TextView) parent.findViewById(R.id.poster_name);
        if (null == author_name) {
            return;
        }

        author_name.setText(stream.fromUser.nick_name);

//        String htmlText = stream.getCache(CacheMap.postFrom);
//        if (isEmpty(htmlText)) {
//            if (author_name != null && stream.fromUser != null) {
//                author_name.setText(stream.fromUser.nick_name);
//
//                htmlText = encodeStreamPosterHtmlText(stream);
//                stream.cacheMeta(CacheMap.postFrom, htmlText);
//            }
//        }
//        stripHtmlUnderlines(author_name, htmlText);
    }

    void setupOriginalPosterName(View parent, final Stream stream) {
        if (null == parent || null == stream || null == stream.fromUser) {
            Log.w(TAG, "setupOriginalPosterName, ignore invalid view or stream item.");
            return;
        }

        TextView authorView = (TextView) parent.findViewById(R.id.poster_name);
        if (null != authorView) {
            String htmlText = stream.getCache(CacheMap.postFrom);
            if (isEmpty(htmlText)) {
                if (authorView != null && stream.fromUser != null) {
                    htmlText = HtmlUtils.text2html(
                            String.format(POSTER_NAME_FORMAT_SINGLE,
                                    stream.fromUser.nick_name,
                                    stream.fromUser.uid, profileURL));
//                    htmlText = encodeStreamPosterHtmlText(stream);
                    stream.cacheMeta(CacheMap.postFrom, htmlText);
                    authorView.setOnClickListener(new StreamRowItemClicker() {
                        @Override
                        protected void onRowItemClick(View view) {
                            IntentUtil.startUserDetailIntent(getContext(), stream.fromUser);
                        }
                    });
                }
            }
            stripHtmlUnderlines(authorView, htmlText);
//                author_name.setText(stream.fromUser.nick_name);
        }
    }

//    private void setupReshareAttachment() {
//        StreamRowView st;
//        st = new StreamRowView(mContext, post.retweet, forcomments, true);
//        st.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
//                LayoutParams.WRAP_CONTENT));
//        attachmentContainer.addView(st);
//    }

    //    private void attachAttachContentClick(OnClickListener attachContentClicker) {
//        if (!isForReshare) {
//            View share_content_span = findViewById(R.id.share_content_ll);
//            if (null != share_content_span) {
//                share_content_span.setOnClickListener(attachContentClicker);
//            }
//        }
//    }

    void setupTimeSpanUi() {
        TextView postTimeTV = (TextView) findViewById(R.id.post_time);
        if (null != postTimeTV) {
            String day = DateUtil.converToRelativeTime(mContext, post.created_time);
            if (!isEmpty(post.device)) {
                try {
                    String[] devices = post.device.split(";");
                    if (devices.length >= 4) {
                        if (forcomments == false) {
                            day += String.format(from_device_single,
                                    devices[1].substring(devices[1].indexOf("=") + 1)/*,
	                                devices[3].substring(devices[3].indexOf("=") + 1)*/);
                        } else {
                            day += String.format(from_device,
                                    devices[1].substring(devices[1].indexOf("=") + 1),
                                    devices[3].substring(devices[3].indexOf("=") + 1));
                        }
                    }
                } catch (Exception ne) {
                    ne.printStackTrace();
                }
            }
            postTimeTV.setText(day);
        }

        TextView recipient = (TextView) findViewById(R.id.post_privacy_property);
        if (null != recipient && null != post) {
            final boolean emptyRecipient = null == post.toUsers || post.toUsers.isEmpty();
            final String prop;

            Drawable hintDrawable = null;
            Drawable toDrawable = null;
            if (emptyRecipient) {
//                prop = anonymous_public;
                prop = "";
            } else {
                if (post.isPrivacy()) {
//                    prop = recipient_limit;
                    hintDrawable = getContext().getResources().getDrawable(R.drawable.stream_row_privacy);
                } else {
//                    prop = recipient_public;
                }
                toDrawable = getContext().getResources().getDrawable(R.drawable.to_icon);
                prop = formatRecipient(getContext(), post.toUsers);
            }

            recipient.setCompoundDrawablesWithIntrinsicBounds(toDrawable, null, hintDrawable, null);
            if (!emptyRecipient) {
                String cachedSummary = post.getCache(CacheMap.postRecipient);
                if (isEmpty(cachedSummary)) {
                    cachedSummary = MyHtml.toDumbClickableText(prop);
                    post.cacheMeta(CacheMap.postRecipient, cachedSummary);
                }
                final String trimText = cachedSummary.trim();
                recipient.setText(MyHtml.fromHtml(trimText));
                if (forcomments) {
                    recipient.setOnClickListener(recipientClicker);
                    attachMovementMethod(recipient);
                }
            } else {
                recipient.setText(prop);
            }
        }
    }

    private OnClickListener recipientClicker = new StreamRowItemClicker() {
        @Override
        public void onRowItemClick(View view) {
            showRecipientList();
        }
    };

    private void showRecipientList() {
        if (post.toUsers.isEmpty()) {
            Log.e(TAG, "showRecipientList, skip with empty user list");
            return;
        } else if (post.toUsers.size() == 1) {
            QiupuSimpleUser user = post.toUsers.get(0);
            IntentUtil.startUserDetailIntent(getContext(), user);
        } else {
            final String title = mContext.getResources().getString(R.string.dialog_recipient_title);
            FriendsListActivity.showUserList(mContext, title, (ArrayList) post.toUsers);
        }
    }

    protected void setupPostContentUi(String txtContent, OnClickListener clickListener) {
        setupPostContentUi(txtContent, clickListener, false);
    }

    protected void setupPostContentUi(String txtContent, OnClickListener clickListener, boolean txtOnly) {
        TextView postContentTV = (TextView) findViewById(R.id.post_content);
        if (null != postContentTV) {
            final String trimText = txtContent.trim();
            if (!TextUtils.isEmpty(trimText)) {
                if (txtOnly) {
                    postContentTV.setCompoundDrawables(null, null, null, null);
                    postContentTV.setTextAppearance(getContext(), R.style.sns_text);
                    postContentTV.setText(trimText);
                } else {
//                    postContentTV.setCompoundDrawables(R.drawable.default_app_icon, null, null, null);
                    postContentTV.setTextAppearance(getContext(), R.style.sns_text_second_copy);
                    postContentTV.setText(MyHtml.fromHtml(trimText));
                    attachMovementMethod(postContentTV);
                }

                postContentTV.setVisibility(View.VISIBLE);
                if (!isForReshare) {
                    postContentTV.setOnClickListener(clickListener);
                }
            } else {
                postContentTV.setVisibility(View.GONE);
            }
        }
    }

    protected void setupPosterImageRunner(View parent, Stream stream) {
        ImageView posterIconIV = (ImageView) parent.findViewById(R.id.user_icon);
        if (null != posterIconIV && null != stream && null != stream.fromUser) {
            String fromUserIcon = Stream.getFromUserPhotoUrl(stream);
            if (!stream.fromUser.reset_image_url) {
                final String dbIconPath = orm.getUserImageUrl(stream.fromUser.uid);
                if (!isEmpty(dbIconPath) && !dbIconPath.equals(fromUserIcon)) {
                    stream.fromUser.profile_image_url = fromUserIcon;
                    stream.fromUser.reset_image_url = true;
                    fromUserIcon = dbIconPath;
                } else if (isEmpty(dbIconPath) && !isEmpty(fromUserIcon)) {
                    orm.cacheUserImageUrl(stream.fromUser.uid, fromUserIcon);
                }
            }

            if (isEmpty(fromUserIcon)) {
                posterIconIV.setImageResource(R.drawable.default_user_icon);
            } else {
                ImageRun imagerun = new ImageRun(null, fromUserIcon, 0);
                imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
                imagerun.noimage = false;
                imagerun.addHostAndPath = true;
                imagerun.setRoundAngle = true;
                if(imagerun.setImageView(posterIconIV) == false)
                imagerun.post(null);
            }
            posterIconIV.setOnClickListener(userClick);
        }
    }

    public int getTargetLayoutId() {
        return getTargetLayoutTag(post);
    }

    private static final int STREAM_ROW_LAYOUT_ID_TEXT = 1;
    private static final int STREAM_ROW_LAYOUT_ID_PHOTO = 2;
    private static final int STREAM_ROW_LAYOUT_ID_PLUGIN = 3;
    private static final int STREAM_ROW_LAYOUT_ID_FILE = 4;
    private static final int STREAM_ROW_LAYOUT_ID_AUDIO = 5;
    private static final int STREAM_ROW_LAYOUT_ID_VIDEO = 6;
    private static final int STREAM_ROW_LAYOUT_ID_APP_LINK = 7;
    private static final int STREAM_ROW_LAYOUT_ID_URL_LINK = 8;
    private static final int STREAM_ROW_LAYOUT_ID_VCARD = 9;
    private static final int STREAM_ROW_LAYOUT_ID_LOCATION = 10;

    private static int STREAM_ROW_LAYOUT_ID_SHARE_OFFSET = 10;

    public static int getTargetLayoutTag(Stream stream) {
    	if (null == stream) {
    		return 0;
    	}
    	int type = stream.type;
    	int targetId = 0;
    	if(stream.isRetweet()) {
    		if(stream.retweet == null ) {
    			//TODO if retweet is null, will used DEFAULT_SHARE_LAYOUT
    			type = -1;
    			targetId = -1;
    		}else {
    			type = stream.retweet.type;
    			targetId = STREAM_ROW_LAYOUT_ID_SHARE_OFFSET;
    		}
    	}
        switch (type) {
            default:
                if (isValidLocationAttachment(stream) &&
                        targetId <= 0 &&
                        TextUtils.isEmpty(stream.message)) {
                    targetId += STREAM_ROW_LAYOUT_ID_LOCATION;
                } else if (isValidCardAttachment(stream)) {
                    targetId += STREAM_ROW_LAYOUT_ID_VCARD;
                } else {
                    targetId += STREAM_ROW_LAYOUT_ID_TEXT;
                }
                break;
            case BpcApiUtils.APK_POST:
            case BpcApiUtils.APK_COMMENT_POST:
            case BpcApiUtils.APK_LIKE_POST:
            case BpcApiUtils.BOOK_POST:
            case BpcApiUtils.BOOK_COMMENT_POST:
            case BpcApiUtils.BOOK_LIKE_POST:
            case BpcApiUtils.MUSIC_POST:
            case BpcApiUtils.MUSIC_COMMENT_POST:
            case BpcApiUtils.MUSIC_LIKE_POST:
                targetId += STREAM_ROW_LAYOUT_ID_PLUGIN;
                break;
            case BpcApiUtils.APK_LINK_POST:
                targetId += STREAM_ROW_LAYOUT_ID_APP_LINK;
                break;
            case BpcApiUtils.LINK_POST:
                if (TextUtils.isEmpty(stream.extension)) {
                    targetId += STREAM_ROW_LAYOUT_ID_URL_LINK;
                    break;
                }
            case BpcApiUtils.AUDIO_POST:
                targetId += STREAM_ROW_LAYOUT_ID_AUDIO;
                break;
            case BpcApiUtils.VIDEO_POST:
                targetId += STREAM_ROW_LAYOUT_ID_VIDEO;
                break;
            case BpcApiUtils.STATIC_FILE_POST:
                targetId += STREAM_ROW_LAYOUT_ID_FILE;
                break;
            case BpcApiUtils.IMAGE_POST:
                targetId += STREAM_ROW_LAYOUT_ID_PHOTO;
                break;
        }

        return targetId;
    }

    public static int getLayoutResourceId(int layoutTag) {
        final int resId;

        if (layoutTag > STREAM_ROW_LAYOUT_ID_SHARE_OFFSET) {
            // re-share layout.
            switch (layoutTag - STREAM_ROW_LAYOUT_ID_SHARE_OFFSET) {
                case STREAM_ROW_LAYOUT_ID_TEXT:
                    resId = R.layout.stream_reshare_layout_text;
                    break;
                case STREAM_ROW_LAYOUT_ID_PHOTO:
                	resId = R.layout.stream_row_layout_reshare_photo;
                    break;
                case STREAM_ROW_LAYOUT_ID_URL_LINK:
                	resId = R.layout.stream_row_layout_reshare_weburl;
                	break;
                case STREAM_ROW_LAYOUT_ID_PLUGIN:
                case STREAM_ROW_LAYOUT_ID_FILE:
                case STREAM_ROW_LAYOUT_ID_AUDIO:
                    resId = DEFAULT_FORWARD_LAYOUT;
                    break;
                case STREAM_ROW_LAYOUT_ID_VIDEO:
                    resId = R.layout.stream_row_layout_reshare_video;
                    break;
                case STREAM_ROW_LAYOUT_ID_APP_LINK:
                case STREAM_ROW_LAYOUT_ID_VCARD:
                case STREAM_ROW_LAYOUT_ID_LOCATION:
                    resId = DEFAULT_FORWARD_LAYOUT;
                    break;
                default:
                    resId = DEFAULT_FORWARD_LAYOUT;
                    Log.d(TAG, "newInstance, unknown id: " + layoutTag);
                    break;
            }
        } else {
            // share layout.
            switch (layoutTag) {
                case STREAM_ROW_LAYOUT_ID_TEXT:
                    resId = R.layout.stream_row_layout_text;
                    break;
                case STREAM_ROW_LAYOUT_ID_PHOTO:
                    resId = R.layout.stream_row_layout_photo;
                    break;
                case STREAM_ROW_LAYOUT_ID_URL_LINK:
                    resId = R.layout.stream_row_layout_weburl;
                    break;
                case STREAM_ROW_LAYOUT_ID_PLUGIN:
                case STREAM_ROW_LAYOUT_ID_FILE:
                case STREAM_ROW_LAYOUT_ID_AUDIO:
                    resId = DEFAULT_SHARE_LAYOUT;
                    break;
                case STREAM_ROW_LAYOUT_ID_VIDEO:
                    resId = R.layout.stream_row_layout_video;
                    break;
                case STREAM_ROW_LAYOUT_ID_APP_LINK:
                case STREAM_ROW_LAYOUT_ID_VCARD:
                case STREAM_ROW_LAYOUT_ID_LOCATION:
                    resId = DEFAULT_SHARE_LAYOUT;
                    break;
                default:
                    resId = DEFAULT_SHARE_LAYOUT;
                    Log.d(TAG, "newInstance, unknown id: " + layoutTag);
                    break;
            }
        }

        return resId;
    }

    public static AbstractStreamRowView newInstance(Context context, int layoutTag, Stream stream) {
        return newInstance(context, layoutTag, stream, false);
    }

    public static AbstractStreamRowView newInstance(Context context, int layoutTag, Stream stream, boolean forComment) {
        final int resId;

        if (layoutTag > STREAM_ROW_LAYOUT_ID_SHARE_OFFSET) {
            // re-share layout.
            switch (layoutTag - STREAM_ROW_LAYOUT_ID_SHARE_OFFSET) {
                case STREAM_ROW_LAYOUT_ID_TEXT:
                    resId = R.layout.stream_reshare_layout_text;
                    break;
                case STREAM_ROW_LAYOUT_ID_PHOTO:
                	return new StreamRowPhotoReshareView(context, stream, forComment);
                case STREAM_ROW_LAYOUT_ID_URL_LINK:
                	return new StreamRowWebUrlReshareView(context, stream, forComment);

                case STREAM_ROW_LAYOUT_ID_PLUGIN:
                case STREAM_ROW_LAYOUT_ID_FILE:
                case STREAM_ROW_LAYOUT_ID_AUDIO:
                    resId = DEFAULT_FORWARD_LAYOUT;
                    break;
                case STREAM_ROW_LAYOUT_ID_VIDEO:
                    return new StreamRowVideoReshareView(context, stream, forComment);
                case STREAM_ROW_LAYOUT_ID_APP_LINK:
                case STREAM_ROW_LAYOUT_ID_VCARD:
                case STREAM_ROW_LAYOUT_ID_LOCATION:
                    resId = DEFAULT_FORWARD_LAYOUT;
                    break;
                default:
                    resId = DEFAULT_FORWARD_LAYOUT;
                    Log.d(TAG, "newInstance, unknown id: " + layoutTag);
                    break;
            }
        } else {
            // share layout.
            switch (layoutTag) {
                case STREAM_ROW_LAYOUT_ID_TEXT:
                    resId = R.layout.stream_row_layout_text;
                    break;
                case STREAM_ROW_LAYOUT_ID_PHOTO:
//                    resId = R.layout.stream_row_layout_photo;
                    return new StreamRowPhotoView(context, stream, forComment);
                case STREAM_ROW_LAYOUT_ID_URL_LINK:
                    return new StreamRowWebUrlView(context, stream, forComment);
                case STREAM_ROW_LAYOUT_ID_PLUGIN:
                case STREAM_ROW_LAYOUT_ID_FILE:
                case STREAM_ROW_LAYOUT_ID_AUDIO:
                    resId = DEFAULT_SHARE_LAYOUT;
                    break;
                case STREAM_ROW_LAYOUT_ID_VIDEO:
                    resId = R.layout.stream_row_layout_video;
                    break;
                case STREAM_ROW_LAYOUT_ID_APP_LINK:
                case STREAM_ROW_LAYOUT_ID_VCARD:
                case STREAM_ROW_LAYOUT_ID_LOCATION:
                    resId = DEFAULT_SHARE_LAYOUT;
                    break;
                default:
                    resId = DEFAULT_SHARE_LAYOUT;
                    Log.d(TAG, "newInstance, unknown id: " + layoutTag);
                    break;
            }
        }

        return new StreamRowView(context, resId, stream, forComment);
    }

    public static AbstractStreamRowView newInstance(Context context, Stream stream, boolean forComment) {
        return newInstance(context, getTargetLayoutTag(stream), stream, forComment);
    }

    protected static int getLayoutResourceId(Stream stream) {
        return getLayoutResourceId(getTargetLayoutTag(stream));
    }

//    private static String formatRecipient(Context context, List<QiupuSimpleUser> toUsers) {
//        if (null == toUsers || toUsers.isEmpty()) {
//            return "";
//        } else {
//            boolean toMe = false;
//            int index = 0;
//            int otherCount = toUsers.size();
//            QiupuSimpleUser[] otherRecipient = new QiupuSimpleUser[2];
//            final long myId = AccountServiceUtils.getBorqsAccountID();
//            for (QiupuSimpleUser user : toUsers) {
//                if (user.uid == myId) {
//                    toMe = true;
//                    otherCount--;
//                } else {
//                    if (toMe && index >= 1) {
//                        break;
//                    }
//                    if (index <= 1) {
//                        otherRecipient[index] = user;
//                        index++;
//                    }
//                }
//            }
//
//            StringBuilder formatBuilder = new StringBuilder();
//            if (toMe) {
//                formatBuilder.append(String.format(POSTER_NAME_FORMAT_SUFFIX,
//                        context.getString(R.string.me), myId,
//                        profileURL, TO_SIGN_STRING));
//                if (otherCount > 0) {
//                    QiupuSimpleUser qsUser = otherRecipient[0];
//                    formatBuilder = getToUsersInHTML(formatBuilder, qsUser, POSTER_NAME_FORMAT_SUFFIX, SEPARATOR_COMMA);
//                    if (otherCount > 1) {
//                        qsUser = otherRecipient[1];
//                        formatBuilder = getToUsersInHTML(formatBuilder, qsUser, POSTER_NAME_FORMAT_SUFFIX, SEPARATOR_COMMA);
//                        if (otherCount > 2) {
//                            formatBuilder.append(context.getString(R.string.other_more_people, otherCount - 3));
//                        }
//                    }
//                }
//            } else {
//                if (otherCount > 0) {
//                    QiupuSimpleUser qsUser = otherRecipient[0];
//                    formatBuilder.append(String.format(POSTER_NAME_FORMAT_SUFFIX,
//                            replaceToUserNickName(qsUser.nick_name), qsUser.uid,
//                            profileURL, TO_SIGN_STRING));
//                    if (otherCount > 1) {
//                        qsUser = otherRecipient[1];
//                        formatBuilder = getToUsersInHTML(formatBuilder, qsUser, POSTER_NAME_FORMAT_SUFFIX, SEPARATOR_COMMA);
//                        if (otherCount > 2) {
//                            formatBuilder.append(context.getString(R.string.other_more_people, otherCount - 2));
//                        }
//                    }
//                }
//            }
//            return formatBuilder.toString();
//        }
//    }

    private static String formatRecipient(Context context, List<QiupuSimpleUser> toUsers) {
        if (null == toUsers || toUsers.isEmpty()) {
            return "";
        } else {
            boolean toMe = false;
            int index = 0;
            int otherCount = toUsers.size();
            QiupuSimpleUser[] otherRecipient = new QiupuSimpleUser[3];
            final long myId = AccountServiceUtils.getBorqsAccountID();
            for (QiupuSimpleUser user : toUsers) {
                if (user.uid == myId) {
                    toMe = true;
                    otherCount--;
                } else {
                    if (index <= 2) {
                        otherRecipient[index] = user;
                        index++;
                    }

                    if (toMe && index > 2) {
                        break;
                    }
                }
            }

            StringBuilder formatBuilder = new StringBuilder();
            if (toMe) {
                formatBuilder.append(TO_SIGN_STRING).append(context.getString(R.string.me));
                if (otherCount > 0) {
                    QiupuSimpleUser qsUser = otherRecipient[0];
                    formatBuilder.append(SEPARATOR_COMMA).append(qsUser.nick_name);
                    if (otherCount > 1) {
                        qsUser = otherRecipient[1];
                        formatBuilder.append(SEPARATOR_COMMA).append(qsUser.nick_name);
                        if (otherCount > 2) {
                            if (otherCount == 3) {
                                qsUser = otherRecipient[2];
                                formatBuilder.append(SEPARATOR_COMMA).append(qsUser.nick_name);
                            } else {
                                formatBuilder.append(context.getString(R.string.other_more_people, otherCount - 2));
                            }
                        }
                    }
                }
            } else {
                if (otherCount > 0) {
                    QiupuSimpleUser qsUser = otherRecipient[0];
                    formatBuilder.append(TO_SIGN_STRING).append(qsUser.nick_name);
                    if (otherCount > 1) {
                        qsUser = otherRecipient[1];
                        formatBuilder.append(SEPARATOR_COMMA).append(qsUser.nick_name);
                        if (otherCount > 2) {
                            if (otherCount == 3) {
                                qsUser = otherRecipient[2];
                                formatBuilder.append(SEPARATOR_COMMA).append(qsUser.nick_name);
                            } else {
                                formatBuilder.append(context.getString(R.string.other_more_people, otherCount - 2));
                            }
                        }
                    }
                }
            }
            return formatBuilder.toString();
        }
    }

    @Override
    public String getText() {
        if (null == post) {
            return null;
        }

        return post.message;
    }

    @Override
    protected Bundle getTagBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("TAGS_URI", "tag/create");
        bundle.putString("type", String.valueOf(QiupuConfig.POST_OBJECT));
        bundle.putString("target_id", post.post_id);
        return bundle;
    }

    protected View setupLocationUi() {
        TextView post_location = (TextView) findViewById(R.id.post_location);
        if (null != post_location) {
            if (isEmpty(post.location)) {
                post_location.setVisibility(View.GONE);
            } else {
                String mapURL = getLocationUrl();
                post_location.setText(Html.fromHtml(mapURL));
                attachMovementMethod(post_location);
                post_location.setVisibility(View.VISIBLE);
            }
        }
        return post_location;
    }

    protected String getLocationUrl() {
        try {
            String locationurlCached = post.getCache("locationurl");
            if (isEmpty(locationurlCached)) {
                Location lc = new Location("gps");
                String[] array = post.location.split(";");
                if (QiupuConfig.IS_USE_BAIDU_LOCATION_API) {
                    lc.setLongitude(Double.valueOf(array[0].substring(array[0].indexOf("=") + 1)));
                    lc.setLatitude(Double.valueOf(array[1].substring(array[1].indexOf("=") + 1)));
                } else {
                    lc.setLongitude(Float.valueOf(array[0].substring(array[0].indexOf("=") + 1)));
                    lc.setLatitude(Float.valueOf(array[1].substring(array[1].indexOf("=") + 1)));
                }

                final String mapURL = parseLocationMapUrl(array, lc);

                post.cacheMeta("locationurl", mapURL);
                return mapURL;
            } else {
                return locationurlCached;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface SetTopInterface {
        public long getTopStreamTargetId();
        public void notifyTopListChanged();
    }

    protected long getTopStreamTargetId() {
        Context context = getContext();
        if (null != context) {
            if (context instanceof SetTopInterface) {
                SetTopInterface topInterface = (SetTopInterface)context;
                return topInterface.getTopStreamTargetId();
            }
        }

        return -1;
    }

    protected String parseLocationMapUrl(String[] array, Location lc) {
        final int size = null == array ? 0 : array.length;
        final String mapUrl;
        if (size == 6) {
            mapUrl = String.format("<a href='%1$s'>%2$s</a>",
                    LocationRequest.getPureMapsSearchString(getContext(), lc),
                    array[5].substring(array[5].indexOf("=") + 1));
        } else {
            mapUrl = LocationRequest.getMapsSearchString(getContext(), lc);
        }

        return mapUrl;
    }
}
