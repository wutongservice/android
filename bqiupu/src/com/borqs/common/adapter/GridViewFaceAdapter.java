package com.borqs.common.adapter;

import com.borqs.common.util.SmileyParser;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GridViewFaceAdapter extends BaseAdapter {
    private Context mContext;
    private int[]   mImageIds;

    public GridViewFaceAdapter(Context c) {
        mContext = c;
        mImageIds = SmileyParser.Smileys.sIconIds;
    }

    public int getCount() {
        return mImageIds.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return mImageIds[position];
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mImageIds[position]);
        if (position < 65)
            imageView.setTag("[" + position + "]");
        else if (position < 100)
            imageView.setTag("[" + (position + 1) + "]");
        else
            imageView.setTag("[" + (position + 2) + "]");

        return imageView;
    }

}