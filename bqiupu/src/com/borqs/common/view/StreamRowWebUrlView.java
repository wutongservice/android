package com.borqs.common.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.MyHtml;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.cache.ThumbnailImageRun;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.QiupuPhotoActivity;
import com.borqs.qiupu.util.StringUtil;
import twitter4j.CacheMap;
import twitter4j.Stream;
import twitter4j.Stream.URLLinkAttachment.URLLink;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-11-21
 * Time: 下午3:11
 * To change this template use File | Settings | File Templates.
 */
class StreamRowWebUrlView extends StreamRowView {
    private static final String TAG = "StreamRowWebUrlView";

    private static int mImageViewDimension;
    private static int mItemPaddingDimension;

    private ArrayList<String> mPhotos;
//    private int mIndex = 0;
    private View mCurrentView;
    private View mThumbnail;
    private OnClickListener coverClicker;

    private ObjectCountSwitcher mPlayer;

    public StreamRowWebUrlView(Context ctx, Stream stream, boolean isComments) {
        super(ctx, R.layout.stream_row_layout_weburl, stream, isComments);
    }
    
    protected StreamRowWebUrlView(Context ctx, int resId, Stream stream, boolean isComments) {
    	 super(ctx, resId, stream, isComments);
    }
    @Override
    protected void setUI() {
        if (null == mPhotos) {
            mImageViewDimension = (int) getResources().getDimension(R.dimen.stream_image_dimension);
            mItemPaddingDimension = (int)getResources().getDimension(R.dimen.stream_item_content_padding);

            mPhotos = new ArrayList<String>();
//            switchListener = new StreamRowItemClicker() {
//                @Override
//                protected void onRowItemClick(View view) {
//                    final int size = mPhotos.size();
//                    post.mSelectImageIndex = post.mSelectImageIndex >= size - 1 ? 0 : post.mSelectImageIndex + 1;
//                    onImageHeaderShown();
//                    // mCurrentView.startAnimation(getHideAnimation());
//                    setupThumbnailImageRunner(mCurrentView, mThumbnail, mPhotos.get(post.mSelectImageIndex), mImageViewDimension, coverClicker);
//                }
//            };
        }

        setupUrlLinkAttachment(this, post);
        refreshPostedMessageTextContent(this, post);
        setStreamFooterUi(true);
    }

    protected void setupUrlLinkAttachment(ViewGroup parent, Stream stream) {
        String description = null;
        if (null != parent) {
            final int count = null == stream || null == stream.attachment
                    || null == stream.attachment.attachments ? 0 :
                    stream.attachment.attachments.size();
            if (count > 0) {
                if (!mPhotos.isEmpty()) {
                    mPhotos.clear();
                }

                final URLLink link = (URLLink) stream.attachment.attachments.get(0);
                if (null != link) {
                    coverClicker = new StreamRowItemClicker() {
                        public void onRowItemClick(View view) {
                            try {
                                IntentUtil.startLinkTraActivity(getContext(), link.url);
//                                Intent intent = new Intent(Intent.ACTION_VIEW);
//                                intent.setData(Uri.parse(link.url));
//                                intent.putExtra(Browser.EXTRA_APPLICATION_ID, getContext().getPackageName());
//                                getContext().startActivity(intent);
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


                    description = link.description;

                    final boolean hasPhoto = inflatePhotoAlbum(parent, link.all_image_urls, coverClicker);
                    setupPostContentUi(linkContent, hasPhoto, null);
                }
            }
        }
        showStreamMessageDescription(description);
    }

    @Override
    protected boolean inflatePhotoAlbum(View attachmentContainer,
                                        ArrayList<String> screenLink,
                                        OnClickListener clickListener) {
        final int screenShotSize = null == screenLink ? 0 : screenLink.size();
        ImageView stream_photo_1 = (ImageView) attachmentContainer.findViewById(R.id.stream_photo_1);
        ImageView thumbnail = (ImageView)attachmentContainer.findViewById(R.id.stream_photo_thumbnail);
        if (null != stream_photo_1) {
            mCurrentView = stream_photo_1;
            mThumbnail = thumbnail;
            if (screenShotSize > 0) {
                mPhotos.addAll(screenLink);
                post.mSelectImageIndex = post.mSelectImageIndex > screenShotSize - 1 ? 0 : post.mSelectImageIndex;
                setupThumbnailImageRunner(mCurrentView, mThumbnail,
                        mPhotos.get(post.mSelectImageIndex), mImageViewDimension, clickListener);

                initImageCountSwitcher(post.mSelectImageIndex, screenShotSize);

                stream_photo_1.setVisibility(View.VISIBLE);
                thumbnail.setVisibility(View.VISIBLE);

                return true;
            } else {
                adjustLayoutPadding(false, attachmentContainer);
                stream_photo_1.setVisibility(View.GONE);
                thumbnail.setVisibility(View.GONE);
                onImageHeaderShown();
            }
        }

        return false;
    }

    private void adjustLayoutPadding(boolean photoVisible, View parent) {
        ImageView posterIconIV = (ImageView) parent.findViewById(R.id.user_icon);
        if (null != posterIconIV) {
            ViewGroup.MarginLayoutParams lp = (RelativeLayout.LayoutParams) posterIconIV.getLayoutParams();
            lp.setMargins(mItemPaddingDimension, mItemPaddingDimension, 0, 0);
            posterIconIV.requestLayout();
        }

        View header = parent.findViewById(R.id.row_header);
        if (null != header) {
            ViewGroup.MarginLayoutParams lp = (RelativeLayout.LayoutParams)header.getLayoutParams();
            lp.setMargins(0, mItemPaddingDimension, 0, 0);
            header.requestLayout();
        }
    }


    protected void setupPostContentUi(String txtContent, OnClickListener clickListener) {
        TextView postContentTV = (TextView) findViewById(R.id.post_content);
        if (null != postContentTV) {
            final String trimText = txtContent.trim();
            if (!TextUtils.isEmpty(trimText)) {
                postContentTV.setText(trimText);
                postContentTV.setOnClickListener(clickListener);
            }
        }
    }

    private void setupPostContentUi(String txtContent, boolean hasPhoto, OnClickListener clickListener) {
        TextView postContentTV = (TextView) findViewById(R.id.post_content);
        TextView coverTV = (TextView) findViewById(R.id.post_content_cover);
        if (hasPhoto) {
            postContentTV.setVisibility(View.GONE);
            if (null != coverTV) {
                final String trimText = txtContent.trim();
                if (!TextUtils.isEmpty(trimText)) {
                    coverTV.setText(MyHtml.fromHtml(trimText));
                    coverTV.setVisibility(View.VISIBLE);
                    coverTV.setOnClickListener(clickListener);
                    attachMovementMethod(coverTV);
                }
            }
        } else {
            coverTV.setVisibility(View.INVISIBLE);
            if (null != postContentTV) {
                final String trimText = txtContent.trim();
                if (!TextUtils.isEmpty(trimText)) {
                    postContentTV.setText(MyHtml.fromHtml(trimText));
                    postContentTV.setVisibility(View.VISIBLE);
                    postContentTV.setOnClickListener(clickListener);
                    attachMovementMethod(postContentTV);
                }
            }
        }
    }

//    private OnClickListener switchListener;

    private void setupThumbnailImageRunner(View image, View thumbnail, final String path, int size, OnClickListener clickListener) {
//        post.mSelectImageIndex = mIndex;
        if (null == image || !(image instanceof ImageView || image instanceof ImageSwitcher)) {
            Log.d(TAG, "setupThumbnailImageRunner, invalid image, ignore path: " + path);
            return;
        }

//        image.setBackgroundResource(getSubImageRes());
        if (image instanceof ImageView) {
            ((ImageView) image).setImageDrawable(null);
        } else if (image instanceof ImageSwitcher) {
            ((ImageSwitcher) image).setImageDrawable(null);
        } else {
            Log.d(TAG, "setupThumbnailImageRunner, unknown view: " + image);
            return;
        }

        if (StringUtil.isValidString(path)) {
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

            ImageRun photoRunner = new ThumbnailImageRun(null, path, 0, thumbnail);
            photoRunner.width = size;
            photoRunner.height = size;

            if (post.default_image_random_id == 0) {
                post.default_image_random_id = getRandomDefaultIconIndex();
            }

            photoRunner.default_image_index = post.default_image_random_id;
            if (photoRunner.setImageView(image) == false) {
                photoRunner.isSavedMode = QiupuORM.isDataFlowAutoSaveMode(getContext());
                photoRunner.post(null);
            }
        }
    }

    private void onImageHeaderShown() {
        final View header = findViewById(R.id.stream_row_picture_unit);
        if (null != header) {
            header.setVisibility(mPhotos.isEmpty() ? GONE : VISIBLE);
        }
//
//        View countView = findViewById(R.id.image_count);
//        if (null != countView && countView instanceof TextView) {
//            final int count = null == mPhotos ? 0 : mPhotos.size();
//            if (count > 1) {
//                ((TextView)countView).setText(String.format("%1$s/%2$s", post.mSelectImageIndex + 1, count));
//                countView.setVisibility(View.VISIBLE);
//            } else {
//                countView.setVisibility(View.GONE);
//            }
//        }
//        return countView;
    }


    private void initImageCountSwitcher(int current, int screenShotSize) {
        View view = findViewById(R.id.image_count);
        if (null != view && view instanceof TextView) {
            if (null == mPlayer) {
                mPlayer = ObjectCountSwitcher.instanceSwitcher((TextView)view, screenShotSize, current);
            } else {
                mPlayer.attach((TextView)view, screenShotSize, current);
            }

            mPlayer.setPlayListener(new ObjectCountSwitcher.Callback() {
                @Override
                public int play(int selection) {
                    return playIndex(selection);
                }
            });
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (View.VISIBLE != visibility && mPlayer != null && mPlayer.getPlayStatus() == true && mPlayer.getPlayButton() != null) {
            mPlayer.getPlayButton().performClick();
        }
    }

    private int playIndex(int selection) {
        if (mPhotos.isEmpty()) {
            return 0;
        }

        final int size = mPhotos.size();
        post.mSelectImageIndex = selection > size - 1 ? 0 : selection;

        setupThumbnailImageRunner(mCurrentView, mThumbnail,
                mPhotos.get(post.mSelectImageIndex),
                mImageViewDimension, coverClicker);

        return post.mSelectImageIndex;
    }
}
