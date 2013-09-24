package com.borqs.qiupu.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.borqs.common.ShareSourceItem;
import com.borqs.common.adapter.ShareSourceAdapter;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.service.ApkFileManager;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.service.ShareSourceResultReceiver;
import twitter4j.QiupuAccountInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ProfileShareSourceFragment extends BasicFragment implements ShareSourceResultReceiver.ShareSourceServiceListener {
	private final static String TAG = "ProfileShareSourceFragment";
    private Activity mActivity; 
    private ListView mListView;
    private ShareSourceAdapter mShareSourceAdapter;
    private ProfileShareSourceFragmentCallbackListener mCallBackListener;
    private QiupuAccountInfo mUser;
    private QiupuORM orm;
    private long mCircleId;
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	mActivity = activity;
    	orm = QiupuORM.getInstance(mActivity);
    	try{
    		mCallBackListener = (ProfileShareSourceFragmentCallbackListener)activity;
    		mCallBackListener.getProfileShareSourceFragment(this);
		}catch (ClassCastException e) {
			Log.d(TAG, activity.toString() +  "must implement CallBackProfileInfoFragmentListener");
		}
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if(mCallBackListener != null) {
    		mUser = mCallBackListener.getUserInfo();
    		mCircleId = mCallBackListener.getCircleId();
    	}else {
    		mUser = new QiupuAccountInfo();
    		Log.d(TAG, "get user is null from activity");
    	}
    	
    	ShareSourceResultReceiver.registerServiceListener(getClass().getName(), this);
    	if(mCircleId <= 0) {
    		QiupuService.sendShareSourceBroadcast(mActivity, mUser.uid);
    	}
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	mListView = (ListView) inflater.inflate(R.layout.default_listview, container, false);
    	if(orm.isShowAlbumSettings()) {
    		setHeaderView(inflater);
    	}
    	mShareSourceAdapter = new ShareSourceAdapter(mActivity);
    	mListView.setAdapter(mShareSourceAdapter);
    	mListView.setOnItemClickListener(userOnClick);
		return mListView;
    }
    
    private void setHeaderView(LayoutInflater inflater) {
        View headerView = inflater.inflate(R.layout.share_source_head_view, null);
		headerView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
        mListView.addHeaderView(headerView);
        ImageView icon = (ImageView) headerView.findViewById(R.id.app_icon);
        TextView label = (TextView) headerView.findViewById(R.id.app_label);
        label.setText(R.string.home_album);
        icon.setImageResource(R.drawable.ic_menu_gallery);
        headerView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				IntentUtil.startAlbumIntent(mActivity, mUser.uid,mUser.nick_name);
			}
		});
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	// add share source from db
    	if(mCircleId > 0) { 
    		mUser.sharedResource = orm.queryShareSources(mCircleId);
    	}else {
    		mUser.sharedResource = orm.queryShareSources(mUser.uid); 
    	}
        refreshShareSourceResult();
    }
    
    @Override
	public void onResume() {		
		super.onResume();
		
		// add share source from db
        mUser.sharedResource = orm.queryShareSources(mUser.uid); 
        refreshShareSourceResult();
	}

	@Override
    public void onDestroy() {
    	super.onDestroy();
    	ShareSourceResultReceiver.unregisterServiceListener(getClass().getName());
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
			if (keys.isEmpty()) {
				Log.d(TAG, "refreshShareSourceResult, no any share source.");
				return;
			}
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
		
		mShareSourceAdapter.alterDataList(dataList);
	}

    AdapterView.OnItemClickListener userOnClick = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
            if (ShareSourceAdapter.ShareSourceItemView.class.isInstance(view)) {
                ShareSourceAdapter.ShareSourceItemView itemview = (ShareSourceAdapter.ShareSourceItemView) view;
                final ShareSourceItem item = itemview.getShareSourceItem();
                if (!TextUtils.isEmpty(item.mScheme)) {
                    final String scheme = item.mScheme;
                    final String schemeTxt = scheme + "?" +
                            BpcApiUtils.SEARCH_KEY_UID + "=" + mUser.uid +
                            "&" + BpcApiUtils.User.USER_NAME + "=" + mUser.nick_name;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(schemeTxt));
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    if (BpcApiUtils.isActivityReadyForIntent(mActivity.getApplicationContext(), intent)) {
//                        Bundle bundle = BpcApiUtils.getUserBundle(mUser.uid, mUser.nick_name, "");
//                        intent.putExtras(bundle);
                        startActivity(intent);
                    } else {
                        if (!TextUtils.isEmpty(item.mTarget)) {
                            // download the target app.
                            ApkFileManager.shootDownloadAppService(mActivity, item.mTarget, "Plugin App");
                        } else {
                            Log.e(TAG, "OnItemClickListener.onItemClick, no activity for intent:" + intent +
                                    ", or no share item count:" + item.mCount);
                        }
                    }
                } else {
                    IntentUtil.startShareSourceActivity(mActivity, mUser.uid, mCircleId, item.mType,
                            itemview.getSourceItemLabel(""));
                }
            }
        }
    };
	
	public interface ProfileShareSourceFragmentCallbackListener {
		public void getProfileShareSourceFragment(ProfileShareSourceFragment fragment);
		public QiupuAccountInfo getUserInfo();
		public long getCircleId();
	}

	public void updateSourceItem(long uid) {
		mUser.sharedResource = orm.queryShareSources(uid); 
        refreshShareSourceResult();
	}
}
