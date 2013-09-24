package com.borqs.common.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.fragment.StreamListFragment;
import com.borqs.qiupu.fragment.TitleProvider;

public class StreamListFragmentAdapter extends FragmentPagerAdapter implements TitleProvider{
	private static final String TAG = "StreamListFragmentAdapter";
	private Context mContext;
	private ArrayList<StreamListFragment> mStreamsFragments = new ArrayList<StreamListFragment>();
	private HashMap<Integer, PageData> mStreamsMap = new HashMap<Integer, PageData>();
	private FragmentManager mFragmentManager;
	private FragmentTransaction mCurTransaction;
	private boolean newlayout = false;

    public static class PageData {
        long mUserId;
        long mCircleId;
        String mFragmentTitle;
        PageData() {
            mUserId = QiupuConfig.USER_ID_ALL;
            mCircleId = QiupuConfig.CIRCLE_ID_ALL;
            mFragmentTitle = "";
        }
        public PageData(long userId, long circleId, String title) {
            mUserId = userId;
            mCircleId = circleId;
            mFragmentTitle = title;
        }

        public long getCircleId() {
            return mCircleId;
        }
        public String getFragmentTitle() {
            return mFragmentTitle;
        }
        public long getUserId() {
            return mUserId;
        }
    }

    public StreamListFragmentAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public void useNewUI(boolean newui)
	{
		newlayout = newui;
	}

	public StreamListFragmentAdapter(FragmentManager fm, Context context, ArrayList<PageData> pageDataList){
		this(fm);

		mContext = context;
		mFragmentManager = fm;
//		StreamListFragment fragment;
        for (int i = 0; i < pageDataList.size(); ++i) {
//            fragment = new StreamListFragment();
//            fragment.setInitialIndex(i);
//            mStreamsFragments.add(fragment);
            mStreamsMap.put(i, pageDataList.get(i));
        }
	}

	@Override
	public Fragment getItem(int position) {
		Log.d(TAG, "getItem: " + position);
        return mStreamsFragments.get(position);
	}

	@Override
	public int getCount() {
//		return mStreamsFragments.size();
	    return mStreamsMap.size();
	}

	@Override
	public String getTitle(int position) {
	    return mStreamsMap.get(position).getFragmentTitle();
//		return mStreamsFragments.get(position).getFragmentTitle();
	}

	public void loadRefresh(int index){
//		if(mStreamsFragments.size() > 0) { 
//			mStreamsFragments.get(index).loadRefresh();
//		}
	}

    public boolean getLoadStatus(int index) {
//        if (!mStreamsFragments.get(index).getLoadStatus()) {
//            return true;
//        }

        return false;
    }
    
    private String getCircleId(int position) {
        return String.valueOf(mStreamsMap.get(position).getCircleId());
    }
    private String makeFragmentName(String circleId, int index) {
        return "android:switcher:" + circleId + ":" + index;
    }
    
    @Override
    public Object instantiateItem(View container, int position) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        // Do we already have this fragment?
        String name = makeFragmentName(getCircleId(position), position);
        Log.d(TAG, "fragment.getitle(): " + name);
        
        StreamListFragment fragment = (StreamListFragment) mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
//            if (QiupuConfig.LOGD) Log.v(TAG, "Attaching item #" + position + ": f=" + fragment);
//            mCurTransaction.attach(fragment);
            ViewPager a = (ViewPager) container;
            if(a.getChildAt(position) != null) {
//                a.removeViewAt(position);
//                a.addView(fragment.getView(), position);
            }
            
        } else {
            fragment = new StreamListFragment();
            fragment.setInitialIndex(position);
            mStreamsFragments.add(fragment);
            ViewPager a = (ViewPager) container;
            if(a.getChildAt(position) != null) {
                a.removeViewAt(position);
            }
                
//            fragment = getItem(position);
            if (QiupuConfig.LOGD) Log.v(TAG, "Adding item #" + position + ": f=" + fragment);
            mCurTransaction.add(container.getId(), fragment, name);
        }
        return fragment;
    }
    
    @Override
    public void destroyItem(View container, int position, Object object) {
//        if (mCurTransaction == null) {
//            mCurTransaction = mFragmentManager.beginTransaction();
//        }
//        if (QiupuConfig.LOGD) Log.v(TAG, "Detaching item #" + position + ": f=" + object
//                + " v=" + ((Fragment)object).getView());
//        mCurTransaction.detach((Fragment)object);
    }
    @Override
    public void finishUpdate(View container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }
    
    public void reloadFragment(ArrayList<PageData> pageData) {
//    	StreamListFragment fragment;
    	
    	mStreamsFragments.clear(); 
    	mStreamsMap.clear();
    	for (int i = 0; i < pageData.size(); ++i) {
//            fragment = new StreamListFragment();
//            fragment.setInitialIndex(i);
//            mStreamsFragments.add(fragment);
            mStreamsMap.put(i, pageData.get(i));
        }
    	
    	notifyDataSetChanged();
    }
    
    @Override
    public int getItemPosition(Object object) {
        // TODO Auto-generated method stub
        return POSITION_NONE;
    }
}
