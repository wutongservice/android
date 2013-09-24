package com.borqs.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;


public class EsImageView extends ImageView {

	public EsImageView(Context context) {
		this(context, null, 0);		
	}
	
	public EsImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);		
	}
	
	public EsImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		super.onDraw(canvas);
		
		canvas.drawColor(0x2f888888);
	}
}
