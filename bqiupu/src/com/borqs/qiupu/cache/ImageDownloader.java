/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.qiupu.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HttpsURLConnection;

import twitter4j.threadpool.QueuedThreadPool;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper.myHostnameVerifier;

/**
 * This helper class download images from the Internet and binds those with the provided ImageView.
 *
 * <p>It requires the INTERNET permission, which should be added to your application's manifest
 * file.</p>
 *
 * A local cache of downloaded images is maintained internally to improve performance.
 */
public class ImageDownloader implements Runnable{
    private static final String LOG_TAG = "ImageDownloader";

    public enum Mode { NO_ASYNC_TASK, NO_DOWNLOADED_DRAWABLE, CORRECT }
    private Mode mode = Mode.CORRECT;
    private Context con;
    public int max_width;
    Handler mHandler;
    
    
    public ImageDownloader(Context con) {
		super();
		this.con = con;
		mHandler = new Handler();
	}
    
    

	/**
     * Download the specified image from the Internet and binds it to the provided ImageView. The
     * binding is immediate if the image is found in the cache and will be done asynchronously
     * otherwise. A null bitmap will be associated to the ImageView if an error occurs.
     *
     * @param url The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    private  String photoUrl;
    private ImageView pImageView;
    public void download(String url, ImageView imageView) {
    	pImageView = imageView;
    	photoUrl = url;
    	imageView.setImageResource(R.drawable.photo_default_img);
//        getThreadPool().dispatch(this);
//        mHandler.post(this);
        
        resetPurgeTimer();
        Bitmap bitmap = getBitmapFromCache(url);

        if (bitmap == null) {
            forceDownload(url, imageView);
        } else {
            cancelPotentialDownload(url, imageView);
            imageView.setImageBitmap(bitmap);
        }
    }

    /*
     * Same as download but the image is always downloaded and the cache is not used.
     * Kept private at the moment as its interest is not clear.
       private void forceDownload(String url, ImageView view) {
          forceDownload(url, view, null);
       }
     */

    /**
     * Same as download but the image is always downloaded and the cache is not used.
     * Kept private at the moment as its interest is not clear.
     */
    private void forceDownload(String url, ImageView imageView) {
        // State sanity: url is guaranteed to never be null in DownloadedDrawable and cache keys.
        if (url == null) {
            imageView.setImageDrawable(null);
            return;
        }

        if (cancelPotentialDownload(url, imageView)) {
            switch (mode) {
                case NO_ASYNC_TASK:
                    Bitmap bitmap = downloadBitmap(url);
                    addBitmapToCache(url, bitmap);
                    imageView.setImageBitmap(bitmap);
                    break;

                case NO_DOWNLOADED_DRAWABLE:
                    imageView.setMinimumHeight(156);
                    BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
                    task.execute(url);
                    break;

                case CORRECT:
                    task = new BitmapDownloaderTask(imageView);
                    DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
                    imageView.setImageDrawable(downloadedDrawable);
                    imageView.setMinimumHeight(156);
                    task.execute(url);
                    break;
            }
        }
    }

    /**
     * Returns true if the current download has been canceled or if there was no download in
     * progress on this image view.
     * Returns false if the download in progress deals with the same url. The download is not
     * stopped in that case.
     */
    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active download task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

//    Bitmap downloadBitmap(String url) {
//    	final int IO_BUFFER_SIZE = 4 * 1024;
//    	
//    	// AndroidHttpClient is not allowed to be used from the main thread
//    	final HttpClient client = (mode == Mode.NO_ASYNC_TASK) ? new DefaultHttpClient() :
//    		AndroidHttpClient.newInstance("Android");
//    	final HttpGet getRequest = new HttpGet(url);
//    	
//    	try {
//    		HttpResponse response = client.execute(getRequest);
//    		final int statusCode = response.getStatusLine().getStatusCode();
//    		if (statusCode != HttpStatus.SC_OK) {
//    			Log.w("ImageDownloader", "Error " + statusCode +
//    					" while retrieving bitmap from " + url);
//    			return null;
//    		}
//    		
//    		final HttpEntity entity = response.getEntity();
//    		if (entity != null) {
//    			InputStream inputStream = null;
//    			try {
//    				inputStream = entity.getContent();
//    				// return BitmapFactory.decodeStream(inputStream);
//    				// Bug on slow connections, fixed in future release.
//    				return BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
//    			} finally {
//    				if (inputStream != null) {
//    					inputStream.close();
//    				}
//    				entity.consumeContent();
//    			}
//    		}
//    	} catch (IOException e) {
//    		getRequest.abort();
//    		Log.w(LOG_TAG, "I/O error while retrieving bitmap from " + url, e);
//    	} catch (IllegalStateException e) {
//    		getRequest.abort();
//    		Log.w(LOG_TAG, "Incorrect URL: " + url);
//    	} catch (Exception e) {
//    		getRequest.abort();
//    		Log.w(LOG_TAG, "Error while retrieving bitmap from " + url, e);
//    	} finally {
//    		if ((client instanceof AndroidHttpClient)) {
//    			((AndroidHttpClient) client).close();
//    		}
//    	}
//    	return null;
//    }
    
    
    Bitmap downloadBitmap(String url) {
    	Bitmap bitmap = null;
    	HttpURLConnection conn = null;
        InputStream inputStream = null;
        try {
        	URL imageurl = new URL(url);
            conn = (HttpURLConnection) imageurl.openConnection();

            if (HttpsURLConnection.class.isInstance(conn)) {
                myHostnameVerifier passv = new myHostnameVerifier();
                ((HttpsURLConnection) conn).setHostnameVerifier(passv);
            }

            conn.setConnectTimeout(15 * 1000);
            conn.setReadTimeout(30 * 1000);
            inputStream = conn.getInputStream();

            int retcode = conn.getResponseCode();
            if(retcode == 200)
            {
            	cacheBitmapToFile(url, inputStream);
            	bitmap = decodeBitmapFromFile(getImageFilePath(imageurl));
//            	bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
            	addBitmapToCache(url, bitmap);
            	return bitmap;
            }
            

        } catch (IOException ne) {
            Log.d(LOG_TAG, "fail to get image=" + ne.getMessage());
            return null;
        } finally {
            if (conn != null) {
            	try{
                    conn.disconnect();
            	}catch(Exception ne){}
            }
            if (inputStream != null) {
                try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            
        }

        return bitmap;
    }
    
    public void  cacheBitmapToFile( String url,InputStream inputStream) {
        if (url == null || url.length() == 0)
            return;

        try {
            URL imageurl = new URL(url);
            String filepath = getImageFilePath(imageurl);

            File file = new File(filepath);
            if (file.exists() == true) {
            	file.delete();
            }
            
            //check the available space
            try {
            	final String expectedPath;
            	if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            		expectedPath = "";
            	} else {
            		expectedPath = "/data/data/";
            	}
            	if (!QiupuConfig.isEnoughSpace(expectedPath, QiupuHelper.remainSize)) {
            		Log.d(LOG_TAG, "getImagePathFromURL, no enough space in path: " + expectedPath);
            		return;
            	}
            } catch (Exception ne) {
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            	TrafficStats.setThreadStatsTag(0xB0AC);
            //get bitmap
            FileOutputStream fos = null;
            try {
            	File filep = QiupuHelper.createTempPackageFile(con, filepath);
            	filepath = filep.getAbsolutePath();
            	
            	fos = new FileOutputStream(filep);
            	
            	int len = -1;
            	byte[] buf = new byte[1024 * 4];
            	while ((len = inputStream.read(buf, 0, 1024 * 4)) > 0) {
            		fos.write(buf, 0, len);
            	}
            	fos.close();                    
            	
            	Log.d(LOG_TAG, "save url=" + url + " as file=" + filepath);
            } catch (IOException ne) {
            	Log.d(LOG_TAG, "fail to get image=" + ne.getMessage());
            	file.delete();
            	return;
            } finally {
            	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            		TrafficStats.clearThreadStatsTag();
            	
            	if(fos != null)
            	{
            		try{
            			fos.close();
            		}catch(Exception ne){}
            	}
            }
        } catch (java.net.MalformedURLException ne) {
        }
    }
    
    public String isImageExistInPhone(String url) {
        String localpath = null;
        try {
            final URL imageurl = new URL(url);
            final String filepath = getImageFilePath(imageurl);
            final File file = new File(filepath);
            if (file.exists() == true && file.length() > 0) {
                localpath = filepath;
            } else if (url.endsWith(".icon.png")){
                final String alterFilePath = getAlterLocalImageFilePath(imageurl);
                final File alterFile = new File(alterFilePath);
                if (alterFile.exists() && alterFile.length() > 0) {
                    localpath = alterFilePath;
                }
            }
        } catch (java.net.MalformedURLException ne) {
            Log.d(LOG_TAG, "isImageExistInPhone exception=" + ne.getMessage() + " url=" + url);
        }
        return localpath;

    }
    private  String getAlterLocalImageFilePath(URL imageurl) {
        final String alterPath;
        
        final String imageFilePath = imageurl.getFile();
        final int subStart = imageFilePath.lastIndexOf('/');
        final int subEnd = imageFilePath.indexOf('-', subStart);
        final String localFileName;
        if (subEnd > 0) {
            localFileName = imageFilePath.substring(subStart + 1, subEnd) + ".png";
        } else {
            localFileName = imageFilePath.substring(subStart + 1) + ".png";
        }

        alterPath = QiupuHelper.tempimagePath + new File(getImageFileName(localFileName)).getName();
        return alterPath;
    }
    
    public String getImageFilePath(URL imageurl)
    {
         return QiupuHelper.tempimagePath + new File(getImageFileName(imageurl.getFile())).getName();
    }
    
    private String getImageFileName(String filename)
	{
         if(filename.contains("=") || filename.contains("?") || filename.contains("&") ||filename.contains("%"))
         {
        	 filename = filename.replace("?", "");
        	 filename = filename.replace("=", "");
        	 filename = filename.replace("&", "");
        	 filename = filename.replace("%", "");
         }
         
         return filename;
	}

    /*
     * An InputStream that skips the exact number of bytes provided, unless it reaches EOF.
     */
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private String url;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            return downloadBitmap(url);
        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            addBitmapToCache(url, bitmap);

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with it
                // Or if we don't use any bitmap to task association (NO_DOWNLOADED_DRAWABLE mode)
                if ((this == bitmapDownloaderTask) || (mode != Mode.CORRECT)) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }


    /**
     * A fake Drawable that will be attached to the imageView while the download is in progress.
     *
     * <p>Contains a reference to the actual download task, so that a download task can be stopped
     * if a new binding is required, and makes sure that only the last started download process can
     * bind its result, independently of the download finish order.</p>
     */
    static class DownloadedDrawable extends ColorDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
            super(Color.BLACK);
            bitmapDownloaderTaskReference =
                new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        clearCache();
    }

    
    /*
     * Cache-related fields and methods.
     * 
     * We use a hard and a soft cache. A soft reference cache is too aggressively cleared by the
     * Garbage Collector.
     */
    
    private static final int HARD_CACHE_CAPACITY = 3;
    private static final int DELAY_BEFORE_PURGE = 10 * 1000; // in milliseconds

    // Hard cache, with a fixed maximum capacity and a life duration
    private final HashMap<String, Bitmap> sHardBitmapCache =
        new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                // Entries push-out of hard reference cache are transferred to soft reference cache
                sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
                return true;
            } else
                return false;
        }
    };

    // Soft cache for bitmaps kicked out of hard cache
    private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache =
        new ConcurrentHashMap<String, SoftReference<Bitmap>>(HARD_CACHE_CAPACITY / 2);

    private final Handler purgeHandler = new Handler();

    private final Runnable purger = new Runnable() {
        public void run() {
            clearCache();
        }
    };

    /**
     * Adds this bitmap to the cache.
     * @param bitmap The newly downloaded bitmap.
     */
    private void addBitmapToCache(String url, Bitmap bitmap) {
        if (bitmap != null) {
            synchronized (sHardBitmapCache) {
                sHardBitmapCache.put(url, bitmap);
            }
        }
    }

    /**
     * @param url The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    private Bitmap getBitmapFromCache(String url) {
        // First try the hard reference cache
        synchronized (sHardBitmapCache) {
            final Bitmap bitmap = sHardBitmapCache.get(url);
            if (bitmap != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                sHardBitmapCache.remove(url);
                sHardBitmapCache.put(url, bitmap);
                return bitmap;
            }
        }

        // Then try the soft reference cache
        SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                // Bitmap found in soft cache
                return bitmap;
            } else {
                // Soft reference has been Garbage Collected
                sSoftBitmapCache.remove(url);
            }
        }
        
        String imagePath = isImageExistInPhone(url);
        if(TextUtils.isEmpty(imagePath)) {
        	return null;
        }else {
        	Bitmap bmP = decodeBitmapFromFile(imagePath);
        	if(bmP != null) {
        		addBitmapToCache(url, bmP);
        	}
        	return bmP;
        }
        
    }
    
 
    /**
     * Clears the image cache used internally to improve performance. Note that for memory
     * efficiency reasons, the cache will automatically be cleared after a certain inactivity delay.
     */
    public void clearCache() {
        sHardBitmapCache.clear();
        sSoftBitmapCache.clear();
    }

    /**
     * Allow a new delay before the automatic cache clear is done.
     */
    private void resetPurgeTimer() {
        purgeHandler.removeCallbacks(purger);
        purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    }
    
    
    private Bitmap  decodeBitmapFromFile(String filepath) {
    	Bitmap bmp = null;
    	BitmapFactory.Options localOptions_normal = new BitmapFactory.Options();
    	localOptions_normal.inJustDecodeBounds = true;
    	BitmapFactory.decodeFile(filepath,  localOptions_normal);
    	if(max_width < Math.max(localOptions_normal.outWidth, localOptions_normal.outHeight)) {
    		int widthScale_normal = (int)Math.ceil(localOptions_normal.outWidth/max_width);
    		int heightScale_normal = (int)Math.ceil(localOptions_normal.outHeight/max_width);
    		if ((widthScale_normal > 1) || (heightScale_normal > 1))
    			if (widthScale_normal <= heightScale_normal) {
    				
    				localOptions_normal.inSampleSize = heightScale_normal;
    			}
    			else {
    				localOptions_normal.inSampleSize = widthScale_normal;
    			}
    		localOptions_normal.inJustDecodeBounds = false;
    		bmp = BitmapFactory.decodeFile(filepath,  localOptions_normal);
    		if(bmp != null) {
    			if (widthScale_normal <= heightScale_normal) {
    				bmp = Bitmap.createScaledBitmap(bmp, max_width*localOptions_normal.outWidth/localOptions_normal.outHeight, max_width, true);
    			}else {
    				bmp = Bitmap.createScaledBitmap(bmp,  max_width,max_width*localOptions_normal.outHeight/localOptions_normal.outWidth, true);
    			}
    		}
    	} else {
    		bmp = BitmapFactory.decodeFile(filepath,  null);
    	}
    	
    	ExifInterface exifInterface;
		try {
			exifInterface = new ExifInterface(filepath);
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
				if(bmp != null) {
					bmp = Bitmap.createBitmap(
							bmp, 0, 0, bmp.getWidth(),bmp.getHeight(), matrix, true);
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return bmp;
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
	            
	            Runtime.getRuntime().addShutdownHook(new Thread(LOG_TAG)
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

	@Override
	public void run() {
		resetPurgeTimer();
      Bitmap bitmap = getBitmapFromCache(photoUrl);

      if (bitmap == null) {
          forceDownload(photoUrl, pImageView);
      } else {
          cancelPotentialDownload(photoUrl, pImageView);
          pImageView.setImageBitmap(bitmap);
      }
		
	}
    
}
