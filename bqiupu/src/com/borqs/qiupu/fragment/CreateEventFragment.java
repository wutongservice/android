package com.borqs.qiupu.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.AsyncQiupu;
import twitter4j.EventTheme;
import twitter4j.PublicCircleRequestUser;
import twitter4j.QiupuSimpleUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.UserCircle.Group;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.RecipientsAdapter;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.view.EventTimeview;
import com.borqs.common.view.GroupPrivacySetview;
import com.borqs.common.view.GroupPrivacySetview.SignChoiceAdapter;
import com.borqs.qiupu.AddressPadMini;
import com.borqs.qiupu.AddressPadMini.PhoneNumberEmailDecorater;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.PickAudienceActivity;
import com.borqs.qiupu.ui.circle.EventThemeActivity;
import com.borqs.qiupu.util.CalendarMappingUtils;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class CreateEventFragment extends BasicFragment {
	private final static String TAG = "CreateEventFragment";
	
	private Activity mActivity;
	private AsyncQiupu asyncQiupu;
	private QiupuORM orm;
	private Handler mhandler;
	
	private EditText mPublic_circle_name;
	private EditText mPublic_circle_description;
	private EditText mPublic_circle_location;
	private ProgressDialog mprogressDialog;
	private AddressPadMini mShareTo;
	private EventTimeview mTimeView;
//	private GroupPrivacySetview mPrivacySetView;
	private View mLayout_process;
	private ImageView mCoverView;
	
	private CreateEventFragmentCallBack mCallBackListener;
	private static final int selectThemeRequestCode = 55555;
	
	private long mCircleId;
	private static final int default_member_limit = 1000;
	private long mThemeId = -1;
	private static final String RESULT = "result";
	private EventTheme mDefaultTheme;
	private String mReceivers;
	private HashMap<String, String> mReceiveMap;
	private HashMap<Long, String> mSelectUserCircleMap = new HashMap<Long, String>();
	
	private long mParentId;
	private long mSceneId;
	
	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		mActivity = activity;
		try {
			mCallBackListener = (CreateEventFragmentCallBack) mActivity;
			mCallBackListener.getCreateEventFragment(this);

		} catch (ClassCastException e) {
			Log.d(TAG, activity.toString() + "must implement CallBackAppsSearchFragment");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		orm = QiupuORM.getInstance(mActivity);
		asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null,
				null);
		mhandler = new MainHandler();
		
		Intent intent = mActivity.getIntent();
		mReceivers = intent.getStringExtra("receiver");
		mReceiveMap =  (HashMap<String, String>) intent.getSerializableExtra("receivermap");
		mParentId = intent.getLongExtra(CircleUtils.INTENT_PARENT_ID, -1);
		mSceneId = intent.getLongExtra(CircleUtils.INTENT_SCENE, -1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View contentView = inflater.inflate(R.layout.create_event_ui, container, false);
    	
    	mPublic_circle_name = (EditText) contentView.findViewById(R.id.public_circle_name);
    	mPublic_circle_description = (EditText) contentView.findViewById(R.id.public_circle_description);
    	mPublic_circle_location = (EditText) contentView.findViewById(R.id.public_circle_location);
    	
    	mTimeView = (EventTimeview) contentView.findViewById(R.id.time_view);
    	
    	ImageView mChooseShareUserIV = (ImageView) contentView.findViewById(R.id.choose_share_user);
        mChooseShareUserIV.setOnClickListener(chooseShareUserClickListener);
        
        mShareTo = (AddressPadMini) contentView.findViewById(R.id.receiver_editor);
        mShareTo.setAdapter(new RecipientsAdapter(mActivity));
        mShareTo.setOnDecorateAddressListener(new FBUDecorater());
        
        if(StringUtil.isValidString(mReceivers)) {
        	mShareTo.setAddresses(mReceivers);
//        	mShareTo.setEnabled(false);
//        	mChooseShareUserIV.setVisibility(View.GONE);
        }else {
        	mShareTo.setEnabled(true);
//        	mChooseShareUserIV.setVisibility(View.VISIBLE);
        }
        
//        mPrivacySetView = (GroupPrivacySetview) contentView.findViewById(R.id.privacy_view);
        mLayout_process = contentView.findViewById(R.id.layout_process);
        mCoverView = (ImageView) contentView.findViewById(R.id.select_cover);
        View cover_rl = contentView.findViewById(R.id.cover_rl);
        cover_rl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mActivity, EventThemeActivity.class); 
				startActivityForResult(intent, selectThemeRequestCode);
			}
		});
		return contentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		long themeId = orm.getDefaultThemeId();
		if(themeId > 0) {
			mDefaultTheme = orm.queryOneTheme(themeId);
			if(mDefaultTheme != null) {
			    mThemeId = themeId;
				shootImageRunner(mDefaultTheme.image_url, mCoverView);
			}else {
				mhandler.obtainMessage(SYNC_DEFAULT_THEME).sendToTarget();
			}
		}else {
			mhandler.obtainMessage(SYNC_DEFAULT_THEME).sendToTarget();
		}
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

	private static final int CREATE_CIRCLE_END = 101;
	private final static int LOAD_SUCCESS = 102;
	private final static int LOAD_FAILED = 103;
	private final static int SYNC_DEFAULT_THEME = 104;
	private final static int SYNC_DEFAULT_THEME_END = 105;
	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CREATE_CIRCLE_END: {
                try {
                	mprogressDialog.dismiss();
                	mprogressDialog = null;
                } catch (Exception ne) {
                }
                boolean ret = msg.getData().getBoolean(RESULT, false);
                if (ret == true) {
                	QiupuHelper.updateActivityUI(null);
                	ToastUtil.showOperationOk(mActivity, mhandler, true);
                	mActivity.finish();
                } else {
                    ToastUtil.showOperationFailed(mActivity, mhandler, true);
                }
                break;
            }
			case LOAD_SUCCESS : {
				mLayout_process.setVisibility(View.GONE);
				break;
			}
			case LOAD_FAILED: {
				mLayout_process.setVisibility(View.GONE);
				break;
			}
			case SYNC_DEFAULT_THEME: {
				syncDefaultEventInfo(0, 1);//sync first cover to set default cover.
				break;
			}
			case SYNC_DEFAULT_THEME_END: {
				setLayoutProcessVisibility(View.GONE);
				if (msg.getData().getBoolean("refresh")) {
					if (mDefaultTheme != null) {
						shootImageRunner(mDefaultTheme.image_url, mCoverView);
					} 
				}
				else {
					Log.d(TAG, "selected other theme , no nend refesh theme");
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

        asyncQiupu.createEvent(AccountServiceUtils.getSessionID(), map, new TwitterAdapter() {
                    public void createEvent(UserCircle circle) {
                        Log.d(TAG, "finish createEvent=" + circle.circleid);
                        mCircleId = circle.circleid;
                        
                        UserCircle backCircle = new UserCircle();
                        backCircle = tmpCircle.clone();
                        backCircle.circleid = circle.circleid;
                        backCircle.uid = AccountServiceUtils.getBorqsAccountID();
                        backCircle.type = UserCircle.CIRCLE_TYPE_EVENT;
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
                        if(backCircle.mGroup.creator == null) { 
                        	backCircle.mGroup.creator = new QiupuSimpleUser();
                        }
                        backCircle.mGroup.creator.uid = AccountServiceUtils.getBorqsAccountID();
                        backCircle.mGroup.creator.nick_name = AccountServiceUtils.getBorqsAccount().nickname;
                        backCircle.mGroup.creator.profile_image_url = orm.getUserProfileImageUrl(backCircle.mGroup.creator.uid);                       
                        orm.insertCircleInfo(backCircle);
                        if(backCircle.mGroup.parent_id > 0) {
                        	orm.insertOneCircleEvent(backCircle.mGroup.parent_id, backCircle);
                        }
                        orm.insertCircleUserTable(circle.circleid, AccountServiceUtils.getBorqsAccountID());
                        if(circle.joinIds != null) {
                            orm.updateLocalUserCircleInfo(circle.joinIds, backCircle.circleid, backCircle.name);
                        }
                        
                        // auto export to calendar when create suc.
                        if(CalendarMappingUtils.checkApkExist(mActivity)) {
                        	CalendarMappingUtils.insertToCalendar(mActivity, backCircle, orm);
                        }else {
                        	Log.d(TAG, "calendar is not exist");
                        }

                        Message msg = mhandler.obtainMessage(CREATE_CIRCLE_END);
                        msg.getData().putBoolean(RESULT, true);
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
    
    boolean inLoadingTheme;
    Object mLockSyncEventTheme = new Object();
    public void syncDefaultEventInfo(final int page, final int count) {
        if (!ToastUtil.testValidConnectivity(mActivity)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }
        
    	if (inLoadingTheme == true) {
    		ToastUtil.showShortToast(mActivity, mhandler, R.string.string_in_processing);
    		return;
    	}
    	
    	synchronized (mLockSyncEventTheme) {
    		inLoadingTheme = true;
    	}
    	
    	setLayoutProcessVisibility(View.VISIBLE);
    	asyncQiupu.syncThemes(AccountServiceUtils.getSessionID(), page, count, new TwitterAdapter() {
    		public void syncEventThemes(ArrayList<EventTheme> themes) {
    			Log.d(TAG, "finish syncEventThemes=" + themes.size());
    			
    			boolean isNeedRefreshTheme = false;
    			if (themes.size() > 0) {
    				mDefaultTheme = themes.get(0);
    				orm.setDefaultThemeId(mDefaultTheme.id);
    				orm.insertOneTheme(mDefaultTheme);
    				
    				if(mThemeId == -1) {
    					mThemeId = mDefaultTheme.id;
    					isNeedRefreshTheme = true;
    				}
                }
    			
    			Message msg = mhandler.obtainMessage(SYNC_DEFAULT_THEME_END);
    			msg.getData().putBoolean(RESULT, true);
    			msg.getData().putBoolean("refresh", isNeedRefreshTheme);
    			msg.sendToTarget();
    			synchronized (mLockSyncEventTheme) {
    				inLoadingTheme = false;
    			}
    		}
    		
    		public void onException(TwitterException ex, TwitterMethod method) {
    		    synchronized (mLockSyncEventTheme) {
    		    	inLoadingTheme = false;
    			}
    			Message msg = mhandler.obtainMessage(SYNC_DEFAULT_THEME_END);
    			msg.getData().putBoolean(RESULT, false);
    			msg.sendToTarget();
    		}
    	});
    }
    
	public interface CreateEventFragmentCallBack {
		public void getCreateEventFragment(CreateEventFragment fragment);
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
    	if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case BasicActivity.userselectcode: {
            	String usersAddress = data.getStringExtra(PickAudienceActivity.RECEIVE_ADDRESS);
                mSelectUserCircleMap.clear();
                HashMap<Long, String> tmpMap = (HashMap<Long, String>) data.getSerializableExtra(PickAudienceActivity.RECEIVE_SELECTUSERCIRCLE_NAME);
                mSelectUserCircleMap.putAll(tmpMap);
                
//                HashMap<String, String> tmpPhoneMap = (HashMap<String, String>) data.getSerializableExtra(PickAudienceActivity.RECEIVE_SELECTPHONEEMAIL_NAME);
//                mSelectPhoneEmailNameMap.clear();
//                mSelectPhoneEmailNameMap.putAll(tmpPhoneMap);
                
//                String selectUserIds = data.getStringExtra("address");
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
                // re-set select names.
                    mSelectNames.delete(0, mSelectNames.length());
                    mShareTo.setAddresses(usersAddress);
                    break;
            }
            case selectThemeRequestCode: {
            	String mThemeUrl = data.getStringExtra(EventThemeActivity.SELECT_THEME_URL);
            	mThemeId = data.getLongExtra(EventThemeActivity.SELECT_THEME_ID, -1);
            	Log.d(TAG, "onActivityResult ; " + mThemeUrl);
            	shootImageRunner(mThemeUrl, mCoverView);
            	// dismiss process dialog if the dialog is show.
            	if(mLayout_process != null && mLayout_process.getVisibility() == View.VISIBLE) {
		    		mLayout_process.setVisibility(View.GONE);
		    	}
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
                		if(mReceiveMap != null && mReceiveMap.get(suid) != null) {
                    		username = mReceiveMap.get(suid);
                		}else {
                			username = mSelectUserCircleMap.get(Long.parseLong(suid)); 
                		}
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
    	mprogressDialog = DialogUtils.createProgressDialog(mActivity, 
    			resId, CanceledOnTouchOutside, Indeterminate, cancelable);
    	mprogressDialog.show();    	
    }
    
    View.OnClickListener chooseShareUserClickListener = new OnClickListener() {
        public void onClick(View arg0) {
        	Intent intent = new Intent(mActivity, PickAudienceActivity.class);
            intent.putExtra(PickAudienceActivity.RECEIVE_ADDRESS, mShareTo.getAddressesArray());
            intent.putExtra(PickAudienceActivity.RECEIVE_SELECTUSERCIRCLE_NAME, mSelectUserCircleMap);
            intent.putExtra(PickAudienceActivity.PICK_FROM, PickAudienceActivity.PICK_FROM_EVNET);
            intent.putExtra(CircleUtils.INTENT_SCENE, mSceneId);
            startActivityForResult(intent, BasicActivity.userselectcode);
        }
    };
    
    private void createPublicCircle() {
    	final String circleName = mPublic_circle_name.getText().toString().trim();
		if(TextUtils.isEmpty(circleName)) {
			ToastUtil.showShortToast(mActivity, mhandler, R.string.name_isnull_toast);
			mPublic_circle_name.requestFocus();
			return ;
		}
		
		final String des = mPublic_circle_description.getText().toString().trim();
		final String location = mPublic_circle_location.getText().toString().trim();
		
		UserCircle tmpCircle = new UserCircle();
		tmpCircle.name = circleName;
		tmpCircle.description = des;
		tmpCircle.location = location;
		
		tmpCircle.mGroup = new Group();
		int privacy = mPrivacy;
		int approve = UserCircle.APPROVE_MAMANGER;
		int joinper = UserCircle.JOIN_PREMISSION_VERIFY;
		
		UserCircle.setPrivacyValue(privacy, tmpCircle.mGroup);
        UserCircle.setApproveValue(approve, tmpCircle.mGroup);
        tmpCircle.mGroup.can_join = joinper;
        tmpCircle.mGroup.startTime = mTimeView.mStartMillis;
        tmpCircle.mGroup.endTime = mTimeView.mEndMillis;
        tmpCircle.mGroup.repeat_type = mTimeView.mRepeat_type;
        tmpCircle.mGroup.reminder_time = mTimeView.mReminderTime;
        if(mDefaultTheme != null) {
        	tmpCircle.mGroup.coverUrl = mDefaultTheme.image_url;	
        }
        
		HashMap<String, String> map = new HashMap<String, String>();
		String selectedIds = mShareTo.getAddresses();
		
		// because the editText can edit, so need re-set select names.
		mSelectNames.delete(0, mSelectNames.length());
		mShareTo.setAddresses(selectedIds);
		
		if(QiupuConfig.LOGD)Log.d(TAG, "selectedIds: " + selectedIds + " selectedNames: " + mSelectNames.toString());
		map.put("name", circleName);
		map.put("description", des);
		map.put("address", StringUtil.createAddressJsonString(location));
		map.put("members", mShareTo.getAddresses());
		map.put("names", mSelectNames.toString());
		map.put("privacy", String.valueOf(privacy));
		map.put("can_member_approve", String.valueOf(approve));
		map.put("can_member_invite", String.valueOf(approve));
		map.put("can_join", String.valueOf(joinper));
		map.put("start_time", String.valueOf(mTimeView.mStartMillis));
		map.put("end_time", String.valueOf(mTimeView.mEndMillis));
		map.put(UserCircle.REPEAT_TYPE, String.valueOf(mTimeView.mRepeat_type));
		map.put(UserCircle.REMINDER_TIME, String.valueOf(mTimeView.mReminderTime));
		if(mThemeId > 0) {
			map.put("theme_id", String.valueOf(mThemeId));
		}
		
		if(mParentId > 0) {
			map.put(UserCircle.PARENT_IDS, String.valueOf(mParentId));
		}
		
		createPublicCircle(tmpCircle, map);
    }
    
    private void shootImageRunner(String photoUrl,ImageView img) {
    	ImageRun photo_1 = new ImageRun(mhandler, photoUrl, 0);
		photo_1.SetOnImageRunListener(new ImageRun.OnImageRunListener() {

			@Override
			public void onLoadingFinished() {
				Message msg = mhandler.obtainMessage(LOAD_SUCCESS);
				msg.sendToTarget();
			}

			@Override
			public void onLoadingFailed() {
				Message msg = mhandler.obtainMessage(LOAD_FAILED);
				msg.sendToTarget();
			}});
		photo_1.addHostAndPath = true;
		final Resources resources = mActivity.getResources();
		photo_1.width = resources.getDisplayMetrics().widthPixels;
		photo_1.height = photo_1.width;
		photo_1.need_scale = true;
		photo_1.setImageView(img);
		photo_1.post(null);
	}
    
    private void setLayoutProcessVisibility(int visibility) {
    	if(mLayout_process != null) {
    		mLayout_process.setVisibility(visibility);
    	}
    }
    
    private int mPrivacy;
    public void showPrivacyDialog() {
    	final String circleName = mPublic_circle_name.getText().toString().trim();
		if(TextUtils.isEmpty(circleName)) {
			ToastUtil.showShortToast(mActivity, mhandler, R.string.name_isnull_toast);
			mPublic_circle_name.requestFocus();
			return ;
		}
    	
    	String[] privacySummary = getResources().getStringArray(R.array.privacy_set_summary);
    	String[] privacyItems = getResources().getStringArray(R.array.privacy_set_title);
        int checkedItem = 0;
        SignChoiceAdapter adapter = new GroupPrivacySetview(mActivity).new SignChoiceAdapter(mActivity);
//        SignChoiceAdapter adapter = new GroupPrivacySetview.SignChoiceAdapter(mActivity);
        adapter.alterStringArray(privacyItems, privacySummary);
        DialogUtils.showSingleChoiceDialogWithAdapter(mActivity, R.string.privacy_set_title, adapter, checkedItem, privacyItemClickListener, positiveListener, negativeListener);
    }
    
    DialogInterface.OnClickListener privacyItemClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(which == 0) {
			    mPrivacy = UserCircle.PRIVACY_OPEN;
			}else if(which == 1) {
			    mPrivacy = UserCircle.PRIVACY_CLOSED;
			}else if(which == 2) {
			    mPrivacy = UserCircle.PRIVACY_SECRET;
			}else {
			    Log.d(TAG, "have no checked ");
			}
		}
	};
	
	DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			createPublicCircle();
		}
	};
	DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
		}
	};
}
