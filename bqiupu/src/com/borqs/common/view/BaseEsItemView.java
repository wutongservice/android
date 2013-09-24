package com.borqs.common.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.borqs.qiupu.R;

public abstract class BaseEsItemView extends SNSItemView {
    final static String TAG = "Qiupu.BaseEsItemView";

    protected final static int TOUCH_STATE_REST = 0;
    protected final static int TOUCH_STATE_SCROLLING = 1;
    protected int mTouchState = TOUCH_STATE_REST;
    protected float mDownMotionX;
    protected float mLastMotionX;
    protected float mLastMotionY;
    protected static int mTouchSlop = -1;

    CheckForLongPress mPendingCheckForLongPress;
    boolean mHasPerformedLongPress = false;

    public BaseEsItemView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public BaseEsItemView(Context context) {
        super(context);
    }

    private void checkForLongClick(int delayOffset) {
        if (true) {
            mHasPerformedLongPress = false;

            if (mPendingCheckForLongPress == null) {
                mPendingCheckForLongPress = new CheckForLongPress();
            }

            postDelayed(mPendingCheckForLongPress, 200 - delayOffset);
        }
    }

    class CheckForLongPress implements Runnable {
        public void run() {
            if (mHasPerformedLongPress == false) {
                setBackgroundColor(mContext.getResources().getColor(R.color.stream_row_view_check_longpress_bg));

                postDelayed(new Runnable() {
                    public void run() {
                        {
                            if (mHasPerformedLongPress == false) {
                                ignoreLongPress();
                                Log.d(TAG, "CheckForLongPress  performLongClick");
                                performLongClick();
                            }

                            setBackgroundColor(Color.TRANSPARENT);
                        }
                    }
                }, 700);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        //Log.d(TAG,"onTouchEvent event="+ev.getAction());

        if (mTouchSlop == -1) {
            final ViewConfiguration configuration = ViewConfiguration.get(getContext());
            mTouchSlop = configuration.getScaledTouchSlop();
        }

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //EnfoldmentView.foldEnfoldmentView();
                // Remember where the motion event started
                mLastMotionX = x;
                mLastMotionY = y;
                mDownMotionX = x;

                cancelLongPress();
                checkForLongClick(100);
                //Log.d(TAG,"onTouchEvent ACTION_DOWN, mLastMotionX:"+mLastMotionX);
                break;
            case MotionEvent.ACTION_MOVE:
                //final float xx = ev.getX(pointerIndex);
                final float xx = x;
                if (mTouchState != TOUCH_STATE_SCROLLING) {
                    final int xDiff = (int) Math.abs(xx - mLastMotionX);
                    final int yDiff = (int) Math.abs(y - mLastMotionY);
                    int moveSlop = mTouchSlop;


                    boolean xMoved = xDiff > moveSlop;
//        	    if(yDiff > (0.577f * xDiff)){
//        			xMoved = false;
//
//        			if(yDiff > this.getHeight()/4)
//        			{
//        			    //change to other category
//        				mLastMotionY = y;
//        				mLastMotionX = xx;
//        				mDownMotionX = xx;
//        			}
//        		}

                    if (xMoved) {
                        mTouchState = TOUCH_STATE_SCROLLING;
                    }
                }

                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    // Scroll to follow the motion event
                    ignoreLongPress();
                }
                break;
            case MotionEvent.ACTION_UP:
                int tmpState = mTouchState;
                mTouchState = TOUCH_STATE_REST;

                if (tmpState == TOUCH_STATE_REST && mHasPerformedLongPress == false) {
                    setBackgroundColor(mContext.getResources().getColor(R.color.stream_row_view_check_longpress_bg));
                    postDelayed(new Runnable() {
                        public void run() {
                            {
                                setBackgroundColor(Color.TRANSPARENT);
                                performClick();
                            }
                        }
                    }, 100);

                    ignoreLongPress();
                }

                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                ignoreLongPress();
                break;
        }
        return true;
    }

    private void ignoreLongPress() {
        mHasPerformedLongPress = true;
        cancelLongPress();

        Log.d(TAG, "ignoreLongPress cancelLongPress");
    }

}
