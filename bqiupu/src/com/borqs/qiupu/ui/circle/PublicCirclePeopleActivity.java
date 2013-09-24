package com.borqs.qiupu.ui.circle;

import com.borqs.qiupu.ui.BasicNavigationActivity;
import twitter4j.PublicCircleRequestUser;
import twitter4j.UserCircle;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.borqs.common.api.BpcApiUtils;
import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.PublicRequestPeopleFragment;
import com.borqs.qiupu.fragment.PublicRequestPeopleFragment.RequestPeopleCallBackListener;

public class PublicCirclePeopleActivity extends BasicNavigationActivity implements RequestPeopleCallBackListener{

	private final static String TAG = "PublicCirclePeopleActivity";
	public final static String STATUS = "STATUS";
	public final static String CIRCLEINFO = "CIRCLEINFO";

	private FragmentManager mFragmentManager;
	private PublicRequestPeopleFragment mPublicRequestPeopleFragment;
//	private long mCircleId;
	private UserCircle mCircle;
	private int mStatus;
	private View mTitleSelectView;
	private TextView mSelectCount;
	
	private final static String PEOPLE_FRINEDS_TAG = "PEOPLE_FRINEDS_TAG";
	private final static String PEOPLE_REQUEST_TAG = "PEOPLE_REQUEST_TAG";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.public_circle_people_ui);
        parseActivityIntent(getIntent());
        mFragmentManager = getSupportFragmentManager();
        showRightActionBtn(false);
        showLeftActionBtn(true);
        mTitleSelectView = findViewById(R.id.title_select_toast);
        mSelectCount = (TextView) findViewById(R.id.select_count);
        findViewById(R.id.cab_done).setOnClickListener(this);
        initUI();
        
    }

    private void parseActivityIntent(Intent intent) {
        String url = getIntentURL(intent);
        if (TextUtils.isEmpty(url)) {
            mCircle = (UserCircle) intent.getSerializableExtra(CIRCLEINFO);
            mStatus = intent.getIntExtra(STATUS, -1);
            setHeadTitle(intent.getStringExtra("title"));
        }else {
            final long circleId = Long.parseLong(BpcApiUtils.parseSchemeValue(intent,
                    BpcApiUtils.SEARHC_KEY_CIRCLEID));
            //TODO will 
            mStatus = PublicCircleRequestUser.STATUS_APPLY;
            mCircle = orm.queryOneCircleWithGroup(circleId);
            if (mCircle == null){
                mCircle = new UserCircle();
                mCircle.circleid =circleId;
            }
        }
    }
    
    private void initUI() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        mPublicRequestPeopleFragment = new PublicRequestPeopleFragment();
        ft.add(R.id.fragment_content, mPublicRequestPeopleFragment, PEOPLE_REQUEST_TAG);
        ft.commit();
	}
    
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.cab_done) {
            if(mPublicRequestPeopleFragment != null) {
                mTitleSelectView.setVisibility(View.GONE);
                mPublicRequestPeopleFragment.doneSelect();
            }
        }
        super.onClick(view);
    }
    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
	protected void loadSearch()  {
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

//	@Override
//	public void setLeftMenuPosition() {
//		mPosition = LeftMenuMapping.getPositionForActivity(this);
//	}
	
	private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            }
        }
    }
	
	@Override
	protected void loadRefresh() {
	    if(mPublicRequestPeopleFragment != null) {
	        mPublicRequestPeopleFragment.loadRefresh();
	    }
	}

	@Override
	public UserCircle getCircle() {
		return mCircle;
	}

    @Override
    public void getPublicRequestPeopleFragment(
            PublicRequestPeopleFragment fragment) {
        mPublicRequestPeopleFragment = fragment;
    }

    @Override
    public int getStatus() {
        return mStatus;
    }

    @Override
    public void changeTitleSelectUI(int size) {
        if(size > 0) {
            mTitleSelectView.setVisibility(View.VISIBLE);
            mSelectCount.setText(String.format(getString(R.string.title_select_count), size));
        }else {
            mTitleSelectView.setVisibility(View.GONE);
        }
        
    }
}
