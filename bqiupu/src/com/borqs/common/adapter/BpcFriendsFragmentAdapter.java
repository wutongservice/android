package com.borqs.common.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;

import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.AllCirclesListFragment;
import com.borqs.qiupu.fragment.PageListFragment;
import com.borqs.qiupu.fragment.TabsAdapter;
import com.borqs.qiupu.fragment.ViewPagerTabButton;

public class BpcFriendsFragmentAdapter extends FragmentPagerAdapter implements TabsAdapter{
	private static final String TAG = "BpcFriendsFragmentAdapter";
	private Context mContext;
//	private FriendsListFragment mFriendsFragment;
	private PageListFragment mPageListFragment;
	private AllCirclesListFragment mCirclesListFragment;
	private ViewPagerTabButton mPagesBtn;
	private ViewPagerTabButton mCirclesBtn;
	private static final int TAB_PAGE = 1;
	private static final int TAB_CIRCLE = 0;
	
	public BpcFriendsFragmentAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public BpcFriendsFragmentAdapter(FragmentManager fm, Context context){
		this(fm);
		mContext = context;
		mPageListFragment = new PageListFragment();
		mCirclesListFragment = new AllCirclesListFragment();
	}

	@Override
	public Fragment getItem(int position) {
		Log.d(TAG, "getItem: " + position);
		if(position == TAB_PAGE){
		    return mPageListFragment;
		}else {
			return mCirclesListFragment;
		}
	}

	@Override
	public int getCount() {
        return 2;
	}
	
	@Override
	public void destroyItem(View container, int position, Object object) {
		Log.d(TAG, "here is no need to destory item ");
	}

	@Override
	public View getView(int position) {
		Resources res = mContext.getResources();
//		ViewPagerTabButton btn = new ViewPagerTabButton(mContext);
//		btn.setTextColor(res.getColor(R.color.white));
//		btn.setTextSize(14);
		if(position == TAB_PAGE){
			mPagesBtn = new ViewPagerTabButton(mContext);
			mPagesBtn.setTextColor(res.getColor(R.color.white));
//		    mFriendsBtn.setTextSize(14);
			mPagesBtn.setText(R.string.page_label);
			return mPagesBtn;
		}else {
		    mCirclesBtn = new ViewPagerTabButton(mContext);
		    mCirclesBtn.setTextColor(res.getColor(R.color.white));
//		    mCirclesBtn.setTextSize(14);
		    mCirclesBtn.setText(R.string.user_circles);
			return mCirclesBtn;
		}
	}
	
	public void setTabBtnBg(int position) {
        if (getCount() > 1) {
            if (position == TAB_PAGE) {
            	mPagesBtn.setBackgroundResource(R.drawable.bottom_btn_pressed);
                mCirclesBtn.setBackgroundResource(R.drawable.tab_view_bg);
            } else if (position == TAB_CIRCLE) {
                mCirclesBtn.setBackgroundResource(R.drawable.bottom_btn_pressed);
                mPagesBtn.setBackgroundResource(R.drawable.tab_view_bg);
            }
        }
	}
}