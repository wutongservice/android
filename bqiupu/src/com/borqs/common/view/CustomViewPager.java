package com.borqs.common.view;

import com.borqs.qiupu.QiupuConfig;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {
	private static final String TAG = "CustomViewPager";

	private int mIndex ;
	private int startX;
	private CustomViewPagerListenter mListener;
	public CustomViewPager(Context context) {
		super(context);
	}
	
	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if(mIndex >= 0) {
			mIndex = mListener.getCurentIndex();
		}
		if(QiupuConfig.DBLOGD)Log.d(TAG, "onInterceptTouchEvent: " + mIndex);
		if(mIndex > 0) {
			return false;
		}else if(mIndex < 0){
			return super.onInterceptTouchEvent(event);
		}else {
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
				startX = (int) event.getX();
			} else if(event.getAction() == MotionEvent.ACTION_MOVE) {
				int tmpx = (int) event.getX();
				if(tmpx < startX) {  // right move
					return false;
				}
			}
			return super.onInterceptTouchEvent(event);
		}
	}
	
	public void setIndex(int index) {
		mIndex = index;
	}
	
	public void setListener(CustomViewPagerListenter listener) {
		mListener = listener;
	}
	public interface CustomViewPagerListenter {
		public int getCurentIndex();
	}
}
