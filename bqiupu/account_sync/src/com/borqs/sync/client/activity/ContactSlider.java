/*
 * Copyright © 2012 Borqs Ltd.  All rights reserved.
 * This document is Borqs Confidential Proprietary and shall not be used, of published, or disclosed, or disseminated outside of Borqs 
 * in whole or in part without Borqs 's permission.
 */
package com.borqs.sync.client.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.borqs.contacts_plus.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 12/26/12
 * Time: 2:20 PM
 * Borqs project
 */
public class ContactSlider extends HorizontalScrollView implements View.OnClickListener {
//    private Loader mLoader;
    private List<BuddyData> mBuddyData;
    private LinearLayout mContainer;
//    private Runnable mCallback;

    public ContactSlider(Context context) {
        super(context);
        init();
    }

    public ContactSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ContactSlider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

//    public void loadInBackground(Runnable runnable){
//        if(mLoader != null){
//            return;
//        }
//        mCallback = runnable;
//        mLoader = new Loader();
//        mLoader.execute();
//    }

    private void init(){
        mBuddyData = new ArrayList<BuddyData>();
        mContainer = new LinearLayout(getContext());
        addView(mContainer, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void onDataChange(){
        mContainer.removeAllViews();

        if(mBuddyData.isEmpty())
            return;
        for(BuddyData data: mBuddyData){
            View buddyItem = createBuddyItem(data);
            mContainer.addView(buddyItem);
        }
    }

    private View createBuddyItem(BuddyData data) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View item = inflater.inflate(R.layout.contact_item_mini_thumbnail, null);
        item.setOnClickListener(this);
        item.setTag(data);
        setBuddyItem(item, data);
        return item;
    }

    private void setBuddyItem(View view, BuddyData data){
        if(data.mPhoto != null){
            ImageView photoView = (ImageView)view.findViewById(R.id.card_photo);
            photoView.setImageBitmap(data.mPhoto);
        }

        if(data.mName != null){
            TextView nameView = (TextView)view.findViewById(R.id.card_name);
            nameView.setText(data.mName);
        }
        
        ImageView typeView = (ImageView)view.findViewById(R.id.sync_type_icon);
        typeView.setVisibility(View.GONE);
        if(data.mType == BuddyData.TYPE_FROM_SERVER){
            typeView.setVisibility(View.VISIBLE);
            typeView.setImageResource(R.drawable.ic_server_change);
        }else if(data.mType == BuddyData.TYPE_FROM_LOCAL){
            typeView.setVisibility(View.VISIBLE);
            typeView.setImageResource(R.drawable.ic_local_change);
        }
    }

    @Override
    public void onClick(final View v) {
        final BuddyData data = (BuddyData)v.getTag();
        if (data == null){
            return;
        }
    }

    private void notifyDataChanged(){
        post(new Runnable() {
            @Override
            public void run() {
                onDataChange();
            }
        });
    }

    public void setContactData(List<BuddyData> unconnected){
      mBuddyData = unconnected;
      notifyDataChanged();
    }

    public static final class BuddyData {
        private String mBorqsId;
        private String mName;
        private Bitmap mPhoto;
        private int mType;

        public String getmBorqsId() {
            return mBorqsId;
        }

        public void setmBorqsId(String mBorqsId) {
            this.mBorqsId = mBorqsId;
        }

        public String getmName() {
            return mName;
        }

        public void setmName(String mName) {
            this.mName = mName;
        }

        public Bitmap getmPhoto() {
            return mPhoto;
        }

        public void setmPhoto(Bitmap mPhoto) {
            this.mPhoto = mPhoto;
        }

        public int getmType() {
            return mType;
        }

        public void setmType(int mType) {
            this.mType = mType;
        }

        public static final int TYPE_FROM_SERVER = 1;
        public static final int TYPE_FROM_LOCAL = 2;

    }
}
