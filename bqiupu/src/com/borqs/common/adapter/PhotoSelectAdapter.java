package com.borqs.common.adapter;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.borqs.common.util.FileUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;

public class PhotoSelectAdapter extends CursorAdapter {
	private static String TAG = "PhotoSelectAdapter";
	LayoutInflater mInflater;
	int mPreferredImageSize;
	Context context;

	public PhotoSelectAdapter(Context context, Cursor c, int image_width) {
		super(context, c);
		mInflater = LayoutInflater.from(context);
		this.mPreferredImageSize = image_width;
		this.context = context;
	}

	class ViewHolder {
		public ImageView image_cover;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mInflater.inflate(R.layout.photo_select_item, null);
		ImageView image_cover = (ImageView) view
				.findViewById(R.id.image_cover);
		image_cover.setLayoutParams(new RelativeLayout.LayoutParams(
				mPreferredImageSize, mPreferredImageSize));
		image_cover.setImageResource(R.drawable.photo_default_img);
		image_cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
		return view;
	}

//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		View view = super.getView(position, convertView, parent);
//		ImageView image_cover = (ImageView) view.findViewById(R.id.image_cover);
//		final View view_cover =  view.findViewById(R.id.view_cover);
//		view_cover.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				view_cover.setBackgroundResource(R.drawable.list_selected_holo);
//				
//			}
//		});
//		image_cover.setLayoutParams(new RelativeLayout.LayoutParams(
//				mPreferredImageSize, mPreferredImageSize));
//		image_cover.setImageResource(R.drawable.photo_default_img);
//		image_cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
//		return view;
//	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (cursor != null && cursor.getCount() > 0) {
//			final View view_cover =  view.findViewById(R.id.view_cover);
//			view_cover.setOnClickListener(new View.OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					view_cover.setBackgroundResource(R.drawable.list_selected_holo);
//					
//				}
//			});
			
			
			final ImageView image_cover = (ImageView) view
					.findViewById(R.id.image_cover);
//			image_cover.setLayoutParams(new RelativeLayout.LayoutParams(
//					mPreferredImageSize, mPreferredImageSize));
			image_cover.setImageResource(R.drawable.photo_default_img);
//			image_cover.setScaleType(ImageView.ScaleType.CENTER_CROP);

			final String filePath = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
			image_cover.setTag(filePath);
			QiupuORM.sWorker.post(new Runnable() {

				@Override
				public void run() {
					if (!filePath.equals(image_cover.getTag())) {
						Log.v(TAG, "image_cover is gone ");
					}else {
						BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.inJustDecodeBounds = true;
						BitmapFactory.decodeFile(filePath, opts);
						
						opts.inSampleSize = FileUtils.computeSampleSize(opts, -1,
								mPreferredImageSize * mPreferredImageSize);
						opts.inJustDecodeBounds = false;
						final Bitmap photo = BitmapFactory.decodeFile(filePath,
								opts);
						image_cover.post(new Runnable() {
							
							@Override
							public void run() {
								image_cover.setImageBitmap(photo);
							}
						});
					}
				}
			});

		}

	}

}
