package com.borqs.common.adapter;

import twitter4j.AsyncQiupu;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;

import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.BpcShakeExchangeCardFragment;
import com.borqs.qiupu.fragment.FindContactsFragment;
import com.borqs.qiupu.fragment.NearByListFragment;
import com.borqs.qiupu.fragment.SuggestionListFragment;
import com.borqs.qiupu.fragment.TabsAdapter;
import com.borqs.qiupu.fragment.ViewPagerTabButton;
import com.borqs.qiupu.ui.bpc.UsersArrayListActivity;

public class BpcFindFriendsAdapter extends FragmentPagerAdapter implements TabsAdapter {
	private static final String TAG = "BpcFindFriendsAdapter";

	private Context mContext;

    private BpcShakeExchangeCardFragment mShakeFragment;
    private NearByListFragment mNearByFragment;
    private FindContactsFragment mContactUserFragment;
    private SuggestionListFragment mSuggestionFragment;
    private UsersArrayListActivity.UsersArrayListFragment mFansFragment;

	private ViewPagerTabButton mShakingBtn;
	private ViewPagerTabButton mNearByBtn;
    private ViewPagerTabButton mContactBtn;
    private ViewPagerTabButton mSuggestionBtn;
    private ViewPagerTabButton mFansBtn;

    public static final int TAB_SHAKE      = 0;
    public static final int TAB_NEARBY     = 1;
    public static final int TAB_CONTACT    = 2;
    public static final int TAB_SUGGESTION = 3;
    public static final int TAB_FANS       = 4;

    private static final int TAB_COUNT = 5;
    private boolean mIsInitialNearBy = false;

    public BpcFindFriendsAdapter(FragmentManager fm) {
        super(fm);
    }

    public BpcFindFriendsAdapter(FragmentManager fm, Context context,
            long uid, AsyncQiupu asyncQiupu) {
        this(fm);
        mContext = context;

        mShakeFragment = new BpcShakeExchangeCardFragment(asyncQiupu, false);
        mNearByFragment = new NearByListFragment();
        mContactUserFragment = new FindContactsFragment();
        mSuggestionFragment = SuggestionListFragment.newInstance(false);
        mFansFragment = UsersArrayListActivity.UsersArrayListFragment.newInstance(uid);
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem: " + position);
        switch (position) {
            case TAB_SHAKE:
                return mShakeFragment;
            case TAB_NEARBY:
                return mNearByFragment;
            case TAB_CONTACT:
                return mContactUserFragment;
            case TAB_SUGGESTION:
                return mSuggestionFragment;
            case TAB_FANS:
                return mFansFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        /**
         * don't destroy fragment item.
         */
    }

    @Override
    public View getView(int position) {
        switch (position) {
            case TAB_SHAKE:
                mShakingBtn = newPageTabButton(R.string.shaking_find_friends);
                return mShakingBtn;
            case TAB_NEARBY:
                mNearByBtn = newPageTabButton(R.string.near_by_people_tab_title);
                return mNearByBtn;
            case TAB_CONTACT:
                mContactBtn = newPageTabButton(R.string.from_contact_title);
                return mContactBtn;
            case TAB_SUGGESTION:
                mSuggestionBtn = newPageTabButton(R.string.from_suggestion_title);
                return mSuggestionBtn;
            case TAB_FANS:
                mFansBtn = newPageTabButton(R.string.from_fans_title);
                return mFansBtn;
            default:
                return null;
        }
    }

    private ViewPagerTabButton newPageTabButton(int textId) {
        Resources res = mContext.getResources();
        ViewPagerTabButton button = new ViewPagerTabButton(mContext);
        button.setTextColor(res.getColor(R.color.white));
        button.setText(textId);
        return button;
    }

	public void setTabBtnBg(int position) {
	    if(position == TAB_FANS) {
	        mShakeFragment.disableSensor(true);
	        mFansBtn.setBackgroundResource(R.drawable.bottom_btn_pressed);
	        mShakingBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mContactBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mSuggestionBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mNearByBtn.setBackgroundResource(R.drawable.tab_view_bg);
	    }else if(position == TAB_SHAKE) {
	        mShakeFragment.disableSensor(false);
	        mShakingBtn.setBackgroundResource(R.drawable.bottom_btn_pressed);
	        mFansBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mContactBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mSuggestionBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mNearByBtn.setBackgroundResource(R.drawable.tab_view_bg);
	    } else if (position == TAB_CONTACT) {
	        mShakeFragment.disableSensor(true);
            mFansBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mShakingBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mContactBtn.setBackgroundResource(R.drawable.bottom_btn_pressed);
            mSuggestionBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mNearByBtn.setBackgroundResource(R.drawable.tab_view_bg);
        } else if (position == TAB_SUGGESTION) {
            mShakeFragment.disableSensor(true);
            mFansBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mShakingBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mContactBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mSuggestionBtn.setBackgroundResource(R.drawable.bottom_btn_pressed);
            mNearByBtn.setBackgroundResource(R.drawable.tab_view_bg);
        } else if (position == TAB_NEARBY) {
            mShakeFragment.disableSensor(true);
            if (mNearByFragment != null && mIsInitialNearBy == false) {
                mNearByFragment.getNearByPeople();
                mIsInitialNearBy = true;
            } else {
                Log.d(TAG, "=================no need to fetch data from server");
            }
            mFansBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mShakingBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mContactBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mSuggestionBtn.setBackgroundResource(R.drawable.tab_view_bg);
            mNearByBtn.setBackgroundResource(R.drawable.bottom_btn_pressed);
        } else {
            Log.w(TAG, "setTabBtnBg, unexpected position: " + position);
        }
	}

}
