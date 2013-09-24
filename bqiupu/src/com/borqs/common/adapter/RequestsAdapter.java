package com.borqs.common.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.Requests;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.borqs.common.listener.RequestActionListner;
import com.borqs.common.view.RequestItemView;
import com.borqs.qiupu.R;

public class RequestsAdapter extends BaseAdapter{
	private static final String TAG = "RequestsAdapter";
	private ArrayList<Requests> mRequestsItems = new ArrayList<Requests>();
    private Context mContext;
    private RequestActionListner mListener;
    
//    private HashMap<String, RequestActionListner> mRequestActionListenerMap = new HashMap<String, RequestActionListner>();
//    public void registerRequestActionListener(String key, RequestActionListner rl) {
//    	mRequestActionListenerMap.put(key, rl);
//    }
//
//    public void unRegisterRequestActionListener(String key) {
//    	mRequestActionListenerMap.remove(key);
//    }
    
    public void setRequestActionListener(RequestActionListner listener) {
    	mListener = listener;
    }
    
	public RequestsAdapter(Context context,ArrayList<Requests> itemList){
		mContext = context;
		mRequestsItems.clear();
		mRequestsItems.addAll(itemList);
	}
	
	public RequestsAdapter(Context context){
        mContext = context;
    }
	
	public void alterRequests(ArrayList<Requests> itemList){
		mRequestsItems.clear();
		mRequestsItems.addAll(itemList);
		notifyDataSetChanged();
	}
	
	public int getCount() {
	    int count = 0;
        if(mRequestsItems.size() > 0){
            count = mRequestsItems.size();
        }
        return count;
	}
	
	public Requests getItem(int position) {
		if(position < mRequestsItems.size()) {
			return mRequestsItems.get(position);
        }
        else{
            return null;
        }
	}
	
	public long getItemId(int position) {
		Requests request =  getItem(position);
		return request !=null ? request.type : -1;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;		
		Requests request = getItem(position);
		if(request != null) {
			if (convertView == null || false == (convertView instanceof RequestItemView) ) {
			    RequestItemView  rView = new RequestItemView(mContext, request);
		        holder = new ViewHolder();
		        rView.setTag(holder);
		        holder.view = rView;
		        convertView = rView;
//		        rView.attachRequestListener(mRequestActionListenerMap);
		        rView.setRequestListener(mListener);
		        
		    } else {
		        holder = (ViewHolder)convertView.getTag();
		        holder.view.setRequest(request);
		        holder.view.setRequestListener(mListener);
//		        holder.view.attachRequestListener(mRequestActionListenerMap);
		    }
		} else {
			Log.d(TAG, "request is null");
		}
		return convertView;
	}

	private TextView generateSpanItem() {
	    TextView but = (TextView)(((Activity) mContext).getLayoutInflater().inflate(R.layout.a_2_z_textview, null));
        but.setTextColor(mContext.getResources().getColor(R.color.atoz_font));
        but.setOnClickListener(null);
        return but;
    }

	static class ViewHolder
	{
		public RequestItemView view;
	}
}
