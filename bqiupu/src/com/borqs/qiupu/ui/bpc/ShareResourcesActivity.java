package com.borqs.qiupu.ui.bpc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.fragment.StreamListFragment;
import com.borqs.qiupu.fragment.StreamListFragment.StreamListFragmentCallBack;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CircleUtils;

public class ShareResourcesActivity extends BasicActivity implements StreamListFragmentCallBack {

    private static final String TAG = "ShareResourcesActivity";
//    private long mUserid;
//    private int mSourceFilter;
    StreamListFragment.MetaData mFragmentData = new StreamListFragment.MetaData();

    private StreamListFragment mStreamListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.share_resource_view);

        Intent intent = getIntent();
        mFragmentData.mUserId = intent.getLongExtra("userid", -1);
        mFragmentData.mCircleId = intent.getLongExtra(CircleUtils.CIRCLE_ID, -1);
        mFragmentData.mSourceFilter = intent.getIntExtra("sourcefilter", -1);
        setHeadTitle(intent.getStringExtra("title"));

        mStreamListFragment = new StreamListFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction tra = manager.beginTransaction();
        tra.add(R.id.contact_people_list, mStreamListFragment);
        tra.commit();
        showRightActionBtn(false);
        showLeftActionBtn(true);
    }

    protected void createHandler() { }

    @Override
    public StreamListFragment.MetaData getFragmentMetaData(int index) {
        return mFragmentData;
    }

    @Override
	public String getSerializeFilePath() {
		return QiupuHelper.getShareSourceCachePath() + mFragmentData.mUserId + mFragmentData.mCircleId + mFragmentData.mSourceFilter;
	}
	
	@Override
	protected void loadRefresh() {
		if(mStreamListFragment != null){
			mStreamListFragment.loadRefresh();
		}
	}
}
