package com.borqs.common.view;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import twitter4j.QiupuPhoto;
import twitter4j.QiupuSimpleUser;
import twitter4j.Stream;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.borqs.common.util.IntentUtil;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.AnimationBitmapDisplayer;
import com.borqs.qiupu.cache.AnimationImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.QiupuPhotoActivity;
import com.borqs.qiupu.util.StringUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-11-21
 * Time: 下午3:11
 * To change this template use File | Settings | File Templates.
 */
class StreamRowPhotoView extends StreamRowView implements ViewSwitcher.ViewFactory{
    private static final String TAG = "StreamRowPhotoView";

    private ArrayList<QiupuPhoto> mPhotoList;
    private ImageSwitcher mSwitcher;

    private ObjectCountSwitcher mPlayer;

    public StreamRowPhotoView(Context ctx, Stream stream, boolean isComments) {
        this(ctx, R.layout.stream_row_layout_photo, stream, isComments);
    }

    protected StreamRowPhotoView(Context ctx, int resId, Stream stream, boolean isComments) {
        super(ctx, resId, stream, isComments);

        mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
        if (null != mSwitcher) {
            mSwitcher.setFactory(this);
            mSwitcher.setInAnimation(AnimationUtils.loadAnimation(getContext(),
                    android.R.anim.fade_in));
            mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getContext(),
                    android.R.anim.fade_out));
        }
    }

    @Override
    protected void setUI() {
        if (null == mPhotoList) {
            mImageViewDimension = (int) getResources().getDimension(R.dimen.stream_big_image_dimension);
            mPhotoList = new ArrayList<QiupuPhoto>();
//            switchListener = new StreamRowItemClicker() {
//                @Override
//                protected void onRowItemClick(View view) {
//                    final int size = mPhotoList.size();
//                    post.mSelectImageIndex = post.mSelectImageIndex >= size -1 ? 0 : post.mSelectImageIndex + 1;
//                    onImageCountShown();
//                    setupAppScreenShotImageRunner(mCurrentView, mPhotoList.get(post.mSelectImageIndex).photo_url_small,
//                            mImageViewDimension, coverClicker);
//                }
//            };
        }

        setupPhotoAttachment(this, post);
        refreshPostedMessageTextContent(this, post);
        setStreamFooterUi(true);
    }

    protected int parsePhotoAttachment(Stream stream) {
        int count = null == stream || null == stream.attachment
                || null == stream.attachment.attachments ? 0 :
                stream.attachment.attachments.size();
        if (count > 0) {
            if (!mPhotoList.isEmpty()) {
                mPhotoList.clear();
            }

            QiupuPhoto photo;
            QiupuSimpleUser user = stream.fromUser;
            for (Object item : stream.attachment.attachments) {
                photo = (QiupuPhoto) item;
                addToPhotoList(photo, user.uid, user.nick_name, user.profile_image_url);
            }
        }

        return count;
    }

    @Override
    protected void setupPhotoAttachment(ViewGroup parent, final Stream stream) {
        final int photoCount = parsePhotoAttachment(stream);
        if (photoCount <= 0) {
            Log.e(TAG, "setupPhotoAttachment, invilid photo attachment, stream= "
                    + stream.toString());
            return;
        }

        coverClicker = inflatePhotoTypeAlbum(parent, stream);
        final String albumSummary = mContext.getString(R.string.stream_content_share_photo,
                photoCount);
        setupPostContentUi(albumSummary, coverClicker);
    }

    private void addToPhotoList(final QiupuPhoto photo, long uid, String userName, String profileUrl) {
        photo.from_user_id = uid;
        photo.from_nick_name = userName;
        photo.from_image_url = profileUrl;
        mPhotoList.add(photo);
    }

    private static int mImageViewDimension;
//    private int mIndex = 0;
    private View mCurrentView;
    private OnClickListener coverClicker;
//    private OnClickListener switchListener;

    private OnClickListener inflatePhotoTypeAlbum(View attachmentContainer, final Stream stream) {
        OnClickListener coverClick = null;
        final int screenShotSize = mPhotoList.size();
//        mIndex = post.mSelectImageIndex > screenShotSize - 1 ? post.mSelectImageIndex : 0;
        post.mSelectImageIndex = post.mSelectImageIndex > screenShotSize - 1 ? 0 : post.mSelectImageIndex;
        if (screenShotSize > 0) {
            ImageView stream_photo_1 = (ImageView) attachmentContainer.findViewById(R.id.stream_photo_1);
            if (null != stream_photo_1 || null != mSwitcher) {
                final QiupuPhoto photo = mPhotoList.get(post.mSelectImageIndex);

                coverClick = new StreamRowItemClicker() {
                    public void onRowItemClick(View view) {
                        try {
                            final QiupuSimpleUser user = stream.fromUser;
                            IntentUtil.startPhotosViewIntent(getContext(), Long.valueOf(photo.album_id), user.uid,
                                    post.mSelectImageIndex, photo.album_name,
                                    mPhotoList, user.nick_name);
                        } catch (Exception ne) {
                        }
                    }
                };

                if (null != stream_photo_1) {
                    mCurrentView = stream_photo_1;
                } else {
                    mCurrentView = mSwitcher;
                }

                setupAppScreenShotImageRunner(mCurrentView, photo.photo_url_small, mImageViewDimension, coverClick);

                initImageCountSwitcher(post.mSelectImageIndex, screenShotSize);

            } else {
            }
        }

        return coverClick;
    }

//    private View onImageCountShown() {
//        View countView = findViewById(R.id.image_count);
//        if (null != countView && countView instanceof TextView) {
//            ((TextView)countView).setText(String.format("%1$s/%2$s", post.mSelectImageIndex + 1, mPhotoList.size()));
//        }
//        return countView;
//    }

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
        if (View.VISIBLE != visibility && mPlayer != null &&
                mPlayer.getPlayStatus() == true && mPlayer.getPlayButton() != null) {
            mPlayer.getPlayButton().performClick();
        }
    }

    private int playIndex(int selection) {
        if (mPhotoList.isEmpty()) {
            return 0;
        }

        final int size = mPhotoList.size();
        post.mSelectImageIndex = selection > size - 1 ? 0 : selection;

        setupAppScreenShotImageRunner(mCurrentView,
                mPhotoList.get(post.mSelectImageIndex).photo_url_small,
                mImageViewDimension, coverClicker);
        return post.mSelectImageIndex;
    }

    protected void setupPostContentUi(String txtContent, OnClickListener clickListener) {
        TextView postContentTV = (TextView) findViewById(R.id.post_content);
        if (null != postContentTV) {
            final String trimText = txtContent.trim();
            if (!TextUtils.isEmpty(trimText)) {
                postContentTV.setText(trimText);
            }
        }
    }

    @Override
    public View makeView() {
        ImageView i = new ImageView(getContext());
        i.setBackgroundResource(R.drawable.stream_photo_bg);
//        i.setBackgroundColor(0xFF000000);
        i.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        return i;
    }

    private void setupAppScreenShotImageRunner(View image, final String path, int size, OnClickListener clickListener) {
//        post.mSelectImageIndex = post.mSelectImageIndex;
        if (null == image || !(image instanceof ImageView || image instanceof ImageSwitcher)) {
            Log.d(TAG, "setupAppScreenShotImageRunner, invalid image, ignore path: " + path);
            return;
        }

        if (image instanceof ImageView) {
//            ((ImageView)image).setImageDrawable(null);
        } else if (image instanceof ImageSwitcher) {
//            ((ImageSwitcher)image).setImageDrawable(null);
        } else {
            Log.d(TAG, "setupAppScreenShotImageRunner, unknown view: " + image);
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

//            AnimationImageRun photoRunner = new AnimationImageRun(null, path, 0);
//            photoRunner.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_SCREENSHOT;
//            photoRunner.addHostAndPath = true;
//            photoRunner.noimage = true;
//            photoRunner.forStreamPhoto=true;
//            photoRunner.width = size;
//            photoRunner.height = size;
//            if(photoRunner.setImageView(image) == false)
//            {
//                photoRunner.isSavedMode = QiupuORM.isDataFlowAutoSaveMode(getContext());
//                photoRunner.post(null);
//            }
            if (post.default_image_random_id == 0) {
                post.default_image_random_id = getSubImageRes();
            }
            shootImageRunner(path, (ImageView)image, true, post.default_image_random_id);
        }
    }
    
    
    private void shootImageRunner(String photoUrl,final ImageView img,boolean hasListener, int image_random_id) {
		// Get singletone instance of ImageLoader
				ImageLoader imageLoader = QiupuApplication.getApplication(img.getContext()).getImageLoader();
				// Creates display image options for custom display task (all options are optional)
				DisplayImageOptions options = new DisplayImageOptions.Builder()
				           .resetViewBeforeLoading()
				           .cacheInMemory()
				           .cacheOnDisc()
				           .showStubImage(image_random_id)
				           .loadFromWeb(!QiupuORM.isDataFlowAutoSaveMode(getContext()))
				           .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				           .bitmapConfig(Bitmap.Config.RGB_565)
				           .displayer(new AnimationBitmapDisplayer(
				        		   AnimationUtils.loadAnimation(
				   				img.getContext(), android.R.anim.fade_out),
				   				AnimationUtils.loadAnimation(
				   						img.getContext(), android.R.anim.fade_in)))
				           .build();
				ImageLoadingListener listener = null;
				if(hasListener) {
					listener = new ImageLoadingListener() {
					    @Override
					    public void onLoadingStarted() {
					    }
					    @Override
					    public void onLoadingFailed(FailReason failReason) {
//					    	img.setImageResource(R.drawable.photo_default_img);
					    }
					    @Override
					    public void onLoadingComplete(Bitmap loadedImage) {
					    }
					    @Override
					    public void onLoadingCancelled() {
					        // Do nothing
					    }
					};
				}
				// Load and display image asynchronously
				imageLoader.displayImage(photoUrl, img,options,listener );
	}

}
