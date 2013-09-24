package com.borqs.qiupu.util;

import java.util.ArrayList;
import java.util.HashMap;

import android.database.Cursor;
import android.util.Log;

import com.borqs.common.util.AlphaPost;
import com.borqs.qiupu.db.QiupuORM;


public class ATOZUtils {

    private static final String TAG = "ATOZUtils";
    private final static boolean debugsort = false;
  	
    public static void getAlphabetPos(ArrayList<String> namePinYin, ArrayList<AlphaPost> alphaPos, Cursor sortKey, String pinyinCol)
    {
        namePinYin.clear();
        alphaPos.clear();
        HashMap<String , Integer> pos = new HashMap<String , Integer>();	
        if(sortKey != null && sortKey.moveToFirst())
        {	
        	try{
        	    do{
        	        String sortStr = "";
        	        if(ContactUtils.isOPhone20())
        	        {
        	            sortStr = QiupuORM.getSortKey(sortKey.getString(sortKey.getColumnIndex(pinyinCol)));
        	        }
        	        else
        	        {
        	            sortStr = sortKey.getString(sortKey.getColumnIndex(pinyinCol));
        	        }
        	        
        	        sortStr = sortStr.toUpperCase();
        	        
        	        String alpha = sortStr.subSequence(0, 1).toString();
        	        
        	        if(debugsort)
        	            Log.d(TAG, "name_pinyin="+sortStr + " alpha="+alpha);
        	        
        	        namePinYin.add(alpha);
        	        
        	        if(pos.get(alpha) == null)
        	        {
        	            pos.put(alpha, sortKey.getPosition());
        	            if(debugsort)
        	                Log.d(TAG, "add name_pinyin="+sortStr + "                   alpha="+alpha + " position="+sortKey.getPosition());
        	            alphaPos.add(new AlphaPost(alpha, sortKey.getPosition()));
        	        }
        	    }
	            while(!sortKey.isClosed() && sortKey.moveToNext());
        	}catch(Exception ne){
        		Log.d(TAG, "message ="+ne.getMessage());
        		ne.printStackTrace();
        	}
            
            if(debugsort)
            {
                for(int i=0;i<alphaPos.size();i++)
                {	    		
                    Log.d(TAG, "alpha="+alphaPos.get(i).alpha + " pos="+alphaPos.get(i).pos);
                }
            }
        }
        
        pos.clear();
        pos = null;
        
        //Collections.sort(alphaPos);		
    }
}
