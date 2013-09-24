package com.borqs.common.adapter;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.borqs.qiupu.fragment.PicsFragment;

public class PicsAdapter extends FragmentPagerAdapter {
	private final  ArrayList<String> pics;
	public PicsAdapter(FragmentManager fm,ArrayList<String> pics) {
		super(fm);
		this.pics = pics;
	}

	@Override
	public Fragment getItem(int position) {
		return new PicsFragment(position,pics.get(position));
	}

	@Override
	public int getCount() {
		return pics.size();
	}
	
}