package com.borqs.common.view;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.borqs.common.util.AlphaPost;
import com.borqs.qiupu.R;

public class AtoZ extends View{

	final static String TAG = "AtoZ";
	
	static int init=-1;
	static int BOTTOM_MARGIN = 5, TOP_MARGIN = 10;
	static int A_Z_COUNT = 26;
	static float CORNER_RADIUS = 0.0f;
    static float PADDING_H = 5.0f;
    static float PADDING_V = 1.0f;
    static int textPaddingTop=5;
    static int DEFAULT_WIDTH = 20;
    static float start_postion = DEFAULT_WIDTH/3.0f;
    static float end_alpha   = (DEFAULT_WIDTH*5.0f)/6.0f;
    static float start_alpha = (DEFAULT_WIDTH*1.0f)/6.0f;
    
	int BIG_TEXT_SIZE = 12;
	int LITTLE_TEXT_SIZE = 10;
	int TEXT_SIZE = 12;
	
	final int TEXT_COLOR = 0xff9d9d9d;
	final int TEXT_BKG_COLOR = 0xff000000;
	
	int BKG_COLOR = Color.TRANSPARENT;
//	final int BKG_PRESS_COLOR = 0xffebebeb;//TODO
	final int BKG_PRESS_COLOR = 0x80c9c9c9;
	final int HIGH_LIGHT_COLOR = Color.argb(0xff, 0x043, 0x084, 0x0d3);
	static private Paint mPaint, mBkgPaint, mHighLightPaint, mSelectPaint; 
	static private Shader mShader;
	
	
	private AbsListView mList;
    
    int mOrigentation = -1;
    boolean mShowTwoColumn = false;
    private float mCornerRadius;
    int mOverlaySize;
    
    PopupWindow mPopup;
    EditText mTextFilter;
    
	RectF mBkgRect = new RectF();
	
	int mX, mY; //the popup window postion
	float mRealHeight;
	public AtoZ(Context context) {
		this(context, null);
	}
	
	
	int firstVisiblePosition;
	AbsListView.OnScrollListener  scrollListener = new AbsListView.OnScrollListener()
	{
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			//find visible items	
			//should be delay
			firstVisiblePosition = firstVisibleItem;			
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			//just do it when going to stop or stoped
			if(false)
			{
				if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
				{
					currentAlpha = findAlpha(firstVisiblePosition);
					if(currentAlpha != null)
					{
					    invalidate();
					}
				}
			}
		}
		
	};
	
	private AlphaPost findAlpha(int position)
    {
		for(AlphaPost item:alphaList)
		{
			if(position >= item.newPos)
			{
				Log.d(TAG, "find ="+item);
				return item;
			}
		}
		
    	return null;
    }
	
	public AtoZ(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	Matrix mShaderMatrix;
	static float density = 1.0f;
	public AtoZ(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		BKG_COLOR = getResources().getColor(R.color.transparent);//TODO
		if(init == -1)
		{
			density = this.getResources().getDisplayMetrics().density;
			TEXT_SIZE = (int)(density * TEXT_SIZE);
			
			BOTTOM_MARGIN = (int)(density *BOTTOM_MARGIN); 
			TOP_MARGIN = (int)(density *TOP_MARGIN);
			
			textPaddingTop = (int)(density * textPaddingTop);
			 
		    CORNER_RADIUS = density* CORNER_RADIUS;
		    PADDING_H = density *5.0f;
		    PADDING_V = density * 1.0f;	    
	
	//		BIG_TEXT_SIZE = (int)(density * BIG_TEXT_SIZE);
	//		LITTLE_TEXT_SIZE = (int)(density * LITTLE_TEXT_SIZE);
	//		TEXT_SIZE = (int)(density * TEXT_SIZE);	
			DEFAULT_WIDTH = (int)(density * DEFAULT_WIDTH);
			
			end_alpha   = (DEFAULT_WIDTH*5.0f)/6.0f;
			start_alpha = (DEFAULT_WIDTH*1.0f)/6.0f;
			start_postion = DEFAULT_WIDTH/3.0f;
			
			mPaint = new Paint();
			mPaint.setColor(TEXT_COLOR);
			mPaint.setTextSize(TEXT_SIZE);
			mPaint.setAntiAlias(true);
			
			mBkgPaint = new Paint();
			mBkgPaint.setStyle(Style.FILL);
			mBkgPaint.setColor(getResources().getColor(R.color.qiupu_list_color));
			mBkgPaint.setAntiAlias(true);
			mBkgPaint.setAlpha(126);
			
			
			mSelectPaint = new Paint();
			mSelectPaint.setStyle(Style.FILL);
			mSelectPaint.setColor(0x237eaf);
			mSelectPaint.setAntiAlias(true);
			mSelectPaint.setAlpha(126);
			
			mHighLightPaint = new Paint();
			mHighLightPaint.setAntiAlias(true);
			mShader = new LinearGradient(0, 0, 0, 1, 
					0xff3e69c8, 0xff4384d3, Shader.TileMode.CLAMP);
			mHighLightPaint.setShader(mShader);
			
			init = 0;
		}		
		
		mShaderMatrix = new Matrix();
		
		
	}
	ArrayList<AlphaPost> alphaList;
	HashMap<Long, Integer> positionMap;
	
	private class YPosition
	{
		public String alpha;
		public float  topY;
		public float  bottomY;
		
		public YPosition(String data, float top, float bottom)
		{
			alpha = data;
			topY = top;
			bottomY = bottom;
		}
	}
	
	ArrayList<YPosition> YPostionMap = new ArrayList<YPosition>(); 
	
	public void setAlphaMap(HashMap<Long, Integer> posMap, ArrayList<AlphaPost> alphas) {
//		alphaList = alphas;
	    if(alphaList == null)
	        alphaList = new ArrayList<AlphaPost>();

	    alphaList.clear();
		alphaList.addAll(alphas);
		positionMap = posMap;
		//calculate the position
		alphaList.add(0, new AlphaPost("#", 0));
		
		
		regenerateYPosition();
		invalidate();
	}
	
	float startOffside=0.0f;
	private void regenerateYPosition()
	{
		if(alphaList == null)
			return ;
		
		float height = mRealHeight;
		if(height <=0)
		{
		    height = this.getHeight() - BOTTOM_MARGIN - TOP_MARGIN;
		}
		
		int count = alphaList.size();
		if(count >0)
		{
			YPostionMap.clear();
			float each = (float)(height/(count*1.0f));
			startOffside = each/3;
			for(int i=0;i<alphaList.size();i++)
			{
				float begin = each*i + TOP_MARGIN + CORNER_RADIUS;
				float end   = each*(i+1) + TOP_MARGIN + CORNER_RADIUS;
				
				YPostionMap.add(new YPosition(alphaList.get(i).alpha, begin, end));
			}
		}		
	}
	
	private AlphaPost findAlpha(MotionEvent ev)
    {
		//Log.d(TAG, "event ="+ev);
		
    	float y    = ev.getY();
    	for(int i=0;i<YPostionMap.size();i++)
    	{
    		YPosition itemY = YPostionMap.get(i);
    		if(y>= itemY.topY && y<=itemY.bottomY)
    		{
    			//TODO
    			//performance issue
    			for(AlphaPost item:alphaList)
    			{
    				if(item.alpha.equalsIgnoreCase(itemY.alpha))
    				{
    					Log.d(TAG, "find ="+item);
    					return item;
    				}
    			}
    		}
    	}   
    	
    	return null;
    }

	/**
	 * This must be set after constructor.
	 * @param list
	 */
	public void setListView(AbsListView list){
		mList = list;
		createTextFilter(true);
        mCornerRadius = CORNER_RADIUS;
        
        //mList.setOnScrollListener(scrollListener);
	}
	
	public interface MoveFilterListener {
		public void  enterPosition(String alpha, int position);
		public void  leavePosition(String alpha);	
		public void  beginMove();
		public void  endMove();
	}
	
	MoveFilterListener motionFilter;
	public void MoveFilterListener(MoveFilterListener listener)
	{
		motionFilter = listener;
	}
	
	public void setMoveFilterListener(MoveFilterListener l){
		motionFilter = l;
	}
	
	private Runnable mDissmissPopup = new Runnable(){
		public void run() {
			if(mPopup != null){
				try {
                    mPopup.dismiss();
                } catch (Exception e) {
                }
			}
		}
	};
	
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private final static int TOUCH_STATE_PTESSED   = 2;
	private int mTouchState = TOUCH_STATE_REST;
	VelocityTracker mVelocityTracker;
    private float mDownMotionX;
    private float mLastMotionX;
    private float mLastMotionY;
    private int   mTouchSlop;
    
    private AlphaPost currentAlpha;
    @Override
	public boolean onTouchEvent(MotionEvent ev) {
		//Log.d(TAG, "onTouchEvent ev="+ev);
		
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
            {
            	currentAlpha = findAlpha(ev);            
        		if(currentAlpha != null)
                {
                	mTouchState = TOUCH_STATE_PTESSED;			        
			        positionPopup(currentAlpha.alpha);			        
			        //draw rectangle			        
			        invalidate();
			        
			        motionFilter.enterPosition(currentAlpha.alpha, currentAlpha.newPos);
                }
            
            }
        	break;
        case MotionEvent.ACTION_MOVE:	
        	if(mTouchState != TOUCH_STATE_SCROLLING){  
        		final int xDiff = (int) Math.abs(x - mLastMotionX);
        		final int yDiff = (int) Math.abs(y - mLastMotionY);
                int moveSlop = mTouchSlop; 
        		boolean xMoved = xDiff > moveSlop;
        		if (xMoved) {
        			mTouchState = TOUCH_STATE_SCROLLING ;
        			motionFilter.beginMove();
        		}
        	}
        	
        	if (mTouchState == TOUCH_STATE_SCROLLING) {
        		AlphaPost tmp = findAlpha(ev);
        		if(tmp != null && currentAlpha != null && tmp.alpha.equalsIgnoreCase(currentAlpha.alpha))
            	{
            	    //same alpha
            	}
            	else        		
            	{
            		currentAlpha = tmp;
            		if(currentAlpha != null)
                    {
                    	//mTouchState = TOUCH_STATE_PTESSED;    			        
    			        positionPopup(currentAlpha.alpha);			        
    			        //draw rectangle    			        
    			        invalidate();
    			        
    			        //reflect a little later
    			        motionFilter.enterPosition(currentAlpha.alpha, currentAlpha.newPos);
                    }
            	}
    		}
        	break;
        case MotionEvent.ACTION_UP:	            
        case MotionEvent.ACTION_CANCEL:
        	mTouchState = TOUCH_STATE_REST;
        	motionFilter.endMove();
        	
        	invalidate();
        	post(mDissmissPopup);
        	break;
        }        
        
		return true;		
	}


	@Override
	protected void onDraw(Canvas canvas) {
		int w = getMeasuredWidth();
		//int h = (int)mRealHeight;
		
		if(alphaList == null || alphaList.size() < 4) return;
		
		RectF bkgRect = mBkgRect;
		bkgRect.left = 0;
		bkgRect.right = w;
		bkgRect.top = 0;//TOP_MARGIN;
		bkgRect.bottom = bkgRect.top + getHeight();
		float x = 5, y = 0; 
		
//		if(mOrigentation == Configuration.ORIENTATION_LANDSCAPE){
//			bkgRect.left = w - (w >> 2);
//			x += w - (w >> 2);
//		};
		
		//mBkgPaint.setAlpha(80);
		if(mTouchState == TOUCH_STATE_REST)
		    mBkgPaint.setColor(BKG_COLOR);
		else
			mBkgPaint.setColor(BKG_PRESS_COLOR);
		
		canvas.drawRoundRect(bkgRect, mCornerRadius, mCornerRadius, mBkgPaint);
		boolean drawedSelected = false;
		for(int i=0;i<YPostionMap.size();i++)
		{
			YPosition item = YPostionMap.get(i);
		    canvas.drawText(item.alpha, 0, 1, start_postion, item.topY+startOffside, mPaint);
		    if(drawedSelected == false && currentAlpha != null)
		    {
		    	if(currentAlpha.alpha.equalsIgnoreCase(item.alpha))
		    	{
		    		final int span = getHeight()-(int)mRealHeight;
		    		canvas.drawRoundRect(new RectF(start_alpha, 
		    				                    item.topY-startOffside, end_alpha, item.bottomY-startOffside),  
		    				   5, 
		    				   5, 
		    				   mSelectPaint);
		    		
		    		drawedSelected = true;
		    	}
		    }
		}		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mRealHeight = MeasureSpec.getSize(heightMeasureSpec) - BOTTOM_MARGIN - TOP_MARGIN;
		int w = DEFAULT_WIDTH;
		
		int newOrigentation = getResources().getConfiguration().orientation;
		mOrigentation = newOrigentation; 
		if(mOrigentation == Configuration.ORIENTATION_LANDSCAPE){
			w = 2 * w;
		}
		
		int len = 0;
		if(alphaList != null && (len = alphaList.size()) > 0){
			float step = mRealHeight/len;
			if(step < TEXT_SIZE){
				TEXT_SIZE = (int)step;
			}else{
				TEXT_SIZE = BIG_TEXT_SIZE;
			}
		}

		regenerateYPosition();
		setMeasuredDimension(w, (int)MeasureSpec.getSize(heightMeasureSpec));
	}
    
    private void positionPopup(String alpha) {
    	//Log.i(TAG, "index = " + index);
    	mTextFilter.setText(alpha);
		int ww = mList.getWidth();
        int hh = mList.getHeight();
        mX = (ww - mOverlaySize) * 3/ 4;
        mY = hh / 10 + mOverlaySize - 10;	
        
        if (!mPopup.isShowing()) {
            mPopup.showAtLocation(mList, Gravity.LEFT | Gravity.TOP,
                    mX, mY);
        } else {
            mPopup.update(mX, mY, -1, -1);
        }
    }
    
    private void createTextFilter(boolean animateEntrance) {
        if (mPopup == null) {
        	Context c = getContext();
            mOverlaySize = c.getResources().getDimensionPixelSize(R.dimen.fastscroll_overlay_size);

            LayoutInflater layoutInflater = (LayoutInflater)
                    c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mTextFilter = (EditText) layoutInflater.inflate(
                    R.layout.typing_filter, null);
            
            mTextFilter.setRawInputType(EditorInfo.TYPE_CLASS_TEXT
                    | EditorInfo.TYPE_TEXT_VARIATION_FILTER);
            mTextFilter.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            //mTextFilter.setBackgroundColor(0x020000000);
            mTextFilter.setBackgroundColor(c.getResources().getColor(R.color.filter_text_bg));
            mTextFilter.setPadding(0, textPaddingTop, 0, 0);
            TypedArray ta = c.getTheme().obtainStyledAttributes(new int[] { 
                    android.R.attr.textColorPrimary });
            ColorStateList textColor = ta.getColorStateList(ta.getIndex(0));
            int textColorNormal = textColor.getDefaultColor();
            mTextFilter.setTextColor(c.getResources().getColor(R.color.white));
            ta.recycle();
            
            mTextFilter.setTextSize(mOverlaySize/3);

            PopupWindow p = new PopupWindow(mTextFilter, mOverlaySize, mOverlaySize);
            p.setFocusable(false);
            p.setTouchable(false);
            p.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);

            p.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.menu_submenu_background));
            p.setAnimationStyle(R.style.azpopup);
            p.update();
            
			int ww = mList.getWidth();
	        int hh = mList.getHeight();
	        mX = (ww - mOverlaySize) / 2;
	        mY = hh / 10 + mOverlaySize - 10;	
	        
            mPopup = p;            
        }
    }    

}
