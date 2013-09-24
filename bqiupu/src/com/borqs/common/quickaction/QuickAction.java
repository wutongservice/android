package com.borqs.common.quickaction;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.ViewGroup;

import java.util.ArrayList;
import com.borqs.qiupu.R;

/**
 * Popup window, shows action list as icon and text like the one in Gallery3D app. 
 * 
 * @author Lorensius. W. T
 */
public class QuickAction extends CustomPopupWindow {
	private final static String TAG = "Launcher.QuickAction";
	private final View root;
	private final View quickcontact;
	private final ImageView mArrowUp;
	private final ImageView mArrowDown;
	private final LayoutInflater inflater;
	private final Context context;
	
	protected static final int ANIM_GROW_FROM_LEFT = 1;
	public static final int ANIM_GROW_FROM_RIGHT = 2;
	public static final int ANIM_GROW_FROM_CENTER = 3;
	protected static final int ANIM_REFLECT = 4;
	protected static final int ANIM_AUTO = 5;
	
	private int animStyle;
	private ViewGroup mTrack;
	private HorizontalScrollView scroller;
	private ArrayList<ActionItem> actionList;
	private Animation mTrackAnim;
	
	/**
	 * Constructor
	 * 
	 * @param anchor {@link View} on where the popup window should be displayed
	 */
	public QuickAction(View anchor) {
		super(anchor);
		
		actionList	= new ArrayList<ActionItem>();
		context		= anchor.getContext();
		inflater 	= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		root		= (ViewGroup) inflater.inflate(R.layout.popup, null);
		
		mArrowDown 	= (ImageView) root.findViewById(R.id.arrow_down);
		mArrowUp 	= (ImageView) root.findViewById(R.id.arrow_up);
		quickcontact = root.findViewById(R.id.quickcontact);
		setContentView(root);
	    
		mTrack 			= (ViewGroup) root.findViewById(R.id.tracks);
		scroller		= (HorizontalScrollView) root.findViewById(R.id.scroller);
		animStyle		= ANIM_AUTO;
		
		// Prepare track entrance animation
        mTrackAnim = AnimationUtils.loadAnimation(context, R.anim.quickmms);
        mTrackAnim.setInterpolator(new Interpolator() {
            public float getInterpolation(float t) {
                // Pushes past the target area, then snaps back into place.
                // Equation for graphing: 1.2-((x*1.6)-1.1)^2
                final float inner = (t * 1.55f) - 1.1f;
                return 1.2f - inner * inner;
            }
        });
	}

	public void clearData()
	{
		actionList.clear();
	}
	/**
	 * Set animation style
	 * 
	 * @param animStyle animation style, default is set to ANIM_AUTO
	 */
	public void setAnimStyle(int animStyle) {
		this.animStyle = animStyle;
	}

	/**
	 * Add action item
	 * 
	 * @param action  {@link ActionItem} object
	 */
	public void addActionItem(ActionItem action) {
		actionList.add(action); 
	}
	
	/**
	 * Show popup window. Popup is automatically positioned, on top or bottom of anchor view.
	 * 
	 */
	public void show () {
		preShow();
//		showDropDown();
//		showDropDown(xOffset, yOffset);
		int xPos, yPos;
		int[] location = new int[2];
	
		anchor.getLocationOnScreen(location);
		Rect anchorRect	= new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());

		mTrack.removeAllViews();
		createActionList();
		
		root.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	
		int rootHeight 		= root.getMeasuredHeight();
		int rootWidth		= root.getMeasuredWidth();
		int screenWidth 	= windowManager.getDefaultDisplay().getWidth();
		int screenHeight	= windowManager.getDefaultDisplay().getHeight();
		int spanheight      = screenHeight/2;
		int defaultYPos = 70;
		
		if(screenWidth < screenHeight){
			int dyTop			= anchorRect.top;
			int dyBottom		= screenHeight - anchorRect.bottom;
			boolean onTop = (location[1] > spanheight) ? true: false;
//			boolean onTop		= (dyTop > dyBottom) ? true : false;
			if ((anchorRect.left + rootWidth) > screenWidth) {
				xPos = anchorRect.left - (rootWidth-anchor.getWidth());
			} else {
				if (anchor.getWidth() > rootWidth) {
					xPos = anchorRect.centerX() - (rootWidth/2);
				} else {
					xPos = anchorRect.left;
				}
			}
			
			if (onTop) {
				LayoutParams l 	= scroller.getLayoutParams();
				
				if (rootHeight > dyTop) {
					yPos = defaultYPos;
					l.height = 12 * (int)anchor.getResources().getDisplayMetrics().density + screenHeight - yPos - anchor.getHeight() - anchor.getResources().getDrawable(R.drawable.cmcc_launcher_arrow_down).getIntrinsicHeight();// dyTop - anchor.getHeight();
				} else {
					//yPos = anchorRect.top - rootHeight + 70 * (int)anchor.getResources().getDisplayMetrics().density;
					yPos = screenHeight - rootHeight - anchor.getHeight();
					if(yPos < defaultYPos){
						yPos = defaultYPos;
						l.height = 12 * (int)anchor.getResources().getDisplayMetrics().density + screenHeight - yPos - anchor.getHeight() - anchor.getResources().getDrawable(R.drawable.cmcc_launcher_arrow_down).getIntrinsicHeight();// dyTop - anchor.getHeight();
					}else{
						yPos += 12 * (int)anchor.getResources().getDisplayMetrics().density;
					}
				}
			} else {
				yPos = anchorRect.bottom;
				if (rootHeight > dyBottom) { 
					LayoutParams l 	= scroller.getLayoutParams();
					l.height		= dyBottom;
				}
			}
			showArrow(((onTop) ? R.id.arrow_up : R.id.arrow_down), anchorRect.centerX()-xPos);
			
			//Log.d(TAG, "show  dyTop:"+dyTop+" dyBottom:"+dyBottom+" yPos:"+yPos+" rootHeight:"+rootHeight+" anchor.getHeight():"+anchor.getHeight());
			
			setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
		}else{
			int dyLeft	= anchorRect.left;
			int dyRight	= screenWidth - anchorRect.right;
			boolean onLeft	= (dyLeft > dyRight) ? true : false;
			yPos = 40* (int)anchor.getResources().getDisplayMetrics().density;
			LayoutParams l 	= scroller.getLayoutParams();
			
			if(rootHeight > (screenHeight - anchorRect.top)){
				l.height = screenHeight - anchorRect.top - 15 * (int)anchor.getResources().getDisplayMetrics().density;
			}
			
		   xPos = screenWidth - rootWidth - anchorRect.width();
		   
//		   Log.d(TAG, "show yPos:"+yPos+"  xPos:"+xPos+" rootWidth:"+rootWidth+" scroller.getWidth():"+scroller.getWidth()+"  mArrowUp.getWidth():"+mArrowUp.getWidth()+" bottom:"+anchorRect.bottom);
		   final View showArrow = onLeft ? mArrowUp : mArrowDown;
	       final View hideArrow = onLeft ? mArrowDown : mArrowUp;
//           final int arrowHeight = mArrowUp.getMeasuredHeight();
           
           showArrow.setVisibility(View.VISIBLE);
	       ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();
	       param.topMargin = 12 * (int)anchor.getResources().getDisplayMetrics().density;
   	       hideArrow.setVisibility(View.INVISIBLE);
		   setAnimationStyle(screenWidth, anchorRect.centerX(), onLeft);
		}
		
		quickcontact.startAnimation(mTrackAnim);
		
		if(location[1] > spanheight)
		{
			defaultYPos = (int) (location[1] - context.getResources().getDimension(R.dimen.quick_qiupu_show_top));
		}
		else
		{
			defaultYPos = (int) (location[1] + context.getResources().getDimension(R.dimen.quick_qiupu_show_down));
		}
		
		window.showAtLocation(anchor, Gravity.NO_GRAVITY, location[0], defaultYPos);
	}
	
	/**
	 * Set animation style
	 * 
	 * @param screenWidth screen width
	 * @param requestedX distance from left edge
	 * @param onTop flag to indicate where the popup should be displayed. Set TRUE if displayed on top of anchor view
	 * 		  and vice versa
	 */
	private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
		int arrowPos = requestedX - mArrowUp.getMeasuredWidth()/2;

		switch (animStyle) {
		case ANIM_GROW_FROM_LEFT:
			window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
			break;
					
		case ANIM_GROW_FROM_RIGHT:
			window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
			break;
					
		case ANIM_GROW_FROM_CENTER:
			window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
		break;
			
		case ANIM_REFLECT:
			window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Reflect : R.style.Animations_PopDownMenu_Reflect);
		break;
		
		case ANIM_AUTO:
			if (arrowPos <= screenWidth/4) {
				window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
			} else if (arrowPos > screenWidth/4 && arrowPos < 3 * (screenWidth/4)) {
				window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
			} else {
				window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
			}
			break;
		}
	}
	
	/**
	 * Create action list
	 */
	private void createActionList() {
		View view;
		String title;
		Bitmap icon;
		OnClickListener listener;
	
		for (int i = 0; i < actionList.size(); i++) {
			title 		= actionList.get(i).getTitle();
			icon 		= actionList.get(i).getIcon();
			listener	= actionList.get(i).getListener();
	
			view 		= getActionItem(title, icon, listener,i);
		
			view.setFocusable(true);
			view.setClickable(true);
			
			mTrack.addView(view);
		}
	}
	
	/**
	 * Get action item {@link View}
	 * 
	 * @param title action item title
	 * @param icon {@link Drawable} action item icon
	 * @param listener {@link View.OnClickListener} action item listener
	 * @return action item {@link View}
	 */
	private View getActionItem(String title, Bitmap icon, OnClickListener listener,int lastitem) {
		RelativeLayout container	= (RelativeLayout) inflater.inflate(R.layout.action_item, null);
		
		ImageView img			= (ImageView) container.findViewById(R.id.icon);
//		TextView text			= (TextView) container.findViewById(R.id.title);
		ImageView span_icon     = (ImageView)container.findViewById(R.id.span_icon);
		if(lastitem == actionList.size()-1)
		{
			span_icon.setVisibility(View.GONE);
		}
		else
		{
			span_icon.setVisibility(View.VISIBLE);
		}
		if (icon != null) {
			img.setImageBitmap(icon);
		}
		
		if (title != null) {			
//			text.setText(title);
		}
		
		if (listener != null) {
			container.setOnClickListener(listener);
		}
		
		return container;
	}
	
	/**
	 * Show arrow
	 * 
	 * @param whichArrow arrow type resource id
	 * @param requestedX distance from left screen
	 */
	private void showArrow(int whichArrow, int requestedX) {
        final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

        final int arrowWidth = mArrowUp.getMeasuredWidth();

        showArrow.setVisibility(View.VISIBLE);
        
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();
        
//        Log.d(TAG, "showArrow  param topMargin:"+param.topMargin+" height:"+param.height);
       
        param.leftMargin = requestedX - arrowWidth / 2;
        
//        hideArrow.setVisibility(View.INVISIBLE);
    }
}