package com.borqs.common.view;

import com.borqs.qiupu.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;


public class EsTextView extends TextView {
    final static String TAG = "EsTextView";
    
	public EsTextView(Context context) {
		this(context, null, 0);		
	}
	public EsTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);		
	}
	
	int needtakeoverClick = 0;
	public EsTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);	
		
		TypedArray sa = context.obtainStyledAttributes(attrs, R.styleable.EsTextView);
	    needtakeoverClick = sa.getInt(R.styleable.EsTextView_need_handler_click, 0);        
	    
	    bgspan = new BackgroundColorSpan(context.getResources().getColor(R.color.textview_span_bg));
	    
	    sa.recycle();
	}
	
	boolean isFinished = true;
	ClickableSpan clickSpan = null;
	static BackgroundColorSpan bgspan /*= new BackgroundColorSpan(Color.parseColor("#33b5e5"))*/;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	final int action;
    	if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO)
        {
    		action = event.getActionMasked();
        }
    	else
    	{
    		action = event.getAction();
    	}
    	
	   boolean isClickedClickedSpan = false;
	   if( getText() instanceof Spannable)
	   {
		   ClickableSpan[] links = ((Spannable) getText()).getSpans(getSelectionStart(),
                   getSelectionEnd(), ClickableSpan.class);

           if (links.length != 0) {
        	   isClickedClickedSpan = true;
        	   if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE)
        	   {
        		   isFinished = false;
        		   clickSpan = links[0];
        		   
        		   //TextPaint ts = getPaint();
       			   //ts.bgColor = Color.BLUE;       			
       			   //clickSpan.updateDrawState(ts);
       			   
       			   int start = ((Spannable) getText()).getSpanStart(clickSpan);
       			   int end   = ((Spannable) getText()).getSpanEnd(clickSpan);
       			   
       			   Log.d(TAG, "onTouchEvent start="+start + " end"+end);
       			
       			   ((Spannable) getText()).setSpan(bgspan, start, end, Spannable.SPAN_INTERMEDIATE|Spannable.SPAN_POINT_POINT);
        		   invalidate();
        	   }
        	   else
        	   {
        		   clickSpan = links[0];
        		   //TextPaint ts = getPaint();
                   //ts.bgColor = Color.TRANSPARENT;
   				   //clickSpan.updateDrawState(ts);
   				   if(needtakeoverClick == 1)
   				   {
   					   //clickSpan.onClick(this);
   				   }
   				   ((Spannable) getText()).removeSpan(bgspan);
   				   isFinished = true;   
   				   invalidate();
        	   }
           }
           else
           {   
        	    int x = (int) event.getX();
	            int y = (int) event.getY();

	            x -= getTotalPaddingLeft();
	            y -= getTotalPaddingTop();

	            x += getScrollX();
	            y += getScrollY();

	            Layout layout = getLayout();
	            int line = layout.getLineForVertical(y);
	            int off = layout.getOffsetForHorizontal(line, x);

	            ClickableSpan[] link = ((Spannable) getText()).getSpans(off, off, ClickableSpan.class);

	            if (link.length != 0) {
	            	ClickableSpan tmpclickSpan = link[0];
	            	int start = ((Spannable) getText()).getSpanStart(tmpclickSpan);
        			int end   = ((Spannable) getText()).getSpanEnd(tmpclickSpan);
        			
        			isClickedClickedSpan = true;
        			
	                if (action == MotionEvent.ACTION_UP) {
	                    link[0].onClick(this);
	                } else if (action == MotionEvent.ACTION_DOWN) {
	                	
	                    ((Spannable) getText()).setSpan(bgspan, start, end, Spannable.SPAN_INTERMEDIATE|Spannable.SPAN_POINT_POINT);
	          		    invalidate();
	          		    
	                    Selection.setSelection(((Spannable) getText()),
	                    		((Spannable) getText()).getSpanStart(link[0]),
	                    		((Spannable) getText()).getSpanEnd(link[0]));
	                }
	                
	            } else {
	            	Selection.removeSelection(((Spannable) getText()));
	            }
	            
        	   //isClickedClickedSpan = true;
        	   if(getUrls().length == 0)
        	   {
        		   isClickedClickedSpan = false;
        	   }
        	   
        	   
               if(clickSpan != null)
               {
        		   //TextPaint ts = getPaint();
                   //ts.bgColor = Color.TRANSPARENT;
			       //clickSpan.updateDrawState(ts);
			       
			       ((Spannable) getText()).removeSpan(bgspan);
			       invalidate();
               }
               
               ((Spannable) getText()).removeSpan(bgspan);
			   isFinished = true;
           }
	   }
	   
	   //just care about the click
	   //if(isClickedClickedSpan == true)
	   {
		   boolean ret = super.onTouchEvent(event);
		   //TODO
		   //When first time one the view, can't find the clickSpan	   
		   //
		   //what mean ret==true? I don't want the event travel?
		   //So remove the long press?
		   if(isClickedClickedSpan == true || ret == true)
		   {
			   cancelLongPress();
			   //also need the parent cancel
			   ViewParent vp = null;
			   View view = this;
			   while((vp = view.getParent()) != null)
			   {
				    if (vp instanceof SNSItemView) {
				    	((View)vp).cancelLongPress();
		                break;
		            }
				    
				    if (vp instanceof View == false) {
		                break;
		            }else{
				    	view = (View)vp;
				    }
			   }
		   }
		   
		   //Log.d(TAG, "onTouchEvent return="+ret + " isClickedClickedSpan="+isClickedClickedSpan + "action="+event.getAction());
	   }
	   return isClickedClickedSpan;
    }

	@Override
    protected void onDraw(Canvas canvas) {
		
		 // The LinkMovementMethod which should handle taps on links has not been installed
        // on non editable text that support text selection.
        // We reproduce its behavior here to open links for these.
		/*
		if(isFinished == false && clickSpan != null)
		{
			TextPaint ts = getPaint();
			ts.bgColor = Color.BLUE;
			clickSpan.updateDrawState(ts);
		}
		else 
		{	
			if(clickSpan != null)
			{
				TextPaint ts = getPaint();
				ts.bgColor = Color.TRANSPARENT;
				clickSpan.updateDrawState(ts);
			}
		}*/
		
		super.onDraw(canvas);
	}
}
