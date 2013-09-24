package com.borqs.qiupu.ui.bpc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.QiupuComposeActivity.FinishActivityListener;
import com.borqs.qiupu.util.FileUtils;
import com.borqs.qiupu.util.ToastUtil;

public class PhotoProcessActivity extends BasicActivity implements FinishActivityListener {
	public static final String TAG = "PhotoProcessActivity";
	public static final int ORIGINAL_WIDTH = 2048;
	public static final int MAX_WIDTH = 1024;
	public static final int MIN_WIDTH =600; 
	public static final int QUALITY = 85; 
	private static final int TYPE_ORIGINAL = 0; 
	private static final int TYPE_HD = 1; 
	private static final int TYPE_NORMAL = 2;
	public static final String IMG_COMPRESS = "img_compress_";

	public static final String IMAGE_UNSPECIFIED = "image/*";
	ImageView photo_preview = null;
	ImageView rigth_rotate = null;
	ImageView left_rotate = null;
	
	Bitmap scale_bitmap_normal = null;
//	Bitmap rotate_bitmap_normal = null;
	
	TextView originalbutton;
	TextView highbutton;
	TextView lowbutton;
	TextView quality_title;
//	String photo_path = "/storage/sdcard0/DCIM/Camera/IMG_20120324_140223.jpg";
	private String photo_path = null;
//	private boolean isHighQuality = true; 
	private int defalut_type = TYPE_HD;
	private String title;
	private File low_file;
	private boolean bmpIsChanged = false;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_process);

		showLeftActionBtn(false);
		showMiddleActionBtn(false);
		showRightActionBtn(true);
//		if(isWifiActive()) {
//			defalut_type = TYPE_ORIGINAL;
//		}
		photo_path = getIntent().getStringExtra("photo_path");
        title = getIntent().getStringExtra("title");
		low_file = new File(QiupuHelper.getTmpCachePath() + "/"+ title + ".jpg");
		overrideRightActionBtn(R.drawable.actionbar_icon_commit_normal, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				synchronized(lockObj) {
					if(inloading) {
//						ToastUtil.showShortToast(PhotoProcessActivity.this, mHandler, R.string.string_in_processing);
//			            return;
						low_file = new File(photo_path);
					} else {
					    if(scale_bitmap_normal != null && !scale_bitmap_normal.isRecycled()) {
		                    scale_bitmap_normal.recycle();
		                    scale_bitmap_normal = null;
		                }
					    if(low_file.length() == 0) {
					        low_file = new File(photo_path);
					    }
					}
				}
				Intent data = new Intent();
				data.putExtra("file", low_file);
				data.putExtra("title", title);
				setResult(RESULT_OK, data);
//				if(scale_bitmap_normal != null && !scale_bitmap_normal.isRecycled()) {
//					scale_bitmap_normal.recycle();
//					scale_bitmap_normal = null;
//				}
//				if(rotate_bitmap_normal != null && !rotate_bitmap_normal.isRecycled()) {
//					rotate_bitmap_normal.recycle();
//					rotate_bitmap_normal = null;
//				}
				finish();
				
			}
		});
		
		if(TextUtils.isEmpty(photo_path)) {
			finish();
		}
		photo_preview = (ImageView) findViewById(R.id.photo_preview);
		left_rotate = (ImageView) findViewById(R.id.photo_preview_left_rotate);
		rigth_rotate = (ImageView) findViewById(R.id.photo_preview_rigth_rotate);
		originalbutton = (TextView) findViewById(R.id.photo_preview_select_quality_originalbutton);
		highbutton = (TextView) findViewById(R.id.photo_preview_select_quality_highbutton);
		lowbutton = (TextView) findViewById(R.id.photo_preview_select_quality_lowbutton);
		quality_title = (TextView) findViewById(R.id.photo_preview_select_quality_title);
		originalbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(defalut_type != TYPE_ORIGINAL) {
					setLoading();
					defalut_type = TYPE_ORIGINAL;
					originalbutton.setSelected(true);
            		highbutton.setSelected(false);
            		lowbutton.setSelected(false);
            		originalbutton.setTextColor(getResources().getColor(android.R.color.white));
            		highbutton.setTextColor(getResources().getColor(android.R.color.black));
            		lowbutton.setTextColor(getResources().getColor(android.R.color.black));
//					setImageQuality(photo_path);
            		QiupuORM.sWorker.post(new Runnable() {
						public void run() {
							
							try {
								int result  = FileUtils.copyfile(photo_path, low_file.getPath());
								if(result == 1) {
									mHandler.sendEmptyMessage(PROCESS_END);
//									quality_title.setText(String.format(getString(R.string.app_info_version_size_title),
//											String.valueOf(low_file.length()/1024)+"K"));
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
				}
			}
		});
		highbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(defalut_type != TYPE_HD) {
					defalut_type = TYPE_HD;
					setImageQuality(photo_path);
				}
			}
		});
		lowbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(defalut_type != TYPE_NORMAL) {
					defalut_type = TYPE_NORMAL;
					setImageQuality(photo_path);
				}
			}
		});
		setImageQuality(photo_path);
		left_rotate.setOnClickListener(leftRotateClickListener);
		rigth_rotate.setOnClickListener(rightRotateClickListener);
        
//		photo_preview.setImageBitmap(scale_bitmap_normal);
		
		QiupuComposeActivity.setFinishListener(this);
	}
	
	Object lockObj = new Object();
	boolean inloading;
	public void setImageQuality(final String path) {
		setLoading();
	    bmpIsChanged = false;
//		mHandler.post(new Runnable() {
//            public void run() {
            	int max_width = 0;
            	if(defalut_type == TYPE_ORIGINAL) {
            		max_width = ORIGINAL_WIDTH;
            		originalbutton.setSelected(true);
            		highbutton.setSelected(false);
            		lowbutton.setSelected(false);
            		originalbutton.setTextColor(getResources().getColor(android.R.color.white));
            		highbutton.setTextColor(getResources().getColor(android.R.color.black));
            		lowbutton.setTextColor(getResources().getColor(android.R.color.black));
            	}else if(defalut_type == TYPE_HD) {
            		max_width = MAX_WIDTH;
            		originalbutton.setSelected(false);
            		highbutton.setSelected(true);
            		lowbutton.setSelected(false);
            		originalbutton.setTextColor(getResources().getColor(android.R.color.black));
            		highbutton.setTextColor(getResources().getColor(android.R.color.white));
            		lowbutton.setTextColor(getResources().getColor(android.R.color.black));
            	}else {
            		max_width = MIN_WIDTH;
            		originalbutton.setSelected(false);
            		highbutton.setSelected(false);
            		lowbutton.setSelected(true);
            		originalbutton.setTextColor(getResources().getColor(android.R.color.black));
            		highbutton.setTextColor(getResources().getColor(android.R.color.black));
            		lowbutton.setTextColor(getResources().getColor(android.R.color.white));
            	}
            	final int MAX_W = max_width;
            	QiupuORM.sWorker.post(new Runnable() {
					
					@Override
					public void run() {
						
						BitmapFactory.Options localOptions_normal = new BitmapFactory.Options();
						localOptions_normal.inJustDecodeBounds = true;
						BitmapFactory.decodeFile(path,  localOptions_normal);
						if(MAX_W < Math.max(localOptions_normal.outWidth, localOptions_normal.outHeight)) {
							int widthScale_normal = (int)Math.ceil(localOptions_normal.outWidth/MAX_W);
							int heightScale_normal = (int)Math.ceil(localOptions_normal.outHeight/MAX_W);
							if ((widthScale_normal > 1) || (heightScale_normal > 1))
								if (widthScale_normal <= heightScale_normal) {
									
									localOptions_normal.inSampleSize = heightScale_normal;
								}
								else {
									localOptions_normal.inSampleSize = widthScale_normal;
								}
							localOptions_normal.inJustDecodeBounds = false;
							scale_bitmap_normal = BitmapFactory.decodeFile(path,  localOptions_normal);
							if (widthScale_normal <= heightScale_normal) {
								scale_bitmap_normal = Bitmap.createScaledBitmap(scale_bitmap_normal, MAX_W*localOptions_normal.outWidth/localOptions_normal.outHeight, MAX_W, true);
							}else {
								scale_bitmap_normal = Bitmap.createScaledBitmap(scale_bitmap_normal,  MAX_W,MAX_W*localOptions_normal.outHeight/localOptions_normal.outWidth, true);
							}
							bmpIsChanged = true;
						} else {
							scale_bitmap_normal = BitmapFactory.decodeFile(path,  null);
						}
						
						ExifInterface exifInterface;
						try {
							exifInterface = new ExifInterface(photo_path);
							int result = exifInterface.getAttributeInt(
									ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
							int rotate = 0;
							switch(result) {
							case ExifInterface.ORIENTATION_ROTATE_90:
								rotate = 90;
								break;
							case ExifInterface.ORIENTATION_ROTATE_180:
								rotate = 180;
								break;
							case ExifInterface.ORIENTATION_ROTATE_270:
								rotate = 270;
								break;
							default:
								break;
							}
							if(rotate > 0) {
								Matrix matrix = new Matrix();
								matrix.setRotate(rotate);
								scale_bitmap_normal = Bitmap.createBitmap(
										scale_bitmap_normal, 0, 0, scale_bitmap_normal.getWidth(),scale_bitmap_normal.getHeight(), matrix, true);
								bmpIsChanged = true;
							}
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
//	        bitmap_normal.recycle();
						
						
						try {
							if(bmpIsChanged) {
								FileOutputStream fo2 = new FileOutputStream(low_file);
								if(scale_bitmap_normal.compress(Bitmap.CompressFormat.JPEG, QUALITY, fo2)) {
									fo2.flush();
									fo2.close();
								}
							}else {
								FileUtils.copyfile(photo_path, low_file.getPath());
							}
							
							mHandler.sendEmptyMessage(PROCESS_END);
//            		quality_title.setText(getString(R.string.app_info_version_size_title)+low_file.length()/1024+"k");
//							quality_title.setText(String.format(getString(R.string.app_info_version_size_title),
//									String.valueOf(low_file.length()/1024)+"K"));
//				Toast.makeText(TestImageCropActivity.this, String.valueOf(low_file.length()/1024), Toast.LENGTH_SHORT).show();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//            }
//        });
						catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
	        
	        
	}

//	File low_file = new File(QiupuHelper.getTmpCachePath() + "/"+ IMG_COMPRESS + System.currentTimeMillis() + ".jpg");
	
	View.OnClickListener leftRotateClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			setLoading();
			QiupuORM.sWorker.post(new Runnable() {

				@Override
				public void run() {
					rotatePhoto(-90);
					mHandler.sendEmptyMessage(PROCESS_END);
				}
				
			});
		}
	};
	
	View.OnClickListener rightRotateClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			setLoading();
			QiupuORM.sWorker.post(new Runnable() {

				@Override
				public void run() {
					rotatePhoto(90);
					mHandler.sendEmptyMessage(PROCESS_END);
				}
				
			});
		}
	};
	
	private void setLoading() {
		synchronized(lockObj) {
			if(inloading) return;
			inloading = true;
			quality_title.setText(String.format(getString(R.string.app_info_version_size_title),getString(R.string.string_in_calculating)));
		}
	}
	
	private void rotatePhoto(final int rotate) {
//		mHandler.post(new Runnable() {
//            public void run() {
            	Matrix matrix = new Matrix();
        		matrix.setRotate(rotate);
        		scale_bitmap_normal=Bitmap.createBitmap(scale_bitmap_normal, 0, 0,
        				scale_bitmap_normal.getWidth(), scale_bitmap_normal.getHeight(), matrix, true);
        		
//        		photo_preview.setImageBitmap(scale_bitmap_normal);
        		try {
                	
        			FileOutputStream fo2 = new FileOutputStream(low_file);
        			if(scale_bitmap_normal.compress(Bitmap.CompressFormat.JPEG, 85, fo2)) {
        				fo2.flush();
        				fo2.close();
        			}
        		} catch (FileNotFoundException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
//            }
//            
//		});
		
	}
	
    private final static int PROCESS_END = 1;
    
	private class MainHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case PROCESS_END:
				quality_title.setText(String.format(getString(R.string.app_info_version_size_title),
						String.valueOf(low_file.length()/1024)+"K"));
				photo_preview.setImageBitmap(scale_bitmap_normal);
				synchronized(lockObj) {
					inloading = false;
				}
				break;
				default:
					break;
			}
		}
	}

	@Override
	protected void createHandler() {
		mHandler = new MainHandler();
	}


    @Override
    public void finishPhotoActivity() {
        finish();
    }

}