package com.borqs.qiupu.fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import twitter4j.AsyncQiupu;
import twitter4j.ErrorResponse;
import twitter4j.InfoCategory;
import twitter4j.PublicCircleRequestUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Html;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.listener.RefreshCircleListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.MyLinkMovementMethod;
import com.borqs.common.view.PublicCircleLayoutView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.cache.StreamCacheManager;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.TopPostListFragment.UpdateTitleUICallBack;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.PickAudienceActivity;
import com.borqs.qiupu.ui.bpc.PickAudienceActivity.inviteListeners;
import com.borqs.qiupu.ui.circle.EditPublicCircleActivity;
import com.borqs.qiupu.ui.circle.quickAction.ActionItem;
import com.borqs.qiupu.ui.circle.quickAction.QuickAction;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.DateUtil;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class PublicCircleMainFragment extends StreamListFragment implements
        RefreshCircleListener, inviteListeners,
        UpdateTitleUICallBack {
	private final static String TAG = "PublicCircleMainFragment";
	private Activity mActivity; 
	
	private Handler mHandler;
	private QiupuORM orm;
	private AsyncQiupu asyncQiupu;
	private CircleMainFragmentCallBack mCallBackListener;
    private TextView mCircle_Name;
//    private TextView tv_top_post;
//	private View layout_top_post;
//	private TextView tv_top_poll;
//    private View span_view;
	private TextView mCircleBulletin;
	private TextView mBulletin_time;
	private ImageView mProfile_img_ui;
	private ImageView mSelect_cover;
	
    private UserCircle mCircle;
    private static final String ERRORMSG = "errormsg";
    private static final String ERROR_CODE = "error_code";
    private ProgressDialog mprogressDialog;
    
//    private TextView mInviteBtn;
    private TextView mOptionBtn;
    private TextView mApplyBtn;
    private View mActionView;
    
    private View mContentView;
//    private HorizontalLinearLayoutView mSourceView;
    private PublicCircleLayoutView mSourceView;
//    private View mPage_btn;
    
    private TextView mCategoryView;
    private View mCategoryRl;
    private ImageView mCategoryIcon;
//    private View mExitbtn;
//    private View mEditbtn;
//    private View mSubscribeSet;
    
    private File mCurrentPhotoFile;
    private Bitmap photo ;
    public static final int TYPE_TEXT  = 0;
    public static final int PHOTO_PICKED_WITH_DATA = 3021;
    public static final int CAMERA_WITH_DATA = 3023;
    
    private static final String mBulletinViewTag = "bulletintag";
    
    private static final long category_all_id = -100;
    private static final long category_add_id = -200;
    
    private boolean mIsDirectExit;
    private boolean mIsAreadyLoadCircle;
    
    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
    	super.onAttach(activity);
    	mActivity = activity;

        if (mActivity instanceof CircleMainFragmentCallBack) {
        	mCallBackListener = (CircleMainFragmentCallBack)activity;
        	mCallBackListener.getCircleInfoFragment(this);
        	mCircle = mCallBackListener.getCircleInfo();
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
    	super.onCreate(savedInstanceState);
    	parserSavedState(savedInstanceState);
    	QiupuHelper.registerRefreshCircleListener(getClass().getName(), this);
    	PickAudienceActivity.registerInviteListener(getClass().getName(), this);
    	orm = QiupuORM.getInstance(mActivity);
		asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
		mHandler = new MainHandler();
		mSelectCategoryId = category_all_id;
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
    	mContentView = inflater.inflate(R.layout.public_circle_main_head, null, false);
    	mCircle_Name = (TextView) mContentView.findViewById(R.id.public_cricle_name);
    	mCircle_Name.setMovementMethod(MyLinkMovementMethod.getInstance());
    	mSelect_cover = (ImageView) mContentView.findViewById(R.id.select_cover);
//    	tv_top_post = (TextView) mContentView.findViewById(R.id.tv_top_post);
//    	layout_top_post = mContentView.findViewById(R.id.layout_top_post);
//    	span_view = mContentView.findViewById(R.id.view_span);
//        tv_top_poll = (TextView) mContentView.findViewById(R.id.tv_top_poll);
    	mProfile_img_ui =  (ImageView) mContentView.findViewById(R.id.public_circle_img_ui);
    	mCircleBulletin = (TextView) mContentView.findViewById(R.id.public_cricle_bulletin);
    	mBulletin_time = (TextView) mContentView.findViewById(R.id.last_bulletin_time);
    	
    	mSourceView = (PublicCircleLayoutView) mContentView.findViewById(R.id.source_view);
//    	mInviteBtn = (TextView) mContentView.findViewById(R.id.invite_btn);
//    	mInviteBtn.setOnClickListener(invitePeopleClickListener);
    	
    	mOptionBtn = (TextView) mContentView.findViewById(R.id.option_btn);
    	mOptionBtn.setOnClickListener(moreActionClickListener);
    	
    	mApplyBtn = (TextView) mContentView.findViewById(R.id.apply_btn);
    	mApplyBtn.setOnClickListener(applyPeopleClickListener);
    	
    	mActionView = mContentView.findViewById(R.id.id_action_fl);
    	
    	mSelect_cover.setOnClickListener(gotoDetailClickListener);
        mCircle_Name.setOnClickListener(gotoDetailClickListener);
        
    	final ImageView dropDownView = (ImageView) mContentView.findViewById(R.id.dropdown_icon);
    	dropDownView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				refreshHorizontalLayout();
				if(mSourceView.getVisibility() == View.GONE) {
					dropDownView.setImageResource(R.drawable.up_icon);
					mSourceView.setVisibility(View.VISIBLE);
				}else {
					dropDownView.setImageResource(R.drawable.down_icon);
					mSourceView.setVisibility(View.GONE);
				}
			}
		});
    	
    	mCircleBulletin.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "mCircleBulletin.getLineCount(): " + mCircleBulletin.getLineCount());
				if(mCircleBulletin.getTag() != null) {
					mCircleBulletin.setMaxLines(mCircleBulletin.getLineCount());
					mCircleBulletin.setTag(null);
				}else {
					mCircleBulletin.setMaxLines(2);
					mCircleBulletin.setTag(mBulletinViewTag);
				}
			}
		});
    	
    	mCategoryView = (TextView) mContentView.findViewById(R.id.info_category);
    	mCategoryView.setText(R.string.all_category_label);
    	
    	mCategoryRl = mContentView.findViewById(R.id.category_rl);
    	mCategoryRl.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			if(mSelectCategoryId == category_add_id) {
					showAddCategoryDialog();
				}else {
					showCategoryDropDownDialog();
				}
    		}
    	});
    	mCategoryIcon = (ImageView) mContentView.findViewById(R.id.category_title_icon);
    	
//    	mPage_btn = mContentView.findViewById(R.id.page_btn);
//    	mPage_btn.setOnClickListener(gotoPageClickListener);
    	
    	View action_more = mContentView.findViewById(R.id.action_more);
    	action_more.setOnClickListener(moreActionClickListener);
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
        mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO).sendToTarget();
			}
		}, 1000);
    }

    private void refreshCircleInfoUi() {
    	if(mCircle != null && isDetached() == false && getActivity() != null) {
    		mCircle.uid = mCircle.circleid;
    	    refreshHeadUi();
    	    refreshActionBtn();
    		refreshOtherInfoUi();
    		refreshProfileIcon();
        	if(mCallBackListener != null) {
        		mCallBackListener.refreshActivityBottom();
				mCallBackListener.changeHeadTitle(mCircle);
			}
        	mSourceView.setCircleInfo(mCircle);
		}
    }
    
    private void refreshProfileIcon() {
    	 if(mCircle.mGroup != null && mCircle.mGroup.viewer_can_update) {
             findFragmentViewById(R.id.icon_camera).setVisibility(View.VISIBLE);
             mProfile_img_ui.setOnClickListener(editProfileImageListener);
         }else {
        	 findFragmentViewById(R.id.icon_camera).setVisibility(View.GONE);
             mProfile_img_ui.setOnClickListener(null);
         }
    }
    
    private void refreshActionBtn() {
        if(mCircle.mGroup != null) {
        	if(mCircle.mGroup.role_in_group < 0) {
        		Log.d(TAG, "refreshActionbtn has no role, hide action btn");
        		return;
        	}
            if(PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
//            	if(PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group) ||
//            			PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group)) {
            		mOptionBtn.setVisibility(View.VISIBLE);
                    mApplyBtn.setVisibility(View.GONE);
                    mActionView.setVisibility(View.VISIBLE);
//            	}else {
//            		if(UserCircle.VALUE_ALLOWED == mCircle.mGroup.can_member_invite) {
//            			mActionView.setVisibility(View.VISIBLE);
//                        mInviteBtn.setVisibility(View.VISIBLE);
//                        mApplyBtn.setVisibility(View.GONE);
//                    }else {
//                    	mInviteBtn.setVisibility(View.GONE);
//                        mApplyBtn.setVisibility(View.GONE);
//                        mActionView.setVisibility(View.GONE);
//                    }	
//            	}
            }else {
            	if(UserCircle.JOIN_PERMISSION_DERECT_ADD == mCircle.mGroup.can_join
            			|| UserCircle.JOIN_PREMISSION_VERIFY == mCircle.mGroup.can_join) { 
            		mApplyBtn.setVisibility(View.VISIBLE);
            		mOptionBtn.setVisibility(View.GONE);
                    mActionView.setVisibility(View.VISIBLE);
            	}else {
            		mApplyBtn.setVisibility(View.GONE);
            		mOptionBtn.setVisibility(View.GONE);
                    mActionView.setVisibility(View.GONE);
            	}
            }
            
            if(mSelectCategoryId > 0) {
            	Log.d(TAG, "already select category, no need refresh ");
            }else {
            	if(PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group) ||
            			PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group)) {
            		
            		mCategoryRl.setVisibility(View.VISIBLE);
            		if(mCircle.categories != null && mCircle.categories.size() > 0) {
            			mSelectCategoryId = category_all_id;
            			mCategoryView.setText(R.string.all_category_label);
            			mCategoryIcon.setVisibility(View.VISIBLE);
            		}else {
            			mSelectCategoryId = category_add_id;
            			mCategoryView.setText(R.string.add_category_label);
            			mCategoryIcon.setVisibility(View.GONE);
            		}
            	}else {
            		if(mCircle.categories != null && mCircle.categories.size() > 0) {
            			mCategoryRl.setVisibility(View.VISIBLE);
            		}else {
            			mCategoryRl.setVisibility(View.GONE);
            		}
            	}
            }
        }
    }
    
    private void refreshOtherInfoUi() {
        if(mCircle.mGroup != null) {
            refreshProfileStatus(mCircle.mGroup.bulletin, DateUtil.converToRelativeTime(getActivity(), mCircle.mGroup.bulletin_updated_time));
//            refreshTopPostAndPoll();
        }
    }

//    private void refreshTopPostAndPoll() {
//        refreshTopPostInfo();
//    }

//    private void refreshTopPostInfo() {
//        if(mCircle != null && mCircle.mGroup != null && mCircle.mGroup.top_post_count > 0) {
//        	tv_top_post.setVisibility(View.VISIBLE);
////            span_view.setVisibility(View.VISIBLE);
//            if(TextUtils.isEmpty(mCircle.mGroup.top_post_name)) {
//                tv_top_post.setText(R.string.view_top);
//            }else {
//                tv_top_post.setText(mCircle.mGroup.top_post_name);
//            }
//            tv_top_post.setOnClickListener(new View.OnClickListener() {
//                
//                @Override
//                public void onClick(View v) {
//                    IntentUtil.startTopPostIntent(mActivity, mCircle.circleid,mCircle.mGroup.top_post_name,
//                            mCircle.mGroup.viewer_can_update);
//                    
//                }
//            });
//        }else {
//        	tv_top_post.setVisibility(View.GONE);
//        }
//    }
    
    private void refreshProfileStatus(final String status, final String time) {
        if(StringUtil.isValidString(status)) {
        	mCircleBulletin.setVisibility(View.VISIBLE);
        	mBulletin_time.setVisibility(View.VISIBLE);
//            if(mCircle.mGroup.role_in_group == PublicCircleRequestUser.ROLE_TYPE_CREATER
//                    || mCircle.mGroup.role_in_group == PublicCircleRequestUser.ROLE_TYPE_CREATER) {
                mCircleBulletin.setText(Html.fromHtml("<font color='#5f78ab'> "+ status + "</font>"));
//            }else {
//                mProfile_user_status.setText(status);
//            }
            mBulletin_time.setText(time);
//        mProfile_user_status.setOnClickListener(updateStatusClickListener);
//        mLast_status_time.setOnClickListener(updateStatusClickListener);
            
//            if(mCircleBulletin.getLineCount() > 2) {
            	mCircleBulletin.setMaxLines(2);
//            	mCircleBulletin.setEllipsize(TruncateAt.END);
            	mCircleBulletin.setTag(mBulletinViewTag);
//            }
        }else {
//        	mCircleBulletin.setVisibility(View.GONE);
//        	mBulletin_time.setVisibility(View.GONE);
        }
    }
    
    private void refreshHeadUi() {
        mCircle_Name.setText(mCircle.name);
        setViewIcon(mCircle.profile_image_url, mProfile_img_ui, true);
        if(mCircle.mGroup != null) {
			if(StringUtil.isEmpty(mCircle.mGroup.coverUrl)) {
				mSelect_cover.setImageResource(R.drawable.event_default_cover);
			}else {
				setViewIcon(mCircle.mGroup.coverUrl, mSelect_cover, false);
			}
		}
        
        // set page_btn visibility status
//        if(mCircle.mGroup != null) {
//			if(mCircle.mGroup.formal == UserCircle.circle_free) {
//				Log.d(TAG, "this circle is free circle, no need show pageitem");
//				mPage_btn.setVisibility(View.GONE);
//			}else {
//				mPage_btn.setVisibility(View.VISIBLE);
//			}
//		}
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
    	QiupuHelper.unregisterRefreshCircleListener(getClass().getName());
    	PickAudienceActivity.unregisterInviteListener(getClass().getName());
    }
    
    private final static int GET_PUBLIC_CIRCLE_INFO = 101;
    private final static int GET_PUBLIC_CIRCLE_INFO_END = 102;
//    private final static int INVIT_PEOPLE_END = 103;
    private final static int EDIT_PROFILE_IMAGE_END = 104;
    private final static int APPLY_END = 105;
    private final static int ADD_CATEGORY_END = 106;
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
            	if (ret) {
            		mIsAreadyLoadCircle = true;
            	    refreshCircleInfoUi();
            	    if(mCallBackListener != null) {
        				mCallBackListener.refreshRightFragmentUi(mCircle);
        			}
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
                        // insert to circle_circles table
                        if(mCircle.mGroup != null && mCircle.mGroup.parent_id > 0) {
                        	orm.insertOneCircleCircles(mCircle.mGroup.parent_id, mCircle);
                        }
                        
                        if(mCallBackListener != null) {
                        	mCallBackListener.refreshActivityBottom();
                        }
                        QiupuHelper.updateCirclesUI();
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
            
            case ADD_CATEGORY_END: {
            	 try {
                     mprogressDialog.dismiss();
                     mprogressDialog = null;
                 } catch (Exception ne) { }
            	 if(msg.getData().getBoolean(RESULT, false)) {
//            		 Drawable[] dws = mCategoryView.getCompoundDrawables();
            		 if(mSelectCategoryId == category_add_id) {
            			 mSelectCategoryId = category_all_id;
            			 mCategoryView.setText(R.string.all_category_label);
            			 mCategoryIcon.setVisibility(View.VISIBLE);
            		 }
            		 showCategoryDropDownDialog();
            		 ToastUtil.showOperationOk(mActivity, mHandler, true);
            	 }else {
            		 if(msg.getData().getInt(ERROR_CODE) == ErrorResponse.CATEGORY_IS_EXISTS) {
            			ToastUtil.showShortToast(mActivity, mHandler, R.string.category_exist_message);
            		 }else {
            			 ToastUtil.showOperationFailed(mActivity, mHandler, true);
            		 }
                 }
            	 break;
            }
            }
        }
    }
    
    boolean inGetPublicInfo;
    Object mLockGetPublicInfo = new Object();
    protected void syncPublicCircleInfo(final String circleId, final boolean with_member, final boolean isEvent) {
        if (!ToastUtil.testValidConnectivity(mActivity)) {
            Log.i(TAG, "syncPublicCircleInfo, ignore while no connection.");
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
    	
    	mLoadingCircleId = Long.parseLong(circleId);
        mPendingLoadCircleId = mLoadingCircleId;
        
    	asyncQiupu.syncPublicCirclInfo(AccountServiceUtils.getSessionID(), circleId, with_member, isEvent, new TwitterAdapter() {
    		public void syncPublicCirclInfo(UserCircle circle) {
    			Log.d(TAG, "finish syncPublicCirclInfo=" + circle.toString());
    			
    			mCircle = circle;
    			if(mCircle.mGroup != null && PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
    			    orm.insertOneCircle(mCircle);
    			}
    			
    			// insert to circle_circles 
    			if(mCircle.mGroup != null && mCircle.mGroup.parent_id > 0) {
                	orm.insertOneCircleCircles(mCircle.mGroup.parent_id, mCircle);
                }
    			
    			onLoadingCircleReady("", true);
//    			Message msg = mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO_END);
//    			msg.getData().putBoolean(RESULT, true);
//    			msg.sendToTarget();
//    			synchronized (mLockGetPublicInfo) {
//    				inGetPublicInfo = false;
//    			}
    		}
    		
    		public void onException(TwitterException ex, TwitterMethod method) {
    			onLoadingCircleReady(ex.getMessage(), false);
//    		    synchronized (mLockGetPublicInfo) {
//    				inGetPublicInfo = false;
//    			}
//    			Message msg = mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO_END);
//    			msg.getData().putBoolean(RESULT, false);
//    			msg.sendToTarget();
    		}
    	});
    }
    
    private void onLoadingCircleReady(String promptText, boolean result) {
    	synchronized (mLockGetPublicInfo) {
			inGetPublicInfo = false;
		}
        if (mPendingLoadCircleId != mLoadingCircleId) {
            Log.d(TAG, "onLoadingReady invoke circleinfo loading.");
            if(mHandler == null)
        		return;
            
            mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO).sendToTarget();
        } else {
        	if(mHandler != null) {
        		Message msg = mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO_END);
    			msg.getData().putBoolean(RESULT, result);
    			msg.sendToTarget();
        	}
        }
    }
    
    View.OnClickListener invitePeopleClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			gotoPickCircleUserActivity();
		}
	};
	
	View.OnClickListener applyPeopleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            applyInPublicCircle(String.valueOf(mCircle.circleid), "");
        }
    };


	private void gotoPickCircleUserActivity() {
        IntentUtil.gotoPickAudienceActivity(getActivity(), mCircle, PickAudienceActivity.PICK_FROM_CREATE_CIRCLE);
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
    	if (resultCode != Activity.RESULT_OK) {
    		return ;
    	}
        switch (requestCode) {
            case PHOTO_PICKED_WITH_DATA: {
            	if (mCurrentPhotoFile != null && mCurrentPhotoFile.exists()) {
            		mCurrentPhotoFile.delete();
            	} 
            	
            	photo = data.getParcelableExtra("data");
            	if(photo == null){
            		//get photo url
            		Uri originalUri = data.getData();
            		try {
            			tryCropProfileImage(Uri.parse(originalUri.toString()));
            		} catch (Exception e) {
            			e.printStackTrace();
            		}
            	}else {
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
            	}
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
    
    View.OnClickListener editProfileImageListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            String[] items = new String[] {
                    getString(R.string.take_photo),
                    getString(R.string.phone_album) };
            DialogUtils.showItemsDialog(mActivity, mActivity.getResources().getString(R.string.select_circle_photo), 0, items,
                    ChooseEditImageItemClickListener);
        }
    };
    
    DialogInterface.OnClickListener ChooseEditImageItemClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == 0) {
                doTakePhoto();// from camera  
            } else {
                doPickPhotoFromGallery();// from  gallery
            }
        }
    };
    
    private void doPickPhotoFromGallery() {
        try {
            // Launch picker to choose photo for selected contact  
            final Intent intent = QiupuHelper.getPhotoPickIntent();  
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        }
        catch (ActivityNotFoundException e) {}
    }
    
    private void doTakePhoto() {
        try {
            // Launch camera to take photo for selected contact  
            QiupuHelper.PHOTO_DIR.mkdirs();
            mCurrentPhotoFile = new File(QiupuHelper.PHOTO_DIR, QiupuHelper.getPhotoFileName());  
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCurrentPhotoFile));
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            startActivityForResult(intent, CAMERA_WITH_DATA);
        }
        catch (ActivityNotFoundException e) {}
    }
    
    public void refreshCircle() {
    	super.loadRefresh();
    	mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO).sendToTarget();
    }
    
	public interface CircleMainFragmentCallBack {
		public void getCircleInfoFragment(PublicCircleMainFragment fragment);
		public UserCircle getCircleInfo();
		public void changeHeadTitle(UserCircle circle);
		public void changeshareSource(long uid);
		public void createAspage();
		public void refreshRightFragmentUi(UserCircle circle);
		public void exitCircle(boolean isDirect);
		public void circleToPage(UserCircle circle);
		public void deleteCircle();
		public void createShortcut();
		public void refreshActivityBottom();
	}
	
	private Dialog mDialog;
    public void showEditGroupInfoDialog() {
        final View editview = createEditGroupInfoView();
        mDialog = new Dialog(mActivity, R.style.FlowViewStyle);
        mDialog.setContentView(editview);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
    }
    
    private void dismissEditDialog() {
        if(mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
    private View createEditGroupInfoView() {
        View editview = View.inflate(mActivity, R.layout.edit_group_info_layout, null);
        
        View base_info = editview.findViewById(R.id.base_info);
        base_info.setOnClickListener(baseInfoClickListener);
        
        View base_set = editview.findViewById(R.id.base_set);
        base_set.setOnClickListener(baseSetClickListener);
        
        View people_manager = editview.findViewById(R.id.people_manager);
        people_manager.setOnClickListener(peopleManagerClickListener);
        
        View contact_info = editview.findViewById(R.id.contact_info);
        contact_info.setOnClickListener(ContactInfoClickListener);
        return editview;
    }
    
    View.OnClickListener baseInfoClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            dismissEditDialog();
            IntentUtil.gotoEditPublicCircleActivity(mActivity, mCircle.name, mCircle, EditPublicCircleActivity.eidt_type_base_info);
        }
    };
    
    View.OnClickListener baseSetClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            dismissEditDialog();
            IntentUtil.gotoEditPublicCircleActivity(mActivity, mCircle.name, mCircle, EditPublicCircleActivity.eidt_type_base_set);
        }
    };
    View.OnClickListener peopleManagerClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            dismissEditDialog();
            int status = PublicCircleRequestUser.STATUS_IN_CIRCLE;
            String title = getString(R.string.members_in_circle);
            IntentUtil.startPublicCirclePeopleActivity(mActivity, mCircle, status, title);
        }
    };
    View.OnClickListener ContactInfoClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            dismissEditDialog();
            IntentUtil.gotoEditPublicCircleActivity(mActivity, mCircle.name, mCircle, EditPublicCircleActivity.eidt_type_contact_info);
        }
    };

	private void approvedPeopleCallback(final long uid) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if(mCircle.applyedMembersList != null && mCircle.applyedMembersList.size() > 0) {
					for(int i=0; i<mCircle.applyedMembersList.size(); i++) {
						if(uid == mCircle.applyedMembersList.get(i).user_id) {
							mCircle.applyedMembersList.remove(i);
							mCircle.applyedMembersCount --;
						}
					}
				}
			}
		});
	}

    @Override
    public void setTopListTitle(String title) {
    	//TODO 
//    	mSourceView.setTopListTitle(title);
//        mCircle.mGroup.top_post_name = title;
//        if(TextUtils.isEmpty(mCircle.mGroup.top_post_name)) {
//            tv_top_post.setText(R.string.view_top);
//        }else {
//            tv_top_post.setText(mCircle.mGroup.top_post_name);
//        }
    }

    @Override
    public void hideTopListTitle() {
    	//TODO
//    	mSourceView.hideTopListTitle();
//        mCircle.mGroup.top_post_count = 0;
//        tv_top_post.setVisibility(View.GONE);
    }
    
	DialogInterface.OnClickListener createAsPageOkListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(mCallBackListener != null) {
				mCallBackListener.createAspage();
			}
		}
	};
	
	View.OnClickListener gotoDetailClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
//			if(mCallBackListener != null)
//				mCallBackListener.gotoProfileDetail();
		}
	};
	
	public void gotoCreatePollActivity() {
		if(mCircle != null) {
//			HashMap<String, String> receiverMap = new HashMap<String, String>();
//			String receiverid = "#" +  mCircle.circleid;
//			receiverMap.put(String.valueOf(mCircle.circleid), mCircle.name);
//			IntentUtil.startCreatePollActivity(mActivity, receiverMap, receiverid, mCircle.circleid);
		}else {
			Log.d(TAG, "mCircle is null ");
		}
	}
	public void gotoCreateEventActivity() {
		if(mCircle != null) {
//			HashMap<String, String> receiverMap = new HashMap<String, String>();
//			String receiverid = "#" +  mCircle.circleid;
//			receiverMap.put(String.valueOf(mCircle.circleid), mCircle.name);
//			IntentUtil.gotoCreateEventActivity(mActivity,  EditPublicCircleActivity.type_create_event, receiverMap, receiverid, mCircle.circleid);
		}else {
			Log.d(TAG, "mCircle is null ");
		}
	}

	@Override
	public void refreshInfo() {
		if(mCircle != null && mCircle.mGroup != null) {
			mCircle.mGroup.invited_ids = orm.queryInviteIds(mCircle.circleid);
		}
	}
    @Override
    public boolean switchCircle(long circleId) {
    	boolean streamChanged = super.switchCircle(circleId);
        if (streamChanged) {
        }

        boolean infoChanged = swithcCircleToLoadCircle(circleId);
        return streamChanged || infoChanged;
    }
    
    private long mPendingLoadCircleId;
    private long mLoadingCircleId;
    private boolean swithcCircleToLoadCircle(long circleId) {
    	if(mPendingLoadCircleId == circleId) {
    		return false;
    	}
    	
    	mPendingLoadCircleId = circleId;
    	mCircle = orm.queryOneCircleWithGroup(circleId);
    	if(mCircle == null) {
    		mCircle = new UserCircle();
    		mCircle.circleid = circleId;
    	}
    	refreshCircleInfoUi();
    	
    	mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO).sendToTarget();
    	return true;
    }
    
    View.OnClickListener gotoPageClickListener = new OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		gotoPageDetail();
    	}
    };
    
    public void gotoPageDetail() {
    	if(mCircle.mGroup != null) {
			if(mCircle.mGroup.pageid > 0) {
				IntentUtil.startPageDetailActivity(mActivity, mCircle.mGroup.pageid);
				return ;
			}
			
			if(PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group)
					|| PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group)) {
				if(mCircle.mGroup.pageid <=0 && (mCircle.mGroup.formal == UserCircle.circle_top_formal || mCircle.mGroup.formal == UserCircle.circle_sub_formal)) {
					DialogUtils.showConfirmDialog(mActivity, getString(R.string.circle_as_page), getString(R.string.create_as_page_msg_label), createAsPageOkListener);
				} else {
					ToastUtil.showShortToast(mActivity, mHandler, R.string.have_no_permission_to_seePage);
				}
			}else {
				ToastUtil.showShortToast(mActivity, mHandler, R.string.have_no_permission_to_seePage);
			}
		} else {
			ToastUtil.showShortToast(mActivity, mHandler, R.string.have_no_permission_to_seePage);
		}
    }
    
    private void showCategoryDropDownDialog() {
    	
//    	mCategoryView.setBackgroundResource(R.drawable.info_category_title_top_line_bg);
//    	mCategoryView.setPadding((int)getResources().getDimension(R.dimen.category_padding_left), 
//    			(int)getResources().getDimension(R.dimen.default_text_padding_top), (int)getResources().getDimension(R.dimen.category_padding_right), (int)getResources().getDimension(R.dimen.default_text_padding_bottom));
    	
    	final QuickAction quickAction = new QuickAction(mActivity, QuickAction.VERTICAL);
    	quickAction.setSelectActionId(mSelectCategoryId);
    	
    	quickAction.addActionItem(new ActionItem(category_all_id, getString(R.string.all_category_label), null));
    	
    	if(mCircle != null && mCircle.categories != null) {
    		for(int i=0; i<mCircle.categories.size(); i++) {
    			InfoCategory ic = mCircle.categories.get(i);
    			quickAction.addActionItem(new ActionItem(ic.categoryId, ic.categoryName, null));
    		}
    	}
    	
    	if(mCircle != null && mCircle.mGroup != null) {
    		if(PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group) ||
    				PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group)) {
    			quickAction.addActionItem(new ActionItem(category_add_id, getString(R.string.add_category_label), null));
    		}	
    	}
        
        //Set listener for action item clicked
		quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {			
			@Override
			public void onItemClick(QuickAction source, int pos, long actionId) {				
				ActionItem actionItem = quickAction.getActionItem(pos);
				quickAction.dismiss();
                 if(actionItem.getActionId() == category_add_id) {
                	 showAddCategoryDialog();
                 }else {
                	 mCategoryView.setText(actionItem.getTitle());
                	 if(mSelectCategoryId == actionItem.getActionId()) {
                		 Log.d(TAG, "select same actionid , do nothing");
                	 }else {
                		 mSelectCategoryId = actionItem.getActionId();
                		 //TODO
                		 switchCategory(mSelectCategoryId);
                	 }
                 }
			}
		});
		
		//set listnener for on dismiss event, this listener will be called only if QuickAction dialog was dismissed
		//by clicking the area outside the dialog.
		quickAction.setOnDismissListener(new QuickAction.OnDismissListener() {			
			@Override
			public void onDismiss(boolean onTop) {
				Log.d(TAG, "quickAction dismiss.");
				if(onTop) {
    				Animation operatingAnim = AnimationUtils.loadAnimation(mActivity, R.anim.rotate_tip_arrowup_revert); 
    				operatingAnim.setFillAfter(true);
    				mCategoryIcon.startAnimation(operatingAnim);
    			}else {
    				Animation operatingAnim = AnimationUtils.loadAnimation(mActivity, R.anim.rotate_tip_revert); 
    				operatingAnim.setFillAfter(true);
    				mCategoryIcon.startAnimation(operatingAnim);
    			}
				mCategoryRl.setBackgroundResource(R.drawable.info_category_title_bg);
				mCategoryRl.setPadding((int)getResources().getDimension(R.dimen.category_padding_left), 
		    			(int)getResources().getDimension(R.dimen.small_text_padding_top), (int)getResources().getDimension(R.dimen.category_padding_right), (int)getResources().getDimension(R.dimen.small_text_padding_bottom));
			}
		});
    	
		quickAction.show(mCategoryRl, mCategoryIcon);
		quickAction.setAnimStyle(QuickAction.ANIM_REFLECT);
    }
    
    private EditText mAddCategoryEditText;
    private void showAddCategoryDialog() {
    	LayoutInflater factory = LayoutInflater.from(mActivity);
    	mAddCategoryEditText = (EditText) factory.inflate(R.layout.default_edittext, null);
    	mAddCategoryEditText.setHint(R.string.poll_title_hint);
    	mAddCategoryEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25)});
        DialogUtils.ShowDialogwithView(mActivity, getString(R.string.add_category_label), -1, mAddCategoryEditText, positiveListener, negativeListener);
    }
    DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
    	
    	@Override
    	public void onClick(DialogInterface dialog, int which) {
    	}
    };
    
    DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(mAddCategoryEditText != null && mCircle != null) {
				final String text = mAddCategoryEditText.getText().toString().trim();
				if(StringUtil.isValidString(text)) {
					addCategory(mCircle.circleid, text);
				}else {
					Log.d(TAG, "input text is null");
				}
			}
		}
	};
	
	boolean isAddCategory;
    Object mLockAddCategory = new Object();
    protected void addCategory(final long circleId, final String categoryName) {
        if (!ToastUtil.testValidConnectivity(mActivity)) {
            Log.i(TAG, "syncPublicCircleInfo, ignore while no connection.");
            return;
        }
        
    	if (isAddCategory == true) {
    		ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
    		return;
    	}
    	
    	synchronized (mLockAddCategory) {
    		isAddCategory = true;
    	}
    	//Show progress dialog
    	showProcessDialog(R.string.string_in_processing, false, true, true);
        
    	asyncQiupu.addCategory(AccountServiceUtils.getSessionID(), circleId, categoryName, new TwitterAdapter() {
    		public void addCategory(ArrayList<InfoCategory> infocategory) {
    			Log.d(TAG, "finish addCategory" );
    			if(infocategory != null) {
    				if(mCircle != null) {
    					if(mCircle.categories != null) {
    						mCircle.categories.addAll(infocategory);
    					}else {
    						mCircle.categories = infocategory;
    					}
    				}else {
    					mCircle = new UserCircle();
    					mCircle.categories = infocategory;
    				}
    				orm.insertCategories(infocategory);
    			}
    			
    			Message msg = mHandler.obtainMessage(ADD_CATEGORY_END);
    			msg.getData().putBoolean(RESULT, true);
    			msg.sendToTarget();
    			synchronized (mLockAddCategory) {
    				isAddCategory = false;
    			}
    		}
    		
    		public void onException(TwitterException ex, TwitterMethod method) {
    		    synchronized (mLockAddCategory) {
    		    	isAddCategory = false;
    			}
    			Message msg = mHandler.obtainMessage(ADD_CATEGORY_END);
    			msg.getData().putBoolean(RESULT, false);
    			msg.getData().putInt(ERROR_CODE, ex.getStatusCode());
    			msg.sendToTarget();
    		}
    	});
    }
    
    private boolean switchCategory(long categoryId) {
    	if(mPendingLoadCategoryId == categoryId) {
    		return false;
    	}
    	
    	if(mIsDirty) {
    		String oldfeed_sfile;
    		if(categoryId > 0) {
    			oldfeed_sfile = QiupuHelper.posts_public + AccountServiceUtils.getBorqsAccountID() + mPendingLoadCategoryId + mCircle.circleid ;
    		}else {
    			oldfeed_sfile = super.getNewsfile();
    		}

            StreamCacheManager.addCache(oldfeed_sfile, mPosts);
    		serialization(oldfeed_sfile);
    		mIsDirty = false;
//    	}else {
//    		for(Stream stream : mPosts) {
//    			stream.despose();
//    			stream = null;
//    		}
    	}
    	
    	mPendingLoadCategoryId = categoryId;
//        mMetaData.mCircleId = circleId;
        if(categoryId > 0) {
        	newsfeed_sfile = QiupuHelper.posts_public + AccountServiceUtils.getBorqsAccountID() + mPendingLoadCategoryId + mCircle.circleid;
        }else {
        	newsfeed_sfile = super.getNewsfile();
        }

//        new SwitchCircleTask().execute();
        mPosts.clear();
        notifyDataSetChanged();
        new DeSerializationTask().execute();

        return true;
    }
    
    private View.OnClickListener moreActionClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			showMoreStreamOptions();
		}
	};
	
	private String[] mDialogItems;
	
	private void showMoreStreamOptions() {
		mDialogItems = buildDialogItemContent();
        String dialogTitle = getActivity().getString(R.string.more_action);
        DialogUtils.showItemsDialog(getActivity(), dialogTitle, -1, mDialogItems, mChooseItemClickListener);
    }
	
	private String[] buildDialogItemContent() {
        ArrayList<String> actionList = new ArrayList<String>();
        final long myId = AccountServiceUtils.getBorqsAccountID();
        
        if(PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
        	if(PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group) ||
        			PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group)) {
        		actionList.add(getString(R.string.public_circle_invite));
        	}else {
        		if(UserCircle.VALUE_ALLOWED == mCircle.mGroup.can_member_invite) {
        			actionList.add(getString(R.string.public_circle_invite));
                }	
        	}
        }
        
		if(mCircle.mGroup != null) {
			if(mCircle.mGroup.viewer_can_update) {
				actionList.add(getString(R.string.edit_string));
			}
			if(mCircle.mGroup.formal == UserCircle.circle_free) {
				Log.d(TAG, "this circle is free circle, no need show pageitem");
			}else {
				actionList.add(getString(R.string.page_label));
			}
			
			if(PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
				if(PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group)
						|| PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group)) {
					if(mCircle.mGroup.pageid <=0 && (mCircle.mGroup.formal == UserCircle.circle_top_formal || mCircle.mGroup.formal == UserCircle.circle_sub_formal)) {
						actionList.add(getString(R.string.circle_as_page));
					}
				}
				if(mCircle.mGroup.viewer_can_destroy) {
					actionList.add(getString(R.string.delete));
	    		}
				if(mCircle.mGroup.viewer_can_quit) {
					actionList.add(getString(R.string.circle_exit_label));
	    			mIsDirectExit = true;
	    		}else {
	    			if(PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group)
	    					|| PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group)) {
	    				if(mCircle.mGroup.can_member_quit == UserCircle.VALUE_ALLOWED) {
	    					actionList.add(getString(R.string.circle_exit_label));
	    					mIsDirectExit = false;
	    				}
	    			}
	    		}
				
				actionList.add(getString(R.string.public_circle_receive_set));
			}
		}
		
		actionList.add(getString(R.string.public_circle_shortcut));
		String[] dialogItems = new String[actionList.size()];
		return actionList.toArray(dialogItems);
	}
	
	private DialogInterface.OnClickListener mChooseItemClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            final String item = mDialogItems[which];
            if (item.equals(getString(R.string.edit_string))) {
            	showEditGroupInfoDialog();
            } else if (item.equals(getString(R.string.page_label))) {
            	gotoPageDetail();
            } else if (item.equals(getString(R.string.circle_as_page))) {
            	if(mCallBackListener != null) {
            		mCallBackListener.circleToPage(mCircle);
            	}
            } else if (item.equals(getString(R.string.delete))) {
            	if(mCallBackListener != null) {
            		mCallBackListener.deleteCircle();
            	}
            } else if (item.equals(getString(R.string.circle_exit_label))) {
            	if(mCallBackListener != null) {
					mCallBackListener.exitCircle(mIsDirectExit);
				}
            } else if (item.equals(getString(R.string.public_circle_receive_set))) {
            	IntentUtil.gotoEditPublicCircleActivity(mActivity, mCircle.name, mCircle, EditPublicCircleActivity.edit_type_receive_set);
            } else if (item.equals(getString(R.string.public_circle_shortcut))) {
            	if(mCallBackListener != null) {
            		mCallBackListener.createShortcut();
            	}
            } else if(item.equals(getString(R.string.public_circle_invite))){
            	gotoPickCircleUserActivity();
            } else {
                Log.d(TAG, "Dialog click listener, unexpected item: " + item);
            }
        }
    };
    
//    @Override
//    protected void onScrollingStateIdleView(boolean toShow, View view) {
//    	if(PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
//    		super.onScrollingStateIdleView(toShow, view);
//    	}else {
//    		view.setVisibility(View.GONE);
//    	}
//    };
    
    @Override
    public void onScrollingBottomView(boolean toShow, View view) {
    	if(mCircle != null && mCircle.mGroup != null && PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
    		super.onScrollingBottomView(toShow, view);
    	}else {
    		view.setVisibility(View.GONE);
    	}
    };
    
    @Override
    protected void onListViewRefresh() {
    	super.onListViewRefresh();
    	if(mIsAreadyLoadCircle == false) {
    		mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO).sendToTarget();    		
    	}
    }

	@Override
	public void refreshUi() {
		mCircle = orm.queryOneCircleWithGroup(mCircle.circleid);
		refreshCircleInfoUi();		
	}
}
