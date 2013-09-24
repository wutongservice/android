package com.borqs.qiupu.ui.bpc;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;

import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.PickContactBaseFragment;
import com.borqs.qiupu.fragment.PickContactBaseFragment.PickContactBaseFragmentCallBack;
import com.borqs.qiupu.fragment.PickContactEmailFragment;
import com.borqs.qiupu.fragment.PickContactPhoneFragment;
import com.borqs.qiupu.ui.BasicActivity;

public class InvitePeopleFragmentActivity extends BasicActivity implements PickContactBaseFragmentCallBack {
	private static final String TAG = "InvitePeopleFragmentActivity";
	private PickContactBaseFragment mPickContactFragment;
	
	private int mType;
	public static final String TYPE_TAG = "TYPE_TAG";
	public static final int TYPE_PHONE_INVITE = 0;
    public static final int TYPE_EMAIL_INVITE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invite_people_fragment_view);

//		Button select_ok = (Button) this.findViewById(R.id.select_ok);
//		Button select_cancel = (Button) this.findViewById(R.id.select_cancel);
//
//		select_ok.setOnClickListener(doSelectClick);
//		select_cancel.setOnClickListener(doCancel);

		showLeftActionBtn(false);
		showRightActionBtn(false);
		showRightTextActionBtn(true);
		overrideRightTextActionBtn(R.string.qiupu_invite, inviteClickListener);
		
		mType = getIntent().getIntExtra(TYPE_TAG, TYPE_PHONE_INVITE);
		FragmentManager frag = getSupportFragmentManager();
		
		if(TYPE_PHONE_INVITE == mType) {
            setHeadTitle(R.string.invite_contact_with_phone);
            mPickContactFragment = new PickContactPhoneFragment();
            frag.beginTransaction().add(R.id.request_container, mPickContactFragment).commit();
        }else if(TYPE_EMAIL_INVITE == mType) {
            setHeadTitle(R.string.invite_contact_with_email);
            mPickContactFragment = new PickContactEmailFragment();
            frag.beginTransaction().add(R.id.request_container, mPickContactFragment).commit();
        }else {
            Log.d(TAG, "have no this type");
        }

	}

	@Override
	protected void createHandler() {
	    mHandler = new Handler();
	}


//	View.OnClickListener doSelectClick = new View.OnClickListener() {
//		
//		public void onClick(View arg0) {
//		    StringBuilder idsBuilder = new StringBuilder();
//		    StringBuilder namesBuilder = new StringBuilder();
//		    
//			if(mPickContactFragment != null) {
//			    idsBuilder.append(mPickContactFragment.getselectValue());
//			    namesBuilder.append(mPickContactFragment.getSelectName());
//			}
//			
//			setSelectResult(idsBuilder.toString(), namesBuilder.toString());
//		}
//	};
	
//	private void setSelectResult(String toUsers, String toUsersName) {
//        if(QiupuConfig.LOGD)Log.d(TAG, "setSelectResult, toUsers: " + toUsers + " toUsersName: " + toUsersName);
//        Intent data = new Intent();
//        data.putExtra("toUsers", toUsers);
//        data.putExtra("toUsersName", toUsersName);
//        setResult(Activity.RESULT_OK, data);
//        finish();
//    }
	
//	View.OnClickListener doCancel = new View.OnClickListener() {
//		
//		public void onClick(View arg0){
//			InvitePeopleFragmentActivity.this.setResult(Activity.RESULT_CANCELED);
//			InvitePeopleFragmentActivity.this.finish();
//		}
//	};
	
    @Override
    protected void loadRefresh() {
        super.loadRefresh();
        
    }

    @Override
    public void getPickContactBaseFragment(PickContactBaseFragment fragment) {
        mPickContactFragment = fragment;
    }
    
    View.OnClickListener inviteClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(mPickContactFragment != null) {
                mPickContactFragment.invitePeople();
            }            
        }
    };
}
