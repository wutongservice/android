package com.borqs.common.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.qiupu.R;
import com.borqs.common.listener.FriendsContactActionListner;
import com.borqs.common.util.AlphaPost;
import com.borqs.common.view.AtoZ;
import com.borqs.common.view.PickPeopleItemView;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.QiupuORM.LookUpPeopleColumns;
import com.borqs.qiupu.util.ATOZUtils;
import com.borqs.qiupu.util.ContactUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Adapter that combine an ArrayList and a Cursor within a ListView, which allow
 * to use as a ArrayList adapter, or a Cursor adapter, or event both.
 */
public class PickPeopleAdapter extends BaseAdapter {
    private static final String TAG = "Qiupu.PickPeopleAdapter";
    private Cursor              mCursor;
    private Context             mContext;
    private ArrayList<AlphaPost> alphaPos   = new ArrayList<AlphaPost>();
    private ArrayList<String>    namePinYin = new ArrayList<String>();	
    private HashMap<Long, Integer> posMap = new HashMap<Long, Integer>();
    private final static boolean debugsort = false;
    private QiupuORM orm;
    private boolean mIsVCard = false;
    
    private HashMap<String, FriendsContactActionListner> mfriendsContactMap = new HashMap<String, FriendsContactActionListner>();

    public void registerFriendsContactActionListener(String key, FriendsContactActionListner rl) {
    	mfriendsContactMap.put(key, rl);
    }

    public void unregisterFriendsContactActionListener(String key) {
    	mfriendsContactMap.remove(key);
    }

    private void resetData(Cursor cursor, Cursor extraCursor) {
        if (mCursor != cursor) {
        	if(mCursor != null)
        		mCursor.close();
        		mCursor = null;
        	
            mCursor = cursor;
        }
        
        finishedFetchData = false;
        refreshedClicked = true;        
        constructContactCache(mCursor);
    }

    public PickPeopleAdapter(Context context, Cursor cursor, Cursor extraCursor) {
        mContext = context;
        orm = QiupuORM.getInstance(mContext);
        resetData(cursor, extraCursor);
    }

    public PickPeopleAdapter(Context context, boolean isVCard) {
        mContext = context;
        mIsVCard = isVCard;
        orm = QiupuORM.getInstance(mContext);
    }

    @Override
    public int getCount() {
    	
    	int count = alphaPos.size();
    	
    	if(mCursor != null && mCursor.isClosed() == false && mCursor.getCount() > 0)
    		count = count + mCursor.getCount();
    	return count;
    }

    @Override
    public ContactSimpleInfo getItem(int position) {
        Integer newposition = posMap.get(new Long(position));
    	if(debugsort)
    		Log.d(TAG, "get Item position="+position + " map to="+newposition);
    	if(newposition != null && newposition >= 0){
    	    if(mCursor != null && mCursor.moveToPosition(newposition)){
    	    	ContactSimpleInfo info = parserCursor(mCursor, newposition);
                return info;
    	    }else{
    	        return null;
    	    }
    	}else{
    	    return null;
    	}
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	final ViewHolder holder;
        ContactSimpleInfo di = getItem(position);
        if (di != null) {
        	PickPeopleItemView v;
            if (convertView == null || false == (convertView instanceof PickPeopleItemView)) {
                holder = new ViewHolder();
                v = PickPeopleItemView.newInstance(mContext, di, orm, mIsVCard);
                v.attachActionListener(mfriendsContactMap);
                holder.view = v;
                v.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                v = holder.view;
                v.setUserItem(di);
                v.attachActionListener(mfriendsContactMap);
            }
            return v;
        } else{
        	Integer newposition = posMap.get(new Long(position));
            String dockStr = "";
            if(newposition != null)
            {	
                if(newposition < 0)
                {
                    final int pos = Math.abs(newposition);
                    if(pos == 10000)
                    {
                        dockStr = namePinYin.get(0);
                    }
                    else
                    {
                        dockStr = namePinYin.get(pos);
                    }
                }
            }
            TextView tv = (TextView) generateATOZItem();
            tv.setText(dockStr);
            return tv;
		}
    }
    
    static class ViewHolder {
        public PickPeopleItemView view;
    }

    // TODO: handle multiple registered phone/email entities
    static HashMap<Long, ContactSimpleInfo> contacts = new HashMap<Long, ContactSimpleInfo>();
    static boolean finishedFetchData = false;   
    
    //if is going fetch the contact data, will break again and refetch
    static boolean refreshedClicked = false;
    
    BasicHandler   mHanlder = new BasicHandler();
    
    public void refresh()
    {
    	if(finishedFetchData == true)
    	{
    		finishedFetchData = false;
	    	constructContactCache(mCursor);
    	}
    	else
    	{
    		refreshedClicked = true;
    	}
    }
    
    public static final int Need_Do_nexttime = 0;
    public static final int UPDATE_UI        = 1;
    public static final int DO_CALC_ALPHABET_NEXT =2;
    
    class BasicHandler extends Handler {   
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Need_Do_nexttime:
                	Log.d(TAG, "do next time to fetch contact data");
                	finishedFetchData = false;
                	constructContactCache(mCursor);
                    break;
                case UPDATE_UI:
                	Log.d(TAG, "end fetch contact map with borqsid  and update UI");
                	notifyDataSetChanged();
                	break;
                case DO_CALC_ALPHABET_NEXT:
                	break;
            }
        }
    }
    
    public void constructContactCache(final Cursor cursor)
    {
    	if(finishedFetchData == true)
			return;
    	
    	QiupuORM.sWorker.post(new Runnable()
    	{
    		public void run()
    		{
    			try{
	    			if(cursor.moveToFirst())
			    	{
	    				Log.d(TAG, "begin fetch contact map with borqsid");
			    		do{
			    			
			    			final long contactId = cursor.getLong(cursor.getColumnIndex(Contacts._ID));
					    	ContactSimpleInfo info = contacts.get(new Long(contactId));
					    	if(info == null)
					    	    info = new ContactSimpleInfo();     
					    	
					        info.display_name_primary = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
					        info.mPhotoId = cursor.getLong(cursor.getColumnIndex(Contacts.PHOTO_ID));
					        info.mContactId = contactId;
					        
					        Cursor localPeopleCursor = orm.queryLookUpPeopleByContactId(info.mContactId);
					        
					        if(localPeopleCursor != null) {
					        	if (localPeopleCursor.getCount() > 0)
					        	{
					        		if(localPeopleCursor.moveToFirst()){
						        		int tempUid;
						        		do {
						        			tempUid = localPeopleCursor.getInt(localPeopleCursor.getColumnIndex(LookUpPeopleColumns.UID));
						        			if (info.mBorqsId < tempUid) {
						        				info.mBorqsId = tempUid;
						        			}
						        		} while(localPeopleCursor.moveToNext());
										//info.image_url = localPeopleCursor.getString(localPeopleCursor.getColumnIndex(LookUpPeopleColumns.IMAGE_URL));
										//info.system_display_name = localPeopleCursor.getString(localPeopleCursor.getColumnIndex(LookUpPeopleColumns.DISPLAY_NAME));
					        	    }
					        	}
					        	localPeopleCursor.close();
					        }
					        
					        contacts.put(new Long(contactId), info);
			    		}while(cursor.moveToNext());
			    	}
    			}catch(Exception ne){}
		    	
		    	finishedFetchData = true;
		    	
		    	Log.d(TAG, "end fetch contact map with borqsid");
		    	mHanlder.obtainMessage(UPDATE_UI).sendToTarget();		    	
		    	if(refreshedClicked == true)
		    	{
		    	    mHanlder.obtainMessage(Need_Do_nexttime).sendToTarget();
		    	    refreshedClicked = false;
		    	}
    		}
    	});
    }
    
    public ContactSimpleInfo parserCursor(Cursor cursor, int position) {
        if (null == cursor) {
            Log.d(TAG, "parserCursor, return null for empty cursor");
            return null;
        } else if (position < 0 || position >= cursor.getCount()) {
            Log.d(TAG, "parserCursor, return null for invalid position:" + position);
            return null;
        }

        final long contactId = cursor.getLong(cursor.getColumnIndex(Contacts._ID));
        ContactSimpleInfo item = contacts.get(new Long(contactId));
        if(item != null)
        	return item;
        
        ContactSimpleInfo info = new ContactSimpleInfo();        
        info.display_name_primary = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
        info.mPhotoId = cursor.getLong(cursor.getColumnIndex(Contacts.PHOTO_ID));
        info.mContactId = contactId;
        
       
        if(finishedFetchData == false)
        {
        	//wait until finished fetch the data
        	return info;
        }
        //too slowly to get the data
        if(debugsort)
        Log.d(TAG, "begin do fetch="+new Date().toGMTString());
        Cursor localPeopleCursor = orm.queryLookUpPeopleByContactId(info.mContactId);
        
        if(localPeopleCursor != null) {
        	if (localPeopleCursor.getCount() > 0){
        		if(debugsort)
        		Log.d(TAG, "begin do fetch size="+localPeopleCursor.getCount());
        		
        		if(localPeopleCursor.moveToFirst()){
	        		int tempType;
	        		int tempUid;
	        		do {
	        			tempUid = localPeopleCursor.getInt(localPeopleCursor.getColumnIndex(LookUpPeopleColumns.UID));
	        			if (info.mBorqsId < tempUid) {
	        				info.mBorqsId = tempUid;
	        			}
	        		} while(localPeopleCursor.moveToNext());
					//info.image_url = localPeopleCursor.getString(localPeopleCursor.getColumnIndex(LookUpPeopleColumns.IMAGE_URL));
					//info.system_display_name = localPeopleCursor.getString(localPeopleCursor.getColumnIndex(LookUpPeopleColumns.DISPLAY_NAME));
	        	}
        	}
        	localPeopleCursor.close();
        }
        
        if(debugsort)
        Log.d(TAG, "end do fetch="+new Date().toGMTString());
        
        contacts.put(new Long(contactId), info);
        return info;
    }

    public void alterDataList(Cursor cursor, Cursor extraCuror) {
        resetData(cursor, extraCuror);
        notifyDataSetChanged();
    }
    
    public void alterDataList(Cursor cursor, Cursor cursorClone, AtoZ atoz) {
    	resetData(cursor, null);
    	
    	if(cursorClone != null){
    		if(alphaPos != null)
    			alphaPos.clear();
    		
    		final int prePos = cursorClone.getPosition();
    		Log.d(TAG, "what is my current positin="+prePos);
    		ATOZUtils.getAlphabetPos(namePinYin, alphaPos, cursorClone, ContactUtils.sortCollom);	
    		
    		try{
    		    cursorClone.close();
    		}catch(Exception ne){}
    		
    		posMap.clear();
    		String sections[] = new String[alphaPos.size()];
    		int counts[] = new int[alphaPos.size()];
    		for(int i=0;i<alphaPos.size();i++)
    		{	
    			AlphaPost item = alphaPos.get(i);
    			sections[i] = item.alpha;			
    			
    			if(debugsort)
    				Log.d(TAG, "original="+posMap.size() +" map to="+(item.pos==0?-10000:(-1*item.pos)));
    			
    			item.setNewPosition(posMap.size());
    			posMap.put(new Long(posMap.size()), item.pos==0?-10000:(-1*item.pos));
    			
    			if((i+1) < alphaPos.size())
    			{
    				counts[i]   = alphaPos.get(i+1).pos - item.pos;
    				
    				for(int index=item.pos;index<alphaPos.get(i+1).pos;index++)
    				{			
    					if(debugsort)
    						Log.d(TAG, "original="+posMap.size() +" map to="+index);
    					posMap.put(new Long(posMap.size()), index);
    				}
    			}
    			else
    			{
    				counts[i] = namePinYin.size() - item.pos;
    				//for last one
    				for(int index=item.pos;index<namePinYin.size();index++)
    				{	
    					if(debugsort)
    						Log.d(TAG, "original="+posMap.size() +" map to="+index);
    					posMap.put(new Long(posMap.size()), index);
    				}
    			}
    		}		
    		
    		if(atoz != null) {
    			atoz.setAlphaMap(posMap, alphaPos);
    		}
    		
    		//calculator the position		
    		notifyDataSetChanged();
    	}
    }
    
    private View generateATOZItem()
	 {
	     TextView but = (TextView)(((Activity) mContext).getLayoutInflater().inflate(R.layout.a_2_z_textview, null));
	     but.setTextColor(mContext.getResources().getColor(R.color.atoz_font));
	     but.setOnClickListener(null);
	     return but;
	 }
}
