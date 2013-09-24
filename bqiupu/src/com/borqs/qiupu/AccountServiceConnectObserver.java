package com.borqs.qiupu;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.borqs.account.service.BorqsAccount;

public class AccountServiceConnectObserver {
    static final private HashMap<String,WeakReference<AccountServiceConnectListener>> listeners = new HashMap<String, WeakReference<AccountServiceConnectListener>>();
    
    
    public static void registerAccountServiceConnectListener(String key,AccountServiceConnectListener listener){    	
   	    synchronized(listeners)
        {
	   	     WeakReference<AccountServiceConnectListener> ref = listeners.get(key);
			 if(ref != null && ref.get() != null)
			 {
				 ref.clear();
			 }
   		     listeners.put(key, new WeakReference<AccountServiceConnectListener>(listener));
        }
   }
    
    public static void unregisterAccountServiceConnectListener(String key){
    	 synchronized(listeners)
         {
    		 WeakReference<AccountServiceConnectListener> ref = listeners.get(key);
    		 if(ref != null && ref.get() != null)
    		 {
    			 ref.clear();
    		 }
    		 
    		 listeners.remove(key);
         }
    }
    
    public static void onAccountServiceDisconnected(){
        synchronized(listeners)
        {
            Set<String> set = listeners.keySet();
            Iterator<String> it = set.iterator();
            while(it.hasNext())
            {
                String key = it.next();
                AccountServiceConnectListener listener = listeners.get(key).get();
                if(listener != null)
                {
                    listener.onAccountServiceDisconnected();
                }
            }      
        }      
    }
    
	public static void onAccountServiceConnected(){
	        synchronized(listeners)
	        {
	            Set<String> set = listeners.keySet();
	            Iterator<String> it = set.iterator();
	            while(it.hasNext())
	            {
	                String key = it.next();
	                AccountServiceConnectListener listener = listeners.get(key).get();
	                if(listener != null)
	                {
	                    listener.onAccountServiceConnected();
	                }
	            }
	            
	        }
   }
}
