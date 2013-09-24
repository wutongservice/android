package com.borqs.common.adapter;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.common.view.ContactUserSelectItemView;
import com.borqs.qiupu.ui.InviteContactActivity;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Adapter that combine an ArrayList and a Cursor within a ListView, which
 * allow to use as a ArrayList adapter, or a Cursor adapter, or event both.
 */
public class ContactUserAdapter extends BaseAdapter{
	private static final String TAG = "Qiupu.ContactUserAdapter";
    private ArrayList<ContactSimpleInfo> items;
    private Cursor       mCursor;
    private Context      mContext;
    private int          mBindId;
    private  HashSet<String> mSelectedSet;
    private boolean      mSelectedAll;
    private boolean mIsPickContact;

    private void resetData(ArrayList<ContactSimpleInfo> list, Cursor cursor,
                           HashSet<String> selectedSet, int bindId) {
        if (null == list) {
            if (null == items) {
                items = new ArrayList<ContactSimpleInfo>();
            } else {
                items.clear();
            }
        } else {
            items = list;
        }

        mCursor = cursor;
        mBindId = bindId;
        mSelectedSet = selectedSet;
        mSelectedAll = false;
    }

    public ContactUserAdapter(Context context, boolean isPickContact){
		mContext = context;
		mIsPickContact = isPickContact;
        resetData(null, null, null, -1);
	}

    public ContactUserAdapter(Context context, ArrayList<ContactSimpleInfo> list, Cursor cursor){
		mContext = context;
        resetData(list, cursor, null, -1);
	}

	public int getCount() {
		int count = null == items ? 0 : items.size();
		count += null == mCursor ? 0 : mCursor.getCount();
        return count;
	}
	
	public ContactSimpleInfo getItem(int position) {
		final int arraySize = null == items ? 0 : items.size();
		final int cursorSize = null == mCursor ? 0 : mCursor.getCount(); 
        final ContactSimpleInfo info;
		if(position < arraySize) {
            info = items.get(position);
	    } else if (position < arraySize + cursorSize) {
            if (InviteContactActivity.class.isInstance(mContext)) {
                info = ((InviteContactActivity)mContext).parserCursor(mCursor, mBindId, position);
            } else {
                Log.i(TAG, "getItem, no implement for InviteContactActivity item.");
                info = null;
            }
//            info = null;
//            info.mBindId = mBindId;
//            info.mBindPosition = position;
        } else {
            info = null;
        }

        postSetupContactInfo(info);

        return info;
	}
	
	public long getItemId(int position) {
		return position;
	}

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        ContactSimpleInfo di = getItem(position);
        if (di != null) {
            ContactUserSelectItemView v;
            if (convertView == null || false == (convertView instanceof ContactUserSelectItemView)) {
                holder = new ViewHolder();
                v = new ContactUserSelectItemView(mContext, di, mIsPickContact);
                holder.view = v;
                v.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                v = holder.view;
                v.setUserItem(di);
            }
            return v;
        } else {
//            Button but = new Button(mContext);
//            but.setTextAppearance(mContext, R.style.sns_load_old);
//            but.setBackgroundColor(Color.WHITE);
//            but.setText(mContext.getString(R.string.list_view_more));
//            if (RequestContactActivity.class.isInstance(mContext)) {
//                RequestContactActivity fs = (RequestContactActivity) mContext;
//                but.setOnClickListener(fs.loadOlderClick);
//                if (fs.isInProcess()) {
//                    but.setText(mContext.getString(R.string.loading));
//                }
//            }
//            return but;
            Log.d(TAG, "getView, get no item for position:" + position + ", bindId" + mBindId);
            return convertView;
        }
    }

    static class ViewHolder {
        public ContactUserSelectItemView view;
    }

    public void swapToCursor(Cursor cursor, HashSet<String> selectedSet, int bindId) {
        resetData(null, cursor, selectedSet, bindId);
        notifyDataSetChanged();
    }

//    public void swapToList(ArrayList<ContactSimpleInfo> list, int bindId) {
//        mCursor = null;
//
//        items.clear();
//        items.addAll(list);
//        mBindId = bindId;
//
//        notifyDataSetChanged();
//    }


    public void setSelectAll(boolean selected) {
        mSelectedAll = selected;
    }

    private void postSetupContactInfo(ContactSimpleInfo info) {
        if (null != info) {
            info.mBindId = mBindId;
//            info.mBindPosition = position;
            if (mSelectedAll) {
                info.selected = true;
            } else if (null != mSelectedSet) {
                if (mSelectedSet.contains(info.email) || mSelectedSet.contains(info.phone_number)){
                    info.selected = true;
                }
            }
        }
    }
}
