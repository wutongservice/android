package com.borqs.common.view;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import com.borqs.qiupu.R;

public class PageIndicatorLineStyleView extends View{
    private static final String TAG = "Qiupu.PageIndicatorLineStyleView";
    
    private NinePatch ninePatch;
	public PageIndicatorLineStyleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		final Resources res = context.getResources();
		indicatorBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.page_indicator_new);
		ninePatch = new NinePatch(indicatorBitmap, indicatorBitmap.getNinePatchChunk(), "indicator");
		pageIndicatorRawWidth = res.getDrawable(R.drawable.page_indicator_new).getIntrinsicWidth();
//	    pageIndicatorRawHeight = res.getDrawable(R.drawable.page_indicator).getIntrinsicHeight();
	}
	
	protected Bitmap indicatorBitmap;
    protected int leftX=0;
    protected int topY = 0;    
    protected RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(0, 0);    
    
    protected Rect rectD = new Rect(0,0,0,0);;
	
	@Override
	protected void onDraw(Canvas canvas) 
    {
		if(indicatorBitmap != null)
		{
		    ninePatch.draw(canvas, rectD);			    
		}
		/*if(getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			//use topY
			if(indicatorBitmap != null)
			{
			    //canvas.drawBitmap(indicatorBitmap, 0, topY, null);
			    ninePatch.draw(canvas, rectD);			    
			}
		}
		else
		{
			//use topX
			if(indicatorBitmap != null)
			{
				ninePatch.draw(canvas, rectD);
				//canvas.drawBitmap(indicatorBitmap, new Rect(0,0,indicatorBitmap.getWidth(), indicatorBitmap.getHeight()), new Rect(leftX, 0, leftX+lp.width, lp.height),  null);
			}
		}*/
    }
	
	
	/**
	 * @param width  raw image width
	 * @param height raw image height
	 * @param screenWidth, 
	 * TODO, please remove the status bar height??????????????????????????
	 *  
	 * @param pageIndex
	 * @param pageCount
	 */
	private int pageIndicatorRawWidth = 0;
//	private int pageIndicatorRawHeight = 0;
	public void refreshPosition(int width, int height, int screenWidth, int screenHeight, int pageIndex, int pageCount){
		if(width == 0)width = pageIndicatorRawWidth;
//		if(height == 0)height = pageIndicatorRawHeight;
		
		if(screenWidth < screenHeight){
			int drawWidth = 0;			
			
			if(pageCount == 1){  // keep full screen width
				drawWidth = screenWidth;
				leftX = 0;
			}else if(width * pageCount <= screenWidth){ // image raw width is shorter
				drawWidth = Math.round((screenWidth*1.0f)/pageCount);
				leftX = drawWidth*pageIndex;
			}else{  // image raw width is longer
				drawWidth = width;
				leftX = Math.round(((screenWidth-drawWidth)*1.0f*pageIndex)/(pageCount-1));
			}
			
			lp.width  = drawWidth+1;
			lp.height = (int)(6*getContext().getResources().getDisplayMetrics().density);

		}else{
			int drawHeight = 0;			
			
			if(pageCount == 1){  // keep full screen width
				drawHeight = screenHeight;
				topY = 0;
			}else if(width * pageCount <= screenHeight){ // image raw width is shorter
				drawHeight = Math.round((screenHeight*1.0f)/pageCount);
				topY = drawHeight*pageIndex;
			}else{  // image raw width is longer
				drawHeight = width;
				topY = (screenHeight-drawHeight)*pageIndex/(pageCount-1);
			}
			
			lp.width  = (int)(6*getContext().getResources().getDisplayMetrics().density);;
			lp.height = drawHeight;

		}		

		rectD.left = leftX;
		rectD.top  = topY;
		rectD.right= leftX+lp.width;
		rectD.bottom = topY+lp.height;
		
		invalidate();
	}
	
	/**
	 * @param drawWidth  current image width
	 * @param drawHeight current image height
	 * @param deltaX  user move deltaX 
	 * @param screenWidth
	 * @param currentPageIndex
	 * @param pageCount
	 */	
	public void movePosition(int drawWidth, int drawHeight, float deltaX, int screenWidth, int screenHeight,int currentPageIndex, int pageCount){
		if(screenWidth < screenHeight){
			float curX = 0.f;
			float dX = 0.f ;
			
			if((pageCount == 1) || (currentPageIndex == 0 && deltaX <= 0) || currentPageIndex ==  (pageCount -1) && deltaX >= 0){
				return; // needn't move
			}else if(drawWidth * pageCount <= screenWidth){
				curX = drawWidth*currentPageIndex;
				dX = (float)(deltaX*drawWidth)/(float)screenWidth;
				leftX = Math.round(curX +  dX);
			}else{
				curX = (screenWidth-drawWidth)*currentPageIndex/(pageCount-1);
				dX = (float)deltaX*(float)((screenWidth-drawWidth)/(float)(pageCount-1))/(float)screenWidth;
				leftX = Math.round(curX +  dX);
			}
			
			lp.width  = drawWidth+1;
			lp.height = drawHeight;
			
		}else{			
			float curY = 0.f;
			float dY = 0.f ;
			
			if((pageCount == 1) || (currentPageIndex == 0 && deltaX <= 0) || currentPageIndex ==  (pageCount -1) && deltaX >= 0){
				return; // needn't move
			}else if(drawHeight * pageCount <= screenWidth){
				curY = drawHeight*currentPageIndex;
				dY = (float)(deltaX*drawHeight)/(float)screenWidth;
				topY = Math.round(curY +  dY);
			}else{
				curY = (screenWidth-drawHeight)*currentPageIndex/(pageCount-1);
				dY = (float)deltaX*(float)((drawHeight-drawWidth)/(float)(pageCount-1))/(float)screenWidth;
				topY = Math.round(curY +  dY);
			}
			
			lp.width  = drawWidth;
			lp.height = drawHeight;

		}
		
		rectD.left = leftX;
		rectD.top  = topY;
		rectD.right= leftX+lp.width;
		rectD.bottom = topY+lp.height;
		
		invalidate();
	}
}
