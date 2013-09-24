package com.borqs.account.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class AccountObserver {
    static final private HashMap<String,AccountListener> listeners = new HashMap<String,AccountListener>();
    
    public static void registerAccountListener(String key,AccountListener listener){
    	
    	 synchronized(listeners)
         {
    		 listeners.put(key,listener);
         }
    }
    
    public static void unregisterAccountListener(String key){
    	 synchronized(listeners)
         {
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
                AccountListener listener = listeners.get(key);
                if(listener != null)
                {
                    listener.onLogin();
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
	                AccountListener listener = listeners.get(key);
	                if(listener != null)
	                {
	                    listener.onLogout();
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
                AccountListener listener = (AccountListener)listeners.get(key);
                if(listener != null)
                {
                    listener.onCancelLogin();
                }
            }
        }
		
	}
}
