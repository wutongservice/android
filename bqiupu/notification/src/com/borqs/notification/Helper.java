package com.borqs.notification;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

//import twitter4j.conf.ConfigurationBase;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;


public class Helper {
    
    static String LOGTAG = "Helper";

    /*
     * First look into the preference if we have a configured host
     * there, if yes use it; if not use the default one then save it.
     */
    public static String getHostName(Context context) {
        SharedPreferences p =
            PreferenceManager.getDefaultSharedPreferences(context);
        //String host = p.getString(Constants.KEY_SERVER, null);
        // directly  to get from Constants.DEFAULT_SERVER
        String host = Constants.DEFAULT_SERVER;
        Log.i("NotificationService", "host is:" + host);
        if(host == null) {
            host = Constants.DEFAULT_SERVER;
            Editor editor = p.edit();
            editor.putString(Constants.KEY_SERVER, host);
            editor.commit();
        }
        return host;
    }

    /*
     * Return user's Borqs ID by name (phone number or email, etc.)
     * @param name User's phone number or Email.
     */
    public static String getUserIdByName(String name, int type) {
        if(type != 1 && type != 2) {
            return null;
        }

        name = fixUserName(name);

        //String queryUrl = ConfigurationBase.getAPIURL()+ "account/openface/user_id?phone=" + name;
        //http://apptest0.borqs.com/account/who?login=13801190273
        String queryUrl = LinxSettings.getInstance().getApiServer() + "/account/who?login=" + name;
        Log.i("NotificationService","queryUrl is:" + queryUrl);
        
        try {
            URL url = new URL(queryUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(5000);
            conn.connect();
            int responseCode = conn.getResponseCode();
            InputStream is = conn.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            StringBuffer content = new StringBuffer();
            String line = "";
            while((line = in.readLine()) != null){
                content.append(line);
            }
            if (Constants.LOGD_ENABLED) {
                Log.w(LOGTAG, "ResponseCode:" + responseCode + "   content:" + content.toString());
            }
            
            JSONObject json = new JSONObject(content.toString());
            conn.disconnect();

            return json == null ? null : json.get("result").toString();
        }catch (Exception e){
            Log.w(LOGTAG, "report download finish exception:" + e);  
        }

        return null;
    }

    /*
     * Refine e.g. phone number or email address.
     */
    private static String fixUserName(String name) {
        String fixed = name.trim();
        if(fixed.contains("+86")){
            fixed = fixed.substring(3);
        }
        return fixed;
    }

    public static boolean download(String fileUrl) {
        if(fileUrl == null || fileUrl.length() < 5) { // 5 is a magic number.
            return false;
        }
        if(!validateExternalStorage()) {
            return false;
        }

        try{
            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(5000);
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (Constants.LOGD_ENABLED) {
                Log.d(LOGTAG, "download responseCode: " + responseCode );
            }
            if(responseCode != 200) {
                return false;
            }

            String fileName;
            int index = fileUrl.lastIndexOf("/");
            if(index <= 0) {
                fileName = "Unknown.bin";
            } else {
                fileName = fileUrl.substring(index + 1);
            }
            File file = new File(fileName);
            if(!file.exists() && !file.createNewFile()) {
                return false;
            }

            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            
            int size = 0;
            byte[] buffer = new byte[4096];
            FileOutputStream fos = new FileOutputStream(file);
            while((size = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, size);
            }
            fos.close();
            bis.close();
            conn.disconnect();
            
            return true;
        }catch (Exception e){
            Log.w(LOGTAG, "download file exception:" + e);  
        }
        return false;
    }

    //static boolean sValidated = false;
    private static boolean validateExternalStorage() {
        //String path = Constants.DEFAULT_DOWNLOAD_PATH;
        File path = new File(LinxSettings.DEFAULT_DOWNLOAD_PATH);
        if(!path.isDirectory() && !path.mkdirs()) {
            Log.w(LOGTAG, "Failed to create download folder.");  
            return false;
        }

        // TODO: calculate available free spaces.

        return true;
    }
}
