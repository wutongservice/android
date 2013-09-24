package com.borqs.qiupu.ui.company;

import java.util.ArrayList;

import com.borqs.qiupu.ui.BasicNavigationActivity;
import twitter4j.Company;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.CompanyAdapter;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CompanyItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.util.ToastUtil;

public class CompanyListActivity extends BasicNavigationActivity {

    private final static String TAG = "CompanyListActivity";

    private ListView            mListView;
    private CompanyAdapter     mAdapter;
    private ArrayList<Company> mCompanyList = new ArrayList<Company>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableLeftNav();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.company_list_main);
        setHeadTitle(R.string.company);
        showLeftActionBtn(true);
        showRightActionBtn(false);
        // overrideRightActionBtn(R.drawable.build_event_icon,
        // addEventListener);
        mCompanyList = orm.queryCompanyList(this);
        mListView = (ListView) findViewById(R.id.default_listview);
//        View headerView = LayoutInflater.from(this).inflate(R.layout.company_list_header, null);
//        mListView.addHeaderView(headerView);
        mAdapter = new CompanyAdapter(this, mCompanyList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mItemClickListener);

        loadRefresh();
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            if (CompanyItemView.class.isInstance(view)) {
            	CompanyItemView itemView = (CompanyItemView) view;
                IntentUtil.startCompanyDetailActivity(CompanyListActivity.this, itemView.getCompanyInfo());
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
        mHandler.obtainMessage(LOAD_DATA).sendToTarget();
    }

    private final int LOAD_DATA     = 101;
    private final int LOAD_DATA_END = 102;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_DATA: {
                    getBelongCompany();
                    break;
                }
                case LOAD_DATA_END: {
                    if (msg.getData().getBoolean(RESULT)) {
                        mAdapter.alterDataList(mCompanyList);
                    } else {
                        ToastUtil.showOperationFailed(CompanyListActivity.this, mHandler, false);
                    }
                    break;
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        insertCompanyToDb();
        mCompanyList.clear();
        super.onDestroy();
    }
    
    private void insertCompanyToDb() {
        if(mCompanyList == null) return;
        int size = mCompanyList.size();
        if (size > 0) {
            final ArrayList<Company> cachePollList = new ArrayList<Company>();
            for(int i=0;i<size;i++) {
                cachePollList.add(mCompanyList.get(i));
            }
            QiupuORM.sWorker.post(new Runnable() {
                
                @Override
                public void run() {
                    orm.insertCompanyList(cachePollList);
                    cachePollList.clear();
                }
            });
        }
    }

    
    private boolean inLoading;
    private Object mLockSyncInfo = new Object();
    
    private boolean setLoadingStatus(boolean isLoad) {
        if(isLoad) {
        	if (inLoading == true) {
        		begin();
        		ToastUtil.showShortToast(this, mHandler,
        				R.string.string_in_processing);
        		return false;
        	} 
            synchronized (mLockSyncInfo) {
                inLoading = true;
            }
            begin();
        }else {
        	end();
            synchronized (mLockSyncInfo) {
            	inLoading = false;
            }
        }
        return true;
    }
    
    public void getBelongCompany() {
        if (!ToastUtil.testValidConnectivity(this)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }
        if(!setLoadingStatus(true)) return;

        asyncQiupu.getBelongCompany(AccountServiceUtils.getSessionID(), new TwitterAdapter() {
        	
        	@Override
        	public void getBelongCompany(ArrayList<Company> companys) {
        		
        		orm.insertCompanyList(companys);

        		if(mCompanyList != null ) {
        			mCompanyList.clear();
        			mCompanyList = companys;
        		}
        		Message msg = mHandler.obtainMessage(LOAD_DATA_END);
        		msg.getData().putBoolean(RESULT, true);
        		msg.sendToTarget();
        		
        		setLoadingStatus(false);
        	}

            public void onException(TwitterException ex,
                    TwitterMethod method) {
            	
                Message msg = mHandler.obtainMessage(LOAD_DATA_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
                setLoadingStatus(false);
            }
        });
    }

//    @Override
//    public void setLeftMenuPosition() {
//        mPosition = LeftMenuMapping.getPositionForActivity(this);
//        mTitle = getString(R.string.company);
//    }

}