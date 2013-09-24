package com.borqs.common.model;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;

public class MainItemInfo implements Comparable<MainItemInfo>{
	public Drawable mIcon;
	public String mLabel;
    public ComponentName mComponent;//for plug in
    public ItemCallBack mCallBack;  //for user defined
    public boolean iamrequest;
        
    public interface ItemCallBack
    {
    	public void CallBack();
    }

	
	public MainItemInfo(Drawable ic, String label){
		mIcon = ic;
		mLabel = label;
        mComponent = null;
	}


	@Override
	public int compareTo(MainItemInfo another) {		
		return mLabel.compareTo(another.mLabel);
	}
}
