package com.borqs.qiupu.ui.circle.quickAction;

import java.util.HashMap;

import twitter4j.UserCircle;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.PopupWindow.OnDismissListener;

import com.borqs.common.util.IntentUtil;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.circle.EditPublicCircleActivity;
import com.borqs.qiupu.ui.page.CreateCircleMainActivity;
import com.borqs.qiupu.util.CircleUtils;

/**
 * QuickAction dialog, shows action list as icon and text like the one in Gallery3D app. Currently supports vertical 
 * and horizontal layout.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 * 
 * Contributors:
 * - Kevin Peck <kevinwpeck@gmail.com>
 */
public class BottomMoreQuickAction extends PopupWindows implements OnDismissListener {
	
	private static final String TAG = "BottomMoreQuickAction";
	private View mRootView;
	private LayoutInflater mInflater;
	private OnDismissListener mDismissListener;

	private boolean mDidAction;
	private boolean mIsShow;

    private int mAnimStyle;
    private int rootWidth=0;
    
    public static final int ANIM_GROW_FROM_LEFT = 1;
	public static final int ANIM_GROW_FROM_RIGHT = 2;
	public static final int ANIM_GROW_FROM_CENTER = 3;
	public static final int ANIM_REFLECT = 4;
	public static final int ANIM_AUTO = 5;
	public static final int ANIM_SLIDING = 6;
	
	private boolean mOnTop;
	private int mTitleNtfType;
	
	private Animation mRoateTopAnimation;
	private Animation mRoateBottomAnimation;
	private BaseAdapter mAdapter;
	private int mWidth;
	
	private UserCircle mCircle;

    /**
     * Constructor for default vertical layout
     * 
     * @param context  Context
     */
    public BottomMoreQuickAction(Context context) {
        this(context, 0, null);
    }

    public BottomMoreQuickAction(Context context, int width, UserCircle circle) {
        super(context);
        mWidth = width;
        mCircle = circle;
        mInflater 	 = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//        if (mOrientation == HORIZONTAL) {
//            setRootViewId(R.layout.ntf_popup_horizontal);
//        } else {
            setRootViewId(R.layout.bottom_more_popup);
//        }

        mAnimStyle 	= ANIM_SLIDING;
//        mChildPos 	= 0;
        
        mRoateTopAnimation = AnimationUtils.loadAnimation(mContext, R.anim.rotate_tip_arrowup);
    	mRoateTopAnimation.setFillAfter(true);
    	
    	mRoateBottomAnimation = AnimationUtils.loadAnimation(mContext, R.anim.rotate_tip); 
    	mRoateBottomAnimation.setFillAfter(true);
    	
    }

	/**
	 * Set root view.
	 * 
	 * @param id Layout resource id
	 */
	public void setRootViewId(int id) {
		mRootView	= (ViewGroup) mInflater.inflate(id, null);
//		if(mWidth > 0) {
//			mRootView.setLayoutParams(new LayoutParams(mWidth, LayoutParams.WRAP_CONTENT));
//		}else {
			mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//		}

		View moreEvent = mRootView.findViewById(R.id.toggle_event);
		View morePoll =  mRootView.findViewById(R.id.toggle_poll);
		View moreCircle = mRootView.findViewById(R.id.toggle_circle);
		setContentView(mRootView);
		moreEvent.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mCircle != null) {
					HashMap<String, String> receiverMap = new HashMap<String, String>();
					String receiverid = "#" +  mCircle.circleid;
					receiverMap.put(String.valueOf(mCircle.circleid), mCircle.name);
					long sceneId = -1;
					if(mCircle.mGroup != null) {
						sceneId = mCircle.mGroup.parent_id;
					}
					if(sceneId <= 0) {
						sceneId = mCircle.circleid;
					}
					
					IntentUtil.gotoCreateEventActivity(mContext,  EditPublicCircleActivity.type_create_event, receiverMap, receiverid, mCircle.circleid, sceneId);
				}else {
					Log.d(TAG, "mCircle is null");
				}
				
				dismiss();
			}
		});
		
		morePoll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mCircle != null) {
					HashMap<String, String> receiverMap = new HashMap<String, String>();
					String receiverid = "#" +  mCircle.circleid;
					receiverMap.put(String.valueOf(mCircle.circleid), mCircle.name);
					
					long sceneId = -1;
					if(mCircle.mGroup != null) {
						sceneId = mCircle.mGroup.parent_id;
					}
					if(sceneId <= 0) {
						sceneId = mCircle.circleid;
					}
					
					IntentUtil.startCreatePollActivity(mContext, receiverMap, receiverid, mCircle.circleid, sceneId);
				}else {
					Log.d(TAG, "mCircle is null ");
				}
				
				dismiss();
			}
		});
		
		moreCircle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, CreateCircleMainActivity.class);
	            intent.putExtra(UserCircle.PARENT_ID, mCircle.circleid);
	            if(mCircle != null && mCircle.mGroup != null) {
	            	intent.putExtra(CreateCircleMainActivity.SUBTYPE, mCircle.mGroup.subtype);
	            	long sceneId = -1;
	            	sceneId = mCircle.mGroup.parent_id;
	            	
					if(sceneId <= 0) {
						sceneId = mCircle.circleid;
					}
	            	intent.putExtra(CircleUtils.INTENT_SCENE, sceneId);
	            }
	            
	            mContext.startActivity(intent);
	            dismiss();
			}
		});
		
		if(mCircle != null && mCircle.mGroup != null && mCircle.mGroup.parent_id <= 0 && mCircle.mGroup.formal == UserCircle.circle_top_formal ) {
			moreCircle.setVisibility(View.VISIBLE);
		}else {
			moreCircle.setVisibility(View.GONE);
		}
	}
	

	/**
	 * Set animation style
	 * 
	 * @param mAnimStyle animation style, default is set to ANIM_AUTO
	 */
	public void setAnimStyle(int mAnimStyle) {
		this.mAnimStyle = mAnimStyle;
	}
	
	public void show(View anchor) {
		show(anchor, null);
	}

	/**
	 * Show quickaction popup. Popup is automatically positioned, on top or bottom of anchor view.
	 * 
	 */
	public void show (View anchor, View anchorIcon) {
		preShow(mWidth);

		int xPos, yPos, arrowPos;

		mIsShow = true;
		mDidAction 			= false;

		int[] location 		= new int[2];

		anchor.getLocationOnScreen(location);

		Rect anchorRect 	= new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] 
		                	+ anchor.getHeight());

		Log.d(TAG, "location[0]: " + location[0] + " location[1]: " + location[1] + " anchor.getWidth(): " + anchor.getWidth());
		//mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		int rootHeight 		= mRootView.getMeasuredHeight();

		if (rootWidth == 0) {
			rootWidth		= mRootView.getMeasuredWidth();
		}

		int screenWidth 	= mWindowManager.getDefaultDisplay().getWidth();
		int screenHeight	= mWindowManager.getDefaultDisplay().getHeight();

		
		//automatically get X coord of popup (top left)
		if ((anchorRect.left + rootWidth) > screenWidth) {
			xPos 		= anchorRect.left /*- (rootWidth-anchor.getWidth())*/;			
			xPos 		= (xPos < 0) ? 0 : xPos;

			arrowPos 	= anchorRect.centerX()-xPos;

		} else {
			if (anchor.getWidth() > rootWidth) {
				xPos = anchorRect.centerX() - (rootWidth/2);
			} else {
				xPos = anchorRect.left;
			}

			arrowPos = anchorRect.centerX()-xPos;
		}

		int dyTop			= anchorRect.top;
		int dyBottom		= screenHeight - anchorRect.bottom;

		mOnTop		= (dyTop > dyBottom) ? true : false;

		Log.d(TAG, "show: " + mOnTop);
		
		if(anchorIcon != null) {
			if(mOnTop) {
    	    	anchorIcon.startAnimation(mRoateTopAnimation);
			}else {
    	    	anchorIcon.startAnimation(mRoateBottomAnimation);
			}
		}
		
		if (mOnTop) {
			if (rootHeight > dyTop) {
				yPos 			= 15;
				mRootView.getLayoutParams().height		= dyTop - anchor.getHeight();
			} else {
				yPos = anchorRect.top - rootHeight;
			}
		} else {
			yPos = anchorRect.bottom;

			if (rootHeight > dyBottom) { 
				mRootView.getLayoutParams().height		= dyBottom;
			}
		}
		
//		showArrow(((mOnTop) ? R.id.arrow_down : R.id.arrow_up), arrowPos);

		setAnimationStyle(screenWidth, anchorRect.centerX(), mOnTop);

		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
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
		int arrowPos = requestedX /*- mArrowUp.getMeasuredWidth()/2*/;

		switch (mAnimStyle) {
		case ANIM_GROW_FROM_LEFT:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
			break;

		case ANIM_GROW_FROM_RIGHT:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
			break;

		case ANIM_GROW_FROM_CENTER:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
		break;

		case ANIM_REFLECT:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Reflect : R.style.Animations_PopDownMenu_Reflect);
		break;
		case ANIM_SLIDING:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Sliding : R.style.Animations_PopDownMenu_Sliding);
			break;

		case ANIM_AUTO:
			if (arrowPos <= screenWidth/4) {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
			} else if (arrowPos > screenWidth/4 && arrowPos < 3 * (screenWidth/4)) {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
			} else {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
			}

			break;
		}
	}

	/**
	 * Set listener for window dismissed. This listener will only be fired if the quicakction dialog is dismissed
	 * by clicking outside the dialog or clicking on sticky item.
	 */
	public void setOnDismissListener(BottomMoreQuickAction.OnDismissListener listener) {
		setOnDismissListener(this);

		mDismissListener = listener;
	}

	@Override
	public void onDismiss() {
		mIsShow = false;
		if (!mDidAction && mDismissListener != null) {
			mDismissListener.onDismiss(mOnTop, mTitleNtfType);
		}
	}

	/**
	 * Listener for item click
	 *
	 */
	public interface OnActionItemClickListener {
		public abstract void onItemClick(BottomMoreQuickAction source, int pos, long actionId);
	}

	/**
	 * Listener for window dismiss
	 * 
	 */
	public interface OnDismissListener {
		public abstract void onDismiss(boolean onTop, int ntftype);
	}
	
	public boolean isShow(){
		return mIsShow;
	}
}