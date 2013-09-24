package com.borqs.qiupu.fragment;

import java.util.HashMap;
import java.util.HashSet;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
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

public final class PickContactEmailFragment extends PickContactBaseFragment {
	private static final String TAG = "PickContactEmailFragment";

	private Activity mActivity;
	private Cursor mCursorSearch;
	private HashSet<String> mSelectedEmail = new HashSet<String>();
	private HashMap<String, String> mSelectedmap = new HashMap<String, String>();
	private PickContactEmailFragmentCallbackListener mCallbackListener;
    private Cursor mCursorEmail;

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		mActivity = activity;
		try{
		    mCallbackListener = (PickContactEmailFragmentCallbackListener)activity;
		    mCallbackListener.getPickContactEmailFragment(this);
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

            mCursorEmail = getLocalEmailCursor(null);
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

                refreshUI(mCursorEmail, mSelectedEmail);
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
		ContactUserSelectItemView.unRegisterAllPeopleCheckItemListener(getClass().getName());
	};

	@Override
	protected void selectEmail(ContactSimpleInfo user) {
	    
	    if (user.selected) {
            mSelectedEmail.add(user.email);
            mSelectedmap.put(user.email, user.display_name_primary);
        } else {
            mSelectedEmail.remove(user.email);
            mSelectedmap.remove(user.email);
        }
	    if(mCallbackListener != null) {
	        mCallbackListener.refreshEmailSelectCount(mSelectedEmail.size());
	    }
	}
	
//	public void changeSelect(int bindId, String itemId, boolean isSelected) {
//	    
//	    if (bindId == UITAB.DISPLAY_PHONE) {
//	        if (isSelected) {
//	            mSelectedEmail.add(itemId);
//	        } else {
//	            mSelectedEmail.remove(itemId);
//	        }
//	    } else {
//	        Log.e(TAG, "changeSelect, unexpected tab id: " + bindId + ", itemId:" + itemId);
//	    }
//	    
//	    refreshSelectedCount();
//	}
//	
//	private void refreshSelectedCount() {
//    }
//	
	
	@Override
	public void doSearch(String key) {
	    if (StringUtil.isEmpty(key) == false) {
	        if (null != mCursorSearch && !mCursorSearch.isClosed()) {
	            mCursorSearch.close();
	        }
	        
	        mCursorSearch = getLocalEmailCursor(key);
	        
	        refreshUI(mCursorSearch, mSelectedEmail);
	    } else {
	        refreshUI(mCursorEmail, mSelectedEmail);
	    }
	}

	private static final String SORT_ORDER = Contacts.DISPLAY_NAME + " ASC";

	 private Cursor getLocalEmailCursor(String keyword) {
	        final String where = TextUtils.isEmpty(keyword) ?
	                Email.DATA +" IS NOT NULL and " + Email.DATA + "!= ''" :
	                Email.DATA +" IS NOT NULL and " + Email.DATA + "!= '' and (" +
	                        Contacts.DISPLAY_NAME + " like '%" + keyword + "%' or " +
	                        Email.DATA + " like '%" + keyword + "%')";
	        return mActivity.getContentResolver().query(Email.CONTENT_URI,
	                    new String[]{Contacts.DISPLAY_NAME, Email.DATA},
	                    where, null, SORT_ORDER);
	    }
    
    private void refreshUI(Cursor cursor, HashSet<String> selectedSet) {
        if (null != cursor) {
            mAdapter.swapToCursor(cursor, selectedSet, UITAB.DISPLAY_EMAIL);
        }
    }
    
    public String getSelectEmail() {
        return super.getSelectValue(mSelectedEmail);
    }
    
    @Override
    public String getSelectName() {
        return super.getSelectName(mSelectedmap);
    }
    
    @Override
    public String getselectValue() {
        return getSelectEmail();
    }
    
	public interface PickContactEmailFragmentCallbackListener {
		public void getPickContactEmailFragment(PickContactEmailFragment fragment);
		public void refreshEmailSelectCount(int count);
	}
}
