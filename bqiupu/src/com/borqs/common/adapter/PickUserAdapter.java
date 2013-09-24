package com.borqs.common.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnScrollChangedListener;

import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.ContactUserFragment;
import com.borqs.qiupu.fragment.LocalUserListFragment;
import com.borqs.qiupu.fragment.TabsAdapter;
import com.borqs.qiupu.fragment.TitleProvider;
import com.borqs.qiupu.fragment.ViewPagerTabButton;

public class PickUserAdapter extends FragmentPagerAdapter implements TabsAdapter{
	private static final String TAG = "PickUserAdapter";
	private final int mCount = 2;
	private Context mContext;
	private LocalUserListFragment localUserFragment;
	private ContactUserFragment contactUserFragment;
	private int currentpos ;
	
	public PickUserAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public PickUserAdapter(FragmentManager fm, Context context){
		this(fm);
		mContext = context;
		localUserFragment = new LocalUserListFragment();
		contactUserFragment = new ContactUserFragment();
	}

	@Override
	public Fragment getItem(int position) {
		Log.d(TAG, "getItem: " + position);
		if(position == 0){
			return localUserFragment;
		}else{
			return contactUserFragment;
		}
	}

	@Override
	public int getCount() {
		return mCount;
	}

//	@Override
//	public String getTitle(int position) {
//		currentpos = position;
//		Resources res = mContext.getResources();
//		if(position == 0){
//			return res.getString(R.string.tab_friends);
//		}else{
//			return res.getString(R.string.attach_people);
//		}
//	}
	
	public void doSearch(String key){
		Log.d(TAG, "currentpos: " + currentpos + "key: " + key);
		if(currentpos == 0){
		}else{
		}
	}

    @Override
    public View getView(int position) {
        Resources res = mContext.getResources();
        ViewPagerTabButton btn = new ViewPagerTabButton(mContext, null, true);
        btn.setTextColor(res.getColor(R.color.white));
        btn.setTextSize(14);
        if(position == 0){
            btn.setText(R.string.tab_friends);
            return btn;
        }else {
            btn.setText(R.string.attach_people);
            return btn;
        }
    }
	
//	public LocalUserListFragment getLocalUserListFragment(){
//		return localUserFragment;
//	}
//	
//	public ContactUserFragment getContactUserFragment(){
//		return contactUserFragment;
//	}
}