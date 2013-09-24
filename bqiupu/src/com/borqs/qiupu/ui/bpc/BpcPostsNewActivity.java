package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import twitter4j.UserCircle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.borqs.common.SelectionItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.OnListItemClickListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.PushingServiceAgent;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.common.view.EnfoldmentView;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.CircleColumns;
import com.borqs.qiupu.fragment.StreamListFragment;
import com.borqs.qiupu.ui.BasicNavigationActivity;
import com.borqs.qiupu.ui.circle.quickAction.BottomMoreQuickAction;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;
import com.borqs.wutong.HomePickerActivity;

public class BpcPostsNewActivity extends BasicNavigationActivity implements
        OnListItemClickListener, StreamListFragment.StreamListFragmentCallBack,
        HomePickerActivity.PickerInterface {

	private static final String TAG = "Qiupu.BpcPostsNewActivity";

    private static final boolean isLowPerformance = false || QiupuConfig.LowPerformance;

    StreamListFragment.MetaData mFragmentData;

    StreamListFragment mStreamListFragment;
    
    private BottomMoreQuickAction mMoreDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate");

        enableLeftNav();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.stream_fragment_activity);

        PushingServiceAgent.bindNotificationService(getApplicationContext());
        
     // Create the list fragment and add it as our sole content.
        mStreamListFragment = (StreamListFragment) getSupportFragmentManager().findFragmentById(R.id.stream_fragment);
//        if (mStreamListFragment == null) {
//            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            mStreamListFragment = new StreamListFragment();
//            ft.add(R.id.embedded_fragment, mStreamListFragment).commit();
//        }
        
        if (savedInstanceState == null) {
            mFragmentData  = new StreamListFragment.MetaData();
        } else {
            unpackParcel(savedInstanceState);
        }

        parseActivityIntent(getIntent());

        orm.checkExpandCirCle();

        new Handler().post(new Runnable() {
            
            @Override
            public void run() {
                Cursor localCircles = orm.queryLocalCircles();
                if(null != localCircles && localCircles.getCount() <= 0) {
                    orm.removeAllCircles();
                    orm.insertExpandCirCleInfo();
                    IntentUtil.loadCircleFromServer(BpcPostsNewActivity.this);
                }
                QiupuORM.closeCursor(localCircles);
            }
        });
    }

    @Override
    protected void createHandler() {
    }

    @Override
    protected void loadSearch() {
    	showSearhView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_STREAM_FILTER) {
            if (resultCode == Activity.RESULT_OK && null != data) {
                final int filterType = data.getIntExtra(BpcApiUtils.SEARCH_KEY_TYPE, mFragmentData.mSourceFilter);
                Log.d(TAG, "onActivityResult, get type: " + filterType);
                if (null != mStreamListFragment) {
                    mStreamListFragment.applyFilterType(filterType);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void parseActivityIntent(Intent intent) {
        final int initType = BpcApiUtils.parseSearchedStreamType(intent);

        final int appId = BpcApiUtils.parseSearchedSteamAppId(intent);
        if (initType != mFragmentData.mSourceFilter) {
            if (appId != mFragmentData.mSourceAppKey) {
                mFragmentData.mSourceFilter = initType;
            } else {
                mFragmentData.mSourceFilter = initType;
            }
        }

        if (appId != mFragmentData.mSourceAppKey) {
            mFragmentData.mSourceAppKey = appId;
        }

        mFragmentData.mUserId = QiupuConfig.USER_ID_ALL;
        mFragmentData.mCircleId = QiupuConfig.CIRCLE_ID_ALL;
        mFragmentData.mFragmentTitle = getString(R.string.circle_detail_post);

        boolean for_appbox = intent.getBooleanExtra("for_appbox", false);
        if (for_appbox) {
            showTitleSpinnerIcon(false);
            mFragmentData.mSourceFilter = BpcApiUtils.ONLY_PURE_APK_POST;
            setHeadTitle(mFragmentData.mFragmentTitle);
        } else {
            setHeadTitle(R.string.circle_id_all);
            showTitleSpinnerIcon(true);
        }

        setupActionButtons(for_appbox);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);

        packParcel(outState);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (null != savedInstanceState) {
            unpackParcel(savedInstanceState);
        }
    }

    private static final String KET_FRAGMENT_DATA = "KET_FRAGMENT_DATA";
    private void packParcel(Bundle outState) {
        outState.putParcelable(KET_FRAGMENT_DATA, mFragmentData);
    }

    private void unpackParcel(Bundle inState) {
        if (null != inState) {
            mFragmentData = (StreamListFragment.MetaData)inState.getParcelable(KET_FRAGMENT_DATA);
        }
    }

    @Override
    protected void loadRefresh() {
        if(mStreamListFragment != null) {
            mStreamListFragment.loadRefresh();
        }else {
            Log.d(TAG, "loadRefresh() mStreamListFragment is null ");
        }
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
        return null;
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PushingServiceAgent.stopNotificationService(this);
	}

    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_SEARCH) {
   			showSearhView();
   			return true;
   		}
    	
        if (!EnfoldmentView.mIsClose) {
            return true;
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    protected void showCorpusSelectionDialog(View view) {
        int location[] = new int[2];
        view.getLocationInWindow(location);
        int x = location[0];
        int y = getResources().getDimensionPixelSize(R.dimen.title_bar_height);

        ArrayList<SelectionItem> items =  getCircleNameArray();
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

    private ArrayList<SelectionItem> getCircleNameArray() {
        ArrayList<SelectionItem> circleNames = new ArrayList<SelectionItem>();
        circleNames.add(new SelectionItem(String.valueOf(QiupuConfig.CIRCLE_ID_ALL), getString(R.string.circle_id_all)));
        circleNames.add(new SelectionItem(String.valueOf(QiupuConfig.CIRCLE_ID_HOT), getString(R.string.circle_id_hot)));
        circleNames.add(new SelectionItem(String.valueOf(QiupuConfig.CIRCLE_ID_NEAR_BY), getString(R.string.circle_id_nearby)));
        circleNames.add(new SelectionItem(String.valueOf(QiupuConfig.CIRCLE_ID_PUBLIC), getString(R.string.public_dynamic)));

        ArrayList<UserCircle> userCircles = orm.queryLocalCircleList();
        for (UserCircle circle : userCircles) {
            if (!QiupuHelper.inFilterCircle(String.valueOf(circle.circleid))) {
                circleNames.add(new SelectionItem(String.valueOf(circle.circleid),
                        CircleUtils.getLocalCircleName(this, circle.circleid, circle.name)));
            }
        }

        return circleNames;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menu_search).setVisible(false);
        menu.findItem(R.id.menu_stream_filter).setVisible(null != mStreamListFragment && mStreamListFragment.isFilterNeeded() && !isUsingActionBar());

        return true;
    }

    @Override
    protected void onAccountLoginCancelled() {
        super.onAccountLoginCancelled();
        if (!fromtab) {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_stream_filter) {
            mStreamListFragment.filterStream();
        } else {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void switchCircleStream(String label, String id) {
        Log.d(TAG, "switchCircleStream, circle label = " + label + ", id = " + id);

        setHeadTitle(label);

        long circleId = Long.parseLong(id);
        mFragmentData.mCircleId = circleId;

        if (null != mStreamListFragment) {
            mStreamListFragment.switchCircle(circleId);
        }
    }

//	@Override
//	public void setLeftMenuPosition() {
//		mPosition = LeftMenuMapping.getPositionForActivity(this);
//		mTitle = getString(R.string.dynamic_friends);
//	}

    /**
     * Override and set itself as persist activity.
     * @return true to keep while navigating to other activities from navigation panel.
     */
    @Override
    protected boolean isPersistActivity() {
//        mPosition = LeftMenuMapping.getPositionForActivity(this);
        return true;
    }

    private void setupActionButtons (boolean forApp) {
//        showLeftActionBtn(true);
//        showMiddleActionBtn(true);
//        showRightActionBtn(true);
//        overrideLeftActionBtn(R.drawable.actionbar_icon_search_normal, searchClickListener);
//        overrideMiddleActionBtn(R.drawable.actionbar_icon_release_normal , composeStreamListener);
//        if (forApp) {
//            overrideRightActionBtn(R.drawable.bottom_app, appStreamListener);
//        } else {
//            overrideRightActionBtn(R.drawable.bottom_camera, photoStreamListener);
//        }
        
    	overrideRightActionBtn(R.drawable.ic_menu_moreoverflow, editProfileClick);
    	
        View searchView = findViewById(R.id.toggle_search);
        if (null != searchView) {
        	searchView.setVisibility(View.VISIBLE);
        	searchView.setOnClickListener(searchClickListener);
        }

        View composingBtn = findViewById(R.id.toggle_composer);
        if (null != composingBtn) {
            composingBtn.setOnClickListener(composeStreamListener);
        }

        View photoBtn = findViewById(R.id.toggle_photo);
        if (null != photoBtn) {
            photoBtn.setOnClickListener(photoStreamListener);
        }
        
        View moreBtn = findViewById(R.id.toggle_more);
        if (null != moreBtn) {
        	moreBtn.setOnClickListener(showMoreActionListener);
        }
    }

    
    private View.OnClickListener showMoreActionListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(mMoreDialog == null) {
				mMoreDialog = new BottomMoreQuickAction(BpcPostsNewActivity.this, v.getWidth(), null);
				mMoreDialog.show(v);
			}else {
				mMoreDialog.show(v);
			}
		}
	};
	
    private View.OnClickListener appStreamListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            QiupuComposeActivity.startPickingAppsIntent(v.getContext());
        }
    };

    private View.OnClickListener photoStreamListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DialogUtils.ShowPhotoPickDialog(BpcPostsNewActivity.this, R.string.share_photo_title, new DialogUtils.PhotoPickInterface() {
                @Override
                public void doTakePhotoCallback() {
                    IntentUtil.startTakingPhotoIntent(BpcPostsNewActivity.this);
                }

                @Override
                public void doPickPhotoFromGalleryCallback() {
                    IntentUtil.startPickingPhotoIntent(BpcPostsNewActivity.this);
                }
            });
        }
    };

    public void onStart() {
        // start tracing to “/sdcard/stream.trace”
        if (isLowPerformance) Debug.startMethodTracing("stream");
        super.onStart();
        // other start up code here…
    }

    public void onStop() {
        super.onStop();
        // other shutdown code here
        if (isLowPerformance) Debug.stopMethodTracing();
    }

    View.OnClickListener editProfileClick = new View.OnClickListener() {
        public void onClick(View v) {
            ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
            items.add(new SelectionItem("", getString(R.string.home_steam)));
            Cursor pOrgazitaionCircles = orm.queryOrganizationWithImage();
            if(pOrgazitaionCircles != null) {
            	if(pOrgazitaionCircles.getCount() > 0) {
            		if(pOrgazitaionCircles.moveToFirst()) {
            			do {
            				items.add(new SelectionItem(String.valueOf(pOrgazitaionCircles.getLong(pOrgazitaionCircles.getColumnIndex(CircleColumns.CIRCLE_ID))), 
            						pOrgazitaionCircles.getString(pOrgazitaionCircles.getColumnIndex(CircleColumns.CIRCLE_NAME))));
						} while (pOrgazitaionCircles.moveToNext());
            		}
            	}
            	pOrgazitaionCircles.close();
            	pOrgazitaionCircles = null;
            }else {
            	Log.d(TAG, "have not orgazitaion Circles");
            }
//            items.add(new SelectionItem("", getString(R.string.menu_title_search)));
//            items.add(new SelectionItem("", getString(R.string.label_refresh)));
//            items.add(new SelectionItem("", getString(R.string.wutong_home_picker_header)));
            showCorpusSelectionDialog(items);
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
                onCorpusSelected(item.getItemId());             
            }
        }
    };
    
    private void onCorpusSelected(String value) {
    	if(getString(R.string.home_steam).equals(value)) {
    		if(QiupuConfig.LOGD)Log.d(TAG, "onCorpusSelected: it is current stream page, do nothing");
    	}else {
    		try {
    			if(value != null && TextUtils.isDigitsOnly(value)) {
    				long circleid = Long.parseLong(value);
    				QiupuORM.addSetting(this, QiupuORM.HOME_ACTIVITY_ID, value);
    				UserCircle uc = orm.queryOneCircle(QiupuConfig.USER_ID_ALL, circleid);
                    QiupuApplication.mTopOrganizationId = uc;
                    IntentUtil.gotoOrganisationHome(this, uc.name, circleid);
                    IntentUtil.loadCircleDirectoryFromServer(this, circleid);
                    finish();
    			}
    		} catch (Exception e) {
    		}
    	}
//        if(getString(R.string.label_refresh).equals(value)) {
//            loadRefresh();
//        }else if(getString(R.string.wutong_home_picker_header).equals(value)) {
//        	HomePickerActivity.registerPickerListener(getClass().getName(), BpcPostsNewActivity.this);
//            IntentUtil.gotoHomePickerActivity(BpcPostsNewActivity.this);
//        }else if(getString(R.string.menu_title_search).equals(value)) {
//        	loadSearch();
//        }
    }

    @Override
    public boolean onCancelled() {
        HomePickerActivity.unregisterPickerListener(getClass().getName());
        return true;
    }

    @Override
    public boolean onPicked(UserCircle circle) {
        HomePickerActivity.unregisterPickerListener(getClass().getName());
        finish();
        return true;
    }
    
    View.OnClickListener searchClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        	loadSearch();
        }
    };
    
//    @Override
//    public boolean onQueryTextSubmit(String query) {
//    	Log.d(TAG, "IntentUtil onQueryTextSubmit: " + query);
//    	if(query != null && query.length() > 0) {
//    		IntentUtil.startSearchActivity(this, query, BpcSearchActivity.SEARCH_TYPE_STREAM);
//		}else {
//			Log.d(TAG, "onQueryTextSubmit, query is null " );
//			ToastUtil.showShortToast(this, mHandler, R.string.search_recommend);
//		}
//    	return super.onQueryTextSubmit(query);
//    }
}
