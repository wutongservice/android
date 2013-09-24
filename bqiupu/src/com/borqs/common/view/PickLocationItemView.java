package com.borqs.common.view;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.util.LocationUtils;
import com.borqs.qiupu.util.LocationUtils.HideLocationListener;

public class PickLocationItemView extends SNSItemView {

    private static final String TAG = "PickLocationItemView";

//    private String mLocation;
//    private String mLocationGeo;
    private LocationUtils.Geometry mGeometry;
    private boolean mIsFromLocation = false;

    public PickLocationItemView(Context context, LocationUtils.Geometry geometry, boolean fromLocation) {
        super(context);
        mContext = context;
        mGeometry = geometry;
        mIsFromLocation = fromLocation;
        init();
    }

    public void setGeometry(LocationUtils.Geometry geometry, boolean fromLocation) {
        mGeometry = geometry;
        mIsFromLocation = fromLocation;
        flushGeometry();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public String getLocationGeo() {
        return mGeometry.getGeoString();
    }

    public String getLocationURL() {
        return mGeometry.getLocationString();
    }

    public String getLocationName() {
        if (null == mGeometry) {
            return null;
        }

        final String location = mGeometry.getLocationString();
        if (TextUtils.isEmpty(location)) {
            return null;
        }

        return Html.fromHtml(location).toString();
    }

    private void init() {
        removeAllViews();
        View view = LayoutInflater.from(mContext).inflate(R.layout.show_location_item_layout, null);
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.list_item_height)));
        addView(view);

        flushGeometry();
    }

    private void flushGeometry() {
        ImageView iconView = (ImageView) findViewById(R.id.location_icon);
        TextView contentView = (TextView) findViewById(R.id.location_content);
        ImageView removeView = (ImageView) findViewById(R.id.remove_location);
        TextView detail = (TextView) findViewById(R.id.location_detail);
        TextView distance = (TextView) findViewById(R.id.distance);

        contentView.setText(getLocationName());

//        iconView.setImageDrawable(getResources().getDrawable(R.drawable.location_gray));
        if (TextUtils.isEmpty(mGeometry.icon)) {
            iconView.setImageDrawable(getResources().getDrawable(R.drawable.location_gray));
        } else {
            ImageRun imageRun = new ImageRun(null, mGeometry.icon, 0);
            if (!imageRun.setImageView(iconView)) {
                imageRun.post(null);
            }
        }

//        Log.d(TAG, "mGeometry.id = " + mGeometry.id + ", mGeometry.name = " + mGeometry.name);
        if (TextUtils.isEmpty(mGeometry.id) && mIsFromLocation == false) {
            detail.setVisibility(View.GONE);
            distance.setVisibility(View.GONE);
            iconView.setImageDrawable(getResources().getDrawable(R.drawable.location_blue));
            removeView.setVisibility(View.VISIBLE);
            removeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mHideLocationListener != null && mHideLocationListener.get() != null) {
                        mHideLocationListener.get().hideLocation(true);
                    }
                }
            });
        } else {
            if (!TextUtils.isEmpty(mGeometry.vicinity)) {
                if (mGeometry.isBaidu) {
                    distance.setText(String.format(mContext.getString(R.string.format_location_distance), mGeometry.distance));
                    distance.setVisibility(View.VISIBLE);
                }

                detail.setVisibility(View.VISIBLE);
                detail.setText(mGeometry.vicinity);
            }
        }

    }

    @Override
    public String getText() {
        return getLocationName();
    }

    private WeakReference<HideLocationListener> mHideLocationListener;
    public void attachHideLocationListener(WeakReference<HideLocationListener> hideLocationListener) {
        mHideLocationListener = hideLocationListener; 
    }
}
