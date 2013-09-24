package com.borqs.qiupu.cache;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import com.borqs.qiupu.QiupuConfig;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-11-26
 * Time: 下午3:37
 * To change this template use File | Settings | File Templates.
 */
public class ThumbnailImageRun extends AnimationImageRun {
    private static final String TAG = "ThumbnailImageRun";

    private static int THUMBNAIL_WIDTH = 150;
    private static int THUMBNAIL_HEIGHT = 80;

    private View mThumbnail;
    private int widthThreshold;
    private int heightThreshold;

    public ThumbnailImageRun(Handler handler, String url, int highPriority, View thumbnail) {
        super(handler, url, highPriority);

        mThumbnail = thumbnail;

        widthThreshold = THUMBNAIL_WIDTH;
        heightThreshold = THUMBNAIL_HEIGHT;

        init();
    }

    public ThumbnailImageRun(Handler handler, String url, int highPriority,
                             View thumbnail, int widthThreshold, int heightThreshold) {
        super(handler, url, highPriority);

        mThumbnail = thumbnail;

        widthThreshold = widthThreshold;
        heightThreshold = heightThreshold;

        init();
    }

    private void init() {
        default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_SCREENSHOT;
        addHostAndPath = true;
        noimage = false;
        forStreamPhoto = true;
    }

    @Override
    protected void setImageBmp(int resbmp) {
        if (null != imgView) {
            imgView.startAnimation(AnimationUtils.loadAnimation(
                    imgView.getContext(), android.R.anim.fade_out));
            if (ImageView.class.isInstance(imgView))
                ((ImageView) imgView).setImageResource(resbmp);
            else if (TextView.class.isInstance(imgView))
                ((TextView) imgView).setCompoundDrawables(imgView.getContext().getResources().getDrawable(resbmp), null, null, null);
            else if (imgView instanceof ImageSwitcher) {
                ((ImageSwitcher) imgView).setImageResource(resbmp);
            }
            imgView.startAnimation(AnimationUtils.loadAnimation(
                    imgView.getContext(), android.R.anim.fade_in));

            onImageSet();
        }

        if (null != mThumbnail) {
            mThumbnail.setVisibility(View.GONE);
        }
    }


    @Override
    protected void setImageBmpWithDispose(Bitmap bmp, boolean withDispose) {
        if (null != imgView) {
            fadeOutAnimate(imgView);
            super.setImageBmpWithDispose(bmp, withDispose);
            fadeInAnimate(imgView);

            if (withDispose) {
                onImageSet();
            }
        }
    }

    @Override
    protected void dispose() {
        // Override to do nothing and invoke super.dispose after animation played.
        mPostDisposing = true;
    }

    private boolean mPostDisposing;

    private void onImageSet() {
        if (mPostDisposing) {
            mPostDisposing = false;
            Log.v(TAG, "run dispose onImageSet()");
            super.dispose();

            mThumbnail = null;
        }
    }

    public static void fadeOutAnimate(View view) {
        view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), android.R.anim.fade_out));
    }

    public static void fadeInAnimate(View view) {
        view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), android.R.anim.fade_in));
    }

    @Override
    public boolean setImageView(View view) {
        if (null != mThumbnail) {
            mThumbnail.setVisibility(View.GONE);
        }

        return super.setImageView(view);
    }

    @Override
    protected void onThumbnailShow(Bitmap bitmap) {
        if (null != mThumbnail) {
            if (bitmap != null &&
                    bitmap.getWidth() > widthThreshold &&
                    bitmap.getHeight() > heightThreshold) {
                mThumbnail.setVisibility(View.GONE);
            } else {
                mThumbnail.setVisibility(View.VISIBLE);
                fadeOutAnimate(mThumbnail);
                super.setImageBmpWithDispose(mThumbnail, bitmap, false);
                fadeInAnimate(mThumbnail);
            }
        }
    }
}
