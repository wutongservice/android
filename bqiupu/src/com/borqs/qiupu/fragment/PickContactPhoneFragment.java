package com.borqs.qiupu.fragment;

import java.util.HashMap;
import java.util.HashSet;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.common.view.ContactUserSelectItemView;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.InviteContactActivity.UITAB;
import com.borqs.qiupu.util.StringUtil;

public final class PickContactPhoneFragment extends PickContactBaseFragment {
	private static final String TAG = "PickPeoplePhoneFragment";

	private Activity mActivity;
	private Cursor mCursorSearch;
	private HashSet<String> mSelectedPhone = new HashSet<String>();
    private HashMap<String, String> mSelectedmap = new HashMap<String, String>();
	private PickPeoplePhoneFragmentCallbackListener mCallbackListener;
    private Cursor mCursorPhone;

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
		super.onPause();
	};
	
	public void onDestroy() {
		super.onDestroy();
		ContactUserSelectItemView.registerAllPeopleCheckItemListener(getClass().getName(), this);
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
    
    @Override
    public String getSelectName() {
        return super.getSelectName(mSelectedmap);
    }
    
    @Override
    public String getselectValue() {
        return getSelectPhone();
    };
    
	public interface PickPeoplePhoneFragmentCallbackListener {
		public void getPickPeoplePhoneFragment(PickContactPhoneFragment fragment);
		public void refreshPhoneSelectCount(int count);
	}
	
	@Override
	protected void selectPhone(ContactSimpleInfo user) {
	    if (user.selected) {
            mSelectedPhone.add(user.phone_number);
            mSelectedmap.put(user.phone_number, user.display_name_primary);
        } else {
            mSelectedPhone.remove(user.phone_number);
            mSelectedmap.remove(user.phone_number);
        }
	    if(mCallbackListener !=  null) {
	        mCallbackListener.refreshPhoneSelectCount(mSelectedPhone.size());
	    }
	}
}
