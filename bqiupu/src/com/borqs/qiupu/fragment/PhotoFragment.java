package com.borqs.qiupu.fragment;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.borqs.common.view.ImageViewTouch;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.AnimationBitmapDisplayer;
import com.borqs.qiupu.ui.bpc.PhotosViewActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class PhotoFragment extends BasicFragment {
    public static final String TAG = "PhotoFragment";
    private ClickListener cListener;
    private String small_url;
    private String big_url;
    public interface ClickListener {
    	void onclick();
    }
    
	public PhotoFragment() {
		super();
	}

	public PhotoFragment(String small_url,String big_url) {
		super();
		this.small_url = small_url;
		this.big_url = big_url;
	}
	

	MainHandler mHandler;
	View layout_process;
	public ImageViewTouch img;
	public ImageView small_imge;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(getActivity() != null && getActivity() instanceof PhotosViewActivity) {
			cListener = (ClickListener)getActivity();
		}
		View view = inflater.inflate(R.layout.photo_fragment, null);
		img = (ImageViewTouch) view
				.findViewById(R.id.img);
		small_imge = (ImageView) view
				.findViewById(R.id.small_imge);
		img.setImageViewTouchListener((ImageViewTouch.ImageViewTouchListener)getActivity());
		img.setFitToScreen(true);
		layout_process =  view
				.findViewById(R.id.layout_process);
		
		layout_process.setVisibility(View.VISIBLE);
		mHandler = new MainHandler();
		if(Build.VERSION.SDK_INT  < Build.VERSION_CODES.FROYO) {
			img.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					cListener.onclick();
					
				}
			});
		}
		 if(!TextUtils.isEmpty(small_url) && !small_url.equals(big_url)) {
			 shootImageRunner(small_url,small_imge,false);
		 }
		shootImageRunner(big_url,img,true);
		return view;
	}
	
//	public void refreshImageView(String path) {
//		if(!path.equals(this.path)) {
//			this.path = path;
//			shootImageRunner(path, img);
//		}
//	}
	
	public static final String RESULT = "RESULT";
	private final static int LOAD_SUCCESS = 0x0001;
	private final static int LOAD_FAILED = 0x0002;

	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_SUCCESS: {
				layout_process.setVisibility(View.GONE);
				break;
			}
			case LOAD_FAILED: {
				break;
			}
			}
		}
	}
	
//	@Override
//	public void onDetach() {
//	    Log.v(TAG, "-------------onDetach()------------------");
//	    if(img != null) {
//	    	img.clear();
//	    }
//	    if(!TextUtils.isEmpty(small_url) && !small_url.equals(big_url)) {
//			 ImageCacheManager.ContextCache.revokeImageViewByUrl(small_imge.getContext(),small_url);
//		 }
//		ImageCacheManager.ContextCache.revokeImageViewByUrl(img.getContext(),big_url);
//		super.onDetach();
//	}
	
//	@Override
//	public void onDestroyView() {
////		if(img != null) {
////			if(img.getDrawable() != null) {
////				if (img.getDrawable() instanceof BitmapDrawable) {
////				    BitmapDrawable bitmapDrawable = (BitmapDrawable) img.getDrawable();
////				    Bitmap bitmap = bitmapDrawable.getBitmap();
////				    bitmap.recycle();
////				    bitmapDrawable.setCallback(null);
////				}
////			}
////			img.clear();
////			img = null;
////		}
////		if(photo_1 != null) {
////			if(photo_1.imageViewBitmap != null && photo_1.imageViewBitmap.isRecycled() == false)
////			{
////				photo_1.imageViewBitmap.recycle();
////				photo_1.imageViewBitmap = null;
////				photo_1 = null;
////			}
////		}
////		System.gc();
//		super.onDestroyView();
//	}
	
	public void onDestroy() {
	    Log.v(TAG, "-------------onDestroy()------------------");
	    super.onDestroy();
	    
	};
//	AnimationImageRun photo_1;
//	private void shootImageRunner(String photoUrl,ImageView img,final boolean sendMsg) {
//		photo_1 = new AnimationImageRun(mHandler, photoUrl, 0);
//		photo_1.SetOnImageRunListener(new ImageRun.OnImageRunListener() {
//			
//			@Override
//			public void onLoadingFinished() {
//				if(sendMsg) {
//					Message msg = mHandler.obtainMessage(LOAD_SUCCESS);
//					msg.sendToTarget();
//				}
//			}
//			
//			@Override
//			public void onLoadingFailed() {
//				if(sendMsg) {
//					Message msg = mHandler.obtainMessage(LOAD_FAILED);
//					msg.sendToTarget();
//				}
//			}});
//		photo_1.addHostAndPath = true;
//		final Resources resources = img.getResources();
//		float dendity = resources.getDisplayMetrics().density;
////		photo_1.max_num_pixels = (int)(460*460*dendity*dendity);
//		photo_1.width = resources.getDisplayMetrics().widthPixels;
//		photo_1.height = resources.getDisplayMetrics().heightPixels;
//		photo_1.noimage = true;
//		photo_1.setImageView(img);
//		photo_1.post(null);
//	}
	
	private void shootImageRunner(String photoUrl,final ImageView img,boolean hasListener) {
		// Get singletone instance of ImageLoader
				ImageLoader imageLoader = QiupuApplication.getApplication(img.getContext()).getImageLoader();
				// Creates display image options for custom display task (all options are optional)
				DisplayImageOptions options = new DisplayImageOptions.Builder()
				           .resetViewBeforeLoading()
				           .cacheInMemory()
				           .cacheOnDisc()
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
					    	layout_process.setVisibility(View.VISIBLE);
					    }
					    @Override
					    public void onLoadingFailed(FailReason failReason) {
					    	layout_process.setVisibility(View.GONE);
					    	img.setImageResource(R.drawable.photo_default_img);
					    }
					    @Override
					    public void onLoadingComplete(Bitmap loadedImage) {
					    	layout_process.setVisibility(View.GONE);
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
//	SuperImageRun photo_1;
//	private void shootImageRunner(String photoUrl,CacheableImageView img,final boolean sendMsg) {
//		photo_1 = new SuperImageRun( photoUrl, mCache);
//		photo_1.SetOnImageRunListener(new SuperImageRun.OnImageRunListener() {
//
//			@Override
//			public void onLoadingFinished() {
//				if(sendMsg) {
//					Message msg = mHandler.obtainMessage(LOAD_SUCCESS);
//					msg.sendToTarget();
//				}
//			}
//
//			@Override
//			public void onLoadingFailed() {
//				if(sendMsg) {
//					Message msg = mHandler.obtainMessage(LOAD_FAILED);
//					msg.sendToTarget();
//				}
//			}});
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
	
	
}
