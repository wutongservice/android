package com.borqs.common.view;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.borqs.common.util.FileUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.util.StringUtil;
import twitter4j.ApkBasicInfo;
import twitter4j.Stream;

public class StreamApplicationRowView extends AbstractStreamRowView {
    private static final String TAG = "StreamApplicationRowView";

    private TextView author_name;

    public StreamApplicationRowView(Context ctx, AttributeSet at) {
        super(ctx);

        orm = QiupuORM.getInstance(ctx);

        init();
    }


    public StreamApplicationRowView(Context ctx, Stream stream) {
        super(ctx);
        post = stream;

        orm = QiupuORM.getInstance(ctx);

        init();
    }


    public StreamApplicationRowView(Context ctx, Stream stream, boolean forcomments) {
        super(ctx);

        post = stream;

        this.forcomments = forcomments;
        orm = QiupuORM.getInstance(ctx);

        init();
    }

    StreamApplicationRowView(Context ctx, Stream stream, boolean isComments, boolean isReshare) {
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


    @Override
    public String getText() {

        return null;
    }

    private void init() {
        removeAllViews();

        final int layoutResId = R.layout.stream_app_row_view;
        View convertView = LayoutInflater.from(mContext).inflate(layoutResId, null);
        convertView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        addView(convertView);

        setOnClickListener(itemClick);
        
        setupCommonUi();
        setupPrivacyUi();
        setUI();
    }

    // TODO: this method will be reform later, which need to update stream row if
    // possible, rather than current involves too heavy task.
    public void setContent(Stream content) {
        if (post.equals(content)) {
            Log.d(TAG, "setContent, the same stream object, only update its comments.");
            setupStreamLikeUi();
            requestLayout();
        } else {
            post = content;
            // during the reforming period, it need to re-init as both old and new style layout are co-existing.
            init();
        }
    }

//    private void resetLinkForProfile(String username, String rawname) {
//        //if(SNSService.DEBUG)
//        if (null != author_name) {
//            username = text2html(username);
//            author_name.setText(Html.fromHtml(username));
//            final CharSequence charSequence = author_name.getText();
//            if (charSequence instanceof SpannableString) {
//                SpannableString sb = (SpannableString) charSequence;
//                SpannableString ss = new SpannableString(rawname);
//                URLSpan[] spans = author_name.getUrls();
//                for (URLSpan span1 : spans) {
//                    int start = sb.getSpanStart(span1);
//                    int end = sb.getSpanEnd(span1);
//                    String text = sb.subSequence(start, end).toString();
//
//                    int startpp = rawname.indexOf(text);
//                    if (start < startpp) {
//                        int span = (startpp - start);
//                        start += (span);
//                        end += (span);
//                    }
//
//                    MyURLSPan my = new MyURLSPan(span1.getURL());
//                    ss.setSpan(my, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    my = null;
//                }
//
//                author_name.setText(ss);
//                ss = null;
//                sb = null;
//            }
//        }
//    }

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

//    private void setupPostedContent() {
//        final String message = getPostedContent();
//        if (!isEmpty(message) && !isEmpty(message.trim())) {
//            if (null == mExpandableWidget) {
//                mExpandableWidget = ((ViewStub) findViewById(R.id.expandable_span)).inflate();
//                initExpandablePostMessageView(message.trim(), true);
//            }
//        } else {
//            initExpandablePostMessageView("", false);
//        }
//    }

    protected void setUI() {
        if (post.isRetweet()) {
            setupAppShareAttachment(true);
        } else if (isValidAppAttachment(post)) {
            setupAppShareAttachment(false);
        } else {
            if (QiupuConfig.DBLOGD) Log.d(TAG, "setUI, without attachment stream type: " + post.type);
        }
        setupPosterImageRunner(this, post);
        setupPosterSummary(this, post);

        refreshPostedMessageTextContent(this, post);

        setupTimeSpanUi();
        setupLocationUi();

        setupStreamIconRunner();

        setupStreamLikeUi();

        requestLayout();
    }

    private void showStreamMessageDescription(String description) {
        TextView stream_message_des = (TextView) findViewById(R.id.stream_message_des);
        if (null != stream_message_des) {
            final String descTrim = StringUtil.getTrimText(description, LEN_MAX_EXTRA_TEXT);
            if (isEmpty(descTrim)) {
                stream_message_des.setVisibility(View.GONE);
            } else {
                stream_message_des.setVisibility(View.VISIBLE);
                stream_message_des.setText(descTrim);
            }
        }
    }


    private void setupStreamPhotoIndicatorRunner(int defaultIconId, int defaultImageIndex,
                                                 String photoUrl,
                                                 OnClickListener itemDetailClick) {
        setupStreamPhotoIndicatorRunner(defaultIconId, defaultImageIndex, photoUrl, 48, itemDetailClick);
    }

    private void setupStreamPhotoIndicatorRunner(int defaultIconId, int defaultImageIndex,
                                                 String photoUrl, int dimension,
                                                 OnClickListener itemDetailClick) {
        TextView stream_photo_indicator = (TextView) findViewById(R.id.post_content);
        stream_photo_indicator.setOnClickListener(itemDetailClick);

        stream_photo_indicator.setCompoundDrawables(getContext().getResources().getDrawable(defaultIconId), null, null, null);
        shootImageRunner(photoUrl, defaultImageIndex, dimension, stream_photo_indicator, false);
    }

    private void shootImageRunner(String photoUrl, int defaultImageIndex, int dimension,
                                  View stream_photo_indicator, boolean noImage) {
        ImageRun photo_1 = new ImageRun(null, photoUrl, 0);
        photo_1.default_image_index = defaultImageIndex;
        photo_1.addHostAndPath = true;
        photo_1.need_scale = true;
        photo_1.width = dimension;
        photo_1.noimage = noImage;
        photo_1.setImageView(stream_photo_indicator);
        photo_1.post(null);
    }

//    private void setupStreamLikeUi() {
//        TextView stream_like_string = (TextView) findViewById(R.id.stream_like_string);
//
//        if (null != stream_like_string) {
//            final int likerCount = null == post || null == post.likes ? 0 : post.likes.count;
//
//            // Already try parse it from server response, see StreamJSONImpl.StreamJSONImpl().
//            StringBuilder likerBuilder = new StringBuilder();
//            boolean isMoreLiker = false;
//            if (likerCount > 0) {
//                likerBuilder.append("(").append(likerCount).append(") ");
//                final int size = null == post.likes.friends ? 0 : post.likes.friends.size();
//                for (int i = 0; i < size; i++) {
//                    long tmpid = post.likes.friends.get(i).uid;
//                    if (AccountServiceUtils.getBorqsAccountID() == tmpid) {
//                        post.iLike = true;
//                    } else {
//                        String tmpname = orm.getUserName(tmpid);
//                        if (!isEmpty(tmpname)) {
//                            if (isMoreLiker) {
//                                likerBuilder.append(SEPARATOR_COMMA);
//                            } else {
//                                isMoreLiker = true;
//                            }
//                            likerBuilder.append(tmpname);
//                        }
//                    }
//                }
//            }
//
//            final String showLikeTxt;
//            if (post.iLike) {
//                showLikeTxt = isMoreLiker ? likerBuilder + ", " + ilike : ilike;
//            } else {
//                showLikeTxt = isMoreLiker ? likerBuilder + likeStr : null;
//            }
//
//            if (TextUtils.isEmpty(showLikeTxt)) {
//                stream_like_string.setVisibility(View.GONE);
//            } else {
//                stream_like_string.setVisibility(View.VISIBLE);
//                stream_like_string.setText(showLikeTxt);
//            }
//        }
//    }

    private void setupStreamIconRunner() {
        ImageView stream_icon = (ImageView) findViewById(R.id.stream_icon);
        if (null != stream_icon) {
            if (null == post.attachment || isEmpty(post.icon)) {
                stream_icon.setVisibility(View.GONE);
            } else {
                stream_icon.setVisibility(View.VISIBLE);
                ImageRun iconrun = new ImageRun(null, post.icon, 0);
                iconrun.noimage = true;
                iconrun.setImageView(stream_icon);
                iconrun.post(null);
            }
        }
    }

//    private void setupPosterSummary() {
//        if (author_name != null && post.fromUser != null) {
//            author_name.setText(post.fromUser.nick_name);
//
//            StringBuilder formatBuilder = new StringBuilder();
//            formatBuilder.append(String.format(POSTER_NAME_FORMAT_SINGLE,
//                    post.fromUser.nick_name, post.fromUser.uid, profileURL));
//            StringBuilder rawNameBuilder = new StringBuilder();
//            rawNameBuilder.append(post.fromUser.nick_name);
//
//            if (!isForMyself()) {
//                final int toUserSize = post.toUsers.size();
//                if (toUserSize > 0) {
//                    rawNameBuilder.append(TO_SIGN_STRING).append(post.toUsers.get(0).nick_name);
//                    formatBuilder.append(String.format(POSTER_NAME_FORMAT_SUFFIX,
//                            replaceToUserNickName(post.toUsers.get(0).nick_name),
//                            post.toUsers.get(0).uid, profileURL, TO_SIGN_STRING));
//                    if (forcomments) {
//                        String userName;
//                        for (int i = 1; i < toUserSize; i++) {
//                            userName = post.toUsers.get(i).nick_name;
//                            formatBuilder.append(String.format(POSTER_NAME_FORMAT_SUFFIX,
//                                    replaceToUserNickName(userName),
//                                    post.toUsers.get(i).uid, profileURL, SEPARATOR_COMMA));
//                            rawNameBuilder.append(SEPARATOR_COMMA).append(userName);
//                        }
//                    } else {
//                        final int moreCount = toUserSize - 1;
//                        if (moreCount > 0) {
//                            final String moreUser = String.format(getContext().getString(R.string.other_more_people), moreCount);
//                            formatBuilder.append(String.format(POSTER_NAME_FORMAT_SUFFIX,
//                                    replaceToUserNickName(moreUser),
//                                    -9999, profileURL, SEPARATOR_COMMA));
//                            rawNameBuilder.append(SEPARATOR_COMMA).append(moreUser);
//                        }
//                    }
//                }
//            }
//
//            stripHtmlUnderlines(author_name, formatBuilder.toString());
//        }
//    }

    private void setupAppShareAttachment(boolean isforRetweet) {
        String description = null;
        if (post.attachment.attachments.size() > 0) {
            final ApkBasicInfo response = (isforRetweet == true) ?
                    (ApkBasicInfo) post.retweet.attachment.attachments.get(0) :
                    (ApkBasicInfo) post.attachment.attachments.get(0);
            if (response != null) {

                final OnClickListener itemDetailClick = new StreamRowItemClicker() {
                    public void onRowItemClick(View view) {
                        gotoApkDetail(response);
                    }
                };

                setupStreamPhotoIndicatorRunner(R.drawable.default_app_icon,
                        QiupuConfig.DEFAULT_IMAGE_INDEX_APK, response.iconurl, itemDetailClick);
                StringBuffer appSummary = new StringBuffer();
                appSummary.append(response.label).append("<br>");
                appSummary.append("NO.").append(response.versioncode);
                appSummary.append("  v").append(response.versionname);
                appSummary.append("<br>");
                appSummary.append(FileUtils.formatPackageFileSizeString(getContext(), response.apksize));
                
                String htmlStr = String.format("<a href='borqs://app/details?noneedtakeaction=true'>%1$s</a>", appSummary.toString());
                setupPostContentUi(htmlStr, itemDetailClick);

                description = response.description; // record description, which was set later.
            } else {
                Log.w(TAG, "parseApkPostsAttachmentsUi, fail to get apk info from: "
                        + post.attachment.attachments);
            }
        }
        showStreamMessageDescription(description);
    }

    public Stream getStream() {
        return post;
    }
}