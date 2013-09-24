package com.borqs.qiupu.cache;

import java.io.File;

import android.widget.ImageSwitcher;
import twitter4j.threadpool.QueuedThreadPool;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.service.QiupuService;

public class ImageRun implements Runnable
{
    private static final String TAG = "Qiupu.ImageRun";
	protected String url;
    protected View imgView;
    boolean fromlocal;
    int     highPriority;    
    Handler pHandler;
    
    public int default_image_index;
    
//    public final static int MAX_NUM_PIXELS = 12*1024*8;
//    public static int max_size             = 120*1024*8;
    public static int max_size             = 120*1024;
    public int app_profile_max_size = 100*1024;
    public int     width=120;
    public int     height=120;
    public boolean isRoate=false;
    public boolean noimage;
    public boolean forappprofile=false;
    public boolean need_scale;
    public boolean scalebaseHeight;
    public boolean forStreamPhoto;
    public boolean forceweb;//if true, every time get the image from web
    public boolean addHostAndPath=true;//need have a rule to name the save file
    public boolean setRoundAngle =false;
    public Bitmap imageViewBitmap;
    //public boolean need_compress = false; //need compress image as soon as get Image from web server only for (link photo,video photo)
    private static float scaleD = -1.0f;
    private OnImageRunListener mImageRunListener;
    public boolean isSavedMode;
    
    private final float offsetValve = 0.1f;
    
    public int max_num_pixels = 12*1024*8;

    public void SetOnImageRunListener(OnImageRunListener listener) {
    	mImageRunListener = listener;
    }
    
    public ImageRun(Handler handler, String url, int highPriority)
    {
        this.url = url;
        this.highPriority = highPriority;        
        pHandler = new Handler();
    }
    public ImageRun(Handler handler, String url, boolean fromLocal, int highPriority)
    {
        this.url = url;
        fromlocal = fromLocal;
        this.highPriority = highPriority;        
        pHandler = new Handler();
    }    
    
    public void post(Runnable run )
    {
    	if(iaminCache == false && url != null)
    	{
    		//dispatch is too early, will do this when the scroll is finished
    	    getThreadPool().dispatch(this);
    	}
    }
    
    
    
    static final int ImagePoolSize=8;
    static QueuedThreadPool threadpool=null;
    public static  QueuedThreadPool getThreadPool()
	{
		synchronized(QueuedThreadPool.class)
		{
	        if(null == threadpool)
	        {
	            threadpool = new QueuedThreadPool(ImagePoolSize);
	            threadpool.setName("Image--Thread--Pool");
	            try 
	            {
	            	threadpool.start();
	            } catch (Exception e) {}
	            
	            Runtime.getRuntime().addShutdownHook(new Thread(TAG)
	            {
	                public void run() 
	                {
	                    if(threadpool != null)
	                    {
	                        try {
	                            threadpool.stop();
	                        } catch (Exception e) {}
	                    }
	                }
	            });
	        }
		}		
	    return threadpool;
	}
	 
    boolean iaminCache = false;

    //just for screen shot cut show, no use in other place
    public boolean setScreenImageView(ImageView view)
    {
    	boolean ret = false;
    	iaminCache = false;
    	imgView = view;
    	imgView.setTag(url);
    	
    	Bitmap cacheBmp = ImageCacheManager.ContextCache.getImageFromCache(url, imgView.getContext().getClass().getName());
    	if(cacheBmp != null)
    	{
    		iaminCache = true;
    		if(need_scale)
	        {
			     int h = (int)(width);
			     if(imgView != null)
			     {
                     if (scaleD < 0) {
                         final Resources resources = imgView.getResources();
                         scaleD = resources.getDisplayMetrics().density;
                     }

    		    	 h = (int)(width*scaleD);//imgView.getWidth();
			     }
		    	 Bitmap mBaseImage = cacheBmp;
		    	 
		    	 if(mBaseImage != null)
		    	 {
		    		 float scale = getScale(h, mBaseImage);
    		    	    
    		         if(Math.abs(scale-1.0) > offsetValve)
    		         {
    		        	 Matrix matrix = new Matrix();
    		        	 matrix.setScale(scale, scale);    		        	 
    		             mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage
    		                  .getWidth(), mBaseImage.getHeight(), matrix, true);
    		             matrix = null;
    		             
    		             if(view != null)
                             ImageCacheManager.ContextCache.putBitmapIntoMap(view.getContext(), mBaseImage, (String) view.getTag());
    		         }
    		            
    		         setImageBmp(mBaseImage);
		    	 }
		         
	        }
			else
			{
			    setImageBmp(cacheBmp);
			}
    		
    		ret = true;
    	}
    	else
    	{   
    		//get from local, if has in local,    		
    		final String localpath = QiupuHelper.isImageExistInPhone(url, addHostAndPath);
    		if(localpath != null)
    		{
    		    if(forceweb == false)//no need force get image from web
    		    {
        			iaminCache = true;
        			//load from local
        			try{      
        				Bitmap tmp = null;
        				if(new File(localpath).length() < max_size || forappprofile == true)
                        {
        					tmp = BitmapFactory.decodeFile(localpath);
            			}else if(new File(localpath).length() >= max_size) {
            				BitmapFactory.Options opts = new BitmapFactory.Options();
            		        opts.inJustDecodeBounds = true;
            		        BitmapFactory.decodeFile(localpath, opts);
            		        
//            		        opts.inSampleSize = computeSampleSize(opts, -1,max_num_pixels);
//            		        opts.inSampleSize = computeSampleSize(width, height,
//            		        		UNCONSTRAINED, BACKUP_PIXEL_COUNT);
            		        if(width > height) {
            		        	opts.inSampleSize = computeSampleSizeLarger(opts.outWidth, opts.outHeight, height);
            		        }else {
            		        	opts.inSampleSize = computeSampleSizeLarger(opts.outWidth, opts.outHeight, width);
            		        }
            		        opts.inJustDecodeBounds = false;
            		        tmp = BitmapFactory.decodeFile(localpath, opts);
            			}
        				if(tmp != null)
        				{
        					boolean cached = ImageCacheManager.instance().addCache(url, tmp);
        					if(need_scale)
        					{
        						int h = (int)(width);
        						if(imgView != null)
        						{
        							if (scaleD < 0) {
        								final Resources resources = imgView.getResources();
        								scaleD = resources.getDisplayMetrics().density;
        							}
        							h = (int)(width*scaleD);//imgView.getWidth();
        						}
        						Bitmap mBaseImage = tmp;
        						
        						//Log.d(TAG, "image ="+mBaseImage);
        						float scale = getScale(h, mBaseImage);
        						
        						if(Math.abs(scale-1.0) > offsetValve)
        						{
        							Matrix matrix = new Matrix();	  
        							matrix.setScale(scale, scale);
        							mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage
        									.getWidth(), mBaseImage.getHeight(), matrix, true);
        							
        							matrix = null;
        							
        							if(view != null)
                                        ImageCacheManager.ContextCache.putBitmapIntoMap(view.getContext(), mBaseImage, (String) view.getTag());
        							
        							if(cached == false)
        							{
        								tmp.recycle();
        								tmp = null;
        							}    	
        						} 		        		        
        						
        						setImageBmp(mBaseImage);
        					}
        					else
        					{
        						setImageBmp(tmp);
        					}
        				}else {
//        					if(mImageRunListener != null) { 
//        		    			mImageRunListener.onLoadingFailed();
//        		    		}
        					deleteFile(localpath);
        					
        				}
        			}catch(Exception ne)
        			{
        				Log.v(TAG, "run dispose() setScreenImageView...");
        				dispose();
        				//should we remove the file, maybe the file is bad
        				//
        				deleteFile(localpath);
        				if(mImageRunListener != null) { 
    		    			mImageRunListener.onLoadingFailed();
    		    		}
        				Log.d(TAG, "exception=+"+ne.getMessage());
        			}
        			
    		    }
    		    else//TODO a bug for forceweb
    		    {
    		    	dispose();
    		    }   
    		    
    		    ret = true;
    		}
    		else
    		{
    			iaminCache = false;
	    		//set as no image firstly, this will remove the pre-image
	    		if(noimage == false)
    		    {
    		    	final int res;
    		    	if(default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_USER)
    		    	{
    		    		res = R.drawable.default_user_icon;
    		    	}
    		    	else if(default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_BOOK)
    		    	{
    		    		res = R.drawable.default_book;
    		    	}
    		    	else if(default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_Music)
    		    	{
    		    		res = R.drawable.music_default;
    		    	}
    		    	else if(default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_SCREENSHOT)
    		    	{
    		    		res = R.drawable.photo_transparent;
    		    	} else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_APK) {
                        res = R.drawable.default_app_icon;
                    } else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_LINK) {
                        res = R.drawable.list_public;
                    } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_GREEN) {
                        res = R.drawable.wutong_screen_bg_green;
                    } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_BLUE) {
                        res = R.drawable.wutong_screen_bg_blue;
                    } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_RED) {
                        res = R.drawable.wutong_screen_bg_red;
                    } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_YELLOW) {
                        res = R.drawable.wutong_screen_bg_yellow;
                    } else {
                        res = R.drawable.photo_transparent;
                    }
    		    	
    		    	if(need_scale)
    		    	{
    		    	    int h = (int)(width);
   	    			    if(imgView != null)
   	    			    {
	   	    			    if (scaleD < 0) {
	   	                        final Resources resources = imgView.getResources();
	   	                        scaleD = resources.getDisplayMetrics().density;
	   	                    }
   	        		        h = (int)(width*scaleD);//imgView.getWidth();
   	    			    }

    		    	    Bitmap tmp = BitmapFactory.decodeResource(QiupuService.getQiupuService().getResources(), res);
    		    	    
    		    	    Bitmap mBaseImage = tmp;
    		    	  
    		    	    //Log.d(TAG, "image h="+ h+ "= height="+mBaseImage.getHeight() + " width="+mBaseImage.getWidth());
    		    	    float scale = getScale(h, mBaseImage);
    		    	    
    		    	    if(Math.abs(scale-1.0) > offsetValve)
	        		    {
	        		    	Matrix matrix = new Matrix();	  
	        		    
    		                matrix.setScale(scale, scale);
    		                if(isRoate&&(tmp.getWidth()>tmp.getHeight())) {
      		                	matrix.postRotate(90);
      		                }
    		                mBaseImage = Bitmap.createBitmap(tmp, 0, 0, tmp
    		                  .getWidth(), tmp.getHeight(), matrix, true);
    		                matrix = null;
    		                
    		                //recycle the old icon bitmap
    		                tmp.recycle();
    		                tmp = null;
    		                
    		                if(view != null)
                                ImageCacheManager.ContextCache.putBitmapIntoMap(view.getContext(), mBaseImage, (String) view.getTag());
	        		    }
    		    	    
    		    	    setImageBmp(mBaseImage);
    		    	}
    		    	else
    		    	{
    		            setImageBmp(res);
    		    	}
    		    	
    		    }
	    		
	    		if(url != null)
	    		{
	    		    synchronized(/*imageurl*/view)
	    		    {
	    		    	view.setTag(url);
			    	    //imageurl.put(view,  url);
			    	    //Log.d(TAG, "new mapcount="+imageurl.size() + " image="+url + " view="+imgView);
	    		    }				    	
	    		}
    		}
    		
    		ret = false;
    	}
    	return ret;
    }
    public float getScale(int goal, Bitmap mBaseImage, boolean iambigpicture)
    {
    	if(iambigpicture)
    	{
    		return 1.0f;
    	}
    	else
    	{
    		 float scale = 1.0f;
        	 final int minValue = Math.min(mBaseImage.getHeight(), mBaseImage.getWidth());
        	 if(forStreamPhoto)
        	 {
        		scale = (float)goal/(float)minValue;
        	 }
        	 else
        	 {
    	    	 if(scalebaseHeight)
    	    	 {
    	    		scale = (float)goal/(float)mBaseImage.getHeight(); 
    	    	 }
    	    	 else
    	    	 {
    	    		scale = (float)goal/(float)mBaseImage.getWidth();
    	    	 }
        	 }
        	 
        	 return scale;
    	}
    }
    
    public float getScale(int goal, Bitmap mBaseImage)
    {
    	return getScale(goal, mBaseImage, false);
    }
    
    public boolean setImageView(View view)
    {
    	boolean ret = false;
    	iaminCache = false;
    	imgView = view;
    	imgView.setTag(url);
    	final Bitmap cacheBmp = ImageCacheManager.ContextCache.getImageFromCache(url, imgView.getContext().getClass().getName());
    	if(cacheBmp != null && cacheBmp.getWidth() > 0)
    	{
    		iaminCache = true;
    		pHandler.post( new Runnable()
        	{
        		public void run()
        		{
                    onThumbnailShow(cacheBmp);

        			if(need_scale)
   			        {
	    			     int h = (int)(width);
	    			     if(imgView != null)
	    			     {
                             if (scaleD < 0) {
                                 final Resources resources = imgView.getResources();
                                 scaleD = resources.getDisplayMetrics().density;
                             }

	        		    	 h = (int)(width*scaleD);//imgView.getWidth();
	    			     }
        		    	 Bitmap mBaseImage = cacheBmp;
        		    	 
        		    	 if(mBaseImage != null && !mBaseImage.isRecycled() && mBaseImage.getWidth() > 0)
        		    	 {
        		    		 float scale = getScale(h, mBaseImage);
	        		    	    
        		    	     if(Math.abs(scale-1.0) > offsetValve)
     	        		     {
     	        		    	Matrix matrix = new Matrix();	  
     	        		    
         		                matrix.setScale(scale, scale);
        		                if(isRoate&&(mBaseImage.getWidth()>mBaseImage.getHeight())) {
         		                	matrix.postRotate(90);
         		                }
         		                mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage
         		                  .getWidth(), mBaseImage.getHeight(), matrix, true);
         		                matrix = null;
         		                
         		                //NO need recycle this data
         		                //should recycle the cacheBmp
         		                //if this bitmap already in another activity, we should not recycle this data
         		                //
         		                if(cacheBmp.getWidth() > ImageCacheManager.maxCacheWidth)
         		                {   
         		                    if(false == ImageCacheManager.ContextCache.hasCachedBMPInContext(imgView.getContext(), cacheBmp))
         		                    {
         		                	    cacheBmp.recycle();
         		                    }
         		                }
         		                
         		                if(imgView != null)
                                     ImageCacheManager.ContextCache.putBitmapIntoMap(imgView.getContext(), mBaseImage, (String) imgView.getTag());
     	        		     }
	        		            
	        		         setImageBmp(mBaseImage);
        		    	 } else {        		         
        		    	     Log.i(TAG, "setImageView, got a null or recycled image.");
        		    	 }
   			        }
        			else
        			{
        			    setImageBmp(cacheBmp);
        			}
        			
        		}
        	});
    		
    		ret = true;
    	}
    	else
    	{   
    		//get from local, if has in local,
    		final String localpath = QiupuHelper.isImageExistInPhone(url, addHostAndPath);
    		if(localpath != null)
    		{
    		    if(forceweb == false)//no need force get image from web
    		    {
        			iaminCache = true;
        			//load from local
        			pHandler.post( new Runnable()
                	{
                		public void run()
                		{    
            		        try{ 
            		        	boolean iambigpicture = false;
            		        	Bitmap tmp = null;
                                File imageFile = new File(localpath);
                				if(imageFile.length() < max_size || forappprofile == true)
                                {
                					tmp = BitmapFactory.decodeFile(localpath);
                                    if (null == tmp) {
                                        imageFile.delete();
                                        checkAndSeetDefault();
                                    }
                    			}else if(new File(localpath).length() >= max_size) {
                    				BitmapFactory.Options opts = new BitmapFactory.Options();
                    		        opts.inJustDecodeBounds = true;
                    		        BitmapFactory.decodeFile(localpath, opts);
                    		        
//                    		        opts.inSampleSize = computeSampleSize(opts, -1,max_num_pixels);
//                    		        opts.inSampleSize = computeSampleSize(width, height,
//                    		                UNCONSTRAINED, BACKUP_PIXEL_COUNT);
                    		        if(width > height) {
                    		        	opts.inSampleSize = computeSampleSizeLarger(opts.outWidth, opts.outHeight, height);
                    		        }else {
                    		        	opts.inSampleSize = computeSampleSizeLarger(opts.outWidth, opts.outHeight, width);
                    		        }
                    		        opts.inJustDecodeBounds = false;
                    		        try{
                    		        	tmp = BitmapFactory.decodeFile(localpath, opts);
                    		        }catch (OutOfMemoryError oof) {
										// TODO: handle exception
									}
                    		        iambigpicture = true;
                    			}
    			    			 if(tmp != null)
    			    			 {
                                     if (!iambigpicture) {
                                         onThumbnailShow(tmp);
                                     }

//    			    			     Log.v(TAG, "---rotateBitmap--------2222---------");
//    			    			     tmp = rotateBitmap(tmp);
    			    				 boolean cached = ImageCacheManager.instance().addCache(url, tmp);
    			    			     if(need_scale)
    			    			     {
	    			    			     int h = (int)(width);
	    			    			     if(imgView != null)
	    			    			     {
                                             if (scaleD < 0) {
                                                 final Resources resources = imgView.getResources();
                                                 scaleD = resources.getDisplayMetrics().density;
                                             }
	    			        		    	 h = (int)(width*scaleD);//imgView.getWidth();
	    			    			     }
		 		        		    	 Bitmap scaleImage = tmp;
		 		        		    	  
		 		        		    	 //Log.d(TAG, "image ="+mBaseImage);
		 		        		    	 float scale = getScale(h, scaleImage, iambigpicture);
		                                 
		 		        		    	 if(Math.abs(scale-1.0) > offsetValve)
		 	     	        		     {
		 	     	        		    	Matrix matrix = new Matrix();	  
		 	     	        		    
		 	         		                matrix.setScale(scale, scale);
		 	       		                    if(isRoate&&(scaleImage.getWidth()>scaleImage.getHeight())) {
		 	         		                	matrix.postRotate(90);
		 	         		                }
                                               scaleImage = Bitmap.createBitmap(scaleImage, 0, 0, scaleImage
		 	         		                  .getWidth(), scaleImage.getHeight(), matrix, true);
		 	         		                matrix = null;

    		 		        		        if(cached == false)
    		 		        		        {
    		 		        		            tmp.recycle();
    		 		        		            tmp = null;
    		 		        		        }  
    		 		        		        
    		 		        		        //remember for future recycle
    		 		        		        if(imgView != null)
                                                 ImageCacheManager.ContextCache.putBitmapIntoMap(imgView.getContext(), scaleImage, (String) imgView.getTag());
		 	     	        		     }    		 		        		        	 		        		        
		 		        		         setImageBmp(scaleImage);
    			    			     }
    			    			     else
    			    			     {
                                         if (false == cached && null != imgView) {
                                             ImageCacheManager.ContextCache.putBitmapIntoMap(imgView.getContext(), tmp, (String) imgView.getTag());
                                         }

    			    			         setImageBmp(tmp);
    			    			     }
//    			    			 }else {
//    			    				 if(mImageRunListener != null) { 
//    		        		    			mImageRunListener.onLoadingFailed();
//    		        		    		}
//    		        					deleteFile(localpath);
    			    			 }
    		    			 }catch(Exception ne)
    		    			 {
    		    				 Log.v(TAG, "run dispose() catch exception");
    		    				 dispose();
    		    			     //should we remove the file, maybe the file is bad
    		    			     //
    	        				 deleteFile(localpath);
    	        				 if(mImageRunListener != null) { 
		        		    			mImageRunListener.onLoadingFailed();
		        		    		}
    		    				 Log.d(TAG, "exception=+"+ne.getMessage());
    		    			 }
		    			 
                		}
                	});
    		    }
    		    else//TODO a bug for forceweb
    		    {
    		    	Log.v(TAG, "run dispose() a bug for forceweb");
    		    	dispose();
    		    }   
    		    
    		    ret = true;
    		}
    		else
    		{
    			iaminCache = false;
	    		//set as no image firstly, this will remove the pre-image
	    		pHandler.post( new Runnable()
	        	{
	        		public void run()
	        		{
//                        checkAndSeetDefault();
	        		    if(noimage == false)
	        		    {
	        		    	final int res;
	        		    	if(default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_USER)
	        		    	{
	        		    		res = R.drawable.default_user_icon;
	        		    	}
	        		    	else if(default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_BOOK)
            		    	{
	        		    		res = R.drawable.default_book;
            		    	}else if(default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_SCREENSHOT)
                            {
                                res = R.drawable.photo_transparent;
                            }
	        		    	else if(default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_Music)
            		    	{
	        		    		res = R.drawable.music_default;
            		    	} else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_APK) {
                                res = R.drawable.default_app_icon;
                            } else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_LINK) {
                                res = R.drawable.list_public;
                            } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_GREEN) {
                                res = R.drawable.wutong_screen_bg_green;
                            } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_BLUE) {
                                res = R.drawable.wutong_screen_bg_blue;
                            } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_RED) {
                                res = R.drawable.wutong_screen_bg_red;
                            } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_YELLOW) {
                                res = R.drawable.wutong_screen_bg_yellow;
                            } else {
                                res = R.drawable.photo_transparent;
                            }
	        		    	
	        		    	if(need_scale)
	        		    	{
	        		    	    int h = (int)(width);
		   	    			    if(imgView != null)
		   	    			    {
		   	    			    	if (scaleD < 0) {
		   	                         final Resources resources = imgView.getResources();
		   	                         scaleD = resources.getDisplayMetrics().density;
		   	                        }
		   	    			    	
		   	        		        h = (int)(width*scaleD);//imgView.getWidth();
		   	    			    }

	        		    	    Bitmap tmp = BitmapFactory.decodeResource(QiupuService.getQiupuService().getResources(), res);
	        		    	  
	        		    	    //Log.d(TAG, "image h="+ h+ "= height="+mBaseImage.getHeight() + " width="+mBaseImage.getWidth());
	        		    	    float scale = getScale(h, tmp);
	        		    	    Bitmap mBaseImage = tmp;
	        		    	    if(Math.abs(scale-1.0) > offsetValve)
	     	        		    {
	     	        		    	Matrix matrix = new Matrix();	  
	     	        		    
	         		                matrix.setScale(scale, scale);
	        		                if(isRoate&&(tmp.getWidth()>tmp.getHeight())) {
	         		                	matrix.postRotate(90);
	         		                }
	         		                mBaseImage = Bitmap.createBitmap(tmp, 0, 0, tmp
	         		                  .getWidth(), tmp.getHeight(), matrix, true);
	         		                matrix = null;	 		
	         		                
	         		                //recycle old resource
	         		                tmp.recycle();
	         		                
	         		                if(imgView != null)
                                         ImageCacheManager.ContextCache.putBitmapIntoMap(imgView.getContext(), mBaseImage, (String) imgView.getTag());
	     	        		    }    		
	        		            
	        		            setImageBmpWithDispose(mBaseImage,false);	        		            
	        		    	}
	        		    	else
	        		    	{
	        		            setImageBmp(res);
	        		    	}
	        		    	
	        		    }
	    			}            		
	        	});
	    		
	    		if(url != null)
	    		{
	    		    synchronized(/*imageurl*/view)
	    		    {
	    		    	view.setTag(url);
			    	    //imageurl.put(view,  url);
			    	    //Log.d(TAG, "new mapcount="+imageurl.size() + " image="+url + " view="+imgView);
	    		    }				    	
	    		}
	    		ret = false;
    		}
    		
    	}
    	return ret;
    }

    private void checkAndSeetDefault() {
        if (noimage == false) {
            final int res;
            if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_USER) {
                res = R.drawable.default_user_icon;
            } else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_BOOK) {
                res = R.drawable.default_book;
            } else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_SCREENSHOT) {
                res = R.drawable.photo_transparent;
            } else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_Music) {
                res = R.drawable.music_default;
            } else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_APK) {
                res = R.drawable.default_app_icon;
            } else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_LINK) {
                res = R.drawable.list_public;
            } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_GREEN) {
                res = R.drawable.wutong_screen_bg_green;
            } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_BLUE) {
                res = R.drawable.wutong_screen_bg_blue;
            } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_RED) {
                res = R.drawable.wutong_screen_bg_red;
            } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_YELLOW) {
                res = R.drawable.wutong_screen_bg_yellow;
            } else {
                res = R.drawable.photo_transparent;
            }

            if (need_scale) {
                int h = (int) (width);
                if (imgView != null) {
                    if (scaleD < 0) {
                        final Resources resources = imgView.getResources();
                        scaleD = resources.getDisplayMetrics().density;
                    }

                    h = (int) (width * scaleD);//imgView.getWidth();
                }

                Bitmap tmp = BitmapFactory.decodeResource(QiupuService.getQiupuService().getResources(), res);

                //Log.d(TAG, "image h="+ h+ "= height="+mBaseImage.getHeight() + " width="+mBaseImage.getWidth());
                float scale = getScale(h, tmp);
                Bitmap mBaseImage = tmp;
                if (Math.abs(scale - 1.0) > offsetValve) {
                    Matrix matrix = new Matrix();

                    matrix.setScale(scale, scale);
                    if (isRoate && (tmp.getWidth() > tmp.getHeight())) {
                        matrix.postRotate(90);
                    }
                    mBaseImage = Bitmap.createBitmap(tmp, 0, 0, tmp
                            .getWidth(), tmp.getHeight(), matrix, true);
                    matrix = null;

                    //recycle old resource
                    tmp.recycle();

                    if (imgView != null)
                        ImageCacheManager.ContextCache.putBitmapIntoMap(imgView.getContext(), mBaseImage, (String) imgView.getTag());
                }

                setImageBmpWithDispose(mBaseImage, false);
            } else {
                setImageBmp(res);
            }
        }
    }
    
    Bitmap bmp  = null;
    public void run()
    {
    	//remove the pre-bmp
    	bmp = null;
    	if(imgView != null)
    	{	
    		if(fromlocal == false && isSavedMode == false)
    		{
	            bmp = QiupuHelper.getImageFromURL(imgView.getContext(), url, highPriority==0?false:true, addHostAndPath, setRoundAngle/*false*/,max_num_pixels,width,height);
    		}
    		else
    		{
    		    if(new File(url).length() < max_size)
    		    {
    			    bmp =  BitmapFactory.decodeFile(url);
    		    }else {
    		    	BitmapFactory.Options opts = new BitmapFactory.Options();
    		        opts.inJustDecodeBounds = true;
    		        BitmapFactory.decodeFile(url, opts);
    		        
//    		        opts.inSampleSize = computeSampleSize(opts, -1,max_num_pixels);
//    		        opts.inSampleSize = computeSampleSize(width, height,
//    		                UNCONSTRAINED, BACKUP_PIXEL_COUNT);
    		        if(width > height) {
    		        	opts.inSampleSize = computeSampleSizeLarger(opts.outWidth, opts.outHeight, height);
    		        }else {
    		        	opts.inSampleSize = computeSampleSizeLarger(opts.outWidth, opts.outHeight, width);
    		        }
    		        opts.inJustDecodeBounds = false;
    		        bmp = BitmapFactory.decodeFile(url, opts);
    		    }
    		}
    		
    		if(bmp == null)
            {
    		    
    			//String v = imageurl.get(imgView);
    			String v = null;
    			if(imgView != null)
    			    v= (String)imgView.getTag();
    			Log.v(TAG, "bmp == null*******imgView != null???="+imgView);
    			Log.v(TAG, "bmp == null*******v="+v);
    			if(v != null && v.equals(url))
    			{        	
    				pHandler.post( new Runnable()
	            	{
	            		public void run()
	            		{
	            		    if(noimage == false)
	            		    {
	            		    	if(default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_USER)
		        		    	{
		    	            		setImageBmp(R.drawable.default_user_icon);
		        		    	}
	            		    	else if(default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_BOOK)
	            		    	{
	            		    		setImageBmp(R.drawable.default_book);
	            		    	}
	            		    	else if(default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_SCREENSHOT)
	            		    	{
	            		    		setImageBmp(R.drawable.photo_transparent);
                                } else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_APK) {
                                    setImageBmp(R.drawable.default_app_icon);
	            		    	}
                                else if (default_image_index == QiupuConfig.DEFAULT_IMAGE_INDEX_LINK) {
                                    setImageBmp(R.drawable.list_public);
                                } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_GREEN) {
                                    setImageBmp(R.drawable.wutong_screen_bg_green);
                                } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_BLUE) {
                                    setImageBmp(R.drawable.wutong_screen_bg_blue);
                                } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_RED) {
                                    setImageBmp(R.drawable.wutong_screen_bg_red);
                                } else if (default_image_index == QiupuConfig.DEFAUTL_RANDOM_LINK_INT_YELLOW) {
                                    setImageBmp(R.drawable.wutong_screen_bg_yellow);
                                }
		        		    	else
		        		    	{
		        		    		setImageBmp(R.drawable.photo_transparent);
		        		    	}
	            		    }
	            		    else
	            		    {
	            		    	 Log.v(TAG, "run dispose() is noimage==true");
	            		    	dispose();
	            		    }
            			}            		
	            	});
    			}
    			else
    			{
    				Log.v(TAG, "run dispose() a bug for forceweb");
    				dispose();
    			}      
    			if(mImageRunListener != null) {
    				mImageRunListener.onLoadingFailed();
    			}
            }
            else
            {
                //String v = imageurl.get(imgView);
            	String v = null;
    			if(imgView != null)
    			    v= (String)imgView.getTag();
    			Log.v(TAG, "bmp != null*******imgView != null???="+imgView);
    			Log.v(TAG, "bmp != null*******v="+v);
    			if(v != null && v.equals(url))
    			{
    				pHandler.post( new Runnable()
	            	{
	            		public void run()
	            		{
                            onThumbnailShow(bmp);

//	            		    Log.v(TAG, "---rotateBitmap--------1111---------");
//	            		    bmp = rotateBitmap(bmp);
	            		    if(need_scale == true)
	            		    {
	            		        if(bmp.getWidth()>width)
	            		        {
	            		            int h = (int)(width);
	       	    			        if(imgView != null)
	       	    			        {
	       	    			            if (scaleD < 0) {
	       	                                final Resources resources = imgView.getResources();
	       	                                scaleD = resources.getDisplayMetrics().density;
	       	                            }
	       	        		    	    h = (int)(width*scaleD);//imgView.getWidth();
	       	    			        }
                                    Bitmap mBaseImage = bmp;
                                     
                                    Log.d(TAG, "need_compress image ="+mBaseImage);
                                    float scale = (float)h/(float)mBaseImage.getWidth();
                                    if(Math.abs(scale-1.0) > offsetValve)
    	     	        		    {
    	     	        		    	Matrix matrix = new Matrix();	  
    	     	        		    
    	         		                matrix.setScale(scale, scale);
    	        		                if(isRoate&&(mBaseImage.getWidth()>mBaseImage.getHeight())) {
	 	         		                	matrix.postRotate(90);
	 	         		                }
    	         		                mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage
    	         		                  .getWidth(), mBaseImage.getHeight(), matrix, true);
    	         		                matrix = null;	 	
    	         		                
    	         		                //not cached, we can recycle the memory
    	 		        		        if(bmp.getWidth() > ImageCacheManager.maxCacheWidth)
    	 		        		        {
    	 		        		       	   bmp.recycle();
    	 		        		           bmp = null;
    	 		        		        }
    	 		        		        
    	 		        		        if(imgView != null)
                                             ImageCacheManager.ContextCache.putBitmapIntoMap(imgView.getContext(), mBaseImage, (String) imgView.getTag());
    	     	        		    }    	
                                       
                                    setImageBmp(mBaseImage);
	            		        }
	            		        else
	            		        {
	            		        	//may cause the cache object recycle, if the bmp comes from cache manager
	            		        	if(imgView != null)
                                        ImageCacheManager.ContextCache.putBitmapIntoMap(imgView.getContext(), bmp, (String) imgView.getTag());
	            		        	
	            		            setImageBmp(bmp); 
	            		        }
	            		       
	            		    }
	            		    else
	            		    {
	            		    	////may cause the cache object recycle, if the bmp comes from cache manager
	            		    	
	            		    	if(imgView != null)
                                    ImageCacheManager.ContextCache.putBitmapIntoMap(imgView.getContext(), bmp, (String) imgView.getTag());
	            		    	
                                setImageBmp(bmp);
	            		    }
	            		}
	            	});
    			}
    			else
    			{
    				dispose();
    			}
            }
    	}    	
    }
    
    
    protected void setImageBmp(int resbmp)
    {
    	if(null != imgView){
//    		Bitmap bmp = QiupuHelper.getResouseDrawable(imgView.getContext(),resbmp);
//    		bmp = QiupuHelper.getRoundedCornerBitmap(bmp, 10.0f);
//    		imgView.setImageBitmap(bmp);
    		if(ImageView.class.isInstance(imgView))
    		    ((ImageView)imgView).setImageResource(resbmp);
    		else if(TextView.class.isInstance(imgView))
    		    ((TextView)imgView).setCompoundDrawables(imgView.getContext().getResources().getDrawable(resbmp), null, null, null);
            else if (imgView instanceof ImageSwitcher) {
                ((ImageSwitcher)imgView).setImageResource(resbmp);
            }
    		
//    		dispose();
    	}
    }
    
    protected void setImageBmp(Bitmap bmp)
    {
    	setImageBmpWithDispose(bmp, true);
    }
    protected void setImageBmpWithDispose(Bitmap bmp,boolean withDispose)
    {
        setImageBmpWithDispose(imgView, bmp, withDispose);
    }

    protected void setImageBmpWithDispose(View imgView, Bitmap bmp,boolean withDispose) {
    	if(bmp != null && bmp.isRecycled() == false)
    	{
    	    if(null != imgView)
    	    {
	    		String tagurl = (String)imgView.getTag();
	    		if(tagurl == null)
	    		{
	    			if(ImageView.class.isInstance(imgView))
	        		    ((ImageView)imgView).setImageBitmap(bmp);
	        		else if(TextView.class.isInstance(imgView))
	        		    ((TextView)imgView).setCompoundDrawables(new BitmapDrawable(imgView.getResources(),bmp), null, null, null);
                    else if (imgView instanceof ImageSwitcher) {
                        ((ImageSwitcher)imgView).setImageDrawable(new BitmapDrawable(imgView.getResources(), bmp));
                    }

	    		}
	    		else if(tagurl != null && tagurl.equals(url))
	    		{
	    			if(ImageView.class.isInstance(imgView))
	        		    ((ImageView)imgView).setImageBitmap(bmp);
	        		else if(TextView.class.isInstance(imgView))
	        		{
	        			BitmapDrawable bd = new BitmapDrawable(imgView.getResources(), bmp);
	        			bd.setBounds(0, 0, bmp.getWidth(), bmp.getHeight());
	        		    ((TextView)imgView).setCompoundDrawables(bd, null, null, null);
	        		} else if (imgView instanceof ImageSwitcher) {
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(imgView.getResources(), bmp);
                        ((ImageSwitcher)imgView).setImageDrawable(bitmapDrawable);
                    }
	    		}
	    		else
	    		{
	    			Log.i(TAG,"setImageBmp changed="+url + " pre="+tagurl);
	    		}
	    		if(withDispose) {
	    			Log.v(TAG, "run dispose() in setImageBmp()");
	    			dispose();
	    		}
    	    }
    	    if(mImageRunListener != null) {
    	    	mImageRunListener.onLoadingFinished();
    	    }

    	}
    	else
    	{
    		if(mImageRunListener != null) {
    			mImageRunListener.onLoadingFailed();
    		}
    		Log.d(TAG, " setImageBmp Why come here bmp is recycle? or "+bmp, new Throwable());
    	}
    }
    
    protected void dispose()
    {
    	if(QiupuConfig.DBLOGD)Log.i(TAG,"dispose ======url="+url+"============ remove imgView="+imgView);    	
    	imgView = null;
    	pHandler = null;
    	url      = null;
    }
    
    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees,
                    (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(
                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle(); 
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
            }
        }
        return b;
    }
    
    public static interface OnImageRunListener {
    	void onLoadingFinished();
    	
    	void onLoadingFailed();
    }
    
    
    // This computes a sample size which makes the longer side at least
    // minSideLength long. If that's not possible, return 1.
    public static int computeSampleSizeLarger(int w, int h,
            int minSideLength) {
        int initialSize = Math.max(w / minSideLength, h / minSideLength);
        if (initialSize <= 1) return 1;

        return initialSize <= 8
                ? prevPowerOf2(initialSize)
                : initialSize / 8 * 8;
    }
    
 // Returns the previous power of two.
    // Returns the input if it is already power of 2.
    // Throws IllegalArgumentException if the input is <= 0
    public static int prevPowerOf2(int n) {
        if (n <= 0) throw new IllegalArgumentException();
        return Integer.highestOneBit(n);
    }
    
    
    private void deleteFile(String path) {
    	try{
			new File(path).delete();
		}catch(Exception nee){
			nee.printStackTrace();
		}
    }

    protected void onThumbnailShow(Bitmap bitmap) {
    }

    public static void shootImageRunner(String photoUrl, int defaultImageIndex, int dimension,
                                           View stream_photo, boolean noImage, boolean needscale) {
        ImageRun photo_1 = new ImageRun(null, photoUrl, 0);
        photo_1.default_image_index = defaultImageIndex;
        photo_1.addHostAndPath = true;
        photo_1.need_scale = needscale;
        photo_1.width = dimension;
        photo_1.noimage = noImage;
        if(photo_1.setImageView(stream_photo) == false)
            photo_1.post(null);
    }
}
