package com.borqs.qiupu.fragment;

import java.io.File;

import twitter4j.AsyncQiupu;
import twitter4j.Company;
import twitter4j.QiupuUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.MyLinkMovementMethod;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.PickContactBaseFragment.invitePepoleListeners;
import com.borqs.qiupu.ui.company.CompanyDepListActivity;
import com.borqs.qiupu.util.ToastUtil;

public class CompanyFragment extends StreamListFragment implements UsersActionListner, invitePepoleListeners {
	private final static String TAG = "CompanyFragment";
	private Activity mActivity; 
	
	private Handler mHandler;
	private QiupuORM orm;
	private AsyncQiupu asyncQiupu;
	private CompanyFragmentListenerCallBack mCallBackListener;
	private TextView mEvent_title;
	private ImageView mProfile_img_ui;
	
    private Company mCompany;
    private static final String RESULT = "result";
    private static final String ERRORMSG = "errormsg";
    private ProgressDialog mprogressDialog;
    
    private TextView mInviteBtn;
    private TextView mApplyBtn;
    private View mActionView;
    
    private View mContentView;
    private TextView mPrivacyTextView;
    private View layout_top_post;
    private TextView tv_top_post;
    
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

        if (mActivity instanceof CompanyFragmentListenerCallBack) {
        	mCallBackListener = (CompanyFragmentListenerCallBack)activity;
        	mCallBackListener.getCompanyInfoFragment(this);
        	mCompany = mCallBackListener.getCompanyInfo();
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
    	super.onCreate(savedInstanceState);
    	parserSavedState(savedInstanceState);
    	QiupuHelper.registerUserListener(getClass().getName(), this);
    	PickContactBaseFragment.registerUserListener(getClass().getName(), this);
    	orm = QiupuORM.getInstance(mActivity);
		asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
		mHandler = new MainHandler();
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
    	mContentView = inflater.inflate(R.layout.company_info_headview, null, false);
    	mEvent_title = (TextView) mContentView.findViewById(R.id.id_event_title);
    	mEvent_title.setMovementMethod(MyLinkMovementMethod.getInstance());
    	mProfile_img_ui =  (ImageView) mContentView.findViewById(R.id.creator_icon);
    	mCover = (ImageView) mContentView.findViewById(R.id.select_cover);
    	mTime = (TextView) mContentView.findViewById(R.id.id_time);
//    	mPrivacyTextView = (TextView) mContentView.findViewById(R.id.id_privacy);
//    	
//    	mInviteBtn = (TextView) mContentView.findViewById(R.id.invite_btn);
////    	mInviteBtn.setOnClickListener(invitePeopleClickListener);
////    	
//    	mApplyBtn = (TextView) mContentView.findViewById(R.id.apply_btn);
////    	mApplyBtn.setOnClickListener(applyPeopleClickListener);
//    	
//    	mActionView = mContentView.findViewById(R.id.id_action_fl);
//    	mActionView.setOnClickListener(actionClickListener);
//    	
//    	layout_top_post = mContentView.findViewById(R.id.layout_top_post);
    	tv_top_post = (TextView) mContentView.findViewById(R.id.id_top_post);
    	tv_top_post.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              IntentUtil.startTopPostIntent(mActivity, mCompany.department_id,getString(R.string.label_bulletin),false);
              
              
//              Intent intent = new Intent(mActivity,CompanyDepListActivity.class);
//              intent.putExtra(Company.COMPANY_ID, String.valueOf(mCompany.id));
//              startActivity(intent);
              
              
          }
      });
//        View head_rl = mContentView.findViewById(R.id.head_rl);
//        head_rl.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
////				final View bodyInfo = findFragmentViewById(R.id.body_ll);
////				if(bodyInfo.getVisibility() == View.VISIBLE) {
////					bodyInfo.setVisibility(View.GONE);
////				}else {
////					bodyInfo.setVisibility(View.VISIBLE);
////				}
//			}
//		});
//        
        final ImageView goto_detail = (ImageView) mContentView.findViewById(R.id.goto_detail);
        final View parent = (View) goto_detail.getParent();
        parent.post( new Runnable() {
            // Post in the parent's message queue to make sure the parent
            // lays out its children before we call getHitRect()
            public void run() {
                final Rect hitRect = new Rect();
                goto_detail.getHitRect(hitRect);
				hitRect.top += 15;
				hitRect.bottom += 15;
				hitRect.left += 15;
				hitRect.right += 15;
                parent.setTouchDelegate( new TouchDelegate( hitRect , goto_detail));
            }
        });
        goto_detail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCallBackListener != null)
					mCallBackListener.gotoProfileDetail();
			}
		});
//        
//        if(mCircle != null && mCircle.mGroup != null && mCircle.mGroup.viewer_can_update) {
////            findFragmentViewById(R.id.icon_camera).setVisibility(View.VISIBLE);
////            mProfile_img_ui.setOnClickListener(editProfileImageListener);
//        }
    	return mContentView;
    }
    
    private void parserSavedState(Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            if(mCompany == null) {
            	mCompany = new Company();
            }
            
            mCompany.id = savedInstanceState.getLong(Company.COMPANY_ID);
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
        refreshCompanyInfoUi();
        mHandler.obtainMessage(GET_COMPANY_INFO).sendToTarget();
    }

    private void refreshCompanyInfoUi() {
    	if(mCompany != null && isDetached() == false && getActivity() != null) {
    	    refreshHeadUi();
    	    refreshActionBtn();
//    	    refreshTopPostInfo();
        	if(mCallBackListener != null) {
				mCallBackListener.refreshCompanyInfo(mCompany);
			}
		}
    }
    
//    private void refreshTopPostInfo() {
//        if(mCircle.mGroup != null && mCircle.mGroup.top_post_count > 0) {
//        	layout_top_post.setVisibility(View.VISIBLE);
//            if(TextUtils.isEmpty(mCircle.mGroup.top_post_name)) {
//                tv_top_post.setText(R.string.view_top);
//            }else {
//                tv_top_post.setText(mCircle.mGroup.top_post_name);
//            }
//            layout_top_post.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    IntentUtil.startTopPostIntent(mActivity, mCircle.circleid,mCircle.mGroup.top_post_name);
//                    
//                }
//            });
//        }else {
//        	layout_top_post.setVisibility(View.GONE);
//        }
//    }
    
    private void refreshActionBtn() {
//        if(mCircle.mGroup != null) {
//            if(PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
//            	if(PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group) ||
//            			PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group)) {
//            		mActionView.setVisibility(View.VISIBLE);
//            		mInviteBtn.setVisibility(View.VISIBLE);
//                    mApplyBtn.setVisibility(View.GONE);
//            	}else {
//            		if(UserCircle.VALUE_ALLOWED == mCircle.mGroup.can_member_invite) {
//            			mActionView.setVisibility(View.VISIBLE);
//                        mInviteBtn.setVisibility(View.VISIBLE);
//                        mApplyBtn.setVisibility(View.GONE);
//                    }else {
//                    	mActionView.setVisibility(View.GONE);
//                    	mInviteBtn.setVisibility(View.GONE);
//                        mApplyBtn.setVisibility(View.GONE);
//                    }	
//            	}
//            }else {
//            	if(UserCircle.JOIN_PERMISSION_DERECT_ADD == mCircle.mGroup.can_join
//            			|| UserCircle.JOIN_PREMISSION_VERIFY == mCircle.mGroup.can_join) {
//            		mActionView.setVisibility(View.VISIBLE);
//            		mApplyBtn.setVisibility(View.VISIBLE);
//                    mInviteBtn.setVisibility(View.GONE);
//            	}else {
//            		mActionView.setVisibility(View.GONE);
//            		mApplyBtn.setVisibility(View.GONE);
//                    mInviteBtn.setVisibility(View.GONE);
//            	}
//            }
//        }
    }
    
    private void refreshHeadUi() {
    	mEvent_title.setText(mCompany.name);
        if(!TextUtils.isEmpty(mCompany.large_logo_url)) {
        	setViewIcon(mCompany.large_logo_url, mProfile_img_ui);
        }
        mTime.setText(mCompany.website);
////        initImageUI(mCircle.profile_image_url);
//        if(mCircle.mGroup != null) {
//			if(mCircle.circleid == 14000000000L) {
//				mCover.setImageResource(R.drawable.borqs_cover);
//			}else if(StringUtil.isEmpty(mCircle.mGroup.coverUrl)) {
//				mCover.setImageResource(R.drawable.event_default_cover);
//			}else {
//				setViewIcon(mCircle.mGroup.coverUrl, mCover);
//			}
//			StringBuilder timeStr = new StringBuilder();
//			timeStr.append(DateUtil.converToRelativeTime(mActivity,
//					mCircle.mGroup.startTime));
//			if(mCircle.mGroup.endTime > 0) {
//				timeStr.append(" ~ ");
//				timeStr.append(DateUtil.converToRelativeTime(mActivity,
//	    					mCircle.mGroup.endTime));
//			}
//			
//			if(UserCircle.isPrivacyOpen(mCircle.mGroup)) {
//				mPrivacyTextView.setText(R.string.privacy_set_open);
//		    }else if(UserCircle.isPrivacyClosed(mCircle.mGroup)) { 
//		    	mPrivacyTextView.setText(R.string.privacy_set_closed);
//		    }else if(UserCircle.isPrivacySecret(mCircle.mGroup)) {
//		    	mPrivacyTextView.setText(R.string.privacy_set_secret);
//		    }
//		}
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mCompany != null) {
            outState.putLong(Company.COMPANY_ID, mCompany.id);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	QiupuHelper.unregisterUserListener(getClass().getName());
    	PickContactBaseFragment.unregisterUserListener(getClass().getName());
    }
    
    private final static int GET_COMPANY_INFO = 101;
    private final static int GET_COMPANY_INFO_END = 102;
    private final static int GET_PUBLIC_CIRCLE_INFO = 103;
    private final static int GET_PUBLIC_CIRCLE_INFO_END = 104;
    private final static int EDIT_PROFILE_IMAGE_END = 105;
	private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case GET_COMPANY_INFO: {
            	getCompanyInfo(mCompany.id);
            	break;
            }
            case GET_COMPANY_INFO_END: {
            	end();
            	boolean ret = msg.getData().getBoolean(RESULT, false);
            	if (ret == true) {
            		refreshCompanyInfoUi();
                } else {
                    ToastUtil.showShortToast(mActivity, mHandler, R.string.get_info_failed);
                }
            	break;
            }
//            case GET_PUBLIC_CIRCLE_INFO: {
//            	syncPublicCircleInfo(String.valueOf(mCircle.circleid), false, QiupuConfig.isEventIds(mCircle.circleid));
//            	break;
//            }
//            case GET_PUBLIC_CIRCLE_INFO_END: {
//            	end();
//            	boolean ret = msg.getData().getBoolean(RESULT, false);
//            	if (ret == true) {
//            	    refreshCircleInfoUi();
//                } else {
//                    ToastUtil.showShortToast(mActivity, mHandler, R.string.get_info_failed);
//                }
//            	break;
//            }
            case EDIT_PROFILE_IMAGE_END: {
//                try {
//                    mprogressDialog.dismiss();
//                    mprogressDialog = null;
//                } catch (Exception ne) { }
//                if(msg.getData().getBoolean(BasicActivity.RESULT, false)) {
//                    mCurrentPhotoFile.delete();
//                    mProfile_img_ui.setImageBitmap(photo);
//                    mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO).sendToTarget(); 
//                }else {
//                    Log.d(TAG, "edit profile iamge end ");
//                    ToastUtil.showShortToast(mActivity, mHandler, R.string.toast_update_failed);
//                }
                break;
            }
            }
        }
    }
    
	private void setViewIcon(final String url, final ImageView view) {
		ImageRun imagerun = new ImageRun(null, url, 0);
		imagerun.noimage = true;
		imagerun.addHostAndPath = true;
		imagerun.setRoundAngle=true;
		imagerun.setImageView(view);
		imagerun.post(null);
	}
    
    boolean inLoading;
    Object lockObj = new Object();
    public void getCompanyInfo(final long company_id) {
        if (!ToastUtil.testValidConnectivity(mActivity)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }
        
    	if (inLoading == true) {
    		ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
    		return;
    	}
    	
    	synchronized (lockObj) {
    		inLoading = true;
    	}
    	begin();
    	
    	asyncQiupu.getCompanyInfo(AccountServiceUtils.getSessionID(), company_id, new TwitterAdapter() {
    		
    		@Override
    		public void getCompanyInfo(Company company) {
    			if(mCompany == null) return;
    			mCompany = company;
//    			if(mCompany.id == 15000000016L) {
//    				mCompany.logo_url = "http://tp3.sinaimg.cn/2638952174/180/5621735674/1";
//    				mCompany.small_logo_url = "http://tp3.sinaimg.cn/2638952174/180/5621735674/1";
//    				mCompany.large_logo_url = "http://tp3.sinaimg.cn/2638952174/180/5621735674/1";
//    				mCompany.address = "北京市朝阳区酒仙桥路10号恒通商务园B23楼A座";
//    				mCompany.email = "service@borqs.com";
//    				mCompany.tel = "010-5975 6336";
//    				mCompany.website = "http://www.borqs.com/cn";
//    				mCompany.description = "BORQS/播思国际控股公司成立于2007年9月，是一家致力于为全球移动互联网领域的运营商、终端厂商及芯片制造商提供可定制化的智能终端软件平台和端到端服务平台解决方案的高新技术企业。公司总部设在中国，在美国、印度等地设有分支机构和研发中心。由包括凯旋创投、金沙江创 业投资、清华投资、Norwest Venture Partners和 Intel Capital Corporation及SK Telecom China Fund I L.P. 在内的多家具有实力的风险基金投资。播思也是谷歌/OHA（开放手机联盟）和TD产业联盟和TD论坛的主要成员";
//    			}
    			Message msg = mHandler.obtainMessage(GET_COMPANY_INFO_END);
    			msg.getData().putBoolean(RESULT, true);
    			msg.sendToTarget();
    			synchronized (lockObj) {
    				inLoading = false;
    			}
    		}
    		
    		public void onException(TwitterException ex, TwitterMethod method) {
    		    synchronized (lockObj) {
    		    	inLoading = false;
    			}
    			Message msg = mHandler.obtainMessage(GET_COMPANY_INFO_END);
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
//				applyInPublicCircle(String.valueOf(mCircle.circleid), "");
			}else {
				Log.d(TAG, "have no view ");
			}
		}
	};
    
	private void gotoPickCircleUserActivity() {
//    	Intent intent = new Intent(mActivity, InvitePeopleMainActivity.class);
//    	if(mCircle.mGroup != null) {
//    	    intent.putExtra(InviteSysUsersFragment.FILTER_IDS, mCircle.mGroup.invited_ids);
//    	}
//    	intent.putExtra(CircleUtils.CIRCLE_ID, mCircle.circleid);
//    	intent.putExtra(CircleUtils.CIRCLE_NAME, mCircle.name);
//    	startActivity(intent);
    }
	
    
    
    private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable){
    	mprogressDialog = DialogUtils.createProgressDialog(mActivity, 
    			resId, CanceledOnTouchOutside, Indeterminate, cancelable);
    	mprogressDialog.show();    	
    }
    
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//    	Log.d(TAG, "onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode);
//        switch (requestCode) {
//            case PHOTO_PICKED_WITH_DATA: {
//                if (mCurrentPhotoFile != null && mCurrentPhotoFile.exists()) {
//                    mCurrentPhotoFile.delete();
//                } 
//                photo = data.getParcelableExtra("data");
//                mCurrentPhotoFile = new File(QiupuHelper.getTmpCachePath()+"screenshot.png");
//                FileOutputStream fOut = null;
//                try {
//                    if (mCurrentPhotoFile.exists()) {
//                        mCurrentPhotoFile.delete();
//                    }                
//                    mCurrentPhotoFile.createNewFile();
//                    fOut = new FileOutputStream(mCurrentPhotoFile);                      
//                    photo.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//                    fOut.flush();
//                    fOut.close();
//
//                    editPublicCircleImage(new File(QiupuHelper.getTmpCachePath()+"screenshot.png"));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                //TODO
////                sharedImageUri = null;
//                break;  
//            }  
//            case CAMERA_WITH_DATA: {
//                tryCropProfileImage(Uri.fromFile(mCurrentPhotoFile));
//                break;  
//            }  
//        }
//    }

    private void tryCropProfileImage(Uri uri) {
        try {
            // start gallery to crop photo
            final Intent intent = QiupuHelper.getCropImageIntent(uri);
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {}
    }
    
//    Object mEditPublicImageLock = new Object();
//    boolean inEditImageProcess;
//    private void editPublicCircleImage(File file){
//        synchronized(mEditPublicImageLock)
//        {
//            if(inEditImageProcess == true)
//            {
//                ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
//                return ;
//            }
//        }
//        
//        showProcessDialog(R.string.edit_profile_update_dialog, false, true, true);
//        
//        synchronized(mEditPublicImageLock)
//        {
//            inEditImageProcess = true;          
//        }
//        asyncQiupu.editPublicCircleImage(AccountServiceUtils.getSessionID(),mCircle.circleid, file, new TwitterAdapter() {
//            public void editPublicCircleImage(boolean result) {
//                Log.d(TAG, "finish edit public circle image ");
//                Message msg = mHandler.obtainMessage(EDIT_PROFILE_IMAGE_END);               
//                msg.getData().putBoolean(BasicActivity.RESULT, result);
//                msg.sendToTarget();
//                synchronized(mEditPublicImageLock)
//                {
//                    inEditImageProcess = false;         
//                }
//            }
//
//            public void onException(TwitterException ex, TwitterMethod method) {
//                
//                synchronized(mEditPublicImageLock)
//                {
//                    inEditImageProcess = false;         
//                }
//                TwitterExceptionUtils.printException(TAG, "editUserProfileImage, server exception:", ex, method);
//
//                Message msg = mHandler.obtainMessage(EDIT_PROFILE_IMAGE_END);
//                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
//                msg.getData().putBoolean(BasicActivity.RESULT, false);
//                msg.sendToTarget();
//            }
//        });
//    }
    
    
//    boolean inGetPublicInfo;
//    Object mLockGetPublicInfo = new Object();
//    public void syncPublicCircleInfo(final String circleId, final boolean with_member) {
//        if (!ToastUtil.testValidConnectivity(mActivity)) {
//            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
//            return;
//        }
//        
//    	if (inGetPublicInfo == true) {
//    		ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
//    		return;
//    	}
//    	
//    	synchronized (mLockGetPublicInfo) {
//    		inGetPublicInfo = true;
//    	}
//    	begin();
//    	
//    	asyncQiupu.syncPublicCirclInfo(AccountServiceUtils.getSessionID(), circleId, with_member, false, new TwitterAdapter() {
//    		public void syncPublicCirclInfo(UserCircle circle) {
//    			Log.d(TAG, "finish syncPublicCirclInfo=" + circle.toString());
//    			
//    			mCircle = circle;
//    			if(mCircle.mGroup != null && PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
//    			    orm.insertOneCircle(mCircle);
//    			    importEventTocalendar();
//    			}
//    			
//    			Message msg = mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO_END);
//    			msg.getData().putBoolean(RESULT, true);
//    			msg.sendToTarget();
//    			synchronized (mLockGetPublicInfo) {
//    				inGetPublicInfo = false;
//    			}
//    		}
//    		
//    		public void onException(TwitterException ex, TwitterMethod method) {
//    		    synchronized (mLockGetPublicInfo) {
//    				inGetPublicInfo = false;
//    			}
//    			Message msg = mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO_END);
//    			msg.getData().putBoolean(RESULT, false);
//    			msg.sendToTarget();
//    		}
//    	});
//    }
    
    View.OnClickListener editProfileImageListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            String[] items = new String[] {
                    getString(R.string.take_photo),
                    getString(R.string.phone_album) };
            DialogUtils.showItemsDialog(mActivity, "", 0, items,
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
    
    public void refreshCompanyInfo() {
    	super.loadRefresh();
    	mHandler.obtainMessage(GET_COMPANY_INFO).sendToTarget();
    }
    
	public interface CompanyFragmentListenerCallBack {
		public void getCompanyInfoFragment(CompanyFragment fragment);
		public Company getCompanyInfo();
		public void refreshCompanyInfo(Company company);
		public void gotoProfileDetail();
	}

	@Override
	public void updateItemUI(QiupuUser user) {
		mCompany = orm.queryCompany(mActivity,mCompany.id);
		refreshCompanyInfoUi();
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
	
    @Override
    public void updateUi() {
        mHandler.obtainMessage(GET_COMPANY_INFO).sendToTarget(); //TODO invite people end,  then re-get circle info
    }
    
    
}
