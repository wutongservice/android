package com.borqs.common.view;


import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.common.view.AllPeoplePickItemView.AllPeoplecheckItemListener;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.InviteContactActivity;

public class ContactUserSelectItemView extends SNSItemView 
{
	private final String TAG="UserSelectItem";
	
	private TextView  username;
	private TextView  phonenumber;
	private CheckBox  chekbox;
	private ImageView portrait;
	
	ContactSimpleInfo user;
	private boolean mIsPickContact;
	private static int LIST_ITEM_HEIGHT = 0;
	
	public ContactUserSelectItemView(Context context, ContactSimpleInfo di, boolean IsPickContact) {
		super(context);
		mContext = context;
		user = di;
		mIsPickContact = IsPickContact;
		Log.d(TAG, "call UserSelectItem");
		LIST_ITEM_HEIGHT = (int) getResources().getDimension(R.dimen.list_item_height);
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
        View view;
        if (mIsPickContact) {
            view = factory.inflate(R.layout.show_contact_item_layout, null);
            view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LIST_ITEM_HEIGHT));
            addView(view);
            portrait = (ImageView) view.findViewById(R.id.head_portrait);
        } else {
            // child 1
            view = factory.inflate(R.layout.contact_user_select_list_item, null);
            view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            addView(view);

            chekbox = (CheckBox) view.findViewById(R.id.user_check);
            chekbox.setOnClickListener(stOnClik);
        }

        username = (TextView) view.findViewById(R.id.contact_user_name);
        phonenumber = (TextView) view.findViewById(R.id.contact_user_phone_number);
        setUI();
    }

    public ContactSimpleInfo getContactSimpleInfo() {
        return user;
    }

	private void setUI()
	{
		final boolean isUserNameEmpty = TextUtils.isEmpty(user.display_name_primary);
		if(user.type == ContactSimpleInfo.CONTACT_INFO_TYPE_EMAIL)
		{
			phonenumber.setText(user.email);
			username.setText(isUserNameEmpty ? user.email: user.display_name_primary);	
		}
		else if(user.type == ContactSimpleInfo.CONTACT_INFO_TYPE_PHONE)
		{
		    if (mIsPickContact) {
		        Bitmap contactPhoto = null;
		        if (user.mPhotoId > 0) {
		            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, user.mContactId);
		            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(mContext.getContentResolver(), uri);
		            contactPhoto = BitmapFactory.decodeStream(input);
		        } else {
		            contactPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.default_user_icon);
		        }

		        portrait.setImageBitmap(contactPhoto);
		        if (contactPhoto != null) {
		            contactPhoto = null;
		        }
		    }
			phonenumber.setText(user.phone_number);
			username.setText(isUserNameEmpty ? user.phone_number : user.display_name_primary);
		} else {
			Log.e(TAG, "setUI, unexpected user type: " + user.type);
		}
		if (chekbox != null) {
		    chekbox.setChecked(user.selected);
		}
	}
	
	public void setUserItem(ContactSimpleInfo  di) 
	{
	    user = di;
	    setUI();
	}
	
	public void switchCheck()
	{
		selectUser(!user.selected);

        if(InviteContactActivity.class.isInstance(mContext)) {
            InviteContactActivity re = (InviteContactActivity) mContext;
//				re.changeSelect();
            Log.d(TAG, "switchCheck " + user.mBindId + " " + phonenumber.getText().toString()+" " + isSelected());
            re.changeSelect(user.mBindId, phonenumber.getText().toString(), isSelected());
        }
		Log.d(TAG, "onClick select ="+user.selected);
		changeActivitySelected();
	}
	
	View.OnClickListener stOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			switchCheck();

		}
	};
	
	@Override
	public String getText() 
	{		
		return user !=null?user.display_name_primary:"";
	}

    public void selectUser(boolean flag) {
        user.selected = flag;
        chekbox.setChecked(user.selected);
    }
    
    public void changeActivitySelected() {

        synchronized (listeners) {
            Set<String> set = listeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                AllPeoplecheckItemListener listener = listeners.get(key).get();
                if (listener != null) {
                    listener.selectItem(user);
                }
            }
        }
    }
    
    
    public interface AllPeoplecheckItemListener {
        public void selectItem(ContactSimpleInfo user);
    }
    
    public static final HashMap<String,WeakReference<AllPeoplecheckItemListener>> listeners = new HashMap<String,WeakReference<AllPeoplecheckItemListener>>();
    
    public static void registerAllPeopleCheckItemListener(String key,AllPeoplecheckItemListener listener){
        synchronized(listeners) {
            WeakReference<AllPeoplecheckItemListener> ref = listeners.get(key);
            if(ref != null && ref.get() != null) {
                ref.clear();
            }
            listeners.put(key, new WeakReference<AllPeoplecheckItemListener>(listener));
        }
    }
    
    public static void unRegisterAllPeopleCheckItemListener(String key){
        synchronized(listeners) {
            WeakReference<AllPeoplecheckItemListener> ref = listeners.get(key);
            if(ref != null && ref.get() != null) {
                ref.clear();
            }
            listeners.remove(key);
        }
    }
    
    public boolean refreshCheckItem(String phoneEmail, boolean isSelected) {
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "refreshItem: " + phoneEmail + " " + user.type);
    	if(user.type == ContactSimpleInfo.CONTACT_INFO_TYPE_EMAIL) {
    		if(user.email != null && user.email.equals(phoneEmail)) {
    			selectUser(isSelected);
    			return true;
    		}
		}
		else if(user.type == ContactSimpleInfo.CONTACT_INFO_TYPE_PHONE) {
			if(user.phone_number != null && user.phone_number.equals(phoneEmail)) {
    			selectUser(isSelected);
    			return true;
    		}
		}
    	return false;
    }
}
