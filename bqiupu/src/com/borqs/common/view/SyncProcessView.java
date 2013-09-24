package com.borqs.common.view;

import com.borqs.qiupu.R;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class SyncProcessView extends View{
    private static final String TAG = "SyncProcessView";
    
    private NinePatch ninePatch;
    
	public SyncProcessView(Context context, AttributeSet attrs) {
		super(context, attrs);
		final Resources res = context.getResources();
		indicatorBitmap = BitmapFactory.decodeResource(res, R.drawable.process);
		ninePatch = new NinePatch(indicatorBitmap, indicatorBitmap.getNinePatchChunk(), "process");
	}
	
	protected Bitmap indicatorBitmap;
    protected int leftX=0;
    protected int topY = 0;    
    protected RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(0, 0);    
    
    protected Rect rectD = new Rect(0,0,0,0);
	
	@Override
	protected void onDraw(Canvas canvas) 
    {
		if(getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			//use topY
			if(indicatorBitmap != null)
			{
			    ninePatch.draw(canvas, rectD);			    
			}
		}
		else
		{
			//use topX
			if(indicatorBitmap != null)
			{
				ninePatch.draw(canvas, rectD);
			}
		}
    }
	
	
	public void refreshProcess(int width, int height, float process){
		leftX = 0;
		lp.width  = (int)(width * process);
		lp.height = (int)(6*getContext().getResources().getDisplayMetrics().density);

		rectD.left = leftX;
		rectD.top  = topY;
		rectD.right= leftX+lp.width;
		rectD.bottom = topY+lp.height;
		
		invalidate();
	}
}
