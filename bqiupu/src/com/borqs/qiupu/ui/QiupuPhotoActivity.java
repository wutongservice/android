package com.borqs.qiupu.ui;

import java.io.File;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.borqs.common.adapter.PicsAdapter;
import com.borqs.common.view.ImageViewTouchBase;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;

public class QiupuPhotoActivity extends BasicActivity
{
	private String TAG = "Qiupu.QiupuPhotoActivity";
	private ArrayList<String> link_list;
	private int current_item;
	private ViewPager mPager;
	private PicsAdapter mAdapter;
	private Button zoom_small;
	private Button zoom_big;	
	private ImageViewTouchBase photo_image;
	
	private String photoURL;	
	
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);    

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.photo_view_ui);
        
        current_item = getIntent().getIntExtra("current_item", 0);
        link_list =  getIntent()
				.getStringArrayListExtra("link_list");
//        photoURL = this.getIntent().getStringExtra("photo");
        mPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new PicsAdapter(getSupportFragmentManager(),
        		link_list);
		mPager.setAdapter(mAdapter);
		mPager.setCurrentItem(current_item);
        
//        zoom_small = (Button)this.findViewById(R.id.zoom_small);
//        zoom_small.setId(0);
//        zoom_big = (Button)this.findViewById(R.id.zoom_big);
//        zoom_big.setId(1);
//        zoom_small.setOnClickListener(zoomClick);
//        zoom_big.setOnClickListener(zoomClick);
//        
//
//               
//        photo_image = (ImageViewTouchBase)this.findViewById(R.id.photo_image);  
//        if(photo_image != null)
//        {
//        	photo_image.context = this;
//        }
//        
//        setUI();        
    }


	@Override
	protected void createHandler() {
		// TODO Auto-generated method stub
		
	}

//    View.OnClickListener zoomClick = new View.OnClickListener()
//    {
//        public void onClick(View v) {
//            if(v.getId() == 0)
//            {
//                scaleSmall();
//            }
//            else if(v.getId() == 1)
//            {
//                scaleLarge();
//            }
//        }      
//    };
//    
//	private void launchGetPhotoInfoByPID(String pid)
//	{
//	    Message msd = mHandler.obtainMessage(PHOTO_GET_BEGIN);
//	    msd.getData().putString("pid", pid);
//	    msd.sendToTarget();
//	}	
//
//	protected void setZoom(float scale)
//	{
//	    Bitmap bmp = photo_image.getDrawingCache();
//	    if(bmp != null)
//	    {
//	        
//	    }
//	}
//	
//	int mX = 0;
//	int mY = 0;
//	private void scaleSmall() {
//		Log.d(TAG, "scaleSmall");
//	    int width  = photo_image.getWidth();
//        int height = photo_image.getHeight();
//        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
//        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
//        
//        if(width>screenWidth && width > 200)
//        {
//            //screenHeight = (int)(((float)screenWidth * (float)height)/(float)width);
//            if(height<screenHeight)
//            {
//                photo_image.scrollBy(-mX, 0);
//            }
//            else
//            {
//                photo_image.scrollBy(-mX, -mY);
//            }
//        }
//        
//        Log.d(TAG, "width="+width + " height="+height+" screenWidth="+screenWidth+"screenHeight="+screenHeight); 
//        if(width > 200)
//        {
//            int afterwidth  = (int)(width*0.9);
//            int afterheight =  (int)(height*0.9);
//            LayoutParams params = photo_image.getLayoutParams();
//            Log.d(TAG, params.getClass().getName());
//            params.height = afterheight;
//            params.width  = afterwidth;
//            photo_image.setLayoutParams(params);
//            
//            if(afterwidth > screenWidth)
//            {
//                if(afterheight < screenHeight)
//                {
//                    mX = (int)((afterwidth - screenWidth)/2);
//                    mY = 0;
//                    photo_image.scrollBy(mX, mY);
//                }
//                else
//                {
//                    mX = (int)((afterwidth - screenWidth)/2);
//                    mY = (int)((afterheight - screenHeight)/2);
//                    photo_image.scrollBy(mX, mY);
//                }
//            }
//        }
//	}
//
//    private void scaleLarge() {
//        int width  = photo_image.getWidth();
//        int height = photo_image.getHeight();
//        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
//        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
//        
//        if(width>screenWidth && width < 2000)
//        {
//            screenHeight = (int)(((float)screenWidth * (float)height)/(float)width);
//            if(height<screenHeight)
//            {
//                photo_image.scrollBy(-mX, 0);
//            }
//            else
//            {
//                photo_image.scrollBy(-mX, -mY);
//            }
//        }
//        Log.d(TAG, "width="+width + " height="+height+" screenWidth="+screenWidth+"screenHeight="+screenHeight); 
//
//        if(width < 2000)
//        {
//            int afterwidth  = (int)(width*1.1);
//            int afterheight =  (int)(height*1.1);
//            LayoutParams params = photo_image.getLayoutParams();
//            Log.d(TAG, params.getClass().getName());
//            params.height = afterheight;
//            params.width  = afterwidth;
//            photo_image.setLayoutParams(params);
//            if(afterwidth > screenWidth)
//            {
//                if(afterheight < screenHeight)
//                {
//                    mX = (int)((afterwidth - screenWidth)/2);
//                    mY = 0;
//                    photo_image.scrollBy(mX, mY);
//                }
//                else
//                {
//                    mX = (int)((afterwidth - screenWidth)/2);
//                    mY = (int)((afterheight - screenHeight)/2);
//                    photo_image.scrollBy(mX, mY);
//                }
//            }
//        }
//    }
//   
//    
//    private void setImage()
//    {
//        String filebigPath = QiupuHelper.getImagePathFromURL_noFetch(photoURL);
//        if(filebigPath != null && new File(filebigPath).exists() && new File(filebigPath).length() > 0)
//        {
//            Log.d(TAG, "I have big one, no need use the small one");
//            try{                    
//                Bitmap tmp = BitmapFactory.decodeFile(filebigPath);
//                photo_image.setImageBitmap(tmp);
//            }
//            catch(Exception ne)
//            {
//                
//            }
//        }        
//    }
//    
//	private void setUI() 
//	{   
//	    if(photo_image != null)
//	    {
//	        photo_image.scrollTo(0, 0);
//	    }
//	    
//	    
//		photo_image.setImageResource(R.drawable.photo_downloading);
//		String filebigPath = QiupuHelper.getImagePathFromURL_noFetch(photoURL);
//		if(filebigPath != null && new File(filebigPath).exists() && new File(filebigPath).length()>0)
//		{
//			Log.d(TAG, "I have big one, no need use the small one");
//			try{				    
//	    	    Bitmap tmp = BitmapFactory.decodeFile(filebigPath);
//	    	    photo_image.setImageBitmap(tmp);
//		    }
//			catch(Exception ne)
//			{}
//		}
//		else
//		{   
//		    ImageRun imagerun = new ImageRun(mHandler,photoURL, 0);
//            imagerun.noimage = true;
//            imagerun.setImageView(photo_image);
//            imagerun.post(imagerun); 
//            
//			try{
//			    Log.d(TAG, "big not exist, so set small one");
//			    String filePath = QiupuHelper.getImagePathFromURL_noFetch(photoURL);
//			    if(filePath != null && new File(filePath).exists() == true)
//	    	    {
//	    	        Bitmap tmp = BitmapFactory.decodeFile(filePath);
//	    	        photo_image.setImageBitmap(tmp);	    	        
//	    	    }
//		    }catch(Exception ne){Log.d(TAG, "set small one="+ne.getMessage());}
//		}		
//	}
//	
//	@Override
//	protected void createHandler() 
//	{
//		mHandler = new EditHandler();
//	}
//	
//	final static int PHOTO_GET_BEGIN       = 6;
//    final static int PHOTO_GET_UI          = 7;
//    final static int PHOTO_GET_END         = 8; 
//    
//    private class EditHandler extends Handler 
//    {
//        public EditHandler()
//        {
//            super();            
//            Log.d(TAG, "new EditHandler");
//        }
//        
//        @Override
//        public void handleMessage(Message msg) 
//        {
//            switch(msg.what)
//            {
//                case PHOTO_GET_BEGIN:
//                {
//                    break;
//                }
//                case PHOTO_GET_UI:
//                {
//                    setUI();
//                    break;
//                }
//                case PHOTO_GET_END:
//                {
//                    end();
//                    break;
//                }
//            }
//        }
//    }    
}
