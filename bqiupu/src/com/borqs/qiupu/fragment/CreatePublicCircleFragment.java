package com.borqs.qiupu.fragment;

import java.util.HashMap;

import twitter4j.AsyncQiupu;
import twitter4j.Circletemplate;
import twitter4j.PageInfo;
import twitter4j.PublicCircleRequestUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.UserCircle.Group;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.borqs.account.login.service.ConstData;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.RecipientsAdapter;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.GroupPrivacySetview;
import com.borqs.qiupu.AddressPadMini;
import com.borqs.qiupu.AddressPadMini.PhoneNumberEmailDecorater;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.PageColumns;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.PickAudienceActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class CreatePublicCircleFragment extends BasicFragment {
	private final static String TAG = "CreatePublicCircleFragment";
	
	private Activity mActivity;
	private AsyncQiupu asyncQiupu;
	private QiupuORM orm;
	private Handler mhandler;
	
	private EditText mPublic_circle_name;
	private EditText mPublic_circle_description;
	private EditText mPublic_circle_location;
	private ProgressDialog mprogressDialog;
	private AddressPadMini mShareTo;
	private GroupPrivacySetview mPrivacySetView;
	private Spinner mSubtype_spinner;
	private int mCreate_formal = 0;
	private long mPageId;
	private long mParentId;
	private long mSceneId;
	
	String mSubtype;
	
	private CreatePublicCircleFragmentCallBack mCallBackListener;
	private HashMap<Long, String> mSelectUserCircleMap = new HashMap<Long, String>();
	
//	private long mCircleId;
//	private String mCompanyId;
	private static final int default_member_limit = 1000;
	private static final String RESULT = "result";
	
	private boolean mIsFromRegister;

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		mActivity = activity;
		try {
			mCallBackListener = (CreatePublicCircleFragmentCallBack) mActivity;
			mCallBackListener.getCreatePublicCircleFragment(this);

		} catch (ClassCastException e) {
			Log.d(TAG, activity.toString() + "must implement CallBackAppsSearchFragment");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		mCreate_formal = mActivity.getIntent().getIntExtra(UserCircle.CIRCLE_CREATE_TYPE, 0);
		Intent intent = getActivity().getIntent();
		mPageId = intent.getLongExtra(PageInfo.PAGE_ID, 0);
		mParentId = intent.getLongExtra(UserCircle.PARENT_ID, 0);
		mSceneId = intent.getLongExtra(CircleUtils.INTENT_SCENE, 0);
		mSubtype = intent.getStringExtra("subtype");
		mIsFromRegister = intent.getBooleanExtra(ConstData.REGISTER_RESPONSE_RESULT, false);
		
		parserSavedState(savedInstanceState);
		orm = QiupuORM.getInstance(mActivity);
		asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null,
				null);
		mhandler = new MainHandler();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View contentView = inflater.inflate(R.layout.create_public_circle_ui, container, false);
    	
    	mPublic_circle_name = (EditText) contentView.findViewById(R.id.public_circle_name);
    	mPublic_circle_description = (EditText) contentView.findViewById(R.id.public_circle_description);
    	mPublic_circle_location = (EditText) contentView.findViewById(R.id.public_circle_location);
    	mPrivacySetView = (GroupPrivacySetview) contentView.findViewById(R.id.privacy_view);
    	
    	ImageView mChooseShareUserIV = (ImageView) contentView.findViewById(R.id.choose_share_user);
        mChooseShareUserIV.setOnClickListener(chooseShareUserClickListener);
        
        mShareTo = (AddressPadMini) contentView.findViewById(R.id.receiver_editor);
        mShareTo.setAdapter(new RecipientsAdapter(mActivity));
        mShareTo.setOnDecorateAddressListener(new FBUDecorater());
        
        if(mPageId > 0 || mPageId == UserCircle.CREATE_CIRCLE_DEFAULT_PAGE_ID) {
        	if(mCreate_formal == UserCircle.circle_top_formal) {
        		contentView.findViewById(R.id.sub_type_rl).setVisibility(View.VISIBLE);
        		mPrivacySetView.setVisibility(View.GONE);
        	}else {
        		contentView.findViewById(R.id.sub_type_rl).setVisibility(View.GONE);
//        		mPrivacySetView.setVisibility(View.VISIBLE);
        	}
        }
        // hide privacy set always
        mPrivacySetView.setVisibility(View.GONE);
        
        mSubtype_spinner = (Spinner) contentView.findViewById(R.id.subtype_spinner);
        
        String[] adapterValue = getResources().getStringArray(R.array.page_sub_title);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity,
                R.layout.event_spinner_textview, adapterValue);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSubtype_spinner.setAdapter(adapter);
        mSubtype_spinner.setSelection(0);
        
		return contentView;
	}

	private void parserSavedState(Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            
            mPageId = savedInstanceState.getLong(PageInfo.PAGE_ID);
            mCreate_formal = savedInstanceState.getInt(UserCircle.CIRCLE_CREATE_TYPE);
        }
    }
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
		outState.putLong(PageInfo.PAGE_ID, mPageId);
		outState.putInt(UserCircle.CIRCLE_CREATE_TYPE, mCreate_formal);
        super.onSaveInstanceState(outState);
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		mPublic_circle_name.setText(mCallBackListener.getCircleName());
		mPublic_circle_name.selectAll();
//		mCompanyId = mCallBackListener.getCompanyId();
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

	private void startMainCircle(String circleName, long circleId) {
		if(mActivity == null) {
			return ;
		}
		QiupuORM.addSetting(mActivity, QiupuORM.HOME_ACTIVITY_ID, String.valueOf(circleId));
		UserCircle tmpcircle = new UserCircle();
		tmpcircle.circleid = circleId;
		tmpcircle.name = circleName;
		QiupuApplication.mTopOrganizationId = tmpcircle;
		IntentUtil.gotoOrganisationHome(mActivity, circleName, circleId);
        IntentUtil.loadCircleDirectoryFromServer(mActivity, circleId);
        mActivity.finish();
	}
	
	private static final int CREATE_CIRCLE_END = 1;
	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CREATE_CIRCLE_END: {
                try {
                	mprogressDialog.dismiss();
                	mprogressDialog = null;
                } catch (Exception ne) {
                }
                Bundle msgdata = msg.getData();
                boolean ret = msgdata.getBoolean(RESULT, false);
                if (ret == true) {
                	QiupuHelper.updateCirclesUI();
                	QiupuHelper.updatePageActivityUI(null);
                	ToastUtil.showOperationOk(mActivity, mhandler, true);
                	if(mIsFromRegister) {
                		startMainCircle(msgdata.getString(CircleUtils.CIRCLE_NAME), msgdata.getLong(CircleUtils.CIRCLE_ID));
                	}else {
                		mActivity.setResult(Activity.RESULT_OK);
                		// directly finish will throws  android.view.WindowLeaked exception.
                		// 1. finish activity must after progressdialog .
                		// 2. progressdialog.dismiss() is handler.post(), so finish() should delayed.
                		mhandler.postDelayed(new Runnable() {
                			
                			@Override
                			public void run() {
                				mActivity.finish();
                			}
                		}, 1000);
                	}
                } else {
                    ToastUtil.showOperationFailed(mActivity, mhandler, true);
                }
                break;
            }
			}
		}
	}

	boolean inCreatePublicCircle;
    Object mLockcreatePublicCircle = new Object();
    public void createPublicCircle(final UserCircle tmpCircle, final HashMap<String, String> map) {
        if (inCreatePublicCircle == true) {
        	ToastUtil.showShortToast(mActivity, mhandler, R.string.string_in_processing);
            return;
        }

        synchronized (mLockcreatePublicCircle) {
        	inCreatePublicCircle = true;
        }
        
        showProcessDialog(R.string.create_circle_process, false, true, true);

        asyncQiupu.createPublicCircle(AccountServiceUtils.getSessionID(), map, new TwitterAdapter() {
                    public void createPublicCircle(UserCircle circle) {
                        Log.d(TAG, "finish createCircle=" + circle.circleid + " " + circle.name);
//                        mCircleId = circle.circleid;
                        
                        UserCircle backCircle = new UserCircle();
                        backCircle = tmpCircle.clone();
                        backCircle.circleid = circle.circleid;
                        backCircle.uid = AccountServiceUtils.getBorqsAccountID();
                        backCircle.type = UserCircle.CIRLCE_TYPE_PUBLIC;
                        backCircle.memberCount = 1;  // insert myself to public circle

                        if(backCircle.mGroup == null) {
                        	backCircle.mGroup = new Group();
                        }
                        backCircle.mGroup.member_limit = default_member_limit;
                        backCircle.mGroup.role_in_group = PublicCircleRequestUser.ROLE_TYPE_CREATER;
                        backCircle.mGroup.viewer_can_destroy = true;
                        backCircle.mGroup.viewer_can_grant = true;
                        backCircle.mGroup.viewer_can_remove = true;
                        backCircle.mGroup.viewer_can_update = true;
                        
                        orm.insertCircleInfo(backCircle);
                        if(backCircle.mGroup.parent_id > 0) {
                        	orm.insertOneCircleCircles(backCircle.mGroup.parent_id, backCircle);
                        }
                        orm.insertCircleUserTable(circle.circleid, AccountServiceUtils.getBorqsAccountID());
                        if(circle.joinIds != null) {
                            orm.updateLocalUserCircleInfo(circle.joinIds, backCircle.circleid, backCircle.name);
                        }
                        
                        if(mPageId > 0) {
                        	ContentValues cv = new ContentValues();
                        	if(mCreate_formal == UserCircle.circle_top_formal) {
                        		cv.put(PageColumns.ASSOCIATED_ID, circle.circleid);
                            }else {
                            	cv.put(PageColumns.FREE_CIRCLE_IDS, circle.circleid);
                            }
                        	orm.updatePageInfo(mPageId, cv);
                        }

                        Message msg = mhandler.obtainMessage(CREATE_CIRCLE_END);
                        msg.getData().putBoolean(RESULT, true);
                        msg.getData().putLong(CircleUtils.CIRCLE_ID, backCircle.circleid);
                        msg.getData().putString(CircleUtils.CIRCLE_NAME, backCircle.name);
                        msg.sendToTarget();
                        synchronized (mLockcreatePublicCircle) {
                        	inCreatePublicCircle = false;
                        }
                    }

                    public void onException(TwitterException ex, TwitterMethod method) {
                        synchronized (mLockcreatePublicCircle) {
                        	inCreatePublicCircle = false;
                        }
                        Message msg = mhandler.obtainMessage(CREATE_CIRCLE_END);
                        msg.getData().putBoolean(RESULT, false);
                        msg.sendToTarget();
                    }
                });
    }
    
	public interface CreatePublicCircleFragmentCallBack {
		public void getCreatePublicCircleFragment(CreatePublicCircleFragment fragment);
		public String getCircleName() ;
		public String getCompanyId() ;
	}

    DialogInterface.OnClickListener invitedialogCancel = new DialogInterface.OnClickListener() {
    	
    	@Override
    	public void onClick(DialogInterface dialog, int which) {
    		mActivity.finish();
    	}
    };
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode);
        switch (requestCode) {
            case BasicActivity.userselectcode: {
            	String usersAddress = data.getStringExtra(PickAudienceActivity.RECEIVE_ADDRESS);
                mSelectUserCircleMap.clear();
                HashMap<Long, String> tmpMap = (HashMap<Long, String>) data.getSerializableExtra(PickAudienceActivity.RECEIVE_SELECTUSERCIRCLE_NAME);
                mSelectUserCircleMap.putAll(tmpMap);
            	
//            	String selectUserIds = data.getStringExtra("address");
//                String selectCircleIds = data.getStringExtra("circles");
//                Log.d(TAG, "onActivityResult address: " + selectUserIds + selectCircleIds);
//                StringBuilder addressString = new StringBuilder();
//                    if(StringUtil.isValidString(selectCircleIds)){
//                            addressString.append(selectCircleIds);
//                    }
//                    if(StringUtil.isValidString(selectUserIds)) {
//                    	if(addressString.length() > 0) {
//                    		addressString.append(",");
//                    		addressString.append(selectUserIds);
//                    	}else {
//                    		addressString.append(selectUserIds);
//                    	}
//                    }
                    mSelectNames.delete(0, mSelectNames.length());
                    mShareTo.setAddresses(usersAddress);
                break;
            }
        }
    }
    
    private StringBuilder mSelectNames = new StringBuilder();
 // similar as ShareActivity.java
    private class FBUDecorater implements
            AddressPadMini.OnDecorateAddressListener {

        public String onDecorate(String address) {
            String suid = address.trim();
            try {
                if (suid.contains("#")) {
                    int index = suid.indexOf("#");
                    suid = suid.substring(index + 1, suid.length());
                    UserCircle uc = orm.queryOneCircle(QiupuConfig.USER_ID_ALL, Long.valueOf(suid));
                    if (uc != null) {
                    	String circleName = CircleUtils.getCircleName(mActivity, uc.circleid, uc.name); 
                    	if(mSelectNames.length() > 0) {
                    		mSelectNames.append(",");
                    	}
                    	mSelectNames.append(circleName);
                        return circleName;
                    }
                } else if (suid.contains("*")) {
                    int index = suid.indexOf("*");
                    suid = suid.substring(index + 1, suid.length());
                    PhoneNumberEmailDecorater number = new AddressPadMini(
                            mActivity).new PhoneNumberEmailDecorater();
                    number.setNameString(mSelectNames);
                    return number.onDecorate(suid);
                } else {
                	String username = orm.queryUserName(Long.valueOf(suid));
                	if (username == null) {
//                		if(mReceiveMap != null && mReceiveMap.get(suid) != null) {
//                    		username = mReceiveMap.get(suid);
//                		}else {
                			username = mSelectUserCircleMap.get(Long.parseLong(suid)); 
//                		}
                    }
                	
                	if(mSelectNames.length() > 0) {
                		mSelectNames.append(",");
                	}
                	mSelectNames.append(username);
                	return username;
                }
            } catch (Exception ne) {
            	return null;
            }
            return address;
        }
    }
    
    private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable){
    	if(mprogressDialog != null) {
//    		if(mprogressDialog.isShowing()) {
    			mprogressDialog.dismiss();
//    		}
    		mprogressDialog = null;
    	}
        
    	mprogressDialog = DialogUtils.createProgressDialog(getActivity(), 
    			resId, CanceledOnTouchOutside, Indeterminate, cancelable);
    	mprogressDialog.show();    	
    }
    
    View.OnClickListener chooseShareUserClickListener = new OnClickListener() {
        public void onClick(View arg0) {
        	Intent intent = new Intent(mActivity, PickAudienceActivity.class);
            intent.putExtra(PickAudienceActivity.RECEIVE_ADDRESS, mShareTo.getAddressesArray());
            intent.putExtra(PickAudienceActivity.RECEIVE_SELECTUSERCIRCLE_NAME, mSelectUserCircleMap);
            intent.putExtra(PickAudienceActivity.PICK_FROM, PickAudienceActivity.PICK_FROM_CREATE_CIRCLE);
            intent.putExtra(CircleUtils.INTENT_SCENE, mSceneId);
            intent.putExtra(PickFriendsFragment.FILTER_IDS, String.valueOf(AccountServiceUtils.getBorqsAccountID()));
            startActivityForResult(intent, BasicActivity.userselectcode);
        }
    };
    
    public void createPublicCircle() {
    	final String circleName = mPublic_circle_name.getText().toString().trim();
		if(TextUtils.isEmpty(circleName)) {
			ToastUtil.showShortToast(mActivity, mhandler, R.string.name_isnull_toast);
			mPublic_circle_name.requestFocus();
			return ;
		}
		
		final String des = mPublic_circle_description.getText().toString().trim();
		final String location = mPublic_circle_location.getText().toString().trim();
		
		int privacy ;
		int approve ;
		int joinper ;
		int inviteper ;
		boolean sendEmail = mPrivacySetView.getSendEmailStatus();
		boolean sendSms = mPrivacySetView.getSendSmsStatus();
		
		if((mPageId > 0 || mPageId == UserCircle.CREATE_CIRCLE_DEFAULT_PAGE_ID) && mCreate_formal == UserCircle.circle_top_formal) {
			privacy = UserCircle.PRIVACY_SECRET;
			approve = UserCircle.APPROVE_MAMANGER;
			joinper = UserCircle.JOIN_PREMISSION_VERIFY;
			inviteper = UserCircle.INVITE_PERMISSION_NOT_NEED_CONFIRM;
			sendEmail = true;
        }else {
//        	privacy = mPrivacySetView.getPrivacy();
//        	approve = mPrivacySetView.getApprove();
//        	joinper = mPrivacySetView.getJoinPermission();
//        	inviteper = mPrivacySetView.getInvitePermission();
        	privacy = UserCircle.PRIVACY_OPEN;
			approve = UserCircle.APPROVE_MAMANGER;
			joinper = UserCircle.JOIN_PREMISSION_VERIFY;
			inviteper = UserCircle.INVITE_PERMISSION_NOT_NEED_CONFIRM;
			sendEmail = true;
        }
		
		UserCircle tmpCircle = new UserCircle();
		tmpCircle.name = circleName;
		tmpCircle.description = des;
		tmpCircle.location = location;
		tmpCircle.mGroup = new Group();
		UserCircle.setPrivacyValue(privacy, tmpCircle.mGroup);
		UserCircle.setApproveValue(approve, tmpCircle.mGroup);
		tmpCircle.mGroup.can_join = joinper;
		tmpCircle.mGroup.need_invite_confirm = inviteper;

		HashMap<String, String> map = new HashMap<String, String>();
		//TODO here is only select borqs users
		String selectedIds = mShareTo.getAddresses();

		// because the editText can edit, so need re-set select names.
		mSelectNames.delete(0, mSelectNames.length());
		mShareTo.setAddresses(selectedIds);
		
		if(QiupuConfig.DBLOGD)Log.d(TAG, "selectedIds: " + selectedIds + " selectedNames: " + mSelectNames.toString());
		map.put("name", circleName);
		map.put("description", des);
		map.put("address", StringUtil.createAddressJsonString(location));
		map.put("members", selectedIds);
		map.put("names", mSelectNames.toString());
		map.put("privacy", String.valueOf(privacy));
		map.put("can_member_approve", String.valueOf(approve));
		map.put("can_member_invite", String.valueOf(approve));
		map.put("can_join", String.valueOf(joinper));
		map.put("need_invited_confirm", String.valueOf(inviteper));
		map.put("send_email", String.valueOf(sendEmail));
		map.put("send_sms", String.valueOf(sendSms));
		
//		if(!TextUtils.isEmpty(mCompanyId)) {
//			map.put("company", mCompanyId);
//		}
		if(mPageId > 0) {
			map.put(UserCircle.PAGE_ID, String.valueOf(mPageId));
			tmpCircle.mGroup.pageid = mPageId;
		}
		map.put(UserCircle.FORMAL, String.valueOf(mCreate_formal));
		tmpCircle.mGroup.formal = mCreate_formal;
		
		// save circle subtype to server for category child circle
		if(!TextUtils.isEmpty(mSubtype)) {
			Log.d(TAG, "circle subtype: " + mSubtype);
			tmpCircle.mGroup.subtype = mSubtype;
			map.put(UserCircle.SUBTYPE, mSubtype);
		}
		
		if((mPageId > 0 || (mPageId == UserCircle.CREATE_CIRCLE_DEFAULT_PAGE_ID) && mCreate_formal == UserCircle.circle_top_formal)) {
			if(mSubtype_spinner.getSelectedItemPosition() == 0) {
				map.put(UserCircle.SUBTYPE, Circletemplate.SUBTYPE_TEMPLATEFORMALCOMPANY);
				tmpCircle.mGroup.subtype = Circletemplate.SUBTYPE_TEMPLATEFORMALCOMPANY;
			}else if(mSubtype_spinner.getSelectedItemPosition() == 1){
				map.put(UserCircle.SUBTYPE, Circletemplate.SUBTYPE_TEMPLATEFORMALSCHOOL);
				tmpCircle.mGroup.subtype = Circletemplate.SUBTYPE_TEMPLATEFORMALSCHOOL;
			}else {
				Log.d(TAG, "have no this subtype");
			}
		}else {
			Log.d(TAG, "create type is free, no need set subtype");
		}
		
		if(mParentId > 0) {
			tmpCircle.mGroup.parent_id = mParentId;
			map.put(UserCircle.PARENT_ID, String.valueOf(mParentId));
		}
		createPublicCircle(tmpCircle, map);
    }
}
