package com.borqs.common.view;

import com.borqs.qiupu.R;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;

public class MenuHorizontalScrollView extends HorizontalScrollView {
    private static final String  TAG   = "MenuHorizontalScrollView";
    private static final boolean debug = true;

    public interface SizeCallBack {
        public void onGlobalLayout();

        public void getViewSize(int idx, int width, int height, int[] dims);
    }

    public interface MenuCallBack {
        public void onMenuSlide(boolean out);
    }

    /* itself */
//    private MenuHorizontalScrollView me;

    /* left menu, here we use ListView */
    private View                     menu;

    /* menu in/out status */
    public static boolean            menuOut       = false;

    /* extend width */
    public static final int          ENLARGE_WIDTH = 80;

    /* menu width */
    private int                      menuWidth;

    /* the beginning coordinate of gesture */
    private float                    lastMotionX   = -1;

    /* top left button */
    private View                     menuBtn;

    /* current position */
    private int                      current;

    private int                      scrollToViewPos;

    MenuCallBack                     menuCallBack;

    public MenuHorizontalScrollView(Context context) {
        super(context);
//        init();
    }

    public MenuHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        init();
    }

    public MenuHorizontalScrollView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
//        init();
    }

    private void init() {
        this.setHorizontalFadingEdgeEnabled(false);
        this.setVerticalFadingEdgeEnabled(false);
//        this.me = this;
//        this.me.setVisibility(View.INVISIBLE);
        setVisibility(View.INVISIBLE);
//        menuOut = false;
    }

    public void initViews(View[] children, SizeCallBack sizeCallBack,
            View menu, MenuCallBack menuCallBack, int width, boolean out) {
        init();
        menuOut = out;

        this.menu = menu;
        this.menuCallBack = menuCallBack;
        ViewGroup parent = (ViewGroup) getChildAt(0);

        parent.removeAllViews();

        for (int i = 0; i < children.length; i++) {
            children[i].setVisibility(View.INVISIBLE);
            parent.addView(children[i]);
        }
        OnGlobalLayoutListener onGlLayoutistener = new MenuOnGlobalLayoutListener(
                parent, children, sizeCallBack, width);
        getViewTreeObserver().addOnGlobalLayoutListener(onGlLayoutistener);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    /**
     * set top left button
     * 
     * @param btn
     */
    public void setMenuBtn(View btn) {
        this.menuBtn = btn;
    }

    public void clickMenuBtn(boolean withAnimation, boolean out) {
        menuOut = out;

        if (debug)
        Log.d(TAG, "clickMenuBtn() menuOut = " + menuOut);

        if (!menuOut) {
            menuWidth = 0;
        } else {
            menuWidth = menu.getMeasuredWidth() - menuBtn.getMeasuredWidth() - ENLARGE_WIDTH;
        }
        menuSlide(withAnimation);
    }

    /**
     * scroll navigation menu to be out/in
     */
    private void menuSlide(boolean withAnimation) {

        if (menuWidth == 0) {
            menuOut = true;
//            me.setClickable(false);
            setClickable(false);
        } else {
            menuOut = false;
//            me.setClickable(true);
            setClickable(true);
        }
        menuCallBack.onMenuSlide(menuOut);

        if (debug)
        Log.d(TAG, "menuSlide() menuOut = " + menuOut + " , menuWidth = " + menuWidth);

        if (withAnimation) {
            smoothScrollTo(menuWidth, 0);
        } else {
            scrollTo(this.menuWidth, 0);
        }

//        if (menuWidth == 0) {
//            menu.setVisibility(View.INVISIBLE);
//        }

        Log.d(TAG, ">>>>> menuOut = " + menuOut);
        // me.scrollTo(this.menuWidth, 0);
        // if(menuOut == true)
        // this.menuBtn.setBackgroundResource(R.drawable.menu_fold);
        // else
        // this.menuBtn.setBackgroundResource(R.drawable.menu_unfold);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (l < (menu.getMeasuredWidth() - menuBtn.getMeasuredWidth() - ENLARGE_WIDTH) / 2) {
            menuWidth = 0;
        } else {
            menuWidth = menu.getWidth() - menuBtn.getMeasuredWidth() - ENLARGE_WIDTH;
        }
        current = l;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int x = (int) ev.getRawX();

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            lastMotionX = (int) ev.getRawX();
        }

        if ((current == 0 && x < scrollToViewPos)
                || (current == scrollToViewPos * 2 && x > ENLARGE_WIDTH)) {
            return false;
        }

//        if (debug)
//        Log.d(TAG, "onTouchEvent() menuOut = " + menuOut + ", lastMotionX = " + lastMotionX);

        if (menuOut == false && lastMotionX > 160) {
            return true;
        } else {
            if (ev.getAction() == MotionEvent.ACTION_UP) {
                menuSlide(true);
                return false;
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        int width = getMeasuredWidth();
//        int height = getMeasuredHeight();
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            if (width < height) {
//                int tmp = width;
//                width = height;
//                height = tmp;
//            }
//            Log.d(TAG, ">>>> land scape <<<<");
//        } else {
//            Log.d(TAG, ">>>> port scape <<<<");
//        }
//        Log.d(TAG, ">>>>>>>>> onConfigurationChanged() width = " + width + ", height = " + height);
    }

    public class MenuOnGlobalLayoutListener implements OnGlobalLayoutListener {

        private ViewGroup    parent;
        private View[]       children;
        private SizeCallBack sizeCallBack;
        private int width;

        
        public MenuOnGlobalLayoutListener(ViewGroup parent, View[] children,
                SizeCallBack sizeCallBack, int width) {

            this.parent = parent;
            this.children = children;
            this.sizeCallBack = sizeCallBack;
            this.width = width;
        }

        @Override
        public void onGlobalLayout() {
            getViewTreeObserver().removeGlobalOnLayoutListener(this);
            this.sizeCallBack.onGlobalLayout();
            this.parent.removeViewsInLayout(0, children.length);
            int width = getMeasuredWidth();
            int height = getMeasuredHeight();
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Log.d(TAG, ">>>> land scape <<<< width = " + width + ", height = " + height);
                if (width < height) {
                    int tmp = width;
                    width = /*height*/this.width;
                    height = tmp;
                }
            } else {
                Log.d(TAG, ">>>> port scape <<<< width = " + width + ", height = " + height);
                if (width > height) {
                    int tmp = width;
                    width = /*height*/this.width;
                    height = tmp;
                }
            }

            Log.d(TAG, ">>>>>>>>> onGlobalLayout() width = " + width 
                    + ", height = " + height + ", length = " + children.length);

            int[] dims = new int[2];
            scrollToViewPos = 0;

            for (int i = 0; i < children.length; i++) {
                this.sizeCallBack.getViewSize(i, width, height, dims);
                children[i].setVisibility(View.VISIBLE);
                parent.addView(children[i], dims[0], dims[1]);
                if (i == 0) {
                    scrollToViewPos += dims[0];
                }
            }

            Log.d(TAG, ">>>>>>>>> scrollToViewPos = " + scrollToViewPos);
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    scrollBy(scrollToViewPos, 0);

                    setVisibility(View.VISIBLE);
                    menu.setVisibility(View.VISIBLE);
                    menu.setBackgroundResource(R.drawable.navigation_bg);
                }
            });
        }
    }

}
