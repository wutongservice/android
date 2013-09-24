package com.borqs.wutong;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.PublicCircleRequestUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.SelectionItem;
import com.borqs.common.adapter.StreamFilpperFragmentAdapter;
import com.borqs.common.listener.ActivityFinishListner;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.common.view.CustomViewPager;
import com.borqs.common.view.CustomViewPager.CustomViewPagerListenter;
import com.borqs.information.util.InformationUtils;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.CircleCirclesColumns;
import com.borqs.qiupu.fragment.StreamListFragment;
import com.borqs.qiupu.fragment.StreamRightFlipperFragment;
import com.borqs.qiupu.fragment.StreamRightFlipperFragment.StreamRightFlipperCallBack;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.BasicNavigationActivity;
import com.borqs.qiupu.ui.circle.quickAction.BottomMoreQuickAction;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;

/**
 * Created with IntelliJ IDEA.
 * User: yangfeng
 * Date: 13-1-16
 * Time: 下午4:42
 * To change this template use File | Settings | File Templates.
 */
public class OrganizationHomeActivity extends BasicNavigationActivity implements 
        StreamListFragment.StreamListFragmentCallBack,
        StreamRightFlipperCallBack,
        HomePickerActivity.PickerInterface, ActivityFinishListner, CustomViewPagerListenter {

    private static final String TAG = "OrganizationHomeActivity";

    private static final boolean FORCE_SHOW_DROPDOWN = false;

    private UserCircle mCircle;
//    private PublicCircleInfoFragment mCircleInfoFragment;
    private StreamListFragment mHomeFragment;
    private StreamRightFlipperFragment mRightFragment;

    StreamListFragment.MetaData mFragmentData = new StreamListFragment.MetaData();
    public static final int in_member_selectcode = 5555;

    private final static int PAGE_STRAM = 0;
    private final static int PAGE_RIGHT_INFO = 1;
    private int mCurrentPage = PAGE_STRAM;
    private BottomMoreQuickAction mMoreDialog;
    
    private StreamFilpperFragmentAdapter mAdapter;
    private CustomViewPager mPager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        enableLeftNav(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wutong_organization_home_activity);

        QiupuHelper.registerFinishListner(getClass().getName(), this);
        parseActivityIntent(getIntent());
        overrideRightActionBtn(R.drawable.home_screen_menu_people_icon_default, editProfileClick);
        
        View actionView;
        actionView = findViewById(R.id.toggle_search);
        if (null != actionView) {
            actionView.setVisibility(View.VISIBLE);
            actionView.setOnClickListener(searchClickListener);
        }
        actionView = findViewById(R.id.toggle_photo);
        if (null != actionView) {
            actionView.setOnClickListener(mTogglePhotoListener);
        }
        actionView = findViewById(R.id.toggle_composer);
        if (null != actionView) {
            actionView.setOnClickListener(gotoComposeListener);
        }
        
        actionView = findViewById(R.id.toggle_more);
        if (null != actionView) {
            actionView.setOnClickListener(showMoreActionListener);
        }

        final View foot = findViewById(R.id.bottom_actions_layout);
        
        mAdapter = new StreamFilpperFragmentAdapter(getSupportFragmentManager(), this);
        mPager = (CustomViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(mCurrentPage);
        mPager.setListener(this);
        mPager.setIndex(-1);
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int pos) {
				hideSearhView();
				
				if(pos == PAGE_RIGHT_INFO) {
					mCurrentPage = PAGE_RIGHT_INFO;
					mPager.setIndex(0);
					if(mRightFragment != null) {
						mRightFragment.setisCurrentScreen(true);
					}
					mAdapter.onScrollingBottomView(false, foot);
				}else {
					mCurrentPage = PAGE_STRAM;
					mPager.setIndex(-1);
					if(mRightFragment != null) {
						mRightFragment.setisCurrentScreen(false);
					}
					
					mAdapter.onScrollingBottomView(true, foot);
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
        
        View guide = findViewById(R.id.right_move_guide);
        guide.setVisibility(orm.showRightMoveGuide() ? View.VISIBLE : View.GONE);
        guide.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				orm.setShowRightMoveGuide(false);
				v.setVisibility(View.GONE);
			}
		});
        
        orm.checkExpandCirCle();
        
        mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO).sendToTarget();
				
			}
		}, 1000);
    }
	
    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
    protected void loadSearch()
    {
        gotoSearchActivity();
    }

    private void parseActivityIntent(Intent intent) {
        String url = getIntentURL(intent);
        if (TextUtils.isEmpty(url)) {
            Bundle bundle = intent.getExtras();
            String requestName = bundle.getString(CircleUtils.CIRCLE_NAME);
            mFragmentData.mCircleId = bundle.getLong(CircleUtils.CIRCLE_ID, -1);
            mFragmentData.mFragmentTitle = TextUtils.isEmpty(requestName) ?
                    "" : requestName;
            mFragmentData.mFromHome = QiupuConfig.FROM_HOME;
        }

        mCircle = orm.queryOneCircleWithGroup(mFragmentData.mCircleId);
        if (mCircle == null){
            mCircle = new UserCircle();
            mCircle.circleid = mFragmentData.mCircleId;
            mCircle.name = mFragmentData.mFragmentTitle;
        }

//        showTitleSpinnerIcon(FORCE_SHOW_DROPDOWN || orm.existingChildCircles(mCircle.circleid));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    };

    @Override
    protected void loadRefresh() {
        if(mHomeFragment != null) {
        	mHomeFragment.loadRefresh();
        }
    }

//    @Override
//    protected void uiLoadEnd() {
//        showLeftActionBtn(true);
//        showProgressBtn(false);
//    }

    @Override
    public StreamListFragment.MetaData getFragmentMetaData(int index) {
        return mFragmentData;
    }

    @Override
    public String getSerializeFilePath() {
        return QiupuHelper.circle + mFragmentData.mCircleId + mFragmentData.mFromHome;
    }

    @Override
    protected void onDestroy() {
    	QiupuHelper.unregisterFinishListner(getClass().getName());
    	InformationUtils.unregisterNotificationListener(getClass().getName());
    	if(mRightFragment != null) {
    		mRightFragment.onDestroy();
    	}
        super.onDestroy();
    }

    View.OnClickListener editProfileClick = new View.OnClickListener() {
        public void onClick(View v) {
        	ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
//        	items.add(new SelectionItem("", getString(R.string.home_label)));
        	items.add(new SelectionItem(String.valueOf(mCircle.circleid), mCircle.name));
            Cursor pOrgazitaionCircles = orm.queryInCircleCircles(mCircle.circleid);
            if(pOrgazitaionCircles != null) {
            	if(pOrgazitaionCircles.getCount() > 0) {
            		if(pOrgazitaionCircles.moveToFirst()) {
            			do {
            				items.add(new SelectionItem(String.valueOf(pOrgazitaionCircles.getLong(pOrgazitaionCircles.getColumnIndex(CircleCirclesColumns.CIRCLEID))), 
            						pOrgazitaionCircles.getString(pOrgazitaionCircles.getColumnIndex(CircleCirclesColumns.CIRCLE_NAME))));
						} while (pOrgazitaionCircles.moveToNext());
            		}
            	}
            	pOrgazitaionCircles.close();
            	pOrgazitaionCircles = null;
            }else {
            	Log.d(TAG, "have no child Circles");
            }
            showCorpusSelectionDialog(items);
        }
    };

    View.OnClickListener gotoComposeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//        	gotoComposeAcitvity();
            startComposeActivity();
        }
    };

    protected void showCorpusSelectionDialog(ArrayList<SelectionItem> items) {
        if(mRightActionBtn != null) {
            int location[] = new int[2];
            mRightActionBtn.getLocationInWindow(location);
            int x = location[0];
            int y = getResources().getDimensionPixelSize(R.dimen.title_bar_height);

            DialogUtils.showCorpusSelectionDialog(this, x, y, items, actionListItemClickListener);
        }
    }

    AdapterView.OnItemClickListener actionListItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	if(CorpusSelectionItemView.class.isInstance(view)) {
                CorpusSelectionItemView item = (CorpusSelectionItemView) view;
                String selectedstring = item.getItemId();
                if(TextUtils.isEmpty(selectedstring)) {
                	selectedstring = item.getText();
                }
                onCorpusSelected(item.getItemId());             
            }
        }
    };

    @Override
    public UserCircle getCircleInfo() {
        return mCircle;
    }

    private void startComposeActivity() {
        boolean isAdmin = false;
        if (mCircle != null) {
        	HashMap<String, String> receiverMap = new HashMap<String, String>();
        	String receiverid = "";
        	if(mCircle.mGroup != null && mCircle.mGroup.creator != null) {
        		isAdmin = PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group)
        				|| PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group);
        	}
        	long parent_id = -1;
        	if(mCircle.mGroup != null) {
        		if(mCircle.mGroup.formal == UserCircle.circle_top_formal
        				|| (mCircle.mGroup.formal == UserCircle.circle_free && mCircle.mGroup.parent_id <=0)) {
        			parent_id = mCircle.circleid;	
        		}else {
        			parent_id = mCircle.mGroup.parent_id;
        		}
        	}
        	// this is home page, so intent param: scene is the top circle id, fromid is -1.
        	IntentUtil.startComposeActivity(this, receiverid, true, isAdmin, receiverMap, parent_id, -1);
        }else {
        	Log.d(TAG, "startComposeActivity, circle is null ");
        }
    }

    private final static int INVIT_EPEOPLE_END = 101;
    private final static int EXIT_CIRCLE_END = 102;
    private final static int CIRCLE_DELETE_END = 103;
    private final static int CIRCLE_AS_PAGE_END = 104;
    
    private final static int GET_PUBLIC_CIRCLE_INFO = 105;
    private final static int GET_PUBLIC_CIRCLE_INFO_END = 106;
    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INVIT_EPEOPLE_END: {
                    try {
                        dismissDialog(DIALOG_SET_CIRCLE_PROCESS);
                    } catch (Exception ne) {
                    }
                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret == true) {
                        Log.d(TAG, "invite people end ");
                        showOperationSucToast(true);
                    } else {
                        showOperationFailToast("", true);
                    }
                    break;
                }
                case EXIT_CIRCLE_END: {
                    try {
                        dismissDialog(DIALOG_SET_CIRCLE_PROCESS);
                    } catch (Exception ne) {
                    }
                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret == true) {
                        Log.d(TAG, "exit circle end ");
                        showOperationSucToast(true);
                        QiupuHelper.updateCirclesUI();
                        finish();
                    } else {
                        showOperationFailToast("", true);
                    }
                    break;
                }case CIRCLE_DELETE_END: {
                    try {
                        dismissDialog(DIALOG_DELETE_CIRCLE_PROCESS);
                    } catch (Exception ne) {}
                    boolean ret = msg.getData().getBoolean(RESULT, false);
                    if (ret)
                    {
                        showOperationSucToast(true);
                        QiupuHelper.updateCirclesUI();
                        finish();
                    } else {
                        ToastUtil.showOperationFailed(OrganizationHomeActivity.this, mHandler, true);
                    }
                    break;
                } case CIRCLE_AS_PAGE_END: {
                    try {
                        dismissDialog(DIALOG_SET_CIRCLE_PROCESS);
                    } catch (Exception ne) {}
                    if(msg.getData().getBoolean(RESULT, false)) {
                        QiupuHelper.updatePageActivityUI(null);
                        showOperationSucToast(true);
                    }else {
                        ToastUtil.showOperationFailed(OrganizationHomeActivity.this, mHandler, true);
                    }
                    break;
                } case GET_PUBLIC_CIRCLE_INFO: {
                	syncPublicCircleInfo(String.valueOf(mCircle.circleid), false, QiupuConfig.isEventIds(mCircle.circleid));
                	break;
                } case GET_PUBLIC_CIRCLE_INFO_END: {
//                	end();
                	boolean ret = msg.getData().getBoolean(RESULT, false);
                	if (ret) {
                	    if(mRightFragment != null) {
                	    	mRightFragment.refreshUI(mCircle);
            			}
                    } else {
                        ToastUtil.showShortToast(OrganizationHomeActivity.this, mHandler, R.string.get_info_failed);
                    }
                	break;
                }
            }
        }
    }

    boolean inGetPublicInfo;
    Object mLockGetPublicInfo = new Object();
    protected void syncPublicCircleInfo(final String circleId, final boolean with_member, final boolean isEvent) {
        if (!ToastUtil.testValidConnectivity(this)) {
            Log.i(TAG, "syncPublicCircleInfo, ignore while no connection.");
            return;
        }
        
    	if (inGetPublicInfo == true) {
    		ToastUtil.showShortToast(this, mHandler, R.string.string_in_processing);
    		return;
    	}
    	
    	synchronized (mLockGetPublicInfo) {
    		inGetPublicInfo = true;
    	}
//    	begin();
    	
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
    	if(mHandler != null) {
    		Message msg = mHandler.obtainMessage(GET_PUBLIC_CIRCLE_INFO_END);
    		msg.getData().putBoolean(RESULT, result);
    		msg.sendToTarget();
    	}
    }
    
    boolean inDeletePeople;
    Object mLockDeletePeople = new Object();
    private void deletePublicCirclePeople(final long circleid, final String userids, final String admins) {
        if (inDeletePeople == true) {
            Toast.makeText(this, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLockDeletePeople) {
            inDeletePeople = true;
        }
        showDialog(DIALOG_SET_CIRCLE_PROCESS);

        asyncQiupu.deletePublicCirclePeople(AccountServiceUtils.getSessionID(), circleid, userids, admins, new TwitterAdapter() {
            public void deletePublicCirclePeople(boolean result) {
                Log.d(TAG, "finish deletePublicCirclePeople=" + result);
                if(result) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //delete circle in DB
                            orm.deleteCircleByCricleId(AccountServiceUtils.getBorqsAccountID(), String.valueOf(circleid));
                            //update user info
//                            orm.updateUserInfoInCircle(AccountServiceUtils.getBorqsAccountID(), circleid, circleName);
                        }
                    });
                }

                Message msg = mHandler.obtainMessage(EXIT_CIRCLE_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();
                synchronized (mLockDeletePeople) {
                    inDeletePeople = false;
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                Message msg = mHandler.obtainMessage(EXIT_CIRCLE_END);
                msg.getData().putString(BasicActivity.ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
                synchronized (mLockDeletePeople) {
                    inDeletePeople = false;
                }
            }
        });
    }
    boolean inDeleteCircle;
    Object mLockdeleteCircle = new Object();

    public void deleteCircleFromServer(final UserCircle circle, final String circleName, final int type) {
        if (inDeleteCircle == true) {
            Toast.makeText(this, R.string.string_in_processing,Toast.LENGTH_SHORT).show();
            return ;
        }

        synchronized (mLockdeleteCircle) {
            inDeleteCircle = true;
        }
        showDialog(DIALOG_DELETE_CIRCLE_PROCESS);
        asyncQiupu.deleteCircle(AccountServiceUtils.getSessionID(), String.valueOf(circle.circleid), type, new TwitterAdapter() {
            public void deleteCircle(boolean suc) {
                Log.d(TAG, "finish deleteCircle=" + suc);

                if(suc) {
                    //delete circle in DB
                    orm.deleteCircleByCricleId(AccountServiceUtils.getBorqsAccountID(), String.valueOf(circle.circleid));
                    //update user info
                    orm.updateUserInfoInCircle(AccountServiceUtils.getBorqsAccountID(), String.valueOf(circle.circleid), circleName);

                    //update circle_circle table
                    orm.deleteCacheCircleCircle(circle);
                    
                    updatePageInfoAfterRemoveCircle(circle);
                }
                Message msg = mHandler.obtainMessage(CIRCLE_DELETE_END);
                msg.getData().putBoolean(RESULT, suc);
                msg.sendToTarget();

                synchronized (mLockdeleteCircle) {
                    inDeleteCircle = false;
                }
            }

            public void onException(TwitterException ex,TwitterMethod method) {
                synchronized (mLockdeleteCircle) {
                    inDeleteCircle = false;
                }

                Message msg = mHandler.obtainMessage(CIRCLE_DELETE_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }

    public void onCorpusSelected(String value) {
    	if(getString(R.string.home_label).equals(value)) {
    		Log.d(TAG, "Click home item , do nothing.");
    	}else {
    		try {
    			if(value != null && TextUtils.isDigitsOnly(value)) {
    				long circleid = Long.parseLong(value);
//    				if(circleid == mCircle.circleid) {
//    					Log.d(TAG, "select same circle, do nothing.");
//    					return ;
//    				}
    				UserCircle uc = orm.queryOneCircleWithGroup(circleid);
    				IntentUtil.startPublicCircleDetailIntent(this, uc);
    				if(uc.mGroup != null && uc.mGroup.formal == UserCircle.circle_top_formal) {
    					IntentUtil.loadCircleDirectoryFromServer(this, circleid);
    				}else {
    				}
    			}
    		} catch (Exception e) {
    		}
    	}
    }

    static Bitmap centerToFit(Bitmap bitmap, int width, int height, Context context) {
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();

        if (bitmapWidth < width || bitmapHeight < height) {
            int color = context.getResources().getColor(R.color.light_blue);

            Bitmap centered = Bitmap.createBitmap(bitmapWidth < width ? width : bitmapWidth,
                    bitmapHeight < height ? height : bitmapHeight, Bitmap.Config.RGB_565);
            centered.setDensity(bitmap.getDensity());
            Canvas canvas = new Canvas(centered);
            canvas.drawColor(color);
            canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(2,2,centered.getWidth()-2, centered.getHeight()-2),
                    null);

            bitmap = centered;
        }

        return bitmap;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode);
        if(requestCode == in_member_selectcode) {
            if(resultCode == Activity.RESULT_OK) {
                String selectUserIds = data.getStringExtra("toUsers");
                Log.d(TAG, "onActivityResult: " + selectUserIds);
                if(TextUtils.isEmpty(selectUserIds)) {
                    Log.d(TAG, "select null , do nothing ");
                }else {
                    deletePublicCirclePeople(mCircle.circleid, String.valueOf(AccountServiceUtils.getBorqsAccountID()), selectUserIds);
                }
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public long getTopStreamTargetId() {

        if (mCircle != null && mCircle.mGroup != null && mCircle.mGroup.viewer_can_update) {
            return mCircle.circleid;
        }

        return super.getTopStreamTargetId();
    }

    public long getCircleId() {
        if(mCircle != null) {
            return mCircle.circleid;
        }else {
            return 0;
        }
    }

    private void updatePageInfoAfterRemoveCircle(UserCircle circle) {
        if(circle.mGroup != null && circle.mGroup.pageid > 0) {
            ContentValues cv = new ContentValues();
            if(circle.mGroup.formal == UserCircle.circle_free) {
                cv.put(QiupuORM.PageColumns.FREE_CIRCLE_IDS, "");
            } else if(circle.mGroup.formal == UserCircle.circle_top_formal) {
                cv.put(QiupuORM.PageColumns.ASSOCIATED_ID, -1);
            }
            orm.updatePageInfo(circle.mGroup.pageid, cv);
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
//            if (mCurrentFragment == profile_detail) {
//                return true;
//            }
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (KeyEvent.KEYCODE_BACK == keyCode) {
//            if (mCurrentFragment == profile_detail) {
//                handlerBackKey(false);
//                return true;
//            }
//        }
//        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
//			showSearhView();
//			return true;
//		}
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected boolean isPersistActivity() {
        return true;
    }

    @Override
    public boolean onCancelled() {
        HomePickerActivity.unregisterPickerListener(getClass().getName());
        return false;
    }

    @Override
    public boolean onPicked(UserCircle circle) {
        HomePickerActivity.unregisterPickerListener(getClass().getName());
        finish();
        return true;
    }

    private View.OnClickListener mTogglePhotoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DialogUtils.ShowPhotoPickDialog(OrganizationHomeActivity.this, R.string.share_photo_title, new DialogUtils.PhotoPickInterface() {
                @Override
                public void doTakePhotoCallback() {
                	if(mCircle != null) {
                		long parent_id = -1;
                		if(mCircle.mGroup != null) {
                			if(mCircle.mGroup.formal == UserCircle.circle_top_formal) {
                				parent_id = mCircle.circleid;	
                			}else {
                				parent_id = mCircle.mGroup.parent_id;
                			}
                		}
                		IntentUtil.startTakingPhotoIntent(OrganizationHomeActivity.this, getDefaultRecipient(), parent_id, -1);
                	}
                }

                @Override
                public void doPickPhotoFromGalleryCallback() {
                	if(mCircle != null) {
                		long parent_id = -1;
                		if(mCircle.mGroup != null) {
                			if(mCircle.mGroup.formal == UserCircle.circle_top_formal) {
                				parent_id = mCircle.circleid;	
                			}else {
                				parent_id = mCircle.mGroup.parent_id;
                			}
                		}
                		IntentUtil.startPickingPhotoIntent(OrganizationHomeActivity.this, getDefaultRecipient(), parent_id, -1);
                	}
                }
            });
        }
    };

    private String getDefaultRecipient() {
        return "#" + mCircle.circleid;
    }

    @Override
    protected void showCorpusSelectionDialog(View view) {
        int location[] = new int[2];
        view.getLocationInWindow(location);
        int x = location[0];
        int y = getResources().getDimensionPixelSize(R.dimen.title_bar_height);

        ArrayList<SelectionItem> items =  getChildCircleNameArray();
        DialogUtils.showCorpusSelectionDialog(this, x, y, items, circleListItemClickListener);
    }


    AdapterView.OnItemClickListener circleListItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (CorpusSelectionItemView.class.isInstance(view)) {
                CorpusSelectionItemView item = (CorpusSelectionItemView) view;
                switchCircleStream(item.getText(), item.getItemId());
            }
        }
    };

    private ArrayList<SelectionItem> getChildCircleNameArray() {
        ArrayList<SelectionItem> circleNames = new ArrayList<SelectionItem>();

        if(QiupuApplication.mTopOrganizationId == null) {
        	Log.e(TAG, "getChildCircleNameArray, organization circle is null");
        	return circleNames;
        }
        if (QiupuApplication.mTopOrganizationId.circleid != mCircle.circleid) {
        	SelectionItem item = new SelectionItem(String.valueOf(QiupuApplication.mTopOrganizationId.circleid), QiupuApplication.mTopOrganizationId.name);
        	circleNames.add(item);
        }
        Cursor cursor = FORCE_SHOW_DROPDOWN ? orm.queryAllCircleList() : orm.queryChildCircleList(QiupuApplication.mTopOrganizationId.circleid);
        if (null != cursor && cursor.moveToFirst()) {
            String name;
            String cid;
            do {
                name = cursor.getString(cursor.getColumnIndex(QiupuORM.CircleColumns.CIRCLE_NAME));
                cid = cursor.getString(cursor.getColumnIndex(QiupuORM.CircleColumns.CIRCLE_ID));
                circleNames.add(new SelectionItem(cid, name));
            } while (cursor.moveToNext());
        }

        return circleNames;
    }

    private void switchCircleStream(String label, String id) {
        Log.d(TAG, "switchCircleStream, circle label = " + label + ", id = " + id);

        setHeadTitle(label);

        long circleId = Long.parseLong(id);
        mFragmentData.mCircleId = circleId;

        if (null != mHomeFragment) {
        	mHomeFragment.switchCircle(circleId);
        }
    }
    
    
    private View.OnClickListener showMoreActionListener = new View.OnClickListener() {
    	
    	@Override
    	public void onClick(View v) {
    		if(mMoreDialog == null) {
				mMoreDialog = new BottomMoreQuickAction(OrganizationHomeActivity.this, v.getWidth(), mCircle);
				mMoreDialog.show(v);
			}else {
				mMoreDialog.show(v);
			}
    	}
    };
	
    private View.OnClickListener searchClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	showSearhView();
        }
    };
    
//    @Override
//    public boolean onQueryTextSubmit(String query) {
//    	Log.d(TAG, "IntentUtil onQueryTextSubmit: " + query);
//    	if(mCurrentPage == PAGE_STRAM ) {
//    		if(query != null && query.length() > 0) {
//    			IntentUtil.startSearchActivity(this, query, BpcSearchActivity.SEARCH_TYPE_STREAM, mCircle.circleid);
//    		}else {
//    			Log.d(TAG, "onQueryTextSubmit, query is null " );
//    			ToastUtil.showShortToast(this, mHandler, R.string.search_recommend);
//    		}
//    	}else if(mCurrentPage == PAGE_RIGHT_INFO){
//    		if(mRightFragment != null) {
//    			mRightFragment.onQueryTextSubmit(query);
//    		}
//    	}
//    	return super.onQueryTextSubmit(query);
//    }

    @Override
    public boolean onQueryTextChange(String newText) {
		if(mCurrentPage == PAGE_RIGHT_INFO) {
			if(mRightFragment != null) {
				mRightFragment.doSearch(newText);
			}
		}
    	return super.onQueryTextChange(newText);
    }
    
	@Override
	public void getStreamRightFlipperFragment(
			StreamRightFlipperFragment fragment) {
		mRightFragment = fragment;
	}

	@Override
	public CustomViewPager getParentViewPager() {
		return mPager;
	}

	@Override
	public void startSearch() {
		showSearhView();
	}

	@Override
	public void hidSearch() {
		hideSearhView();
	}

	@Override
	public void finishActivity() {
		finish();
	}

	@Override
	public int getCurentIndex() {
		if(mRightFragment != null) {
			return mRightFragment.getCurrentIndex();
		}
		return -1;
	}
}
