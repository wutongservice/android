package com.borqs.common.view;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.util.DateUtil;

public class CustomListView extends ListView implements OnScrollListener {  
	
	private final static int RELEASE_To_REFRESH = 0;  
	private final static int PULL_To_REFRESH = 1;  
	private final static int REFRESHING = 2;  
	private final static int DONE = 3;  
	private LayoutInflater inflater;  
	
	private LinearLayout headView;  
	private TextView tipsTextview;  
	private TextView lastUpdatedTextView;  
	private ImageView arrowImageView;  
	private ProgressBar progressBar;  

	private RotateAnimation animation;  
	private RotateAnimation reverseAnimation;  
	
	private boolean isRecored;  
	
	private int headContentWidth;  
	private int headContentHeight;  

    private int yDown;
	private int startY;  
	private int firstItemIndex;  
	
	private int state;  
	
	private boolean isBack;  
	private Context mContext;
	private OnRefreshListener refreshListener;  
	private LoadOldListener mLoadOldListener;
	private int mItemsCount = 0;
	
	private final static String TAG = "CustomListView";  
	
	public void destroy()
	{
		mContext = null;
		inflater = null;  
		
		headView = null;  
		tipsTextview = null;  
		lastUpdatedTextView = null;  
		arrowImageView = null;  
		progressBar = null;  

		animation = null;  
		reverseAnimation = null;  
	}
	
	public CustomListView(Context context, AttributeSet attrs) {  
		super(context, attrs);  
		mContext = context;
		init(context);  
	}  
	
	@Override
	public boolean shouldDelayChildPressedState() {
	    return false;
	}
	 
	private void init(Context context) {  
		this.setVerticalScrollBarEnabled(false);
		inflater = LayoutInflater.from(context);  
		headView = (LinearLayout) inflater.inflate(R.layout.head, null);  
		arrowImageView = (ImageView) headView.findViewById(R.id.head_arrowImageView);  
		arrowImageView.setMinimumWidth(50);  
		arrowImageView.setMinimumHeight(50);  
		progressBar = (ProgressBar) headView.findViewById(R.id.head_progressBar);  
		tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);  
		lastUpdatedTextView = (TextView) headView.findViewById(R.id.head_lastUpdatedTextView);  
		
		measureView(headView);  
		
		headContentHeight = headView.getMeasuredHeight();  
		headContentWidth = headView.getMeasuredWidth();  
		
		headView.setPadding(0, -1 * headContentHeight, 0, 0);  
		headView.invalidate();  
		
		Log.v("size", "width:" + headContentWidth + " height:"  
				+ headContentHeight);  
		
		addHeaderView(headView);  
		setOnScrollListener(this);  
		
		animation = new RotateAnimation(0, -180,  
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,  
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);  
		animation.setInterpolator(new LinearInterpolator());  
		animation.setDuration(500);  
		animation.setFillAfter(true);  
		
		reverseAnimation = new RotateAnimation(-180, 0,  
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,  
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);  
		reverseAnimation.setInterpolator(new LinearInterpolator());  
		reverseAnimation.setDuration(500);  
		reverseAnimation.setFillAfter(true);  
	}  
	
	public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
		firstItemIndex = firstVisibleItem;
		mItemsCount = firstVisibleItem + visibleItemCount;
	}  
	
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		boolean forloadmore = (scrollState == OnScrollListener.SCROLL_STATE_IDLE);
		if(mLoadOldListener != null) {
			mLoadOldListener.loadmore(forloadmore, false, mItemsCount);
		}
	}  
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		AllAppsScreen.mIsCurrentScreen = false;
		switch (event.getAction()) {  
		case MotionEvent.ACTION_DOWN:
            yDown = (int) event.getY();
			if (firstItemIndex == 0 && !isRecored) {  
				startY = yDown;
				isRecored = true;  
				
				Log.v(TAG, "onInterceptTouchEvent Record the pressed pos " + startY );  
			}  
			break;  
		case MotionEvent.ACTION_UP: {
			
			isRecored = false;
			isBack = false;
			break;
		}
		}
		return super.onInterceptTouchEvent(event);
	}
	
	public boolean onTouchEvent(MotionEvent event) {  
		switch (event.getAction()) {  
		case MotionEvent.ACTION_DOWN:
            yDown = (int) event.getY();
            onRefresh(false, false);
            if (firstItemIndex == 0 && !isRecored) {
				startY = yDown;
				isRecored = true;  
				
				Log.v(TAG, "Record the pressed pos");  
			}  
			break;  
			
		case MotionEvent.ACTION_UP:
            final int yEnd = (int)event.getY();
            if (state != REFRESHING) {
				if (state == DONE) {  
					Log.v(TAG, "do nothing");  
				}  
				if (state == PULL_To_REFRESH) {  
					state = DONE;  
					changeHeaderViewByState();  
					Log.v(TAG, "status from refreshing to comple refresh");  
				}

				if (state == RELEASE_To_REFRESH) {
					state = REFRESHING;  
					changeHeaderViewByState();
                    onRefresh(true, yEnd > yDown + 10);
                } else {
                    onRefresh(false, yEnd > yDown + 10);
                }
			} else {
                onRefresh(false, yEnd > yDown + 10);
            }


            isRecored = false;
			isBack = false;  
			
			break;  
			
		case MotionEvent.ACTION_MOVE:  
			int tempY = (int) event.getY();
			if(QiupuConfig.DBLOGD)
				Log.d(TAG, "getwidth: " + this.getWidth() + " getheight() "+ getHeight() + " " + this.getMeasuredHeight() + " " + this.getMeasuredWidth());
			
			// 下划距离缩小为二分之一，防止headview之上的空白过大
			if (!isRecored && firstItemIndex == 0) {  
				isRecored = true;  
				startY = tempY;  
			}  
			if (state != REFRESHING && isRecored) {  
				if (state == RELEASE_To_REFRESH) {  
					// 往上推，推到屏幕足够掩盖head的程度，但还没有全部掩盖  
					if ((tempY - startY < headContentHeight)  
							&& (tempY - startY) > 0) {  
						state = PULL_To_REFRESH;  
						changeHeaderViewByState();  
						
						Log.v(TAG, "由松开刷新状态转变到下拉刷新状态");  
					}  
					// 一下子推到顶   
					else if (tempY - startY <= 0) {  
						state = DONE;  
						changeHeaderViewByState();  
						
						Log.v(TAG, "由松开刷新状态转变到done状态");  
					}  
					// 往下拉，或者还没有上推到屏幕顶部掩盖head   
					else {  
						// 不用进行特别的操作，只用更新paddingTop的值就行了   
					}  
				}  
				// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态   
				if (state == PULL_To_REFRESH) {  
					// 下拉到可以进入RELEASE_TO_REFRESH的状态   
					if (tempY - startY >= headContentHeight) {  
						state = RELEASE_To_REFRESH;  
						isBack = true;  
						changeHeaderViewByState();  
						
						Log.v(TAG, "由done或者下拉刷新状态转变到松开刷新");  
					}  
					// 上推到顶了   
					else if (tempY - startY <= 0) {  
						state = DONE;  
						changeHeaderViewByState();  
						
						Log.v(TAG, "由Done或者下拉刷新状态转变到done状态");  
					}  
				}  
				
				// done status   
				if (state == DONE) {  
					if (tempY - startY > 0) {  
						state = PULL_To_REFRESH;  
						changeHeaderViewByState();  
					}  
				}  
				
				// update headView size   
				if (state == PULL_To_REFRESH) {  
					setSelection(0);
					// 保持下滑速度和上滑速度一致
					headView.setPadding(0, -1 * headContentHeight  
							+ (tempY - startY), 0, 0);  
					headView.invalidate();
				}  
				
				// update heandView paddingtop    
				if (state == RELEASE_To_REFRESH) {  
					setSelection(0);
					headView.setPadding(0, (tempY - startY - headContentHeight)/2,  
							0, 0);  
					headView.invalidate();
				}  
			}  
			break;  
		}  
		return super.onTouchEvent(event);  
	}  
	
	// change head view   
	private void changeHeaderViewByState() {  
		switch (state) {  
		case RELEASE_To_REFRESH:  
			
			arrowImageView.setVisibility(View.VISIBLE);  
			progressBar.setVisibility(View.GONE);  
			tipsTextview.setVisibility(View.VISIBLE);  
			lastUpdatedTextView.setVisibility(View.VISIBLE);  
			
			arrowImageView.clearAnimation();  
			arrowImageView.startAnimation(animation);  
			
			tipsTextview.setText(R.string.stream_head_release);  
			Log.d(TAG, "RELEASE_To_REFRESH ");
			
			break;  
		case PULL_To_REFRESH:  
			
			progressBar.setVisibility(View.GONE);  
			tipsTextview.setVisibility(View.VISIBLE);  
			lastUpdatedTextView.setVisibility(View.VISIBLE);  
			arrowImageView.clearAnimation();  
			arrowImageView.setVisibility(View.VISIBLE);  
			Log.d(TAG, "PULL_To_REFRESH  " + isBack);
			if (isBack) {  
				isBack = false;  
				arrowImageView.clearAnimation();  
				arrowImageView.startAnimation(reverseAnimation);  
				
				tipsTextview.setText(R.string.stream_head_refresh);  
			} else {  
				tipsTextview.setText(R.string.stream_head_refresh);  
			}  
			break;  
			
		case REFRESHING:  
			
			headView.setPadding(0, 0, 0, 0);  
			headView.invalidate();  
			
			progressBar.setVisibility(View.VISIBLE);  
			arrowImageView.clearAnimation();  
			arrowImageView.setVisibility(View.GONE);  
			tipsTextview.setText(R.string.stream_head_refreshing);  
			lastUpdatedTextView.setVisibility(View.GONE);  
			
			Log.v(TAG, "REFRESHING");  
			break;  
		case DONE:  
			headView.setPadding(0, -1 * headContentHeight, 0, 0);  
			headView.invalidate();  
			
			progressBar.setVisibility(View.GONE);  
			arrowImageView.clearAnimation();  
			arrowImageView.setImageResource(R.drawable.z_arrow_down);  
			
			tipsTextview.setText(R.string.stream_head_refresh);  
			lastUpdatedTextView.setVisibility(View.VISIBLE);  
			Log.d(TAG, "DONE");
			
			break;  
		}  
	}  
	
	public void setonRefreshListener(OnRefreshListener refl) {
		refreshListener = refl;
	}
	
	public void setLoadOldListener(LoadOldListener loadmoreListener) {
		mLoadOldListener = loadmoreListener;
	}  
	
	public interface OnRefreshListener {  
		public void onRefresh();
        public void onFootShown(boolean show);
	}  
	
	public interface LoadOldListener {
		public void loadmore(boolean formore, boolean forceget, int count);
	}
	
	public void onRefreshComplete() {  
		state = DONE;  
		lastUpdatedTextView.setText(String.format(mContext.getString(R.string.stream_head_Last_updated),DateUtil.converToRelativeTime(mContext,
				new Date().getTime())));  
		changeHeaderViewByState();  
	}

    public void onRefreshStart() {
        state = REFRESHING;
        lastUpdatedTextView.setText(R.string.stream_head_refreshing);
        changeHeaderViewByState();
    }

	private void onRefresh(boolean headerRefresh, boolean showFoot) {
		if (refreshListener != null ) {
            if (headerRefresh) {
                refreshListener.onRefresh();
            }
            refreshListener.onFootShown(showFoot);
		}
	}  
	
	// estimated headView width and height 
	private void measureView(View child) {  
		ViewGroup.LayoutParams p = child.getLayoutParams();  
		if (p == null) {  
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,  
					ViewGroup.LayoutParams.WRAP_CONTENT);  
		}  
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);  
		int lpHeight = p.height;  
		int childHeightSpec;  
		if (lpHeight > 0) {  
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,  
					MeasureSpec.EXACTLY);  
		} else {  
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,  
					MeasureSpec.UNSPECIFIED);  
		}  
		child.measure(childWidthSpec, childHeightSpec);  
	}
	
	private int getScreenHeight(){
		DisplayMetrics  dm = new DisplayMetrics();  
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);  
		  
		return dm.heightPixels;		  
	}	
}  