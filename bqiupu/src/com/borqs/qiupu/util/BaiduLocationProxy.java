package com.borqs.qiupu.util;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.internal.http.HttpClientImpl;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.net.Uri.Builder;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.borqs.account.service.LocationRequest;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;

/**
 *   this is a proxy to use Baidu location service, and you can follow below steps:
 *   1. call initBaiduLocationEnv() in your application such as QiupuApplication.
 *   2. do something in BaiduLocationListener's onReceiveLocation(BDLocation location) method.
 *   3. call setLocationListener() in your activity.
 *   4. implement LocationListener's callback method to update UI.
 *
 */
public class BaiduLocationProxy {
    private static final String TAG = "BaiduLocationProxy";

    private static BaiduLocationProxy mBaiduLocationProxy;
    private WeakReference<LocationListener> mBDLocationlistener;
    public static LocationClient mLocationClient = null;
    private Context mContext;
    private static final int UPDATE_TIME = 5000;
    private static final double INIT_LATITUDE = 4.9E-324;
 
    /**
     *返回国测局经纬度坐标系 coor=gcj02
      返回百度墨卡托坐标系 coor=bd09
      返回百度经纬度坐标系 coor=bd09ll 
     */
    private static final String STATE_LAT_LON_COORDINATE  = "gcj02";
    private static final String BAIDU_MERCATOR_COORDINATE = "bd09";
    private static final String BAIDU_LAT_LON_COORDINATE  = "bd09ll";

    private BaiduLocationProxy(Context context) {
        mContext = context;
    }

    public static BaiduLocationProxy getInstance(Context context) {
        if (mBaiduLocationProxy == null) {
            mBaiduLocationProxy = new BaiduLocationProxy(context);
        }
        return mBaiduLocationProxy;
    }

    /**
     * initial environment in application, such as QiupuApplication
     * @param context
     */
    public void initBaiduLocationEnv(Context context) {
        getLocationClient(context);
        mLocationClient.registerLocationListener(newBaiduLocationListenner());
    }

    public LocationClient getLocationClient(Context context) {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(context);
            mLocationClient.setLocOption(buildLocationClientOption(false));
        }
        return mLocationClient;
    }

    private LocationClientOption buildLocationClientOption(boolean needReTry) {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType(BAIDU_LAT_LON_COORDINATE);
        option.setPriority(LocationClientOption.NetWorkFirst);
//        if (needReTry) {
//            option.setScanSpan(UPDATE_TIME);
//        }
//        option.setProdName("Wutong");
//        option.setServiceName("com.baidu.location.service_v2.9");
        option.disableCache(true);
        option.setAddrType("all");// v3.1 add new api
        option.setPoiNumber(20); // default value is 3 and max value is 10
        option.setPoiDistance(500); // default is 500m, it won't return data if the distance is more than 2000m 
        option.setPoiExtraInfo(true);
        return option;
    }

    /**
     * start baidu location service
     */
    public void start() {
        if (mLocationClient != null) {
            mLocationClient.start();
            if (mLocationClient.isStarted()) {
                int result = mLocationClient.requestLocation();
                Log.d(TAG, "result = " + result);
            }
        }
    }

    /**
     * stop baidu location service
     */
    public void stop() {
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
    }

    public interface LocationListener {
        public void updateLocationWithBaiduApi(final BDLocation location, final String address, String locString);
        public void onLocationSucceed(String locString);
        public void onLocationFailed(int errorType);
        public void updatePoiWithBaiduApi(String poiJson);
        public void onPoiFailed();
    }

    public void setLocationListener(LocationListener loc) {
        if(loc == null) {
            if(mBDLocationlistener != null) {
                mBDLocationlistener.clear();
            }
            mBDLocationlistener = null;
        } else {
            if(mBDLocationlistener != null) {
                mBDLocationlistener.clear();
            }
            mBDLocationlistener = new WeakReference<LocationListener>(loc);
        }
    }

    public WeakReference<LocationListener> getLocationListener() {
        return mBDLocationlistener;
    }

    public BaiduLocationListenner newBaiduLocationListenner() {
        return new BaiduLocationListenner();
    }

    /**
     * 1. location SDK needs the application to guarantee that the network connection unobstructed
     * 2. location SDK calls must be in the main thread
     * 3. We strongly recommend that you set up their own prodName, and keeping good, 
     *     so that it is convenient for us to provide you with better location service
     */
    public class BaiduLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            if (location == null) {
                return;
            }
            tranformLocation(location, mContext);
        }

        @Override
        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null) {
                return;
            }
            if(poiLocation.hasPoi()){
                String poiJson = poiLocation.getPoi();
//                Log.d(TAG, "poiJson = " + poiJson);

                if (mBDLocationlistener != null && mBDLocationlistener.get() != null) {
                    mBDLocationlistener.get().updatePoiWithBaiduApi(poiJson);
                }

            } else {
                handleGetPoiFailedCase();
            }
        }
    }

    private void handleGetPoiFailedCase() {
        Log.d(TAG, "handleGetPoiFailedCase(), noPoi information");
        if (mBDLocationlistener != null && mBDLocationlistener.get() != null) {
            mBDLocationlistener.get().onPoiFailed();
        }
    }

    public void requestPoiInformation() {
        if (mLocationClient != null && mLocationClient.isStarted()) {
            int requestPoiResult = mLocationClient.requestPoi();
            Log.d(TAG, "requestPoiResult = " + requestPoiResult);
            //TODO: if need, open it to fetch nearby data from google
//            if (requestPoiResult != 0) {
//                handleGetPoiFailedCase();
//            }
            /**
             * 0：正常发起了定位。
             * 1：服务没有启动。
             * 2：没有监听函数。
             * 6：请求间隔过短。 前后两次请求定位时间间隔不能小于1000ms。
             */
        }
    }

    /**
     * transform geo information to address and update ui
     * @param location
     * @param context
     */
    public void tranformLocation(final BDLocation location, final Context context) {
        transformGeo(location);
        transformAddress(location, context);
    }

    public void transformGeo(final BDLocation location) {
        String locString = String.format("longitude=%1$s;latitude=%2$s;altitude=%3$s;speed=%4$s;time=%5$s;geo=%6$s",
                location.getLongitude(),
                location.getLatitude(),
                location.getAltitude(),
                location.getSpeed(),
                location.getTime(),
                location.getAddrStr());

        Log.d(TAG, "bd locString = " + locString);
        if (INIT_LATITUDE != location.getLatitude()) {
            HttpClientImpl.setLocation(locString);
            if (mBDLocationlistener != null && mBDLocationlistener.get() != null) {
                mBDLocationlistener.get().onLocationSucceed(locString);
            }
        }
    }

    public void transformAddress(final BDLocation location, final Context context) {
        QiupuORM.sWorker.post(new Runnable() {
            @Override
            public void run() {
                int type = location.getLocType();
                Log.d(TAG, "location.getLocType() = " + type);
                switch (type) {
                    case BDLocation.TypeGpsLocation:
                        Address address = getLocationAddress(context, location);
                        if (address != null) {
                            final String locationInfo = LocationRequest.getAddressInfo(context, address);
                            getLocationAddress(location, locationInfo);
                        } else {
                            final String locationInfo = context.getResources().getString(R.string.location_at);
                            getLocationAddress(location, locationInfo);
                        }
//                        mLocationClient.setLocOption(buildLocationClientOption(false));
                        break;
                    case BDLocation.TypeNetWorkLocation:
                        String locationInfo =  location.getAddrStr();
                        Log.d(TAG, "========== network location.getAddrStr() = " + locationInfo);
                        if (TextUtils.isEmpty(locationInfo)) {
                            locationInfo = context.getResources().getString(R.string.location_at);
                        }
                        getLocationAddress(location, locationInfo);
//                        mLocationClient.setLocOption(buildLocationClientOption(false));
                        break;
//                    case BDLocation.TypeNetWorkException:
//                        getLocationError(type);
//                        break;
//                    case BDLocation.TypeServerError:
//                        getLocationError(type);
//                        break;
//                    case BDLocation.TypeCriteriaException:
//                        getLocationError(type);
//                        break;
//                    case BDLocation.TypeNone:
//                        getLocationError(type);
//                        break;
                    default:
                        getLocationError(type);
                        break;
                }
            }
        });
    }

    private void getLocationError(int type) {
        if (mBDLocationlistener != null && mBDLocationlistener.get() != null) {
            mBDLocationlistener.get().onLocationFailed(type);
        }
    }

    private void getLocationAddress(final BDLocation loc, final String locationInfo) {
//        Log.d(TAG, "bd locationInfo = " + locationInfo);
        final String locString = String.format("longitude=%1$s;latitude=%2$s;altitude=%3$s;speed=%4$s;time=%5$s;geo=%6$s",
                loc.getLongitude(),
                loc.getLatitude(),
                loc.getAltitude(),
                loc.getSpeed(),
                loc.getTime(),
                locationInfo);

        if (mBDLocationlistener != null && mBDLocationlistener.get() != null) {
            mBDLocationlistener.get().updateLocationWithBaiduApi(loc, locationInfo, locString);
        }
        HttpClientImpl.setLocation(locString);
    }

    public Address getLocationAddress(Context ctx, BDLocation location) {
        if(location.getLatitude() != new Location("").getLatitude()) {
            Geocoder geo = new Geocoder(ctx);//, Locale.CHINA);
            Address address = null;
            try {
                List<Address>ads = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if(ads.size() != 1)  {
                    Log.d(TAG, "Fail to get geocoded address");
                } else {
                    address = ads.get(0);
                 }
            } catch(Exception e) {
                Log.d(TAG, "get Location exception "+e.getMessage());
            }
            return address;
        }
        return null;
    }

    public String getPureMapsSearchString(Context ctx,  BDLocation location) {
        if(location == null || (location.getLatitude() == 0.0 && location.getLongitude() == 0.0)) {
            return "";
        }

        Uri mapUrl = Uri.parse(String.format("http://maps.google.com/maps"));
        Builder urlBuilder = mapUrl.buildUpon();
        urlBuilder.appendQueryParameter("q", String.format("%f,%f", location.getLatitude(), location.getLongitude()));
        return urlBuilder.toString();
    }
}
