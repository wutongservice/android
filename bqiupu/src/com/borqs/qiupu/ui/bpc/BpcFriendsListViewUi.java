package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import twitter4j.QiupuUser;
import twitter4j.UserCircle;
import android.content.Context;
import android.database.Cursor;
import android.widget.ListView;

import com.borqs.common.adapter.BPCFriendsNewAdapter;
import com.borqs.common.adapter.CircleAdapter;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.view.AtoZ;

public class BpcFriendsListViewUi {
	private BPCFriendsNewAdapter mListViewAdapter;
	private CircleAdapter mcircleAdapter;
	ListView   mListView;

	public void initUsers (Context con,ListView listView) {
		 mListViewAdapter = new BPCFriendsNewAdapter(con, null, false, false);
		 listView.setAdapter(mListViewAdapter);
		 mListView = listView;
	}
	
	public void initCircles (Context con, ListView listView) {
		mcircleAdapter = new CircleAdapter(con);
		listView.setAdapter(mcircleAdapter);
		mListView = listView;
	}

	public void loadUserRefresh(ArrayList<QiupuUser> userList) {
        mListViewAdapter.alterDataList(userList);
	}
	
	public void loadUserRefresh(Cursor userList) {
        mListViewAdapter.alterDataList(userList);        
	}
	
	public void loadUserRefresh(Cursor userList, AtoZ atoz) {
        mListViewAdapter.alterDataList(userList, atoz);        
	}
	
	public void loadCircleRefresh(ArrayList<UserCircle> circleList)
	{
		mcircleAdapter.alterDataList(circleList);
	}
	
	public int getChildCount()
	{
		return mListViewAdapter.getCount();
	}

	public void movetoPostion(int position) {
		mListView.setSelection(position);
	}
	
	public void registerUsersActionListner(String key, UsersActionListner listener) {
		mListViewAdapter.registerUsersActionListner(key, listener);
	}
	
	public void unregisterUsersActionListner(String key) {
		mListViewAdapter.unregisterUsersActionListner(key);
	}
	
	public void resetCursor(Cursor cursor) {
		mListViewAdapter.resetCursor(cursor);
	}
}


