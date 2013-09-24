package com.borqs.common.view;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xml.sax.XMLReader;

import twitter4j.ApkBasicInfo;
import twitter4j.BookBasicInfo;
import twitter4j.CacheMap;
import twitter4j.FileBasicInfo;
import twitter4j.QiupuPhoto;
import twitter4j.QiupuSimpleUser;
import twitter4j.Stream;
import twitter4j.Stream.URLLinkAttachment.URLLink;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.text.Editable;
import android.text.Html;
import android.text.Html.TagHandler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.MediaPlaySelectionAdapter;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.util.AsyncApiUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.FileUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.MyHtml;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.service.ApkFileManager;
import com.borqs.qiupu.ui.QiupuPhotoActivity;
import com.borqs.qiupu.util.StringUtil;

class StreamRowView extends AbstractStreamRowView {
    private static final String TAG = "StreamRowView";

    protected static final int LEN_MAX_LINK_TITLE = 50;
    protected static final int LEN_MAX_EXTRA_TEXT = 100;
    protected static final int DIMENSION_LINK_IMAGE_INDICATOR = 24;
    
    private static final String TO_SIGN_STRING = " > ";
    private static final String SEPARATOR_COMMA = ", ";
    
//    private TextView author_name;

    public StreamRowView(Context ctx, AttributeSet at) {
        super(ctx);

        orm = QiupuORM.getInstance(ctx);

        init();
    }


    public StreamRowView(Context ctx, Stream stream) {
        super(ctx);
        post = stream;

        orm = QiupuORM.getInstance(ctx);

        init();
    }

    public StreamRowView(Context ctx, int resId, Stream stream, boolean isComments) {
        super(ctx);
        post = stream;

        forcomments = isComments;
        orm = QiupuORM.getInstance(ctx);

        init(resId);
    }

    public StreamRowView(Context ctx, Stream stream, boolean isComments) {
        super(ctx);

        post = stream;

        this.forcomments = isComments;
        orm = QiupuORM.getInstance(ctx);

        init();
    }

    StreamRowView(Context ctx, Stream stream, boolean isComments, boolean isReshare) {
        super(ctx);

        post = stream;

        forcomments = isComments;
        isForReshare = isReshare;

        orm = QiupuORM.getInstance(ctx);

        init();
    }

	@Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        init(getLayoutResourceId(post));
    }

    private void init (int layoutResId) {
        removeAllViews();

        View convertView = LayoutInflater.from(mContext).inflate(layoutResId, this);
//        convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
//        addView(convertView);

        setOnClickListener(itemClick);

        setupCommonUi();
        setupPrivacyUi();

        setBackgroundColor(Color.TRANSPARENT);
        setUI();
    }

    // TODO: this method will be reform later, which need to update stream row if
    // possible, rather than current involves too heavy task.
    public void setContent(Stream content) {
    	if (post.equals(content) && post.updated_time == content.updated_time) {
    		setBackgroundColor(Color.TRANSPARENT);
    		
            Log.d(TAG, "setContent, the same stream object, only update its comments.");
            setupSharedStreamFoot();

            requestLayout();
        } else {
            post = content;
            // during the reforming period, it need to re-init as both old and new style layout are co-existing.
            init();
        }
    }

    protected void setUI() {
        if (post.isRetweet()) {
            setupReshareAttachment(post.retweet);
            refreshPostedMessageTextContent(this, post);
            if (post.wasDeletedRetweet()) {
                setStreamUnitUi(this, post, forcomments, isForReshare);
            }
        } else {
            setStreamUnitUi(this, post, forcomments, isForReshare);
        }
    }

    private void setStreamUnitUi(ViewGroup parent, Stream stream, boolean isCommented, boolean isReshared) {
        boolean isNotLocationType = true;

        if (isValidAppAttachment(stream)) {
            setupAppShareAttachment(parent, stream, isCommented);
        } else if (isValidAppLinkAttachment(stream)) {
            setupAppLinkAttachment(parent, stream);
        } else if (isValidUrlLinkAttachment(stream)) {
            setupUrlLinkAttachment(parent, stream);
        } else if (isValidBookAttachment(stream)) {
            setupBookAttachment(parent, stream);
        } else if (isValidMusicAttachment(stream)) {
            setupMusicAttachment(parent, stream);
        } else if (isValidAudioAttachment(stream)) {
            setupAudioAttachment(parent, stream);
        } else if (isValidVideoAttachment(stream)) {
            setupVideoAttachment(parent, stream);
        } else if (isValidStaticFileAttachment(stream)) {
            setupStaticFileAttachment(parent, stream);
        } else if (isValidCardAttachment(stream)) {
            setupCardAttachment(parent, stream);
        } else if (isValidPhotoAttachment(stream)) {
            setupPhotoAttachment(parent, stream);
        } else if (isValidLocationAttachment(stream)) {
            // TODO: type ---> location != null
            setupLocationAttachment(parent, stream);
            isNotLocationType = false;
        } else {
            if(QiupuConfig.DBLOGD)Log.d(TAG, "setUI, without attachment stream type: " + stream.type);
        }

        refreshPostedMessageTextContent(parent, stream);

        setStreamFooterUi(isNotLocationType);
    }

    protected void setStreamFooterUi(boolean isNotLocationType) {
        setupPosterImageRunner(this, post);
        setupPosterSummary(this, post);

        if (isNotLocationType) {
            setupLocationUi();

            setupTimeSpanUi();
            setupStreamIconRunner();
        } else {
            setupTimeSpanUi();
//            if (isReshared) {
//                setReShareLocationUI();
//            }
            setupStreamIconRunner();
        }

        setupSharedStreamFoot();
        requestLayout();
    }

    private String getApkNameMessage(String message) {
        if (null != message) {
            final int start = message.lastIndexOf(mContext.getString(R.string.post_message_last_index_of));
            final int end = message.indexOf(mContext.getString(R.string.post_message_index_of));
            if (start < end) {
                return message.substring(start + 3, end);
            }
        }
        return null;
    }   

    private void gotoBookDetail(BookBasicInfo response) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(response.DETAIL_ACTIVITY_SCHEME));
        intent.addCategory(Intent.CATEGORY_DEFAULT);

        intent.putExtra(BookBasicInfo.KEY_ID, response.id);

        if (BpcApiUtils.isActivityReadyForIntent(getContext(), intent)) {
            getContext().startActivity(intent);
        } else {
            final String pkgName = BpcApiUtils.TARGET_PKG_BROOK;
            final String pkgLabel = getContext().getString(R.string.home_book);
            Log.d(TAG, String.format("gotoBookDetail, download absent app(%s/%s) for intent(%s) info(%s): ",
                    pkgLabel, pkgName, intent.toString(), response.toString()));
            ApkFileManager.shootDownloadAppService(getContext(), pkgName, pkgLabel);
        }
    }

    private void gotoMusicDetail(BookBasicInfo response) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(response.MUSIC_ACTIVITY_SCHEME));
        intent.addCategory(Intent.CATEGORY_DEFAULT);

        intent.putExtra(BookBasicInfo.KEY_ID, response.id);

        if (BpcApiUtils.isActivityReadyForIntent(getContext(), intent)) {
            getContext().startActivity(intent);
        } else {
            final String pkgName = BpcApiUtils.TARGET_PKG_BMUSIC;
            final String pkgLabel = getContext().getString(R.string.home_music);
            Log.d(TAG, String.format("gotoMusicDetail, download absent app(%s/%s) for intent(%s) info(%s): ",
                    pkgLabel, pkgName, intent.toString(), response.toString()));
            ApkFileManager.shootDownloadAppService(getContext(), pkgName, pkgLabel);
        }
    }

    private boolean setInlineComments() {
        List<Stream.Comments.Stream_Post> commentList = (null == post || null == post.comments) ?
                null : post.comments.getCommentList();
        final int size = null == commentList ? 0 : commentList.size();
        final Stream.Comments.Stream_Post firstComment;
        final Stream.Comments.Stream_Post secondComment;
        if (size > 1) {
            firstComment = commentList.get(0);
            secondComment = commentList.get(1);
        } else if (size > 0) {
            firstComment = commentList.get(0);
            secondComment = null;
        } else {
            firstComment = null;
            secondComment = null;
        }
        setInlineCommentItem(firstComment, secondComment);

        return size > 0;
    }

    private int setCommentsSummary() {
        final int totalCount = post.comments.getCount();

//        View iconView = findViewById(R.id.stream_summary_icon);
//        if (null != iconView) {
//            iconView.setVisibility(forcomments || totalCount <= 0 ? GONE : VISIBLE);
//        }

        TextView summaryView = (TextView) findViewById(R.id.qiupu_stream_comment_summary);
        if (null != summaryView) {
            if (totalCount > 0) {
                if (totalCount > 2) {
                    String comments = String.format(commentStr, totalCount);
//                String comments = String.valueOf(totalCount);
                    summaryView.setText(comments);
                    summaryView.setVisibility(View.VISIBLE);
                } else {
//                    summaryView.setText("");
                    summaryView.setVisibility(View.GONE);
                }
            } else {
                summaryView.setVisibility(View.GONE);
//                        summaryView.setText(commentStr);
//                summaryView.setText(String.valueOf(0));
            }
        }
        return totalCount;
    }

    private void setupStreamActionButton(boolean showFlag) {
        View hidingView;
        final int visibility = showFlag ? VISIBLE : GONE;
        hidingView = findViewById(R.id.stream_action_more);
        hidingView.setOnClickListener(showFlag ? moreActionClick : null);
        if (null != hidingView) {
            hidingView.setVisibility(visibility);
        }
        hidingView = findViewById(R.id.stream_like_string);
        if (null != hidingView) {
            hidingView.setVisibility(visibility);
        }
        hidingView = findViewById(R.id.stream_action_comment);
        if (null != hidingView) {
            hidingView.setVisibility(visibility);
            hidingView.setOnClickListener(showFlag ? detailClick : null);
        }
    }

    private void setupSharedStreamFoot() {
        final boolean hasLiker = setupStreamLikeUi();

        final View footer = findViewById(R.id.stream_row_view_footer);
        if (null != footer) {
            // using stream_row_view_footer layout.
            final boolean noLocation = TextUtils.isEmpty(post.location);
            if (forcomments) {
                setInlineCommentItem(null, null);

                if (hasLiker || !noLocation) {
                    footer.setVisibility(VISIBLE);
                    setupStreamActionButton(false);
                } else {
                    footer.setVisibility(GONE);
                }
            } else {
                setCommentsSummary();
                boolean hasComments = setInlineComments();
                if (hasComments || hasLiker || !noLocation) {
                    footer.setVisibility(VISIBLE);
                    setupStreamActionButton(true);
                } else {
                    footer.setVisibility(GONE);
                }
            }
        } else {
            // using stream_row_view_comments layout
            View share_comment_ll = findViewById(R.id.share_comment_ll);
            if (null != share_comment_ll) {
                if (forcomments) {
                    share_comment_ll.setVisibility(GONE);
                } else {
                    final int count = setCommentsSummary();
                    if (count > 0) {
                        setInlineComments();
                        share_comment_ll.setVisibility(VISIBLE);
                    } else {
                        share_comment_ll.setVisibility(GONE);
                    }
                }
            }
        }
        
        View stream_content_rl = findViewById(R.id.stream_content_rl);
        if(null != stream_content_rl) {
        	if (forcomments) {
        		stream_content_rl.setBackgroundDrawable(null);
            }else {
            	stream_content_rl.setBackgroundResource(R.drawable.item_white_bg);
            }
        }
        
    }

    private void setInlineCommentItem(Stream.Comments.Stream_Post first,
                                      Stream.Comments.Stream_Post second) {
        setInlineComment(R.id.qiupu_stream_comment_1, first);
        setInlineComment(R.id.qiupu_stream_comment_2, second);
    }

    private void setInlineComment(int resId, Stream.Comments.Stream_Post current) {
        CommentsSimpleView commentView = (CommentsSimpleView) findViewById(resId);
        if (null != commentView) {
            if (null == current) {
                commentView.setVisibility(View.GONE);
            } else {
                commentView.setCommentItem(current);
                commentView.setVisibility(View.VISIBLE);
                commentView.setOnClickListener(detailClick);
            }
        }
    }

    protected View.OnClickListener detailClick = new StreamRowItemClicker() {
        public void onRowItemClick(View v) {
            if (QiupuConfig.DBLOGD) Log.d(TAG, "onClick, detailClick clicked");
            gotoStreamItemComment();
        }
    };

    protected Object getAttachedObject()
    {
    	return post;
    }
    
    void showStreamMessageDescription(String description) {
        TextView stream_message_des = (TextView) findViewById(R.id.stream_message_des);
        if (null != stream_message_des) {
            final String descTrim = StringUtil.getTrimText(description, LEN_MAX_EXTRA_TEXT);
            if (isEmpty(descTrim)) {
                stream_message_des.setVisibility(View.GONE);
            } else {
                stream_message_des.setVisibility(View.VISIBLE);

                if(hasLinks(descTrim))
                {
	                attachMovementMethod(stream_message_des);	
	                stripHtmlUnderlines(stream_message_des, descTrim);
                }
                else
                {
                	stripHtmlUnderlines(stream_message_des, descTrim);
                }             
            }
        }
    }

    private View showStreamPhotoSpan(View parent, boolean showFlag) {
        View stream_photo_span = null == parent ? null : parent.findViewById(R.id.stream_photo_span);
        if (null != stream_photo_span) {
            stream_photo_span.setVisibility(showFlag ? View.VISIBLE : View.GONE);
        }
        return stream_photo_span;
    }

    private void setupStreamPhotoIndicatorRunner(int defaultIconId, int defaultImageIndex,
                                                 String photoUrl,
                                                 OnClickListener itemDetailClick) {
        setupStreamPhotoIndicatorRunner(defaultIconId, defaultImageIndex, photoUrl, 64, itemDetailClick);
    }

    private void setupStreamPhotoIndicatorRunner(int defaultIconId, int defaultImageIndex,
                                                 String photoUrl, int dimension,
                                                 OnClickListener itemDetailClick) {
        TextView stream_photo_indicator = (TextView) findViewById(R.id.post_content);
        stream_photo_indicator.setOnClickListener(itemDetailClick);

        stream_photo_indicator.setCompoundDrawables(getContext().getResources().getDrawable(defaultIconId), null, null, null);
        ImageRun.shootImageRunner(photoUrl, defaultImageIndex, dimension, stream_photo_indicator, false, true);
    }

    protected void setupStreamPhotoIndicatorRunner(int defaultImageIndex, String photoUrl, int dimension, boolean noImage, boolean needscale) {
        TextView stream_photo_indicator = (TextView) findViewById(R.id.post_content);
        if (null != stream_photo_indicator && !isEmpty(photoUrl)) {
            stream_photo_indicator.setCompoundDrawables(null, null, null, null);
            ImageRun.shootImageRunner(photoUrl, defaultImageIndex, dimension, stream_photo_indicator, noImage, needscale);
        }
    }


    @Override
    protected boolean setupStreamLikeUi() {
        boolean hasLike = super.setupStreamLikeUi();
        setupLikeSummary();
        return hasLike;
    }

    private void setupLikeSummary() {
        TextView stream_like_string = (TextView) findViewById(R.id.stream_like_summary);
        if (null != stream_like_string) {
            final int likerCount = null == post || null == post.likes ? 0 : post.likes.count;

            // Already try parse it from server response, see StreamJSONImpl.StreamJSONImpl().
            StringBuilder likerBuilder = new StringBuilder();
            boolean isMoreLiker = false;
            final long myUid = AccountServiceUtils.getBorqsAccountID();
            final int sizeFriends;
            if (likerCount > 0) {
                sizeFriends = null == post.likes.friends ? 0 : post.likes.friends.size();
                int listIndex = 2;
                if (sizeFriends > 0) {
                    for (QiupuSimpleUser user : post.likes.friends) {
                        if (myUid != user.uid) {
                            String tmpname = user.nick_name;
                            if (!isEmpty(tmpname)) {
                                if (isMoreLiker) {
                                    likerBuilder.append(SEPARATOR_COMMA);
                                } else {
                                    isMoreLiker = true;
                                }
                                listIndex--;
                                likerBuilder.append(tmpname);
                                if (listIndex == 0) {
                                    break;
                                }
                            }
                        }
                    }
                }

                if (likerCount > sizeFriends) {
                    likerBuilder.append(getContext().getString(R.string.other_more_people,
                            (likerCount - sizeFriends)));
                }
            } else {
                sizeFriends = 0;
            }

            final String showLikeTxt;
            if (post.iLike) {
                if (forcomments || sizeFriends > 1) {
                    showLikeTxt = isMoreLiker ? getContext().getString(R.string.who_like, getContext().getString(R.string.i_like) + ", " + likerBuilder) : ilike;
                } else {
                    showLikeTxt = null;
                }
            } else {
                showLikeTxt = isMoreLiker ? likerBuilder + likeStr : null;
            }

            if (TextUtils.isEmpty(showLikeTxt)) {
            	stream_like_string.setVisibility(GONE);
            	stream_like_string.setOnClickListener(null);
            } else {
            	stream_like_string.setVisibility(View.VISIBLE);
                stream_like_string.setText(showLikeTxt);
                stream_like_string.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogUtils.ShowDialogUserListDialog(getContext(),
                                R.string.dialog_like_user_title, post.likes.friends,
                                UserSelectItemView.userClickListener);
                    }
                });
            }
        }
    }

    private void setupStreamIconRunner() {
    	TextView stream_icon = (TextView) findViewById(R.id.post_time);
        if (null != stream_icon) {
            if (null == post.attachment || isEmpty(post.icon)) {
                stream_icon.setCompoundDrawables(null, null, null, null);
            } else {
                stream_icon.setVisibility(View.VISIBLE);
                ImageRun iconrun = new ImageRun(null, post.icon, 0);
                iconrun.noimage = true;
                if(iconrun.setImageView(stream_icon) == false)
                   iconrun.post(null);
            }
        }
    }

    void setupAppScreenShotImageRunner(ImageView image, final String path, int size, OnClickListener clickListener) {
        if (null == image) {
            Log.d(TAG, "setupAppScreenShotImageRunner, invalid image, ignore path: " + path);
            return;
        }

        image.setImageDrawable(null);

        if (StringUtil.isValidString(path)) {
            image.setVisibility(View.VISIBLE);
            if (null == clickListener) {
                clickListener = new StreamRowItemClicker() {
                    public void onRowItemClick(View arg0) {
                        String filePath = "";
                        try {
                            filePath = QiupuHelper.getImageFilePath(new URL(path), true);
                        } catch (MalformedURLException e) {
                        }

                        if (new File(filePath).exists()) {
                            //open in Image view
                            Intent intent = new Intent(getContext(), QiupuPhotoActivity.class);
                            intent.putExtra("photo", path);
                            getContext().startActivity(intent);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(path));
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            getContext().startActivity(intent);
                        }
                    }
                };
            }
            image.setOnClickListener(clickListener);

            ImageRun photoRunner = new ImageRun(null, path, 0);
            photoRunner.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_SCREENSHOT;
            photoRunner.addHostAndPath = true;
            photoRunner.noimage = true;
//            photoRunner.need_scale = true;
            photoRunner.forStreamPhoto=true;
            photoRunner.width = size;
            photoRunner.height = size;
            if(photoRunner.setImageView(image) == false)
            {
	            photoRunner.isSavedMode = QiupuORM.isDataFlowAutoSaveMode(getContext());
	            photoRunner.post(null);
            }
        } else {
//            image.setVisibility(View.GONE);
        }
    }

    TagHandler borqsTagHandler = new TagHandler() {
        @Override
        public void handleTag(boolean opening, String tag, Editable output,
                              XMLReader xmlReader) {
            Log.d(TAG, "tag=" + tag + " output=" + output.toString());
        }
    };

    private void setupAppShareAttachment(ViewGroup parent, Stream stream, boolean forcomments) {
        String description = null;
        boolean isAttachmentShow = false;
//        View attachView = parent.findViewById(R.id.stream_container);
        View attachView = inflateDefaultStreamContainerUnit(parent);
        if (null != attachView) {
            if (stream.attachment.attachments.size() > 0) {
                final ApkBasicInfo response = (ApkBasicInfo) stream.attachment.attachments.get(0);
                if (response != null) {
                    isAttachmentShow = true;

                    final OnClickListener appItemClick = new StreamRowItemClicker() {
                        @Override
                        public void onRowItemClick(View view) {
                            gotoApkDetail(response);
                        }
                    };

                    setupStreamPhotoIndicatorRunner(R.drawable.default_app_icon,
                            QiupuConfig.DEFAULT_IMAGE_INDEX_APK, response.iconurl, null);

                    String appSummaryCache = stream.getCache(CacheMap.appSummary);
                    if (isEmpty(appSummaryCache)) {
                        StringBuffer appSummary = new StringBuffer();
                        appSummary.append(response.label).append("<br>");
                        appSummary.append("NO.").append(response.versioncode);
                        appSummary.append("  v").append(response.versionname);
                        appSummary.append("<br>");
                        appSummary.append(FileUtils.formatPackageFileSizeString(getContext(), response.apksize));

                        String htmlStr = MyHtml.toDumbClickableText(appSummary.toString());
                        stream.cacheMeta(CacheMap.appSummary, htmlStr);
                        setupPostContentUi(htmlStr, appItemClick);
                    } else {
                        setupPostContentUi(appSummaryCache, appItemClick);
                    }

                    description = response.description; // record description, which was set later.

                    if (!forcomments && orm.showApkScreenSnap()) {
                        inflatePhotoAlbum(attachView, response.screenshotLink);
                    } else {
//                    showImagePicUi(false);
                    }
                } else {
                    Log.w(TAG, "parseApkPostsAttachmentsUi, fail to get apk info from: "
                            + stream.attachment.attachments);
                }
            }

            attachView.setVisibility(isAttachmentShow ? View.VISIBLE : View.GONE);
        }
        showStreamMessageDescription(description);
    }

    private void setupAppLinkAttachment(ViewGroup parent, Stream stream) {
        final String href = stream.attachment.link.href;
        boolean isAttachmentShow = false;
//        View attachView = parent.findViewById(R.id.stream_container);
        View attachView = inflateDefaultStreamContainerUnit(parent);
        if (null != attachView) {
            if (!isEmpty(href)) {
                isAttachmentShow = true;

                final OnClickListener appLinkClick = new StreamRowItemClicker() {
                    public void onRowItemClick(View arg0) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse(href));
                        getContext().startActivity(intent);
                    }
                };

                final StringBuilder txtContent = new StringBuilder();
                txtContent.append(getContext().getString(R.string.post_prompt)).append(" ");
                final String apkLink = getApkNameMessage(stream.message);
                if (null == apkLink) {
                    int start = href.indexOf("=");
                    txtContent.append(href.substring(start + 1, href.length()));
                } else {
                    txtContent.append(apkLink);
                }

                String apkurllink = stream.getCache(CacheMap.apkurllink);
                if (isEmpty(apkurllink)) {
                    final String htmlText = MyHtml.toDumbClickableText(txtContent.toString());
                    setupPostContentUi(htmlText, appLinkClick);
                    stream.cacheMeta(CacheMap.apkurllink, htmlText);
                } else {
                    setupPostContentUi(apkurllink, appLinkClick);
                }

                if (href.contains(QiupuConfig.MARKET_SEARCH_HOST)) {
                    inflateAttachmentExtra(attachView, null, null);
                }
            }

            attachView.setVisibility(isAttachmentShow ? View.VISIBLE : View.GONE);
        }
        showStreamMessageDescription(null);
    }

    protected void setupUrlLinkAttachment(ViewGroup parent, Stream stream) {
        String description = null;
        final URLLink link = (URLLink) stream.attachment.attachments.get(0);
        boolean isAttachmentShow = false;
        View attachView = inflateDefaultStreamContainerUnit(parent);
        if (null != attachView) {
            if (null != link) {
                isAttachmentShow = true;

                final OnClickListener itemDetailClick = new StreamRowItemClicker() {
                    public void onRowItemClick(View view) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(link.url));
                            intent.putExtra(Browser.EXTRA_APPLICATION_ID, getContext().getPackageName());
                            getContext().startActivity(intent);
                        } catch (Exception ne) {
                        }
                    }
                };

                final String linkContent;
                String urllinkContentcache = stream.getCache(CacheMap.urllinkContent);
                if (isEmpty(urllinkContentcache)) {
                    if (TextUtils.isEmpty(link.host)) {
                        linkContent = StringUtil.getTrimText(link.title, LEN_MAX_LINK_TITLE);
                    } else {
                        linkContent = String.format("<a href=\'%1$s\'>%2$s</a>", link.url,
                                isEmpty(link.title) == false ?
                                        (StringUtil.getTrimText(link.title, LEN_MAX_LINK_TITLE) + "<br>" + link.host) :
                                        link.host);
                    }

                    stream.cacheMeta(CacheMap.urllinkContent, linkContent);
                } else {
                    linkContent = urllinkContentcache;
                }

                final String linkIndicatorImageUrl = TextUtils.isEmpty(linkContent.trim()) ? "" : link.favorite_icon_url;
                setupStreamPhotoIndicatorRunner(QiupuConfig.DEFAULT_IMAGE_INDEX_LINK, linkIndicatorImageUrl,
                        DIMENSION_LINK_IMAGE_INDICATOR, false, true);

                setupPostContentUi(linkContent, null);

                description = link.description;

                inflatePhotoAlbum(attachView, link.all_image_urls, itemDetailClick);
            }

            attachView.setVisibility(isAttachmentShow ? View.VISIBLE : View.GONE);
        }
        showStreamMessageDescription(description);
    }
    
    protected void setupPhotoAttachment(ViewGroup parent, final Stream stream) {
        String description = null;
        if(stream.attachment.attachments.size() <= 0) {
        	Log.e(TAG, "setupPhotoAttachment, invalid photo attachment, stream= " + stream.toString());
        	return ;
        }
        boolean isAttachmentShow = false;
        View attachView = inflateDefaultStreamContainerUnit(parent);
        if (null != attachView) {
            isAttachmentShow = true;

            setupStreamPhotoIndicatorRunner(QiupuConfig.DEFAULT_IMAGE_INDEX_LINK, null,
                    DIMENSION_LINK_IMAGE_INDICATOR, false, true);

            String albumName = null;
            ArrayList<QiupuPhoto> photos = new ArrayList<QiupuPhoto>();
            for (Object item : stream.attachment.attachments) {
                QiupuPhoto photo = (QiupuPhoto) item;
                photos.add(photo);
                if (TextUtils.isEmpty(description)) {
                    description = photo.caption;
                }
                if (TextUtils.isEmpty(albumName)) {
                    albumName = photo.album_name;
                }
            }
            OnClickListener coverClicker = inflatePhotoTypeAlbum(attachView, photos, stream);
            final int photoCount = photos.size();
            if (photoCount > 0) {
                final String albumSummary = mContext.getString(R.string.stream_content_share_photo,
                        photoCount);
                setupPostContentUi(albumSummary, coverClicker, true);
            } else {
                setupPostContentUi("", null);
            }

            if (!TextUtils.isEmpty(albumName)) {
                final String htmlText;
                String cachedHtmlText = stream.getCache(CacheMap.albumDescription);
                if (isEmpty(cachedHtmlText)) {
                    htmlText = MyHtml.toDumbClickableText(getResources().getString(R.string.share_album_mame));
                    stream.cacheMeta(CacheMap.albumDescription, htmlText);
                } else {
                    htmlText = cachedHtmlText;
                }
                inflateAttachmentExtra(attachView, htmlText, coverClicker);
            }

            attachView.setVisibility(isAttachmentShow ? View.VISIBLE : View.GONE);
        }
        showStreamMessageDescription(description);
    }

    private void setupAudioAttachment(ViewGroup parent, final Stream stream) {
        String description = null;
        final FileBasicInfo fileinfo = (FileBasicInfo) stream.attachment.attachments.get(0);
        boolean isAttachmentShow = false;
        View attachView = inflateDefaultStreamContainerUnit(parent);
        if (null != attachView) {
            if (null != fileinfo) {
                isAttachmentShow = true;

//                final OnClickListener itemDetailClick = new StreamRowItemClicker() {
//                    public void onRowItemClick(View view) {
//                        try {
//                        } catch (Exception ne) {
//                        }
//                    }
//                };


                inflateMediaFileAttachmentExtra(attachView, fileinfo, stream);
                setupPostContentUi("", null);

                description = fileinfo.description;

//                ArrayList<String> files = new ArrayList<String>();
//                for (Object item : stream.attachment.attachments) {
//                    FileBasicInfo file = (FileBasicInfo) item;
//                    files.add(file.file_url);
//                }
            }

            attachView.setVisibility(isAttachmentShow ? View.VISIBLE : View.GONE);
        }
        showStreamMessageDescription(description);
    }

    protected void setupVideoAttachment(ViewGroup parent, final Stream stream) {
        String description = null;
        final FileBasicInfo fileinfo = (FileBasicInfo) stream.attachment.attachments.get(0);
        boolean isAttachmentShow = false;
        View attachView = inflateDefaultStreamContainerUnit(parent);
        if (null != attachView) {
            if (null != fileinfo) {
                isAttachmentShow = true;

//                final OnClickListener itemDetailClick = new StreamRowItemClicker() {
//                    public void onRowItemClick(View view) {
//                        try {
//                        } catch (Exception ne) {
//                        }
//                    }
//                };

//                Log.d(TAG, "before === default_iamge_id = " + stream.default_image_random_id);
                if (stream.default_image_random_id == 0) {
                    stream.default_image_random_id = getRandomDefaultIconIndex();
                }
//                Log.d(TAG, "end    === default_iamge_id = " + stream.default_image_random_id);

                inflateMediaFileAttachmentExtra(attachView, fileinfo, stream);
                setupPostContentUi("", null);

                description = fileinfo.description;

//                ArrayList<String> files = new ArrayList<String>();
//                for (Object item : stream.attachment.attachments) {
//                    FileBasicInfo file = (FileBasicInfo) item;
//                    files.add(file.file_url);
//                }
            }

            attachView.setVisibility(isAttachmentShow ? View.VISIBLE : View.GONE);
        }
        showStreamMessageDescription(description);
    }

    private void setupVideoScreenShotImageRunner(View image, final String path, int size, OnClickListener clickListener, int imageRes) {
        if (null == image) {
            Log.d(TAG, "setupAppScreenShotImageRunner, invalid image, ignore path: " + path);
            return;
        }

        image.setBackgroundResource(0);// will use default image

        if (StringUtil.isValidString(path)) {
//            image.setVisibility(View.VISIBLE);
//            if (null == clickListener) {
//                clickListener = new StreamRowItemClicker() {
//                    public void onRowItemClick(View arg0) {
//                        String filePath = "";
//                        try {
//                            filePath = QiupuHelper.getImageFilePath(new URL(path), true);
//                        } catch (MalformedURLException e) {
//                        }
//
//                        if (new File(filePath).exists()) {
//                            //open in Image view
//                            Intent intent = new Intent(getContext(), QiupuPhotoActivity.class);
//                            intent.putExtra("photo", path);
//                            getContext().startActivity(intent);
//                        } else {
//                            Intent intent = new Intent(Intent.ACTION_VIEW);
//                            intent.setData(Uri.parse(path));
//                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            getContext().startActivity(intent);
//                        }
//                    }
//                };
//            }
//            image.setOnClickListener(clickListener);

            Log.d(TAG, "================ path = " + path + " imageRes = " + imageRes);
            ImageRun photoRunner = new ImageRun(null, path, 0);
            photoRunner.default_image_index = imageRes/*QiupuConfig.DEFAULT_IMAGE_INDEX_SCREENSHOT*/;
            photoRunner.addHostAndPath = true;
            photoRunner.noimage = false;
            photoRunner.need_scale = true;
            photoRunner.forStreamPhoto=true;
            photoRunner.width = size;

//            Log.d(TAG, "\n\n================= photoRunner.setImageView(image) = " + photoRunner.setImageView(image) + "\n\n");
            if(photoRunner.setImageView(image) == false)
            {
                photoRunner.isSavedMode = QiupuORM.isDataFlowAutoSaveMode(getContext());
                photoRunner.post(null);
            }
        } else {
//            image.setVisibility(View.GONE);
        }
    }

    private void setupStaticFileAttachment(ViewGroup parent, final Stream stream) {
        String description = null;
        FileBasicInfo fileinfo = null;
        // just to avoid server parse static file failed and return null, if so, client will always crash.
        if (stream != null && stream.attachment != null) {
            if (stream.attachment.attachments != null && stream.attachment.attachments.size() > 0) {
                fileinfo = (FileBasicInfo) stream.attachment.attachments.get(0);
            } else {
                Log.d(TAG, "setupStaticFileAttachment() stream.attachment.attachments = " + stream.attachment.attachments);
                return;
            }
        } else {
            Log.d(TAG, "setupStaticFileAttachment() stream = " + stream);
            return;
        }
        boolean isAttachmentShow = false;
        View attachView = inflateDefaultStreamContainerUnit(parent);
        if (null != attachView) {
            if (null != fileinfo) {
                isAttachmentShow = true;
                inflateMediaFileAttachmentExtra(parent, fileinfo, stream);
                setupPostContentUi("", null);

                description = fileinfo.description;

//                ArrayList<String> files = new ArrayList<String>();
//                for (Object item : stream.attachment.attachments) {
//                    FileBasicInfo file = (FileBasicInfo) item;
//                    files.add(file.file_url);
//                }
                
            }

            attachView.setVisibility(isAttachmentShow ? View.VISIBLE : View.GONE);
        }
        showStreamMessageDescription(description);
    }

    private void setupBookAttachment(ViewGroup parent, Stream stream) {
        String description = null;
        boolean isAttachmentShow = false;
//        View attachView = parent.findViewById(R.id.stream_container);
        View attachView = inflateDefaultStreamContainerUnit(parent);
        if (null != attachView) {
            if (stream.attachment.attachments.size() > 0) {
                final BookBasicInfo response = (BookBasicInfo) stream.attachment.attachments.get(0);
                if (response != null) {
                    isAttachmentShow = true;

                    final OnClickListener itemDetailClick = new StreamRowItemClicker() {
                        public void onRowItemClick(View view) {
                            gotoBookDetail(response);
                        }
                    };

//                attachAttachContentClick(itemDetailClick);

                    setupStreamPhotoIndicatorRunner(R.drawable.default_book,
                            QiupuConfig.DEFAULT_IMAGE_INDEX_BOOK, response.coverurl,
                            itemDetailClick);

                    final String htmlText;
                    String urllinkContentcache = stream.getCache(CacheMap.bookLinkContent);
                    if (isEmpty(urllinkContentcache)) {
                	   StringBuffer appSummary = new StringBuffer();
                	   if(isEmpty(response.writer) == false)
                       appSummary.append(response.name).append("<br><br>");
                	   
                       appSummary.append(response.writer).append("<br>");
                       
                       if(isEmpty(response.size) == false)
                       appSummary.append(FileUtils.formatPackageFileSizeString(getContext(), Integer.parseInt(response.size)));
                        htmlText = MyHtml.toDumbClickableText(appSummary.toString());
                        stream.cacheMeta(CacheMap.bookLinkContent, htmlText);
                    } else {
                        htmlText = urllinkContentcache;
                    }
                    setupPostContentUi(htmlText, itemDetailClick);
                    description = response.summary;
                }
            }

            attachView.setVisibility(isAttachmentShow ? View.VISIBLE : View.GONE);
        }
        showStreamMessageDescription(description);
    }

    private void setupMusicAttachment(ViewGroup parent, Stream stream) {
        String description = null;
        boolean isAttachmentShow = false;
//        View attachView = parent.findViewById(R.id.stream_container);
        View attachView = inflateDefaultStreamContainerUnit(parent);
        if (null != attachView) {
            if (stream.attachment.attachments.size() > 0) {
                final BookBasicInfo response = (BookBasicInfo) stream.attachment.attachments.get(0);
                if (response != null) {
                    isAttachmentShow = true;

                    final OnClickListener itemDetailClick = new StreamRowItemClicker() {
                        public void onRowItemClick(View view) {
                            gotoMusicDetail(response);
                        }
                    };

//                attachAttachContentClick(itemDetailClick);

                    setupStreamPhotoIndicatorRunner(R.drawable.music_default,
                            QiupuConfig.DEFAULT_IMAGE_INDEX_Music, response.coverurl,
                            itemDetailClick);

                    final String htmlText = MyHtml.toDumbClickableText(response.name);
                    setupPostContentUi(htmlText, itemDetailClick);
                    description = response.summary;
                }
            }

            attachView.setVisibility(isAttachmentShow ? View.VISIBLE : View.GONE);
        }
        showStreamMessageDescription(description);
    }

    private void setupCardAttachment(ViewGroup parent, Stream stream) {
        boolean isAttachmentShow = false;
//        View attachView = parent.findViewById(R.id.stream_container);
        View attachView = inflateVcardStreamContainerUnit(parent);
        if (null != attachView) {
            if (!TextUtils.isEmpty(stream.extension)) {
                isAttachmentShow = true;

                TextView nameView = (TextView) attachView.findViewById(R.id.id_user_name);
                LinearLayout vcard = (LinearLayout) attachView.findViewById(R.id.id_vcard);
                ImageView importContact = (ImageView) attachView.findViewById(R.id.import_contact);

                try {
                    JSONTokener jsonTokener = new JSONTokener(stream.extension);
                    JSONObject obj = new JSONObject(jsonTokener);
                    String name = null;
                    String contactWay = null;
                    JSONArray array = null;
                    final ArrayList<String> phoneList = new ArrayList<String>();
                    final ArrayList<String> emailList = new ArrayList<String>();

                    if (!obj.isNull("name")) {
                        name = obj.getString("name");
                    }

                    if (!obj.isNull("phone_email")) {
                        array = obj.getJSONArray("phone_email");
                    }

                    if (null != nameView) {
                        nameView.setText(name);
                    }
                    final String contact_name = name;

                    for (int i = 0; i < array.length(); i++) {
                        if (!array.getJSONObject(i).isNull("phone")) {
                            String phone = array.getJSONObject(i).getString("phone");
                            contactWay = mContext.getResources().getString(R.string.phone_way) + phone;
                            RequestContactInfoSimpleView info = new RequestContactInfoSimpleView(mContext, contactWay);
                            vcard.addView(info);
                            phoneList.add(phone);
                        }

                        if (!array.getJSONObject(i).isNull("email")) {
                            String email = array.getJSONObject(i).getString("email");
                            contactWay = mContext.getResources().getString(R.string.email_way) + email;
                            RequestContactInfoSimpleView info = new RequestContactInfoSimpleView(mContext, contactWay);
                            vcard.addView(info);
                            emailList.add(email);
                        }
                    }

                    final OnClickListener itemDetailClick = new StreamRowItemClicker() {
                        public void onRowItemClick(View view) {
                            DialogUtils.showConfirmDialog(mContext, R.string.import_vcard,
                                    R.string.import_vcard_message, R.string.label_ok, R.string.label_cancel, 
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    boolean success = FileUtils.insertContact(mContext, null, contact_name, phoneList, emailList);
                                    if (success) {
                                        Toast.makeText(mContext, R.string.import_vcard_success, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(mContext, R.string.import_vcard_failed, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    };
                    importContact.setOnClickListener(itemDetailClick);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            attachView.setVisibility(isAttachmentShow ? View.VISIBLE : View.GONE);
        }
//        showStreamMessageDescription(description);
    }

    private void setupLocationAttachment(View parent, Stream stream) {
        TextView post_location = (TextView) parent.findViewById(R.id.post_location);
        if (null != post_location) {
            if (isEmpty(stream.location)) {
                post_location.setVisibility(View.GONE);
            } else {
                try {
                    Location lc = new Location("gps");
                    String[] array = stream.location.split(";");

                    if (QiupuConfig.IS_USE_BAIDU_LOCATION_API) {
                        lc.setLongitude(Double.valueOf(array[0].substring(array[0].indexOf("=") + 1)));
                        lc.setLatitude(Double.valueOf(array[1].substring(array[1].indexOf("=") + 1)));
                    } else {
                        lc.setLongitude(Float.valueOf(array[0].substring(array[0].indexOf("=") + 1)));
                        lc.setLatitude(Float.valueOf(array[1].substring(array[1].indexOf("=") + 1)));
                    }

                    final String mapURL = parseLocationMapUrl(array, lc);

                    post_location.setPadding(0, 5, 0, 0);
                    post_location.setText(Html.fromHtml(mapURL));
                    post_location.setVisibility(View.VISIBLE);
                    attachMovementMethod(post_location);
                } catch (Exception ne) {
                }
            }
        }
    }

    private void inflateAttachmentExtra(View parent, String overlayText, OnClickListener clicker) {
        RelativeLayout extraContainer = (RelativeLayout) parent.findViewById(R.id.stream_extra_span);
        if (null != extraContainer) {
            if (extraContainer.getChildCount() > 0) {
                extraContainer.removeAllViews();
            }

            if (isEmpty(overlayText)) {
                extraContainer.setVisibility(View.GONE);
            } else {
                TextView extraView = (TextView) LayoutInflater.from(getContext()).
                        inflate(R.layout.stream_row_attachment_extra_market_download, null);
                if (clicker == null) {
                    extraView.setText(overlayText);
                } else {
                    extraView.setText(MyHtml.fromHtml(overlayText));
//                    attachMovementMethod(extraView);
                }
                extraView.setOnClickListener(clicker);

                extraView.setCompoundDrawables(null, null, null, null);
                attachMovementMethod(extraView);

                extraContainer.addView(extraView);
                extraContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    private void inflateMediaFileAttachmentExtra(View parent, final FileBasicInfo fileinfo, Stream stream) {
        RelativeLayout extraContainer = (RelativeLayout) parent.findViewById(R.id.stream_extra_span);
        if (null != extraContainer) {
            if (extraContainer.getChildCount() > 0) {
                extraContainer.removeAllViews();
            }

            if (isEmpty(fileinfo.title)) {
                extraContainer.setVisibility(View.GONE);
            } else {
                View view = null;
                if ((stream.type & BpcApiUtils.STATIC_FILE_POST) == BpcApiUtils.STATIC_FILE_POST) {
                    view = LayoutInflater.from(mContext).inflate(R.layout.static_file_attachment_layout, null);
                    TextView extraView = (TextView) view.findViewById(R.id.static_file_title);
                    extraView.setText(fileinfo.title);
                    TextView sizeView = (TextView) view.findViewById(R.id.static_file_size);
                    sizeView.setText(getFileSize(fileinfo));
//                    attachMovementMethod(extraView);
//                    ImageView download = (ImageView) view.findViewById(R.id.down_load_id);
//                    download.setVisibility(View.VISIBLE);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setFileTipUI(fileinfo, false);
                        }
                    });
                } else {
                    View bgView = null;
                    TextView extraView = null;
                    TextView sizeView = null;

                    if ((stream.type & BpcApiUtils.AUDIO_POST) == BpcApiUtils.AUDIO_POST) {
                        view = LayoutInflater.from(getContext()).
                                inflate(R.layout.audio_attachment_layout, null);
                        view.setLayoutParams(new LayoutParams((int)mContext.getResources().getDimension(R.dimen.audio_layout_width), (int)mContext.getResources().getDimension(R.dimen.audio_layout_height)));
                        bgView = view.findViewById(R.id.background_id);
                        extraView = (TextView) view.findViewById(R.id.qiupu_download_from_market);
                        sizeView = (TextView) view.findViewById(R.id.media_size);

                        sizeView.setText(getFileSize(fileinfo));

//                        ImageView playView = (ImageView) view.findViewById(R.id.play_id);
//                        playView.setBackgroundResource(R.drawable.ic_play_overlay);
//                        screenShot.setImageResource(R.drawable.notification_btn_bg);//TODO:will use default image
                    } else if ((stream.type & BpcApiUtils.VIDEO_POST) == BpcApiUtils.VIDEO_POST) {
                        view = LayoutInflater.from(getContext()).
                                inflate(R.layout.media_attachment_layout, null);
                        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)mContext.getResources().getDimension(R.dimen.stream_big_image_dimension_device)));
                        bgView = view.findViewById(R.id.background_id);
                        extraView = (TextView) view.findViewById(R.id.qiupu_download_from_market);
                        sizeView = (TextView) view.findViewById(R.id.media_size);

                        sizeView.setText(getFileSize(fileinfo));

                        ImageView screenShot = (ImageView) view.findViewById(R.id.screen_shot_id);
//                        screenShot.setImageResource(R.drawable.video_bg);
//                        screenShot.setImageResource(/*R.color.video_background_color*/resId);

                        ImageView playView = (ImageView) view.findViewById(R.id.play_id);
                        playView.setBackgroundResource(R.drawable.video_overlay);
                        final int dimSmall = (int) getResources().getDimension(
                                R.dimen.stream_big_image_dimension_device);
                        
//                        Log.d(TAG, "=====>>> post_id = " + stream.post_id);
//                        Log.d(TAG, "=====>>> stream.created_time" + stream.created_time);
//                        if (post.retweet != null) {
//                            Log.d(TAG, "=====>>> post.retweet.post_id = " + post.retweet.post_id);
//                            Log.d(TAG, "=====>>> post.retweet.created_time" + post.retweet.created_time);
//                        }
                        if (TextUtils.isEmpty(fileinfo.thumbnail_url)) {
                            //TODO: should remove this code after server return video screenShot
                            fileinfo.thumbnail_url = "postid_" + stream.post_id + "_" + (int)(Math.random()*100);
                        }
                        setupVideoScreenShotImageRunner(screenShot, fileinfo.thumbnail_url, dimSmall, null, stream.default_image_random_id);
                    } else {
                        Log.d(TAG,"unsupported file type");
                    }

                    if (extraView != null) {
                        extraView.setText(fileinfo.title);
                        extraView.setCompoundDrawables(null, null, null, null);
//                        attachMovementMethod(extraView);
                    }

                    if (bgView != null) {
                        bgView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setFileTipUI(fileinfo, true);
                            }
                        });
                    } else {
                        Log.d(TAG, "============ bgView is null");
                    }
                }

                if(view != null) {
                    extraContainer.addView(view);
                    extraContainer.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private String getFileSize(FileBasicInfo fileinfo) {
        return FileUtils.formatFileSize(fileinfo.file_size, 1);
    }

    private void setFileTipUI(final FileBasicInfo fileinfo, final boolean isMediaFile) {
        try {
            ListView listView = (ListView) ((Activity) mContext).getLayoutInflater().inflate(R.layout.default_listview, null);
            listView.setAdapter(new MediaPlaySelectionAdapter(mContext, isMediaFile));
    
            final AlertDialog alertDialog = DialogUtils.ShowDialogwithView(mContext, mContext.getString(R.string.play_title),
                    0, listView, null, null);
            listView.setBackgroundResource(R.color.white);
    
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    if (MediaPlayItemView.class.isInstance(view)) {
                        MediaPlayItemView itemView = (MediaPlayItemView)view;
                        setListItemClickEvent(itemView, fileinfo, isMediaFile);
    
                        if (null != alertDialog && alertDialog.isShowing()) {
                            alertDialog.dismiss();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setListItemClickEvent(MediaPlayItemView itemView, final FileBasicInfo fileinfo, final boolean isMediaFile) {
        try {
//            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//            State wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
//            State mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
//            String title = itemView.getItemData();
//
//            if (wifi == State.CONNECTED || wifi == State.CONNECTING) {
//                setWifiStatusAction(title, fileinfo);
//            } else if (mobile == State.CONNECTED || wifi == State.CONNECTING) {
//                setMobileStatusAction(title, fileinfo);
//            } else {
//                Toast.makeText(mContext, R.string.no_wifi_mobile, Toast.LENGTH_SHORT).show();
//            }
        	
            String title = itemView.getItemData();
            if(isNetworkConnected(mContext)) {
            	if(isWifiConnected(mContext)) {
            		setWifiStatusAction(title, fileinfo);
            	}else if(isMobileConnected(mContext)) {
            		setMobileStatusAction(title, fileinfo);
            	}
            }else {
                Toast.makeText(mContext, R.string.no_wifi_mobile, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean isNetworkConnected(Context context) {  
        if (context != null) {  
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
                    .getSystemService(Context.CONNECTIVITY_SERVICE);  
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
            if (mNetworkInfo != null) {  
                return mNetworkInfo.isAvailable();  
            }  
        }  
        return false;  
    }
    
    public boolean isMobileConnected(Context context) {  
        if (context != null) {  
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
                    .getSystemService(Context.CONNECTIVITY_SERVICE);  
            NetworkInfo mMobileNetworkInfo = mConnectivityManager  
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);  
            if (mMobileNetworkInfo != null) {  
                return mMobileNetworkInfo.isAvailable();  
            }  
        }  
        return false;  
    }
    
    public boolean isWifiConnected(Context context) {  
        if (context != null) {  
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
                    .getSystemService(Context.CONNECTIVITY_SERVICE);  
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager  
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
            if (mWiFiNetworkInfo != null) {  
                return mWiFiNetworkInfo.isAvailable();  
            }  
        }  
        return false;  
    }

    private void setMobileStatusAction(String title, FileBasicInfo fileinfo) {
        try {
            if (mContext.getResources().getString(R.string.play_pos_button).equals(title)) {
                showPlayMediaDialog(fileinfo);
            } else if (mContext.getResources().getString(R.string.play_neu_button).equals(title)) {
                showDownloadDialog(fileinfo);
            } else {
                Log.d(TAG, "error, unsupported type");
            }
        } catch (Exception e) {
            Log.d(TAG, "setMobileStatusAction() error msg = " + e.getMessage());
        }
    }

    private void setWifiStatusAction(String title, FileBasicInfo fileinfo) {
        try {
            if (mContext.getResources().getString(R.string.play_pos_button).equals(title)) {
                playMediaFile(fileinfo);
            } else if (mContext.getResources().getString(R.string.play_neu_button).equals(title)) {
                downloadMediaFile(fileinfo);
            } else {
                Log.d(TAG, "error, unsupported type");
            }
        } catch (Exception e) {
            Log.d(TAG, "setWifiStatusAction() error msg = " + e.getMessage());
        }
    }

    private void showPlayMediaDialog(final FileBasicInfo fileinfo) {
        DialogUtils.showConfirmDialog(mContext, R.string.play_title, R.string.play_msg, 
                R.string.label_ok, R.string.label_cancel, 
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                playMediaFile(fileinfo);
            }
        });
    }

    private void showDownloadDialog(final FileBasicInfo fileinfo) {
        DialogUtils.showConfirmDialog(mContext, R.string.play_title, R.string.play_msg, 
                R.string.label_ok, R.string.label_cancel, 
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadMediaFile(fileinfo);
            }
        });
    }

    private void playMediaFile(FileBasicInfo fileinfo) {
        try {
            if(fileinfo == null || TextUtils.isEmpty(fileinfo.file_url)) {
                return;
            }

            Intent playerIntent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(fileinfo.file_url);
            playerIntent.setDataAndType(uri , fileinfo.content_type);
            mContext.startActivity(playerIntent);
        } catch (Exception e) {
            Log.d(TAG, "playMediaFile() e.getMessage() = " + e.getMessage());
            Toast.makeText(mContext, R.string.media_play_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadMediaFile(FileBasicInfo fileinfo) {
        try {
            if (fileinfo == null || TextUtils.isEmpty(fileinfo.file_url)) {
                return;
            }
            
            if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO)
            {
            	android.app.DownloadManager dm = (android.app.DownloadManager)this.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            	android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(Uri.parse(fileinfo.file_url));
            	
            	 String newMimeType =
                         MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(fileinfo.file_url));
                 if (newMimeType != null) {
                	 request.setMimeType(newMimeType);
                 }

            	
            	if(fileinfo.file_url.endsWith(".apk"))
            	{
            	    request.setMimeType("application/vnd.android.package-archive");
            	}
            	
    			request.setVisibleInDownloadsUi(true);
    			
    			if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB)
    			{
    				try{
    				    request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE|android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
    				}catch(Exception ne){}
    			}
    			
    			long enqueue = dm.enqueue(request);
    			
    			Log.d(TAG, "begin download file="+fileinfo.file_url);
            }
            else
            {
	            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileinfo.file_url));
	            browserIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	            mContext.startActivity(browserIntent);
            }
        } catch (Exception e) {
            Log.d(TAG, "downloadMediaFile() e.getMessage() = " + e.getMessage());
        }
    }

    public Stream getStream() {
        return post;
    }

    private boolean inflatePhotoAlbum(View attachmentContainer, ArrayList<String> screenshotLink) {
        return inflatePhotoAlbum(attachmentContainer, screenshotLink, null);
    }

    private int getResourceIdForImages(int count) {
        if (count == 2) {
            return R.layout.stream_row_attachment_newui_big_two_photo;
        } else if (count > 2) {
            return R.layout.stream_row_attachment_newui_big_more_photo;
        } else {
            return R.layout.stream_row_attachment_newui_big_photo;
        }
    }

    private OnClickListener inflatePhotoTypeAlbum(View attachmentContainer, final ArrayList<QiupuPhoto> photos, final Stream stream) {
        OnClickListener coverClick = null;

        final int screenShotSize = null == photos ? 0 : photos.size();
        final boolean showFlag = screenShotSize > 0;
        ViewGroup extraContainer = (ViewGroup)showStreamPhotoSpan(attachmentContainer, showFlag);

        if (showFlag && null != extraContainer) {
            final int resId = getResourceIdForImages(screenShotSize);
            View photoView = LayoutInflater.from(getContext()).inflate(resId, null);
            extraContainer.removeAllViews();
            extraContainer.addView(photoView);
            extraContainer.setVisibility(View.VISIBLE);

            for (QiupuPhoto p : photos) {
                p.from_user_id = stream.fromUser.uid;
                p.from_nick_name = stream.fromUser.nick_name;
                p.from_image_url = stream.fromUser.profile_image_url;
            }

            int dimension = (int) getResources().getDimension(
                    R.dimen.stream_big_image_dimension);
            ImageView stream_photo_1 = (ImageView) findViewById(R.id.stream_photo_1);
            coverClick = instancePhotoClicker(photos, stream, 0, photos);
            setupAppScreenShotImageRunner(stream_photo_1,
                    photos.get(0).photo_url_small, dimension, coverClick);

            if (screenShotSize > 1) {
                int dimSmall = (int) getResources().getDimension(
                        R.dimen.stream_big_image_small_dimension);
                if (screenShotSize == 2) {
                    dimSmall = (int) getResources().getDimension(
                            R.dimen.stream_big_image_dimension);
                }

                ImageView stream_photo_2 = (ImageView) findViewById(R.id.stream_photo_2);
                setupAppScreenShotImageRunner(stream_photo_2,
                        photos.get(1).photo_url_middle, dimSmall, new StreamRowItemClicker() {
                    public void onRowItemClick(View view) {
                        try {
                            IntentUtil.startPhotosViewIntent(getContext(), Long.valueOf(photos.get(1).album_id), stream.fromUser.uid,
                                    1, photos.get(1).album_name,
                                    photos, stream.fromUser.nick_name);
                        } catch (Exception ne) {
                        }
                    }
                });

                if (screenShotSize > 2) {
                    ImageView stream_photo_3 = (ImageView) findViewById(R.id.stream_photo_3);
                    setupAppScreenShotImageRunner(stream_photo_3,
                            photos.get(2).photo_url_middle, dimSmall, new StreamRowItemClicker() {
                        public void onRowItemClick(View view) {
                            try {
                                IntentUtil.startPhotosViewIntent(getContext(), Long.valueOf(photos.get(2).album_id), stream.fromUser.uid,
                                        2, photos.get(2).album_name,
                                        photos, stream.fromUser.nick_name);
                            } catch (Exception ne) {
                            }
                        }
                    });
                }
            }
        }

        return coverClick;
    }

    OnClickListener instancePhotoClicker(final ArrayList<QiupuPhoto> photos, final Stream stream, final int index, final ArrayList<QiupuPhoto> photoList) {
        return new StreamRowItemClicker() {
            public void onRowItemClick(View view) {
                try {
                    final QiupuPhoto photo = photos.get(index);
                    final QiupuSimpleUser user = stream.fromUser;
                	IntentUtil.startPhotosViewIntent(getContext(), Long.valueOf(photo.album_id), user.uid,
                			index, photo.album_name,
                			photoList, user.nick_name);
                } catch (Exception ne) {
                }
            }
        };
    }

    protected boolean inflatePhotoAlbum(View attachmentContainer,
                                      ArrayList<String> screenLink,
                                      OnClickListener clickListener) {
        final int screenShotSize = null == screenLink ? 0 : screenLink.size();
        final boolean showFlag = screenShotSize > 0;
        ViewGroup extraContainer = (ViewGroup) showStreamPhotoSpan(attachmentContainer, showFlag);
        if (showFlag && null != extraContainer) {
            int resId = getResourceIdForImages(screenLink.size());
            View photoView = LayoutInflater.from(getContext()).inflate(resId, null);
            extraContainer.removeAllViews();
            extraContainer.addView(photoView);
            extraContainer.setVisibility(View.VISIBLE);

            final int dimension = (int) getResources().getDimension(
                    R.dimen.stream_image_dimension);
            ImageView stream_photo_1 = (ImageView) findViewById(R.id.stream_photo_1);
            setupAppScreenShotImageRunner(stream_photo_1,
                    screenLink.get(0), dimension, clickListener);

            if (screenShotSize > 1) {
                final int dimSmall = (int) getResources().getDimension(
                        R.dimen.stream_image_small_dimension);
                ImageView stream_photo_2 = (ImageView) findViewById(R.id.stream_photo_2);
                setupAppScreenShotImageRunner(stream_photo_2,
                        screenLink.get(1), dimSmall, clickListener);

                if (screenShotSize > 2) {
                    ImageView stream_photo_3 = (ImageView) findViewById(R.id.stream_photo_3);
                    setupAppScreenShotImageRunner(stream_photo_3,
                            screenLink.get(2), dimSmall, clickListener);
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Show reshared post info to ui item in stream_reshare_row_view,
     * @param origin
     */
    private void setupReshareAttachment(Stream origin) {
        boolean isVisibleContainer = false;
        ViewGroup container = (ViewGroup)findViewById(R.id.stream_reshared_stream);
        if (null != container) {
            if (null != origin) {
                isVisibleContainer = true;
                setStreamUnitUi(container, origin, forcomments, true);
                setupPosterImageRunner(container, origin);
                setupOriginalPosterName(container, origin);
            } else {
                // The origin stream has been removed.
                TextView textView = (TextView)container.findViewById(R.id.reshare_source);
                if (null != textView) {
                    isVisibleContainer = true;
                    textView.setText(R.string.exception_stream_removed);
                }
            }

            container.setVisibility(isVisibleContainer ? View.VISIBLE : View.GONE);
        }
    }

    private ViewGroup reattachStreamContainerUnit(ViewGroup parent, int targetId) {
        View container = parent.findViewById(R.id.stream_container);
        if (null != container && container instanceof LinearLayout) {
            LinearLayout attachment = (LinearLayout) container;
            if (null != attachment) {
                if (attachment.getChildCount() > 0) {
                    attachment.removeAllViews();
                }
                View views = LayoutInflater.from(getContext()).inflate(targetId, null);
                attachment.addView(views);
            }

            return attachment;
        }

        return parent;
    }

    private ViewGroup inflateDefaultStreamContainerUnit(ViewGroup parent) {
        return reattachStreamContainerUnit(parent, R.layout.stream_row_attachment);
    }

    private ViewGroup inflateVcardStreamContainerUnit(ViewGroup parent) {
        return reattachStreamContainerUnit(parent, R.layout.stream_row_vcard);
    }

    OnClickListener moreActionClick = new StreamRowItemClicker() {
        public void onRowItemClick(View arg0) {
            showMoreStreamOptions();
        }
    };

    private static class OptionLabel {
        boolean init = false;
        static String RE_SHARE;
        static String COPY_MESSAGE;
        static String MUTE;
        static String MUTE_ALL;
        static String ADD_TAG;
        static String DELETE;
        static String REPORT_ABUSE;
        static String ADD_FAVORITE;
        static String DEL_FAVORITE;
        static String TOP_SET;
        static String TOP_UNSET;
        static void init(Context context) {
            RE_SHARE = context.getString(R.string.news_feed_reshare);
            COPY_MESSAGE = context.getString(R.string.item_copy);
            MUTE = context.getString(R.string.menu_mute_post);
            MUTE_ALL = context.getString(R.string.menu_mute_all_his_post);
            ADD_TAG = context.getString(R.string.item_tags);
            DELETE = context.getString(R.string.delete_comment);
            REPORT_ABUSE = context.getString(R.string.menu_report_abuse);
            ADD_FAVORITE = context.getString(R.string.add_favorites);
            DEL_FAVORITE = context.getString(R.string.remove_favorites);
            TOP_SET = context.getString(R.string.set_top);
            TOP_UNSET = context.getString(R.string.cancel_top);
        }
    }
    private String[] mDialogItems;
    private static OptionLabel mLabels = new OptionLabel();

    private void showMoreStreamOptions() {
        mDialogItems = buildDialogItemContent();
        String dialogTitle = getContext().getString(R.string.context_menu_stream_title, post.fromUser.nick_name);
//        DialogUtils.showItemsDialog(getContext(), dialogTitle, R.drawable.btn_stream_more_bg, mDialogItems, mChooseItemClickListener);
        DialogUtils.showItemsDialog(getContext(), dialogTitle, -1, mDialogItems, mChooseItemClickListener);
    }

    private String[] buildDialogItemContent() {
        if (!mLabels.init) {
            mLabels.init(getContext());
        }

        ArrayList<String> actionList = new ArrayList<String>();
        final long myId = AccountServiceUtils.getBorqsAccountID();

        if (null != post) {
            if (post.isOwnBy(myId)) {
                actionList.add(mLabels.DELETE);
            } else {
                actionList.add(mLabels.REPORT_ABUSE);
                actionList.add(mLabels.MUTE);
                // TODO : add later.
    //            actionList.add(mLabels.MUTE_ALL);
            }


            if (post.canReshare) {
                actionList.add(mLabels.RE_SHARE);
            }

            if (!TextUtils.isEmpty(post.message)) {
                actionList.add(mLabels.COPY_MESSAGE);
            }

            if (BpcApiUtils.isActivityReadyForAction(getContext(), IntentUtil.WUTONG_ACTION_TAGS)) {
                actionList.add(mLabels.ADD_TAG);
            }

//            if (post.isFavorite) {
//                actionList.add(mLabels.ADD_FAVORITE);
//            } else {
//                actionList.add(mLabels.DEL_FAVORITE);
//            }

            final long circleId = getTopStreamTargetId();
            if (circleId > 0) {
                final String targets = post.top_in_targets;
                if (TextUtils.isEmpty(targets) == false) {
                    String[] targetArray = targets.split(",");
                    ArrayList<String> targetList = new ArrayList<String>();
                    for (String target : targetArray) {
                        if (TextUtils.isEmpty(target) == false) {
                            targetList.add(target);
                        }
                    }

                    if (targetList.contains(String.valueOf(circleId))) {
                        actionList.add(mLabels.TOP_UNSET);
                    } else {
                        actionList.add(mLabels.TOP_SET);
                    }
                } else {
                    actionList.add(mLabels.TOP_SET);
                }
            }
        }

        String[] dialogItems = new String[actionList.size()];
        return actionList.toArray(dialogItems);
    }

    private DialogInterface.OnClickListener mChooseItemClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            final String item = mDialogItems[which];
            if (item.equals(mLabels.RE_SHARE)) {
                gotoStreamReshare();
            } else if (item.equals(mLabels.COPY_MESSAGE)) {
                copyItemText(getContext(), getText());
            } else if (item.equals(mLabels.MUTE)) {
                mutePost(2);
            } else if (item.equals(mLabels.MUTE_ALL)) {
                mutePost(1);
            } else if (item.equals(mLabels.ADD_TAG)) {
                Bundle bundle = getTagBundle();
                tagItem(getContext(), bundle);
            } else if (item.equals(mLabels.DELETE)) {
                DialogUtils.showConfirmDialog(getContext(), R.string.delete_post_title,
                        R.string.post_delete_question,
                        R.string.label_ok, R.string.label_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                removeStream();
                            }
                        });
            } else if (item.equals(mLabels.REPORT_ABUSE)) {
                DialogUtils.showConfirmDialog(getContext(),
                        R.string.menu_report_abuse, R.string.post_report_question,
                        R.string.label_ok, R.string.label_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                reportAbuse();
                            }
                        });
            } else if (item.equals(mLabels.ADD_FAVORITE)) {
                ;
            } else if (item.equals(mLabels.DEL_FAVORITE)) {
                ;
            } else if (item.equals(mLabels.TOP_SET)) {
                setTopList(true);
            } else if (item.equals(mLabels.TOP_UNSET)) {
                setTopList(false);
            } else {
                Log.d(TAG, "Dialog click listener, unexpected item: " + item);
            }
        }
    };

    /**
     * @param muteType, 1 - user, 2 - stream, 3 - comments
     */
    protected void mutePost(final int muteType) {
//        final int STREAM_MUTE_TYPE = 2; // 1 - user, 3 - comments.
        final String objectId = post.post_id;
        //no need show progress dialog;
        //        final Dialog dlg = DialogUtils.showProgressDialog(this, R.string.menu_mute_post,
        //                getString(R.string.status_update_summary));
        AsyncApiUtils.asyncInstance.muteObject(AccountServiceUtils.getSessionID(), objectId,
                muteType, new TwitterAdapter() {
            @Override
            public void muteObject(final boolean result) {
                Log.d(TAG, "mutePost, get result: " + result);
                final int msgId = result ? R.string.mute_object_suc : R.string.mute_object_failed;

                post(new Runnable() {
                    @Override
                    public void run() {
                        if (result) {
                            if (muteType == 1) {
                                QiupuHelper.updateStreamRemovedUI(post.fromUser);
                            } else {
                                QiupuHelper.updateStreamRemovedUI(objectId);
                            }
                        }
//                        if (dlg.isShowing()) {
//                            dlg.dismiss();
//                        }
                        Toast.makeText(getContext(), msgId, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                Log.d(TAG, "reportAbuse exception:" + ex.getMessage());
                final String message = ex.getMessage();
                post(new Runnable() {
                    @Override
                    public void run() {
//                        if (dlg.isShowing()) {
//                            dlg.dismiss();
//                        }
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void reportAbuse() {
        final String objectId = post.post_id;
        final Dialog dlg = DialogUtils.showProgressDialog(getContext(),
                R.string.menu_report_abuse,
                getContext().getString(R.string.status_update_summary));
        AsyncApiUtils.asyncInstance.reportAbusedObject(AccountServiceUtils.getSessionID(),
                objectId, new TwitterAdapter() {
            @Override
            public void reportAbuse(final boolean result) {
                Log.d(TAG, "reportAbuse, get result: " + result);
                final int msgId = result ? R.string.report_abuse_suc : R.string.report_abuse_failed;

                post(new Runnable() {
                    @Override
                    public void run() {
                        if (result) {
                            QiupuHelper.updateStreamRemovedUI(objectId);
                        }
                        if (dlg.isShowing()) {
                            dlg.dismiss();
                        }
                        Toast.makeText(getContext(), msgId, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                Log.d(TAG, "reportAbuse exception:" + ex.getMessage());
                final String message = ex.getMessage();
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (dlg.isShowing()) {
                            dlg.dismiss();
                        }
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private boolean inremoveStream = false;
    private final Object mremoveLock = new Object();
    protected void removeStream() {
        synchronized (mremoveLock) {
            if (inremoveStream == true) {
                Log.d(TAG, "in doing remove stream");
                return;
            }
        }

        synchronized (mremoveLock) {
            inremoveStream = true;
        }

        final String objectId = post.post_id;
        final Dialog dlg = DialogUtils.showProgressDialog(getContext(),
                R.string.stream_remove,
                getContext().getString(R.string.status_update_summary));

        AsyncApiUtils.asyncInstance.deletePost(AccountServiceUtils.getSessionID(), objectId,
                new TwitterAdapter() {
            public void deletePost(final boolean result) {
                Log.d(TAG, "finish removeStream" + result);

                synchronized (mremoveLock) {
                    inremoveStream = false;
                }

//                final int msgId = result ? R.string.report_abuse_suc : R.string.report_abuse_failed;

                post(new Runnable() {
                    @Override
                    public void run() {
                        if (result) {
                            QiupuHelper.updateStreamRemovedUI(objectId);
                        }
                        if (dlg.isShowing()) {
                            dlg.dismiss();
                        }
//                        Toast.makeText(getContext(), msgId, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mremoveLock) {
                    inremoveStream = false;
                }

                final String message = ex.getMessage();
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (dlg.isShowing()) {
                            dlg.dismiss();
                        }
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    boolean mIsSetTopList = false;
    Object mLockTopList = new Object();

    private void setTopList(final boolean setTop) {
        Log.d(TAG, "setTopList() setTop = " + setTop);
        final long circleId = getTopStreamTargetId();
        if (circleId <= 0) {
            Log.d(TAG, "setTopList() skip without invalid group: " + circleId);
            return;
        }

        if (mIsSetTopList) {
            Toast.makeText(getContext(), R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLockTopList) {
            mIsSetTopList = true;
        }

        final String stream_id = post.post_id;

//        showDialog(DIALOG_SET_CIRCLE_PROCESS);

        final Dialog dlg = DialogUtils.showProgressDialog(getContext(),
                -1,
                getContext().getString(R.string.set_friends_circle));
//        dlg.setCanceledOnTouchOutside(false);
//        dlg.setCancelable(true);

        final String group_id = String.valueOf(circleId);
        AsyncApiUtils.asyncInstance.setTopList(AccountServiceUtils.getSessionID(),
                group_id, stream_id, setTop, new TwitterAdapter() {
            public void setTopList(ArrayList<String> topIdList) {
                Log.d(TAG, "finish setTopList() topIdList = " + topIdList);
                synchronized (mLockTopList) {
                    mIsSetTopList = false;
                }

//                Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_SET_TOP_END);
//                msg.getData().putBoolean(RESULT, true);
//                msg.getData().putString("stream_id", stream_id);
//                msg.getData().putString("group_id", group_id);
//                msg.getData().putBoolean("set_top", setTop);
//                msg.sendToTarget();

                post(new Runnable() {
                    @Override
                    public void run() {
                        if (setTop == true) {
                            QiupuHelper.onTargetTopCreate(group_id, stream_id);
                        } else {
                            QiupuHelper.onTargetTopCancel(group_id, stream_id);
                        }

                        if (dlg.isShowing()) {
                            dlg.dismiss();
                        }
                    }
                });
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockTopList) {
                    mIsSetTopList = false;
                }
//                Message msg = mBasicHandler.obtainMessage(QiupuMessage.MESSAGE_SET_TOP_END);
//                msg.getData().putBoolean(RESULT, false);
//                msg.sendToTarget();

                final String message = ex.getMessage();
                post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        if (dlg.isShowing()) {
                            dlg.dismiss();
                        }
                    }
                });
            }
        });
    }

    private final int RANDOM_MIN_RANGE = 1;
    private final int RANDOM_MAX_RANGE = 4;

    private final int DRAWABLE_GREEN      = 1;
    private final int DRAWABLE_BLUE       = 2;
    private final int DRAWABLE_RED        = 3;
    private final int DRAWABLE_YELLOW     = 4;

    /**
     * random Integer value from RANDOM_MIN_RANGE to RANDOM_MAX_RANGE,
     * represent four random background image icon.
     * @return random background image id
     */
    protected int getSubImageRes() {
        int subImageRes = 0;
        int index = RANDOM_MIN_RANGE + (int)(Math.random()*RANDOM_MAX_RANGE);
        switch (index) {
            case DRAWABLE_GREEN:
                subImageRes = R.drawable.wutong_screen_bg_green;
                break;
            case DRAWABLE_BLUE:
                subImageRes = R.drawable.wutong_screen_bg_blue;
                break;
            case DRAWABLE_RED:
                subImageRes = R.drawable.wutong_screen_bg_red;
                break;
            case DRAWABLE_YELLOW:
                subImageRes = R.drawable.wutong_screen_bg_yellow;
                break;
            default:
                break;
        }
        return subImageRes;
    }

    /**
     * random Integer value from 6 to 9
     * @return 6 or 7 or 8 or 9
     */
    protected int getRandomDefaultIconIndex() {
        return 6 + (int)(Math.random()*RANDOM_MAX_RANGE);
    }

    protected static class ObjectCountSwitcher {
        private TextView mLabel;
        private int mSize;
        private int mSelection;

        private Callback mSwitcherCallback;
        private boolean mIsPlaying = false;
        private long mInterval = 3 * QiupuConfig.A_SECOND;

        protected interface Callback {
            public int play(int selection);
        }

        public void setPlayListener(Callback callback) {
            mSwitcherCallback = callback;
        }

        public boolean getPlayStatus() {
            return mIsPlaying;
        }

        public TextView getPlayButton() {
            return mLabel;
        }

        private OnClickListener switchListener = new StreamRowItemClicker() {
            @Override
            protected void onRowItemClick(View view) {
                if (mIsPlaying) {
                    mIsPlaying = false;
                } else {
                    mIsPlaying = true;
                    setupPlayingRunnable(0);
                }

                onPlayStateReady();
            }
        };

        private void onPlayStateReady() {
            mLabel.setBackgroundResource(mIsPlaying ? R.drawable.slide_pause : R.drawable.slide_play);
//            mLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,
//                    mIsPlaying ? R.drawable.video_overlay_pause : R.drawable.video_overlay);
        }

        private void setupPlayingRunnable(long interval) {
            mLabel.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mIsPlaying) {
                        if (mSelection >= mSize - 1) {
                            mSelection = 0;
                        } else {
                            mSelection++;
                        }

                        int index = mSwitcherCallback.play(mSelection);
                        mLabel.setText(String.format("%1$s/%2$s", index + 1, mSize));

                        setupPlayingRunnable(mInterval);
                    } else {
                        // do nothing while it was stopped.
                    }
                }
            }, interval);
        }

        private ObjectCountSwitcher() {
            // not instance.
        }

        public static ObjectCountSwitcher instanceSwitcher(TextView label, int count, int selection) {
            ObjectCountSwitcher instance = new ObjectCountSwitcher();
            instance.attach(label, count, selection);
            return instance;
        }

        public void attach (TextView label, int count, int selection) {
            mIsPlaying = false;
            mLabel = label;
            mSize = count;
            mSelection = selection;
            if (null != mLabel) {
                if (count > 1) {
                    mLabel.setVisibility(VISIBLE);
                    mLabel.setOnClickListener(switchListener);
                    mLabel.setText(String.format("%1$s/%2$s", selection + 1, count));
                    onPlayStateReady();
                } else {
                    mLabel.setVisibility(GONE);
                }
            }
        }
    }
}
