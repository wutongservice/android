package com.borqs.common.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.borqs.qiupu.R;

public class RatingView extends ImageView{
    private static final String TAG = "Qiupu.RatingView";
    private static Bitmap ratingFullBitmap, ratingHalfBitmap, ratingNullBitmap;
    private static float density;
    private static float bitmapWidth, bitmapHeight;
    private static float SLOP;
    private static int   densityDisplay;
    
	public RatingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context);
	}

	private static Bitmap bitmap_00;
	private static Bitmap bitmap_05;
	private static Bitmap bitmap_10;
	private static Bitmap bitmap_15;
	private static Bitmap bitmap_20;
	private static Bitmap bitmap_25;
	private static Bitmap bitmap_30;
	private static Bitmap bitmap_35;
	private static Bitmap bitmap_40;
	private static Bitmap bitmap_45;
	private static Bitmap bitmap_50;
	
    private void init(Context context) {
		final Resources res = getResources();
		
		if(ratingFullBitmap == null)
		{
			ratingFullBitmap = BitmapFactory.decodeResource(res, R.drawable.rate_star_small_on);
			ratingHalfBitmap = BitmapFactory.decodeResource(res, R.drawable.rate_star_small_half);
			ratingNullBitmap = BitmapFactory.decodeResource(res, R.drawable.rate_star_small_off);
			
			
			density = res.getDisplayMetrics().density;
			SLOP = 4 * density;
			bitmapWidth = res.getDimension(R.dimen.rate_span_width);
			bitmapHeight = res.getDimension(R.dimen.rate_span_heigth);
			
			densityDisplay = res.getDisplayMetrics().densityDpi;
		}
		  
	}
    
    public void drawRating(float rating){
//    	Log.d(TAG, "drawRating rating:"+rating);
    	Bitmap ratingBitmap = null;
    	float left = 0;
    	if(rating <= 0.1){
    		if(bitmap_00 == null)
    		{
    			bitmap_00 = Bitmap.createBitmap((int)bitmapWidth, (int)bitmapHeight, Config.ARGB_8888);
    			bitmap_00.setDensity(Bitmap.DENSITY_NONE);
    	    	
    	    	final Canvas canvas = new Canvas(bitmap_00);
    	    	
    	    	for(int i = 0; i < 5; i++){            
        			canvas.drawBitmap(ratingNullBitmap, left, 0, null);
        			left = left + (SLOP + ratingNullBitmap.getWidth()); 
        		}	
    		}
    		
    		ratingBitmap = bitmap_00;
    	}else if(0.1< rating && rating <= 0.5){
    		if(bitmap_05 == null)
    		{
    			bitmap_05 = Bitmap.createBitmap((int)bitmapWidth, (int)bitmapHeight, Config.ARGB_8888);
    			bitmap_05.setDensity(Bitmap.DENSITY_NONE);
    	    	
    	    	final Canvas canvas = new Canvas(bitmap_05);
    	    	
    	    	for(int i = 0; i < 5; i++){        
        			if(i == 0){
        				canvas.drawBitmap(ratingHalfBitmap, left, 0, null);
        			}else{
        				canvas.drawBitmap(ratingNullBitmap, left, 0, null);
        			}
            		left += (SLOP + ratingFullBitmap.getWidth()); 
            	}	
    		}
    		
    		ratingBitmap = bitmap_05;
    	}else if(0.5 < rating && rating <= 1){
    		if(bitmap_10 == null)
    		{
    			bitmap_10 = Bitmap.createBitmap((int)bitmapWidth, (int)bitmapHeight, Config.ARGB_8888);
    			bitmap_10.setDensity(Bitmap.DENSITY_NONE);
    	    	
    	    	final Canvas canvas = new Canvas(bitmap_10);
    	    	
    	    	for(int i = 0; i < 5; i++){        
        			if(i == 0){
        				canvas.drawBitmap(ratingFullBitmap, left, 0, null);
        			}else{
        				canvas.drawBitmap(ratingNullBitmap, left, 0, null);
        			}
            		left += (SLOP + ratingFullBitmap.getWidth()); 
            	}	
    		}
    		
    		ratingBitmap = bitmap_10;
    	}else if(1 < rating && rating <= 1.5){
    		if(bitmap_15 == null)
    		{
    			bitmap_15 = Bitmap.createBitmap((int)bitmapWidth, (int)bitmapHeight, Config.ARGB_8888);
    			bitmap_15.setDensity(Bitmap.DENSITY_NONE);
    	    	
    	    	final Canvas canvas = new Canvas(bitmap_15);
    	    	
    	    	for(int i = 0; i < 5; i++){        
        			if(i == 0){
        				canvas.drawBitmap(ratingFullBitmap, left, 0, null);
        			}else if(i == 1){
        				canvas.drawBitmap(ratingHalfBitmap, left, 0, null);
        			}else{
        				canvas.drawBitmap(ratingNullBitmap, left, 0, null);
        			}
            		left += (SLOP + ratingFullBitmap.getWidth()); 
            	}	
    		}
    		
    		ratingBitmap = bitmap_15;
    	}else if(1.5 < rating && rating <= 2){
    		if(bitmap_20 == null)
    		{
    			bitmap_20 = Bitmap.createBitmap((int)bitmapWidth, (int)bitmapHeight, Config.ARGB_8888);
    			bitmap_20.setDensity(Bitmap.DENSITY_NONE);
    	    	
    	    	final Canvas canvas = new Canvas(bitmap_20);
    	    	
    	    	for(int i = 0; i < 5; i++){        
    	    		if(i < 2){
    					canvas.drawBitmap(ratingFullBitmap, left, 0, null);
    				}else{
    					canvas.drawBitmap(ratingNullBitmap, left, 0, null);
    				}
    	    		left += (SLOP + ratingFullBitmap.getWidth()); 
        		}
    		}    		
    		ratingBitmap = bitmap_20;
    	}else if(2 < rating && rating <= 2.5){    		
    		if(bitmap_25 == null)
    		{
    			bitmap_25 = Bitmap.createBitmap((int)bitmapWidth, (int)bitmapHeight, Config.ARGB_8888);
    			bitmap_25.setDensity(Bitmap.DENSITY_NONE);
    	    	
    	    	final Canvas canvas = new Canvas(bitmap_25);
    	    	
    	    	for(int i = 0; i < 5; i++){        
        			if(i < 2){
        				canvas.drawBitmap(ratingFullBitmap, left, 0, null);
        			}else if(i == 2){
        				canvas.drawBitmap(ratingHalfBitmap, left, 0, null);
        			}else{
        				canvas.drawBitmap(ratingNullBitmap, left, 0, null);
        			}
            		left += (SLOP + ratingFullBitmap.getWidth()); 
            	}	
    		}    		
    		ratingBitmap = bitmap_25;
    	}else if(2.5 < rating && rating <= 3){
    		if(bitmap_30 == null)
    		{
    			bitmap_30 = Bitmap.createBitmap((int)bitmapWidth, (int)bitmapHeight, Config.ARGB_8888);
    			bitmap_30.setDensity(Bitmap.DENSITY_NONE);
    	    	
    	    	final Canvas canvas = new Canvas(bitmap_30);
    	    	
    	    	for(int i = 0; i < 5; i++){        
    	    		if(i < 3){
    					canvas.drawBitmap(ratingFullBitmap, left, 0, null);
    				}else{
    					canvas.drawBitmap(ratingNullBitmap, left, 0, null);
    				}
    	    		left += (SLOP + ratingFullBitmap.getWidth()); 
        		}
    		}    		
    		ratingBitmap = bitmap_30;
    		
    	}else if(3 < rating && rating <= 3.5){
    		if(bitmap_35 == null)
    		{
    			bitmap_35 = Bitmap.createBitmap((int)bitmapWidth, (int)bitmapHeight, Config.ARGB_8888);
    			bitmap_35.setDensity(Bitmap.DENSITY_NONE);
    	    	
    	    	final Canvas canvas = new Canvas(bitmap_35);	    	
    	    	for(int i = 0; i < 5; i++){        
        			if(i < 3){
        				canvas.drawBitmap(ratingFullBitmap, left, 0, null);
        			}else if(i == 3){
        				canvas.drawBitmap(ratingHalfBitmap, left, 0, null);
        			}else{
        				canvas.drawBitmap(ratingNullBitmap, left, 0, null);
        			}
            		left += (SLOP + ratingFullBitmap.getWidth()); 
            	}	
    		}    		
    		ratingBitmap = bitmap_35;
    		
    		
    	}else if(3.5 < rating && rating <= 4){    		
    		if(bitmap_40 == null)
    		{
    			bitmap_40 = Bitmap.createBitmap((int)bitmapWidth, (int)bitmapHeight, Config.ARGB_8888);
    			bitmap_40.setDensity(Bitmap.DENSITY_NONE);
    	    	
    	    	final Canvas canvas = new Canvas(bitmap_40);	    	
    	    	for(int i = 0; i < 5; i++){        
    	    		if(i < 4){
    					canvas.drawBitmap(ratingFullBitmap, left, 0, null);
    				}else{
    					canvas.drawBitmap(ratingNullBitmap, left, 0, null);
    				}
    	    		left += (SLOP + ratingFullBitmap.getWidth()); 
        		}
    		}    		
    		ratingBitmap = bitmap_40;
    	}else if(4 < rating && rating <= 4.5){    		
    		if(bitmap_45 == null)
    		{
    			bitmap_45 = Bitmap.createBitmap((int)bitmapWidth, (int)bitmapHeight, Config.ARGB_8888);
    			bitmap_45.setDensity(Bitmap.DENSITY_NONE);
    	    	
    	    	final Canvas canvas = new Canvas(bitmap_45);	    	
    	    	for(int i = 0; i < 5; i++){        
    	    		if(i < 4){
    					canvas.drawBitmap(ratingFullBitmap, left, 0, null);
    				}else{
    					canvas.drawBitmap(ratingHalfBitmap, left, 0, null);
    				}
    	    		left += (SLOP + ratingFullBitmap.getWidth()); 
        		}
    		}    		
    		ratingBitmap = bitmap_45;
    		
    	}else {
    		if(bitmap_50 == null)
    		{
    			bitmap_50 = Bitmap.createBitmap((int)bitmapWidth, (int)bitmapHeight, Config.ARGB_8888);
    			bitmap_50.setDensity(Bitmap.DENSITY_NONE);
    	    	
    	    	final Canvas canvas = new Canvas(bitmap_50);	    	
    	    	for(int i = 0; i < 5; i++){            
            		canvas.drawBitmap(ratingFullBitmap, left, 0, null);
            		left += (SLOP + ratingFullBitmap.getWidth()); 
            	}
    		}    		
    		ratingBitmap = bitmap_50;
        }
    	
        setImageBitmap(ratingBitmap);
    }
    
    public void drawRatingNoNull(float rating){
//    	Log.d(TAG, "drawRating rating:"+rating);
    	float left = 0;
    	Bitmap ratingBitmap = Bitmap.createBitmap((int)bitmapWidth, (int)bitmapHeight, Config.ARGB_8888);
    	ratingBitmap.setDensity(densityDisplay);
    	
    	final Canvas canvas = new Canvas(ratingBitmap);
    	
    	if(rating <= 0.1){
    		
    	}else if(0.1< rating && rating <= 0.5){
    		canvas.drawBitmap(ratingHalfBitmap, left, 0, null);    			
    	}else if(0.5 < rating && rating <= 1){
    		canvas.drawBitmap(ratingFullBitmap, left, 0, null);
    	}else if(1 < rating && rating <= 1.5){
    		for(int i = 0; i < 5; i++){        
    			if(i == 0){
    				canvas.drawBitmap(ratingFullBitmap, left, 0, null);
    			}else if(i == 1){
    				canvas.drawBitmap(ratingHalfBitmap, left, 0, null);
    			}else{
    				break;
    			}
        		left += (SLOP + ratingFullBitmap.getWidth()); 
        	}	
    	}else if(1.5 < rating && rating <= 2){
    		for(int i = 0; i < 5; i++){        
	    		if(i < 2){
					canvas.drawBitmap(ratingFullBitmap, left, 0, null);
				}else{
					break;
				}
	    		left += (SLOP + ratingFullBitmap.getWidth()); 
    		}
    	}else if(2 < rating && rating <= 2.5){
    		for(int i = 0; i < 5; i++){        
    			if(i < 2){
    				canvas.drawBitmap(ratingFullBitmap, left, 0, null);
    			}else if(i == 2){
    				canvas.drawBitmap(ratingHalfBitmap, left, 0, null);
    			}else{
    				break;
    			}
        		left += (SLOP + ratingFullBitmap.getWidth()); 
        	}	
    	}else if(2.5 < rating && rating <= 3){
    		for(int i = 0; i < 5; i++){        
	    		if(i < 3){
					canvas.drawBitmap(ratingFullBitmap, left, 0, null);
				}else{
					break;
				}
	    		left += (SLOP + ratingFullBitmap.getWidth()); 
    		}
    	}else if(3 < rating && rating <= 3.5){
    		for(int i = 0; i < 5; i++){        
    			if(i < 3){
    				canvas.drawBitmap(ratingFullBitmap, left, 0, null);
    			}else if(i == 3){
    				canvas.drawBitmap(ratingHalfBitmap, left, 0, null);
    			}else{
    				break;
    			}
        		left += (SLOP + ratingFullBitmap.getWidth()); 
        	}	
    	}else if(3.5 < rating && rating <= 4){
    		for(int i = 0; i < 5; i++){        
	    		if(i < 4){
					canvas.drawBitmap(ratingFullBitmap, left, 0, null);
				}else{
					break;
				}
	    		left += (SLOP + ratingFullBitmap.getWidth()); 
    		}
    	}else if(4 < rating && rating <= 4.5){
    		for(int i = 0; i < 5; i++){        
	    		if(i < 4){
					canvas.drawBitmap(ratingFullBitmap, left, 0, null);
				}else{
					canvas.drawBitmap(ratingHalfBitmap, left, 0, null);
				}
	    		left += (SLOP + ratingFullBitmap.getWidth()); 
    		}
    	}else {
    		for(int i = 0; i < 5; i++){            
        		canvas.drawBitmap(ratingFullBitmap, left, 0, null);
        		left += (SLOP + ratingFullBitmap.getWidth()); 
        	}
    	}
    	
        setImageBitmap(ratingBitmap);
    }

    public static void clearStaticData(){
    }
}
