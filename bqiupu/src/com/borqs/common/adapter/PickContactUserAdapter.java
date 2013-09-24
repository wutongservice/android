package com.borqs.common.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.common.listener.CheckBoxClickActionListener;
import com.borqs.common.util.AlphaPost;
import com.borqs.common.view.AtoZ;
import com.borqs.common.view.PickContactUserItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.util.ATOZUtils;
import com.borqs.qiupu.util.ContactUtils;

/**
 * Adapter that combine an ArrayList and a Cursor within a ListView, which
 * allow to use as a ArrayList adapter, or a Cursor adapter, or event both.
 */
public class PickContactUserAdapter extends BaseAdapter{
	private static final String TAG = "Qiupu.PickContactUserAdapter";
    private Cursor       mCursor;
    private Context      mContext;
    private  HashSet<Long> mSelectedSet = new HashSet<Long>() ;
    private ArrayList<AlphaPost> alphaPos   = new ArrayList<AlphaPost>();
    private ArrayList<String>    namePinYin = new ArrayList<String>();	
    private HashMap<Long, Integer> posMap = new HashMap<Long, Integer>();
    private final static boolean debugsort = false;
    private boolean      mSelectedAll;

    private HashMap<String, CheckBoxClickActionListener> mCheckClickListenerMap = new HashMap<String, CheckBoxClickActionListener>();

    public void registerCheckClickActionListener(String key, CheckBoxClickActionListener rl) {
        mCheckClickListenerMap.put(key, rl);
    }

    public void unRegisterCheckClickActionListener(String key) {
        mCheckClickListenerMap.remove(key);
    }

    private void resetData(Cursor cursor, int bindId) {
    	if(mCursor != cursor)
    		mCursor = cursor;
    	
        mSelectedAll = false;
    }

    public PickContactUserAdapter(Context context, Cursor cursor){
		mContext = context;
        resetData(cursor, -1);
	}

	public int getCount() {
		int count = alphaPos.size();
		
		if(mCursor != null && mCursor.getCount() > 0)
			count = count + mCursor.getCount();

//		int count = 0;
//		if(mCursor != null && mCursor.getCount() > 0)
//			count = mCursor.getCount();
		
		return count;
	}
	
	public ContactSimpleInfo getItem(int position) {
		Integer newposition = posMap.get(new Long(position));
    	if(debugsort)
    		Log.d(TAG, "get Item position="+position + " map to="+newposition);
    	if(newposition != null && newposition >= 0){
    	    if(mCursor != null && mCursor.moveToPosition(newposition)){
    	    	ContactSimpleInfo info = parserCursor(mCursor, newposition);
    	    	postSetupContactInfo(info);
                return info;
    	    }else{
    	        return null;
    	    }
    	}else{
    	    return null;
    	}
        
//	    if (mCursor != null && mCursor.moveToPosition(position)) {
//	    	info = parserCursor(mCursor, position);
//        } else {
//            info = null;
//        }
//
//        postSetupContactInfo(info);
//
//        return info;
	}
	
	public long getItemId(int position) {
		return position;
	}

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        ContactSimpleInfo di = getItem(position);
        if (di != null) {
        	PickContactUserItemView v;
            if (convertView == null || false == (convertView instanceof PickContactUserItemView)) {
                holder = new ViewHolder();
                v = new PickContactUserItemView(mContext, di);
                v.attachCheckListener(mCheckClickListenerMap);
                holder.view = v;
                v.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                v = holder.view;
                v.setUserItem(di);
                v.attachCheckListener(mCheckClickListenerMap);
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
        public PickContactUserItemView view;
    }


    public void setSelectAll(boolean selected) {
        mSelectedAll = selected;
    }

    private void postSetupContactInfo(ContactSimpleInfo info) {
        if (null != info) {
            if (mSelectedAll) {
                info.selected = true;
            } else if (null != mSelectedSet) {
                if (mSelectedSet.contains(info.mContactId)){
                    info.selected = true;
                }
            }
        }
    }
    
    public ContactSimpleInfo parserCursor(Cursor cursor, int position) {
        if (null == cursor) {
            Log.d(TAG, "parserCursor, return null for empty cursor");
            return null;
        } else if (position < 0 || position >= cursor.getCount()) {
            Log.d(TAG, "parserCursor, return null for invalid position:" + position);
            return null;
        }
        ContactSimpleInfo info = new ContactSimpleInfo();
        info.display_name_primary = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
        info.mPhotoId = cursor.getLong(cursor.getColumnIndex(Contacts.PHOTO_ID));
        info.mContactId = cursor.getLong(cursor.getColumnIndex(Contacts._ID));

        return info;
    }
    
    public void setSelectmap(HashSet<Long> set)
    {
    	mSelectedSet.clear();
    	mSelectedSet.addAll(set);
    	
    	if(mCursor != null)
    		mCursor.moveToPosition(-1);
    	
		 notifyDataSetChanged();
    }
    
    public void alterDataList(Cursor cursor, HashSet<Long> set, AtoZ atoz){
    	mSelectedSet.clear();
    	mSelectedSet.addAll(set);
		 
    	alterDataList(cursor, atoz);
    }
    
    public void alterDataList(Cursor cursor, AtoZ atoz) {
        if(mCursor != cursor)
		mCursor = cursor;
        
        if(alphaPos != null)
            alphaPos.clear();
        
        final int prePos = mCursor.getPosition();
        Log.d(TAG, "what is my current positin="+prePos);
        ATOZUtils.getAlphabetPos(namePinYin, alphaPos, mCursor, ContactUtils.sortCollom);		
      
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
    
    private View generateATOZItem()
	 {
	     TextView but = (TextView)(((Activity) mContext).getLayoutInflater().inflate(R.layout.a_2_z_textview, null));
	     but.setTextColor(mContext.getResources().getColor(R.color.atoz_font));
	     but.setOnClickListener(null);
	     return but;
	 }
}
