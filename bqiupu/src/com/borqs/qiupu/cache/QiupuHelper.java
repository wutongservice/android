package com.borqs.qiupu.cache;

import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.getExternalStorageState;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;


import org.apache.http.conn.ssl.AbstractVerifier;

import twitter4j.*;
import twitter4j.conf.ConfigurationBase;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.listener.ActivityFinishListner;
import com.borqs.common.listener.CircleActionListner;
import com.borqs.common.listener.ContactInfoActionListner;
import com.borqs.common.listener.NotifyActionListener;
import com.borqs.common.listener.PageActionListener;
import com.borqs.common.listener.RefreshCircleListener;
import com.borqs.common.listener.RefreshComposeItemActionListener;
import com.borqs.common.listener.RefreshPostListener;
import com.borqs.common.listener.RefreshPostProfileImageListener;
import com.borqs.common.listener.RequestRefreshListner;
import com.borqs.common.listener.StreamActionListener;
import com.borqs.common.listener.TargetLikeActionListener;
import com.borqs.common.listener.TargetTopActionListener;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.PhoneEmailColumns;
import com.borqs.qiupu.fragment.RequestFragment.UpdateRequestCountListener;

public class QiupuHelper {
	private static final String TAG="QiupuHelper";

    private static final int FORCE_UPDATE_CACHED_PROFILE_VERSION = 332; // begin use round profile image from this version.

	private static ImageCacheManager cachemanager;
	public static final String tempimagePath = QiupuConfig.APP_ICON_SDCARD_PATH;
	public static final String tempimagePath_nosdcard = QiupuConfig.APP_ICON_PHONE_PATH;
	private static String tmpPath = tempimagePath;

	public static String posts_public        = QiupuHelper.getTmpCachePath() + "qiupu_posts.ser";
    public static String posts_friend        = QiupuHelper.getTmpCachePath() + "qiupu_posts_friend.ser";
    public static String posts_bpc        = QiupuHelper.getTmpCachePath() + "qiupu_posts_bpc.ser";
//    public static String posts_me        = QiupuHelper.getTmpCachePath() + "qiupu_posts_me.ser";

	public static String contacts     = QiupuHelper.getTmpCachePath() + "contacts.ser";
	public static String contacts_email = QiupuHelper.getTmpCachePath() + "contacts_email.ser";
	public static String public_posts = QiupuHelper.getTmpCachePath() + "qiupu_public_posts.ser";
	public static String lastmail     = QiupuHelper.getTmpCachePath() + "qiupu_lastmail.ser";
	public static String notification = QiupuHelper.getTmpCachePath() + "qiupu_notifications.ser";
	public static String filter       = QiupuHelper.getTmpCachePath() + "qiupu_filter.ser";
	public static String profile      = QiupuHelper.getProfileCachePath();
	public static String top_post      = QiupuHelper.getProfileCachePath() + "top_post.ser";
	public static String circle       = QiupuHelper.getCircleCachePath();
	public static String page         = getPageCachePath();
	
	public static String qiupu_allapp       = QiupuHelper.getTmpCachePath() + "qiupu_allapp.ser";
	public static String qiupu_recommendapp = QiupuHelper.getTmpCachePath() + "qiupu_recommendapp.ser";
	public static String qiupu_topiccategory = QiupuHelper.getTmpCachePath() + "qiupu_topiccategory.ser";
	public static String qiupu_mastercategory = QiupuHelper.getTmpCachePath() + "qiupu_mastercategory.ser";
	public static final File PHOTO_DIR = new File( QiupuHelper.getTmpCachePath());
	
	public static String qiupu_location = QiupuHelper.getTmpCachePath() + "qiupu_location.ser";
	public static String qiupu_location_geo = QiupuHelper.getTmpCachePath() + "qiupu_location_geo.ser";

	public static String qiupu_compose_all = QiupuHelper.getTmpCachePath() + "qiupu_compose_all.ser";
	public static String qiupu_compose_succeed = QiupuHelper.getTmpCachePath() + "qiupu_compose_succeed.ser";
	public static String qiupu_compose_failed = QiupuHelper.getTmpCachePath() + "qiupu_compose_failed.ser";

    public static final int MAX_FILE_NAME_LEN = 255;

	private static QiupuORM orm ;
	static StatFs stat = null;

    public static int mVersionCode;

    static {
        cachemanager = ImageCacheManager.instance();
        mVersionCode = 0;
        ensureAppIconPath(mVersionCode);
        setStaticVariabl(false);
    }

	static long remainSize= 4*1024*1024L;
	static void setStaticVariabl(boolean readonly)
	{
		if(readonly==true)
		{
			tmpPath = tempimagePath_nosdcard;
			stat = null;//new StatFs("/data/data/com.tormas.litesina/files/");
			remainSize = 4*1024*1024L;
		}
		else
		{		
			  if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == false)
		      {
			      tmpPath = tempimagePath_nosdcard;
			      remainSize = 4*1024*1024L;
		      }
			  else
			  {
				  tmpPath=tempimagePath;
				  remainSize = 4*1024*1024L;		  
			  }
			  
			  try{
			      new File(tmpPath).mkdirs();
			  }catch(Exception ne){
				  Log.d(TAG, "exception ="+ne.getMessage());
			  }
			  			
              
			  //for statics
			  if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == true)
			  {    	    
				  try
				  {
					  stat = null;
			          stat = new StatFs(QiupuConfig.getSdcardPath());
				  }
				  catch(java.lang.IllegalArgumentException ne)
				  {
					  Log.d(TAG, "why come here="+ne.getMessage());
				  }
			  }
			  else
			  {
				  stat = null;//new StatFs("/data/data/com.tormas.litesina/files/");
			  }
		}
	}
	
	public static boolean  serialization(final twitter4j.Stream post)
	{
		boolean ser = false;
	    FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try
		{
			final String postser = getTmpCachePath() + "postser.ser";
		    fos = new FileOutputStream(postser);
		    out = new ObjectOutputStream(fos);
		    Date date = new Date();
		    out.writeLong(date.getTime());
		    out.writeObject(post);
		    out.close();
		    ser = true;
		}
		catch(IOException ex)
		{
		    Log.d(TAG, "serialization fail="+ex.getMessage());
		}
		
		return ser;					
	}
	
	public static twitter4j.Stream  deSerialization()
	{
		twitter4j.Stream item = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try{
		    fis = new FileInputStream( getTmpCachePath() + "postser.ser");
		    in = new ObjectInputStream(fis);
		    long lastrecord = in.readLong();
		    Date now = new Date();
		    //Log.d(TAG, now.toGMTString());		    
		    item = (twitter4j.Stream) in.readObject();
		}
		catch(Exception ex)
		{
			Log.d(TAG, "deserialization fail="+ex.getMessage());
		}
		
		return item;
	}
	
    public static boolean hasEnoughspace(long apksize)
    {
        final boolean isMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String expectedPath = isMounted ? "" : "/data/data";

        return QiupuConfig.isEnoughSpace(expectedPath, remainSize + 2 * apksize);
    }
	
	public static String getDownloadPath() {	
		String apkdownloadPath = "";
		String esStatus = Environment.getExternalStorageState();		
		if (esStatus.equals(Environment.MEDIA_MOUNTED)) 
		{
			apkdownloadPath = QiupuConfig.APP_APK_SDCARD_PATH;
		}
		else 
		{
			apkdownloadPath = QiupuConfig.APP_PHONE_PATH + QiupuConfig.APP_APK_PHONE_PATH;
		}
		
		createAllWritableFolder(apkdownloadPath);
		
		return apkdownloadPath;
	}
	
	public static String saveAPKBMP(Context con, String packagename, String fileName)
    {
    	String dir = con.getFilesDir().getAbsolutePath();
		if(new File(dir).exists() == false)
		{
			new File(dir).mkdirs();
		}
		
		String iconPath = con.getFilesDir().getAbsolutePath() + "/" + fileName.replaceFirst(".apk", ".png");
		Bitmap bitmap = null;    	
		
		if(iconPath != null) 
		{
			File iconFile = new File(iconPath);
			FileOutputStream fOut = null;
			try {
				final PackageManager pm = con.getPackageManager();				
				bitmap = ((BitmapDrawable)(pm.getPackageInfo(packagename, 0).applicationInfo.loadIcon(pm))).getBitmap();
				
				iconFile.createNewFile();
				fOut = new FileOutputStream(iconFile);
				
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);				
				fOut.flush();				
			}catch(NameNotFoundException ne){
				ne.printStackTrace();				
				iconPath = null;
			}
			catch (Exception e2) {
				e2.printStackTrace();				
				iconPath = null;
			}
			finally
			{
				try {
					fOut.close();
				} catch (Exception e) {}
			}			
		}
		return iconPath;
		
    }

    public static String getDownloadSdcardPath(String packagename) {
        String apkdownloadPath = "";
        String esStatus = Environment.getExternalStorageState();
        if (esStatus.equals(Environment.MEDIA_MOUNTED)) {
            apkdownloadPath = QiupuConfig.APP_APK_SDCARD_PATH;
        }

        File destDir = new File(apkdownloadPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
            createAllWritableFolder(apkdownloadPath);
        }


        return apkdownloadPath + packagename + ".apk";
    }
	
	public static String getDownloadFileSdcardPath(String name) {   
        String filedownloadPath = "";
        String esStatus = Environment.getExternalStorageState();
        if (esStatus.equals(Environment.MEDIA_MOUNTED)) {
            filedownloadPath = QiupuConfig.APP_MEDIA_SDCARD_PATH;
        }

        File destDir = new File(filedownloadPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
            createAllWritableFolder(filedownloadPath);
        }

        
        return filedownloadPath + name;
    }
	
	//TODO monitor sdcard unmount/mount
	//if mounted sdcard, need to move the sns/images to /sdcard/
	//and reset stat, tmpPath
	public static void unmountSdcard()
	{
		setStaticVariabl(false);
	}
	public static void mountSdcard(boolean readonly)
	{
		setStaticVariabl(readonly);
	}
	
	public static String getTmpPath()
	{
		return tmpPath;
	}
	
//	private static String cachePath = "";
//	private static String sdcardRoot="";
//	private static String profileCachePath = "";
public static String getTmpCachePath() {
    final String cachePath;
    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == false) {
        cachePath = QiupuConfig.APP_CACHE_PHONE_PATH;
    } else {
        cachePath = QiupuConfig.APP_CACHE_SDCARD_PATH;
    }

    tryMakePath(cachePath);

    return cachePath;
}

    public static String getProfileCachePath() {
        final String cachedPath;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == false) {
            cachedPath = QiupuConfig.APP_CACHE_PHONE_PATH + "profile/";
        } else {
            cachedPath = QiupuConfig.APP_CACHE_SDCARD_PATH + "profile/";
        }

        tryMakePath(cachedPath);

        return cachedPath;
    }
    
    public static String getShareSourceCachePath() {
        final String cachedPath;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == false) {
            cachedPath = QiupuConfig.APP_CACHE_PHONE_PATH + "profile/share/";
        } else {
            cachedPath = QiupuConfig.APP_CACHE_SDCARD_PATH + "profile/share/";
        }

        tryMakePath(cachedPath);

        return cachedPath;
    }

    public static String getCircleCachePath() {
        final String cachedPath;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == false) {
            cachedPath = QiupuConfig.APP_CACHE_PHONE_PATH + "cirlce/";
        } else {
            cachedPath = QiupuConfig.APP_CACHE_SDCARD_PATH + "circle/";
        }

        tryMakePath(cachedPath);

        return cachedPath;
    }
    
    public static String getPageCachePath() {
        final String cachedPath;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == false) {
            cachedPath = QiupuConfig.APP_CACHE_PHONE_PATH + "page/";
        } else {
            cachedPath = QiupuConfig.APP_CACHE_SDCARD_PATH + "page/";
        }

        tryMakePath(cachedPath);

        return cachedPath;
    }

    private static void tryDeletePath(final String path) throws IOException {
        Log.v(TAG, "tryDeletePath, path: " + path + ", start time: "  + System.currentTimeMillis());
        File file = new File(path);
        if (file.exists() && file.canWrite()) {
            String deleteCmd = "rm -r " + path;
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(deleteCmd);
//            file.delete();
        } else {
            Log.d(TAG, "tryDeletePath, can not clear cache folder: " + path);
        }
    }

    private static HashMap<String, ResetCacheListener> listenerHashMap = new HashMap<String, ResetCacheListener>();
    private static void resetCachedPath(final String path) throws IOException {
        Log.v(TAG, "resetCachedPath, path: " + path + ", start time: "  + System.currentTimeMillis());
        File file = new File(path);
        Runtime runtime = Runtime.getRuntime();

        if (file.exists() && file.canWrite()) {
            ResetCacheListener listener = new ResetCacheListener(file.getPath());
            listenerHashMap.put(file.getPath(), listener);
            listener.startWatching();

            final String deleteCmd = "rm -r " + path;
            runtime.exec(deleteCmd);
        }
//
//        final String mkdirCmd = "mkdir -p " + path;
//        runtime.exec(mkdirCmd);
    }

    private static void resetCachedPath(final String gcPath, final String iconPath, final String otherPath) throws IOException {
        Log.v(TAG, "resetCachedPath, icon path: " + iconPath + ", other path:" + otherPath +
                ", start time: " + System.currentTimeMillis());

        Runtime runtime = Runtime.getRuntime();

        File file = new File(gcPath);
        if (!file.exists()) {
            runtime.exec("mkdir -p " + gcPath);
        }

        file = new File(iconPath);
        if (file.exists() && file.canWrite()) {
            runtime.exec("mv " + iconPath + " " + gcPath);
        }

        file = new File(otherPath);
        if (file.exists() && file.canWrite()) {
            runtime.exec("mv " + otherPath + " " + gcPath);
        }

        runtime.exec("mkdir -p " + iconPath + " " + otherPath);

        runtime.exec("rm -r " + gcPath);
    }

    public static void ClearCache()
	{
		Log.d(TAG, "clear all cache files");
		try{
            resetCachedPath(QiupuConfig.APP_CACHE_SDCARD_PATH);
            resetCachedPath(QiupuConfig.APP_ICON_SDCARD_PATH);
//            resetCachedPath(QiupuConfig.APP_GC_SDCARD_PATH, QiupuConfig.APP_CACHE_SDCARD_PATH, QiupuConfig.APP_ICON_SDCARD_PATH);

            resetCachedPath(QiupuConfig.APP_CACHE_PHONE_PATH);
            resetCachedPath(QiupuConfig.APP_ICON_PHONE_PATH);
		}catch(Exception ne){
			ne.printStackTrace();
		}
	}
	
    public static boolean deleteDirectory(File path) 
    {
	    if( path.exists() ) 
	    {
	        File[] files = path.listFiles();
	        for(int i=0; i<files.length; i++) 
                {
	            if(files[i].isDirectory()) 
	            {
	                deleteDirectory(files[i]);
	            }
	            else 
	            {
	            	try
	            	{
	                    files[i].delete();
	            	}
	            	catch(Exception ne)
	            	{
	            		Log.d(TAG, "delete file fail="+files[i].getAbsolutePath());
	            	}
	            }
	        }
	    }
	    return( path.delete() );
    }
   
    public static void deleteFiles(File path) 
    {
       if( path.exists() ) 
	   {
	       File[] files = path.listFiles();
	       for(int i=0; i<files.length; i++) 
	       {
	           if(files[i].isDirectory()) 
	           {
	               try
	               {
	                   deleteDirectory(files[i]);
	               }
	               catch(Exception ne)
	               {
	            		Log.d(TAG, "delete file fail="+files[i].getAbsolutePath());
	               }
	           }
	           else 
	           {
	               try
	               {
	                   files[i].delete();
	               }
	               catch(Exception ne)
	               {
	            		Log.d(TAG, "delete file fail="+files[i].getAbsolutePath());
	               }
	           }
	       }
	   }     
    }

    public static String isImageExistInPhone(String url, boolean addHostAndPath) {
        String localpath = null;
        try {
            final URL imageurl = new URL(url);
            final String filepath = getImageFilePath(imageurl, addHostAndPath);
            final File file = new File(filepath);
            if (file.exists() == true && file.length() > 0) {
                localpath = filepath;
                if (QiupuConfig.LowPerformance)
                    Log.d(TAG, "isImageExistInPhone is true, url=" + url + " file=" + filepath);
            } else if (url.endsWith(".icon.png")){
                final String alterFilePath = getAlterLocalImageFilePath(imageurl, addHostAndPath);
                final File alterFile = new File(alterFilePath);
                if (alterFile.exists() && alterFile.length() > 0) {
                    if(QiupuConfig.DBLOGD)Log.d(TAG, "isImageExistInPhone is true, url=" + url + " alter local file=" + alterFilePath);
                    localpath = alterFilePath;
                }
            }
        } catch (java.net.MalformedURLException ne) {
            Log.d(TAG, "isImageExistInPhone exception=" + ne.getMessage() + " url=" + url);
        }
        return localpath;

    }
	
	public static class myHostnameVerifier extends AbstractVerifier 
	{	        
        public myHostnameVerifier() 
        {
        	
        }
        public final void verify(final String host, final String[] cns, final String[] subjectAlts)
        {
        	Log.d(TAG, "host ="+host);
        }
	}
	
	public static String getImagePathFromURL_noFetch(String url)
	{
		try{
			 URL  imageurl = new URL(url);
	         String filename = getImageFileName(imageurl.getFile());
	         String filepath = tmpPath + new File(filename).getName();		
	         return filepath;
		}catch(MalformedURLException ne){}
		return "";
	}

    public static String getImagePathFromURL(Context con, String url, boolean addHostAndPath) {
        if (url == null || url.length() == 0)
            return null;

        try {
            URL imageUrl = new URL(url);
            String filePath = getImageFilePath(imageUrl, addHostAndPath);

            if (QiupuConfig.LOGD) Log.d(TAG, "getImagePathFromURL \n   url=" + url + " file=" + filePath);

            File file = new File(filePath);
            if (file.exists() == false || file.length() == 0) {
                if (file.exists() == true) {
                    file.delete();
                }

                if (isLowStorage()) {
                    return null;
                }

                File savedFile = createTempPackageFile(con, filePath);
                if (!downloadImageFromInternet(imageUrl, savedFile)) {
                    file.delete();
                    return null;
                }

//                if (isProfileUrl(url)) {
//                    setRoundedCornerBitmap(filePath);
//                }
            }
            return filePath;
        } catch (java.net.MalformedURLException ne) {
            Log.e(TAG, "getImageFromURL url=" + url + " exception=" + ne.getMessage());
            return null;
        }
    }

    private static String getImageFileName(String filename) {
        if (filename.contains("=") || filename.contains("?")
                || filename.contains("&") || filename.contains("%")
                || filename.contains(":") || filename.contains("|")
                || filename.contains(" ") || filename.contains(",")) {
            filename = filename.replace("?", "");
            filename = filename.replace("=", "");
            filename = filename.replace("&", "");
            filename = filename.replace("%", "");
            filename = filename.replace(":", "");
            filename = filename.replace("|", "");
            filename = filename.replace(" ", "");
            filename = filename.replace(",", "");
        }

        return filename;
    }

    /*
	 * photos-a.ak.fbcdn.net
	 * api.facebook.com
	 * secure-profile.facebook.com
	 * ssl.facebook.com
	 * www.facebook.com
     * x.facebook.com
     * api-video.facebook.com
     * developers.facebook.com
     * iphone.facebook.com
     * developer.facebook.com
     * m.facebook.com
     * s-static.ak.facebook.com
     * secure-profile.facebook.com
     * secure-media-sf2p.facebook.com
     * ssl.facebook.com
     * profile.ak.facebook.com
     * b.static.ak.facebook.com
     * 
     * photos-h.ak.fbcdn.net
     * photos-f.ak.fbcdn.net
	 */	
	private static boolean isInTrustHost(String host)
	{
	    if(host.contains(".fbcdn.net"))
	        return true;
	    
	    if(host.contains("secure-profile.facebook.com"))
	        return true;
	    
	    return false;
	}
	
	public static String getConfigurationFilePath()
    {
	   return tmpPath + QiupuConfig.HOST_CONFIG;	   
    }
	
	public static String getImageFilePath(URL imageUrl, boolean addHostAndPath)
    {
        return getImageFilePath(tmpPath, imageUrl, addHostAndPath);
    }

    public static String getImageFilePath(String path, URL imageUrl, boolean addHostAndPath) {
        final int pathLength = path.length();
        final String filename = getImageFileName(imageUrl.getFile());
        final String targetFileName;
        if (addHostAndPath == false) {
            final String origin = new File(filename).getName();
            final int subIndex = pathLength + origin.length() - MAX_FILE_NAME_LEN;
            if (subIndex > 0) {
                targetFileName = origin.substring(subIndex);
            } else {
                targetFileName = origin;
            }
        } else {
            final String host = imageUrl.getHost().replace("/", "");
            if (isInTrustHost(host) == false) {
                final String originName = removeChar(filename);
                final int subIndex = pathLength + host.length() + 1 + filename.length() - MAX_FILE_NAME_LEN;
                if (subIndex > 0) {
                    targetFileName = new File(host + "_" + originName.substring(subIndex)).getName();
                } else {
                    targetFileName = new File(host + "_" + originName).getName();
                }
            } else {
                Log.d(TAG, "***********   i am in trust=" + host + " filename=" + filename);
                final int subIndex = pathLength + filename.length() - MAX_FILE_NAME_LEN;
                if (subIndex > 0) {
                    targetFileName = new File(filename.substring(subIndex)).getName();
                } else {
                    targetFileName = new File(filename).getName();
                }
            }
        }

        return path + targetFileName;
    }

    private static String removeChar(String filename) {
        if (filename.contains("=") || filename.contains("?")
                || filename.contains("&") || filename.contains("%")
                || filename.contains(" ") || filename.contains(",") || filename.contains("/")) {
            filename = filename.replace("?", "");
            filename = filename.replace("=", "");
            filename = filename.replace("&", "");
            filename = filename.replace("%", "");
            filename = filename.replace(",", "");
            filename = filename.replace(".", "");
            filename = filename.replace("-", "");
            filename = filename.replace(" ", "");
            filename = filename.replace(",", "");
            filename = filename.replace("/", "");
        }
        return filename;
    }

    private static Bitmap getImageFromPath(String filePath, int sampleSize) {
        if (filePath != null) {
            Bitmap tmp = null;
            if (new File(filePath).length() >= ImageRun.max_size) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filePath, opts);

                opts.inSampleSize = ImageRun.computeSampleSizeLarger(opts.outWidth, opts.outHeight, sampleSize);
                opts.inJustDecodeBounds = false;
                try {
                    tmp = BitmapFactory.decodeFile(filePath, opts);
                } catch (OutOfMemoryError oof) {

                }
            } else {
                tmp = BitmapFactory.decodeFile(filePath);
            }
            return tmp;
        } else
            return null;
    }

//    private static Bitmap getRoundedImagePathFromURL(Context con, String url, boolean addHostAndPath, int sampleSize) {
//        if (TextUtils.isEmpty(url))
//            return null;
//        try {
//            URL imageUrl = new URL(url);
//            String filePath = getRoundedImageFilePath(imageUrl, addHostAndPath);
//
//            if (QiupuConfig.DBLOGD) Log.d(TAG, "getRoundedImagePathFromURL \n   url=" + url + " file=" + filePath);
//
//            File file = new File(filePath);
//            if (file.exists() == false || file.length() == 0) {
//                if (file.exists() == true) {
//                    file.delete();
//                }
//
//                if (isLowStorage()) {
//                    return null;
//                }
//
//                File savedFile = createTempPackageFile(con, filePath);
//                if (!downloadImageFromInternet(imageUrl, savedFile)) {
//                    file.delete();
//                    return null;
//                }
//
//                // Rounded converting the downloaded image before return it.
//                Bitmap image = getImageFromPath(filePath, sampleSize);
//                if (null != image) {
//                    Bitmap backup = image;
//                    image = QiupuHelper.getRoundedCornerBitmap(con, image);
//                    saveBitmap(image, filePath);
//                    backup.recycle();
//                }
//                return image;
//
//            } else {
//                return getImageFromPath(filePath, sampleSize);
//            }
//        } catch (java.net.MalformedURLException ne) {
//            Log.e(TAG, "getImageFromURL url=" + url + " exception=" + ne.getMessage());
//            return null;
//        }
//    }

    public static Bitmap getImageFromURL(Context con, String url, boolean isHighPriority, boolean addHostAndPath, boolean setRoundAngle,int max_num_pixels,int width,int height)
    {
        ImageCacheManager.ImageCache cache = cachemanager.getCache(url);
        if(cache == null || cache.bmp == null || cache.bmp.isRecycled())
        {
            final Bitmap image;
            String filePath = getImagePathFromURL(con, url, addHostAndPath);
            image = getImageFromPath(filePath, width < height ? width : height);

            if (image != null) {
                cachemanager.addCache(url, image);
            }
            return image;
        }
        else
        {
        	return cache.bmp;
        }
    }
    
	static File createTempPackageFile(Context con, String filePath) 
	{
		File tmpPackageFile;
		if(filePath.startsWith(QiupuConfig.getSdcardPath()) == true)
		{
			tmpPackageFile = new File(filePath);
			return tmpPackageFile;
		}
		
        int i = filePath.lastIndexOf("/");
        String tmpFileName;
        if(i != -1) 
        {
            tmpFileName = filePath.substring(i+1);
        } 
        else 
        {
            tmpFileName = filePath;
        }
        FileOutputStream fos;
        try 
        {
            fos=con.openFileOutput(tmpFileName, 1|2);
        } 
        catch (FileNotFoundException e1) 
        {
            Log.e(TAG, "Error opening file "+tmpFileName);
            return null;
        }
        try 
        {
            fos.close();
        } 
        catch (IOException e) 
        {
            Log.e(TAG, "Error opening file "+tmpFileName);
            return null;
        }
        tmpPackageFile=con.getFileStreamPath(tmpFileName);            
        return tmpPackageFile;
	}

	public static String getAPKURL(String packagename) {
		return ConfigurationBase.getAPIURL()+ "search?q=" + packagename;
	}
    
    public static String getQpApkUrl() {
        return getAPKURL(QiupuConfig.APP_PACKAGE_NAME);
    }

	 public static File writeImage(final String path, final String name, final byte[] data) {
   	  File img = null;
         File dir = new File(path);
         if (!dir.exists())
             dir.mkdir();
         FileOutputStream fo = null;
         BufferedOutputStream bos = null;
         try {
        	 Log.d(TAG, "path + name "+ path+name);
             img = new File(path + name);
             if(img != null && img.exists()){
                 img.delete();
             }
//             img.createNewFile();
             fo = new FileOutputStream(img);
             bos = new BufferedOutputStream(fo);
             bos.write(data);
             bos.flush();
             if (bos != null)
             {
                 bos.close();
             }
             if (fo != null)
             {
                 fo.close();
             }
             return img;
         } catch (Exception e) {
        	 Log.e(TAG, "Error writeImage file "+data);
             e.printStackTrace();
         }
         return null;
     }
     public static byte[] Bitmap2Bytes(Bitmap bm){  
   	     ByteArrayOutputStream baos = new ByteArrayOutputStream();    
   	     bm.compress(Bitmap.CompressFormat.PNG, 100, baos);    
   	     return baos.toByteArray();  
       }  

     public static String getPhotoFileName() {  
         Date date = new Date(System.currentTimeMillis());  
         SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_HH_mm_ss");  
         return dateFormat.format(date) + ".jpg";  
     }  
     
     public static Intent getPhotoPickIntent() {  
    	 return getPhotoPickIntentWithXY(1, 1, 300, 300);
     }  
     
     public static Intent getPhotoPickIntentWithXY(int aspectX, int aspectY, int outputX, int outputY) {
//    	 Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
    	 Intent intent = new Intent();
    	 intent.setAction(Intent.ACTION_PICK);
         intent.setData(Uri.parse("content://media/internal/images/media"));
         intent.setType("image/*");  
         intent.putExtra("crop", "true");  
         intent.putExtra("aspectX", aspectX);  
         intent.putExtra("aspectY", aspectY);  
         intent.putExtra("outputX", outputX);  
         intent.putExtra("outputY", outputY);  
         intent.putExtra("return-data", true);  
         return intent;  
     }
		        
     /**  
     * Constructs an intent for image cropping.  
     */  
     public static Intent getCropImageIntent(Uri photoUri) {  
    	return getCropImageIntent(photoUri, 1, 1, 300, 300);
     }
     
     public static Intent getCropImageIntent(Uri photoUri, int aspectX, int aspectY, int outputX, int outputY) {  
         Intent intent = new Intent("com.android.camera.action.CROP");  
         intent.setDataAndType(photoUri, "image/*");  
         intent.putExtra("crop", "true");  
         intent.putExtra("aspectX", aspectX);  
         intent.putExtra("aspectY", aspectY);  
         intent.putExtra("outputX", outputX);  
         intent.putExtra("outputY", outputY);  
         intent.putExtra("return-data", true);  
         return intent;  
     }
     
     public static String refreshFileList(String strPath) { 
     	long a = System.currentTimeMillis();
     	System.out.println(System.currentTimeMillis() - a);
         File dir = new File(strPath); 
         File[] files = dir.listFiles(); 
         
         if (files == null) 
             return ""; 
         for (int i = 0; i < files.length; i++) { 
             if (files[i].isDirectory()) { 
//                 refreshFileList(files[i].getAbsolutePath()); 
             } else { 
                String strFileName = files[i].getAbsolutePath().toLowerCase();
                String tmppath = QiupuConfig.SDCARD_DOWNLOAD_PATH;
                strFileName = strFileName.substring(tmppath.length(), strFileName.length());
                Log.d(TAG, "!!!!!!!!!!!!!!!!!!"+strFileName);
                if (strFileName.contains(".com.borqs.qiupu")) 
                {
             	   return strFileName;
 			   }
             } 
         } 
         return "";
     }
     // just to delete for file witch is qiupu
     public static void deleteFile(String strPath)
     {
         long a = System.currentTimeMillis();
      	 System.out.println(System.currentTimeMillis() - a);
         File dir = new File(strPath); 
         File[] files = dir.listFiles(); 
          
         if (files == null) 
             return ; 
          
         for (int i = 0; i < files.length; i++)
         { 
             if (files[i].isDirectory()) 
             { 
                 //refreshFileList(files[i].getAbsolutePath()); 
             } 
             else 
             { 
                 String strFileName = files[i].getAbsolutePath().toLowerCase();
                 String tmppath = QiupuConfig.SDCARD_DOWNLOAD_PATH;
                 strFileName = strFileName.substring(tmppath.length(), strFileName.length());
                 if (strFileName.contains(".com.borqs.qiupu")) 
                 {
                    files[i].delete();
  		         }
              } 
          } 
     }

	 public static void removeQiupuApplication() 
	 {
		String dpath = getDownloadPath();		
		try{
		    new File(dpath+"com.borqs.qiupu.apk").delete();
		}catch(Exception ne){}
		
		try{
		    deleteFile(QiupuConfig.SDCARD_DOWNLOAD_PATH);
		}catch(Exception ne){}
	 }
	 
	 public static Bitmap getRoundedCornerBitmap(Context con, Bitmap bitmap)
	 {
		 float roundPx = con.getResources().getDimension(R.dimen.people_icon_corner);
	     Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);  
	     Canvas canvas = new Canvas(output);  
	     final int color = 0xff424242;  
	     final Paint paint = new Paint();  
	     final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());  
	     final RectF rectF = new RectF(rect);  
         paint.setAntiAlias(true);  
	     canvas.drawARGB(0, 0, 0, 0);  
	     paint.setColor(color);  
	     canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
	     paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
	     canvas.drawBitmap(bitmap, rect, rect, paint);  
	     return output;  
	 }   
	 
//	 public static Bitmap getResouseDrawable(Context context,int resbmp){
//	     InputStream is = context.getResources().openRawResource(resbmp);
//	     BitmapDrawable  bmpDraw = new BitmapDrawable(is);
//	     Bitmap bmp = bmpDraw.getBitmap();
//	     return bmp ;
//	 }
	 
	 public static Bitmap drawableToBitmap(Drawable drawable)
	 {
	     int width = drawable.getIntrinsicWidth();
	     int height = drawable.getIntrinsicHeight();
	     Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
	     Canvas canvas = new Canvas(bitmap);
	     drawable.setBounds(0,0,width,height);
	     drawable.draw(canvas);
	     return bitmap;
     }

	public static void setRoundedCornerBitmap(String localpath) {
		try
		{
		Bitmap bitmap = BitmapFactory.decodeFile(localpath);
		if(bitmap != null)
		{
			 float roundPx = 4.0f;
		     Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);  
		     Canvas canvas = new Canvas(output);  
		     final int color = 0xff424242;  
		     final Paint paint = new Paint();  
		     final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());  
		     final RectF rectF = new RectF(rect);  
	         paint.setAntiAlias(true);  
		     canvas.drawARGB(0, 0, 0, 0);  
		     paint.setColor(color);  
		     canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
		     paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
		     canvas.drawBitmap(bitmap, rect, rect, paint);
		     
		     saveBitmap(output, localpath);
		     
		     bitmap.recycle();
		     bitmap = null;
		     
		     output.recycle();
		     output = null;
		     canvas = null;
		}
		}catch(Exception ne){}
	}
	
	private static void saveBitmap(Bitmap bmp, String localpath)
	{	
		try {
			File iconFile = new File(localpath);
			iconFile.createNewFile();
		
			FileOutputStream fOut = new FileOutputStream(iconFile);
			bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	private static final HashMap<String,WeakReference<StreamActionListener>> listeners = new HashMap<String,WeakReference<StreamActionListener>>();
	    
	public static void registerStreamListener(String key,StreamActionListener listener){
		synchronized(listeners)
		{
			WeakReference<StreamActionListener> ref = listeners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			listeners.put(key, new WeakReference<StreamActionListener>(listener));
		}
	}
	
	public static void unregisterStreamListener(String key){
		synchronized(listeners)
		{
			WeakReference<StreamActionListener> ref = listeners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			listeners.remove(key);
		}
	}

    public static void updateStreamRemovedUI(QiupuSimpleUser user) {
        synchronized (QiupuHelper.listeners) {
            Set<String> set = QiupuHelper.listeners.keySet();
            Iterator<String> it = set.iterator();
            final long uid = user.uid;
            while (it.hasNext()) {
                String key = it.next();
                if (QiupuHelper.listeners.get(key) != null) {
                    StreamActionListener listener = QiupuHelper.listeners.get(key).get();
                    if (listener != null) {
                        listener.updateStreamRemovedUI(null, uid);
                    }
                }
            }
        }
    }

    public static void updateStreamCommentStatus(String postid, boolean canComment, boolean canLike, boolean canReshare) {
        synchronized (QiupuHelper.listeners) {
            Set<String> set = QiupuHelper.listeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                if(QiupuHelper.listeners.get(key) != null)
                {
                    StreamActionListener listener = QiupuHelper.listeners.get(key).get();
                    if (listener != null) {
                        listener.updateStreamCommentStatus(postid, canComment, canLike, canReshare);
                    }
                }
            }
        }
    }

    public static void refreshPhotoStreamCallBack(final Stream stream) {
        synchronized (QiupuHelper.listeners) {
            Set<String> set = QiupuHelper.listeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                if (QiupuHelper.listeners.get(key) != null) {
                    StreamActionListener listener = QiupuHelper.listeners.get(key).get();
                    if (listener != null) {
                        listener.updatePhotoStreamUI(stream);
                    }
                }
            }
        }
    }

    public static void onCommentsUpdated(final Stream stream, final int commentType, final ArrayList<Stream.Comments.Stream_Post> latestComments, final int totalCount) {
        synchronized (QiupuHelper.listeners) {
            Set<String> set = QiupuHelper.listeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                if (QiupuHelper.listeners.get(key) != null) {
                    StreamActionListener listener = QiupuHelper.listeners.get(key).get();
                    if (listener != null) {
                        listener.updateStreamCommentUI(stream, commentType, totalCount, latestComments);
                    }
                }
            }
        }
    }
    public static void updateStreamRemovedUI(String postId) {
        synchronized (QiupuHelper.listeners) {
            Set<String> set = QiupuHelper.listeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                if(QiupuHelper.listeners.get(key) != null)
                {
	                StreamActionListener listener = QiupuHelper.listeners.get(key).get();
	                if (listener != null) {
	                    listener.updateStreamRemovedUI(postId, -1);
	                }
                }
            }
        }
    }

    private static final HashMap<String, WeakReference<TargetLikeActionListener>> likeListeners = new HashMap<String,WeakReference<TargetLikeActionListener>>();
	public static void registerTargetLikeListener(String key, TargetLikeActionListener listener){
		synchronized(likeListeners)
		{
			WeakReference<TargetLikeActionListener> ref = likeListeners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			likeListeners.put(key, new WeakReference<TargetLikeActionListener>(listener));
		}
	}
	    	
	public static void unRegisterTargetLikeListener(String key){
		synchronized(likeListeners)
		{
			WeakReference<TargetLikeActionListener> ref = likeListeners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			likeListeners.remove(key);
		}
	}

    public static void onTargetLikeCreated(String targetId, String targetType) {
        synchronized (likeListeners) {
            Set<String> set = QiupuHelper.likeListeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                TargetLikeActionListener listener = QiupuHelper.likeListeners.get(key).get();
                if (listener != null) {
                    listener.onTargetLikeCreated(targetId, targetType);
                }
            }
        }
    }

    public static void onTargetLikeRemoved(String targetId, String targetType) {
        synchronized (likeListeners) {
            Set<String> set = QiupuHelper.likeListeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                TargetLikeActionListener listener = QiupuHelper.likeListeners.get(key).get();
                if (listener != null) {
                    listener.onTargetLikeRemoved(targetId, targetType);
                }
            }
        }
    }

    private static final HashMap<String, WeakReference<TargetTopActionListener>> topListeners = new HashMap<String,WeakReference<TargetTopActionListener>>();
    public static void registerTargetTopListener(String key, TargetTopActionListener listener){
        synchronized(likeListeners) {
            WeakReference<TargetTopActionListener> ref = topListeners.get(key);
            if(ref != null && ref.get() != null) {
                ref.clear();
            }
            topListeners.put(key, new WeakReference<TargetTopActionListener>(listener));
        }
    }

    public static void unRegisterTargetTopListener(String key){
        synchronized(topListeners) {
            WeakReference<TargetTopActionListener> ref = topListeners.get(key);
            if(ref != null && ref.get() != null) {
                ref.clear();
            }
            topListeners.remove(key);
        }
    }

    public static void onTargetTopCreate(String group_id, String stream_id) {
        synchronized (topListeners) {
            Set<String> set = QiupuHelper.topListeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                TargetTopActionListener listener = QiupuHelper.topListeners.get(key).get();
                if (listener != null) {
                    listener.onTargetTopCreated(group_id, stream_id);
                }
            }
        }
    }

    public static void onTargetTopCancel(String group_id, String stream_id) {
        synchronized (topListeners) {
            Set<String> set = QiupuHelper.topListeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                TargetTopActionListener listener = QiupuHelper.topListeners.get(key).get();
                if (listener != null) {
                    listener.onTargetTopCancel(group_id, stream_id);
                }
            }
        }
    }

	public static final HashMap<String,WeakReference<UsersActionListner>> userlisteners = new HashMap<String,WeakReference<UsersActionListner>>();
	
	public static void registerUserListener(String key,UsersActionListner listener){
		synchronized(userlisteners)
		{
			WeakReference<UsersActionListner> ref = userlisteners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			userlisteners.put(key, new WeakReference<UsersActionListner>(listener));
		}
	}
	
	public static void unregisterUserListener(String key){
		synchronized(userlisteners)
		{
			WeakReference<UsersActionListner> ref = userlisteners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			userlisteners.remove(key);
		}
	}
	
	public static final HashMap<String,WeakReference<RefreshCircleListener>> refreshCirclelisteners = new HashMap<String,WeakReference<RefreshCircleListener>>();
	
	public static void registerRefreshCircleListener(String key,RefreshCircleListener listener){
		synchronized(refreshCirclelisteners)
		{
			WeakReference<RefreshCircleListener> ref = refreshCirclelisteners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			refreshCirclelisteners.put(key, new WeakReference<RefreshCircleListener>(listener));
		}
	}
	
	public static void unregisterRefreshCircleListener(String key){
		synchronized(refreshCirclelisteners)
		{
			WeakReference<RefreshCircleListener> ref = refreshCirclelisteners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			refreshCirclelisteners.remove(key);
		}
	}
	
	public static final HashMap<String,WeakReference<CircleActionListner>> circleActionlisteners = new HashMap<String,WeakReference<CircleActionListner>>();
	
	public static void registerCircleActionListener(String key,CircleActionListner listener){
		synchronized(circleActionlisteners)
		{
			WeakReference<CircleActionListner> ref = circleActionlisteners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			circleActionlisteners.put(key, new WeakReference<CircleActionListner>(listener));
		}
	}
	
	public static void unregisterCircleActionListener(String key){
		synchronized(circleActionlisteners)
		{
			WeakReference<CircleActionListner> ref = circleActionlisteners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			circleActionlisteners.remove(key);
		}
	}
	
	public static final HashMap<String, WeakReference<ContactInfoActionListner>> contactinfolisteners =
        new HashMap<String, WeakReference<ContactInfoActionListner>>();

    public static void registerContactUpdateListener(String key, ContactInfoActionListner listener) {        
        synchronized (contactinfolisteners) {
        	WeakReference<ContactInfoActionListner> ref = contactinfolisteners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
        	contactinfolisteners.put(key, new WeakReference<ContactInfoActionListner>(listener));
        }        
    }
    
    public static void unregisterContactUpdateListener(String key){
		synchronized(contactinfolisteners)
		{
			WeakReference<ContactInfoActionListner> ref = contactinfolisteners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			contactinfolisteners.remove(key);
		}
	}
    
    public static final HashMap<String,WeakReference<RefreshPostListener>> refreshPostListeners = new HashMap<String,WeakReference<RefreshPostListener>>();
    
	public static void registerRefreshPostListener(String key,RefreshPostListener listener){
		synchronized(refreshPostListeners)
		{
			WeakReference<RefreshPostListener> ref = refreshPostListeners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			refreshPostListeners.put(key, new WeakReference<RefreshPostListener>(listener));
		}
	}
	    	
	public static void unregisterRefreshPostListener(String key){
		synchronized(refreshPostListeners)
		{
			WeakReference<RefreshPostListener> ref = refreshPostListeners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			refreshPostListeners.remove(key);
		}
	}
	
	public static final HashMap<String,WeakReference<RefreshPostProfileImageListener>> refreshProfileImageListeners = new HashMap<String,WeakReference<RefreshPostProfileImageListener>>();
	
	public static void registerRefreshPostProfileImageListener(String key,RefreshPostProfileImageListener listener){
		synchronized(refreshProfileImageListeners)
		{
			WeakReference<RefreshPostProfileImageListener> ref = refreshProfileImageListeners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			refreshProfileImageListeners.put(key, new WeakReference<RefreshPostProfileImageListener>(listener));
		}
	}
	
	public static void unregisterRefreshPostProfileImageListener(String key){
		synchronized(refreshProfileImageListeners)
		{
			WeakReference<RefreshPostProfileImageListener> ref = refreshProfileImageListeners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			refreshProfileImageListeners.remove(key);
		}
	}
    
	public static boolean intToBoolean(int value)
	{
		if(value == 1)
			return true;
		else 
			return false;
	}
	
	public static boolean inLocalCircle(String circleid)
	{
		return String.valueOf(QiupuConfig.BLOCKED_CIRCLE).equals(circleid)
		|| String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE).equals(circleid)
		|| String.valueOf(QiupuConfig.DEFAULT_CIRCLE).equals(circleid)
		|| String.valueOf(QiupuConfig.FAMILY_CIRCLE).equals(circleid)
		|| String.valueOf(QiupuConfig.CLOSE_FRIENDS_CIRCLE).equals(circleid)
		|| String.valueOf(QiupuConfig.ACQUAINTANCE_CIRCLE).equals(circleid);
	}
	
	public static boolean inFilterCircle(String circleid)
	{
		return String.valueOf(QiupuConfig.BLOCKED_CIRCLE).equals(circleid)
		|| String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE).equals(circleid)
		|| String.valueOf(QiupuConfig.CIRCLE_ID_ALL).equals(circleid)
		|| String.valueOf(QiupuConfig.CIRCLE_ID_HOT).equals(circleid)
		|| String.valueOf(QiupuConfig.CIRCLE_ID_NEAR_BY).equals(circleid)
		|| String.valueOf(QiupuConfig.CIRCLE_ID_PUBLIC).equals(circleid);
	}
	
	public static boolean inFilterStreamCircle(String circleid)
	{
		return String.valueOf(QiupuConfig.BLOCKED_CIRCLE).equals(circleid)
		|| String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE).equals(circleid);
	}
	
	static String platform = "";
	public static String fetch_cup_info()
	{
		if(platform.length() == 0)
		{
			try{
			    platform = Build.CPU_ABI.substring(0, 3);
			}catch(Exception ne){}
		}		
		return platform;
		
//		String result = null;
//		CMDExecute cmdexe = new CMDExecute();
//		try{
//			String[] arg = {"/system/bin/cat","/proc/cpuinfo"};
//			result = cmdexe.run(arg,"/system/bin/");
//		    Log.d(TAG,"result : "+ result);
//			
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//		return result;
	}
	
	
	//TODO only to test
	public static void setORM(QiupuORM qiupuorm)
	{
		orm = qiupuorm;
	}
	public static QiupuORM getORM()
	{
		return orm;
	}

    /**
     * if the SD card is ready, verify is the target icon folder.
     * some extra work will be done for old code. 
     */
    public static void ensureAppIconPath(int versionCode) {
        if (versionCode > mVersionCode) {
            mVersionCode = versionCode;

            ensureAppIconPath(false, versionCode);
            if (getExternalStorageState().equals(MEDIA_MOUNTED)) {
                ensureAppIconPath(true, versionCode);
            }
        }
    }

    private static void ensureAppIconPath(boolean external, int versionCode) {
        if (null != orm) {
            final int lastVersion = orm.getLastUsedIconVersion(external);
            final String targetPath = external ? QiupuConfig.APP_ICON_SDCARD_PATH :
                    QiupuConfig.APP_CACHE_PHONE_PATH;
            try {
                if (versionCode > lastVersion) {
                    if (lastVersion < FORCE_UPDATE_CACHED_PROFILE_VERSION) {
                        resetCachedPath(targetPath);
                    }
                    orm.setLastUsedIconVersion(external, versionCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                File file = new File(targetPath);
                if (!file.exists()) {
                    file.mkdir();
                }
            }
        }
    }

    /**
     * try to make the path if not existing, and change the mode of path to 777
     * @param pathName
     */
    private static void tryMakePath(String pathName) {
        if (!new File(pathName).exists()) {
            new File(pathName).mkdirs();

            Process p;
            int status;
            try {
                p = Runtime.getRuntime().exec("chmod 777 " + pathName);
                status = p.waitFor();
                if (status == 0) {
                    Log.i(TAG, "tryMakePath, chmod succeed:" + pathName);
                } else {
                    Log.i(TAG, "tryMakePath, chmod failed:" + pathName);
                }
            } catch (Exception ex) {
                Log.i(TAG, "tryMakePath, chmod failed:" + pathName);
                ex.printStackTrace();
            }
        }
    }

    public static void createAllWritableFolder(String pathName) {
        if (TextUtils.isEmpty(pathName)) {
            Log.d(TAG, "createAllWritableFolder, invalid path name: " + pathName);
            return;
        }

        File pathFile = new File(pathName);
        if (!pathFile.exists()) {
            pathFile.mkdirs();

            Process p;
            int status;
            try {
                p = Runtime.getRuntime().exec("chmod 777 " +  pathName);
                status = p.waitFor();
                if (status == 0) {
                    Log.i(TAG, "createAllWritableFolder, chmod succeed, " + pathName);
                } else {
                    Log.i(TAG,"createAllWritableFolder, chmod failed, " + pathName);
                }
            } catch(Exception ex) {
                Log.i(TAG,"createAllWritableFolder, chmod exception, " + pathName);
                ex.printStackTrace();
            }
        }
    }

    public static String encodeLocalIconPath(String pkgName, int versionCode) {
//        return "http://local/" + pkgName + "-" + String.valueOf(versionCode) + ".png";
        return "http://local/" + pkgName + ".png";
    }

    private static String getAlterLocalImageFilePath(URL imageurl, boolean addHostAndPath) {
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

        Log.d(TAG, "getAlterLocalImageFilePath, get localFileName:" + localFileName + ", from:" + imageFilePath);
        if (addHostAndPath == false) {
            alterPath = tmpPath + new File(getImageFileName(localFileName)).getName();
        } else {
            String filename = removeChar(localFileName);
            filename = "local_" + filename;
            if (filename.contains("/")) {
                filename = filename.replace("/", "");
            }
            alterPath = tmpPath + new File(filename).getName();
        }
        
        Log.v(TAG, "getAlterLocalImageFilePath, return alter local path:" + alterPath);
        return alterPath;
    }
    
    public static void updateActivityUI(final QiupuUser user){

		synchronized(QiupuHelper.userlisteners)
        {
			Log.d(TAG, "userlisteners.size() : " + QiupuHelper.userlisteners.size());
            Set<String> set = QiupuHelper.userlisteners.keySet();
            Iterator<String> it = set.iterator();
            while(it.hasNext())
            {
                String key = it.next();
                UsersActionListner listener = QiupuHelper.userlisteners.get(key).get();
                if(listener != null)
                {
                    listener.updateItemUI(user);
                }
            }      
        }      
    }

    public static String getBorqsURL()
	{
		if(orm == null)
		{
			return ConfigurationBase.DEFAULT_BORQS_URL;
		}

		// used by new platform
//		if(orm.isUserNewPlatform()) {
//		    return ConfigurationBase.NEW_PLATFORM_URL;
//		}

        // Should this always query db be cached for performance.
        final String apiUrl = orm.getCurrentApiUrl();
        if (!TextUtils.isEmpty(apiUrl)) {
            return apiUrl;
        } else {
            if(orm.isUsingTestURL())
            {
                return ConfigurationBase.DEFAULT_BORQS_URL_TEST;
            }
            else
            {
                return ConfigurationBase.DEFAULT_BORQS_URL;
            }
        }
	}

//    public static boolean isUsingNewPlatform() {
//   	    if(orm == null) {
//   	        return false;
//   	    }else {
//   	        return orm.isUserNewPlatform();
//   	    }
//   	}

    public static boolean isOpenPublicCircle() {
           if(orm == null) {
               return false;
           }else {
               return orm.isOpenPublicCircle();
           }
       }
    
    public static String getSceneId() {
        if(orm == null) {
            return "";
        }else {
            return orm.getSettingValue(QiupuORM.HOME_ACTIVITY_ID);
        }
    }
    
    public static final HashMap<String,WeakReference<ActivityFinishListner>> finishListener = 
    		new HashMap<String,WeakReference<ActivityFinishListner>>();
    
    public static void registerFinishListner(String key,ActivityFinishListner listener){
    	synchronized(finishListener) {
    		WeakReference<ActivityFinishListner> ref = finishListener.get(key);
    		if(ref != null && ref.get() != null) {
    			ref.clear();
    		}
    		finishListener.put(key, new WeakReference<ActivityFinishListner>(listener));
    	}
    }
    
    public static void unregisterFinishListner(String key){
    	synchronized(finishListener) {
    		WeakReference<ActivityFinishListner> ref = finishListener.get(key);
    		if(ref != null && ref.get() != null) {
    			ref.clear();
    		}
    		finishListener.remove(key);
    	}
    }
    
    public static final HashMap<String,WeakReference<RequestRefreshListner>> requestrefreshListener = 
    		new HashMap<String,WeakReference<RequestRefreshListner>>();
    
    public static void registerRequestRefreshListner(String key,RequestRefreshListner listener){
    	synchronized(requestrefreshListener) {
    		WeakReference<RequestRefreshListner> ref = requestrefreshListener.get(key);
    		if(ref != null && ref.get() != null) {
    			ref.clear();
    		}
    		requestrefreshListener.put(key, new WeakReference<RequestRefreshListner>(listener));
    	}
    }
    
    public static void unregisterRequestRefreshListner(String key){
    	synchronized(requestrefreshListener) {
    		WeakReference<RequestRefreshListner> ref = requestrefreshListener.get(key);
    		if(ref != null && ref.get() != null) {
    			ref.clear();
    		}
    		requestrefreshListener.remove(key);
    	}
    }

    public static final HashMap<String,WeakReference<UpdateRequestCountListener>> requestChangeCountListener = 
                new HashMap<String,WeakReference<UpdateRequestCountListener>>();

    public static void registerUpdateRequestCountListener(String key,UpdateRequestCountListener listener){
        synchronized(requestChangeCountListener) {
            WeakReference<UpdateRequestCountListener> ref = requestChangeCountListener.get(key);
            if(ref != null && ref.get() != null) {
                ref.clear();
            }
            requestChangeCountListener.put(key, new WeakReference<UpdateRequestCountListener>(listener));
        }
    }

    public static void unregisterUpdateRequestCountListener(String key){
        synchronized(requestChangeCountListener) {
            WeakReference<UpdateRequestCountListener> ref = requestChangeCountListener.get(key);
            if(ref != null && ref.get() != null) {
                ref.clear();
            }
            requestChangeCountListener.remove(key);
        }
    }

    public static final HashMap<String,WeakReference<NotifyActionListener>> notificationListeners = new HashMap<String,WeakReference<NotifyActionListener>>();

    public static void registerNotificationListener(String key, NotifyActionListener listener){
        synchronized(notificationListeners) {
            WeakReference<NotifyActionListener> ref = notificationListeners.get(key);
            if(ref != null && ref.get() != null) {
                ref.clear();
            }
            notificationListeners.put(key, new WeakReference<NotifyActionListener>(listener));
        }
    }

    public static void unregisterNotificationListener(String key){
        synchronized(listeners) {
            WeakReference<NotifyActionListener> ref = notificationListeners.get(key);
            if(ref != null && ref.get() != null) {
                ref.clear();
            }
            notificationListeners.remove(key);
        }
    }

    // listen for deleting a file/folder, and then create the empty one again.
    private static class ResetCacheListener extends FileObserver {
        private String listenPath;
        public ResetCacheListener(String path) {
            super(path, FileObserver.DELETE_SELF);
            listenPath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            switch (event) {
                case FileObserver.DELETE_SELF:
                    if (TextUtils.isEmpty(listenPath)) {
                        Log.w(TAG, "ResetCacheListener.onEvent, empty path");
                    } else {
                        try {
                            File file = new File(listenPath);
                            if (file.exists()) {
                                Log.e(TAG, "ResetCacheListener.onEvent, not deleted: " + listenPath);
                            } else {
                                Log.v(TAG, "ResetCacheListener.onEvent, create again: " + listenPath);
                                Runtime runtime = Runtime.getRuntime();
                                final String mkdirCmd = "mkdir -p " + listenPath;
                                runtime.exec(mkdirCmd);
                            }

                            ResetCacheListener listener = listenerHashMap.remove(listenPath);
                            listener.stopWatching();
                        } catch (IOException e) {
                            Log.i(TAG, "deleted path could not create: " + path);
                        }
                    }
                    break;
            }
        }
    }


    //check the available space
    private static boolean isLowStorage() {
        try {
            final String expectedPath;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                expectedPath = "";
            } else {
                expectedPath = "/data/data/";
            }
            if (!QiupuConfig.isEnoughSpace(expectedPath, remainSize)) {
                Log.w(TAG, "isLowStorage, no enough space in path: " + expectedPath);
                return true;
            }
        } catch (Exception ne) {
        }

        return false;
    }

    private static boolean downloadImageFromInternet(URL imageUrl, File filep) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            TrafficStats.setThreadStatsTag(0xB0AC);
        //get bitmap
        HttpURLConnection conn = null;
        FileOutputStream fos = null;
        try {
            String filepath = filep.getAbsolutePath();

            fos = new FileOutputStream(filep);

            conn = (HttpURLConnection) imageUrl.openConnection();

            if (HttpsURLConnection.class.isInstance(conn)) {
                myHostnameVerifier passv = new myHostnameVerifier();
                ((HttpsURLConnection) conn).setHostnameVerifier(passv);
            }

            conn.setConnectTimeout(15 * 1000);
            conn.setReadTimeout(30 * 1000);
            InputStream in = conn.getInputStream();

            int retcode = conn.getResponseCode();
            if (retcode == 200) {
                final long totalLength = conn.getContentLength();

                long downlen = 0;
                int len = -1;
                byte[] buf = new byte[1024 * 4];
                while ((len = in.read(buf, 0, 1024 * 4)) > 0) {
                    downlen += len;
                    fos.write(buf, 0, len);
                }
                buf = null;

                if (totalLength == downlen) {
                    if (QiupuConfig.DBLOGD) Log.d(TAG, "downloadImageFromInternet, to file: " + filepath);
                } else {

                }
            }

            fos.close();

            if (QiupuConfig.DBLOGD) Log.d(TAG, "downloadImageFromInternet, to file: " + filepath);
        } catch (IOException ne) {
            Log.e(TAG, "fail to get image=" + ne.getMessage());
            return false;
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                TrafficStats.clearThreadStatsTag();
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception ne) {
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ne) {
                }
            }
        }

        return true;
    }

    private static final HashMap<String, WeakReference<RefreshComposeItemActionListener>> composeListeners = new HashMap<String,WeakReference<RefreshComposeItemActionListener>>();
    public static void registerComposeListener(String key, RefreshComposeItemActionListener listener){
        synchronized(likeListeners) {
            WeakReference<RefreshComposeItemActionListener> ref = composeListeners.get(key);
            if(ref != null && ref.get() != null) {
                ref.clear();
            }
            composeListeners.put(key, new WeakReference<RefreshComposeItemActionListener>(listener));
        }
    }

    public static void unRegisterComposeListener(String key){
        synchronized(composeListeners) {
            WeakReference<RefreshComposeItemActionListener> ref = composeListeners.get(key);
            if(ref != null && ref.get() != null) {
                ref.clear();
            }
            composeListeners.remove(key);
        }
    }

    public static void refreshItemUI(ComposeShareData data) {
        synchronized (QiupuHelper.composeListeners) {
            Set<String> set = QiupuHelper.composeListeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                WeakReference<RefreshComposeItemActionListener> ref = QiupuHelper.composeListeners.get(key);
                if (ref != null && ref.get() != null) {
                    ref.get().refreshComposeItemUI(data);
                }
            }
        }
    }
    
    public static boolean isZhCNLanguage(Context context) {
    	String language = context.getResources().getConfiguration().locale.getLanguage();
    	Log.d(TAG, "current language " + language);
    	if(language != null && language.contains("zh")) {
    		return true;
    	}else {
    		return false;
    	}
    }
    
public static final HashMap<String,WeakReference<PageActionListener>> pagelisteners = new HashMap<String,WeakReference<PageActionListener>>();
	
	public static void registerPageListener(String key,PageActionListener listener){
		synchronized(pagelisteners)
		{
			WeakReference<PageActionListener> ref = pagelisteners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			pagelisteners.put(key, new WeakReference<PageActionListener>(listener));
		}
	}
	
	public static void unregisterPageListener(String key){
		synchronized(pagelisteners)
		{
			WeakReference<PageActionListener> ref = pagelisteners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			pagelisteners.remove(key);
		}
	}
	
	public static void updatePageActivityUI(final PageInfo info){

		synchronized(QiupuHelper.pagelisteners)
        {
			Log.d(TAG, "pagelisteners.size() : " + QiupuHelper.pagelisteners.size());
            Set<String> set = QiupuHelper.pagelisteners.keySet();
            Iterator<String> it = set.iterator();
            while(it.hasNext())
            {
                String key = it.next();
                PageActionListener listener = QiupuHelper.pagelisteners.get(key).get();
                if(listener != null)
                {
                    listener.refreshpage(info);
                }
            }      
        }      
    }
	
	public static void updateCirclesUI(){

		synchronized(QiupuHelper.refreshCirclelisteners)
        {
			Log.d(TAG, "pagelisteners.size() : " + QiupuHelper.refreshCirclelisteners.size());
            Set<String> set = QiupuHelper.refreshCirclelisteners.keySet();
            Iterator<String> it = set.iterator();
            while(it.hasNext())
            {
                String key = it.next();
                RefreshCircleListener listener = QiupuHelper.refreshCirclelisteners.get(key).get();
                if(listener != null)
                {
                    listener.refreshUi();
                }
            }      
        }      
    }

    // dynamically detect profile url with format:
    // http://storage.aliyun.com/wutong-data/media/photo/profile_10000_1354692110163_M.jpg
    private static boolean isProfileUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            return url.startsWith("http://storage.aliyun.com/wutong-data/media/photo/profile_")
                    && url.endsWith("_M.jpg");
        }

        return false;
    }
    
    public static interface DropDownDialogListener {
    	public void DialogConfigurationChanged(Configuration newConfig);
    }
    private static final HashMap<String,WeakReference<DropDownDialogListener>> dropdownlisteners = new HashMap<String,WeakReference<DropDownDialogListener>>();
    
	public static void registerDropDownListener(String key,DropDownDialogListener listener){
		synchronized(dropdownlisteners)
		{
			WeakReference<DropDownDialogListener> ref = dropdownlisteners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			dropdownlisteners.put(key, new WeakReference<DropDownDialogListener>(listener));
		}
	}
	
	public static void unregisterDropDownListener(String key){
		synchronized(dropdownlisteners)
		{
			WeakReference<DropDownDialogListener> ref = dropdownlisteners.get(key);
			if(ref != null && ref.get() != null)
			{
				ref.clear();
			}
			dropdownlisteners.remove(key);
		}
	}
	
	public static void updateDropDownDialogUI(Configuration newConfig){
		
		synchronized(QiupuHelper.dropdownlisteners)
		{
			Log.d(TAG, "dropdownlisteners.size() : " + QiupuHelper.dropdownlisteners.size());
			Set<String> set = QiupuHelper.dropdownlisteners.keySet();
			Iterator<String> it = set.iterator();
			while(it.hasNext())
			{
				String key = it.next();
				DropDownDialogListener listener = QiupuHelper.dropdownlisteners.get(key).get();
				if(listener != null)
				{
					listener.DialogConfigurationChanged(newConfig);
				}
			}      
		}      
	}
	
	public static  HashMap<String, String> organizationContactMap(String col, String val, Map<String, String> map) {
        HashMap<String, String> contactInfoMap = new HashMap<String, String>();
        contactInfoMap.put(col, val);
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (!key.equals(col)) {
                if (value.length() > 0) {
                    contactInfoMap.put(key, value);
                }
            }
        }
        return contactInfoMap;
    }

    private void initContactInfomap(Map<String, String> map, QiupuORM orm) {
        Cursor cursor = orm.queryOneUserPhoneEmail(AccountServiceUtils
                .getBorqsAccountID());
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String type = cursor.getString(cursor.getColumnIndex(PhoneEmailColumns.TYPE));
                String info = cursor.getString(cursor.getColumnIndex(PhoneEmailColumns.INFO));
                map.put(type, info);
            } while (cursor.moveToNext());
            cursor.close();
            cursor = null;
        }else {
            Log.d(TAG, "need load myself info from server");
        }
    }
}
