package com.borqs.qiupu.fragment;

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
import com.borqs.common.view.ContactUserSelectItemView.AllPeoplecheckItemListener;
import com.borqs.qiupu.AddressPadMini;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.InviteContactActivity.UITAB;
import com.borqs.qiupu.util.StringUtil;

public final class PickAudienceEmailFragment extends PickAudienceBaseFragment implements AllPeoplecheckItemListener{
	private static final String TAG = "PickAudienceEmailFragment";

	private Activity mActivity;
	private Cursor mCursorSearch;
	private HashSet<String> mSelectedEmail = new HashSet<String>();
	
	private PickAudienceEmailFragmentCallbackListener mCallbackListener;
    private Cursor mCursorEmail;

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		mActivity = activity;
		try{
		    mCallbackListener = (PickAudienceEmailFragmentCallbackListener)activity;
		    mCallbackListener.getPickAudienceEmailFragment(this);
		}catch (ClassCastException e) {
			Log.d(TAG, activity.toString() +  "must implement CallBackLocalUserListFragmentListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		orm = QiupuORM.getInstance(mActivity);
		mSelectedEmail = mCallbackListener.getSelectEmail();
		
		ContactUserSelectItemView.registerAllPeopleCheckItemListener(getClass().getName(), this);
		AddressPadMini.registerNoteActionListener(getClass().getName(), this);
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
//		contactEmailSelectAdds = getSelectValue(mSelectedEmail);
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
		AddressPadMini.unregisterNoteActionListener(getClass().getName());
	};

	private void selectEmail(ContactSimpleInfo user) {
	    
	    if (user.selected) {
            mSelectedEmail.add(user.email);
            mSelectePhoneEmailNamedmap.put(user.email, user.display_name_primary);
        } else {
            mSelectedEmail.remove(user.email);
            mSelectePhoneEmailNamedmap.remove(user.email);
        }
	}
	
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
    
	public interface PickAudienceEmailFragmentCallbackListener {
		public void getPickAudienceEmailFragment(PickAudienceEmailFragment fragment);
		public HashSet<String> getSelectEmail();
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
    public void selectItem(ContactSimpleInfo user) {
        if (user != null) {
//            if (user.mBindId == UITAB.DISPLAY_EMAIL) {
//                selectEmail(user);
//                contactEmailSelectAdds = getselectValue();
//            } else if (UITAB.DISPLAY_PHONE == user.mBindId) {
            	if(QiupuConfig.DBLOGD)Log.d(TAG, "selecctItem: " + user.toString());
                selectEmail(user);
                contactEmailSelectAdds = getSelectEmail();
                setAddress();
//            }
        }
    }
	
	@Override
	public void noteRemove(String notStr) {
		if(QiupuConfig.DBLOGD)Log.d(TAG, "noteRemove, notStr: " + notStr);
		if(notStr != null) {
			if (notStr.contains("*")) {
				int index = notStr.indexOf("*");
				notStr = notStr.substring(index + 1, notStr.length());
				mSelectedEmail.remove(notStr);
				mSelectePhoneEmailNamedmap.remove(notStr);
				mAdapter.refreshSelectSet(mSelectedEmail);
				contactEmailSelectAdds = getSelectEmail();
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
