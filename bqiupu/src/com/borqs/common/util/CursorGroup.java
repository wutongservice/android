package com.borqs.common.util;

import java.util.ArrayList;

import android.database.Cursor;
import android.util.Log;

public class CursorGroup {
	private final String TAG = "borqsaccount.CursorGroup";
    public  ArrayList<Cursor> cursors = new ArrayList<Cursor>();
    
    public void requery(){
    	synchronized(cursors){
    		for(Cursor cursor : cursors){
    			cursor.requery();
    		}
    	}
    }
    
    public void close(){
    	synchronized(cursors){
    		for(Cursor cursor : cursors){
    			if(cursor != null){
    				if(!cursor.isClosed()){
    					cursor.close();
    				}
    				cursor = null;
    			}
    		}
    		cursors.clear();
    	}
    }

	public boolean hasData() {
		boolean hasdata = false;
		synchronized(cursors){
			for(Cursor cursor : cursors){
				if(cursor != null){
					if(cursor.getCount()>0){
						hasdata = true;
						break;
					}
				}
			}
		}
		Log.d(TAG,"hasData =="+hasdata);
		return hasdata;
	}
}
