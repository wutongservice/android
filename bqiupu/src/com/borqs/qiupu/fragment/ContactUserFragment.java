package com.borqs.qiupu.fragment;

import java.util.HashSet;
import java.util.Iterator;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.borqs.common.adapter.PickContactUserAdapter;
import com.borqs.common.listener.CheckBoxClickActionListener;
import com.borqs.common.view.AtoZ;
import com.borqs.common.view.PickContactUserItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.fragment.LocalUserListFragment.CallBackLocalUserListFragmentListener;
import com.borqs.qiupu.ui.bpc.PickUserFragmentActivity;
import com.borqs.qiupu.util.ContactUtils;
import com.borqs.qiupu.util.StringUtil;

public class ContactUserFragment extends BasicFragment implements CheckBoxClickActionListener, AtoZ.MoveFilterListener {
	private final static String TAG = "ContactUserFragment";
    private Activity mActivity; 
    private ListView mListView;
    private Cursor searchContact;
    private PickContactUserAdapter mAdapter;
    private HashSet<Long> mSelectedUser = new HashSet<Long>();
    private HashSet<String> mSelectedPhone = new HashSet<String>();
    private HashSet<String> mSelectedEmail = new HashSet<String>();
    private AtoZ mAtoZ;
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	mActivity = activity;
    	try{
			CallBackContactuserFragmentListener listener = (CallBackContactuserFragmentListener)activity;
			listener.getContactuserFragment(this);
		}catch (ClassCastException e) {
			Log.d(TAG, activity.toString() +  "must implement CallBackContactuserFragmentListener");
		}
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	HashSet<Long> selectContactIds = (HashSet<Long>) mActivity.getIntent().getSerializableExtra("selectContactIds");
    	if(selectContactIds != null && selectContactIds.size() > 0){
    	    if(QiupuConfig.DBLOGD)Log.d(TAG, "selectContactIds.size: " + selectContactIds.size());
    	    mSelectedUser.addAll(selectContactIds);
    	    new Handler().post(new Runnable()
    		{
    			@Override
    			public void run()
    			{
    				setSelectPhoneEmail();
    			}
    		});
    	}
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView");
    	View convertView = inflater.inflate(R.layout.friends_list_a2z, container, false);			 
		mListView = (ListView)convertView.findViewById(R.id.friends_list);
		mListView.setOnItemClickListener(contactitemClickListener);
		
		AtoZ atoz = (AtoZ) convertView.findViewById(R.id.atoz);
    	if (atoz != null) {
    		mAtoZ = atoz;
    		atoz.setFocusable(true);
    		atoz.setMoveFilterListener(this);
    		atoz.setVisibility(View.VISIBLE);        		
    		mAtoZ.setListView(mListView);        		
    	}
		return convertView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        Cursor peopleCursor = ContactUtils.getContacts(mActivity);
        mAdapter = new PickContactUserAdapter(mActivity, peopleCursor);
        mAdapter.registerCheckClickActionListener(getClass().getName(), this);
        mAdapter.setSelectmap(mSelectedUser);
        mAdapter.alterDataList(peopleCursor, mSelectedUser, mAtoZ);
		mListView.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
        mAdapter.unRegisterCheckClickActionListener(getClass().getName());
    	if(searchContact != null){
    		searchContact.close();
    	}
    }
    
    private AdapterView.OnItemClickListener contactitemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
            if(PickContactUserItemView.class.isInstance(view)) {
            	if(PickUserFragmentActivity.class.isInstance(mActivity)){
            		PickContactUserItemView uv = (PickContactUserItemView)view;
            		uv.switchCheck();
            		changeSelect(uv.getDataItemId(), uv.isSelected());
            	}
            }
        }
    };
    
    private void changeSelect(long itemId, boolean isSelected){
    	if(QiupuConfig.LOGD) Log.d(TAG, "itemId: " + itemId + " isSelected: " + isSelected );
    	if (isSelected) {
			if(!mSelectedUser.contains(itemId))
			{
				mSelectedUser.add(itemId);
			}
		} else {
			if(mSelectedUser.contains(itemId))
			{
				mSelectedUser.remove(itemId);
			}
		}

    	ContactUtils.generatePhoneEmailSetById(mActivity, itemId, mSelectedPhone,
    	        mSelectedEmail, isSelected);
    	mAdapter.setSelectmap(mSelectedUser);
    }
    
    public HashSet<String> getSelectPhone(){
		return mSelectedPhone;
	}
    
	public HashSet<String> getSeleceEmail(){
		return mSelectedEmail;
	}
	
	public HashSet<Long> getSelectUid(){
	    return mSelectedUser;
	}
	
	public void doSearch(String key)
    {
		if(!StringUtil.isEmpty(key))
		{
			if (null != searchContact && !searchContact.isClosed()) {
				searchContact.close();
            }
			searchContact = ContactUtils.searchContactByKey(mActivity, key);
		}
		else
		{
			searchContact = ContactUtils.getContacts(mActivity);
		}
		mAdapter.alterDataList(searchContact, mSelectedUser, mAtoZ);
    }
	
	private void setSelectPhoneEmail(){
		Iterator it = mSelectedUser.iterator();
		  while(it.hasNext())
		  {
			  Long contactId = (Long) it.next();
			  ContactUtils.generatePhoneEmailSetById(mActivity, contactId, mSelectedPhone,
		    	        mSelectedEmail, true);
		  }
	}

	@Override
	public void changeItemSelect(long itemId, String itemlabel, boolean isSelect, boolean isuser) {
		changeSelect(itemId, isSelect);
	}

	@Override
	public void enterPosition(String alpha, int position) { 
		mListView.setSelection(position);
	}

	@Override
	public void leavePosition(String alpha) { }

	@Override
	public void beginMove() { }

	@Override
	public void endMove() { }
	
	public interface CallBackContactuserFragmentListener {
		public void getContactuserFragment(ContactUserFragment fragment);
	}
}
