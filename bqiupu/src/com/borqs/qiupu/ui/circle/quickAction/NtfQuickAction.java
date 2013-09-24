package com.borqs.qiupu.ui.circle.quickAction;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.borqs.common.view.CustomListView;
import com.borqs.information.InformationDownloadService;
import com.borqs.information.db.Notification;
import com.borqs.information.util.InformationConstant;
import com.borqs.information.util.InformationUtils;
import com.borqs.qiupu.R;
import com.borqs.qiupu.service.QiupuService;

/**
 * QuickAction dialog, shows action list as icon and text like the one in Gallery3D app. Currently supports vertical 
 * and horizontal layout.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 * 
 * Contributors:
 * - Kevin Peck <kevinwpeck@gmail.com>
 */
public class NtfQuickAction extends PopupWindows implements OnDismissListener {
	
	private static final String TAG = "NtfQuickAction";
	private View mRootView;
	private ImageView mArrowUp;
	private ImageView mArrowDown;
	private LayoutInflater mInflater;
	private CustomListView mListView;
//	private ViewGroup mTrack;
//	private ScrollView mScroller;
	private TextView mPopup_title;
	private OnActionItemClickListener mItemClickListener;
	private OnDismissListener mDismissListener;

	private List<ActionItem> actionItems = new ArrayList<ActionItem>();

	private boolean mDidAction;
	private boolean mIsShow;

//	private int mChildPos;
//    private int mInsertPos;
    private int mAnimStyle;
    private int mOrientation;
    private int rootWidth=0;
    
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    
    public static final int ANIM_GROW_FROM_LEFT = 1;
	public static final int ANIM_GROW_FROM_RIGHT = 2;
	public static final int ANIM_GROW_FROM_CENTER = 3;
	public static final int ANIM_REFLECT = 4;
	public static final int ANIM_AUTO = 5;
	
	private boolean mOnTop;
	private int mTitleNtfType;
	
	private Animation mRoateTopAnimation;
	private Animation mRoateBottomAnimation;
	private BaseAdapter mAdapter;
	private Context mContext;

    /**
     * Constructor for default vertical layout
     * 
     * @param context  Context
     */
    public NtfQuickAction(Context context) {
        this(context, VERTICAL);
    }

    /**
     * Constructor allowing orientation override
     * 
     * @param context    Context
     * @param orientation Layout orientation, can be vartical or horizontal
     */
    public NtfQuickAction(Context context, int orientation) {
    	this(context, orientation, -1);
    	
    }
    
    public NtfQuickAction(Context context, int orientation, int ntfType) {
        super(context);
        mContext = context;
        
        mOrientation = orientation;
        mTitleNtfType = ntfType;
        
        mInflater 	 = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (mOrientation == HORIZONTAL) {
            setRootViewId(R.layout.ntf_popup_horizontal);
        } else {
            setRootViewId(R.layout.ntf_popup_vertical);
        }

        mAnimStyle 	= ANIM_AUTO;
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
//		mTrack 		= (ViewGroup) mRootView.findViewById(R.id.tracks);

		mArrowDown 	= (ImageView) mRootView.findViewById(R.id.arrow_down);
		mArrowUp 	= (ImageView) mRootView.findViewById(R.id.arrow_up);

//		mScroller	= (ScrollView) mRootView.findViewById(R.id.scroller);
		mListView   = (CustomListView) mRootView.findViewById(R.id.content);

		mPopup_title = (TextView) mRootView.findViewById(R.id.popup_title);
		//This was previously defined on show() method, moved here to prevent force close that occured
		//when tapping fastly on a view to show quickaction dialog.
		//Thanx to zammbi (github.com/zammbi)
		mRootView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		TextView emptyTextView = (TextView) mRootView.findViewById(R.id.empty_text);
		setContentView(mRootView);
		
		setTextViewValue(emptyTextView);
		mListView.setEmptyView(emptyTextView);
		 mListView.setonRefreshListener(new CustomListView.OnRefreshListener() {
	            public void onRefresh() {
	                onListViewRefresh();
	            }
	            public void onFootShown(boolean show) {
	            }
	        });
		
		setPopupTitle();
	}
	
	private void onListViewRefresh() {
		if(mTitleNtfType == Notification.ntf_type_request) {
			if(QiupuService.mRequestsService != null) {
				QiupuService.mRequestsService.rescheduleRequests(true);
			}else {
				Log.e(TAG, "requestService is null");
			}
		}else if(mTitleNtfType == Notification.ntf_type_tome) {
			Intent intent = new Intent(mContext, InformationDownloadService.class);
			intent.putExtra(InformationConstant.NOTIFICATION_INTENT_SYNC_TYPE, InformationDownloadService.sync_topN_tome);
			intent.putExtra(InformationConstant.NOTIFICATION_INTENT_PARAM_ISTOME, true);
			mContext.startService(intent);
		}else if(mTitleNtfType == Notification.ntf_type_other) {
			Intent intent = new Intent(mContext, InformationDownloadService.class);
			intent.putExtra(InformationConstant.NOTIFICATION_INTENT_SYNC_TYPE, InformationDownloadService.sync_topN_other);
			mContext.startService(intent);
		}else {
			Log.d(TAG, "setPopupTitle: have not this ntf type. " + mTitleNtfType);
		}
	}
	
	private void setTextViewValue(TextView view ) {
		if(mTitleNtfType == Notification.ntf_type_request) {
			view.setText(R.string.have_no_request_info);
		}else if(mTitleNtfType == Notification.ntf_type_tome) {
			view.setText(R.string.notification_none);
		}else if(mTitleNtfType == Notification.ntf_type_other) {
			view.setText(R.string.notification_none);
		}else {
			Log.d(TAG, "setPopupTitle: have not this ntf type. " + mTitleNtfType);
		}
	}

	private void setPopupTitle() {
		if(mTitleNtfType == Notification.ntf_type_request) {
			mPopup_title.setText(R.string.home_requests);
		}else if(mTitleNtfType == Notification.ntf_type_tome) {
			mPopup_title.setText(R.string.ntf_label_tome);
		}else if(mTitleNtfType == Notification.ntf_type_other) {
			mPopup_title.setText(R.string.ntf_label_other);
		}else {
			Log.d(TAG, "setPopupTitle: have not this ntf type. " + mTitleNtfType);
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
	
	public void setListAdapter(BaseAdapter adapter) {
		mAdapter = adapter;
		if(mListView != null) {
			mListView.setAdapter(adapter);
		}else {
			Log.e(TAG, "setListAdapter: listView is null" );
		}
	}
	public void setListItemClickListener(OnItemClickListener listener) {
		if(mListView != null) {
			mListView.setOnItemClickListener(listener);
		}else {
			Log.e(TAG, "setListAdapter: listView is null" );
		}
	}

	public void show(View anchor) {
		show(anchor, null);
	}

	/**
	 * Show quickaction popup. Popup is automatically positioned, on top or bottom of anchor view.
	 * 
	 */
	public void show (View anchor, View anchorIcon) {
		preShow(true);

		int xPos, yPos, arrowPos;

		mIsShow = true;
		mDidAction 			= false;

		int[] location 		= new int[2];

		anchor.getLocationOnScreen(location);

		Rect anchorRect 	= new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] 
		                	+ anchor.getHeight());

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
			xPos 		= anchorRect.left - (rootWidth-anchor.getWidth());			
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
				mListView.getLayoutParams().height		= dyTop - anchor.getHeight();
			} else {
				yPos = anchorRect.top - rootHeight;
			}
		} else {
			yPos = anchorRect.bottom;

			if (rootHeight > dyBottom) { 
				mListView.getLayoutParams().height		= dyBottom;
			}
		}
		
		if(mAdapter != null) {
			ViewGroup.LayoutParams params = mListView.getLayoutParams();
			if (mOrientation == HORIZONTAL) {
				if(mAdapter.getCount() >= 2) {
					params.height = getListViewNewHeight();
				}
			} else {
				if(mAdapter.getCount() >= 4) {
					params.height = getListViewNewHeight();
				}
			}
		}

		showArrow(((mOnTop) ? R.id.arrow_down : R.id.arrow_up), arrowPos);

		setAnimationStyle(screenWidth, anchorRect.centerX(), mOnTop);

		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
	}
	
	private int getListViewNewHeight() {
		WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display d = windowManager.getDefaultDisplay();
        
        Resources res = mContext.getResources();
        int titleBarHeight = (int) res.getDimension(R.dimen.title_bar_height);
        int bottomBarHeight = (int) res.getDimension(R.dimen.bottom_height);
        int bottomSeeAllHeight = (int) res.getDimension(R.dimen.second_title_bar_height);
        int contentHeight = d.getHeight() - titleBarHeight - bottomBarHeight - 120 - bottomSeeAllHeight; 
        return contentHeight;
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
       
        param.leftMargin = requestedX - arrowWidth / 2;
        
        hideArrow.setVisibility(View.INVISIBLE);
    }

	/**
	 * Set listener for window dismissed. This listener will only be fired if the quicakction dialog is dismissed
	 * by clicking outside the dialog or clicking on sticky item.
	 */
	public void setOnDismissListener(NtfQuickAction.OnDismissListener listener) {
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
		public abstract void onItemClick(NtfQuickAction source, int pos, long actionId);
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

	public void OnRefreshComplete() {
		mListView.onRefreshComplete();
	}
}