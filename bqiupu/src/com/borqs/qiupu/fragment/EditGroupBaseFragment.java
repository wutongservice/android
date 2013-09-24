package com.borqs.qiupu.fragment;

import java.util.HashMap;

import twitter4j.AsyncQiupu;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.borqs.qiupu.db.QiupuORM;

public class EditGroupBaseFragment extends BasicFragment {
	private final static String TAG = "EditGroupBaseFragment";
	
	protected Activity mActivity;
	protected AsyncQiupu asyncQiupu;
	protected QiupuORM orm;
	protected Handler mhandler;
	protected UserCircle mCircle;
	protected UserCircle mCopyCircle;
	protected static final String RESULT = "result";
	protected static final String ERROR = "error";

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		mActivity = activity;
		try {
		    EditGroupBaseFragmentCallBack listener = (EditGroupBaseFragmentCallBack) mActivity;
			listener.getEditGroupBaseFragment(this);
			mCircle = listener.getCircleInfo();

		} catch (ClassCastException e) {
			Log.d(TAG, activity.toString() + "must implement CallBackAppsSearchFragment");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		orm = QiupuORM.getInstance(mActivity);
		asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null,
				null);
		mhandler = new Handler();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public interface EditGroupBaseFragmentCallBack {
		public void getEditGroupBaseFragment(EditGroupBaseFragment fragment);
		public UserCircle getCircleInfo() ;
	}
	
	public HashMap<String, String> getEditGroupMap() {
	    return null;
	}
	
	public UserCircle getCopyCircle() { 
	    return mCopyCircle;
	}

}
