package com.borqs.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

public class ExpandGridView extends GridView {

    public ExpandGridView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public ExpandGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
