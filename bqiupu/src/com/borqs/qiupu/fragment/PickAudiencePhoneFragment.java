package com.borqs.qiupu.fragment;

import java.util.HashMap;
import java.util.HashSet;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.common.view.ContactUserSelectItemView;
import com.borqs.common.view.ContactUserSelectItemView.AllPeoplecheckItemListener;
import com.borqs.qiupu.AddressPadMini;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.InviteContactActivity.UITAB;
import com.borqs.qiupu.util.StringUtil;

public final class PickAudiencePhoneFragment extends PickAudienceBaseFragment implements AllPeoplecheckItemListener {
	private static final String TAG = "PickAudiencePhoneFragment";

	private Activity mActivity;
	private Cursor mCursorSearch;
	private HashSet<String> mSelectedPhone = new HashSet<String>();
	private PickPeoplePhoneFragmentCallbackListener mCallbackListener;
    private Cursor mCursorPhone;
    private Handler mHandler;

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		mActivity = activity;
		try{
		    mCallbackListener = (PickPeoplePhoneFragmentCallbackListener)activity;
		    mCallbackListener.getPickPeoplePhoneFragment(this);
		}catch (ClassCastException e) {
			Log.d(TAG, activity.toString() +  "must implement CallBackLocalUserListFragmentListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		orm = QiupuORM.getInstance(mActivity);
		mHandler = new Handler();
		mSelectedPhone = mCallbackListener.getSelectPhone();
		AddressPadMini.registerNoteActionListener(getClass().getName(), this);
		ContactUserSelectItemView.registerAllPeopleCheckItemListener(getClass().getName(), this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
//		contactPhoneSelectAdds = getSelectValue(mSelectedPhone);
		setAddress();
		new DeSerializationTask().execute((Void[])null);
	}
	
	private class DeSerializationTask extends android.os.AsyncTask<Void, Void, Void>
    {       
        public DeSerializationTask()
        {
            super();            
            Log.d(TAG, "create DeSerializationTask="+this);
        }

        @Override
        protected Void doInBackground(Void... params)             
        {
            new GetContactTask().execute("");
            return null;
        }
    }
	
	private class GetContactTask extends AsyncTask<String, String, String> {
        public String doInBackground(String... params) {
            Log.d(TAG, "GetContactTask doInBackground enter.");
//            begin();            

            mCursorPhone = getLocalPhoneCursor(null);
//            mCursorSimContacts = GetSimContact(null);
//          }
//            generateSelectData(receiveUserAdds, mSelectedPhone);
            Log.d(TAG, "GetContactTask doInBackground exit.");
            return "";
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        public void onPostExecute(String Re) {
            Log.d(TAG, "GetContactTask onPostExecute enter.");

                refreshUI(mCursorPhone, mSelectedPhone);
//            end();
            Log.d(TAG, "GetContactTask onPostExecute exit.");
        }
    }
	
	
	public void onResume() {
		super.onResume();
	};
	
	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	};
	
	public void onDestroy() {
		super.onDestroy();
		ContactUserSelectItemView.unRegisterAllPeopleCheckItemListener(getClass().getName());
		AddressPadMini.unregisterNoteActionListener(getClass().getName());
	};

	@Override
	public void doSearch(String key) {
	    if (StringUtil.isEmpty(key) == false) {
	        if (null != mCursorSearch && !mCursorSearch.isClosed()) {
	            mCursorSearch.close();
	        }
	        
	        mCursorSearch = getLocalPhoneCursor(key);
	        
	        refreshUI(mCursorSearch, mSelectedPhone);
	    } else {
	        refreshUI(mCursorPhone, mSelectedPhone);
	    }
	}
	
	private static final String SORT_ORDER = Contacts.DISPLAY_NAME + " ASC";
    private Cursor getLocalPhoneCursor(String keyword) {
        final String where = TextUtils.isEmpty(keyword) ?
                Phone.NUMBER + " IS NOT NULL and " + Phone.NUMBER + "!= ''" :
                Phone.NUMBER + " IS NOT NULL and " + Phone.NUMBER + "!= '' and (" +
                        Contacts.DISPLAY_NAME + " like '%" + keyword + "%' or " +
                        Phone.NUMBER + " like '%" + keyword + "%')";
        String[] projection = new String[]{Contacts.DISPLAY_NAME, Phone.NUMBER};
        return mActivity.getContentResolver().query(Phone.CONTENT_URI, projection, where, null, SORT_ORDER);
    }
    
    private void refreshUI(Cursor cursor, HashSet<String> selectedSet) {
        if (null != cursor) {
            mAdapter.swapToCursor(cursor, selectedSet, UITAB.DISPLAY_PHONE);
        }
    }
    
    public String getSelectPhone() {
        return super.getSelectValue(mSelectedPhone);
    }
    
	public interface PickPeoplePhoneFragmentCallbackListener {
		public void getPickPeoplePhoneFragment(PickAudiencePhoneFragment fragment);
		public HashSet<String> getSelectPhone();
		public HashMap<String, String> getSelectPhoneEmailNameMap();
	}
	
	private void selectPhone(ContactSimpleInfo user) {
	    if (user.selected) {
            mSelectedPhone.add(user.phone_number);
            mSelectePhoneEmailNamedmap.put(user.phone_number, user.display_name_primary);
        } else {
            mSelectedPhone.remove(user.phone_number);
            mSelectePhoneEmailNamedmap.remove(user.phone_number);
        }
	}
	
	@Override
    public void selectItem(ContactSimpleInfo user) {
        if (user != null) {
//            if (user.mBindId == UITAB.DISPLAY_EMAIL) {
//                selectEmail(user);
//                contactEmailSelectAdds = getselectValue();
//            } else if (UITAB.DISPLAY_PHONE == user.mBindId) {
            	if(QiupuConfig.DBLOGD)Log.d(TAG, "selecctItem: " + user.selected);
                selectPhone(user);
                contactPhoneSelectAdds = getSelectPhone();
                setAddress();
//            }
        }
    }
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		if(hidden) {
			ContactUserSelectItemView.unRegisterAllPeopleCheckItemListener(getClass().getName());
		}else {
			ContactUserSelectItemView.registerAllPeopleCheckItemListener(getClass().getName(), this);
		}
	}
	
	@Override
	public void noteRemove(String notStr) {
		if(QiupuConfig.DBLOGD)Log.d(TAG, "noteRemove, notStr: " + notStr);
		if(notStr != null) {
			if (notStr.contains("*")) {
				int index = notStr.indexOf("*");
				notStr = notStr.substring(index + 1, notStr.length());
				mSelectedPhone.remove(notStr);
				mSelectePhoneEmailNamedmap.remove(notStr);
				mAdapter.refreshSelectSet(mSelectedPhone);
				contactPhoneSelectAdds = getSelectPhone();
				setAddress();

				final String tmpStr = notStr;
				mHandler.post( new Runnable() {
					public void run() {
						for(int i= 0; i < mListView.getChildCount(); i++) {
							View v = mListView.getChildAt(i);
							if(ContactUserSelectItemView.class.isInstance(v)) {
								ContactUserSelectItemView fv = (ContactUserSelectItemView)v;
								if(fv.refreshCheckItem(tmpStr, false)){
//							break;
								}
							}
						}
					}
				});
			}
		}
	}
}
