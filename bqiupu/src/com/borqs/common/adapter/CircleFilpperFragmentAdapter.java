package com.borqs.common.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;

import com.borqs.qiupu.fragment.PublicCircleMainFragment;
import com.borqs.qiupu.fragment.StreamRightFlipperFragment;

public class CircleFilpperFragmentAdapter extends FragmentPagerAdapter/* implements TabsAdapter*/{
	private static final String TAG = "CircleDetailFragmentAdapter";
	private Context mContext;
	private StreamRightFlipperFragment mRightFragment;
	private PublicCircleMainFragment mCircleMainFragment;
	private static final int TAB_OTHER_INFO = 1;
	private static final int TAB_MAIN_INFO = 0;
	private static int FRAGMENT_COUNT = 2;
	
	public CircleFilpperFragmentAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public CircleFilpperFragmentAdapter(FragmentManager fm, Context context){
		this(fm);
		mContext = context;
		mRightFragment = new StreamRightFlipperFragment();
		mCircleMainFragment = new PublicCircleMainFragment();
	}

	@Override
	public Fragment getItem(int position) {
		Log.d(TAG, "getItem: " + position);
		if(position == TAB_OTHER_INFO){
		    return mRightFragment;
		}else {
			return mCircleMainFragment;
		}
	}

	@Override
	public int getCount() {
        return FRAGMENT_COUNT;
	}
	
	@Override
	public void destroyItem(View container, int position, Object object) {
		Log.d(TAG, "here is no need to destory item ");
	}

//	@Override
//	public View getView(int position) {
//		Resources res = mContext.getResources();
//		ViewPagerTabButton btn = new ViewPagerTabButton(mContext);
//		btn.setTextColor(res.getColor(R.color.white));
//		btn.setTextSize(14);
//		if(position == TAB_PAGE){
//			mPagesBtn = new ViewPagerTabButton(mContext);
//			mPagesBtn.setTextColor(res.getColor(R.color.white));
////		    mFriendsBtn.setTextSize(14);
//			mPagesBtn.setText(R.string.page_label);
//			return mPagesBtn;
//		}else {
//		    mCirclesBtn = new ViewPagerTabButton(mContext);
//		    mCirclesBtn.setTextColor(res.getColor(R.color.white));
////		    mCirclesBtn.setTextSize(14);
//		    mCirclesBtn.setText(R.string.user_circles);
//			return mCirclesBtn;
//		}
//	}
	
//	public void setTabBtnBg(int position) {
//        if (getCount() > 1) {
//            if (position == TAB_PAGE) {
//            	mPagesBtn.setBackgroundResource(R.drawable.bottom_btn_pressed);
//                mCirclesBtn.setBackgroundResource(R.drawable.tab_view_bg);
//            } else if (position == TAB_CIRCLE) {
//                mCirclesBtn.setBackgroundResource(R.drawable.bottom_btn_pressed);
//                mPagesBtn.setBackgroundResource(R.drawable.tab_view_bg);
//            }
//        }
//	}
}