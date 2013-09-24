package com.borqs.common.view;


import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.common.listener.CheckBoxClickActionListener;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.bpc.BpcFriendsFragmentActivity;

public class PickContactUserItemView extends SNSItemView 
{
	private final String TAG="PickContactUserItemView";
	
	private TextView  username;
	private CheckBox  checkbox;
	private ImageView portrait;
    private HashMap<String, CheckBoxClickActionListener> mCheckClickListenerMap;
	
	ContactSimpleInfo user;
	
	public PickContactUserItemView(Context context, ContactSimpleInfo di) {
		super(context);
		mContext = context;
		user = di;
		init();
	} 
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();
		init();
	}
	
	public String getName()
	{
		return user.display_name_primary;
	}	
	
	public boolean isSelected()
	{
		return user.selected;
	}
	
    private void init() {
        Log.d(TAG, "call SelectUserItemView init");
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        View view = factory.inflate(R.layout.user_select_list_item, null);
        view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,(int)mContext.getResources().getDimension(R.dimen.list_item_height)));
        addView(view);
 
        portrait = (ImageView) view.findViewById(R.id.user_icon);
        username = (TextView) view.findViewById(R.id.user_name);
        checkbox    = (CheckBox)view.findViewById(R.id.user_check);    
        checkbox.setOnClickListener(stOnClick);
        setUI();
    }

    public ContactSimpleInfo getContactSimpleInfo() {
        return user;
    }

	private void setUI()
	{
		Bitmap contactPhoto = null;
		portrait.setImageResource(R.drawable.default_user_icon);
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, user.mContactId);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(mContext.getContentResolver(), uri);
        contactPhoto = BitmapFactory.decodeStream(input);
        if (contactPhoto != null) {
            portrait.setImageBitmap(contactPhoto);
            contactPhoto = null;
        }
	    
		username.setText(user.display_name_primary);
		checkbox.setChecked(user.selected);
	}
	
	public void setUserItem(ContactSimpleInfo  di) 
	{
	    user = di;
	    setUI();
	}
	
	public void switchCheck()
	{
		Log.d(TAG, "onClick select before ="+user.selected );
	    user.selected = !user.selected;
	    checkbox.setChecked(user.selected);
		Log.d(TAG, "onClick select end ="+user.selected + " " + checkbox.isChecked());
	}
	
	public long getDataItemId() {
	    return user.mContactId;
	}

	@Override
	public String getText() 
	{		
		return user !=null?user.display_name_primary:"";
	}


    public void attachCheckListener(HashMap<String, CheckBoxClickActionListener> listenerMap) {
        mCheckClickListenerMap = listenerMap;
    }

	View.OnClickListener stOnClick = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			switchCheck();
            if (null != mCheckClickListenerMap) {
                Collection<CheckBoxClickActionListener> listeners = mCheckClickListenerMap.values();
                Iterator it = listeners.iterator();
                while (it.hasNext()) {
                    CheckBoxClickActionListener checkListener = (CheckBoxClickActionListener)it.next();
                    checkListener.changeItemSelect(user.mContactId, user.display_name_primary, user.selected, true);
                }
            }
		}
	};

}
