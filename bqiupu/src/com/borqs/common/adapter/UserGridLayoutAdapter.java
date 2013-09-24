package com.borqs.common.adapter;

import java.util.ArrayList;

import twitter4j.UserImage;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;

public class UserGridLayoutAdapter extends BaseAdapter {

	private final Context mContext;
	private final int COUNT = 6;
    private final int mWidth;
    private ArrayList<UserImage> mLists;
    public UserGridLayoutAdapter(Context context,int width,ArrayList<UserImage> lists) {
        super();
        mContext = context;
        mWidth = width;
        mLists = lists;
    }

    @Override
    public int getCount() {
        return COUNT;
    }

    @Override
    public Object getItem(int position) {
        return mLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    class ViewHolder {
    	public ImageView img;
	}

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
    	ImageView imageView;
        if (convertView == null) { // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(new GridView.LayoutParams(mWidth/6, mWidth/6));
        } else {
            imageView = (ImageView) convertView;
        }
        if(position>=mLists.size()) {
        	imageView.setImageResource(R.drawable.default_user_icon);
        }else {
        	initImageUI(mLists.get(position).image_url, imageView);// Load image into ImageView
        }
        return imageView;
    }
    
    private void initImageUI(String image_url,ImageView imageView)
	{
		ImageRun imagerun = new ImageRun(null, image_url, 0);
		imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
		imagerun.noimage = true;
	    imagerun.addHostAndPath = true;
        imagerun.setImageView(imageView);    
        imagerun.post(null);
	}
}
