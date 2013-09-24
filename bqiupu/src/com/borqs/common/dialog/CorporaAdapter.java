/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.common.dialog;


import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.SelectionItem;
import com.borqs.common.view.CorpusSelectionItemView;

public class CorporaAdapter extends BaseAdapter {

	private static final String TAG = "CorporaAdapter";
    private ArrayList<SelectionItem> items;
    private Context      mContext;
    private int mTextColor;
    
    public ArrayList<SelectionItem> getItems() {
		return items;
	}
    
	public void setItems(ArrayList<SelectionItem> items) {
		this.items = items;
	}

	public CorporaAdapter(Context context, ArrayList<SelectionItem> list){
		mContext = context;
		items = list;
	}
	
	public CorporaAdapter(Context context, ArrayList<SelectionItem> list, int textColor){
		mContext = context;
		items = list;
		mTextColor = textColor;
	}
	
	public CorporaAdapter(Context context)
	{
		mContext = context;
		items = new ArrayList<SelectionItem>();
	}
	public int getCount() {
		if(items == null){
			return 0;
		}else{
			return items.size();
		}
	}
	
	public SelectionItem getItem(int position) {
		if(items != null && position < items.size())
		{
			return items.get(position);
		}
		else
		{
			return null;
		}
	}
	
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;		
		SelectionItem item = getItem(position);
		if(item != null)
		{
			if (convertView == null || false == (convertView instanceof CorpusSelectionItemView) )
		    {
			    CorpusSelectionItemView view = new CorpusSelectionItemView(mContext, item, mTextColor);
				holder = new ViewHolder();
				holder.view =  view;	   
				view.setTag(holder);
				convertView = view;
				
		    } else {
		    	holder = (ViewHolder)convertView.getTag();	        
		    	holder.view.setSelectItem(item);
		    }
			return convertView;
		}
		else 
		{
			return null;
		}
	}
	
    
	static class ViewHolder
	{
		public CorpusSelectionItemView view;
	}
	
	public void alterDataList(ArrayList<SelectionItem> newList) {
		items.clear();
		
		if (null != newList) {
			items.addAll(newList);
		}
		notifyDataSetChanged();
	}
}
