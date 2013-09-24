package com.borqs.qiupu.ui.bpc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import twitter4j.ApkResponse;
import twitter4j.ComposeShareData;
import twitter4j.InfoCategory;
import twitter4j.Stream;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.internal.http.HttpClientImpl;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.common.ExpandAnimation;
import com.borqs.common.adapter.CommentSettingAdapter;
import com.borqs.common.adapter.ComposeShareAdapter;
import com.borqs.common.adapter.GridViewFaceAdapter;
import com.borqs.common.adapter.RecipientsAdapter;
import com.borqs.common.adapter.RecipientsCirclesAdapter;
import com.borqs.common.adapter.StreamListAdapter;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.ComposeActionListener;
import com.borqs.common.listener.RefreshComposeItemActionListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.MyHtml;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.util.UserTask;
import com.borqs.common.view.CommentSettingItemView;
import com.borqs.common.view.ComposeShareItemView;
import com.borqs.common.view.ConversationMultiAutoCompleteTextView;
import com.borqs.common.view.RecipientsCirclesItemView;
import com.borqs.common.view.SNSItemView;
import com.borqs.qiupu.AccountServiceConnectObserver;
import com.borqs.qiupu.AddressPadMini;
import com.borqs.qiupu.AddressPadMini.EnableCompletionListener;
import com.borqs.qiupu.AddressPadMini.PhoneNumberEmailDecorater;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.circle.quickAction.ActionItem;
import com.borqs.qiupu.ui.circle.quickAction.QuickAction;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ContactUtils;
import com.borqs.qiupu.util.FileInfo;
import com.borqs.qiupu.util.JSONUtil;
import com.borqs.qiupu.util.LocationUtils;
import com.borqs.qiupu.util.StringUtil;

public class QiupuComposeActivity extends BasicActivity implements
        ComposeActionListener,
        RefreshComposeItemActionListener, EnableCompletionListener {
    private static String TAG="Qiupu.QiupuComposeActivity";

    private static final String RECIPIENT_FLAG_KEY = "receivers";

    public static final String EXTERNAL_COMMENT = "external-comment";

    public static final String EXTRA_FLAG_KEY = "EXTRA_FLAG_KEY";
    public static final String EXTRA_FILE_URI = "EXTRA_FILE_URI";
    public static final String EXTRA_PHOTO_ID = "EXTRA_PHOTO_ID";
    private static final int EXTRA_FLAG_CAMERA = 1;
    private static final int EXTRA_FLAG_GALLERY = 2;
    public static final int EXTRA_FLAG_SERVER = 3;
    private int mExtraFlag;

    private AddressPadMini mShareTo;
    private ConversationMultiAutoCompleteTextView mShareComment;

    private String receivers;
    private long mScene;
    private Stream post;
    private boolean privateShare = true;
    private String locationString;

    private TextView mLocationText;
    private ImageView mLocationIcon, mRemoveLocaitonIcon, mAddFaceView, mAddAttachmentView;
    private View mLocationProgressBar, mBottomView, mFaceView;
    private View mSpanView;
    private GridView mGridView;
    private LinearLayout mStreamContainer;

//    private HashSet<Long> selectContactIds;
    private boolean mIsLocationDisplayed = false;
    private ApkResponse mApkResponse;
    private ContactSimpleInfo mContactInfo;
    
    public static final String SHARE_FAVICON = "share_favicon";

    private File mCurrentPhotoFile;
    private Uri imageFileUri;
    private final static int Max_length = 4*1024;
    private final static int FILE_UNIT = 1024*1024;
    private final static int MAX_FILE_SIZE = 500*FILE_UNIT;

    private ListView mListView;
    private ImageView mChooseShareUserIV;
    private ComposeShareAdapter mComposeShareAdapter;
    private static ArrayList<ComposeShareData> mShareDataList = new ArrayList<ComposeShareData>();
    private ArrayList<ComposeShareData> cloneList = new ArrayList<ComposeShareData>();

    private Bitmap mFavicon;
    private String photo_id;
    private HashMap<String, String> mReceiveMap;
    private HashMap<Long, String> mSelectUserCircleMap = new HashMap<Long, String>();
    
    public static final String FROM_TYPE_CAMERA = "from_camera";
    public static final String FROM_TYPE_APP    = "from_app";
    public static final String FROM_TYPE_LOCATION = "from_location";
    public static final String RESHARE_KEY = "reshare";
    public static final String IS_ADMIN_KEY = "is_admin";
    public static final String IS_TOP_KEY = "is_top";
    public static final String IS_PRIVATE_KEY = "isprivate";
    public static final String RECEIVER_KEY = "receiversMap";
    private Intent intent;
    private boolean isTop = false;
    private boolean isAdmin = false;
    private CheckBox sendEmail;
    private CheckBox sendSms;
    private boolean isRetweet = false;
    private static final boolean SUPPORT_SEND_MESSAGE = false;
    private long mFromId;
    private static final String PUBLIC_SHARE_TAG = "#-2";
    private static final String MULTI_SHARE_PHOTO = "multi_share_photo";
    private static final String SINGLE_SHARE_PHOTO = "single_share_photo";
    private static final boolean USE_OLD_LOCATION = false;
    
    private TextView mCategoryView;
    private View mCategoryRl;
    private ImageView mCategoryIcon;
    private long mSelectCategoryId ;
    private long MNO_CATEGORY_ID = -111;
    private InputMethodManager mIme;
    
    private String mBackLocation;
    private Spinner mCircleSelectSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bpc_compose_ui);

        intent = getIntent();
        if (AccountServiceUtils.isAccountReady() == false) {
            ensureAccountLogin();
        } else {
            initialComposeEnv();
        }
    }

    /// avoid mupltiple invocation that will show up duplication of UI component.
    private boolean wasInitialized = false;
    private void initialComposeEnv() {
        if (wasInitialized) {
            Log.i(TAG, "initialComposeEnv, initialized already");
            return;
        }

        wasInitialized = true;
        initInputMethodSeting();
        initActionButton();
        initIntentData();
        initView();
        setHeaderViewData();
        parseIntent();
        setupLocationListener();
        HttpClientImpl.setLocation("");

        LocationUtils.addLocationRemovedListener(getClass().getName(), new LocationUtils.HideLocationListener() {
            @Override
            public void hideLocation(boolean hide) {
                if (mIsLocationDisplayed) {
                    LocationUtils.deactivateLocationService(QiupuComposeActivity.this);
                    onLocationHidden();
                }
            }
        });
        
        initCircleSelectUi();
        if(mShareDataList != null && mShareDataList.size() > 0) {
        	mSpanView.setVisibility(View.VISIBLE);
        }
    }
    
    private void initCircleSelectUi() {
    	if(mScene > 0 && mFromId <= 0) {
    		ArrayList<UserCircle> circleList = orm.queryInCircleCirclesList(mScene);
    		UserCircle justSayCircle = new UserCircle();
    		justSayCircle.circleid = QiupuConfig.CIRCLE_ID_PUBLIC;
    		justSayCircle.name = getString(R.string.just_saying_label);
    		circleList.add(0, justSayCircle);
    		
    		UserCircle privacySayCircle = new UserCircle();
    		privacySayCircle.circleid = QiupuConfig.CIRCLE_ID_PRIVACY;
    		privacySayCircle.name = getString(R.string.privacy_saying_label);
    		circleList.add(1, privacySayCircle);
    		
    		UserCircle sceneCircle = orm.queryOneCircleWithImage(mScene);
    		if(sceneCircle != null) {
    			circleList.add(2, sceneCircle);
    		}
    		
    		RecipientsCirclesAdapter adapter = new RecipientsCirclesAdapter(this, circleList);
            mCircleSelectSpinner.setAdapter(adapter);
            mCircleSelectSpinner.setSelection(0);
            mCircleSelectSpinner.setBackgroundResource(R.drawable.compose_circle_bg);
    	}else {
    		if(mFromId > 0) {
    			ArrayList<UserCircle> circleList = new ArrayList<UserCircle>();
    			UserCircle sceneCircle = orm.queryOneCircleWithImage(mFromId);
    			if(sceneCircle != null) {
    				circleList.add(0, sceneCircle);
    			}
    			RecipientsCirclesAdapter adapter = new RecipientsCirclesAdapter(this, circleList);
    			mCircleSelectSpinner.setAdapter(adapter);
    			mCircleSelectSpinner.setSelection(0);
    			mCircleSelectSpinner.setEnabled(false);
    			findViewById(R.id.drop_down_circle).setVisibility(View.GONE);
    			mCircleSelectSpinner.setBackgroundResource(R.drawable.compose_circel_bg_pressed);
    			// show input people text
    			mShareTo.setVisibility(View.VISIBLE);
    			mShareTo.requestFocus();
    		}
    	}
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.default_listview);
        mListView.addHeaderView(initFirstHeaderView(), null, false);
        mListView.addHeaderView(initSecondHeaderView(), null, false);
        mListView.addHeaderView(initHeaderView(isRetweet), null, false);
        mListView.setHeaderDividersEnabled(false);
        mComposeShareAdapter = new ComposeShareAdapter(this);
        mListView.setAdapter(mComposeShareAdapter);
        initFace();
        
        mCategoryView = (TextView) findViewById(R.id.info_category);
    	mCategoryView.setText(R.string.all_category_label);
    	
    	mCategoryRl = findViewById(R.id.category_rl);
    	mCategoryRl.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			showCategoryDropDownDialog();
    		}
    	});
    	mCategoryIcon = (ImageView) findViewById(R.id.category_title_icon);
        initBottomLayout();
        initInputTextAction();
    }

    private void initInputTextAction() {
//    	ImageView faceIv = (ImageView) findViewById(R.id.face_insert);
//    	if(faceIv != null) {
//    		faceIv.setOnClickListener(new OnClickListener() {
//    			
//    			@Override
//    			public void onClick(View v) {
//    				 showSmileyDialog();
//    			}
//    		});
//    	}
    	ImageView peopleIv = (ImageView) findViewById(R.id.people_insert);
    	peopleIv.setOnClickListener(ConversationMultiAutoCompleteTextView.instanceMentionButtonClickListener(this, mShareComment));
    }
    private void setHeaderViewData() {
        mShareTo.setAdapter(new RecipientsAdapter(this));
        mShareTo.setOnDecorateAddressListener(new FBUDecorater());
        mShareTo.setEnableCompletionListener(this);

        removeSucceedPost();
        if(TextUtils.isEmpty(receivers)) {
        	final String homeid = QiupuORM.getSettingValue(this, QiupuORM.HOME_ACTIVITY_ID);
        	if(TextUtils.isEmpty(homeid) == false) {
//        		receivers = "#" + homeid;
        		if(mScene <= 0) {
        			try {
        				mScene = Long.parseLong(homeid);
					} catch (Exception e) {
						Log.d(TAG, "homeid is null");
					}
        		}
        	}
        }
        initAddress();
        mShareTo.requestFocus();
    }

    private void initIntentData() {
        isAdmin = (null == intent ? false : intent.getBooleanExtra(IS_ADMIN_KEY, false));
        Log.d(TAG, "isAdmin = " + isAdmin);
        isTop = (null == intent ? false : intent.getBooleanExtra(IS_TOP_KEY, false));
        isRetweet = null != intent && intent.getBooleanExtra(QiupuMessage.BUNDLE_STREAM_IN_FILE, false);
        privateShare = (null == intent ? true : intent.getBooleanExtra(IS_PRIVATE_KEY, true));
        receivers = (null == intent ? "" : intent.getStringExtra(RECIPIENT_FLAG_KEY));
        mReceiveMap = (null == intent ? null : (HashMap<String, String>) intent.getSerializableExtra(RECEIVER_KEY));
        mScene = intent.getLongExtra(CircleUtils.INTENT_SCENE, -1);
        mFromId = intent.getLongExtra(CircleUtils.CIRCLE_ID, -1);
    }

    private void parseIntent() {
        cloneList.addAll(mShareDataList);

        if ("from_app".equals(intent.getStringExtra("from_type"))) {
            toggleAppPicker();
        }

        if(intent.getSerializableExtra(QiupuMessage.BUNDLE_APKINFO) != null) {
            showApp(intent);
        }

        if (null != mShareComment) {
            InputFilter[] filters = new InputFilter[]{new InputFilter.LengthFilter(Max_length)};
            mShareComment.setFilters(filters);
            final String externalComment = intent.getStringExtra(EXTERNAL_COMMENT);

            if (!TextUtils.isEmpty(externalComment)) {
                mShareComment.setText(MyHtml.fromHtml(externalComment));
            }
//            mShareComment.requestFocus();
        }

        parsePhotoIntent(intent);
        parsePhotoLinkFileUri(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupLocationListener();
        
        if (AccountServiceUtils.isAccountReady() == true) {
            initEnv();
        }

        if (mIsLocationDisplayed && isGetLocation() == false) {
            LocationUtils.activateLocationService(this);
        }

        QiupuHelper.registerComposeListener(this.toString(), this);
    }

    private boolean isGetLocation() {
        if (mLocationText != null && !TextUtils.isEmpty(mLocationText.getText())) {
            if (mLocationText.getText().equals(getResources().getString(R.string.location_text))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocationUtils.deactivateLocationService(this);
        QiupuConfig.setDefaultLocationApi(true);

        QiupuHelper.unRegisterComposeListener(this.toString());
    }

    private void initEnv() {
//        handleSerialize(false);

    	if(mComposeShareAdapter != null) {
    		if (isRetweet) {
    			mComposeShareAdapter.alterDataList(new ArrayList<ComposeShareData>());
    		} else {
    			mComposeShareAdapter.alterDataList(mShareDataList);
    		}
    		mComposeShareAdapter.setComposeActionListener(this);
    	}
        if (USE_OLD_LOCATION) {
            attachStreamProperty(R.id.id_stream_property, mIsCommentAllowed, mIsLikeAllowed, mIsShareAllowed);
        }
    }

    private void initInputMethodSeting() {
//        KeyboardLayout mainView = (KeyboardLayout) findViewById(R.id.share_ui);
//        mainView.setOnkbdStateListener(new onKybdsChangeListener() {
//            @Override
//            public void onKeyBoardStateChange(int state) {
//                if(state == KeyboardLayout.KEYBOARD_STATE_HIDE) {
//                    
//                }else if(state == KeyboardLayout.KEYBOARD_STATE_SHOW) {
//                    lastDownKeyCode = 0;
//                }
//            }
//        });
        mIme = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void initActionButton() {
        enableLeftActionBtn(false);
        enableMiddleActionBtn(false);
        enableRightActionBtn(true);
        overrideRightActionBtn(R.drawable.actionbar_post, shareAction);
//        showSlideToggle(navListener);
    }

    private View.OnClickListener navListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showConfirmDialog();
        }
    };

    private void showConfirmDialog() {
        if (needEscapeConfirm()) {
            String title = getString(R.string.discard_dialog_title, getString(R.string.home_steam));
            String message = getString(R.string.discard_dialog_message, getString(R.string.home_steam));
            DialogUtils.showConfirmDialog(this, title, message, confirmlistener);
        } else {
            finish();
        }
    }

    private DialogInterface.OnClickListener confirmlistener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            finish();
        }
    };

    private GridViewFaceAdapter mGVFaceAdapter;
    private void initFace() {
    	final String[] smileyText = getResources().getStringArray(R.array.default_smiley_texts);
    	mGVFaceAdapter = new GridViewFaceAdapter(this);
    	((LinearLayout.LayoutParams)(mFaceView.getLayoutParams())).bottomMargin = -96;
    	mAddFaceView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	if (mBottomView.getVisibility() == View.VISIBLE) {
                		mBottomView.setVisibility(View.GONE);
                		((LinearLayout.LayoutParams)(mFaceView.getLayoutParams())).bottomMargin = -96;
                	}
//                	if (mFaceView.getVisibility() == View.GONE) {
//                		mIme.hideSoftInputFromWindow(mFaceView.getWindowToken(), 0);
//                	}
                    mFaceView.startAnimation(new ExpandAnimation(mFaceView, 200));
                }
        });
    	
    	mGridView.setAdapter(mGVFaceAdapter);
    	mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String text = smileyText[position];
				if (mShareComment instanceof EditText) {
                    EditText editText = (EditText)mShareComment;
                    editText.clearComposingText();
                    final int index = editText.getSelectionStart();
                    if (index < 0 || index > editText.length()) {
                        editText.append(text);
                    } else {
                        Editable editable = editText.getEditableText();
                        editable.insert(index, text);
                    }
                } else {
                	mShareComment.append(text);
                }
				mShareComment.requestFocus();
				mFaceView.startAnimation(new ExpandAnimation(mFaceView, 200));
			}
    	});
    }

    private void initBottomLayout() {
        if (RESHARE_KEY.equals(intent == null ? "" : intent.getStringExtra(RESHARE_KEY))) {
            findViewById(R.id.bottom_layout).setVisibility(View.GONE);
        } else {
            // initial animation end height
            ((LinearLayout.LayoutParams)(mBottomView.getLayoutParams())).bottomMargin = -96;
            mAddAttachmentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	if (mFaceView.getVisibility() == View.VISIBLE) {
                		mFaceView.setVisibility(View.GONE);
                		((LinearLayout.LayoutParams)(mBottomView.getLayoutParams())).bottomMargin = -96;
                	}
                    mBottomView.startAnimation(new ExpandAnimation(mBottomView, 200));
                }
            });

            ImageView toggle_location = (ImageView) findViewById(R.id.toggle_location);
            ImageView toggle_link = (ImageView) findViewById(R.id.toggle_link);
            ImageView toggle_app = (ImageView) findViewById(R.id.toggle_app);
            ImageView toggle_photo = (ImageView) findViewById(R.id.toggle_photo);
            ImageView toggle_contact = (ImageView) findViewById(R.id.toggle_contact);
            ImageView toggle_file = (ImageView) findViewById(R.id.toggle_file);
    
            if (null != toggle_location) {
                if (!USE_OLD_LOCATION) {
                    toggle_location.setVisibility(View.GONE);
                }
                toggle_location.setOnClickListener(mToggleLocationListener);
            }
            if (null != toggle_link) {
                toggle_link.setOnClickListener(mToggleLinkListener);
            }
            if (null != toggle_app) {
                toggle_app.setOnClickListener(mToggleAppListener);
            }
            if (null != toggle_photo) {
                toggle_photo.setOnClickListener(mTogglePhotoListener);
            }
            if (null != toggle_contact) {
                toggle_contact.setOnClickListener(mToggleContactListener);
            }
            if (null != toggle_file) {
                toggle_file.setOnClickListener(mToggleFileListener);
            }
        }
    }

    private void parsePhotoLinkFileUri(Intent intent) {
        final String action = null == intent ? null : intent.getAction();
        if (action != null) {
            if (intent.hasExtra(Intent.EXTRA_STREAM)) {
                if (action.equals(Intent.ACTION_SEND_MULTIPLE)) {
                    ArrayList<Uri> list = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    for (Uri uri : list) {
                        if (isImageFile(uri)) {
                            imageFileUri = uri;
                            showPhoto(true, intent,true);
                        } else {
                            showOtherFiles(uri, mFileMimeType);
                        }
                    }
                }

                if (action.equals(Intent.ACTION_SEND)){
                    Bundle bundle = intent.getExtras();
                    Uri uri = (Uri)bundle.getParcelable(Intent.EXTRA_STREAM);
                    // for album overflow menu share, share by id
                    String mime_type = intent.getType();
                    boolean isImage = mime_type != null && mime_type.contains("image/");
                    if (isImage || isImageFile(uri)) {
                        imageFileUri = uri;
                        showPhoto(true, intent,false);
                    } else {
                        showOtherFiles(uri, mFileMimeType);
                    }
                }
            } else {
                if (action.equals(Intent.ACTION_SEND)){
                    showLink(intent, "from_other_app");
                }
            }
        }
    }

    private void parsePhotoIntent(Intent intent) {
        mExtraFlag = null == intent ? -1 : intent.getIntExtra(EXTRA_FLAG_KEY, -1);
        if (EXTRA_FLAG_CAMERA == mExtraFlag) {
            doTakePhoto();
        } else if (EXTRA_FLAG_GALLERY == mExtraFlag) {
            doPickPhotoFromGallery();
        } else if (EXTRA_FLAG_SERVER == mExtraFlag) {
            String fileUri = intent.getStringExtra(EXTRA_FILE_URI);
            photo_id = intent.getStringExtra(EXTRA_PHOTO_ID);
            addDataToList(getPhotoName(fileUri), fileUri, "", ComposeShareData.PHOTO_TYPE);
        }
    }

    @Override
    protected void onAccountLoginCancelled() {
        finish();
        if (finshListener != null && finshListener.get() != null) {
            finshListener.get().finishPhotoActivity();
        }
    }

//    @Override
//    protected void onAccountLoginDone() {
//        super.onAccountLoginDone();
//        initialComposeEnv();
//        initEnv(intent);
//    }

    private String mFileMimeType;
    private boolean isImageFile(Uri uri) {
        String tmpUri = getRealPathFromURI(uri, MediaStore.Images.Media.DATA);
        Log.d(TAG, "isImageFile() real path : tmpUri = " + tmpUri);

        if (TextUtils.isEmpty(tmpUri)) {
            tmpUri = uri.toString();
        }

        tmpUri = tmpUri.toLowerCase();
        mFileMimeType = FileInfo.mimeType(tmpUri);
        boolean isImageFile = false;
        Log.d(TAG, "isImageFile() mFileMimeType = " + mFileMimeType);
        if (mFileMimeType.contains("image/")) {
            isImageFile = true;
        }
        return isImageFile;
    }

    private void removeSucceedPost() {
        synchronized(mShareDataList){
            for (int i = 0; i < mShareDataList.size(); i++) {
                ComposeShareData data = mShareDataList.get(i);
                if (data.mStatus == ComposeShareData.SUCCEED_STATUS) {
                    mShareDataList.remove(i);
                    i--;
                }
            }
        }
    }

    private boolean hasVcard() {
        boolean hasVcard = false;
        if (mShareDataList.size() > 0) {
            for (ComposeShareData data: mShareDataList) {
                if (data.mType == ComposeShareData.VCARD_TYPE && data.mCommit != ComposeShareData.COMMIT_STATUS) {
                    hasVcard = true;
                    break;
                }
            }
        } else {
            Log.d(TAG, "hasVcard(), no restored data.");
        }

        return hasVcard;
    }

    private View initFirstHeaderView() {
        LayoutInflater factory = LayoutInflater.from(this);
        View convertView = factory.inflate(R.layout.bpc_compose_firsthead_ui, null);
        mChooseShareUserIV = (ImageView) convertView.findViewById(R.id.choose_share_user);
        mChooseShareUserIV.setOnClickListener(chooseShareUserClickListener);
        mShareTo = (AddressPadMini) convertView.findViewById(R.id.receiver_editor);
        mCircleSelectSpinner = (Spinner) convertView.findViewById(R.id.circle_select);
        mCircleSelectSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(RecipientsCirclesItemView.class.isInstance(view)) {
					RecipientsCirclesItemView item = (RecipientsCirclesItemView) view;
		        	Log.d(TAG, "select id " + item.getDataId());
		        	showCategoryUi(String.valueOf(item.getDataId()));
		        	
		        	if(item.getDataId() == QiupuConfig.CIRCLE_ID_PRIVACY) {
		        		// show input people text if select privacy
		        		mShareTo.setVisibility(View.VISIBLE);
		        		mShareTo.requestFocus();
		        	}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});        
        
        return convertView;
    }
    
    private View initSecondHeaderView() {
        LayoutInflater factory = LayoutInflater.from(this);
        View convertView = factory.inflate(R.layout.bpc_compose_secondhead_ui, null);
        mShareComment = (ConversationMultiAutoCompleteTextView) convertView.findViewById(R.id.share_comment);
        mLocationText = (TextView) convertView.findViewById(R.id.location_info);
        mLocationProgressBar = convertView.findViewById(R.id.location_progress_bar);
        mLocationIcon = (ImageView) convertView.findViewById(R.id.location_left_icon);
        mRemoveLocaitonIcon = (ImageView) convertView.findViewById(R.id.location_right_arrow);

        if (USE_OLD_LOCATION) {
            mRemoveLocaitonIcon.setOnClickListener(mLocationListener);
//            setLocationClickListener(mLocationIcon);
            View view = convertView.findViewById(R.id.location_container);
            setLocationClickListener(view);
        } else {
        	mLocationIcon.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.d(TAG, "do nothing ");
					
				}
			});
            mRemoveLocaitonIcon.setVisibility(View.GONE);
            mRemoveLocaitonIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    onLocationHidden();
                	launchPlacePicker();
                }
            });

            View locationContainer = convertView.findViewById(R.id.location_layout);
            setLocationClickListener(locationContainer);
            
            final View stream_content = convertView.findViewById(R.id.stream_content);
            
            mShareComment.setOnFocusChangeListener(new OnFocusChangeListener() {
    			
    			@Override
    			public void onFocusChange(View v, boolean hasFocus) {
    				if(hasFocus) {
    					stream_content.setBackgroundResource(R.drawable.textfield_activated_holo_light);
    				}else {
    					stream_content.setBackgroundResource(R.drawable.input_text_bg);
    				}
    				
    			}
    		});
        }
        return convertView;
    }
    
    private View initHeaderView(boolean isStreamRetweet) {
        LayoutInflater factory = LayoutInflater.from(this);
        View convertView = factory.inflate(R.layout.bpc_compose_top_ui, null);
        findHeaderViewById(convertView, isStreamRetweet);
        return convertView;
    }

    private void findHeaderViewById(View convertView, boolean isStreamRetweet) {
        if (isStreamRetweet) {
            post = QiupuHelper.deSerialization();
            mStreamContainer = (LinearLayout) convertView.findViewById(R.id.stream_container);
            SNSItemView st = StreamListAdapter.newStreamItemView(this, post, true);
            st.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            mStreamContainer.addView(st);
        }

        mAddFaceView = (ImageView) convertView.findViewById(R.id.face_insert);
        mAddAttachmentView = (ImageView) convertView.findViewById(R.id.add_attachment);
        mGridView = (GridView) convertView.findViewById(R.id.tweet_pub_faces);
        mBottomView = convertView.findViewById(R.id.bottom_layout);
        mFaceView = convertView.findViewById(R.id.face_layout);
        
        mSpanView = convertView.findViewById(R.id.divider_line1);
        
        /*mShareTo.setAddressWatcher(new AddressPadMini.AddressWatcher() {
            @Override
            public void onAccept(String text) {
//                Log.v(TAG, "onAccept: " + text);
                recipientChanged = true;
            }

            @Override
            public void onInput(String text) {
//                Log.v(TAG, "onInput: " + text);
//                recipientChanged = true;
            }

            @Override
            public void onRemove(String text) {
//                Log.v(TAG, "onRemove: " + text);
                recipientChanged = true;
            }
        });*/
//        if (isTop) {//isTop is false as default value
//            convertView.findViewById(R.id.share_ll).setVisibility(View.GONE);
//            setHeadTitle(R.string.compose_top_title);
//        } else {
            setHeadTitle(R.string.share_compose_title);
//        }

        if (isAdmin) {
//            mSendEmailStatus = true;

            sendEmail = (CheckBox) convertView.findViewById(R.id.send_email_checkbox);
            sendEmail.setVisibility(View.VISIBLE);
            sendEmail.setChecked(mSendEmailStatus);
            sendEmail.setOnClickListener(sendEmailListener);

            if (SUPPORT_SEND_MESSAGE) {
//                mSendSmsStatus = true;
                sendSms = (CheckBox) convertView.findViewById(R.id.send_sms_checkbox);
                sendSms.setVisibility(View.VISIBLE);
                sendSms.setChecked(mSendSmsStatus);
                sendSms.setOnClickListener(sendSmsListener);
            }
        }

//        mShareComment.setOnTouchListener(mTouchListener);
//        mShareTo.setOnTouchListener(mTouchListener);
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (mFaceView.getVisibility() == View.VISIBLE) {
					mFaceView.startAnimation(new ExpandAnimation(mFaceView, 200));
//					mIme.showSoftInput(mShareComment, 0);
//					mFaceView.setVisibility(View.GONE);
				}
			}
			return false;
		}
	};

    private boolean mSendEmailStatus = false;
    private boolean mSendSmsStatus = false;
    private View.OnClickListener sendEmailListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSendEmailStatus = !mSendEmailStatus;
            sendEmail.setChecked(mSendEmailStatus);
        }
    };

    private View.OnClickListener sendSmsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSendSmsStatus = !mSendSmsStatus;
            sendSms.setChecked(mSendSmsStatus);
        }
    };

    public boolean isShowNotification() {
        return true;
    }

//    private void handleSerialize(boolean serialize) {
//        new HandleSerializeTask(serialize).execute((Void[]) null);
//    }

//    private class HandleSerializeTask extends UserTask<Void, Void, Void> {
//
//        private boolean mSerialize = false;
//
//        public HandleSerializeTask(boolean serialize) {
//            mSerialize = serialize;
//        }
//
//        @Override
//        public Void doInBackground(Void... params) {
//            if (mSerialize) {
//                LocationUtils.serializeLocation();
//            } else {
//                LocationUtils.deSerializeLocation();
//            }
//            return null;
//        }
//    }

    private void startPlaceLocationPicker(Intent intent, int code) {
        FROM_GALLERY_OR_CAMERA_KEY = 1;
        startActivityForResult(intent, code);
    }

    private View.OnClickListener mLocationListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(QiupuComposeActivity.this, PickLocationActivity.class);
            if(StringUtil.isValidString(mBackLocation)) {
            	intent.putExtra(LocationUtils.CURRENT_GEO, mBackLocation);
            }
            LocationUtils.encodeListExtra(intent);
            startPlaceLocationPicker(intent, STATUS_LOCATION_RESULT);
        }
    };

    private void setLocationClickListener(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (USE_OLD_LOCATION) { // todo: set it false while PickPlaceActivity is ready.
//                    setLocationClickAction();
//                } else {
//                    launchPlacePicker();
//                }
                
                setLocationClickAction();
            }
        });
    }

    private void launchPlacePicker() {
//        if (!ToastUtil.testValidConnectivity(this)) {
//            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
//            return;
//        }

        Intent intent = new Intent();
        intent.setClass(QiupuComposeActivity.this, PickPlaceActivity.class);

        if (!TextUtils.isEmpty(locationString)) {
            LocationUtils.encodeCurrentExtra(intent, "", locationString);
        }

        startPlaceLocationPicker(intent, STATUS_PLACE_RESULT);
    }

    private void setLocationClickAction() {
        if (mIsLocationDisplayed) {
//            LocationUtils.deactivateLocationService(this);
//            onLocationHidden();
        	launchPlacePicker();
        } else if(mLocationProgressBar.getVisibility() == View.VISIBLE) {
        	LocationUtils.deactivateLocationService(this);
        	onLocationHidden();
        }else {
            checkGpsSetting();
        }
    }

    private void checkGpsSetting() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            boolean gpsOpen = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkOpen = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (gpsOpen || networkOpen) {
            	gotoLoadLocation();
            } else {
                DialogUtils.showOpenGPSDialog(this);
            }
        }
    }
    
    private void gotoLoadLocation() {
    	LocationUtils.activateLocationService(this);
    	mLocationIcon.setVisibility(View.GONE);
        mLocationText.setVisibility(View.GONE);
        mLocationProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void locationUpdated(String mapUrl, String locString) {
        Log.d(TAG, "locationUpdated() locString = " + locString);
        mBackLocation = locString;
        setLocation(mapUrl, locString);
    }

    private void setLocation(String url, String address) {
        if (mLocationText != null) {
            setLocationSucceed();
            //TODO
//            if (USE_OLD_LOCATION) {
//                mLocationText.setMovementMethod(LinkMovementMethod.getInstance());
//                mLocationText.setLinksClickable(true);
//            }

            mRemoveLocaitonIcon.setVisibility(View.VISIBLE);
            mLocationText.setText(Html.fromHtml(url));
            LocationUtils.saveLocations(url, address);
            locationString = address;
            HttpClientImpl.setLocation(address);
        }
    }

    protected void getGLocationFailed() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ImageView toggle_location = (ImageView) findViewById(R.id.toggle_location);
                if (toggle_location != null) {
                    toggle_location.performClick();
                    showCustomToast(R.string.get_location_failed);
                }
            }
        });
    }

    private void setLocationSucceed() {
        mIsLocationDisplayed = true;
        mLocationIcon.setImageResource(R.drawable.location_blue);
        mLocationIcon.setVisibility(View.VISIBLE);
        mLocationText.setVisibility(View.VISIBLE);
        mLocationProgressBar.setVisibility(View.GONE);
    }

    private View.OnClickListener mToggleLocationListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setLocationClickAction();
        }
    };

    private View.OnClickListener mToggleLinkListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FROM_GALLERY_OR_CAMERA_KEY = 1;
            launchBrowserBookmark();
        }
    };

    private void toggleAppPicker() {
        View toggle = findViewById(R.id.toggle_app);
        if (null != toggle) {
            FROM_GALLERY_OR_CAMERA_KEY = 1;
            toggle.performClick();
        }
    }
    private View.OnClickListener mToggleAppListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FROM_GALLERY_OR_CAMERA_KEY = 1;
            launchAppBox();
        }
    };

    private View.OnClickListener mToggleContactListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FROM_GALLERY_OR_CAMERA_KEY = 1;
            launchMockContact();
        }
    };

    private View.OnClickListener mToggleFileListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FROM_GALLERY_OR_CAMERA_KEY = 1;
            launchPickFileActivity();
        }
    };

    private void launchPickFileActivity() {
//        Intent intent = new Intent();
//        intent.setType("*/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(intent, STATUS_FILE_RESULT);
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.string_select)),
                    STATUS_FILE_RESULT);
        } catch (Exception e) {
            Log.d(TAG, "launchPickFileActivity, exception = " + e.getMessage());
        }
    }

    private int FROM_GALLERY_OR_CAMERA_KEY = 0;
    private View.OnClickListener mTogglePhotoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DialogUtils.ShowPhotoPickDialog(QiupuComposeActivity.this, R.string.share_photo_title, new DialogUtils.PhotoPickInterface() {
                    @Override
                    public void doTakePhotoCallback() {
                        FROM_GALLERY_OR_CAMERA_KEY = 1;
                        doTakePhoto();
                    }

                    @Override
                    public void doPickPhotoFromGalleryCallback() {
                        FROM_GALLERY_OR_CAMERA_KEY = 1;
                        doPickPhotoFromGallery();
                    }
                });
        	
//        	Intent intent = new Intent(QiupuComposeActivity.this,PhotoSelectActivity.class);
//        	startActivity(intent);
        }
    };

    private void doPickPhotoFromGallery() {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setData(Uri.parse("content://media/internal/images/media"));
            intent.setType("image/*");
//            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, CAMERA_WITH_DATA);
        }
        catch (ActivityNotFoundException e) {
            Log.d(TAG, "doPickPhotoFromGallery() ActivityNotFoundException e.getMessage() = " + e.getMessage());
        }
    }

    private boolean no_sdcard = false;

    private void doTakePhoto() {
        try {
            String esStatus = Environment.getExternalStorageState();
            if (esStatus.equals(Environment.MEDIA_MOUNTED) == false) {
                no_sdcard = true;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,PHOTO_PICKED_WITH_DATA);
            } else {
                no_sdcard = false;
                QiupuHelper.PHOTO_DIR.mkdirs();
                mCurrentPhotoFile = new File(QiupuHelper.PHOTO_DIR, QiupuHelper.getPhotoFileName());
                imageFileUri = Uri.fromFile(mCurrentPhotoFile);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,imageFileUri);
                startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
            }
        }
        catch (ActivityNotFoundException e) {
            Log.d(TAG, "doTakePhoto() ActivityNotFoundException e.getMessage() = " + e.getMessage());
        }
	}

    private void launchBrowserBookmark() {
        Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        PackageManager pm = this.getPackageManager();
        List<ResolveInfo> list = new ArrayList<ResolveInfo>();
        list.addAll(pm.queryIntentActivities(intent, 0));
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            String packagename = info.activityInfo.packageName;
            if (packagename.indexOf("browser") >= 0) {
                intent.setComponent(new ComponentName(packagename,info.activityInfo.name));
                startActivityForResult(intent, STATUS_LINK_RESULT);
                break;
            }
        }
    	
//    	Intent intent = new Intent(this,LinkSelectActivity.class);
//    	startActivityForResult(intent, STATUS_LINK_RESULT);
    }

    private void launchAppBox() {
        Intent intent = IntentUtil.getAppPickerIntent(this);
        if (BpcApiUtils.isActivityReadyForIntent(this, intent)) {
            startActivityForResult(intent, STATUS_APPLICATION_RESULT);
        } else {
            IntentUtil.startApplicationBoxActivity(this);
        }
    }

    private void launchMockContact() {
        Intent intent = IntentUtil.getVCardPickerIntent(this);
        startActivityForResult(intent, STATUS_PEOPLE_RESULT);
    }

    private void showConditionLog() {
        Log.d(TAG, "hasVcard() = " + hasVcard() 
                + ", privateShare = " + privateShare 
                + ", address.length = " + mShareTo.getAddressesArray().length
                + ", mShareDataList.size() = " + mShareDataList.size()
                + ", hasPureTextOrLocaiton() = " + hasPureTextOrLocaiton()
                + ", isNoChange() = " + isListNoChange());
    }

    private View.OnClickListener shareAction = new View.OnClickListener() {
        public void onClick(View v) {
            if (ensureAccountLogin()) {
                if (post == null) {
                    final String content = getComposingText();
                    String recipient = buildRecipient();
                    showConditionLog();

                    //condition: no vCard ----> directly post
                    //case1. a new post && no failed post  ---> mShareDataList.size() <= 0
                    //case2. has failed post && (noChange) ---> list && comment && location && toUsers (all is no change)

                    if (hasVcard() && (privateShare == false || (recipient.contains(PUBLIC_SHARE_TAG) == false && mShareTo.getAddressesArray().length == 0))) {
                        showCustomToast(R.string.share_message2);
                        return;
                    } else if (privateShare == true && recipient.length() == 0 
                            && (mShareDataList.size() <= 0 || hasPureTextOrLocaiton() || isListNoChange() == false)) {
                    	showGotoPickDialog();
//                        showCustomToast(R.string.invalid_recipient);
                        return;
                    } else if (validateComposeContent(content, recipient)) {
                    	// add "reffred_count" for circles/users
                    	new ChangeReffredCountTask(recipient).execute((Void[]) null);
//                    	changeReffredCount(mShareTo.getAddressesArray());
                    	
                        shareAppAndOtherInfo();
                    } else {
                        Log.d(TAG, "shareAction ----> skip empty compose content.");
                    }
                } else {
                	final String recipient = buildRecipient();
                	if (privateShare == true && recipient.length() == 0) {
                		showGotoPickDialog();
//                        showCustomToast(R.string.invalid_recipient);
                        return;
                    }else {
                    	final String content = buildRetweetContent();
                    	if (validateComposeContent(content, recipient)) {
                    		// add "reffred_count" for circles/users
                        	new ChangeReffredCountTask(recipient).execute((Void[]) null);
                    		
                    		final String postId = post.isRetweet() ? post.retweet.post_id : post.post_id;
                    		retweet(postId, recipient, content, mIsCommentAllowed, mIsLikeAllowed, mIsShareAllowed, privateShare);
                    	} else {
                    		Log.d(TAG, "skip empty compose content.");
                    	}
                    }
                }
            }
        }
    };
    
    private void showGotoPickDialog() {
//    	DialogUtils.showConfirmDialog(QiupuComposeActivity.this, R.string.invalid_recipient,
//                R.string.dlg_message_add_recipient, R.string.label_ok, R.string.label_cancel,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
                        startPickActivity();
//                    }
//                });
    }

    @Override
	protected void retweetCallback(boolean suc)
	{
		if(suc)
		{
			Toast.makeText(QiupuComposeActivity.this, R.string.share_apk_ok, Toast.LENGTH_SHORT).show();
			finish();
		}
	}

//    public final void checkLogin() {
//        Log.d(TAG, "checkLogin");
//
//        BorqsAccount account = AccountServiceUtils.getBorqsAccount();
//        Log.d(TAG, "checkLogin account: " + account);
//        if (account == null || StringUtil.isEmpty(account.sessionid)) {
//            gotoLogin();
//        } else {
////            setSaveAccount(account);
//            Intent qsintent = new Intent(this, QiupuService.class);
//            startService(qsintent);
//
//            UserAccountObserver.login();
//        }
//    }

    @Override
    public void onLogin() {
        super.onLogin();

        dumpCallStack();

        initialComposeEnv();
        initEnv();

        startService(new Intent(this, QiupuService.class));
    }

    private static final int STATUS_LINK_RESULT = 1001;
    private static final int STATUS_APPLICATION_RESULT = 1002;
    private static final int STATUS_PEOPLE_RESULT = 1003;
    private static final int STATUS_LOCATION_RESULT = 1004;
    private static final int STATUS_PLACE_RESULT = 1005;
    private static final int STATUS_FILE_RESULT = 1006;

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        Log.d(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode + ",intent = " + intent);

//        if (intent == null) {
//        	Log.d(TAG, "intent is null");
//            return;        	
//        }

        if (resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "mExtraFlag = " + mExtraFlag + ", FROM_GALLERY_KEY = " + FROM_GALLERY_OR_CAMERA_KEY);
            if (mExtraFlag == EXTRA_FLAG_CAMERA || mExtraFlag == EXTRA_FLAG_GALLERY || (intent == null && FROM_GALLERY_OR_CAMERA_KEY != 1)) {
                finish();
            }

            return;
        }
        switch (requestCode) {
            case userselectcode: {
                String usersAddress = intent.getStringExtra(PickAudienceActivity.RECEIVE_ADDRESS);
                mSelectUserCircleMap.clear();
                HashMap<Long, String> tmpMap = (HashMap<Long, String>) intent.getSerializableExtra(PickAudienceActivity.RECEIVE_SELECTUSERCIRCLE_NAME);
                mSelectUserCircleMap.putAll(tmpMap);
                
                if(!TextUtils.isEmpty(usersAddress)) {
                	mShareTo.setVisibility(View.VISIBLE);
                }else {
                	mShareTo.setVisibility(View.GONE);
                }
                Log.d(TAG, "onActivityResult address: " + usersAddress);
                mShareTo.setAddresses(usersAddress);
//                setLastRecipient(sbUsers.toString());
                break;
            }
            case STATUS_LINK_RESULT: {
                showLink(intent, "from_bookmark");
                break;
            }
            case STATUS_APPLICATION_RESULT: {
                showApp(intent);
                break;
            }
            case STATUS_PEOPLE_RESULT: {
                showContact(intent);
                break;
            }
            case CAMERA_WITH_DATA: {
                if (intent != null) {
                    imageFileUri = intent.getData();
                    showPhoto(true, intent,false);
                } else {
                    Log.d(TAG, "intent is null");
                }
            	break;
            }
            case PHOTO_PICKED_WITH_DATA: {
                showPhoto(false, intent,false);
                break;  
            }
            case STATUS_PLACE_RESULT:
                // fall through as STATUS_LOCATION_RESULT
            case STATUS_LOCATION_RESULT: {
                showLocation(intent);
                break;
            }
            
            case PROCESS_PHOTO: {
                String oldPath = mCurrentPhotoFile.getPath();
            	String title = intent.getStringExtra("title");
            	mCurrentPhotoFile = (File)intent.getSerializableExtra("file");
            	addDataToList(title, oldPath, "", ComposeShareData.PHOTO_TYPE);
            	break;
            }
            case STATUS_FILE_RESULT: {
                parseFileUri(intent);
                break;
            }
            default:
        }
    }

    private void showLocation(Intent intent) {
        String url  = intent == null ? "" : LocationUtils.decodeCurrentLocation(intent);
        String address = intent == null ? "" : LocationUtils.decodeCurrentGeo(intent);
        if (TextUtils.isEmpty(url)) {
//            setLocationStatus(true);
            onLocationHidden();
            return;
        } else {
            setLocation(url, address);
        }
    }

    private void showPhoto(boolean isGallery, Intent intent,boolean isMultiplePhoto) {
        if (imageFileUri == null && no_sdcard == false) {
            Log.d(TAG, "showPhoto() imageFileUri is null ");
            return;
        }

        String tmpUri = null;
        String title = null;
        if(isGallery) {
            tmpUri = getRealPathFromURI(imageFileUri, MediaStore.Images.Media.DATA);
            if (TextUtils.isEmpty(tmpUri)) {
                tmpUri = imageFileUri.getPath();
                title = getPhotoName(tmpUri);
            } else {
                mCurrentPhotoFile = new File(tmpUri);
                title = mCurrentPhotoFile.getName();
            }
        }else {
            if (no_sdcard) {
                Bitmap photo = intent.getParcelableExtra("data");
                mCurrentPhotoFile = createFileFromBitmap(photo);

                photo.recycle();
                photo = null;

                imageFileUri = Uri.fromFile(mCurrentPhotoFile);
                tmpUri = imageFileUri.getPath();
                title = getPhotoName(tmpUri);
            } else {
                tmpUri = imageFileUri.getPath();
                title = getPhotoName(tmpUri);
            }
        }

        if(isMultiplePhoto) {
	        addDataToList(title, tmpUri, MULTI_SHARE_PHOTO, ComposeShareData.PHOTO_TYPE);
        } else {
            addDataToList(title, tmpUri, SINGLE_SHARE_PHOTO, ComposeShareData.PHOTO_TYPE);
        }
    }

    private void startProcessPhotoActivity(String tmpUri, String title) {
        Intent processIntent = new Intent(this,PhotoProcessActivity.class);
        processIntent.putExtra("photo_path", tmpUri);
        processIntent.putExtra("title", title);
        startActivityForResult(processIntent, PROCESS_PHOTO);
    }

    private File other;
    private File screenShotFile;
    private void showOtherFiles(Uri uri, String mimeType) {
        if (uri == null) {
            Log.d(TAG, "showOtherFiles() uri is null");
            return;
        }

        String tmpPath = getRealPathFromURI(uri, MediaStore.Video.Media.DATA);
        if (TextUtils.isEmpty(tmpPath)) {
            tmpPath = uri.getPath();
        }

        Log.d(TAG, "showOtherFiles() tmpPath = " + tmpPath + " uri = " + uri.toString());
        other = new File(tmpPath);
        if (other.length() > MAX_FILE_SIZE) {
            Toast.makeText(this, String.format(getString(R.string.max_file_size_tip), MAX_FILE_SIZE), Toast.LENGTH_SHORT).show();
            return;
        }
        String title = other.getName();

        if (TextUtils.isEmpty(mimeType) == false && mimeType.contains("video/")) {
            Bitmap screen_shot = ThumbnailUtils.createVideoThumbnail(tmpPath, Images.Thumbnails.MINI_KIND);
            screenShotFile = createFileFromBitmap(screen_shot);
            screen_shot = null;
        }

        addDataToList(title, tmpPath, mimeType, ComposeShareData.OTHER_TYPE);
    }

    private File createFileFromBitmap(Bitmap screen_shot) {
        if (screen_shot != null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            screen_shot.compress(CompressFormat.PNG, 100,os);
            byte[] bytes = os.toByteArray();
    
            QiupuHelper.PHOTO_DIR.mkdirs();
            File file = new File(QiupuHelper.getTmpCachePath());
            File tmp = new File(file, QiupuHelper.getPhotoFileName());
    
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(tmp);
                fos.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return new File(tmp.getPath());
        } else {
            return null;
        }
    }

    private String getPhotoName(String tmpUri) {
        if (!TextUtils.isEmpty(tmpUri)) {
            mCurrentPhotoFile = new File(tmpUri);
            return mCurrentPhotoFile.getName();
        } else {
            mCurrentPhotoFile = null;
            imageFileUri = null;
            return "";
        }
    }

    private String getRealPathFromURI(Uri contentUri, String projection) {
        String[] proj = { projection };

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                Log.d(TAG, "getRealPathFromURI() cursor is null or cursor.getCount() == 0.");
                return null;
            } else {
                cursor.moveToFirst();
                int column_index = cursor.getColumnIndexOrThrow(projection);
                return cursor.getString(column_index);
            }
        } finally {
            QiupuORM.closeCursor(cursor);
        }
    }

    private void showLink(Intent intent, String fromBookMark) {
        String link_title = "";
        String link_url = "";
        Log.d(TAG, "showLink() fromBookMark = " + fromBookMark + " flag = " + "from_bookmark".equals(fromBookMark));
        if ("from_bookmark".equals(fromBookMark)) {
            Intent shortIn = (Intent)intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
            link_title = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
            link_url = shortIn.getData().toString();
//            link_url = intent.getData().toString();
            mFavicon = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
            
            addDataToList(link_title, link_url, fromBookMark, ComposeShareData.LINK_TYPE);
        } else {
            Bundle bundle = intent.getExtras();
            if (null != bundle) {
//                link_url = bundle.getString(Intent.EXTRA_TEXT);
                CharSequence cs = bundle.getCharSequence(Intent.EXTRA_TEXT);
                if (cs != null) {
                    link_url = cs.toString();
                }
                link_title = bundle.getString(Intent.EXTRA_SUBJECT);
                Log.d(TAG, "showLink() link_url = " + link_url);
                mFavicon = intent.getParcelableExtra(SHARE_FAVICON);
            }
            if (SNSItemView.isURL(link_url) == false) {
            	// set to comment content as extra text is not valid url.
            	String comment = TextUtils.isEmpty(link_title) ? link_url
            			: link_title + "\n" + link_url;
            	mShareComment.setText(comment);
            	return;
            } else {
            	addDataToList(link_title, link_url, fromBookMark, ComposeShareData.LINK_TYPE);
            }
        }

    }

    private boolean addDataToList(String title, String url, String version, int type) {
    	
    	if(mSpanView != null) {
    		mSpanView.setVisibility(View.VISIBLE);
    	}
        ComposeShareData shareData = new ComposeShareData();
        shareData.mTitle = title;
        shareData.mUrl = url;
        shareData.mType = type;

        String message = getResources().getString(R.string.repeat_message);
        String repeatSelectMsg = "";
        if (type == ComposeShareData.LINK_TYPE) {
            repeatSelectMsg = String.format(message, getResources().getString(R.string.attach_link));
            shareData.mFavIcon = mFavicon;

        } else if (type == ComposeShareData.APK_TYPE) {
            repeatSelectMsg = String.format(message, getResources().getString(R.string.attach_app));

            shareData.mVersion = version;
            shareData.mApkServerId = mApkResponse.apk_server_id;
            shareData.mAPkPackageName = mApkResponse.packagename;

        } else if (type == ComposeShareData.PHOTO_TYPE) {
            repeatSelectMsg = String.format(message, getResources().getString(R.string.attach_photo));

            shareData.mFile = mCurrentPhotoFile;            
            shareData.mVersion = version;

        } else if (type == ComposeShareData.VCARD_TYPE) {
            repeatSelectMsg = String.format(message, getResources().getString(R.string.attach_people));

            shareData.mAppData = getAppData();
            shareData.mVersion = version;
        } else if (type == ComposeShareData.OTHER_TYPE) {
            repeatSelectMsg = String.format(message, getResources().getString(R.string.attach_file));
            shareData.mFile = other;
            shareData.mVersion = version;
            shareData.mScreenShotFile = screenShotFile;
        }

        for (ComposeShareData data: mShareDataList) {
            Log.d(TAG, "data.toString() = " + data.toString());
        }
        Log.d(TAG, "shareData = " + shareData.toString());

//        if ("from_bookmark".equals(version) == false) {
//            mShareDataList.add(shareData);
//            mComposeShareAdapter.alterDataList(mShareDataList);
//            return true;
//        } else {
            if (mShareDataList.contains(shareData)) {
            	Log.d(TAG, "list contains this sharedata ");
//                showCustomToast(repeatSelectMsg);
                return false;
            } else {
                if (type == ComposeShareData.PHOTO_TYPE && SINGLE_SHARE_PHOTO.equals(version)) {
                    startProcessPhotoActivity(url, title);
                } else {
                	synchronized(mShareDataList)
                	{
	                    mShareDataList.add(shareData);
	                    mComposeShareAdapter.alterDataList(mShareDataList);
                	}
                }
                return true;
            }
//        }
    }

    private void showApp(Intent intent) {
        // single pick
        if (intent.getSerializableExtra(QiupuMessage.BUNDLE_APKINFO) != null) {
            mApkResponse = (ApkResponse) intent.getSerializableExtra(QiupuMessage.BUNDLE_APKINFO);
            addDataToList(mApkResponse.label, mApkResponse.iconurl, mApkResponse.versionname, ComposeShareData.APK_TYPE);
        }

        // multi pick
        if (intent.getSerializableExtra("apk_response") != null) {
            ArrayList<ApkResponse> apkList = (ArrayList<ApkResponse>) intent.getSerializableExtra("apk_response");
            for (int i = 0, len = apkList.size(); i < len; i++) {
                mApkResponse = apkList.get(i);
                addDataToList(mApkResponse.label, mApkResponse.iconurl, mApkResponse.versionname, ComposeShareData.APK_TYPE);
            }
        }
    }

    private String mContactInfoStr = null;
    private void showContact(Intent intent) {
        Bundle bundle = intent.getBundleExtra(QiupuMessage.BUNDLE_SHARE_CONTACT);
        mContactInfo= (ContactSimpleInfo) bundle.getSerializable(QiupuMessage.BUNDLE_SHARE_CONTACT_INFO);

        ArrayList<String> phonesList = ContactUtils.getPhonesList(this, mContactInfo.mContactId);
        ArrayList<String> emailsList = ContactUtils.getEmailsList(this, mContactInfo.mContactId);

        // we don't care borqsId is null or not.
        mContactInfoStr = JSONUtil.createPhoneAndEmailJSONArray(-1, mContactInfo.display_name_primary, phonesList, emailsList);

        boolean result = addDataToList(mContactInfo.display_name_primary, String.valueOf(mContactInfo.mContactId), String.valueOf(mContactInfo.mPhotoId), ComposeShareData.VCARD_TYPE);

        if (result == false) {
            return;
        }

        Log.d(TAG, "====== showContact() ======= privateShare = " + privateShare);
        if(privateShare == false) {
            DialogUtils.showOKDialog(this, -1, R.string.has_vcard_tips, null);
        }
        privateShare = true;
        String[] ids = mShareTo.getAddressesArray();
        StringBuilder newAddress = new StringBuilder();
        for(int i=0; i<ids.length; i++ ) {
            if(ids[i].equals(QiupuConfig.CIRCLE_NAME_PUBLIC) == false) {
                if(newAddress.length() > 0) {
                    newAddress.append(",");
                }
                newAddress.append(ids[i]);
            }
        }
        mShareTo.setAddresses(newAddress.toString());
    }

    private void initAddress() {
    	mShareTo.setEnabled(true);
    	mShareTo.requestFocus();
    	mChooseShareUserIV.setVisibility(View.VISIBLE);
//        mShareTo.setAdapter(new RecipientsAdapter(this));
//        mShareTo.setOnDecorateAddressListener(new FBUDecorater());
        StringBuilder ads = new StringBuilder();
        if(privateShare == false) {
            ads.append(QiupuConfig.CIRCLE_NAME_PUBLIC);
        }
        if (receivers != null) {
//        if (receivers == null) {
//            final String recipients = getDefaultRecipients();
//            if (!TextUtils.isEmpty(recipients)) {
//                ads.append(recipients);
//            }
//        } else {
//            if(privateShare == false) {
//                ads.append(QiupuConfig.CIRCLE_NAME_PUBLIC);
//            }
            if(ads.length() > 0) {
                ads.append(",");
            }
            ads.append(receivers);
//            mShareTo.setEnabled(false);
//            mChooseShareUserIV.setVisibility(View.GONE);
//            if(receivers.startsWith("#")) {
//            	int index = receivers.indexOf("#");
//                String tmpid = receivers.substring(index + 1, receivers.length());
//                try {
//                	mFromId = Long.parseLong(tmpid); 
//				} catch (Exception e) {
//					Log.d(TAG, "from is not circle/event. ");
//				}
//            }
        }

        if(ads.toString().length() > 0) {
        	mShareTo.setVisibility(View.VISIBLE);
        	mShareTo.setAddresses(ads.toString());
        }
//        setLastRecipient(ads.toString());
    }

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
                        return CircleUtils.getCircleName(QiupuComposeActivity.this, uc.circleid, uc.name);
                    } else {
                        if (mReceiveMap != null && mReceiveMap.get(suid) != null) {
                            return mReceiveMap.get(suid);
                        }else if(mSelectUserCircleMap != null && mSelectUserCircleMap.get(Long.parseLong(suid)) != null){
                        	return mSelectUserCircleMap.get(Long.parseLong(suid));
                        }
                    }
                } else if (suid.contains("*")) {
                    int index = suid.indexOf("*");
                    suid = suid.substring(index + 1, suid.length());
                    PhoneNumberEmailDecorater number = new AddressPadMini(
                            QiupuComposeActivity.this).new PhoneNumberEmailDecorater();
                    return number.onDecorate(suid);
                } else {
//                	String username = orm.queryUserName(Long.valueOf(suid));
                	String username = orm.queryEmployeeName(mScene, Long.valueOf(suid));
                	
                	if(username != null) {
                		return username;
                	}else {
                		if(mReceiveMap != null && mReceiveMap.get(suid) != null) {
                			return mReceiveMap.get(suid);
                		}else if(mSelectUserCircleMap != null && mSelectUserCircleMap.get(Long.parseLong(suid)) != null){
                			return mSelectUserCircleMap.get(Long.parseLong(suid));
                		}
                	}
                }
            } catch (Exception ne) {
            	return null;
            }
            return address;
        }
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    public static final int SHARE_END = 1;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHARE_END:
                    synchronized (mLock) {
                        inProcess = false;
                    }

                    if (!msg.getData().getBoolean(RESULT, false)) {
                        //failed, then save failed post info
                        showCustomToast(msg.getData().getString(ERROR_MSG));
                    } else {
                        //succeed,then save succeed post info
                        showCustomToast(R.string.share_apk_ok);
                        HttpClientImpl.setLocation("");
                    }

                    ComposeShareData data = (ComposeShareData)msg.getData().getSerializable("data");
                    QiupuHelper.refreshItemUI(data);

                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }
    }

    private void setLocation(String location) {
        Log.d("TAG", "location = " + location);
        if (TextUtils.isEmpty(location) == false) {
            HttpClientImpl.setLocation(location);
        } else {
            HttpClientImpl.setLocation("");
        }
    }

    private void shareLink(final ComposeShareData data) {
        if (!AccountServiceUtils.isAccountReady()) {
            return;
        }

        if (data.mIsPrivate && data.mRecipient.length() <= 0) {
            showCustomToast(R.string.privacy_show);
            requestFocusForShareTo();
            return;
        }

        if (TextUtils.isEmpty(data.mTitle) || "null".equals(data.mTitle)) {
            data.mTitle = null;
        }

        setLocation(data.mLocaiton);

        data.mStatus = ComposeShareData.UPLOADING_STATUS;
        data.mCommit = ComposeShareData.COMMIT_STATUS;
        asyncQiupu.postLinkAsync(getSavedTicket(), getSaveUid(), data.mRecipient,
                data.mMessage, data.mTitle, data.mUrl, data.mIsPrivate,
                data.mAllowComment, data.mAllowLike, data.mAllowShare, data.mIsTop,
                data.mSendEmail, data.mSendSms, data.mCategoryId, new TwitterAdapter() {
                    public void postLink(Stream post) {
                        Log.d(TAG, "share link succeed =" + post);

                        data.mStatus = ComposeShareData.SUCCEED_STATUS;
                        Message msg = mHandler.obtainMessage(SHARE_END);
                        msg.getData().putBoolean(RESULT, true);
                        msg.getData().putSerializable("data", data);
                        msg.sendToTarget();
                    }

                    public void onException(TwitterException ex,
                            TwitterMethod method) {
                        Log.d(TAG, "share link failed =" + post);

                        TwitterExceptionUtils.printException(TAG, "shareLink, server exception:", ex, method);

                        data.mStatus = ComposeShareData.FAILED_STATUS;
                        Message msg = mHandler.obtainMessage(SHARE_END);
                        msg.getData().putString(ERROR_MSG, ex.getMessage());
                        msg.getData().putBoolean(RESULT, false);
                        msg.getData().putSerializable("data", data);
                        msg.sendToTarget();
                    }
                });

    }

    private void shareApk(final ComposeShareData data) {
        if (data == null) {
            if (QiupuConfig.LOGD) Log.d(TAG, "postShare data is null, return");
            return;
        }

        if (!ensureAccountLogin()) {
            if (QiupuConfig.LOGD) Log.d(TAG, "postShare without login account, return");
            return;
        }

        if (QiupuConfig.LOGD)  Log.d(TAG, "====== postShare");

        if (data.mApkServerId.length() <= 0) {
            if (QiupuConfig.LOGD) Log.d(TAG, "postShare intentApk.apk_server_id is 0, return");
        }

        if (data.mIsPrivate && data.mRecipient.length() <= 0) {
            showCustomToast(R.string.privacy_show);
            return;
        }

        setLocation(data.mLocaiton);

        data.mStatus = ComposeShareData.UPLOADING_STATUS;
        data.mCommit = ComposeShareData.COMMIT_STATUS;
        asyncQiupu.postQiupuShare(getSavedTicket(), data.mRecipient,
                data.mMessage, BpcApiUtils.APK_POST, data.mApkServerId,
                data.mAPkPackageName, data.mIsPrivate, data.mIsTop,
                data.mSendEmail, data.mSendSms, data.mCategoryId, new TwitterAdapter() {
            public void postQiupuShare(Stream post) {
                Log.d(TAG, "share apk succeed =" + post);

                data.mStatus = ComposeShareData.SUCCEED_STATUS;
                Message msg = mHandler.obtainMessage(SHARE_END);
                msg.getData().putBoolean(RESULT, true);
                msg.getData().putSerializable("data", data);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex,
                    TwitterMethod method) {
                Log.d(TAG, "share apk failed =" + post);
                Log.d(TAG, "getApkDetailInformation exception:" + ex.getMessage());

                data.mStatus = ComposeShareData.FAILED_STATUS;
                Message msg = mHandler.obtainMessage(SHARE_END);
                msg.getData().putBoolean(RESULT, false);
                msg.getData().putSerializable("data", data);
                msg.getData().putString(ERROR_MSG, ex.getMessage());
                msg.sendToTarget();
            }
        });
    }

    private void sharePhoto(final ComposeShareData data) {
        if (!AccountServiceUtils.isAccountReady()) {
            return;
        }

        if (data.mIsPrivate && data.mRecipient.length() <= 0) {
            showCustomToast(R.string.privacy_show);
            requestFocusForShareTo();
            return;
        }

        setLocation(data.mLocaiton);

        data.mStatus = ComposeShareData.UPLOADING_STATUS;
        data.mCommit = ComposeShareData.COMMIT_STATUS;

        asyncQiupu.postPhotoAsync(getSavedTicket(), data.mMessage,
                data.mRecipient, data.mFile, photo_id, "", data.mAppData,
                data.mIsPrivate, data.mAllowComment,
                data.mAllowLike, data.mAllowShare, data.mIsTop, 
                data.mSendEmail, data.mSendSms, data.mCategoryId, new TwitterAdapter() {
                    public void photoShare(Stream post) {
                        Log.d(TAG, " share photo succeed =" + post);
                        if(data.mFile != null) {
                            if(QiupuHelper.getTmpCachePath().endsWith(data.mFile.getPath())
                                    && data.mFile.getName().startsWith(PhotoProcessActivity.IMG_COMPRESS)) {
                                data.mFile.delete();
                                data.mFile = null;
                            }
                        }
                        data.mStatus = ComposeShareData.SUCCEED_STATUS;
                        Message msg = mHandler.obtainMessage(SHARE_END);
                        msg.getData().putSerializable("data", data);
                        msg.getData().putBoolean(RESULT, true);
                        msg.sendToTarget();
                    }

                    public void onException(TwitterException ex, TwitterMethod method) {
                        Log.d(TAG, " share photo failed =" + post);
                        TwitterExceptionUtils.printException(TAG, "postPhotoAsync, server exception:", ex, method);

                        data.mStatus = ComposeShareData.FAILED_STATUS;
                        Message msg = mHandler.obtainMessage(SHARE_END);
                        msg.getData().putString(ERROR_MSG, ex.getMessage());
                        msg.getData().putSerializable("data", data);
                        msg.getData().putBoolean(RESULT, false);
                        msg.sendToTarget();
                    }
                });

    }

    private void requestFocusForShareTo() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mShareTo.requestFocus();
            }
        });
    }

    private void postToWall(final ComposeShareData data) {
        if (!AccountServiceUtils.isAccountReady()) {
            return;
        }

        if (data.mIsPrivate && data.mRecipient.length() <= 0) {
            showCustomToast(R.string.privacy_show);
            requestFocusForShareTo();
            return;
        }

        setLocation(data.mLocaiton);

        data.mStatus = ComposeShareData.UPLOADING_STATUS;
        data.mCommit = ComposeShareData.COMMIT_STATUS;
        asyncQiupu.postToMultiWallAsync(getSavedTicket(), data.mRecipient, data.mMessage,
                data.mAppData, data.mIsPrivate, data.mAllowComment, data.mAllowLike,
                data.mAllowShare, data.mIsTop, data.mSendEmail, data.mSendSms, data.mCategoryId, new TwitterAdapter() {
                public void postToWall(Stream post) {
                    Log.d(TAG, "share vcard or pure text succeed =" + post);

                    data.mStatus = ComposeShareData.SUCCEED_STATUS;
                    Message msg = mHandler.obtainMessage(SHARE_END);
                    msg.getData().putBoolean(RESULT, true);
                    msg.getData().putSerializable("data", data);
                    msg.sendToTarget();
                }

                public void onException(TwitterException ex,
                        TwitterMethod method) {
                    Log.d(TAG, "share vcard or pure text failed =" + post);
                    TwitterExceptionUtils.printException(TAG, "postToWall, server exception:", ex, method);

                    data.mStatus = ComposeShareData.FAILED_STATUS;
                    Message msg = mHandler.obtainMessage(SHARE_END);
                    msg.getData().putSerializable("data", data);
                    msg.getData().putString(ERROR_MSG, ex.getMessage());
                    msg.getData().putBoolean(RESULT, false);
                    msg.sendToTarget();
                }
            });

    }

    private void finishActivity() {
        finish();
//        mHandler.postDelayed(new Runnable() {
//            public void run() {
//                if (recipientChanged) {
//                    lastRecipient = buildRecipient();
//                }
//                finish();
//            }
//        }, 1000);
    }

    private boolean finishedUpload()
    {
        boolean ret = true;
        synchronized(mShareDataList)
        {
	        for(ComposeShareData item: mShareDataList)
	        {
	        	if(item.mStatus == ComposeShareData.WAITING_STATUS )
	            {
	                ret = false;
	                break;
	            }
	        }
        }
        return ret;
    }

    private ComposeShareData getNextNotUploadData()
    {
    	synchronized(mShareDataList)
    	{
	        for(ComposeShareData item: mShareDataList)
	        {
	            if(item.mStatus == ComposeShareData.WAITING_STATUS )
	            {
	                return item;
	            }
	        }
    	}
        return null;
    }

    private void performDealNextPostData() {
    	if (finishedUpload()) {
            return ;
        }
    	
    	ComposeShareData item =  getNextNotUploadData();

        if ( item == null && mShareDataList.size() != 0) {
            return;
        }
        
        if(item.mStatus== ComposeShareData.SUCCEED_STATUS || item.mStatus== ComposeShareData.UPLOADING_STATUS)
        {
        	return ;
        }

        if (item == null) {
            return;
        }

        performOneData(item);
        
        mHandler.post(new Runnable()
        {
        	public void run()
        	{
        		mComposeShareAdapter.notifyDataSetChanged();
        	}
        }); 
        
        
        mHandler.postDelayed(new Runnable()
        {
            public void run()
            {
                performDealNextPostData();
            }
        }, 100);
    }
    
    private void performOneData(ComposeShareData item )
    {
        switch (item.mType) {
            case ComposeShareData.LINK_TYPE:
                shareLink(item);
                break;
            case ComposeShareData.APK_TYPE:
                shareApk(item);
                break;
            case ComposeShareData.PHOTO_TYPE:
                sharePhoto(item);
                break;
            case ComposeShareData.VCARD_TYPE:
                postToWall(item);
                break;
            case ComposeShareData.PURETEXT_TYPE:
                postToWall(item);
                break;
            case ComposeShareData.OTHER_TYPE:
                fileShare(item);
                break;
            default:
                Log.d(TAG, "performDealPostData()  default type, do nothing.");
                break;
        }
    }

    private class BackgroundTask extends UserTask<Void, Void, Void> {
        public BackgroundTask() {
            super();
            Log.d(TAG, "create BackgroundTask = " + this);
        }

        @Override
        public Void doInBackground(Void... params) {
            performDealNextPostData();
            return null;
        }
    }

    //fetch data for initial status
    private void fetchDataForUploadTask(ComposeShareData shareData)
    {
    	shareData.mRecipient = buildRecipient();
        if (QiupuConfig.IS_USE_BAIDU_LOCATION_API) {
            if (mIsLocationDisplayed) {
                shareData.mLocation = mLocationText.getText().toString();
            } else {
                shareData.mLocation = null;
            }
        } else {
            if (mLocationText.getText().toString().contains(",")) {
                shareData.mLocation = mLocationText.getText().toString();
            } else {
                shareData.mLocation = null;
            }
        }	
         
    	shareData.mIsPrivate = privateShare;
        shareData.mAllowComment = mIsCommentAllowed;
        shareData.mAllowLike = mIsLikeAllowed;
        shareData.mAllowShare = mIsShareAllowed;
        shareData.mMessage = getComposingText();
        shareData.mLocaiton = locationString;
        if(mSelectCategoryId > 0) {
        	shareData.mCategoryId = String.valueOf(mSelectCategoryId);
        }

        if (isAdmin) {
            shareData.mSendEmail = mSendEmailStatus;
            shareData.mSendSms = mSendSmsStatus;
            // isTop is no use, should add one checkBox just like send email or message ui
            shareData.mIsTop = isTop;
        }

    }

    private boolean isListNoChange() {
        //case1. if user delete history list data without add ---> already handle
        //case2. if user delete data and add new data ---> handle by add case
        //case3. has uncommit data, but quit from wutong, then re-enter and post
        boolean hasUnCommitData = false;
        for (ComposeShareData data: mShareDataList) {
            if (data.mCommit != ComposeShareData.COMMIT_STATUS) {
                hasUnCommitData = true;
                break;
            }
        }
        
        return /*cloneList.containsAll(mShareDataList) && mShareDataList.containsAll(cloneList) 
                || */cloneList.containsAll(mShareDataList) && hasUnCommitData == false;
    }

    private boolean hasPureTextOrLocaiton() {
        return TextUtils.isEmpty(locationString) == false || TextUtils.isEmpty(getComposingText()) == false;
    }

    private void handleFailedCase(ArrayList<ComposeShareData> dataList) {
        //the text just has effect with initial data
        for (int i = 0, len = dataList.size(); i < len; i++) 
        {
            ComposeShareData shareData = dataList.get(i);
            
            //just need waiting action re-change the upload data
            if(shareData.mStatus == ComposeShareData.WAITING_STATUS)
            {
                fetchDataForUploadTask(shareData);
            }
                                
            if(shareData.mStatus == ComposeShareData.FAILED_STATUS){
                shareData.mStatus = ComposeShareData.WAITING_STATUS;
            }
        }
    }

    private void shareAppAndOtherInfo() {
        synchronized(mShareDataList)
    	{
            Log.d(TAG, "================ nochange = " + (mShareDataList.size() == 0 || isListNoChange()));
	        if (mShareDataList.size() == 0 || isListNoChange()) {
	            
	            //if user change list status(add/remove)
	            //case1. add    ----> already handle 
	            //case2. remove ----> already handle
	            //add && remove ----> no need to handle this case, we do care add data. 
	            
	            //case1. if no change && no pure text && no location, but has failed case
	            boolean isNullText = TextUtils.isEmpty(locationString) && TextUtils.isEmpty(getComposingText());
	            Log.d(TAG, "isNullText = " + isNullText);
	            if (isNullText) {
	                // directly handle failed case
	                handleFailedCase(cloneList);
	            } else {
	                //case1. if no change, but with pure text or location
	                // for pure text or location share
	                ComposeShareData shareData = new ComposeShareData();
	                shareData.mType = ComposeShareData.PURETEXT_TYPE;

	                fetchDataForUploadTask(shareData);
	                mShareDataList.add(shareData);

	                //case1. if there is failed case.
	                handleFailedCase(cloneList);
	            }
	        } else{
	            handleFailedCase(mShareDataList);
	        }
    	}

        new BackgroundTask().execute((Void[]) null);

        showCustomToast(R.string.string_enter_background_share);
//        finishActivity();
        finishCurrentActivity();
    }

    private static final int DIALOG_POST_WALL        = 10001;

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_POST_WALL: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.post_to_wall);
                dialog.setMessage(getString(R.string.post_to_wall_summary));
                dialog.setCanceledOnTouchOutside(false);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
            default:
        }

        return super.onCreateDialog(id);
    }

    private DialogInterface.OnClickListener mConfirmShareListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int button) {
            
            shareAppAndOtherInfo();
            return ;
        }
    };

    private static DialogInterface.OnClickListener mOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int button) {
            return ;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        USE_BAIDU_API = true;
        LocationUtils.removeLocationRemovedListener(getClass().getName());

//        handleSerialize(true);

        synchronized(mShareDataList){
	        for(int i=0;i<mShareDataList.size();i++)
	        {
	        	ComposeShareData item = mShareDataList.get(i);
	        	if(item.mStatus == ComposeShareData.SUCCEED_STATUS)
	        	{
	        		mShareDataList.remove(i);
	        		i--;
	        	}
	        }
        }

        AccountServiceConnectObserver.unregisterAccountServiceConnectListener(getClass().getName());

        if (mShareComment != null) {
            mShareComment.destroy();
            mShareComment = null;
        }
    }

    View.OnClickListener chooseShareUserClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            startPickActivity();
        }
    };

    private void startPickActivity() {
        FROM_GALLERY_OR_CAMERA_KEY = 1;
        Intent intent = new Intent(QiupuComposeActivity.this, PickAudienceActivity.class);
        intent.putExtra(PickAudienceActivity.RECEIVE_ADDRESS, mShareTo.getAddressesArray());
        intent.putExtra(PickAudienceActivity.RECEIVE_SELECTUSERCIRCLE_NAME, mSelectUserCircleMap);
        intent.putExtra(PickAudienceActivity.PICK_FROM, PickAudienceActivity.PICK_FROM_COMPOSE);
        intent.putExtra(CircleUtils.INTENT_FROM_ID, mFromId);
        intent.putExtra(CircleUtils.INTENT_SCENE, mScene);
        startActivityForResult(intent, userselectcode);
    }

    private String buildRetweetContent() {
        return getComposingText();
    }

    private String buildRecipient() {
        privateShare = true;
        StringBuilder users = new StringBuilder();
//        String tmpFromId = null ;
//        if(mScene != mFromId && mFromId > 0) {
//        	tmpFromId = "#" + mFromId;
//        	users.append(tmpFromId);
//        }
        String[] userArray = mShareTo.getAddressesArray();

        String selectCircle = "";
        View selectView = mCircleSelectSpinner.getSelectedView();
        if(selectView != null && RecipientsCirclesItemView.class.isInstance(selectView)) {
        	RecipientsCirclesItemView item = (RecipientsCirclesItemView) selectView;
        	Log.d(TAG, "select id " + item.getDataId());
        	if(item.getDataId() == QiupuConfig.CIRCLE_ID_PRIVACY) {
        		privateShare = true;
        	}else {
        		selectCircle = "#" + item.getDataId();
        	}
        }
        
        Log.d(TAG, "buildRecipient() privateShare = " + privateShare + ", userArray = " + userArray.toString());
//        if (privateShare) {
            for (int i = 0; i < userArray.length; i++) {
                if (users.length() > 0) {
                    users.append(",");
                }
//                if(userArray[i].equals(QiupuConfig.CIRCLE_NAME_PUBLIC)) {
//                    privateShare = false;
//                    users.append(QiupuConfig.CIRCLE_NAME_PUBLIC);
//                }else {
//                	if(!userArray[i].equals(tmpFromId)) {
                		users.append(userArray[i]);
//                	}
//                }
            }
            if(!TextUtils.isEmpty(selectCircle)) {
            	if(users.length() > 0) {
            		users.append(",");
            	}
            	users.append(selectCircle);
            	if(QiupuConfig.CIRCLE_NAME_PUBLIC.equals(selectCircle)) {
            		privateShare = false;
            	}
            }
            
//        } else {
//            users.append(QiupuConfig.CIRCLE_NAME_PUBLIC);
//            if (userArray.length > 0) {
//                users.append(",");
//            }
//            for (int i = 0; i < userArray.length; i++) {
//                if (i > 0) {
//                    users.append(",");
//                }
//                if(userArray[i].equals(QiupuConfig.CIRCLE_NAME_PUBLIC)) {
//                    //privateShare = false;
//                    //users.append(QiupuConfig.CIRCLE_NAME_PUBLIC);
//                } else {
//                    users.append(userArray[i]);
//                }
//            }
//        }
        return users.toString();
    }
    
    private boolean validateComposeContent(String content, String recipient) {
        boolean hasLocation = false;
        if (QiupuConfig.IS_USE_BAIDU_LOCATION_API) {
            hasLocation = mIsLocationDisplayed;
        } else {
            hasLocation= mLocationText.getText().toString().contains(",");
        }

        //case. exist history data without change but from other's profile

//        boolean hasListData = mShareDataList.size() > 0;

        boolean hasRetweet = mStreamContainer != null && mStreamContainer.getChildCount() != 0; // false

        boolean emptyData = TextUtils.isEmpty(content) && hasLocation == false;

        Log.d(TAG, "======== emptyData = " + emptyData 
                + ", hasRetweet = " + hasRetweet 
                + ", isListNoChange() = " + isListNoChange()
                + ", hasAddress = " + (recipient.length() != 0));

        if (emptyData && hasRetweet == false && (isListNoChange() && recipient.length() != 0)) {
            showCustomToast(R.string.toast_invalid_input_content);
            mShareComment.requestFocus();
            return false;
        }

//        if (!hasListData && !hasRetweet && TextUtils.isEmpty(content) && hasLocation == false) {
//            showCustomToast(R.string.toast_invalid_input_content);
//            mShareComment.requestFocus();
//            return false;
//        }

        return true;
    }

    private String getAppData() {
        if (mContactInfo == null) {
            return "";
        }
        Log.d(TAG, "mContactInfoStr = " + mContactInfoStr);
        return mContactInfoStr;
    }

    public static void startShareIntent(Context context, ApkResponse apk) {
        startShareIntent(context, apk, null, null);
    }
    
    public static void startShareIntent(Context context, ApkResponse apk, String comment, String reshare) {
        Intent intent = new Intent(context, QiupuComposeActivity.class);
        intent.putExtra(QiupuMessage.BUNDLE_APKINFO, apk);
        intent.putExtra(EXTERNAL_COMMENT, comment);
        intent.putExtra("reshare", reshare);

        context.startActivity(intent);
    }

    private ListView mSetListView;

    @Override
    protected void setCommentSettingListener() {
        mSetListView = (ListView) getLayoutInflater().inflate(R.layout.default_listview, null);
        mSetListView.setBackgroundResource(R.color.white);
        mSetListView.setAdapter(new CommentSettingAdapter(this, mIsCommentAllowed, mIsLikeAllowed, mIsShareAllowed));
        mSetListView.setOnItemClickListener(listItemListener);
        DialogUtils.ShowDialogwithView(this, getString(R.string.stream_setting),
                0, mSetListView, positiveListener, negativeListener);
    }

    private AdapterView.OnItemClickListener listItemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            if (CommentSettingItemView.class.isInstance(view)) {
                CommentSettingItemView commentView = (CommentSettingItemView) view;
                commentView.setCheckedStatus();
            }
        }
    };

    private DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mSetListView != null && mSetListView.getCount() > 0) {
                boolean[] canParameter = new boolean[mSetListView.getCount()];
                for (int i = 0, count = mSetListView.getCount(); i < count; i++) {
                    canParameter[i] = ((CompoundButton)mSetListView.getChildAt(i).findViewById(R.id.item_button)).isChecked();
                    Log.d(TAG, "canParameter[" + i + "] = " + canParameter[i]);
                }
                if (canParameter[0] == mIsCommentAllowed && canParameter[1] == mIsLikeAllowed && canParameter[2] == mIsShareAllowed) {
                    Log.d(TAG, "No status change");
                } else {
                    mIsCommentAllowed = canParameter[0];
                    mIsLikeAllowed = canParameter[1];
                    mIsShareAllowed = canParameter[2];
                    attachStreamProperty(R.id.id_stream_property, mIsCommentAllowed, mIsLikeAllowed, mIsShareAllowed);
                }
            } else {
                Log.d(TAG, "Set ListView is null.");
            }
        }
    };

    private DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
        }
    };

    private boolean mIsCommentAllowed = true;
    private boolean mIsLikeAllowed = true;
    private boolean mIsShareAllowed = true;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!USE_OLD_LOCATION) {
            MenuItem commentItem = menu.findItem(R.id.menu_comment_attribute);
            commentItem.setVisible(true);
            commentItem.setIcon(mIsCommentAllowed ? R.drawable.menu_forbid_comments : R.drawable.menu_comments);
            commentItem.setTitle(mIsCommentAllowed ? R.string.menu_comment_attribute_disable : R.string.menu_comment_attribute_enable);

            MenuItem likeItem = menu.findItem(R.id.menu_like_attribute);
            likeItem.setVisible(true);
            likeItem.setIcon(mIsLikeAllowed ? R.drawable.memu_forbid_like : R.drawable.menu_like);
            likeItem.setTitle(mIsLikeAllowed ? R.string.menu_like_attribute_disable : R.string.menu_like_attribute_enable);

            MenuItem shareItem = menu.findItem(R.id.menu_share_attribute);
            shareItem.setVisible(true);
            shareItem.setIcon(mIsShareAllowed ? R.drawable.memu_forbid_share : R.drawable.menu_share_attribute_enable);
            shareItem.setTitle(mIsShareAllowed ? R.string.menu_share_attribute_disable : R.string.menu_share_attribute_enable);
        }
//        menu.findItem(R.id.menu_insert_smiley).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (!USE_OLD_LOCATION) {
            if (i == R.id.menu_comment_attribute) {
                toggleComment();
            } else if (i == R.id.menu_like_attribute) {
                toggleLike();
            } else if (i == R.id.menu_share_attribute) {
                toggleShare();
            } else if (i == R.id.menu_insert_smiley) {
                showSmileyDialog();
            }
        } else {
            if (R.id.menu_insert_smiley == i) {
                showSmileyDialog();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void appendAddressString(StringBuilder builder, String appendString){
        if(appendString != null && appendString.length() > 0){
            if(builder.length() > 0){
                builder.append(",");
                builder.append(appendString);
            }else{
                builder.append(appendString);
            }
        }
        
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    private boolean mDiscardConfirm = false;
    private boolean needEscapeConfirm() {
        boolean ret = false;
        boolean hasComments = TextUtils.isEmpty(getComposingText()) == false;

        if (hasComments) {
            ret = true;
        }

        return ret;
    }

    public void onDiscard() {
        mDiscardConfirm = false;
        if (needEscapeConfirm()) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                	synchronized(mShareDataList){
                        mShareDataList.clear();
                	}
                    finish();
                    mDiscardConfirm = true;
                }
            };

            final String title = getString(R.string.discard_dialog_title, getString(R.string.home_steam));
            final String message = getString(R.string.discard_dialog_message, getString(R.string.home_steam));
            DialogUtils.showConfirmDialog(this, title, message, listener);
        } else {
            finish();
            mDiscardConfirm = true;
        }
    }

    @Override
    protected boolean preEscapeActivity() {
        onDiscard();
        return mDiscardConfirm;
    }

    private String getComposingText() {
        return ConversationMultiAutoCompleteTextView.getConversationText(mShareComment);
    }

    @Override
    public void deleteItem(ComposeShareData shareData) {
//        Log.d(TAG, "deleteItem() mShareDataList.size() = " + mShareDataList.size());
//        for (int i = 0; i < mShareDataList.size(); i++) {
//            Log.d(TAG, "mShareDataList.get(" + i + ") = " + mShareDataList.get(i).toString());
//        }

        synchronized(mShareDataList)
        {
	        if (mShareDataList.size() > 0) {
	            mShareDataList.remove(shareData);
	        }
        }
        
        if(mShareDataList.size() <= 0 && mSpanView != null) {
        	mSpanView.setVisibility(View.GONE);
        }

        Log.d(TAG, "deleteItem() privateShare = " + privateShare + ", size = " + mShareDataList.size());
//        restoreVisibility();
//        if (privateShare == false) {
//            mShareTo.setAddresses(buildRecipient());
//        }

        mComposeShareAdapter.alterDataList(mShareDataList);
    }

    @Override
    public void retryItem(ComposeShareData shareData) {
        performOneData(shareData);
    }

    private String setprivateShareValue(String circleIds) {
        String[] ids = circleIds.split(",");
        StringBuilder newCircleids = new StringBuilder();
        privateShare = true;
        if(hasVcard()) {
            for(int i=0; i<ids.length; i++) {
                if(ids[i].equals(QiupuConfig.CIRCLE_NAME_PUBLIC)) {
                    DialogUtils.showOKDialog(this, -1, R.string.has_vcard_tips, null);
                }else {
                    if(newCircleids.length() > 0) {
                        newCircleids.append(",");
                    }
                    newCircleids.append(ids[i]);
                }
            }
        } else {
            newCircleids.append(circleIds);
            for(int i=0; i<ids.length; i++) {
                if(ids[i].equals(QiupuConfig.CIRCLE_NAME_PUBLIC)) {
                    privateShare = false;
                    break;
                }
            }
        }
        return newCircleids.toString();
    }


    private void toggleComment() {
        mIsCommentAllowed = !mIsCommentAllowed;
        if (USE_OLD_LOCATION) {
            attachStreamProperty(R.id.id_stream_property, mIsCommentAllowed, mIsLikeAllowed, mIsShareAllowed);
        }
    }

    private void toggleLike() {
        mIsLikeAllowed = !mIsLikeAllowed;
        if (USE_OLD_LOCATION) {
            attachStreamProperty(R.id.id_stream_property, mIsCommentAllowed, mIsLikeAllowed, mIsShareAllowed);
        }
    }

    private void toggleShare() {
        mIsShareAllowed = !mIsShareAllowed;
        if (USE_OLD_LOCATION) {
            attachStreamProperty(R.id.id_stream_property, mIsCommentAllowed, mIsLikeAllowed, mIsShareAllowed);
        }
    }

    private void fileShare(final ComposeShareData data) {
        if (!AccountServiceUtils.isAccountReady()) {
            return;
        }

        if (data.mIsPrivate && data.mRecipient.length() <= 0) {
//            Toast.makeText(this, R.string.privacy_show, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "ticket = " + getSavedTicket());
        setLocation(data.mLocaiton);

        data.mStatus = ComposeShareData.UPLOADING_STATUS;
        data.mCommit = ComposeShareData.COMMIT_STATUS;
        asyncQiupu.fileShare(getSavedTicket(), data.mRecipient, data.mMessage,
                data.mAppData, data.mIsPrivate, data.mAllowComment, data.mAllowLike,
                data.mAllowShare, "", "", data.mFile, data.mTitle, data.mScreenShotFile, 
                data.mVersion, data.mIsTop, data.mSendEmail, data.mSendSms,data.mCategoryId, new TwitterAdapter() {
                public void fileShare(Stream post) {
                    Log.d(TAG, "share static file succeed =" + post);

                    data.mStatus = ComposeShareData.SUCCEED_STATUS;
                    Message msg = mHandler.obtainMessage(SHARE_END);
                    msg.getData().putBoolean(RESULT, true);
                    msg.getData().putSerializable("data", data);
                    msg.sendToTarget();
                }

                public void onException(TwitterException ex,
                        TwitterMethod method) {
                    Log.d(TAG, "share static file failed =" + post);
                    TwitterExceptionUtils.printException(TAG, "fileShare, server exception:", ex, method);

                    data.mStatus = ComposeShareData.FAILED_STATUS;
                    Message msg = mHandler.obtainMessage(SHARE_END);
                    msg.getData().putSerializable("data", data);
                    msg.getData().putString(ERROR_MSG, ex.getMessage());
                    msg.getData().putBoolean(RESULT, false);
                    msg.sendToTarget();
                }
            });
    }

    public static void startPickingAppsIntent(Context context) {
        Intent intent = new Intent(context, QiupuComposeActivity.class);
        intent.putExtra("from_type", "from_app");
        context.startActivity(intent);
    }

    public static void startTakingPhotoIntent(Context context) {
        Intent intent = new Intent(context, QiupuComposeActivity.class);
        intent.putExtra(EXTRA_FLAG_KEY, EXTRA_FLAG_CAMERA);
        context.startActivity(intent);
    }

    public static void startPickingPhotoIntent(Context context) {
        Intent intent = new Intent(context, QiupuComposeActivity.class);
        intent.putExtra(EXTRA_FLAG_KEY, EXTRA_FLAG_GALLERY);
        context.startActivity(intent);
    }

    public static void startTakingPhotoIntent(Context context, String recipient,  long scene, long fromid) {
    	startTakingPhotoIntent(context, recipient, null, scene, fromid);
    }
    
    public static void startTakingPhotoIntent(Context context, String recipient, HashMap<String, String> recipientMap,  long scene, long fromid) {
        Intent intent = new Intent(context, QiupuComposeActivity.class); 
        intent.putExtra(EXTRA_FLAG_KEY, EXTRA_FLAG_CAMERA);
//        if (!TextUtils.isEmpty(recipient)) {
//        	intent.putExtra(RECIPIENT_FLAG_KEY, recipient);
//        }
//        if(recipientMap != null) {
//        	intent.putExtra(RECEIVER_KEY, recipientMap);
//        }
        intent.putExtra(CircleUtils.CIRCLE_ID, fromid);
        intent.putExtra(CircleUtils.INTENT_SCENE, scene);
        context.startActivity(intent);
    }

    public static void startPickingPhotoIntent(Context context, String recipient, long scene, long fromid) {
    	startPickingPhotoIntent(context, recipient, null, scene, fromid);
    }
    
    public static void startPickingPhotoIntent(Context context, String recipient, HashMap<String, String> recipientMap, long scene, long fromid) {
        Intent intent = new Intent(context, QiupuComposeActivity.class);
        intent.putExtra(EXTRA_FLAG_KEY, EXTRA_FLAG_GALLERY);
//        if (!TextUtils.isEmpty(recipient)) {
//        	intent.putExtra(RECIPIENT_FLAG_KEY, recipient);
//        }
//        if(recipientMap != null) {
//        	intent.putExtra(RECEIVER_KEY, recipientMap);
//        }
        intent.putExtra(CircleUtils.CIRCLE_ID, fromid);
        intent.putExtra(CircleUtils.INTENT_SCENE, scene);
        context.startActivity(intent);
    }

    public interface FinishActivityListener {
        public void finishPhotoActivity();
    }

    private static WeakReference<FinishActivityListener> finshListener;

    public static void setFinishListener(FinishActivityListener listener) {
        finshListener = new WeakReference<FinishActivityListener>(listener);
    }

    private AlertDialog mSmileyDialog;
    private void showSmileyDialog() {
        if (mSmileyDialog == null) {
            mSmileyDialog =  DialogUtils.showSmileyDialog(this, mShareComment);
        } else {
            mSmileyDialog.show();
        }
    }

    @Override
    public void refreshComposeItemUI(ComposeShareData data) {
        if(data != null) {
            final int count = mListView.getChildCount();
            for(int i=0;i<count;i++) {
                View view = mListView.getChildAt(i);
                if(ComposeShareItemView.class.isInstance(view)) {
                    ComposeShareItemView civ = (ComposeShareItemView)view;
                    if(civ.getID() == data.ID) {
                        civ.refreshStatusUI();
                        break;
                    }
                }
            }
        }
    }

//    private static String lastRecipient;
//    private static boolean recipientChanged;
//    private String getDefaultRecipients() {
//        if (TextUtils.isEmpty(lastRecipient)) {
//            if (privateShare) {
//                Log.w(TAG, "getDefaultRecipients, unexpected private share.");
//                return null;
//            } else {
//                return QiupuConfig.CIRCLE_NAME_PUBLIC;
//            }
//        } else {
//            return lastRecipient;
//        }
//    }
//
//    private void setLastRecipient(String receivers) {
//        recipientChanged = false;
//        lastRecipient = receivers;
//        mShareTo.setAddresses(lastRecipient);
//    }

    private void dumpCallStack() {
        if (false) {
            Exception e = new Exception("this is a log");
            e.printStackTrace();
        }
    }

    private void parseFileUri(Intent intent) {
        if (intent != null) {
            Uri uri = intent.getData();
            String mimeType = intent.getType();
            boolean isImage = mimeType != null && mimeType.contains("image/");
            if (isImage || isImageFile(uri)) {
                imageFileUri = uri;
                showPhoto(true, intent,false);
            } else {
                showOtherFiles(uri, mFileMimeType);
            }
        }
    }

    private void onLocationHidden() {
    	mIsLocationDisplayed = false;
        mLocationText.setText(getResources().getString(R.string.location_text));
        mLocationText.setMovementMethod(null);
        mLocationText.setLinksClickable(false);
        mLocationIcon.setImageResource(R.drawable.location_gray);
        mRemoveLocaitonIcon.setVisibility(View.GONE);
        mLocationIcon.setVisibility(View.VISIBLE);
        mLocationText.setVisibility(View.VISIBLE);
        mLocationProgressBar.setVisibility(View.GONE);
        locationString = null;
        HttpClientImpl.setLocation("");
    }
    
//    private void changeReffredCount(String[] recipientAddr) {
//    	try {
//			for (int i = 0; i < recipientAddr.length; i++) {
//				String id = recipientAddr[i];
//				if (id.contains("#")) {
//					int index = id.indexOf("#");
//					id = id.substring(index + 1, id.length());
//					QiupuORM.acceptCircle(getApplicationContext(), id);
//				}else {
//					if(TextUtils.isDigitsOnly(id)) {
//						QiupuORM.acceptUser(getApplicationContext(), id);
//					}
//				}
//			}
//		} catch (Exception e) {
//			Log.e(TAG, "change reffredCount error: " + e.getMessage());
//		}
//    }
    private class ChangeReffredCountTask extends UserTask<Void, Void, Void> {

    	private String[] recipientAddr;
        public ChangeReffredCountTask(String addr) {
        	if(addr != null) {
        		recipientAddr = addr.split(",");
        	}
        }

        @Override
        public Void doInBackground(Void... params) {
        	if (recipientAddr != null) {
        		try {
        			for (int i = 0; i < recipientAddr.length; i++) {
        				String id = recipientAddr[i];
        				if (id.contains("#")) {
        					int index = id.indexOf("#");
        					id = id.substring(index + 1, id.length());
        					QiupuORM.acceptCircle(getApplicationContext(), id);
        				}else {
        					if(TextUtils.isDigitsOnly(id)) {
        						QiupuORM.acceptEmpolyee(getApplicationContext(), id);
        					}
        				}
        			}
				} catch (Exception e) {
					Log.e(TAG, "change reffredCount error: " + e.getMessage());
				}
    		}
            return null;
        }
    }

    @Override
    public void onInputCompletion() {
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "onInputCompletion " + mShareTo.getAddresses() + " vv " + mShareTo.getAddressesArray().length) ;
    	if(mShareTo.getAddressesArray().length == 1) {
    		String scopeid = mShareTo.getAddressesArray()[0];
    		if(scopeid != null && scopeid.contains("#")) {
    			int index = scopeid.indexOf("#");
    			scopeid = scopeid.substring(index + 1, scopeid.length());
    			showCategoryUi(scopeid);
    		}else {
    			mCategoryRl.setVisibility(View.GONE);
    		}
    		
    	}else {
    		mCategoryRl.setVisibility(View.GONE);
    	}
    }
    
    private ArrayList<InfoCategory> mCategories = new ArrayList<InfoCategory>();
    private void showCategoryUi(String scopeid) {
		ArrayList<InfoCategory> categories = new ArrayList<InfoCategory>();
    	categories = QiupuORM.queryCategories(this, scopeid);
    	mCategories.clear();
    	mCategories.addAll(categories);
    	
    	if(categories.size() > 0) {
    		mCategoryRl.setVisibility(View.VISIBLE);
    		mCategoryView.setText(categories.get(0).categoryName);
    		mSelectCategoryId = categories.get(0).categoryId;
    	}else {
    		mCategoryRl.setVisibility(View.GONE);
    	}
    }

    
    private void showCategoryDropDownDialog() {
//    	Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_tip); 
//    	operatingAnim.setFillAfter(true);
//    	mCategoryIcon.startAnimation(operatingAnim);
//    	mCategoryView.setBackgroundResource(R.drawable.info_category_title_top_line_bg);
//    	mCategoryView.setPadding((int)getResources().getDimension(R.dimen.category_padding_left), 
//    			(int)getResources().getDimension(R.dimen.default_text_padding_top), (int)getResources().getDimension(R.dimen.category_padding_right), (int)getResources().getDimension(R.dimen.default_text_padding_bottom));
    	
    	final QuickAction quickAction = new QuickAction(this, QuickAction.VERTICAL);
    	quickAction.setSelectActionId(mSelectCategoryId);
    	
    	if(mCategories != null ) {
    		for(int i=0; i<mCategories.size(); i++) {
    			InfoCategory ic = mCategories.get(i);
    			quickAction.addActionItem(new ActionItem(ic.categoryId, ic.categoryName, null));
    		}
    	}
    	
    	quickAction.addActionItem(new ActionItem(MNO_CATEGORY_ID, getString(R.string.no_category), null));
    	
    	//Set listener for action item clicked
    	quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {			
    		@Override
    		public void onItemClick(QuickAction source, int pos, long actionId) {				
    			ActionItem actionItem = quickAction.getActionItem(pos);
    			quickAction.dismiss();
    			mCategoryView.setText(actionItem.getTitle());
    			if(mSelectCategoryId == actionItem.getActionId()) {
    				Log.d(TAG, "select same actionid , do nothing");
    			}else {
    				mSelectCategoryId = actionItem.getActionId();
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
    				Animation operatingAnim = AnimationUtils.loadAnimation(QiupuComposeActivity.this, R.anim.rotate_tip_arrowup_revert); 
    				operatingAnim.setFillAfter(true);
    				mCategoryIcon.startAnimation(operatingAnim);
    			}else {
    				Animation operatingAnim = AnimationUtils.loadAnimation(QiupuComposeActivity.this, R.anim.rotate_tip_revert); 
    				operatingAnim.setFillAfter(true);
    				mCategoryIcon.startAnimation(operatingAnim);
    			}
    			mCategoryRl.setBackgroundResource(R.drawable.info_category_title_bg);
    			mCategoryRl.setPadding((int)getResources().getDimension(R.dimen.category_padding_left), 
    					(int)getResources().getDimension(R.dimen.small_text_padding_top), (int)getResources().getDimension(R.dimen.category_padding_right), (int)getResources().getDimension(R.dimen.small_text_padding_bottom));
    		}
    	});
    	
    	quickAction.show(mCategoryRl,mCategoryIcon);
    	quickAction.setAnimStyle(QuickAction.ANIM_REFLECT);
    }
}
