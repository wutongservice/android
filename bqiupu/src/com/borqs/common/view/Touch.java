package com.borqs.common.view;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class Touch implements OnTouchListener {  
	  private Touch.ClickListener clickListener;
	 // These matrices will be used to move and zoom image  
	 Matrix matrix = new Matrix();  
	 Matrix savedMatrix = new Matrix();  
//	 Bitmap bitmap;
	 DisplayMetrics dm;
	 // We can be in one of these 3 states  
	 static final int NONE = 0;  
	 static final int DRAG = 1;  
	 static final int ZOOM = 2; 
	 static final int CLICK = 3;
	 int mode = NONE;  
	  
	 // Remember some things for zooming  
	 PointF start = new PointF();  
	 PointF mid = new PointF();  
	 float oldDist = 1f;  
	 float new_dist = 1f;  
	 float old_scale = 1f;  
	  
	 
	 public Touch(DisplayMetrics dm) {
		super();
		this.dm = dm;
	}

	public void setClickListener(Touch.ClickListener clickListener) {
		this.clickListener = clickListener;
	}

	@Override  
	 public boolean onTouch(View v, MotionEvent event) {  
	  ImageView view = (ImageView) v;  
//	  bitmap = view.getDrawingCache();
	  float min_width = view.getWidth()-100;
	  // Dump touch event to log  
	  
	  dumpEvent(event);  
	  
	  // Handle touch events here...  
	  switch (event.getAction() & MotionEvent.ACTION_MASK) {  
	  case MotionEvent.ACTION_DOWN:  
	   savedMatrix.set(matrix);  
	   start.set(event.getX(), event.getY());  
	   mode = CLICK;  
	   break;  
	  case MotionEvent.ACTION_POINTER_DOWN:  
	   oldDist = spacing(event);  
	   if (oldDist > 10f) {  
	    savedMatrix.set(matrix);  
	    midPoint(mid, event);  
	    mode = ZOOM;  
	   }  
	   break;  
	  case MotionEvent.ACTION_UP:
		  if (mode == ZOOM) { 
			  float[] values = new float[9];
			  matrix.getValues(values);
			  Rect r = view.getDrawable().getBounds();
			  float width = values[0]*r.width();
//			  float height = values[4]*r.height();
			  
			  if(width<min_width) {
				  float scale = min_width/width;
				  matrix.postScale(scale, scale, mid.x, mid.y); 
			  }
		  }else if(mode == CLICK){
			  if(clickListener!=null) {
				  matrix.reset();
				  clickListener.onClick();
			  }
			  
		  }  
	   mode = NONE;  
	   break;
	  case MotionEvent.ACTION_POINTER_UP:  
		  if (mode == ZOOM) { 
			  float[] values = new float[9];
			  matrix.getValues(values);
			  Rect r = view.getDrawable().getBounds();
			  float width = values[0]*r.width();
//			  float height = values[4]*r.height();
			  
			  if(width<min_width) {
				  float scale = min_width/width;
				  matrix.postScale(scale, scale, mid.x, mid.y); 
			  }
			   }  
	   mode = NONE;  
	   break;  
	  case MotionEvent.ACTION_MOVE:  
	   if (mode == CLICK||mode == DRAG) {
		   float move_dis = spacingForMove(event);
	    if(move_dis > 10f) {
//	    	float x = event.getX() - start.x;  
//			  float y = event.getY() - start.y;
//			  Log.v("wgl", "***************view.setImageMatrix******************");
//	    	Log.v("wgl", "***************xxxxxx="+x+"******************");
//	  	  Matrix image_matrix =view.getImageMatrix();
//	  	  float[] values = new float[9];
//	  	  image_matrix.getValues(values);
//	  	  float globalX = values[2];
//	  	  float globalY = values[5];
//	  	  Rect r = view.getDrawable().getBounds();
//	  	  float width = values[0]*r.width();
//	  	  float height = values[4]*r.height();
//	  	  Rect rect = new Rect();
//	  	  view.getLocalVisibleRect(rect);
//	  	  Log.v("wgl", "--------------**left--***"+rect.left+"------width---**"+rect.width()+"---------");
//	  	  Log.v("wgl", "--------------globalX--**"+globalX+"------globalY---**"+globalY+"---------");
//	  	  Log.v("wgl", "--------------width--**"+width+"------height---**"+height+"---------");
//	  	  if(x>0) {
//	  		  //右移
//	  		  if(globalX<0 ) {
//	  			  if((x+globalX)>0) {
//	  				  x=-globalX;
//	  			  }
//	  			matrix.set(savedMatrix);  
//		  		  matrix.postTranslate(x, event.getY() - start.y); 
//	  		  }else {
//	  			  //nothing
//	  		  }
//	  	  }else if(x<0) {
//	  		  //左移
//	  		  if((globalX+width)>rect.width()) {
//	  			  if((globalX+width-x)<rect.width()) {
//	  				  x = globalX+width+rect.width();
//	  			  }
//	  			  matrix.set(savedMatrix);  
//		  		  matrix.postTranslate(x, event.getY() - start.y); 
//	  		  }else {
//	  			  //nothing
//	  		  }
//	  	  }
////	  	  if(globalX>0||(globalX+width)<rect.width()) {
////	  		  mode = DRAG;
////	  	  }else {
////	  		  
////	  		  matrix.set(savedMatrix);  
////	  		  matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); 
////	  	  }
	    	
	    	matrix.set(savedMatrix);  
		  matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
	  	  mode = DRAG;
	    	
	    }     
	   } else if (mode == ZOOM) {  
		   float newDist = spacing(event);
	    if (newDist > 10f) {  
	     matrix.set(savedMatrix);  
	     float scale = newDist / oldDist; 
	     new_dist = newDist;
	     matrix.postScale(scale, scale, mid.x, mid.y);  
	    }  
	   }  
	   break;  
	  }  
	  if (mode != CLICK) {
		  view.setImageMatrix(matrix);
	  }
	  CheckView(view);
	  
	  return true; // indicate event was handled  
	 }  
	
	/**
     * 限制最大最小缩放比例，自动居中
     */
    private void CheckView(ImageView imgView) {
        float p[] = new float[9];
        matrix.getValues(p);
        if (mode == ZOOM) {
            if (p[0] < minScaleR) {
                matrix.setScale(minScaleR, minScaleR);
            }
            if (p[0] > MAX_SCALE) {
                matrix.set(savedMatrix);
            }
        }
        center(imgView);
    }
    float minScaleR = 1f;// 最小缩放比例
    static final float MAX_SCALE = 4f;// 最大缩放比例
    /**
     * 最小缩放比例，最大为100%
     */
//    private void minZoom() {
//        minScaleR = Math.min(
//                (float) dm.widthPixels / (float) bitmap.getWidth(),
//                (float) dm.heightPixels / (float) bitmap.getHeight());
//        if (minScaleR < 1.0) {
//            matrix.postScale(minScaleR, minScaleR);
//        }
//    }

    private void center(ImageView imgView) {
        center(imgView,true, true);
    }

    /**
     * 横向、纵向居中
     */
    protected void center(ImageView imgView,boolean horizontal, boolean vertical) {
    	
    	Matrix image_matrix =imgView.getImageMatrix();
	  	  float[] values = new float[9];
	  	  image_matrix.getValues(values);
	  	  Rect r = imgView.getDrawable().getBounds();
	  	  float imgView_width = values[0]*r.width();
	  	  float imgView_height = values[4]*r.height();
    	
        Matrix m = new Matrix();
        m.set(matrix);
        RectF rect = new RectF(0, 0,imgView_width,imgView_height);
        m.mapRect(rect);

        float height = rect.height();
        float width = rect.width();

        float deltaX = 0, deltaY = 0;

        if (vertical) {
            // 图片小于屏幕大小，则居中显示。大于屏幕，上方留空则往上移，下方留空则往下移
            int screenHeight = dm.heightPixels;
            if (height < screenHeight) {
                deltaY = (screenHeight - height) / 2 - rect.top;
            } else if (rect.top > 0) {
                deltaY = -rect.top;
            } else if (rect.bottom < screenHeight) {
                deltaY = imgView.getHeight() - rect.bottom;
            }
        }

        if (horizontal) {
            int screenWidth = dm.widthPixels;
            if (width < screenWidth) {
                deltaX = (screenWidth - width) / 2 - rect.left;
            } else if (rect.left > 0) {
                deltaX = -rect.left;
            } else if (rect.right < screenWidth) {
                deltaX = screenWidth - rect.right;
            }
        }
        matrix.postTranslate(deltaX, deltaY);
    }
	  
	 /** Show an event in the LogCat view, for debugging */  
	 private void dumpEvent(MotionEvent event) {  
	  String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",  
	    "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };  
	  StringBuilder sb = new StringBuilder();  
	  int action = event.getAction();  
	  int actionCode = action & MotionEvent.ACTION_MASK;  
	  sb.append("event ACTION_").append(names[actionCode]);  
	  if (actionCode == MotionEvent.ACTION_POINTER_DOWN  
	    || actionCode == MotionEvent.ACTION_POINTER_UP) {  
	   sb.append("(pid ").append(  
	     action >> MotionEvent.ACTION_POINTER_ID_SHIFT);  
	   sb.append(")");  
	  }  
	  sb.append("[");  
	  for (int i = 0; i < event.getPointerCount(); i++) {  
	   sb.append("#").append(i);  
	   sb.append("(pid ").append(event.getPointerId(i));  
	   sb.append(")=").append((int) event.getX(i));  
	   sb.append(",").append((int) event.getY(i));  
	   if (i + 1 < event.getPointerCount())  
	    sb.append(";");  
	  }  
	  sb.append("]");  
	 }  
	  
	 /** Determine the space between the first two fingers */  
	 private float spacing(MotionEvent event) {  
	  float x = event.getX(0) - event.getX(1);  
	  float y = event.getY(0) - event.getY(1);  
	  return FloatMath.sqrt(x * x + y * y);  
	 }  
	 
	 private float spacingForMove(MotionEvent event) {  
		  float x = event.getX() - start.x;  
		  float y = event.getY() - start.y;  
		  return FloatMath.sqrt(x * x + y * y);  
		 } 
	  
	 /** Calculate the mid point of the first two fingers */  
	 private void midPoint(PointF point, MotionEvent event) {  
	  float x = event.getX(0) + event.getX(1);  
	  float y = event.getY(0) + event.getY(1);  
	  point.set(x / 2, y / 2);  
	 }  
	 
	 public static interface ClickListener {
		 void onClick(); 
	 }
}
