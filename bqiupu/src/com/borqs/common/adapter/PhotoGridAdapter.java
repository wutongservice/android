package com.borqs.common.adapter;

import java.util.ArrayList;

import twitter4j.QiupuPhoto;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.db.QiupuORM.QiupuPhotoColumns;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class PhotoGridAdapter extends CursorAdapter {
    private ArrayList<QiupuPhoto> mLists;
    LayoutInflater mInflater;
    int mPreferredImageSize;
    boolean isProfileImage;
    
    
    public PhotoGridAdapter(Context context, Cursor c,int image_width,boolean isProfileImage) {
		super(context, c);
		 mInflater = LayoutInflater.from(context);
		 this.mPreferredImageSize = image_width;
		 this.isProfileImage = isProfileImage;
	}

	class ViewHolder {
    	public ImageView image_cover;
	}

    
    private void initImageUI(String image_url,ImageView imageView)
	{
		ImageRun imagerun = new ImageRun(null, image_url, 0);
		imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_SCREENSHOT;
		imagerun.width = mPreferredImageSize;
		imagerun.height = mPreferredImageSize;
		imagerun.noimage = true;
		imagerun.isRoate = true;
	    imagerun.addHostAndPath = true;
        imagerun.setImageView(imageView);    
        imagerun.post(null);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mInflater.inflate(R.layout.photos_album_grid_item2, null);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		 if(cursor != null && cursor.getCount() > 0) {
			final  ImageView image_cover = (ImageView) view
 						.findViewById(R.id.image_cover);
			 image_cover.setLayoutParams(new RelativeLayout.LayoutParams(mPreferredImageSize, mPreferredImageSize));
 	 		image_cover.setImageResource(R.drawable.photo_default_img);
 	 		String photo_url = null;
 	 		if(isProfileImage) {
 	 			photo_url = cursor.getString(cursor.getColumnIndex(QiupuPhotoColumns.PHOTO_URL_BIG));// Load image into ImageView
 	 		}else {
 	 			photo_url = cursor.getString(cursor.getColumnIndex(QiupuPhotoColumns.PHOTO_URL_THUMBNAIL));// Load image into ImageView
 	 		}
 	 		image_cover.setTag(photo_url);
 	 		final String url = photo_url;
 	 		view.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					shootImageRunner(url, image_cover);// Load image into ImageView
				}
			}, 500);
		 }
		
	}
	
//	private void shootImageRunner(String photoUrl,CacheableImageView img) {
//		photo_1 = new SuperImageRun( photoUrl, QiupuApplication.getApplication(img.getContext()).getBitmapCache());
////		photo_1.SetOnImageRunListener(new SuperImageRun.OnImageRunListener() {
////
////			@Override
////			public void onLoadingFinished() {
////				if(sendMsg) {
////					Message msg = mHandler.obtainMessage(LOAD_SUCCESS);
////					msg.sendToTarget();
////				}
////			}
////
////			@Override
////			public void onLoadingFailed() {
////				if(sendMsg) {
////					Message msg = mHandler.obtainMessage(LOAD_FAILED);
////					msg.sendToTarget();
////				}
////			}});
//		photo_1.addHostAndPath = true;
////		final Resources resources = img.getResources();
////		float dendity = resources.getDisplayMetrics().density;
//////		photo_1.max_num_pixels = (int)(460*460*dendity*dendity);
////		photo_1.width = resources.getDisplayMetrics().widthPixels;
////		photo_1.height = resources.getDisplayMetrics().heightPixels;
////		photo_1.noimage = true;
//		photo_1.setImageView(img);
//		photo_1.post(null);
//	}
	private void shootImageRunner(String photoUrl,final ImageView img) {
		if(TextUtils.isEmpty(photoUrl) || !photoUrl.equals(img.getTag())) {
			return;
		}
//		img.setTag(photoUrl);
		// Get singletone instance of ImageLoader
		ImageLoader imageLoader = QiupuApplication.getApplication(img.getContext()).getImageLoader();
		// Creates display image options for custom display task (all options are optional)
		DisplayImageOptions options = new DisplayImageOptions.Builder()
					.showStubImage(R.drawable.photo_default_img)
		           .resetViewBeforeLoading()
		           .cacheInMemory()
		           .cacheOnDisc()
		           .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
		           .bitmapConfig(Bitmap.Config.RGB_565)
//		           .delayBeforeLoading(1000)
//		           .displayer(new RoundedBitmapDisplayer(20))
		           .build();
		// Load and display image asynchronously
		imageLoader.displayImage(photoUrl, img,options, new ImageLoadingListener() {
		    @Override
		    public void onLoadingStarted() {
		    }
		    @Override
		    public void onLoadingFailed(FailReason failReason) {
//		    	img.setImageResource(R.drawable.about_logo);
		    }
		    @Override
		    public void onLoadingComplete(Bitmap loadedImage) {
		    }
		    @Override
		    public void onLoadingCancelled() {
		        // Do nothing
		    }
		});
	}
}
