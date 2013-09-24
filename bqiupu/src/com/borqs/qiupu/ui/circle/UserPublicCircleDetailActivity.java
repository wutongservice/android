package com.borqs.qiupu.ui.circle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.PageInfo;
import twitter4j.PublicCircleRequestUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.SelectionItem;
import com.borqs.common.adapter.CircleFilpperFragmentAdapter;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.common.view.CustomViewPager;
import com.borqs.common.view.CustomViewPager.CustomViewPagerListenter;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.CircleCirclesColumns;
import com.borqs.qiupu.db.QiupuORM.PageColumns;
import com.borqs.qiupu.fragment.PublicCircleMainFragment;
import com.borqs.qiupu.fragment.PublicCircleMainFragment.CircleMainFragmentCallBack;
import com.borqs.qiupu.fragment.StreamListFragment;
import com.borqs.qiupu.fragment.StreamListFragment.StreamListFragmentCallBack;
import com.borqs.qiupu.fragment.StreamRightFlipperFragment;
import com.borqs.qiupu.fragment.StreamRightFlipperFragment.StreamRightFlipperCallBack;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.BasicNavigationActivity;
import com.borqs.qiupu.ui.bpc.BpcSearchActivity;
import com.borqs.qiupu.ui.circle.quickAction.BottomMoreQuickAction;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;

public class UserPublicCircleDetailActivity extends BasicNavigationActivity  implements  
StreamListFragmentCallBack, CircleMainFragmentCallBack, StreamRightFlipperCallBack, CustomViewPagerListenter{

	private static final String TAG = "UserPublicCircleDetailActivity";

    private UserCircle mCircle;
    private PublicCircleMainFragment mCircleMainFragment;
    private StreamRightFlipperFragment mRightFragment;
    
    
    StreamListFragment.MetaData mFragmentData = new StreamListFragment.MetaData();
    public static final int in_member_selectcode = 5555;
    
    private final static int PAGE_STRAM = 0;
    private final static int PAGE_RIGHT_INFO = 1;
    private int mCurrentPage = PAGE_STRAM;
    
    private CircleFilpperFragmentAdapter mAdapter;
    private CustomViewPager mPager;

    private BottomMoreQuickAction mMoreDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate");
    	enableTitleNtf(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.circle_profile_main);

        parseActivityIntent(getIntent());

         if(QiupuConfig.isEventIds(mCircle.circleid)) {
        	UserCircle circle = new UserCircle();
            circle.circleid = mCircle.circleid;
            circle.name = mCircle.name;
            IntentUtil.startEventDetailIntent(this, circle);
            finish();
            return ;
         }
        setHeadTitle(getString(R.string.user_circles));

        setupActionButtons();
        
        View actionView;
        actionView = findViewById(R.id.toggle_search);
        if (null != actionView) {
            actionView.setVisibility(View.VISIBLE);
            actionView.setOnClickListener(searchClickListener);
        }
//        actionView = findViewById(R.id.toggle_poll);
//        if (null != actionView) {
//            actionView.setVisibility(View.VISIBLE);
//            actionView.setOnClickListener(createPoll);
//        }
        actionView = findViewById(R.id.toggle_photo);
        if (null != actionView) {
            actionView.setOnClickListener(photoStreamListener);
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
        
        mAdapter = new CircleFilpperFragmentAdapter(getSupportFragmentManager(), this);
        mPager = (CustomViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(mCurrentPage);
        mPager.setListener(this);
        mPager.setIndex(-1);
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int pos) {
				Log.d(TAG, "onPageSelected: " + pos);
				if(pos == 1) {
					mCurrentPage = PAGE_RIGHT_INFO;
					mPager.setIndex(0);
					if(mRightFragment != null) {
						mRightFragment.setisCurrentScreen(true);
					}
					if(mCircleMainFragment != null) {
						mCircleMainFragment.onScrollingBottomView(false, foot);
					}
				}else {
					mCurrentPage = PAGE_STRAM;
					mPager.setIndex(-1);
					if(mRightFragment != null) {
						mRightFragment.setisCurrentScreen(false);
					}
					if(mCircleMainFragment != null) {
						mCircleMainFragment.onScrollingBottomView(true, foot);
					}
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
    }
    
    private void setupActionButtons() {
         overrideRightActionBtn(R.drawable.home_screen_menu_people_icon_default, editProfileClick);
         
        View actionView;
        actionView = findViewById(R.id.toggle_search);
        if (null != actionView) {
            actionView.setVisibility(View.VISIBLE);
            actionView.setOnClickListener(searchClickListener);
        }
//        actionView = findViewById(R.id.toggle_poll);
//        if (null != actionView) {
//            actionView.setVisibility(View.VISIBLE);
//            actionView.setOnClickListener(createPoll);
//        }
        actionView = findViewById(R.id.toggle_photo);
        if (null != actionView) {
            actionView.setOnClickListener(photoStreamListener);
        }
        actionView = findViewById(R.id.toggle_composer);
        if (null != actionView) {
            actionView.setOnClickListener(gotoComposeListener);
        }
        
        actionView = findViewById(R.id.toggle_more);
        if (null != actionView) {
            actionView.setOnClickListener(showMoreActionListener);
        }
    }

    private View.OnClickListener showMoreActionListener = new View.OnClickListener() {
    	
    	@Override
    	public void onClick(View v) {
    		if(mMoreDialog == null) {
    			mMoreDialog = new BottomMoreQuickAction(UserPublicCircleDetailActivity.this, v.getWidth(), mCircle);
    			mMoreDialog.show(v);
    		}else {
    			mMoreDialog.show(v);
    		}
    	}
    };
    
    private View.OnClickListener photoStreamListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	final HashMap<String, String> receiverMap = new HashMap<String, String>();
        	final String receiverid = "#" +  mFragmentData.mCircleId;
        	receiverMap.put(String.valueOf(mFragmentData.mCircleId), mCircle.name);
            DialogUtils.ShowPhotoPickDialog(UserPublicCircleDetailActivity.this, R.string.share_photo_title, new DialogUtils.PhotoPickInterface() {
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
                		IntentUtil.startTakingPhotoIntent(UserPublicCircleDetailActivity.this, receiverid, receiverMap, parent_id, mCircle.circleid);
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
                		IntentUtil.startPickingPhotoIntent(UserPublicCircleDetailActivity.this, receiverid, receiverMap, parent_id, mCircle.circleid);
                	}
                }
            });
        }
    };
    
    @Override
    protected void createHandler() {
    	mHandler = new MainHandler();
    }

    @Override
    protected void loadSearch() {
    }

    private void parseActivityIntent(Intent intent) {
        String url = getIntentURL(intent);
        if (TextUtils.isEmpty(url)) {
            String requestName = null;
            Bundle bundle = intent.getExtras();
            requestName = bundle.getString(CircleUtils.CIRCLE_NAME);
            mFragmentData.mCircleId = bundle.getLong(CircleUtils.CIRCLE_ID, -1);
            mFragmentData.mFragmentTitle = getString(R.string.circle_detail_post);
            
            mCircle = orm.queryOneCircleWithGroup(mFragmentData.mCircleId);
            if (mCircle == null){
            	mCircle = (UserCircle) intent.getSerializableExtra(CircleUtils.CIRCLEINFO);
            }

//            setHeadTitle(requestName);
        }else {
            final String circleId = BpcApiUtils.parseSchemeValue(intent,
                    BpcApiUtils.SEARHC_KEY_CIRCLEID);
            mFragmentData.mCircleId = Long.parseLong(circleId);
            mCircle = orm.queryOneCircleWithGroup(mFragmentData.mCircleId);
        }
        
        if(mCircle == null) {
    		mCircle = new UserCircle();
    		mCircle.circleid = mFragmentData.mCircleId;
    	}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    };

    @Override
    protected void loadRefresh() {
    	if(mCircleMainFragment != null) {
    		mCircleMainFragment.refreshCircle();
    	}
    }

    @Override
    protected void uiLoadEnd() {
        showProgressBtn(false);
        showLeftActionBtn(false);
    }

    @Override
    public StreamListFragment.MetaData getFragmentMetaData(int index) {
        return mFragmentData;
    }

    @Override
    public String getSerializeFilePath() {
        return QiupuHelper.circle + mFragmentData.mCircleId;
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	View.OnClickListener editProfileClick = new View.OnClickListener() {
        public void onClick(View v) {
        	ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
        	items.add(new SelectionItem("", getString(R.string.home_label)));
        	final String homeid = QiupuORM.getSettingValue(UserPublicCircleDetailActivity.this, QiupuORM.HOME_ACTIVITY_ID);
        	long homeScene = 0;
        	if(TextUtils.isEmpty(homeid) == false) {
        		try {
        			homeScene = Long.parseLong(homeid);
        		} catch (Exception e) {
        			Log.d(TAG, "homeid is not number or is null");
        		}
        	}
        	if(homeScene > 0) {
        		UserCircle homecircle = orm.queryOneCircle(AccountServiceUtils.getBorqsAccountID(), homeScene);
        		if(homecircle != null) {
        			items.add(new SelectionItem(String.valueOf(homecircle.circleid), homecircle.name));
        		}
                Cursor pOrgazitaionCircles = orm.queryInCircleCircles(homeScene);
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
                	Log.d(TAG, "have no circle's Circles");
                }
        	}else {
        		Log.d(TAG, "the global scene is null");
        	}
            
//        	ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
//        	items.add(new SelectionItem("", getString(R.string.label_refresh)));
//        	
//        	if(mCircle.mGroup != null) {
//    			if(mCircle.mGroup.viewer_can_update) {
//    				items.add(new SelectionItem("", getString(R.string.edit_string)));
//    			}
//    			
//    			if(mCircle.mGroup.formal == UserCircle.circle_free) {
//    				Log.d(TAG, "this circle is free circle, no need show pageitem");
//    			}else {
//    				items.add(new SelectionItem("", getString(R.string.page_label)));
//    			}
//    		}
//    		
//    		items.add(new SelectionItem("", getString(R.string.public_circle_shortcut)));
//    		
//    		if(mCircle.mGroup != null && PublicCircleRequestUser.isInGroup(mCircle.mGroup.role_in_group)) {
//    			if(PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group)
//    					|| PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group)) {
//        			if(mCircle.mGroup.pageid <=0 && (mCircle.mGroup.formal == UserCircle.circle_top_formal || mCircle.mGroup.formal == UserCircle.circle_sub_formal)) {
//        				items.add(new SelectionItem("", getString(R.string.circle_as_page)));
//        			}
//        		}
//
//    			if(mCircle.mGroup.parent_id <= 0 && mCircle.mGroup.formal == UserCircle.circle_top_formal) {
//    				items.add(new SelectionItem("", getString(R.string.create_public_circle_title)));
//    			}
//    			
//    			items.add(new SelectionItem("", getString(R.string.create_event_label)));
//        		items.add(new SelectionItem("", getString(R.string.create_poll_label)));
//        		
//        		items.add(new SelectionItem("", getString(R.string.public_circle_receive_set)));
//        		if(mCircle.mGroup.viewer_can_destroy) {
//        			items.add(new SelectionItem("", getString(R.string.delete)));
//        		}
//        		
//        		if(mCircle.mGroup.viewer_can_quit) {
//        			items.add(new SelectionItem("", getString(R.string.circle_exit_label)));
//        			isDirectExit = true;
//        		}else {
//        			if(PublicCircleRequestUser.isCreator(mCircle.mGroup.role_in_group)
//        					|| PublicCircleRequestUser.isManager(mCircle.mGroup.role_in_group)) {
//        				if(mCircle.mGroup.can_member_quit == UserCircle.VALUE_ALLOWED) {
//        					items.add(new SelectionItem("", getString(R.string.public_circle_exit)));
//        					isDirectExit = false;
//        				}
//        			}
//        		}
//    		}
//        	
        	showCorpusSelectionDialog(items);
        }
    };
    
    View.OnClickListener gotoComposeListener = new OnClickListener() {
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
    
    OnItemClickListener actionListItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	
        	if(CorpusSelectionItemView.class.isInstance(view)) {
        		CorpusSelectionItemView item = (CorpusSelectionItemView) view;
        		String selectedstring = item.getItemId();
        		if(TextUtils.isEmpty(selectedstring)) {
        			selectedstring = item.getText();
        		}
        		onCorpusSelected(selectedstring);             
        	}
        }
    };

	@Override
	public UserCircle getCircleInfo() {
		return mCircle;
	}
	
	public static void startPublicCircleDetailIntent(Context context, UserCircle circle) {
        if (null != context && null != circle) {
            final Intent intent = new Intent(context, UserPublicCircleDetailActivity.class);

            Bundle bundle = new Bundle();
            bundle.putString(CircleUtils.CIRCLE_NAME,
                    CircleUtils.getLocalCircleName(context, circle.circleid, circle.name));
            bundle.putLong(CircleUtils.CIRCLE_ID, circle.circleid);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }

    private void startComposeActivity() {
        boolean isAdmin = false;
        if (mCircle != null) {
        	HashMap<String, String> receiverMap = new HashMap<String, String>();
        	String receiverid = "";
//        	if(mCircle.mGroup != null) {
//        		if(mCircle.mGroup.formal == UserCircle.circle_top_formal) {
//        			receiverid = "#-2";
//        			receiverMap.put("-2", getString(R.string.circle_id_public));
//        		}else {
//        			receiverid = "#" +  mFragmentData.mCircleId;
//        			receiverMap.put("-2", mCircle.name);
//        		}
//        		
//        	}
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
        	
        	IntentUtil.startComposeActivity(this, receiverid, true, isAdmin, receiverMap, parent_id, mCircle.circleid);
        }else {
        	Log.d(TAG, "startComposeActivity, circle is null ");
        }
    }

	private final static int INVIT_EPEOPLE_END = 101;
	private final static int EXIT_CIRCLE_END = 102;
	private final static int CIRCLE_DELETE_END = 103;
	private final static int CIRCLE_AS_PAGE_END = 104;
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
                    ToastUtil.showOperationFailed(UserPublicCircleDetailActivity.this, mHandler, true);
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
            		ToastUtil.showOperationFailed(UserPublicCircleDetailActivity.this, mHandler, true);
            	}
            	break;
            }
            }
        }
    }
	
	boolean inDeletePeople;
    Object mLockDeletePeople = new Object();
    private void deletePublicCirclePeople(final UserCircle circle, final String userids, final String admins) {
        if (inDeletePeople == true) {
            Toast.makeText(this, R.string.string_in_processing, Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (mLockDeletePeople) {
            inDeletePeople = true;
        }
        showDialog(DIALOG_SET_CIRCLE_PROCESS);
        
        asyncQiupu.deletePublicCirclePeople(AccountServiceUtils.getSessionID(), circle.circleid, userids, admins, new TwitterAdapter() {
            public void deletePublicCirclePeople(boolean result) {
                Log.d(TAG, "finish deletePublicCirclePeople=" + result);
                if(result) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                          //delete circle in DB
                            orm.deleteCircleByCricleId(AccountServiceUtils.getBorqsAccountID(), String.valueOf(circle.circleid));
                            //update user info
//                            orm.updateUserInfoInCircle(AccountServiceUtils.getBorqsAccountID(), circleid, circleName);
                            // exit the circle, need remove cache circle
                            orm.deleteCacheCircleCircle(circle);
                            
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
                
                if(suc)
                {
                    //delete circle in DB
                    orm.deleteCircleByCricleId(AccountServiceUtils.getBorqsAccountID(), String.valueOf(circle.circleid));
                    //update user info
                    orm.updateUserInfoInCircle(AccountServiceUtils.getBorqsAccountID(), String.valueOf(circle.circleid), circleName);
                    
                    updatePageInfoAfterRemoveCircle(circle);
                     //update circle_circle table
                    orm.deleteCacheCircleCircle(circle);
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
	@Override
	public void changeHeadTitle(UserCircle circle) {
	    mCircle = circle;
//	    if(mCircleInfoFragment != null) {
//	    	mCircleInfoFragment.refreshCircle(circle);
//	    }
	}

    public void onCorpusSelected(String value) {
    	if(getString(R.string.home_label).equals(value)) {
    		Log.d(TAG, "Click home item , do nothing.");
    		finish();
    	}else {
    		try {
    			if(value != null && TextUtils.isDigitsOnly(value)) {
    				long circleid = Long.parseLong(value);
    				if(circleid == mCircle.circleid) {
    					Log.d(TAG, "select same circle, do nothing.");
    					return ;
    				}
    				// go to other top circle
    				UserCircle uc = orm.queryOneCircleWithGroup(circleid);
    				IntentUtil.startPublicCircleDetailIntent(this, uc);
    				if(uc.mGroup != null && uc.mGroup.formal == UserCircle.circle_top_formal) {
    					IntentUtil.loadCircleDirectoryFromServer(this, circleid);
    				}else {
    				}
    				finish();
    			}
    		} catch (Exception e) {
    			Log.d(TAG, "start circle exception." + e.getMessage());
    		}
    	}
    	
//        if(getString(R.string.label_refresh).equals(value)) {
//            loadRefresh();
//        }else if(getString(R.string.public_circle_exit).equals(value)) {
//            if(isDirectExit) {
//                DialogUtils.showConfirmDialog(this, R.string.public_circle_exit, R.string.public_circle_exit_message, 
//                        R.string.label_ok, R.string.label_cancel, ExitListener);
//            }else {
//                DialogUtils.showExitPublicDialog(this,  R.string.public_circle_exit_toast, R.string.public_circle_exit_prompt, ExitNeutralListener, ExitNegativeListener);
//            }
//        } 
//        else if(getString(R.string.public_circle_shortcut).equals(value))
//        {
//        	final Intent intent = createShortcutIntent(mCircle.uid,  mCircle.name, mCircle.mGroup.coverUrl);            
//            Activity a = getParent() == null ? this : getParent();
//            a.sendBroadcast(intent);         
//        }else if(getString(R.string.public_circle_receive_set).equals(value)) {
//        	IntentUtil.gotoEditPublicCircleActivity(this, mCircle.name, mCircle, EditPublicCircleActivity.edit_type_receive_set);
////        	final Intent intent = new Intent(UserPublicCircleDetailActivity.this, PubliccircleSettingsActivity.class);
////        	intent.putExtra(CircleUtils.CIRCLE_ID, mCircle.circleid);
////        	startActivity(intent);
//        }else if(getString(R.string.edit_string).equals(value)) {
//        	if(QiupuConfig.isEventIds(mCircle.circleid)) {
//        		Intent intent = new Intent(this, EditEventActivity.class);
//        		intent.putExtra(CircleUtils.CIRCLEINFO, mCircle);
//        		startActivity(intent);
//        	}else {
//        		if(mCircleMainFragment != null) {
//        			mCircleMainFragment.showEditGroupInfoDialog();
//        		}
//        	}
////        }else if(getString(R.string.home_album).equals(value)) {
////        	IntentUtil.startAlbumIntent(this, mCircle.circleid,mCircle.name);
//        }else if(getString(R.string.delete).equals(value)) {
//        	int dialogMes = -1;
//        	if(QiupuConfig.isEventIds(mCircle.circleid)) {
//        		dialogMes = R.string.delete_event_message;
//        	}else {
//        		dialogMes = R.string.delete_public_circle_message;
//        	}
//        	DialogUtils.showConfirmDialog(this, R.string.delete, dialogMes, 
//                    R.string.label_ok, R.string.label_cancel, ExitNeutralListener);
//        }else if(getString(R.string.create_public_circle_title).equals(value)) {
//        	Intent intent = new Intent(this, CreateCircleMainActivity.class);
//        	intent.putExtra(UserCircle.PARENT_ID, mCircle.circleid);
//        	intent.putExtra(CreateCircleMainActivity.SUBTYPE, mCircle.mGroup.subtype);
//        	startActivity(intent);
//        }else if(getString(R.string.circle_as_page).equals(value)) {
//        	circleAsPage(mCircle);
//        }else if(getString(R.string.create_event_label).equals(value)) {
//        	if(mCircleMainFragment != null) {
//        		mCircleMainFragment.gotoCreateEventActivity();
//        	}else {
//        		Log.v(TAG, "circle main fragment is null");
//        	}
//        }else if(getString(R.string.create_poll_label).equals(value)) {
//        	if(mCircleMainFragment != null) {
//        		mCircleMainFragment.gotoCreatePollActivity();
//        	}else {
//        		Log.v(TAG, "circle main fragment is null");
//        	}
//        }else if(getString(R.string.page_label).equals(value)) {
//        	if(mCircleMainFragment != null) {
//        		mCircleMainFragment.gotoPageDetail();
//        	}
//        }
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

    private Intent createShortcutIntent(long uid, String title, String pic_url) {
    	final Intent i = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
    	
    	final Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);	    
    	shortcutIntent.setData(Uri.parse("borqs://profile/details?uid="+uid));
    	i.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
    	i.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
    	
    	//this is photo
    	Bitmap bmp = null;
    	String filepath = QiupuHelper.getImagePathFromURL_noFetch(pic_url);
    	if(new File(filepath).exists())
    	{
    		try{				    
    			bmp = BitmapFactory.decodeFile(filepath);	    	    
    		}
    		catch(Exception ne){}
    	}
    	else
    	{
    		try{				    
    			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.home_screen_menu_people_icon_default);	    	    
    		}
    		catch(Exception ne){}		    
    	}
    	
    	if(bmp == null)
    	{
    		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.home_screen_menu_people_icon_default);
    	}
    	
    	//resize the bmp to 60dip
    	final int  width = (int)(48*getResources().getDisplayMetrics().density);
    	bmp = centerToFit(bmp, width, width, this);
    	
    	Bitmap favicon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_bpc_launcher);
    	
    	// Make a copy of the regular icon so we can modify the pixels.        
    	Bitmap copy = bmp.copy(Bitmap.Config.ARGB_8888, true);
    	Canvas canvas = new Canvas(copy);
    	
    	// Make a Paint for the white background rectangle and for
    	// filtering the favicon.
    	Paint p = new Paint(Paint.ANTI_ALIAS_FLAG
    			| Paint.FILTER_BITMAP_FLAG);
    	p.setStyle(Paint.Style.FILL_AND_STROKE);
    	p.setColor(Color.WHITE);
    	
    	final DisplayMetrics metrics = this.getResources().getDisplayMetrics();
    	final float density = metrics.density;
    	
    	// Create a rectangle that is slightly wider than the favicon
    	final float iconSize = 8*density; // 16x16 favicon
    	final float padding = 1*density;   // white padding around icon
    	final float rectSize = iconSize + 2 * padding;
    	final float y = bmp.getHeight() - rectSize;
    	RectF r = new RectF(0, y, rectSize, y + rectSize);
    	
    	// Draw a white rounded rectangle behind the favicon
    	canvas.drawRoundRect(r, 2, 2, p);
    	
    	// Draw the favicon in the same rectangle as the rounded rectangle
    	// but inset by the padding (results in a 16x16 favicon).
    	r.inset(padding, padding);
    	canvas.drawBitmap(favicon, null, r, p);
    	i.putExtra(Intent.EXTRA_SHORTCUT_ICON, copy);
    	
    	// Do not allow duplicate items
    	i.putExtra("duplicate", false);
    	return i;
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
                    deletePublicCirclePeople(mCircle, String.valueOf(AccountServiceUtils.getBorqsAccountID()), selectUserIds);
                }
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    private DialogInterface.OnClickListener ExitListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            deletePublicCirclePeople(mCircle, String.valueOf(AccountServiceUtils.getBorqsAccountID()), null);
        }
    };
    
    private DialogInterface.OnClickListener ExitNeutralListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            deleteCircleFromServer(mCircle, mCircle.name, mCircle.type);
        }
    };
    
    private DialogInterface.OnClickListener ExitNegativeListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(UserPublicCircleDetailActivity.this, PublicCircleMemberPickActivity.class);
            intent.putExtra(CircleUtils.CIRCLE_ID, mCircle.circleid);
            startActivityForResult(intent, in_member_selectcode);
        }
    };

	@Override
	public void changeshareSource(long uid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getCircleInfoFragment(PublicCircleMainFragment fragment) {
		mCircleMainFragment = fragment;
	}
	
	@Override
    public long getTopStreamTargetId() {
        Log.d(TAG, "getTopStreamTargetId() viewer_can_update = " + mCircle.mGroup.viewer_can_update);

        if (mCircle.mGroup.viewer_can_update) {
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
				cv.put(PageColumns.FREE_CIRCLE_IDS, "");
			} else if(circle.mGroup.formal == UserCircle.circle_top_formal) {
				cv.put(PageColumns.ASSOCIATED_ID, -1);
			}
			orm.updatePageInfo(circle.mGroup.pageid, cv);
		}
	}
	
	private void circleAsPage(final UserCircle circle) {
		HashMap<String, String> infoMap = new HashMap<String, String>();
		if(circle != null) {
			infoMap.put("name", circle.name);
			infoMap.put("name_en", circle.name);
			infoMap.put("address", circle.location);
			infoMap.put("address_en", circle.location);
			infoMap.put("description", circle.description);
			infoMap.put("description_en", circle.description);
			infoMap.put("description_en", circle.description);
			infoMap.put("description_en", circle.description);
			if(circle.emailList != null && circle.emailList.size() > 0) {
				infoMap.put("email", circle.emailList.get(0).info);
			}
			if(circle.phoneList != null && circle.phoneList.size() > 0) {
				infoMap.put("tel", circle.phoneList.get(0).info);
			}
			infoMap.put("small_logo_url", circle.profile_simage_url);
			infoMap.put("logo_url", circle.profile_image_url);
			infoMap.put("large_logo_url", circle.profile_limage_url);
			if(circle.mGroup != null) {
				infoMap.put("cover_url", circle.mGroup.coverUrl);	
			}
			
			circleAsPage(circle.circleid, infoMap);
		}else {
			Log.d(TAG, "circle as page, circle is null ");
		}
	}
	
	boolean inAsPage;
    Object mLockAsPage = new Object();
    
    public void circleAsPage(final long circleid, HashMap<String, String> infoMap) {
        if (inAsPage == true) {
            Toast.makeText(this, R.string.string_in_processing,Toast.LENGTH_SHORT).show();
            return ;
        }
        
        synchronized (mLockAsPage) {
        	inAsPage = true;
        }
        showDialog(DIALOG_SET_CIRCLE_PROCESS);
        asyncQiupu.circleAsPage(AccountServiceUtils.getSessionID(), circleid, infoMap, new TwitterAdapter() {
            public void circleAsPage(PageInfo info) {
                Log.d(TAG, "finish circleAsPage=" + info.page_id);
                
                // insert pageinfo to db
                orm.insertOnePage(info);
                
                //update current circle pageid
                if(mCircle != null && mCircle.mGroup != null) {
                	mCircle.mGroup.pageid = info.page_id; 	
                	orm.updateCirclePageId(mCircle.circleid, info.page_id);
                }
                
                Message msg = mHandler.obtainMessage(CIRCLE_AS_PAGE_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();
                
                synchronized (mLockAsPage) {
                	inAsPage = false;
                }
            }
            
            public void onException(TwitterException ex,TwitterMethod method) {
                synchronized (mLockAsPage) {
                	inAsPage = false;
                }
                
                Message msg = mHandler.obtainMessage(CIRCLE_AS_PAGE_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }

	@Override
	public void createAspage() {
		circleAsPage(mCircle);
	}
	
	View.OnClickListener searchClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	showSearhView();
        }
    };

    @Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			showSearhView();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
    
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
	public void refreshRightFragmentUi(UserCircle circle) {
		if(mRightFragment != null) {
			mRightFragment.refreshUI(circle);
		}
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
	public void exitCircle(boolean isDirect) {
		if(isDirect) {
            DialogUtils.showConfirmDialog(this, R.string.public_circle_exit, R.string.public_circle_exit_message, 
                    R.string.label_ok, R.string.label_cancel, ExitListener);
        }else {
            DialogUtils.showExitPublicDialog(this,  R.string.public_circle_exit_toast, R.string.public_circle_exit_prompt, ExitNeutralListener, ExitNegativeListener);
        }
	}

	@Override
	public void circleToPage(UserCircle circle) {
		circleAsPage(mCircle);
	}

	@Override
	public void deleteCircle() {
		int dialogMes = -1;
		if(QiupuConfig.isEventIds(mCircle.circleid)) {
			dialogMes = R.string.delete_event_message;
		}else {
			dialogMes = R.string.delete_public_circle_message;
		}
		DialogUtils.showConfirmDialog(this, R.string.delete, dialogMes, 
				R.string.label_ok, R.string.label_cancel, ExitNeutralListener);		
	}

	@Override
	public void createShortcut() {
		final Intent intent = createShortcutIntent(mCircle.uid,  mCircle.name, mCircle.mGroup.coverUrl);
		Activity a = getParent() == null ? this : getParent();
		a.sendBroadcast(intent);		
	}

	@Override
	public void refreshActivityBottom() {
		final View foot = findViewById(R.id.bottom_actions_layout);
		if(mCurrentPage == PAGE_STRAM && mCircleMainFragment != null) {
			mCircleMainFragment.onScrollingBottomView(true, foot);
		}
	}

	@Override
	public int getCurentIndex() {
		if(mRightFragment != null) {
			return mRightFragment.getCurrentIndex();
		}
		return -1;
	}
}
