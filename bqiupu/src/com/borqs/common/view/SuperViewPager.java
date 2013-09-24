package com.borqs.common.view;

import twitter4j.util.ImageUpload.ImgLyOAuthUploader;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.borqs.qiupu.fragment.PhotoFragment;
import com.borqs.qiupu.ui.bpc.PhotosViewActivity.PlacePicsAdapter;

public class SuperViewPager extends ViewPager {
//	private VelocityTracker vTracker = null; 
	
	private boolean isTwo = false;
	
	private int currItem = 0;
	private MotionEvent oldEvent = null;
	
	
	public int getCurrItem() {
		return currItem;
	}



	public void setCurrItem(int currItem) {
		this.currItem = currItem;
	}



	public SuperViewPager(Context context) {
		super(context);
	}
	
	
	
	public SuperViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	@Override
	public boolean onTouchEvent(MotionEvent ev) {
//		int action = ev.getAction();  
//        switch(action){  
//        case MotionEvent.ACTION_DOWN:  
//            if(vTracker == null){  
//                vTracker = VelocityTracker.obtain();  
//            }else{  
//                vTracker.clear();  
//            }  
//            vTracker.addMovement(event);  
//            break;  
		return super.onTouchEvent(ev);
	}
	float old_x = 0;
	float old_y = 0;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(getAdapter() != null) {
			PlacePicsAdapter adapter = (PlacePicsAdapter)getAdapter();
			if(adapter.getItem(getCurrItem()) != null) {
				
				PhotoFragment fragment = (PhotoFragment)(adapter.instantiateItem(this,getCurrItem()));
				float new_x = ev.getX();
				float new_y = ev.getY();
//		View v= getChildAt(currItem);
				ImageView image = fragment.img;
				if(image == null) {
					return super.onInterceptTouchEvent(ev);
				}
//		ImageView image = (ImageView)findViewWithTag(Integer.valueOf(currItem));
//		ImageView image = (ImageView)v.findViewWithTag(Integer.valueOf(5));
				
				Matrix matrix =image.getImageMatrix();
				float[] values = new float[9];
				matrix.getValues(values);
				float globalX = values[2];
				float globalY = values[5];
				if(image.getDrawable() == null) {
					return super.onInterceptTouchEvent(ev);
				}
				Rect r = image.getDrawable().getBounds();
				float width = values[0]*r.width();
				float height = values[4]*r.height();
				//  Log.v("wgl", "--------------left--"+rect.left+"------width---"+rect.width()+"---------");
//		  Log.v("wgl", "***************globalX*********"+globalX+"************globalY*********"+globalY+"***********");
//		  Log.v("wgl", "***************width*********"+width+"************height*********"+height+"***********");
				switch (ev.getAction() & MotionEventCompat.ACTION_MASK) {
				case MotionEventCompat.ACTION_POINTER_DOWN:
					isTwo = true;
//			 Log.v("wgl", "----------------ACTION_POINTER_DOWN------------------");
					break;
				case MotionEventCompat.ACTION_POINTER_UP:
					Log.v("wgl", "----------------ACTION_POINTER_UP------------------");
					isTwo = false;
					break;
					
				}
				
				if(ev.getAction()==MotionEvent.ACTION_MOVE&&isTwo) {
//			 Log.v("wgl", "----------------MotionEvent.ACTION_MOVE&&isTwo------------------");
					old_x = ev.getX();
					old_y = ev.getY();
					return false;
				}else if(ev.getAction()==MotionEvent.ACTION_MOVE&&!isTwo){
					if(old_x>new_x) {
						old_x = ev.getX();
						old_y = ev.getY();
						//左移
//				 Log.v("wgl", "----------------左移----------------");
						if(globalX+width<image.getWidth()+5) {
							return super.onInterceptTouchEvent(ev);
						}
						else{
							
							return false;
						}
					}else {
						old_x = ev.getX();
						old_y = ev.getY();
//				 Log.v("wgl", "----------------//右移----------------");
						//右移
						if(globalX>-5) {
							return super.onInterceptTouchEvent(ev);
						}
						else{
							
							return false;
						}
					}
					
				}
			}
			}
		old_x = ev.getX();
		old_y = ev.getY();
		 return super.onInterceptTouchEvent(ev);
	}
}
