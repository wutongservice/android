package com.borqs.common.view;

import com.borqs.common.listener.TabFilterListener;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class TabBottomView extends RelativeLayout
{
	private static String TAG="TabBottomView"; 
	
	TabFilterListener motionFilter;
	public void setTabFilterLister(TabFilterListener listener)
	{
		motionFilter = listener;
	}
	
	public TabBottomView(Context context) {
		super(context, null);
	}
	public TabBottomView(Context context, AttributeSet attrs) {
		super(context, attrs);	
		
		 final ViewConfiguration configuration = ViewConfiguration.get(getContext());
	     mTouchSlop = configuration.getScaledTouchSlop();
	}
	
	
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private int mTouchState = TOUCH_STATE_REST;
	VelocityTracker mVelocityTracker;
    private float mDownMotionX;
    private float mLastMotionX;
    private float mLastMotionY;
    private int   mTouchSlop;
	
	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
		Log.d(TAG, "onInterceptTouchEvent ev="+ev);
		
		 final int action = ev.getAction();	        
	        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
	            return true;
	        }	        

	        if (mVelocityTracker == null) {
	            mVelocityTracker = VelocityTracker.obtain();
	        }
	        mVelocityTracker.addMovement(ev);
	        
	        switch (action & MotionEvent.ACTION_MASK) {
	            case MotionEvent.ACTION_MOVE: {
	                final float x = ev.getX();
	                final float y = ev.getY();
	                final int xDiff = (int) Math.abs(x - mLastMotionX);
	                final int yDiff = (int) Math.abs(y - mLastMotionY);	                
	                final int touchSlop = mTouchSlop;
	                boolean xMoved = xDiff > touchSlop;
	                boolean yMoved = yDiff > touchSlop;	               

	                if (xMoved || yMoved) {
	                    
	                    if (xMoved) {
	                        // Scroll if the user moved far enough along the X axis
	                        mTouchState = TOUCH_STATE_SCROLLING;
	                        //when move, record the current postion as first pos
	                        mDownMotionX = x;
	                        mLastMotionX = x;
	                        mLastMotionY = y;
	                    }	                    
	                }
	                break;
	            }
	            case MotionEvent.ACTION_DOWN: {
	                final float x = ev.getX();
	                final float y = ev.getY();
	                // Remember location of down touch
	            	mDownMotionX = x;
	                mLastMotionX = x;
	                mLastMotionY = y;
	               
	                mTouchState = TOUCH_STATE_REST ;         
	                
	                break;
	            }

	            case MotionEvent.ACTION_CANCEL:
	            case MotionEvent.ACTION_UP:
	                mTouchState = TOUCH_STATE_REST;     
	                if (mVelocityTracker != null) {
	                    mVelocityTracker.recycle();
	                    mVelocityTracker = null;
	                }

	                break;
	                
	            case MotionEvent.ACTION_POINTER_UP:
	                
	                break;
	        }

	        /*
	         * The only time we want to intercept motion events is if we are in the
	         * drag mode.
	         */
	        //if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent return "+  String.valueOf(mTouchState != TOUCH_STATE_REST));
	        return mTouchState != TOUCH_STATE_REST;	    
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		Log.d(TAG, "onTouchEvent ev="+ev);
		
		final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();
        
        if (mVelocityTracker == null) {
        	mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        switch (action) {
        case MotionEvent.ACTION_DOWN:
        	mLastMotionX = x;
            mLastMotionY = y;
            mDownMotionX = x;
        	break;
        case MotionEvent.ACTION_MOVE:	
        	if(mTouchState != TOUCH_STATE_SCROLLING){  
        		final int xDiff = (int) Math.abs(x - mLastMotionX);
        		final int yDiff = (int) Math.abs(y - mLastMotionY);
                int moveSlop = mTouchSlop; 
        		boolean xMoved = xDiff > moveSlop;
        		if (xMoved) {
        			mTouchState = TOUCH_STATE_SCROLLING ;
        			motionFilter.beginDrag(ev);
        		}
        	}
        	
        	if (mTouchState == TOUCH_STATE_SCROLLING) {
    			motionFilter.filterMoveAction(ev);
    		}
        	break;
        case MotionEvent.ACTION_UP:	            
        case MotionEvent.ACTION_CANCEL:
        	mTouchState = TOUCH_STATE_REST;
        	motionFilter.dismissOverlayer();
        	break;
        }        
        
		return true;		
	}
}
