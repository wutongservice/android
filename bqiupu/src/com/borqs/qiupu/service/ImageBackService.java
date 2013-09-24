package com.borqs.qiupu.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.borqs.qiupu.QiupuConfig;
import twitter4j.QiupuSimpleUser;

import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

//try to get the image in background service
public class ImageBackService 
{
    final String TAG="ImageBackService";
        
    private FacebookUserObserver    facebookObserver;
    Handler handler;
    QiupuORM orm;
    Context   mContext;
    List<String >urls = new ArrayList<String>();    
    public ImageBackService(Context con, QiupuORM orm)
    {
        handler = new ImageHandler();
        this.orm = orm;
        
        //just first time
        handler.obtainMessage(RE_GET_URLS).sendToTarget();
        mContext = con;
        init();
    }
    private void init()
    {
    	facebookObserver         = new FacebookUserObserver();
    	mContext.getContentResolver().registerContentObserver(QiupuORM.USERS_CONTENT_URI, true, facebookObserver);    	
    }
    
    public void Stop()
    {
    	mContext.getContentResolver().unregisterContentObserver(facebookObserver);
    }
    
    static long nLastFUserID=-1;
    private class FacebookUserObserver extends ContentObserver 
	{
    	public FacebookUserObserver() 
	    {
	        super(new Handler());	    
	    }
	    
	    @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
	    
	    @Override
	    public void onChange(boolean selfChange) 
	    {		    	
	    	QiupuORM.sWorker.post(new Runnable()
		    {
				@Override
				public void run()
				{
					String next = "";
			    	QiupuSimpleUser user = orm.getLastSimpleQiupuUser();
			    	if(user!=null && user.uid != nLastFUserID)
			    	{
			    		nLastFUserID = user.uid;
			    		next = user.profile_image_url;
			    	}
			    	else
			    	{
			    		return;
			    	}
			    	
			    	if(next != null && next.length()>0)
			    	{
				    	synchronized(urls)
				    	{
				    		urls.add(next);
				    	}
				    	if(QiupuConfig.DBLOGD) Log.d(TAG, "new user is coming="+user.nick_name);
				    	Message msd = handler.obtainMessage(TRY_TO_GET_IMAGE);
			            msd.getData().putString("imageurl", next);
		                handler.sendMessage(msd);
			    	}
			    	
			    	user.despose();
			    	user = null;						
				}		    		
		    });
	    }
	}
    
    
    private String nextvalue()
    {
    	String value="";
    	synchronized(urls)
    	{        
	        if(urls.size()>0)
	        {
	            value = urls.get(0);
	            urls.remove(0);
	        }
	        else
	        {
	            //reget the urls
	            //reGetTheData();            
	        }
    	}
        return value;
    }
    
    private void reGetTheData()
    {
        synchronized(urls)
        {
            //try to get urls from database
            urls.clear();            
            urls.addAll(orm.getUserImagesByuid());
            
            String next = nextvalue();
            if(next.length() > 0)
            {
                Message msd = handler.obtainMessage(TRY_TO_GET_IMAGE);
                msd.getData().putString("imageurl", next);
                handler.sendMessageDelayed(msd, 10*60*1000);
            }   
        }
    }
    
    private void removepath(String url)
    {
        Log.d(TAG, "get image from url="+url);
    }
    
    final int TRY_TO_GET_IMAGE    =0;
    final int TRY_TO_GET_IMAGE_END=1;
    final int RE_GET_URLS         =2;
    private class ImageHandler extends Handler 
    {
        public ImageHandler()
        {
            super();            
            Log.d(TAG, "new ImageHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case TRY_TO_GET_IMAGE:
                {
                    final String url = msg.getData().getString("imageurl");
                    
                    final String localpath = QiupuHelper.isImageExistInPhone(url, true);
                    if(localpath == null || new File(localpath).exists() == false)
                    {
	                    if(url != null && url.length()>0)
	                    {
	                    	//dispatch to thread pool		                           
		                    ImageRun.getThreadPool().dispatch(new Runnable()
		                    {
		                        public void run() 
		                        {
		                            String localpath = QiupuHelper.getImagePathFromURL(mContext.getApplicationContext(), url, true);
		                            if(localpath != null)
		                            {
//		                            	QiupuHelper.setRoundedCornerBitmap(localpath);
		                                removepath(localpath);
		                            }
		                        }
		                        
		                    });	                    
		                    
		                    String next = nextvalue();                   
		                    Message msd = handler.obtainMessage(TRY_TO_GET_IMAGE);
		                    msd.getData().putString("imageurl", next);
		                    handler.sendMessageDelayed(msd, 30*1000);    
	                    }    
	                    else
	                    {
	                        //if no url, stop to loop to save battery
	                    }
                    }
                    else
                    {
                    	//Log.d(TAG, "url image exist="+url + " path="+filepath);
                    }
                    
                    break;
                } 
                case TRY_TO_GET_IMAGE_END:
                {
                    break;
                } 
                case RE_GET_URLS:
                {
                    Log.d(TAG, "reget the image from url");
                    reGetTheData();
                    
                    break;
                }
            }
        }
    }
}
