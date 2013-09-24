package com.borqs.qiupu.fragment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import twitter4j.AsyncQiupu;
import twitter4j.QiupuUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.PickCircleUserAdapter.MoreItemCheckListener;
import com.borqs.common.adapter.PickContactAdapter;
import com.borqs.common.view.ContactUserSelectItemView;
import com.borqs.qiupu.AddressPadMini;
import com.borqs.qiupu.AddressPadMini.AddressPadNoteActionListener;
import com.borqs.qiupu.AddressPadMini.PhoneNumberEmailDecorater;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class PickAudienceBaseFragment extends PeopleSearchableFragment implements MoreItemCheckListener, AddressPadNoteActionListener {
    private static final String TAG = "PickAudienceBaseFragment";

    protected Activity mActivity;
    protected ListView mListView;
    protected PickContactAdapter mAdapter;
    protected Handler mHandler;
    private AsyncQiupu asyncQiupu;
    protected long mCircleId;
    protected String mCircleName;
    private ArrayList<QiupuUser> mUserList = new ArrayList<QiupuUser>();

    private static final String RESULT = "result";
    private static final String ERRORMSG = "errormsg";
    private int mPage = 0;
    private static final int mCount = 20;
    public static final int PICK_TYPE_PHONE = 1;
    public static final int PICK_TYPE_EMAIL = 2;

    private boolean isUserShowMore;
    protected AddressPadMini mSelectAddress;
    private View mAddressSPan;
    private PickAudienceBaseFragmentCallBack mCallBackListener;
    protected HashMap<Long, String> mSelectedUserCircleNameMap = new HashMap<Long, String>();
    protected HashMap<String, String> mSelectePhoneEmailNamedmap = new HashMap<String, String>();
    private StringBuilder mBackSelectNames = new StringBuilder();
    
    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach");
        super.onAttach(activity);
        mActivity = activity;
        try{
			mCallBackListener = (PickAudienceBaseFragmentCallBack)activity;
			mCallBackListener.getPickAduienceBaseFragment(this);
		}catch (ClassCastException e) {
			Log.d(TAG, activity.toString() +  "must implement CallBackLocalUserListFragmentListener");
		}
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        orm = QiupuORM.getInstance(mActivity);
        mHandler = new MainHandler();
        asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(), null, null);
        Intent intent = mActivity.getIntent();
        mCircleId = intent.getLongExtra(CircleUtils.CIRCLE_ID, 0);
        mCircleName = intent.getStringExtra(CircleUtils.CIRCLE_NAME);
        mSelectedUserCircleNameMap = mCallBackListener.getSelectUserCircleNameMap();
        mSelectePhoneEmailNamedmap = mCallBackListener.getSelectPhoneEmailNameMap();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        mListView = (ListView) inflater.inflate(R.layout.default_listview,
                container, false);

        mListView.addHeaderView(initHeadView());
        mListView.addHeaderView(initHeadAddressView(inflater));
        mListView.setOnItemClickListener(contactitemClickListener);
        mAdapter = new PickContactAdapter(mActivity, false);
        mListView.setAdapter(mAdapter);
        return mListView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }
    
    public void onDestroy() {
        super.onDestroy();
    }

    private static final int LOAD_SEARCH_END = 101;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_SEARCH_END: {
                    end();
                    setMoreItem(false);
                    if (msg.getData().getBoolean(RESULT)) {
                        showSearchFromServerButton(false, mSearchKey, null);
                        doSearchEndCallBack(mUserList);
                        if (mUserList.size() <= 0) {
                            ToastUtil.showShortToast(mActivity, mHandler, R.string.search_people_result_null);
                        }
                    } else {
                        ToastUtil.showOperationFailed(mActivity, mHandler, true);
                    }
                    break;
                }
            }
        }
    }

    private AdapterView.OnItemClickListener contactitemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
            if (ContactUserSelectItemView.class.isInstance(view)) {
                ContactUserSelectItemView uv = (ContactUserSelectItemView) view;
                uv.switchCheck();
            }
        }
    };

    public String getSelectValue(HashSet<String> selectValue) {
        Iterator<String> it = selectValue.iterator();
        StringBuilder ids = new StringBuilder();

        while (it.hasNext()) {
            if (ids.length() > 0) {
                ids.append(",");
            }
            ids.append("*" + it.next());
        }
        return ids.toString();
    }

    public String getSelectName(HashMap<String, String> selectMap) {
        StringBuilder tmpString = new StringBuilder();
        Iterator iter = selectMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String val = (String) entry.getValue();
            if (tmpString.length() > 0) {
                tmpString.append(",");
            }
            tmpString.append(val);
        }
        return tmpString.toString();
    }

    protected void doSearch(String key) {
    }

    public interface PickAudienceBaseFragmentCallBack {
        public void getPickAduienceBaseFragment(PickAudienceBaseFragment fragment);
        public HashMap<String, String> getSelectPhoneEmailNameMap();
        public HashMap<Long, String> getSelectUserCircleNameMap();
    }

    @Override
    protected void showSearchFromServerButton(boolean show, final String key,
                                              OnClickListener callback) {
        super.showSearchFromServerButton(show, key, new View.OnClickListener() {
            public void onClick(View v) {
                mPage = 0;
                isNeedRefreshUi = true;
                searchFriends(key, mPage);
            }
        });
    }

    private void searchFriends(String str, int page) {
        if (QiupuConfig.LOGD) Log.d(TAG, "searchFriends");

        if (str == null || str.equals("")) {
            callFailLoadUserMethod();
            if (QiupuConfig.LOGD) Log.d(TAG, "input string is null");
            return;
        }
        str = str.trim().toLowerCase();

        begin();
        setMoreItem(true);
        asyncQiupu.getUserListWithSearchName(AccountServiceUtils.getSessionID(), str, str, str, page, mCount, new TwitterAdapter() {
            public void getUserListWithSearchName(ArrayList<QiupuUser> users) {
                Log.d(TAG, "finish search user : " + users.size());

                if (users != null) {
                    doSearchUserCallBack(true, users);
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                doSearchUserCallBack(false, null);
                callFailLoadUserMethod();
            }
        });
    }

    protected void doSearchUserCallBack(boolean result, ArrayList<QiupuUser> userList) {
        if (result) {
            if (mPage == 0) {
                mUserList.clear();
            }
            mUserList.addAll(userList);
            if (userList.size() <= 0) {
                isUserShowMore = false;
            } else {
                isUserShowMore = true;
            }
        }
        Message msg = mHandler.obtainMessage(LOAD_SEARCH_END);
        msg.getData().putBoolean(RESULT, result);
        msg.sendToTarget();
    }

    protected void doSearchEndCallBack(ArrayList<QiupuUser> userList) {
    }

    public interface invitePepoleListeners {
        public void updateUi();
    }

    @Override
    public boolean isMoreItemHidden() {
        return isUserShowMore;
    }

    @Override
    public OnClickListener getMoreItemClickListener() {
        return loadOlderClick;
    }

    @Override
    public int getMoreItemCaptionId() {
        return R.string.list_view_more;
    }

    public View.OnClickListener loadOlderClick = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "load more user");
            getOldData();
        }
    };
    
    private void getOldData() {
        mPage += 1;
        Log.i(TAG, "Page:" + mPage);
        try {
            failCallSyncUserMethod = PickAudienceBaseFragment.class
                    .getDeclaredMethod("SubUserPage", (Class[]) null);
        } catch (Exception e) {
        }
        searchFriends(mSearchKey, mPage);
    }

    protected Method failCallSyncUserMethod;

    protected void SubUserPage() {
        Log.d(TAG, "resore the dpage--");
        mPage--;
        if (mPage < 0)
            mPage = 0;
    }

    protected void callFailLoadUserMethod() {
        try {
            if (failCallSyncUserMethod != null) {
                failCallSyncUserMethod.invoke(this, (Object[]) null);
            }
        } catch (Exception ne) {
        }
    }

    private void setMoreItem(boolean loading) {
        //set load older button text process for UI
        if (mListView != null) {
            for (int i = mListView.getChildCount() - 1; i > 0; i--) {
                View v = mListView.getChildAt(i);
                if (Button.class.isInstance(v)) {
                    Button bt = (Button) v;
                    if (loading) {
                        bt.setText(R.string.loading);
                    } else {
                        bt.setText(R.string.list_view_more);
                    }
                    break;
                }
            }
        }
    }
    
    protected View initHeadAddressView(LayoutInflater inflater) {
		View headview = inflater.inflate(R.layout.pick_user_headaddress, null, false);
		mSelectAddress = (AddressPadMini) headview.findViewById(R.id.receiver_editor);
		mSelectAddress.setEnabled(false);
		mSelectAddress.mNeedClick = true;
		mSelectAddress.setOnDecorateAddressListener(new FBUDecorater());
		mAddressSPan = headview.findViewById(R.id.address_span);
		return headview;
	}
    
    private class FBUDecorater implements
    AddressPadMini.OnDecorateAddressListener {
    	
    	public String onDecorate(String address) {
    		String suid = address.trim();
    		try {
    			if (suid.contains("#")) {
    				int index = suid.indexOf("#");
    				suid = suid.substring(index + 1, suid.length());
    				UserCircle uc = orm.queryOneCircle(QiupuConfig.USER_ID_ALL, Long.valueOf(suid));
    				String circleName = null ;
    				if(uc != null) {
    					circleName = CircleUtils.getCircleName(mActivity, uc.circleid, uc.name);
    				}
    				
    				if(TextUtils.isEmpty(circleName)) {
						circleName = mSelectedUserCircleNameMap.get(Long.valueOf(suid));
					}
    					
    				if(mBackSelectNames.length() > 0) {
    					mBackSelectNames.append(",");
    				}
    				mBackSelectNames.append(circleName);
    				return circleName;
    			} else if (suid.contains("*")) {
    				int index = suid.indexOf("*");
    				suid = suid.substring(index + 1, suid.length());
    				PhoneNumberEmailDecorater number = new AddressPadMini(
    						mActivity).new PhoneNumberEmailDecorater();
    				number.setNameString(mBackSelectNames);
    				return number.onDecorate(suid);
    			} else {
    				String username = orm.queryUserName(Long.valueOf(suid));
                	if (username == null) {
                		username = mSelectedUserCircleNameMap.get(Long.parseLong(suid)); 
                    }
                	
                	if(mBackSelectNames.length() > 0) {
                		mBackSelectNames.append(",");
                	}
                	mBackSelectNames.append(username);
    				return username;
    				
    			}
    		} catch (Exception ne) {
    		}
    		return address;
    	}
    }
    
    public static String mainSelectAdds = "";
    public static String contactPhoneSelectAdds = "";
    public static String contactEmailSelectAdds = "";
    public void setAddress() {
    	String adds = getAllSelectString();
		if(StringUtil.isValidString(adds)) {
			mSelectAddress.setVisibility(View.VISIBLE);
			mAddressSPan.setVisibility(View.VISIBLE);
			mSelectAddress.setAddresses(adds);
		}else {
			mSelectAddress.setVisibility(View.GONE);
			mAddressSPan.setVisibility(View.GONE);
		}
	}
    
    public String getAllSelectString() {
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "setAddress: " + mainSelectAdds + " " + contactEmailSelectAdds + " " + contactPhoneSelectAdds );
    	StringBuilder tmpAdd = new StringBuilder();
    	if(StringUtil.isValidString(mainSelectAdds)) {
    		tmpAdd.append(mainSelectAdds);
    	}
    	if(StringUtil.isValidString(contactPhoneSelectAdds)) {
    		if(tmpAdd.length() > 0) {
    			tmpAdd.append(",");
    		}
    		tmpAdd.append(contactPhoneSelectAdds);
    	}
    	if(StringUtil.isValidString(contactEmailSelectAdds)) {
    		if(tmpAdd.length() > 0) {
    			tmpAdd.append(",");
    		}
    		tmpAdd.append(contactEmailSelectAdds);
    	}
    	return tmpAdd.toString();
	}

    public void clearAddress() {
    	mainSelectAdds = "";
    	contactPhoneSelectAdds = "";
    	contactEmailSelectAdds = "";
    }

	@Override
	public void noteRemove(String notStr) {
	}
	
	public String getSelectNames() {
		return mBackSelectNames.toString();
	}
}
