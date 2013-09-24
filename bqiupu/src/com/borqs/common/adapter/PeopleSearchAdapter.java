package com.borqs.common.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.view.BpcFriendsItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import twitter4j.QiupuUser;

import java.util.ArrayList;
import java.util.HashMap;

public class PeopleSearchAdapter extends BaseAdapter{
	private static final String TAG = "Qiupu.PeopleSearchAdapter";
	private static final int spanCount = 1;
    private Cursor items;
    private ArrayList<QiupuUser> itemList = new ArrayList<QiupuUser>();
    private Context      mContext;
    
    private QiupuORM orm;
    
    private HashMap<String, UsersActionListner> mUserActionMap = new HashMap<String, UsersActionListner>();

    public void registerUsersActionListner(String key, UsersActionListner rl) {
    	mUserActionMap.put(key, rl);
    }

    public void unregisterUsersActionListner(String key) {
    	mUserActionMap.remove(key);
    }
    
    private MoreItemCheckListener mCheckerListener;

    public interface MoreItemCheckListener {
        public boolean isMoreItemHidden();
        public View.OnClickListener getMoreItemClickListener();
        public int getMoreItemCaptionId();
    }
    
	public PeopleSearchAdapter(Context context, MoreItemCheckListener listener){
		mContext = context;
		mCheckerListener = listener;
		orm = QiupuORM.getInstance(context);
	}
	
	public PeopleSearchAdapter(Context context, Cursor users, MoreItemCheckListener listener ){
		this(context, listener);
		items = users;
	}
	
	public PeopleSearchAdapter(Context context,  ArrayList<QiupuUser> users,MoreItemCheckListener listener){
		this(context, listener);
		itemList.clear();
		itemList.addAll(users);
	}

	public int getCount() {
		int count = 0;
		if(items != null){
			count = items.getCount();
		}
		if(itemList != null && itemList.size() > 0)
		{
			count = count + itemList.size() + spanCount;
			if (null != mCheckerListener && mCheckerListener.isMoreItemHidden()) {
			    ++count;
			}
		}
		return count;
	}
	
	public QiupuUser getItem(int position) {	
		Integer newposition = posMap.get(new Long(position));
    	if(debugsort)
    		Log.d(TAG, "get Item position="+position + " map to="+newposition);
    	if(items != null && newposition != null
    			&& newposition >= 0 && items.moveToPosition(newposition))
    	{
    		return orm.createUserInformation(items);
    	}
    	else if(itemList != null && newposition != null && newposition >= 0 
    			&& newposition - getCusorCount() >= 0
    			&& itemList.size() >= (newposition - getCusorCount()))
    	{
    		return itemList.get(newposition - getCusorCount());
    	}
    	else
    	{
    		//TODO					
    		return null;
    	}
	}
	
	public long getItemId(int position) {		
		//QiupuUser user =  getItem(position);
		Integer newposition = posMap.get(new Long(position));		
		return newposition ==null?-1:newposition;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;		
		QiupuUser user = getItem(position);
		Integer newposition = posMap.get(new Long(position));
		if(user != null)
		{
			if (convertView == null || false == (convertView instanceof BpcFriendsItemView) )
		    {
				BpcFriendsItemView FView = new BpcFriendsItemView(mContext, user, false, false);
		        holder = new ViewHolder();
		        
		        FView.setTag(holder);
		        holder.view = FView;
		        FView.attachActionListener(mUserActionMap);
		        convertView = FView;
		        
		    } else {
		        holder = (ViewHolder)convertView.getTag();
		        holder.view.setUser(user);
		        holder.view.attachActionListener(mUserActionMap);
		    }
			
			return convertView;
		}
		else
		{
			String dockStr = "";
			if(newposition != null)
			{	
				if(newposition < 0)
				{
					final int pos = Math.abs(newposition);
					if(pos == 1000)
					{
						dockStr = mContext.getString(R.string.search_from_net_title);
					}
				}
				TextView but = (TextView)(((Activity) mContext).getLayoutInflater().inflate(R.layout.a_2_z_textview, null));	    	    
				but.setText(dockStr);
				return but;
			}
			else 
            {
                return generateMoreItem();
            }
		}
	}

	static class ViewHolder
	{
		public BpcFriendsItemView view;
	}
	
	private View generateMoreItem() {
        if (null != mCheckerListener) {
            Button but = generateMoreItemView();
            but.setOnClickListener(mCheckerListener.getMoreItemClickListener());
            but.setText(mCheckerListener.getMoreItemCaptionId());
            return but;
        }
        return null;
    }
    
    private Button generateMoreItemView() {
        Button but = (Button) (((Activity) mContext).getLayoutInflater().inflate(R.layout.more_button_stream, null));
        but.setTextColor(mContext.getResources().getColor(R.color.more_text_color));
        but.setBackgroundResource(R.drawable.list_selector_background);
        return but;
    }
	
	private HashMap<Long, Integer> posMap = new HashMap<Long, Integer>();
	private final static boolean debugsort = false;
	
	public void alterDataList(Cursor cursor) {
		if(items == cursor)
			return;
		
		items = cursor;

		setATOZitem();
		//calculator the position		
		notifyDataSetChanged();
	}

	public void alterDataList(Cursor cursor, ArrayList<QiupuUser> userList) {
		if(items != null && items != cursor)
		{
			items.close();
			items = cursor;
		}
		else
		{
			items = cursor;
		}
		
		itemList.clear();
		itemList.addAll(userList);
		
		setATOZitem();
		
		notifyDataSetChanged();
	}	
	
	private void setATOZitem()
	 {
		posMap.clear();
		 if(items != null)
		 {
			 final int cursorsize = items.getCount();
			 for(int i=0; i<cursorsize; i++)
			 {
				 posMap.put(new Long(posMap.size()), i);
			 }
		 }
		 
		 if(itemList != null && itemList.size() > 0)
		 {
			 posMap.put(new Long(posMap.size()), -1000);
			 for(int i=0; i<itemList.size(); i++)
			 {
				 posMap.put(new Long(posMap.size()), i + getCusorCount());
			 }	
		 }
	 }
	
	public int getCusorCount() {		
		return items != null ? items.getCount() : 0;
	}
	
	public void setCursor(Cursor cursor)
	{
		if(items != null && items != cursor)
		{
			items.close();
			items = cursor;
		}
		else
		{
			items = cursor;
		}
	}
	
	public void setUsersList(ArrayList<QiupuUser> users) {
		itemList.clear();
		itemList.addAll(users);
	}
}
