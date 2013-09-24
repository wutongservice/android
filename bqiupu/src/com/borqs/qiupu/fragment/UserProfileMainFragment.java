package com.borqs.qiupu.fragment;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import twitter4j.QiupuUser;
import twitter4j.Stream;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.ExpandAnimation;
import com.borqs.common.ShareSourceItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.RefreshPostListener;
import com.borqs.common.listener.RefreshPostProfileImageListener;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.HorizontalLinearLayoutView;
import com.borqs.common.view.MyLinkMovementMethod;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.service.ShareSourceResultReceiver;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.PickCircleUserActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.DateUtil;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public final class UserProfileMainFragment extends StreamListFragment implements UsersActionListner, ShareSourceResultReceiver.ShareSourceServiceListener{
    private static final String TAG = "UserProfileMainFragment";

    private Activity mActivity;
    private ImageView profile_img_ui;
	private TextView  id_user_circle;
	private TextView  profile_user_status;
	private TextView  profile_publish_time;
	private TextView  mConcernCount;
	private TextView  mFollowerCount;
	private TextView  mFavoriteCount;
	private ImageView  profile_edit_img;
	private TextView tv_user_name;
	private TextView mImComposeView;
	private HorizontalLinearLayoutView mSourceView;
	private View mHorizontalScrollView;
	private View mConcernView;
	private View mFollowerView;
	private View mFavoriteView;
	private Handler mHandler;
	private QiupuUser mUser;
	private QiupuORM orm;
//	private AsyncQiupu asyncQiupu;
	private File mCurrentPhotoFile;
	private Bitmap photo ;
	private ProgressDialog mprogressDialog;
	private UserProfileMainFragmentCallBack mFragmentCallBack;
	public static final int CAMERA_WITH_DATA = 3023;
    public static final int PHOTO_PICKED_WITH_DATA = 3021;
    public static final int SELECT_USER_DATE = 3022;
    public static final String MAIL_URI = "mailto:";
    public static final String TEL_URI = "tel:";
    
    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach ");
        super.onAttach(activity);
        mActivity = activity;
        if (mActivity instanceof UserProfileMainFragmentCallBack) {
        	mFragmentCallBack = (UserProfileMainFragmentCallBack) activity;
        	mFragmentCallBack.getProfileInfoFragment(this);
            mUser = mFragmentCallBack.getUserInfo();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate ");
        super.onCreate(savedInstanceState);
        orm = QiupuORM.getInstance(mActivity);
		mHandler = new MainHandler();
//		asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
		QiupuHelper.registerUserListener(getClass().getName(), this);
		
    	if(mUser == null) {
    		mUser = new QiupuUser();
    	}
    	ShareSourceResultReceiver.registerServiceListener(getClass().getName(), this);
    		QiupuService.sendShareSourceBroadcast(mActivity, mUser.uid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView ");
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mListView.addHeaderView(initProfileHeadView(inflater));
        return view; 
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshUserInfoUI();
//        refreshShareSourceResult();
        mHandler.obtainMessage(GET_USER_DETAIL).sendToTarget();
        
    }

    @Override
    public void onResume() {
        Log.d(TAG, "");
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        QiupuHelper.unregisterUserListener(getClass().getName());
    }
    
//    private View findFragmentViewById(int id) {
//        if(mHeadView != null) {
//            return mHeadView.findViewById(id);
//        }
//        return null;
//    }
    
    private View initProfileHeadView(LayoutInflater inflater) {
    	View mHeadView = inflater.inflate(R.layout.bpc_user_detail_main_header, null, false);
    	initView(mHeadView);
    	return mHeadView;
    }
    
    private View initView(View headerView) {
    	profile_img_ui       = (ImageView)headerView.findViewById(R.id.profile_img_ui);
		id_user_circle      = (TextView) headerView.findViewById(R.id.id_user_circle);
		mConcernView = headerView.findViewById(R.id.concern_ll);
		mFollowerView = headerView.findViewById(R.id.follower_ll);
		mFavoriteView = headerView.findViewById(R.id.favorite_ll);
		
		mConcernCount         = (TextView) headerView.findViewById(R.id.id_concern_count);
		mFollowerCount        = (TextView) headerView.findViewById(R.id.id_followers_count);
		profile_user_status  = (TextView) headerView.findViewById(R.id.profile_user_status);
		profile_publish_time = (TextView) headerView.findViewById(R.id.profile_status_time);
		mFavoriteCount    = (TextView) headerView.findViewById(R.id.id_favourites_count);
		profile_edit_img     = (ImageView) headerView.findViewById(R.id.icon_camera);
		tv_user_name = (TextView) headerView.findViewById(R.id.user_name);
		id_user_circle.setVisibility(View.VISIBLE);
		tv_user_name.setMovementMethod(MyLinkMovementMethod.getInstance());
		mImComposeView = (TextView) headerView.findViewById(R.id.im_compose);
		
		mSourceView = (HorizontalLinearLayoutView) headerView.findViewById(R.id.source_view);
		mHorizontalScrollView = headerView.findViewById(R.id.share_source_view);
		((LinearLayout.LayoutParams)(mHorizontalScrollView.getLayoutParams())).bottomMargin = -100;
		mConcernView.setOnClickListener(userDisplayClickListener);
		mFollowerView.setOnClickListener(userDisplayClickListener);
		mFavoriteView.setOnClickListener(userDisplayClickListener);

        View appView = headerView.findViewById(R.id.applications_ll);
        View shareSourceView = headerView.findViewById(R.id.share_source_ll);
            if (null != appView) {
                appView.setVisibility(View.GONE);
            }
            shareSourceView.setVisibility(View.VISIBLE);
            shareSourceView.setOnClickListener(shareSourceClickListener);

		ImageView profile_more = (ImageView) headerView.findViewById(R.id.profile_more);
//		View profile_head = headerView.findViewById(R.id.profile_head);
		profile_more.setOnClickListener(gotoProfileDetailClickListener);
//		profile_head.setOnClickListener(gotoProfileDetailClickListener);
		
		id_user_circle.setOnClickListener(setcircleClick);
		setUI();
    	return headerView;
    }
    

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    View.OnClickListener shareSourceClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
//			if(mHorizontalScrollView.getVisibility() == View.VISIBLE) {
				ExpandAnimation expandAni = new ExpandAnimation(mHorizontalScrollView, 250);
				// Start the animation on the toolbar
				mHorizontalScrollView.startAnimation(expandAni);
//			}else {
//				mHorizontalScrollView.setVisibility(View.VISIBLE);
//			}
		}
	};
	
	View.OnClickListener gotoProfileDetailClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(mFragmentCallBack != null)
				mFragmentCallBack.gotoProfileDetailFragment();
		}
	};
	
    private void initImageUI(String image_url)
	{
		ImageRun imagerun = new ImageRun(mHandler, image_url, 0);
		imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
		imagerun.noimage = true;
	    imagerun.addHostAndPath = true;
	    imagerun.setRoundAngle=true;
        imagerun.setImageView(profile_img_ui);    
        imagerun.post(null);
	}
    
    private void setUI(){
		if(mUser.uid == AccountServiceUtils.getBorqsAccountID())
		{
			profile_edit_img.setVisibility(View.VISIBLE);
			id_user_circle.setVisibility(View.GONE);
		}
		else
		{
			id_user_circle.setVisibility(View.VISIBLE);
			profile_edit_img.setVisibility(View.GONE);
		}
		
		profile_img_ui.setImageResource(R.drawable.default_user_icon);//first set default icon
		refreshUserInfoUI();
	}

    private void setImComposeUI() {
//    	if(mUser.uid == AccountServiceUtils.getBorqsAccountID() || StringUtil.isValidString(mUser.circleId) == false) {
//    		mImComposeView.setVisibility(View.GONE);
//    	}else {
////    		if(StringUtil.isValidString(mUser.circleId)) {
//    			mImComposeView.setVisibility(View.VISIBLE);
//    			mImComposeView.setOnClickListener(new OnClickListener() {
//    				public void onClick(View v) {
//    					IntentUtil.startImComposeIntent(mActivity, mUser);
//    				}
//    			});
////    		}else {
////    			mImComposeView.setVisibility(View.GONE);
////    		}
//    	}
    }
    private void refreshProfileStatus(int statusId) {
        if(AccountServiceUtils.getBorqsAccountID() == mUser.uid) {
            profile_user_status.setText(Html.fromHtml("<font color='#5f78ab'> "+ getString(statusId) + "</font>"));
        }else {
            profile_user_status.setText(statusId);
        }
        refreshProfileStateTime(null);
    }
    private void refreshProfileStatus(final String status, final String time) {
        if(AccountServiceUtils.getBorqsAccountID() == mUser.uid) {
            profile_user_status.setText(Html.fromHtml("<font color='#5f78ab'> "+ status + "</font>"));
        }else {
            profile_user_status.setText(status);
        }
        refreshProfileStateTime(time);
    }

    private void refreshProfileStateTime(final String time) {
//        TextView timeView = (TextView)getActivity().findViewById(R.id.profile_status_time);
        if (null != profile_publish_time) {
            if (TextUtils.isEmpty(time)) {
            	profile_publish_time.setText("");
            } else {
            	profile_publish_time.setText(time);
            }

            profile_user_status.setOnClickListener(updateStatusClickListener);
            profile_publish_time.setOnClickListener(updateStatusClickListener);
        }
    }
    
    View.OnClickListener updateStatusClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showStatusOptionDialog(AccountServiceUtils.getBorqsAccountID() == mUser.uid);            
        }
    };
    
	private void refreshUserInfoUI() {
		if (mUser != null) {
			// set status ui
			if (StringUtil.isValidString(mUser.status)) {
				refreshProfileStatus(mUser.status,
						DateUtil.converToRelativeTime(getActivity(),
								mUser.status_time));
			} else {
				if (AccountServiceUtils.getBorqsAccountID() == mUser.uid) {
					refreshProfileStatus(R.string.my_profile_have_no_status);
				} else {
					refreshProfileStatus(R.string.other_profile_have_no_status);
				}
			}
			// set status click event
			profile_user_status.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					showStatusOptionDialog(AccountServiceUtils
							.getBorqsAccountID() == mUser.uid);
				}
			});
			initImageUI(mUser.profile_image_url);
			initUserInfoUI();
			setCircleUI();
			setImComposeUI();
			refreshShareSourceResult();
		}
	}
    
    private EditText mStatusEditText ;
    private CheckBox isSendPost;
    private void showUpdateStatusDialog(){
    	View selectview = LayoutInflater.from(mActivity).inflate(R.layout.edit_profile_status, null);
    	
    	isSendPost = (CheckBox) selectview.findViewById(R.id.isSendPost);
    	TextView profile_dialog_title = (TextView) selectview.findViewById(R.id.profile_dialog_title);
    	mStatusEditText = (EditText) selectview.findViewById(R.id.edit_content);
    	ImageView profile_status_commit = (ImageView) selectview.findViewById(R.id.profile_status_commit);
    	
    	profile_status_commit.setVisibility(View.GONE);
    	profile_dialog_title.setVisibility(View.GONE);
    	
    	mStatusEditText.setText(mUser.status);
    	mStatusEditText.setHint(R.string.update_my_status_hint);
        if (mStatusEditText.getText().length() > 0) {
            mStatusEditText.setSelection(0, mStatusEditText.getText().length());
        }
    	
    	DialogUtils.ShowDialogwithView(mActivity, getString(R.string.update_my_status_title), 0,
    			selectview, UpdateStatusOkListener, ChangeRequestCancelListener);
        mStatusEditText.requestFocus();
    }

    private void showStatusOptionDialog(boolean isMyself) {
        if (isMyself) {
            showUpdateStatusDialog();
        }
    }
    
    private void initUserInfoUI()
	{
		profile_img_ui.setOnClickListener(AccountServiceUtils.getBorqsAccountID() == mUser.uid ? userDisplayClickListener : setcircleClick);
		setDisplayNameUi();
		
//		if(mUser.friends_count > 0) {
//		    mConcernView.setOnClickListener(userDisplayClickListener);
//		}else {
//		    mConcernView.setOnClickListener(null);
//		}
//		if(mUser.followers_count > 0) {
//            mFollowerView.setOnClickListener(userDisplayClickListener);
//        }else {
//            mFollowerView.setOnClickListener(null);
//        }
//		if(mUser.favorites_count > 0) {
//		    mFavoriteView.setOnClickListener(userDisplayClickListener);
//        }else {
//            mFavoriteView.setOnClickListener(null);
//        }
        
		mConcernCount.setText(String.valueOf(mUser.friends_count));
		mFollowerCount.setText(String.valueOf(mUser.followers_count));
		mFavoriteCount.setText(String.valueOf(mUser.favorites_count));
	}
    
    
    private void setDisplayNameUi () {
        if(AccountServiceUtils.getBorqsAccountID() == mUser.uid) {
            tv_user_name.setText(mUser.nick_name);
            tv_user_name.setOnClickListener(null);
        }else {
        	tv_user_name.setText(mUser.nick_name);
            tv_user_name.setOnClickListener(null);
//            if(StringUtil.isValidString(mUser.circleId)) {
//                tv_user_name.setText(StringUtil.formatRemarkHtmlString(mUser.nick_name, showRemarkName() ));
//                tv_user_name.setOnClickListener(remarkClickListener);
//            }else {
//                tv_user_name.setText(mUser.nick_name);
//                tv_user_name.setOnClickListener(null);
//            }
        }
    }
    
    private void setCircleUI()
	{
		String tmpcircleName = "";
		
         // set circle text
		if(StringUtil.isValidString(mUser.circleId))
		{
//			circle_icon.setImageResource(R.drawable.icon_ingroups);
			if(mUser.circleName != null && mUser.circleName.length() > 0)
			{
				String localcircle =  CircleUtils.getCircleNameByCirlceId(mActivity, mUser.circleId);
				if(localcircle.length() > 0)
					tmpcircleName = localcircle + "," + mUser.circleName;
				else
					tmpcircleName = mUser.circleName;
			}
			else
			{
				tmpcircleName = CircleUtils.getCircleNameByCirlceId(mActivity, mUser.circleId);
			}
			id_user_circle.setBackgroundResource(R.drawable.profile_in_circle_bg);
			id_user_circle.setCompoundDrawablesWithIntrinsicBounds(
					getResources().getDrawable(
							R.drawable.profile_circle_icon), null,
					null, null);
			id_user_circle.setText(tmpcircleName);
			id_user_circle.setTextColor(Color.BLACK);
		}
		else
		{
//			circle_icon.setImageResource(R.drawable.icon_outgroups);
			id_user_circle.setTextColor(Color.WHITE);
//			id_user_circle.setCompoundDrawablesWithIntrinsicBounds(
//					getResources().getDrawable(
//							R.drawable.profile_add_icon), null,
//					null, null);
			id_user_circle.setBackgroundResource(R.drawable.profile_add_circle_bg);
			id_user_circle.setText(getString(R.string.add_to_circle));
		}
	}
    
    View.OnClickListener userDisplayClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            int viewid = v.getId();
            if (viewid == R.id.concern_ll) {
                if (mUser.uid == AccountServiceUtils.getBorqsAccountID()) {
                    IntentUtil.showMyConcerningPeople(mActivity);
                } else {
                    IntentUtil.startFriendActivity(mActivity,
                            QiupuConfig.USER_INDEX_FRIENDS, mUser.uid);
                }
            } else if (viewid == R.id.follower_ll) {
                if (mUser.uid == AccountServiceUtils.getBorqsAccountID()) {
                    showMyFansList();
                } else {
                    IntentUtil.startFriendActivity(mActivity,
                            QiupuConfig.USER_INDEX_FOLLOWERS, mUser.uid);
                }
            } else if (viewid == R.id.favorite_ll || viewid == R.id.id_favourites) {
                showFavoriteApps();
            } else if (viewid == R.id.applications_ll) {
                showApplications();
            } else if (viewid == R.id.request_concern) {
            } else if (viewid == R.id.profile_img_ui) {
                String[] items = new String[] {
                        getString(R.string.take_photo),
                        getString(R.string.phone_album) };
                DialogUtils.showItemsDialog(mActivity, mActivity.getResources().getString(R.string.select_profile_photo), 0, items,
                        ChooseEditImageItemClickListener);
            } else if (viewid == R.id.recommend_ll) {
                pickRecommendationList();
            } else {
                Log.d(TAG, "");
            }
        }
    };
	
	View.OnClickListener setcircleClick = new OnClickListener()
	{
		public void onClick(View arg0)
		{
			IntentUtil.startCircleSelectIntent(mActivity, mUser.uid, mUser.circleId);
		}
	};
	
   	
    private static final int GET_USER_DETAIL           = 101;
	private static final int GET_USER_DETAIL_END       = 102;
	private static final int EDIT_PROFILE_IMAGE_END    = 103;
	private static final int RECOMMEND_FRIENDS         = 104;
	private static final int RECOMMEND_FRIENDS_END     = 105;
	private static final int CHANGE_REQUEST_END        = 106;
	private static final int UPDATE_STATUS_END         = 107;
	private static final int UPDATE_STATUS             = 108;
	private static final int POST_SAY_HI_END           = 109;
    private static final int REMARK_SET_END            = 112;
	
	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_USER_DETAIL: {
				getUserInfo(mUser.uid);
				break;
			}
			case GET_USER_DETAIL_END: {			
				end();
				if(msg.getData().getBoolean(BasicActivity.RESULT, false)) {
				    if(getActivity() != null && isDetached() == false) {
				        refreshUserInfoUI(); 
//				        refreshShareSourceResult();
				        if(mFragmentCallBack != null) {
				        	mFragmentCallBack.changeUserInfo(mUser);
				        }
				    }
				}
				else
				{
					String ErrorMsg = msg.getData().getString(BasicActivity.ERROR_MSG);
					if(StringUtil.isEmpty(ErrorMsg) == false) {
//                            showLongToast(ErrorMsg);
					}
				}
				break;	
			}
			case EDIT_PROFILE_IMAGE_END: {
				dimissProgressDialog();
				if (mCurrentPhotoFile != null && mCurrentPhotoFile.exists()) {
					mCurrentPhotoFile.delete();
				}
				
				if(msg.getData().getBoolean(BasicActivity.RESULT, false)) {
					profile_img_ui.setImageBitmap(photo);
					initImageUI(mUser.profile_image_url);
					updatePostProfileImage(mUser.profile_image_url);
				}else {
					Log.d(TAG, "edit profile iamge failed " );
				}
				break;
			}
			case RECOMMEND_FRIENDS: {
				String selectuserid = msg.getData().getString("selectUserid");
				Log.d(TAG, "recommendFriends:"+selectuserid);
				recommendFriends(selectuserid);
				break;
			}
			case RECOMMEND_FRIENDS_END: {
				dimissProgressDialog();
				if(msg.getData().getBoolean(BasicActivity.RESULT, false)) {
					ToastUtil.showShortToast(mActivity, mHandler, R.string.recommend_success);
				}
				else {
					ToastUtil.showShortToast(mActivity, mHandler, R.string.recommend_failed);
				}
				break;
			}
			case CHANGE_REQUEST_END: {
				dimissProgressDialog();
				//TODO
				if(msg.getData().getBoolean(BasicActivity.RESULT, false)) {
					ToastUtil.showShortToast(mActivity, mHandler, R.string.request_ok);
				}
				break;
			}
			case UPDATE_STATUS: {
				updateStatus();
				break;
			}
			case UPDATE_STATUS_END: {
				dimissProgressDialog();
				if(msg.getData().getBoolean(BasicActivity.RESULT, false)) {
                    refreshProfileStatus(mUser.status, DateUtil.converToRelativeTime(getActivity(), mUser.status_time));
//		    		profile_user_status.setText(StringUtil.formatStatusHtmlString(getActivity(), mUser)/*content*/);
				}
				break;
			}
			case POST_SAY_HI_END: {
			    dimissProgressDialog();
                if(msg.getData().getBoolean(BasicActivity.RESULT, false)) {
                    ToastUtil.showShortToast(mActivity, mHandler, R.string.send_message_successful);
                }
                break;
            }
			
			case REMARK_SET_END: {
			    dimissProgressDialog();
                if(msg.getData().getBoolean(BasicActivity.RESULT)) {
                    
                    tv_user_name.setText(mUser.nick_name /*+ showRemarkName()*/ );
                    orm.updateUserRemark(mUser);// sync remark to DB
                    if(mFragmentCallBack != null) {
                    	mFragmentCallBack.changeUserInfo(mUser);
                    }
                    QiupuHelper.updateActivityUI(mUser);
                }else {
                    Log.d(TAG, "remark set failed");
                }
			    break;
			}
			}
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
            if (requestCode == PHOTO_PICKED_WITH_DATA) {
            	//TODO
//                testSharedAccountPhotoCancelled();
            }
            return;
        }
        
        switch (requestCode) {
          case PHOTO_PICKED_WITH_DATA: 
          {
        	  if (mCurrentPhotoFile != null && mCurrentPhotoFile.exists()) {
            	  mCurrentPhotoFile.delete();
              } 
        	  
              photo = data.getParcelableExtra("data");
              
              if(photo == null){
                  //get photo url
                Uri originalUri = data.getData();
                //将图片内容解析成字节数组
//                byte[] mContent;
				try {
					tryCropProfileImage(Uri.parse(originalUri.toString()));
//					mContent = readStream(mActivity.getContentResolver().openInputStream(Uri.parse(originalUri.toString())));
					//将字节数组转换为ImageView可调用的Bitmap对象
//					photo = getPicFromBytes(mContent, null); 
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
            		  
            		  editProfileImage(mCurrentPhotoFile);
            	  } catch (Exception e) {
            		  e.printStackTrace();
            	  }
              }
              break;  
          }  
          case CAMERA_WITH_DATA: 
          {
              tryCropProfileImage(Uri.fromFile(mCurrentPhotoFile));
              break;  
          }  
          case SELECT_USER_DATE:
          {
        	  String userid = data.getStringExtra("address");
        	  Log.d(TAG, "userid :" + userid);
        	  Message msg = mHandler.obtainMessage(RECOMMEND_FRIENDS);
        	  msg.getData().putString("selectUserid", userid);
        	  msg.sendToTarget();
        	  break;  
          }
          default:
              super.onActivityResult(requestCode, resultCode, data);
        } 
		
	};
	
	Object mInfoLock = new Object();
	boolean inInfoProcess;
	
	private void getUserInfo(final long userid) {
		if (!AccountServiceUtils.isAccountReady()) {
			return;
		}
		
		if (getActivity() == null || ((BasicActivity) getActivity()).asyncQiupu == null) {
			return;
		}

		synchronized (mInfoLock) {
			if (inInfoProcess == true) {
				Log.d(TAG, "in loading info data");
				return;
			}
		}

        begin();

		synchronized (mInfoLock) {
			inInfoProcess = true;
		}
		((BasicActivity) getActivity()).asyncQiupu.getUserInfo(userid, AccountServiceUtils.getSessionID(),new TwitterAdapter() {
			public void getUserInfo(QiupuUser user) {
				Log.d(TAG, "finish getUserInfo=" + user);
				mUser = user;
				
				synchronized (mInfoLock) {
					inInfoProcess = false;
				}
				
				// update database
				if((user.circleId != null && user.circleId.length() > 0)
						|| mUser.uid == AccountServiceUtils.getBorqsAccountID())
				{
					orm.insertUserinfo(user);
				}
				// create share source data.
				orm.updateShareSourceDB(userid, user.sharedResource);
				
				Message msg = mHandler.obtainMessage(GET_USER_DETAIL_END);
				msg.getData().putBoolean(BasicActivity.RESULT, true);
				msg.sendToTarget();
			}
			
			public void onException(TwitterException ex, TwitterMethod method) {
				
				synchronized (mInfoLock) {
					inInfoProcess = false;
				}
				TwitterExceptionUtils.printException(TAG, "getUserInfo, server exception:", ex, method);
				Log.d(TAG, "fail to load user info=" + ex.getMessage());
				
				Message msg = mHandler.obtainMessage(GET_USER_DETAIL_END);
				msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
				msg.getData().putBoolean(BasicActivity.RESULT, false);
				msg.sendToTarget();
			}
		});
	}
	
	Object mEditProfileImageLock = new Object();
	boolean inEditImageProcess;
	private void editProfileImage(File file){
		if (((BasicActivity) getActivity()).asyncQiupu == null) {
			return;
		}
		
		synchronized(mEditProfileImageLock)
		{
			if(inEditImageProcess == true)
			{
				Log.d(TAG, "in loading info data");
				return ;
			}
		}
		
		showProcessDialog(R.string.edit_profile_update_dialog, false, true, true);
		
		synchronized(mEditProfileImageLock)
		{
			inEditImageProcess = true;			
		}
		((BasicActivity) getActivity()).asyncQiupu.editUserProfileImage(AccountServiceUtils.getSessionID(), file, new TwitterAdapter() {
			public void editUserProfileImage(String result) {
				Log.d(TAG, "finish edit user profile" + result);

				mUser.profile_image_url = result;
				orm.updateProfileImageUrl(result);
				
				Message msg = mHandler.obtainMessage(EDIT_PROFILE_IMAGE_END);				
				msg.getData().putBoolean(BasicActivity.RESULT, true);
				msg.sendToTarget();
				synchronized(mEditProfileImageLock)
				{
					inEditImageProcess = false;			
				}
			}

			public void onException(TwitterException ex, TwitterMethod method) {
				
				synchronized(mEditProfileImageLock)
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
	
	Object mRecommendLock = new Object();
	boolean inRecommendProcess;
	private void recommendFriends(String selectid){
		
		if (((BasicActivity) getActivity()).asyncQiupu == null) {
			return;
		}
		synchronized(mRecommendLock)
		{
			if(inRecommendProcess == true)
				return ;
		}
		
		showProcessDialog(R.string.recommend_friends_progress, false, true, true);
		synchronized(mRecommendLock)
		{
			inRecommendProcess = true;			
		}
		((BasicActivity) getActivity()).asyncQiupu.recommendFriends(AccountServiceUtils.getSessionID(),mUser.uid, selectid, new TwitterAdapter()
		{
			public void recommendFriends(boolean result) 
			{
				Log.d(TAG, "finish recommend Friends");
//				if (result) {
					Message msg = mHandler.obtainMessage(RECOMMEND_FRIENDS_END);
					msg.getData().putBoolean(BasicActivity.RESULT, result);
					msg.sendToTarget();
//				}
				synchronized(mRecommendLock)
				{
					inRecommendProcess = false;			
				}
			}
			
			public void onException(TwitterException ex,TwitterMethod method) {
                TwitterExceptionUtils.printException(TAG, "recommendFriends, server exception:", ex, method);

				synchronized(mRecommendLock)
				{
					inRecommendProcess = false;			
				}
			    
			    Message msg = mHandler.obtainMessage(RECOMMEND_FRIENDS_END);
			    msg.getData().putBoolean(BasicActivity.RESULT, false);
			    msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
				msg.sendToTarget();
			}
		});
	}
	
	private void updateStatus()
	{	
		if (((BasicActivity) getActivity()).asyncQiupu == null) {
			return;
		}
		
		String content = "";
		if(mStatusEditText == null) {
			return ;
		}else {
			content = mStatusEditText.getEditableText().toString().trim();
			if(content.equals(mUser.status))
				return ;
		}
		final String status = content;
		showProcessDialog(R.string.status_update_summary, false, true, true);
		
		((BasicActivity) getActivity()).asyncQiupu.statusUpdateAsync(AccountServiceUtils.getSessionID(), AccountServiceUtils.getBorqsAccountID(), status,isSendPost.isChecked(),new TwitterAdapter() {
			public void updateQiupuStatus(final Stream post) {
				Log.d(TAG, "finish statusUpdateAsync="+post);

				mHandler.post(new Runnable(){
					public void run()
					{
						//TODO need to broadcast to other stream 
						updateActivityUI(false, "");
					}
				});

				mUser.status = status;
				if(post != null) {
					mUser.status_time = post.created_time > post.updated_time ? post.created_time : post.updated_time;
				}
                orm.updateUserStatus(mUser);

				Message msg = mHandler.obtainMessage(UPDATE_STATUS_END);				
				msg.getData().putBoolean(BasicActivity.RESULT, true);
				msg.sendToTarget();
			}

			public void onException(TwitterException ex, TwitterMethod method) {
                TwitterExceptionUtils.printException(TAG, "statusUpdateAsync, server exception:", ex, method);

				Message msg = mHandler.obtainMessage(UPDATE_STATUS_END);
				msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
				msg.getData().putBoolean(BasicActivity.RESULT, false);
				msg.sendToTarget();
			}
		});
	}	
	
	boolean isSayHiLoading;
    Object mLockSayHi = new Object();
    public void postSayHi()
    {
    	if (((BasicActivity) getActivity()).asyncQiupu == null) {
			return;
		}
        if (isSayHiLoading == true) {
            ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
            return;
        }

        synchronized (mLockSayHi) {
            isSayHiLoading = true;
        }
        
        showProcessDialog(R.string.post_to_wall_summary, false, true, true);
        
        String content = getString(R.string.say_hi_content);
        
        ((BasicActivity) getActivity()).asyncQiupu.postToMultiWallAsync(AccountServiceUtils.getSessionID(), 
                String.valueOf(mUser.uid), content, null, true, true, true, true,false, false, false,"", new TwitterAdapter() {
            public void postToWall(final Stream post) {
                Log.d(TAG, "finish postSayHi="+post);

                mHandler.post(new Runnable(){
                    public void run()
                    {
                        updateActivityUI(true, String.valueOf(mUser.uid));
                    }
                });         
                
                synchronized (mLockSayHi) {
                    isSayHiLoading = false;
                }
                
                Message msg = mHandler.obtainMessage(POST_SAY_HI_END);              
                msg.getData().putBoolean(BasicActivity.RESULT, true);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                TwitterExceptionUtils.printException(TAG, "postFeedbackAsync, server exception:", ex, method);

                synchronized (mLockSayHi) {
                    isSayHiLoading = false;
                }
                
                Message msg = mHandler.obtainMessage(POST_SAY_HI_END);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(BasicActivity.RESULT, false);
                msg.sendToTarget();
            }
        });
    }
    
    
	public boolean getLoadStatus(){
		return inInfoProcess;
	}
	
	public void refreshUserInfo() {
		super.loadRefresh();
		mHandler.obtainMessage(GET_USER_DETAIL).sendToTarget();
	}
    
	private void showMyFansList() {
        IntentUtil.showUserFansList(mActivity, mUser.uid);
	}
	
	private void showFavoriteApps() {
	    if(mUser.favorites_count > 0) {
            IntentUtil.startApplicationBoxActivity(mActivity, BpcApiUtils.APP_FAVORITE_SCHEME_PATH,
                    mUser.uid, mUser.nick_name);
	    }else {
	        ToastUtil.showShortToast(mActivity, mHandler, R.string.message_have_no_favorite);
	    }
	}

    private void showApplications() {
        IntentUtil.startApplicationBoxActivity(mActivity, BpcApiUtils.APP_LIST_SCHEME,
                mUser.uid, mUser.nick_name, mUser.circleName);
    }
	
	public void pickRecommendationList() {
        Intent reintent = new Intent(mActivity, PickCircleUserActivity.class);
        reintent.putExtra(PickCircleUserActivity.RECEIVER_TYPE, PickCircleUserActivity.type_all_friends);
        startActivityForResult(reintent, SELECT_USER_DATE);
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
	
	private void doPickPhotoFromGallery() {
		try {
			// Launch picker to choose photo for selected contact  
			final Intent intent = QiupuHelper.getPhotoPickIntent();  
			startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
		}
		catch (ActivityNotFoundException e) {}
	}
	
	private void dimissProgressDialog() {
		try {
			mprogressDialog.dismiss();
			mprogressDialog = null;
		}catch(Exception e){
			Log.d(TAG, "progress dialog dimiss exception !");
		}
		
	}
	public void refreshUserInfo(QiupuUser user) {
		mUser = user;
		refreshUserInfoUI();
	}
	
	private void tryCropProfileImage(Uri uri) {
        try {
            // start gallery to crop photo
            final Intent intent = QiupuHelper.getCropImageIntent(uri);
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {}
    }
	
	private void updateActivityUI(final boolean isScretly, final String toUsers){

		synchronized(QiupuHelper.refreshPostListeners)
        {
			Log.d(TAG, "refreshPostListeners.size() : " + QiupuHelper.refreshPostListeners.size());
            Set<String> set = QiupuHelper.refreshPostListeners.keySet();
            Iterator<String> it = set.iterator();
            while(it.hasNext())
            {
                String key = it.next();
                RefreshPostListener listener = QiupuHelper.refreshPostListeners.get(key).get();
                if(listener != null)
                {
                    listener.loadNewPost(isScretly, toUsers);
                }
            }      
        }      
    }
	
	private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable){
		mprogressDialog = DialogUtils.createProgressDialog(mActivity, 
				resId, CanceledOnTouchOutside, Indeterminate, cancelable);
		mprogressDialog.show();    	
	}
	
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
	
	DialogInterface.OnClickListener UpdateStatusOkListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(mUser.uid == AccountServiceUtils.getBorqsAccountID()) {
				mHandler.obtainMessage(UPDATE_STATUS).sendToTarget();
			}
		}
	};
	
	@Override
	public void updateItemUI(QiupuUser user) {
		Log.d(TAG, "user: " + user);
		if(user != null) {
			Log.d(TAG, "updateItemUI: " + user.circleId);
			if(user.circleId != null && user.circleId.length() <= 0)
			{
				mUser.circleId = "";
				mUser.circleName = "";
			}
			else
			{
				mUser.circleId = user.circleId;
				mUser.circleName = user.circleName;
				mUser.pedding_requests = user.pedding_requests;
				mUser.profile_privacy = user.profile_privacy;
			}
			
			setCircleUI();
			setDisplayNameUi();
		}
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

    
    private String showRemarkName() {
        String remarkname = getString(R.string.profile_remark);
        if(StringUtil.isEmpty(mUser.remark) == false) {
            remarkname = mUser.remark;
        }else {
//            if(mUser.perhapsNames != null && mUser.perhapsNames.size() > 0) {
//                PerhapsName tmpPerhapsName = new PerhapsName();
//                tmpPerhapsName.name = mUser.nick_name;
//                
//                if(mUser.perhapsNames.contains(tmpPerhapsName)) {
//                    mUser.perhapsNames.remove(tmpPerhapsName);
//                }
//                
//                for(int i=0; i<mUser.perhapsNames.size(); i++) {
//                    if(mUser.nick_name.equals(mUser.perhapsNames.get(i).name) == false) {
//                        remarkname = mUser.perhapsNames.get(i).name;
//                        break;
//                    }
//                }
//            }
        }
//        if(remarkname.length() > 0) {
//            remarkname = "(" + remarkname + ")";
//        }
        
        return remarkname;
    }
    
    View.OnClickListener remarkClickListener = new OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "click remark view");
            showEditRemarkUI();
        }
    };
    
    private void gotoUpdateRemark(String remarkName) {
        if(StringUtil.isValidString(remarkName)) {
            if(remarkName.equals(mUser.remark)) {
                Log.d(TAG, "the remark has not changed , do nothing");
            }else {
                remarkSet(mUser.uid, remarkName);
            }
        }else {
            if(StringUtil.isValidString(mUser.remark)) {
                remarkSet(mUser.uid, "");
            }else {
                Log.d(TAG, "the remark has not changed , do nothing");
            }
        }
    }
    
    private AutoCompleteTextView mRemarkEditText;
    
    public void showEditRemarkUI() {
        View editRemarkView = LayoutInflater.from(mActivity).inflate(R.layout.edit_remark_dialog, null);
        mRemarkEditText = (AutoCompleteTextView) editRemarkView.findViewById(R.id.edit_content);
        
        setRemarkTextValue(mUser.remark);

        if(mUser.perhapsNames != null && mUser.perhapsNames.size() > 0) {
            String[] names = new String[mUser.perhapsNames.size()];
            for(int i=0; i<mUser.perhapsNames.size(); i++) {
                names[i] = mUser.perhapsNames.get(i).name;
            }
            
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity, 
                    android.R.layout.simple_dropdown_item_1line, names);
            
            mRemarkEditText.setAdapter(adapter);
            mRemarkEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
                
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    AutoCompleteTextView view = (AutoCompleteTextView) v;
                    if (hasFocus) {
                        view.showDropDown();
                    }
                }
            });
        }
        
        DialogUtils.ShowDialogwithView(mActivity, getString(R.string.update_remark_title)
                , 0, editRemarkView, EditRemarkOkListener, ChangeRequestCancelListener);        
    }
    
    DialogInterface.OnClickListener EditRemarkOkListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(mRemarkEditText != null) {
                gotoUpdateRemark(mRemarkEditText.getText().toString());
            }
        }
    };
    
    private void setRemarkTextValue(String content) {
        if(mRemarkEditText != null) {
            mRemarkEditText.setText(content);
            mRemarkEditText.setSelection(mRemarkEditText.getText().toString().length());
        }
    }
    public QiupuUser getUserInfo() {
        return mUser;
    }
    
    Object mRemarkSetLock = new Object();
    boolean inremarkSetProcess;
    
    private void remarkSet(final long userid, final String remark) {
    	if (((BasicActivity) getActivity()).asyncQiupu == null) {
			return;
		}
    	
        synchronized (mRemarkSetLock) {
            if (inremarkSetProcess == true) {
                Log.d(TAG, "in loading info data");
                return;
            }
        }
        
        synchronized (mRemarkSetLock) {
            inremarkSetProcess = true;
        }
        showProcessDialog(R.string.status_update_summary, false, true, true);

        ((BasicActivity) getActivity()).asyncQiupu.remarkSet(userid, AccountServiceUtils.getSessionID(), remark, new TwitterAdapter() {
            public void remarkSet(boolean suc) {
                
                if(suc) {
                    mUser.remark = remark;
                }
                
                synchronized (mRemarkSetLock) {
                    inremarkSetProcess = false;
                }
                
                Message msg = mHandler.obtainMessage(REMARK_SET_END);
                msg.getData().putBoolean(BasicActivity.RESULT, suc);
                msg.sendToTarget();
            }
            
            public void onException(TwitterException ex, TwitterMethod method) {
                
                synchronized (mRemarkSetLock) {
                    inremarkSetProcess = false;
                }
                TwitterExceptionUtils.printException(TAG, "remarkSet, server exception:", ex, method);
                Log.d(TAG, "fail to load user info=" + ex.getMessage());
                
                Message msg = mHandler.obtainMessage(REMARK_SET_END);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(BasicActivity.RESULT, false);
                msg.sendToTarget();
            }
        });
    }

    DialogInterface.OnClickListener ChangeRequestCancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {}
	};
	
    public interface UserProfileMainFragmentCallBack {
		public void getProfileInfoFragment(UserProfileMainFragment fragment);
		public QiupuUser getUserInfo();
		public void gotoProfileDetailFragment();
		public void changeUserInfo(QiupuUser user);
	}

	@Override
	public void updateUI(int msgcode, Message msg) {
		if(QiupuConfig.LOGD)Log.d(TAG, "updateUI msgcode:"+msgcode+" msg:"+msg);
        switch(msgcode) {
            case QiupuMessage.MESSAGE_REFRESH_UI:
                refreshShareSourceResult();
                break;
            default:
                break;
        }
	}
	private void refreshShareSourceResult() {
		ArrayList<ShareSourceItem> dataList = new ArrayList<ShareSourceItem>();
		if(mUser.uid > 0 && QiupuConfig.isPublicCircleProfile(mUser.uid) == false) {
			Map<String, ShareSourceItem> shareMap = ShareSourceResultReceiver.mShareSourceMap;
			Set<String> keys = shareMap.keySet();
//			if (keys.isEmpty()) {
//				Log.d(TAG, "refreshShareSourceResult, no any share source.");
//				return;
//			}
			Log.d(TAG, "refreshShareSourceResult, map size:" + shareMap.size() + ", keyset size:" + keys.size());
			Iterator<String> itr = keys.iterator();
			String pkgName;
			ShareSourceItem item = null;
			while (itr.hasNext()) {
				pkgName = itr.next();
				if (null == pkgName) {
					Log.v(TAG, "refreshShareSourceResult, skip null key.");
				} else {
					item = shareMap.get(pkgName);
					Log.d(TAG, String.format("refreshShareSourceResult, item key:%s, value(id:%s, label:%s, scheme:%s, count:%d)",
							pkgName, item.mId, item.mLabel, item.mScheme, item.mCount));
					dataList.add(item);
				}
			}
		}
		
		dataList.addAll(mUser.sharedResource);
		if(mSourceView != null) 
			mSourceView.setDataArray(dataList, mUser);
	}
	
	private void updatePostProfileImage(final String imageUrl){

		synchronized(QiupuHelper.refreshProfileImageListeners)
        {
			Log.d(TAG, "refreshProfileImageListeners.size() : " + QiupuHelper.refreshProfileImageListeners.size());
            Set<String> set = QiupuHelper.refreshProfileImageListeners.keySet();
            Iterator<String> it = set.iterator();
            while(it.hasNext())
            {
                String key = it.next();
                RefreshPostProfileImageListener listener = QiupuHelper.refreshProfileImageListeners.get(key).get();
                if(listener != null)
                {
                    listener.refreshPostProfileImage(imageUrl);
                }
            }      
        }      
    }
	
	private static byte[] readStream(InputStream inStream) throws Exception {
		byte[] buffer = new byte[1024];
		int len = -1;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();
		outStream.close();
		inStream.close();
		return data;
	}
	
	private static Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
        if (bytes != null)
            if (opts != null)
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,opts);
            else
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return null;
    } 
}
