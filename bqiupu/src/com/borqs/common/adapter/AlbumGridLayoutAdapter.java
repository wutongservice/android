package com.borqs.common.adapter;

import java.util.ArrayList;

import twitter4j.QiupuAlbum;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class AlbumGridLayoutAdapter extends BaseAdapter {
	private static final String TAG = AlbumGridLayoutAdapter.class.getSimpleName();
	private final int COUNT = 6;
    private final int mWidth;
    private ArrayList<QiupuAlbum> mLists = new ArrayList<QiupuAlbum>();
    LayoutInflater mInflater;
    public AlbumGridLayoutAdapter(Context context,int width,ArrayList<QiupuAlbum> lists) {
        super();
        mWidth = width;
        mLists.clear();
        mLists.addAll(lists);
        mInflater = LayoutInflater.from(context);
    }

    public void alertData(ArrayList<QiupuAlbum> list) {
    	if(list == null) {
    		Log.d(TAG, "alertdata list is null");
    		return ;
    	}
    	mLists.clear();
    	mLists.addAll(list);
    	notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return mLists.size();
    }

    @Override
    public Object getItem(int position) {
        return mLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    class ViewHolder {
    	public ImageView image_cover;
    	public TextView badge;
    	public TextView title;
//    	public TextView number;
	}

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
    	ViewHolder viewHolder;
    	QiupuAlbum album = mLists.get(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.photos_album_grid_item, null);
			viewHolder = new ViewHolder();
			viewHolder.image_cover = (ImageView) convertView
					.findViewById(R.id.image_cover);
			viewHolder.title = (TextView) convertView
					.findViewById(R.id.title);
//			viewHolder.number = (TextView) convertView
//					.findViewById(R.id.number);
			viewHolder.badge = (TextView) convertView
					.findViewById(R.id.badge);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
//		viewHolder.number.setText("("+String.valueOf(album.photo_count)+")");
		viewHolder.title.setText(album.title+"("+String.valueOf(album.photo_count)+")");
		if(TextUtils.isEmpty(album.album_cover_photo_middle)) {
			viewHolder.image_cover.setImageResource(R.drawable.ic_menu_gallery);
		}else {
		    viewHolder.image_cover.setImageBitmap(null);
			initImageUI(album.album_cover_photo_middle, viewHolder.image_cover);// Load image into ImageView
		}
        return convertView;
    }
    
//    private void initImageUI(String image_url,ImageView imageView)
//    {
//    	ImageRun imagerun = new ImageRun(null, image_url, 0);
//    	imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
//    	imagerun.noimage = true;
//    	imagerun.addHostAndPath = true;
//    	imagerun.setImageView(imageView);    
//    	imagerun.post(null);
//    }
    private void initImageUI(String image_url,final ImageView imageView)
	{
    	// Get singletone instance of ImageLoader
		ImageLoader imageLoader = QiupuApplication.getApplication(imageView.getContext()).getImageLoader();
		// Creates display image options for custom display task (all options are optional)
		DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.photo_default_img)
    		.resetViewBeforeLoading()
    		.cacheInMemory()
    		.cacheOnDisc()
    		.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
    		.bitmapConfig(Bitmap.Config.RGB_565)
//    		.delayBeforeLoading(1000)
//    		.displayer(new RoundedBitmapDisplayer(20))
    		.build();
    	// Load and display image asynchronously
    	imageLoader.displayImage(image_url, imageView,options, new ImageLoadingListener() {
    		@Override
    		public void onLoadingStarted() {
    		}
		    @Override
		    public void onLoadingFailed(FailReason failReason) {
		    	imageView.setImageResource(R.drawable.photo_default_img);
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
