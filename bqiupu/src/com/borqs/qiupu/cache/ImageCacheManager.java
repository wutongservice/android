package com.borqs.qiupu.cache;

import java.lang.ref.WeakReference;
import java.util.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.ui.BasicActivity;

public class ImageCacheManager {
    final static String TAG="ImageCacheManager";
    int cacheSize = 50; //it is too big, we just 50
    List<ImageCache> caches = new ArrayList<ImageCache>();
    boolean mhasLimitation = true;
        
    private static ImageCacheManager _instance;
    public static ImageCacheManager instance()
    {
    	if(_instance == null)
    	{
    		_instance = new ImageCacheManager();
    	}
    	
    	return _instance;
    }
    
    
    public static ImageCacheManager buildNewInstance(int cachesize, boolean hasLimitation)
    {
    	return new ImageCacheManager(cachesize, hasLimitation);    	
    }
    
    private ImageCacheManager()
    {
    	
    }
    
    private ImageCacheManager(int cachesize, boolean hasLimitation)
    {
    	cacheSize = cachesize;
    	mhasLimitation = hasLimitation;
    }
    
    public static int maxCacheWidth=100;
    
    //TODO
    private static boolean recyclesmalllicon=false;
    
    public boolean addCache(String url, Bitmap bmp)
    {
        if (null == bmp) {
            if (QiupuConfig.DBLOGD) Log.d(TAG, "addCache, invalid bitmap, url: " + url);
            return false;
        }
        if (!mhasLimitation) {
            if (QiupuConfig.DBLOGD) Log.d(TAG, "addCache, exceed limitation, url: " + url);
            return false;
        } else if (bmp.getHeight() <= 0 || bmp.getWidth() <= 0) {
            if (QiupuConfig.DBLOGD) Log.d(TAG, "addCache, invalid size: height=" + bmp.getHeight() +
                    ", width=" + bmp.getWidth() + ", " + url);
            return false;
        } else if (bmp.getHeight() > maxCacheWidth || bmp.getWidth() > maxCacheWidth) {
            if (QiupuConfig.DBLOGD) Log.d(TAG, "addCache, too big: height=" + bmp.getHeight() +
                    ", width=" + bmp.getWidth() + ", " + url);
            return false;
        } else {
            synchronized (caches) {
                ImageCache cache = new ImageCache();
                cache.url = url;
                cache.bmp = bmp;
                cache.age = System.currentTimeMillis();

                caches.add(0, cache);

                //TODO
                //need refactor in future
                if(recyclesmalllicon)
                {
	                if (caches.size() > cacheSize) {
	                    //remove last 5 items
	                    for (int i = 0; i < cacheSize / 8; i++) {
	                        ImageCache item = caches.remove(caches.size() - 1);
	                        item.bmp.recycle();
	                        item.bmp = null;
	                        item.url = null;
	                        item.age = 0;
	                        item = null;
	                    }
	                }
                }
            }

            return true;
        }
    }
    
    public void dump()
    {
        Log.d(TAG, "size="+caches.size());
        long memsize=0;
        for(int i=0;i<caches.size();i++)
        {
            memsize += caches.get(i).bmp.getRowBytes();
            Log.d(TAG, "item: "+caches.get(i).url + " bmp="+caches.get(i).bmp.getRowBytes());
        }
        Log.d(TAG, "totle size: "+memsize); 
    }
    
    public ImageCache getCache(String key)
    {
    	synchronized(caches)
    	{
	        for(int i=0;i<caches.size();i++)
	        {
	            ImageCache cache = caches.get(i);
	            if(cache.url.equalsIgnoreCase(key))
	            {
	                Log.d(TAG, "get from cache");
	                cache.ref++;
	                caches.remove(cache);
                    caches.add(0, cache);
	                return cache;
	            }
	        }
    	}
        return null;
    }
    
    public boolean isCacheHit(String key)
    {
        synchronized(caches)
        {
            for(ImageCache cache : caches)
            {
                if(cache.url.equalsIgnoreCase(key))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public class ImageCache
    {
        public String url;
        public Bitmap bmp;
        public long   age;
        public int    ref;
        
        //for compare
        public boolean equals(Object ref)
        {            
            if(url.equalsIgnoreCase(((ImageCache)ref).url))
                return true;
            else
                return false;
        }
        
        //for sort
        public int compareTo(ImageCache ref)
        {
            if(age >=ref.age)
            {
                return 1;
            }
            else
            {
               return -1; 
            }
        } 
    }

	public void despose() {
		for(ImageCache ic:caches)
        {
            if(ic.bmp != null && ic.bmp.isRecycled() == false)
            {
            	ic.bmp.recycle();
            }
        }	
		
		caches.clear();
	}

    // only cache image in context life cycle, and release when the context is gone.
    public static class ContextCache {
        static Map<WeakReference<Context>, ArrayList<Bitmap>> imageMap = new HashMap<WeakReference<Context>, ArrayList<Bitmap>>();
        static Map<String, WeakReference<Bitmap>> bmpurl = new HashMap<String, WeakReference<Bitmap>>();

        public static boolean hasCachedBMPInContext(Context con, Bitmap bmp) {
            boolean haveCached = false;
            Set<WeakReference<Context>> sets = imageMap.keySet();
            Iterator<WeakReference<Context>> it = sets.iterator();
            while (it.hasNext()) {
                try {
                    WeakReference<Context> ref = it.next();
                    if (ref != null && ref.get() != null) {
                        Context context = ref.get();
                        if (BasicActivity.class.isInstance(context) &&
                                BasicActivity.class.isInstance(con) &&
                                context.equals(con)) {
                            ArrayList<Bitmap> bmps = imageMap.get(ref);
                            if (bmps != null) {
                                for (Bitmap item : bmps) {
                                    if (item.equals(bmp)) {
                                        haveCached = true;
                                        break;
                                    }
                                    Log.e(TAG, "find cache bmp " + bmp + " in context=" + con);
                                }
                            }

                        }
                    }
                } catch (Exception ne) {
                    Log.e(TAG, "check bmp exception=" + ne.getMessage());
                }
            }

            return haveCached;
        }

        static WeakReference<Bitmap> createReference(Bitmap bmp)
        {
        	return new WeakReference<Bitmap>(bmp);
        }
        
        public static void putBitmapIntoMap(Context con, Bitmap bmp, String url) {
            bmpurl.put(url + con.getClass().getName(), createReference(bmp));

            boolean haveCurrentContext = false;
            Set<WeakReference<Context>> sets = imageMap.keySet();
            Iterator<WeakReference<Context>> it = sets.iterator();
            while (it.hasNext()) {
                try {
                    WeakReference<Context> ref = it.next();
                    if (ref != null && ref.get() != null) {
                        Context context = ref.get();
                        if (BasicActivity.class.isInstance(context) &&
                                BasicActivity.class.isInstance(con) &&
                                context.equals(con)) {
                            haveCurrentContext = true;
                            ArrayList<Bitmap> bmps = imageMap.get(ref);
                            if (bmps == null) {
                                bmps = new ArrayList<Bitmap>();
                            }

                            //
                            bmps.add(bmp);
                            imageMap.put(ref, bmps);
                            Log.e(TAG, "put bmp " + bmp + " context=" + con + " url=" + url);

                            break;
                        } else if (context.equals(con)) {
                            /// TODO: the cached context exists but is not expected one.
                            Log.i(TAG, "putBitmapIntoMap, unexpected case will fall through.");
                        }
                    }
                } catch (Exception ne) {
                    Log.e(TAG, "put bmp exception=" + ne.getMessage());
                }
            }

            /// TODO: As it was not track by the context that will invoke revokeAllImageView(),
            /// such cached item might be leak as there was not opportunity to recycle the bitmap.
            if (haveCurrentContext == false) {
                ArrayList<Bitmap> bmps = new ArrayList<Bitmap>();
                bmps.add(bmp);
                Log.e(TAG, "put bmp " + bmp + " context=" + con + " url=" + url);
                imageMap.put(new WeakReference<Context>(con), bmps);
            }
        }

        public static void revokeAllImageView(Context con) {
            synchronized (imageMap) {
                Set<WeakReference<Context>> sets = imageMap.keySet();
                Iterator<WeakReference<Context>> it = sets.iterator();
                while (it.hasNext()) {
                    WeakReference<Context> ref = it.next();
                    if (ref != null && ref.get() != null) {
                        Context context = ref.get();
                        if (BasicActivity.class.isInstance(context) &&
                                BasicActivity.class.isInstance(con) &&
                                context.equals(con)) {
                            ArrayList<Bitmap> bmps = imageMap.get(ref);
                            if (bmps != null) {
                                for (Bitmap bmp : bmps) {
                                    if (bmp != null && bmp.isRecycled() == false) {
                                        try {
                                            bmp.recycle();
                                            Log.d(TAG, "recycle bmp =" + bmp + " current context=" + con);
                                        } catch (Exception ne) {
                                            Log.e(TAG, "recyle bmp exception=" + ne.getMessage());
                                        }
                                    }
                                }
                                bmps.clear();
                            }
                            it.remove();
                            break;
                        }
                    }
                }
            }
        }

        public static Bitmap getImageFromCache(String url, String className) {
            final ImageCacheManager.ImageCache cache = ImageCacheManager.instance().getCache(url);
            Bitmap cacheBmp = null;
            if (cache != null && cache.bmp != null && cache.bmp.isRecycled() == false) {
                cacheBmp = cache.bmp;
            } else {
            	if(bmpurl.get(url + className) != null)
            	{
	                Bitmap tmpBmp = bmpurl.get(url + className).get();
	                if (tmpBmp != null && tmpBmp.isRecycled() == false) {
	                    cacheBmp = tmpBmp;
	                    Log.d(TAG, "get bitmap for url=" + url);
	                }
            	}
            }
            
            return cacheBmp;
        }
        
        
        public static void revokeImageViewByUrl(Context con,String url) {
            Log.v(TAG, "----------revokeImageViewByUrl---------------");
            Bitmap cacheBmp = null;
            if(bmpurl.get(url + con.getClass().getName()) != null)
        	{
	            Bitmap tmpBmp = bmpurl.get(url + con.getClass().getName()).get();
	            if (tmpBmp != null && tmpBmp.isRecycled() == false) {
	                cacheBmp = tmpBmp;
	            }
        	}
            if(cacheBmp == null) return;
            Set<WeakReference<Context>> sets = imageMap.keySet();
            Iterator<WeakReference<Context>> it = sets.iterator();
            while (it.hasNext()) {
                try {
                    WeakReference<Context> ref = it.next();
                    if (ref != null && ref.get() != null) {
                        Context context = ref.get();
                        if (context.equals(con)) {

                            ArrayList<Bitmap> bmps = imageMap.get(ref);
                            if (bmps != null) {
                                for (Bitmap item : bmps) {
                                    if (item.equals(cacheBmp)) {
                                        try {
                                            cacheBmp.recycle();
                                            imageMap.remove(item);
                                            bmpurl.remove(cacheBmp);
                                            Log.d(TAG, "recycle bmp =" + cacheBmp + " current context=" + con);
                                        } catch (Exception ne) {
                                            Log.e(TAG, "recyle bmp exception=" + ne.getMessage());
                                        }
                                        break;
                                    }
                                }
                            }

                        }
                    }
                } catch (Exception ne) {
                    Log.e(TAG, "check bmp exception=" + ne.getMessage());
                }
            }

        }

    }
}
