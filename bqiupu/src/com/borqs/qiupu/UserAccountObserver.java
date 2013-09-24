package com.borqs.qiupu;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.borqs.account.service.BorqsAccount;

public class UserAccountObserver {
    static final private HashMap<String,WeakReference<AccountListener>> listeners = new HashMap<String,WeakReference<AccountListener>>();
    
    public static void registerAccountListener(String key,AccountListener listener){
    	
    	 synchronized(listeners)
         {
    		 WeakReference<AccountListener> ref = listeners.get(key);
    		 if(ref != null && ref.get() != null)
    		 {
    			 ref.clear();
    		 }
    		 listeners.put(key,new WeakReference<AccountListener>(listener));
         }
    }
    
    public static void unregisterAccountListener(String key){
    	 synchronized(listeners)
         {
    		 WeakReference<AccountListener> ref = listeners.get(key);
    		 if(ref != null && ref.get() != null)
    		 {
    			 ref.clear();
    		 }
    		 listeners.remove(key);
         }
    }
    
    public static void login(){
        synchronized(listeners)
        {
            Set<String> set = listeners.keySet();
            Iterator<String> it = set.iterator();
            while(it.hasNext())
            {
                String key = it.next();
                if(listeners.get(key) != null)
                {
	                AccountListener listener = listeners.get(key).get();
	                if(listener != null)
	                {
	                    listener.onLogin();
	                }
                }
            }      
        }      
    }
    
	public static void logout(){
	        synchronized(listeners)
	        {
	            Set<String> set = listeners.keySet();
	            Iterator<String> it = set.iterator();
	            while(it.hasNext())
	            {
	                String key = it.next();
	                WeakReference<AccountListener> ref = listeners.get(key);
	                if(ref != null && ref.get() != null)
	                {
    	                AccountListener listener = ref.get();
    	                if(listener != null)
    	                {
    	                    listener.onLogout();
    	                }
	                }
	            }
	            
	        }
   }

	public static void cancelLogin() {
		synchronized(listeners)
        {
            Set<String> set = listeners.keySet();
            Iterator<String> it = set.iterator();
            while(it.hasNext())
            {
                String key = it.next();
                WeakReference<AccountListener> ref = listeners.get(key);
                if(ref != null && ref.get() != null)
                {
                    AccountListener listener = ref.get();
                    if(listener != null)                
                    {
                        listener.onCancelLogin();
                    }
                }
            }
        }
		
	}
}
