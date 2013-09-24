package com.borqs.common.view;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Scroller;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;

public class AllAppsScreen extends ViewGroup {
    protected static final String TAG = "oms2.5Launcher.AllAppsScreen";
    protected Context mContext;
    
    protected boolean mFirstLayout = true;
    protected static final int INVALID_SCREEN = -1;
    protected int mNextScreen = INVALID_SCREEN;

    public int mCurrentScreen;
    protected float mDownMotionX;
    protected float mLastMotionX;
    protected float mLastMotionY;
    
    protected final static int TOUCH_STATE_REST = 0;
    protected final static int TOUCH_STATE_SCROLLING = 1;
    protected int mTouchState = TOUCH_STATE_REST;
    protected static final int SNAP_VELOCITY = 200;

    protected OnLongClickListener mLongClickListener;

    protected VelocityTracker mVelocityTracker;

    protected boolean mAllowLongPress = true;

    protected int mTouchSlop;
    protected int mMaximumVelocity;
    
    protected static final int INVALID_POINTER = -1;
    
    protected Drawable mNextIndicator;
    protected Drawable mPreviousIndicator;
    protected float mTouchX;
    protected float mSmoothingTime;
    
    protected int SLOP = 8;
    public static final float NANOTIME_DIV = 1000000000.0f;
    public static final float SMOOTHING_SPEED = 0.75f;
    public static final float SMOOTHING_CONSTANT = (float) (0.016 / Math.log(SMOOTHING_SPEED));
    public static final float BASELINE_FLING_VELOCITY = 2500.f;
    public static final float FLING_VELOCITY_INFLUENCE = 0.4f;

//    public static final int DEFAULT_SCREEN_COUNT = 3;
//    public static final int DEFAULT_SCREEN_PAGE = 1;
    public static final int DEFAULT_SCREEN_COUNT = 4;
    public static final int DEFAULT_SCREEN_PAGE = 0;

    protected PageIndicatorLineStyleView mLinePageIndicator ;
    protected int pageIndicatorRawWidth = 0;
    protected int pageIndicatorRawHeight = 0;
    protected int screenWidth, screenHeight;
    protected Scroller mScroller;
    
    public static boolean mIsCurrentScreen = true;
    
    protected WeakReference<TitleActionListener> tl;
    //TODO
    protected WeakReference<LoadDataActionListener> loaddatalistener;
    
    public interface LoadDataActionListener
    {
    	public void loaddata(int index);
    }
    public interface TitleActionListener
    {
    	public void setPageTitle(int index);
    }
    
    public AllAppsScreen(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setIsCurrentScreen(boolean iscurrentScreen) {
    	mIsCurrentScreen = iscurrentScreen;
    }
    
    protected boolean justForMySelf=true;
    public AllAppsScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        setHapticFeedbackEnabled(false);
        
		initAllAppsScreens();
		
		setAlwaysDrawnWithCacheEnabled(true);
		
		mScroller = new Scroller(getContext());
		pageIndicatorRawWidth = context.getResources().getDrawable(R.drawable.page_indicator).getIntrinsicWidth();
        pageIndicatorRawHeight = context.getResources().getDrawable(R.drawable.page_indicator).getIntrinsicHeight();
        
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        
        TypedArray sa = context.obtainStyledAttributes(attrs, R.styleable.AllAppsScreen);
        int screenNumber = sa.getInt(R.styleable.AllAppsScreen_screen_number, 4);        
        sa.recycle();
        
        initAllAppsScreenCellLayout(screenNumber);
    }
    
    public void setScreenNumber(int screenNumber)
    {
    	initAllAppsScreenCellLayout(screenNumber);
    	
    	if(mLinePageIndicator != null)
    	{
	    	if(screenNumber == 1)
	    	{    		
	    	    mLinePageIndicator.setVisibility(View.GONE);
	    	}else{
	    		mLinePageIndicator.setVisibility(View.VISIBLE);
	    	}
    	}
    }
    
    public void initAllAppsScreens() {        
        mCurrentScreen = DEFAULT_SCREEN_PAGE;

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }
    
    protected void initAllAppsScreenCellLayout(int screenCount){
    	Log.d(TAG, "initCellLayout");
    	/*
        final int tmpCount = getChildCount();
        for(int j=0;j<tmpCount;j++){
        	View vg = getChildAt(j);
        	if(ViewGroup.class.isInstance(vg))
        	{
	        	((ViewGroup)vg).removeAllViews();	        	
        	}
        }*/
        
        removeAllViews();
        
        final int childCount = screenCount > 0 ? screenCount:1;
        final LayoutInflater mInflater = LayoutInflater.from(mContext);
        for (int i = 0; i < childCount; i++) {
            View view = mInflater.inflate(R.layout.workspace_item, null);
            if(i == 0)
            {
             //   view.setBackgroundColor(Color.RED);
            }
            else if(i == 1)
            {
            	
            }
            else if(i == 2)
            {
             
            }
            
            view.setId(200 + i); //give cell layout an id.
            addView(view, i);
        }
    }

    //use for page exchange, the screen no need update.
    public void resetCellLayout(){
    	
    }
    
    public void setCurrentScreen(int currentScreen) {
        if (!mScroller.isFinished()) mScroller.abortAnimation();
        mCurrentScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));
        scrollTo(mCurrentScreen * getWidth(), 0);
        invalidate();
    }
    
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mLongClickListener = l;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).setOnLongClickListener(l);
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        mTouchX = x;
        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
    }
    
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mTouchX = mScroller.getCurrX();            
            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
            
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        } else if (mNextScreen != INVALID_SCREEN) {
            mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
            mNextScreen = INVALID_SCREEN;
            clearChildrenCache();
        } else if (mTouchState == TOUCH_STATE_SCROLLING) {
            final float now = System.nanoTime() / NANOTIME_DIV;
            final float e = (float) Math.exp((now - mSmoothingTime) / SMOOTHING_CONSTANT);
            final float dx = mTouchX -getScrollX();
            
            mSmoothingTime = now;            
            scrollTo((int)(getScrollX() + dx * e), getScrollY());
            if (dx > 1.f || dx < -1.f) {
                postInvalidate();
            }
        }
    }

    boolean needrecord =false;
    int     count = 1;
    long alltime  = 0;
    long lastDrawtime=0;    
    boolean SHOW_DARW = false;
        
    @Override
    protected void dispatchDraw(Canvas canvas) {
    	long pre = System.currentTimeMillis();
        long span = (pre-lastDrawtime);
        if(span < 200)
        {
            if(alltime > 0)
               count++;
            alltime += span;
        }
        lastDrawtime = pre;

        boolean restore = false;
        int restoreCount = 0;    
        
        int drawCount = 0;
        

        boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING && mNextScreen == INVALID_SCREEN;
        if (fastDraw) {
        	if(mCurrentScreen >= 0 && mCurrentScreen <  getChildCount()){
        		drawChild(canvas, getChildAt(mCurrentScreen), getDrawingTime());
        		drawCount++;
        	}
        } else {
        	boolean isNeedDrawAll = false;
            final long drawingTime = getDrawingTime();
            final float scrollPos = (float) getScrollX() / getWidth();
            final int leftScreen = (int) scrollPos;
            final int rightScreen = leftScreen + 1;
            if (leftScreen >= 0 && leftScreen < getChildCount()) {
                drawChild(canvas, getChildAt(leftScreen), drawingTime);
                drawCount++;
            }else{
                isNeedDrawAll = true;
            }
            
            if(isNeedDrawAll){
            	Log.d(TAG, "dispatchDraw oops! draw last screen. leftScreen:"+leftScreen+" screenCount:"+getChildCount());
            	for (int i = 0; i < getChildCount(); i++) 
            	{
            		drawChild(canvas, getChildAt(i), drawingTime);
            		drawCount++;
            	}
            }else if (scrollPos != leftScreen && rightScreen < getChildCount() && rightScreen >= 0) {
                drawChild(canvas, getChildAt(rightScreen), drawingTime);
                drawCount++;
            }
        }

        if (restore) {
            canvas.restoreToCount(restoreCount);
        }
        
        if(SHOW_DARW)
        {
        	long drawtime = (System.currentTimeMillis()-pre);
        	Log.d(TAG, "dispatchDraw time:"+ drawtime + " span="+span + " average time="+alltime/count + "\npage="+drawCount);
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        computeScroll();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//        int []location = new int[2];
//        getLocationInWindow(location);
//        if(mFirstLayout == false && location[0] > 10)
//        	return ;
        

        if(!mIsCurrentScreen){
        	return;
        }
        
//        
        if(justForMySelf)
        {
	        final int width = MeasureSpec.getSize(widthMeasureSpec);
	//        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
	//        if (widthMode != MeasureSpec.EXACTLY) {
	//            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
	//        }
	//
	//        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
	//        if (heightMode != MeasureSpec.EXACTLY) {
	//            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
	//        }
	
	        // The children are given the same width and height as the workspace
	        final int count = getChildCount();
	        for (int i = 0; i < count; i++) {
	            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
	        }
	
	        if (mFirstLayout) {
	            setHorizontalScrollBarEnabled(false);
	            scrollTo(mCurrentScreen * width, 0);
	            setHorizontalScrollBarEnabled(true);
	            mFirstLayout = false;
	        }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft = 0;

//        int []location = new int[2];
//        getLocationInWindow(location);
//        if(mFirstLayout == false && location[0] > 10)
//        	return ;
        
        if(!mIsCurrentScreen){
        	return;
        }
        
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
    	Log.d(TAG,"requestChildRectangleOnScreen view:"+child);
        int screen = indexOfChild(child);
        if (screen != mCurrentScreen || !mScroller.isFinished()) {
            snapToScreen(screen);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
    	Log.d(TAG, "onRequestFocusInDescendants  direction:"+direction+" rect:"+previouslyFocusedRect);
        int focusableScreen;
        if (mNextScreen != INVALID_SCREEN) {
            focusableScreen = mNextScreen;
        } else {
            focusableScreen = mCurrentScreen;
        }
		if(null != getChildAt(focusableScreen)) {
			getChildAt(focusableScreen).requestFocus(direction, previouslyFocusedRect);
		}
        return false;
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
    	Log.d(TAG, "dispatchUnhandledMove   focused view:"+focused+"  direction:"+direction);
        if (direction == View.FOCUS_LEFT) {
            if (mCurrentScreen > 0) {
                snapToScreen(mCurrentScreen - 1);
                return true;
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (mCurrentScreen < getChildCount() - 1) {
                snapToScreen(mCurrentScreen + 1);
                return true;
            }
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        getChildAt(mCurrentScreen).addFocusables(views, direction);
        if (direction == View.FOCUS_LEFT) {
            if (mCurrentScreen > 0) {
                getChildAt(mCurrentScreen - 1).addFocusables(views, direction);
            }
        } else if (direction == View.FOCUS_RIGHT){
            if (mCurrentScreen < getChildCount() - 1) {
                getChildAt(mCurrentScreen + 1).addFocusables(views, direction);
            }
        }
    }

	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(QiupuConfig.DBLOGD)Log.d("touch ", "AllAppsScreen onInterceptTouchEvent: " + ev.getAction() );
        //Log.d(TAG,"onInterceptTouchEvent ev:"+ev.getAction()+" currenScreen:"+mCurrentScreen+"  view:"+getChildAt(mCurrentScreen));
        final int action = ev.getAction();
        
        if(mLoadingBar!=null && mLoadingBar.isShown()){
        	//Log.d(TAG,"onInterceptTouchEvent return false, AllApps is shown");
        	return false;
        }
        
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            //Log.d(TAG,"onInterceptTouchEvent return true   ---------  !TOUCH_STATE_REST && ACTION_MOVE");
            return true;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        
        final float x = ev.getX();
        final float y = ev.getY();
        
        switch (action) {
            case MotionEvent.ACTION_MOVE: {               
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);
                int moveSlop = SLOP;
                final int touchSlop = mTouchSlop;
                boolean xMoved = xDiff > moveSlop;
                boolean yMoved = yDiff > moveSlop;

                if (xMoved || yMoved) {
                    
                	 if(yDiff > (0.577f * xDiff)){
             			xMoved = false;
             			
             			final VelocityTracker velocityTracker = mVelocityTracker;
     	                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
     	                int velocityX = (int) velocityTracker.getXVelocity();
     	                
             			if(yDiff > this.getHeight()/4 || velocityX > 600)
             			{
             			    //change to other category
             				boolean next = (y - mLastMotionY) > 0;
             				
             				
             				mLastMotionY = y;
            				mLastMotionX = x;
            				mDownMotionX = x;
             			}
             		}
                	 
                    if (xMoved) {
                        // Scroll if the user moved far enough along the X axis
                        mTouchState = TOUCH_STATE_SCROLLING;
                        //when move, record the current postion as first pos
                        mDownMotionX = x;
                        mLastMotionX = x;
                        mLastMotionY = y;
                        mTouchX = getScrollX();
                        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                        enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
                        
                        alltime = 0;
                        count = 1;
                    }
                    // Either way, cancel any pending longpress
                    if (mAllowLongPress) {
                        mAllowLongPress = false;

                        final View currentScreen = getChildAt(mCurrentScreen);
                        currentScreen.cancelLongPress();
                    }
                }else{
                }
                
                break;
            }
            case MotionEvent.ACTION_DOWN: {              
                // Remember location of down touch
            	mDownMotionX = x;
                mLastMotionX = x;
                mLastMotionY = y;
                mAllowLongPress = true;
                
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                Log.d(TAG,"onInterceptTouchEvent ACTION_DOWN, mLastMotionX:"+x+" mLastMotionY:"+y);
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            	alltime = 0;
                count = 1;
                   
            	final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity();
                mVelocityTracker.clear();

                // Release the drag
                clearChildrenCache();
                mTouchState = TOUCH_STATE_REST;               
                mAllowLongPress = false;
                
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                break; 
        }

        //Log.d(TAG,"onInterceptTouchEvent return "+  String.valueOf(mTouchState != TOUCH_STATE_REST));
        return mTouchState != TOUCH_STATE_REST;
    }   

    @Override
    public void focusableViewAvailable(View focused) {
        View current = getChildAt(mCurrentScreen);
        View v = focused;
        while (true) {
            if (v == current) {
                super.focusableViewAvailable(focused);
                return;
            }
            if (v == this) {
                return;
            }
            ViewParent parent = v.getParent();
            if (parent instanceof View) {
                v = (View)v.getParent();
            } else {
                return;
            }
        }
    }

    void enableChildrenCache(int fromScreen, int toScreen) {
        if (fromScreen > toScreen) {
            final int temp = fromScreen;
            fromScreen = toScreen;
            toScreen = temp;
        }
        
        //enable cell cache
        setChildrenDrawnWithCacheEnabled(true);
        setChildrenDrawingCacheEnabled(true);       
    }

    void clearChildrenCache() {
    	setChildrenDrawnWithCacheEnabled(false);    	
    }

    boolean NeedScrollForActionUP = false;
    
    int preDelta  = 0; 
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	if(QiupuConfig.DBLOGD)Log.d("touch ", "AllAppsScreen onTouchEvent: " + ev.getAction() );
        if (getChildCount() <= 1) {
            Log.v(TAG, "need no velocity tracker if child view count is less than 2.");
            return super.onTouchEvent(ev);
        }

        //Log.d(TAG,"onTouchEvent ev:"+ev.getAction());
        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();
        
        if (mVelocityTracker == null) {
        	mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
       
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            /*
             * If being flinged and user touches, stop the fling. isFinished
             * will be false if being flinged.
             */
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }

            // Remember where the motion event started
            mLastMotionX = x;
            mLastMotionY = y;
            mDownMotionX = x;
            
            //Log.d(TAG,"onTouchEvent ACTION_DOWN, mLastMotionX:"+mLastMotionX);
            break;
        case MotionEvent.ACTION_MOVE:
            //final float xx = ev.getX(pointerIndex);
            final float xx = x;
        	if(mTouchState != TOUCH_STATE_SCROLLING){        		
        		final int xDiff = (int) Math.abs(xx - mLastMotionX);
        		final int yDiff = (int) Math.abs(y - mLastMotionY);
                int moveSlop = SLOP;
 

        		boolean xMoved = xDiff > moveSlop;
        	    if(yDiff > (0.577f * xDiff)){
        			xMoved = false;
        			
        			final VelocityTracker velocityTracker = mVelocityTracker;
	                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
	                int velocityX = (int) velocityTracker.getXVelocity();
	                
        			if(yDiff > this.getHeight()/4 || velocityX > 600)
        			{
        			    //change to other category
        				boolean next = (y - mLastMotionY) > 0;
        				mLastMotionY = y;
        				mLastMotionX = xx;
        				mDownMotionX = xx;        				
        			}
        		}
        	    
        		if (xMoved) {
        			mTouchState = TOUCH_STATE_SCROLLING ;
        		    mTouchX = getScrollX();        		    
                    mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
        			enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
        			
        			alltime = 0;
                    count = 1;
        		}
        	}
        	
        	if (mTouchState == TOUCH_STATE_SCROLLING) {
                // Scroll to follow the motion event
                int deltaX = (int) (mLastMotionX - xx);               
                
                //must use float to decrease the loose point
                float deltaOrignal = (mDownMotionX - xx);
                
                if(preDelta ==0)
                	preDelta = deltaX;

                if (deltaX < 0) {
                    if (getScrollX() > 0) {
                    	//change the move direction
                	    if (mTouchX > 0) {
                            mTouchX += Math.max(-mTouchX, deltaX);
                            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                            invalidate();
                            
                        }
                        mLastMotionX = xx;
                    }
                    else if(getScrollX() <=0)
                    {
                    	NeedScrollForActionUP = false;                    	
                    }
                } else if (deltaX > 0) {
                    final int availableToScroll = getChildAt(getChildCount() - 1).getRight() - getScrollX() - getWidth();
                    if (availableToScroll > 0) {
                    	mTouchX += Math.min(availableToScroll, deltaX);
                        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                        invalidate();
                        mLastMotionX = xx;
                    }
                    else if(availableToScroll <=0)
                    {
                    	NeedScrollForActionUP = false;                    	
                    }
                    
                } else {
                    awakenScrollBars();
                }
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && mLinePageIndicator != null)
                    mLinePageIndicator.movePosition(Math.round((1.0f*mLinePageIndicator.getWidth())/ getChildCount()), mLinePageIndicator.getHeight(), deltaOrignal, screenWidth, screenHeight, mCurrentScreen, getChildCount());
                else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && mLinePageIndicator != null)
                	mLinePageIndicator.movePosition(mLinePageIndicator.getWidth(), Math.round((1.0f*mLinePageIndicator.getHeight())/ getChildCount()), deltaOrignal, screenWidth, screenHeight, mCurrentScreen, getChildCount());
            }
            break;
        case MotionEvent.ACTION_UP:
        	
        	alltime = 0;
            count = 1;
               
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity();
                mVelocityTracker.clear();

                int span  = (int)(x - mLastMotionX);
                float deltaOrignal = (mDownMotionX - x);
            	if(mCurrentScreen > 0 && velocityX > 200)
            	{
            		 if(-100 <= deltaOrignal && deltaOrignal <= 0){
                     	snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen - 1, velocityX);
                     }else{
                    	 snapToScreenWithVelocityX(mCurrentScreen - 1, velocityX);
                     }
            	}
            	else if(mCurrentScreen > 0 && span > (getWidth()/10))
            	{          
            		if(-100 <= deltaOrignal && deltaOrignal <= 0){
                     	snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen - 1, velocityX);
                     }else{
                    	 snapToScreenWithVelocityX(mCurrentScreen - 1, velocityX);
                     }
            	}	
            	else if(mCurrentScreen < getChildCount() - 1 && velocityX < -200)
            	{
            		if(0 <= deltaOrignal && deltaOrignal <= 100){
                    	snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen + 1, velocityX);
                    }else{
                    	snapToScreenWithVelocityX(mCurrentScreen + 1, velocityX);
                    }
            	}
            	else if(mCurrentScreen < getChildCount() - 1 && span < -(getWidth()/10))
            	{
            		if(0 <= deltaOrignal && deltaOrignal <= 100){
                    	snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen + 1, velocityX);
                    }else{
                    	snapToScreenWithVelocityX(mCurrentScreen + 1, velocityX);
                    }
            	}
            	else
            	{
                     snapToDestination(velocityX);
            	}
            	
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
            }
            
            mTouchState = TOUCH_STATE_REST;
            break;
        case MotionEvent.ACTION_CANCEL:
            mTouchState = TOUCH_STATE_REST;
            if(null != mVelocityTracker){
            	mVelocityTracker.clear();
            }
            break;      
        }

    //    if(Launcher.LOGD)Log.d(TAG,"onTouchEvent return true");
        return true;
    } 
    
    public void setTitleListener(TitleActionListener acl)
    {
    	if(tl != null && tl.get() != null)
    	{
    		tl.clear();
    	}
    	
    	tl = new WeakReference<TitleActionListener>(acl);
    }
    
    public void setloadDataListener(LoadDataActionListener acl)
    {
    	if(loaddatalistener != null && loaddatalistener.get() != null)
    	{
    		loaddatalistener.clear();
    	}
    	loaddatalistener = new WeakReference<LoadDataActionListener>(acl);
    }
    
	public void setPageIndicatorLineStyleView(PageIndicatorLineStyleView pv){
    	mLinePageIndicator = pv;

        refreshScreenPage(mCurrentScreen);
//    	mLinePageIndicator.refreshPosition(pageIndicatorRawWidth, pageIndicatorRawHeight, screenWidth, screenHeight, mCurrentScreen, getChildCount());
//    	setTitle(mCurrentScreen);
    }

    protected void refreshScreenPage(int nIndex) {
        if (null != mLinePageIndicator) {
            mLinePageIndicator.refreshPosition(pageIndicatorRawWidth,
                    pageIndicatorRawHeight, screenWidth, screenHeight,
                    nIndex, getChildCount());
        }

        setTitle(nIndex);
    }
	protected void setTitle(int nIndex)
	{
		if(tl != null && tl.get() != null)
		{
			tl.get().setPageTitle(nIndex);
		}
		if(loaddatalistener != null && loaddatalistener.get() != null)
		{
			loaddatalistener.get().loaddata(nIndex);
		}
	}
    public void snapToScreenWithVelocityX(int whichScreen, int velocityX) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        enableChildrenCache(mCurrentScreen, whichScreen);

        final int screenDelta = Math.abs(whichScreen - mCurrentScreen);
        mNextScreen = whichScreen;
        
        
        //pageIndicator.drawPageIndicator(mNextScreen,getChildCount());
//        mLinePageIndicator.refreshPosition(pageIndicatorRawWidth, pageIndicatorRawHeight, screenWidth, screenHeight, mNextScreen, getChildCount());
//        setTitle(mNextScreen);
        refreshScreenPage(mNextScreen);
        
        View focusedChild = getFocusedChild();
        if (focusedChild != null && screenDelta != 0 && focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }
        
        final int newX = whichScreen * getWidth();
        final int delta = newX - getScrollX();
        int duration = 500;
   
        awakenScrollBars(duration);
        mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
        invalidate();        
    }
    
    void snapToScreenWithVelocityXSMALLSLOP(int whichScreen, int velocityX) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        enableChildrenCache(mCurrentScreen, whichScreen);
        
        final int screenDelta = Math.abs(whichScreen - mCurrentScreen);
        mNextScreen = whichScreen;

        if(mLinePageIndicator != null)
        mLinePageIndicator.refreshPosition(pageIndicatorRawWidth, pageIndicatorRawHeight, screenWidth, screenHeight, mNextScreen, getChildCount());
        setTitle(mNextScreen);
        
        View focusedChild = getFocusedChild();
        if (focusedChild != null && screenDelta != 0 && focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }
        
        final int newX = whichScreen * getWidth();
        final int delta = newX - getScrollX();
        int duration = 650;
    	
        awakenScrollBars(duration);
        mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
        invalidate();       
        
    }   
   

    final float distance = 0.8f;
    protected void snapToDestination(int velocityX) {
        final int screenWidth = getWidth();
        float rate = distance;
        //to right
        if((mDownMotionX - mLastMotionX) < 0)
        {
        	rate = 1-distance;
        }
        final int whichScreen = (getScrollX() + (int)(screenWidth *rate)) / screenWidth;

        snapToScreen(whichScreen, 0/*velocityX*/, true);
    }   
   

    public void snapToScreen(int whichScreen) {
        snapToScreen(whichScreen, 0, false);
    }

    protected void snapToScreen(int whichScreen, int velocity, boolean settle) {
    	Log.d(TAG, "snapToScreen whichScreen:"+whichScreen);
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        enableChildrenCache(mCurrentScreen, whichScreen);

        mNextScreen = whichScreen;

//        mLinePageIndicator.refreshPosition(pageIndicatorRawWidth, pageIndicatorRawHeight, screenWidth, screenHeight, mNextScreen, getChildCount());
//        setTitle(mNextScreen);
        refreshScreenPage(mNextScreen);
        
        View focusedChild = getFocusedChild();
        if (focusedChild != null && whichScreen != mCurrentScreen &&
                focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }
        
        final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurrentScreen));
        final int newX = whichScreen * getWidth();
        final int delta = newX - getScrollX();
        int duration = (screenDelta + 1) * 200;

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        
        if(delta != 0)
        {
	        velocity = Math.abs(velocity);
	        if (velocity > 0) {
	            duration += (duration / (velocity / BASELINE_FLING_VELOCITY))
	                    * FLING_VELOCITY_INFLUENCE;
	        } else {
	            duration += 100;
	        }
	
	        awakenScrollBars(duration);
	        mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
	        invalidate();
        }
    }

    public void scrollLeft() {
        if (mScroller.isFinished()) {
            if (mCurrentScreen > 0) snapToScreen(mCurrentScreen - 1);
        } else {
            if (mNextScreen > 0) snapToScreen(mNextScreen - 1);            
        }
    }

    public void scrollRight() {
        if (mScroller.isFinished()) {
            if (mCurrentScreen < getChildCount() -1) snapToScreen(mCurrentScreen + 1);
        } else {
            if (mNextScreen < getChildCount() -1) snapToScreen(mNextScreen + 1);            
        }
    }

    public int getScreenForView(View v) {
        int result = -1;
        if (v != null) {
            ViewParent vp = v.getParent();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (vp == getChildAt(i)) {
                    return i;
                }
            }
        }
        return result;
    }

    public View getViewForTag(Object tag) {
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            CellLayout currentScreen = ((CellLayout) getChildAt(screen));
            int count = currentScreen.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = currentScreen.getChildAt(i);
                if (child.getTag() == tag) {
                    return child;
                }
            }
        }
        return null;
    }

    public boolean allowLongPress() {
        return mAllowLongPress;
    }
    
    public void setAllowLongPress(boolean allowLongPress) {
        mAllowLongPress = allowLongPress;
    }

    void setIndicators(Drawable previous, Drawable next) {
        mPreviousIndicator = previous;
        mNextIndicator = next;
        previous.setLevel(mCurrentScreen);
        next.setLevel(mCurrentScreen);
    }
    
    protected View categoryView;
    void setCategoryView(View view) {
        categoryView = view;
    }
    
    
    public static class SavedState extends BaseSavedState {
        int currentScreen = -1;

        SavedState(Parcelable superState) {
            super(superState);
        }

        protected SavedState(Parcel in) {
            super(in);
            currentScreen = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentScreen);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
    
	public void onDropCompleted(View target, boolean success) {
		
	}
	
	View mLoadingBar;
	public void setLoadingBar(View loadingBar){
		mLoadingBar = loadingBar;
	}
	
	public int getCurrentScreen()
	{
		return mCurrentScreen;
	}
}
