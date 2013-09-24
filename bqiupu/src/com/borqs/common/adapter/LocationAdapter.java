package com.borqs.common.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.common.view.PickLocationItemView;
import com.borqs.common.view.SNSItemView;
import com.borqs.qiupu.util.LocationUtils;
import com.borqs.qiupu.util.LocationUtils.Geometry;
import com.borqs.qiupu.util.LocationUtils.HideLocationListener;

import org.json.JSONObject;

public class LocationAdapter extends BaseAdapter {
//    private static final String TAG = "Qiupu.LocationAdapter";

    

    private Context mContext;
//    protected boolean forappshareview = false;
//    private List<String> mLocations = new ArrayList<String>();
//    private List<String> mLocationGeos = new ArrayList<String>();
    private List<Geometry> mGeometries = new ArrayList<Geometry>();
    private boolean mIsFromLocation = false;

    public LocationAdapter(Context context, ArrayList<Geometry> geometries, boolean fromLocation) {
        mContext = context;
        mGeometries = geometries;
        mIsFromLocation = fromLocation;
    }

    public int getCount() {
        if (null == mGeometries) {
            return 0;
        } else {
            return mGeometries.size();
        }
    }

    public Geometry getItem(int pos) {
        if (pos >= mGeometries.size()) {
            return null;
        } else {
            return mGeometries.get(pos);
        }
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final Geometry geometry = getItem(position);

        if (null != convertView && false == (convertView instanceof PickLocationItemView)) {
            holder = (ViewHolder)convertView.getTag();
            ((PickLocationItemView)holder.view).setGeometry(geometry, mIsFromLocation);
            ((PickLocationItemView)holder.view).attachHideLocationListener(mHideLocationListener);
            return holder.view;
        } else {
            PickLocationItemView view = new PickLocationItemView(mContext, geometry, mIsFromLocation);
            view.attachHideLocationListener(mHideLocationListener);
            holder = new ViewHolder();
            holder.view = view;
            view.setTag(holder);
            return holder.view;
        }

    }

    static class ViewHolder {
        public SNSItemView view;
    }

    private WeakReference<HideLocationListener> mHideLocationListener;
    public void setHideLocationListener(HideLocationListener listener) {
        mHideLocationListener = new WeakReference<HideLocationListener>(listener);
    }
}
