package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;
import java.util.HashMap;

import com.borqs.qiupu.ui.BasicNavigationActivity;
import twitter4j.QiupuUser;
import twitter4j.UserCircle;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Profile;
import android.provider.ContactsContract.RawContacts;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.borqs.account.commons.AccountServiceAdapter;
import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.SelectionItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.OnListItemClickListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.StreamListFragment;
import com.borqs.qiupu.fragment.StreamListFragment.StreamListFragmentCallBack;
import com.borqs.qiupu.fragment.UserProfileDetailFragment;
import com.borqs.qiupu.fragment.UserProfileDetailFragment.userProfileDetailCallBack;
import com.borqs.qiupu.fragment.UserProfileMainFragment;
import com.borqs.qiupu.fragment.UserProfileMainFragment.UserProfileMainFragmentCallBack;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class UserProfileFragmentActivity extends BasicNavigationActivity implements
	OnListItemClickListener, StreamListFragmentCallBack, UserProfileMainFragmentCallBack, userProfileDetailCallBack{

	private static final String TAG = "Qiupu.UserProfileFragmentActivity";

    private static final String EXTRA_KEY_USER = "EXTRA_KEY_USER";
    
    private Uri sharedImageUri;
    private QiupuUser mUser;
    private UserProfileMainFragment mUserProfileInfoFragment;
    private UserProfileDetailFragment mUserProfileDetailFragment;
    private FragmentManager mFragmentManager;
    private int mCurrentFragment;
    private static final int profile_main = 1;
    private static final int profile_detail = 2;
    private boolean misSupportLeftNavigation = false;
    
//    private boolean mOnlineStatus;
    
    private final static String PROFILE_MAIN_TAG = "PROFILE_MAIN_TAG";
	private final static String PROFILE_DETAIL_TAG = "PROFILE_DETAIL_TAG";

    StreamListFragment.MetaData mFragmentData = new StreamListFragment.MetaData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate");
    	misSupportLeftNavigation  = getIntent().getBooleanExtra("supportLeftNavigation", false);
        enableLeftNav(misSupportLeftNavigation);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_main);
        
        if(!misSupportLeftNavigation) {
        	showSlideToggle(overrideSlideToggleClickListener);
        }
        
        setHeadTitle(getString(R.string.profile));

        parseActivityIntent(getIntent());
        
        if(QiupuConfig.isEventIds(mUser.uid)) {
        	UserCircle circle = new UserCircle();
            circle.circleid = mUser.uid;
            circle.name = mUser.nick_name;
            IntentUtil.startEventDetailIntent(this, circle);
            finish();
            return ;
        }else if (QiupuConfig.isPageId(mUser.uid)) {
        	IntentUtil.startPageDetailActivity(this, mUser.uid);
        	finish();
        	return;
        }else {
            if(QiupuConfig.isActivityId(mUser.uid)) {
                ToastUtil.showShortToast(this, mHandler, String.format(getString(R.string.undefind_app_id), mUser.nick_name));
                finish();
                return ;
            }else if(mUser.uid >= QiupuConfig.PUBLIC_CIRCLE_MIN_ID) {
            	UserCircle circle = new UserCircle();
                circle.circleid = mUser.uid;
                circle.name = mUser.nick_name;
                IntentUtil.startPublicCircleDetailIntent(this, circle);
                finish();
                return ;
            }
        }
        mCurrentFragment = profile_main;
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        mUserProfileInfoFragment = new UserProfileMainFragment();
        ft.add(R.id.fragment_content, mUserProfileInfoFragment, PROFILE_MAIN_TAG);
        ft.commit();
    }

    @Override
    protected void createHandler() {
        mHandler = new Handler();
    }

    @Override
    protected void loadSearch()
    {
        gotoSearchActivity();
    }

    View.OnClickListener overrideSlideToggleClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			handlerBackKey(false);
		}
	};

    private void parseActivityIntent(Intent intent) {
        String url = getIntentURL(intent);
        if (isEmpty(url)) {
            QiupuUser user = (QiupuUser) intent.getSerializableExtra(EXTRA_KEY_USER);
            if (null != user) {
                mUser = user;
                mFragmentData.mUserId = user.uid;
            }
        } else {
            String uid = BpcApiUtils.parseSchemeValue(intent, BpcApiUtils.SEARCH_KEY_UID);

            if (TextUtils.isEmpty(uid)) {
                Log.i(TAG, "processIntent, unknown uid url: " + url);
                mFragmentData.mUserId = QiupuConfig.USER_ID_ALL;
            } else {
                mFragmentData.mUserId = Long.parseLong(uid);
                final String tab = BpcApiUtils.parseSchemeValue(intent, BpcApiUtils.SEARCH_KEY_TAB);
                if (!TextUtils.isEmpty(tab)) {
                }
            }
        }

        Bundle bundle = intent.getExtras();
        final String action = null == intent ? "" : intent.getAction();
        boolean waitForLogin = false;
        if (null != bundle) {
            if (Intent.ACTION_SEND.equals(action) && bundle.containsKey(Intent.EXTRA_STREAM)) {
                sharedImageUri = (Uri) bundle.getParcelable(Intent.EXTRA_STREAM);
                if (ensureAccountLogin()) {
                    mFragmentData.mUserId = getSaveUid();
                } else {
                    waitForLogin = true;
                }
            } else {
                sharedImageUri = null;
            }

            if (mFragmentData.mUserId <= 0) {
                mFragmentData.mUserId = bundle.getLong(BpcApiUtils.User.USER_ID);
            }
        } else {
            sharedImageUri = null;
        }

        preSetupUserForId();

        if (null != bundle) {
            String bundleString = bundle.getString(BpcApiUtils.User.USER_NAME);
            if (!TextUtils.isEmpty(bundleString)) {
                mUser.nick_name = bundleString;
            }

            bundleString = bundle.getString(BpcApiUtils.User.USER_CIRCLE);
            if (!TextUtils.isEmpty(bundleString)) {
                mUser.circleName = bundleString;
            }
        }

        showLeftActionBtn(false);
        showRightActionBtn(true);
        overrideRightActionBtn(R.drawable.ic_menu_moreoverflow, editProfileClick);

        if(mUser.uid == AccountServiceUtils.getBorqsAccountID()) {
        	showMiddleActionBtn(false);
        }else {
        	showMiddleActionBtn(true);
        	overrideMiddleActionBtn(R.drawable.actionbar_icon_release_normal, gotoComposelistener);
        }

        if (!waitForLogin) {
            if (null != sharedImageUri) {
            }
        }

        mFragmentData.mFragmentTitle = getString(R.string.circle_detail_post);
    }

    private void preSetupUserForId() {
        if (mUser == null) {
            QiupuUser user = orm.queryOneUserInfo(mFragmentData.mUserId);
            if (null == user) {
                mUser = new QiupuUser();
                mUser.uid = mFragmentData.mUserId;
            } else {
                mUser = user;
            }
        }
    }

    View.OnClickListener gotoComposelistener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			startComposeIntent();
//			IntentUtil.startAlbumIntent(UserProfileFragmentActivity.this, mUser.uid,mUser.nick_name);
		}
	};

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	if(mUser != null) {
    		outState = BpcApiUtils.getUserBundle(mUser.uid, mUser.nick_name, mUser.circleName);
    	}
    	super.onSaveInstanceState(outState);
    };

    @Override
    protected void onResume() {
    	super.onResume();
    	setOnlineStatus();
    }
    
    @Override
    protected void loadRefresh() {
//        queryUserOnlineStatus();
    	setOnlineStatus();
        if(mUserProfileInfoFragment != null) {
        	mUserProfileInfoFragment.refreshUserInfo();
        }
    }

    private void setOnlineStatus() {
//    	final boolean onlineStatus = PushingServiceAgent.queryOnlineUser(this, String.valueOf(mUser.uid));
//    	setOnlineView(onlineStatus);
    }
    @Override
    protected void uiLoadEnd() {
//    	if(mUserProfileInfoFragment != null && mUserProfileInfoFragment.getLoadStatus()) {
            showProgressBtn(false);
            showLeftActionBtn(false);
//    	}
    }

    @Override
    public void onListItemClick(View view, Fragment fg) {

    }

    @Override
    public StreamListFragment.MetaData getFragmentMetaData(int index) {
        return mFragmentData;
    }

    @Override
    public String getSerializeFilePath() {
        return QiupuHelper.profile + mFragmentData.mUserId;
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

    private void testSharedAccountPhotoCancelled() {
        if (null != sharedImageUri) {
            sharedImageUri = null;
            finish();
        }
    }

    private void startComposeIntent() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(String.valueOf(mFragmentData.mUserId), mUser.nick_name);
        IntentUtil.startComposeIntent(this, String.valueOf(mFragmentData.mUserId), true, map);
    }

    View.OnClickListener composeClick = new View.OnClickListener()
	{
		public void onClick(View arg0)
		{
            startComposeIntent();
		}
	};

    private void editProfile() {
        boolean done = false;
//        if(QiupuConfig.IS_USER_CONTACT_EDIT && Integer.parseInt(sdkversion) >= sdk_version_15) {
//        if(orm.isUseContactEditSettings() && Integer.parseInt(sdkversion) >= sdk_version_15) {
//            done = doEdit(this);
//        }

        if (!done) {
            Intent intent = new Intent(this, EditProfilesActivity.class);
            mUser = orm.queryOneUserInfo(AccountServiceUtils.getBorqsAccountID());
            intent.putExtra("bundle_userinfo", mUser);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
        }
    }
    

	View.OnClickListener editProfileClick = new View.OnClickListener() {
        public void onClick(View v) {
        	ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
        	if(mCurrentFragment == profile_main) {
        		items.add(new SelectionItem("", getString(R.string.label_refresh)));
        	}
        	items.add(new SelectionItem("", getString(R.string.home_album)));
        	
        	if(mUser.uid == AccountServiceUtils.getBorqsAccountID()) {
        		items.add(new SelectionItem("", getString(R.string.edit_profile_title)));
    			items.add(new SelectionItem("", getString(R.string.edit_contact)));
    		}
    		else {
    			if(mCurrentFragment == profile_main) {
    				items.add(new SelectionItem("", getString(R.string.say_hi)));
    			}
    			if(canRemark() /*&& mCurrentFragment == profile_main*/) {
    			    items.add(new SelectionItem("", getString(R.string.update_remark)));
    			}
    			items.add(new SelectionItem("", getString(R.string.friend_recomm)));
    		}
        	showCorpusSelectionDialog(items);
        }
    };

    // TODO : handle profile edit result.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            if (requestCode == PHOTO_PICKED_WITH_DATA) {
                testSharedAccountPhotoCancelled();
            }
            return;
        }

        switch (requestCode) {
            case EDIT_PROFILE_REQUEST_CODE: {
                mUser = orm.queryOneUserInfo(AccountServiceUtils.getBorqsAccountID());
                if(mUserProfileInfoFragment != null) {
                    mUserProfileInfoFragment.refreshUserInfo(mUser);
                }
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static void startUserDetailIntent(Context context, long uid, String nickName, String circleName) {
    	startUserDetailIntent(context, 0, uid, nickName, circleName);
//        startUserDetailIntent(context, TAB_INFO, uid, nickName, circleName);
    }

    public static void startUserDetailAboutIntent(Context context, long uid, String nickName) {
        startUserDetailIntent(context, 0, uid, nickName, "");
    }


    public static Intent buildUserDetailIntent(Context context, long uid, String nickName) {
    	if((context instanceof UserProfileFragmentActivity)) {
//    		if(uid == getSaveUid()) {
//    			return null;
//    		}
    		UserProfileFragmentActivity myActivity = (UserProfileFragmentActivity)context;
    		if(myActivity.getUserInfo() != null) {
    			if(myActivity.getUserInfo().uid == uid) {
    				return null;
    			}
    		} 
    	}
        Intent intent = new Intent(context, UserProfileFragmentActivity.class);
        Bundle bundle = BpcApiUtils.getUserBundle(uid, nickName, null);
//        bundle.putInt(DEFAULT_TAB, TAB_INFO);
        intent.putExtras(bundle);
        if (BpcApiUtils.isActivityReadyForIntent(context, intent)) {
            return intent;
        } else {
            Log.e(TAG, "buildUserDetailIntent, no valid activity for intent: " + intent);

            return null;
        }
    }

    private static void startUserDetailIntent(Context context, int tabIndex, long uid, String nickName, String circleName) {
    	if((context instanceof UserProfileFragmentActivity)) {
//    		if(uid == getSaveUid()) {
//    			return null;
//    		}
    		UserProfileFragmentActivity myActivity = (UserProfileFragmentActivity)context;
    		if(myActivity.getUserInfo() != null) {
    			if(myActivity.getUserInfo().uid == uid) {
    				return;
    			}
    		} 
    	}
        Intent intent = new Intent(context, UserProfileFragmentActivity.class);
        Bundle bundle = BpcApiUtils.getUserBundle(uid, nickName, circleName);
        
        intent.putExtras(bundle);
        if (BpcApiUtils.isActivityReadyForIntent(context, intent)) {
            context.startActivity(intent);
        } else {
            Log.e(TAG, "startUserDetailIntent, no valid activity for intent: " + intent);
        }
    }

    public static void startUserDetailIntent(Context context, QiupuUser qiupuUser) {
        if ((context instanceof UserProfileFragmentActivity)) {
            UserProfileFragmentActivity myActivity = (UserProfileFragmentActivity) context;
            if (myActivity.getUserInfo() != null) {
                if (myActivity.getUserInfo().uid == qiupuUser.uid) {
                    return;
                }
            }
        }

        Intent intent = new Intent(context, UserProfileFragmentActivity.class);
        intent.putExtra(EXTRA_KEY_USER, qiupuUser);
        if (BpcApiUtils.isActivityReadyForIntent(context, intent)) {
            context.startActivity(intent);
        } else {
            Log.e(TAG, "startUserDetailIntent, no valid activity for intent: " + intent);
        }
    }

    
	@Override
	public QiupuUser getUserInfo() {
		return mUser;
		
	}

//	@Override
//	public void setLeftMenuPosition() {
//		mPosition = LeftMenuMapping.getPositionForActivity(this);
//		mTitle = getString(R.string.personal_infomation);
//	}
	
	private boolean doEdit(Context context) {
        boolean done = false;

        String SELECTION = RawContacts.ACCOUNT_NAME + "=? AND "
                + RawContacts.ACCOUNT_TYPE + "=? AND " + RawContacts.DELETED
                + "=?";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Profile.CONTENT_RAW_CONTACTS_URI,
                new String[] { Profile._ID }, SELECTION, new String[] {
                        AccountServiceAdapter.getUserID(context),
                AccountServiceAdapter.BORQS_ACCOUNT_TYPE, "0" }, null);
        if (cursor != null && cursor.moveToFirst()) {
            //raw contact id of me
            long id = cursor.getLong(0);
            Uri me = ContentUris.withAppendedId(
                    Profile.CONTENT_RAW_CONTACTS_URI, id);
            Intent intent = new Intent(Intent.ACTION_EDIT, me);
            startActivity(intent);
            done = true;
        }

        QiupuORM.closeCursor(cursor);
        return done;
    }

//    @Override
//    public void changeHeadTitle(String username) {
//        Log.d(TAG, "username : " + username);
////        setHeadTitle(username);
//
////        final String statusText = mOnlineStatus ? "(online)" : "(offline)";
//        if (mUserProfileInfoFragment != null) {
//            mUserProfileInfoFragment.resetOnlineStatus(mOnlineStatus);
//        }
//        setHeadTitle(getString(R.string.personal_infomation)/* + statusText*/);
//    }
    
	protected void showCorpusSelectionDialog(ArrayList<SelectionItem> items) {
	    if(mRightActionBtn != null) {
	        int location[] = new int[2];
	        mRightActionBtn.getLocationInWindow(location);
	        int x = location[0];
	        int y = getResources().getDimensionPixelSize(R.dimen.title_bar_height);
	        
	        DialogUtils.showCorpusSelectionDialog(this, x, y, items, actionListItemClickListener);
	    }
	}
	
    private boolean canRemark() {
        boolean canremark = false;
        QiupuUser tmpUser = mUser;
        if(mUserProfileInfoFragment != null) {
            tmpUser = mUserProfileInfoFragment.getUserInfo();
        }
        
        if(tmpUser != null && StringUtil.isValidString(tmpUser.circleId)) {
            canremark = true;
        }
        return canremark; 
    }
    
    OnItemClickListener actionListItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(CorpusSelectionItemView.class.isInstance(view)) {
                CorpusSelectionItemView item = (CorpusSelectionItemView) view;
                onCorpusSelected(item.getText());             
            }
        }
    };
    
    private void onCorpusSelected(String value) {
        if (getString(R.string.label_refresh).equals(value)) {
            loadRefresh();
        } else if (getString(R.string.edit_profile_title).equals(value)) {
            editProfile();
	    } else if (getString(R.string.edit_contact).equals(value)) {
	    	Intent intent = new Intent(this, EditProfileActivity.class);
            mUser = orm.queryOneUserInfo(AccountServiceUtils.getBorqsAccountID());
            intent.putExtra("bundle_userinfo", mUser);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
	    }else if (getString(R.string.say_hi).equals(value)) {
            if (mUserProfileInfoFragment != null) {
                mUserProfileInfoFragment.postSayHi();
//                sendInstanceMessage();
            }
        } else if (getString(R.string.home_album).equals(value)) {
        	IntentUtil.startAlbumIntent(UserProfileFragmentActivity.this, mUser.uid,mUser.nick_name);
        } else if (getString(R.string.update_remark).equals(value)) {
        	if (mUserProfileInfoFragment != null) {
        		mUserProfileInfoFragment.showEditRemarkUI();
        	}
//        } else if (getString(R.string.home_album).equals(value)) {
//        	Intent intent = new Intent(UserProfileFragmentActivity.this,AlbumActivity.class);
//        	intent.putExtra("uid", mUser.uid);
//        	startActivity(intent);
        } else if (getString(R.string.send_im_message).equals(value)) {
            Intent intent = new Intent(UserProfileFragmentActivity.this, IMComposeActivity.class);
//            intent.putExtra("to_name", mUser.nick_name);
//            intent.putExtra("from_url", mUser.profile_image_url);
            intent.putExtra("user", mUser);
            intent.putExtra("to_url", QiupuORM.getInstance(this).getUserProfileImageUrl(getSaveUid()));
            startActivity(intent);
        } else if(getString(R.string.friend_recomm).equals(value)) {
        	if(mUserProfileInfoFragment != null) {
        		mUserProfileInfoFragment.pickRecommendationList();
        	}
        }else {
            Log.d(TAG, "unsupported item action!");
        }
    }

	@Override
	public void getProfileInfoFragment(UserProfileMainFragment fragment) {
		mUserProfileInfoFragment = fragment;
	}

	@Override
	public void gotoProfileDetailFragment() {
		if(mCurrentFragment != profile_detail){
			if(AccountServiceUtils.getBorqsAccountID() == mUser.uid) {
				overrideSlideIcon(R.drawable.ic_back_holo_dark, overrideSlideToggleClickListener);
			}
			mCurrentFragment = profile_detail;
			FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
			if(mUserProfileInfoFragment != null && !mUserProfileInfoFragment.isHidden()){
				mFragmentManager.beginTransaction().hide(mUserProfileInfoFragment).commit();
			}
			
			mUserProfileDetailFragment = (UserProfileDetailFragment) mFragmentManager.findFragmentByTag(PROFILE_DETAIL_TAG);
			
			if(mUserProfileDetailFragment == null){
				mUserProfileDetailFragment = new UserProfileDetailFragment();
				mFragmentTransaction.add(R.id.fragment_content, mUserProfileDetailFragment, PROFILE_DETAIL_TAG);
			}else {
				mFragmentTransaction.show(mUserProfileDetailFragment);
			}
			mFragmentTransaction.commit();
		}
	}
	
	@Override
	public void onBackPressed() {
	    handlerBackKey(true);
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
	    if (KeyEvent.KEYCODE_BACK == keyCode) {
    	    if (mCurrentFragment == profile_detail) {
    	        return true;
    	    }
	    }
	    return super.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (KeyEvent.KEYCODE_BACK == keyCode) {
	        if (mCurrentFragment == profile_detail) {
	            handlerBackKey(false);
	            return true;
	        }
	    }
	    return super.onKeyUp(keyCode, event);
	}

	private void handlerBackKey(boolean isBackKey) {
	    if(mCurrentFragment == profile_detail){
            
            mCurrentFragment = profile_main;
            FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
            if(mUserProfileDetailFragment != null && !mUserProfileDetailFragment.isHidden()){
                mFragmentManager.beginTransaction().hide(mUserProfileDetailFragment).commit();
            }
            
            mUserProfileInfoFragment = (UserProfileMainFragment) mFragmentManager.findFragmentByTag(PROFILE_MAIN_TAG);
            
            if(mUserProfileInfoFragment == null){
                mUserProfileInfoFragment = new UserProfileMainFragment();
                mFragmentTransaction.add(R.id.fragment_content, mUserProfileInfoFragment, PROFILE_MAIN_TAG);
            }else {
                mFragmentTransaction.show(mUserProfileInfoFragment);
            }
            mFragmentTransaction.commit();
            
            if(AccountServiceUtils.getBorqsAccountID() == mUser.uid) {
            	if(misSupportLeftNavigation) {
            		overrideSlideIcon(R.drawable.navbar_icon_launcher, leftClicker);
            	}
            }
	    } else {
	        if (isBackKey) {
	            super.onBackPressed();
	        } else {
	        	if(AccountServiceUtils.getBorqsAccountID() != mUser.uid) {
	        		finish();
	            }else {
	            	if(!misSupportLeftNavigation) {
	            		finish();
	            	}
	            	Log.d(TAG, "is myself , should show left menu");
	            }
	        }
	    }
	}

	@Override
	public void getUserProfileDetailFragment(UserProfileDetailFragment fragment) {
		mUserProfileDetailFragment = fragment;		
	}

	@Override
	public void changeUserInfo(QiupuUser user) {
		if(user != null) {
			mUser = user;
			if(mUserProfileDetailFragment != null) {
				mUserProfileDetailFragment.setUser(user);
			}
		}
	}
	
//	private void setOnlineView(boolean onlineStatus) {
//		if(AccountServiceUtils.getBorqsAccountID() == mUser.uid) {
//			mSubTitle.setVisibility(View.GONE);
//			return ;
//		}
//		mSubTitle.setVisibility(View.VISIBLE);
//		if(onlineStatus) {
//			if(mSubTitle != null) {
//				mSubTitle.setText(R.string.online_title);
//				mSubTitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.icon_green), null, null, null);
//			}
//		}else {
//			if(mSubTitle != null) {
//				mSubTitle.setText(R.string.offline_title);
//				mSubTitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.icon_grey), null, null, null);
//			}
//		}
//    }
}
