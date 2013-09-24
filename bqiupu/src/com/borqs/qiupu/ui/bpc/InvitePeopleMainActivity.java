package com.borqs.qiupu.ui.bpc;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.InviteSysUsersFragment;
import com.borqs.qiupu.fragment.InviteSysUsersFragment.CallBackInviteSysUsersFragmentListener;
import com.borqs.qiupu.fragment.PickContactBaseFragment;
import com.borqs.qiupu.fragment.PickContactBaseFragment.PickContactBaseFragmentCallBack;
import com.borqs.qiupu.ui.BasicActivity;

public class InvitePeopleMainActivity extends BasicActivity implements PickContactBaseFragmentCallBack, CallBackInviteSysUsersFragmentListener{
	private static final String TAG = "BpcInvitePeopleActivity";
	private PickContactBaseFragment mInviteSysUsersFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invite_people_main);

		setHeadTitle(R.string.invite_people_title);

		showLeftActionBtn(false);
		showRightActionBtn(false);
		showRightTextActionBtn(true);
		overrideRightTextActionBtn(R.string.qiupu_invite, inviteClickListener);
		
		FragmentManager frag = getSupportFragmentManager();
		
		mInviteSysUsersFragment = new InviteSysUsersFragment();
		frag.beginTransaction().add(R.id.request_container, mInviteSysUsersFragment).commit();

	}

	@Override
	protected void createHandler() {
	    mHandler = new Handler();
	}

	View.OnClickListener inviteClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(mInviteSysUsersFragment != null) {
                mInviteSysUsersFragment.invitePeople();
            }
        }
    };
    
    @Override
    public void getPickContactBaseFragment(PickContactBaseFragment fragment) {
        mInviteSysUsersFragment = fragment;
    }

    @Override
    public void getInviteSysUsersFragment(InviteSysUsersFragment fragment) {
        fragment.setType(InviteSysUsersFragment.TYPE_CIRCLE_USER);
    }
}
