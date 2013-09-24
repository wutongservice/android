package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import twitter4j.PollInfo;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.SelectionItem;
import com.borqs.common.adapter.PollListAdapter;
import com.borqs.common.adapter.PollListAdapter.LoaderMoreListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.common.view.PollItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.PollDetailFragment;
import com.borqs.qiupu.ui.BasicNavigationActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class PollListActivity extends BasicNavigationActivity implements LoaderMoreListener{

    private final static String TAG = "PollListActivity";

    private ListView            mListView;
    private TextView            mToastTextView;
    private PollListAdapter     mPollAdapter;
    private boolean mForceRefresh;
//    private int page = 0;
    private final int PAGE_COUNT = 20;
    private ArrayList<PollInfo> mPollList = new ArrayList<PollInfo>();
//    private static final int TYPE_PUBLIC = 1;
//    private static final int TYPE_BY_ME = 2;
//    private static final int TYPE_INVIDED_ME = 3;
    private int mCurrentScreen = PollInfo.TYPE_INVITED_ME;
    private boolean showMoreButton = false;
    private long mUserId;
    private String mUserName;
    private boolean isFromCircle = false;
    public static final String EXTRA_USER_NAME_KEY = "user_name";
    public static final String EXTRA_USER_ID_KEY = "user_id";
    public static final String EXTRA_CURRENT_SCREEN_KEY = "current_screen";
    public static final String EXTRA_FROM_CIRCLE = "from_circel_poll";
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserId = getIntent().getLongExtra(EXTRA_USER_ID_KEY, 0);
        mUserName = getIntent().getStringExtra(EXTRA_USER_NAME_KEY);
        isFromCircle = getIntent().getBooleanExtra(EXTRA_FROM_CIRCLE, false);
        if (mUserId == 0) {
            enableLeftNav();
            setHeadTitle(getTitleRes());
        } else {
        	if(StringUtil.isValidString(mUserName)) {
        		setHeadTitle(String.format(getString(R.string.whose_poll), mUserName));        		
        	}else {
        		setHeadTitle(R.string.poll);
        	}
        }
        setContentView(R.layout.poll_list_main);

        mCurrentScreen = getIntent().getIntExtra(EXTRA_CURRENT_SCREEN_KEY, PollInfo.TYPE_INVITED_ME);
        if (mUserId == 0) {
            showTitleSpinnerIcon(true);
        }
//        showLeftActionBtn(true);
        overrideRightActionBtn(R.drawable.ic_menu_moreoverflow, editProfileClick);

        mListView = (ListView) findViewById(R.id.default_listview);
        mToastTextView = (TextView) findViewById(R.id.toast_tv);
        if (mUserId != 0) {
            mPollList = QiupuORM.queryCirclePollList(this, String.valueOf(mUserId));
        } else {
            mPollList = QiupuORM.queryPollListInfo(this,mCurrentScreen);
        }
        mPollAdapter = new PollListAdapter(this, mPollList,false);
        mListView.setSelector(R.drawable.list_selector_background);
        mListView.setAdapter(mPollAdapter);
        mListView.setOnItemClickListener(mItemClickListener);
        
        mSpinner = (Spinner) findViewById(R.id.poll_category_spinner);
        buildPollCategoryList();

        loadRefresh();
    }

    private boolean isActivity = true;

    private View.OnClickListener createPollListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isActivity) {
                showActivity();
            } else {
                showDialog();
            }
        }
    };

    private void showActivity() {
        Intent intent = new Intent();
        intent.setClassName(this, PollCreateActivity.class.getName());
        HashMap<String, String> receiverMap = new HashMap<String, String>();
        receiverMap.put(String.valueOf(mUserId), mUserName);
    	intent.putExtra(PollCreateActivity.RECEIVER_MAP_KEY, receiverMap);
        if(mUserId > 0 && (QiupuConfig.isEventIds(mUserId) || QiupuConfig.isPublicCircleProfile(mUserId))) {
        	intent.putExtra(PollCreateActivity.RECEIVER_STR_KEY, "#" + mUserId);
        }else if(mUserId > 0 && QiupuConfig.isPageId(mUserId)) {
        	intent.putExtra(PollCreateActivity.RECEIVER_STR_KEY, String.valueOf(mUserId));
        }
        final String homeid = QiupuORM.getSettingValue(this, QiupuORM.HOME_ACTIVITY_ID);
    	long homeScene = TextUtils.isEmpty(homeid) ? -1 : Long.parseLong(homeid);
        intent.putExtra(CircleUtils.INTENT_SCENE, homeScene);
        startActivityForResult(intent, CREATE_POLL_CODE);
    }

    private LinearLayout container;
    private void showDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.poll_detail_main, null);
        view.setBackgroundResource(R.color.white);


        DialogUtils.showDialogWithOnlyView(this, view);
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            if (PollItemView.class.isInstance(view)) {
                PollItemView itemView = (PollItemView) view;
                IntentUtil.startPollDetailActivity(PollListActivity.this, itemView.getPollInfo(),REQ_CODE);
            } else {
                Log.d(TAG, "mItemClickListener error, view = " + view);
            }
        }
        
    };

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
    protected void loadRefresh() {
        mForceRefresh = true;
        mHandler.obtainMessage(SYNC_POLL).sendToTarget();
    }

    private final int SYNC_POLL     = 101;
    private final int SYNC_POLL_END = 102;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SYNC_POLL: {
//                    int tmpPage = page;
//                    if(mForceRefresh) {
//                        tmpPage = 0;
//                    }else {
//                        tmpPage++;
//                    }
                    if(mCurrentScreen == PollInfo.TYPE_PUBLIC) {
                        getPublicPollList(mCurrentScreen,getCurrentPage());
                    }else if(mCurrentScreen == PollInfo.TYPE_INVITED_ME) {
                        getUserPollList(mCurrentScreen,2, getCurrentPage());
                    }else {
                        getUserPollList(mCurrentScreen,0, getCurrentPage());
                    }
                    break;
                }
                case SYNC_POLL_END: {
//                    end();
                    mPollAdapter.refreshLoadingStatus();
                    if(mForceRefresh) {
//                        page = 0;
                        mForceRefresh = false;
                    }
                    if (msg.getData().getBoolean(RESULT)) {
//                        ArrayList<PollInfo> pollList = (ArrayList<PollInfo>) msg.getData().getSerializable("poll_list");
//                        page++;
                        refreshUI();
                    } else {
                        ToastUtil.showOperationFailed(PollListActivity.this, mHandler, false);
                    }
                    break;
                }
            }
        }
    }

    private void refreshUI() {
        refreshLoadProcessBarUI();
        mPollAdapter.alterPollList(mPollList,showMoreButton);
    }

    @Override
    protected void onDestroy() {
        if(LockSyncMap != null) {
            LockSyncMap.clear();
        }

        if (isFromCircle) {
            insertCirclePollToDb(mPollList);
        } else {
            insertPollToDb(mPollList);
        }

        super.onDestroy();
    }
    
    private void insertPollToDb(ArrayList<PollInfo> datalist) {
        final int type = mCurrentScreen;
        insertPollToDb(type,datalist);
    }
    private void insertPollToDb(final int type,ArrayList<PollInfo> datalist) {
        if(datalist == null) return;
        int size = datalist.size();
        if (size > 0) {
            Log.v("poll","insertPollToDb--------type="+type+"-------------size="+size);
            if(size > PAGE_COUNT) {
                size = PAGE_COUNT;
            }
            final ArrayList<PollInfo> cachePollList = new ArrayList<PollInfo>();
            for(int i=0;i<size;i++) {
                cachePollList.add(datalist.get(i));
            }
            datalist.clear();
            datalist = null;
            QiupuORM.sWorker.post(new Runnable() {
                
                @Override
                public void run() {
                    Log.v("poll","insertPollToDb--------type="+type+"-------------cachePollListsize="+cachePollList.size());
                    orm.insertPollList(cachePollList,type);
                    cachePollList.clear();
                }
            });
        }
    }

    private void insertCirclePollToDb(ArrayList<PollInfo> datalist) {
        if(datalist == null) return;
        int size = datalist.size();
        if (size > 0) {
            Log.d(TAG, "insertCirclePollToDb -------------size = " + size);
            if(size > PAGE_COUNT) {
                size = PAGE_COUNT;
            }
            final ArrayList<PollInfo> cachePollList = new ArrayList<PollInfo>();
            for(int i = 0; i < size; i++) {
                cachePollList.add(datalist.get(i));
            }
            datalist.clear();
            datalist = null;
            QiupuORM.sWorker.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "insertCirclePollToDb---------cachePollListsize = " + cachePollList.size());
                    orm.insertCirclePollList(cachePollList);
                    cachePollList.clear();
                }
            });
        }
    }

    private void deletePollById(final String poll_id) {
        QiupuORM.sWorker.post(new Runnable() {
            
            @Override
            public void run() {
                orm.deletePollnfo(poll_id);
            }
        });
    }

//    private boolean inLoadingPoll;
//    private Object  mLockSyncPollInfo = new Object();
    
    class LockData {
        private boolean inLoading;
        private Object mLockSyncInfo = new Object();
    }
    private Map<String, LockData> LockSyncMap= new HashMap<String, LockData>();

    public void getPublicPollList(final int currentType,final int page) {
        if (!ToastUtil.testValidConnectivity(this)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }
        
        if(!setLoadingStatus(true, currentType,false)) return;
        
        
//        if (inLoadingPoll == true) {
//            ToastUtil.showShortToast(this, mHandler,
//                    R.string.string_in_processing);
//            return;
//        }
//        
//        synchronized (mLockSyncPollInfo) {
//            inLoadingPoll = true;
//        }
        mPollAdapter.refreshLoadingStatus();
//        begin();
        
        asyncQiupu.getPublicPollList(AccountServiceUtils.getSessionID(), page, PAGE_COUNT, new TwitterAdapter() {
            public void getPublicPollList(ArrayList<PollInfo> pollList) {
                Log.d(TAG, "finish pollList.size() =" + pollList.size() + "and the type = "+currentType + "and the current type="+mCurrentScreen);
                if(mCurrentScreen == currentType) {
                    showMoreButton(pollList);
                    if(mPollList == null) {
                        return ;
                    }
                    
                    if(mForceRefresh || page == 0) {
                        mPollList.clear();
                    }
                    mPollList.addAll(pollList);
                    
                    Message msg = mHandler.obtainMessage(SYNC_POLL_END);
                    msg.getData().putBoolean(RESULT, true);
//                        msg.getData().putSerializable("poll_list", pollList);
                    msg.sendToTarget();
//                synchronized (mLockSyncPollInfo) {
//                    inLoadingPoll = false;
//                }
                }else {
                    if(page == 0) {
                        insertPollToDb(currentType,pollList);
                    }
                    
                }
                setLoadingStatus(false, currentType,false);
            }
            
            public void onException(TwitterException ex,
                    TwitterMethod method) {
//                synchronized (mLockSyncPollInfo) {
//                    inLoadingPoll = false;
//                }
                if(mCurrentScreen == currentType) {
                    Message msg = mHandler.obtainMessage(SYNC_POLL_END);
                    msg.getData().putBoolean(RESULT, false);
                    msg.sendToTarget();
                }
                setLoadingStatus(false, currentType,true);
            }
        });
    }
    
    private boolean setLoadingStatus(boolean isLoad,int type,boolean isRemove) {
        LockData lockData =  LockSyncMap.get(String.valueOf(type));
        if(isLoad) {
            if(lockData != null) {
                if (lockData.inLoading == true) {
                    if(mCurrentScreen == type) {
                        begin();
                    ToastUtil.showShortToast(this, mHandler,
                            R.string.string_in_processing);
                    }
                    return false;
                } 
            }else {
                lockData = new LockData();
                LockSyncMap.put(String.valueOf(mCurrentScreen),new LockData());
            }
            synchronized (lockData.mLockSyncInfo) {
                lockData.inLoading = true;
            }
            if(mCurrentScreen == type) {
                begin();
            }
        }else {
            if(mCurrentScreen == type) {
                end();
            }
            if(lockData != null) {
                synchronized (lockData.mLockSyncInfo) {
                    lockData.inLoading = false;
                }
                if(isRemove) {
                    lockData = null;
                    LockSyncMap.remove(String.valueOf(type));
                }
            }
            
        }
        return true;
    }
    
    private void showMoreButton(ArrayList<PollInfo> pollList) {
        LockData lockData =  LockSyncMap.get(String.valueOf(mCurrentScreen));
        if(lockData != null) {
            if (pollList.size() < PAGE_COUNT) {
                showMoreButton = false;
            }else {
                showMoreButton = true;
            }
        }else {
            showMoreButton = false;
        }
    }
    public void getUserPollList(final int currentType,final int type, final int page) {
        if (!ToastUtil.testValidConnectivity(this)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }
        if(!setLoadingStatus(true, currentType,false)) return;
//        if (inLoadingPoll == true) {
//            ToastUtil.showShortToast(this, mHandler,
//                    R.string.string_in_processing);
//            return;
//        }
//
//        synchronized (mLockSyncPollInfo) {
//            inLoadingPoll = true;
//        }
        mPollAdapter.refreshLoadingStatus();
//        begin();

        asyncQiupu.getUserPollList(AccountServiceUtils.getSessionID(),type, page, PAGE_COUNT, mUserId, new TwitterAdapter() {
            @Override
            public void getUserPollList(ArrayList<PollInfo> pollList) {
//                setLoadingStatus(false,type);
                Log.d(TAG, "finish pollList.size() =" + pollList.size() + "and the type = "+currentType + "and the current type="+mCurrentScreen);
                if(mCurrentScreen == currentType) {
                    showMoreButton(pollList);
                    if(mPollList == null) {
                        return ;
                    }
                    if(mForceRefresh || page == 0) {
                        mPollList.clear();
                    }
                    mPollList.addAll(pollList);

                    Message msg = mHandler.obtainMessage(SYNC_POLL_END);
                    msg.getData().putBoolean(RESULT, true);
//                        msg.getData().putSerializable("poll_list", pollList);
                    msg.sendToTarget();
//                synchronized (mLockSyncPollInfo) {
//                    inLoadingPoll = false;
//                }
                }else {
                    if(page == 0) {
                        insertPollToDb(currentType,pollList);
                    }
                }
                setLoadingStatus(false, currentType,false);
            }

            public void onException(TwitterException ex,
                    TwitterMethod method) {
//                synchronized (mLockSyncPollInfo) {
//                    inLoadingPoll = false;
//                }
                if(mCurrentScreen == currentType) {
                    Message msg = mHandler.obtainMessage(SYNC_POLL_END);
                    msg.getData().putBoolean(RESULT, false);
                    msg.sendToTarget();
                }
                setLoadingStatus(false, currentType,true);
            }
        });
    }

//    @Override
//    public void setLeftMenuPosition() {
//        mPosition = LeftMenuMapping.getPositionForActivity(this);
//        mTitle = getString(R.string.poll);
//    }

    @Override
    public int getCaptionResourceId() {
        LockData lockData =  LockSyncMap.get(String.valueOf(mCurrentScreen));
        if(lockData != null && lockData.inLoading) {
            return R.string.loading;
        }
        return R.string.list_view_more;
    }

    @Override
    public OnClickListener loaderMoreClickListener() {
        View.OnClickListener clickListener = new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mHandler.obtainMessage(SYNC_POLL).sendToTarget();
                
            }
        };
        return clickListener;
    }
    
    
    @Override
    protected void showCorpusSelectionDialog(View view) {
        int location[] = new int[2];
        view.getLocationInWindow(location);
        int x = location[0];
        int y = getResources().getDimensionPixelSize(R.dimen.title_bar_height);

        ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
        items.add(new SelectionItem("", getString(R.string.public_poll)));
        items.add(new SelectionItem("", getString(R.string.invited_me_poll)));
        items.add(new SelectionItem("", getString(R.string.poll_by_me)));
        DialogUtils.showCorpusSelectionDialog(this, x, y, items, circleListItemClickListener);
    }
    
    AdapterView.OnItemClickListener circleListItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (CorpusSelectionItemView.class.isInstance(view)) {
                CorpusSelectionItemView item = (CorpusSelectionItemView) view;
                setHeadTitle(item.getText());
                onCorpusSelected(item.getText());
            }
        }
    };
    
    private void onCorpusSelected(String value) {
        if(mCurrentScreen != PollInfo.TYPE_PUBLIC && getString(R.string.public_poll).equals(value)) {
            setHeadTitle(R.string.public_poll);
            insertPollToDb(mPollList);
            Log.v("poll","---mCurrentScreen="+mCurrentScreen+"-----------type="+PollInfo.TYPE_PUBLIC+"------------------");
            mCurrentScreen = PollInfo.TYPE_PUBLIC;
        }else if(mCurrentScreen != PollInfo.TYPE_INVITED_ME && getString(R.string.invited_me_poll).equals(value)) {
            setHeadTitle(R.string.invited_me_poll);
            insertPollToDb(mPollList);
            Log.v("poll","---mCurrentScreen="+mCurrentScreen+"-----------type="+PollInfo.TYPE_INVITED_ME+"------------------");
            mCurrentScreen = PollInfo.TYPE_INVITED_ME;
        }else if(mCurrentScreen != PollInfo.TYPE_I_CREATED && getString(R.string.poll_by_me).equals(value)) {
            setHeadTitle(R.string.poll_by_me);
            insertPollToDb(mPollList);
            Log.v("poll","---mCurrentScreen="+mCurrentScreen+"-----------type="+PollInfo.TYPE_I_CREATED+"------------------");
            mCurrentScreen = PollInfo.TYPE_I_CREATED;
        }else if(getString(R.string.create_poll).equals(value)) {
        	if (isActivity) {
                showActivity();
            } else {
                showDialog();
            }
    	}else if(getString(R.string.label_refresh).equals(value)) {
    		loadRefresh();
    	}else {
            Log.d(TAG, "more drop down items " + value);
            return;
        }
        
        mPollList = QiupuORM.queryPollListInfo(this,mCurrentScreen);
        showMoreButton = false;
        if(isneedLoadData(mCurrentScreen)) {
            Log.v("poll", "need load............");
            refreshUI();
            loadRefresh();
        }else {
            Log.v("poll", "don't need load............");
            showMoreButton(mPollList);
            refreshUI();
        }
    }
    
    private boolean isneedLoadData(int type) {
        LockData lockData =  LockSyncMap.get(String.valueOf(type));
        if(lockData != null) {
            return false;
        }else {
            return true;
        }
        
    }
    
    private void refreshLoadProcessBarUI() {
        LockData lockData =  LockSyncMap.get(String.valueOf(mCurrentScreen));
        if(lockData != null && lockData.inLoading) {
            begin();
            return;
        }
        end();
    }
    
    private int getTitleRes() {
        if(mCurrentScreen == PollInfo.TYPE_PUBLIC) {
            return R.string.public_poll;
        }else if(mCurrentScreen == PollInfo.TYPE_INVITED_ME) {
            return R.string.invited_me_poll;
        }else {
            return R.string.poll_by_me;
        }
        
    }
    
    private int getCurrentPage() {
        if(mPollList != null) {
            if(mForceRefresh) {
                return 0;
            }else {
                return  mPollList.size() / PAGE_COUNT;
            }
        }else {
            return 0;
        }
    }
    
    private final int REQ_CODE = 0;
    public static final int CREATE_POLL_CODE = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult resultCode = " + resultCode + ", requestCode = " + requestCode);
        if(resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQ_CODE:
                String pid  = data.getStringExtra(PollDetailFragment.POLL_ID_KEY);
                deletePollById(pid);
                if(mPollList != null && mPollList.size() >0) {
                    for(PollInfo p:mPollList) {
                        if(p.poll_id.equals(pid)) {
                            mPollList.remove(p);
                            refreshUI();
                            break;
                        }
                    }
                }
                break;
            case CREATE_POLL_CODE: {
//                mHandler.obtainMessage(SYNC_POLL).sendToTarget();
                PollInfo pollInfo = (PollInfo) data.getSerializableExtra(PollCreateActivity.POLL_OUT_KEY);
                Log.d(TAG, "onActivityResult() pollInfo = " + pollInfo);
                if (pollInfo != null) {
                    mPollList.add(pollInfo);
                    refreshUI();
                }
            }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    
    View.OnClickListener editProfileClick = new View.OnClickListener() {
        public void onClick(View v) {
        	ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
        	items.add(new SelectionItem("", getString(R.string.label_refresh)));
        	items.add(new SelectionItem("", getString(R.string.create_poll)));
        	
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
                onCorpusSelected(item.getText());             
            }
        }
    };
    
    private void buildPollCategoryList() {
    	final String[] adapterValue = new String[]{getString(R.string.public_poll), getString(R.string.invited_me_poll), getString(R.string.poll_by_me)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.event_spinner_textview, adapterValue);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setSelection(0);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View view,
        			int position, long id) {
        		onCorpusSelected(adapterValue[position]);
        	}
        	
        	@Override
        	public void onNothingSelected(AdapterView<?> parent) {
        	}
        });
    }
}
