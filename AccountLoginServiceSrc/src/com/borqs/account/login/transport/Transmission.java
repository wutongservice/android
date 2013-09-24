/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.account.login.transport;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class Transmission {
    
    private static  InputStream sendHttpRequest(String urlPath) {
        InputStream is = null;
        try {
            URL url = new URL(urlPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10 * 1000);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Charset", "utf-8");
            conn.setReadTimeout(15*1000);
            DataOutputStream dataOutStream = new DataOutputStream(conn.getOutputStream());
            dataOutStream.flush();
            dataOutStream.close();
            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
                if("gzip".equals(conn.getContentEncoding()) && !(is instanceof GZIPInputStream)){
                    is = new GZIPInputStream(is);
                }                
            }          
        } catch (Exception e) {
            e.printStackTrace();
        }
        return is;
    }
    
    private static  String responseToString(InputStream is){
        try {
            if(is == null){
                return null;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String result = new String(sb);
            result = new String(result.getBytes(), "utf-8");
            br.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String getResponseString(String url){
        return responseToString(sendHttpRequest(url));
    }

    public static Bitmap returnBitMap(String url) {
        URL imageUrl = null;
        Bitmap bitmap = null;
        try {
            imageUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection)
                    imageUrl.openConnection();
            conn.setDoInput(true);
            conn.setConnectTimeout(10 * 1000);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}
