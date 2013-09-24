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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.qiupu.R;

public class AllPeoplePickItemView extends SNSItemView {
    private final String TAG = "AllPeoplePickItemView";

    private TextView username;
    private CheckBox checkbox;
    private ImageView portrait;

    ContactSimpleInfo user;

    public AllPeoplePickItemView(Context context, ContactSimpleInfo di) {
        super(context);
        mContext = context;
        user = di;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    public String getName() {
        return user.display_name_primary;
    }

    public boolean isSelected() {
        return user.selected;
    }

    public long getBorqsId() {
        return user.mBorqsId;
    }
    
    public long getDataItemId() {
        return user.mContactId;
    }
    
    public ContactSimpleInfo getUser() {
        return user;
    }

    private void init() {
        Log.d(TAG, "call SelectUserItemView init");
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        View view = factory.inflate(R.layout.user_select_list_item, null);
        view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));
        addView(view);

        portrait = (ImageView) view.findViewById(R.id.user_icon);
        username = (TextView) view.findViewById(R.id.user_name);
        checkbox = (CheckBox) view.findViewById(R.id.user_check);
        checkbox.setOnClickListener(stOnClick);
        setUI();
    }

    public ContactSimpleInfo getContactSimpleInfo() {
        return user;
    }

    private void setUI() {
        Bitmap contactPhoto = null;
        portrait.setImageResource(R.drawable.default_user_icon);
        Uri uri = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI, user.mContactId);
        InputStream input = ContactsContract.Contacts
                .openContactPhotoInputStream(mContext.getContentResolver(), uri);
        contactPhoto = BitmapFactory.decodeStream(input);
        if (contactPhoto != null) {
            portrait.setImageBitmap(contactPhoto);
            contactPhoto = null;
        }

        setPortraitV();

        username.setText(user.display_name_primary);
        checkbox.setChecked(user.selected);
    }

    private void setPortraitV() {
        ImageView view = (ImageView) findViewById(R.id.portrait_v);
        if (view != null) {
            if (user.mBorqsId > 0) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }

    public void setUserItem(ContactSimpleInfo di) {
        user = di;
        setUI();
    }

    public void switchCheck() {
        Log.d(TAG, "onClick select before =" + user.selected);
        user.selected = !user.selected;
        checkbox.setChecked(user.selected);
        Log.d(TAG,
                "onClick select end =" + user.selected + " "
                        + checkbox.isChecked());
    }


    View.OnClickListener stOnClick = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            switchCheck();
            changeActivitySelected();
        }
    };
    
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
    
    @Override
    public String getText() {
        return user != null ? user.display_name_primary : "";
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
}
