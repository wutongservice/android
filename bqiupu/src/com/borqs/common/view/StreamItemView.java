package com.borqs.common.view;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Parcel;
import android.text.*;
import android.text.Html.TagHandler;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.util.HtmlUtils;
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
import org.xml.sax.XMLReader;
import twitter4j.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StreamItemView extends AbstractStreamRowView {
    private static final String TAG = "StreamItemView";

    private static String reshareString;

    private TextView author_name;
    private TextView post_message;
    private TextView reshare_source;

    private TextView postContentTV;
    private TextView postTimeTV;
    private TextView post_location;
    private ImageView stream_icon;
    private ImageView posterIconIV;
    private TextView qiupu_stream_comments;
    private TextView qiupu_stream_like;
    private View share_content_span;

    private View stream_like_span;
    private TextView stream_like_string;

    private View stream_photo_span, share_comment_ll;
    private ImageView stream_photo_indicator;
    private ImageView stream_photo_2;
    private ImageView stream_photo_3;
    private ImageView img_pic_ui;

    private CommentsSimpleView comment_1;
    private CommentsSimpleView comment_2;
    private ImageView stream_comment_divider;

    private TextView stream_message_des;
    private TextView qiupu_download_from_market;

    private View share_link_ll;
    private TextView link_title, link_host, link_des;
    private ImageView link_photo;

    private LinearLayout stream_container;

    private QiupuORM orm;

    public StreamItemView(Context ctx, AttributeSet at) {
        super(ctx);

        orm = QiupuORM.getInstance(ctx);

        init();
    }


    public StreamItemView(Context ctx, Stream stream) {
        super(ctx);
        post = stream;

        orm = QiupuORM.getInstance(ctx);

        init();
    }

    public StreamItemView(Context ctx, Stream stream, boolean forcomments) {
        super(ctx);

        post = stream;

        this.forcomments = forcomments;
        orm = QiupuORM.getInstance(ctx);

        init();
    }

    public StreamItemView(Context ctx, Stream stream, boolean forcomments, boolean forReshare) {
        super(ctx);
        post = stream;

        this.forcomments = forcomments;
        orm = QiupuORM.getInstance(ctx);

        isForReshare = forReshare;

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

        reshareString = getContext().getString(R.string.news_feed_reshare_hint);

        int resid = R.layout.post_item_view;
        if (isForReshare == true) {
            resid = R.layout.post_item_reshare_view;
        }
        View convertView = LayoutInflater.from(mContext).inflate(resid, null);

        convertView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        addView(convertView);

        posterIconIV = (ImageView) convertView.findViewById(R.id.user_icon);
        author_name = (TextView) convertView.findViewById(R.id.poster_name);
        reshare_source = (TextView) convertView.findViewById(R.id.reshare_source);
        post_message = (TextView) convertView.findViewById(R.id.post_message);


        postTimeTV = (TextView) convertView.findViewById(R.id.post_time);
        post_location = (TextView) convertView.findViewById(R.id.post_location);
        stream_icon = (ImageView) convertView.findViewById(R.id.stream_icon);

        share_content_span = convertView.findViewById(R.id.share_content_ll);
        postContentTV = (TextView) convertView.findViewById(R.id.post_content);
        stream_message_des = (TextView) convertView.findViewById(R.id.stream_message_des);
        stream_photo_indicator = (ImageView) convertView.findViewById(R.id.stream_photo_indicator);

        stream_like_string = (TextView) convertView.findViewById(R.id.stream_like_string);

        stream_like_span = convertView.findViewById(R.id.stream_like_span);
        qiupu_stream_comments = (TextView) convertView.findViewById(R.id.qiupu_stream_comments);
        qiupu_stream_like = (TextView) convertView.findViewById(R.id.qiupu_stream_like);
        TextView qiupu_stream_reshare = (TextView) convertView.findViewById(R.id.qiupu_stream_reshare);

        comment_1 = (CommentsSimpleView) convertView.findViewById(R.id.qiupu_stream_comment_1);
        comment_2 = (CommentsSimpleView) convertView.findViewById(R.id.qiupu_stream_comment_2);
        stream_comment_divider = (ImageView) convertView.findViewById(R.id.stream_comment_divider);


        stream_photo_span = convertView.findViewById(R.id.stream_photo_span);
        stream_photo_2 = (ImageView) convertView.findViewById(R.id.stream_photo_1);
        stream_photo_3 = (ImageView) convertView.findViewById(R.id.stream_photo_2);

        share_comment_ll = convertView.findViewById(R.id.share_comment_ll);
        stream_container = (LinearLayout) findViewById(R.id.stream_container);

        img_pic_ui = (ImageView) convertView.findViewById(R.id.img_pic_ui);
        qiupu_download_from_market = (TextView) convertView.findViewById(R.id.qiupu_download_from_market);


        share_link_ll = convertView.findViewById(R.id.share_link_ll);
        link_title = (TextView) convertView.findViewById(R.id.post_link_title);
        link_host = (TextView) convertView.findViewById(R.id.post_link_host);
        link_photo = (ImageView) convertView.findViewById(R.id.stream_photo_link);
        link_des = (TextView) convertView.findViewById(R.id.stream_message_link_des);

        posterIconIV.setOnClickListener(userClick);
        qiupu_stream_reshare.setOnClickListener(reshareClick);
        qiupu_download_from_market.setOnClickListener(downloadFromMarketClick);
        qiupu_stream_comments.setOnClickListener(commentsClick);
        comment_1.setOnClickListener(commentsClick);
        comment_2.setOnClickListener(commentsClick);

        author_name.setMovementMethod(LinkMovementMethod.getInstance());
        author_name.setLinksClickable(true);

        post_location.setMovementMethod(LinkMovementMethod.getInstance());
        post_location.setLinksClickable(true);

        post_message.setMovementMethod(LinkMovementMethod.getInstance());
        post_message.setLinksClickable(true);

        setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                gotoStreamItemComment();
            }
        });

        setUI();
    }

    public void setContent(Stream content) {
        post = content;
        setUI();
    }

    OnClickListener downloadFromMarketClick = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (post.type == BpcApiUtils.APK_LINK_POST) {
                Uri link = Uri.parse(post.attachment.link.href);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + link.getQueryParameter("q")));
                try {
                    getContext().startActivity(intent);
                } catch (Exception ne) {
                }
            } else if (post.type == BpcApiUtils.APK_POST || post.type == BpcApiUtils.APK_COMMENT_POST || post.type == BpcApiUtils.APK_LIKE_POST) {
                ApkBasicInfo apkInfo = (ApkBasicInfo) post.attachment.attachments.get(0);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + apkInfo.packagename));
                try {
                    getContext().startActivity(intent);
                } catch (Exception ne) {
                }
            } else {
                Log.e(TAG, "downloadFromMarketClick, un-supported post type:" + post.type);
            }
        }
    };

    OnClickListener userClick = new OnClickListener() {
        public void onClick(View arg0) {
            IntentUtil.startUserDetailIntent(getContext(), post.fromUser.uid, post.fromUser.nick_name);
        }
    };

    private class MessageURLSPan extends URLSpan {

        String url;

        public MessageURLSPan(Parcel src) {
            super(src);
        }

        public MessageURLSPan(String src) {
            super(src);
            url = src;
        }

        @Override
        public String getURL() {
            return super.getURL();
        }


        @Override
        public void updateDrawState(TextPaint ds) {
            //super.updateDrawState(ds);
            ds.setColor(ds.linkColor);
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View widget) {
            SpannableString sb = (SpannableString) post_message.getText();

            int start = sb.getSpanStart(this);
            int end = sb.getSpanEnd(this);
            String text = sb.subSequence(start, end).toString();

            if (QiupuConfig.LOGD)
                Log.d("MyURLSPan", "click= text=" + text + " url=" + getURL());
            Uri uri = Uri.parse(getURL());
            processUserNameClick(uri, text);
        }
    }

    private void processUserNameClick(Uri uri, String text) {
        String owner = uri.getQueryParameter("uid");
        if (owner != null) {
            try {
                QiupuUser user = orm.queryOneUserInfo(Long.parseLong(owner));
                if (user != null) {
                    IntentUtil.startUserDetailIntent(getContext(), user.uid, user.nick_name,
                            user.circleName);

                    user.despose();
                    user = null;
                } else {
                    IntentUtil.startUserDetailIntent(getContext(), Long.valueOf(owner), text);
                }
            } catch (Exception ne) {
            }
        } else {
            //open in Browser
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }
    }

    private void resetLinkForProfile(String username, String rawname, String message) {
        //for make friends
        if (post.type == BpcApiUtils.MAKE_FRIENDS_POST) {
            if (post.attachment != null && post.attachment.attachments.size() > 0) {
                boolean isFirst = true;
                StringBuilder raw = new StringBuilder();
                StringBuilder htmlraw = new StringBuilder();
                final String seperator = ", ";
                for (Object obj : post.attachment.attachments) {
                    QiupuSimpleUser suser = (QiupuSimpleUser) obj;

                    raw.append((!isFirst ? seperator : ""));
                    raw.append(suser.nick_name);

                    htmlraw.append(String.format("%4$s<a href='%3$s%2$s'>%1$s</a>", suser.nick_name, suser.uid, profileURL, (!isFirst ? seperator : "")));


                    if (isFirst) {
                        isFirst = false;
                    }
                }

                rawname += String.format(getContext().getString(R.string.stream_make_friends), raw);
                username += String.format(getContext().getString(R.string.stream_make_friends), htmlraw);
            }
        }

        //if(SNSService.DEBUG)
        {
            username = HtmlUtils.text2html(username);
            author_name.setText(MyHtml.fromHtml(username));
            /*
            SpannableString sb = (SpannableString) author_name.getText();
            SpannableString ss = new SpannableString(rawname);
            URLSpan[] spans = author_name.getUrls();
            for (URLSpan span1 : spans) {
                int start = sb.getSpanStart(span1);
                int end = sb.getSpanEnd(span1);
                String text = sb.subSequence(start, end).toString();

                int startpp = rawname.indexOf(text);
                if (start < startpp) {
                    int span = (startpp - start);
                    start += (span);
                    end += (span);
                }
                //if(SNSService.DEBUG)
                //Log.d(TAG, "text="+text + " url="+spans[i].getURL());

                MyHtml.BorqsUrlSpan my = new MyHtml.BorqsUrlSpan(span1.getURL());
                ss.setSpan(my, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                //Log.i(TAG,"----- ss:"+ss);
                my = null;
            }

            //if(SNSService.DEBUG)
            //Log.d(TAG, "username  SpannableString = "+ss);
            author_name.setText(ss);
            ss = null;
            sb = null;
            */
        }

        final String trimMessage = message.trim();
        if (!isEmpty(trimMessage)) {
            post_message.setVisibility(View.VISIBLE);
            stripHtmlUnderlines(post_message, message);

//          SpannableString sbs = (SpannableString)post_message.getText();
//          MessageURLSPan mys = new MessageURLSPan(spanss[i].getURL());
//          sss.setSpan(mys, starts, ends, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        } else {
            post_message.setVisibility(View.GONE);
        }
    }

    TagHandler tagHandler = new TagHandler() {
        public void handleTag(boolean open, String tag, Editable output, XMLReader xmlreader) {
            Log.d(TAG, "open=" + open + " tag=" + tag);
        }
    };

    protected void setUI() {
        if (post == null) {
            return;
        }

        if (post.isRetweet()) {
            //just show message and the re-tweet
            stream_container.setVisibility(View.VISIBLE);
            StreamItemView st;
            if (isForReshare && null != reshare_source) {
                reshare_source.setText(reshareString);
            }
            if (stream_container.getChildCount() > 0) {
                st = (StreamItemView) stream_container.getChildAt(0);
                st.setContent(post.retweet);
            } else {
                st = new StreamItemView(mContext, post.retweet, true, true);
                st.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                stream_container.addView(st);
            }
        } else {
            stream_container.setVisibility(View.GONE);
            //stream_container.removeAllViews();
        }

        if (forcomments) {
            share_comment_ll.setVisibility(View.GONE);
            qiupu_download_from_market.setVisibility(View.GONE);
        }

        posterIconIV.setImageResource(R.drawable.default_user_icon);
        String fromUserIcon = Stream.getFromUserPhotoUrl(post);
        if (!post.fromUser.reset_image_url) {
            final String dbIconPath = orm.getUserImageUrl(post.fromUser.uid);
            if (!isEmpty(dbIconPath)  && !dbIconPath.equals(fromUserIcon)) {
                post.fromUser.profile_image_url = fromUserIcon;
                post.fromUser.reset_image_url = true;
                fromUserIcon = dbIconPath;
            }
        }

        if (!isEmpty(fromUserIcon)) {
            ImageRun imagerun = new ImageRun(null, fromUserIcon, 0);
            imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
            imagerun.noimage = false;
            imagerun.addHostAndPath = true;
            imagerun.setRoundAngle = true;
            imagerun.setImageView(posterIconIV);
            imagerun.post(null);
        }

        String apkLink = null;

        if (post.fromUser != null) {
            author_name.setText(post.fromUser.nick_name);

            if (isForMyself()) {
                StringBuilder nameformat = new StringBuilder();
                nameformat.append(String.format("<a href='%3$s%2$s'>%1$s</a>", post.fromUser.nick_name, post.fromUser.uid, profileURL));
                if (!isEmpty(post.message)) {
                    if (!isEmpty(post.parent_id))
                    {
                        resetLinkForProfile(nameformat.toString(), post.fromUser.nick_name, reshareString + "<br>" + post.message);
                    } else {
                        resetLinkForProfile(nameformat.toString(), post.fromUser.nick_name, post.message);
                    }
                } else {
                    if (!isEmpty(post.parent_id))
                    {
                        resetLinkForProfile(nameformat.toString(), post.fromUser.nick_name, reshareString);
                    } else {
                        resetLinkForProfile(nameformat.toString(), post.fromUser.nick_name, "");
                    }
                }

            } else {
                StringBuilder nameformat = new StringBuilder();
                nameformat.append(String.format("<a href='%3$s%2$s'>%1$s</a>", post.fromUser.nick_name, post.fromUser.uid, profileURL));

                StringBuilder nameraw = new StringBuilder();
                nameraw.append(post.fromUser.nick_name);

                for (int i = 0; i < post.toUsers.size(); i++) {
                    if (i == 0) {
                        nameformat.append(String.format(" > <a href='%3$s%2$s'>%1$s</a>", replaceToUserNickName(post.toUsers.get(i).nick_name), post.toUsers.get(i).uid, profileURL));
                        nameraw.append(" > ").append(post.toUsers.get(i).nick_name);
                    } else {
                        nameformat.append(String.format(", <a href='%3$s%2$s'>%1$s</a>", replaceToUserNickName(post.toUsers.get(i).nick_name), post.toUsers.get(i).uid, profileURL));
                        nameraw.append(", ").append(post.toUsers.get(i).nick_name);
                    }
                }

                String message = "";
                if (!isEmpty(post.message)) {
                    apkLink = getApkNameMessage(post.message);
                    message = post.message;
                } else {
                    if (!isEmpty(post.parent_id)) {
                        message = reshareString;
                    }
                }

                resetLinkForProfile(nameformat.toString(), nameraw.toString(), message);
            }
        }

        // share apk
        stream_message_des.setVisibility(View.GONE);
        img_pic_ui.setVisibility(View.GONE);
        findViewById(R.id.post_prompt).setVisibility(View.GONE);

        if (post.type != BpcApiUtils.LINK_POST) {
            share_link_ll.setVisibility(View.GONE);
        }

        if (post.type == BpcApiUtils.APK_POST || post.type == BpcApiUtils.APK_COMMENT_POST
                || post.type == BpcApiUtils.APK_LIKE_POST) {
            parseApkPostsAttachmentsUi();
        } else if (post.type == BpcApiUtils.APK_LINK_POST) {
            parseApkLinkPostsAttachmentsUi(apkLink);
        } else if (post.type == BpcApiUtils.LINK_POST) {
            //no need set
            stream_photo_2.setImageDrawable(null);
            stream_photo_3.setImageDrawable(null);
            stream_photo_span.setVisibility(View.GONE);

            qiupu_download_from_market.setVisibility(View.GONE);

            share_content_span.setVisibility(View.GONE);
            parseLinkPostsAttachmentsUi();
        } else if (post.type == BpcApiUtils.BOOK_POST || post.type == BpcApiUtils.BOOK_COMMENT_POST
                || post.type == BpcApiUtils.BOOK_LIKE_POST) {
            stream_photo_2.setImageDrawable(null);
            stream_photo_3.setImageDrawable(null);
            qiupu_download_from_market.setVisibility(View.GONE);
            parseBookPostsAttachmentsUi();
        } else if (post.type == BpcApiUtils.MUSIC_POST || post.type == BpcApiUtils.MUSIC_COMMENT_POST
                || post.type == BpcApiUtils.MUSIC_LIKE_POST) {
            stream_photo_2.setImageDrawable(null);
            stream_photo_3.setImageDrawable(null);
            qiupu_download_from_market.setVisibility(View.GONE);
            parseMusicPostsAttachmentsUi();
        } else {
            share_content_span.setVisibility(View.GONE);
            stream_photo_span.setVisibility(View.GONE);
            stream_photo_indicator.setVisibility(View.GONE);
            qiupu_download_from_market.setVisibility(View.GONE);
        }

        String day = com.borqs.qiupu.util.DateUtil.converToRelativeTime(mContext, post.created_time);
        if (!isEmpty(post.device)) {
            //"device" : "os=android-7-armeabi-v7a;client=com.borqs.qiupu-115-arm;lang=CN;model=OMAP_SS"
            try {

                String[] devices = post.device.split(";");
                if (devices.length >= 4) {
                    day += String.format(from_device, devices[1].substring(devices[1].indexOf("=") + 1), devices[3].substring(devices[3].indexOf("=") + 1));
                }
            } catch (Exception ne) {
                ne.printStackTrace();
            }
        }
        postTimeTV.setText(day);

        setupLocationUi();

        if (null != post.attachment && !isEmpty(post.icon)) {
            stream_icon.setVisibility(View.VISIBLE);
            ImageRun iconrun = new ImageRun(null, post.icon, 0);
            iconrun.noimage = true;
            iconrun.setImageView(stream_icon);
            iconrun.post(null);
        } else {
            stream_icon.setVisibility(View.GONE);
        }

        String comments = String.format("%2$s %1$s", commentStr, post.comments.getCount());
        if (post.comments.getCount() > 0) {
            qiupu_stream_comments.setText(comments);
        } else {
            qiupu_stream_comments.setText(commentStr);
        }

        String likestr = likeStr;
        if (post.iLike) {
            likestr = unlikeStr;
        }

        // TODO: if we could parse iLike from server response directly, these code is useless.
        // Already try parse it from server response, see StreamJSONImpl.StreamJSONImpl().
        StringBuilder friends = new StringBuilder();
        if (/*post.iLike == false &&*/ post.likes.count > 0) {
            final int size = post.likes.friends.size();
            for (int i = 0; i < size; i++) {
                long tmpid = post.likes.friends.get(i).uid;
                if (AccountServiceUtils.getBorqsAccountID() == tmpid) {
                    likestr = unlikeStr;
                    post.iLike = true;
                } else {
                    String tmpname = orm.getUserName(tmpid);
                    if (!isEmpty(tmpname)) {
                        if (friends.length() > 0) friends.append(", ");
                        friends.append(tmpname);
                    }
                }
            }
        }

        if (post.iLike) {
            stream_like_span.setVisibility(View.VISIBLE);
            if (friends.length() > 0) {
                stream_like_string.setText(friends + ", " + ilike);
            } else {
                stream_like_string.setText(ilike);
            }
        } else {
            if (friends.length() > 0) {
                stream_like_span.setVisibility(View.VISIBLE);
                stream_like_string.setText(friends + likeStr);
            } else {
                stream_like_span.setVisibility(View.GONE);
            }
        }

        String likes = String.format("%1$s(%2$s)", likestr, post.likes.count);
        if (post.likes.count > 0) {
            qiupu_stream_like.setText(likes);
        } else {
            qiupu_stream_like.setText(likestr);
        }

        //show comments
        List<Stream.Comments.Stream_Post> commentList = post.comments.getCommentList();
        if (commentList.size() > 0) {
            if (share_comment_ll.getBackground() == null) {
                share_comment_ll.setBackgroundResource(R.color.bpc_comments_bg);
            }
            stream_comment_divider.setVisibility(View.VISIBLE);
            comment_1.setVisibility(View.VISIBLE);
            comment_1.setCommentItem(commentList.get(0));

            if (commentList.size() > 1) {
                comment_2.setVisibility(View.VISIBLE);
                comment_2.setCommentItem(commentList.get(1));
            } else {
                comment_2.setVisibility(View.GONE);
            }
        } else {
            share_comment_ll.setBackgroundDrawable(null);
            comment_2.setVisibility(View.GONE);
            comment_1.setVisibility(View.GONE);
            stream_comment_divider.setVisibility(View.GONE);
        }

        this.requestLayout();
    }

    private OnClickListener commentsClick = new OnClickListener() {
        public void onClick(View arg0) {

            if (post.type == BpcApiUtils.APK_COMMENT_POST || post.type == BpcApiUtils.APK_LIKE_POST) {
                IntentUtil.startAppCommentIntent(getContext(), post);;
            } else if (post.type == BpcApiUtils.BOOK_COMMENT_POST || post.type == BpcApiUtils.BOOK_LIKE_POST) {
                gotoBookItemComment();
            } else if (post.type == BpcApiUtils.MUSIC_COMMENT_POST || post.type == BpcApiUtils.MUSIC_LIKE_POST) {
                gotoMusicItemComment();
            } else {
                gotoStreamItemComment();
            }
        }
    };

    private void loadScreenShotImage(ArrayList<String> screenshotlink) {
        stream_photo_span.setVisibility(View.VISIBLE);
        img_pic_ui.setVisibility(View.VISIBLE);
        stream_photo_2.setImageDrawable(null);
        stream_photo_3.setImageDrawable(null);

        if (screenshotlink.size() > 0 && StringUtil.isValidString(screenshotlink.get(0))) {
            final String sLink1 = screenshotlink.get(0);
            stream_photo_2.setVisibility(View.VISIBLE);
            stream_photo_2.setImageResource(R.drawable.default_app_icon);
            stream_photo_2.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    String filePath = "";
                    try {
                        filePath = QiupuHelper.getImageFilePath(new URL(sLink1), true);
                    } catch (MalformedURLException e) {
                    }

                    if (new File(filePath).exists()) {
                        //open in Image view
                        Intent intent = new Intent(getContext(), QiupuPhotoActivity.class);
                        intent.putExtra("photo", sLink1);
                        getContext().startActivity(intent);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(sLink1));
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                    }
                }
            });
            ImageRun photo_2 = new ImageRun(null, sLink1, 0);
            photo_2.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_SCREENSHOT;
            photo_2.addHostAndPath = true;
            photo_2.need_scale = true;
            photo_2.width = 80;
            photo_2.setImageView(stream_photo_2);
            photo_2.post(null);
        }

        if (screenshotlink.size() > 1 && StringUtil.isValidString(screenshotlink.get(1))) {
            final String sLink2 = screenshotlink.get(1);
            stream_photo_3.setVisibility(View.VISIBLE);
            stream_photo_3.setImageResource(R.drawable.default_app_icon);
            stream_photo_3.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    String filePath = "";
                    try {
                        filePath = QiupuHelper.getImageFilePath(new URL(sLink2), true);
                    } catch (MalformedURLException e) {
                    }

                    if (new File(filePath).exists()) {
                        //open in Image view
                        Intent intent = new Intent(getContext(), QiupuPhotoActivity.class);
                        intent.putExtra("photo", sLink2);
                        getContext().startActivity(intent);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(sLink2));
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                    }
                }
            });

            ImageRun photo_3 = new ImageRun(null, sLink2, 0);
            photo_3.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_SCREENSHOT;
            photo_3.addHostAndPath = true;
            photo_3.need_scale = true;
            photo_3.width = 80;
            photo_3.setImageView(stream_photo_3);
            photo_3.post(null);
        }
    }

    private String getApkNameMessage(String message) {
        final int start = message.lastIndexOf(mContext.getString(R.string.post_message_last_index_of));
        final int end = message.indexOf(mContext.getString(R.string.post_message_index_of));
        if (start < end) {
            return message.substring(start + 3, end);
        }
        return null;
    }    

    private void parseApkPostsAttachmentsUi() {
        if (null == post) {
            Log.i(TAG, "parseApkPostsAttachmentsUi, ignore empty post.");
            return;
        } else if (null == post.attachment) {
            Log.i(TAG, "parseApkPostsAttachmentsUi, ignore empty attachment post:" + post);
            return;
        } else if (!(post.attachment instanceof Stream.ApkAttachment)) {
            Log.e(TAG, "parseApkPostsAttachmentsUi, ignore invalid Apk attachment: " + post.attachment);
            return;
        }

        if (post.attachment.attachments.size() <= 0) {
            share_content_span.setVisibility(View.GONE);
            stream_photo_span.setVisibility(View.GONE);
            qiupu_download_from_market.setVisibility(View.GONE);
            Log.e(TAG, "no attachment found in post: " + post);
        } else {
            final ApkBasicInfo response = (ApkBasicInfo) post.attachment.attachments.get(0);
            if (response != null) {
                share_content_span.setVisibility(View.VISIBLE);
                stream_photo_indicator.setVisibility(View.VISIBLE);
                stream_photo_indicator.setImageResource(R.drawable.default_app_icon);

                postContentTV.setText(response.label + " " + response.versionname);
                postContentTV.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        gotoApkDetail(response);
                    }
                });

                ImageRun photo_1 = new ImageRun(null, response.iconurl, 0);
                photo_1.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_APK;
                photo_1.addHostAndPath = true;
                photo_1.noimage = false;
                photo_1.need_scale = true;
                photo_1.width = 48;
                photo_1.setImageView(stream_photo_indicator);
                photo_1.post(null);

                stream_photo_indicator.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        gotoApkDetail(response);
                    }
                });

                //set image region
                if (!forcomments) {
                    boolean showDownloadFromMarket = false;
                    if (showDownloadFromMarket) {
                        qiupu_download_from_market.setVisibility(View.VISIBLE);
                    }
                }

                if (response.screenshotLink != null && response.screenshotLink.size() > 0) {
                    if (!forcomments && orm.showApkScreenSnap()) {
                        loadScreenShotImage(response.screenshotLink);
                    } else {
                        img_pic_ui.setVisibility(View.GONE);
                    }
                } else {
                    img_pic_ui.setVisibility(View.GONE);
                }

                if (!isEmpty(response.description)) {
                    stream_message_des.setVisibility(View.VISIBLE);
                    stream_message_des.setText(Html.fromHtml(response.description.subSequence(0, response.description.length() > 100 ? 100 : response.description.length()).toString().trim()));
                } else {
                    stream_message_des.setVisibility(View.GONE);
                }
            }
        }
    }

    private void parseLinkPostsAttachmentsUi() {
        if (post.attachment != null && post.attachment.attachments != null && post.attachment.attachments.size() > 0) {
            share_link_ll.setVisibility(View.VISIBLE);
            final Stream.URLLinkAttachment.URLLink link = (Stream.URLLinkAttachment.URLLink) (post.attachment.attachments.get(0));
            if (!isEmpty(link.title)) {
                link_title.setVisibility(View.VISIBLE);
                final String title = link.title.length() > 50 ? link.title.substring(0, 50) : link.title;
                link_title.setText(title);
                link_title.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(link.url));

                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            getContext().startActivity(intent);
                        } catch (Exception ne) {
                        }
                    }
                });
            } else {
                link_title.setVisibility(View.GONE);
            }

            if (!isEmpty(link.host)) {
                link_host.setVisibility(View.VISIBLE);
                link_host.setText(link.host);
            } else {
                link_host.setVisibility(View.GONE);
            }

            if (!isEmpty(link.description)) {
                link_des.setVisibility(View.VISIBLE);
                link_des.setText(link.description);
            } else {
                link_des.setVisibility(View.GONE);
            }


            if (isEmpty(link.host)) {
                link_photo.setVisibility(View.GONE);
            } else {
                link_photo.setVisibility(View.VISIBLE);
                link_photo.setImageBitmap(null);
                ImageRun photo_1 = new ImageRun(null, link.favorite_icon_url, 0);
                photo_1.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_SCREENSHOT;
                photo_1.addHostAndPath = true;
                photo_1.need_scale = true;
                photo_1.width = 100;
                photo_1.noimage = true;
                photo_1.setImageView(link_photo);
                photo_1.post(null);
            }
        } else {
            share_link_ll.setVisibility(View.GONE);
        }
    }

    private void parseApkLinkPostsAttachmentsUi(String apkLink) {
        if (null == post) {
            Log.i(TAG, "parseApkLinkPostsAttachmentsUi, ignore empty post.");
            return;
        } else if (null == post.attachment) {
            Log.i(TAG, "parseApkLinkPostsAttachmentsUi, ignore empty attachment post:" + post);
            return;
        } else if (!(post.attachment instanceof Stream.ApkAttachment)) {
            Log.e(TAG, "parseApkLinkPostsAttachmentsUi, ignore invalid Apk attachment: " + post.attachment);
            return;
        }

        if (!isEmpty(post.attachment.link.href)) {
            findViewById(R.id.post_prompt).setVisibility(View.VISIBLE);
            share_content_span.setVisibility(View.VISIBLE);
            stream_photo_span.setVisibility(View.GONE);
            stream_photo_indicator.setVisibility(View.GONE);
            String href = post.attachment.link.href;
            if (apkLink != null) {
                postContentTV.setText(apkLink);
            } else {
                int start = href.indexOf("=");
                String tmphref = href.substring(start + 1, href.length());
                postContentTV.setText(tmphref);

            }
            if (post.attachment.link.href.contains(QiupuConfig.MARKET_SEARCH_HOST)) {
                qiupu_download_from_market.setVisibility(View.VISIBLE);
            } else {
                qiupu_download_from_market.setVisibility(View.GONE);
            }

            postContentTV.setOnClickListener(new OnClickListener() {

                public void onClick(View arg0) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(post.attachment.link.href));

                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                }
            });

        }
    }

    private void parseBookPostsAttachmentsUi() {
        if (null == post) {
            Log.i(TAG, "parseBookPostsAttachmentsUi, ignore empty post.");
            return;
        } else if (null == post.attachment) {
            Log.i(TAG, "parseBookPostsAttachmentsUi, ignore empty attachment post:" + post);
            return;
        } else if (!(post.attachment instanceof Stream.BookAttachment)) {
            Log.e(TAG, "parseBookPostsAttachmentsUi, ignore invalid Book attachment: " + post.attachment);
            return;
        }

        if (post.attachment.attachments.size() <= 0) {
            share_content_span.setVisibility(View.GONE);
            stream_photo_span.setVisibility(View.GONE);
            qiupu_download_from_market.setVisibility(View.GONE);
            Log.e(TAG, "no attachment found in post: " + post);
        } else {
            final BookBasicInfo response = (BookBasicInfo) post.attachment.attachments.get(0);
            if (response != null) {
                share_content_span.setVisibility(View.VISIBLE);
                postContentTV.setText(response.name);
                postContentTV.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        gotoBookDetail(response);
                    }
                });


                stream_photo_indicator.setVisibility(View.VISIBLE);
                stream_photo_indicator.setImageResource(R.drawable.default_book);

                ImageRun photo_1 = new ImageRun(null, response.coverurl, 0);
                photo_1.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_BOOK;
                photo_1.addHostAndPath = true;
                photo_1.need_scale = true;
                photo_1.width = 48;
                photo_1.setImageView(stream_photo_indicator);
                photo_1.post(null);

                stream_photo_indicator.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        gotoBookDetail(response);
                    }
                });

                //set image region
//                if (!forcomments) {
//                    stream_photo_span.setVisibility(View.VISIBLE);
//                    qiupu_download_from_market.setVisibility(View.VISIBLE);
//                }
//                stream_photo_2.setImageDrawable(null);
//                stream_photo_3.setImageDrawable(null);
//
//                if (response.screenshotLink != null && response.screenshotLink.length > 0) {
//                    img_pic_ui.setVisibility(View.VISIBLE);
//                    if (orm.showApkScreenSnap() && forcomments == false) {
//                        loadScreenShotImage(response.screenshotLink);
//                    }
//                } else {
//                    img_pic_ui.setVisibility(View.GONE);
//                }

                final String description = response.summary;
                if (!isEmpty(description)) {
                    stream_message_des.setVisibility(View.VISIBLE);
                    stream_message_des.setText(description.subSequence(0, description.length() > 100 ? 100 : description.length()));
                } else {
                    stream_message_des.setVisibility(View.GONE);
                }
            }
        }
    }

    private void parseMusicPostsAttachmentsUi() {
        // TODO: do for music itselft.
        if (null == post) {
            Log.i(TAG, "parseBookPostsAttachmentsUi, ignore empty post.");
            return;
        } else if (null == post.attachment) {
            Log.i(TAG, "parseBookPostsAttachmentsUi, ignore empty attachment post:" + post);
            return;
        } else if (!(post.attachment instanceof Stream.BookAttachment)) {
            Log.e(TAG, "parseBookPostsAttachmentsUi, ignore invalid Book attachment: " + post.attachment);
            return;
        }

        if (post.attachment.attachments.size() <= 0) {
            share_content_span.setVisibility(View.GONE);
            stream_photo_span.setVisibility(View.GONE);
            qiupu_download_from_market.setVisibility(View.GONE);
            Log.e(TAG, "no attachment found in post: " + post);
        } else {
            final BookBasicInfo response = (BookBasicInfo) post.attachment.attachments.get(0);
            if (response != null) {
                share_content_span.setVisibility(View.VISIBLE);
                postContentTV.setText(response.name);
                postContentTV.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        gotoMusicDetail(response);
                    }
                });


                stream_photo_indicator.setVisibility(View.VISIBLE);
                stream_photo_indicator.setImageResource(R.drawable.music_default);

                ImageRun photo_1 = new ImageRun(null, response.coverurl, 0);
                photo_1.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_Music;
                photo_1.addHostAndPath = true;
                photo_1.need_scale = true;
                photo_1.width = 48;
                photo_1.setImageView(stream_photo_indicator);
                photo_1.post(null);

                stream_photo_indicator.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        gotoMusicDetail(response);
                    }
                });

                final String description = response.summary;
                if (!isEmpty(description)) {
                    stream_message_des.setVisibility(View.VISIBLE);
                    stream_message_des.setText(description.subSequence(0, description.length() > 100 ? 100 : description.length()));
                } else {
                    stream_message_des.setVisibility(View.GONE);
                }
            }
        }
//        parseBookPostsAttachmentsUi();
    }
//
//    private void gotoApkDetail(ApkBasicInfo response) {
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
//    }

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

    OnClickListener moreToUserClick = new OnClickListener() {
        public void onClick(View arg0) {
//			more_to_user.setVisibility(View.GONE);

            StringBuilder nameformat = new StringBuilder();
            nameformat.append(String.format("<a href='%3$s%2$s'>%1$s</a>", post.fromUser.nick_name, post.fromUser.uid, profileURL));

            StringBuilder nameraw = new StringBuilder();
            nameraw.append(post.fromUser.nick_name);
            for (int i = 0; i < post.toUsers.size(); i++) {
                if (i == 0) {
                    nameformat.append(String.format(" > <a href='%3$s%2$s'>%1$s</a>", replaceToUserNickName(post.toUsers.get(i).nick_name), post.toUsers.get(i).uid, profileURL));
                    nameraw.append(" > ").append(post.toUsers.get(i).nick_name);
                } else {
                    nameformat.append(String.format(", <a href='%3$s%2$s'>%1$s</a>", replaceToUserNickName(post.toUsers.get(i).nick_name), post.toUsers.get(i).uid, profileURL));
                    nameraw.append(", ").append(post.toUsers.get(i).nick_name);
                }
            }

            parseName(nameformat.toString(), nameraw.toString());
        }
    };

    private void parseName(String username, String rawname) {
        username = HtmlUtils.text2html(username);
        author_name.setText(MyHtml.fromHtml(username));
        /*
        SpannableString sb = (SpannableString) author_name.getText();
        SpannableString ss = new SpannableString(rawname);
        URLSpan[] spans = author_name.getUrls();
        for (URLSpan span1 : spans) {
            int start = sb.getSpanStart(span1);
            int end = sb.getSpanEnd(span1);
            String text = sb.subSequence(start, end).toString();

            int startpp = rawname.indexOf(text);
            if (start < startpp) {
                int span = (startpp - start);
                start += (span);
                end += (span);
            }
            //if(SNSService.DEBUG)
            //Log.d(TAG, "text="+text + " url="+spans[i].getURL());

            MyHtml.BorqsUrlSpan my = new MyHtml.BorqsUrlSpan(span1.getURL());
            ss.setSpan(my, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            //Log.i(TAG,"----- ss:"+ss);
            my = null;
        }

        //if(SNSService.DEBUG)
        //Log.d(TAG, "username  SpannableString = "+ss);
        author_name.setText(ss);
        ss = null;
        sb = null;
        */
    }

	@Override
	public Stream getStream() {		
		return post;
	}
}
