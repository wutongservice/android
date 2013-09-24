package com.borqs.qiupu.fragment;

import java.util.HashMap;

import twitter4j.UserCircle;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.GroupPrivacySetview;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.PickCircleUserActivity;

public class EditGroupBaseSetFragment extends EditGroupBaseFragment {
	private final static String TAG = "EditGroupBaseSetFragment";
	
	private EditText mMember_limit;
	private GroupPrivacySetview mPrivacySetView;
	
	private int mPrivacy = -1;
	private int mApproveSet = -1;
//	private int mJoinPermission  = -1;
	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View contentView = inflater.inflate(R.layout.public_circle_base_set, container, false);
		mMember_limit  = (EditText)contentView.findViewById(R.id.member_limit);
		mPrivacySetView = (GroupPrivacySetview) contentView.findViewById(R.id.privacy_view);
		return contentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		initUI();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public interface EditPublicCircleFragmentCallBack {
		public void getEditPublicCircleFragment(EditGroupBaseSetFragment fragment);
		public UserCircle getCircleInfo() ;
	}

	private void initUI() {
		if(mCircle != null && mCircle.mGroup != null) {
		    String limit_str = String.valueOf(mCircle.mGroup.member_limit);
		    mMember_limit.setText(limit_str);
		    mMember_limit.setSelection(limit_str.length());
		    
		    mPrivacySetView.setContent(mCircle);
		    if(UserCircle.isPrivacyOpen(mCircle.mGroup)) {
		        mPrivacy = UserCircle.PRIVACY_OPEN;
		    }else if(UserCircle.isPrivacyClosed(mCircle.mGroup)) { 
		        mPrivacy = UserCircle.PRIVACY_CLOSED;
		    }else if(UserCircle.isPrivacySecret(mCircle.mGroup)) {
		        mPrivacy = UserCircle.PRIVACY_SECRET;
		    }
		    
		    if(UserCircle.canApproveInvite(mCircle.mGroup)) {
		        mApproveSet = UserCircle.APPROVE_MEMBER;
		    }else {
		        mApproveSet = UserCircle.APPROVE_MAMANGER;
		    }
		}
	}
	
	View.OnClickListener invitePeopleClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			gotoPickCircleUserActivity(PickCircleUserActivity.type_add_friends);
		}
	};
	
	private void gotoPickCircleUserActivity(int type) {
    	Intent intent = new Intent(mActivity, PickCircleUserActivity.class);
        intent.putExtra(PickCircleUserActivity.RECEIVER_TYPE, type);
        intent.putExtra(IntentUtil.EXTRA_KEY_CIRCLE_ID, String.valueOf(QiupuConfig.CIRCLE_ID_ALL));
        startActivityForResult(intent, BasicActivity.userselectcode);
    }
	
	@Override
	public HashMap<String, String> getEditGroupMap() {
	    return getBaseSetEditGroupMap();
	}
	
    private HashMap<String, String> getBaseSetEditGroupMap() {
		String memberCount = mMember_limit.getText().toString().trim();
		int member_limit = 100;
		if(memberCount.length() > 0 && TextUtils.isDigitsOnly(memberCount)) {
			member_limit = Integer.parseInt(memberCount);
		}else {
			Log.d(TAG, "input member_limit is Invalid ");
		}
//		
		int privacy = mPrivacySetView.getPrivacy();
        int approve = mPrivacySetView.getApprove();
		int can_join = mPrivacySetView.getJoinPermission();
		int inviteper = mPrivacySetView.getInvitePermission();
		
		mCopyCircle = mCircle.clone();
		UserCircle.setPrivacyValue(privacy, mCopyCircle.mGroup);
		UserCircle.setApproveValue(approve, mCopyCircle.mGroup);
		mCopyCircle.mGroup.member_limit = member_limit;
		mCopyCircle.mGroup.can_join = can_join;
		mCopyCircle.mGroup.need_invite_confirm = inviteper;
		
		HashMap<String , String> editMap = new HashMap<String, String>();
		if(!(member_limit == mCircle.mGroup.member_limit)) {
			editMap.put("member_limit", String.valueOf(member_limit));
		}
		
		if(privacy != mPrivacy) {
		    editMap.put("privacy", String.valueOf(privacy));
		}
		
		if(approve != mApproveSet) {
		    editMap.put("can_member_approve", String.valueOf(approve));
		    editMap.put("can_member_invite", String.valueOf(approve));
		}
		
		if(can_join != mCircle.mGroup.can_join) {
			editMap.put("can_join", String.valueOf(can_join));
		}
		if(inviteper != mCircle.mGroup.need_invite_confirm) {
			editMap.put("need_invited_confirm", String.valueOf(inviteper));
		}
		return editMap;
	}
}
