package com.borqs.common.listener;

import android.view.MotionEvent;

public interface TabFilterListener {
	public void filterMoveAction(MotionEvent ev);
	public void dismissOverlayer();
	public void beginDrag(MotionEvent ev);
}
