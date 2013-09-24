package com.borqs.common.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;

import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.*;

public class UserCircleDetailFragmentAdapter extends FragmentPagerAdapter implements TabsAdapter{
	private static final String TAG = "UserCircleDetailFragmentAdapter";
	private final int mCount = 2;
	private Context mContext;
//	private CirclePeopleListFragment mPeopleFragment;
	private FriendsListFragment mPeopleFragment;
	private StreamListFragment mStreamsFragment;
	private ViewPagerTabButton mPeopleBtn;
	private ViewPagerTabButton mStreamBtn;

	private final static int TAB_PEOPLE = 0;
	private final static int TAB_STREAM = 1;
	
	public UserCircleDetailFragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	public UserCircleDetailFragmentAdapter(FragmentManager fm, Context context){
		this(fm);

		mContext = context;
//		mPeopleFragment = new CirclePeopleListFragment();
		mPeopleFragment = new FriendsListFragment();
        mStreamsFragment = new StreamListFragment();
	}

	@Override
	public Fragment getItem(int position) {
		Log.d(TAG, "getItem: " + position);
		if(position == TAB_PEOPLE){
			return mPeopleFragment;
		}else if(position == TAB_STREAM){
			return mStreamsFragment;
		}else{
			return null;
		}
	}

	@Override
	public int getCount() {
		return mCount;
	}
//
//	@Override
//	public String getTitle(int position) {
//		Resources res = mContext.getResources();
//		if(position == 0){
//			return res.getString(R.string.circle_detail_people);
//		}else{
//            return mStreamsFragment.getFragmentTitle();
//		}
//	}

	public void loadRefresh(int index){
        if (index == TAB_PEOPLE) {
            mPeopleFragment.loadRefresh(false);
        } else if (index == TAB_STREAM) {
            mStreamsFragment.loadRefresh();
        } else {
        }
	}

    public boolean getLoadStatus(int index) {
        if (!mStreamsFragment.getLoadStatus()/* && !mPeopleFragment.getLoadStatus()*/) {
            return true;
        }

        return false;
    }

    @Override
    public View getView(int position) {
        Resources res = mContext.getResources();
        if(position == TAB_PEOPLE){
            mPeopleBtn = new ViewPagerTabButton(mContext);
            mPeopleBtn.setTextColor(res.getColor(R.color.white));
            mPeopleBtn.setText(R.string.circle_detail_people);
            return mPeopleBtn;
        }else {
            mStreamBtn = new ViewPagerTabButton(mContext);
            mStreamBtn.setTextColor(res.getColor(R.color.white));
            mStreamBtn.setText(R.string.circle_detail_post);
            return mStreamBtn;
        }
    }
    
    public void setTabBtnBg(int position) {
        if(TAB_PEOPLE == position) {
            mPeopleBtn.setBackgroundResource(R.drawable.bottom_btn_pressed);
            mStreamBtn.setBackgroundResource(R.drawable.tab_view_bg);
        }else if(TAB_STREAM == position) {
            mPeopleBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mStreamBtn.setBackgroundResource(R.drawable.bottom_btn_pressed);
        }
    }
}