package com.borqs.common;

import android.text.TextUtils;
import android.util.Log;

public class SelectionItem implements java.io.Serializable {
	private static final long serialVersionUID = 8020166132262934739L;
	 public String id;
     public String value;
     
     public boolean isSelected;

     public SelectionItem () {
         id = "";
         value = "";
     }

     public SelectionItem (String itemId, String itemValue) {
         id = itemId;
         value = itemValue;
     }
     
     public SelectionItem (String itemId, String itemValue, String selectItem) {
         id = itemId;
         value = itemValue;
         if(!TextUtils.isEmpty(itemId) && !TextUtils.isEmpty(selectItem) && itemId.equals(selectItem)) {
        	 isSelected = true;
         }else {
        	 isSelected = false;
         }
     }
     
     public String getId() {
         return id;
     }
     public String getValue() {
         return value;
     }
}
