package com.borqs.qiupu.fragment;

import java.io.File;
import java.io.FileOutputStream;

import twitter4j.AsyncQiupu;
import twitter4j.PublicCircleRequestUser;
import twitter4j.QiupuUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.MyLinkMovementMethod;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.TopPostListFragment.UpdateTitleUICallBack;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.PickAudienceActivity;
import com.borqs.qiupu.ui.bpc.PickAudienceActivity.inviteListeners;
import com.borqs.qiupu.util.CalendarMappingUtils;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.DateUtil;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class EventInfoFragment extends StreamListFragment implements UsersActionListner, inviteListeners,
        UpdateTitleUICallBack {
	private final static String TAG = "EventInfoFragment";
	private Activity mActivity; 
	
	private Handler mHandler;
	private QiupuORM orm;
	private AsyncQiupu asyncQiupu;
	private EventInfoFragmentListenerCallBack mCallBackListener;
	private TextView mEvent_title;
	private ImageView mProfile_img_ui;
	
    private UserCircle mCircle;
    private static final String RESULT = "result";
    private static final String ERRORMSG = "errormsg";
    private ProgressDialog mprogressDialog;
    
    private TextView mInviteBtn;
    private TextView mApplyBtn;
    private View mActionView;
    
    private View mContentView;
    private TextView mPrivacyTextView;
    private View layout_top_post;
    private View span_view;
    private TextView tv_top_post;
    private TextView tv_top_poll;
    
    private File mCurrentPhotoFile;
    private Bitmap photo ;
    private ImageView mCover;
    private TextView mTime;
    public static final int TYPE_TEXT  = 0;
    public static final int PHOTO_PICKED_WITH_DATA = 3021;
    public static final int CAMERA_WITH_DATA = 3023;
    
    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
    	super.onAttach(activity);
    	mActivity = activity;

        if (mActivity instanceof EventInfoFragmentListenerCallBack) {
        	mCallBackListener = (EventInfoFragmentListenerCallBack)activity;
        	mCallBackListener.getEventInfoFragment(this);
        	mCircle = mCallBackListener.getCircleInfo();
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
    	super.onCreate(savedInstanceState);
    	parserSavedState(savedInstanceState);
    	QiupuHelper.registerUserListener(getClass().getName(), this);
    	PickAudienceActivity.registerInviteListener(getClass().getName(), this);
    	orm = QiupuORM.getInstance(mActivity);
		asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
		mHandler = new MainHandler();
		TopPostListFragment.setTopListTitleListener(this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView");
    	View view = super.onCreateView(inflater, container, savedInstanceState);
    	mListView.addHeaderView(initProfileHeadView(inflater));
        return view; 
    }
    
    private View initProfileHeadView(LayoutInflater inflater) {
    	mContentView = inflater.inflate(R.layout.event_info_headview, null, false);
    	mEvent_title = (TextView) mContentView.findViewById(R.id.id_event_title);
    	mEvent_title.setMovementMethod(MyLinkMovementMethod.getInstance());
    	mProfile_img_ui =  (ImageView) mContentView.findViewById(R.id.creator_icon);
    	mCover = (ImageView) mContentView.findViewById(R.id.select_cover);
    	mTime = (TextView) mContentView.findViewById(R.id.id_time);
    	mPrivacyTextView = (TextView) mContentView.findViewById(R.id.id_privacy);
    	
    	mInviteBtn = (TextView) mContentView.findViewById(R.id.invite_btn);
//    	mInviteBtn.setOnClickListener(invitePeopleClickListener);
//    	
    	mApplyBtn = (TextView) mContentView.findViewById(R.id.apply_btn);
//    	mApplyBtn.setOnClickListener(applyPeopleClickListener);
    	
    	mActionView = mContentView.findViewById(R.id.id_action_fl);
    	mActionView.setOnClickListener(actionClickListener);
    	
    	layout_top_post = mContentView.findViewById(R.id.layout_top_post);
    	tv_top_post = (TextView) mContentView.findViewById(R.id.tv_top_post);
    	span_view = mContentView.findViewById(R.id.view_span);
    	tv_top_poll = (TextView) mContentView.findViewById(R.id.tv_top_poll);
        View head_rl = mContentView.findViewById(R.id.head_rl);
        head_rl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				final View bodyInfo = findFragmentViewById(R.id.body_ll);
//				if(bodyInfo.getVisibility() == View.VISIBLE) {
//					bodyInfo.setVisibility(View.GONE);
//				}else {
//					bodyInfo.setVisibility(View.VISIBLE);
//				}
			}
		});
        
        ImageView goto_detail = (ImageView) mContentView.findViewById(R.id.goto_detail);
        goto_detail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCallBackListener != null)
					mCallBackListener.gotoProfileDetail();
			}
		});
        
    	return mContentView;
    }
    
    private void parserSavedState(Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            if(mCircle == null) {
                mCircle = new UserCircle();
            }
            
            mCircle.circleid = savedInstanceState.getLong(CircleUtils.CIRCLE_ID);
            mCircle.uid = savedInstanceState.getLong(CircleUtils.CIRCLE_ID);
        }
    }
    
    private View findFragmentViewById(int id) {
        if(mContentView != null) {
            return mContentView.findViewById(id);
        }
        return null;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        refreshCircleInfoUi();
        mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO).sendToTarget();
    }

    private void refreshCircleInfoUi() {
    	if(mCircle != null && isDetached() == false && getActivity() != null) {
    		mCircle.uid = mCircle.circleid;
    	    refreshHeadUi();
    	    refreshActionBtn();
    	    refreshTopPostAndPoll();
        	if(mCallBackListener != null) {
				mCallBackListener.refreshCircleInfo(mCircle);
			}
		}
    }

    private void refreshTopPostAndPoll() {
        refreshTopPostInfo();
        refreshTopPollInfo();
    }

    private void refreshTopPostInfo() {
        if(mCircle != null && mCircle.mGroup != null && mCircle.mGroup.top_post_count > 0) {
        	layout_top_post.setVisibility(View.VISIBLE);
        	span_view.setVisibility(View.VISIBLE);
            if(TextUtils.isEmpty(mCircle.mGroup.top_post_name)) {
                tv_top_post.setText(R.string.view_top);
            }else {
                tv_top_post.setText(mCircle.mGroup.top_post_name);
            }
            layout_top_post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentUtil.startTopPostIntent(mActivity, mCircle.circleid,mCircle.mGroup.top_post_name, 
                            mCircle.mGroup.viewer_can_update);
                    
                }
            });
        }else {
        	layout_top_post.setVisibility(View.GONE);
        }
    }

    private void refreshTopPollInfo() {
        if(mCircle != null && mCircle.mGroup != null && mCircle.mGroup.top_poll_count > 0) {
            tv_top_poll.setText(R.string.poll);
            tv_top_poll.setVisibility(View.VISIBLE);
            tv_top_poll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentUtil.startPollIntent(mActivity, mCircle.circleid, mCircle.name, 1, true);
                }
            });
        }else {
            tv_top_poll.setVisibility(View.GONE);
        }
    }

    private void refreshActionBtn() {
        if(mCircle.mGroup != null) {
        	if(mCircle.mGroup.role_in_group < 0) {
        		Log.d(TAG, "refreshActionbtn has no role, hide action btn");
        		return;
        	}
            if(PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
            	if(PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group) ||
            			PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group)) {
            		mActionView.setVisibility(View.VISIBLE);
            		mInviteBtn.setVisibility(View.VISIBLE);
                    mApplyBtn.setVisibility(View.GONE);
            	}else {
            		if(UserCircle.VALUE_ALLOWED == mCircle.mGroup.can_member_invite) {
            			mActionView.setVisibility(View.VISIBLE);
                        mInviteBtn.setVisibility(View.VISIBLE);
                        mApplyBtn.setVisibility(View.GONE);
                    }else {
                    	mActionView.setVisibility(View.GONE);
                    	mInviteBtn.setVisibility(View.GONE);
                        mApplyBtn.setVisibility(View.GONE);
                    }	
            	}
            }else {
            	if(UserCircle.JOIN_PERMISSION_DERECT_ADD == mCircle.mGroup.can_join
            			|| UserCircle.JOIN_PREMISSION_VERIFY == mCircle.mGroup.can_join) {
            		mActionView.setVisibility(View.VISIBLE);
            		mApplyBtn.setVisibility(View.VISIBLE);
                    mInviteBtn.setVisibility(View.GONE);
            	}else {
            		mActionView.setVisibility(View.GONE);
            		mApplyBtn.setVisibility(View.GONE);
                    mInviteBtn.setVisibility(View.GONE);
            	}
            }
        }
    }
    
    private void refreshHeadUi() {
        mEvent_title.setText(mCircle.name);
        if(mCircle.mGroup != null) {
			if(mCircle.circleid == 14000000000L) {
				mCover.setImageResource(R.drawable.borqs_cover);
			}else if(StringUtil.isEmpty(mCircle.mGroup.coverUrl)) {
				mCover.setImageResource(R.drawable.event_default_cover);
			}else {
				setViewIcon(mCircle.mGroup.coverUrl, mCover, false);
			}
			StringBuilder timeStr = new StringBuilder();
			timeStr.append(DateUtil.converToRelativeTime(mActivity,
					mCircle.mGroup.startTime));
			if(mCircle.mGroup.endTime > 0) {
				timeStr.append(" ~ ");
				timeStr.append(DateUtil.converToRelativeTime(mActivity,
	    					mCircle.mGroup.endTime));
			}
			mTime.setText(timeStr.toString());
			
			if(UserCircle.isPrivacyOpen(mCircle.mGroup)) {
				mPrivacyTextView.setText(R.string.privacy_set_open);
		    }else if(UserCircle.isPrivacyClosed(mCircle.mGroup)) { 
		    	mPrivacyTextView.setText(R.string.privacy_set_closed);
		    }else if(UserCircle.isPrivacySecret(mCircle.mGroup)) {
		    	mPrivacyTextView.setText(R.string.privacy_set_secret);
		    }
			
			if(mCircle.mGroup.creator != null) {
				setViewIcon(mCircle.mGroup.creator.profile_image_url, mProfile_img_ui, true);
			}
		}
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mCircle != null) {
            outState.putLong(CircleUtils.CIRCLE_ID, mCircle.circleid);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	QiupuHelper.unregisterUserListener(getClass().getName());
    	PickAudienceActivity.unregisterInviteListener(getClass().getName());
    }
    
    private final static int GET_PUBLIC_CIRCLE_INFO = 101;
    private final static int GET_PUBLIC_CIRCLE_INFO_END = 102;
    private final static int EDIT_PROFILE_IMAGE_END = 104;
    private final static int APPLY_END = 105;
	private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case GET_PUBLIC_CIRCLE_INFO: {
            	syncPublicCircleInfo(String.valueOf(mCircle.circleid), false, QiupuConfig.isEventIds(mCircle.circleid));
            	break;
            }
            case GET_PUBLIC_CIRCLE_INFO_END: {
            	end();
            	boolean ret = msg.getData().getBoolean(RESULT, false);
            	if (ret == true) {
            	    refreshCircleInfoUi();
                } else {
                    ToastUtil.showShortToast(mActivity, mHandler, R.string.get_info_failed);
                }
            	break;
            }
            case EDIT_PROFILE_IMAGE_END: {
                try {
                    mprogressDialog.dismiss();
                    mprogressDialog = null;
                } catch (Exception ne) { }
                if(msg.getData().getBoolean(BasicActivity.RESULT, false)) {
                    mCurrentPhotoFile.delete();
                    mProfile_img_ui.setImageBitmap(photo);
                    mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO).sendToTarget(); 
                }else {
                    Log.d(TAG, "edit profile iamge end ");
                    ToastUtil.showShortToast(mActivity, mHandler, R.string.toast_update_failed);
                }
                break;
            }
            case APPLY_END: {
                try {
                    mprogressDialog.dismiss();
                    mprogressDialog = null;
                } catch (Exception ne) { }
                if(msg.getData().getBoolean(RESULT, false)) {
                    ToastUtil.showOperationOk(mActivity, mHandler, true);
                    if(msg.getData().getInt("result_status") == UserCircle.STATUS_JOINED) {
                    	mCircle.mGroup.role_in_group = PublicCircleRequestUser.ROLE_TYPE_MEMEBER;
                    	refreshActionBtn();
                        //TODO if joined, need update local db
                        orm.insertOneCircle(mCircle);
                        if(mCircle.mGroup != null && mCircle.mGroup.parent_id > 0) {
                        	orm.insertOneCircleEvent(mCircle.mGroup.parent_id, mCircle);
                        }
                        importEventTocalendar();
                        QiupuHelper.updateActivityUI(null);
//                        UserImage myself = new UserImage();
//                        QiupuSimpleUser tmpUser = orm.querySimpleUserInfo(AccountServiceUtils.getBorqsAccountID());
//                        if(tmpUser != null) {
//                        	myself.user_id = tmpUser.uid;
//                        	myself.userName = tmpUser.nick_name;
//                        	myself.image_url = tmpUser.profile_image_url;
//                        	if(mCircle.inMembersImageList.size() > 5) {
//                        		mCircle.inMembersImageList.set(0, myself);
//                        	}else {
//                        		mCircle.inMembersImageList.add(myself);
//                        	}
//                        	mCircle.memberCount +=1;
//                        	
//                        	for(int i=0; i<mCircle.invitedMembersList.size(); i++) {
//                        		if(mCircle.invitedMembersList.get(i).user_id == tmpUser.uid) {
//                        			mCircle.invitedMembersList.remove(i);
//                        			mCircle.invitedMembersCount --;
//                        			if(mCircle.invitedMembersCount <= 0) {
//                        				mCircle.invitedMembersCount = 0;
//                        			}
//                        			break;
//                        		}
//                        	}
//                        	refreshMemberUi();
//                        }
//                        }else {
                        	mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO).sendToTarget(); 
//                        }
                    }
                }else {
                    ToastUtil.showOperationFailed(mActivity, mHandler, true);
                }
                break; 
            }
            }
        }
    }
    
    boolean inGetPublicInfo;
    Object mLockGetPublicInfo = new Object();
    public void syncPublicCircleInfo(final String circleId, final boolean with_member, final boolean isEvent) {
        if (!ToastUtil.testValidConnectivity(mActivity)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }
        
    	if (inGetPublicInfo == true) {
    		ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
    		return;
    	}
    	
    	synchronized (mLockGetPublicInfo) {
    		inGetPublicInfo = true;
    	}
    	begin();
    	
    	asyncQiupu.syncPublicCirclInfo(AccountServiceUtils.getSessionID(), circleId, with_member, isEvent, new TwitterAdapter() {
    		public void syncPublicCirclInfo(UserCircle circle) {
    			Log.d(TAG, "finish syncPublicCirclInfo=" + circle.toString());
    			
    			mCircle = circle;
    			if(mCircle.mGroup != null && PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
    			    orm.insertOneCircle(mCircle);
    			    importEventTocalendar();
    			}
    			// insert to circle_event
    			if(mCircle.mGroup != null && mCircle.mGroup.parent_id > 0) {
                	orm.insertOneCircleEvent(mCircle.mGroup.parent_id, mCircle);
                }
    			
    			Message msg = mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO_END);
    			msg.getData().putBoolean(RESULT, true);
    			msg.sendToTarget();
    			synchronized (mLockGetPublicInfo) {
    				inGetPublicInfo = false;
    			}
    		}
    		
    		public void onException(TwitterException ex, TwitterMethod method) {
    		    synchronized (mLockGetPublicInfo) {
    				inGetPublicInfo = false;
    			}
    			Message msg = mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO_END);
    			msg.getData().putBoolean(RESULT, false);
    			msg.sendToTarget();
    		}
    	});
    }
    
    View.OnClickListener actionClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mInviteBtn.getVisibility() == View.VISIBLE) {
				gotoPickCircleUserActivity();
			}else if(mApplyBtn.getVisibility() == View.VISIBLE) {
				applyInPublicCircle(String.valueOf(mCircle.circleid), "");
			}else {
				Log.d(TAG, "have no view ");
			}
		}
	};
    
	private void gotoPickCircleUserActivity() {
        IntentUtil.gotoPickAudienceActivity(getActivity(), mCircle, PickAudienceActivity.PICK_FROM_EVNET);
    }
	
    
    boolean inApplying;
    Object mLockApplying = new Object();
    private void applyInPublicCircle(final String circleId, final String message) {
        if (inApplying == true) {
            ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
            return;
        }
        
        synchronized (mLockApplying) {
            inApplying = true;
        }
        showProcessDialog(R.string.apply_to_public_circle, false, true, true);
        
        asyncQiupu.applyInPublicCircle(AccountServiceUtils.getSessionID(), circleId, message, new TwitterAdapter() {
            public void applyInPublicCircle(int result) {
                Log.d(TAG, "finish applyInPublicCircle=" + result);

                Message msg = mHandler.obtainMessage(APPLY_END);
                msg.getData().putBoolean(RESULT, true);
                msg.getData().putInt("result_status", result);
                msg.sendToTarget();
                synchronized (mLockApplying) {
                    inApplying = false;
                }
            }
            
            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mLockApplying) {
                    inApplying = false;
                }
                Message msg = mHandler.obtainMessage(APPLY_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }
    
    private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable){
    	mprogressDialog = DialogUtils.createProgressDialog(mActivity, 
    			resId, CanceledOnTouchOutside, Indeterminate, cancelable);
    	mprogressDialog.show();    	
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode);
        switch (requestCode) {
            case PHOTO_PICKED_WITH_DATA: {
                if (mCurrentPhotoFile != null && mCurrentPhotoFile.exists()) {
                    mCurrentPhotoFile.delete();
                } 
                photo = data.getParcelableExtra("data");
                mCurrentPhotoFile = new File(QiupuHelper.getTmpCachePath()+"screenshot.png");
                FileOutputStream fOut = null;
                try {
                    if (mCurrentPhotoFile.exists()) {
                        mCurrentPhotoFile.delete();
                    }                
                    mCurrentPhotoFile.createNewFile();
                    fOut = new FileOutputStream(mCurrentPhotoFile);                      
                    photo.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                    fOut.close();

                    editPublicCircleImage(new File(QiupuHelper.getTmpCachePath()+"screenshot.png"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //TODO
//                sharedImageUri = null;
                break;  
            }  
            case CAMERA_WITH_DATA: {
                tryCropProfileImage(Uri.fromFile(mCurrentPhotoFile));
                break;  
            }  
        }
    }

    private void tryCropProfileImage(Uri uri) {
        try {
            // start gallery to crop photo
            final Intent intent = QiupuHelper.getCropImageIntent(uri);
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {}
    }
    
    Object mEditPublicImageLock = new Object();
    boolean inEditImageProcess;
    private void editPublicCircleImage(File file){
        synchronized(mEditPublicImageLock)
        {
            if(inEditImageProcess == true)
            {
                ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
                return ;
            }
        }
        
        showProcessDialog(R.string.edit_profile_update_dialog, false, true, true);
        
        synchronized(mEditPublicImageLock)
        {
            inEditImageProcess = true;          
        }
        asyncQiupu.editPublicCircleImage(AccountServiceUtils.getSessionID(),mCircle.circleid, file, new TwitterAdapter() {
            public void editPublicCircleImage(boolean result) {
                Log.d(TAG, "finish edit public circle image ");
                Message msg = mHandler.obtainMessage(EDIT_PROFILE_IMAGE_END);               
                msg.getData().putBoolean(BasicActivity.RESULT, result);
                msg.sendToTarget();
                synchronized(mEditPublicImageLock)
                {
                    inEditImageProcess = false;         
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                
                synchronized(mEditPublicImageLock)
                {
                    inEditImageProcess = false;         
                }
                TwitterExceptionUtils.printException(TAG, "editUserProfileImage, server exception:", ex, method);

                Message msg = mHandler.obtainMessage(EDIT_PROFILE_IMAGE_END);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(BasicActivity.RESULT, false);
                msg.sendToTarget();
            }
        });
    }
    
    public void refreshEventInfo() {
    	super.loadRefresh();
    	mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO).sendToTarget();
    }
    
	public interface EventInfoFragmentListenerCallBack {
		public void getEventInfoFragment(EventInfoFragment fragment);
		public UserCircle getCircleInfo();
		public void refreshCircleInfo(UserCircle circle);
		public void gotoProfileDetail();
	}

	@Override
	public void updateItemUI(QiupuUser user) {
		mCircle = orm.queryOneCircleWithGroup(mCircle.circleid);
		refreshCircleInfoUi();
	}

	@Override
	public void addFriends(QiupuUser user) {
	}

	@Override
	public void refuseUser(long uid) {
	}

	@Override
	public void deleteUser(QiupuUser user) {
	}

	@Override
	public void sendRequest(QiupuUser user) {
	}
	
    private void importEventTocalendar() {
    	if(orm.needInsertToCalendar(mActivity, mCircle)) {
    		orm.updateEventCalendarTime(mCircle);
			CalendarMappingUtils.importEventTocalendar(mActivity, mCircle, orm);
    	}else {
    		Log.d(TAG, "no need export to calendar");
    	}
    	
//    	if(CalendarMappingUtils.checkApkExist(mActivity)) {
//    		long calendarEventId = orm.getEventCalendarid(mCircle.circleid);
//    		if(CalendarMappingUtils.isImportedToCalendar(mActivity, calendarEventId)) {
//    			if(needUpdateEvent()) {
//    				CalendarMappingUtils.removeCalendarEvent(mActivity, calendarEventId);
//    				CalendarMappingUtils.insertToCalendar(mActivity, mCircle, orm);
//    			}else {
////				ToastUtil.showShortToast(this, mHandler, R.string.already_import);
//    			}
//    		}else {
//    			CalendarMappingUtils.insertToCalendar(mActivity, mCircle, orm);
//    		}
//    	}else {
//    		Log.d(TAG, "calendar is not exist");
//    	}
	}
    
    private boolean needUpdateEvent() {
		boolean ret = false;
		if(mCircle.mGroup == null) {
			ret = true;
		}else {
			if(mCircle.mGroup.updated_time != orm.queryEventUpdateTime(mCircle.circleid)) {
				ret = true;
			}
		}
		return ret;
	}

    @Override
    public void setTopListTitle(String title) {
        mCircle.mGroup.top_post_name = title;
        if(TextUtils.isEmpty(mCircle.mGroup.top_post_name)) {
            tv_top_post.setText(R.string.view_top);
        }else {
            tv_top_post.setText(mCircle.mGroup.top_post_name);
        }
    }

    @Override
    public void hideTopListTitle() {
        mCircle.mGroup.top_post_count = 0;
        layout_top_post.setVisibility(View.GONE);
    }

	@Override
	public void refreshInfo() {
		if(mCircle != null && mCircle.mGroup != null) {
			mCircle.mGroup.invited_ids = orm.queryInviteIds(mCircle.circleid);
		}
	}

}
