package com.borqs.common.view;

import com.borqs.qiupu.ui.BasicActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ImageViewTouchBase extends ImageView 
{
    public static final String TAG="ImageViewTouchBase";
    public Context context;
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE; 
    float  oldDist; 
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix(); 
    PointF mid = new PointF(); 
    PointF start = new PointF(); 
    
    public ImageViewTouchBase(Context context, AttributeSet attrs) {
        super(context, attrs);       
    }
    public ImageViewTouchBase(Context context) {
        super(context);
    }
    
    public ImageViewTouchBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        Log.d(TAG, "onKeyDown keycode="+keyCode + " event="+event);
        switch(event.getAction())
        {
            case KeyEvent.ACTION_DOWN:
            {
                break;
            }
            case KeyEvent.ACTION_UP:
            {
                break;
            }
        }
        return true;
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) 
    {
        Log.d(TAG, "onKeyUp keycode="+keyCode + " event="+event);
        
        scrollTo(-100+getLeft(), -100+getTop());
        return true;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) 
    {
        switch (ev.getAction()) 
        {
            case MotionEvent.ACTION_DOWN: 
            {
                offsetx = 0;
                offsety = 0;
                
                Log.d(TAG, "ACTION_DOWN ="+ev);
                startx = (int)ev.getX();                
                starty = (int)ev.getY();
                mode = DRAG;
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                Log.d(TAG, "ACTION_UP xspan="+ev);
                
                startx = 0;
                starty = 0;
                break;
            }  
            case MotionEvent.ACTION_MOVE:
            {
            	if (mode == DRAG) {
            		Log.d(TAG, "ACTION_MOVE xspan="+ev);
                    int mx = (int)ev.getX();                
                    int my = (int)ev.getY();
                  
                    int nSpanx = startx-mx;
                    int nSpany = starty-my;
                    
                    Log.d(TAG, "nSpanx="+nSpanx + " nSpany="+nSpany + " mx="+mx + " my="+my + " width="+getWidth() + " left="+getLeft());
                    if(getRight()> 100 && getBottom()>100 && getTop() < 400 &&  getLeft() < 400)
                    {
                        scrollBy(nSpanx, nSpany);
                        //return true;
                    }
                    
                    startx = mx;
                    starty = my;
                 }
                else if (mode == ZOOM) 
                {
                    float newDist = spacing(ev);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 10f) 
                    {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                 } 
                break;
            }
            
            case MotionEvent.ACTION_POINTER_DOWN://TODO do not work
            {
            	oldDist = spacing(ev);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 10f) {
                   savedMatrix.set(matrix);
                   midPoint(mid, ev);
                   mode = ZOOM;
                   Log.d(TAG, "mode=ZOOM" );
                } 
            	break;
            }
        }
        this.setImageMatrix(matrix);  
        return true;
    }
    
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
     } 
    
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
     } 
    
    
    @Override
	public void setImageBitmap(Bitmap bm) {
		if(context != null && BasicActivity.class.isInstance(context) && bm!=null)
		{
			//reset photo_image size;
			int width = bm.getWidth();
			int height = bm.getHeight();
			int screenWidth = ((BasicActivity)context).getWindowManager().getDefaultDisplay().getWidth();
			int screenHeight = ((BasicActivity)context).getWindowManager().getDefaultDisplay().getHeight();
		    
			if(screenWidth < screenHeight)
			{
			    //portrait mode
			    screenHeight = (screenWidth * height)/width;
	            
	            if(width < screenWidth)
	            {
	                LayoutParams params = this.getLayoutParams();
	                params.width = screenWidth;
	                params.height = screenHeight;
	            }
			}
			else
			{
			    //landspace mode
                screenWidth = (screenHeight * width)/height;
                if(width < screenWidth)
                {
                    LayoutParams params = this.getLayoutParams();
                    params.width = screenWidth;
                    params.height = screenHeight;
                }
			}
			
		}
		super.setImageBitmap(bm);
		//reset PhotoCommentsActivity
	}

	int offsetx=0;
    int offsety=0;
    
    int startx = 0;
    int starty = 0;
   
    
}

