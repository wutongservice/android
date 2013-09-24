package com.borqs.qiupu.ui.circle;

import java.util.HashMap;

import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.UserCircle.Group;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.view.AddContactInfoview;
import com.borqs.common.view.EventTimeview;
import com.borqs.common.view.GroupPrivacySetview;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CalendarMappingUtils;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class EditEventActivity extends BasicActivity {

	private static final String TAG = "EditEventActivity";
	
	private UserCircle mCircle;
	private UserCircle mCopyCircle;
	private EditText mPublic_circle_name;
	private EditText mPublic_circle_description;
	private EditText mPublic_circle_location;
	private EventTimeview mTimeView;
	private GroupPrivacySetview mPrivacySetView;
	private AddContactInfoview mContactInfoview;
	private ImageView mCoverView;
	private View mLayout_process;
	private int mPrivacy = -1;
	private int mApproveSet = -1;
	private long mThemeId = -1;
	private String mThemeImage;
	private static final int selectThemeRequestCode = 55555;
	@Override
	protected void createHandler() {
		mHandler = new MainHandler();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_event_ui);
		setHeadTitle(R.string.edit_event_title);
		showRightActionBtn(false);
		showRightTextActionBtn(true);
		overrideRightTextActionBtn(R.string.label_save, editClickListener);
		
		mCircle = (UserCircle) getIntent().getSerializableExtra(CircleUtils.CIRCLEINFO);
		
		mPublic_circle_name = (EditText) findViewById(R.id.public_circle_name);
    	mPublic_circle_description = (EditText) findViewById(R.id.public_circle_description);
    	mPublic_circle_location = (EditText) findViewById(R.id.public_circle_location);
    	mTimeView = (EventTimeview) findViewById(R.id.time_view);
        mPrivacySetView = (GroupPrivacySetview) findViewById(R.id.privacy_view);
        mContactInfoview = (AddContactInfoview) findViewById(R.id.contactinfo_view);
        View cover_rl = findViewById(R.id.cover_rl);
        mCoverView = (ImageView) findViewById(R.id.select_cover);
        mLayout_process = findViewById(R.id.layout_process);
        cover_rl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EditEventActivity.this, EventThemeActivity.class); 
				startActivityForResult(intent, selectThemeRequestCode);
			}
		});
        initUI();
	}
	
	private void initUI() {
		if(mCircle != null) {
			mPublic_circle_name.setText(mCircle.name);
			mPublic_circle_name.selectAll();
			mPublic_circle_description.setText(mCircle.description);
			mPublic_circle_location.setText(mCircle.location);
			mTimeView.setContent(mCircle);
			mPrivacySetView.setContent(mCircle);
			mContactInfoview.setContent(mCircle);
			if(mCircle.mGroup != null) {
				shootImageRunner(mCircle.mGroup.coverUrl, mCoverView);
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
			}else {
				Log.d(TAG, "initUi mCircle.mGroup is null");
			}
		}
	}
	
	View.OnClickListener editClickListener = new View.OnClickListener() {
        public void onClick(View v) {
                HashMap<String, String> tmpMap = getEditEventMap();
                if(tmpMap != null) {
                    if(tmpMap.size() > 0) {
                        tmpMap.put("id", String.valueOf(mCircle.circleid));
                        editPublicCircle(mCopyCircle, tmpMap);
                    }else {
                        EditEventActivity.this.finish();
                    }
            }
        }
    };
    
    private HashMap<String, String> getEditEventMap() {
		final String circleName = mPublic_circle_name.getText().toString().trim();
		if(TextUtils.isEmpty(circleName)) {
			ToastUtil.showShortToast(this, mHandler, R.string.name_isnull_toast);
			mPublic_circle_name.requestFocus();
			return null;
		}
		final String des = mPublic_circle_description.getText().toString().trim();
		final String location =  mPublic_circle_location.getText().toString().trim();
		
		mCopyCircle = mCircle.clone();
		mCopyCircle.name = circleName;
		mCopyCircle.description = des;
		mCopyCircle.location = location;
		
		if(mCopyCircle.mGroup == null) {
		    mCopyCircle.mGroup = new Group();
		}
		int privacy = mPrivacySetView.getPrivacy();
        int approve = mPrivacySetView.getApprove();
		int can_join = mPrivacySetView.getJoinPermission();
		UserCircle.setPrivacyValue(privacy, mCopyCircle.mGroup);
		UserCircle.setApproveValue(approve, mCopyCircle.mGroup);
		mCopyCircle.mGroup.can_join = can_join;
		
		long startMillis = mTimeView.getStartMillis();
		long endMillis = mTimeView.getEndMillis();
		
		mCopyCircle.mGroup.startTime = startMillis;
		mCopyCircle.mGroup.endTime = endMillis;
		mCopyCircle.mGroup.repeat_type = mTimeView.mRepeat_type;
		mCopyCircle.mGroup.reminder_time = mTimeView.mReminderTime;
		if(mThemeId > 0) {
			mCopyCircle.mGroup.coverUrl = mThemeImage;
		}
		
		HashMap<String , String> editMap = new HashMap<String, String>();
		if(!(circleName.equals(mCircle.name))) {
			editMap.put("name", circleName);
		}
		if(!(des.equals(mCircle.description))) {
			editMap.put("description", des);
		}
		if(!(location.equals(mCircle.location))) {
			editMap.put("address", StringUtil.createAddressJsonString(location));
		}
		
		if(privacy != mPrivacy) {
		    editMap.put("privacy", String.valueOf(privacy));
		}
		
		if(approve != mApproveSet) {
		    editMap.put("can_member_approve", String.valueOf(approve));
		    editMap.put("can_member_invite", String.valueOf(approve));
		}
		
		if(!(can_join == mCircle.mGroup.can_join)) {
			editMap.put("can_join", String.valueOf(can_join));
		}
		if(!(startMillis == mCircle.mGroup.startTime)) {
			editMap.put("start_time", String.valueOf(startMillis));
		}
		if(!(endMillis == mCircle.mGroup.endTime)) {
			editMap.put("end_time", String.valueOf(endMillis));
		}
		
		if(mCircle.mGroup.repeat_type != mTimeView.mRepeat_type) {
			editMap.put(UserCircle.REPEAT_TYPE, String.valueOf(mTimeView.mRepeat_type));
		}
		if(mCircle.mGroup.reminder_time != mTimeView.mReminderTime) {
			editMap.put(UserCircle.REMINDER_TIME, String.valueOf(mTimeView.mReminderTime));
		}
		
		if(mThemeImage != null && !mThemeImage.equals(mCircle.mGroup.coverUrl)) {
			editMap.put("theme_id", String.valueOf(mThemeId));
		}
		
		mCopyCircle.mGroup.bulletin_updated_time = System.currentTimeMillis();
		mContactInfoview.getBaseSetEditGroupMap(mCopyCircle, editMap);
		
		return editMap;
	}
    
	private final static int EDIT_PUBLIC_END = 1;
	private final static int LOAD_SUCCESS = 102;
	private final static int LOAD_FAILED = 103;
    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EDIT_PUBLIC_END: {
                try {
                    dismissDialog(DIALOG_SET_USER_PROCESS);
                } catch (Exception ne) {
                }
                boolean ret = msg.getData().getBoolean(RESULT, false);
                if (ret == true) {
                    QiupuHelper.updateActivityUI(null);
                    showOperationSucToast(true);
                    EditEventActivity.this.finish();
                } else {
                    showOperationFailToast("", true);
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
            }
        }
    }
    
    boolean inEditPublicInfo;
    Object mLockEditPublicInfo = new Object();
    public void editPublicCircle(final UserCircle tmpCircle, final HashMap<String, String> map) {
        if (inEditPublicInfo == true) {
            ToastUtil.showShortToast(this, mHandler, R.string.string_in_processing);
            return;
        }
        
        synchronized (mLockEditPublicInfo) {
            inEditPublicInfo = true;
        }
        
        showDialog(DIALOG_SET_USER_PROCESS);
        asyncQiupu.editPulbicCircle(AccountServiceUtils.getSessionID(), map, QiupuConfig.isEventIds(tmpCircle.circleid), new TwitterAdapter() {
            public void editPulbicCircle(boolean suc) {
                Log.d(TAG, "finish editPulbicCircle=" + suc);
                
                if(suc) {
                    orm.updateCircleInfo(tmpCircle);
                    // update event to calendar
                    if(CalendarMappingUtils.checkApkExist(EditEventActivity.this)) {
                    	if(isneedInsertToCalendar(tmpCircle)) {
                    		CalendarMappingUtils.removeCalendarEvent(EditEventActivity.this, orm.getEventCalendarid(tmpCircle.circleid));
                    		CalendarMappingUtils.insertToCalendar(EditEventActivity.this, tmpCircle, orm);
                    	}else {
                    		CalendarMappingUtils.removeCalendarEvent(EditEventActivity.this, orm.getEventCalendarid(tmpCircle.circleid));
                    	}
                    }
                }
                
                Message msg = mHandler.obtainMessage(EDIT_PUBLIC_END);
                msg.getData().putBoolean(RESULT, suc);
                msg.sendToTarget();
                synchronized (mLockEditPublicInfo) {
                    inEditPublicInfo = false;
                }
            }
            
            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockEditPublicInfo) {
                    inEditPublicInfo = false;
                }
                Message msg = mHandler.obtainMessage(EDIT_PUBLIC_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode);
    	if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case selectThemeRequestCode: {
            	mThemeImage = data.getStringExtra(EventThemeActivity.SELECT_THEME_URL);
            	Log.d(TAG, "onActivityResult ; " + mThemeImage);
            	mThemeId = data.getLongExtra(EventThemeActivity.SELECT_THEME_ID, -1);
            	shootImageRunner(mThemeImage, mCoverView);
            	break;
            }
        }
    }
    
    private void shootImageRunner(String photoUrl,ImageView img) {
    	ImageRun imageRun = new ImageRun(mHandler, photoUrl, 0);
    	imageRun.SetOnImageRunListener(new ImageRun.OnImageRunListener() {

			@Override
			public void onLoadingFinished() {
				Message msg = mHandler.obtainMessage(LOAD_SUCCESS);
				msg.sendToTarget();
			}

			@Override
			public void onLoadingFailed() {
				Message msg = mHandler.obtainMessage(LOAD_FAILED);
				msg.sendToTarget();
			}});
    	imageRun.addHostAndPath = true;
		final Resources resources = getResources();
		imageRun.width = resources.getDisplayMetrics().widthPixels;
		imageRun.height = imageRun.width;
		imageRun.need_scale = true;
		imageRun.setImageView(img);
		imageRun.post(null);
	}
    
    private boolean isneedInsertToCalendar(UserCircle circle) {
    	boolean needInsert = false;
    	if(circle.mGroup.endTime == 0) {
    		if(circle.mGroup.repeat_type == CalendarMappingUtils.DOES_NOT_REPEAT) {
    			if(circle.mGroup.startTime > System.currentTimeMillis()) {
    				needInsert = true;
    			}
    		}
    	}else {
    		if(circle.mGroup.endTime > System.currentTimeMillis()) {
    			needInsert =  true;
    		}
    	}
    	return needInsert;
    }
}
