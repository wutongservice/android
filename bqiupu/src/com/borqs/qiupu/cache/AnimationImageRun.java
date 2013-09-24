package com.borqs.qiupu.cache;

import com.borqs.qiupu.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-11-26
 * Time: 下午3:37
 * To change this template use File | Settings | File Templates.
 */
public class AnimationImageRun extends ImageRun {
	 private static final String TAG = "AnimationImageRun";
    public AnimationImageRun(Handler handler, String url, int highPriority) {
        super(handler, url, highPriority);
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
    }

    
    @Override
    protected void setImageBmpWithDispose(Bitmap bmp, boolean withDispose) {
    	if (null != imgView) {
            fadeOutAnimate(imgView);
            super.setImageBmpWithDispose(bmp, withDispose);
            fadeInAnimate(imgView);

            if(withDispose) {
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
            Log.v(TAG, "run dispose() onImageSet()");
            super.dispose();
        }
    }

    public static void fadeOutAnimate(View view) {
        view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), /*android.R.anim.fade_out*/R.anim.slide_out));
    }

    public static void fadeInAnimate(View view) {
        view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), /*android.R.anim.fade_in*/R.anim.slide_in));
    }
}
