package com.borqs.qiupu.cache;

import android.graphics.Bitmap;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.display.BitmapDisplayer;

public class AnimationBitmapDisplayer  implements BitmapDisplayer {
	
	private Animation start_anim;
	private Animation end_anim;

	public AnimationBitmapDisplayer(Animation start_anim, Animation end_anim) {
		super();
		this.start_anim = start_anim;
		this.end_anim = end_anim;
	}

	@Override
	public Bitmap display(Bitmap bitmap, ImageView imageView) {
		if(start_anim != null) {
			imageView.startAnimation(start_anim);
		}
		imageView.setImageBitmap(bitmap);
		if(end_anim != null) {
			imageView.startAnimation(end_anim);
		}
		return bitmap;
	}

}
