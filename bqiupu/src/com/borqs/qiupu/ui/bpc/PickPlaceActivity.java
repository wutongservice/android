package com.borqs.qiupu.ui.bpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.borqs.common.adapter.LocationAdapter;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.view.PickLocationItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.NoCachedImageRun;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.LocationUtils;
import com.borqs.qiupu.util.LocationUtils.Geometry;
import com.borqs.qiupu.util.LocationUtils.HideLocationListener;
import com.borqs.qiupu.util.ToastUtil;


public class PickPlaceActivity extends BasicActivity implements HideLocationListener {
    private static final String TAG = "PickPlaceActivity";

    // <a href='http://maps.google.com/maps?q=40.061502%2C116.622471'>北京市顺义区一经路</a>
//    private ArrayList<String> mLocations = new ArrayList<String>();
    // longitude=116.622471;latitude=40.061502;altitude=4.9E-324;speed=0.0;time=2013-02-10 23:06:44;geo=北京市顺义区一经路
//    private ArrayList<String> mGeos = new ArrayList<String>();
    private ArrayList<Geometry> mGeometries = new ArrayList<Geometry>();
    private LocationAdapter locationAdapter;

    private View mEmptyView;
    private ImageView mCurrentPlaceMap;

    /// demo data
    // see google map api: https://developers.google.com/maps/documentation/staticmaps/ for STATIC_MAP_URL
    private static final String STATIC_MAP_URL = "http://maps.googleapis.com/maps/api/staticmap?center=Brooklyn+Bridge,New+York,NY&zoom=13&size=600x300&maptype=roadmap" +
            "&markers=color:blue%7Clabel:S%7C40.702147,-74.015794&markers=color:green%7Clabel:G%7C40.711614,-74.012318" +
            "&markers=color:red%7Ccolor:red%7Clabel:C%7C40.718217,-73.998284&sensor=false&format=jpg";

    // see google place api: https://developers.google.com/places/documentation/search
    private static final String EXAMPLE_PLACE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&sensor=false&key=AIzaSyCRLa4LQZWNQBcjCYcIVYA45i9i8zfClqc";
    ///

    private Geometry mCurrentGeometry;
    private boolean mGeometryQuerying = false;
    private ProgressDialog mProgressDialog;
    private boolean mIsBaiduResult = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bpc_pick_location);
        setHeadTitle(R.string.share_my_location);

        mEmptyView = findViewById(R.id.empty_text);

        Intent intent = getIntent();
//        String location = LocationUtils.decodeCurrentLocation(intent);
        String geo = LocationUtils.decodeCurrentGeo(intent);
        setupLocationListener();
        begin();

        if (TextUtils.isEmpty(geo) /*mIsBaiduResult*/) {
            startQueryMyLocation();
            showProcessDialog(R.string.location_progress_bar_text, false, true, true);
        } else {
            mCurrentGeometry = Geometry.parseGeoString(geo);
            onGeometryReady();
        }

        showRightActionBtn(false);
    }

    private void onGeometryReady() {
        if (null != mCurrentGeometry) {
            initEnv();
            if (isConnected()) {
                new QueryPlaceList().execute(Geometry.getPlaceQueryUrl(mCurrentGeometry));
            }
        }
    }

    private ListView mListView;
    private void initEnv() {
        locationAdapter = new LocationAdapter(this, mGeometries, false);
        locationAdapter.setHideLocationListener(this);
        mListView = (ListView) findViewById(R.id.location_list);
        mListView.setVerticalScrollBarEnabled(false);
        mListView.addHeaderView(initListHeadView(mCurrentGeometry.getGeoString()));
        mListView.setAdapter(locationAdapter);
        mListView.setOnItemClickListener(mOnClickItemListener);
        mEmptyView.setVisibility(View.GONE);
        mGeometries.add(mCurrentGeometry);
    }

    private boolean isConnected() {
      if (!ToastUtil.testValidConnectivity(this)) {
          Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
          return false;
      }
      return true;
    }

    // query my location and show progress, when location is ready, 1. load static map,
    // 2. query nearby place list.
    private void startQueryMyLocation() {
        mGeometryQuerying = true;
    }

    private View initListHeadView(String geo) {
        if (null == mCurrentPlaceMap) {
            mCurrentPlaceMap = new ImageView(this);
            final int height = (int)getResources().getDimension(R.dimen.stream_big_image_dimension_device);
            mCurrentPlaceMap.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, height));
            mCurrentPlaceMap.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        final String mapUrl = Geometry.getStaticMapUrl(geo, QiupuConfig.IS_USE_BAIDU_LOCATION_API);
        Log.d(TAG, "static map url = " + mapUrl);
        ImageRun imageRunnable = new NoCachedImageRun(mapUrl);
        imageRunnable.default_image_index = QiupuConfig.DEFAUTL_RANDOM_LINK_INT_GREEN;
        imageRunnable.noimage = false;

        if (!imageRunnable.setImageView(mCurrentPlaceMap)) {
            imageRunnable.post(null);
        }

        return mCurrentPlaceMap;
    }

    private OnItemClickListener mOnClickItemListener = new OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            if (PickLocationItemView.class.isInstance(view)) {
                PickLocationItemView pv = (PickLocationItemView) view;
                String location = pv.getLocationURL();
                String geo = pv.getLocationGeo();

                Intent intent = new Intent();
                if (location != null) {
                    LocationUtils.encodeCurrentExtra(intent, location, geo);
                }
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationAdapter != null) {
            locationAdapter.setHideLocationListener(null);
        }
    }

    @Override
    protected void createHandler() {
        
    }

    @Override
    protected void loadSearch() {
        
    }

    private class QueryPlaceList extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return LocationUtils.downloadUrl(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            parseListResult(result);
        }
    }

    private void parseListResult(String response) {
        boolean modified = LocationUtils.parsePlaceListResult(response, mGeometries);
        if (modified) {
            Collections.sort(mGeometries, LocationUtils.Geometry.comparator);
            locationAdapter.notifyDataSetChanged();
            // should not happen
            mEmptyView.setVisibility(mGeometries.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
        	if (mListView != null) {
        		if (mGeometries != null && mGeometries.get(0) != null 
        				&& TextUtils.isEmpty(mGeometries.get(0).getLocationString())) {
        			mListView.setVisibility(View.GONE);
        		}
        	}
        }
        end();
    }

    @Override
    protected void uiLoadEnd() {
        showProgressBtn(false);
        showLeftActionBtn(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGeometryQuerying) {
            LocationUtils.activateLocationService(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopLocationQuerying();
    }

    @Override
    protected void updatePoi(final String poiJson) {
//        Log.d(TAG, "updatePoi() poiJson = " + poiJson);
        stopLocationQuerying();
        parseListResult(poiJson);
    }

    @Override
    protected void getPoiFailed() {
        Log.d(TAG, "getPoiFailed() mCurrentGeometry = " + mCurrentGeometry);
        if (mCurrentGeometry != null) {
            if (isConnected()) {
                new QueryPlaceList().execute(Geometry.getPlaceQueryUrl(mCurrentGeometry));
            } else {
                end();
            }
        } else {
            end();
        }
    }

    @Override
    protected void getLocationSucceed(String locString) {
        Log.d(TAG, "============= getLocationSucceed() locString = " + locString);
        if (mGeometryQuerying && !TextUtils.isEmpty(locString)) {
            dismissProcessDialog();
            mCurrentGeometry = Geometry.parseGeoString(locString);

            if (QiupuConfig.IS_USE_BAIDU_LOCATION_API) {
                initEnv();
                LocationUtils.requestPoi(getApplicationContext());
            } else {
                onGeometryReady();
                stopLocationQuerying();
            }

//            onGeometryReady(QiupuConfig.IS_USE_BAIDU_LOCATION_API);
//            stopLocationQuerying();
        }
    }

    private void stopLocationQuerying() {
        if (mGeometryQuerying) {
            mGeometryQuerying = false;
            LocationUtils.deactivateLocationService(this);
            QiupuConfig.setDefaultLocationApi(true);
        }
    }

    @Override
    public void hideLocation(boolean hide) {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable){
        mProgressDialog = DialogUtils.createProgressDialog(this, 
                resId, CanceledOnTouchOutside, Indeterminate, cancelable);
        mProgressDialog.setInverseBackgroundForced(true);
        mProgressDialog.show();
    }

    private void dismissProcessDialog() {
        try {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        } catch (Exception e) {}
    }
}
