/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.app.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;

import com.borqs.account.login.provider.AccountProvider;
import com.borqs.account.login.util.BLog;

/**
 * Date: 8/9/12
 * Time: 6:41 PM
 * Borqs project
 */
public class HostLoader {
    private static String TAG = "Borqs_Account_host_config";

    static final int BEGIN_LOAD = 10;
    static final int FAILED_LOAD = 11;
    static final int FINISH_LOAD = 12;
    static final String HOST_CONFIG = "host_config.xml";
    static final String HOST_CONFIG_URL = "http://api.borqs.com/config/host_config.xml";
    static final String HOST_CONFIG_FILE = Environment.getExternalStorageDirectory().getPath() +"/borqs/" + HOST_CONFIG;
    static final String HOST_CONFIG_FILE_PATH = "file://" + HOST_CONFIG_FILE;

    private Context mContext;
    private Config mConfig;

    public HostLoader(Context context){
        mContext = context;
        mConfig = new Config(mContext);
    }
    
    public void initIfNecessary(){
        BLog.d("initIfNecessary");
        AccountProvider provider = new AccountProvider(mContext);
        if(TextUtils.isEmpty(provider.getAccountData(Config.KEY_SERVER_MODE))){
            BLog.d("Set to default release host config!");
            mConfig.setServerMode(Config.SERVER_MODE_RELEASE);            
            if(!load(HOST_CONFIG_URL, null)){
//            loader.initDefault();
            }
        }
    }

    File getLocalDebugConfigFile(){
        File hostF = new File(HOST_CONFIG_FILE);
        if (hostF == null || hostF.exists() == false) {
            return null;
        }
        return hostF;
    }

    boolean hasLocalDebugConfig(){
        return null != getLocalDebugConfigFile();
    }


    boolean load(String url, final Handler handler){
        BLog.d("Load config host from:" + url);
        boolean ret = false;
        if (handler != null)
            handler.obtainMessage(BEGIN_LOAD).sendToTarget();
        Reader stringReader = readConfigData(url);
        if(stringReader != null){
            ret = loadDebugSettingHost(stringReader, mConfig.getServerMode());
        }

        if (handler != null)
            handler.obtainMessage(ret ? FINISH_LOAD : FAILED_LOAD).sendToTarget();

        return ret;
    }

    String listConfig(){
        StringBuilder builder = new StringBuilder()
            .append("api_host=").append(mConfig.getData("api_host")).append("\n")
            .append("sync_syncml_host=").append(mConfig.getData("sync_syncml_host")).append("\n")
            .append("sync_webagent_host=").append(mConfig.getData("sync_webagent_host")).append("\n")
            .append("music_host=").append(mConfig.getData("music_host")).append("\n")
            .append("book_uri=").append(mConfig.getData("book_uri")).append("\n")
            .append("book_admin_uri=").append(mConfig.getData("book_admin_uri")).append("\n")
            .append("xdevice_host=").append(mConfig.getData("xdevice_host")).append("\n")
            .append("notification_uri=").append(mConfig.getData("notification_uri"));

        return builder.toString();
    }

    private Reader readConfigData(String configUrl){
        URLConnection conn = null;
        Reader stringReader = null;
        try {
            URL hostUrl = new URL(configUrl);
            conn = hostUrl.openConnection();
            if(conn instanceof HttpURLConnection){
                conn.setConnectTimeout(15 * 1000);
                conn.setReadTimeout(30 * 1000);
            }
            InputStream in = conn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuffer buf = new StringBuffer();
            String line;
            while (null != (line = br.readLine())) {
                buf.append(line).append("\n");
            }
            BLog.d("come from server, default configue string" + buf.toString());

            if (isNotValidXMLFile(buf.toString()) == false) {
                stringReader = new StringReader(buf.toString());
            }

            //BLog.d("save server config url=" + buf.toString());
        } catch (Exception ne) {
            ne.printStackTrace();
            BLog.d("fail to get server config=" + ne.getMessage());
        } finally {
            if (conn != null && conn instanceof HttpURLConnection) {
                ((HttpURLConnection)conn).disconnect();
            }
        }

        return stringReader;
    }

    private boolean loadDebugSettingHost(Reader reader, int mode) {
        boolean ret = false;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            db = dbf.newDocumentBuilder();

            InputSource inputSource = new InputSource(reader);
            Document doc = db.parse(inputSource);

            NodeList list = null;
            NodeList debuglist = doc.getElementsByTagName("development");
            NodeList releaselist = doc.getElementsByTagName("release");
            NodeList prerelease = doc.getElementsByTagName("prerelease");
            NodeList test = doc.getElementsByTagName("test");

            if (mode == Config.SERVER_MODE_RELEASE) {
                list = releaselist;
                BLog.d("release mode");

                if (list == null || list.getLength() == 0) {
                    BLog.d("no release settings, so use debug mode");
                    list = debuglist;
                }

            } else if(mode == Config.SERVER_MODE_DEV){
                list = debuglist;
                BLog.d("debug mode");

                if (list == null || list.getLength() == 0) {
                    BLog.d("no debug settings, so use release mode");
                    list = releaselist;
                }
            }
            else if(mode == Config.SERVER_MODE_PRE_RELEASE){
                list = prerelease;
                BLog.d("prerelease mode");

                if (list == null || list.getLength() == 0) {
                    BLog.d("no prerelease settings, so use release mode");
                    list = releaselist;
                }
            }
            else if(mode == Config.SERVER_MODE_TEST){
                list = test;
                BLog.d("test mode");

                if (list == null || list.getLength() == 0) {
                    BLog.d("no test settings, so use release mode");
                    list = releaselist;
                }
            }
            else
            {
                list = releaselist;
            }

            if (list != null && list.item(0) != null) {
                Node node = list.item(0);
                if (node == null) return false;
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    for (Node v_node = node.getFirstChild(); v_node != null; v_node = v_node.getNextSibling()) {
                        String v_name = v_node.getNodeName();
                        if (v_node.getNodeType() == Node.ELEMENT_NODE) {
                            String v_value = getChildText(v_name, (Element) v_node);

                            BLog.d(String.format("user defined configue %1$s=%2$s", v_name, v_value));

                            if (v_value.equals("")) continue;
                            if (v_name.equalsIgnoreCase("music_host")) updateHostConfiguration("music_host", v_value);
                            else if (v_name.equalsIgnoreCase("book_host")) updateHostConfiguration("book_host", v_value);
                            else if (v_name.equalsIgnoreCase("sync_host")) updateHostConfiguration("sync_host", v_value);
                            else if (v_name.equalsIgnoreCase("push_host")) updateHostConfiguration("push_host", v_value);
                            else if (v_name.equalsIgnoreCase("api_host")) updateHostConfiguration("api_host", v_value);
                            else updateHostConfiguration(v_name, v_value);
                        }
                    }
                }
            } else {
                BLog.d("no element, what to do?");
                //mConfig.initDefaultHostIfNecessary();
            }
            ret = true;
        } catch (Exception e) {
            ret = false;
            e.printStackTrace();
        }

        return ret;
    }

    private void updateHostConfiguration(String v_name, String v_value) {
        mConfig.setData(v_name, v_value);
    }


    private static boolean isNotValidXMLFile(String content)
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();

            InputSource inputSource = new InputSource(new StringReader(content));
            Document doc = db.parse(inputSource);
            doc.getElementsByTagName("release");
        } catch (Exception e) {
            e.printStackTrace();

            return true;
        }

        return false;
    }


    public static String getChildText(String str, Element elem) {
        if (elem.getChildNodes() == null || elem.getChildNodes().getLength() == 0) {
            return "";
        }
        {
            StringBuilder result = new StringBuilder();
            NodeList nlist = elem.getChildNodes();
            for (int i = 0; i < nlist.getLength(); i++) {
                Node node = nlist.item(i);
                if (node.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                    String name = node.getNodeName();
                    if (name.indexOf("#") == 0) {
                        name = name.substring(1);
                        try {
                            long uni = Long.parseLong(name);
                            char val = (char) uni;
                            result.append(val);
                        } catch (NumberFormatException ne) {
                            result.append(name);
                        }
                    }
                } else if (node.getNodeType() == Node.TEXT_NODE) {
                    result.append(node.getNodeValue());
                } else
                    result.append(node.getNodeValue() == null ? "" : node.getNodeValue());

            }
            return result.toString();
            //return elem.getElementsByTagName(str).item(0).getFirstChild().getNodeValue();
        }

    }
}
