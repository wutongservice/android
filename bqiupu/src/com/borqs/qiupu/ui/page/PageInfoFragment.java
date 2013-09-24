package com.borqs.qiupu.ui.page;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import twitter4j.AsyncQiupu;
import twitter4j.Circletemplate;
import twitter4j.PageInfo;
import twitter4j.QiupuSimpleUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.ExpandAnimation;
import com.borqs.common.ProfileHeadSourceItem;
import com.borqs.common.listener.PageActionListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.HorizontalLinearLayoutView;
import com.borqs.common.view.MyLinkMovementMethod;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.StreamListFragment;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.FriendsListActivity;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class PageInfoFragment extends StreamListFragment implements PageActionListener{
	private final static String TAG = "PageInfoFragment";
	private Activity mActivity; 
	
	private Handler mHandler;
	private QiupuORM orm;
	private AsyncQiupu asyncQiupu;
	private PageInfoFragmentListenerCallBack mCallBackListener;
	private TextView mPageTitle;
	private ImageView mProfile_img_ui;
	
    private PageInfo mPage;
    private static final String RESULT = "result";
    private static final String ERRORMSG = "errormsg";
    private ProgressDialog mprogressDialog;
    
    private View mContentView;
    
    private File mCurrentPhotoFile;
    private Bitmap photo ;
    private ImageView mCover;
    private static final String PAGE_ID = "PAGE_ID";
    public static final int TYPE_TEXT  = 0;
    public static final int PHOTO_PICKED_WITH_DATA = 3021;
    public static final int CAMERA_WITH_DATA = 3023;
    
    private View mActionView;
    private TextView mFollow;
    private TextView mUnFollow;
    private TextView mPageFans;
    private HorizontalLinearLayoutView mSourceView;
    private TextView mPage_circle;
    private View mHorizontalview;
    
    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
    	super.onAttach(activity);
    	mActivity = activity;

        if (mActivity instanceof PageInfoFragmentListenerCallBack) {
        	mCallBackListener = (PageInfoFragmentListenerCallBack)activity;
        	mCallBackListener.getPageInfoFragment(this);
        	mPage = mCallBackListener.getPageInfo();
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
    	super.onCreate(savedInstanceState);
    	QiupuHelper.registerPageListener(getClass().getName(), this);
    	parserSavedState(savedInstanceState);
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
    	mContentView = inflater.inflate(R.layout.page_info_headview, null, false);
    	mPageTitle = (TextView) mContentView.findViewById(R.id.id_page_title);
    	mPageTitle.setMovementMethod(MyLinkMovementMethod.getInstance());
    	mProfile_img_ui =  (ImageView) mContentView.findViewById(R.id.page_logo);
    	mCover = (ImageView) mContentView.findViewById(R.id.page_cover);
    	mPageFans = (TextView) mContentView.findViewById(R.id.id_page_fan_count);
    	
    	mActionView = mContentView.findViewById(R.id.id_action_fl);
    	mActionView.setOnClickListener(actionClickListener);
    	
    	mFollow = (TextView) mContentView.findViewById(R.id.follow_btn);
    	mUnFollow = (TextView) mContentView.findViewById(R.id.unfollow_btn);
    	
    	mSourceView = (HorizontalLinearLayoutView) mContentView.findViewById(R.id.source_view);
    	mPage_circle = (TextView) mContentView.findViewById(R.id.page_circle_label);
    	mCover.setOnClickListener(gotoDetailClickListener);
    	mPageTitle.setOnClickListener(gotoDetailClickListener);
    	
    	ImageView dropDownView = (ImageView) mContentView.findViewById(R.id.dropdown_icon);
    	dropDownView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				refreshHorizontalLayout();
			}
		});
        
    	mPage_circle.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(mPage.creatorId == AccountServiceUtils.getBorqsAccountID() 
						|| mPage.in_associated_circle) {
					gotoPageCircle();
				}
			}
		});
    	return mContentView;
    }
    
    private void parserSavedState(Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            if(mPage == null) {
            	mPage = new PageInfo();
            }
            
            mPage.page_id = savedInstanceState.getLong(PAGE_ID);
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
        refreshPageInfoUi();
        mHandler.obtainMessage(GET_PAGE_INFO).sendToTarget();
    }

    private void refreshPageInfoUi() {
    	if(mPage != null && isDetached() == false && getActivity() != null) {
    	    refreshHeadUi();
    	    refreshProfileIcon();
    	    refreshActionBtn();
        	if(mCallBackListener != null) {
				mCallBackListener.refreshPageInfo(mPage);
			}
		}
    }

    private void refreshProfileIcon() {
   	 if(mPage.viewer_can_update) {
            findFragmentViewById(R.id.icon_camera).setVisibility(View.VISIBLE);
            mProfile_img_ui.setOnClickListener(editProfileImageListener);
        }else {
       	 findFragmentViewById(R.id.icon_camera).setVisibility(View.GONE);
            mProfile_img_ui.setOnClickListener(null);
        }
   }
    
    private void refreshHeadUi() {
    	if(QiupuHelper.isZhCNLanguage(mActivity)) {
    		if(StringUtil.isValidString(mPage.name)) {
    			mPageTitle.setText(mPage.name);
    		}else {
    			mPageTitle.setText(mPage.name_en);
    		}
    	}else {
    		if(StringUtil.isValidString(mPage.name_en)) {
    			mPageTitle.setText(mPage.name_en);
    		}else {
    			mPageTitle.setText(mPage.name);
    		}
    	}
    	
    	if(StringUtil.isValidString(mPage.cover_url)) {
    		setViewIcon(mPage.cover_url, mCover, false);
    	}else {
    		mCover.setImageResource(R.drawable.event_default_cover);
    	}
    	
    	if(StringUtil.isValidString(mPage.logo_url)) {
    		setViewIcon(mPage.logo_url, mProfile_img_ui, false);
    	}else {
    		mProfile_img_ui.setImageResource(R.drawable.default_public_circle);
    	}
    	mPageFans.setText(String.format(getResources().getString(R.string.page_fans_count), mPage.followers_count));
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPage != null) {
            outState.putLong(PAGE_ID, mPage.page_id);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
    	QiupuHelper.unregisterPageListener(getClass().getName());
    	super.onDestroy();
    }
    
    private final static int GET_PAGE_INFO = 101;
    private final static int GET_PAGE_INFO_END = 102;
    private final static int EDIT_PROFILE_IMAGE_END = 104;
    private final static int FOLLOW_ACTION_END = 105;
	private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case GET_PAGE_INFO: {
            	syncPageInfo(mPage.page_id);
            	break;
            }
            case GET_PAGE_INFO_END: {
            	end();
            	boolean ret = msg.getData().getBoolean(RESULT, false);
            	if (ret == true) {
            	    refreshPageInfoUi();
            	    mPage_circle.setVisibility(View.VISIBLE);
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
                    QiupuHelper.updatePageActivityUI(mPage);
                }else {
                    Log.d(TAG, "edit profile iamge end ");
                    ToastUtil.showShortToast(mActivity, mHandler, R.string.toast_update_failed);
                }
                break;
            }
            case FOLLOW_ACTION_END: {
            	 try {
                     mprogressDialog.dismiss();
                     mprogressDialog = null;
                 } catch (Exception ne) { }
            	 if(msg.getData().getBoolean(RESULT, false)) {
            		 refreshActionBtn();
            		 QiupuHelper.updatePageActivityUI(mPage);
            	 }else {
            		 ToastUtil.showOperationFailed(mActivity, mHandler, true);
            	 }
				break;
			}
            }
        }
    }
    
    boolean inGetPageInfo;
    Object mLockGetPageInfo = new Object();
    public void syncPageInfo(final long pageId) {
        if (!ToastUtil.testValidConnectivity(mActivity)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }
        
    	if (inGetPageInfo == true) {
    		ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
    		return;
    	}
    	
    	synchronized (mLockGetPageInfo) {
    		inGetPageInfo = true;
    	}
    	begin();
    	
    	asyncQiupu.syncPageInfo(AccountServiceUtils.getSessionID(), pageId, new TwitterAdapter() {
    		public void syncPageInfo(PageInfo pageinfo) {
    			Log.d(TAG, "finish syncPageInfo=" + pageinfo.toString());
    			
    			mPage = pageinfo;
    			if(pageinfo.creatorId == AccountServiceUtils.getBorqsAccountID() || pageinfo.followed) {
    				orm.insertOnePage(pageinfo);
    			}
    			
    			Message msg = mHandler.obtainMessage(GET_PAGE_INFO_END);
    			msg.getData().putBoolean(RESULT, true);
    			msg.sendToTarget();
    			synchronized (mLockGetPageInfo) {
    				inGetPageInfo = false;
    			}
    		}
    		
    		public void onException(TwitterException ex, TwitterMethod method) {
    		    synchronized (mLockGetPageInfo) {
    		    	inGetPageInfo = false;
    			}
    			Message msg = mHandler.obtainMessage(GET_PAGE_INFO_END);
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
            			
            			editPageLogo(new File(QiupuHelper.getTmpCachePath()+"screenshot.png"));
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
    
    Object mEditPageLogoLock = new Object();
    boolean inEditPageLogoProcess;
    private void editPageLogo(File file){
        synchronized(mEditPageLogoLock)
        {
            if(inEditPageLogoProcess == true)
            {
                ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
                return ;
            }
        }
        
        showProcessDialog(R.string.edit_profile_update_dialog, false, true, true);
        
        synchronized(mEditPageLogoLock)
        {
        	inEditPageLogoProcess = true;          
        }
        asyncQiupu.editPageLogo(AccountServiceUtils.getSessionID(),mPage.page_id, file, new TwitterAdapter() {
            public void editPageLogo(PageInfo pageInfo) {
                Log.d(TAG, "finish edit public circle image " + pageInfo.toString());
                mPage = pageInfo;
                if(pageInfo != null) {
                	orm.insertOnePage(pageInfo);
                }
                	
                Message msg = mHandler.obtainMessage(EDIT_PROFILE_IMAGE_END);               
                msg.getData().putBoolean(BasicActivity.RESULT, true);
                msg.sendToTarget();
                synchronized(mEditPageLogoLock)
                {
                	inEditPageLogoProcess = false;         
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                
                synchronized(mEditPageLogoLock)
                {
                	inEditPageLogoProcess = false;         
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
            DialogUtils.showItemsDialog(mActivity, getResources().getString(R.string.select_circle_photo), 0, items,
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
    
    public void refreshPageInfo() {
    	super.loadRefresh();
    	mHandler.obtainMessage(GET_PAGE_INFO).sendToTarget();
    }
    
	public interface PageInfoFragmentListenerCallBack {
		public void getPageInfoFragment(PageInfoFragment fragment);
		public PageInfo getPageInfo();
		public void refreshPageInfo(PageInfo page);
		public void gotoProfileDetail();
	}

	@Override
	public void refreshpage(PageInfo info) {
		PageInfo tmpinfo = orm.queryOnePage(mPage.page_id);
		if(tmpinfo != null) {
			mPage = tmpinfo;	
			refreshPageInfoUi();
		}
	}
	
	private void refreshActionBtn() {
		if(mPage != null) {
			if(mPage.creatorId == AccountServiceUtils.getBorqsAccountID()) {
				mActionView.setVisibility(View.GONE);
			}else {
				mActionView.setVisibility(View.VISIBLE);
				if(mPage.followed) {
					mUnFollow.setVisibility(View.VISIBLE);
					mFollow.setVisibility(View.GONE);
					mActionView.setBackgroundResource(R.drawable.btn_unfollow_bg);
					mUnFollow.setTextColor(getResources().getColor(android.R.color.black));
				}else {
					mUnFollow.setVisibility(View.GONE);
					mFollow.setVisibility(View.VISIBLE);
					mActionView.setBackgroundResource(R.drawable.profile_add_circle_bg);
					mFollow.setTextColor(getResources().getColor(android.R.color.white));
				}
			}
		}
    }
	
	View.OnClickListener actionClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			followPages(mPage.page_id, !mPage.followed);
		}
	};
	
	DialogInterface.OnClickListener followOkListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			followPages(mPage.page_id, !mPage.followed);
		}
	};
	
	boolean inFollowPage = false;
    Object  mFollowLock = new Object();
	private void followPages(final long pageid, final boolean isfollow) {

		synchronized(mFollowLock) {
            if(inFollowPage == true) {
                Log.d(TAG, "in doing get loadSearchAPPs data");
                return;
            }
        }
        synchronized(mFollowLock) {
        	inFollowPage = true;
        }
        
        showProcessDialog(R.string.edit_profile_update_dialog, false, true, true);
		asyncQiupu.followPage(AccountServiceUtils.getSessionID(), pageid, isfollow, new TwitterAdapter() {
			public void followPage(PageInfo pageinfo) {
				Log.d(TAG, "finish followPage : " + pageinfo.page_id);
				if(mPage != null) {
					mPage.followed = pageinfo.followed;
				}
				if(isfollow) {
					orm.insertOnePage(pageinfo);
				}else {
					orm.deletePageByPageId(pageid);
				}
				synchronized(mFollowLock) {
					inFollowPage = false;
				}
				Message msg = mHandler.obtainMessage(FOLLOW_ACTION_END);
				msg.getData().putBoolean(RESULT, true);
				msg.sendToTarget();
			}

			public void onException(TwitterException ex,TwitterMethod method) {
				synchronized(mFollowLock) {
					inFollowPage = false;
		        }
				Message msg = mHandler.obtainMessage(FOLLOW_ACTION_END);
	            msg.getData().putBoolean(RESULT, false);
	            msg.sendToTarget();
			}
		});
	}
	
	View.OnClickListener gotoDetailClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(mCallBackListener != null)
				mCallBackListener.gotoProfileDetail();
		}
	};
	
	private void refreshHorizontalLayout() {
    	mHandler.post(new Runnable() {
			@Override
			public void run() {
				if(mHorizontalview == null) {
					mHorizontalview = findFragmentViewById(R.id.share_source_view);
					((LinearLayout.LayoutParams)(mHorizontalview.getLayoutParams())).bottomMargin = -100;
				}
//				if(sourceView.getVisibility() == View.VISIBLE) {
//					ExpandAnimation expandAni = new ExpandAnimation(sourceView, 250);
//					// Start the animation on the toolbar
//					sourceView.startAnimation(expandAni);
//				}else {
					ArrayList<ProfileHeadSourceItem> items = new ArrayList<ProfileHeadSourceItem>();
					ProfileHeadSourceItem item = new ProfileHeadSourceItem();
					item.mIconSource = R.drawable.profile_detail_icon;
					item.mLabel = getString(R.string.profile_detail_info_title);
					item.mClickListener = gotoDetailClickListener;
					items.add(item);
					
					item = new ProfileHeadSourceItem();
					item.mIconSource = R.drawable.share_picture;
					item.mLabel = getString(R.string.home_album);
					item.mClickListener = new OnClickListener() {
						@Override
						public void onClick(View v) {
							if(QiupuHelper.isZhCNLanguage(mActivity)) {
								IntentUtil.startAlbumIntent(mActivity, mPage.page_id, mPage.name);
							}else {
								IntentUtil.startAlbumIntent(mActivity, mPage.page_id, mPage.name_en);
							}
						}
					}; 
					items.add(item);
					
					item = new ProfileHeadSourceItem();
					item.mIconSource = R.drawable.profile_votting_icon;
					item.mLabel = getString(R.string.poll);
					item.mClickListener = new OnClickListener() {
						@Override
						public void onClick(View v) {
							if(mPage.followed) {
								if(QiupuHelper.isZhCNLanguage(mActivity)) {
									IntentUtil.startPollIntent(mActivity, mPage.page_id, mPage.name, 1, true);
								}else {
									IntentUtil.startPollIntent(mActivity, mPage.page_id, mPage.name_en, 1, true);
								}
							}else {
								Log.d(TAG, "you have not follow the page.");
								DialogUtils.showConfirmDialog(mActivity, getString(R.string.following), getString(R.string.check_follow_msg_label), followOkListener);
							}
						}
					}; 
					items.add(item);
					
					item = new ProfileHeadSourceItem();
					item.mIconSource = R.drawable.profile_event_icon;
					item.mLabel = getString(R.string.event);
					item.mClickListener = new OnClickListener() {
						@Override
						public void onClick(View v) {
							if(mPage.followed) {
								if(QiupuHelper.isZhCNLanguage(mActivity)) {
									IntentUtil.startCircleEventList(mActivity, mPage.page_id, mPage.name);
								}else {
									IntentUtil.startCircleEventList(mActivity, mPage.page_id, mPage.name_en);
								}
							}else {
								Log.d(TAG, "you have not follow the page.");
								DialogUtils.showConfirmDialog(mActivity, getString(R.string.following), getString(R.string.check_follow_msg_label), followOkListener);
							}
							
						}
					}; 
					items.add(item);
					
					if(items.size() > 0) {
						mSourceView.setProfileDataArray(items);
						ExpandAnimation expandAni = new ExpandAnimation(mHorizontalview, 250);
						// Start the animation on the toolbar
						mHorizontalview.startAnimation(expandAni);
					}
				}
//			}
		});
    }
	
	DialogInterface.OnClickListener createPublicCircleListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			gotoCreateCircles();
		}
	};
	
	public void gotoCreateCircles() { 
		Intent intent = new Intent(mActivity, CreateCircleMainActivity.class);
    	intent.putExtra(PageInfo.PAGE_ID, mPage.page_id);
    	if(mPage.associated_id > 0 && TextUtils.isEmpty(mPage.free_circle_ids)) {
    		intent.putExtra(CreateCircleMainActivity.CREATE_STATUS, CreateCircleMainActivity.CREATE_STATUS_ONLYFREE);
    	} else if(mPage.associated_id <=0 && !TextUtils.isEmpty(mPage.free_circle_ids)) {
    		intent.putExtra(CreateCircleMainActivity.CREATE_STATUS, CreateCircleMainActivity.CREATE_STATUS_ONLYFORMAL);
    	}
    	intent.putExtra(CreateCircleMainActivity.SUBTYPE, Circletemplate.SUBTYPE_TEMPLATE);
    	startActivity(intent);
	}
	
	private void gotoPageCircle() {
		ArrayList<QiupuSimpleUser> list = new ArrayList<QiupuSimpleUser>();
		if(mPage.associatedCircle != null) {
			QiupuSimpleUser tmpUser = new QiupuSimpleUser();
			tmpUser.uid = mPage.associatedCircle.circleid;
			tmpUser.nick_name = mPage.associatedCircle.name;
			tmpUser.profile_image_url = mPage.associatedCircle.profile_image_url;
			list.add(tmpUser);
		}
		if(mPage.freeCircles != null && mPage.freeCircles.size() > 0) {
			for(int i=0; i<mPage.freeCircles.size(); i++) {
				UserCircle tmpCircle = mPage.freeCircles.get(i);
				QiupuSimpleUser tmpUser = new QiupuSimpleUser();
				tmpUser.uid = tmpCircle.circleid;
				tmpUser.nick_name = tmpCircle.name;
				tmpUser.profile_image_url = tmpCircle.profile_image_url;
				list.add(tmpUser);
			}
		}
		
		if(list.size() > 1) {
			FriendsListActivity.showUserList(mActivity, getString(R.string.user_circles), list);
		}else if(list.size() == 1) {
			QiupuSimpleUser tmpinfo = list.get(0);
			if(tmpinfo.uid > 0) {
				UserCircle circle = new UserCircle();
				circle.circleid = tmpinfo.uid;
				circle.name = tmpinfo.nick_name;
				IntentUtil.startPublicCircleDetailIntent(mActivity, circle);
			}
		}else if(list.size() <= 0) {
			if(mPage.creatorId == AccountServiceUtils.getBorqsAccountID()) {
				DialogUtils.showConfirmDialog(mActivity, getString(R.string.create_public_circle_title), getString(R.string.create_circle_msg_label), createPublicCircleListener);
			}
			ToastUtil.showShortToast(mActivity, mHandler, R.string.page_have_no_circle_label);
		}
	}
}
