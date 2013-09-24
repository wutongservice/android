package com.borqs.common.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.borqs.common.view.InformationItemView;
import com.borqs.information.InformationBase;
import com.borqs.information.db.NotificationOperator;
import com.borqs.qiupu.R;

public class InformationAdapter extends BaseAdapter{
	private static final String TAG = "InformationAdapter";
	private Cursor items;
    private Context      mContext;
    
    private MoreItemCheckListener mCheckerListener;

    public interface MoreItemCheckListener {
        public boolean isMoreItemHidden();
        public View.OnClickListener getMoreItemClickListener();
        public int getMoreItemCaptionId();
    }
    
    public InformationAdapter(Context context) {
        mContext = context;
    }
    
    public InformationAdapter(Context context, MoreItemCheckListener checkListener) {
        mContext = context;
        mCheckerListener = checkListener;
    }

    public int getCount() {
    	if (items == null) {
    		return 0;
    	} else {
    		if(null != mCheckerListener) {
    			if(mCheckerListener.isMoreItemHidden()) {
    				return items.getCount();
    			}else {
    				return items.getCount() + 1;
    			}
    		}else {
    			return items.getCount();
    		}
    	}
    }
    
    public InformationBase getItem(int position) {	
    	if(items != null ) {
    		if(position >= 0 && items.moveToPosition(position)) {
    			return NotificationOperator.createInformation(items);
    		}
    		else {
    			return null;
    		}
    	} else {
    		return null;
    	}
    }
    
    public long getItemId(int position) {		
        return -1;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;		
        InformationBase information = getItem(position);
        if(information != null) {
            InformationItemView FView;
            if (convertView == null || false == (convertView instanceof InformationItemView) ) {
                FView = new InformationItemView(mContext, information);
                holder = new ViewHolder();
                
                FView.setTag(holder);
                holder.view = FView;
                convertView = FView;
            } else {
                holder = (ViewHolder)convertView.getTag();
                holder.view.setInformation(information);
            }
        }else {
        	return generateRefreshItem();
        }
        return convertView;
    }

    private Button generateMoreItem() {
        Button but = (Button) (((Activity) mContext).getLayoutInflater().inflate(R.layout.more_button_stream, null));
        but.setHeight((int) mContext.getResources().getDimension(R.dimen.more_button_height));
        but.setTextColor(mContext.getResources().getColor(R.color.more_text_color));
        but.setBackgroundResource(R.drawable.list_selector_background);
        return but;
    }

    private View generateRefreshItem() {
        if (null != mCheckerListener) {
            Button but = generateMoreItem();
            but.setOnClickListener(mCheckerListener.getMoreItemClickListener());
            but.setText(mCheckerListener.getMoreItemCaptionId());
            return but;
        }
        return null;
    }

    
    static class ViewHolder {
        public InformationItemView view;
    }
    
    
    public void alterDataList(Cursor cursor) {
    	if(items != null ){
    		items.close();
    	}
    	items = cursor; 
        notifyDataSetChanged();
    }
    
    public void closeCursor() {
    	if(items != null) {
    		items.close();
    		items = null;
    	}
    }
}
