package com.borqs.qiupu.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.account.service.LocationRequest;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.cache.QiupuHelper;

/**
 * Created with IntelliJ IDEA.
 * User: yangfeng
 * Date: 13-2-18
 * Time: 下午6:10
 * To change this template use File | Settings | File Templates.
 */
public class LocationUtils {
    private static final String TAG = "LocationUtils";

    public interface HideLocationListener {
        public void hideLocation(boolean hide);
    }

    private static final int MAX_LOCATION_SIZE = 20;

    private static String location_file = QiupuHelper.qiupu_location;
    private static String location_geo_file = QiupuHelper.qiupu_location_geo;
    private static ArrayList<String> mLocationGeos = new ArrayList<String>();
    private static ArrayList<String> mLocations = new ArrayList<String>();

    public static final String HISTORY_LOCATIONS = "history_locations";
    public static final String HISTORY_GEOS = "history_geos";
    private static final String CURRENT_LOCATION = "current_location";
    public static final String CURRENT_GEO = "current_locationGeo";
    public static final String LOCATION_ICON_KEY = "location_icon_key";

    public static void deSerializeLocation() {
        synchronized (mLocations) {
            FileInputStream fis = null;
            ObjectInputStream in = null;
            File postsFile = new File(location_file);
            File geoFile = new File(location_geo_file);

            if (postsFile.exists() && geoFile.exists()){
                try {
                    fis = new FileInputStream(location_file);
                    in = new ObjectInputStream(fis);
                    mLocations = (ArrayList<String>) in.readObject();

                    fis = new FileInputStream(geoFile);
                    in = new ObjectInputStream(fis);
                    mLocationGeos = (ArrayList<String>) in.readObject();

                } catch (IOException ex) {
                    ex.printStackTrace();
                    try {
                        new File(location_file).delete();
                    } catch (Exception ne) {
                    }

                    Log.d(TAG, "deserialization IOException fail=" + ex.getMessage());
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                    Log.d(TAG, "deserialization ClassNotFoundException fail=" + ex.getMessage());
                } finally {
                    try {
                        in.close();
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.d(TAG, "no cache for location");
                return;
            }
        }
        return;
    }

    public static void serializeLocation() {
        if (mLocations != null && mLocations.size() > 0 && mLocationGeos != null && mLocationGeos.size() > 0) {
            synchronized (mLocations) {
                FileOutputStream fos = null;
                ObjectOutputStream out = null;
                try {
                    File locationFile = new File(location_file);
                    File geoFile = new File(location_geo_file);
                    deleteExistFileAndCreateNewFile(locationFile);
                    deleteExistFileAndCreateNewFile(geoFile);

                    if (locationFile.canWrite() && geoFile.canWrite()) {
                        fos = new FileOutputStream(location_file);
                        out = new ObjectOutputStream(fos);
                        out.writeObject(mLocations);

                        fos = new FileOutputStream(location_geo_file);
                        out = new ObjectOutputStream(fos);
                        out.writeObject(mLocationGeos);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Log.d(TAG, "serialization IOException fail=" + ex.getMessage());
                } finally {
                    try {
                        out.close();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void encodeListExtra(Intent intent) {
        intent.putStringArrayListExtra(HISTORY_LOCATIONS, mLocations);
        intent.putStringArrayListExtra(HISTORY_GEOS, mLocationGeos);
    }

    public static void encodeCurrentExtra(Intent intent) {
        encodeCurrentExtra(intent, mLocations.get(0), mLocationGeos.get(0));
    }

    public static void encodeCurrentExtra(Intent intent, String location, String geo) {
        intent.putExtra(CURRENT_LOCATION, location);
        intent.putExtra(CURRENT_GEO, geo);
    }

    public static String decodeCurrentLocation(Intent intent) {
        return intent.getStringExtra(CURRENT_LOCATION);
    }

    public static String decodeCurrentGeo(Intent intent) {
        return intent.getStringExtra(CURRENT_GEO);
    }

    public static void saveLocations(String location, String locString) {
        int start = location.indexOf(">");
        int end   = location.indexOf("<", start);
        String tmp = location.substring(start + 1, end);
        ArrayList<String> tmpList = com.borqs.common.util.FileUtils.subList(mLocations);

        if (tmpList.contains(tmp)) {
            for (int i = 0; i < mLocations.size(); i++) {
                if (tmpList.get(i).equals(tmp) && i != 0){
                    String tmpInfo = mLocationGeos.get(i);
                    mLocationGeos.remove(i);
                    mLocationGeos.add(0, tmpInfo);

                    mLocations.remove(i);
                    mLocations.add(0, location);
                    break;
                }
            }
        } else {
            if (mLocations.size() == MAX_LOCATION_SIZE) {
                mLocations.remove(mLocations.size()-1);
                mLocations.add(0, location);

                mLocationGeos.remove(mLocationGeos.size() -1);
                mLocationGeos.add(0, locString);
            } else {
                mLocations.add(0, location);
                mLocationGeos.add(0, locString);
            }
        }
    }

    private static void deleteExistFileAndCreateNewFile(File file) {
        if (file.exists()) {
            file.delete();
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void activateLocationService(Context context) {
        if (QiupuConfig.IS_USE_BAIDU_LOCATION_API) {
            if (BaiduLocationProxy.LocationListener.class.isInstance(context)) {
                if (BaiduLocationProxy.getInstance(context).getLocationListener() == null) {
                    setBDLocationListener(context, (BaiduLocationProxy.LocationListener) context);
                }
            }
            Log.d(TAG, "===========> activate baidu location service, listener = " + BaiduLocationProxy.getInstance(context).getLocationListener());
            BaiduLocationProxy.getInstance(context.getApplicationContext()).start();
        } else {
            if (LocationRequest.IFLocationListener.class.isInstance(context)) {
                if (LocationRequest.instance().getLocationListener() == null) {
                    setLocationListener((LocationRequest.IFLocationListener) context);
                }
            }
            Log.d(TAG, "===========> activate google location service, listener = " + LocationRequest.instance().getLocationListener());
            LocationRequest.instance().activate(context);
        }
    }

    public static void setBDLocationListener(Context context, BaiduLocationProxy.LocationListener listener) {
        BaiduLocationProxy.getInstance(context.getApplicationContext()).setLocationListener(listener);
    }

    public static void setLocationListener(LocationRequest.IFLocationListener listener) {
        LocationRequest.instance().setLocationListener(listener);
    }

    public static void deactivateLocationService(Context context) {
        if (QiupuConfig.IS_USE_BAIDU_LOCATION_API) {
            Log.d(TAG, "===========> deactivate baidu location service");
            BaiduLocationProxy.getInstance(context.getApplicationContext()).stop();
        } else {
            Log.d(TAG, "===========> deactivate google location service");
            LocationRequest.instance().deActivate(context);
            QiupuConfig.IS_USE_BAIDU_LOCATION_API = true;
        }
    }

    public static void requestPoi(Context context) {
        BaiduLocationProxy.getInstance(context.getApplicationContext()).requestPoiInformation();
    }

    public static void addLocationRemovedListener(String key, HideLocationListener listener) {
          hideLocationListener.put(key, new WeakReference<HideLocationListener>(listener));
    }

    public static void removeLocationRemovedListener(String key) {
          hideLocationListener.remove(key);
    }

    public static void removeLocation() {
        if (hideLocationListener != null && !hideLocationListener.isEmpty()) {
            Collection<WeakReference<HideLocationListener>> listenerCollection = hideLocationListener.values();
            for (WeakReference<HideLocationListener> listenerWeakReference : listenerCollection) {
                if (null != listenerWeakReference && listenerWeakReference.get() != null) {
                    listenerWeakReference.get().hideLocation(true);
                }
            }
        }
    }

    private static HashMap<String, WeakReference<HideLocationListener>> hideLocationListener =
            new HashMap<String, WeakReference<HideLocationListener>>();

    public static class Geometry {
        private static final String TAG = "Geometry";

        public String id;
        public String icon;
        public String name;
        public String vicinity;
        public Location location;

        public boolean isBaidu = false;
        public int distance;

        public static class Location {
            public double longitude;
            public double latitude;

            public static Location parseJsonObject(JSONObject object) {
                if (null == object) {
                    return null;
                }

                JSONObject data = null;

                data = object.optJSONObject("geometry");
                if (null == data) {
                    return null;
                }
                data = data.optJSONObject("location");

                if (null == data) {
                    return null;
                }

                Location loc = new Location();
                loc.latitude = data.optDouble("lat");
                loc.longitude = data.optDouble("lng");
                return loc;
            }

            @Override
            public String toString() {
                return "latitude = " + latitude 
                        + " longitude = " + longitude;
            }
        }

        @Override
        public String toString() {
            return "name = " + name 
                    + " id = " + id
                    + " location = " + location.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Geometry)) {
                return false;
            }

            Geometry geometry = (Geometry) o;
            if (!TextUtils.isEmpty(geometry.name)) {
                return geometry.name.equals(name);
            }

            return super.equals(o);
        }

        public static Comparator<Geometry> comparator = new Comparator<Geometry>() {
            @Override
            public int compare(Geometry object1, Geometry object2) {
                return object1.distance - object2.distance;
            }
        };

        // longitude=116.622471;latitude=40.061502;altitude=4.9E-324;speed=0.0;time=2013-02-10 23:06:44;geo=北京市顺义区一经路
        public static Geometry parseGeoString(String geo) {
            if (TextUtils.isEmpty(geo) || !geo.contains("longitude=") ||
                    !geo.contains("latitude=") || !geo.contains("geo=")) {
                Log.i(TAG, "parseGeoString, skip unexpected string: " + geo);
                return null;
            }
            Geometry geometry = new Geometry();
            final int nameIndex = geo.indexOf("geo=");
            if (nameIndex >= 0) {
                int end = geo.indexOf(';', nameIndex);
                if (end > nameIndex) {
                    geometry.name = geo.substring(nameIndex + "geo=".length(), end);
                } else {
                    geometry.name = geo.substring(nameIndex + "geo=".length());
                }
            } else {
                Log.e(TAG, "parseGeoString, unexpectedly case here.");
            }

            geometry.location = new Location();
            final int latIndex = geo.indexOf("latitude=");
            if (latIndex >= 0) {
                int end = geo.indexOf(';', latIndex);
                if (end > latIndex) {
                    geometry.location.latitude = Double.parseDouble(geo.substring(latIndex + "latitude=".length(), end));
                } else {
                    geometry.location.latitude = Double.parseDouble(geo.substring(latIndex + "latitude=".length()));
                }
            } else {
                Log.e(TAG, "parseGeoString, unexpectedly case here.");
            }

            final int lngIndex = geo.indexOf("longitude=");
            if (lngIndex >= 0) {
                int end = geo.indexOf(';', lngIndex);
                if (end > lngIndex) {
                    geometry.location.longitude = Double.parseDouble(geo.substring(lngIndex + "longitude=".length(), end));
                } else {
                    geometry.location.longitude = Double.parseDouble(geo.substring(lngIndex + "longitude=".length()));
                }
            } else {
                Log.e(TAG, "parseGeoString, unexpectedly case here.");
            }

            return geometry;
        }
        public static Geometry parseJsonObject(JSONObject object) {
            if (null == object) {
                return null;
            }

            Geometry geometry = new Geometry();

            boolean useBaiduApi = false;
            if (object.has("dis")) {
                useBaiduApi = true;
            }

//            Log.d(TAG, "useBaiduPoi = " + useBaiduApi);
            if (useBaiduApi) {
                // {"addr":"北京市朝阳区酒仙桥路10号","dis":"39.720505","y":"39.989788",
                // "name":"京东方半导体","tel":"(010)64362255","x":"116.509106"}
                geometry.id = object.optString("dis");
                geometry.name = object.optString("name");
                Location loc = new Location();
                loc.longitude = object.optDouble("x");
                loc.latitude = object.optDouble("y");
                geometry.location = loc;
                geometry.vicinity = object.optString("addr");
                geometry.isBaidu = true;

                try {
                    geometry.distance = (int) Float.parseFloat(object.optString("dis"));
                } catch (Exception e) {}

            } else {
                geometry.id = object.optString("id");
                geometry.icon = object.optString("icon");
                geometry.name = object.optString("name");
                geometry.location = Location.parseJsonObject(object);
                geometry.vicinity = object.optString("vicinity");
                geometry.isBaidu = false;
            }

            return geometry;
        }

        private static final String LOC_FORMATTER = "<a href='http://maps.google.com/maps?q=%1$S,%2$S'>%3$S</a>";
        private static final String GEO_FORMATTER = "longitude=%2$s;latitude=%1$s;altitude=4.9E-324;speed=0.0;time=2013-02-10 23:06:44;geo=%3$s";
        public String getLocationString() {
            if (null == location) {
                return null;
            }

            final String str = String.format(LOC_FORMATTER, location.latitude, location.longitude, name);
            return str;
        }

        public String getGeoString() {
            if (null == location) {
                return null;
            }

            final String str = String.format(GEO_FORMATTER, location.latitude, location.longitude, name);
            return str;
        }

        private static final String MAP_URL_FORMATTER =
                "http://maps.googleapis.com/maps/api/staticmap?" +
                "center=%1$s&zoom=13&size=600x300&maptype=roadmap" +
                "&markers=color:%2$s|label:S|%3$s,%4$s&language=%5$s&sensor=false&format=jpg";

        private static final String BAIDU_MAP_URL_FORMATTER = 
                "http://api.map.baidu.com/staticimage?" +
                "width=400&height=240&center=%1$s,%2$s&zoom=15&markers=%3$s";

        // &markerStyles=m,Y,0x398aff  &markerStyles=m,Y,0x0000FF
        // "http://api.map.baidu.com/staticimage?width=400&height=300&center=116.403874,39.914889&zoom=11";

        public static String getStaticMapUrl(Geometry geometry, String markColor, boolean withBaidu) {
            String str = "";
            if (withBaidu) {
                str = formatBaiduMapUrl(geometry);
            } else {
                str = formatGoogleMapUrl(geometry, markColor);
            }
            return str;
        }

        private static String formatGoogleMapUrl(Geometry geometry, String markColor) {
            return String.format(MAP_URL_FORMATTER, geometry.name, markColor,
                    geometry.location.latitude, geometry.location.longitude,
                    Locale.getDefault().toString());
        }

        private static String formatBaiduMapUrl(Geometry geometry) {
            try {
                return String.format(BAIDU_MAP_URL_FORMATTER,
                        geometry.location.longitude, geometry.location.latitude,
                        URLEncoder.encode(geometry.name, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
        }

        public static String getStaticMapUrl(String geo, boolean withBaidu) {
            Geometry geometry = Geometry.parseGeoString(geo);
            return getStaticMapUrl(geometry, "blue", withBaidu);
        }

        /** 
         * with Baidu api, output = json or xml
         */
        private static final String BAIDU_PLACE_URL_FORMATTER = 
                "http://api.map.baidu.com/place/search?&query=%1$s" + 
                "&location=%2$s,%3$s&radius=4000&output=json&key=" + QiupuConfig.BAIDU_MAP_APPKEY;

        /**
         *  with Google api, return json
         */
        private static final String PLACE_URL_FORMATTER =
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=%1$s,%2$s&radius=500&language=%3$s&sensor=false" +
                        "&key=" + QiupuConfig.GOOGLE_MAP_APPKEY;

        private static String formatGoogleUrl(Geometry geometry) {
            return String.format(PLACE_URL_FORMATTER,
                    geometry.location.latitude, geometry.location.longitude,
                    Locale.getDefault().toString());
        }

        private static String formatBaiduUrl(Geometry geometry) {
            try {
                return String.format(BAIDU_PLACE_URL_FORMATTER, URLEncoder.encode(geometry.name, "UTF-8"),
                        geometry.location.latitude, geometry.location.longitude);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
        }

        /**
         * @param geometry
         * @param useBaiduApi, always false
         * @return
         */
        public static String getPlaceQueryUrl(Geometry geometry) {
            String str = "";
//            if (false) { // don't use baidu place api: searchnearby
//                str = formatBaiduUrl(geometry);
//            } else {
                str = formatGoogleUrl(geometry);
//            }
            return str;
        }

//        public static String getPlaceUrlFormatter(String geo) {
//            Geometry geometry = Geometry.parseGeoString(geo);
//            return getPlaceQueryUrl(geometry);
//        }

    }

    public static String downloadUrl(String myurl) throws IOException{
        InputStream is = null;
        Log.d(TAG, ">>>>>>>>>>>>>> myurl = " + myurl);
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(20000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response code(200 is OK) is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string.
            String connectAsString = convertStreamToString(is);
            return connectAsString;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "downloadUrl, exception = " + myurl);
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (Exception e){
                }
            }
        }
        return "";
    }

    public static String convertStreamToString(InputStream stream) {
        Reader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[4096]; // set buffer as 4kb
            int count;
            while ((count = reader.read(buffer)) > 0) {
                sb.append(buffer, 0, count);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return sb.toString();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
    *
    * Google Web Service Json
    * {
   "geometry" : {
   "location" : {
   "lat" : -33.85998270,
   "lng" : 151.20212820
   },
   "viewport" : {
   "northeast" : {
   "lat" : -33.85525840,
   "lng" : 151.20314010
   },
   "southwest" : {
   "lat" : -33.86578950,
   "lng" : 151.20001230
   }
   }
   },
   "icon" : "http://maps.gstatic.com/mapfiles/place_api/icons/geocode-71.png",
   "id" : "92f1bbd4ecab8e9add032bccee40a57a8dfd42b4",
   "name" : "Barangaroo",
   "reference" : "CqQBkQAAAMQ425fwrc1No5hbgR3xg0wZplCeVocvFjA8A4eYThVddoQfZO0CxDZJywX_hd7QW6fpU-7bjzpsVcB5H6O9AYHTi7y9qNCn-ppEUrUTZAsz9J-SI8PhTSfRps4I9JriQBs1GadxVrv85cSTAO6KklSCDReZwIu9pLz1VhNcKBqWWad-CUqsFd3LkwEEvkbaW0QQB1aP1zed2w1h9gjDDIESEBT6dlhfF9eb8_bYK_PoWBIaFNfyOEerFJkn_2sdMLxTX73T0GY1",
   "types" : [ "locality", "political" ],
   "vicinity" : "Barangaroo"
   }
    */
   private static boolean parsePlaceResult(JSONObject object, ArrayList<Geometry> geometries) {
       boolean modified = false;
       if (null != object) {
           Geometry geometry = Geometry.parseJsonObject(object);
           if (null != geometry) {
               if (!geometries.contains(geometry)) {
                   geometries.add(geometry);
               }
               modified = true;
           }
       }

       return modified;
   }

   /**
    * Baidu Web Service Json 
    * {
    "status":"OK",
    "results":[
        {
            "name":"酒仙桥中路",
            "location":{
                "lat":39.982484,
                "lng":116.496348
            },
            "address":"北京市朝阳区",
            "uid":"8232a30444453215c256ac2c",
            "detail_url":"http://api.map.baidu.com/place/detail?uid=8232a30444453215c256ac2c&output=html&source=placeapi"
        },
        {
            "name":"酒仙桥中路/酒仙桥路(路口)",
            "location":{
                "lat":39.982486,
                "lng":116.496251
            },
            "address":"北京市朝阳区",
            "uid":"2310f004fb1be0ef2d47336d",
            "detail_url":"http://api.map.baidu.com/place/detail?uid=2310f004fb1be0ef2d47336d&output=html&source=placeapi"
        }
        ]
      }
    */
   public static boolean parsePlaceListResult(String response, ArrayList<Geometry> geometries) {
       if(QiupuConfig.DBLOGD)Log.d(TAG, "parsePlaceListResult, parsing: " + response);
       boolean modified = false;
       try {
           JSONObject object = new JSONObject(response);
           if (null != object) {
               JSONArray array = null;
               if (object.has("results")) {
                   array = object.optJSONArray("results");
               } else {
                   array = object.optJSONArray("p");
               }

               final int size = null == array ? 0 : array.length();
               for (int i = 0; i < size; ++i) {
                   object = array.optJSONObject(i);
                   if (parsePlaceResult(object, geometries)) {
                       modified = true;
                   }
               }
           }

           return modified;
       } catch (JSONException e) {
           e.printStackTrace();
           return modified;
       }
   }

   /**
    * Baidu Poi Json
    * {"p":
    * [
    *  {
    *   "dis":"159.677666",
    *     "y":"39.988984",
    *  "name":"星科大厦B座",
    *     "x":"116.507658"
    *  },
    *  
    *  {
    *   "dis":"289.095555",
    *     "y":"39.98273",
    *  "name":"锦江之星北京酒仙桥店",
    *     "x":"116.499905"
    *  }
    *  ]
    */
   
}
