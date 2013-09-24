package com.borqs.common.view;

import android.widget.TextView;

public interface LeftNavigationCallBack {	
	public void onResume();
	public void onPause();
	public void onDestroy();
    public void setNotificationTextView(TextView findViewById);
    public void openLeftUI();
    public void onCreate();
}
